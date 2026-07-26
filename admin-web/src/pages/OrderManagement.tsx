import { useEffect, useState } from 'react';
import { Receipt, Search, RefreshCw, Send, CheckCircle, XCircle } from 'lucide-react';
import api from '../utils/api';

interface OrderItem {
  id: string;
  name: string;
  price: number;
  quantity: number;
}

interface Order {
  id: string;
  orderNo: string;
  status: string;
  totalAmount: number;
  merchantName: string;
  createdAt: string;
  items: OrderItem[];
  user: { nickname: string; phone: string };
}

export function OrderManagement() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const res = await api.get('/orders', { params: { search, status } });
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

  const handleAction = async (id: string, action: 'refund' | 'assign_rider' | 'complete', confirmMsg: string) => {
    if (!window.confirm(confirmMsg)) return;
    try {
      await api.post(`/orders/${id}/action`, { action });
      fetchOrders();
      alert('操作成功');
    } catch (e: any) {
      alert(e.response?.data?.message || '操作失败');
    }
  };

  const getStatusBadge = (status: string) => {
    switch(status) {
      case 'pending': return <span className="badge badge-pending">待支付</span>;
      case 'paid': return <span className="badge" style={{ background: '#e0f2fe', color: '#0284c7' }}>已支付</span>;
      case 'preparing': return <span className="badge" style={{ background: '#fef3c7', color: '#d97706' }}>备餐中</span>;
      case 'delivering': return <span className="badge" style={{ background: '#e0e7ff', color: '#4f46e5' }}>配送中</span>;
      case 'delivered': return <span className="badge badge-active">已送达</span>;
      case 'cancelled': return <span className="badge badge-offline">已取消</span>;
      default: return <span className="badge">{status}</span>;
    }
  };

  return (
    <div style={{ padding: 16 }}>
      <div className="flex justify-between items-center mb-4">
        <h1 style={{ display: 'flex', alignItems: 'center', margin: 0 }}>
          <Receipt style={{ marginRight: 8 }} /> 资金与订单管理
        </h1>
        <button className="glass-button" onClick={fetchOrders}>
          <RefreshCw size={18} /> 刷新
        </button>
      </div>

      <div className="glass-panel p-4 mb-4 flex gap-4">
        <div style={{ flex: 1, position: 'relative' }}>
          <Search style={{ position: 'absolute', left: 12, top: 12, color: 'var(--text-secondary)' }} size={20} />
          <input
            type="text"
            placeholder="搜索订单号/商家名/手机号..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="glass-input"
            style={{ paddingLeft: 40 }}
          />
        </div>
        <select
          value={status}
          onChange={(e) => setStatus(e.target.value)}
          className="glass-input"
          style={{ width: 200 }}
        >
          <option value="">全部状态</option>
          <option value="pending">待支付</option>
          <option value="paid">已支付</option>
          <option value="delivering">配送中</option>
          <option value="delivered">已完成</option>
          <option value="cancelled">已取消</option>
        </select>
      </div>

      <div className="glass-panel">
        {loading ? (
          <div style={{ padding: 40, textAlign: 'center' }}>加载中...</div>
        ) : (
          <table className="glass-table">
            <thead>
              <tr>
                <th>订单信息</th>
                <th>买卖双方</th>
                <th>商品详情</th>
                <th>总金额</th>
                <th>状态</th>
                <th style={{ textAlign: 'right' }}>操作干预</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => (
                <tr key={order.id}>
                  <td>
                    <div style={{ fontWeight: 600, fontSize: 14 }}>{order.orderNo}</div>
                    <div style={{ fontSize: 12, color: 'var(--text-secondary)' }}>
                      {new Date(order.createdAt).toLocaleString()}
                    </div>
                  </td>
                  <td>
                    <div style={{ fontSize: 14 }}>买家: {order.user?.nickname} ({order.user?.phone})</div>
                    <div style={{ fontSize: 14 }}>卖家: {order.merchantName || '个人卖家'}</div>
                  </td>
                  <td>
                    <div style={{ fontSize: 12 }}>
                      {order.items.slice(0, 2).map((item, idx) => (
                        <div key={idx}>{item.name} x{item.quantity}</div>
                      ))}
                      {order.items.length > 2 && <div>...等{order.items.length}件商品</div>}
                    </div>
                  </td>
                  <td>
                    <div style={{ fontWeight: 600, color: 'var(--primary)' }}>¥{order.totalAmount.toFixed(2)}</div>
                  </td>
                  <td>
                    {getStatusBadge(order.status)}
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <div className="flex gap-2" style={{ justifyContent: 'flex-end', flexWrap: 'wrap', maxWidth: 200, float: 'right' }}>
                      
                      {/* Can assign rider if paid/preparing */}
                      {['paid', 'preparing'].includes(order.status) && (
                        <button className="glass-button" style={{ padding: '4px 8px', fontSize: 12 }} onClick={() => handleAction(order.id, 'assign_rider', '确认指派虚拟骑手配送？')}>
                          <Send size={14} /> 指派骑手
                        </button>
                      )}
                      
                      {/* Can complete if delivering */}
                      {['delivering'].includes(order.status) && (
                        <button className="glass-button" style={{ padding: '4px 8px', fontSize: 12, background: 'var(--success)' }} onClick={() => handleAction(order.id, 'complete', '确认强制完成订单并进行资金结算？')}>
                          <CheckCircle size={14} /> 强制完成
                        </button>
                      )}

                      {/* Can refund if not delivered/cancelled */}
                      {!['delivered', 'cancelled'].includes(order.status) && (
                        <button className="glass-button danger" style={{ padding: '4px 8px', fontSize: 12 }} onClick={() => handleAction(order.id, 'refund', '确认强制取消并退款？')}>
                          <XCircle size={14} /> 强制退款
                        </button>
                      )}

                    </div>
                  </td>
                </tr>
              ))}
              {orders.length === 0 && (
                <tr>
                  <td colSpan={6} style={{ textAlign: 'center', padding: 40 }}>暂无订单</td>
                </tr>
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
