<template>
  <div class="credit-page">
    <div class="page-header">
      <h1 class="page-title">信用分管理</h1>
      <p class="page-desc">管理平台老板与零工的信用分，支持调整与查看信用分明细</p>
    </div>

    <div class="card filter-card">
      <div class="filter-bar">
        <el-input v-model="filters.keyword" clearable placeholder="搜索 ID / 姓名 / 手机号" prefix-icon="Search" style="width:220px" @keyup.enter="search" />
        <el-select v-model="filters.role" clearable placeholder="全部角色" style="width:125px">
          <el-option label="零工" value="worker" /><el-option label="老板" value="boss" />
        </el-select>
        <el-select v-model="filters.level" clearable placeholder="全部等级" style="width:135px">
          <el-option label="优秀（≥90）" value="excellent" /><el-option label="良好（75-89）" value="good" />
          <el-option label="一般（60-74）" value="medium" /><el-option label="较差（<60）" value="low" />
        </el-select>
        <el-button type="primary" @click="search"><i class="fas fa-search"></i> 查询</el-button>
        <el-button @click="reset"><i class="fas fa-rotate-left"></i> 重置</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" stripe class="credit-table">
        <el-table-column prop="id" label="ID" min-width="110" />
        <el-table-column label="角色" width="90"><template #default="{ row }"><span :class="['role-tag', row.role]">{{ row.role === 'worker' ? '零工' : '老板' }}</span></template></el-table-column>
        <el-table-column label="姓名 / 账号" min-width="180"><template #default="{ row }"><div class="name-cell"><strong>{{ row.name }}</strong><small>{{ row.id }}</small></div></template></el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="135" />
        <el-table-column label="当前信用分" min-width="130"><template #default="{ row }"><div class="score-cell"><b :class="`credit-${row.level}`">{{ row.score }}</b><span class="credit-bar"><i :class="`bar-${row.level}`" :style="{ width: `${Math.max(0, Math.min(100, row.score))}%` }"></i></span></div></template></el-table-column>
        <el-table-column label="信用等级" width="100"><template #default="{ row }"><span :class="['level-text', `credit-${row.level}`]">{{ levelText(row.level) }}</span></template></el-table-column>
        <el-table-column label="累计加分" width="100"><template #default="{ row }"><span class="num-in">+{{ row.addTotal }}</span></template></el-table-column>
        <el-table-column label="累计扣分" width="100"><template #default="{ row }"><span class="num-out">-{{ row.subTotal }}</span></template></el-table-column>
        <el-table-column label="操作" width="170" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openAdjust(row)">调整</el-button><el-button link type="primary" @click="openDetail(row)">明细</el-button></template></el-table-column>
      </el-table>
      <div class="pagination"><span>共 {{ total }} 条记录</span><el-pagination v-model:current-page="page" v-model:page-size="size" :page-sizes="[10, 20, 50]" :total="total" layout="sizes, prev, pager, next, jumper" background @size-change="load" @current-change="load" /></div>
    </div>

    <el-dialog v-model="adjustVisible" title="调整信用分" width="480px">
      <div v-if="current" class="adjust-form">
        <div class="target-line"><b>{{ current.name }}</b><span>{{ current.id }}</span></div>
        <div class="current-score">当前信用分：<strong>{{ current.score }}</strong></div>
        <el-form label-width="90px"><el-form-item label="调整方向"><el-radio-group v-model="adjust.direction"><el-radio-button label="in">＋ 加分</el-radio-button><el-radio-button label="out">－ 减分</el-radio-button></el-radio-group></el-form-item><el-form-item label="调整分值"><el-input-number v-model="adjust.amount" :min="1" :max="100" /></el-form-item><el-form-item label="调整原因"><el-input v-model="adjust.reason" type="textarea" :rows="3" placeholder="将记录到信用分明细" /></el-form-item></el-form>
        <div class="after-score">调整后：<b :class="adjust.direction === 'in' ? 'num-in' : 'num-out'">{{ afterScore }}</b> 分</div>
      </div>
      <template #footer><el-button @click="adjustVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="submitAdjust">确认调整</el-button></template>
    </el-dialog>

    <el-dialog v-model="detailVisible" :title="`信用分明细 · ${current?.name || ''}`" width="900px">
      <div class="detail-stats"><span>当前信用分：<b>{{ current?.score ?? 0 }}</b></span><span class="num-in">累计加分：+{{ detailStats.add }}</span><span class="num-out">累计扣分：-{{ detailStats.sub }}</span><span>明细记录：{{ flows.length }} 条</span></div>
      <el-table :data="flows" size="small"><el-table-column prop="id" label="明细ID" width="90" /><el-table-column label="类型" width="80"><template #default="{ row }">{{ row.delta >= 0 ? '加分' : '减分' }}</template></el-table-column><el-table-column label="分值变动" width="100"><template #default="{ row }"><span :class="row.delta >= 0 ? 'num-in' : 'num-out'">{{ row.delta >= 0 ? '+' : '' }}{{ row.delta }}</span></template></el-table-column><el-table-column prop="afterScore" label="变动后" width="90" /><el-table-column prop="ruleCode" label="触发规则" width="150" /><el-table-column prop="timestamp" label="时间" width="170" /><el-table-column prop="reason" label="备注" min-width="180" /></el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adjustCredit, getCreditDetail, listCreditUsers } from '@/api/credit'

const rows = ref([]); const total = ref(0); const page = ref(1); const size = ref(10); const loading = ref(false)
const filters = reactive({ keyword: '', role: '', level: '' })
const adjustVisible = ref(false); const detailVisible = ref(false); const saving = ref(false); const current = ref(null); const flows = ref([])
const adjust = reactive({ direction: 'in', amount: 5, reason: '' })
const afterScore = computed(() => Math.max(0, Math.min(100, Number(current.value?.score || 0) + (adjust.direction === 'in' ? 1 : -1) * Number(adjust.amount || 0))))
const detailStats = computed(() => ({ add: flows.value.filter(f => f.delta > 0).reduce((n, f) => n + f.delta, 0), sub: flows.value.filter(f => f.delta < 0).reduce((n, f) => n + Math.abs(f.delta), 0) }))
const levelText = key => ({ excellent: '优秀', good: '良好', medium: '一般', low: '较差' }[key] || '-')
async function load () { loading.value = true; try { const result = await listCreditUsers({ ...filters, page: page.value - 1, size: size.value }); const data = result.data; rows.value = data?.content || []; total.value = result.total ?? data?.totalElements ?? 0 } catch (e) { rows.value = []; total.value = 0 } finally { loading.value = false } }
function search () { page.value = 1; load() }
function reset () { Object.assign(filters, { keyword: '', role: '', level: '' }); search() }
function openAdjust (row) { current.value = row; Object.assign(adjust, { direction: 'in', amount: 5, reason: '' }); adjustVisible.value = true }
async function submitAdjust () { if (!current.value || !adjust.amount) return; saving.value = true; try { await adjustCredit(current.value.id, { scoreType: current.value.scoreType, delta: adjust.direction === 'in' ? adjust.amount : -adjust.amount, reason: adjust.reason || '管理员手动调整' }); ElMessage.success('信用分调整成功'); adjustVisible.value = false; await load() } catch (e) { ElMessage.error('信用分调整失败') } finally { saving.value = false } }
async function openDetail (row) { current.value = row; try { const result = await getCreditDetail(row.id); const data = result.data || {}; flows.value = data.creditFlows || []; detailVisible.value = true } catch (e) { ElMessage.error('信用分明细加载失败') } }
onMounted(load)
</script>

<style scoped>
.filter-card{padding:20px}.filter-bar{display:flex;align-items:center;gap:10px;margin-bottom:16px}.credit-table :deep(th){background:#fafafa;color:#6b7280;font-weight:600}.credit-table :deep(td){height:58px}.role-tag{padding:3px 10px;border-radius:12px;font-size:12px}.role-tag.worker{color:#ff6b35;background:#fff1eb}.role-tag.boss{color:#2563eb;background:#eff6ff}.name-cell{display:flex;flex-direction:column;gap:3px}.name-cell small{color:#9ca3af;font-family:monospace}.score-cell{display:flex;flex-direction:column;gap:4px}.score-cell b{font-size:15px}.credit-excellent{color:#f59e0b}.credit-good{color:#059669}.credit-medium{color:#6b7280}.credit-low{color:#dc2626}.credit-bar{width:80px;height:6px;background:#e5e7eb;border-radius:3px;overflow:hidden}.credit-bar i{display:block;height:100%;border-radius:3px}.bar-excellent{background:#f59e0b}.bar-good{background:#059669}.bar-medium{background:#6b7280}.bar-low{background:#dc2626}.level-text{font-weight:600}.num-in{color:#059669;font-weight:600}.num-out{color:#dc2626;font-weight:600}.pagination{display:flex;align-items:center;justify-content:space-between;margin-top:16px;color:#6b7280;font-size:13px}.target-line{display:flex;gap:12px;align-items:baseline}.target-line span{color:#9ca3af;font-family:monospace}.current-score{margin:18px 0;color:#6b7280}.current-score strong{color:#ff6b35;font-size:20px}.after-score{margin:0 0 8px 90px;color:#6b7280}.detail-stats{display:flex;gap:28px;padding:14px 18px;margin-bottom:14px;background:#fafafa;border-radius:8px;color:#6b7280;font-size:13px}.detail-stats b{color:#ff6b35;font-size:16px}
</style>
