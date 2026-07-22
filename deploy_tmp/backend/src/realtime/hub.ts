import { WebSocketServer, WebSocket } from 'ws';
import type { Server } from 'node:http';
import { verifyToken } from '../lib/jwt.js';
import { prisma } from '../lib/prisma.js';

const clients = new Map<string, Set<WebSocket>>();
const rooms = new Map<string, Set<WebSocket>>();

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
 */
export function attachRealtime(server: Server) {
  const wss = new WebSocketServer({ server, path: '/ws' });

  wss.on('connection', (ws, req) => {
    try {
      const url = new URL(req.url ?? '', 'http://localhost');
      const token = url.searchParams.get('token');
      if (!token) return ws.close(4001, 'missing token');
      const { sub: userId } = verifyToken(token);

      if (!clients.has(userId)) clients.set(userId, new Set());
      clients.get(userId)!.add(ws);

      ws.send(JSON.stringify({ event: 'connected', userId }));

      ws.on('message', async (data) => {
        try {
          const msg = JSON.parse(data.toString());
          if (msg.action === 'subscribe' && msg.room) {
             const room = msg.room as string;
             if (!rooms.has(room)) rooms.set(room, new Set());
             rooms.get(room)!.add(ws);
             ws.send(JSON.stringify({ event: 'subscribed', room }));
          } else if (msg.action === 'chat') {
             // msg: { action: 'chat', toUserId: '...', type: 'text', content: '...' }
             const toUserId = msg.toUserId;
             const content = msg.content;
             const type = msg.type || 'text';
             if (!toUserId || !content) return;

             // 1. Find or create ChatSession
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

             // 2. Create Message
             const chatMsg = await prisma.chatMessage.create({
                 data: {
                     sessionId: session.id,
                     senderId: userId,
                     type,
                     content
                 }
             });

             // 3. Update Session lastMessage and unread count
             const isUser1 = toUserId === u1;
             await prisma.chatSession.update({
                 where: { id: session.id },
                 data: {
                     lastMessage: type === 'text' ? content : '[图片]',
                     unread1: isUser1 ? { increment: 1 } : undefined,
                     unread2: !isUser1 ? { increment: 1 } : undefined,
                 }
             });

             const payload = {
                 event: 'chat_message',
                 message: chatMsg
             };

             // 4. Push to recipient and sender
             pushToUser(toUserId, payload);
             ws.send(JSON.stringify(payload)); // echo to sender
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
