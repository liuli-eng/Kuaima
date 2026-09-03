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
        <el-button type="primary" style="margin-left: auto;" @click="handleCreate"><i class="fas fa-plus" style="margin-right:4px;"></i>新建公告</el-button>
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
import { listNotices, createNotice, updateNotice, deleteNotice } from '@/api/content'
import { noticesData as fallbackNotices } from '@/mock'

const notices = ref([...fallbackNotices])
const searchKeyword = ref('')
const typeFilter = ref('')
const statusFilter = ref('')
const apiAvailable = ref(false)

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
    if (res && Array.isArray(res)) {
      notices.value = res
    } else if (res && Array.isArray(res.data)) {
      notices.value = res.data
    }
    apiAvailable.value = true
  } catch (e) {
    console.warn('[API] listNotices 后端暂未接入，使用 mock 数据')
    notices.value = [...fallbackNotices]
  }
}

const resetFilters = () => {
  searchKeyword.value = ''
  typeFilter.value = ''
  statusFilter.value = ''
}

const handleCreate = () => {
  ElMessageBox.prompt('请输入公告标题', '新建公告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async ({ value }) => {
    if (!value) return
    const newNotice = {
      title: value,
      type: '系统',
      typeClass: 'info',
      scope: '全平台',
      status: '草稿',
      statusClass: 'default',
      publishTime: '-'
    }
    try {
      if (apiAvailable.value) {
        const res = await createNotice(newNotice)
        newNotice.id = res?.id || Date.now()
      } else {
        newNotice.id = Date.now()
      }
      notices.value.unshift(newNotice)
      ElMessage.success('新建成功')
    } catch (e) {
      console.warn('[API] createNotice 后端暂未接入')
      newNotice.id = Date.now()
      notices.value.unshift(newNotice)
    }
  }).catch(() => {})
}

const handleEdit = (notice) => {
  ElMessageBox.prompt('修改标题', '编辑公告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: notice.title
  }).then(async ({ value }) => {
    try {
      if (apiAvailable.value) {
        await updateNotice(notice.id, { ...notice, title: value })
      } else {
        console.warn('[API] updateNotice 后端暂未接入')
      }
      notice.title = value
      ElMessage.success('修改成功')
    } catch (e) {
      console.warn('[API] updateNotice 请求失败')
      notice.title = value
    }
  }).catch(() => {})
}

const handlePublish = async (notice) => {
  const now = new Date().toISOString().slice(0, 10)
  try {
    if (apiAvailable.value) {
      await updateNotice(notice.id, { ...notice, status: '已发布', statusClass: 'success', publishTime: now })
    } else {
      console.warn('[API] updateNotice 后端暂未接入')
    }
    notice.status = '已发布'
    notice.statusClass = 'success'
    notice.publishTime = now
    ElMessage.success('已发布')
  } catch (e) {
    console.warn('[API] updateNotice 请求失败')
    notice.status = '已发布'
    notice.statusClass = 'success'
  }
}

const handleUnpublish = async (notice) => {
  try {
    if (apiAvailable.value) {
      await updateNotice(notice.id, { ...notice, status: '已下架', statusClass: 'warning' })
    } else {
      console.warn('[API] updateNotice 后端暂未接入')
    }
    notice.status = '已下架'
    notice.statusClass = 'warning'
    ElMessage.success('已下架')
  } catch (e) {
    console.warn('[API] updateNotice 请求失败')
    notice.status = '已下架'
    notice.statusClass = 'warning'
  }
}

const handleDelete = async (notice) => {
  try {
    await ElMessageBox.confirm('确定删除该公告？', '提示', { type: 'warning' })
    if (apiAvailable.value) {
      await deleteNotice(notice.id)
    } else {
      console.warn('[API] deleteNotice 后端暂未接入')
    }
    notices.value = notices.value.filter(n => n.id !== notice.id)
    ElMessage.success('删除成功')
  } catch (e) {
    if (e !== 'cancel') {
      console.warn('[API] deleteNotice 请求失败')
      notices.value = notices.value.filter(n => n.id !== notice.id)
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
