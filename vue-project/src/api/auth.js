import request from '@/utils/request';

// 用户注册
export function register(data) {
    return request({
        url: '/api/auth/register',
        method: 'post',
        data,
    });
}

// 用户登录
export function login(data) {
    return request({
        url: '/api/auth/login',
        method: 'post',
        data,
    });
}

// 用户退出
export function logout() {
    return request({
        url: '/api/auth/logout',
        method: 'post',
    });
}

// 检查认证状态
export function checkAuth() {
    return request({
        url: '/api/auth/check',
        method: 'get',
    });
}
