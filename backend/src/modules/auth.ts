import { Router } from 'express';
import { z } from 'zod';
import { customAlphabet } from 'nanoid';
import { prisma } from '../lib/prisma.js';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth } from '../middleware/auth.js';
import { signToken } from '../lib/jwt.js';
import { getSmsProvider } from '../services/sms.js';
import { createHash } from 'node:crypto';

const router = Router();
const genCode = customAlphabet('0123456789', 6);

const phoneSchema = z.string().regex(/^1\d{10}$/, '请输入正确的11位手机号');

/** 发送短信验证码 */
router.post(
  '/send-code',
  asyncHandler(async (req, res) => {
    const { phone } = z.object({ phone: phoneSchema }).parse(req.body);

    // 60 秒内限频
    const recent = await prisma.verificationCode.findFirst({
      where: { phone, purpose: 'login', createdAt: { gt: new Date(Date.now() - 60_000) } },
      orderBy: { createdAt: 'desc' },
    });
    if (recent) throw new ApiError(429, '验证码发送过于频繁, 请稍后再试');

    const code = genCode();
    await prisma.verificationCode.create({
      data: { phone, code, purpose: 'login', expiresAt: new Date(Date.now() + 5 * 60_000) },
    });
    const result = await getSmsProvider().send(phone, code);
    // mock 环境把验证码回传, 便于联调; 生产不返回
    return ok(res, { sent: true, mockCode: result.mockCode }, '验证码已发送');
  }),
);

/** 验证码登录 (不存在则自动注册) */
router.post(
  '/login',
  asyncHandler(async (req, res) => {
    const { phone, code } = z.object({ phone: phoneSchema, code: z.string().length(6) }).parse(req.body);

    const record = await prisma.verificationCode.findFirst({
      where: { phone, purpose: 'login', consumed: false, expiresAt: { gt: new Date() } },
      orderBy: { createdAt: 'desc' },
    });
    if (!record || record.code !== code) throw new ApiError(400, '验证码错误或已过期');

    await prisma.verificationCode.update({ where: { id: record.id }, data: { consumed: true } });

    let user = await prisma.user.findUnique({ where: { phone } });
    if (!user) {
      user = await prisma.user.create({
        data: {
          phone,
          nickname: `连山用户${phone.slice(-4)}`,
          avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120&h=120&fit=crop&q=80',
        },
      });
    }

    const token = signToken({ sub: user.id, phone: user.phone });
    return ok(res, { token, user: sanitize(user) }, '登录成功');
  }),
);

/** 当前用户信息 */
router.get(
  '/me',
  requireAuth,
  asyncHandler(async (req, res) => {
    const user = await prisma.user.findUnique({ where: { id: req.userId! } });
    if (!user) throw new ApiError(404, '用户不存在');
    return ok(res, sanitize(user));
  }),
);

/** 实名认证 (身份证号仅存哈希) */
router.post(
  '/realname',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { realName, idCard } = z
      .object({ realName: z.string().min(2), idCard: z.string().regex(/^\d{17}[\dXx]$/, '身份证号格式错误') })
      .parse(req.body);
    const idCardHash = createHash('sha256').update(idCard).digest('hex');
    const user = await prisma.user.update({
      where: { id: req.userId! },
      data: { realName, idCardHash, realNameStatus: 'verified' },
    });
    return ok(res, sanitize(user), '实名认证成功');
  }),
);

/** 更新昵称/头像 */
router.patch(
  '/profile',
  requireAuth,
  asyncHandler(async (req, res) => {
    const { nickname, avatar } = z.object({ nickname: z.string().min(1).optional(), avatar: z.string().url().optional() }).parse(req.body);
    const user = await prisma.user.update({ where: { id: req.userId! }, data: { nickname, avatar } });
    return ok(res, sanitize(user), '已更新');
  }),
);

function sanitize(user: { idCardHash: string | null } & Record<string, unknown>) {
  const { idCardHash, ...rest } = user;
  return rest;
}

export default router;
