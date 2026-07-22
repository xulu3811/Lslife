import { Router } from 'express';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth } from '../middleware/auth.js';

const router = Router();

// 获取当前用户的所有聊天会话列表
router.get(
  '/sessions',
  requireAuth,
  asyncHandler(async (req, res) => {
    const userId = req.userId!;
    
    const sessions = await prisma.chatSession.findMany({
      where: {
        OR: [
          { user1Id: userId },
          { user2Id: userId }
        ]
      },
      orderBy: { updatedAt: 'desc' }
    });

    // 组合对方的简单用户信息
    const result = await Promise.all(sessions.map(async s => {
      const isUser1 = s.user1Id === userId;
      const otherId = isUser1 ? s.user2Id : s.user1Id;
      const other = await prisma.user.findUnique({
        where: { id: otherId },
        select: { id: true, nickname: true, avatar: true }
      });
      return {
        id: s.id,
        targetUser: other,
        lastMessage: s.lastMessage,
        unread: isUser1 ? s.unread1 : s.unread2,
        updatedAt: s.updatedAt
      };
    }));

    return ok(res, result);
  })
);

// 获取某个会话的消息记录
router.get(
  '/sessions/:id/messages',
  requireAuth,
  asyncHandler(async (req, res) => {
    const userId = req.userId!;
    const { id } = req.params;
    
    const session = await prisma.chatSession.findUnique({ where: { id } });
    if (!session) throw new ApiError(404, 'Session not found');
    if (session.user1Id !== userId && session.user2Id !== userId) {
      throw new ApiError(403, 'Forbidden');
    }

    // 清空未读数
    const isUser1 = session.user1Id === userId;
    await prisma.chatSession.update({
      where: { id },
      data: {
        unread1: isUser1 ? 0 : undefined,
        unread2: !isUser1 ? 0 : undefined
      }
    });

    const messages = await prisma.chatMessage.findMany({
      where: { sessionId: id },
      orderBy: { createdAt: 'asc' },
      take: 50
    });

    return ok(res, messages);
  })
);

export default router;
