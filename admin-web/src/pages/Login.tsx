import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Shield, Lock, User, KeyRound } from 'lucide-react';
import api from '../utils/axios';
import '../index.css';

export default function Login() {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [totp, setTotp] = useState('');
  const [showMfa, setShowMfa] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username || !password) return;
    
    // In phase 1, we simulate triggering MFA
    if (!showMfa) {
      setShowMfa(true);
      return;
    }

    setLoading(true);
    try {
      const res = await api.post('/login', { username, password });
      localStorage.setItem('admin_token', res.data.data.token);
      navigate('/dashboard');
    } catch (err: any) {
      alert(err.response?.data?.message || '登录失败');
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', padding: '20px' }}>
      <div className="glass-panel" style={{ padding: '40px', width: '100%', maxWidth: '440px' }}>
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <div style={{ 
            display: 'inline-flex', 
            padding: '16px', 
            background: 'rgba(59, 130, 246, 0.1)', 
            borderRadius: '50%',
            marginBottom: '16px'
          }}>
            <Shield size={48} color="#3b82f6" />
          </div>
          <h1 className="text-gradient" style={{ margin: '0 0 8px 0', fontSize: '28px' }}>
            LsLife Admin
          </h1>
          <p style={{ margin: 0, color: 'var(--text-secondary)' }}>安全管理控制台</p>
        </div>

        <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {!showMfa ? (
            <>
              <div>
                <label style={{ display: 'block', marginBottom: '8px', fontSize: '14px', color: 'var(--text-secondary)' }}>
                  管理账号
                </label>
                <div style={{ position: 'relative' }}>
                  <User size={18} style={{ position: 'absolute', left: '16px', top: '14px', color: 'var(--text-secondary)' }} />
                  <input
                    type="text"
                    className="glass-input"
                    style={{ paddingLeft: '44px' }}
                    placeholder="输入管理员账号"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                  />
                </div>
              </div>
              
              <div>
                <label style={{ display: 'block', marginBottom: '8px', fontSize: '14px', color: 'var(--text-secondary)' }}>
                  访问密码
                </label>
                <div style={{ position: 'relative' }}>
                  <Lock size={18} style={{ position: 'absolute', left: '16px', top: '14px', color: 'var(--text-secondary)' }} />
                  <input
                    type="password"
                    className="glass-input"
                    style={{ paddingLeft: '44px' }}
                    placeholder="输入管理密码"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                  />
                </div>
              </div>
            </>
          ) : (
            <div>
              <label style={{ display: 'block', marginBottom: '8px', fontSize: '14px', color: 'var(--text-secondary)' }}>
                两步验证 (2FA) 代码
              </label>
              <div style={{ position: 'relative' }}>
                <KeyRound size={18} style={{ position: 'absolute', left: '16px', top: '14px', color: 'var(--text-secondary)' }} />
                <input
                  type="text"
                  className="glass-input"
                  style={{ paddingLeft: '44px', letterSpacing: '4px', fontSize: '18px' }}
                  placeholder="000000"
                  maxLength={6}
                  value={totp}
                  onChange={(e) => setTotp(e.target.value)}
                  autoFocus
                />
              </div>
              <p style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '8px', textAlign: 'center' }}>
                请打开您的 Authenticator 应用获取6位验证码
              </p>
            </div>
          )}

          <button 
            type="submit" 
            className="glass-button" 
            style={{ marginTop: '12px' }}
            disabled={loading}
          >
            {loading ? '验证中...' : (showMfa ? '安全登录' : '下一步')}
          </button>
        </form>
      </div>
    </div>
  );
}
