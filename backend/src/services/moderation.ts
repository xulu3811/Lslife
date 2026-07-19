import { env } from '../config/env.js';

// 简易本地敏感词 (包含违法、涉政、黑话等)
const BLOCK_WORDS = [
  // 黄赌毒
  '黄', '赌', '毒', '色情', '嫖娼', '赌场', '毒品', '冰毒', '海洛因',
  // 政治言论与领导人
  '政治言论', '反党', '反社会', '暴乱', '习近平', '李克强', '毛泽东', '邓小平', '江泽民', '胡锦涛',
  // 组织与政府
  '共产党', '中共', '政府', '公安', '警察',
  // 违法黑话术语
  '黑话', '代考', '枪手', '办证', '发票', '洗钱', '走私', '套现', '刷单', '原味'
];

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
  return { pass: true, status: 'pending_review' };
}
