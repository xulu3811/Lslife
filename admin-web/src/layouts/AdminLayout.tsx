import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { LayoutDashboard, Users, MessageSquareWarning, Receipt, Settings, LogOut, ShieldCheck, UserCheck } from 'lucide-react';
import '../index.css';

const SIDEBAR_WIDTH = 260;

const menuItems = [
  { path: '/dashboard', label: '数据大盘', icon: <LayoutDashboard size={20} /> },
  { path: '/users', label: '用户管理', icon: <Users size={20} /> },
  { path: '/kyc', label: '实名认证审核', icon: <UserCheck size={20} /> },
  { path: '/content', label: '内容审核', icon: <MessageSquareWarning size={20} /> },
  { path: '/orders', label: '资金与订单', icon: <Receipt size={20} /> },
  { path: '/settings', label: '系统设置', icon: <Settings size={20} /> },
];

export default function AdminLayout() {
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    localStorage.removeItem('admin_token');
    navigate('/login');
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      {/* Sidebar */}
      <div 
        className="glass-panel" 
        style={{ 
          width: SIDEBAR_WIDTH, 
          position: 'fixed', 
          top: 16, 
          bottom: 16, 
          left: 16, 
          display: 'flex', 
          flexDirection: 'column',
          zIndex: 10 
        }}
      >
        <div style={{ padding: '24px', display: 'flex', alignItems: 'center', gap: '12px', borderBottom: '1px solid var(--surface-border)' }}>
          <ShieldCheck size={32} color="var(--primary)" />
          <div>
            <h2 style={{ margin: 0, fontSize: '18px', fontWeight: 600 }}>LsLife Admin</h2>
            <span style={{ fontSize: '12px', color: 'var(--success)' }}>● 安全防御运行中</span>
          </div>
        </div>

        <div style={{ flex: 1, padding: '24px 16px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
          {menuItems.map((item) => {
            const isActive = location.pathname.startsWith(item.path);
            return (
              <button
                key={item.path}
                onClick={() => navigate(item.path)}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '12px',
                  padding: '12px 16px',
                  background: isActive ? 'rgba(59, 130, 246, 0.15)' : 'transparent',
                  color: isActive ? 'var(--primary)' : 'var(--text-secondary)',
                  border: 'none',
                  borderRadius: 'var(--radius-md)',
                  cursor: 'pointer',
                  fontWeight: isActive ? 600 : 500,
                  transition: 'all 0.2s ease',
                  textAlign: 'left'
                }}
              >
                {item.icon}
                {item.label}
              </button>
            );
          })}
        </div>

        <div style={{ padding: '24px 16px', borderTop: '1px solid var(--surface-border)' }}>
          <button
            onClick={handleLogout}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '12px',
              padding: '12px 16px',
              background: 'transparent',
              color: 'var(--danger)',
              border: 'none',
              width: '100%',
              cursor: 'pointer',
              fontWeight: 500,
            }}
          >
            <LogOut size={20} />
            安全登出
          </button>
        </div>
      </div>

      {/* Main Content Area */}
      <div style={{ marginLeft: SIDEBAR_WIDTH + 32, padding: '16px 16px 16px 0', flex: 1, display: 'flex', flexDirection: 'column' }}>
        <header className="glass-panel" style={{ padding: '16px 24px', marginBottom: '24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h1 style={{ margin: 0, fontSize: '20px', fontWeight: 600 }}>
            {menuItems.find(i => location.pathname.startsWith(i.path))?.label || '管理控制台'}
          </h1>
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <span style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>最后登录IP: 115.191.6.95 (已白名单)</span>
            <div style={{ width: '36px', height: '36px', borderRadius: '50%', background: 'var(--primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold' }}>
              A
            </div>
          </div>
        </header>

        <main style={{ flex: 1 }}>
          <Outlet />
        </main>
      </div>
    </div>
  );
}
