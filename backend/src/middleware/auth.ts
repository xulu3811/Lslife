import type { NextFunction, Request, Response } from 'express';
import { verifyToken } from '../lib/jwt.js';
import { ApiError } from '../lib/http.js';

declare global {
  // eslint-disable-next-line @typescript-eslint/no-namespace
  namespace Express {
    interface Request {
      userId?: string;
      userPhone?: string;
    }
  }
}

/** 强制鉴权: 无有效 token 返回 401 */
export function requireAuth(req: Request, _res: Response, next: NextFunction) {
  const header = req.headers.authorization;
  if (!header?.startsWith('Bearer ')) {
    throw new ApiError(401, '未登录或登录已过期');
  }
  try {
    const payload = verifyToken(header.slice(7));
    req.userId = payload.sub;
    req.userPhone = payload.phone;
    next();
  } catch {
    throw new ApiError(401, '登录凭证无效');
  }
}

/** 可选鉴权: 有 token 则解析, 无则放行 */
export function optionalAuth(req: Request, _res: Response, next: NextFunction) {
  const header = req.headers.authorization;
  if (header?.startsWith('Bearer ')) {
    try {
      const payload = verifyToken(header.slice(7));
      req.userId = payload.sub;
      req.userPhone = payload.phone;
    } catch {
      /* ignore */
    }
  }
  next();
}
