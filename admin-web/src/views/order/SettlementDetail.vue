<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">结算详情</h1>
      <p class="page-desc">查看结算单完整信息与资金流向</p>
    </div>

    <div class="card" v-loading="loading">
      <div class="detail-actions" style="margin-bottom: 16px;">
        <el-button @click="router.back()">返回</el-button>
        <el-button v-if="detail.status === '待结算'" type="success" @click="handleSettle">确认结算</el-button>
      </div>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="结算单号">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <span :class="['status-badge', statusClass]">{{ detail.status }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="订单号">{{ detail.orderId }}</el-descriptions-item>
        <el-descriptions-item label="工种">{{ detail.jobTitle || detail.postion || '-' }}</el-descriptions-item>
        <el-descriptions-item label="雇主">{{ detail.employerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="雇主电话">{{ detail.employerPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="零工">{{ detail.workerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="零工电话">{{ detail.workerPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="工作天数">{{ detail.workDays ?? '-' }} 天</el-descriptions-item>
        <el-descriptions-item label="报名时间">{{ formatTime(detail.applyDate) }}</el-descriptions-item>
        <el-descriptions-item label="结算金额">¥{{ detail.amount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="平台服务费">¥{{ detail.platformFee ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="实付金额">¥{{ detail.actualAmount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付流水号">{{ detail.payNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ formatTime(detail.payTime) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(detail.date) }}</el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSettlementDetail, settlePay } from '@/api/settlement'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref({})

const statusClassMap = {
  '待结算': 'warning',
  '已结算': 'success',
  '结算失败': 'danger',
}
const statusClass = ref('default')

const formatTime = (t) => {
  if (!t) return '-'
  if (typeof t === 'number') {
    const d = new Date(t)
    if (isNaN(d.getTime())) return '-'
    const Y = d.getFullYear()
    const M = String(d.getMonth() + 1).padStart(2, '0')
    const D = String(d.getDate()).padStart(2, '0')
    const h = String(d.getHours()).padStart(2, '0')
    const m = String(d.getMinutes()).padStart(2, '0')
    return `${Y}-${M}-${D} ${h}:${m}`
  }
  return String(t).replace('T', ' ').substring(0, 16)
}

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await getSettlementDetail(route.params.id)
    detail.value = res.data || {}
    statusClass.value = statusClassMap[detail.value.status] ?? 'default'
  } catch (err) {
    console.warn('[SettlementDetail] 加载失败:', err.message)
    ElMessage.error('加载结算详情失败')
  } finally {
    loading.value = false
  }
}

const handleSettle = async () => {
  try {
    await ElMessageBox.confirm(`确认对结算单 ${detail.value.id} 进行支付结算？`, '结算确认', {
      confirmButtonText: '确认结算',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await settlePay(detail.value.id)
    ElMessage.success('结算成功')
    loadDetail()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('结算失败，请重试')
  }
}

onMounted(loadDetail)
</script>
