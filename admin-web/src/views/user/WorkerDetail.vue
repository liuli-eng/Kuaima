<template>
  <div class="worker-detail-page">
    <div class="page-header"><h1 class="page-title">零工详情</h1><p class="page-desc">零工基本信息、技能数据、积分 / 奖励金资产</p></div>

    <div v-loading="loading" class="detail-content">
      <div class="card">
        <div class="detail-header">
          <div class="detail-avatar" :style="{ background: getAvatarColor(workerName) }">{{ getAvatarLetter(workerName) }}</div>
          <div>
            <div class="detail-name">{{ workerName }} <span class="auth-badge">个人授权</span><span :class="['status-badge', isVerified ? 'success' : 'default']">{{ realnameStatus }}</span></div>
            <div class="detail-sub">零工ID：{{ worker.id || '-' }} · {{ worker.phone || '-' }} · 注册时间 {{ formatDate(worker.createdAt || worker.date) }}</div>
          </div>
          <div class="header-stats">
            <div class="header-stat"><div class="hs-label">奖励金余额</div><div class="hs-value">{{ money(worker.rewardAmount ?? worker.rewardBalance) }}</div></div>
            <div class="header-stat-divider"></div>
            <div class="header-stat accent"><div class="hs-label">积分余额</div><div class="hs-value">{{ number(worker.points ?? worker.pointBalance) }}</div></div>
          </div>
        </div>
      </div>

      <div class="card section-card"><div class="card-header"><div class="card-title">基本信息</div></div><div class="info-grid">
        <Info label="零工ID" :value="worker.id" /><Info label="手机号" :value="worker.phone" /><Info label="实名状态" :value="realnameStatus" /><Info label="授权类型" value="个人授权" />
        <Info label="性别" :value="gender" /><Info label="所在地区" :value="worker.city" /><Info label="注册时间" :value="formatDate(worker.createdAt || worker.date)" /><Info label="账户状态" :value="statusText" :success="isNormal(worker.status)" />
      </div></div>

      <div class="card section-card"><div class="card-header"><div class="card-title">技能与数据</div></div>
        <div class="sd-block"><div class="sd-subtitle">擅长技能</div><div class="sd-skills"><span v-for="(skill, i) in skills" :key="skill" class="sd-skill"><b>{{ skill }}</b><span :class="['lv', { mid: i % 3 === 2 }]">{{ i % 3 === 2 ? '一般' : '熟练' }}</span><span class="cnt">{{ skillCount(skill) }} 次</span></span><span v-if="!skills.length" class="empty-text">暂无技能数据</span></div></div>
        <div class="sd-block"><div class="sd-subtitle">作业数据</div><div class="sd-stats">
          <Stat label="诚信分" :value="worker.creditScore ?? '-'" /><Stat label="累计完单" :value="worker.completedOrders ?? 0" unit="单" /><Stat label="本月完单" :value="worker.monthCompletedOrders ?? '-'" unit="单" /><Stat label="累计获得报酬" :value="incomeDisplay" unit="元" accent />
          <Stat label="完成率" :value="percent(worker.completionRate)" good /><Stat label="取消率" :value="percent(worker.cancellationRate)" /><Stat label="失约率" :value="percent(worker.noShowRate)" /><Stat label="早退率" :value="percent(worker.earlyLeaveRate)" />
        </div></div>
      </div>

      <div class="card section-card"><div class="card-header card-header-inline"><div class="card-title"><i class="fas fa-coins points-icon"></i>积分明细</div><Summary :items="pointsSummary" /></div><div class="table-empty">暂无积分流水</div></div>
      <div class="card section-card"><div class="card-header card-header-inline"><div class="card-title"><i class="fas fa-gift reward-icon"></i>奖励金明细</div><Summary :items="rewardSummary" money /></div><div class="table-empty">暂无奖励金流水</div></div>
    </div>

    <div class="page-actions"><button class="btn btn-outline" @click="router.back()">返回列表</button><button v-if="isNormal(worker.status)" class="btn btn-danger" @click="freeze">冻结账户</button><button v-else class="btn btn-success" @click="unfreeze">解冻账户</button></div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { freezeUser, getUser, unfreezeUser } from '@/api/user'

const route = useRoute(); const router = useRouter(); const loading = ref(false); const worker = reactive({})
const workerName = computed(() => worker.realName || worker.nickname || worker.username || worker.phone || '-')
const skills = computed(() => normalizeSkills(worker.skills))
const isVerified = computed(() => ['APPROVED', '已通过', '已认证', 'VERIFIED'].includes(worker.realnameStatus) || worker.certStatus === true || worker.certStatus === 1)
const realnameStatus = computed(() => isVerified.value ? '已认证' : ({ PENDING: '待审核', REJECTED: '已拒绝' }[worker.realnameStatus] || '未认证'))
const statusText = computed(() => isNormal(worker.status) ? '正常' : '冻结')
const gender = computed(() => ({ male: '男', M: '男', female: '女', F: '女' }[worker.gender] || worker.gender || '-'))
const incomeDisplay = computed(() => worker.totalIncome == null ? '-' : (Number(worker.totalIncome) / 100).toFixed(2))
const pointsSummary = [{ label: '当前积分总数', value: computed(() => number(worker.points ?? worker.pointBalance)) }, { label: '累计获得', value: '-' }, { label: '累计消费', value: '-' }]
const rewardSummary = [{ label: '奖励金总数（当前余额）', value: computed(() => money(worker.rewardAmount ?? worker.rewardBalance)) }, { label: '累计获得', value: '-' }, { label: '累计消耗 / 提现', value: '-' }]
const normalizeSkills = value => Array.isArray(value) ? value.filter(Boolean) : String(value || '').split(',').map(v => v.trim()).filter(Boolean)
const number = value => value == null || value === '' ? '-' : Number(value).toLocaleString('zh-CN')
const money = value => value == null || value === '' ? '-' : `¥${(Number(value) / 100).toFixed(2)}`
const percent = value => value == null || value === '' ? '-' : `${value}%`
const formatDate = value => value ? String(value).replace('T', ' ').slice(0, 10) : '-'
const skillCount = skill => worker.skillCounts?.[skill] ?? '-'
const isNormal = value => value === '正常' || value === 'NORMAL' || value === 1
const getAvatarLetter = value => String(value || '?').charAt(0)
const getAvatarColor = value => ['#FF6B35', '#2563EB', '#10B981', '#8B5CF6', '#F59E0B', '#EC4899'][String(value || '').length % 6]
const load = async () => { loading.value = true; try { Object.assign(worker, (await getUser(route.params.id)).data || {}) } catch { ElMessage.error('加载零工详情失败') } finally { loading.value = false } }
const freeze = async () => { try { await ElMessageBox.confirm(`确定要冻结用户 ${workerName.value} 吗？`, '提示', { type: 'warning' }); await freezeUser(worker.id); worker.status = '冻结'; ElMessage.success('冻结成功') } catch (e) { if (e !== 'cancel') ElMessage.error('冻结失败') } }
const unfreeze = async () => { try { await ElMessageBox.confirm(`确定要解冻用户 ${workerName.value} 吗？`, '提示', { type: 'warning' }); await unfreezeUser(worker.id); worker.status = '正常'; ElMessage.success('解冻成功') } catch (e) { if (e !== 'cancel') ElMessage.error('解冻失败') } }
onMounted(load)
</script>

<script>
export default { components: {
  Info: { props: ['label', 'value', 'success'], template: '<div class="info-item"><span class="info-label">{{ label }}</span><span :class="[\'info-value\', { success }]">{{ value || \'-\' }}</span></div>' },
  Stat: { props: ['label', 'value', 'unit', 'accent', 'good'], template: '<div :class="[\'sd-stat\', { accent, good }]" ><div class="v">{{ value }}<span v-if="unit" class="unit">{{ unit }}</span></div><div class="l">{{ label }}</div></div>' },
  Summary: { props: ['items', 'money'], template: '<div class="summary"><template v-for="(item, i) in items" :key="item.label"><div v-if="i" class="summary-divider"></div><div class="summary-item"><b :class="{ money }">{{ item.value && item.value.value !== undefined ? item.value.value : item.value }}</b><span>{{ item.label }}</span></div></template></div>' }
} }
</script>

<style scoped>
.detail-header{display:flex;align-items:center;gap:16px}.detail-avatar{width:56px;height:56px;border-radius:50%;display:flex;align-items:center;justify-content:center;color:#fff;font-size:22px;font-weight:600;flex-shrink:0}.detail-name{display:flex;align-items:center;gap:8px;flex-wrap:wrap;font-size:18px;font-weight:700}.detail-sub{margin-top:4px;color:var(--text-muted);font-size:13px}.auth-badge{padding:2px 8px;border-radius:4px;background:#FFF7E6;color:#D97706;font-size:11px;font-weight:600}.header-stats{display:flex;align-items:center;gap:24px;margin-left:auto}.header-stat{text-align:right}.hs-label{margin-bottom:4px;color:var(--text-muted);font-size:12px}.hs-value{color:var(--text-primary);font-size:22px;font-weight:700;line-height:1.1}.header-stat.accent .hs-value{color:var(--primary)}.header-stat-divider{width:1px;height:32px;background:var(--border)}.section-card{margin-top:16px}.info-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:14px 24px;margin-top:4px}.info-item{display:flex;flex-direction:column;gap:4px}.info-label{color:var(--text-muted);font-size:13px}.info-value{color:var(--text-primary);font-size:14px;font-weight:500}.info-value.success{color:var(--success)}.sd-block+.sd-block{margin-top:22px;padding-top:22px;border-top:1px solid var(--border)}.sd-subtitle{position:relative;margin-bottom:14px;padding-left:10px;color:#374151;font-size:13px;font-weight:600}.sd-subtitle:before{content:'';position:absolute;left:0;top:2px;bottom:2px;width:3px;border-radius:2px;background:linear-gradient(180deg,#FF8C42,#FF6B35)}.sd-skills{display:flex;flex-wrap:wrap;gap:10px}.sd-skill{display:inline-flex;align-items:center;gap:8px;padding:8px 14px;border:1px solid #EEF0F3;border-radius:999px;background:#F9FAFB;color:#374151;font-size:13px}.sd-skill b{color:#111827}.sd-skill .lv{padding:1px 8px;border-radius:999px;background:#ECFDF5;color:#059669;font-size:11px;font-weight:600}.sd-skill .lv.mid{background:#FFFBEB;color:#D97706}.sd-skill .cnt{color:var(--text-muted);font-size:12px}.sd-stats{display:grid;grid-template-columns:repeat(4,1fr);gap:12px}.sd-stat{padding:16px 18px;border:1px solid #F1F2F5;border-radius:10px;background:#F9FAFB}.sd-stat .v{color:#111827;font-size:22px;font-weight:700;line-height:1.2}.sd-stat .unit{margin-left:2px;color:#6B7280;font-size:12px;font-weight:500}.sd-stat .l{margin-top:6px;color:var(--text-muted);font-size:12px}.sd-stat.accent .v{color:var(--primary)}.sd-stat.good .v{color:#059669}.card-header-inline{justify-content:flex-start}.summary{display:flex;align-items:center;gap:20px;margin-left:24px}.summary-item{display:flex;align-items:baseline;gap:6px}.summary-item b{color:var(--primary);font-size:18px}.summary-item b.money{color:var(--primary)}.summary-item span{color:var(--text-secondary);font-size:12px}.summary-divider{width:1px;height:14px;background:#E5E7EB}.points-icon{margin-right:6px;color:#F59E0B}.reward-icon{margin-right:6px;color:#EA580C}.table-empty{padding:28px;text-align:center;color:var(--text-muted);font-size:13px}.page-actions{display:flex;justify-content:flex-end;gap:12px;margin-top:16px;padding-bottom:24px}@media(max-width:900px){.info-grid{grid-template-columns:repeat(2,1fr)}.sd-stats{grid-template-columns:repeat(2,1fr)}.header-stats{gap:12px}}@media(max-width:640px){.detail-header{align-items:flex-start;flex-wrap:wrap}.header-stats{width:100%;justify-content:flex-end}.info-grid,.sd-stats{grid-template-columns:1fr}.summary{gap:8px;margin-left:10px;flex-wrap:wrap}}
</style>
