<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">老板管理</h1>
      <p class="page-desc">管理平台所有雇主用户，查看企业资质和招工情况</p>
    </div>

    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">总雇主</span>
          <div class="stat-card-icon blue"><i class="fas fa-building"></i></div>
        </div>
        <div class="stat-card-value">{{ stats.total }}</div>
        <div class="stat-card-change"><span class="text-muted">全部雇主</span></div>
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
          <div class="stat-card-icon"><i class="fas fa-certificate"></i></div>
        </div>
        <div class="stat-card-value">{{ stats.certified }}</div>
        <div class="stat-card-change"><span class="text-muted">企业认证</span></div>
      </div>
    </div>

    <div class="card">
      <div class="filter-bar">
        <el-input v-model="searchKeyword" placeholder="搜索名称/企业/联系人" clearable style="width: 240px;" prefix-icon="Search" />
        <el-select v-model="certFilter" placeholder="认证状态" clearable style="width: 120px;">
          <el-option label="已认证" value="已认证" />
          <el-option label="待审核" value="待审核" />
        </el-select>
        <el-select v-model="typeFilter" placeholder="企业类型" clearable style="width: 120px;">
          <el-option label="电子厂" value="电子厂" />
          <el-option label="物流" value="物流" />
          <el-option label="餐饮" value="餐饮" />
          <el-option label="仓储" value="仓储" />
          <el-option label="制造业" value="制造业" />
        </el-select>
        <el-button type="primary" @click="handleSearch"><i class="fas fa-search" style="margin-right:4px;"></i>查询</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button style="margin-left: auto;"><i class="fas fa-download" style="margin-right:4px;"></i>导出</el-button>
      </div>

      <el-table :data="tableData" stripe :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="雇主ID" width="100" />
        <el-table-column label="雇主" min-width="200">
          <template #default="{ row }">
            <div>
              <div style="font-weight: 500;">{{ row.displayName }}</div>
              <div class="text-muted" style="font-size: 12px;">{{ row.companyName || row.company }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="industry" label="行业类型" width="100" />
        <el-table-column label="联系人" width="180">
          <template #default="{ row }">
            <div>
              <div>{{ row.contact }}</div>
              <div class="text-muted" style="font-size: 12px;">{{ row.contactPhone || row.phone }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="jobs" label="招工数" width="100">
          <template #default="{ row }">
            <span>{{ row.jobs ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="信用分" width="100">
          <template #default="{ row }">
            <span style="font-weight: 600; color: var(--primary);">{{ row.creditScore || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="certStatus" label="认证状态" width="100">
          <template #default="{ row }">
            <el-tag :type="(row.certStatus === '已认证' || row.certStatus === 'VERIFIED' || row.certStatus === 2) ? 'success' : 'warning'" effect="light">{{ formatCertStatus(row.certStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span :class="['status-badge', isNormal(row.status) ? 'success' : 'danger']">{{ formatStatus(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small">详情</el-button>
            <el-button link type="primary" size="small">资质</el-button>
            <el-button link type="warning" size="small" v-if="isNormal(row.status)" @click="handleFreeze(row)">冻结</el-button>
            <el-button link type="success" size="small" v-else @click="handleUnfreeze(row)">解冻</el-button>
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
import { listBosses, freezeUser, unfreezeUser } from '@/api/user'

const searchKeyword = ref('')
const certFilter = ref('')
const typeFilter = ref('')

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

// 统计数据
const stats = ref({ total: '-', normal: '-', frozen: '-', certified: '-' })

// 加载统计
const loadStats = async () => {
  try {
    const [allRes, normalRes, frozenRes] = await Promise.all([
      listBosses({ page: 0, size: 1 }),
      listBosses({ status: '正常', page: 0, size: 1 }),
      listBosses({ status: '冻结', page: 0, size: 1 })
    ])
    stats.value.total = allRes.total ?? 0
    stats.value.normal = normalRes.total ?? 0
    stats.value.frozen = frozenRes.total ?? 0
    // 已认证：加载全量数据客户端过滤
    const allDataRes = await listBosses({ page: 0, size: 1000 })
    const d = allDataRes.data
    const list = Array.isArray(d) ? d : (d?.content || [])
    stats.value.certified = list.filter(u => u.certStatus === 2 || u.certStatus === '已认证').length
  } catch (e) {
    console.warn('[Bosses] 加载统计失败:', e)
  }
}

// 后端 Boss 真实字段（继承 Worker）+ companyName, contact, contactPhone, industry
const normalizeBoss = (item) => {
  return {
    ...item,
    // 雇主名优先 companyName（企业名），其次 nickname/username
    displayName: item.companyName || item.nickname || item.username || '未知企业',
    // status, certStatus, industry, contact, contactPhone 后端已有，直接透传
    // jobs 后端暂无此字段
  }
}

// 加载数据
const loadData = async () => {
  try {
    const result = await listBosses({
      keyword: searchKeyword.value || undefined,
      status: certFilter.value || undefined,
      page: currentPage.value - 1, // API 0-based
      size: pageSize.value
    })
    const d = result.data
    tableData.value = (Array.isArray(d) ? d : (d?.content || d?.list || [])).map(normalizeBoss)
    total.value = result.total ?? d?.totalElements ?? d?.total ?? 0
  } catch (e) {
    console.warn('[Bosses] 加载列表失败:', e)
    tableData.value = []
    total.value = 0
  }
}

// 兼容后端不同字段/类型
const formatCertStatus = (s) => {
  if (s === 'VERIFIED' || s === 2 || s === '已认证') return '已认证'
  if (s === 'PENDING' || s === 1 || s === '待审核') return '待审核'
  if (s === 'REJECTED' || s === 0 || s === '已拒绝') return '已拒绝'
  return s || '-'
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
  certFilter.value = ''
  typeFilter.value = ''
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
    await ElMessageBox.confirm(`确定要冻结雇主 ${row.displayName || row.id} 吗？`, '提示', { type: 'warning' })
    await freezeUser(row.id)
    ElMessage.success('冻结成功')
    loadData()
    loadStats()
  } catch (e) {
    if (e !== 'cancel') console.warn('[Bosses] 冻结失败:', e)
  }
}
const handleUnfreeze = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要解冻雇主 ${row.displayName || row.id} 吗？`, '提示', { type: 'warning' })
    await unfreezeUser(row.id)
    ElMessage.success('解冻成功')
    loadData()
    loadStats()
  } catch (e) {
    if (e !== 'cancel') console.warn('[Bosses] 解冻失败:', e)
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
