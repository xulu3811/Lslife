import { Router } from 'express';
import { z } from 'zod';
import { prisma } from '../lib/prisma.js';
import { ok } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth } from '../middleware/auth.js';

const router = Router();

/** 获取钱包基本信息与流水 */
router.get(
  '/info',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { page = '1', limit = '20' } = req.query;
    const pageNum = parseInt(page as string) || 1;
    const limitNum = parseInt(limit as string) || 20;

    const user = await prisma.user.findUnique({
      where: { id: req.userId! },
      select: { walletBalance: true, points: true }
    });

    if (!user) return res.status(404).json({ error: 'User not found' });

    const total = await prisma.walletTransaction.count({
      where: { userId: req.userId! }
    });

    const transactions = await prisma.walletTransaction.findMany({
      where: { userId: req.userId! },
      orderBy: { createdAt: 'desc' },
      skip: (pageNum - 1) * limitNum,
      take: limitNum
    });

    return ok(res, {
      balance: user.walletBalance,
      points: user.points,
      transactions,
      pagination: {
        page: pageNum,
        limit: limitNum,
        total,
        totalPages: Math.ceil(total / limitNum)
      }
    });
  })
);

export default router;
