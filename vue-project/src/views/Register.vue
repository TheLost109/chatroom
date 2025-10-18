<template>
  <div class="register-container">
    <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" class="register-form">
      <h3 class="title">用户注册</h3>
      <el-form-item prop="username">
        <el-input v-model="registerForm.username" placeholder="用户名"></el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="registerForm.password" type="password" placeholder="密码"></el-input>
      </el-form-item>
      <el-form-item prop="confirmPassword">
        <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" @keyup.enter="handleRegister"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button :loading="loading" type="primary" style="width:100%;" @click="handleRegister">
          注册
        </el-button>
      </el-form-item>
      <div class="tips">
        <span>已有账号？</span>
        <router-link to="/">立即登录</router-link>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { register } from '@/api/auth';

const router = useRouter();
const loading = ref(false);
const registerFormRef = ref();

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
});

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致!'));
  } else {
    callback();
  }
};

const registerRules = {
  username: [{ required: true, trigger: 'blur', message: '请输入用户名' }],
  password: [{ required: true, trigger: 'blur', message: '请输入密码' }],
  confirmPassword: [{ required: true, validator: validatePass2, trigger: 'blur' }],
};

const handleRegister = () => {
  registerFormRef.value.validate(valid => {
    if (valid) {
      loading.value = true;

      // 从registerForm中提取需要的字段，排除confirmPassword
      const { confirmPassword, ...submitData } = registerForm;

      register(submitData)
          .then(response => {
            if (response.success) {
              ElMessage.success('注册成功，请登录');
              router.push('/login');
            } else {
              ElMessage.error(response.message || '注册失败');
            }
          })
          .catch(error => {
            console.error('注册错误:', error);
            ElMessage.error(error.message || '注册失败');
          })
          .finally(() => {
            loading.value = false;
          });
    }
  });
};
</script>

<style scoped>
.register-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.register-form {
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
