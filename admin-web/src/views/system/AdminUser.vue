<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">管理员管理</h1>
      <p class="page-desc">管理后台管理员账号和权限分配</p>
    </div>

    <div class="card">
      <div class="filter-bar">
        <el-input v-model="searchKeyword" placeholder="搜索账号/姓名" clearable style="width: 240px;" prefix-icon="Search" />
        <el-select v-model="roleFilter" placeholder="角色" clearable style="width: 140px;">
          <el-option label="超级管理员" value="超级管理员" />
          <el-option label="管理员" value="管理员" />
          <el-option label="编辑员" value="编辑员" />
          <el-option label="查看员" value="查看员" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <el-table :data="filteredAdmins" stripe :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }">
        <el-table-column prop="account" label="账号" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="role" label="角色" width="140">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)" effect="light">{{ row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dept" label="部门" width="120" />
        <el-table-column prop="lastLogin" label="最后登录" width="160" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span :class="['status-badge', row.statusClass]">{{ row.status }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button v-if="row.status === '启用'" link type="danger" size="small" @click="handleToggleStatus(row)">禁用</el-button>
            <el-button v-else link type="success" size="small" @click="handleToggleStatus(row)">启用</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listAdminUsers, updateAdminUser, resetAdminPassword, deleteAdminUser } from '@/api/system'

const admins = ref([])
const searchKeyword = ref('')
const roleFilter = ref('')

const filteredAdmins = computed(() => {
  return admins.value.filter(a => {
    if (searchKeyword.value && !a.account.includes(searchKeyword.value) && !a.name.includes(searchKeyword.value)) return false
    if (roleFilter.value && a.role !== roleFilter.value) return false
    return true
  })
})

const getRoleType = (role) => {
  const map = {
    '超级管理员': 'danger',
    '管理员': 'warning',
    '编辑员': 'primary',
    '查看员': 'info'
  }
  return map[role] || 'info'
}

const loadData = async () => {
  try {
    const res = await listAdminUsers({ page: 0, size: 100 })
    const d = res.data
    admins.value = Array.isArray(d) ? d : (d?.content || d?.list || [])
  } catch (e) {
    console.warn('[AdminUser] 加载失败:', e)
    admins.value = []
  }
}

const resetFilters = () => {
  searchKeyword.value = ''
  roleFilter.value = ''
}

const handleEdit = (row) => {
  ElMessageBox.prompt('修改姓名', '编辑管理员', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: row.name
  }).then(async ({ value }) => {
    try {
      await updateAdminUser(row.id, { ...row, name: value })
      ElMessage.success('修改成功')
      loadData()
    } catch (e) {
      console.warn('[AdminUser] 修改失败:', e)
      ElMessage.error('修改失败')
    }
  }).catch(() => {})
}

const handleResetPwd = async (row) => {
  try {
    await ElMessageBox.confirm(`确定重置 ${row.name} 的密码为默认密码 admin123？`, '重置密码', { type: 'warning' })
    try {
      await resetAdminPassword(row.id)
      ElMessage.success('密码已重置为 admin123')
    } catch (e) {
      console.warn('[AdminUser] 重置密码失败:', e)
      ElMessage.error('重置密码失败')
    }
  } catch (e) {
    if (e !== 'cancel') console.warn('[AdminUser] 重置密码异常:', e)
  }
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === '启用' ? '禁用' : '启用'
  const newClass = newStatus === '启用' ? 'success' : 'danger'
  try {
    await updateAdminUser(row.id, { ...row, status: newStatus, statusClass: newClass })
    ElMessage.success('状态更新成功')
    loadData()
  } catch (e) {
    console.warn('[AdminUser] 状态更新失败:', e)
    ElMessage.error('状态更新失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除管理员 ${row.name}？`, '删除确认', { type: 'warning' })
    try {
      await deleteAdminUser(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (e) {
      if (e !== 'cancel') {
        console.warn('[AdminUser] 删除失败:', e)
        ElMessage.error('删除失败')
      }
    }
  } catch (e) { /* cancel */ }
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
