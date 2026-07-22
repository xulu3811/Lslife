import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth } from '../middleware/auth.js';
import { getPaymentProvider, type PayChannel } from '../services/payment.js';
import { markOrderPaid, handleRechargePaid } from '../services/order-fulfillment.js';

const router = Router();
const POINTS_PER_RMB = 10;

/** 创建支付 (支持混合支付) */
router.post(
  '/create',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { orderId, pointsUsed = 0, channel } = z
      .object({ orderId: z.string(), pointsUsed: z.number().default(0), channel: z.enum(['wechat', 'alipay', 'wallet', 'mock']).optional() })
      .parse(req.body);

    const order = await prisma.order.findFirst({ where: { id: orderId, userId: req.userId! } });
    if (!order) throw new ApiError(404, '订单不存在');
    if (order.status !== 'pending') throw new ApiError(400, '订单已支付或已关闭');

    const user = await prisma.user.findUnique({ where: { id: req.userId! } });
    if (!user) throw new ApiError(404, '用户不存在');

    if (pointsUsed > user.points) throw new ApiError(400, '积分余额不足');

    const pointsDiscount = pointsUsed / POINTS_PER_RMB;
    let cashAmount = order.totalAmount - pointsDiscount;
    if (cashAmount <= 0) {
      cashAmount = 0;
      // 全额积分支付
      await prisma.$transaction(async (tx) => {
        const userAfter = await tx.user.update({
          where: { id: user.id },
          data: { points: { decrement: pointsUsed } }
        });
        
        await tx.walletTransaction.create({
          data: {
            userId: user.id,
            type: 'points',
            amount: -pointsUsed,
            balanceBefore: user.points,
            balanceAfter: userAfter.points,
            bizType: 'order_pay',
            orderId: order.id,
            description: `全额积分支付订单 ${order.orderNo}`
          }
        });

        await tx.payment.upsert({
          where: { orderId: order.id },
          update: { provider: 'points', amount: order.totalAmount, pointsUsed, pointsDiscount, cashAmount: 0, status: 'success', paidAt: new Date() },
          create: { orderId: order.id, provider: 'points', amount: order.totalAmount, pointsUsed, pointsDiscount, cashAmount: 0, status: 'success', paidAt: new Date() },
        });
      });
      await markOrderPaid(order.id, `PTS${Date.now()}`);
      return ok(res, { paid: true, orderId: order.id }, '积分支付成功');
    }

    if (!channel) throw new ApiError(400, '需选择支付渠道支付剩余金额');

    // 钱包支付: 校验余额
    if (channel === 'wallet') {
      if (user.walletBalance < cashAmount) throw new ApiError(400, '钱包余额不足');
    }

    const provider = getPaymentProvider();
    const result = await provider.createPayment({
      orderNo: order.orderNo,
      amount: cashAmount,
      channel: channel as PayChannel,
      description: `连山同城-${order.merchantName}`,
    });

    const payment = await prisma.payment.upsert({
      where: { orderId: order.id },
      update: { provider: channel, amount: order.totalAmount, pointsUsed, pointsDiscount, cashAmount, status: 'created', transactionId: result.transactionId, prepayPayload: JSON.stringify(result.prepayPayload) },
      create: { orderId: order.id, provider: channel, amount: order.totalAmount, pointsUsed, pointsDiscount, cashAmount, status: 'created', transactionId: result.transactionId, prepayPayload: JSON.stringify(result.prepayPayload) },
    });

    // 钱包直接扣款完成支付
    if (channel === 'wallet') {
      await prisma.$transaction(async (tx) => {
        // Deduct points
        let currentPoints = user.points;
        if (pointsUsed > 0) {
           const uAfterPts = await tx.user.update({ where: { id: user.id }, data: { points: { decrement: pointsUsed } } });
           await tx.walletTransaction.create({
             data: { userId: user.id, type: 'points', amount: -pointsUsed, balanceBefore: currentPoints, balanceAfter: uAfterPts.points, bizType: 'order_pay', orderId: order.id, description: '混合支付抵扣' }
           });
           currentPoints = uAfterPts.points;
        }

        // Deduct wallet cash
        const uAfter = await tx.user.update({ where: { id: user.id }, data: { walletBalance: { decrement: cashAmount } } });
        await tx.walletTransaction.create({
          data: { userId: user.id, type: 'cash', amount: -cashAmount, balanceBefore: user.walletBalance, balanceAfter: uAfter.walletBalance, bizType: 'order_pay', orderId: order.id, description: '钱包支付' }
        });
      });
      
      await markOrderPaid(order.id, result.transactionId);
      return ok(res, { paid: true, orderId: order.id }, '钱包支付成功');
    }

    return ok(res, { paymentId: payment.id, prepayPayload: JSON.parse(payment.prepayPayload!) });
  }),
);

/** 积分充值 */
router.post('/recharge', requireAuth, asyncHandler(async (req, res) => {
  const { amount, channel = 'wechat' } = z.object({ amount: z.number().min(0.01), channel: z.enum(['wechat', 'alipay', 'mock']).optional() }).parse(req.body);
  
  // 创建一个虚拟订单代表充值
  const order = await prisma.order.create({
    data: {
      orderNo: `REC${Date.now()}${Math.floor(Math.random()*1000)}`,
      userId: req.userId!,
      merchantName: '积分充值',
      itemsTotal: amount,
      totalAmount: amount,
      deliveryName: '__RECHARGE__',
      deliveryPhone: '000000',
      deliveryAddress: 'Virtual'
    }
  });

  const provider = getPaymentProvider();
  const result = await provider.createPayment({
    orderNo: order.orderNo,
    amount,
    channel: channel as PayChannel,
    description: `积分充值 ${amount * POINTS_PER_RMB} 积分`
  });

  const payment = await prisma.payment.create({
    data: { orderId: order.id, provider: channel, amount, cashAmount: amount, status: 'created', transactionId: result.transactionId, prepayPayload: JSON.stringify(result.prepayPayload) },
  });

  return ok(res, { paymentId: payment.id, prepayPayload: JSON.parse(payment.prepayPayload!) });
}));

/** 演示环境: 客户端确认 mock 支付完成 */
router.post(
  '/mock-confirm',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { orderNo } = z.object({ orderNo: z.string() }).parse(req.body);
    const order = await prisma.order.findFirst({ where: { orderNo, userId: req.userId! }, include: { payment: true } });
    if (!order) throw new ApiError(404, '订单不存在');
    if (order.status !== 'pending') return ok(res, { alreadyPaid: true });
    
    if (order.deliveryName === '__RECHARGE__') {
      await handleRechargePaid(order.id, `MOCK${Date.now()}`);
    } else {
      await markOrderPaid(order.id, `MOCK${Date.now()}`);
    }
    return ok(res, { paid: true, orderId: order.id }, '支付成功');
  }),
);

/** 第三方支付异步回调 (微信/支付宝) */
router.post(
  '/callback/:provider',
  asyncHandler(async (req, res) => {
    const provider = getPaymentProvider();
    const result = await provider.verifyCallback(req.body, req.headers as Record<string, unknown>);
    if (result.success) {
      const order = await prisma.order.findUnique({ where: { orderNo: result.orderNo } });
      if (order && order.status === 'pending') {
        if (order.deliveryName === '__RECHARGE__') {
          await handleRechargePaid(order.id, result.transactionId);
        } else {
          await markOrderPaid(order.id, result.transactionId);
        }
      }
    }
    return res.json({ code: 'SUCCESS', message: 'OK' });
  }),
);

export default router;
