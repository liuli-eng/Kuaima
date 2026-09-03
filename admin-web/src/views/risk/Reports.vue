<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">举报处理</h1>
      <p class="page-desc">处理平台用户举报，维护平台公平公正</p>
    </div>

    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">待处理</span>
          <div class="stat-card-icon yellow"><i class="fas fa-clock"></i></div>
        </div>
        <div class="stat-card-value">{{ pendingCount }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">本月举报</span>
          <div class="stat-card-icon"><i class="fas fa-flag"></i></div>
        </div>
        <div class="stat-card-value">{{ totalCount }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">已处理</span>
          <div class="stat-card-icon green"><i class="fas fa-check"></i></div>
        </div>
        <div class="stat-card-value">{{ handledCount }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">处理率</span>
          <div class="stat-card-icon blue"><i class="fas fa-percentage"></i></div>
        </div>
        <div class="stat-card-value">{{ handleRate }}</div>
      </div>
    </div>

    <div class="card">
      <div class="filter-bar">
        <el-select v-model="typeFilter" placeholder="举报类型" clearable style="width: 120px;">
          <el-option label="违规" value="违规" />
          <el-option label="纠纷" value="纠纷" />
          <el-option label="虚假信息" value="虚假信息" />
        </el-select>
        <el-select v-model="statusFilter" placeholder="处理状态" clearable style="width: 120px;">
          <el-option label="待处理" value="待处理" />
          <el-option label="处理中" value="处理中" />
          <el-option label="已处理" value="已处理" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <el-table :data="filteredReports" stripe :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }">
        <el-table-column prop="id" label="举报ID" width="100" />
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.typeClass === 'danger' ? 'danger' : row.typeClass === 'info' ? 'primary' : 'warning'" effect="light">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reporter" label="举报人" width="120" />
        <el-table-column prop="target" label="被举报人" min-width="180" />
        <el-table-column prop="orderId" label="关联订单" width="140">
          <template #default="{ row }">
            <span v-if="row.orderId !== '-'" style="color: var(--primary); font-family: monospace;">{{ row.orderId }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="时间" width="160" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span :class="['status-badge', row.statusClass]">{{ row.status }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small">详情</el-button>
            <el-button v-if="row.status !== '已处理'" link type="success" size="small" @click="handleProcess(row)">处理</el-button>
            <el-button v-if="row.status !== '已处理'" link type="danger" size="small" @click="handleBan(row)">封禁</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listReports, handleReport } from '@/api/risk'
import { reportsData as fallbackReports } from '@/mock'

const reports = ref([...fallbackReports])
const typeFilter = ref('')
const statusFilter = ref('')
const apiAvailable = ref(false)

const filteredReports = computed(() => {
  return reports.value.filter(r => {
    if (typeFilter.value && r.type !== typeFilter.value) return false
    if (statusFilter.value && r.status !== statusFilter.value) return false
    return true
  })
})

const pendingCount = computed(() => reports.value.filter(r => r.status === '待处理').length)
const handledCount = computed(() => reports.value.filter(r => r.status === '已处理').length)
const totalCount = computed(() => reports.value.length || 0)
const handleRate = computed(() => {
  const total = totalCount.value
  const handled = handledCount.value
  if (!total) return '0%'
  return ((handled / total) * 100).toFixed(1) + '%'
})

const loadData = async () => {
  try {
    const res = await listReports()
    if (res && Array.isArray(res)) {
      reports.value = res
    } else if (res && Array.isArray(res.data)) {
      reports.value = res.data
    }
    apiAvailable.value = true
  } catch (e) {
    console.warn('[API] listReports 后端暂未接入，使用 mock 数据')
    reports.value = [...fallbackReports]
  }
}

const resetFilters = () => {
  typeFilter.value = ''
  statusFilter.value = ''
}

const handleProcess = async (row) => {
  try {
    const { value: result } = await ElMessageBox.prompt('请输入处理结果', '处理举报', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '如：已对举报对象进行警告处理'
    }).catch(() => ({ value: '已处理' }))
    if (apiAvailable.value) {
      await handleReport(row.id, result)
    } else {
      console.warn('[API] handleReport 后端暂未接入')
    }
    row.status = '已处理'
    row.statusClass = 'success'
    ElMessage.success('处理完成')
  } catch (e) {
    if (e !== 'cancel') {
      console.warn('[API] handleReport 请求失败')
      row.status = '已处理'
      row.statusClass = 'success'
    }
  }
}

const handleBan = async (row) => {
  try {
    await ElMessageBox.confirm(`确定将 ${row.target} 加入黑名单？`, '封禁确认', { type: 'warning' })
    if (apiAvailable.value) {
      await handleReport(row.id, '封禁处理')
    } else {
      console.warn('[API] handleReport 后端暂未接入')
    }
    row.status = '已处理'
    row.statusClass = 'success'
    ElMessage.success('已封禁')
  } catch (e) {
    if (e !== 'cancel') {
      console.warn('[API] handleReport 请求失败')
    }
  }
}

onMounted(loadData)
</script>

<style scoped>
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
</style>
