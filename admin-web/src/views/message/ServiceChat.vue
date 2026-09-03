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
          <div>
            <div style="font-weight: 600;">{{ userName }}</div>
            <el-tag size="small" effect="light">零工</el-tag>
          </div>
        </div>
        <div class="chat-shortcuts">
          <div class="card-title" style="font-size: 13px; margin-bottom: 12px;">快捷回复</div>
          <div v-for="(reply, i) in quickReplies" :key="i" class="reply-item" @click="sendQuickReply(reply)">
            {{ reply }}
          </div>
        </div>
      </div>

      <div class="chat-main">
        <div class="chat-messages">
          <div v-for="(msg, i) in messages" :key="i" :class="['chat-message', msg.isSelf ? 'self' : 'other']">
            <div class="chat-bubble">
              {{ msg.content }}
            </div>
          </div>
        </div>

        <div class="chat-input">
          <div class="input-toolbar">
            <i class="fas fa-smile" title="表情"></i>
            <i class="fas fa-paperclip" title="附件"></i>
          </div>
          <el-input v-model="inputMsg" type="textarea" :rows="2" placeholder="输入消息..." @keyup.enter="handleSend" />
          <el-button type="primary" @click="handleSend">发送</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

console.warn('[API] 客服会话消息 API 后端暂未接入，当前使用 mock 数据')

const route = useRoute()
const userName = ref('张建国')
const inputMsg = ref('')

const messages = ref([
  { isSelf: false, content: '您好，我想咨询一下关于实名认证的问题' },
  { isSelf: true, content: '您好，请问有什么可以帮您的？' },
  { isSelf: false, content: '我提交了实名认证申请，但是好像一直没有通过，请问是什么原因？' }
])

const quickReplies = [
  '您好，请问有什么可以帮助您的？',
  '请稍等，正在为您查询...',
  '您的问题已记录，我们会尽快处理',
  '感谢您的反馈，祝您生活愉快！'
]

const sendQuickReply = (text) => {
  messages.value.push({ isSelf: true, content: text })
}

const handleSend = () => {
  if (!inputMsg.value.trim()) return
  messages.value.push({ isSelf: true, content: inputMsg.value })
  inputMsg.value = ''
}
</script>

<style scoped>
.chat-container {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 16px;
  height: calc(100vh - 200px);
}

.chat-sidebar {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chat-user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border);
}

.mini-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF8C42, #FF6B35);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  font-size: 18px;
}

.chat-shortcuts { flex: 1; overflow-y: auto; }

.reply-item {
  padding: 10px 14px;
  background: var(--bg-page);
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  margin-bottom: 8px;
  transition: all 0.2s;
  
  &:hover { background: #FFE8DC; color: var(--primary); }
}

.chat-main {
  background: #fff;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: var(--bg-page);
}

.chat-message {
  margin-bottom: 16px;
  
  &.self {
    text-align: right;
    
    .chat-bubble {
      background: var(--primary);
      color: #fff;
      border-radius: 12px 4px 12px 12px;
    }
  }
  
  &.other {
    .chat-bubble {
      background: #fff;
      border-radius: 4px 12px 12px 12px;
    }
  }
}

.chat-bubble {
  display: inline-block;
  padding: 10px 16px;
  max-width: 70%;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  text-align: left;
}

.chat-input {
  padding: 16px;
  border-top: 1px solid var(--border);
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.input-toolbar {
  display: flex;
  gap: 12px;
  padding: 10px 0;
  color: var(--text-muted);
  
  i { cursor: pointer; }
}
</style>
