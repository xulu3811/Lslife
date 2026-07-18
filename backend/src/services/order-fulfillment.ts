import { prisma } from '../lib/prisma.js';
import { pushToUser } from '../realtime/hub.js';

/**
 * 订单支付成功后的履约动作:
 * 1) 置订单为已支付 2) 扣减库存/累计销量 3) 生成配送单
 * 4) 清空该商家购物车 5) 生成通知 + WebSocket 推送
 */
export async function markOrderPaid(orderId: string, transactionId: string) {
  const order = await prisma.order.findUnique({ where: { id: orderId }, include: { items: true } });
  if (!order) return;

  await prisma.$transaction(async (tx) => {
    await tx.order.update({ where: { id: orderId }, data: { status: 'paid', paidAt: new Date() } });
    await tx.payment.updateMany({ where: { orderId }, data: { status: 'success', paidAt: new Date(), transactionId } });

    for (const item of order.items) {
      await tx.product.update({ where: { id: item.productId }, data: { sales: { increment: item.quantity } } });
    }

    await tx.delivery.upsert({
      where: { orderId },
      update: {},
      create: {
        orderId,
        riderName: '阿力 (连山特派骑手)',
        riderPhone: '139-2244-8800',
        riderAvatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120&h=120&fit=crop&q=80',
        riderLat: order.merchantId ? 0 : 0,
        riderLng: 0,
        status: 'preparing',
      },
    });

    await tx.cartItem.deleteMany({ where: { userId: order.userId, merchantId: order.merchantId } });

    await tx.notification.create({
      data: {
        userId: order.userId,
        type: 'merchant_accept',
        title: '商家接单：瑶家美味制作中',
        content: `【${order.merchantName}】已接单并开始为您准备, 火候正佳!`,
        orderId,
      },
    });
  });

  pushToUser(order.userId, { event: 'order_paid', orderId, orderNo: order.orderNo });
}
