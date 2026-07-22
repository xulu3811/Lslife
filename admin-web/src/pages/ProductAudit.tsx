import { useEffect, useState } from 'react';
import { ShoppingBag, Search, Ban, CheckCircle } from 'lucide-react';
import api from '../utils/axios';

interface Product {
  id: string;
  name: string;
  desc: string;
  price: number;
  sales: number;
  status: string;
  merchant?: { name: string };
  createdAt: string;
}

export function ProductAudit() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const res = await api.get('/admin/products', { params: { search, status } });
      setProducts(res.data.data.list);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, [search, status]);

  const handleStatusChange = async (id: string, newStatus: string) => {
    if (!window.confirm(`确认将商品状态修改为 ${newStatus === 'active' ? '正常' : '违规下架'}?`)) return;
    try {
      await api.post(`/admin/products/${id}/status`, { status: newStatus });
      fetchProducts();
    } catch (e: any) {
      alert(e.response?.data?.message || '操作失败');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-900 dark:text-white flex items-center">
          <ShoppingBag className="mr-2" /> 全局商品监管库
        </h1>
      </div>

      <div className="bg-white dark:bg-gray-800 p-4 rounded-xl shadow-sm flex flex-wrap gap-4">
        <div className="flex-1 min-w-[200px] relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 w-5 h-5" />
          <input
            type="text"
            placeholder="搜索商品名/描述..."
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
          <option value="active">正常在售</option>
          <option value="offline">已下架</option>
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
                  <th className="p-4 font-medium">商品名称</th>
                  <th className="p-4 font-medium">所属商户</th>
                  <th className="p-4 font-medium">价格/销量</th>
                  <th className="p-4 font-medium">状态</th>
                  <th className="p-4 font-medium text-right">干预操作</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
                {products.map((product) => (
                  <tr key={product.id} className="hover:bg-gray-50 dark:hover:bg-gray-700/50">
                    <td className="p-4">
                      <div className="font-medium text-gray-900 dark:text-white">{product.name}</div>
                      <div className="text-xs text-gray-500 max-w-xs truncate" title={product.desc}>{product.desc}</div>
                    </td>
                    <td className="p-4 text-gray-900 dark:text-white font-medium">{product.merchant?.name || '未知商户'}</td>
                    <td className="p-4">
                      <div className="text-red-500 font-medium">¥ {product.price.toFixed(2)}</div>
                      <div className="text-sm text-gray-500">已售 {product.sales}</div>
                    </td>
                    <td className="p-4">
                      <span className={`px-2 py-1 rounded-full text-xs font-medium
                        ${product.status === 'active' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                        {product.status === 'active' ? '正常在售' : '强制下架'}
                      </span>
                    </td>
                    <td className="p-4 text-right space-x-2">
                      {product.status === 'active' ? (
                        <button onClick={() => handleStatusChange(product.id, 'offline')} className="p-2 text-red-600 hover:bg-red-50 rounded-lg flex items-center justify-end w-full" title="强制下架">
                          <Ban className="w-5 h-5 mr-1" /> 违规下架
                        </button>
                      ) : (
                        <button onClick={() => handleStatusChange(product.id, 'active')} className="p-2 text-green-600 hover:bg-green-50 rounded-lg flex items-center justify-end w-full" title="解除限制">
                          <CheckCircle className="w-5 h-5 mr-1" /> 解除限制
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
                {products.length === 0 && (
                  <tr><td colSpan={5} className="p-8 text-center text-gray-500">暂无商品</td></tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
