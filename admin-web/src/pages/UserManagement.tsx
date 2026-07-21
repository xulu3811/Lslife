import { useEffect, useState } from 'react';
import api from '../utils/axios';
import { Search, DollarSign, Award } from 'lucide-react';

export default function UserManagement() {
  const [users, setUsers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  const fetchUsers = () => {
    setLoading(true);
    api.get(`/users${search ? `?search=${encodeURIComponent(search)}` : ''}`)
      .then(res => setUsers(res.data.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchUsers();
  }, [search]);

  const handleRecharge = (id: string, currentBalance: number) => {
    const amountStr = window.prompt(`当前余额: ￥${currentBalance}\n请输入要充值或扣减的金额 (扣减请输入负数):`, '0');
    if (!amountStr) return;
    const amount = parseFloat(amountStr);
    if (isNaN(amount) || amount === 0) return;

    api.put(`/users/${id}/balance`, { amount })
      .then(res => {
        alert(res.data.message);
        fetchUsers();
      })
      .catch(err => {
        alert(err.response?.data?.message || '操作失败');
      });
  };

  const handleMembership = (id: string, currentTier: string) => {
    const tiers = ['free', 'vip', 'premium'];
    const newTier = window.prompt(`当前身份: ${currentTier}\n请输入新的身份等级 (free / vip / premium):`, currentTier);
    if (!newTier || !tiers.includes(newTier) || newTier === currentTier) return;

    api.put(`/users/${id}/membership`, { tier: newTier })
      .then(res => {
        alert(res.data.message);
        fetchUsers();
      })
      .catch(err => {
        alert(err.response?.data?.message || '操作失败');
      });
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      <div className="glass-panel" style={{ padding: '24px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
          <h2 style={{ margin: 0, fontSize: '18px' }}>用户管理大盘</h2>
          
          <div style={{ position: 'relative', width: '240px' }}>
            <Search size={16} style={{ position: 'absolute', left: '12px', top: '10px', color: 'var(--text-secondary)' }} />
            <input 
              type="text" 
              placeholder="搜索手机号或昵称" 
              className="glass-input"
              style={{ paddingLeft: '36px', height: '36px' }}
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
        </div>

        {loading ? (
          <p>加载中...</p>
        ) : users.length === 0 ? (
          <p style={{ color: 'var(--text-secondary)' }}>未找到相关用户</p>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--surface-border)', color: 'var(--text-secondary)', fontSize: '14px' }}>
                  <th style={{ padding: '12px 16px' }}>用户信息</th>
                  <th style={{ padding: '12px 16px' }}>手机号</th>
                  <th style={{ padding: '12px 16px' }}>会员等级</th>
                  <th style={{ padding: '12px 16px' }}>钱包余额</th>
                  <th style={{ padding: '12px 16px' }}>实名状态</th>
                  <th style={{ padding: '12px 16px' }}>注册时间</th>
                  <th style={{ padding: '12px 16px' }}>操作</th>
                </tr>
              </thead>
              <tbody>
                {users.map(user => (
                  <tr key={user.id} style={{ borderBottom: '1px solid var(--surface-border)' }}>
                    <td style={{ padding: '12px 16px' }}>
                      <div style={{ fontWeight: 500 }}>{user.nickname}</div>
                      <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>ID: {user.id.slice(0, 8)}...</div>
                    </td>
                    <td style={{ padding: '12px 16px' }}>{user.phone}</td>
                    <td style={{ padding: '12px 16px' }}>
                      <span style={{ 
                        padding: '4px 8px', 
                        borderRadius: '4px', 
                        fontSize: '12px',
                        background: user.membershipTier === 'free' ? 'var(--surface-variant)' : 'rgba(245, 158, 11, 0.1)',
                        color: user.membershipTier === 'free' ? 'var(--text-secondary)' : '#f59e0b',
                        fontWeight: user.membershipTier !== 'free' ? 600 : 400
                      }}>
                        {user.membershipTier.toUpperCase()}
                      </span>
                    </td>
                    <td style={{ padding: '12px 16px', fontWeight: 500 }}>
                      ￥{user.walletBalance.toFixed(2)}
                    </td>
                    <td style={{ padding: '12px 16px' }}>
                      <span style={{ 
                        color: user.realNameStatus === 'verified' ? 'var(--success)' : 
                               user.realNameStatus === 'pending' ? '#f59e0b' : 'var(--text-secondary)' 
                      }}>
                        {user.realNameStatus === 'verified' ? '已实名' : 
                         user.realNameStatus === 'pending' ? '待审核' : '未实名'}
                      </span>
                    </td>
                    <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--text-secondary)' }}>
                      {new Date(user.createdAt).toLocaleDateString()}
                    </td>
                    <td style={{ padding: '12px 16px' }}>
                      <div style={{ display: 'flex', gap: '8px' }}>
                        <button 
                          onClick={() => handleRecharge(user.id, user.walletBalance)}
                          style={{ background: 'transparent', color: 'var(--primary)', border: '1px solid var(--primary)', padding: '4px 8px', borderRadius: '4px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '4px', fontSize: '12px' }}
                        >
                          <DollarSign size={14} /> 调额
                        </button>
                        <button 
                          onClick={() => handleMembership(user.id, user.membershipTier)}
                          style={{ background: 'transparent', color: '#f59e0b', border: '1px solid #f59e0b', padding: '4px 8px', borderRadius: '4px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '4px', fontSize: '12px' }}
                        >
                          <Award size={14} /> 身份
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
