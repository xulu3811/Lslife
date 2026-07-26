import { useEffect, useState } from 'react';
import { MessageSquareWarning, Check, X } from 'lucide-react';
import api from '../utils/api';

interface Post {
  id: string;
  category: string;
  title: string;
  description: string;
  price?: number;
  images: string[];
  status: string;
  createdAt: string;
  user: { nickname: string; phone: string };
}

export default function ContentAudit() {
  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(true);
  const [status, setStatus] = useState('pending_review');

  const fetchPosts = async () => {
    setLoading(true);
    try {
      const res = await api.get('/posts', { params: { status } });
      setPosts(res.data.data || []);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPosts();
  }, [status]);

  const handleAudit = async (id: string, action: 'approve' | 'reject') => {
    let note = '';
    if (action === 'reject') {
      const input = window.prompt('请输入驳回原因:');
      if (input === null) return;
      note = input;
    } else {
      if (!window.confirm('确认通过该内容发布？')) return;
    }

    try {
      await api.post(`/posts/${id}/audit`, { action, note });
      fetchPosts();
    } catch (e: any) {
      alert(e.response?.data?.message || '审核操作失败');
    }
  };

  return (
    <div style={{ padding: 16 }}>
      <div className="flex justify-between items-center mb-4">
        <h1 style={{ display: 'flex', alignItems: 'center', margin: 0 }}>
          <MessageSquareWarning style={{ marginRight: 8 }} /> C2C闲置与服务审核
        </h1>
      </div>

      <div className="glass-panel p-4 mb-4 flex gap-4">
        <select
          value={status}
          onChange={(e) => setStatus(e.target.value)}
          className="glass-input"
          style={{ width: 200 }}
        >
          <option value="pending_review">待审核</option>
          <option value="published">已发布 (已通过)</option>
          <option value="rejected">已驳回</option>
        </select>
      </div>

      <div className="glass-panel">
        {loading ? (
          <div style={{ padding: 40, textAlign: 'center' }}>加载中...</div>
        ) : (
          <table className="glass-table">
            <thead>
              <tr>
                <th>发布者</th>
                <th>内容详情</th>
                <th>图片</th>
                <th>发布时间</th>
                <th>状态</th>
                <th style={{ textAlign: 'right' }}>操作</th>
              </tr>
            </thead>
            <tbody>
              {posts.map((post) => (
                <tr key={post.id}>
                  <td>
                    <div style={{ fontWeight: 600 }}>{post.user?.nickname}</div>
                    <div style={{ fontSize: 12, color: 'var(--text-secondary)' }}>{post.user?.phone}</div>
                  </td>
                  <td style={{ maxWidth: 300 }}>
                    <div style={{ fontWeight: 600, fontSize: 14 }}>
                      <span className="badge" style={{ marginRight: 8, background: '#f3f4f6', color: '#374151' }}>{post.category}</span>
                      {post.title}
                    </div>
                    <div style={{ fontSize: 12, marginTop: 4, color: 'var(--text-secondary)' }}>
                      {post.description.length > 50 ? post.description.substring(0, 50) + '...' : post.description}
                    </div>
                    {post.price !== null && (
                      <div style={{ color: 'var(--primary)', fontWeight: 600, marginTop: 4 }}>
                        ¥{post.price}
                      </div>
                    )}
                  </td>
                  <td>
                    <div className="flex gap-2">
                      {post.images && post.images.slice(0, 3).map((img, i) => (
                        <img key={i} src={img} alt="post" style={{ width: 48, height: 48, objectFit: 'cover', borderRadius: 8, border: '1px solid var(--surface-border)' }} />
                      ))}
                      {post.images && post.images.length > 3 && (
                        <div style={{ width: 48, height: 48, borderRadius: 8, background: '#f3f4f6', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 12 }}>
                          +{post.images.length - 3}
                        </div>
                      )}
                    </div>
                  </td>
                  <td style={{ fontSize: 12, color: 'var(--text-secondary)' }}>
                    {new Date(post.createdAt).toLocaleString()}
                  </td>
                  <td>
                    <span className={`badge badge-${post.status === 'published' ? 'active' : post.status === 'pending_review' ? 'pending' : 'offline'}`}>
                      {post.status === 'published' ? '已发布' : post.status === 'pending_review' ? '审核中' : '已驳回'}
                    </span>
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    {post.status === 'pending_review' && (
                      <div className="flex gap-2" style={{ justifyContent: 'flex-end' }}>
                        <button className="glass-button" style={{ padding: '6px 12px' }} onClick={() => handleAudit(post.id, 'approve')}>
                          <Check size={16} /> 通过
                        </button>
                        <button className="glass-button danger" style={{ padding: '6px 12px' }} onClick={() => handleAudit(post.id, 'reject')}>
                          <X size={16} /> 驳回
                        </button>
                      </div>
                    )}
                  </td>
                </tr>
              ))}
              {posts.length === 0 && (
                <tr>
                  <td colSpan={6} style={{ textAlign: 'center', padding: 40 }}>暂无帖子数据</td>
                </tr>
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
