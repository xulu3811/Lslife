import { prisma } from '../lib/prisma.js';

const PREPARING_SECONDS = 20;
const DELIVERING_SECONDS = 90;
const TOTAL = PREPARING_SECONDS + DELIVERING_SECONDS;

const USER_LOCATION = { lat: 24.472, lng: 112.081 };

export interface LiveDelivery {
  status: 'preparing' | 'delivering' | 'delivered';
  progress: number;
  secondsRemaining: number;
  rider: { name: string; phone: string; avatar: string; lat: number; lng: number };
}

/**
 * 演示用配送模拟: 依据支付时间线性推进骑手位置。
 * 生产: 由骑手端 App 通过 WebSocket 上报真实 GPS, 覆盖此模拟。
 */
export async function computeLiveDelivery(orderId: string, paidAt: Date | null, merchantLat: number, merchantLng: number): Promise<LiveDelivery> {
  const base = paidAt ?? new Date();
  const elapsed = Math.floor((Date.now() - base.getTime()) / 1000);

  let status: LiveDelivery['status'] = 'preparing';
  let progress = 0;
  let lat = merchantLat;
  let lng = merchantLng;

  if (elapsed < PREPARING_SECONDS) {
    status = 'preparing';
    progress = Math.round((elapsed / PREPARING_SECONDS) * 100);
  } else if (elapsed < TOTAL) {
    status = 'delivering';
    const ratio = (elapsed - PREPARING_SECONDS) / DELIVERING_SECONDS;
    progress = Math.round(ratio * 100);
    lat = merchantLat + (USER_LOCATION.lat - merchantLat) * ratio;
    lng = merchantLng + (USER_LOCATION.lng - merchantLng) * ratio;
  } else {
    status = 'delivered';
    progress = 100;
    lat = USER_LOCATION.lat;
    lng = USER_LOCATION.lng;
  }

  // 持久化最新状态 (供列表查询与推送)
  await prisma.delivery.updateMany({ where: { orderId }, data: { status, progress, riderLat: lat, riderLng: lng } });
  if (['delivering', 'delivered'].includes(status)) {
    await prisma.order.updateMany({ where: { id: orderId, status: { notIn: ['delivered', 'cancelled'] } }, data: { status } });
  }

  return {
    status,
    progress,
    secondsRemaining: Math.max(0, TOTAL - elapsed),
    rider: { name: '阿力 (连山特派骑手)', phone: '139-2244-8800', avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120&h=120&fit=crop&q=80', lat, lng },
  };
}
