<template>
  <div class="login-container">
    <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" class="login-form">
      <h3 class="title">聊天室登录</h3>
      <el-form-item prop="username">
        <el-input v-model="loginForm.username" placeholder="用户名"></el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="loginForm.password" type="password" placeholder="密码" @keyup.enter="handleLogin"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button :loading="loading" type="primary" style="width:100%;" @click="handleLogin">
          登录
        </el-button>
      </el-form-item>
      <div class="tips">
        <span>还没有账号？</span>
        <router-link to="/register">立即注册</router-link>
      </div>
    </el-form>
  </div>
</template>

<script>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import request from '@/utils/request';

export default {
  name: 'Login',
  setup() {
    const router = useRouter();
    const loading = ref(false);
    const loginFormRef = ref();

    const loginForm = reactive({
      username: '',
      password: '',
    });

    const loginRules = {
      username: [{ required: true, trigger: 'blur', message: '请输入用户名' }],
      password: [{ required: true, trigger: 'blur', message: '请输入密码' }],
    };

    const handleLogin = () => {
      loginFormRef.value.validate(valid => {
        if (valid) {
          loading.value = true;

          // 发送登录请求
          request.post('/api/auth/login', {
            username: loginForm.username,
            password: loginForm.password
          })
              .then(response => {
                if (response.success) {
                  ElMessage.success('登录成功');
                  // 存储用户信息和token
                  localStorage.setItem('user', JSON.stringify({
                    username: loginForm.username,
                    token: response.token || response.data?.token
                  }));
                  localStorage.setItem('token', response.token || response.data?.token);

                  // 跳转到聊天页面
                  router.push('/im');
                } else {
                  ElMessage.error(response.message || '登录失败');
                }
              })
              .catch(error => {
                console.error('登录错误:', error);
                if (error.response && error.response.status === 400) {
                  ElMessage.error('用户名或密码错误');
                } else {
                  ElMessage.error(error.message || '登录失败，请检查网络连接');
                }
              })
              .finally(() => {
                loading.value = false;
              });
        }
      });
    };

    return {
      loginForm,
      loginRules,
      loginFormRef,
      loading,
      handleLogin
    };
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.login-form {
  width: 400px;
  padding: 30px;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.title {
  margin: 0 auto 30px;
  text-align: center;
  color: #707070;
}

.tips {
  text-align: center;
  font-size: 14px;
  color: #909399;
}

.tips a {
  color: #409EFF;
  text-decoration: none;
}
</style>