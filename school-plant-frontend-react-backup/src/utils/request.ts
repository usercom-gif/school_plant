import axios from 'axios';
import { message } from 'antd';

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
      return res.data;
    }
  },
  (error) => {
    console.error('err' + error);
    message.error(error.message);
    return Promise.reject(error);
  }
);

export default service;
