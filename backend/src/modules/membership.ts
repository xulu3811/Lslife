import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth } from '../middleware/auth.js';

const router = Router();

const PLANS = [
  { tier: 'vip', name: '连山超级会员', price: 9.9, period: '月', benefits: ['每月20条发布额度', '免配送费券x2', '专属会员标识'] },
  { tier: 'premium', name: '连山至尊会员', price: 19.9, period: '月', benefits: ['每月50条发布额度', '免配送费券x5', '尊贵皇冠标识', '优先客服'] },
];

/** 会员套餐 */
router.get('/plans', (_req, res) => ok(res, PLANS));

/** 订阅会员 (真实场景应先支付成功再开通; 此处演示直接开通并落库) */
router.post(
  '/subscribe',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { tier } = z.object({ tier: z.enum(['vip', 'premium']) }).parse(req.body);
    const plan = PLANS.find((p) => p.tier === tier)!;

    const endAt = new Date();
    endAt.setMonth(endAt.getMonth() + 1);

    await prisma.subscription.create({ data: { userId: req.userId!, tier, amount: plan.price, endAt } });
    const user = await prisma.user.update({
      where: { id: req.userId! },
      data: { membershipTier: tier, membershipUntil: endAt },
    });

    return ok(res, { membershipTier: user.membershipTier, membershipUntil: user.membershipUntil }, '会员开通成功');
  }),
);

export default router;
