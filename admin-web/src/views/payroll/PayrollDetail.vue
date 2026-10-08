<template>
  <div>
    <div class="back-bar">
      <button class="btn btn-outline btn-sm" @click="goBack"><i class="fas fa-arrow-left"></i> 返回发薪管理</button>
    </div>

    <div v-loading="loading">
      <!-- Hero 卡 -->
      <div class="card hero-card" v-if="order">
        <div class="hero-top">
          <div>
            <div class="hero-title">{{ order.title }}</div>
            <span :class="['status-badge', statusClass(order.status)]">{{ statusText(order.status) }}</span>
          </div>
          <div class="hero-amount">
            <div class="hero-amount-label">应发总金额</div>
            <div class="hero-amount-value">¥{{ fen2yuan(order.amount) }}</div>
          </div>
        </div>
        <el-descriptions :column="3" border class="hero-desc">
          <el-descriptions-item label="发薪单号">{{ order.orderNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="所属项目">{{ order.projectName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="发薪人数">{{ order.peopleCount ?? 0 }} 人</el-descriptions-item>
          <el-descriptions-item label="制单人">{{ order.creator || '—' }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatTime(order.submitTime) }}</el-descriptions-item>
          <el-descriptions-item label="审核人 / 时间">
            {{ order.reviewBy || '—' }}<template v-if="order.reviewTime"> / {{ formatTime(order.reviewTime) }}</template>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 发薪明细 -->
      <div class="card">
        <div class="card-header">
          <div class="card-title">发薪明细（{{ details.length }} 人）</div>
        </div>
        <el-table :data="details" stripe :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }">
          <el-table-column label="姓名" min-width="120">
            <template #default="{ row }">
              <div class="avatar-cell">
                <div class="mini-avatar" :class="avatarColor(row.name)">{{ (row.name || '?').charAt(0) }}</div>
                <span class="emp-name">{{ row.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="job" label="岗位" show-overflow-tooltip />
          <el-table-column prop="phone" label="手机号" show-overflow-tooltip />
          <el-table-column prop="attendDays" label="出勤天数" width="90" />
          <el-table-column label="日薪" show-overflow-tooltip>
            <template #default="{ row }">¥{{ fen2yuan(row.dailyWage) }}</template>
          </el-table-column>
          <el-table-column label="应发金额" show-overflow-tooltip>
            <template #default="{ row }"><span class="amount-cell">¥{{ fen2yuan(row.amount) }}</span></template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <span :class="['status-badge', detailStatusClass(row.status)]">{{ detailStatusText(row.status) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 底部操作 -->
      <div class="footer-actions" v-if="order">
        <template v-if="order.status === 'pending'">
          <el-button @click="exportStub('PDF')"><i class="fas fa-file-export"></i> 导出明细</el-button>
          <el-button type="success" @click="doApprove"><i class="fas fa-check"></i> 通过</el-button>
          <el-button type="danger" @click="doReject"><i class="fas fa-xmark"></i> 驳回</el-button>
        </template>
        <template v-else>
          <el-button @click="exportStub('PDF')"><i class="fas fa-file-export"></i> 导出明细</el-button>
          <el-button @click="exportStub('Excel')"><i class="fas fa-file-excel"></i> 下载 Excel</el-button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPayroll, approvePayroll, rejectPayroll } from '@/api/payroll'

const route = useRoute()
const router = useRouter()
const id = route.params.id

const loading = ref(false)
const order = ref(null)
const details = ref([])

const fen2yuan = (fen) => {
  if (fen == null || fen === '') return '0.00'
  const n = Number(fen)
  if (isNaN(n)) return '0.00'
  return (n / 100).toFixed(2)
}
const formatTime = (t) => { if (!t) return '—'; const s = String(t).replace('T', ' '); return s.length > 10 ? s.substring(0, 10) : s }

const statusText = (s) => ({ pending: '待审批', approved: '审批通过', rejected: '已驳回', withdrawn: '已撤回' }[s] || s || '—')
const statusClass = (s) => ({ pending: 'warning', approved: 'success', rejected: 'danger', withdrawn: 'default' }[s] || 'default')
const detailStatusText = (s) => ({ pending: '待转账', success: '转账成功', failed: '转账失败' }[s] || s || '—')
const detailStatusClass = (s) => ({ pending: 'warning', success: 'success', failed: 'danger' }[s] || 'default')

const avatarColors = ['orange', 'blue', 'green', 'purple']
const avatarColor = (seed) => {
  let h = 0
  for (let i = 0; i < (seed || '').length; i++) h = (h * 31 + seed.charCodeAt(i)) >>> 0
  return avatarColors[h % avatarColors.length]
}

const goBack = () => router.push('/admin/project-salary')

// 从对象中按优先级取第一个非空字段
const pick = (obj, keys, fallback = null) => {
  for (const k of keys) {
    if (obj && obj[k] != null && obj[k] !== '') return obj[k]
  }
  return fallback
}

// 时间字段兼容：与列表页 ProjectSalary.vue 保持一致的回退链
const pickSubmitTime = (o) => pick(o, ['submitTime', 'submit_time', 'commitTime', 'commit_time', 'date', 'createdAt', 'createTime', 'create_time', 'created_at', 'timestamp'])
const pickReviewTime = (o) => pick(o, ['reviewTime', 'review_time', 'approvedAt', 'approved_at'])
const pickReviewBy = (o) => pick(o, ['reviewBy', 'review_by', 'reviewer', 'approvedBy', 'approved_by'], '—')
const pickCreator = (o) => pick(o, ['creator', 'createdBy', 'created_by', 'creatorName'], '—')

const normalizeOrder = (raw) => {
  if (!raw) return null
  return {
    id: pick(raw, ['id']),
    orderNo: pick(raw, ['orderNo', 'order_no', 'no']),
    company: pick(raw, ['company'], '—'),
    title: pick(raw, ['title'], '—'),
    projectName: pick(raw, ['projectName', 'project_name'], '—'),
    type: pick(raw, ['type'], 'wage'),
    peopleCount: pick(raw, ['peopleCount', 'people_count', 'memberCount', 'workerCount'], 0),
    amount: pick(raw, ['amount'], 0),
    creator: pickCreator(raw),
    submitTime: formatTime(pickSubmitTime(raw)),
    reviewBy: pickReviewBy(raw),
    reviewTime: formatTime(pickReviewTime(raw)),
    status: pick(raw, ['status'], 'pending'),
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPayroll(id)
    const d = res.data || {}
    // 后端可能返回 { order, details } 或直接返回完整对象
    const rawOrder = d.order || d
    order.value = normalizeOrder(rawOrder)
    // 详情人员明细也兼容两种结构
    const rawDetails = d.details || rawOrder.details
    details.value = Array.isArray(rawDetails) ? rawDetails : []
  } catch (err) {
    console.warn('[PayrollDetail] 加载失败:', err.message)
    ElMessage.error('加载薪单详情失败')
  } finally {
    loading.value = false
  }
}

const doApprove = async () => {
  try { await approvePayroll(id); ElMessage.success('已通过'); loadData() }
  catch (err) { ElMessage.error('审批失败') }
}
const doReject = async () => {
  try { await rejectPayroll(id); ElMessage.success('已驳回'); loadData() }
  catch (err) { ElMessage.error('驳回失败') }
}

/** 把任意字符串包上引号（CSV 安全）。 */
const csvCell = (v) => {
  if (v == null || v === '') return '""'
  const s = String(v).replace(/"/g, '""')
  return `"${s}"`
}

/** 通用：二维数组 → CSV Blob 下载。 */
const downloadCsv = (rows, filename) => {
  const bom = '\uFEFF' // 防 Excel 中文乱码
  const content = bom + rows.map(row => row.map(csvCell).join(',')).join('\r\n')
  const blob = new Blob([content], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

/** 导出当前薪单明细为 CSV（薪单头部 + 人员明细）。 */
const exportDetails = () => {
  if (!order.value) { ElMessage.warning('薪单数据未加载'); return }
  const o = order.value
  const now = new Date().toISOString().slice(0, 19).replace('T', '_')

  const rows = [
    ['薪单标题', o.title],
    ['发薪单号', o.orderNo || ''],
    ['所属公司', o.company],
    ['所属项目', o.projectName],
    ['转账类型', ({ wage: '工资', advance: '预支', other: '其他' })[o.type] || o.type || ''],
    ['发薪人数', `${o.peopleCount ?? 0}`],
    ['应发总金额(元)', fen2yuan(o.amount)],
    ['制单人员', o.creator],
    ['提交时间', o.submitTime],
    ['状态', statusText(o.status)],
    ['审核人', o.reviewBy],
    ['审核时间', o.reviewTime],
    [], // 空行分隔
    ['姓名', '手机号', '岗位', '出勤天数', '日薪(元)', '应发金额(元)', '状态'],
  ]
  for (const d of (details.value || [])) {
    rows.push([
      d.name || '',
      d.phone || '',
      d.job || '',
      `${d.attendDays ?? 0}`,
      fen2yuan(d.dailyWage),
      fen2yuan(d.amount),
      detailStatusText(d.status),
    ])
  }

  downloadCsv(rows, `发薪明细_${o.orderNo || o.id || now}.csv`)
  ElMessage.success('导出成功')
}

/** 下载 Excel（CSV 格式，Excel 可直接打开）。 */
const exportStub = (type) => {
  if (type === 'Excel') {
    exportDetails()
    return
  }
  exportDetails()
}

onMounted(loadData)
</script>

<style scoped>
.back-bar { margin-bottom: 12px; }
.hero-card { padding: 20px; }
.hero-top { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 16px; }
.hero-title { font-size: 18px; font-weight: 700; color: #1F2937; margin-bottom: 8px; }
.hero-amount { text-align: right; }
.hero-amount-label { font-size: 12px; color: var(--text-muted); }
.hero-amount-value { font-size: 24px; font-weight: 700; color: var(--primary); }
.hero-desc { margin-top: 4px; }

.card-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.avatar-cell { display: flex; align-items: center; gap: 10px; }
.mini-avatar {
  width: 34px; height: 34px; border-radius: 50%;
  display: inline-flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 600; color: #fff; flex-shrink: 0;
}
.mini-avatar.orange { background: linear-gradient(135deg, #FF8C42 0%, #FF6B35 100%); }
.mini-avatar.blue { background: linear-gradient(135deg, #60A5FA 0%, #2563EB 100%); }
.mini-avatar.green { background: linear-gradient(135deg, #34D399 0%, #10B981 100%); }
.mini-avatar.purple { background: linear-gradient(135deg, #A78BFA 0%, #7C3AED 100%); }
.emp-name { font-weight: 500; font-size: 14px; }
.amount-cell { font-weight: 600; }

.footer-actions { display: flex; gap: 12px; justify-content: flex-end; margin-top: 16px; }
</style>
