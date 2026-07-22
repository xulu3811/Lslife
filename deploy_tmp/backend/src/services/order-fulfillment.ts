import { prisma } from '../lib/prisma.js';
import { pushToUser } from '../realtime/hub.js';

export const POINTS_PER_RMB = 10;
export const COMMISSION_RATE = 0.03;

/**
 * 订单支付成功后的履约动作:
 * 1) 置订单为已支付 2) 扣减库存/累计销量 3) 生成配送单
 * 4) 清空该商家购物车 5) 生成通知 + WebSocket 推送
 */
export async function markOrderPaid(orderId: string, transactionId: string) {
  const order = await prisma.order.findUnique({ where: { id: orderId }, include: { items: true, payment: true } });
  if (!order) return;

  await prisma.$transaction(async (tx) => {
    await tx.order.update({ where: { id: orderId }, data: { status: 'paid', paidAt: new Date() } });
    await tx.payment.updateMany({ where: { orderId }, data: { status: 'success', paidAt: new Date(), transactionId } });

    // Deduct points if used in this payment (and not already deducted by full points/wallet flow)
    // Actually full points flow calls markOrderPaid directly, BUT it already updated the payment to 'success' before calling this? No, it just created the payment.
    // Wait! In payments.ts: if cashAmount <= 0 (full points), it deducts points in tx, creates payment as 'success', then calls markOrderPaid.
    // If it's mock/wechat, it creates payment as 'created', then later calls mock-confirm which calls markOrderPaid.
    // So if we check if payment was 'created', we can deduct. But it's simpler: the payment we fetch is the one BEFORE the updateMany.
    const payment = order.payment;
    if (payment && payment.status === 'created' && payment.pointsUsed > 0) {
      const u = await tx.user.findUnique({ where: { id: order.userId } });
      if (u) {
        const uAfter = await tx.user.update({
          where: { id: u.id },
          data: { points: { decrement: payment.pointsUsed } }
        });
        await tx.walletTransaction.create({
          data: {
            userId: u.id,
            type: 'points',
            amount: -payment.pointsUsed,
            balanceBefore: u.points,
            balanceAfter: uAfter.points,
            bizType: 'order_pay',
            orderId: order.id,
            description: `混合支付抵扣 (支付单号: ${transactionId})`
          }
        });
      }
    }

    for (const item of order.items) {
      if (item.productId) {
        await tx.product.update({ where: { id: item.productId }, data: { sales: { increment: item.quantity } } });
      }
      // Posts don't strictly have a sales counter right now, but could be added later
    }

    await tx.delivery.upsert({
      where: { orderId },
      update: {},
      create: {
        orderId,
        riderName: '阿力 (连山特派骑手)',
        riderPhone: '139-2244-8800',
        riderAvatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120&h=120&fit=crop&q=80',
        riderLat: 0,
        riderLng: 0,
        status: 'preparing',
      },
    });

    if (order.merchantId) {
      await tx.cartItem.deleteMany({ where: { userId: order.userId, merchantId: order.merchantId } });
    } else if (order.sellerId) {
      await tx.cartItem.deleteMany({ where: { userId: order.userId, sellerId: order.sellerId } });
    }

    await tx.notification.create({
      data: {
        userId: order.userId,
        type: 'merchant_accept',
        title: '商家接单：美味/好物制作中',
        content: `【${order.merchantName || '卖家'}】已接单并开始为您准备!`,
        orderId,
      },
    });
  });

  pushToUser(order.userId, { event: 'order_paid', orderId, orderNo: order.orderNo });
}

/**
 * 积分充值成功回调
 */
export async function handleRechargePaid(orderId: string, transactionId: string) {
  const order = await prisma.order.findUnique({ where: { id: orderId } });
  if (!order) return;

  const pointsToAdd = Math.floor(order.totalAmount * POINTS_PER_RMB);

  await prisma.$transaction(async (tx) => {
    await tx.order.update({ where: { id: orderId }, data: { status: 'paid', paidAt: new Date() } });
    await tx.payment.updateMany({ where: { orderId }, data: { status: 'success', paidAt: new Date(), transactionId } });

    const userAfter = await tx.user.update({
      where: { id: order.userId },
      data: { points: { increment: pointsToAdd } }
    });

    // Record recharge cash in (if we track system accounts we could, but here we just track it for completeness if needed)
    // Actually, user paid with WeChat, they bought points. So their point balance increased.
    await tx.walletTransaction.create({
      data: {
        userId: order.userId,
        type: 'points',
        amount: pointsToAdd,
        balanceBefore: userAfter.points - pointsToAdd,
        balanceAfter: userAfter.points,
        bizType: 'recharge',
        orderId,
        description: `充值 ${pointsToAdd} 积分 (支付 ${order.totalAmount} 元)`
      }
    });
  });

  pushToUser(order.userId, { event: 'recharge_success', pointsAdded: pointsToAdd });
}
