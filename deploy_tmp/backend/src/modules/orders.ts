import { Router } from 'express';
import { z } from 'zod';
import { customAlphabet } from 'nanoid';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth } from '../middleware/auth.js';
import { computeLiveDelivery } from '../services/delivery.js';

const router = Router();
router.use(requireAuth);

const genOrderNo = customAlphabet('0123456789', 6);

/** 创建订单 (服务端计算金额, 防止客户端篡改) */
router.post(
  '/',
  asyncHandler(async (req, res) => {
    const { merchantId, sellerId, items, deliveryAddress } = z
      .object({
        merchantId: z.string().optional(),
        sellerId: z.string().optional(),
        items: z.array(z.object({ 
          productId: z.string().optional(), 
          postId: z.string().optional(),
          quantity: z.number().int().positive() 
        })).min(1),
        deliveryAddress: z.object({ name: z.string(), phone: z.string(), address: z.string() }),
      })
      .parse(req.body);

    if (!merchantId && !sellerId) throw new ApiError(400, '必须指定商家或卖家');

    let shopName = '未知店铺';
    let shopLogo = '';
    let baseDeliveryFee = 0;

    if (merchantId) {
      const merchant = await prisma.merchant.findFirst({ where: { OR: [{ id: merchantId }, { externalId: merchantId }] } });
      if (!merchant) throw new ApiError(400, '商家不存在');
      shopName = merchant.name;
      shopLogo = merchant.logo;
      baseDeliveryFee = merchant.deliveryFee;
    } else if (sellerId) {
      const seller = await prisma.user.findUnique({ where: { id: sellerId } });
      if (!seller) throw new ApiError(400, '卖家不存在');
      shopName = seller.nickname;
      shopLogo = seller.avatar || '';
      baseDeliveryFee = 0; // C2C posts have free/negotiated delivery fee by default
    }

    let itemsTotal = 0;
    const orderItemsData: any[] = [];

    for (const item of items) {
      if (item.productId) {
        const p = await prisma.product.findUnique({ where: { id: item.productId } });
        if (!p) throw new ApiError(400, '存在无效商品');
        itemsTotal += p.price * item.quantity;
        orderItemsData.push({ productId: p.id, name: p.name, price: p.price, quantity: item.quantity, image: p.image });
      } else if (item.postId) {
        const p = await prisma.post.findUnique({ where: { id: item.postId } });
        if (!p) throw new ApiError(400, '存在无效商品');
        const price = p.price || 0;
        itemsTotal += price * item.quantity;
        // Parse the first image for the order item
        let image = '';
        try {
          const imgs = JSON.parse(p.images);
          if (Array.isArray(imgs) && imgs.length > 0) image = imgs[0];
        } catch(e) {}
        orderItemsData.push({ postId: p.id, name: p.title, price, quantity: item.quantity, image });
      }
    }

    const totalAmount = itemsTotal + baseDeliveryFee;

    const order = await prisma.order.create({
      data: {
        orderNo: 'LS' + genOrderNo(),
        userId: req.userId!,
        merchantId,
        sellerId,
        merchantName: shopName,
        merchantLogo: shopLogo,
        itemsTotal,
        deliveryFee: baseDeliveryFee,
        totalAmount,
        status: 'pending',
        deliveryName: deliveryAddress.name,
        deliveryPhone: deliveryAddress.phone,
        deliveryAddress: deliveryAddress.address,
        items: { create: orderItemsData },
      },
      include: { items: true },
    });

    return ok(res, order, '订单创建成功, 待支付');
  }),
);

/** 订单列表 */
router.get(
  '/',
  asyncHandler(async (req, res) => {
    const orders = await prisma.order.findMany({
      where: { userId: req.userId! },
      orderBy: { createdAt: 'desc' },
      include: { items: true },
    });
    return ok(res, orders);
  }),
);

/** 订单详情 + 实时配送 */
router.get(
  '/:id',
  asyncHandler(async (req, res) => {
    const order = await prisma.order.findFirst({
      where: { id: req.params.id, userId: req.userId! },
      include: { items: true, merchant: true, seller: true, payment: true },
    });
    if (!order) throw new ApiError(404, '订单不存在');

    let delivery = null;
    if (['paid', 'preparing', 'delivering', 'delivered'].includes(order.status)) {
      let lat = 0, lng = 0;
      if (order.merchant) {
        lat = order.merchant.latitude;
        lng = order.merchant.longitude;
      }
      delivery = await computeLiveDelivery(order.id, order.paidAt, lat, lng);
    }
    return ok(res, { ...order, delivery });
  }),
);

/** 取消订单 (仅未支付) */
router.post(
  '/:id/cancel',
  asyncHandler(async (req, res) => {
    const order = await prisma.order.findFirst({ where: { id: req.params.id, userId: req.userId! } });
    if (!order) throw new ApiError(404, '订单不存在');
    if (order.status !== 'pending') throw new ApiError(400, '当前订单状态不可取消');
    const updated = await prisma.order.update({ where: { id: order.id }, data: { status: 'cancelled' } });
    return ok(res, updated, '已取消');
  }),
);

export default router;
