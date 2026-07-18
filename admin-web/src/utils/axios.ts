import axios from 'axios';

const api = axios.create({
  baseURL: 'http://115.191.6.95/api/admin',
  timeout: 10000,
});

// Request interceptor to inject short-lived JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('admin_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle token refresh and unauthorized errors
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        // Here we would normally call the refresh token endpoint
        // const res = await axios.post('/api/admin/refresh-token');
        // localStorage.setItem('admin_token', res.data.token);
        // return api(originalRequest);
        
        // For now, if unauthorized, redirect to login
        window.location.href = '/login';
      } catch (err) {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
