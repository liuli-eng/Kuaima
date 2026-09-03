<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">招工审核</h1>
      <p class="page-desc">审核雇主发布的招工信息，确保内容真实合规</p>
    </div>

    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">待审核</span>
          <div class="stat-card-icon yellow"><i class="fas fa-clock"></i></div>
        </div>
        <div class="stat-card-value">15</div>
        <div class="stat-card-change up"><i class="fas fa-arrow-up"></i><span>请及时处理</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">已通过</span>
          <div class="stat-card-icon green"><i class="fas fa-check"></i></div>
        </div>
        <div class="stat-card-value">128</div>
        <div class="stat-card-change up"><i class="fas fa-arrow-up"></i><span>本月审核</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">已拒绝</span>
          <div class="stat-card-icon"><i class="fas fa-times"></i></div>
        </div>
        <div class="stat-card-value">8</div>
        <div class="stat-card-change down"><i class="fas fa-arrow-down"></i><span>本月拒绝</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">本月审核</span>
          <div class="stat-card-icon blue"><i class="fas fa-file-alt"></i></div>
        </div>
        <div class="stat-card-value">151</div>
        <div class="stat-card-change up"><i class="fas fa-arrow-up"></i><span>较上月 +23</span></div>
      </div>
    </div>

    <!-- 审核列表 -->
    <div class="card">
      <div class="filter-bar">
        <el-input v-model="searchKeyword" placeholder="搜索招工名称/雇主" clearable style="width: 240px;" prefix-icon="Search" />
        <el-select v-model="statusFilter" placeholder="审核状态" clearable style="width: 120px;">
          <el-option label="待审核" value="待审核" />
          <el-option label="已通过" value="已通过" />
          <el-option label="已拒绝" value="已拒绝" />
        </el-select>
        <el-button type="primary" @click="handleSearch"><i class="fas fa-search" style="margin-right:4px;"></i>查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table :data="auditData" stripe :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }">
        <el-table-column prop="id" label="招工ID" width="100" />
        <el-table-column prop="type" label="工种" min-width="140" />
        <el-table-column prop="employer" label="雇主" min-width="180" />
        <el-table-column prop="price" label="工价" width="100">
          <template #default="{ row }">
            <span style="font-weight: 600; color: var(--primary);">{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="count" label="招聘人数" width="100" />
        <el-table-column label="风险标签" min-width="160">
          <template #default="{ row }">
            <el-tag v-if="row.risk" type="danger" effect="light" style="margin-right: 4px;">价格偏低</el-tag>
            <el-tag v-if="row.risk2" type="warning" effect="light">信息不完整</el-tag>
            <span v-if="!row.risk && !row.risk2" class="text-muted">无风险</span>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="提交时间" width="160" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <span :class="['status-badge', row.statusClass]">{{ row.status }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDetail(row)">查看详情</el-button>
            <template v-if="row.status === '待审核'">
              <el-button link type="success" size="small" @click="handleApprove(row)">通过</el-button>
              <el-button link type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 审核详情弹窗 -->
    <el-dialog v-model="detailVisible" title="审核详情" width="700px">
      <div v-if="currentItem">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="招工ID">{{ currentItem.id }}</el-descriptions-item>
          <el-descriptions-item label="工种">{{ currentItem.type }}</el-descriptions-item>
          <el-descriptions-item label="雇主">{{ currentItem.employer }}</el-descriptions-item>
          <el-descriptions-item label="工价">{{ currentItem.price }}</el-descriptions-item>
          <el-descriptions-item label="招聘人数">{{ currentItem.count }}</el-descriptions-item>
          <el-descriptions-item label="地点">{{ currentItem.location }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">2024-03-16 08:00</el-descriptions-item>
          <el-descriptions-item label="结束时间">2024-03-31 18:00</el-descriptions-item>
          <el-descriptions-item label="性别要求">不限</el-descriptions-item>
          <el-descriptions-item label="经验要求">不限</el-descriptions-item>
          <el-descriptions-item label="提交时间" :span="2">{{ currentItem.time }}</el-descriptions-item>
          <el-descriptions-item label="招工描述" :span="2">负责产品装配、质检等工作，两班倒，包吃住</el-descriptions-item>
        </el-descriptions>
        
        <el-divider />
        
        <div style="margin-bottom: 16px;">
          <div style="font-weight: 600; margin-bottom: 8px;">雇主信息</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="企业名称">{{ currentItem.employer }}</el-descriptions-item>
            <el-descriptions-item label="联系人">张经理</el-descriptions-item>
            <el-descriptions-item label="联系电话">138****8888</el-descriptions-item>
            <el-descriptions-item label="认证状态">已认证</el-descriptions-item>
          </el-descriptions>
        </div>

        <div style="margin-bottom: 16px;">
          <div style="font-weight: 600; margin-bottom: 8px;">审核备注</div>
          <el-input type="textarea" v-model="auditRemark" placeholder="请输入审核备注（可选）" :rows="3" />
        </div>
      </div>
      
      <template #footer>
        <el-button @click="detailVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">拒绝</el-button>
        <el-button type="success" @click="confirmApprove">通过</el-button>
      </template>
    </el-dialog>

    <!-- 拒绝原因弹窗 -->
    <el-dialog v-model="rejectVisible" title="拒绝原因" width="450px">
      <el-form>
        <el-form-item label="拒绝原因">
          <el-select v-model="rejectReason" placeholder="请选择拒绝原因" style="width: 100%;">
            <el-option label="信息不完整" value="incomplete" />
            <el-option label="价格严重偏低" value="low_price" />
            <el-option label="内容违规" value="violation" />
            <el-option label="疑似虚假招工" value="fake" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="补充说明">
          <el-input type="textarea" v-model="rejectDesc" :rows="3" placeholder="请输入补充说明（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmRejectSubmit">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listJobs, auditJobPass, auditJobReject } from '@/api/job'

const searchKeyword = ref('')
const statusFilter = ref('待审核')
const auditRemark = ref('')
const rejectReason = ref('')
const rejectDesc = ref('')
const detailVisible = ref(false)
const rejectVisible = ref(false)
const currentItem = ref(null)

const auditData = ref([])

// mock 数据（带状态映射，用于 fallback）
const fallbackAuditData = [
  { id: 'JB009', type: '电子厂操作工', employer: '某新注册企业', price: '150/天', count: 10, location: '深圳光明区', time: '2024-03-15 11:30', status: '待审核', statusClass: 'warning', risk: true, risk2: false },
  { id: 'JB010', type: '快递分拣员', employer: '某快递公司', price: '20/小时', count: 20, location: '深圳宝安区', time: '2024-03-15 10:15', status: '待审核', statusClass: 'warning', risk: false, risk2: true },
  { id: 'JB011', type: '餐厅服务员', employer: '某餐饮连锁', price: '180/天', count: 8, location: '深圳福田区', time: '2024-03-15 09:00', status: '已通过', statusClass: 'success', risk: false, risk2: false },
  { id: 'JB012', type: '仓库理货员', employer: '某物流公司', price: '300/天', count: 15, location: '深圳龙岗区', time: '2024-03-14 16:00', status: '已拒绝', statusClass: 'danger', risk: true, risk2: false }
]

const statusClassMap = {
  '待审核': 'warning',
  '已通过': 'success',
  '已拒绝': 'danger',
  '招工中': 'info',
  '进行中': 'info',
  '已完成': 'success',
  '已取消': 'default',
  '审核拒绝': 'danger',
}

const normalizeAudit = (item) => {
  const statusVal = item.orderStatus ?? item.status ?? '未知'
  return {
    id: item.id,
    type: item.type ?? item.postion,
    employer: item.employerName ?? item.employer ?? '未知雇主',
    price: item.salary ?? item.price,
    count: item.orderNum ?? item.count,
    location: item.address ?? item.location,
    time: item.timestamp ?? item.time,
    status: statusVal,
    statusClass: statusClassMap[statusVal] ?? 'default',
    risk: item.risk ?? false,
    risk2: item.incomplete ?? item.risk2 ?? false,
  }
}

const loadAuditData = async () => {
  try {
    const res = await listJobs({
      type: undefined,
      status: statusFilter.value || undefined,
      title: searchKeyword.value || undefined,
      page: 0,
      size: 50,
    })
    const d = res.data
    const list = Array.isArray(d) ? d : (d?.content || d?.list || [])
    auditData.value = list.map(normalizeAudit)
  } catch (err) {
    console.warn('[JobAudit] API 加载失败，使用 mock 数据:', err.message)
    auditData.value = fallbackAuditData.map(normalizeAudit)
  }
}

const handleSearch = () => loadAuditData()

const handleReset = () => {
  searchKeyword.value = ''
  statusFilter.value = '待审核'
  loadAuditData()
}

const handleDetail = (row) => {
  currentItem.value = row
  auditRemark.value = ''
  detailVisible.value = true
}

const handleApprove = (row) => {
  currentItem.value = row
  auditRemark.value = ''
  detailVisible.value = true
}

const handleReject = (row) => {
  currentItem.value = row
  rejectReason.value = ''
  rejectDesc.value = ''
  rejectVisible.value = true
}

const confirmApprove = async () => {
  if (!currentItem.value) return
  try {
    await auditJobPass(currentItem.value.id)
    detailVisible.value = false
    ElMessage.success('审核通过')
    loadAuditData()
  } catch (err) {
    // 失败时在 fallback mock 中模拟通过
    detailVisible.value = false
    console.warn('[JobAudit] 审核通过 API 调用失败:', err.message)
    ElMessage.success('审核通过（本地模拟）')
    currentItem.value.status = '已通过'
    currentItem.value.statusClass = 'success'
  }
}

const confirmReject = () => {
  detailVisible.value = false
  rejectReason.value = ''
  rejectDesc.value = ''
  rejectVisible.value = true
}

const confirmRejectSubmit = async () => {
  if (!rejectReason.value) {
    ElMessage.warning('请选择拒绝原因')
    return
  }
  if (!currentItem.value) return

  const reasonLabel = {
    incomplete: '信息不完整',
    low_price: '价格严重偏低',
    violation: '内容违规',
    fake: '疑似虚假招工',
    other: '其他',
  }[rejectReason.value] || rejectReason.value
  const fullReason = rejectDesc.value
    ? `${reasonLabel}：${rejectDesc.value}`
    : reasonLabel

  try {
    await auditJobReject(currentItem.value.id, fullReason)
    rejectVisible.value = false
    ElMessage.success('已拒绝')
    loadAuditData()
  } catch (err) {
    rejectVisible.value = false
    console.warn('[JobAudit] 审核拒绝 API 调用失败:', err.message)
    ElMessage.success('已拒绝（本地模拟）')
    currentItem.value.status = '已拒绝'
    currentItem.value.statusClass = 'danger'
  }
}

onMounted(loadAuditData)
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
