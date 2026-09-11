<template>
  <div>
    <div class="page-header">
      <div style="display: flex; align-items: center; gap: 12px;">
        <el-button text @click="router.back()"><i class="fas fa-arrow-left"></i></el-button>
        <div>
          <h1 class="page-title">老板详情</h1>
          <p class="page-desc">雇主ID：{{ route.params.id }}</p>
        </div>
      </div>
    </div>

    <div class="content-grid" style="grid-template-columns: 2fr 1fr;">
      <div class="card">
        <div class="card-header">
          <span class="card-title">基本信息</span>
        </div>
        <el-descriptions :column="2" border v-loading="loading">
          <el-descriptions-item label="企业名称">{{ detail.companyName || detail.nickname || '-' }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ detail.username || '-' }}</el-descriptions-item>
          <el-descriptions-item label="行业类型">{{ detail.industry || '-' }}</el-descriptions-item>
          <el-descriptions-item label="信用分">
            <span style="font-weight: 600; color: var(--primary);">{{ detail.creditScore ?? '-' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="联系人">{{ detail.contact || detail.realName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detail.contactPhone || detail.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="认证状态">
            <el-tag :type="certTagType" effect="light">{{ formatCertStatus(detail.certStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="账号状态">
            <span :class="['status-badge', isNormal(detail.status) ? 'success' : 'danger']">{{ formatStatus(detail.status) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="企业认证">
            <el-tag :type="enterpriseTagType" effect="light">{{ formatEnterpriseStatus(detail.enterpriseStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="实名认证">
            <el-tag :type="detail.realnameStatus === 'APPROVED' ? 'success' : 'warning'" effect="light">{{ formatRealnameStatus(detail.realnameStatus) }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="card">
        <div class="card-header">
          <span class="card-title">企业资质</span>
        </div>
        <el-descriptions :column="1" border v-loading="loading">
          <el-descriptions-item label="营业执照号">{{ detail.licenseNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="法定代表人">{{ detail.legalRep || '-' }}</el-descriptions-item>
          <el-descriptions-item label="统一社会信用代码">{{ detail.unifiedCode || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <span class="card-title">操作</span>
      </div>
      <div style="display: flex; gap: 12px;">
        <el-button v-if="isNormal(detail.status)" type="warning" @click="handleFreeze">冻结</el-button>
        <el-button v-else type="success" @click="handleUnfreeze">解冻</el-button>
        <el-button @click="router.back()">返回</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUser, freezeUser, unfreezeUser } from '@/api/user'

const route = useRoute()
const router = useRouter()
const detail = ref({})
const loading = ref(true)

const certTagType = computed(() => {
  const s = detail.value.certStatus
  return (s === '已认证' || s === 'VERIFIED' || s === 2) ? 'success' : 'warning'
})
const enterpriseTagType = computed(() => detail.value.enterpriseStatus === 'APPROVED' ? 'success' : 'warning')

const formatCertStatus = (s) => {
  if (s === 'VERIFIED' || s === 2 || s === '已认证') return '已认证'
  if (s === 'PENDING' || s === 1 || s === '待审核') return '待审核'
  if (s === 'REJECTED' || s === 0 || s === '已拒绝') return '已拒绝'
  return s || '未认证'
}
const formatStatus = (s) => {
  if (s === 'NORMAL' || s === 1 || s === '正常') return '正常'
  if (s === 'FROZEN' || s === 0 || s === '冻结') return '冻结'
  return s || '-'
}
const formatEnterpriseStatus = (s) => {
  if (s === 'APPROVED') return '已认证'
  if (s === 'PENDING') return '待审核'
  if (s === 'REJECTED') return '已拒绝'
  return '未认证'
}
const formatRealnameStatus = (s) => {
  if (s === 'APPROVED') return '已认证'
  if (s === 'PENDING') return '待审核'
  if (s === 'REJECTED') return '已拒绝'
  return '未认证'
}
const isNormal = (s) => s === '正常' || s === 'NORMAL' || s === 1

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await getUser(route.params.id)
    detail.value = res.data || res || {}
  } catch (e) {
    ElMessage.error('加载详情失败')
    console.warn('[BossDetail] 加载失败:', e)
  } finally {
    loading.value = false
  }
}

const handleFreeze = async () => {
  try {
    await ElMessageBox.confirm(`确定要冻结雇主「${detail.value.companyName || detail.value.username}」吗？`, '提示', { type: 'warning' })
    await freezeUser(detail.value.id)
    ElMessage.success('冻结成功')
    loadDetail()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('冻结失败')
  }
}
const handleUnfreeze = async () => {
  try {
    await ElMessageBox.confirm(`确定要解冻雇主「${detail.value.companyName || detail.value.username}」吗？`, '提示', { type: 'warning' })
    await unfreezeUser(detail.value.id)
    ElMessage.success('解冻成功')
    loadDetail()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('解冻失败')
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.content-grid {
  display: grid;
  gap: 16px;
  margin-bottom: 16px;
}
</style>
