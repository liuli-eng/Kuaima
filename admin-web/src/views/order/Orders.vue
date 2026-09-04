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
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
        <el-button type="primary" @click="handleSearch"><i class="fas fa-search" style="margin-right:4px;"></i>查询</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button style="margin-left: auto;"><i class="fas fa-download" style="margin-right:4px;"></i>导出</el-button>
      </div>

      <el-table :data="ordersData" stripe :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }">
        <el-table-column prop="id" label="订单号" width="160">
          <template #default="{ row }">
            <span style="color: var(--primary); font-family: monospace;">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="employer" label="雇主" min-width="180" />
        <el-table-column prop="worker" label="零工" width="120" />
        <el-table-column prop="job" label="工种" min-width="140" />
        <el-table-column label="金额" width="100">
          <template #default="{ row }">
            <span style="font-weight: 600;">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="endTime" label="结束时间" width="160" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span :class="['status-badge', row.statusClass]">{{ row.status }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small">详情</el-button>
            <el-button link type="warning" size="small" v-if="row.status === '纠纷'">处理纠纷</el-button>
            <el-button link type="danger" size="small">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <div class="pagination-info">共 {{ total }} 条记录</div>
        <el-pagination background layout="total, sizes, prev, pager, next, jumper" :total="total" :page-size="pageSize" :current-page="currentPage" @current-change="handlePageChange" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listOrders } from '@/api/order'

const searchKeyword = ref('')
const statusFilter = ref('')
const typeFilter = ref('')
const dateRange = ref([])

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const ordersData = ref([])

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

const normalizeOrder = (item) => ({
  id: item.id ?? item.orderNo,
  employer: item.employer ?? item.employerName,
  worker: item.worker ?? item.workerName,
  job: item.job ?? item.jobTitle ?? item.jobName,
  amount: item.amount ?? item.totalAmount,
  status: item.status,
  statusClass: item.statusClass ?? statusClassMap[item.status] ?? 'default',
  startTime: item.startTime ?? item.workStartTime,
  endTime: item.endTime ?? item.workEndTime,
})

const loadOrders = async () => {
  try {
    const res = await listOrders({ status: statusFilter.value || undefined })
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
