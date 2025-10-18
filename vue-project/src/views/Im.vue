<template>
  <div class="chat-container">
    <div style="padding: 10px; margin-bottom: 5px;">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card style="height: 600px;">
            <div slot="header" class="clearfix">
              <span>在线用户 ({{ users.length }})</span>
              <el-badge :value="totalUnreadCount" :max="99" class="item" v-if="totalUnreadCount > 0">
            <span style="font-size: 12px; color: #909399; margin-left: 5px">
              点击用户名开始聊天
            </span>
              </el-badge>
            </div>
            <div
                v-for="user in users"
                :key="user.username"
                class="user-item"
                :class="{ active: chatUser === user.username }"
                @click="selectUser(user.username)"
            >
              <el-avatar :size="40" :src="circleUrl"></el-avatar>
              <div class="user-info">
                <div class="username">{{ user.username }}</div>
                <div class="status">
                  <el-badge :value="getUnreadCount(user.username)" :max="99" class="item" v-if="getUnreadCount(user.username) > 0">
                    <el-tag v-if="user.username === chatUser" size="small" type="success">正在聊天</el-tag>
                    <el-tag v-else size="small">在线</el-tag>
                  </el-badge>
                  <template v-else>
                    <el-tag v-if="user.username === chatUser" size="small" type="success">正在聊天</el-tag>
                    <el-tag v-else size="small">在线</el-tag>
                  </template>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="18">
          <el-card style="height: 600px;">
            <div slot="header" class="clearfix">
              <span>与 {{ chatUser || '请选择聊天对象' }} 的对话</span>
              <el-button
                  v-if="chatUser"
                  style="float: right; padding: 3px 0"
                  type="text"
                  @click="clearChat"
              >
                清空聊天
              </el-button>
            </div>

            <div class="chat-messages" ref="messagesContainer" @scroll="handleScroll">
              <div v-if="hasMoreHistory" class="load-more">
                <el-button
                    :loading="isLoadingHistory"
                    type="text"
                    @click="loadChatHistory"
                >
                  {{ isLoadingHistory ? '加载中...' : '加载更多历史消息' }}
                </el-button>
              </div>

              <!-- 消息列表 -->
              <div
                  v-for="(message, index) in messages"
                  :key="index"
                  :class="['message', message.from === user.username ? 'own-message' : 'other-message']"
              >
                <div class="message-content">
                  <div class="message-sender" v-if="message.from !== user.username">
                    {{ message.from }}
                  </div>
                  <div class="message-bubble">
                    {{ message.text }}
                  </div>
                  <div class="message-time">
                    {{ formatTime(message.timestamp) }}
                  </div>
                </div>
                <el-avatar :size="40" :src="circleUrl"></el-avatar>
              </div>

              <div v-if="!messages.length && chatUser" class="empty-chat">
                <p>还没有消息，开始对话吧</p>
              </div>

              <div v-else-if="!chatUser" class="empty-chat">
                <p>请从左侧选择一个用户开始聊天</p>
              </div>
            </div>

            <div class="chat-input">
              <el-input
                  v-model="text"
                  type="textarea"
                  :rows="3"
                  placeholder="输入消息..."
                  :disabled="!chatUser"
                  @keydown.enter.native="handleEnterKey"
              ></el-input>
              <div class="input-actions">
                <el-button
                    type="primary"
                    :disabled="!text.trim() || !chatUser"
                    @click="send"
                >
                  发送
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <div class="logout-container">
        <el-button type="danger" @click="handleLogout">退出登录</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import request from "@/utils/request";

let socket;
let heartbeatInterval;

export default {
  name: "Im",
  data() {
    return {
      circleUrl: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      users: [],
      chatUser: '',
      text: "",
      messages: [],
      unreadCounts: {}, // 存储每个用户的未读消息数
      totalUnreadCount: 0, // 总未读消息数
      isLoadingHistory: false,
      hasMoreHistory: true,
      historyPage: 0,
      historyPageSize: 50
    }
  },
  mounted() {
    this.initWebSocket();
    this.loadUnreadCount();

    // 设置心跳检测，定期检查未读消息
    setInterval(() => {
      this.loadUnreadCount();
    }, 30000); // 每30秒检查一次
  },
  beforeDestroy() {
    if (socket) {
      socket.close();
    }
    if (heartbeatInterval) {
      clearInterval(heartbeatInterval);
    }
  },
  watch: {
    messages() {
      this.$nextTick(() => {
        this.scrollToBottom();
      });
    }
  },
  methods: {
    handleScroll(event) {
      const container = event.target;
      // 如果滚动到顶部，加载更多历史消息
      if (container.scrollTop === 0 && this.hasMoreHistory && !this.isLoadingHistory) {
        this.loadChatHistory();
      }
    },
    initWebSocket() {
      const username = this.user.username;

      if (typeof WebSocket === "undefined") {
        this.$message.error("您的浏览器不支持WebSocket");
        return;
      }

      // 关闭现有连接
      if (socket != null) {
        socket.close();
        socket = null;
      }

      // 创建新的WebSocket连接
      const socketUrl = `ws://localhost:8080/imserver/${username}`;
      socket = new WebSocket(socketUrl);

      // 连接建立时的处理
      socket.onopen = () => {
        console.log("WebSocket连接已建立");
        this.$message.success("连接已建立");

        // 开始心跳检测
        this.startHeartbeat();
      };

      // 接收消息的处理
      socket.onmessage = (msg) => {
        try {
          const data = JSON.parse(msg.data);
          console.log("收到消息:", data);

          if (data.type === "userlist") {
            // 更新在线用户列表，排除自己
            this.users = data.users.filter(user => user.username !== this.user.username);

            // 为每个用户初始化未读消息数
            this.users.forEach(user => {
              if (!this.unreadCounts[user.username]) {
                this.unreadCounts[user.username] = 0;
              }
            });

          } else if (data.type === "system") {
            // 系统消息
            this.$notify({
              title: '系统通知',
              message: data.content,
              type: 'info'
            });

          } else if (data.type === "chat" || !data.type) {
            // 聊天消息
            data.timestamp = data.timestamp || Date.now();

            // 如果消息是发给当前用户的，添加到消息列表
            if (data.to === this.user.username) {
              this.messages.push(data);

              // 如果消息不是来自当前聊天对象，增加未读计数
              if (data.from !== this.chatUser) {
                this.unreadCounts[data.from] = (this.unreadCounts[data.from] || 0) + 1;
                this.calculateTotalUnreadCount();

                // 显示通知
                this.$notify({
                  title: `新消息来自 ${data.from}`,
                  message: data.text,
                  type: 'info',
                  duration: 3000
                });
              }
            }

            // 如果消息是当前用户发送的，也添加到消息列表
            if (data.from === this.user.username && data.to === this.chatUser) {
              this.messages.push(data);
            }
          }
        } catch (error) {
          console.error("解析消息错误:", error);
        }
      };

      // 连接关闭时的处理
      socket.onclose = () => {
        console.log("WebSocket连接已关闭");
        this.$message.warning("连接已关闭");

        // 停止心跳检测
        this.stopHeartbeat();
      };

      // 发生错误时的处理
      socket.onerror = (error) => {
        console.error("WebSocket错误:", error);
        this.$message.error("连接发生错误");
      };
    },

    startHeartbeat() {
      // 每隔30秒发送一次心跳消息
      heartbeatInterval = setInterval(() => {
        if (socket && socket.readyState === WebSocket.OPEN) {
          socket.send(JSON.stringify({ type: "heartbeat" }));
        }
      }, 30000);
    },

    stopHeartbeat() {
      if (heartbeatInterval) {
        clearInterval(heartbeatInterval);
        heartbeatInterval = null;
      }
    },

    async selectUser(username) {
      // 如果已经选择了该用户，不再处理
      if (this.chatUser === username) {
        return;
      }

      this.chatUser = username;
      this.messages = [];
      this.historyPage = 0;
      this.hasMoreHistory = true;

      // 加载历史消息
      await this.loadChatHistory();

      // 标记该用户的未读消息为已读
      this.markAsRead(username);
    },

    async loadChatHistory() {
      if (this.isLoadingHistory || !this.hasMoreHistory) {
        return;
      }

      this.isLoadingHistory = true;

      try {
        const response = await request.get(`/api/chat/history/${this.chatUser}?currentUser=${this.user.username}&page=${this.historyPage}&size=${this.historyPageSize}`);

        if (response.success) {
          const history = response.data || [];
          const previousScrollHeight = this.$refs.messagesContainer?.scrollHeight || 0;

          // 将历史消息添加到消息列表
          this.messages = [...history.map(item => ({
            from: item.fromUser,
            to: item.toUser,
            text: item.content,
            type: item.messageType || 'text',
            timestamp: new Date(item.timestamp).getTime()
          })), ...this.messages];

          // 保持滚动位置
          this.$nextTick(() => {
            if (this.$refs.messagesContainer) {
              const newScrollHeight = this.$refs.messagesContainer.scrollHeight;
              this.$refs.messagesContainer.scrollTop = newScrollHeight - previousScrollHeight;
            }
          });

          // 检查是否还有更多历史消息
          if (history.length < this.historyPageSize) {
            this.hasMoreHistory = false;
          }

          this.historyPage++;
        } else {
          this.$message.error(response.message || '加载历史消息失败');
        }
      } catch (error) {
        console.error('加载历史消息错误:', error);
        this.$message.error('加载历史消息失败');
      } finally {
        this.isLoadingHistory = false;
      }
    },

    async loadUnreadCount() {
      try {
        const response = await request.get(`/api/chat/unread/count?username=${this.user.username}`);

        if (response.success) {
          this.totalUnreadCount = response.count || 0;
        }
      } catch (error) {
        console.error('获取未读消息数错误:', error);
      }
    },

    async markAsRead(username) {
      try {
        await request.post('/api/chat/mark-as-read', {
          fromUser: username,
          toUser: this.user.username
        });

        // 更新本地未读计数
        this.unreadCounts[username] = 0;
        this.calculateTotalUnreadCount();
      } catch (error) {
        console.error('标记消息为已读错误:', error);
      }
    },

    calculateTotalUnreadCount() {
      this.totalUnreadCount = Object.values(this.unreadCounts).reduce((sum, count) => sum + count, 0);
    },

    getUnreadCount(username) {
      return this.unreadCounts[username] || 0;
    },

    send() {
      if (!this.chatUser) {
        this.$message.warning("请先选择聊天对象");
        return;
      }

      if (!this.text.trim()) {
        this.$message.warning("请输入消息内容");
        return;
      }

      if (socket && socket.readyState === WebSocket.OPEN) {
        const message = {
          to: this.chatUser,
          text: this.text.trim(),
          type: "chat"
        };

        socket.send(JSON.stringify(message));

        // 添加到本地消息列表（发送的消息）
        this.messages.push({
          from: this.user.username,
          to: this.chatUser,
          text: this.text.trim(),
          timestamp: Date.now(),
          type: "chat"
        });

        this.text = ""; // 清空输入框
      } else {
        this.$message.error("连接未就绪，请刷新页面重试");
      }
    },

    handleEnterKey(e) {
      if (e.ctrlKey || e.shiftKey) {
        // 允许换行
        return;
      }
      e.preventDefault();
      this.send();
    },

    clearChat() {
      this.messages = [];
    },

    scrollToBottom() {
      const container = this.$el.querySelector('.chat-messages');
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    },

    formatTime(timestamp) {
      return new Date(timestamp).toLocaleTimeString();
    },

    async handleLogout() {
      try {
        await this.$confirm('确定要退出登录吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });

        // 关闭WebSocket连接
        if (socket) {
          socket.close();
          socket = null;
        }

        // 停止心跳检测
        this.stopHeartbeat();

        // 调用退出API
        await request.post('/api/auth/logout');

        // 清除本地存储
        localStorage.removeItem('user');
        localStorage.removeItem('token');

        this.$message.success('退出成功');
        this.$router.push('/login');

      } catch (error) {
        // 用户点击取消，不处理
        if (error === 'cancel' || error === 'close') {
          return;
        }

        console.error('退出错误:', error);
        // 即使API调用失败，也执行前端退出逻辑
        localStorage.removeItem('user');
        localStorage.removeItem('token');
        this.$router.push('/login');
      }
    }
  }
}
</script>

<style scoped>
.chat-container {
  height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
}
.item {
  margin-top: 10px;
  margin-right: 10px;
}
.user-item {
  position: relative;
  display: flex;
  align-items: center;
  padding: 10px;
  cursor: pointer;
  border-radius: 4px;
  margin-bottom: 5px;
  transition: background-color 0.3s;
}

.user-item:hover {
  background-color: #f0f2f5;
}

.user-item.active {
  background-color: #ecf5ff;
}

.user-info {
  margin-left: 10px;
}

.username {
  font-weight: bold;
  margin-bottom: 4px;
}

.status {
  font-size: 12px;
}

.chat-messages {
  height: 380px;
  overflow-y: auto;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
  margin-bottom: 15px;
}

.message {
  display: flex;
  margin-bottom: 15px;
}

.own-message {
  flex-direction: row-reverse;
}

.message-content {
  max-width: 70%;
  margin: 0 10px;
}

.message-sender {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.own-message .message-sender {
  text-align: right;
}

.message-bubble {
  padding: 10px 15px;
  border-radius: 4px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  word-break: break-word;
}

.own-message .message-bubble {
  background-color: #409EFF;
  color: white;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.own-message .message-time {
  text-align: right;
}

.empty-chat {
  text-align: center;
  color: #909399;
  padding: 50px 0;
}

.chat-input {
  border-top: 1px solid #e6e6e6;
  padding-top: 15px;
}

.input-actions {
  margin-top: 10px;
  text-align: right;
}

.logout-container {
  text-align: center;
  margin-top: 20px;
}
</style>
