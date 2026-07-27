import { WebSocketServer, WebSocket } from 'ws';
import type { Server } from 'node:http';
import { verifyToken } from '../lib/jwt.js';
import { prisma } from '../lib/prisma.js';
import { encryptChatMessage, decryptChatMessage, generateEvidenceHash } from '../lib/crypto.js';

interface ExtWebSocket extends WebSocket {
  isAlive?: boolean;
  userId?: string;
}

const clients = new Map<string, Set<ExtWebSocket>>();
const rooms = new Map<string, Set<ExtWebSocket>>();

/** 向指定用户的所有连接推送消息 (订单状态/聊天消息等) */
export function pushToUser(userId: string, payload: Record<string, unknown>) {
  const set = clients.get(userId);
  if (!set) return;
  const data = JSON.stringify(payload);
  for (const ws of set) {
    if (ws.readyState === WebSocket.OPEN) ws.send(data);
  }
}

/** 向订阅某房间的所有连接推送消息 */
export function pushToRoom(room: string, payload: Record<string, unknown>) {
  const set = rooms.get(room);
  if (!set) return;
  const data = JSON.stringify(payload);
  for (const ws of set) {
    if (ws.readyState === WebSocket.OPEN) ws.send(data);
  }
}

/**
 * 挂载 WebSocket 服务, 路径 /ws?token=JWT。
 * 支持 15s 心跳保活、断线重连补发、AES-256-GCM 终身加密落盘与防篡改存证哈希链
 */
export function attachRealtime(server: Server) {
  const wss = new WebSocketServer({ server, path: '/ws' });

  // 15秒周期心跳保活检测，清理 30 秒无响应的半开连接死 Socket
  const pingInterval = setInterval(() => {
    wss.clients.forEach((ws: WebSocket) => {
       const extWs = ws as ExtWebSocket;
       if (extWs.isAlive === false) {
          console.log('[WS Hub] Terminating inactive half-open socket for user:', extWs.userId);
          return extWs.terminate();
       }
       extWs.isAlive = false;
       extWs.ping();
    });
  }, 15_000);

  wss.on('close', () => {
    clearInterval(pingInterval);
  });

  wss.on('connection', (ws: ExtWebSocket, req) => {
    try {
      const url = new URL(req.url ?? '', 'http://localhost');
      const token = url.searchParams.get('token');
      if (!token) return ws.close(4001, 'missing token');
      const { sub: userId } = verifyToken(token);

      ws.isAlive = true;
      ws.userId = userId;

      if (!clients.has(userId)) clients.set(userId, new Set());
      clients.get(userId)!.add(ws);

      ws.send(JSON.stringify({ event: 'connected', userId, timestamp: Date.now() }));

      ws.on('pong', () => {
        ws.isAlive = true;
      });

      ws.on('message', async (data) => {
        try {
          const msg = JSON.parse(data.toString());
          
          // 1. 应用层心跳探活应答 (增强兼容性)
          if (msg.action === 'ping') {
             ws.isAlive = true;
             ws.send(JSON.stringify({ event: 'pong', timestamp: Date.now() }));
             return;
          }

          // 2. 房间订阅 (如 order:xxx)
          if (msg.action === 'subscribe' && msg.room) {
             const room = msg.room as string;
             if (!rooms.has(room)) rooms.set(room, new Set());
             rooms.get(room)!.add(ws);
             ws.send(JSON.stringify({ event: 'subscribed', room }));
             return;
          }

          // 3. 弱网重连增量同步补送 (Sync Offline Messages)
          if (msg.action === 'sync_offline') {
             const sessionId = msg.sessionId as string | undefined;
             const targetSessions = sessionId && sessionId !== 'all' 
                 ? [{ id: sessionId }] 
                 : await prisma.chatSession.findMany({
                     where: {
                       OR: [
                         { user1Id: userId, unread1: { gt: 0 } },
                         { user2Id: userId, unread2: { gt: 0 } }
                       ]
                     },
                     select: { id: true }
                   });

             for (const s of targetSessions) {
               const unreadMsgs = await prisma.chatMessage.findMany({
                 where: { sessionId: s.id, senderId: { not: userId } },
                 orderBy: { createdAt: 'desc' },
                 take: 30
               });
               if (unreadMsgs.length > 0) {
                 const decryptedMsgs = unreadMsgs.reverse().map(m => ({
                   ...m,
                   content: m.isEncrypted ? decryptChatMessage(m.content) : m.content
                 }));
                 ws.send(JSON.stringify({ event: 'offline_sync', sessionId: s.id, messages: decryptedMsgs }));
               }
             }
             return;
          }

          // 4. 发送实时消息 (自动加解密 + SHA-256 哈希存证链)
          if (msg.action === 'chat') {
             const toUserId = msg.toUserId;
             const content = msg.content;
             const type = msg.type || 'text';
             if (!toUserId || !content) return;

             // 4.1 寻找或创建会话
             const u1 = userId < toUserId ? userId : toUserId;
             const u2 = userId < toUserId ? toUserId : userId;
             
             let session = await prisma.chatSession.findUnique({
                 where: { user1Id_user2Id: { user1Id: u1, user2Id: u2 } }
             });

             if (!session) {
                 session = await prisma.chatSession.create({
                     data: { user1Id: u1, user2Id: u2 }
                 });
             }

             // 4.2 获取该会话上一条消息的防篡改证据哈希
             const prevMsg = await prisma.chatMessage.findFirst({
                 where: { sessionId: session.id },
                 orderBy: { createdAt: 'desc' },
                 select: { evidenceHash: true }
             });

             const now = new Date();
             // 4.3 生成 SHA-256 密码学防篡改存证链哈希
             const evidenceHash = generateEvidenceHash(session.id, userId, now, content, prevMsg?.evidenceHash);
             // 4.4 AES-256-GCM 高强度应用层加密落盘
             const encryptedContent = encryptChatMessage(content);

             const chatMsg = await prisma.chatMessage.create({
                 data: {
                     sessionId: session.id,
                     senderId: userId,
                     type,
                     content: encryptedContent,
                     isEncrypted: true,
                     isRecalled: false,
                     evidenceHash,
                     createdAt: now
                 }
             });

             // 4.5 更新会话最后一条消息和未读数 (在列表显示明文简略)
             const isUser1 = toUserId === u1;
             await prisma.chatSession.update({
                 where: { id: session.id },
                 data: {
                     lastMessage: type === 'text' ? content : (type === 'image' ? '[图片]' : content),
                     unread1: isUser1 ? { increment: 1 } : undefined,
                     unread2: !isUser1 ? { increment: 1 } : undefined,
                 }
             });

             // 4.6 实时向对方与自己推送明文数据包
             const payload = {
                 event: 'chat_message',
                 message: {
                   ...chatMsg,
                   content: content // 在线实时传输传递解密后的原始文字/图片链接供客户端 UI 展示
                 }
             };

             pushToUser(toUserId, payload);
             ws.send(JSON.stringify(payload)); // 回显给发送者
             return;
          }

          // 5. 消息撤回指令 (1分钟窗口限制)
          if (msg.action === 'recall') {
             const messageId = msg.messageId as string;
             if (!messageId) return;

             const msgToRecall = await prisma.chatMessage.findUnique({
                 where: { id: messageId },
                 include: { session: true }
             });

             if (!msgToRecall) {
                 ws.send(JSON.stringify({ event: 'recall_error', messageId, error: '消息不存在' }));
                 return;
             }
             if (msgToRecall.senderId !== userId) {
                 ws.send(JSON.stringify({ event: 'recall_error', messageId, error: '无权撤回他人消息' }));
                 return;
             }

             // 校验 60 秒 (1分钟) 时间窗口
             const elapsedMs = Date.now() - new Date(msgToRecall.createdAt).getTime();
             if (elapsedMs > 60_000) {
                 ws.send(JSON.stringify({ event: 'recall_error', messageId, error: '发送超过1分钟，无法撤回' }));
                 return;
             }

             const recallText = '对方撤回了一条消息';
             await prisma.chatMessage.update({
                 where: { id: messageId },
                 data: {
                   isRecalled: true,
                   type: 'recalled',
                   content: recallText,
                   isEncrypted: false
                 }
             });

             // 更新会话列表上的提示
             await prisma.chatSession.update({
                 where: { id: msgToRecall.sessionId },
                 data: { lastMessage: recallText }
             });

             const recallPayload = {
                 event: 'message_recalled',
                 messageId: msgToRecall.id,
                 sessionId: msgToRecall.sessionId,
                 senderId: userId
             };

             pushToUser(msgToRecall.session.user1Id, recallPayload);
             pushToUser(msgToRecall.session.user2Id, recallPayload);
             return;
          }
        } catch (e) {
          console.error("WS message error", e);
        }
      });

      ws.on('close', () => {
        clients.get(userId)?.delete(ws);
        if (clients.get(userId)?.size === 0) clients.delete(userId);
        
        rooms.forEach((subs, roomName) => {
           subs.delete(ws);
           if (subs.size === 0) rooms.delete(roomName);
        });
      });
    } catch {
      ws.close(4003, 'invalid token');
    }
  });

  return wss;
}
