<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">规则管理</h1>
      <p class="page-desc">管理平台公示、信用评定、收费标准、交易规则等</p>
    </div>

    <div class="card">
      <div class="filter-bar">
        <el-select v-model="categoryFilter" placeholder="规则分类" clearable style="width: 140px;">
          <el-option label="通知公告" value="通知公告" />
          <el-option label="信用评定" value="信用评定" />
          <el-option label="收费标准" value="收费标准" />
          <el-option label="交易规则" value="交易规则" />
          <el-option label="隐私协议" value="隐私协议" />
        </el-select>
        <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px;">
          <el-option label="已发布" value="已发布" />
          <el-option label="草稿" value="草稿" />
          <el-option label="已归档" value="已归档" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <el-table :data="filteredRules" stripe :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }">
        <el-table-column prop="title" label="规则标题" min-width="240" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="version" label="版本号" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span :class="['status-badge', row.statusClass]">{{ row.status }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="effectiveTime" label="生效时间" width="140" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small">预览</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === '草稿'" link type="success" size="small" @click="handlePublish(row)">发布</el-button>
            <el-button v-else-if="row.status === '已发布'" link type="warning" size="small" @click="handleArchive(row)">归档</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <div class="pagination-info">共 {{ filteredRules.length }} 条记录</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listRules, updateRules, deleteRules } from '@/api/content'

const rules = ref([])
const categoryFilter = ref('')
const statusFilter = ref('')

const filteredRules = computed(() => {
  return rules.value.filter(r => {
    if (categoryFilter.value && r.category !== categoryFilter.value) return false
    if (statusFilter.value && r.status !== statusFilter.value) return false
    return true
  })
})

const loadData = async () => {
  try {
    const res = await listRules()
    rules.value = Array.isArray(res.data) ? res.data : (Array.isArray(res) ? res : [])
  } catch (e) {
    console.warn('[Rules] 加载失败:', e)
    rules.value = []
  }
}

const resetFilters = () => {
  categoryFilter.value = ''
  statusFilter.value = ''
}

const handleEdit = (rule) => {
  ElMessageBox.prompt('修改标题', '编辑规则', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: rule.title
  }).then(async ({ value }) => {
    try {
      await updateRules(rule.id, { ...rule, title: value })
      ElMessage.success('修改成功')
      await loadData()
    } catch (e) {
      ElMessage.error('修改失败')
    }
  }).catch(() => {})
}

const handlePublish = async (rule) => {
  const now = new Date().toISOString().slice(0, 10)
  try {
    await updateRules(rule.id, { ...rule, status: '已发布', statusClass: 'success', effectiveTime: now })
    ElMessage.success('已发布')
    await loadData()
  } catch (e) {
    ElMessage.error('发布失败')
  }
}

const handleArchive = async (rule) => {
  try {
    await updateRules(rule.id, { ...rule, status: '已归档', statusClass: 'warning' })
    ElMessage.success('已归档')
    await loadData()
  } catch (e) {
    ElMessage.error('归档失败')
  }
}

const handleDelete = async (rule) => {
  try {
    await ElMessageBox.confirm('确定删除该规则？', '提示', { type: 'warning' })
    await deleteRules(rule.id)
    ElMessage.success('删除成功')
    await loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
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
