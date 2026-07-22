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

// ================== 财务监控 ==================
router.get(
  '/finance/stats',
  asyncHandler(async (_req, res) => {
    // 总充值现金 (bizType = recharge, type = points, BUT wait, user pays cash to get points. The payment is in Payment table or WalletTransaction?)
    // In WalletTransaction, we recorded type='points', bizType='recharge', amount > 0.
    // The actual cash paid is `payment.cashAmount`. Let's just aggregate Payment table for real cash.
    const totalCashIncome = await prisma.payment.aggregate({
      _sum: { cashAmount: true },
      where: { status: 'success' }
    });

    const totalPointsUsed = await prisma.payment.aggregate({
      _sum: { pointsUsed: true },
      where: { status: 'success' }
    });

    const settlementTransactions = await prisma.walletTransaction.aggregate({
      _sum: { amount: true },
      where: { bizType: 'settlement' }
    });
    
    // We can also compute system commission = total order amount - settled amount.
    // Let's just return basic stats.
    return ok(res, {
      totalCashIncome: totalCashIncome._sum.cashAmount || 0,
      totalPointsUsed: totalPointsUsed._sum.pointsUsed || 0,
      totalSettledToMerchants: settlementTransactions._sum.amount || 0
    });
  })
);

router.get(
  '/finance/transactions',
  asyncHandler(async (req, res) => {
    const { page = '1', limit = '20', type, bizType } = req.query as Record<string, string>;
    const p = Math.max(1, parseInt(page, 10) || 1);
    const l = Math.min(100, Math.max(1, parseInt(limit, 10) || 20));

    let where: any = {};
    if (type) where.type = type;
    if (bizType) where.bizType = bizType;

    const [total, list] = await Promise.all([
      prisma.walletTransaction.count({ where }),
      prisma.walletTransaction.findMany({
        where,
        orderBy: { createdAt: 'desc' },
        skip: (p - 1) * l,
        take: l,
        include: {
          user: { select: { phone: true, nickname: true } },
          merchant: { select: { name: true } },
          order: { select: { orderNo: true } }
        }
      })
    ]);

    return ok(res, { total, page: p, pageSize: l, list });
  })
);

// ================== 订单管理 ==================
router.get(
  '/orders',
  asyncHandler(async (req, res) => {
    const { status, search, page = '1', limit = '20' } = req.query as Record<string, string>;
    const p = Math.max(1, parseInt(page, 10) || 1);
    const l = Math.min(100, Math.max(1, parseInt(limit, 10) || 20));

    let where: any = {};
    if (status) where.status = status;
    if (search) {
      where.OR = [
        { orderNo: { contains: search } },
        { merchantName: { contains: search } },
        { deliveryPhone: { contains: search } },
      ];
    }

    const [total, list] = await Promise.all([
      prisma.order.count({ where }),
      prisma.order.findMany({
        where,
        orderBy: { createdAt: 'desc' },
        skip: (p - 1) * l,
        take: l,
        include: {
          user: { select: { nickname: true, phone: true } },
          items: true,
          delivery: true,
          payment: true
        },
      }),
    ]);

    return ok(res, { total, page: p, pageSize: l, list });
  })
);

router.post(
  '/orders/:id/action',
  asyncHandler(async (req, res) => {
    const id = req.params.id;
    const { action } = z.object({ action: z.enum(['refund', 'assign_rider', 'complete']) }).parse(req.body);

    const order = await prisma.order.findUnique({ where: { id }, include: { delivery: true } });
    if (!order) throw new ApiError(404, '订单不存在');

    if (action === 'refund') {
      await prisma.order.update({ where: { id }, data: { status: 'cancelled' } });
      if (order.paidAt) {
        // Mock refund
        await prisma.payment.updateMany({
          where: { orderId: id },
          data: { status: 'refunded' }
        });
      }
      return ok(res, null, '订单已强制退款/取消');
    }

    if (action === 'assign_rider') {
      if (order.status === 'cancelled' || order.status === 'delivered') throw new ApiError(400, '订单状态不允许指派骑手');
      
      const newStatus = 'delivering';
      await prisma.order.update({ where: { id }, data: { status: newStatus } });
      
      if (!order.delivery) {
        await prisma.delivery.create({
          data: {
            orderId: id,
            riderName: '王骑手 (后台指派)',
            riderPhone: '13800000000',
            riderLat: 24.502,
            riderLng: 112.085,
            progress: 30,
            status: newStatus
          }
        });
      } else {
        await prisma.delivery.update({
          where: { orderId: id },
          data: { status: newStatus, progress: 30 }
        });
      }
      return ok(res, null, '已强制指派模拟骑手');
    }

    if (action === 'complete') {
      await prisma.$transaction(async (tx) => {
        await tx.order.update({ where: { id }, data: { status: 'delivered' } });
        if (order.delivery) {
          await tx.delivery.update({
            where: { orderId: id },
            data: { status: 'delivered', progress: 100 }
          });
        }
        
        // 商户结算 (3% 抽成)
        if (order.merchantId && order.paidAt) {
          const COMMISSION_RATE = 0.03;
          const commission = order.totalAmount * COMMISSION_RATE;
          const settlementAmount = order.totalAmount - commission;

          const merchant = await tx.merchant.findUnique({ where: { id: order.merchantId } });
          if (merchant) {
            const mAfter = await tx.merchant.update({
              where: { id: order.merchantId },
              data: { walletBalance: { increment: settlementAmount } }
            });

            await tx.walletTransaction.create({
              data: {
                merchantId: merchant.id,
                userId: order.userId, // Record who the buyer was for reference
                type: 'cash',
                amount: settlementAmount,
                balanceBefore: merchant.walletBalance,
                balanceAfter: mAfter.walletBalance,
                bizType: 'settlement',
                orderId: order.id,
                description: `订单结算收入 (总额: ${order.totalAmount}, 抽成: ${commission.toFixed(2)})`
              }
            });
          }
        }
      });
      return ok(res, null, '订单已强制完成');
    }
  })
);

// ================== 商家管理 ==================
router.get(
  '/merchants',
  asyncHandler(async (req, res) => {
    const { status, search, page = '1', limit = '20' } = req.query as Record<string, string>;
    const p = Math.max(1, parseInt(page, 10) || 1);
    const l = Math.min(100, Math.max(1, parseInt(limit, 10) || 20));

    let where: any = {};
    if (status) where.status = status;
    if (search) {
      where.OR = [
        { name: { contains: search } },
        { phone: { contains: search } },
      ];
    }

    const [total, list] = await Promise.all([
      prisma.merchant.count({ where }),
      prisma.merchant.findMany({
        where,
        orderBy: { createdAt: 'desc' },
        skip: (p - 1) * l,
        take: l,
      }),
    ]);

    return ok(res, { total, page: p, pageSize: l, list });
  })
);

router.post(
  '/merchants/:id/status',
  asyncHandler(async (req, res) => {
    const id = req.params.id;
    const { status } = z.object({ status: z.enum(['active', 'offline']) }).parse(req.body);

    const merchant = await prisma.merchant.update({
      where: { id },
      data: { status }
    });

    return ok(res, merchant, `商户状态已更新为 ${status}`);
  })
);

// ================== 商品管理 ==================
router.get(
  '/products',
  asyncHandler(async (req, res) => {
    const { status, search, merchantId, page = '1', limit = '20' } = req.query as Record<string, string>;
    const p = Math.max(1, parseInt(page, 10) || 1);
    const l = Math.min(100, Math.max(1, parseInt(limit, 10) || 20));

    let where: any = {};
    if (status) where.status = status;
    if (merchantId) where.merchantId = merchantId;
    if (search) {
      where.OR = [
        { name: { contains: search } },
        { desc: { contains: search } },
      ];
    }

    const [total, list] = await Promise.all([
      prisma.product.count({ where }),
      prisma.product.findMany({
        where,
        orderBy: { sales: 'desc' },
        skip: (p - 1) * l,
        take: l,
        include: { merchant: { select: { name: true } } }
      }),
    ]);

    return ok(res, { total, page: p, pageSize: l, list });
  })
);

router.post(
  '/products/:id/status',
  asyncHandler(async (req, res) => {
    const id = req.params.id;
    const { status } = z.object({ status: z.enum(['active', 'offline']) }).parse(req.body);

    const product = await prisma.product.update({
      where: { id },
      data: { status }
    });

    return ok(res, product, `商品状态已更新为 ${status}`);
  })
);

export default router;
