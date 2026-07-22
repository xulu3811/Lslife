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

/** AI 闲鱼风商品文案生成 */
router.post(
  '/generate-description',
  optionalAuth,
  asyncHandler(async (req, res) => {
    const { title, category, draft } = z.object({ 
      title: z.string().nullable().optional(), 
      category: z.string().nullable().optional(),
      draft: z.string().nullable().optional()
    }).parse(req.body);
    
    const hint = title || category || '闲置好物';
    const existing = (draft || '').trim();
    
    const prompt = `你是连山同城闲置发布文案助手。
用户准备发布一款名为“${hint}”的闲置商品。
${existing ? `用户目前填写的草稿内容如下：\n【${existing}】\n请在【保留用户原文全部关键事实】的基础上进行优化润色，补上自然的促销短句。不要删掉用户已写信息，不要编造用户未提及的事实。` : '请写一段优质的闲鱼风格转手文案。字数控制在100字左右，要包含转手原因、成色新、价格实在等卖点。'}

你需要输出以下JSON格式的信息：
{
  "title": "润色后的商品标题",
  "description": "润色后的详细参数和转手原因等文案正文",
  "brand": "提取的品牌名称(若无则留空)",
  "parameters": "提取的商品参数(若无则留空)",
  "purchaseDate": "提取的购买日期(若无则留空)"
}`;

    const result = await getAiProvider().generateText(prompt);
    try {
      const parsed = JSON.parse(result);
      return ok(res, { 
        title: parsed.title || hint, 
        description: parsed.description || result,
        brand: parsed.brand || "",
        parameters: parsed.parameters || "",
        purchaseDate: parsed.purchaseDate || ""
      });
    } catch (e) {
      return ok(res, { title: hint, description: result, brand: "", parameters: "", purchaseDate: "" });
    }
  }),
);

export default router;
