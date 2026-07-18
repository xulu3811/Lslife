import { env } from '../config/env.js';

// 简易本地敏感词 (生产应接入阿里云/腾讯云内容安全 API + 人审队列)
const BLOCK_WORDS = ['黄赌毒', '诈骗', '暴力', '违法'];

export interface ModerationResult {
  pass: boolean;
  status: 'published' | 'pending_review' | 'rejected';
  note?: string;
}

/**
 * 内容审核。UGC 发布需机审 + 人审, 违规留存。
 * 本地: 命中敏感词直接 rejected; 否则 published。
 * 生产: 机审可疑 -> pending_review 进人审队列。
 */
export function moderateContent(title: string, description: string): ModerationResult {
  if (!env.contentModerationEnabled) {
    return { pass: true, status: 'published' };
  }
  const text = `${title} ${description}`;
  const hit = BLOCK_WORDS.find((w) => text.includes(w));
  if (hit) {
    return { pass: false, status: 'rejected', note: `命中违规词: ${hit}` };
  }
  return { pass: true, status: 'published' };
}
