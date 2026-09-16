<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">积分管理</h1>
      <p class="page-desc">平台积分售卖与老板购买订单管理，支持代客下单，1 元 = 100 积分</p>
    </div>

    <div class="stat-cards">
      <div v-for="card in statCards" :key="card.label" class="stat-card">
        <div class="stat-card-header"><span class="stat-card-title">{{ card.label }}</span><div :class="['stat-card-icon', card.color]"><i :class="['fas', card.icon]"></i></div></div>
        <div class="stat-card-value">{{ card.value }}</div>
        <div class="stat-card-change" :class="card.change >= 0 ? 'up' : 'down'"><i :class="['fas', card.change >= 0 ? 'fa-arrow-up' : 'fa-arrow-down']"></i><span>{{ card.compareLabel }} {{ card.change >= 0 ? '+' : '' }}{{ card.change }}%</span></div>
      </div>
    </div>

    <div class="card">
      <div class="filter-bar">
        <div class="filter-item"><span>关键词</span><el-input v-model="filters.keyword" placeholder="企业名称/老板ID/订单号" clearable style="width: 220px" prefix-icon="Search" @keyup.enter="search" /></div>
        <div class="filter-item"><span>支付方式</span><el-select v-model="filters.payMethod" placeholder="全部" clearable style="width: 130px"><el-option v-for="v in payMethods" :key="v" :label="v" :value="v" /></el-select></div>
        <div class="filter-item"><span>订单状态</span><el-select v-model="filters.status" placeholder="全部" clearable style="width: 130px"><el-option v-for="v in statuses" :key="v" :label="v" :value="v" /></el-select></div>
        <div class="filter-item"><span>购买日期</span><el-date-picker v-model="filters.date" type="date" value-format="YYYY-MM-DD" placeholder="购买日期" style="width: 150px" /></div>
        <button class="btn btn-primary btn-sm" @click="search"><i class="fas fa-search"></i> 查询</button>
        <button class="btn btn-outline btn-sm" @click="reset"><i class="fas fa-rotate-left"></i> 重置</button>
        <div class="filter-spacer"></div>
        <button class="btn btn-outline btn-sm" @click="exportOrders"><i class="fas fa-download"></i> 导出数据</button>
        <button class="btn btn-primary btn-sm" @click="openDrawer"><i class="fas fa-plus"></i> 购买积分</button>
      </div>

      <el-table :data="orders" stripe :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }">
        <el-table-column prop="orderNo" label="订单号" min-width="150"><template #default="{ row }"><span class="order-no">{{ row.orderNo }}</span></template></el-table-column>
        <el-table-column label="购买老板" min-width="160"><template #default="{ row }"><div class="boss-cell"><span class="mini-avatar" :style="{ background: row.color }">{{ (row.bossName || '?').slice(0, 1) }}</span><div><div>{{ row.bossName || '-' }}</div><small>ID: {{ row.bossId || '-' }}</small></div></div></template></el-table-column>
        <el-table-column prop="companyName" label="企业名称" min-width="170" show-overflow-tooltip />
        <el-table-column prop="points" label="购买积分" width="110"><template #default="{ row }"><span class="points-val">{{ number(row.points) }}</span></template></el-table-column>
        <el-table-column prop="amount" label="实付金额" width="110"><template #default="{ row }"><span class="money-val">¥{{ money(row.amount) }}</span></template></el-table-column>
        <el-table-column label="单价" width="80">0.01元</el-table-column>
        <el-table-column prop="payMethod" label="支付方式" width="110"><template #default="{ row }"><span :class="['pay-tag', payClass(row.payMethod)]"><i :class="payIcon(row.payMethod)"></i>{{ row.payMethod || '-' }}</span></template></el-table-column>
        <el-table-column label="状态" width="100"><template #default="{ row }"><span :class="['status-badge', statusClass(row.status)]">{{ row.status || '-' }}</span></template></el-table-column>
        <el-table-column prop="purchaseTime" label="购买时间" min-width="155"><template #default="{ row }">{{ formatTime(row.purchaseTime) }}</template></el-table-column>
        <el-table-column label="操作" width="80" fixed="right"><template #default="{ row }"><el-button link type="primary" size="small" @click="showDetail(row)">详情</el-button></template></el-table-column>
      </el-table>
      <div class="pagination"><div class="pagination-info">共 {{ total }} 条记录</div><el-pagination v-model:current-page="page" v-model:page-size="size" :page-sizes="[10, 20, 50, 100]" :total="total" layout="sizes, prev, pager, next, jumper" background @size-change="load" @current-change="load" /></div>
    </div>

    <Teleport to="body">
      <Transition name="modal-fade">
        <div v-if="detailVisible" class="modal-overlay" @click.self="detailVisible = false">
          <div v-if="current" class="purchase-modal">
            <div class="modal-header">
              <div class="modal-title">购买订单详情</div>
              <button class="modal-close" type="button" aria-label="关闭" @click="detailVisible = false"><i class="fas fa-times"></i></button>
            </div>
            <div class="modal-body">
              <div class="d-section-title">订单信息</div>
              <div class="d-grid d-section-grid"><div class="d-item"><span>订单号</span><b class="order-no">{{ current.orderNo }}</b></div><div class="d-item"><span>订单状态</span><b><span :class="['status-badge', statusClass(current.status)]">{{ current.status }}</span></b></div><div class="d-item"><span>购买时间</span><b>{{ formatTime(current.purchaseTime) }}</b></div><div class="d-item"><span>支付方式</span><b>{{ current.payMethod }}</b></div></div>
              <div class="d-section-title">购买信息</div>
              <div class="d-grid d-section-grid"><div class="d-item"><span>购买老板</span><b>{{ current.bossName }}（{{ current.bossId }}）</b></div><div class="d-item"><span>企业名称</span><b>{{ current.companyName }}</b></div><div class="d-item"><span>购买积分</span><b class="points-val">{{ number(current.points) }} 积分</b></div><div class="d-item"><span>兑换单价</span><b>0.01 元/积分（1元=100积分）</b></div></div>
              <div class="d-section-title">金额信息</div>
              <div class="d-grid"><div class="d-item"><span>实付金额</span><b class="money-val">¥{{ currency(current.amount) }}</b></div><div class="d-item"><span>积分到账</span><b class="arrival">{{ current.status === '支付成功' ? '已实时到账' : '待支付' }}</b></div></div>
            </div>
            <div class="modal-footer"><button class="btn btn-outline" @click="detailVisible = false">关闭</button></div>
          </div>
        </div>
      </Transition>

      <div :class="['drawer-mask', { open: drawerVisible }]" @click="drawerVisible = false"></div>
      <aside :class="['purchase-drawer-panel', { open: drawerVisible }]" aria-label="购买积分（代客下单）">
        <div class="drawer-header"><div class="drawer-title"><i class="fas fa-coins"></i>购买积分（代客下单）</div><button class="drawer-close" type="button" aria-label="关闭" @click="drawerVisible = false"><i class="fas fa-times"></i></button></div>
        <div class="drawer-body">
          <div class="form-block"><div class="form-block-title">购买老板 <span>*</span></div><el-select v-model="form.bossId" placeholder="请选择购买老板" filterable style="width:100%"><el-option v-for="b in bosses" :key="b.id" :label="`${b.name || b.nickname || b.username}（${b.id}） · ${b.companyName || b.company || ''}`" :value="b.id" /></el-select></div>
          <div class="form-block"><div class="form-block-title">积分套餐</div><div class="pkg-grid"><div v-for="pkg in packages" :key="pkg" :class="['pkg-card', { active: form.points === pkg }]" @click="selectPackage(pkg)"><em v-if="packageTag(pkg)">{{ packageTag(pkg) }}</em><div class="pkg-points">{{ packageCount(pkg) }}<small>万</small></div><div class="pkg-money">¥{{ currency(pkg / 100) }}</div></div></div><div class="custom-points"><el-input v-model="customPoints" placeholder="自定义积分数量（100的整数倍）" inputmode="numeric" @input="onCustomPoints" /><span class="unit">积分</span></div></div>
          <div class="form-block"><div class="form-block-title">支付方式 <span>*</span></div><div class="pick-cards"><div v-for="v in payMethods" :key="v" :class="['pick-card', { active: form.payMethod === v }]" @click="form.payMethod = v"><i :class="payIcon(v)"></i>{{ v }}</div></div></div>
          <div class="form-block"><div class="form-block-title">订单处理 <span>*</span></div><div class="pick-cards deal-cards"><div :class="['pick-card', { active: form.deal === 'paid' }]" @click="form.deal = 'paid'"><i class="fas fa-circle-check"></i>确认收款并发放</div><div :class="['pick-card', { active: form.deal === 'pending' }]" @click="form.deal = 'pending'"><i class="fas fa-clock"></i>仅生成待支付订单</div></div><div class="form-tip"><i class="fas fa-circle-info"></i> 选择“确认收款”后积分将实时到账老板账户；“待支付”订单需老板后续完成付款。</div></div>
          <div class="form-block"><div class="form-block-title">订单金额</div><div class="calc-box"><div><span>兑换单价</span><b>0.01 元/积分（1元 = 100积分）</b></div><div><span>购买积分</span><b class="points-val">{{ number(form.points) }} 积分</b></div><div><span>手续费</span><b class="fee-free">¥0.00（平台免收）</b></div><div class="calc-total"><span>应付金额</span><strong><small>¥</small>{{ currency(form.points / 100) }}</strong></div></div></div>
          <div class="form-block"><div class="form-block-title">备注</div><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="选填，如：线下转账代录入、活动赠送等" /></div>
        </div>
        <div class="drawer-footer"><button class="btn btn-outline" @click="drawerVisible = false">取消</button><button class="btn btn-primary" :disabled="submitting" @click="submit"><i class="fas fa-check"></i>{{ submitting ? '提交中...' : '确认购买' }}</button></div>
      </aside>
    </Teleport>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listBosses } from '@/api/user'
import { createPointPurchaseOrder, exportPointPurchaseOrders, getPointPurchaseStats, listPointPurchaseOrders } from '@/api/pointPurchase'

const payMethods = ['微信支付', '支付宝', '对公转账']
const statuses = ['支付成功', '待支付', '已退款']
const packages = [10000, 50000, 100000, 500000, 1000000, 5000000]
const filters = reactive({ keyword: '', payMethod: '', status: '', date: '' })
const orders = ref([]); const bosses = ref([]); const total = ref(0); const page = ref(1); const size = ref(10); const stats = reactive({ todayCount: 0, todayPoints: 0, monthPoints: 0, monthAmount: 0, changes: {} })
const drawerVisible = ref(false); const detailVisible = ref(false); const current = ref(null); const customPoints = ref(''); const submitting = ref(false)
const form = reactive({ bossId: '', points: 0, payMethod: '微信支付', deal: 'paid', remark: '' })
const statCards = computed(() => [{ label: '今日购买笔数', value: number(stats.todayCount), icon: 'fa-file-invoice-dollar', color: '' , change: stats.changes.todayCount || 0, compareLabel: '较昨日' }, { label: '今日售出积分', value: number(stats.todayPoints), icon: 'fa-coins', color: 'purple', change: stats.changes.todayPoints || 0, compareLabel: '较昨日' }, { label: '本月售出积分', value: number(stats.monthPoints), icon: 'fa-chart-line', color: 'purple', change: stats.changes.monthPoints || 0, compareLabel: '较上月' }, { label: '本月收入金额', value: `¥${money(stats.monthAmount)}`, icon: 'fa-sack-dollar', color: 'green', change: stats.changes.monthAmount || 0, compareLabel: '较上月' }])
const number = v => Number(v || 0).toLocaleString('zh-CN')
const money = v => Number(v || 0).toFixed(2)
const currency = v => Number(v || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
const formatTime = v => { if (!v) return '-'; const d = typeof v === 'number' ? new Date(v) : null; if (d) return isNaN(d.getTime()) ? '-' : d.toLocaleString('zh-CN', { hour12: false }).replaceAll('/', '-'); return String(v).replace('T', ' ').slice(0, 19) }
const statusClass = s => ({ '支付成功': 'success', '待支付': 'warning', '已退款': 'default' }[s] || 'default')
const payClass = v => ({ '微信支付': 'pay-wechat', '支付宝': 'pay-alipay', '对公转账': 'pay-bank' }[v] || '')
const payIcon = v => ({ '微信支付': 'fab fa-weixin', '支付宝': 'fab fa-alipay', '对公转账': 'fas fa-building-columns' }[v] || 'fas fa-wallet')
const packageCount = v => v / 10000
const packageTag = v => ({ 500000: '常用', 5000000: '企业推荐' }[v] || '')
const normalize = o => ({ ...o, orderNo: o.orderNo || o.no, bossId: o.bossId || o.bid, bossName: o.bossName || o.boss, companyName: o.companyName || o.company, points: o.points || 0, amount: o.amount ?? o.money ?? 0, payMethod: o.payMethod || o.pay, purchaseTime: o.purchaseTime || o.time, color: o.color || '#7C3AED' })
async function load () { try { const [listRes, statRes] = await Promise.all([listPointPurchaseOrders({ ...filters, page: page.value - 1, size: size.value }), getPointPurchaseStats(filters.date ? { date: filters.date } : {})]); const d = listRes.data; const list = Array.isArray(d) ? d : (d?.content || d?.list || []); orders.value = list.map(normalize); total.value = listRes.total ?? d?.totalElements ?? d?.total ?? list.length; Object.assign(stats, statRes.data || {}) } catch (e) { orders.value = []; total.value = 0; ElMessage.error('加载积分订单失败') } }
async function loadBosses () { try { const r = await listBosses({ page: 0, size: 100 }); const d = r.data; bosses.value = (Array.isArray(d) ? d : (d?.content || d?.list || [])).map(b => ({ ...b, name: b.name || b.nickname || b.username })) } catch { bosses.value = [] } }
function search () { page.value = 1; load() }; function reset () { Object.assign(filters, { keyword: '', payMethod: '', status: '', date: '' }); search() }
function showDetail (row) { current.value = row; detailVisible.value = true }
function selectPackage (points) { form.points = points; customPoints.value = number(points) }
function onCustomPoints (v) { const raw = String(v).replace(/\D/g, ''); form.points = raw ? Number(raw) : 0; customPoints.value = raw ? number(form.points) : '' }
function openDrawer () { Object.assign(form, { bossId: '', points: 0, payMethod: '微信支付', deal: 'paid', remark: '' }); customPoints.value = ''; drawerVisible.value = true; if (!bosses.value.length) loadBosses() }
async function submit () { if (!form.bossId) return ElMessage.warning('请选择购买老板'); if (!form.points || form.points < 100 || form.points % 100) return ElMessage.warning('购买积分须为 100 的整数倍且不少于 100'); submitting.value = true; try { await createPointPurchaseOrder({ ...form, amount: form.points / 100 }); ElMessage.success(form.deal === 'paid' ? '购买成功，积分已发放' : '已生成待支付订单'); drawerVisible.value = false; load() } catch { ElMessage.error('创建积分订单失败') } finally { submitting.value = false } }
async function exportOrders () { try { const blob = await exportPointPurchaseOrders({ ...filters }); const url = URL.createObjectURL(blob); const a = document.createElement('a'); a.href = url; a.download = '积分购买订单.xlsx'; a.click(); URL.revokeObjectURL(url) } catch { ElMessage.error('导出失败') } }
onMounted(() => { load(); loadBosses() })
</script>

<style scoped>
.filter-bar { display:flex; align-items:center; gap:10px; flex-wrap:wrap; }
.filter-item { display:flex; align-items:center; gap:8px; }
.filter-item > span { color:var(--text-secondary); font-size:13px; white-space:nowrap; }
.filter-spacer { flex:1; }
.boss-cell { display:flex; align-items:center; gap:10px; }.boss-cell small { display:block; margin-top:2px; color:var(--text-muted); font-size:12px; }.mini-avatar { width:32px; height:32px; display:inline-flex; align-items:center; justify-content:center; flex-shrink:0; border-radius:8px; color:#fff; font-weight:600; }.order-no { color:var(--primary); font-family:monospace; font-size:12px; }.points-val { color:#9333EA; font-weight:600; }.money-val { color:var(--danger); font-weight:700; }.down { color:var(--danger) !important; }
.pay-tag { display:inline-flex; align-items:center; gap:5px; color:#4b5563; font-size:12px; white-space:nowrap; }.pay-tag i { font-size:14px; }.pay-wechat i { color:#07c160; }.pay-alipay i { color:#1677ff; }.pay-bank i { color:#6b7280; }
.modal-overlay { position:fixed; inset:0; z-index:1000; display:flex; align-items:center; justify-content:center; padding:24px; background:rgba(0,0,0,.5); }
.purchase-modal { width:90%; max-width:600px; max-height:90vh; overflow:hidden; background:#fff; border-radius:16px; box-shadow:0 20px 50px rgba(0,0,0,.18); }
.modal-header { display:flex; align-items:center; justify-content:space-between; padding:20px 24px; border-bottom:1px solid var(--border); }
.modal-title { color:var(--text-primary); font-size:18px; font-weight:600; }
.modal-close,.drawer-close { padding:0; border:0; background:transparent; }
.modal-close { width:28px; height:28px; display:flex; align-items:center; justify-content:center; border-radius:6px; color:var(--text-muted); cursor:pointer; }.modal-close:hover { color:var(--text-primary); background:var(--bg-page); }
.modal-body { max-height:60vh; overflow-y:auto; padding:24px; }.modal-footer { display:flex; justify-content:flex-end; gap:12px; padding:16px 24px; border-top:1px solid var(--border); }
.modal-fade-enter-active,.modal-fade-leave-active { transition:opacity .2s ease; }.modal-fade-enter-active .purchase-modal,.modal-fade-leave-active .purchase-modal { transition:transform .2s ease; }.modal-fade-enter-from,.modal-fade-leave-to { opacity:0; }.modal-fade-enter-from .purchase-modal,.modal-fade-leave-to .purchase-modal { transform:translateY(8px) scale(.98); }
.d-section-title { margin:0 0 12px; padding-bottom:8px; color:var(--text-primary); font-size:14px; font-weight:600; border-bottom:1px solid var(--border); }.d-section-grid { margin-bottom:20px; }.d-grid { display:grid; grid-template-columns:repeat(2,1fr); gap:14px 24px; }.d-item { display:flex; flex-direction:column; gap:4px; min-width:0; }.d-item > span { color:var(--text-muted); font-size:12px; }.d-item > b { color:var(--text-primary); font-size:14px; font-weight:500; overflow-wrap:anywhere; }.d-item > b.arrival { color:var(--success); }
.drawer-mask { position:fixed; inset:0; z-index:1000; visibility:hidden; opacity:0; background:rgba(0,0,0,.45); transition:opacity .25s,visibility .25s; }.drawer-mask.open { visibility:visible; opacity:1; }
.purchase-drawer-panel { position:fixed; top:0; right:0; bottom:0; z-index:1001; width:520px; max-width:92vw; display:flex; flex-direction:column; background:#fff; box-shadow:-8px 0 30px rgba(0,0,0,.12); transform:translateX(105%); transition:transform .28s ease; }.purchase-drawer-panel.open { transform:translateX(0); }
.drawer-header { display:flex; align-items:center; justify-content:space-between; flex-shrink:0; padding:18px 24px; border-bottom:1px solid var(--border); }.drawer-body { flex:1; overflow-y:auto; padding:20px 24px; }.drawer-footer { display:flex; flex-shrink:0; gap:12px; padding:14px 24px; border-top:1px solid var(--border); }.drawer-footer .btn { flex:1; justify-content:center; }
.drawer-title { display:flex; align-items:center; gap:8px; color:var(--text-primary); font-size:16px; font-weight:600; }.drawer-title i { color:var(--primary); }
.drawer-close { width:30px; height:30px; display:flex; align-items:center; justify-content:center; border-radius:6px; color:var(--text-muted); cursor:pointer; }.drawer-close:hover { color:var(--text-primary); background:var(--bg-page); }
.form-block { margin-bottom:22px; }.form-block-title { display:flex; align-items:center; gap:6px; margin-bottom:10px; color:var(--text-primary); font-size:13px; font-weight:600; }.form-block-title::before { content:''; width:3px; height:13px; border-radius:2px; background:var(--primary); }.form-block-title span { color:var(--danger); }
.pkg-grid { display:grid; grid-template-columns:repeat(3,1fr); gap:10px; }.pkg-card { position:relative; padding:12px 8px; text-align:center; cursor:pointer; background:#fff; border:1.5px solid var(--border); border-radius:10px; transition:.15s; }.pkg-card:hover { border-color:#ffb98a; }.pkg-card.active { border-color:#9333ea; background:#faf5ff; }.pkg-points { color:#9333ea; font-size:15px; font-weight:700; font-variant-numeric:tabular-nums; }.pkg-points small { font-size:11px; font-weight:500; }.pkg-money { margin-top:3px; color:var(--text-muted); font-size:12px; }.pkg-card.active .pkg-money { color:#9333ea; }.pkg-card em { position:absolute; top:-7px; right:-4px; padding:1px 7px; color:#fff; font-size:10px; font-style:normal; font-weight:600; background:linear-gradient(135deg,#ff6b35,#ff8c5a); border-radius:8px; }
.custom-points { display:flex; align-items:center; gap:8px; margin-top:10px; }.custom-points :deep(.el-input) { flex:1; }.custom-points .unit { color:var(--text-muted); font-size:13px; white-space:nowrap; }
.pick-cards { display:flex; gap:10px; }.pick-card { flex:1; padding:12px 10px; color:var(--text-secondary); font-size:13px; text-align:center; cursor:pointer; background:#fff; border:1.5px solid var(--border); border-radius:10px; transition:.15s; }.pick-card i { display:block; margin-bottom:6px; color:var(--text-muted); font-size:18px; }.pick-card.active { color:var(--primary); font-weight:600; background:#fff7f0; border-color:var(--primary); }.pick-card.active i { color:var(--primary); }.pick-card .fa-weixin { color:#07c160; }.pick-card .fa-alipay { color:#1677ff; }.pick-card .fa-building-columns { color:#6b7280; }
.calc-box { padding:14px 16px; border-radius:12px; background:linear-gradient(135deg,#faf5ff,#fff7f0); }.calc-box > div { display:flex; justify-content:space-between; align-items:center; padding:5px 0; color:var(--text-secondary); font-size:13px; }.calc-box b { color:var(--text-primary); font-weight:500; }.calc-box .fee-free { color:var(--success); }.calc-total { margin-top:6px; padding-top:10px !important; border-top:1px dashed #e9d5ff; }.calc-total span { color:var(--text-primary); font-weight:600; }.calc-total strong { color:var(--danger); font-size:24px; }.calc-total strong small { font-size:13px; }.form-tip { display:flex; align-items:flex-start; gap:5px; margin-top:8px; color:var(--text-muted); font-size:12px; line-height:1.5; }.form-tip i { margin-top:2px; color:#ffb98a; }
@media (max-width:640px) { .modal-overlay { padding:14px; }.purchase-modal { width:100%; }.d-grid { grid-template-columns:1fr; }.pick-cards { flex-direction:column; } }
</style>
