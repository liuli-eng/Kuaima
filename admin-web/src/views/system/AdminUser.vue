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
          <el-option label="审核员" value="审核员" />
          <el-option label="查看员" value="查看员" />
        </el-select>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
        <el-button type="primary" style="margin-left: auto;" @click="goCreate">
          新建账号
        </el-button>
      </div>

      <el-table :data="filteredAdmins" stripe :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }">
        <el-table-column label="ID" width="70">
          <template #default="{ row }">{{ row.id }}</template>
        </el-table-column>
        <el-table-column label="姓名" width="140">
          <template #default="{ row }">
            <div style="display:flex; align-items:center; gap:8px;">
              <span class="avatar-badge" :style="{ background: avatarBg(row.role) }">{{ (row.name || 'A').charAt(0) }}</span>
              {{ row.name || '-' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="登录账号" width="130" />
        <el-table-column prop="role" label="角色" width="130">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)" effect="light">{{ roleLabel(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dept" label="部门" width="110" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="最后登录" width="150">
          <template #default="{ row }">{{ fmtTime(row.lastLoginTime) || '从未登录' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <span :class="['status-badge', getStatusClass(row.status)]">{{ row.status }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="goEdit(row)">编辑</el-button>
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listAdminUsers, updateAdminUser, resetAdminPassword, deleteAdminUser } from '@/api/system'

const router = useRouter()

const admins = ref([])
const searchKeyword = ref('')
const roleFilter = ref('')

// 角色中英文映射（后端存储英文枚举值）
const roleLabelMap = {
  SUPER_ADMIN: '超级管理员',
  ADMIN: '管理员',
  EDITOR: '审核员',
  VIEWER: '查看员'
}
const roleLabel = (role) => roleLabelMap[role] || role || '查看员'

const filteredAdmins = computed(() => {
  return admins.value.filter(a => {
    const kw = searchKeyword.value.trim()
    const account = a.username || a.account || ''
    if (kw && !account.includes(kw) && !(a.name || '').includes(kw)) return false
    if (roleFilter.value && roleLabel(a.role) !== roleFilter.value) return false
    return true
  })
})

const getRoleType = (role) => {
  const map = {
    SUPER_ADMIN: 'danger',
    ADMIN: 'warning',
    EDITOR: 'primary',
    VIEWER: 'info'
  }
  return map[role] || 'info'
}

const getStatusClass = (status) => (status === '启用' ? 'success' : 'danger')

const avatarBg = (role) => {
  const map = {
    SUPER_ADMIN: 'linear-gradient(135deg,#EF4444,#DC2626)',
    ADMIN: 'linear-gradient(135deg,#3B82F6,#2563EB)',
    EDITOR: 'linear-gradient(135deg,#10B981,#059669)',
    VIEWER: 'linear-gradient(135deg,#6B7280,#4B5563)'
  }
  return map[role] || 'linear-gradient(135deg,#FF6B35,#FF8C42)'
}

const fmtTime = (v) => {
  if (!v) return ''
  return String(v).replace('T', ' ').slice(0, 16)
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

// 跳转到原型风格的添加/编辑管理员页
const goCreate = () => {
  router.push('/admin/admin-user/form')
}

const goEdit = (row) => {
  router.push(`/admin/admin-user/form?mode=edit&id=${row.id}`)
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
  try {
    await updateAdminUser(row.id, { ...row, status: newStatus })
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
.avatar-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}
</style>
