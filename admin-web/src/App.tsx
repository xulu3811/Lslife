import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import AdminLayout from './layouts/AdminLayout';
import Dashboard from './pages/Dashboard';
import ContentAudit from './pages/ContentAudit';
import KycAudit from './pages/KycAudit';
import UserManagement from './pages/UserManagement';

// Placeholder pages for other routes
const Placeholder = ({ title }: { title: string }) => (
  <div className="glass-panel" style={{ padding: '24px', minHeight: '400px' }}>
    <h2>{title} (建设中...)</h2>
  </div>
);

// Protected Route Guard
const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const token = localStorage.getItem('admin_token');
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
};

export default function App() {
  return (
    <BrowserRouter basename="/admin-web">
      <Routes>
        <Route path="/login" element={<Login />} />
        
        <Route path="/" element={
          <ProtectedRoute>
            <AdminLayout />
          </ProtectedRoute>
        }>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="users" element={<UserManagement />} />
          <Route path="content" element={<ContentAudit />} />
          <Route path="kyc" element={<KycAudit />} />
          <Route path="orders" element={<Placeholder title="资金与订单模块" />} />
          <Route path="settings" element={<Placeholder title="系统安全设置" />} />
        </Route>

        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
