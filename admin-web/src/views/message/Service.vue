<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">客服管理</h1>
      <p class="page-desc">在线客服会话管理、快捷回复、FAQ设置</p>
    </div>

    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">进行中会话</span>
          <div class="stat-card-icon green"><i class="fas fa-comments"></i></div>
        </div>
        <div class="stat-card-value">8</div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">等待中</span>
          <div class="stat-card-icon yellow"><i class="fas fa-clock"></i></div>
        </div>
        <div class="stat-card-value">5</div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">今日已结束</span>
          <div class="stat-card-icon"><i class="fas fa-check-double"></i></div>
        </div>
        <div class="stat-card-value">42</div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">平均响应</span>
          <div class="stat-card-icon blue"><i class="fas fa-bolt"></i></div>
        </div>
        <div class="stat-card-value">1.2分钟</div>
      </div>
    </div>

    <div class="content-grid">
      <div class="card" style="grid-column: 1 / -1;">
        <div class="card-header">
          <span class="card-title">会话列表</span>
          <el-button type="primary" size="small"><i class="fas fa-plus" style="margin-right:4px;"></i>新建会话</el-button>
        </div>

        <div class="service-list">
          <div v-for="item in serviceData" :key="item.id" class="service-item">
            <div class="service-item-left">
              <div class="mini-avatar">{{ item.user.charAt(0) }}</div>
              <div>
                <div style="font-weight: 500;">{{ item.user }}</div>
                <el-tag size="small" effect="light" :type="item.userType === '零工' ? 'primary' : 'success'">{{ item.userType }}</el-tag>
              </div>
            </div>
            <div class="service-item-center">
              <div class="service-type">{{ item.type }}</div>
              <div class="service-wait">等待 {{ item.waitTime }}</div>
            </div>
            <div class="service-item-right">
              <span :class="['status-badge', item.statusClass]">{{ item.status }}</span>
              <el-button v-if="item.status !== '已结束'" type="primary" link size="small">处理</el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <span class="card-title">快捷回复</span>
          <el-button link type="primary" size="small">管理</el-button>
        </div>
        <div class="quick-replies">
          <div v-for="(reply, index) in quickReplies" :key="index" class="reply-item">
            {{ reply }}
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <span class="card-title">FAQ设置</span>
          <el-button link type="primary" size="small">管理</el-button>
        </div>
        <div class="faq-list">
          <div v-for="(faq, index) in faqList" :key="index" class="faq-item">
            <i class="fas fa-question-circle" style="color: var(--primary); margin-right: 8px;"></i>
            {{ faq }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { serviceData } from '@/mock'

console.warn('[API] 客服会话 API 后端暂未接入，当前使用 mock 数据')

const quickReplies = [
  '您好，请问有什么可以帮助您的？',
  '请稍等，正在为您查询...',
  '您的问题已记录，我们会尽快处理',
  '感谢您的反馈，祝您生活愉快！',
  '您可以通过实名认证来接单哦~'
]

const faqList = [
  '如何成为平台零工？',
  '如何发布招工信息？',
  '工资如何结算？',
  '忘记密码怎么办？',
  '如何提现？'
]
</script>

<style scoped>
.service-list {
  border: 1px solid var(--border);
  border-radius: 8px;
  overflow: hidden;
}

.service-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 16px;
  border-bottom: 1px solid var(--border);
  
  &:last-child { border-bottom: none; }
  &:hover { background: var(--bg-page); }
}

.service-item-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 160px;
}

.mini-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF8C42, #FF6B35);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
}

.service-item-center {
  flex: 1;
  
  .service-type { font-weight: 500; }
  .service-wait { font-size: 12px; color: var(--text-muted); margin-top: 2px; }
}

.service-item-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.quick-replies, .faq-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.reply-item {
  padding: 10px 14px;
  background: var(--bg-page);
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover { background: #FFE8DC; color: var(--primary); }
}

.faq-item {
  padding: 10px 0;
  border-bottom: 1px solid var(--border);
  font-size: 13px;
  cursor: pointer;
  
  &:last-child { border-bottom: none; }
  &:hover { color: var(--primary); }
}
</style>
