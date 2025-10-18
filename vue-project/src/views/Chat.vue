<template>
  <div class="chat-container">
    <!-- 聊天界面代码 -->
    <button @click="handleLogout">退出</button>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { checkAuth, logout } from '@/api/auth';

const router = useRouter();

// 检查认证状态
const verifyAuth = async () => {
  try {
    const response = await checkAuth();
    if (!response.authenticated) {
      ElMessage.warning('请先登录');
      router.push('/login');
      return false;
    }
    return true;
  } catch (error) {
    console.error('认证检查错误:', error);
    router.push('/login');
    return false;
  }
};

// 退出登录
const handleLogout = async () => {
  try {
    await logout();
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    ElMessage.success('退出成功');
    router.push('/login');
  } catch (error) {
    console.error('退出错误:', error);
    ElMessage.error('退出失败');
  }
};

onMounted(async () => {
  const isAuthenticated = await verifyAuth();
  if (!isAuthenticated) {
    return;
  }

  // 初始化聊天室功能
  initChat();
});

// 初始化聊天室
const initChat = () => {
  // 您的聊天室初始化代码
  const username = localStorage.getItem('username');
  if (username) {
    // 建立WebSocket连接
    connectWebSocket(username);
  }
};

// WebSocket连接
const connectWebSocket = (username) => {
  // 您的WebSocket连接代码
  const socket = new WebSocket(`ws://localhost:3306/imserver/${username}`);

  socket.onopen = () => {
    console.log('WebSocket连接已建立');
  };

  socket.onmessage = (event) => {
    const message = JSON.parse(event.data);
    // 处理收到的消息
    handleReceivedMessage(message);
  };

  socket.onclose = () => {
    console.log('WebSocket连接已关闭');
  };

  socket.onerror = (error) => {
    console.error('WebSocket错误:', error);
  };
};
</script>
