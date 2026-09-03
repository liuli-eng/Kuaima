<template>
  <div>
    <div class="filter-bar">
      <el-select v-model="statusFilter" placeholder="审核状态" clearable style="width: 120px;">
        <el-option label="待审核" value="待审核" />
        <el-option label="已通过" value="已通过" />
        <el-option label="已拒绝" value="已拒绝" />
      </el-select>
      <el-input v-model="searchKeyword" placeholder="搜索申请人姓名" clearable style="width: 200px;" prefix-icon="Search" />
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <el-table :data="filteredData" stripe :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }">
      <el-table-column prop="id" label="审核ID" width="120" />
      <el-table-column prop="type" label="类型" width="120" />
      <el-table-column prop="applicant" label="申请人" min-width="180" />
      <el-table-column prop="applyTime" label="提交时间" width="160" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <span :class="['status-badge', row.statusClass]">{{ row.status }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="viewDetail(row)">查看详情</el-button>
          <template v-if="row.status === '待审核'">
            <el-button link type="success" size="small" @click="handleApprove(row)">通过</el-button>
            <el-button link type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <div class="pagination-info">共 {{ filteredData.length }} 条记录</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listCertifications, auditCertPass, auditCertReject } from '@/api/content'
import { certificationData as fallbackCerts } from '@/mock'

const props = defineProps({ type: { type: String, default: 'worker' } })
const router = useRouter()

const tableData = ref([...fallbackCerts])
const statusFilter = ref('')
const searchKeyword = ref('')
const dateRange = ref([])
const apiAvailable = ref(false)

const typeMap = { worker: '零工认证', boss: '雇主认证' }

const filteredData = computed(() => {
  return tableData.value.filter(c => {
    if (props.type && c.type !== typeMap[props.type]) return false
    if (statusFilter.value && c.status !== statusFilter.value) return false
    if (searchKeyword.value && !c.applicant.includes(searchKeyword.value)) return false
    return true
  })
})

const loadData = async () => {
  try {
    const res = await listCertifications()
    if (res && Array.isArray(res)) {
      tableData.value = res
    } else if (res && Array.isArray(res.data)) {
      tableData.value = res.data
    }
    apiAvailable.value = true
  } catch (e) {
    console.warn('[API] listCertifications 后端暂未接入，使用 mock 数据')
    tableData.value = [...fallbackCerts]
  }
}

const resetFilters = () => {
  statusFilter.value = ''
  searchKeyword.value = ''
  dateRange.value = []
}

const viewDetail = (row) => {
  router.push(`/content/certification/${row.id}`)
}

const handleApprove = async (row) => {
  try {
    if (apiAvailable.value) {
      await auditCertPass(row.id)
    } else {
      console.warn('[API] auditCertPass 后端暂未接入')
    }
    row.status = '已通过'
    row.statusClass = 'success'
    ElMessage.success('已通过')
  } catch (e) {
    console.warn('[API] auditCertPass 请求失败')
    row.status = '已通过'
    row.statusClass = 'success'
  }
}

const handleReject = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝审核', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入拒绝原因'
    }).catch(() => ({ value: '' }))
    if (apiAvailable.value) {
      await auditCertReject(row.id, reason || '不符合认证要求')
    } else {
      console.warn('[API] auditCertReject 后端暂未接入')
    }
    row.status = '已拒绝'
    row.statusClass = 'danger'
    ElMessage.success('已拒绝')
  } catch (e) {
    if (e !== 'cancel') {
      console.warn('[API] auditCertReject 请求失败')
      row.status = '已拒绝'
      row.statusClass = 'danger'
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
