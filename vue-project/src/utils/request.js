import axios from 'axios';
import { ElMessage } from 'element-plus'; // 如果您使用了Element Plus
import router from '../router';

// 创建axios实例
const service = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 10000,
});

// 请求拦截器
service.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    error => {
        console.log('Request Error:', error);
        return Promise.reject(error);
    }
);

// 响应拦截器 - 优化错误处理
service.interceptors.response.use(
    response => {
        const res = response.data;

        if (res.hasOwnProperty('success')) {
            if (!res.success) {
                ElMessage({
                    message: res.message || '操作失败',
                    type: 'error',
                    duration: 3000,
                });
                return Promise.reject(new Error(res.message || 'Error'));
            }
            return res;
        }

        return res;
    },
    error => {
        console.log('Response Error Details:', {
            url: error.config?.url,
            method: error.config?.method,
            status: error.response?.status,
            statusText: error.response?.statusText,
            data: error.response?.data
        });

        let message = '请求失败';

        if (error.response) {
            switch (error.response.status) {
                case 400:
                    message = '请求参数错误';
                    break;
                case 401:
                    message = '未授权，请重新登录';
                    localStorage.removeItem('token');
                    localStorage.removeItem('user');
                    router.push('/login');
                    break;
                case 404:
                    message = '请求地址不存在: ' + error.config.url;
                    break;
                case 500:
                    message = '服务器内部错误';
                    break;
                default:
                    message = `请求错误: ${error.response.status}`;
            }
        } else if (error.request) {
            message = '网络错误，请检查网络连接';
        } else {
            message = error.message || '请求失败';
        }

        ElMessage({
            message,
            type: 'error',
            duration: 3000,
        });

        return Promise.reject(error);
    }
);

export default service;
