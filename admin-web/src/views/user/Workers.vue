<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">零工管理</h1>
      <p class="page-desc">管理平台所有零工用户，支持查看、冻结、解冻等操作</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">总人数</span>
          <div class="stat-card-icon"><i class="fas fa-users"></i></div>
        </div>
        <div class="stat-card-value">{{ stats.total }}</div>
        <div class="stat-card-change"><span class="text-muted">全部零工用户</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">正常</span>
          <div class="stat-card-icon green"><i class="fas fa-check-circle"></i></div>
        </div>
        <div class="stat-card-value">{{ stats.normal }}</div>
        <div class="stat-card-change"><span class="text-muted">正常状态</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">已冻结</span>
          <div class="stat-card-icon yellow"><i class="fas fa-lock"></i></div>
        </div>
        <div class="stat-card-value">{{ stats.frozen }}</div>
        <div class="stat-card-change"><span class="text-muted">冻结状态</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">已认证</span>
          <div class="stat-card-icon blue"><i class="fas fa-id-card"></i></div>
        </div>
        <div class="stat-card-value">{{ stats.certified }}</div>
        <div class="stat-card-change"><span class="text-muted">实名认证</span></div>
      </div>
    </div>

    <!-- 筛选和表格 -->
    <div class="card">
      <div class="filter-bar">
        <el-input v-model="searchKeyword" placeholder="搜索姓名/手机号/ID" clearable style="width: 240px;" prefix-icon="Search" />
        <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px;">
          <el-option label="正常" value="正常" />
          <el-option label="冻结" value="冻结" />
        </el-select>
        <div class="date-picker-wrap" style="width: 240px;">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 100%;" />
        </div>
        <button class="btn btn-primary btn-sm" @click="handleSearch"><i class="fas fa-search"></i> 查询</button>
        <button class="btn btn-outline btn-sm" @click="handleReset"><i class="fas fa-rotate-left"></i> 重置</button>
        <button class="btn btn-outline btn-sm" style="margin-left: auto;"><i class="fas fa-download"></i> 导出</button>
      </div>

      <el-table :data="tableData" stripe :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }">
        <el-table-column type="selection" show-overflow-tooltip />
        <el-table-column prop="id" label="零工ID" show-overflow-tooltip />
        <el-table-column label="用户" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="user-cell">
              <span class="mini-avatar" :style="{ background: row.avatarColor || getAvatarColor(row.name) }">{{ getAvatarLetter(row.name) }}</span>
              <div>
                <div style="font-weight: 500;">{{ row.name }}</div>
                <div v-if="row.nickname && row.nickname !== row.name" class="text-muted" style="font-size: 12px;">{{ row.nickname }}</div>
                <div class="text-muted" style="font-size: 12px;">{{ row.phone }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="certStatus" label="实名认证" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag :type="isPhoneVerified(row) ? 'success' : 'info'" effect="light">{{ isPhoneVerified(row) ? '手机号已验证' : '手机号未验证' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="技能标签" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag v-for="(skill, idx) in normalizeSkills(row.skills)" :key="idx" style="margin-right: 4px; margin-bottom: 2px;" type="warning" effect="light" size="small">{{ skill }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orders" label="完成订单" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.completedOrders ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="registerTime" label="注册时间" show-overflow-tooltip>
          <template #default="{ row }">{{ formatDateTime(row.registerTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <div class="action-cell">
              <el-button link type="primary" size="small" @click="handleDetail(row)">显示</el-button>
              <el-button link type="warning" size="small" v-if="isNormal(row.status)" @click="handleFreeze(row)">冻结</el-button>
              <el-button link type="success" size="small" v-else @click="handleUnfreeze(row)">解冻</el-button>
              <el-button link type="danger" size="small">重置密码</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <div class="pagination-info">共 {{ total }} 条记录</div>
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="sizes, prev, pager, next, jumper"
          background
          @size-change="onSizeChange"
          @current-change="onPageChange"
        />
      </div>
    </div>

    <!-- 零工详情弹窗 -->
    <el-dialog v-model="detailVisible" width="680px" :show-close="false" class="worker-detail-dialog" v-loading="detailLoading">
      <div class="detail-dialog-header">
        <span class="detail-dialog-title">零工详情</span>
        <div class="detail-dialog-close" @click="detailVisible = false"><i class="fas fa-times"></i></div>
      </div>
      <div class="detail-dialog-body" v-if="detailData.id">
        <!-- 头像区域 -->
        <div class="detail-avatar-section">
          <div class="detail-avatar" :style="{ background: getAvatarColor(detailData.nickname || detailData.username) }">{{ getAvatarLetter(detailData.nickname || detailData.realName || detailData.username) }}</div>
          <div class="detail-basic-info">
            <h3>{{ detailData.nickname || detailData.realName || detailData.username || '-' }}</h3>
            <p><i class="fas fa-mobile-alt"></i> <span>{{ detailData.phone || '-' }}</span></p>
            <p>
              <i class="fas fa-id-card"></i>
              <span :style="{ color: detailData.realnameStatus === 'APPROVED' ? 'var(--success)' : 'var(--text-muted)' }">
                {{ formatRealnameStatus(detailData.realnameStatus) }}
              </span>
            </p>
            <div style="margin-top: 8px;">
              <span :class="['status-badge', isNormal(detailData.status) ? 'success' : 'danger']">
                {{ formatStatus(detailData.status) }}
              </span>
            </div>
          </div>
        </div>

        <!-- 基本信息 -->
        <div class="detail-section-title">基本信息</div>
        <div class="detail-info-grid" style="margin-bottom: 20px;">
          <div class="detail-info-item">
            <span class="detail-info-label">零工ID</span>
            <span class="detail-info-value">{{ detailData.id }}</span>
          </div>
          <div class="detail-info-item">
            <span class="detail-info-label">性别</span>
            <span class="detail-info-value">{{ formatGender(detailData.gender) }}</span>
          </div>
          <div class="detail-info-item">
            <span class="detail-info-label">年龄</span>
            <span class="detail-info-value">{{ detailData.age || '-' }}</span>
          </div>
          <div class="detail-info-item">
            <span class="detail-info-label">城市</span>
            <span class="detail-info-value">{{ detailData.city || '-' }}</span>
          </div>
          <div class="detail-info-item">
            <span class="detail-info-label">注册时间</span>
            <span class="detail-info-value">{{ formatDateTimeFull(detailData.date) }}</span>
          </div>
          <div class="detail-info-item">
            <span class="detail-info-label">最近活跃</span>
            <span class="detail-info-value">{{ formatDateTimeFull(detailData.timestamp) }}</span>
          </div>
        </div>

        <!-- 技能与数据 -->
        <div class="detail-section-title">技能与数据</div>
        <div class="detail-info-grid" style="margin-bottom: 20px;">
          <div class="detail-info-item" style="grid-column: span 2;">
            <span class="detail-info-label">技能标签</span>
            <div style="display: flex; gap: 6px; margin-top: 4px; flex-wrap: wrap;">
              <template v-if="detailData.skills">
                <span v-for="s in String(detailData.skills).split(',')" :key="s" class="detail-tag detail-tag-blue">{{ s.trim() }}</span>
              </template>
              <span v-else class="text-muted">-</span>
            </div>
          </div>
          <div class="detail-info-item">
            <span class="detail-info-label">完成订单</span>
            <span class="detail-info-value">{{ detailData.completedOrders ?? 0 }}</span>
          </div>
          <div class="detail-info-item">
            <span class="detail-info-label">信用分</span>
            <span class="detail-info-value" :style="{ color: getCreditColor(detailData.creditScore) }">{{ detailData.creditScore ?? '-' }}</span>
          </div>
          <div class="detail-info-item">
            <span class="detail-info-label">钱包余额</span>
            <span class="detail-info-value">{{ detailData.balance != null ? `¥${(detailData.balance / 100).toFixed(2)}` : '-' }}</span>
          </div>
          <div class="detail-info-item">
            <span class="detail-info-label">工作年限</span>
            <span class="detail-info-value">{{ detailData.workYears != null ? `${detailData.workYears}年` : '-' }}</span>
          </div>
        </div>

        <!-- 账户状态 -->
        <div class="detail-section-title">账户状态</div>
        <div class="detail-info-grid">
          <div class="detail-info-item">
            <span class="detail-info-label">实名状态</span>
            <span class="detail-info-value" :style="{ color: detailData.realnameStatus === 'APPROVED' ? 'var(--success)' : 'inherit' }">
              {{ formatRealnameStatus(detailData.realnameStatus) }}
            </span>
          </div>
          <div class="detail-info-item">
            <span class="detail-info-label">账户状态</span>
            <span class="detail-info-value" :style="{ color: isNormal(detailData.status) ? 'var(--success)' : '#EF4444' }">
              {{ formatStatus(detailData.status) }}
            </span>
          </div>
        </div>
      </div>
      <div class="detail-dialog-footer">
        <button class="btn btn-outline" @click="detailVisible = false">关闭</button>
        <button v-if="isNormal(detailData.status)" class="btn btn-danger" @click="handleFreeze(detailData); detailVisible = false">
          <i class="fas fa-ban"></i> 冻结账户
        </button>
        <button v-else class="btn btn-success" @click="handleUnfreeze(detailData); detailVisible = false">
          <i class="fas fa-lock-open"></i> 解冻账户
        </button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listWorkers, getUser, freezeUser, unfreezeUser } from '@/api/user'

const searchKeyword = ref('')
const statusFilter = ref('')
const dateRange = ref([])

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

// 统计数据
const stats = ref({ total: '-', normal: '-', frozen: '-', certified: '-' })

// 详情弹窗
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref({})

// 加载统计
const loadStats = async () => {
  try {
    const [allRes, normalRes, frozenRes] = await Promise.all([
      listWorkers({ page: 0, size: 1 }),
      listWorkers({ status: '正常', page: 0, size: 1 }),
      listWorkers({ status: '冻结', page: 0, size: 1 })
    ])
    stats.value.total = allRes.total ?? 0
    stats.value.normal = normalRes.total ?? 0
    stats.value.frozen = frozenRes.total ?? 0
    const allDataRes = await listWorkers({ page: 0, size: 1000 })
    const d = allDataRes.data
    const list = Array.isArray(d) ? d : (d?.content || [])
    stats.value.certified = list.filter(u => u.certStatus === true || u.certStatus === 1 || u.certStatus === '已认证').length
  } catch (e) {
    console.warn('[Workers] 加载统计失败:', e)
  }
}

// 后端 Worker 真实字段
const normalizeWorker = (item) => {
  return {
    ...item,
    name: item.username || item.phone,
    nickname: item.nickname || '',
    registerTime: item.date || item.timestamp,
  }
}

const formatDateTime = (t) => {
  if (!t) return '-'
  if (typeof t === 'number') {
    const d = new Date(t)
    return isNaN(d.getTime()) ? '-' : `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  }
  const s = String(t)
  if (/^\d{4}-\d{2}-\d{2}$/.test(s)) return s
  return s.replace('T', ' ').substring(0, 10)
}

const formatDateTimeFull = (t) => {
  if (!t || t === 0) return '-'
  if (typeof t === 'number') {
    const d = new Date(t)
    if (isNaN(d.getTime())) return '-'
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const h = String(d.getHours()).padStart(2, '0')
    const min = String(d.getMinutes()).padStart(2, '0')
    return `${y}-${m}-${day} ${h}:${min}`
  }
  const s = String(t).replace('T', ' ')
  if (/^\d{4}-\d{2}-\d{2}$/.test(s)) return s
  return s.length > 16 ? s.substring(0, 16) : s
}

const loadData = async () => {
  try {
    const result = await listWorkers({
      keyword: searchKeyword.value || undefined,
      status: statusFilter.value || undefined,
      page: currentPage.value - 1,
      size: pageSize.value
    })
    const d = result.data
    tableData.value = (Array.isArray(d) ? d : (d?.content || d?.list || [])).map(normalizeWorker)
    total.value = result.total ?? d?.totalElements ?? d?.total ?? 0
  } catch (e) {
    console.warn('[Workers] 加载列表失败:', e)
    tableData.value = []
    total.value = 0
  }
}

const getCreditColor = (score) => {
  if (!score) return 'inherit'
  if (score >= 80) return '#10B981'
  if (score >= 60) return '#F59E0B'
  return '#EF4444'
}

const getAvatarColor = (name) => {
  const palette = ['#FF6B35', '#2563EB', '#10B981', '#8B5CF6', '#F59E0B', '#EC4899', '#06B6D4', '#64748B']
  const idx = (name || '').length % palette.length
  return palette[idx]
}
const getAvatarLetter = (name) => (name || '').charAt(0) || '?'
const normalizeSkills = (skills) => {
  if (!skills) return []
  if (Array.isArray(skills)) return skills
  if (typeof skills === 'string') return skills.split(',').filter(Boolean)
  return []
}
const isPhoneVerified = (row) => {
  return row.certStatus === true
    || row.certStatus === 1
    || row.certStatus === '已通过'
    || row.certStatus === '已认证'
    || row.certStatus === 'VERIFIED'
    || row.certStatus === 'APPROVED'
}
const isNormal = (s) => s === '正常' || s === 'NORMAL' || s === 1

const formatStatus = (s) => {
  if (s === 'NORMAL' || s === 1 || s === '正常') return '正常'
  if (s === 'FROZEN' || s === 0 || s === '冻结') return '冻结'
  return s || '-'
}
const formatRealnameStatus = (s) => {
  if (s === 'APPROVED') return '已认证'
  if (s === 'PENDING') return '待审核'
  if (s === 'REJECTED') return '已拒绝'
  return '未认证'
}
const formatGender = (g) => {
  if (g === '男' || g === 'male' || g === 'M') return '男'
  if (g === '女' || g === 'female' || g === 'F') return '女'
  return g || '-'
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}
const handleReset = () => {
  searchKeyword.value = ''
  statusFilter.value = ''
  dateRange.value = []
  currentPage.value = 1
  loadData()
}

const onSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadData()
}

const onPageChange = (page) => {
  currentPage.value = page
  loadData()
}

// 详情弹窗
const handleDetail = async (row) => {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = {}
  try {
    const res = await getUser(row.id)
    detailData.value = res.data || res || {}
  } catch (e) {
    ElMessage.error('加载详情失败')
    console.warn('[Workers] 加载详情失败:', e)
  } finally {
    detailLoading.value = false
  }
}

// 冻结 / 解冻
const handleFreeze = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要冻结用户 ${row.name || row.nickname || row.id} 吗？`, '提示', { type: 'warning' })
    await freezeUser(row.id)
    ElMessage.success('冻结成功')
    loadData()
    loadStats()
  } catch (e) {
    if (e !== 'cancel') console.warn('[Workers] 冻结失败:', e)
  }
}
const handleUnfreeze = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要解冻用户 ${row.name || row.nickname || row.id} 吗？`, '提示', { type: 'warning' })
    await unfreezeUser(row.id)
    ElMessage.success('解冻成功')
    loadData()
    loadStats()
  } catch (e) {
    if (e !== 'cancel') console.warn('[Workers] 解冻失败:', e)
  }
}

onMounted(() => {
  loadData()
  loadStats()
})
</script>

<style scoped>
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 操作按钮强制一行排列 */
.action-cell {
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  white-space: nowrap;
}
.action-cell .el-button {
  margin-left: 0;
  margin-right: 8px;
  padding: 0;
}
.action-cell .el-button:last-child {
  margin-right: 0;
}

.mini-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  flex-shrink: 0;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
}

.pagination-info {
  font-size: 13px;
  color: var(--text-secondary);
}

/* ====== 详情弹窗样式 ====== */
.worker-detail-dialog :deep(.el-dialog__header) {
  display: none;
}
.worker-detail-dialog :deep(.el-dialog__body) {
  padding: 0;
}
.worker-detail-dialog :deep(.el-dialog__footer) {
  display: none;
}

.detail-dialog-header {
  padding: 20px 24px;
  border-bottom: 1px solid var(--border, #E5E7EB);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.detail-dialog-title {
  font-size: 18px;
  font-weight: 600;
}
.detail-dialog-close {
  cursor: pointer;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  color: var(--text-muted, #9CA3AF);
}
.detail-dialog-close:hover {
  background: #F3F4F6;
  color: var(--text-primary);
}

.detail-dialog-body {
  padding: 24px;
  overflow-y: auto;
  max-height: 60vh;
}

.detail-avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
}
.detail-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  font-weight: 600;
  color: #fff;
  flex-shrink: 0;
}
.detail-basic-info h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 6px 0;
}
.detail-basic-info p {
  margin: 3px 0;
  font-size: 13px;
  color: var(--text-secondary, #6B7280);
  display: flex;
  align-items: center;
  gap: 6px;
}
.detail-basic-info p i {
  color: var(--text-muted, #9CA3AF);
  width: 16px;
  text-align: center;
}

.detail-section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary, #1F2937);
  margin-bottom: 12px;
  padding-left: 8px;
  border-left: 3px solid var(--primary, #FF6B35);
}

.detail-info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0;
}
.detail-info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px 0;
  border-bottom: 1px solid var(--border, #E5E7EB);
}
.detail-info-item:nth-child(odd) {
  padding-right: 24px;
}
.detail-info-item:nth-child(even) {
  padding-left: 24px;
  border-left: 1px solid var(--border, #E5E7EB);
}
.detail-info-label {
  font-size: 12px;
  color: var(--text-muted, #9CA3AF);
}
.detail-info-value {
  font-size: 14px;
  color: var(--text-primary, #1F2937);
  font-weight: 500;
}

.text-muted {
  color: var(--text-muted, #9CA3AF);
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}
.status-badge.success {
  background: #ECFDF5;
  color: #059669;
}
.status-badge.danger {
  background: #FEF2F2;
  color: #DC2626;
}

.detail-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  background: #F3F4F6;
  color: #6B7280;
}
.detail-tag-blue {
  background: #DBEAFE;
  color: #2563EB;
}

.detail-dialog-footer {
  padding: 16px 24px;
  border-top: 1px solid var(--border, #E5E7EB);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-success {
  background: #10B981;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
}
.btn-success:hover {
  opacity: 0.9;
}
</style>
