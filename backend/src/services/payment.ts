import { env } from '../config/env.js';

export type PayChannel = 'wechat' | 'alipay' | 'wallet' | 'mock';

export interface CreatePaymentInput {
  orderNo: string;
  amount: number;
  channel: PayChannel;
  description: string;
}

export interface CreatePaymentResult {
  /** 客户端拉起支付所需参数 (原生 SDK 使用) */
  prepayPayload: Record<string, unknown>;
  transactionId: string;
}

/**
 * 支付服务抽象层。
 * 关键商业化约束: 资金必须走持牌第三方 (微信支付/支付宝), 平台不得自建资金池。
 * 流程: 服务端统一下单 -> 返回 prepay 参数 -> 客户端原生 SDK 拉起 -> 第三方异步回调 -> 服务端验签并置订单为已支付 -> 对账。
 */
export interface PaymentProvider {
  createPayment(input: CreatePaymentInput): Promise<CreatePaymentResult>;
  /** 验签异步回调, 返回 { orderNo, success, transactionId } */
  verifyCallback(rawBody: unknown, headers: Record<string, unknown>): Promise<{ orderNo: string; success: boolean; transactionId: string }>;
}

const mockProvider: PaymentProvider = {
  async createPayment(input) {
    const transactionId = `MOCK${Date.now()}${Math.floor(Math.random() * 1000)}`;
    return {
      transactionId,
      prepayPayload: {
        channel: input.channel,
        mock: true,
        // 演示: 客户端可直接调用 /api/payments/mock-confirm 完成"支付"
        confirmUrl: `/api/payments/mock-confirm`,
        orderNo: input.orderNo,
        amount: input.amount,
      },
    };
  },
  async verifyCallback(rawBody) {
    const body = rawBody as { orderNo: string; transactionId?: string };
    return {
      orderNo: body.orderNo,
      success: true,
      transactionId: body.transactionId ?? `MOCK${Date.now()}`,
    };
  },
};

// TODO: 微信支付 (APP 下单 + V3 验签)
const wechatProvider: PaymentProvider = {
  async createPayment(input) {
    console.log('[Pay:wechat] TODO 微信统一下单', input.orderNo);
    throw new Error('微信支付未配置: 请填写 WECHAT_* 环境变量并实现 wechatProvider');
  },
  async verifyCallback() {
    throw new Error('微信支付回调未实现');
  },
};

export function getPaymentProvider(): PaymentProvider {
  switch (env.payProvider) {
    case 'wechat':
    case 'alipay':
      return wechatProvider;
    default:
      return mockProvider;
  }
}
