import { Router } from 'express';
import multer from 'multer';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { v4 as uuidv4 } from 'uuid';
import { ok, ApiError } from '../lib/http.js';
import { asyncHandler } from '../middleware/error.js';
import { requireAuth } from '../middleware/auth.js';

const router = Router();

const __dirname = path.dirname(fileURLToPath(import.meta.url));
/** 编译后 dist/modules → 项目根 public/uploads */
const UPLOAD_DIR = path.resolve(__dirname, '../../public/uploads');
fs.mkdirSync(UPLOAD_DIR, { recursive: true });

function publicBaseUrl(req: { protocol: string; get: (h: string) => string | undefined }): string {
  if (process.env.PUBLIC_BASE_URL) return process.env.PUBLIC_BASE_URL.replace(/\/$/, '');
  const proto = (req.get('x-forwarded-proto') || req.protocol || 'https').split(',')[0].trim();
  const host = req.get('x-forwarded-host') || req.get('host') || 'localhost:4000';
  return `${proto}://${host}`;
}

const storage = multer.diskStorage({
  destination: (_req, _file, cb) => cb(null, UPLOAD_DIR),
  filename: (_req, file, cb) => {
    const ext = path.extname(file.originalname) || '.jpg';
    cb(null, `${uuidv4()}${ext}`);
  },
});

const upload = multer({
  storage,
  limits: { fileSize: 5 * 1024 * 1024 },
  fileFilter: (_req, file, cb) => {
    if (file.mimetype.startsWith('image/')) cb(null, true);
    else cb(new ApiError(400, '只允许上传图片文件'));
  },
});

router.post(
  '/',
  requireAuth,
  upload.single('image'),
  asyncHandler(async (req, res) => {
    if (!req.file) throw new ApiError(400, '未找到上传的图片文件');
    const url = `${publicBaseUrl(req)}/uploads/${req.file.filename}`;
    return ok(res, { url });
  }),
);

router.post(
  '/batch',
  requireAuth,
  upload.array('images', 9),
  asyncHandler(async (req, res) => {
    const files = req.files as Express.Multer.File[] | undefined;
    if (!files?.length) throw new ApiError(400, '未找到上传的图片文件');
    const base = publicBaseUrl(req);
    const urls = files.map((f) => `${base}/uploads/${f.filename}`);
    return ok(res, { urls });
  }),
);

export default router;
