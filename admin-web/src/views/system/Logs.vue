<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">操作日志</h1>
      <p class="page-desc">记录所有管理员关键操作，支持追溯和审计</p>
    </div>

    <div class="card">
      <div class="filter-bar">
        <el-input v-model="searchKeyword" placeholder="搜索操作人/对象" clearable style="width: 240px;" prefix-icon="Search" />
        <el-select v-model="typeFilter" placeholder="操作类型" clearable style="width: 140px;">
          <el-option label="登录" value="登录" />
          <el-option label="数据修改" value="数据修改" />
          <el-option label="审核" value="审核" />
          <el-option label="权限变更" value="权限变更" />
          <el-option label="删除" value="删除" />
          <el-option label="系统操作" value="系统操作" />
        </el-select>
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
        <el-button style="margin-left: auto;"><i class="fas fa-download" style="margin-right:4px;"></i>导出</el-button>
      </div>

      <el-table :data="filteredLogs" stripe :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }">
        <el-table-column prop="id" label="日志ID" width="100" />
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="type" label="操作类型" width="120">
          <template #default="{ row }">
            <el-tag effect="light">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="target" label="操作对象" min-width="180" />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="time" label="时间" width="180" />
        <el-table-column label="结果" width="100">
          <template #default="{ row }">
            <span :class="['status-badge', row.resultClass]">{{ row.result }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default>
            <el-button link type="primary" size="small">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <div class="pagination-info">共 {{ filteredLogs.length }} 条记录</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { listLogs } from '@/api/system'
import { logsData as fallbackLogs } from '@/mock'

const logs = ref([...fallbackLogs])
const searchKeyword = ref('')
const typeFilter = ref('')
const dateRange = ref([])
const apiAvailable = ref(false)

const filteredLogs = computed(() => {
  return logs.value.filter(l => {
    if (searchKeyword.value && !l.operator.includes(searchKeyword.value) && !l.target.includes(searchKeyword.value)) return false
    if (typeFilter.value && l.type !== typeFilter.value) return false
    return true
  })
})

const loadData = async () => {
  try {
    const res = await listLogs({ page: 0, size: 100 })
    const d = res?.data
    logs.value = Array.isArray(res)
      ? res
      : Array.isArray(d)
        ? d
        : (d?.content || d?.list || [])
    apiAvailable.value = true
  } catch (e) {
    console.warn('[API] listLogs 后端暂未接入，使用 mock 数据')
    logs.value = [...fallbackLogs]
  }
}

const resetFilters = () => {
  searchKeyword.value = ''
  typeFilter.value = ''
  dateRange.value = []
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
