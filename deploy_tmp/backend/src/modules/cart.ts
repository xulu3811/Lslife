import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth } from '../middleware/auth.js';

const router = Router();
router.use(requireAuth);

/** 获取购物车 (按商家和卖家分组) */
router.get(
  '/',
  asyncHandler(async (req, res) => {
    const items = await prisma.cartItem.findMany({
      where: { userId: req.userId! },
      include: {
        product: { include: { merchant: true } },
        post: { include: { user: true } },
      },
    });
    return ok(res, items.map((c) => ({
      id: c.id,
      quantity: c.quantity,
      merchantId: c.merchantId,
      sellerId: c.sellerId,
      product: c.product,
      post: c.post,
    })));
  }),
);

/** 添加/更新购物车项 (quantity<=0 则删除) */
router.post(
  '/',
  asyncHandler(async (req, res) => {
    const { productId, postId, quantity } = z
      .object({ 
        productId: z.string().optional(),
        postId: z.string().optional(),
        quantity: z.number().int() 
      })
      .parse(req.body);

    if (!productId && !postId) throw new ApiError(400, '必须提供 productId 或 postId');

    let merchantId = null;
    let sellerId = null;

    if (productId) {
      const product = await prisma.product.findUnique({ where: { id: productId } });
      if (!product) throw new ApiError(404, '商品不存在');
      merchantId = product.merchantId;
    } else if (postId) {
      const post = await prisma.post.findUnique({ where: { id: postId } });
      if (!post) throw new ApiError(404, '闲置商品不存在');
      sellerId = post.userId;
    }

    if (quantity <= 0) {
      await prisma.cartItem.deleteMany({ 
        where: { 
          userId: req.userId!, 
          ...(productId ? { productId } : {}),
          ...(postId ? { postId } : {})
        } 
      });
      return ok(res, { removed: true });
    }

    // Because there's no unique constraint on (userId, productId, postId) since they can be null,
    // we use findFirst then update or create
    const existing = await prisma.cartItem.findFirst({
      where: {
        userId: req.userId!,
        ...(productId ? { productId } : { postId }) // strict match
      }
    });

    let item;
    if (existing) {
      item = await prisma.cartItem.update({
        where: { id: existing.id },
        data: { quantity }
      });
    } else {
      item = await prisma.cartItem.create({
        data: {
          userId: req.userId!,
          productId,
          postId,
          merchantId,
          sellerId,
          quantity
        }
      });
    }

    return ok(res, item);
  }),
);

/** 清空某商家、个人卖家或整个购物车 */
router.delete(
  '/',
  asyncHandler(async (req, res) => {
    const { merchantId, sellerId } = z.object({ 
      merchantId: z.string().optional(),
      sellerId: z.string().optional()
    }).parse(req.query);
    
    await prisma.cartItem.deleteMany({ 
      where: { 
        userId: req.userId!, 
        ...(merchantId ? { merchantId } : {}),
        ...(sellerId ? { sellerId } : {})
      } 
    });
    return ok(res, { cleared: true });
  }),
);

export default router;
