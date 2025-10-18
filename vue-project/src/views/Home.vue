<template>
  <div class="home-container">
    <!-- 导航栏 -->
    <el-header class="header">
      <div class="nav-container">
        <div class="logo">
          <i class="el-icon-chat-line-round"></i>
          <span>Web聊天室</span>
        </div>
        <div class="nav-links">
          <el-button v-if="!isLoggedIn" type="text" @click="goToLogin">登录</el-button>
          <el-button v-if="!isLoggedIn" type="text" @click="goToRegister">注册</el-button>
          <el-dropdown v-if="isLoggedIn" placement="bottom">
            <span class="user-info">
              <el-avatar :size="30" :src="userAvatar"></el-avatar>
              <span class="username">{{ user.username }}</span>
              <i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item @click.native="goToChat">进入聊天室</el-dropdown-item>
              <el-dropdown-item divided @click.native="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
    </el-header>

    <!-- 主内容区 -->
    <el-main class="main-content">
      <!-- 欢迎区域 -->
      <div class="hero-section">
        <div class="hero-content">
          <h1>欢迎来到 Web聊天室</h1>
          <p>一个基于Spring Boot和Vue.js的实时聊天应用</p>
          <div class="hero-actions">
            <el-button v-if="!isLoggedIn" type="primary" size="large" @click="goToRegister">
              立即注册
            </el-button>
            <el-button v-if="!isLoggedIn" size="large" @click="goToLogin">
              已有账号登录
            </el-button>
            <el-button v-if="isLoggedIn" type="primary" size="large" @click="goToChat">
              进入聊天室 <i class="el-icon-right"></i>
            </el-button>
          </div>
        </div>
        <div class="hero-image">
          <img src="https://cdn.pixabay.com/photo/2020/05/18/16/17/social-media-5187243_960_720.png" alt="聊天室插图">
        </div>
      </div>

      <!-- 功能特点 -->
      <div class="features-section">
        <h2>功能特点</h2>
        <el-row :gutter="30">
          <el-col :xs="24" :sm="12" :md="8">
            <div class="feature-card">
              <div class="feature-icon">
                <i class="el-icon-chat-dot-round"></i>
              </div>
              <h3>实时聊天</h3>
              <p>基于WebSocket技术，实现即时消息传递，零延迟沟通体验</p>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <div class="feature-card">
              <div class="feature-icon">
                <i class="el-icon-user"></i>
              </div>
              <h3>用户管理</h3>
              <p>完整的用户注册、登录系统，保障账号安全和隐私</p>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <div class="feature-card">
              <div class="feature-icon">
                <i class="el-icon-view"></i>
              </div>
              <h3>在线状态</h3>
              <p>实时显示用户在线状态，轻松查看谁可以聊天</p>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 技术栈 -->
      <div class="tech-stack-section">
        <h2>技术栈</h2>
        <el-row :gutter="20" class="tech-icons">
          <el-col :xs="12" :sm="6" :md="4">
            <div class="tech-item spring-logo">
              <!--  <img src="https://spring.io/images/spring-logo-9146a4d3298760c2e7e49595184e1975.svg" alt="Spring Boot">  -->
                <img src="../assets/spring-logo.svg" alt="Spring Boot">
              <!--  <img src="https://spring.io/favicon.svg" alt="Spring Boot">  -->
              <span>Spring Boot</span>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :md="4">
            <div class="tech-item">
              <!-- <img src="https://vuejs.org/images/logo.png" alt="Vue.js"> -->
              <img src="../assets/logo.svg" alt="Vue.js">
              <span>Vue.js</span>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :md="4">
            <div class="tech-item">
              <img src="https://webpack.js.org/assets/icon-square-big.svg" alt="WebSocket">
              <span>WebSocket</span>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :md="4">
            <div class="tech-item">
              <img src="https://www.mysql.com/common/logos/logo-mysql-170x115.png" alt="MySQL">
              <span>MySQL</span>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :md="4">
            <div class="tech-item">
              <img src="https://element.eleme.io/favicon.ico" alt="Element UI">
              <span>Element UI</span>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6" :md="4">
            <div class="tech-item">
              <img src="https://maven.apache.org/images/maven-logo-black-on-white.png" alt="Maven">
              <span>Maven</span>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-main>

    <!-- 页脚 -->
    <el-footer class="footer">
      <div class="footer-content">
        <p>© 2025 Web聊天室 - 基于Spring Boot和Vue.js构建</p>
        <p>20249021053 | 课程设计</p>
      </div>
    </el-footer>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';

export default {
  name: 'Home',
  data() {
    return {
      userAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
    };
  },
  computed: {
    ...mapGetters(['isLoggedIn']),
    user() {
      return JSON.parse(localStorage.getItem('user') || '{}');
    }
  },
  methods: {
    goToLogin() {
      this.$router.push('/login');
    },
    goToRegister() {
      this.$router.push('/register');
    },
    goToChat() {
      this.$router.push('/im');
    },
    handleLogout() {
      this.$confirm('确定要退出登录吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$store.dispatch('logout')
            .then(() => {
              this.$message.success('退出成功');
              this.$router.push('/login');
            })
            .catch(error => {
              console.error('退出错误:', error);
              this.$message.error('退出失败');
            });
      }).catch(() => {
        // 取消退出
      });
    }
  }
};
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.header {
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 0 20px;
}

.nav-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
}

.logo {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
}

.logo i {
  font-size: 24px;
  margin-right: 10px;
}

.nav-links {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.username {
  margin: 0 10px;
  color: #606266;
}

.main-content {
  flex: 1;
  padding: 0;
}

.hero-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 60px 10%;
  background: linear-gradient(135deg, #409EFF 0%, #64b5ff 100%);
  color: white;
}

.hero-content h1 {
  font-size: 2.5rem;
  margin-bottom: 20px;
}

.hero-content p {
  font-size: 1.2rem;
  margin-bottom: 30px;
  opacity: 0.9;
}

.hero-actions .el-button {
  margin-right: 15px;
}

.hero-image {
  flex-shrink: 0;
}

.hero-image img {
  max-width: 400px;
  width: 100%;
}

.features-section {
  padding: 80px 10%;
  background-color: #fff;
}

.features-section h2 {
  text-align: center;
  margin-bottom: 50px;
  font-size: 2rem;
  color: #303133;
}

.feature-card {
  text-align: center;
  padding: 30px 20px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  font-size: 48px;
  color: #409EFF;
  margin-bottom: 20px;
}

.feature-card h3 {
  margin-bottom: 15px;
  color: #303133;
}

.feature-card p {
  color: #606266;
  line-height: 1.6;
}

.tech-stack-section {
  padding: 80px 10%;
  background-color: #f5f7fa;
}

.tech-stack-section h2 {
  text-align: center;
  margin-bottom: 50px;
  font-size: 2rem;
  color: #303133;
}

.tech-icons {
  display: flex;
  justify-content: center;
}

.tech-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
}

.tech-item img {
  height: 60px;
  margin-bottom: 10px;
  object-fit: contain;
}

.tech-item span {
  color: #606266;
  font-size: 14px;
}

.spring-logo img {
  width: 200px;
}

.footer {
  background-color: #303133;
  color: #fff;
  padding: 30px 0;
  text-align: center;
}

.footer-content p {
  margin: 5px 0;
  color: rgba(255, 255, 255, 0.7);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-section {
    flex-direction: column;
    text-align: center;
    padding: 40px 5%;
  }

  .hero-content h1 {
    font-size: 2rem;
  }

  .hero-image {
    margin-top: 40px;
  }

  .features-section,
  .tech-stack-section {
    padding: 60px 5%;
  }

  .tech-item img {
    height: 50px;
  }
}
</style>
