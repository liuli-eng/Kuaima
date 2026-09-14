<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">发薪管理</h1>
      <p class="page-desc">企业发薪单的创建、提交与审批管理，支持工资 / 预支 / 其他转账类型</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card" v-for="card in statCards" :key="card.title">
        <div class="stat-card-header">
          <span class="stat-card-title">{{ card.title }}</span>
          <div class="stat-card-icon" :class="card.iconCls"><i :class="card.icon"></i></div>
        </div>
        <div class="stat-card-value">{{ card.value }}</div>
        <div class="stat-card-change" :class="card.up ? 'up' : 'down'">
          <i :class="card.up ? 'fas fa-arrow-up' : 'fas fa-clock'"></i>
          <span>{{ card.sub }}</span>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <div class="card-title title-switch-group">
          <span class="title-switch" :class="{ active: tab === 'submitted' }" @click="switchTab('submitted')">已提交的发薪单</span>
          <span class="title-switch-divider"></span>
          <span class="title-switch" :class="{ active: tab === 'reviewed' }" @click="switchTab('reviewed')">已审核的发薪单</span>
        </div>
        <div class="card-header-actions">
          <button class="btn btn-primary btn-sm" @click="openCreate"><i class="fas fa-plus"></i> 新建发薪单</button>
        </div>
      </div>

      <!-- 筛选 -->
      <div class="filter-bar">
        <el-input v-model="filters.keyword" placeholder="公司 / 项目 / 标题" clearable style="width: 220px;" prefix-icon="Search" />
        <el-input v-model="filters.company" placeholder="所属公司" clearable style="width: 180px;" />
        <el-input v-model="filters.projectName" placeholder="所属项目" clearable style="width: 160px;" />
        <el-select v-model="filters.status" placeholder="状态" clearable style="width: 130px;">
          <el-option label="全部状态" value="" />
          <el-option label="待审批" value="pending" />
          <el-option label="审批通过" value="approved" />
          <el-option label="已驳回" value="rejected" />
          <el-option label="已撤回" value="withdrawn" />
        </el-select>
        <el-date-picker v-model="filters.dateRange" type="daterange" range-separator="至"
          start-placeholder="提交开始" end-placeholder="提交结束" style="width: 240px;" value-format="YYYY-MM-DD" />
        <button class="btn btn-primary btn-sm" @click="handleSearch"><i class="fas fa-search"></i> 查询</button>
        <button class="btn btn-outline btn-sm" @click="handleReset"><i class="fas fa-rotate-left"></i> 重置</button>
      </div>

      <el-table :data="payrollList" stripe v-loading="loading"
        :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }">
        <el-table-column prop="company" label="所属公司" show-overflow-tooltip />
        <el-table-column prop="title" label="薪单标题" show-overflow-tooltip min-width="140">
          <template #default="{ row }">
            <a class="link-title" @click="goDetail(row)">{{ row.title }}</a>
          </template>
        </el-table-column>
        <el-table-column prop="projectName" label="所属项目" show-overflow-tooltip />
        <el-table-column label="应发金额" show-overflow-tooltip>
          <template #default="{ row }"><span class="amount-cell">¥{{ fen2yuan(row.amount) }}</span></template>
        </el-table-column>
        <el-table-column prop="peopleCount" label="应发人数" width="90" />
        <el-table-column prop="creator" label="制单人员" show-overflow-tooltip />
        <el-table-column prop="submitTime" label="提交时间" show-overflow-tooltip />
        <el-table-column prop="reviewBy" label="审核人员" show-overflow-tooltip />
        <el-table-column prop="reviewTime" label="审核时间" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span :class="['status-badge', statusClass(row.status)]">{{ statusText(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="goDetail(row)">查看</el-button>
            <template v-if="row.status === 'pending'">
              <el-button link type="success" size="small" @click="doApprove(row)">通过</el-button>
              <el-button link type="danger" size="small" @click="doReject(row)">驳回</el-button>
            </template>
            <template v-else>
              <el-button link type="info" size="small" @click="exportStub(row)">导出</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <div class="pagination-info">共 {{ total }} 条记录</div>
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]" :total="total"
          layout="sizes, prev, pager, next, jumper" background
          @size-change="loadData" @current-change="loadData" />
      </div>
    </div>

    <!-- 新建发薪单弹窗 -->
    <el-dialog v-model="createVisible" title="新建发薪单" width="640px">
      <el-form :model="createForm" label-width="90px">
        <el-form-item label="薪单标题" required>
          <el-input v-model="createForm.title" placeholder="请输入薪单标题，如：菜鸟·云联日结 9月第2周" />
        </el-form-item>
        <el-form-item label="所属公司">
          <el-input v-model="createForm.company" placeholder="请输入所属公司名称" />
        </el-form-item>
        <el-form-item label="关联项目">
          <el-input v-model="createForm.projectName" placeholder="请输入所属项目" />
        </el-form-item>
        <el-form-item label="转账类型">
          <el-radio-group v-model="createForm.type">
            <el-radio value="wage">工资</el-radio>
            <el-radio value="advance">预支</el-radio>
            <el-radio value="other">其他</el-radio>
          </el-radio-group>
        </el-form-item>
        <div class="form-section-title">发薪成员</div>
        <div v-for="(m, idx) in createMembers" :key="idx" class="member-row">
          <el-input v-model="m.name" placeholder="姓名" style="width:100px" />
          <el-input v-model="m.phone" placeholder="手机号" style="width:120px" />
          <el-input v-model="m.job" placeholder="岗位" style="width:100px" />
          <el-input v-model="m.dailyWage" placeholder="日薪(元)" style="width:90px" />
          <el-input v-model="m.attendDays" placeholder="出勤天" style="width:80px" />
          <el-button v-if="createMembers.length > 1" text type="danger" @click="removeMemberRow(idx)"><i class="fas fa-trash"></i></el-button>
        </div>
        <el-button class="btn btn-outline btn-sm" @click="addMemberRow"><i class="fas fa-plus"></i> 添加成员</el-button>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listPayrolls, payrollStats, createPayroll,
  approvePayroll, rejectPayroll, withdrawPayroll
} from '@/api/payroll'

const router = useRouter()

const tab = ref('submitted')
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const payrollList = ref([])

const stats = reactive({})
const statCards = computed(() => [
  { title: '项目总数', icon: 'fas fa-folder-open', iconCls: '', value: pick(stats, ['projectCount', 'projectTotal', 'totalProject'], '—'), sub: '企业名下项目', up: true },
  { title: '本月发薪总额', icon: 'fas fa-yen-sign', iconCls: 'green', value: '¥' + fen2yuan(pick(stats, ['monthAmount', 'monthTotalAmount', 'currentMonthAmount'], 0)), sub: '本月累计', up: true },
  { title: '本月发薪笔数', icon: 'fas fa-file-invoice-dollar', iconCls: 'blue', value: pick(stats, ['monthCount', 'monthTotalCount'], '—'), sub: '本月累计', up: true },
  { title: '待审批', icon: 'fas fa-file-signature', iconCls: 'yellow', value: pick(stats, ['pendingApprove', 'pendingCount', 'needApprove'], '—'), sub: '需尽快处理', up: false },
])

const filters = reactive({ keyword: '', company: '', projectName: '', status: '', dateRange: [] })

const createVisible = ref(false)
const createForm = reactive({ title: '', company: '', projectName: '', type: 'wage' })
const createMembers = ref([{ name: '', phone: '', job: '', dailyWage: '', attendDays: '' }])
const addMemberRow = () => createMembers.value.push({ name: '', phone: '', job: '', dailyWage: '', attendDays: '' })
const removeMemberRow = (i) => { if (createMembers.value.length > 1) createMembers.value.splice(i, 1) }

const fen2yuan = (fen) => {
  if (fen == null || fen === '') return '0.00'
  const n = Number(fen)
  if (isNaN(n)) return '0.00'
  return (n / 100).toFixed(2)
}

const pick = (obj, keys, fallback) => {
  for (const k of keys) {
    if (obj && obj[k] != null && obj[k] !== '') return obj[k]
  }
  return fallback
}

const statusText = (s) => ({ pending: '待审批', approved: '审批通过', rejected: '已驳回', withdrawn: '已撤回' }[s] || s || '—')
const statusClass = (s) => ({ pending: 'warning', approved: 'success', rejected: 'danger', withdrawn: 'default' }[s] || 'default')

const formatTime = (t) => {
  if (!t) return '—'
  const s = String(t).replace('T', ' ')
  return s.length > 10 ? s.substring(0, 10) : s
}

const normalize = (o) => ({
  id: o.id,
  orderNo: o.orderNo,
  company: o.company || '—',
  title: o.title || '—',
  projectName: o.projectName || '—',
  amount: o.amount ?? 0,
  peopleCount: o.peopleCount ?? 0,
  creator: o.creator || '—',
  submitTime: formatTime(o.submitTime),
  reviewBy: o.reviewBy || '—',
  reviewTime: formatTime(o.reviewTime),
  status: o.status || 'pending',
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await listPayrolls({
      tab: tab.value,
      status: filters.status || undefined,
      keyword: filters.keyword || undefined,
      page: currentPage.value - 1,
      size: pageSize.value,
    })
    let list = res.data?.content || res.data?.list || (Array.isArray(res.data) ? res.data : [])
    // 客户端补充过滤：公司 / 项目 / 时间区间（后端仅支持 keyword/status/projectId）
    if (filters.company) list = list.filter((x) => (x.company || '').includes(filters.company))
    if (filters.projectName) list = list.filter((x) => (x.projectName || '').includes(filters.projectName))
    if (filters.dateRange && filters.dateRange.length === 2) {
      const [a, b] = filters.dateRange
      list = list.filter((x) => {
        const t = formatTime(x.submitTime)
        return t >= a && t <= b
      })
    }
    const hasLocal = !!(filters.company || filters.projectName || (filters.dateRange && filters.dateRange.length === 2))
    payrollList.value = list.map(normalize)
    total.value = hasLocal ? list.length : (res.total ?? list.length)
  } catch (err) {
    console.warn('[ProjectSalary] 加载失败:', err.message)
    payrollList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const res = await payrollStats()
    Object.assign(stats, res.data || {})
  } catch (err) {
    console.warn('[ProjectSalary] 统计加载失败:', err.message)
  }
}

const switchTab = (t) => {
  tab.value = t
  currentPage.value = 1
  loadData()
}

const handleSearch = () => { currentPage.value = 1; loadData() }
const handleReset = () => {
  filters.keyword = ''; filters.company = ''; filters.projectName = ''; filters.status = ''; filters.dateRange = []
  handleSearch()
}

const goDetail = (row) => router.push(`/admin/payroll-detail/${row.id}`)

const openCreate = () => {
  createForm.title = ''; createForm.company = ''; createForm.projectName = ''; createForm.type = 'wage'
  createMembers.value = [{ name: '', phone: '', job: '', dailyWage: '', attendDays: '' }]
  createVisible.value = true
}
const submitCreate = async () => {
  if (!createForm.title.trim()) { ElMessage.warning('请输入薪单标题'); return }
  try {
    const details = createMembers.value
      .filter((m) => m.name && m.name.trim())
      .map((m) => ({
        name: m.name.trim(),
        phone: m.phone || null,
        job: m.job || null,
        dailyWage: Math.round((Number(m.dailyWage) || 0) * 100),
        attendDays: Number(m.attendDays) || 0,
      }))
    await createPayroll({
      order: {
        title: createForm.title.trim(),
        company: createForm.company || null,
        projectName: createForm.projectName || null,
        type: createForm.type,
      },
      details,
    })
    ElMessage.success('发薪单已创建')
    createVisible.value = false
    loadData(); loadStats()
  } catch (err) {
    console.warn('[ProjectSalary] 创建失败:', err.message)
    ElMessage.error('创建失败，请确认后端服务已启动')
  }
}

const doApprove = async (row) => {
  try { await approvePayroll(row.id); ElMessage.success('已通过'); loadData() }
  catch (err) { ElMessage.error('审批失败') }
}
const doReject = async (row) => {
  try { await rejectPayroll(row.id); ElMessage.success('已驳回'); loadData() }
  catch (err) { ElMessage.error('驳回失败') }
}
const exportStub = () => ElMessage.info('导出功能由后端文件服务支持，演示环境暂不可用')

onMounted(() => { loadData(); loadStats() })
</script>

<style scoped>
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}
.stat-card {
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 18px;
}
.stat-card-header { display: flex; align-items: center; justify-content: space-between; }
.stat-card-title { font-size: 13px; color: var(--text-secondary); }
.stat-card-icon {
  width: 40px; height: 40px; border-radius: 10px;
  background: linear-gradient(135deg, #FFF0EB 0%, #FFE8DC 100%); color: var(--primary);
  display: flex; align-items: center; justify-content: center; font-size: 16px;
}
.stat-card-icon.blue { background: linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 100%); color: var(--secondary); }
.stat-card-icon.green { background: linear-gradient(135deg, #ECFDF5 0%, #D1FAE5 100%); color: var(--success); }
.stat-card-icon.yellow { background: linear-gradient(135deg, #FFFBEB 0%, #FEF3C7 100%); color: var(--warning); }
.stat-card-value { font-size: 26px; font-weight: 700; margin: 10px 0 4px; color: #1F2937; }
.stat-card-change { font-size: 12px; }
.stat-card-change.up { color: var(--success); }
.stat-card-change.down { color: var(--warning); }

.card-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.title-switch-group { display: flex; align-items: center; gap: 4px; }
.title-switch { font-size: 15px; color: var(--text-secondary); cursor: pointer; padding: 4px 2px; }
.title-switch.active { color: var(--primary); font-weight: 600; }
.title-switch-divider { width: 1px; height: 16px; background: var(--border); margin: 0 10px; }

.filter-bar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.pagination { display: flex; align-items: center; justify-content: space-between; margin-top: 16px; }
.pagination-info { font-size: 13px; color: var(--text-secondary); }
.link-title { color: var(--primary); cursor: pointer; }
.link-title:hover { text-decoration: underline; }
.amount-cell { font-weight: 600; }
.form-section-title { font-size: 13px; font-weight: 600; color: var(--text-secondary); margin: 4px 0 10px; }
.member-row { display: flex; gap: 8px; margin-bottom: 8px; align-items: center; }
</style>
