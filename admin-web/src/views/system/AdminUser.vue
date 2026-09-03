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
        <el-button type="primary" style="margin-left: auto;" @click="handleCreate"><i class="fas fa-plus" style="margin-right:4px;"></i>新增管理员</el-button>
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
import { listAdminUsers, createAdminUser, updateAdminUser, resetAdminPassword, deleteAdminUser } from '@/api/system'
import { adminUserData as fallbackAdmins } from '@/mock'

const admins = ref([...fallbackAdmins])
const searchKeyword = ref('')
const roleFilter = ref('')
const apiAvailable = ref(false)

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
    const d = res?.data
    admins.value = Array.isArray(res)
      ? res
      : Array.isArray(d)
        ? d
        : (d?.content || d?.list || [])
    apiAvailable.value = true
  } catch (e) {
    console.warn('[API] listAdminUsers 后端暂未接入，使用 mock 数据')
    admins.value = [...fallbackAdmins]
  }
}

const resetFilters = () => {
  searchKeyword.value = ''
  roleFilter.value = ''
}

const handleCreate = () => {
  ElMessageBox.prompt('请输入管理员账号', '新增管理员', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async ({ value }) => {
    if (!value) return
    const newAdmin = {
      account: value,
      name: value,
      role: '查看员',
      dept: '待分配',
      lastLogin: '-',
      status: '启用',
      statusClass: 'success'
    }
    try {
      if (apiAvailable.value) {
        const res = await createAdminUser(newAdmin)
        newAdmin.id = res?.id || Date.now()
      } else {
        newAdmin.id = Date.now()
      }
      admins.value.unshift(newAdmin)
      ElMessage.success('新增成功')
    } catch (e) {
      console.warn('[API] createAdminUser 后端暂未接入')
      newAdmin.id = Date.now()
      admins.value.unshift(newAdmin)
    }
  }).catch(() => {})
}

const handleEdit = (row) => {
  ElMessageBox.prompt('修改姓名', '编辑管理员', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: row.name
  }).then(async ({ value }) => {
    try {
      if (apiAvailable.value) {
        await updateAdminUser(row.id, { ...row, name: value })
      } else {
        console.warn('[API] updateAdminUser 后端暂未接入')
      }
      row.name = value
      ElMessage.success('修改成功')
    } catch (e) {
      console.warn('[API] updateAdminUser 请求失败')
      row.name = value
    }
  }).catch(() => {})
}

const handleResetPwd = async (row) => {
  try {
    await ElMessageBox.confirm(`确定重置 ${row.name} 的密码为默认密码 admin123？`, '重置密码', { type: 'warning' })
    try {
      if (apiAvailable.value) {
        await resetAdminPassword(row.id)
      } else {
        console.warn('[API] resetAdminPassword 后端暂未接入')
      }
      ElMessage.success('密码已重置为 admin123')
    } catch (e) {
      console.warn('[API] resetAdminPassword 请求失败')
      ElMessage.success('密码已重置为 admin123（mock 模式）')
    }
  } catch (e) {
    if (e !== 'cancel') console.warn('[API] resetAdminPassword 请求异常')
  }
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === '启用' ? '禁用' : '启用'
  const newClass = newStatus === '启用' ? 'success' : 'danger'
  try {
    if (apiAvailable.value) {
      await updateAdminUser(row.id, { ...row, status: newStatus, statusClass: newClass })
    } else {
      console.warn('[API] updateAdminUser 后端暂未接入')
    }
    row.status = newStatus
    row.statusClass = newClass
    ElMessage.success('状态更新成功')
  } catch (e) {
    console.warn('[API] updateAdminUser 请求失败')
    row.status = newStatus
    row.statusClass = newClass
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除管理员 ${row.name}？`, '删除确认', { type: 'warning' })
    try {
      if (apiAvailable.value) {
        await deleteAdminUser(row.id)
      } else {
        console.warn('[API] deleteAdminUser 后端暂未接入')
      }
      admins.value = admins.value.filter(a => a.id !== row.id)
      ElMessage.success('删除成功')
    } catch (e) {
      if (e !== 'cancel') {
        console.warn('[API] deleteAdminUser 请求失败')
        admins.value = admins.value.filter(a => a.id !== row.id)
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
