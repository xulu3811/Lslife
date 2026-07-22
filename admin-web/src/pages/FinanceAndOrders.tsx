import { useEffect, useState } from 'react';
import { Package, Search, Play, CheckCircle, XCircle, Wallet, TrendingUp, DollarSign, Activity } from 'lucide-react';
import api from '../utils/axios';

interface Payment {
  pointsUsed: number;
  cashAmount: number;
}

interface Order {
  id: string;
  orderNo: string;
  merchantName: string;
  totalAmount: number;
  status: string;
  deliveryName: string;
  deliveryPhone: string;
  deliveryAddress: string;
  createdAt: string;
  user?: { nickname: string; phone: string };
  items: { name: string; quantity: number; price: number }[];
  payment?: Payment;
}

interface Transaction {
  id: string;
  type: string;
  amount: number;
  bizType: string;
  balanceBefore: number;
  balanceAfter: number;
  createdAt: string;
  user?: { nickname: string; phone: string };
  merchant?: { name: string };
}

export default function FinanceAndOrders() {
  const [activeTab, setActiveTab] = useState<'orders' | 'finance'>('finance');
  
  // Finance State
  const [stats, setStats] = useState({ totalCashIncome: 0, totalPointsUsed: 0, totalSettledToMerchants: 0 });
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  
  // Orders State
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');

  const fetchFinance = async () => {
    try {
      const [stRes, txRes] = await Promise.all([
        api.get('/admin/finance/stats'),
        api.get('/admin/finance/transactions?limit=50')
      ]);
      setStats(stRes.data.data);
      setTransactions(txRes.data.data.list);
    } catch (e) {
      console.error(e);
    }
  };

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const res = await api.get('/admin/orders', { params: { search, status } });
      setOrders(res.data.data.list);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (activeTab === 'orders') fetchOrders();
    if (activeTab === 'finance') fetchFinance();
  }, [search, status, activeTab]);

  const handleAction = async (id: string, action: string) => {
    if (!window.confirm(`确认执行操作：${action}?`)) return;
    try {
      await api.post(`/admin/orders/${id}/action`, { action });
      fetchOrders();
    } catch (e: any) {
      alert(e.response?.data?.message || '操作失败');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-900 dark:text-white flex items-center">
          <Wallet className="mr-2 text-blue-500" /> 资金与订单管理中心
        </h1>
        <div className="flex bg-gray-100 dark:bg-gray-800 p-1 rounded-lg">
          <button
            onClick={() => setActiveTab('finance')}
            className={`px-4 py-2 rounded-md font-medium text-sm transition-colors ${activeTab === 'finance' ? 'bg-white dark:bg-gray-700 shadow-sm text-blue-600 dark:text-blue-400' : 'text-gray-500 hover:text-gray-700 dark:hover:text-gray-300'}`}
          >
            资金对账大盘
          </button>
          <button
            onClick={() => setActiveTab('orders')}
            className={`px-4 py-2 rounded-md font-medium text-sm transition-colors ${activeTab === 'orders' ? 'bg-white dark:bg-gray-700 shadow-sm text-blue-600 dark:text-blue-400' : 'text-gray-500 hover:text-gray-700 dark:hover:text-gray-300'}`}
          >
            订单监控台
          </button>
        </div>
      </div>

      {activeTab === 'finance' && (
        <div className="space-y-6 fade-in">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="bg-white dark:bg-gray-800 p-6 rounded-xl shadow-sm border-l-4 border-green-500">
              <div className="flex items-center text-gray-500 dark:text-gray-400 mb-2">
                <DollarSign className="w-5 h-5 mr-2" /> 微信支付总入账
              </div>
              <div className="text-3xl font-bold text-gray-900 dark:text-white">¥ {stats.totalCashIncome.toFixed(2)}</div>
            </div>
            <div className="bg-white dark:bg-gray-800 p-6 rounded-xl shadow-sm border-l-4 border-purple-500">
              <div className="flex items-center text-gray-500 dark:text-gray-400 mb-2">
                <Activity className="w-5 h-5 mr-2" /> 全站消耗积分
              </div>
              <div className="text-3xl font-bold text-gray-900 dark:text-white">{stats.totalPointsUsed.toLocaleString()} 积分</div>
            </div>
            <div className="bg-white dark:bg-gray-800 p-6 rounded-xl shadow-sm border-l-4 border-blue-500">
              <div className="flex items-center text-gray-500 dark:text-gray-400 mb-2">
                <TrendingUp className="w-5 h-5 mr-2" /> 累计商户结算金
              </div>
              <div className="text-3xl font-bold text-gray-900 dark:text-white">¥ {stats.totalSettledToMerchants.toFixed(2)}</div>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
            <div className="p-4 border-b dark:border-gray-700 font-semibold text-gray-700 dark:text-gray-300">
              全站实时资金流水 (Ledger)
            </div>
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-gray-50 dark:bg-gray-700/50 text-gray-600 dark:text-gray-300">
                  <tr>
                    <th className="p-4 font-medium">流水时间</th>
                    <th className="p-4 font-medium">用户/商家</th>
                    <th className="p-4 font-medium">业务类型</th>
                    <th className="p-4 font-medium text-right">金额变动</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
                  {transactions.map((tx) => (
                    <tr key={tx.id} className="hover:bg-gray-50 dark:hover:bg-gray-700/50">
                      <td className="p-4 text-gray-500">{new Date(tx.createdAt).toLocaleString()}</td>
                      <td className="p-4 font-medium dark:text-gray-300">
                        {tx.merchant ? `【商】${tx.merchant.name}` : tx.user ? `【客】${tx.user.nickname || tx.user.phone}` : '系统'}
                      </td>
                      <td className="p-4">
                        <span className={`px-2 py-1 rounded-md text-xs font-medium 
                          ${tx.bizType === 'settlement' ? 'bg-blue-100 text-blue-700' :
                            tx.bizType === 'order_pay' ? 'bg-orange-100 text-orange-700' :
                            tx.bizType === 'recharge' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-700'}`}>
                          {tx.bizType === 'settlement' ? '商户结算' : tx.bizType === 'order_pay' ? '订单支付' : tx.bizType === 'recharge' ? '积分充值' : tx.bizType}
                        </span>
                      </td>
                      <td className={`p-4 text-right font-bold ${tx.amount > 0 ? 'text-green-500' : 'text-red-500'}`}>
                        {tx.amount > 0 ? '+' : ''}{tx.amount} {tx.type === 'points' ? '积分' : '元'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'orders' && (
        <div className="space-y-4 fade-in">
          <div className="bg-white dark:bg-gray-800 p-4 rounded-xl shadow-sm flex flex-wrap gap-4">
            <div className="flex-1 min-w-[200px] relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 w-5 h-5" />
              <input
                type="text"
                placeholder="搜索订单号/商家名/手机号..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="w-full pl-10 pr-4 py-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600 dark:text-white focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <select
              value={status}
              onChange={(e) => setStatus(e.target.value)}
              className="border rounded-lg px-4 py-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
            >
              <option value="">全部状态</option>
              <option value="pending">待支付</option>
              <option value="paid">已支付/待接单</option>
              <option value="preparing">备餐中/处理中</option>
              <option value="delivering">配送中</option>
              <option value="delivered">已完成 (已结算)</option>
              <option value="cancelled">已取消</option>
            </select>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
            {loading ? (
              <div className="p-8 text-center text-gray-500">加载中...</div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm">
                  <thead className="bg-gray-50 dark:bg-gray-700/50 text-gray-600 dark:text-gray-300">
                    <tr>
                      <th className="p-4 font-medium">订单号/时间</th>
                      <th className="p-4 font-medium">商家/卖家</th>
                      <th className="p-4 font-medium">买家及地址</th>
                      <th className="p-4 font-medium">资金构成</th>
                      <th className="p-4 font-medium">状态</th>
                      <th className="p-4 font-medium text-right">干预操作</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
                    {orders.map((order) => (
                      <tr key={order.id} className="hover:bg-gray-50 dark:hover:bg-gray-700/50">
                        <td className="p-4">
                          <div className="font-medium text-gray-900 dark:text-white">{order.orderNo}</div>
                          <div className="text-xs text-gray-500">{new Date(order.createdAt).toLocaleString()}</div>
                        </td>
                        <td className="p-4">
                          <div className="font-medium text-gray-900 dark:text-white">{order.merchantName}</div>
                          <div className="text-xs text-gray-500">{order.items.length} 件商品</div>
                        </td>
                        <td className="p-4">
                          <div className="font-medium text-gray-900 dark:text-white">{order.deliveryName} ({order.deliveryPhone})</div>
                          <div className="text-xs text-gray-500 line-clamp-1" title={order.deliveryAddress}>{order.deliveryAddress}</div>
                        </td>
                        <td className="p-4">
                          <div className="font-bold text-gray-900 dark:text-white mb-1">总计: ¥{order.totalAmount.toFixed(2)}</div>
                          {order.payment && (
                            <div className="text-xs space-y-1">
                              {order.payment.pointsUsed > 0 && <div className="text-purple-600">积分抵扣: -{order.payment.pointsUsed}分</div>}
                              {order.payment.cashAmount > 0 && <div className="text-green-600">微信实付: ¥{order.payment.cashAmount.toFixed(2)}</div>}
                            </div>
                          )}
                        </td>
                        <td className="p-4">
                          <span className={`px-2 py-1 rounded-full text-xs font-medium
                            ${order.status === 'delivered' ? 'bg-green-100 text-green-700' :
                              order.status === 'cancelled' ? 'bg-red-100 text-red-700' :
                              'bg-blue-100 text-blue-700'}`}>
                            {order.status === 'pending' ? '待支付' :
                             order.status === 'paid' ? '已支付' :
                             order.status === 'preparing' ? '处理中' :
                             order.status === 'delivering' ? '配送中' :
                             order.status === 'delivered' ? '已完成' :
                             order.status === 'cancelled' ? '已取消' : order.status}
                          </span>
                        </td>
                        <td className="p-4 text-right space-x-2">
                          <button onClick={() => handleAction(order.id, 'refund')} className="p-2 text-red-600 hover:bg-red-50 rounded-lg" title="强制退款/取消"><XCircle className="w-5 h-5" /></button>
                          <button onClick={() => handleAction(order.id, 'assign_rider')} className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg" title="指派模拟骑手"><Play className="w-5 h-5" /></button>
                          <button onClick={() => handleAction(order.id, 'complete')} className="p-2 text-green-600 hover:bg-green-50 rounded-lg" title="强制完成并结算抽成"><CheckCircle className="w-5 h-5" /></button>
                        </td>
                      </tr>
                    ))}
                    {orders.length === 0 && (
                      <tr><td colSpan={6} className="p-8 text-center text-gray-500">暂无订单</td></tr>
                    )}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
