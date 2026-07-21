import { useEffect, useState } from 'react';
import { Users, ShoppingBag, Activity, AlertTriangle } from 'lucide-react';
import api from '../utils/axios';
import '../index.css';

import { Link } from 'react-router-dom';

const StatCard = ({ title, value, trend, icon: Icon, color, linkTo }: any) => (
  <div className="glass-panel" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
      <div>
        <p style={{ margin: '0 0 8px 0', color: 'var(--text-secondary)', fontSize: '14px' }}>{title}</p>
        <h3 style={{ margin: 0, fontSize: '28px', fontWeight: 'bold' }}>{value}</h3>
      </div>
      <div style={{ padding: '12px', background: `rgba(${color}, 0.15)`, borderRadius: '12px', color: `rgb(${color})` }}>
        <Icon size={24} />
      </div>
    </div>
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
      <div>
        <span style={{ color: trend >= 0 ? 'var(--success)' : 'var(--danger)', fontWeight: 600, fontSize: '14px' }}>
          {trend >= 0 ? '+' : ''}{trend}%
        </span>
        <span style={{ color: 'var(--text-secondary)', fontSize: '14px', marginLeft: '8px' }}>较昨日</span>
      </div>
      {linkTo && (
        <Link to={linkTo} style={{ fontSize: '12px', color: 'var(--primary)', textDecoration: 'none' }}>
          前往管理 &rarr;
        </Link>
      )}
    </div>
  </div>
);

export default function Dashboard() {
  const [stats, setStats] = useState({
    newUsers: 0,
    activeOrders: 0,
    revenue: 0,
    pendingReviews: 0,
  });

  useEffect(() => {
    api.get('/dashboard').then(res => {
      setStats(res.data.data);
    }).catch(console.error);
  }, []);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '24px' }}>
        <StatCard title="今日新增用户" value={stats.newUsers.toString()} trend={12.5} icon={Users} color="59, 130, 246" linkTo="/users" />
        <StatCard title="活跃订单数" value={stats.activeOrders.toString()} trend={8.2} icon={ShoppingBag} color="167, 139, 250" linkTo="/orders" />
        <StatCard title="平台流水 (元)" value={`￥${stats.revenue}`} trend={-2.4} icon={Activity} color="34, 197, 94" />
        <StatCard title="待审核内容" value={stats.pendingReviews.toString()} trend={100} icon={AlertTriangle} color="239, 68, 68" linkTo="/content" />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '24px' }}>
        <div className="glass-panel" style={{ padding: '24px', minHeight: '400px' }}>
          <h3 style={{ margin: '0 0 24px 0', fontSize: '18px' }}>平台核心指标趋势 (安全快照)</h3>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '300px', color: 'var(--text-secondary)', border: '1px dashed var(--surface-border)', borderRadius: '8px' }}>
            [ 图表区域: 需接入 ECharts 等第三方库 ]
          </div>
        </div>

        <div className="glass-panel" style={{ padding: '24px' }}>
          <h3 style={{ margin: '0 0 24px 0', fontSize: '18px' }}>最新安全告警</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {[1, 2, 3].map((i) => (
              <div key={i} style={{ padding: '16px', background: 'rgba(239, 68, 68, 0.1)', borderRadius: '8px', borderLeft: '4px solid var(--danger)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                  <span style={{ fontWeight: 600, color: '#fca5a5' }}>异常异地登录拦截</span>
                  <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>10分钟前</span>
                </div>
                <p style={{ margin: 0, fontSize: '14px', color: 'var(--text-secondary)' }}>尝试登录账号: admin_test，来源IP: 182.xx.xx.xx (已被策略阻断)</p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
