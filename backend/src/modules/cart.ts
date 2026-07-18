import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth } from '../middleware/auth.js';

const router = Router();
router.use(requireAuth);

/** 获取购物车 (按商家分组) */
router.get(
  '/',
  asyncHandler(async (req, res) => {
    const items = await prisma.cartItem.findMany({
      where: { userId: req.userId! },
      include: { product: { include: { merchant: true } } },
    });
    return ok(res, items.map((c) => ({
      id: c.id,
      quantity: c.quantity,
      merchantId: c.merchantId,
      product: c.product,
    })));
  }),
);

/** 添加/更新购物车项 (quantity<=0 则删除) */
router.post(
  '/',
  asyncHandler(async (req, res) => {
    const { productId, quantity } = z
      .object({ productId: z.string(), quantity: z.number().int() })
      .parse(req.body);

    const product = await prisma.product.findUnique({ where: { id: productId } });
    if (!product) throw new ApiError(404, '商品不存在');

    if (quantity <= 0) {
      await prisma.cartItem.deleteMany({ where: { userId: req.userId!, productId } });
      return ok(res, { removed: true });
    }

    const item = await prisma.cartItem.upsert({
      where: { userId_productId: { userId: req.userId!, productId } },
      update: { quantity },
      create: { userId: req.userId!, productId, merchantId: product.merchantId, quantity },
    });
    return ok(res, item);
  }),
);

/** 清空某商家或整个购物车 */
router.delete(
  '/',
  asyncHandler(async (req, res) => {
    const { merchantId } = z.object({ merchantId: z.string().optional() }).parse(req.query);
    await prisma.cartItem.deleteMany({ where: { userId: req.userId!, ...(merchantId ? { merchantId } : {}) } });
    return ok(res, { cleared: true });
  }),
);

export default router;
