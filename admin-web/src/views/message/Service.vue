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
        <div class="stat-card-value">{{ stats.openSessions || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">已结束会话</span>
          <div class="stat-card-icon"><i class="fas fa-check-double"></i></div>
        </div>
        <div class="stat-card-value">{{ stats.closedSessions || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">快捷回复</span>
          <div class="stat-card-icon blue"><i class="fas fa-reply"></i></div>
        </div>
        <div class="stat-card-value">{{ stats.totalQuickReplies || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">FAQ 数量</span>
          <div class="stat-card-icon yellow"><i class="fas fa-circle-question"></i></div>
        </div>
        <div class="stat-card-value">{{ stats.totalFaqs || 0 }}</div>
      </div>
    </div>

    <div class="content-grid">
      <div class="card" style="grid-column: 1 / -1;">
        <div class="card-header">
          <span class="card-title">会话列表</span>
          <el-select v-model="filterStatus" size="small" style="width:120px;" @change="loadSessions">
            <el-option label="全部" value="" />
            <el-option label="进行中" value="OPEN" />
            <el-option label="已结束" value="CLOSED" />
          </el-select>
        </div>

        <div class="service-list">
          <div v-for="item in sessions" :key="item.id" class="service-item" @click="goChat(item.id)">
            <div class="service-item-left">
              <div class="mini-avatar">{{ String(item.userId || 'U').charAt(0) }}</div>
              <div>
                <div style="font-weight: 500;">用户 {{ item.userId }}</div>
                <el-tag size="small" effect="light" :type="item.status === 'OPEN' ? 'success' : 'info'">
                  {{ item.status === 'OPEN' ? '进行中' : '已结束' }}
                </el-tag>
              </div>
            </div>
            <div class="service-item-right">
              <span class="service-time">{{ formatTime(item.timestamp) }}</span>
              <el-button type="primary" link size="small">处理</el-button>
            </div>
          </div>
          <div v-if="sessions.length === 0" style="text-align:center;padding:40px;color:#999;">
            暂无会话
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <span class="card-title">快捷回复</span>
          <el-button link type="primary" size="small" @click="showReplyDialog()">新增</el-button>
        </div>
        <div class="quick-replies">
          <div v-for="reply in quickReplies" :key="reply.id" class="reply-card">
            <div class="reply-card-text">{{ reply.content }}</div>
            <div class="reply-card-actions">
              <el-button link size="small" @click.stop="showReplyDialog(reply)">编辑</el-button>
              <el-button link size="small" type="danger" @click.stop="handleDeleteReply(reply.id)">删除</el-button>
            </div>
          </div>
          <div v-if="quickReplies.length === 0" style="text-align:center;padding:20px;color:#999;font-size:13px;">
            暂无快捷回复
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <span class="card-title">FAQ 管理</span>
          <el-button link type="primary" size="small" @click="showFaqDialog()">新增</el-button>
        </div>
        <div class="quick-replies">
          <div v-for="faq in faqs" :key="faq.id" class="reply-card">
            <div class="reply-card-text"><b>{{ faq.question }}</b></div>
            <div class="reply-card-answer">{{ faq.answer }}</div>
            <div class="reply-card-actions">
              <el-button link size="small" @click.stop="showFaqDialog(faq)">编辑</el-button>
              <el-button link size="small" type="danger" @click.stop="handleDeleteFaq(faq.id)">删除</el-button>
            </div>
          </div>
          <div v-if="faqs.length === 0" style="text-align:center;padding:20px;color:#999;font-size:13px;">
            暂无 FAQ
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷回复编辑弹窗 -->
    <el-dialog v-model="replyDialogVisible" :title="replyForm.id ? '编辑快捷回复' : '新增快捷回复'" width="500px">
      <el-form :model="replyForm" label-width="80px">
        <el-form-item label="回复内容">
          <el-input v-model="replyForm.content" type="textarea" :rows="3" placeholder="输入快捷回复内容" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="replyForm.category" placeholder="如：物流、退款" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="replyForm.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="replyForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveReply">保存</el-button>
      </template>
    </el-dialog>

    <!-- FAQ 编辑弹窗 -->
    <el-dialog v-model="faqDialogVisible" :title="faqForm.id ? '编辑FAQ' : '新增FAQ'" width="600px">
      <el-form :model="faqForm" label-width="80px">
        <el-form-item label="问题">
          <el-input v-model="faqForm.question" placeholder="输入常见问题" />
        </el-form-item>
        <el-form-item label="答案">
          <el-input v-model="faqForm.answer" type="textarea" :rows="4" placeholder="输入问题答案" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="faqForm.category" placeholder="如：账户、订单" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="faqForm.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="faqForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="faqDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveFaq">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getServiceStats, getServiceSessions,
  getQuickReplies, createQuickReply, updateQuickReply, deleteQuickReply,
  getFaqs, createFaq, updateFaq, deleteFaq
} from '@/api/service'

const router = useRouter()
const stats = ref({})
const sessions = ref([])
const filterStatus = ref('')
const quickReplies = ref([])
const faqs = ref([])

const formatTime = (ts) => {
  if (!ts) return ''
  const d = new Date(ts)
  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}`
}

const loadStats = async () => {
  try {
    const res = await getServiceStats()
    stats.value = res.data || {}
  } catch (e) { console.error(e) }
}

const loadSessions = async () => {
  try {
    const res = await getServiceSessions({ status: filterStatus.value, page: 0, size: 50 })
    sessions.value = res.data || []
  } catch (e) { console.error(e) }
}

const loadQuickReplies = async () => {
  try {
    const res = await getQuickReplies()
    quickReplies.value = res.data || []
  } catch (e) { console.error(e) }
}

const loadFaqs = async () => {
  try {
    const res = await getFaqs()
    faqs.value = res.data || []
  } catch (e) { console.error(e) }
}

const goChat = (id) => router.push(`/message/service-chat/${id}`)

// ====== 快捷回复 ======
const replyDialogVisible = ref(false)
const replyForm = reactive({ id: null, content: '', category: '', sortOrder: 0, enabled: true })

const showReplyDialog = (item) => {
  if (item) {
    Object.assign(replyForm, item)
  } else {
    Object.assign(replyForm, { id: null, content: '', category: '', sortOrder: 0, enabled: true })
  }
  replyDialogVisible.value = true
}

const saveReply = async () => {
  if (!replyForm.content.trim()) { ElMessage.warning('请输入回复内容'); return }
  try {
    if (replyForm.id) {
      await updateQuickReply(replyForm.id, replyForm)
    } else {
      await createQuickReply(replyForm)
    }
    ElMessage.success('保存成功')
    replyDialogVisible.value = false
    loadQuickReplies()
    loadStats()
  } catch (e) { ElMessage.error('保存失败') }
}

const handleDeleteReply = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除此快捷回复？', '提示', { type: 'warning' })
    await deleteQuickReply(id)
    ElMessage.success('已删除')
    loadQuickReplies()
    loadStats()
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

// ====== FAQ ======
const faqDialogVisible = ref(false)
const faqForm = reactive({ id: null, question: '', answer: '', category: '', sortOrder: 0, enabled: true })

const showFaqDialog = (item) => {
  if (item) {
    Object.assign(faqForm, item)
  } else {
    Object.assign(faqForm, { id: null, question: '', answer: '', category: '', sortOrder: 0, enabled: true })
  }
  faqDialogVisible.value = true
}

const saveFaq = async () => {
  if (!faqForm.question.trim()) { ElMessage.warning('请输入问题'); return }
  try {
    if (faqForm.id) {
      await updateFaq(faqForm.id, faqForm)
    } else {
      await createFaq(faqForm)
    }
    ElMessage.success('保存成功')
    faqDialogVisible.value = false
    loadFaqs()
    loadStats()
  } catch (e) { ElMessage.error('保存失败') }
}

const handleDeleteFaq = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除此FAQ？', '提示', { type: 'warning' })
    await deleteFaq(id)
    ElMessage.success('已删除')
    loadFaqs()
    loadStats()
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

onMounted(() => {
  loadStats()
  loadSessions()
  loadQuickReplies()
  loadFaqs()
})
</script>

<style scoped>
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.stat-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.stat-card-title {
  font-size: 14px;
  color: var(--text-secondary);
}
.stat-card-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  background: #fff0eb;
  color: #FF6B35;
}
.stat-card-icon.blue { background: #eff6ff; color: #409eff; }
.stat-card-icon.green { background: #f0f9eb; color: #67c23a; }
.stat-card-icon.yellow { background: #fdf6ec; color: #e6a23c; }
.stat-card-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}
.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
}
.service-list {
  display: flex;
  flex-direction: column;
}
.service-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}
.service-item:hover {
  background: #f8f9fa;
}
.service-item-left {
  display: flex;
  align-items: center;
  gap: 12px;
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
.service-item-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.service-time {
  font-size: 12px;
  color: #999;
}
.quick-replies {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.reply-card {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 3px solid #FF6B35;
}
.reply-card-text {
  font-size: 13px;
  color: #333;
  margin-bottom: 6px;
}
.reply-card-answer {
  font-size: 12px;
  color: #666;
  margin-bottom: 6px;
  line-height: 1.5;
}
.reply-card-actions {
  display: flex;
  gap: 8px;
}
</style>
