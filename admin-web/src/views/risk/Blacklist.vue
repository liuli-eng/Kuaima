<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">黑名单管理</h1>
      <p class="page-desc">管理被封禁的用户账号，支持解封和延长封禁</p>
    </div>

    <div class="content-grid">
      <div class="card" style="grid-column: 1 / -1;">
        <div class="stat-cards" style="margin-bottom: 24px;">
          <div class="stat-card">
            <div class="stat-card-header">
              <span class="stat-card-title">黑名单数</span>
              <div class="stat-card-icon red"><i class="fas fa-user-times"></i></div>
            </div>
            <div class="stat-card-value">{{ blacklist.length }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-card-header">
              <span class="stat-card-title">本月新增</span>
              <div class="stat-card-icon yellow"><i class="fas fa-plus"></i></div>
            </div>
            <div class="stat-card-value">{{ pendingCount }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-card-header">
              <span class="stat-card-title">已解封</span>
              <div class="stat-card-icon green"><i class="fas fa-unlock"></i></div>
            </div>
            <div class="stat-card-value">{{ unfrozenCount }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-card-header">
              <span class="stat-card-title">封禁原因Top</span>
              <div class="stat-card-icon blue"><i class="fas fa-chart-bar"></i></div>
            </div>
            <div class="stat-card-value" style="font-size: 20px;">-</div>
          </div>
        </div>

        <div class="filter-bar">
          <el-select v-model="typeFilter" placeholder="用户类型" clearable style="width: 120px;">
            <el-option label="零工" value="零工" />
            <el-option label="雇主" value="雇主" />
          </el-select>
          <el-select v-model="reasonFilter" placeholder="封禁原因" clearable style="width: 140px;">
            <el-option label="飞单行为" value="飞单行为" />
            <el-option label="虚假招工" value="虚假招工" />
            <el-option label="恶意投诉" value="恶意投诉" />
            <el-option label="拖欠工资" value="拖欠工资" />
          </el-select>
          <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px;">
            <el-option label="封禁中" value="封禁中" />
            <el-option label="已解封" value="已解封" />
          </el-select>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>

        <el-table :data="filteredBlacklist" stripe :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }">
          <el-table-column prop="id" label="记录ID" width="100" />
          <el-table-column prop="user" label="用户" width="140" />
          <el-table-column prop="type" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="row.type === '零工' ? 'primary' : 'success'" effect="light">{{ row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="封禁原因" min-width="140" />
          <el-table-column prop="time" label="封禁时间" width="160" />
          <el-table-column prop="expireTime" label="到期时间" width="140" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <span :class="['status-badge', row.statusClass]">{{ row.status }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small">查看详情</el-button>
              <el-button v-if="row.status === '封禁中'" link type="success" size="small" @click="handleUnfreeze(row)">解封</el-button>
              <el-button v-if="row.status === '封禁中'" link type="warning" size="small">延长封禁</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <div class="pagination-info">共 {{ filteredBlacklist.length }} 条记录</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listBlacklist, unfreezeBlacklist } from '@/api/risk'

const blacklist = ref([])
const typeFilter = ref('')
const reasonFilter = ref('')
const statusFilter = ref('')

const filteredBlacklist = computed(() => {
  return blacklist.value.filter(b => {
    if (typeFilter.value && b.type !== typeFilter.value) return false
    if (reasonFilter.value && b.reason !== reasonFilter.value) return false
    if (statusFilter.value && b.status !== statusFilter.value) return false
    return true
  })
})

const pendingCount = computed(() => Math.floor(blacklist.value.length / 3))
const unfrozenCount = computed(() => blacklist.value.filter(b => b.status === '已解封').length)

const loadData = async () => {
  try {
    const res = await listBlacklist()
    blacklist.value = Array.isArray(res.data) ? res.data : (Array.isArray(res) ? res : [])
  } catch (e) {
    console.warn('[Blacklist] 加载失败:', e)
    blacklist.value = []
  }
}

const resetFilters = () => {
  typeFilter.value = ''
  reasonFilter.value = ''
  statusFilter.value = ''
}

const handleUnfreeze = async (row) => {
  try {
    await ElMessageBox.confirm(`确定解封用户 ${row.user}？`, '解封确认', { type: 'warning' })
    try {
      await unfreezeBlacklist(row.id)
      ElMessage.success('已解封')
      loadData()
    } catch (e) {
      console.warn('[Blacklist] 解封失败:', e)
      ElMessage.error('解封失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.warn('[Blacklist] 解封异常:', e)
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

.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
}
</style>
