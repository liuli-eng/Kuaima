<template>
  <view class="page">
    <view class="chat-header" :style="{ paddingTop: `${statusBarHeight}px` }">
      <text class="back" @click="uni.navigateBack()">‹</text>
      <text class="avatar">快</text>
      <view class="info">
        <text class="name">快马日结客服</text>
        <text class="status">
          <text class="dot" />在线 · 实时聊天
        </text>
      </view>
    </view>
    <view class="security">请勿向任何人透露验证码、银行卡密码等敏感信息。</view>
    <scroll-view scroll-y class="messages" :scroll-into-view="lastId" :scroll-with-animation="true">
      <view
        v-for="(item, index) in messages"
        :id="`msg-${index}`"
        :key="item.id || index"
        :class="['message-row', { self: item.self }]"
      >
        <text v-if="!item.self" class="small-avatar">快</text>
        <view class="bubble-wrap">
          <text class="bubble">{{ item.text }}</text>
          <text class="msg-time">{{ formatTime(item.timestamp) }}</text>
        </view>
      </view>
      <view class="hot">
        <text class="hot-title">常见问题</text>
        <text v-for="(item, i) in hotQuestions" :key="i" @click="sendText(item)">{{ item }} ›</text>
      </view>
    </scroll-view>
    <view class="composer" :style="{ paddingBottom: `${bottomInset + 12}px` }">
      <input
        v-model="input"
        confirm-type="send"
        placeholder="请输入您的问题"
        @confirm="send"
      />
      <button @click="send">发送</button>
    </view>
  </view>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { startSession, getMessages, sendMessage } from '@/api/chat'
import wsClient from '@/api/chat-websocket'

const system = uni.getSystemInfoSync()
const statusBarHeight = system.statusBarHeight || 0
const bottomInset = system.safeAreaInsets?.bottom || 0
const input = ref('')
const sessionId = ref(null)
const messages = ref([
  { text: '您好，我是快马日结在线客服，请问有什么可以帮您？', self: false, timestamp: Date.now() }
])
const hotQuestions = [
  '如何发布招聘订单？',
  '零工爽约怎么办？',
  '服务费如何收取？',
]
const lastId = computed(() => `msg-${messages.value.length - 1}`)

const formatTime = (ts) => {
  if (!ts) return ''
  const d = new Date(ts)
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

const loadHistory = async () => {
  if (!sessionId.value) return
  try {
    const res = await getMessages(sessionId.value)
    const list = Array.isArray(res) ? res : (res?.data || [])
    list.forEach(msg => {
      messages.value.push({
        id: msg.id,
        text: msg.content,
        self: msg.fromType === 'USER',
        timestamp: msg.timestamp
      })
    })
  } catch (e) {
    console.error('[Chat] 加载历史消息失败:', e)
  }
}

const sendText = (text) => {
  input.value = text
  send()
}

const send = async () => {
  const text = input.value.trim()
  if (!text) return

  const sent = wsClient.send({
    type: 'MESSAGE',
    sessionId: sessionId.value,
    content: text,
    contentType: 'TEXT'
  })

  if (sent) {
    messages.value.push({ text, self: true, timestamp: Date.now() })
  } else {
    try {
      const userId = uni.getStorageSync('userId') || '1001'
      await sendMessage(sessionId.value, userId, text)
      messages.value.push({ text, self: true, timestamp: Date.now() })
    } catch (e) {
      uni.showToast({ title: '发送失败', icon: 'none' })
      return
    }
  }
  input.value = ''
}

const onWsMessage = (data) => {
  if (data.sessionId !== sessionId.value) return
  if (data.type === 'MESSAGE' && data.fromType === 'AGENT') {
    messages.value.push({
      id: data.messageId,
      text: data.content,
      self: false,
      timestamp: data.timestamp
    })
  }
}

onMounted(async () => {
  const userId = uni.getStorageSync('userId') || '1001'
  try {
    const res = await startSession(userId)
    sessionId.value = res?.id || res?.data?.id
    await loadHistory()
  } catch (e) {
    console.error('[Chat] 创建会话失败:', e)
  }

  // 连接 WebSocket（USER 类型），优先使用 VITE_WS_BASE_URL，其次从 HTTP 地址派生
  const httpBase = import.meta.env.VITE_MP_API_BASE_URL || 'http://192.168.2.88:8080'
  const wsBaseUrl = (import.meta.env.VITE_WS_BASE_URL || httpBase).replace('http://', 'ws://').replace('https://', 'wss://')
  wsClient.connect(wsBaseUrl, userId, 'USER')
  wsClient.on('MESSAGE', onWsMessage)
})

onUnmounted(() => {
  wsClient.off('MESSAGE', onWsMessage)
  wsClient.close()
})
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}
.chat-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}
.back {
  font-size: 24px;
  color: #333;
}
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF6B35, #FF8C42);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}
.info {
  display: flex;
  flex-direction: column;
}
.name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}
.status {
  font-size: 12px;
  color: #52c41a;
  display: flex;
  align-items: center;
  gap: 4px;
}
.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #52c41a;
}
.security {
  padding: 8px 16px;
  font-size: 12px;
  color: #999;
  background: #fffbe6;
  text-align: center;
}
.messages {
  flex: 1;
  padding: 16px;
}
.message-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 16px;
}
.message-row.self {
  justify-content: flex-end;
}
.small-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF6B35, #FF8C42);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  flex-shrink: 0;
}
.bubble-wrap {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}
.bubble {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
}
.message-row:not(.self) .bubble {
  background: #fff;
  color: #333;
  border-bottom-left-radius: 4px;
}
.message-row.self .bubble {
  background: #FF6B35;
  color: #fff;
  border-bottom-right-radius: 4px;
}
.msg-time {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
}
.message-row.self .msg-time {
  text-align: right;
}
.hot {
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  margin-top: 8px;
}
.hot-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  display: block;
}
.hot text {
  display: block;
  font-size: 13px;
  color: #FF6B35;
  padding: 6px 0;
  border-bottom: 1px solid #f5f5f5;
}
.hot text:last-child {
  border-bottom: none;
}
.composer {
  display: flex;
  gap: 8px;
  padding: 10px 16px;
  background: #fff;
  border-top: 1px solid #f0f0f0;
  align-items: center;
}
.composer input {
  flex: 1;
  height: 40px;
  background: #f5f5f5;
  border-radius: 20px;
  padding: 0 16px;
  font-size: 14px;
}
.composer button {
  background: #FF6B35;
  color: #fff;
  border: none;
  border-radius: 20px;
  padding: 0 16px;
  font-size: 14px;
}
</style>
