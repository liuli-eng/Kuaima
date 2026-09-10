<template>
  <div>
    <div class="page-header">
      <div style="display: flex; align-items: center; gap: 12px;">
        <el-button text @click="router.back()"><i class="fas fa-arrow-left"></i></el-button>
        <div>
          <h1 class="page-title">报名人员管理</h1>
          <p class="page-desc">招工ID：{{ route.params.id }} - 查看和管理报名零工</p>
        </div>
      </div>
    </div>

    <div class="card">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="待确认" name="pending" />
        <el-tab-pane label="已录用" name="hired" />
        <el-tab-pane label="已到岗" name="working" />
        <el-tab-pane label="已完成" name="finished" />
        <el-tab-pane label="已拒绝" name="rejected" />
        <el-tab-pane label="全部" name="all" />
      </el-tabs>

      <el-table :data="filteredList" stripe :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }">
        <el-table-column label="用户" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="user-cell">
              <span class="mini-avatar">{{ (row.nickname || row.username || '?').charAt(0) }}</span>
              <div>
                <div style="font-weight: 500;">{{ row.nickname || row.username || '-' }}</div>
                <div class="text-muted" style="font-size: 12px;">{{ row.phone || '-' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="实名认证" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.certStatus === '已通过' || row.certStatus === '已认证'" type="success" effect="light">手机号已验证</el-tag>
            <el-tag v-else type="info" effect="light">手机号未验证</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报名状态" width="100">
          <template #default="{ row }">
            <span :class="['status-badge', statusClass(row.status)]">{{ formatStatus(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="报名备注" show-overflow-tooltip>
          <template #default="{ row }">{{ row.remark || '-' }}</template>
        </el-table-column>
        <el-table-column label="报名时间" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ formatDate(row.applyDate) }}</template>
        </el-table-column>
        <el-table-column label="录用时间" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ formatDate(row.hireDate) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === '已报名'">
              <el-button link type="success" size="small" @click="handleHire(row)">录用</el-button>
              <el-button link type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
            </template>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <div class="pagination-info">共 {{ filteredList.length }} 条记录</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listApplicants, hireApplicant, rejectApplicant } from '@/api/job'

const router = useRouter()
const route = useRoute()
const activeTab = ref('pending')
const allApplicants = ref([])

const statusMap = {
  'pending': '已报名',
  'hired': '已录用',
  'working': '已到岗',
  'finished': '已完成',
  'rejected': '取消报名',
}

const filteredList = computed(() => {
  if (activeTab.value === 'all') return allApplicants.value
  const target = statusMap[activeTab.value]
  return allApplicants.value.filter(a => a.status === target)
})

const formatStatus = (s) => {
  if (s === '已报名') return '待确认'
  if (s === '已录用') return '已录用'
  if (s === '已到岗') return '已到岗'
  if (s === '已完成') return '已完成'
  if (s === '取消报名') return '已拒绝'
  return s || '-'
}

const statusClass = (s) => {
  if (s === '已报名') return 'warning'
  if (s === '已录用') return 'success'
  if (s === '已到岗') return 'info'
  if (s === '已完成') return 'success'
  if (s === '取消报名') return 'default'
  return 'default'
}

const formatDate = (d) => {
  if (!d) return '-'
  const s = String(d).replace('T', ' ')
  return s.length > 10 ? s.substring(0, 10) : s
}

const loadData = async () => {
  try {
    const res = await listApplicants(route.params.id)
    allApplicants.value = res.data || []
  } catch (err) {
    console.warn('[JobApplicants] 加载失败:', err.message)
    allApplicants.value = []
    ElMessage.error('加载报名人员失败')
  }
}

const onTabChange = () => {}

const handleHire = async (row) => {
  try {
    await ElMessageBox.confirm(`确定录用「${row.nickname || row.username || row.userId}」吗？`, '录用确认', { type: 'warning' })
    await hireApplicant(row.id)
    ElMessage.success('已录用')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleReject = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因（可选）', '拒绝报名', {
      type: 'warning',
      inputPlaceholder: '拒绝原因',
      inputValidator: () => true,
    })
    await rejectApplicant(row.id, value)
    ElMessage.success('已拒绝')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.mini-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF8C42, #FF6B35);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  flex-shrink: 0;
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

.text-muted {
  color: var(--text-muted, #9CA3AF);
}
</style>
