import { useEffect, useState } from 'react';
import api from '../utils/axios';
import { CheckCircle, XCircle } from 'lucide-react';

export default function KycAudit() {
  const [users, setUsers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState<'pending' | 'verified' | 'none'>('pending');

  const fetchUsers = () => {
    setLoading(true);
    api.get(`/kyc?status=${tab}`)
      .then(res => setUsers(res.data.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchUsers();
  }, [tab]);

  const handleAudit = (id: string, action: 'approve' | 'reject') => {
    if (!window.confirm(`确定要${action === 'approve' ? '通过' : '驳回'}该实名认证吗？`)) return;
    
    api.post(`/kyc/${id}/audit`, { action })
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
        <div style={{ display: 'flex', gap: '24px', borderBottom: '1px solid var(--surface-border)', marginBottom: '24px' }}>
          <div 
            onClick={() => setTab('pending')}
            style={{ paddingBottom: '12px', cursor: 'pointer', fontWeight: tab === 'pending' ? 600 : 400, color: tab === 'pending' ? 'var(--primary)' : 'var(--text-secondary)', borderBottom: tab === 'pending' ? '2px solid var(--primary)' : 'none' }}
          >
            待审核
          </div>
          <div 
            onClick={() => setTab('verified')}
            style={{ paddingBottom: '12px', cursor: 'pointer', fontWeight: tab === 'verified' ? 600 : 400, color: tab === 'verified' ? 'var(--primary)' : 'var(--text-secondary)', borderBottom: tab === 'verified' ? '2px solid var(--primary)' : 'none' }}
          >
            已通过
          </div>
          <div 
            onClick={() => setTab('none')}
            style={{ paddingBottom: '12px', cursor: 'pointer', fontWeight: tab === 'none' ? 600 : 400, color: tab === 'none' ? 'var(--primary)' : 'var(--text-secondary)', borderBottom: tab === 'none' ? '2px solid var(--primary)' : 'none' }}
          >
            未实名/已驳回
          </div>
        </div>
        
        {loading ? (
          <p>加载中...</p>
        ) : users.length === 0 ? (
          <p style={{ color: 'var(--text-secondary)' }}>
            {tab === 'pending' ? '暂无需要审核的实名认证。' : '暂无相关记录。'}
          </p>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {users.map(user => (
              <div key={user.id} style={{ border: '1px solid var(--surface-border)', borderRadius: '12px', padding: '16px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '12px' }}>
                  <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                    <span style={{ fontWeight: 600, fontSize: '16px' }}>{user.realName || '未知姓名'}</span>
                    <span style={{ fontSize: '12px', padding: '2px 8px', background: 'var(--surface-variant)', color: 'var(--primary)', borderRadius: '4px' }}>{user.phone}</span>
                  </div>
                  <div style={{ display: 'flex', gap: '8px' }}>
                    {tab === 'pending' && (
                      <>
                        <button 
                          onClick={() => handleAudit(user.id, 'approve')}
                          style={{ background: 'var(--success)', color: 'white', border: 'none', padding: '6px 16px', borderRadius: '4px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '4px' }}
                        >
                          <CheckCircle size={16} /> 通过
                        </button>
                        <button 
                          onClick={() => handleAudit(user.id, 'reject')}
                          style={{ background: 'var(--danger)', color: 'white', border: 'none', padding: '6px 16px', borderRadius: '4px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '4px' }}
                        >
                          <XCircle size={16} /> 驳回
                        </button>
                      </>
                    )}
                  </div>
                </div>
                
                <div style={{ marginTop: '12px', fontSize: '14px', color: 'var(--text-secondary)', display: 'flex', gap: '16px', flexWrap: 'wrap', flexDirection: 'column' }}>
                  <span>用户昵称: {user.nickname}</span>
                  <span>身份证哈希: {user.idCardHash || '无'}</span>
                  <span>最近更新: {new Date(user.updatedAt).toLocaleString()}</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
