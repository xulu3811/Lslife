import { useEffect, useState } from 'react';
import { Store, Search, ShieldAlert, ShieldCheck, Settings as SettingsIcon } from 'lucide-react';
import api from '../utils/api';

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
  
  // Settings modal state
  const [showSettings, setShowSettings] = useState(false);
  const [requireApproval, setRequireApproval] = useState(false);
  const [savingSettings, setSavingSettings] = useState(false);

  const fetchMerchants = async () => {
    setLoading(true);
    try {
      const res = await api.get('/merchants', { params: { search, status } });
      setMerchants(res.data.data.list);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const fetchSettings = async () => {
    try {
      const res = await api.get('/settings');
      setRequireApproval(res.data.data?.merchant_require_approval === 'true');
    } catch (e) {
      console.error('Failed to fetch settings', e);
    }
  };

  useEffect(() => {
    fetchMerchants();
  }, [search, status]);

  useEffect(() => {
    fetchSettings();
  }, []);

  const handleStatusChange = async (id: string, newStatus: string) => {
    if (!window.confirm(`确认将商户状态修改为 ${newStatus}?`)) return;
    try {
      await api.post(`/merchants/${id}/status`, { status: newStatus });
      fetchMerchants();
    } catch (e: any) {
      alert(e.response?.data?.message || '操作失败');
    }
  };

  const saveSettings = async () => {
    setSavingSettings(true);
    try {
      await api.put('/settings', {
        key: 'merchant_require_approval',
        value: requireApproval ? 'true' : 'false'
      });
      setShowSettings(false);
      alert('设置已保存');
    } catch (e: any) {
      alert('保存失败');
    } finally {
      setSavingSettings(false);
    }
  };

  return (
    <div style={{ padding: 16 }}>
      <div className="flex justify-between items-center mb-4">
        <h1 style={{ display: 'flex', alignItems: 'center', margin: 0 }}>
          <Store style={{ marginRight: 8 }} /> 商家管控台
        </h1>
        <button className="glass-button" onClick={() => setShowSettings(true)}>
          <SettingsIcon size={18} /> 入驻设置
        </button>
      </div>

      <div className="glass-panel p-4 mb-4 flex gap-4">
        <div style={{ flex: 1, position: 'relative' }}>
          <Search style={{ position: 'absolute', left: 12, top: 12, color: 'var(--text-secondary)' }} size={20} />
          <input
            type="text"
            placeholder="搜索商户名称/手机号..."
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
          <option value="active">正常营业</option>
          <option value="pending">待审核</option>
          <option value="offline">强制下线</option>
        </select>
      </div>

      <div className="glass-panel">
        {loading ? (
          <div style={{ padding: 40, textAlign: 'center' }}>加载中...</div>
        ) : (
          <table className="glass-table">
            <thead>
              <tr>
                <th>商户名</th>
                <th>分类</th>
                <th>联系电话</th>
                <th>评分/总销量</th>
                <th>状态</th>
                <th style={{ textAlign: 'right' }}>操作</th>
              </tr>
            </thead>
            <tbody>
              {merchants.map((merchant) => (
                <tr key={merchant.id}>
                  <td>
                    <div style={{ fontWeight: 600 }}>{merchant.name}</div>
                    <div style={{ fontSize: 12, color: 'var(--text-secondary)' }}>
                      入驻: {new Date(merchant.createdAt).toLocaleDateString()}
                    </div>
                  </td>
                  <td>{merchant.category || '默认分类'}</td>
                  <td>{merchant.phone}</td>
                  <td>
                    <div style={{ color: '#f59e0b', fontWeight: 600 }}>★ {merchant.rating}</div>
                    <div style={{ fontSize: 12, color: 'var(--text-secondary)' }}>已售 {merchant.sales}</div>
                  </td>
                  <td>
                    <span className={`badge badge-${merchant.status}`}>
                      {merchant.status === 'active' ? '正常营业' : merchant.status === 'pending' ? '待审核' : '已下线'}
                    </span>
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <div className="flex gap-2" style={{ justifyContent: 'flex-end' }}>
                      {merchant.status === 'pending' && (
                        <>
                          <button className="glass-button" style={{ padding: '6px 12px' }} onClick={() => handleStatusChange(merchant.id, 'active')}>
                            <ShieldCheck size={16} /> 通过
                          </button>
                          <button className="glass-button danger" style={{ padding: '6px 12px' }} onClick={() => handleStatusChange(merchant.id, 'offline')}>
                            <ShieldAlert size={16} /> 驳回
                          </button>
                        </>
                      )}
                      {merchant.status === 'active' && (
                        <button className="glass-button danger" style={{ padding: '6px 12px' }} onClick={() => handleStatusChange(merchant.id, 'offline')} title="强制下线">
                          <ShieldAlert size={16} /> 下线
                        </button>
                      )}
                      {merchant.status === 'offline' && (
                        <button className="glass-button" style={{ padding: '6px 12px' }} onClick={() => handleStatusChange(merchant.id, 'active')} title="恢复营业">
                          <ShieldCheck size={16} /> 恢复
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
              {merchants.length === 0 && (
                <tr>
                  <td colSpan={6} style={{ textAlign: 'center', padding: 40 }}>暂无商户</td>
                </tr>
              )}
            </tbody>
          </table>
        )}
      </div>

      {/* Settings Modal */}
      {showSettings && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 100
        }}>
          <div className="glass-panel p-6" style={{ width: 400, background: 'var(--bg-color)' }}>
            <h2 style={{ marginTop: 0 }}>商户入驻设置</h2>
            <div style={{ display: 'flex', alignItems: 'center', margin: '24px 0' }}>
              <input 
                type="checkbox" 
                id="requireApproval"
                checked={requireApproval}
                onChange={(e) => setRequireApproval(e.target.checked)}
                style={{ width: 18, height: 18, marginRight: 8 }}
              />
              <label htmlFor="requireApproval" style={{ fontWeight: 500, cursor: 'pointer' }}>
                新商户入驻需要人工审核
              </label>
            </div>
            <p style={{ fontSize: 12, color: 'var(--text-secondary)', marginBottom: 24 }}>
              开启后，新注册的商户状态将默认为"待审核"，需在后台手动通过后方可营业。
            </p>
            <div className="flex gap-2" style={{ justifyContent: 'flex-end' }}>
              <button 
                className="glass-button" 
                style={{ background: 'transparent', color: 'var(--text-primary)', border: '1px solid var(--surface-border)' }}
                onClick={() => setShowSettings(false)}
              >
                取消
              </button>
              <button className="glass-button" onClick={saveSettings} disabled={savingSettings}>
                {savingSettings ? '保存中...' : '保存设置'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
