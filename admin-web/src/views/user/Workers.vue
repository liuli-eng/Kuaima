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
        <div class="stat-card-value">28,654</div>
        <div class="stat-card-change up"><i class="fas fa-arrow-up"></i><span>本月新增 +186</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">本月新增</span>
          <div class="stat-card-icon blue"><i class="fas fa-user-plus"></i></div>
        </div>
        <div class="stat-card-value">186</div>
        <div class="stat-card-change up"><i class="fas fa-arrow-up"></i><span>较上月 +12%</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">在线人数</span>
          <div class="stat-card-icon green"><i class="fas fa-wifi"></i></div>
        </div>
        <div class="stat-card-value">2,345</div>
        <div class="stat-card-change up"><i class="fas fa-arrow-up"></i><span>实时数据</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">已冻结</span>
          <div class="stat-card-icon yellow"><i class="fas fa-lock"></i></div>
        </div>
        <div class="stat-card-value">45</div>
        <div class="stat-card-change down"><i class="fas fa-arrow-down"></i><span>较上月 -5</span></div>
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
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
        <el-button type="primary" @click="handleSearch"><i class="fas fa-search" style="margin-right:4px;"></i>查询</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button style="margin-left: auto;"><i class="fas fa-download" style="margin-right:4px;"></i>导出</el-button>
      </div>

      <el-table :data="tableData" stripe :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="零工ID" width="100" />
        <el-table-column label="用户" min-width="160">
          <template #default="{ row }">
            <div class="user-cell">
              <span class="mini-avatar" :style="{ background: row.avatarColor || getAvatarColor(row.name) }">{{ getAvatarLetter(row.name) }}</span>
              <div>
                <div style="font-weight: 500;">{{ row.name }}</div>
                <div class="text-muted" style="font-size: 12px;">{{ row.phone }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="certStatus" label="实名状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.certStatus === '已认证' || row.certStatus === 'VERIFIED' || row.certStatus === true ? 'success' : 'info'" effect="light">{{ formatRealName(row.certStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="技能标签" min-width="140">
          <template #default="{ row }">
            <el-tag v-for="(skill, idx) in normalizeSkills(row.skills)" :key="idx" style="margin-right: 4px; margin-bottom: 2px;" type="warning" effect="light" size="small">{{ skill }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="信用分" width="180">
          <template #default="{ row }">
            <div class="credit-cell">
              <el-progress :percentage="row.creditScore || row.creditProgress || 0" :color="getCreditColor(row.creditScore || row.creditProgress || 0)" :stroke-width="8" style="flex:1;" />
              <span class="credit-score">{{ row.creditScore || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="orders" label="完成订单" width="100">
          <template #default="{ row }">
            <span>{{ row.orders ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="registerTime" label="注册时间" width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <span :class="['status-badge', (row.status === '正常' || row.status === 'NORMAL' || row.status === 1) ? 'success' : 'danger']">{{ formatStatus(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small">详情</el-button>
            <el-button link type="warning" size="small" v-if="isNormal(row.status)" @click="handleFreeze(row)">冻结</el-button>
            <el-button link type="success" size="small" v-else @click="handleUnfreeze(row)">解冻</el-button>
            <el-button link type="danger" size="small">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <div class="pagination-info">共 {{ total }} 条记录</div>
        <el-pagination background layout="total, prev, pager, next, jumper" :total="total" :page-size="pageSize" :current-page="currentPage" @current-change="handlePageChange" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listWorkers, freezeUser, unfreezeUser } from '@/api/user'

const searchKeyword = ref('')
const statusFilter = ref('')
const dateRange = ref([])

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

// 后端 Worker 真实字段：id, username, nickname, phone, role, status, age, gender, certStatus, creditScore, skills, timestamp
const normalizeWorker = (item) => {
  return {
    ...item,
    name: item.nickname || item.username || item.phone,
    registerTime: item.timestamp,
    // status 后端返回 "正常"/"冻结" 或枚举值，formatStatus 已做兼容
    // orders 后端暂无此字段，显示占位
  }
}

// 加载数据
const loadData = async () => {
  try {
    const result = await listWorkers({
      keyword: searchKeyword.value || undefined,
      status: statusFilter.value || undefined,
      page: currentPage.value - 1, // API 0-based
      size: pageSize.value
    })
    // 后端统一 Result：{ code, data, total, page }
    // data 可能是 Spring Data Page { content } 或 { list } 或数组
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
  if (score >= 80) return '#10B981'
  if (score >= 60) return '#F59E0B'
  return '#EF4444'
}

// 兼容后端不同字段/类型
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
const formatRealName = (v) => {
  if (v === true || v === 1 || v === '已认证') return '已认证'
  if (v === false || v === 0 || v === '未认证') return '未认证'
  return v || '-'
}
const formatStatus = (s) => {
  if (s === 'NORMAL' || s === 1 || s === '正常') return '正常'
  if (s === 'FROZEN' || s === 0 || s === '冻结') return '冻结'
  return s || '-'
}
const isNormal = (s) => s === '正常' || s === 'NORMAL' || s === 1

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
const handlePageChange = (page) => {
  currentPage.value = page
  loadData()
}

// 冻结 / 解冻
const handleFreeze = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要冻结用户 ${row.name || row.id} 吗？`, '提示', { type: 'warning' })
    await freezeUser(row.id)
    ElMessage.success('冻结成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.warn('[Workers] 冻结失败:', e)
  }
}
const handleUnfreeze = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要解冻用户 ${row.name || row.id} 吗？`, '提示', { type: 'warning' })
    await unfreezeUser(row.id)
    ElMessage.success('解冻成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.warn('[Workers] 解冻失败:', e)
  }
}

onMounted(() => {
  loadData()
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

.credit-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  
  .credit-score {
    font-weight: 600;
    min-width: 36px;
    text-align: right;
  }
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
