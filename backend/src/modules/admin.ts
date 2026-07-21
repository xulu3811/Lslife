import { Router } from 'express';
import { z } from 'zod';
import bcrypt from 'bcryptjs';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAdminAuth } from '../middleware/auth.js';
import { signToken } from '../lib/jwt.js';

const router = Router();

/** 管理后台登录（公开） */
router.post(
  '/login',
  asyncHandler(async (req, res) => {
    const { username, password } = z
      .object({
        username: z.string().min(1),
        password: z.string().min(1),
      })
      .parse(req.body);

    const admin = await prisma.adminUser.findUnique({ where: { username } });
    if (!admin) throw new ApiError(401, '管理账号不存在或密码错误');

    const isValid = await bcrypt.compare(password, admin.password);
    if (!isValid) throw new ApiError(401, '管理账号不存在或密码错误');

    await prisma.adminUser.update({
      where: { id: admin.id },
      data: {
        lastLogin: new Date(),
        lastIp: req.ip || 'unknown',
      },
    });

    const token = signToken({ sub: admin.id, role: admin.role, isAdmin: true });
    return ok(res, { token, user: { username: admin.username, role: admin.role } }, '登录成功');
  }),
);

/** 以下接口一律要求管理员 JWT */
router.use(requireAdminAuth);

router.get(
  '/dashboard',
  asyncHandler(async (_req, res) => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const newUsers = await prisma.user.count({ where: { createdAt: { gte: today } } });
    const activeOrders = await prisma.order.count({ where: { status: { notIn: ['cancelled', 'delivered'] } } });

    const payments = await prisma.payment.aggregate({
      _sum: { amount: true },
      where: { status: 'success', paidAt: { gte: today } },
    });

    const pendingReviews = await prisma.post.count({ where: { status: 'pending_review' } });

    return ok(res, {
      newUsers,
      activeOrders,
      revenue: payments._sum.amount || 0,
      pendingReviews,
    });
  }),
);

router.get(
  '/posts',
  asyncHandler(async (req, res) => {
    const { status } = z.object({ status: z.string().optional() }).parse(req.query);
    const where = status ? { status } : {};

    const posts = await prisma.post.findMany({
      where,
      orderBy: { createdAt: 'desc' },
      include: {
        user: { select: { nickname: true, phone: true } },
      },
    });

    return ok(
      res,
      posts.map((p) => ({ ...p, images: JSON.parse(p.images) })),
    );
  }),
);

router.post(
  '/posts/:id/audit',
  asyncHandler(async (req, res) => {
    const id = req.params.id;
    const { action, note } = z
      .object({
        action: z.enum(['approve', 'reject']),
        note: z.string().optional(),
      })
      .parse(req.body);

    const post = await prisma.post.findUnique({ where: { id } });
    if (!post) throw new ApiError(404, '帖子不存在');

    const newStatus = action === 'approve' ? 'published' : 'rejected';

    await prisma.post.update({
      where: { id },
      data: {
        status: newStatus,
        reviewNote: note || (action === 'approve' ? '人工审核通过' : '人工审核拒绝'),
      },
    });

    return ok(res, null, `内容已${action === 'approve' ? '通过发布' : '驳回'}`);
  }),
);

router.get(
  '/kyc',
  asyncHandler(async (req, res) => {
    const { status } = z.object({ status: z.string().optional().default('pending') }).parse(req.query);
    const users = await prisma.user.findMany({
      where: { realNameStatus: status },
      orderBy: { updatedAt: 'desc' },
      select: {
        id: true,
        phone: true,
        nickname: true,
        realName: true,
        idCardHash: true,
        realNameStatus: true,
        updatedAt: true,
      },
    });

    return ok(res, users);
  }),
);

router.post(
  '/kyc/:id/audit',
  asyncHandler(async (req, res) => {
    const id = req.params.id;
    const { action } = z.object({ action: z.enum(['approve', 'reject']) }).parse(req.body);

    const user = await prisma.user.findUnique({ where: { id } });
    if (!user) throw new ApiError(404, '用户不存在');
    if (user.realNameStatus !== 'pending') throw new ApiError(400, '用户当前无需审核');

    if (action === 'approve') {
      await prisma.user.update({ where: { id }, data: { realNameStatus: 'verified' } });
    } else {
      await prisma.user.update({
        where: { id },
        data: { realNameStatus: 'none', realName: null, idCardHash: null },
      });
    }

    return ok(res, null, `实名认证已${action === 'approve' ? '通过' : '驳回'}`);
  }),
);

router.get(
  '/users',
  asyncHandler(async (req, res) => {
    const { search } = z.object({ search: z.string().optional() }).parse(req.query);

    let where = {};
    if (search) {
      where = {
        OR: [{ phone: { contains: search } }, { nickname: { contains: search } }],
      };
    }

    const users = await prisma.user.findMany({
      where,
      orderBy: { createdAt: 'desc' },
      select: {
        id: true,
        phone: true,
        nickname: true,
        membershipTier: true,
        walletBalance: true,
        realNameStatus: true,
        createdAt: true,
      },
    });

    return ok(res, users);
  }),
);

router.put(
  '/users/:id/balance',
  asyncHandler(async (req, res) => {
    const id = req.params.id;
    const { amount } = z.object({ amount: z.number() }).parse(req.body);

    const user = await prisma.user.update({
      where: { id },
      data: { walletBalance: { increment: amount } },
    });

    return ok(res, user, `成功为用户充值/扣减 ${amount} 元`);
  }),
);

router.put(
  '/users/:id/membership',
  asyncHandler(async (req, res) => {
    const id = req.params.id;
    const { tier } = z.object({ tier: z.enum(['free', 'vip', 'premium']) }).parse(req.body);

    const user = await prisma.user.update({
      where: { id },
      data: { membershipTier: tier },
    });

    return ok(res, user, `已将用户设为 ${tier} 会员`);
  }),
);

export default router;
