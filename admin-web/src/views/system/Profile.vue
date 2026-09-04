<template>
  <div class="profile-page">
    <div class="page-header">
      <h1 class="page-title">个人资料</h1>
      <p class="page-desc">查看和编辑管理员个人信息</p>
    </div>

    <!-- 顶部 Hero -->
    <div class="profile-hero">
      <div class="profile-avatar-large">{{ displayName.charAt(0) }}</div>
      <div class="profile-hero-info">
        <div class="profile-name">{{ displayName }}</div>
        <div class="profile-title-tag">
          <i class="fas fa-crown" style="margin-right:4px;"></i>{{ displayRole }}
        </div>
        <div class="profile-meta">
          <span><i class="fas fa-envelope"></i> {{ form.email || '-' }}</span>
          <span><i class="fas fa-phone"></i> {{ form.phone || '-' }}</span>
          <span><i class="fas fa-calendar"></i> 加入时间：{{ joinDate || '-' }}</span>
        </div>
      </div>
    </div>

    <!-- 数据统计 -->
    <div class="profile-stats">
      <div class="profile-stat">
        <div class="profile-stat-value">0</div>
        <div class="profile-stat-label">处理任务</div>
      </div>
      <div class="profile-stat">
        <div class="profile-stat-value">0</div>
        <div class="profile-stat-label">审核通过</div>
      </div>
      <div class="profile-stat">
        <div class="profile-stat-value">0</div>
        <div class="profile-stat-label">回复消息</div>
      </div>
      <div class="profile-stat">
        <div class="profile-stat-value">-</div>
        <div class="profile-stat-label">查看次数</div>
      </div>
    </div>

    <!-- 基本信息 -->
    <div class="profile-section">
      <div class="profile-section-title">
        <i class="fas fa-user-edit"></i> 基本信息
      </div>
      <el-form :model="form" label-position="top" class="profile-form-grid">
        <el-form-item label="姓名" class="form-group">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="工号" class="form-group">
          <el-input v-model="form.employeeNo" readonly placeholder="系统自动分配" />
        </el-form-item>
        <el-form-item label="邮箱" class="form-group">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" class="form-group">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="所属部门" class="form-group">
          <el-select v-model="form.dept" placeholder="请选择部门" style="width: 100%;">
            <el-option label="技术运营部" value="技术运营部" />
            <el-option label="客户服务部" value="客户服务部" />
            <el-option label="财务管理部" value="财务管理部" />
            <el-option label="市场推广部" value="市场推广部" />
          </el-select>
        </el-form-item>
        <el-form-item label="职位" class="form-group">
          <el-input v-model="form.role" readonly />
        </el-form-item>
        <el-form-item label="个人简介" class="form-group full-width">
          <el-input
            v-model="form.intro"
            type="textarea"
            :rows="3"
            placeholder="请输入个人简介..."
          />
        </el-form-item>
      </el-form>
      <div class="profile-actions">
        <el-button type="primary" :loading="saving" @click="handleSave">
          <i class="fas fa-save" style="margin-right:4px;"></i> 保存修改
        </el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </div>

    <!-- 安全设置 -->
    <div class="profile-section">
      <div class="profile-section-title">
        <i class="fas fa-shield-alt"></i> 安全设置
      </div>
      <div class="profile-form-grid">
        <div class="form-group">
          <label class="form-label">两步验证</label>
          <div class="toggle-row">
            <el-switch v-model="security.twoStep" />
            <span class="toggle-desc">{{ security.twoStep ? '已开启（手机短信验证）' : '未开启' }}</span>
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">登录通知</label>
          <div class="toggle-row">
            <el-switch v-model="security.loginNotify" />
            <span class="toggle-desc">{{ security.loginNotify ? '异常登录时发送短信' : '关闭通知' }}</span>
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">IP白名单</label>
          <el-input v-model="security.ipWhitelist" placeholder="设置允许登录的IP地址" />
        </div>
        <div class="form-group">
          <label class="form-label">会话超时时间</label>
          <el-select v-model="security.sessionTimeout" style="width: 100%;">
            <el-option label="30分钟" value="30分钟" />
            <el-option label="1小时" value="1小时" />
            <el-option label="2小时" value="2小时" />
            <el-option label="24小时" value="24小时" />
          </el-select>
        </div>
      </div>
    </div>

    <!-- 最近操作记录 -->
    <div class="profile-section">
      <div class="profile-section-title">
        <i class="fas fa-history"></i> 最近操作记录
      </div>
      <div v-if="recentLogs.length === 0" class="empty-tip">暂无操作记录</div>
      <div v-for="log in recentLogs" :key="log.id" class="timeline-item">
        <div class="timeline-dot"></div>
        <div class="timeline-content">
          <div class="timeline-title">{{ log.type || '操作' }}{{ log.target ? ' · ' + log.target : '' }}</div>
          <div class="timeline-desc">{{ log.detail || log.operator || '-' }}</div>
          <div class="timeline-time">
            {{ formatTime(log.createTime) }}
            <template v-if="log.ip"> · IP：{{ log.ip }}</template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { listLogs } from '@/api/system'
import request from '@/api/request'

const userStore = useUserStore()
const saving = ref(false)
const recentLogs = ref([])

const form = reactive({
  adminId: 0,
  username: '',
  employeeNo: '',
  name: '',
  email: '',
  phone: '',
  dept: '',
  role: '',
  intro: ''
})

const security = reactive({
  twoStep: true,
  loginNotify: true,
  ipWhitelist: '192.168.1.*',
  sessionTimeout: '1小时'
})

const joinDate = ref('')

const displayName = computed(() => form.name || userStore.userInfo.name || '管理员')
const displayRole = computed(() => form.role || userStore.userInfo.role || '超级管理员')

const formatDate = (dt) => {
  if (!dt) return ''
  const s = String(dt).replace('T', ' ')
  return s.length > 16 ? s.substring(0, 16) : s
}

const formatTime = (t) => {
  if (!t) return '-'
  return formatDate(t)
}

// 保存原始数据用于重置
const snapshot = reactive({})

const takeSnapshot = () => {
  Object.assign(snapshot, JSON.parse(JSON.stringify(form)))
}

const applyUserData = (data) => {
  form.adminId = data?.id || Number(userStore.userInfo.adminId) || 0
  form.username = data?.username || userStore.userInfo.username || ''
  form.name = data?.name || userStore.userInfo.name || '管理员'
  form.email = data?.email ?? userStore.userInfo.email ?? ''
  form.phone = data?.phone || ''
  form.dept = data?.dept || ''
  form.role = data?.role || userStore.userInfo.role || '超级管理员'
  form.intro = data?.intro || ''
  form.employeeNo = data?.employeeNo || (data?.id ? 'A' + String(data.id).padStart(3, '0') : '')
  joinDate.value = data?.createTime ? formatDate(data.createTime) : ''
  takeSnapshot()
}

const loadAdminDetail = async () => {
  const adminId = Number(userStore.userInfo.adminId)
  if (!adminId) {
    // 没有拿到 adminId 时，回退到 store 中的信息
    applyUserData(null)
    return
  }
  try {
    const res = await request.get(`/admin/admin-users/${adminId}`)
    const data = res?.data
    if (data) {
      applyUserData(data)
    } else {
      applyUserData(null)
    }
  } catch (e) {
    console.warn('[Profile] 加载管理员详情失败:', e)
    applyUserData(null)
  }
}

const loadRecentLogs = async () => {
  try {
    const res = await listLogs({ page: 0, size: 5 })
    const d = res?.data
    const list = Array.isArray(d) ? d : (d?.content || d?.list || [])
    recentLogs.value = list.slice(0, 5)
  } catch (e) {
    console.warn('[Profile] 加载操作记录失败:', e)
    recentLogs.value = []
  }
}

const handleSave = async () => {
  const adminId = Number(form.adminId || userStore.userInfo.adminId)
  if (!adminId) {
    ElMessage.error('未获取到管理员ID，无法保存')
    return
  }
  saving.value = true
  try {
    const payload = {
      name: form.name,
      email: form.email,
      phone: form.phone,
      dept: form.dept
    }
    const res = await request.put(`/admin/admin-users/${adminId}`, payload)
    ElMessage.success('个人资料保存成功')
    // 同步更新本地 store 显示
    if (res?.data) {
      applyUserData({ ...form, ...res.data, id: adminId })
    } else {
      takeSnapshot()
    }
    // 同步更新 store 中的 name/email
    userStore.userInfo.name = form.name
    userStore.userInfo.email = form.email
    localStorage.setItem('admin_name', form.name)
    localStorage.setItem('admin_email', form.email || '')
  } catch (e) {
    ElMessage.error('保存失败：' + (e?.message || '请稍后重试'))
  } finally {
    saving.value = false
  }
}

const handleReset = () => {
  Object.assign(form, JSON.parse(JSON.stringify(snapshot)))
  ElMessage.info('已重置为最近一次保存的数据')
}

onMounted(() => {
  loadAdminDetail()
  loadRecentLogs()
})
</script>

<style scoped>
.profile-page {
  width: 100%;
}

.profile-hero {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C5A 100%);
  border-radius: 16px;
  padding: 40px;
  color: #fff;
  display: flex;
  align-items: center;
  gap: 32px;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
}

.profile-hero::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 400px;
  height: 400px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.profile-avatar-large {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  font-weight: 700;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.profile-hero-info {
  position: relative;
  z-index: 1;
  flex: 1;
}

.profile-name {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 6px;
}

.profile-title-tag {
  display: inline-block;
  background: rgba(255, 255, 255, 0.25);
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 13px;
  margin-bottom: 16px;
}

.profile-meta {
  display: flex;
  gap: 24px;
  font-size: 14px;
  opacity: 0.9;
  flex-wrap: wrap;
}

.profile-meta span {
  display: flex;
  align-items: center;
  gap: 6px;
}

.profile-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.profile-stat {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.profile-stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--primary);
}

.profile-stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.profile-section {
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.profile-section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-primary);
}

.profile-section-title i {
  color: var(--primary);
}

.profile-form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px 24px;
}

.profile-form-grid .full-width {
  grid-column: 1 / -1;
}

.profile-form-grid :deep(.el-form-item) {
  margin-bottom: 0;
}

.profile-form-grid :deep(.el-form-item__label) {
  padding-bottom: 6px;
  font-weight: 500;
  color: var(--text-primary);
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.toggle-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toggle-desc {
  font-size: 14px;
  color: var(--text-secondary);
}

.profile-actions {
  margin-top: 20px;
  display: flex;
  gap: 12px;
}

.timeline-item {
  display: flex;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid var(--border);
}

.timeline-item:last-child {
  border-bottom: none;
}

.timeline-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--primary);
  margin-top: 6px;
  flex-shrink: 0;
  box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.15);
}

.timeline-content {
  flex: 1;
}

.timeline-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
  color: var(--text-primary);
}

.timeline-desc {
  font-size: 13px;
  color: var(--text-secondary);
}

.timeline-time {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}

.empty-tip {
  padding: 24px;
  text-align: center;
  color: var(--text-muted);
  font-size: 13px;
}

@media (max-width: 768px) {
  .profile-hero {
    flex-direction: column;
    text-align: center;
    padding: 28px;
  }

  .profile-meta {
    justify-content: center;
  }

  .profile-stats {
    grid-template-columns: repeat(2, 1fr);
  }

  .profile-form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
