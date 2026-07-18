/**
 * 端到端冒烟测试: 覆盖登录 -> 商家 -> 购物车 -> 下单 -> 支付 -> 订单追踪 -> 发布 -> 会员。
 * 需先启动后端 (npm run dev)。运行: npm run smoke
 */
const BASE = process.env.BASE ?? 'http://localhost:4000/api';
let token = '';

async function call(method: string, path: string, body?: unknown) {
  const res = await fetch(BASE + path, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  const json = (await res.json()) as { code: number; message: string; data: any };
  if (json.code !== 0) throw new Error(`${method} ${path} 失败: ${json.message}`);
  return json.data;
}

function assert(cond: unknown, msg: string) {
  if (!cond) throw new Error('断言失败: ' + msg);
  console.log('  ✓', msg);
}

async function main() {
  const phone = '138' + Math.floor(10000000 + Math.random() * 89999999);

  console.log('1) 健康检查');
  const health = await call('GET', '/health');
  assert(health.status === 'up', '服务健康');

  console.log('2) 短信登录');
  const send = await call('POST', '/auth/send-code', { phone });
  assert(send.mockCode, '验证码已发送');
  const login = await call('POST', '/auth/login', { phone, code: send.mockCode });
  token = login.token;
  assert(token, '登录获取 token');

  console.log('3) 实名认证');
  const rn = await call('POST', '/auth/realname', { realName: '莫小美', idCard: '11010119900101001X' });
  assert(rn.realNameStatus === 'verified', '实名成功');

  console.log('4) 商家列表与详情');
  const merchants = await call('GET', '/merchants?sort=rating');
  assert(merchants.list.length > 0, `商家数量 ${merchants.list.length}`);
  const merchant = merchants.list[0];
  const detail = await call('GET', `/merchants/${merchant.id}`);
  assert(detail.items.length > 0, '商家详情含商品');
  const product = detail.items[0];

  console.log('5) 购物车');
  await call('POST', '/cart', { productId: product.id, quantity: 2 });
  const cart = await call('GET', '/cart');
  assert(cart.length === 1 && cart[0].quantity === 2, '购物车含2件');

  console.log('6) 收货地址');
  const addr = await call('POST', '/addresses', { name: '莫小美', phone, address: '连山吉田镇吉祥路88号', isDefault: true });
  assert(addr.isDefault, '默认地址已建');

  console.log('7) 下单 (服务端计算金额)');
  const order = await call('POST', '/orders', {
    merchantId: merchant.id,
    items: [{ productId: product.id, quantity: 2 }],
    deliveryAddress: { name: '莫小美', phone, address: '连山吉田镇吉祥路88号' },
  });
  const expected = product.price * 2 + merchant.deliveryFee;
  assert(Math.abs(order.totalAmount - expected) < 0.001, `订单金额正确 ¥${order.totalAmount}`);

  console.log('8) 支付 (mock)');
  const pay = await call('POST', '/payments/create', { orderId: order.id, channel: 'mock' });
  assert(pay.prepayPayload, '拿到支付参数');
  const confirm = await call('POST', '/payments/mock-confirm', { orderNo: order.orderNo });
  assert(confirm.paid, '支付完成');

  console.log('9) 订单追踪 (实时配送)');
  const tracked = await call('GET', `/orders/${order.id}`);
  assert(['paid', 'preparing', 'delivering', 'delivered'].includes(tracked.status), `订单状态 ${tracked.status}`);
  assert(tracked.delivery && tracked.delivery.rider, '含骑手信息');

  console.log('10) 支付后购物车已清空');
  const cart2 = await call('GET', '/cart');
  assert(cart2.length === 0, '购物车已清空');

  console.log('11) 通知');
  const notif = await call('GET', '/notifications');
  assert(notif.list.length > 0, `收到 ${notif.list.length} 条通知`);

  console.log('12) 同城发布 + 额度');
  const post = await call('POST', '/posts', { category: 'second_hand', title: '九成新自行车', description: '车况极佳, 自提优先', price: 80, images: [] });
  assert(post.status === 'published', '发布成功');
  const quota = await call('GET', '/posts/quota');
  assert(quota.used === 1 && quota.limit === 3, `免费额度 ${quota.used}/${quota.limit}`);

  console.log('13) 会员订阅提升额度');
  const sub = await call('POST', '/membership/subscribe', { tier: 'vip' });
  assert(sub.membershipTier === 'vip', '开通 VIP');
  const quota2 = await call('GET', '/posts/quota');
  assert(quota2.limit === 20, `VIP 额度提升至 ${quota2.limit}`);

  console.log('14) AI 助手');
  const ai = await call('POST', '/ai/recommend', { prompt: '推荐本地特色美食' });
  assert(typeof ai.reply === 'string', 'AI 返回推荐');

  console.log('\n全部冒烟测试通过 ✅');
}

main().catch((e) => {
  console.error('\n冒烟测试失败 ❌:', e.message);
  process.exit(1);
});
