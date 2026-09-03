<template>
  <view class="container">
    <!-- 状态栏 -->
    <view class="status-bar">
      <text>19:48</text>
      <view class="status-icons">
        <text>📶</text>
        <text>📡</text>
        <text>🔋</text>
      </view>
    </view>

    <!-- 客服头部 -->
    <view class="chat-header">
      <view class="chat-close" @click="goBack">
        <text style="color:#fff;font-size:18px;">←</text>
      </view>
      <view class="chat-avatar">
        <text style="color:#FF6B35;font-size:20px;">🎧</text>
      </view>
      <view class="chat-info">
        <text class="chat-name">快马日结小助手</text>
        <view class="chat-status">
          <view class="status-dot"></view>
          <text style="margin-left:4px;">在线 · 平均1分钟回复</text>
        </view>
      </view>
      <view class="chat-close">
        <text style="color:#fff;font-size:16px;">…</text>
      </view>
    </view>

    <!-- 安全提示 -->
    <view class="security-tip" v-if="showSecurityTip">
      <text style="color:#FA8C16;">🛡</text>
      <text style="flex:1;">平台禁止飞单行为，检测到将进行封号处理</text>
      <text class="✕" style="color:#FA8C16;" @click="showSecurityTip = false"></text>
    </view>

    <!-- 消息列表 -->
    <scroll-view scroll-y class="chat-messages">
      <text class="msg-time">今天 19:48</text>

      <!-- 客服欢迎消息 -->
      <view class="msg-row">
        <view class="msg-avatar service">
          <text style="font-size:14px;">🎧</text>
        </view>
        <view class="msg-content">
          <view class="msg-bubble">您好，请问有什么可以帮助您？</view>
        </view>
      </view>

      <!-- 热门问题 -->
      <view class="msg-row">
        <view class="msg-avatar service">
          <text style="font-size:14px;">🎧</text>
        </view>
        <view class="msg-content">
          <view class="msg-bubble" style="background:transparent;padding:0;">
            <view class="hot-questions">
              <text class="hot-title">热门问题</text>
              <view class="hot-list">
                <view class="hot-item" v-for="(item, index) in hotQuestions" :key="index" @click="selectHotQuestion(item)">
                  <text style="font-size:6px;">●</text>
                  <text>{{ item }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 用户消息 -->
      <view class="msg-row self" v-for="(msg, index) in messages" :key="index">
        <view class="msg-avatar user">
          <text style="font-size:14px;">👤</text>
        </view>
        <view class="msg-content">
          <view class="msg-bubble">{{ msg }}</view>
        </view>
      </view>
    </scroll-view>

    <!-- 输入区域 -->
    <view class="input-area">
      <view class="input-btn">
        <text style="font-size:20px;color:#666;">🖼</text>
      </view>
      <view class="input-wrap">
        <input type="text" class="input-field" v-model="inputText" placeholder="请输入您的问题" @confirm="sendMessage" />
      </view>
      <button class="send-btn" @click="sendMessage">发送</button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      showSecurityTip: true,
      inputText: '',
      messages: [],
      hotQuestions: [
        '零工中途跑了怎么办？',
        '临时有变化不需要招工了怎么办？',
        '工作地址填错了怎么办？',
        '招工信息需要修改怎么办？',
        '零工完工后没有点击收工，一直没法结算报酬怎么办？'
      ]
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    selectHotQuestion(question) {
      this.inputText = question
      this.sendMessage()
    },
    sendMessage() {
      if (!this.inputText.trim()) return
      this.messages.push(this.inputText)
      this.inputText = ''
      // 模拟客服回复
      setTimeout(() => {
        this.messages.push('收到您的问题了，客服专员正在为您处理，请稍候...')
      }, 1000)
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  width: 100%;
  height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.status-bar {
  height: 47px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 28px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  background: #fff;
}

.status-icons {
  display: flex;
  align-items: center;
  gap: 4px;
}

.chat-header {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.chat-avatar {
  width: 40px;
  height: 40px;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-info {
  flex: 1;
}

.chat-name {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
}

.chat-status {
  font-size: 12px;
  color: rgba(255,255,255,0.8);
  display: flex;
  align-items: center;
}

.status-dot {
  width: 6px;
  height: 6px;
  background: #52C41A;
  border-radius: 50%;
}

.chat-close {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.security-tip {
  background: #FFF8E6;
  padding: 8px 14px;
  font-size: 12px;
  color: #B8860B;
  display: flex;
  align-items: center;
  gap: 6px;
  border-bottom: 1px solid #FFE4A0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f5f5;
}

.msg-time {
  text-align: center;
  font-size: 11px;
  color: #999;
  margin: 12px 0;
  display: block;
}

.msg-row {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.msg-row.self {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
}

.msg-avatar.service {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  color: white;
}

.msg-avatar.user {
  background: #52C41A;
  color: white;
}

.msg-content {
  max-width: 75%;
}

.msg-bubble {
  padding: 12px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
}

.msg-row:not(.self) .msg-bubble {
  background: #fff;
  color: #333;
  border-radius: 4px 12px 12px 12px;
}

.msg-row.self .msg-bubble {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  color: white;
  border-radius: 12px 4px 12px 12px;
}

.hot-questions {
  background: #fff;
  border-radius: 12px;
  padding: 14px;
  margin-top: 10px;
}

.hot-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
  display: block;
}

.hot-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.hot-item {
  padding: 10px 12px;
  background: #FFF8F5;
  border-radius: 8px;
  font-size: 13px;
  color: #FF6B35;
  display: flex;
  align-items: center;
  gap: 6px;
}

.input-area {
  background: #fff;
  padding: 12px 14px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.input-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.input-wrap {
  flex: 1;
}

.input-field {
  width: 100%;
  padding: 10px 14px;
  background: #f5f5f5;
  border-radius: 20px;
  font-size: 14px;
  border: none;
  outline: none;
  box-sizing: border-box;
}

.send-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}
</style>
