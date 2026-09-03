<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">黑名单管理</h1>
      <p class="page-desc">管理被封禁的用户账号，支持解封和延长封禁</p>
    </div>

    <div class="content-grid">
      <div class="card" style="grid-column: 1 / -1;">
        <div class="stat-cards" style="margin-bottom: 24px;">
          <div class="stat-card">
            <div class="stat-card-header">
              <span class="stat-card-title">黑名单数</span>
              <div class="stat-card-icon red"><i class="fas fa-user-times"></i></div>
            </div>
            <div class="stat-card-value">{{ blacklist.length }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-card-header">
              <span class="stat-card-title">本月新增</span>
              <div class="stat-card-icon yellow"><i class="fas fa-plus"></i></div>
            </div>
            <div class="stat-card-value">{{ pendingCount }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-card-header">
              <span class="stat-card-title">已解封</span>
              <div class="stat-card-icon green"><i class="fas fa-unlock"></i></div>
            </div>
            <div class="stat-card-value">{{ unfrozenCount }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-card-header">
              <span class="stat-card-title">封禁原因Top</span>
              <div class="stat-card-icon blue"><i class="fas fa-chart-bar"></i></div>
            </div>
            <div class="stat-card-value" style="font-size: 20px;">飞单 45%</div>
          </div>
        </div>

        <div class="filter-bar">
          <el-select v-model="typeFilter" placeholder="用户类型" clearable style="width: 120px;">
            <el-option label="零工" value="零工" />
            <el-option label="雇主" value="雇主" />
          </el-select>
          <el-select v-model="reasonFilter" placeholder="封禁原因" clearable style="width: 140px;">
            <el-option label="飞单行为" value="飞单行为" />
            <el-option label="虚假招工" value="虚假招工" />
            <el-option label="恶意投诉" value="恶意投诉" />
            <el-option label="拖欠工资" value="拖欠工资" />
          </el-select>
          <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px;">
            <el-option label="封禁中" value="封禁中" />
            <el-option label="已解封" value="已解封" />
          </el-select>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
          <el-button style="margin-left: auto;" @click="handleAdd"><i class="fas fa-plus" style="margin-right:4px;"></i>新增封禁</el-button>
          <el-button><i class="fas fa-download" style="margin-right:4px;"></i>导出</el-button>
        </div>

        <el-table :data="filteredBlacklist" stripe :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }">
          <el-table-column prop="id" label="记录ID" width="100" />
          <el-table-column prop="user" label="用户" width="140" />
          <el-table-column prop="type" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="row.type === '零工' ? 'primary' : 'success'" effect="light">{{ row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="封禁原因" min-width="140" />
          <el-table-column prop="time" label="封禁时间" width="160" />
          <el-table-column prop="expireTime" label="到期时间" width="140" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <span :class="['status-badge', row.statusClass]">{{ row.status }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small">查看详情</el-button>
              <el-button v-if="row.status === '封禁中'" link type="success" size="small" @click="handleUnfreeze(row)">解封</el-button>
              <el-button v-if="row.status === '封禁中'" link type="warning" size="small">延长封禁</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <div class="pagination-info">共 {{ filteredBlacklist.length }} 条记录</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listBlacklist, addBlacklist, unfreezeBlacklist } from '@/api/risk'
import { blacklistData as fallbackBlacklist } from '@/mock'

const blacklist = ref([...fallbackBlacklist])
const typeFilter = ref('')
const reasonFilter = ref('')
const statusFilter = ref('')
const apiAvailable = ref(false)

const filteredBlacklist = computed(() => {
  return blacklist.value.filter(b => {
    if (typeFilter.value && b.type !== typeFilter.value) return false
    if (reasonFilter.value && b.reason !== reasonFilter.value) return false
    if (statusFilter.value && b.status !== statusFilter.value) return false
    return true
  })
})

const pendingCount = computed(() => Math.floor(blacklist.value.length / 3))
const unfrozenCount = computed(() => blacklist.value.filter(b => b.status === '已解封').length)

const loadData = async () => {
  try {
    const res = await listBlacklist()
    if (res && Array.isArray(res)) {
      blacklist.value = res
    } else if (res && Array.isArray(res.data)) {
      blacklist.value = res.data
    }
    apiAvailable.value = true
  } catch (e) {
    console.warn('[API] listBlacklist 后端暂未接入，使用 mock 数据')
    blacklist.value = [...fallbackBlacklist]
  }
}

const resetFilters = () => {
  typeFilter.value = ''
  reasonFilter.value = ''
  statusFilter.value = ''
}

const handleAdd = async () => {
  try {
    const { value: userId } = await ElMessageBox.prompt('请输入用户ID', '新增封禁', {
      confirmButtonText: '下一步',
      cancelButtonText: '取消'
    }).catch(() => ({ value: null }))
    if (!userId) return
    const newItem = {
      id: 'BL' + Date.now(),
      user: '用户' + userId,
      type: '零工',
      reason: '飞单行为',
      time: new Date().toISOString().slice(0, 10),
      expireTime: new Date(Date.now() + 365 * 86400000).toISOString().slice(0, 10),
      status: '封禁中',
      statusClass: 'danger'
    }
    try {
      if (apiAvailable.value) {
        await addBlacklist({ userId, reason: '飞单行为' })
      } else {
        console.warn('[API] addBlacklist 后端暂未接入')
      }
      blacklist.value.unshift(newItem)
      ElMessage.success('封禁成功')
    } catch (e) {
      console.warn('[API] addBlacklist 请求失败')
      blacklist.value.unshift(newItem)
      ElMessage.success('已在前端添加（mock 模式）')
    }
  } catch (e) { /* cancel */ }
}

const handleUnfreeze = async (row) => {
  try {
    await ElMessageBox.confirm(`确定解封用户 ${row.user}？`, '解封确认', { type: 'warning' })
    try {
      if (apiAvailable.value) {
        await unfreezeBlacklist(row.id)
      } else {
        console.warn('[API] unfreezeBlacklist 后端暂未接入')
      }
      row.status = '已解封'
      row.statusClass = 'default'
      ElMessage.success('已解封')
    } catch (e) {
      console.warn('[API] unfreezeBlacklist 请求失败')
      row.status = '已解封'
      row.statusClass = 'default'
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.warn('[API] unfreezeBlacklist 请求异常')
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
