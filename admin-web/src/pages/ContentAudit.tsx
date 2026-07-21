import { useEffect, useState } from 'react';
import api from '../utils/axios';
import { CheckCircle, XCircle, AlertTriangle } from 'lucide-react';

export default function ContentAudit() {
  const [posts, setPosts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'pending' | 'published'>('pending');

  const fetchPosts = () => {
    setLoading(true);
    const status = activeTab === 'pending' ? 'pending_review' : 'published';
    api.get(`/posts?status=${status}`)
      .then(res => setPosts(res.data.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchPosts();
  }, [activeTab]);

  const handleAudit = (id: string, action: 'approve' | 'reject', customNote?: string) => {
    const defaultNote = action === 'approve' ? '通过' : '驳回';
    const confirmMsg = action === 'reject' && activeTab === 'published' 
      ? '确定要违规下架该发布吗？' 
      : `确定要${defaultNote}该发布吗？`;
      
    if (!window.confirm(confirmMsg)) return;
    
    const note = customNote || window.prompt('请输入操作备注 (选填):', '');
    
    api.post(`/posts/${id}/audit`, { action, note })
      .then(res => {
        alert(res.data.message);
        fetchPosts();
      })
      .catch(err => {
        alert(err.response?.data?.message || '操作失败');
      });
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      <div className="glass-panel" style={{ padding: '24px' }}>
        <div style={{ display: 'flex', gap: '16px', marginBottom: '24px', borderBottom: '1px solid var(--surface-border)', paddingBottom: '12px' }}>
          <button
            onClick={() => setActiveTab('pending')}
            style={{
              background: 'transparent',
              border: 'none',
              fontSize: '16px',
              fontWeight: activeTab === 'pending' ? 600 : 400,
              color: activeTab === 'pending' ? 'var(--primary)' : 'var(--text-secondary)',
              cursor: 'pointer',
              position: 'relative'
            }}
          >
            待审核
            {activeTab === 'pending' && (
              <div style={{ position: 'absolute', bottom: '-13px', left: 0, right: 0, height: '2px', background: 'var(--primary)' }} />
            )}
          </button>
          <button
            onClick={() => setActiveTab('published')}
            style={{
              background: 'transparent',
              border: 'none',
              fontSize: '16px',
              fontWeight: activeTab === 'published' ? 600 : 400,
              color: activeTab === 'published' ? 'var(--primary)' : 'var(--text-secondary)',
              cursor: 'pointer',
              position: 'relative'
            }}
          >
            已发布管理
            {activeTab === 'published' && (
              <div style={{ position: 'absolute', bottom: '-13px', left: 0, right: 0, height: '2px', background: 'var(--primary)' }} />
            )}
          </button>
        </div>
        
        {loading ? (
          <p>加载中...</p>
        ) : posts.length === 0 ? (
          <p style={{ color: 'var(--text-secondary)' }}>暂无记录。</p>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {posts.map(post => (
              <div key={post.id} style={{ border: '1px solid var(--surface-border)', borderRadius: '12px', padding: '16px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '12px' }}>
                  <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                    <span style={{ fontWeight: 600, fontSize: '16px' }}>{post.title}</span>
                    <span style={{ fontSize: '12px', padding: '2px 8px', background: 'var(--surface-variant)', color: 'var(--primary)', borderRadius: '4px' }}>{post.category}</span>
                    <span style={{ fontSize: '12px', padding: '2px 8px', background: 'var(--surface-border)', color: 'var(--text-secondary)', borderRadius: '4px' }}>
                      {post.status === 'pending_review' ? '待审核' : '已发布'}
                    </span>
                  </div>
                  <div style={{ display: 'flex', gap: '8px' }}>
                    {activeTab === 'pending' ? (
                      <>
                        <button 
                          onClick={() => handleAudit(post.id, 'approve')}
                          style={{ background: 'var(--success)', color: 'white', border: 'none', padding: '6px 16px', borderRadius: '4px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '4px' }}
                        >
                          <CheckCircle size={16} /> 通过发布
                        </button>
                        <button 
                          onClick={() => handleAudit(post.id, 'reject')}
                          style={{ background: 'transparent', color: 'var(--danger)', border: '1px solid var(--danger)', padding: '6px 16px', borderRadius: '4px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '4px' }}
                        >
                          <XCircle size={16} /> 驳回
                        </button>
                      </>
                    ) : (
                      <button 
                        onClick={() => handleAudit(post.id, 'reject', '违规下架')}
                        style={{ background: 'var(--danger)', color: 'white', border: 'none', padding: '6px 16px', borderRadius: '4px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '4px' }}
                      >
                        <AlertTriangle size={16} /> 违规下架
                      </button>
                    )}
                  </div>
                </div>
                
                <p style={{ margin: '0 0 12px 0', color: 'var(--text-secondary)', fontSize: '14px', whiteSpace: 'pre-wrap' }}>
                  {post.description}
                </p>

                {post.images && post.images.length > 0 && (
                  <div style={{ display: 'flex', gap: '8px', overflowX: 'auto', paddingBottom: '8px' }}>
                    {post.images.map((img: string, idx: number) => (
                      <img key={idx} src={img} alt="" style={{ height: '80px', width: '80px', objectFit: 'cover', borderRadius: '8px', backgroundColor: '#f0f0f0' }} />
                    ))}
                  </div>
                )}
                
                <div style={{ marginTop: '12px', fontSize: '12px', color: 'var(--text-secondary)', display: 'flex', gap: '16px', flexWrap: 'wrap' }}>
                  <span>发布人: {post.user?.nickname} ({post.user?.phone})</span>
                  <span>价格: {post.price ? `￥${post.price}` : '未设置'}</span>
                  {post.attributes && Object.entries(JSON.parse(post.attributes)).map(([k, v]) => (
                    <span key={k}>{k}: {String(v)}</span>
                  ))}
                  <span>发布时间: {new Date(post.createdAt).toLocaleString()}</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
