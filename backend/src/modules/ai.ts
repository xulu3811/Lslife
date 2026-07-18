import { Router } from 'express';
import { z } from 'zod';
import { ok } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { optionalAuth } from '../middleware/auth.js';
import { getAiProvider } from '../services/ai.js';

const router = Router();

/** AI 同城助手推荐 (国内合规大模型) */
router.post(
  '/recommend',
  optionalAuth,
  asyncHandler(async (req, res) => {
    const { prompt } = z.object({ prompt: z.string().min(1).max(500) }).parse(req.body);
    const result = await getAiProvider().recommend(prompt);
    return ok(res, result);
  }),
);

export default router;
