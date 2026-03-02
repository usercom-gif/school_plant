import axios from 'axios';
import { message } from 'ant-design-vue';

// Create Axios instance
const service = axios.create({
  baseURL: '/api', // Use proxy
  timeout: 10000,
});

// Request interceptor
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['satoken'] = token; // Sa-Token header
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor
service.interceptors.response.use(
  (response) => {
    const res = response.data;
    console.log('[API Response]', response.config.url, res);
    
    // Assume backend returns { code: 200, msg: "success", data: ... }
    if (res.code !== 200) {
      message.error(res.msg || 'Error');
      if (res.code === 401) {
        // Redirect to login
        localStorage.removeItem('token');
        window.location.href = '/login';
      }
      return Promise.reject(new Error(res.msg || 'Error'));
    } else {
      // Return the data field directly
      return res.data;
    }
  },
  (error) => {
    console.error('[API Error]', error);
    message.error(error.message || 'Network Error');
    return Promise.reject(error);
  }
);

export default service;
