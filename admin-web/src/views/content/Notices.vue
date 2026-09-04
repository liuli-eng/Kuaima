<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">公告管理</h1>
      <p class="page-desc">发布和管理平台系统公告、活动通知</p>
    </div>

    <div class="card">
      <div class="filter-bar">
        <el-input v-model="searchKeyword" placeholder="搜索公告标题" clearable style="width: 240px;" prefix-icon="Search" />
        <el-select v-model="typeFilter" placeholder="公告类型" clearable style="width: 120px;">
          <el-option label="系统" value="系统" />
          <el-option label="活动" value="活动" />
          <el-option label="政策" value="政策" />
        </el-select>
        <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px;">
          <el-option label="已发布" value="已发布" />
          <el-option label="草稿" value="草稿" />
          <el-option label="已下架" value="已下架" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <el-table :data="filteredNotices" stripe :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }">
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.typeClass === 'info' ? 'primary' : row.typeClass === 'warning' ? 'warning' : 'success'" effect="light">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="公告标题" min-width="240" />
        <el-table-column prop="scope" label="发布范围" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span :class="['status-badge', row.statusClass]">{{ row.status }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="140" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" size="small">预览</el-button>
            <el-button v-if="row.status === '草稿'" link type="success" size="small" @click="handlePublish(row)">立即发布</el-button>
            <el-button v-else-if="row.status === '已发布'" link type="warning" size="small" @click="handleUnpublish(row)">下架</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <div class="pagination-info">共 {{ filteredNotices.length }} 条记录</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listNotices, updateNotice, deleteNotice } from '@/api/content'

const notices = ref([])
const searchKeyword = ref('')
const typeFilter = ref('')
const statusFilter = ref('')

const filteredNotices = computed(() => {
  return notices.value.filter(n => {
    if (searchKeyword.value && !n.title.includes(searchKeyword.value)) return false
    if (typeFilter.value && n.type !== typeFilter.value) return false
    if (statusFilter.value && n.status !== statusFilter.value) return false
    return true
  })
})

const loadData = async () => {
  try {
    const res = await listNotices()
    notices.value = Array.isArray(res.data) ? res.data : (Array.isArray(res) ? res : [])
  } catch (e) {
    console.warn('[Notices] 加载失败:', e)
    notices.value = []
  }
}

const resetFilters = () => {
  searchKeyword.value = ''
  typeFilter.value = ''
  statusFilter.value = ''
}

const handleEdit = (notice) => {
  ElMessageBox.prompt('修改标题', '编辑公告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: notice.title
  }).then(async ({ value }) => {
    try {
      await updateNotice(notice.id, { ...notice, title: value })
      ElMessage.success('修改成功')
      await loadData()
    } catch (e) {
      ElMessage.error('修改失败')
    }
  }).catch(() => {})
}

const handlePublish = async (notice) => {
  const now = new Date().toISOString().slice(0, 10)
  try {
    await updateNotice(notice.id, { ...notice, status: '已发布', statusClass: 'success', publishTime: now })
    ElMessage.success('已发布')
    await loadData()
  } catch (e) {
    ElMessage.error('发布失败')
  }
}

const handleUnpublish = async (notice) => {
  try {
    await updateNotice(notice.id, { ...notice, status: '已下架', statusClass: 'warning' })
    ElMessage.success('已下架')
    await loadData()
  } catch (e) {
    ElMessage.error('下架失败')
  }
}

const handleDelete = async (notice) => {
  try {
    await ElMessageBox.confirm('确定删除该公告？', '提示', { type: 'warning' })
    await deleteNotice(notice.id)
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
