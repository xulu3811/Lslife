import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth, optionalAuth } from '../middleware/auth.js';
import { moderateContent } from '../services/moderation.js';

const router = Router();

const MONTHLY_LIMIT: Record<string, number> = { free: 3, vip: 20, premium: 50 };

const ALLOWED_CATEGORIES = [
  'second_hand',
  'job',
  'house',
  'housekeeping',
  'maintenance',
  'moving',
  'veggies',
] as const;

function deriveTitle(title: string | null | undefined, description: string): string {
  const t = title?.trim();
  if (t) return t.slice(0, 60);
  const line = description
    .split(/\r?\n/)
    .map((s) => s.trim())
    .find(Boolean);
  if (!line) return '闲置好物';
  return line.slice(0, 60);
}

/** 发布信息（会员限额 + 内容审核 + 真实入库） */
router.post(
  '/',
  requireAuth,
  asyncHandler(async (req, res) => {
    const body = z
      .object({
        category: z.enum(ALLOWED_CATEGORIES),
        title: z.string().max(60).nullable().optional(),
        description: z.string().min(1).max(2000),
        price: z.number().nonnegative().optional().nullable(),
        images: z.array(z.string().min(8).max(500)).max(9).default([]),
        latitude: z.number().nullable().optional(),
        longitude: z.number().nullable().optional(),
        locationName: z.string().max(80).nullable().optional(),
        publisherType: z.enum(['INDIVIDUAL', 'MERCHANT']).default('INDIVIDUAL'),
        merchantId: z.string().nullable().optional(),
        listingType: z.enum(['GOODS', 'SERVICE']).default('GOODS'),
        attributes: z.record(z.string(), z.string()).optional().default({}),
      })
      .parse(req.body);

    // 闲置类建议至少 1 张图（贴近闲鱼）
    if (body.category === 'second_hand' && body.images.length === 0) {
      throw new ApiError(400, '个人闲置请至少上传1张图片');
    }

    const user = await prisma.user.findUnique({ where: { id: req.userId! } });
    if (!user) throw new ApiError(404, '用户不存在');

    if (body.publisherType === 'MERCHANT' && body.merchantId) {
      // 验证商家归属
      // 注意：这里假设系统通过管理员分配merchant给user，或者需要额外的关联表
      // 目前没有直接的 user-merchant 表，暂定不阻断，或者未来加入 merchant.ownerId
      // 这里可以先验证 merchant 是否存在
      const merchant = await prisma.merchant.findUnique({ where: { id: body.merchantId } });
      if (!merchant) throw new ApiError(404, '指定的商家不存在');
    }

    const monthStart = new Date();
    monthStart.setDate(1);
    monthStart.setHours(0, 0, 0, 0);
    const count = await prisma.post.count({
      where: { userId: user.id, createdAt: { gte: monthStart }, status: { not: 'rejected' } },
    });
    const limit = MONTHLY_LIMIT[user.membershipTier] ?? 3;
    if (count >= limit) {
      throw new ApiError(403, `本月发布额度已用尽 (${count}/${limit}), 升级会员可提升额度`);
    }

    const title = deriveTitle(body.title, body.description);
    const moderation = moderateContent(title, body.description);
    if (!moderation.pass && moderation.status === 'rejected') {
      throw new ApiError(400, `内容审核未通过: ${moderation.note}`);
    }

    const post = await prisma.post.create({
      data: {
        userId: user.id,
        publisherType: body.publisherType,
        merchantId: body.publisherType === 'MERCHANT' ? body.merchantId : null,
        listingType: body.listingType,
        category: body.category,
        title,
        description: body.description.trim(),
        price: body.price ?? null,
        images: JSON.stringify(body.images),
        latitude: body.latitude,
        longitude: body.longitude,
        locationName: body.locationName ?? '连山壮族瑶族自治县',
        attributes: JSON.stringify(body.attributes),
        status: moderation.status,
        reviewNote: moderation.note,
      },
    });

    const message =
      moderation.status === 'published'
        ? '发布成功'
        : moderation.status === 'pending_review'
          ? '已提交，等待审核'
          : '已处理';

    return ok(
      res,
      {
        ...post,
        images: JSON.parse(post.images) as string[],
      },
      message,
    );
  }),
);

/** 信息流 / 我的发布 */
router.get(
  '/',
  optionalAuth,
  asyncHandler(async (req, res) => {
    const { category, publisherType, listingType, mine, page, pageSize, q, minPrice, maxPrice, sortBy } = z
      .object({
        category: z.string().optional(),
        publisherType: z.enum(['INDIVIDUAL', 'MERCHANT']).optional(),
        listingType: z.enum(['GOODS', 'SERVICE']).optional(),
        mine: z.string().optional().transform(v => v === 'true'),
        page: z.coerce.number().min(1).default(1),
        pageSize: z.coerce.number().min(1).max(50).default(20),
        q: z.string().optional(),
        minPrice: z.coerce.number().nonnegative().optional(),
        maxPrice: z.coerce.number().nonnegative().optional(),
        sortBy: z.enum(['latest', 'price_asc', 'price_desc']).default('latest'),
      })
      .parse(req.query);

    const where: Record<string, unknown> = { status: 'published' };
    if (category && category !== 'all') where.category = category;
    if (publisherType) where.publisherType = publisherType;
    if (listingType) where.listingType = listingType;
    if (mine) {
      if (!req.userId) throw new ApiError(401, '未登录');
      where.userId = req.userId;
      delete where.status; // 我的发布看全部状态
    }

    if (q) {
      where.OR = [
        { title: { contains: q } },
        { description: { contains: q } }
      ];
    }

    if (minPrice !== undefined || maxPrice !== undefined) {
      where.price = {
        ...(minPrice !== undefined && { gte: minPrice }),
        ...(maxPrice !== undefined && { lte: maxPrice }),
      };
    }

    let orderBy: any = { createdAt: 'desc' };
    if (sortBy === 'price_asc') {
      orderBy = { price: 'asc' };
    } else if (sortBy === 'price_desc') {
      orderBy = { price: 'desc' };
    }

    const [total, posts] = await Promise.all([
      prisma.post.count({ where }),
      prisma.post.findMany({
        where,
        orderBy,
        skip: (page - 1) * pageSize,
        take: pageSize,
        include: {
          user: { select: { nickname: true, avatar: true } },
          merchant: { select: { name: true, logo: true, status: true } },
        },
      }),
    ]);

    return ok(res, {
      total,
      page,
      pageSize,
      list: posts.map((p) => ({
        ...p,
        images: JSON.parse(p.images) as string[],
        attributes: JSON.parse(p.attributes) as Record<string, string>,
      })),
    });
  }),
);

router.get(
  '/quota',
  requireAuth,
  asyncHandler(async (req, res) => {
    const user = await prisma.user.findUnique({ where: { id: req.userId! } });
    if (!user) throw new ApiError(404, '用户不存在');
    const monthStart = new Date();
    monthStart.setDate(1);
    monthStart.setHours(0, 0, 0, 0);
    const used = await prisma.post.count({
      where: { userId: user.id, createdAt: { gte: monthStart }, status: { not: 'rejected' } },
    });
    const limit = MONTHLY_LIMIT[user.membershipTier] ?? 3;
    return ok(res, { used, limit, tier: user.membershipTier, remaining: Math.max(0, limit - used) });
  }),
);

router.get(
  '/:id',
  optionalAuth,
  asyncHandler(async (req, res) => {
    const post = await prisma.post.findUnique({
      where: { id: req.params.id },
      include: {
        user: { select: { nickname: true, avatar: true, phone: true } },
        merchant: { select: { name: true, logo: true, phone: true } },
      },
    });
    if (!post) throw new ApiError(404, '帖子不存在');
    if (post.status !== 'published' && post.userId !== req.userId) {
      throw new ApiError(404, '帖子不存在');
    }
    return ok(res, {
      ...post,
      images: JSON.parse(post.images) as string[],
      attributes: JSON.parse(post.attributes) as Record<string, string>,
    });
  }),
);

router.put(
  '/:id',
  requireAuth,
  asyncHandler(async (req, res) => {
    const post = await prisma.post.findUnique({ where: { id: req.params.id } });
    if (!post) throw new ApiError(404, '帖子不存在');
    if (post.userId !== req.userId) throw new ApiError(403, '无权修改此帖子');

    const body = z
      .object({
        category: z.enum(ALLOWED_CATEGORIES),
        title: z.string().max(60).nullable().optional(),
        description: z.string().min(1).max(2000),
        price: z.number().nonnegative().optional().nullable(),
        images: z.array(z.string().min(8).max(500)).max(9).default([]),
        latitude: z.number().nullable().optional(),
        longitude: z.number().nullable().optional(),
        locationName: z.string().max(80).nullable().optional(),
        attributes: z.record(z.string(), z.string()).optional().default({}),
      })
      .parse(req.body);

    if (body.category === 'second_hand' && body.images.length === 0) {
      throw new ApiError(400, '个人闲置请至少上传1张图片');
    }

    const title = deriveTitle(body.title, body.description);
    const moderation = moderateContent(title, body.description);
    if (!moderation.pass && moderation.status === 'rejected') {
      throw new ApiError(400, `内容审核未通过: ${moderation.note}`);
    }

    const updatedPost = await prisma.post.update({
      where: { id: post.id },
      data: {
        category: body.category,
        title,
        description: body.description.trim(),
        price: body.price ?? null,
        images: JSON.stringify(body.images),
        latitude: body.latitude,
        longitude: body.longitude,
        locationName: body.locationName ?? '连山壮族瑶族自治县',
        attributes: JSON.stringify(body.attributes),
        status: moderation.status,
        reviewNote: moderation.note,
      },
    });

    return ok(
      res,
      { ...updatedPost, images: JSON.parse(updatedPost.images) as string[] },
      '修改成功，已重新提交审核',
    );
  }),
);

router.put(
  '/:id/status',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { status } = z.object({ status: z.enum(['removed', 'pending_review']) }).parse(req.body);
    const post = await prisma.post.findUnique({ where: { id: req.params.id } });
    if (!post) throw new ApiError(404, '帖子不存在');
    if (post.userId !== req.userId) throw new ApiError(403, '无权操作此帖子');

    const updatedPost = await prisma.post.update({
      where: { id: post.id },
      data: { status },
    });

    return ok(res, updatedPost, status === 'removed' ? '已成功下架' : '已重新提交审核');
  }),
);

router.delete(
  '/:id',
  requireAuth,
  asyncHandler(async (req, res) => {
    const post = await prisma.post.findUnique({ where: { id: req.params.id } });
    if (!post) throw new ApiError(404, '帖子不存在');
    if (post.userId !== req.userId) throw new ApiError(403, '无权删除此帖子');

    await prisma.post.delete({ where: { id: post.id } });
    return ok(res, null, '删除成功');
  }),
);

export default router;
