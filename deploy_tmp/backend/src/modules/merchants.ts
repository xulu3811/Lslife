import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';

const router = Router();

function serialize(m: { tags: string } & Record<string, unknown>) {
  return { ...m, tags: JSON.parse(m.tags) as string[] };
}

/** 商家列表 (支持分类/搜索/排序/分页) */
router.get(
  '/',
  asyncHandler(async (req, res) => {
    const { category, q, sort, page, pageSize } = z
      .object({
        category: z.string().optional(),
        q: z.string().optional(),
        sort: z.enum(['default', 'distance', 'sales', 'rating']).default('default'),
        page: z.coerce.number().min(1).default(1),
        pageSize: z.coerce.number().min(1).max(50).default(20),
      })
      .parse(req.query);

    const where: Record<string, unknown> = { status: 'active' };
    if (category && category !== 'all') where.category = category;
    if (q) where.OR = [{ name: { contains: q } }, { description: { contains: q } }];

    const orderBy =
      sort === 'distance'
        ? { distance: 'asc' as const }
        : sort === 'sales'
          ? { sales: 'desc' as const }
          : sort === 'rating'
            ? { rating: 'desc' as const }
            : { sales: 'desc' as const };

    const [total, merchants] = await Promise.all([
      prisma.merchant.count({ where }),
      prisma.merchant.findMany({
        where,
        orderBy,
        skip: (page - 1) * pageSize,
        take: pageSize,
        include: { products: true },
      }),
    ]);

    return ok(res, {
      total,
      page,
      pageSize,
      list: merchants.map((m) => ({ ...serialize(m), items: m.products })),
    });
  }),
);

/** 智能推荐 (评分 Top N) */
router.get(
  '/recommended',
  asyncHandler(async (_req, res) => {
    const merchants = await prisma.merchant.findMany({ orderBy: { rating: 'desc' }, take: 3, include: { products: true } });
    return ok(res, merchants.map((m) => ({ ...serialize(m), items: m.products })));
  }),
);

/** 商家详情 (兼容 externalId 与 cuid) */
router.get(
  '/:id',
  asyncHandler(async (req, res) => {
    const { id } = req.params;
    const merchant = await prisma.merchant.findFirst({
      where: { OR: [{ id }, { externalId: id }] },
      include: { products: true },
    });
    if (!merchant) throw new ApiError(404, '商家不存在');
    return ok(res, { ...serialize(merchant), items: merchant.products });
  }),
);

export default router;
