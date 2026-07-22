import { useEffect, useState } from 'react';
import { Store, Search, ShieldAlert, ShieldCheck } from 'lucide-react';
import api from '../utils/axios';

interface Merchant {
  id: string;
  name: string;
  phone: string;
  status: string;
  sales: number;
  rating: number;
  category: string;
  createdAt: string;
}

export function MerchantManagement() {
  const [merchants, setMerchants] = useState<Merchant[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');

  const fetchMerchants = async () => {
    setLoading(true);
    try {
      const res = await api.get('/admin/merchants', { params: { search, status } });
      setMerchants(res.data.data.list);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMerchants();
  }, [search, status]);

  const handleStatusChange = async (id: string, newStatus: string) => {
    if (!window.confirm(`确认将商户状态修改为 ${newStatus === 'active' ? '正常营业' : '强制下线'}?`)) return;
    try {
      await api.post(`/admin/merchants/${id}/status`, { status: newStatus });
      fetchMerchants();
    } catch (e: any) {
      alert(e.response?.data?.message || '操作失败');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-900 dark:text-white flex items-center">
          <Store className="mr-2" /> 商家管控台
        </h1>
      </div>

      <div className="bg-white dark:bg-gray-800 p-4 rounded-xl shadow-sm flex flex-wrap gap-4">
        <div className="flex-1 min-w-[200px] relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 w-5 h-5" />
          <input
            type="text"
            placeholder="搜索商户名称/手机号..."
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
          <option value="active">营业中</option>
          <option value="offline">强制下线</option>
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
                  <th className="p-4 font-medium">商户名</th>
                  <th className="p-4 font-medium">分类</th>
                  <th className="p-4 font-medium">联系电话</th>
                  <th className="p-4 font-medium">评分/总销量</th>
                  <th className="p-4 font-medium">状态</th>
                  <th className="p-4 font-medium text-right">干预操作</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
                {merchants.map((merchant) => (
                  <tr key={merchant.id} className="hover:bg-gray-50 dark:hover:bg-gray-700/50">
                    <td className="p-4">
                      <div className="font-medium text-gray-900 dark:text-white">{merchant.name}</div>
                      <div className="text-xs text-gray-500">入驻: {new Date(merchant.createdAt).toLocaleDateString()}</div>
                    </td>
                    <td className="p-4 text-gray-500">{merchant.category || '默认分类'}</td>
                    <td className="p-4 text-gray-900 dark:text-white">{merchant.phone}</td>
                    <td className="p-4">
                      <div className="text-orange-500 font-medium">★ {merchant.rating}</div>
                      <div className="text-sm text-gray-500">已售 {merchant.sales}</div>
                    </td>
                    <td className="p-4">
                      <span className={`px-2 py-1 rounded-full text-xs font-medium
                        ${merchant.status === 'active' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                        {merchant.status === 'active' ? '正常营业' : '已下线'}
                      </span>
                    </td>
                    <td className="p-4 text-right space-x-2">
                      {merchant.status === 'active' ? (
                        <button onClick={() => handleStatusChange(merchant.id, 'offline')} className="p-2 text-red-600 hover:bg-red-50 rounded-lg flex items-center justify-end w-full" title="强制下线">
                          <ShieldAlert className="w-5 h-5 mr-1" /> 下线
                        </button>
                      ) : (
                        <button onClick={() => handleStatusChange(merchant.id, 'active')} className="p-2 text-green-600 hover:bg-green-50 rounded-lg flex items-center justify-end w-full" title="恢复营业">
                          <ShieldCheck className="w-5 h-5 mr-1" /> 恢复
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
                {merchants.length === 0 && (
                  <tr><td colSpan={6} className="p-8 text-center text-gray-500">暂无商户</td></tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
