<template>
  <div>
    <div class="page-header">
      <div class="header-nav">
        <el-button text @click="router.back()"><i class="fas fa-arrow-left"></i></el-button>
        <div>
          <h1 class="page-title">老板详情</h1>
          <p class="page-desc">雇主基本信息、经营数据、认证资质、积分 / 优惠券 / 奖励金资产</p>
        </div>
      </div>
    </div>

    <div v-loading="loading" class="detail-page">
      <div class="card">
        <div class="detail-header">
          <div class="detail-avatar">{{ avatarText }}</div>
          <div class="detail-main">
            <div class="detail-name">
              {{ displayName }}
              <span class="auth-badge">企业认证</span>
              <span class="status-badge" :class="certClass">{{ certStatusText }}</span>
            </div>
            <div class="detail-sub">
              雇主ID：{{ employerId }} · {{ maskPhone(detail.phone) }} · 注册时间 {{ formatDate(detail.timestamp) }}
            </div>
          </div>
          <div class="header-stats">
            <div class="header-stat">
              <div class="hs-label">余额</div>
              <div class="hs-value">{{ formatMoney(detail.balance) }}</div>
            </div>
            <div class="header-stat-divider"></div>
            <div class="header-stat accent">
              <div class="hs-label">积分余额</div>
              <div class="hs-value">{{ formatNumber(detail.points) }}</div>
            </div>
            <div class="header-stat-divider"></div>
            <div class="header-stat">
              <div class="hs-label">奖励金余额</div>
              <div class="hs-value">{{ formatMoney(detail.rewardAmount) }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header"><span class="card-title">基本信息</span></div>
        <div class="info-grid">
          <div class="info-item"><span class="info-label">雇主ID</span><span class="info-value">{{ employerId }}</span></div>
          <div class="info-item"><span class="info-label">手机号</span><span class="info-value">{{ maskPhone(detail.phone) }}</span></div>
          <div class="info-item"><span class="info-label">企业名称</span><span class="info-value">{{ detail.companyName || '-' }}</span></div>
          <div class="info-item"><span class="info-label">行业类型</span><span class="info-value">{{ detail.industry || '-' }}</span></div>
          <div class="info-item"><span class="info-label">联系人</span><span class="info-value">{{ contactText }}</span></div>
          <div class="info-item"><span class="info-label">企业规模</span><span class="info-value">{{ detail.companyScale || '-' }}</span></div>
          <div class="info-item"><span class="info-label">所在地区</span><span class="info-value">{{ detail.city || '-' }}</span></div>
          <div class="info-item">
            <span class="info-label">账户状态</span>
            <span class="info-value" :class="isNormal(detail.status) ? 'text-success' : 'text-danger'">{{ formatStatus(detail.status) }}</span>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header"><span class="card-title">经营数据</span></div>
        <div class="operation-grid">
          <div class="operation-stat"><div class="v">{{ detail.creditScore ?? 0 }}</div><div class="l">信用分</div></div>
          <div class="operation-stat"><div class="v">{{ formatNumber(detail.jobsCount) }}<span class="unit">次</span></div><div class="l">累计招工</div></div>
          <div class="operation-stat accent"><div class="v">{{ formatMoney(detail.balance) }}</div><div class="l">账户余额</div></div>
          <div class="operation-stat good"><div class="v">{{ formatNumber(detail.points) }}</div><div class="l">积分余额</div></div>
          <div class="operation-stat"><div class="v">{{ formatMoney(detail.rewardAmount) }}</div><div class="l">奖励金余额</div></div>
          <div class="operation-stat"><div class="v">{{ couponCounts.available }}<span class="unit">张</span></div><div class="l">可用优惠券</div></div>
          <div class="operation-stat"><div class="v">{{ enterpriseStatusText }}</div><div class="l">企业认证</div></div>
          <div class="operation-stat"><div class="v">{{ realnameStatusText }}</div><div class="l">实名认证</div></div>
        </div>
      </div>

      <div class="card">
        <div class="card-header"><span class="card-title">认证资质</span></div>
        <div class="certificate-list">
          <div class="certificate-item">
            <div class="cert-info"><div class="cert-icon"><i class="fas fa-file-alt"></i></div><div><div class="cert-name">营业执照</div><div class="cert-time">执照号：{{ detail.licenseNo || '-' }}</div></div></div>
            <span class="status-badge" :class="certClass">{{ certStatusText }}</span>
          </div>
          <div class="certificate-item">
            <div class="cert-info"><div class="cert-icon"><i class="fas fa-id-card"></i></div><div><div class="cert-name">法定代表人</div><div class="cert-time">{{ detail.legalRep || '-' }}</div></div></div>
            <span class="status-badge" :class="certClass">{{ certStatusText }}</span>
          </div>
          <div class="certificate-item">
            <div class="cert-info"><div class="cert-icon"><i class="fas fa-building"></i></div><div><div class="cert-name">企业认证</div><div class="cert-time">统一编号：{{ detail.companyCode || '-' }}</div></div></div>
            <span class="status-badge" :class="certClass">{{ enterpriseStatusText }}</span>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="asset-header">
          <div class="card-title"><i class="fas fa-coins coin-icon"></i>积分明细</div>
          <div class="asset-summary"><span class="summary-primary">{{ formatNumber(detail.points) }}</span><span class="summary-label">当前积分总数</span></div>
        </div>
        <el-table :data="pointRecords" empty-text="暂无积分明细">
          <el-table-column prop="id" label="积分ID" width="100" />
          <el-table-column prop="type" label="类型" width="130">
            <template #default="{ row }">{{ pointTypeText(row.type) }}</template>
          </el-table-column>
          <el-table-column label="数量变动" width="110" align="right">
            <template #default="{ row }"><span :class="Number(row.points) >= 0 ? 'text-success' : 'text-danger'">{{ formatChange(row.points) }}</span></template>
          </el-table-column>
          <el-table-column prop="balanceAfter" label="变动后余额" width="120" align="right" />
          <el-table-column label="时间" width="170">
            <template #default="{ row }">{{ formatDateTime(row.time) }}</template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="220" show-overflow-tooltip />
        </el-table>
        <div class="table-pagination">
          <span>共 {{ pointPagination.total }} 条记录</span>
          <el-pagination
            v-model:current-page="pointPagination.page"
            v-model:page-size="pointPagination.size"
            layout="prev, pager, next"
            :page-size="pointPagination.size"
            :total="pointPagination.total"
            background
            @current-change="loadPointRecords"
          />
        </div>
      </div>

      <div class="card">
        <div class="tabs-bar">
          <button class="tab-btn" :class="{ active: couponTab === 'available' }" @click="couponTab = 'available'">
            可用优惠券 <span class="tab-count">{{ couponCounts.available }} 张</span>
          </button>
          <button class="tab-btn" :class="{ active: couponTab === 'history' }" @click="couponTab = 'history'">
            历史使用记录 <span class="tab-count">{{ couponCounts.history }} 张</span>
          </button>
        </div>
        <div v-if="couponRecords.length" class="coupon-list">
          <div v-for="coupon in couponRecords" :key="coupon.id" class="coupon-item">
            <div class="coupon-amount">
              <div class="num">{{ couponText(coupon) }}</div>
              <div class="cond">{{ couponCondition(coupon) }}</div>
            </div>
            <div class="coupon-info">
              <div class="coupon-name">{{ coupon.title }}</div>
              <div class="coupon-meta">{{ couponMeta(coupon) }}</div>
            </div>
            <div class="coupon-status"><span class="status-badge" :class="couponClass(coupon)">{{ couponStatus(coupon) }}</span></div>
          </div>
        </div>
        <el-empty v-else description="暂无优惠券记录" :image-size="72" />
        <div class="table-pagination">
          <span>共 {{ couponPagination.total }} 条记录</span>
          <el-pagination
            v-model:current-page="couponPagination.page"
            v-model:page-size="couponPagination.size"
            layout="prev, pager, next"
            :page-size="couponPagination.size"
            :total="couponPagination.total"
            background
            @current-change="loadCouponRecords"
          />
        </div>
      </div>

      <div class="card">
        <div class="asset-header">
          <div class="card-title"><i class="fas fa-gift gift-icon"></i>奖励金明细</div>
          <div class="asset-summary-group">
            <div class="asset-summary"><span class="summary-primary">{{ formatMoney(detail.rewardAmount) }}</span><span class="summary-label">当前余额</span></div>
            <div class="asset-summary"><span class="summary-success">{{ formatMoney(detail.rewardIncome) }}</span><span class="summary-label">累计获得</span></div>
            <div class="asset-summary"><span class="summary-danger">{{ formatMoney(detail.rewardUsed) }}</span><span class="summary-label">累计消耗 / 提现</span></div>
          </div>
        </div>
        <el-table :data="rewardRecords" empty-text="暂无奖励金明细">
          <el-table-column prop="id" label="流水ID" width="100" />
          <el-table-column prop="type" label="类型" width="100" />
          <el-table-column label="金额" width="110" align="right">
            <template #default="{ row }">{{ formatMoney(row.amount) }}</template>
          </el-table-column>
          <el-table-column label="变动后余额" width="120" align="right">
            <template #default="{ row }">{{ formatMoney(row.balanceAfter) }}</template>
          </el-table-column>
          <el-table-column label="时间" width="170">
            <template #default="{ row }">{{ formatDateTime(row.time) }}</template>
          </el-table-column>
          <el-table-column label="说明" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">{{ row.title || row.remark || '-' }}</template>
          </el-table-column>
        </el-table>
        <div class="table-pagination">
          <span>共 {{ rewardPagination.total }} 条记录</span>
          <el-pagination
            v-model:current-page="rewardPagination.page"
            v-model:page-size="rewardPagination.size"
            layout="prev, pager, next"
            :page-size="rewardPagination.size"
            :total="rewardPagination.total"
            background
            @current-change="loadRewardRecords"
          />
        </div>
      </div>

      <div class="card action-card">
        <el-button v-if="isNormal(detail.status)" type="warning" @click="handleFreeze">冻结账号</el-button>
        <el-button v-else type="success" @click="handleUnfreeze">解冻账号</el-button>
        <el-button @click="router.back()">返回列表</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  freezeUser,
  getBossCouponRecords,
  getBossPointRecords,
  getBossRewardRecords,
  getUser,
  unfreezeUser
} from '@/api/user'

const route = useRoute()
const router = useRouter()
const detail = ref({})
const loading = ref(true)
const couponTab = ref('available')
const pointRecords = ref([])
const rewardRecords = ref([])
const couponRecords = ref([])
const pointPagination = ref({ page: 1, size: 5, total: 0 })
const rewardPagination = ref({ page: 1, size: 5, total: 0 })
const couponPagination = ref({ page: 1, size: 5, total: 0 })
const couponCounts = ref({ available: 0, history: 0 })

const displayName = computed(() => detail.value.companyName || detail.value.nickname || detail.value.username || '未知企业')
const avatarText = computed(() => String(displayName.value || '老').slice(0, 1))
const employerId = computed(() => detail.value.companyCode || `B${String(detail.value.id || 0).padStart(7, '0')}`)
const contactText = computed(() => {
  const name = detail.value.contact || detail.value.realName || detail.value.nickname || '-'
  const position = detail.value.contactPosition || detail.value.contactPhone || ''
  return position ? `${name}（${position}）` : name
})
const isApproved = computed(() => detail.value.enterpriseStatus === 'APPROVED' || detail.value.certStatus === '已通过' || detail.value.certStatus === '已认证')
const certStatusText = computed(() => {
  if (isApproved.value) return '已认证'
  if (detail.value.enterpriseStatus === 'PENDING' || detail.value.certStatus === '待审核') return '待审核'
  if (detail.value.enterpriseStatus === 'REJECTED' || detail.value.certStatus === '已拒绝') return '已拒绝'
  return '未认证'
})
const certClass = computed(() => {
  if (isApproved.value) return 'success'
  if (certStatusText.value === '待审核') return 'warning'
  if (certStatusText.value === '已拒绝') return 'danger'
  return 'default'
})
const enterpriseStatusText = computed(() => statusText(detail.value.enterpriseStatus))
const realnameStatusText = computed(() => statusText(detail.value.realnameStatus))
const assetRows = (result) => {
  const data = result?.data
  return Array.isArray(data) ? data : (data?.content || data?.list || [])
}

const assetTotal = (result) => Number(
  result?.total ?? result?.data?.totalElements ?? result?.data?.total ?? 0
)

const statusText = (status) => {
  if (status === 'APPROVED' || status === '已通过') return '已认证'
  if (status === 'PENDING' || status === '待审核') return '待审核'
  if (status === 'REJECTED' || status === '已拒绝') return '已拒绝'
  return status || '未认证'
}
const formatNumber = (value) => Number(value ?? 0).toLocaleString('zh-CN')
const formatMoney = (value) => `¥${(Number(value ?? 0) / 100).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
const formatChange = (value) => `${Number(value ?? 0) > 0 ? '+' : ''}${formatNumber(value)}`
const maskPhone = (phone) => {
  const value = String(phone || '')
  return /^(\d{3})\d{4}(\d{4})$/.test(value) ? value.replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2') : value || '-'
}
const formatDate = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? '-' : date.toLocaleDateString('zh-CN')
}
const formatDateTime = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? '-' : date.toLocaleString('zh-CN', { hour12: false })
}
const pointTypeText = (type) => {
  const mapping = { PURCHASE: '充值', ADMIN_PURCHASE: '充值', CONSUME: '消费', GIFT: '赠送', RECEIVED: '赠送', EXPIRED: '过期' }
  return mapping[type] || type || '-'
}
const couponText = (coupon) => (coupon.type === 'DISCOUNT' ? `${coupon.discount || '-'}折` : `¥${coupon.amount || '0'}`)
const couponCondition = (coupon) => (coupon.type === 'DISCOUNT' ? `封顶优惠¥${coupon.cap || '-'}` : `满¥${coupon.minSpend || '0'}元可用`)
const couponStatus = (coupon) => ({ UNUSED: '可使用', USED: '已使用', EXPIRED: '已过期' })[coupon.status] || coupon.status || '-'
const couponClass = (coupon) => (coupon.status === 'UNUSED' ? 'success' : 'default')
const couponMeta = (coupon) => (coupon.status === 'USED' && coupon.usedAt ? `使用时间 ${formatDateTime(coupon.usedAt)}` : `有效期至 ${formatDate(coupon.expireAt)}`)
const formatStatus = (status) => {
  if (status === '正常' || status === 'NORMAL') return '正常'
  if (status === '冻结' || status === 'FROZEN') return '冻结'
  return status || '-'
}
const isNormal = (status) => status === '正常' || status === 'NORMAL' || status === 1

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await getUser(route.params.id)
    detail.value = res?.data || res || {}
  } catch (e) {
    ElMessage.error('加载详情失败')
    console.warn('[BossDetail] 加载失败:', e)
  } finally {
    loading.value = false
  }
}

const loadPointRecords = async () => {
  try {
    const result = await getBossPointRecords(route.params.id, {
      page: pointPagination.value.page - 1,
      size: pointPagination.value.size
    })
    pointRecords.value = assetRows(result)
    pointPagination.value.total = assetTotal(result)
  } catch (e) {
    console.warn('[BossDetail] 加载积分明细失败:', e)
    pointRecords.value = []
    pointPagination.value.total = 0
  }
}

const loadRewardRecords = async () => {
  try {
    const result = await getBossRewardRecords(route.params.id, {
      page: rewardPagination.value.page - 1,
      size: rewardPagination.value.size
    })
    rewardRecords.value = assetRows(result)
    rewardPagination.value.total = assetTotal(result)
  } catch (e) {
    console.warn('[BossDetail] 加载奖励金明细失败:', e)
    rewardRecords.value = []
    rewardPagination.value.total = 0
  }
}

const loadCouponCounts = async () => {
  try {
    const [available, history] = await Promise.all([
      getBossCouponRecords(route.params.id, { status: 'AVAILABLE', page: 0, size: 1 }),
      getBossCouponRecords(route.params.id, { status: 'HISTORY', page: 0, size: 1 })
    ])
    couponCounts.value = { available: assetTotal(available), history: assetTotal(history) }
  } catch (e) {
    console.warn('[BossDetail] 加载优惠券数量失败:', e)
    couponCounts.value = { available: 0, history: 0 }
  }
}

const loadCouponRecords = async () => {
  try {
    const result = await getBossCouponRecords(route.params.id, {
      status: couponTab.value.toUpperCase(),
      page: couponPagination.value.page - 1,
      size: couponPagination.value.size
    })
    couponRecords.value = assetRows(result)
    couponPagination.value.total = assetTotal(result)
  } catch (e) {
    console.warn('[BossDetail] 加载优惠券失败:', e)
    couponRecords.value = []
    couponPagination.value.total = 0
  }
}

const handleFreeze = async () => {
  try {
    await ElMessageBox.confirm(`确定要冻结雇主「${displayName.value}」吗？`, '提示', { type: 'warning' })
    await freezeUser(detail.value.id)
    ElMessage.success('冻结成功')
    loadDetail()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('冻结失败')
  }
}
const handleUnfreeze = async () => {
  try {
    await ElMessageBox.confirm(`确定要解冻雇主「${displayName.value}」吗？`, '提示', { type: 'warning' })
    await unfreezeUser(detail.value.id)
    ElMessage.success('解冻成功')
    loadDetail()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('解冻失败')
  }
}

watch(couponTab, () => {
  couponPagination.value.page = 1
  loadCouponRecords()
})

onMounted(() => {
  loadDetail()
  loadPointRecords()
  loadRewardRecords()
  loadCouponCounts()
  loadCouponRecords()
})
</script>

<style scoped>
.header-nav { display: flex; align-items: center; gap: 12px; }
.detail-page { min-height: 320px; }
.detail-header { display: flex; align-items: center; gap: 16px; }
.detail-avatar { width: 56px; height: 56px; flex-shrink: 0; display: flex; align-items: center; justify-content: center; border-radius: 50%; background: linear-gradient(135deg,#FF8C42,#FF6B35); color: #fff; font-size: 22px; font-weight: 600; }
.detail-main { flex: 1; min-width: 0; }
.detail-name { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; color: #111827; font-size: 18px; font-weight: 700; }
.auth-badge { padding: 2px 8px; border-radius: 4px; background: #FFF7E6; color: #D97706; font-size: 11px; font-weight: 600; }
.detail-sub { margin-top: 4px; color: #9CA3AF; font-size: 13px; }
.header-stats { display: flex; align-items: center; gap: 24px; }
.header-stat { text-align: right; }
.hs-label { margin-bottom: 4px; color: #9CA3AF; font-size: 12px; }
.hs-value { color: #111827; font-size: 22px; font-weight: 700; line-height: 1.1; font-variant-numeric: tabular-nums; }
.header-stat.accent .hs-value { color: var(--primary); }
.header-stat-divider { width: 1px; height: 32px; background: #E5E7EB; }
.info-grid { display: grid; grid-template-columns: repeat(4,minmax(0,1fr)); gap: 14px 24px; }
.info-item { display: flex; flex-direction: column; gap: 4px; }
.info-label { color: #9CA3AF; font-size: 13px; }
.info-value { overflow: hidden; color: #111827; font-size: 14px; font-weight: 500; text-overflow: ellipsis; white-space: nowrap; }
.operation-grid { display: grid; grid-template-columns: repeat(4,minmax(0,1fr)); gap: 12px; }
.operation-stat { padding: 16px 18px; border: 1px solid #F1F2F5; border-radius: 10px; background: #F9FAFB; }
.operation-stat .v { color: #111827; font-size: 22px; font-weight: 700; line-height: 1.2; font-variant-numeric: tabular-nums; }
.operation-stat .unit { margin-left: 2px; color: #6B7280; font-size: 12px; font-weight: 500; }
.operation-stat .l { margin-top: 6px; color: #9CA3AF; font-size: 12px; }
.operation-stat.accent .v { color: var(--primary); }
.operation-stat.good .v { color: #059669; }
.certificate-list { display: flex; flex-direction: column; gap: 10px; }
.certificate-item { display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; border: 1px solid #E5E7EB; border-radius: 8px; }
.cert-info { display: flex; align-items: center; gap: 10px; min-width: 0; }
.cert-icon { width: 32px; height: 32px; flex-shrink: 0; display: flex; align-items: center; justify-content: center; border-radius: 8px; background: #FFF0EB; color: var(--primary); }
.cert-name { color: #111827; font-size: 13px; font-weight: 600; }
.cert-time { margin-top: 2px; overflow: hidden; color: #9CA3AF; font-size: 11px; text-overflow: ellipsis; white-space: nowrap; }
.asset-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.coin-icon { margin-right: 6px; color: #F59E0B; }
.gift-icon { margin-right: 6px; color: #EA580C; }
.asset-summary { display: flex; align-items: baseline; gap: 6px; }
.asset-summary-group { display: flex; align-items: center; gap: 20px; }
.summary-primary { color: var(--primary); font-size: 18px; font-weight: 700; }
.summary-success { color: #059669; font-size: 18px; font-weight: 700; }
.summary-danger { color: var(--danger,#DC2626); font-size: 18px; font-weight: 700; }
.summary-label { color: var(--text-secondary,#4B5563); font-size: 12px; }
.tabs-bar { display: flex; gap: 4px; margin-bottom: 16px; border-bottom: 1px solid #E5E7EB; }
.tab-btn { padding: 10px 18px; margin-bottom: -1px; border: 0; border-bottom: 2px solid transparent; background: transparent; color: var(--text-secondary,#4B5563); font-size: 14px; cursor: pointer; }
.tab-btn.active { color: var(--primary); border-bottom-color: var(--primary); font-weight: 600; }
.tab-count { margin-left: 5px; padding: 0 7px; border-radius: 10px; background: #F3F4F6; color: #9CA3AF; font-size: 12px; }
.tab-btn.active .tab-count { background: #FFF0EB; color: var(--primary); }
.coupon-list { display: flex; flex-direction: column; gap: 10px; }
.coupon-item { display: flex; align-items: center; gap: 14px; padding: 12px 14px; border: 1px solid #E5E7EB; border-radius: 8px; }
.coupon-amount { width: 110px; flex-shrink: 0; color: var(--primary); font-weight: 700; }
.coupon-amount .num { font-size: 20px; line-height: 1.2; }
.coupon-amount .cond { margin-top: 3px; color: #9CA3AF; font-size: 11px; font-weight: 400; }
.coupon-info { flex: 1; min-width: 0; }
.coupon-name { color: #111827; font-size: 14px; font-weight: 600; }
.coupon-meta { margin-top: 3px; color: #9CA3AF; font-size: 12px; }
.coupon-status { flex-shrink: 0; }
.table-pagination { display: flex; align-items: center; justify-content: flex-end; gap: 16px; padding-top: 12px; color: var(--text-secondary,#4B5563); font-size: 12px; }
.status-badge { display: inline-flex; align-items: center; gap: 4px; }
.status-badge.success { color: #059669; }
.status-badge.warning { color: #D97706; }
.status-badge.danger { color: #DC2626; }
.status-badge.default { color: #6B7280; }
.text-success { color: #10B981; }
.text-danger { color: #EF4444; }
.action-card { display: flex; justify-content: flex-end; gap: 12px; }
</style>
