<template>
  <div>
    <div class="page-header">
      <div style="display: flex; align-items: center; gap: 12px;">
        <el-button text @click="router.back()"><i class="fas fa-arrow-left"></i></el-button>
        <div>
          <h1 class="page-title">会话处理</h1>
          <p class="page-desc">与用户进行在线沟通</p>
        </div>
      </div>
    </div>

    <div class="chat-container">
      <div class="chat-sidebar">
        <div class="chat-user-info">
          <div class="mini-avatar">{{ userName.charAt(0) }}</div>
          <div style="min-width:0;">
            <div style="font-weight: 600; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">{{ userName }}</div>
            <el-tag size="small" effect="light">用户ID: {{ session?.userId }}</el-tag>
          </div>
        </div>
        <div class="chat-shortcuts">
          <div class="card-title" style="font-size: 13px; margin-bottom: 12px;">快捷回复</div>
          <div
            v-for="reply in quickReplies"
            :key="reply.id"
            class="reply-item"
            @click="sendQuickReply(reply.content)"
          >
            {{ reply.content }}
          </div>
          <div v-if="quickReplies.length === 0" style="font-size:12px;color:#999;text-align:center;padding:12px;">
            暂无快捷回复
          </div>
        </div>
      </div>

      <div class="chat-main">
        <div class="chat-header-bar">
          <span>{{ userName }}</span>
          <el-button v-if="session?.status === 'OPEN'" type="danger" size="small" @click="handleCloseSession">关闭会话</el-button>
        </div>
        <div ref="messagesRef" class="chat-messages">
          <div v-for="msg in messages" :key="msg.id" :class="['chat-message', msg.fromType === 'AGENT' ? 'self' : 'other']">
            <div class="chat-bubble">
              {{ msg.content }}
            </div>
            <div class="chat-time">{{ formatTime(msg.timestamp) }}</div>
          </div>
          <div v-if="session?.status === 'CLOSED'" class="chat-closed-tip">会话已关闭</div>
        </div>

        <div class="chat-input">
          <div class="input-toolbar">
            <i class="fas fa-smile" title="表情"></i>
          </div>
          <el-input
            v-model="inputMsg"
            type="textarea"
            :rows="2"
            placeholder="输入消息..."
            :disabled="session?.status === 'CLOSED'"
            @keyup.enter.exact.prevent="handleSend"
          />
          <el-button type="primary" @click="handleSend" :disabled="session?.status === 'CLOSED'">发送</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getServiceSession, getSessionMessages, sendAgentMessage, closeServiceSession, getQuickReplies } from '@/api/service'
import wsClient from '@/api/websocket'

const route = useRoute()
const router = useRouter()
const sessionId = ref(Number(route.params.id))
const session = ref(null)
const messages = ref([])
const inputMsg = ref('')
const userName = ref('用户')
const messagesRef = ref(null)
const quickReplies = ref([])

const formatTime = (ts) => {
  if (!ts) return ''
  const d = new Date(ts)
  return `${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}`
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

const loadSession = async () => {
  try {
    const res = await getServiceSession(sessionId.value)
    session.value = res.data
    userName.value = '用户' + (session.value.userId || '')
  } catch (e) {
    console.error('加载会话失败:', e)
  }
}

const loadMessages = async () => {
  try {
    const res = await getSessionMessages(sessionId.value, { page: 0, size: 100 })
    messages.value = res.data || []
    scrollToBottom()
  } catch (e) {
    console.error('加载消息失败:', e)
  }
}

const loadQuickReplies = async () => {
  try {
    const res = await getQuickReplies()
    quickReplies.value = (res.data || []).filter(r => r.enabled !== false)
  } catch (e) {
    console.error('加载快捷回复失败:', e)
  }
}

const handleSend = async () => {
  const content = inputMsg.value.trim()
  if (!content) return

  // 优先 WebSocket
  const sent = wsClient.send({
    type: 'MESSAGE',
    sessionId: sessionId.value,
    content,
    contentType: 'TEXT'
  })

  if (sent) {
    // WebSocket 发送成功，乐观添加到列表
    messages.value.push({
      id: Date.now(),
      sessionId: sessionId.value,
      fromId: 'agent',
      fromType: 'AGENT',
      content,
      contentType: 'TEXT',
      timestamp: new Date().toISOString()
    })
    scrollToBottom()
  } else {
    // HTTP 降级
    try {
      await sendAgentMessage(sessionId.value, { content })
      await loadMessages()
    } catch (e) {
      ElMessage.error('发送失败')
      return
    }
  }
  inputMsg.value = ''
}

const sendQuickReply = (content) => {
  inputMsg.value = content
  handleSend()
}

const handleCloseSession = async () => {
  try {
    await ElMessageBox.confirm('确定要关闭此会话吗？', '提示', { type: 'warning' })
    await closeServiceSession(sessionId.value)
    ElMessage.success('会话已关闭')
    await loadSession()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('关闭失败')
  }
}

// WebSocket 消息监听
const onWsMessage = (data) => {
  if (data.sessionId !== sessionId.value) return
  if (data.type === 'MESSAGE') {
    messages.value.push({
      id: data.messageId || Date.now(),
      sessionId: data.sessionId,
      fromId: data.fromId,
      fromType: data.fromType,
      content: data.content,
      contentType: data.contentType,
      timestamp: data.timestamp
    })
    scrollToBottom()
  } else if (data.type === 'SESSION_CLOSED') {
    session.value.status = 'CLOSED'
    ElMessage.info('会话已关闭')
  }
}

onMounted(async () => {
  await Promise.all([loadSession(), loadMessages(), loadQuickReplies()])
  // 连接 WebSocket（AGENT 类型）
  const agentId = localStorage.getItem('admin_user_id') || '1'
  wsClient.connect(agentId, 'AGENT')
  wsClient.on('MESSAGE', onWsMessage)
  wsClient.on('SESSION_CLOSED', onWsMessage)
})

onUnmounted(() => {
  wsClient.off('MESSAGE', onWsMessage)
  wsClient.off('SESSION_CLOSED', onWsMessage)
  wsClient.close()
})
</script>

<style scoped>
.chat-container {
  display: flex;
  gap: 16px;
  height: calc(100vh - 200px);
  min-height: 500px;
}
.chat-sidebar {
  width: 260px;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  flex-shrink: 0;
}
.chat-user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}
.mini-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #FF6B35;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  flex-shrink: 0;
}
.chat-shortcuts {
  flex: 1;
  overflow-y: auto;
}
.reply-item {
  padding: 10px 12px;
  background: #f5f5f5;
  border-radius: 6px;
  font-size: 13px;
  color: #333;
  cursor: pointer;
  margin-bottom: 8px;
  transition: all 0.2s;
  border-left: 3px solid #FF6B35;
}
.reply-item:hover {
  background: #fff0eb;
}
.chat-main {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.chat-header-bar {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.chat-message {
  max-width: 70%;
}
.chat-message.self {
  align-self: flex-end;
}
.chat-bubble {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-all;
}
.chat-message.other .chat-bubble {
  background: #f5f5f5;
  color: #333;
  border-bottom-left-radius: 4px;
}
.chat-message.self .chat-bubble {
  background: #FF6B35;
  color: #fff;
  border-bottom-right-radius: 4px;
}
.chat-time {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
}
.chat-message.self .chat-time {
  text-align: right;
}
.chat-closed-tip {
  text-align: center;
  color: #999;
  font-size: 13px;
  padding: 12px;
}
.chat-input {
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 8px;
  align-items: flex-end;
}
.input-toolbar {
  display: flex;
  gap: 8px;
  font-size: 18px;
  color: #999;
}
.input-toolbar i {
  cursor: pointer;
}
</style>
