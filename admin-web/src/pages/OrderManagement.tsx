import { useEffect, useState } from 'react';
import { Package, Search, Play, CheckCircle, XCircle } from 'lucide-react';
import api from '../utils/axios';

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
}

export function OrderManagement() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');

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
    fetchOrders();
  }, [search, status]);

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
          <Package className="mr-2" /> 订单监控台
        </h1>
      </div>

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
          <option value="delivered">已完成</option>
          <option value="cancelled">已取消</option>
        </select>
      </div>

      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
        {loading ? (
          <div className="p-8 text-center text-gray-500">加载中...</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left">
              <thead className="bg-gray-50 dark:bg-gray-700/50 text-gray-600 dark:text-gray-300">
                <tr>
                  <th className="p-4 font-medium">订单号/时间</th>
                  <th className="p-4 font-medium">商家/卖家</th>
                  <th className="p-4 font-medium">买家及地址</th>
                  <th className="p-4 font-medium">总金额</th>
                  <th className="p-4 font-medium">状态</th>
                  <th className="p-4 font-medium text-right">干预操作</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
                {orders.map((order) => (
                  <tr key={order.id} className="hover:bg-gray-50 dark:hover:bg-gray-700/50">
                    <td className="p-4">
                      <div className="font-medium text-gray-900 dark:text-white">{order.orderNo}</div>
                      <div className="text-sm text-gray-500">{new Date(order.createdAt).toLocaleString()}</div>
                    </td>
                    <td className="p-4">
                      <div className="font-medium text-gray-900 dark:text-white">{order.merchantName}</div>
                      <div className="text-sm text-gray-500">{order.items.length} 件商品</div>
                    </td>
                    <td className="p-4">
                      <div className="font-medium text-gray-900 dark:text-white">{order.deliveryName} ({order.deliveryPhone})</div>
                      <div className="text-sm text-gray-500 line-clamp-1" title={order.deliveryAddress}>{order.deliveryAddress}</div>
                    </td>
                    <td className="p-4 font-medium text-red-500">¥{order.totalAmount.toFixed(2)}</td>
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
                      <button onClick={() => handleAction(order.id, 'complete')} className="p-2 text-green-600 hover:bg-green-50 rounded-lg" title="强制完成"><CheckCircle className="w-5 h-5" /></button>
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
  );
}
