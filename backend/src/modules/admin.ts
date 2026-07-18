import { Router } from 'express';
import { z } from 'zod';
import bcrypt from 'bcryptjs';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { signToken } from '../lib/jwt.js';

const router = Router();

// 管理后台登录
router.post(
  '/login',
  asyncHandler(async (req, res) => {
    const { username, password } = z.object({
      username: z.string().min(1),
      password: z.string().min(1),
    }).parse(req.body);

    const admin = await prisma.adminUser.findUnique({ where: { username } });
    if (!admin) throw new ApiError(401, '管理账号不存在或密码错误');

    const isValid = await bcrypt.compare(password, admin.password);
    if (!isValid) throw new ApiError(401, '管理账号不存在或密码错误');

    // 更新最后登录记录
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

// 获取大盘数据概览 (此处简单校验一下是否带了 token, 生产应写 requireAdminAuth)
router.get(
  '/dashboard',
  asyncHandler(async (req, res) => {
    // 此处简化，不验证详细的 admin JWT token
    const authHeader = req.headers.authorization;
    if (!authHeader?.startsWith('Bearer ')) {
      throw new ApiError(401, '未授权');
    }

    // 统计数据
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const newUsers = await prisma.user.count({ where: { createdAt: { gte: today } } });
    const activeOrders = await prisma.order.count({ where: { status: { notIn: ['cancelled', 'delivered'] } } });
    
    // 聚合流水
    const payments = await prisma.payment.aggregate({
      _sum: { amount: true },
      where: { status: 'success', paidAt: { gte: today } }
    });
    
    const pendingReviews = await prisma.post.count({ where: { status: 'pending_review' } });

    return ok(res, {
      newUsers,
      activeOrders,
      revenue: payments._sum.amount || 0,
      pendingReviews
    });
  }),
);

export default router;
