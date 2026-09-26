<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">用工订单</h1>
      <p class="page-desc">查看和管理平台所有日结订单，追踪订单状态和处理纠纷</p>
    </div>

    <div class="card">
      <div class="filter-bar">
        <el-input v-model="searchKeyword" placeholder="订单号/雇主/零工" clearable style="width: 240px;" prefix-icon="Search" />
        <el-select v-model="statusFilter" placeholder="订单状态" clearable style="width: 120px;">
          <el-option label="全部" value="" />
          <el-option label="待确认" value="待确认" />
          <el-option label="进行中" value="进行中" />
          <el-option label="已完成" value="已完成" />
          <el-option label="已取消" value="已取消" />
          <el-option label="纠纷" value="纠纷" />
        </el-select>
        <el-select v-model="typeFilter" placeholder="工种类型" clearable style="width: 140px;">
          <el-option label="电子厂" value="电子厂" />
          <el-option label="物流" value="物流" />
          <el-option label="餐饮" value="餐饮" />
        </el-select>
        <div class="date-picker-wrap" style="width: 240px;">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 100%;" />
        </div>
        <button class="btn btn-primary btn-sm" @click="handleSearch"><i class="fas fa-search"></i> 查询</button>
        <button class="btn btn-outline btn-sm" @click="handleReset"><i class="fas fa-rotate-left"></i> 重置</button>
        <button class="btn btn-outline btn-sm" style="margin-left: auto;" @click="handleExport"><i class="fas fa-download"></i> 导出</button>
      </div>

      <el-table :data="ordersData" stripe :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }">
        <el-table-column prop="id" label="订单号" show-overflow-tooltip>
          <template #default="{ row }">
            <span style="color: var(--primary); font-family: monospace;">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="employer" label="雇主" show-overflow-tooltip />
        <el-table-column prop="worker" label="零工" show-overflow-tooltip />
        <el-table-column prop="job" label="工种" show-overflow-tooltip />
        <el-table-column label="金额" show-overflow-tooltip>
          <template #default="{ row }">
            <span style="font-weight: 600;">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" show-overflow-tooltip />
        <el-table-column prop="endTime" label="结束时间" show-overflow-tooltip />
        <el-table-column label="状态" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="['status-badge', row.statusClass]">{{ row.status }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showDetail(row)">详情</el-button>
            <el-button link type="warning" size="small" v-if="row.status === '纠纷'" @click="handleDispute(row)">处理纠纷</el-button>
            <el-button link type="danger" size="small" :disabled="isCanceled(row)" @click="handleCancel(row)">取消</el-button>
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
    <el-dialog v-model="detailVisible" title="订单详情" width="620px">
      <el-descriptions v-if="selectedOrder" :column="2" border>
        <el-descriptions-item label="订单号">{{ selectedOrder.id }}</el-descriptions-item><el-descriptions-item label="状态">{{ selectedOrder.status }}</el-descriptions-item>
        <el-descriptions-item label="雇主">{{ selectedOrder.employer }}</el-descriptions-item><el-descriptions-item label="零工">{{ selectedOrder.worker }}</el-descriptions-item>
        <el-descriptions-item label="工种">{{ selectedOrder.job }}</el-descriptions-item><el-descriptions-item label="金额">¥{{ selectedOrder.amount }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ selectedOrder.startTime }}</el-descriptions-item><el-descriptions-item label="结束时间">{{ selectedOrder.endTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="detailVisible = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelOrder, listOrders } from '@/api/order'

const searchKeyword = ref('')
const statusFilter = ref('')
const typeFilter = ref('')
const dateRange = ref([])

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const ordersData = ref([])
const detailVisible = ref(false)
const selectedOrder = ref(null)

const statusClassMap = {
  '已完成': 'success',
  '进行中': 'info',
  '待确认': 'warning',
  '纠纷': 'danger',
  '已取消': 'default',
  '待处理': 'warning',
  '待结算': 'warning',
  '结算中': 'info',
  '已结算': 'success',
  '结算失败': 'danger',
}

const formatTime = (t) => {
  if (!t) return '-'
  if (typeof t === 'number') {
    const d = new Date(t)
    if (isNaN(d.getTime())) return '-'
    const Y = d.getFullYear()
    const M = String(d.getMonth() + 1).padStart(2, '0')
    const D = String(d.getDate()).padStart(2, '0')
    return `${Y}-${M}-${D}`
  }
  const s = String(t).replace('T', ' ')
  return s.length > 10 ? s.substring(0, 10) : s
}

const normalizeOrder = (item) => ({
  id: item.id,
  orderId: item.orderId,
  employer: item.employerName || '-',
  worker: item.workerName || '-',
  job: item.jobTitle || item.postion || item.jobType || '-',
  amount: item.amount ?? item.salary ?? '-',
  status: item.status || '-',
  statusClass: statusClassMap[item.status] ?? 'default',
  startTime: formatTime(item.startTime),
  endTime: formatTime(item.endTime),
})

const loadOrders = async () => {
  try {
    const res = await listOrders({
      status: statusFilter.value || undefined,
      keyword: searchKeyword.value || undefined,
      type: typeFilter.value || undefined,
      page: currentPage.value - 1,
      size: pageSize.value,
    })
    const d = res.data
    const list = Array.isArray(d) ? d : (d?.content || d?.list || [])
    total.value = res.total ?? d?.totalElements ?? d?.total ?? list.length
    ordersData.value = list.map(normalizeOrder)
  } catch (err) {
    console.warn('[Orders] API 加载失败:', err.message)
    ordersData.value = []
    total.value = 0
    ElMessage.error('加载订单列表失败')
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadOrders()
}

const handleReset = () => {
  searchKeyword.value = ''
  statusFilter.value = ''
  typeFilter.value = ''
  dateRange.value = []
  handleSearch()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadOrders()
}

const onSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadOrders()
}

const onPageChange = (page) => {
  currentPage.value = page
  loadOrders()
}

const showDetail = (row) => { selectedOrder.value = row; detailVisible.value = true }
const isCanceled = (row) => ['已取消', '取消招工', '取消报名'].includes(row.status)

const handleCancel = async (row) => {
  if (isCanceled(row)) return
  try {
    const result = await ElMessageBox.prompt('请输入取消原因（可选）', '取消订单', { inputPlaceholder: '例如：后台管理员取消', confirmButtonText: '确认取消', cancelButtonText: '返回' })
    await cancelOrder(row.id, result.value)
    ElMessage.success('订单已取消')
    await loadOrders()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') ElMessage.error(e?.message || '取消订单失败')
  }
}

const handleDispute = (row) => {
  selectedOrder.value = row
  ElMessageBox.alert('请先核实雇主与零工双方凭证，再通过订单详情处理后续结算。', `处理纠纷 · ${row.id}`, { confirmButtonText: '知道了', type: 'warning' })
}

const handleExport = () => {
  if (!ordersData.value.length) return ElMessage.info('当前没有可导出的订单')
  const headers = ['订单号', '雇主', '零工', '工种', '金额', '开始时间', '结束时间', '状态']
  const lines = ordersData.value.map(row => [row.id, row.employer, row.worker, row.job, row.amount, row.startTime, row.endTime, row.status].map(value => `"${String(value ?? '').replaceAll('"', '""')}"`).join(','))
  const blob = new Blob([`\ufeff${headers.join(',')}\n${lines.join('\n')}`], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob); const link = document.createElement('a'); link.href = url; link.download = '用工订单.csv'; link.click(); URL.revokeObjectURL(url)
}

onMounted(loadOrders)
</script>

<style scoped>
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
  
  .pagination-info {
    font-size: 13px;
    color: var(--text-secondary);
  }
}
</style>
