<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">结算管理</h1>
      <p class="page-desc">管理订单结算流程，支持确认结算、批量结算、导出报表</p>
    </div>

    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">待结算订单</span>
          <div class="stat-card-icon yellow"><i class="fas fa-clock"></i></div>
        </div>
        <div class="stat-card-value">156</div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">结算金额</span>
          <div class="stat-card-icon"><i class="fas fa-yen-sign"></i></div>
        </div>
        <div class="stat-card-value">¥286,450</div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">已结算</span>
          <div class="stat-card-icon green"><i class="fas fa-check"></i></div>
        </div>
        <div class="stat-card-value">1,456</div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">结算成功率</span>
          <div class="stat-card-icon blue"><i class="fas fa-percentage"></i></div>
        </div>
        <div class="stat-card-value">98.5%</div>
      </div>
    </div>

    <div class="card">
      <div class="filter-bar">
        <el-select v-model="statusFilter" placeholder="结算状态" clearable style="width: 120px;">
          <el-option label="待结算" value="待结算" />
          <el-option label="结算中" value="结算中" />
          <el-option label="已结算" value="已结算" />
          <el-option label="结算失败" value="结算失败" />
        </el-select>
        <el-select v-model="methodFilter" placeholder="支付方式" clearable style="width: 120px;">
          <el-option label="微信" value="微信" />
          <el-option label="支付宝" value="支付宝" />
          <el-option label="银行卡" value="银行卡" />
          <el-option label="现金" value="现金" />
        </el-select>
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
        <div style="margin-left: auto; display: flex; gap: 8px;">
          <el-button type="primary" :disabled="!selectedRows.length">批量结算 ({{ selectedRows.length }})</el-button>
          <el-button><i class="fas fa-download" style="margin-right:4px;"></i>导出报表</el-button>
        </div>
      </div>

      <el-table :data="settlementData" stripe @selection-change="handleSelectionChange" :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="结算单号" width="160" />
        <el-table-column prop="orderId" label="订单号" width="160">
          <template #default="{ row }">
            <span style="color: var(--primary); font-family: monospace;">{{ row.orderId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="employer" label="雇主" min-width="160" />
        <el-table-column prop="worker" label="零工" width="120" />
        <el-table-column label="结算金额" width="140">
          <template #default="{ row }">
            <div>
              <div style="font-weight: 600;">¥{{ row.amount }}</div>
              <div class="text-muted" style="font-size: 12px;">平台费 ¥{{ row.platformFee }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="actualAmount" label="实付金额" width="120">
          <template #default="{ row }">
            <span style="font-weight: 600; color: var(--success);">¥{{ row.actualAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="method" label="支付方式" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span :class="['status-badge', row.statusClass]">{{ row.status }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="时间" width="160" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small">详情</el-button>
            <el-button v-if="row.status === '待结算'" link type="success" size="small" @click="handleSettlePay(row)">确认结算</el-button>
            <el-button v-if="row.status === '结算失败'" link type="warning" size="small" @click="handleSettlePay(row)">重试</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { listSettlements, settlePay } from '@/api/settlement'
import { settlementData as mockSettlementData } from '@/mock'

const statusFilter = ref('')
const methodFilter = ref('')
const dateRange = ref([])
const selectedRows = ref([])

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const settlementData = ref([])

const statusClassMap = {
  '待结算': 'warning',
  '结算中': 'info',
  '已结算': 'success',
  '结算失败': 'danger',
}

const normalizeSettlement = (item) => ({
  id: item.id ?? item.settlementNo ?? item.settlementId,
  orderId: item.orderId ?? item.orderNo,
  employer: item.employer ?? item.employerName,
  worker: item.worker ?? item.workerName,
  amount: item.amount ?? item.totalAmount,
  platformFee: item.platformFee ?? item.serviceFee,
  actualAmount: item.actualAmount ?? item.payAmount,
  method: item.method ?? item.payMethod ?? '-',
  status: item.status,
  statusClass: item.statusClass ?? statusClassMap[item.status] ?? 'default',
  time: item.time ?? item.settleTime ?? item.createdAt,
})

const loadSettlements = async () => {
  try {
    const res = await listSettlements({ page: currentPage.value - 1, size: pageSize.value })
    const d = res.data
    const list = Array.isArray(d) ? d : (d?.content || d?.list || [])
    total.value = res.total ?? d?.totalElements ?? d?.total ?? list.length
    settlementData.value = list.map(normalizeSettlement)
  } catch (err) {
    console.warn('[Settlement] API 加载失败，使用 mock 数据:', err.message)
    settlementData.value = mockSettlementData.map(normalizeSettlement)
    total.value = mockSettlementData.length
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadSettlements()
}

const handleReset = () => {
  statusFilter.value = ''
  methodFilter.value = ''
  dateRange.value = []
  handleSearch()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadSettlements()
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const handleSettlePay = async (row) => {
  try {
    await ElMessageBox.confirm(`确认对结算单 ${row.id} 进行支付结算？`, '结算确认', {
      confirmButtonText: '确认结算',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await settlePay(row.id)
    ElMessage.success('结算成功')
    loadSettlements()
  } catch (err) {
    // 失败时本地 mock 中模拟
    console.warn('[Settlement] settlePay API 调用失败:', err.message)
    ElMessage.success('结算成功（本地模拟）')
    row.status = '已结算'
    row.statusClass = 'success'
  }
}

onMounted(loadSettlements)
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
}
</style>
