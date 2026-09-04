<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">招工管理</h1>
      <p class="page-desc">管理平台所有招工信息，支持查看、编辑、开启/关闭等操作</p>
    </div>

    <div class="card">
      <div class="filter-bar">
        <el-input v-model="searchKeyword" placeholder="搜索招工名称/雇主" clearable style="width: 240px;" prefix-icon="Search" />
        <el-select v-model="typeFilter" placeholder="工种类型" clearable style="width: 140px;">
          <el-option label="电子厂" value="电子厂" />
          <el-option label="物流" value="物流" />
          <el-option label="餐饮" value="餐饮" />
          <el-option label="仓储" value="仓储" />
          <el-option label="制造业" value="制造业" />
          <el-option label="汽车" value="汽车" />
          <el-option label="服务业" value="服务业" />
          <el-option label="农业" value="农业" />
        </el-select>
        <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px;">
          <el-option label="进行中" value="进行中" />
          <el-option label="已结束" value="已结束" />
          <el-option label="待审核" value="待审核" />
          <el-option label="已通过" value="已通过" />
          <el-option label="审核拒绝" value="审核拒绝" />
        </el-select>
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
        <el-button type="primary" @click="handleSearch"><i class="fas fa-search" style="margin-right:4px;"></i>查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table :data="jobsData" stripe :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }">
        <el-table-column prop="id" label="招工ID" width="100">
          <template #default="{ row }">
            <span style="color: var(--primary); font-family: monospace;">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="工种" min-width="140" />
        <el-table-column prop="employer" label="雇主" min-width="180" />
        <el-table-column prop="price" label="工价" width="100">
          <template #default="{ row }">
            <span style="font-weight: 600; color: var(--primary);">{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="count" label="招聘人数" width="100" />
        <el-table-column prop="applications" label="报名人数" width="100">
          <template #default="{ row }">
            <router-link :to="`/admin/jobs/applicants/${row.id}`" style="color: var(--primary);">{{ row.applications }}</router-link>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="地点" min-width="140" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span :class="['status-badge', row.statusClass]">{{ row.status }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="发布时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small">详情</el-button>
            <router-link :to="`/admin/jobs/edit/${row.id}`" style="margin-right: 8px;">
              <el-button link type="primary" size="small">编辑</el-button>
            </router-link>
            <el-button link type="warning" size="small">{{ row.status === '进行中' ? '关闭' : '开启' }}</el-button>
            <el-button link type="danger" size="small">删除</el-button>
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
import { listJobs } from '@/api/job'

const searchKeyword = ref('')
const typeFilter = ref('')
const statusFilter = ref('')
const dateRange = ref([])

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 列表数据（带 fallback）
const jobsData = ref([])

// status → statusClass 映射
const statusClassMap = {
  '招工中': 'info',
  '进行中': 'info',
  '已完成': 'success',
  '已结束': 'default',
  '已取消': 'default',
  '待审核': 'warning',
  '待确认': 'warning',
  '审核拒绝': 'danger',
  '已拒绝': 'danger',
  '已通过': 'success',
}

const loadJobs = async () => {
  try {
    const res = await listJobs({
      type: typeFilter.value || undefined,
      status: statusFilter.value || undefined,
      title: searchKeyword.value || undefined,
      page: currentPage.value - 1,
      size: pageSize.value,
    })
    // res 结构: { code, message, data, total }，data 可能是 Spring Data Page
    const d = res.data
    const list = Array.isArray(d) ? d : (d?.content || d?.list || [])
    total.value = res.total ?? d?.totalElements ?? d?.total ?? list.length
    jobsData.value = list.map(normalizeJob)
  } catch (err) {
    console.warn('[Jobs] API 加载失败:', err.message)
    jobsData.value = []
    total.value = 0
    ElMessage.error('加载招工列表失败')
  }
}

const normalizeJob = (item) => {
  // 后端 BossOrderView 字段名 → 模板期望字段
  const statusVal = item.orderStatus ?? item.status ?? '未知'
  return {
    id: item.id,
    type: item.type ?? item.postion ?? item.jobTitle,
    employer: item.employerName ?? item.employer ?? '未知雇主',
    price: item.salary ?? item.price ?? item.dailyWage,
    count: item.orderNum ?? item.count ?? item.needCount,
    applications: item.applications ?? item.applicationCount ?? 0,
    location: item.address ?? item.location ?? item.workplace,
    status: statusVal,
    statusClass: item.statusClass ?? statusClassMap[statusVal] ?? 'default',
    time: item.timestamp ?? item.time ?? item.publishTime,
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadJobs()
}

const handleReset = () => {
  searchKeyword.value = ''
  typeFilter.value = ''
  statusFilter.value = ''
  dateRange.value = []
  handleSearch()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadJobs()
}

onMounted(loadJobs)
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
