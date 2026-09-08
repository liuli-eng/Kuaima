<template>
  <el-dialog
    v-model="visible"
    title="未读公告"
    width="560px"
    :close-on-click-modal="false"
    class="unread-notice-modal"
    @close="onClose"
  >
    <div v-if="loading" class="unread-loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中…</span>
    </div>
    <div v-else-if="!notices.length" class="unread-empty">暂无未读公告</div>
    <div v-else class="unread-list">
      <div v-for="(n, idx) in notices" :key="n.id" class="unread-item">
        <div class="unread-item-head">
          <el-tag size="small" type="primary" effect="light">{{ n.type || '系统' }}</el-tag>
          <span class="unread-item-time">{{ formatTime(n.publishTime) }}</span>
        </div>
        <div class="unread-item-title">{{ n.title }}</div>
        <div v-if="n.content" class="unread-item-content">{{ n.content }}</div>
        <div v-if="n.publisher" class="unread-item-publisher">发布人：{{ n.publisher }}</div>
      </div>
    </div>
    <template #footer>
      <div class="unread-footer">
        <span class="unread-count" v-if="notices.length">共 {{ notices.length }} 条未读</span>
        <div>
          <button class="btn btn-outline btn-sm" @click="markAllAndClose">全部已读并关闭</button>
          <button class="btn btn-primary btn-sm" @click="close">关闭</button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { listUnreadNotices, markNoticeRead } from '@/api/content'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const visible = ref(false)
const loading = ref(false)
const notices = ref([])

/** 格式化时间：兼容 2026-09-08T12:27:41 / 2026-09-08 12:27:41.454935 → 2026-09-08 12:27 */
const formatTime = (t) => {
  if (!t) return '-'
  const s = String(t).replace('T', ' ')
  return s.length > 16 ? s.substring(0, 16) : s
}

/** 当天是否已弹过：localStorage key = `unread_popup_<username>_<YYYY-MM-DD>` */
const todayKey = () => {
  const d = new Date()
  const ymd = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  return `unread_popup_${userStore.userInfo.username}_${ymd}`
}

const loadUnread = async () => {
  loading.value = true
  try {
    const res = await listUnreadNotices()
    notices.value = Array.isArray(res?.data) ? res.data : []
  } catch (e) {
    console.warn('[UnreadNotice] 获取失败:', e)
    notices.value = []
  } finally {
    loading.value = false
  }
}

const markAllAndClose = async () => {
  try {
    await Promise.all(notices.value.map(n => markNoticeRead(n.id).catch(() => null)))
    notices.value = []
    visible.value = false
  } catch (e) {
    visible.value = false
  }
}

const close = () => { visible.value = false }

const onClose = () => {
  // 关闭即记为当天已展示，当天不再弹
  try { localStorage.setItem(todayKey(), '1') } catch (e) {}
}

onMounted(async () => {
  // 未登录不处理
  if (!userStore.isLoggedIn) return
  // 当天已弹过则不再弹
  try {
    if (localStorage.getItem(todayKey()) === '1') return
  } catch (e) {}
  await loadUnread()
  if (notices.value.length) {
    visible.value = true
  } else {
    // 没有未读也标记今天已检查过，避免频繁请求
    try { localStorage.setItem(todayKey(), '1') } catch (e) {}
  }
})
</script>

<style scoped>
.unread-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px 0;
  color: var(--text-secondary, #6B7280);
}
.unread-empty {
  text-align: center;
  padding: 40px 0;
  color: var(--text-muted, #9CA3AF);
}
.unread-list {
  max-height: 420px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.unread-item {
  border: 1px solid var(--border, #E5E7EB);
  border-radius: 8px;
  padding: 12px 14px;
  background: #FAFAFA;
}
.unread-item-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.unread-item-time {
  font-size: 12px;
  color: var(--text-muted, #9CA3AF);
}
.unread-item-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary, #1F2937);
  margin-bottom: 6px;
}
.unread-item-content {
  font-size: 13px;
  color: var(--text-secondary, #6B7280);
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}
.unread-item-publisher {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-muted, #9CA3AF);
}
.unread-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.unread-count {
  font-size: 12px;
  color: var(--text-muted, #9CA3AF);
}
.btn-sm {
  padding: 6px 14px;
  font-size: 13px;
  border-radius: 6px;
  margin-left: 8px;
}
</style>
