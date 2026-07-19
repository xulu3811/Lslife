import { useEffect, useState } from 'react';
import api from '../utils/axios';
import { CheckCircle, XCircle } from 'lucide-react';

export default function ContentAudit() {
  const [posts, setPosts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchPosts = () => {
    setLoading(true);
    api.get('/posts?status=pending_review')
      .then(res => setPosts(res.data.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchPosts();
  }, []);

  const handleAudit = (id: string, action: 'approve' | 'reject') => {
    if (!window.confirm(`确定要${action === 'approve' ? '通过' : '驳回'}该发布吗？`)) return;
    
    api.post(`/posts/${id}/audit`, { action })
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
        <h2 style={{ margin: '0 0 24px 0', fontSize: '18px' }}>待审核内容列表</h2>
        
        {loading ? (
          <p>加载中...</p>
        ) : posts.length === 0 ? (
          <p style={{ color: 'var(--text-secondary)' }}>暂无需要审核的发布内容。</p>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {posts.map(post => (
              <div key={post.id} style={{ border: '1px solid var(--surface-border)', borderRadius: '12px', padding: '16px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '12px' }}>
                  <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                    <span style={{ fontWeight: 600, fontSize: '16px' }}>{post.title}</span>
                    <span style={{ fontSize: '12px', padding: '2px 8px', background: 'var(--surface-variant)', color: 'var(--primary)', borderRadius: '4px' }}>{post.category}</span>
                  </div>
                  <div style={{ display: 'flex', gap: '8px' }}>
                    <button 
                      onClick={() => handleAudit(post.id, 'approve')}
                      style={{ background: 'var(--success)', color: 'white', border: 'none', padding: '6px 16px', borderRadius: '4px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '4px' }}
                    >
                      <CheckCircle size={16} /> 通过发布
                    </button>
                    <button 
                      onClick={() => handleAudit(post.id, 'reject')}
                      style={{ background: 'var(--danger)', color: 'white', border: 'none', padding: '6px 16px', borderRadius: '4px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '4px' }}
                    >
                      <XCircle size={16} /> 驳回
                    </button>
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
                
                <div style={{ marginTop: '12px', fontSize: '12px', color: 'var(--text-secondary)', display: 'flex', gap: '16px' }}>
                  <span>发布人: {post.user?.nickname} ({post.user?.phone})</span>
                  <span>价格: {post.price ? `￥${post.price}` : '未设置'}</span>
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
