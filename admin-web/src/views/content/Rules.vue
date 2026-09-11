<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">规则管理</h1>
      <p class="page-desc">平台规则公示、收费、交易、知识产权管理规则</p>
    </div>

    <!-- 分类标签栏 -->
    <div class="rule-tabs">
      <div
        v-for="tab in tabs"
        :key="tab.key"
        :class="['rule-tab', { active: currentTab === tab.key }]"
        @click="switchTab(tab.key)"
      >
        <span class="tab-icon" :style="{ background: tab.gradient }">
          <i :class="['fas', tab.icon]"></i>
        </span>
        {{ tab.label }}
        <span class="count-badge">{{ tab.count }}</span>
      </div>
    </div>

    <div class="card">
      <div class="filter-bar">
        <div class="filter-item">
          <span>状态</span>
          <el-select v-model="statusFilter" placeholder="全部" clearable style="width: 120px;">
            <el-option label="已发布" value="published" />
            <el-option label="草稿" value="draft" />
            <el-option label="已归档" value="archived" />
          </el-select>
        </div>
        <div class="filter-item">
          <el-input
            v-model="keyword"
            placeholder="规则名称搜索"
            style="width: 220px;"
            :prefix-icon="Search"
            clearable
          />
        </div>
        <button class="btn btn-primary btn-sm" @click="() => {}">
          <i class="fas fa-search"></i> 查询
        </button>
        <button class="btn btn-outline btn-sm" @click="resetFilters">
          <i class="fas fa-rotate-left"></i> 重置
        </button>
        <div style="flex:1;"></div>
        <button class="btn btn-primary btn-sm" @click="goCreate">
          <i class="fas fa-plus"></i> 新增规则
        </button>
      </div>

      <el-table
        :data="rules"
        stripe
        :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }"
      >
        <el-table-column label="规则ID" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="rule-id">{{ row.code || row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column label="规则名称" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="rule-name">
              <span class="category-icon" :style="{ background: currentTabMeta.gradient }">
                <i :class="['fas', currentTabMeta.icon]"></i>
              </span>
              <span class="rule-name-text">{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" show-overflow-tooltip />
        <el-table-column label="版本号" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="rule-version">{{ row.version || 'v1.0' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="['status-badge', statusClass(row.status)]">{{ statusLabel(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" show-overflow-tooltip />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <button class="table-action" @click="viewRule(row)">查看</button>
              <button class="table-action" @click="handleEdit(row)">编辑</button>
              <button
                v-if="row.status === 'published'"
                class="table-action table-action-warning"
                @click="toggleStatus(row, 'draft')"
              >下线</button>
              <button
                v-else-if="row.status === 'draft'"
                class="table-action table-action-success"
                @click="toggleStatus(row, 'published')"
              >发布</button>
              <button
                v-else
                class="table-action"
                @click="toggleStatus(row, 'published')"
              >恢复</button>
              <button class="table-action table-action-danger" @click="handleDelete(row)">删除</button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <div class="pagination-info">共 {{ total }} 条记录</div>
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="sizes, prev, pager, next, jumper"
          background
          @size-change="onSizeChange"
          @current-change="onPageChange"
        />
      </div>
    </div>

    <!-- 规则详情预览弹窗 -->
    <el-dialog v-model="viewVisible" title="规则详情" width="640px">
      <div v-if="currentViewRule" class="preview-meta">
        <span><i class="fas fa-tag" style="color: var(--primary);"></i> {{ currentViewRule.category }}</span>
        <span><i class="fas fa-code-branch" style="color: var(--primary);"></i> {{ currentViewRule.version || 'v1.0' }}</span>
        <span v-if="currentViewRule.updateTime"><i class="fas fa-clock"></i> {{ currentViewRule.updateTime }}</span>
        <span :class="['status-badge', statusClass(currentViewRule.status)]">{{ statusLabel(currentViewRule.status) }}</span>
      </div>
      <div class="preview-content" v-html="currentViewRule?.content"></div>
      <template #footer>
        <button class="btn btn-outline" @click="viewVisible = false">关闭</button>
        <button class="btn btn-primary" @click="editFromView">
          <i class="fas fa-edit"></i> 编辑
        </button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { listRules, updateRules, deleteRules } from '@/api/content'

const router = useRouter()
const rules = ref([])
const total = ref(0)
const currentTab = ref('notice')
const statusFilter = ref('')
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const viewVisible = ref(false)
const currentViewRule = ref(null)

const tabs = computed(() => {
  const map = {
    notice: '规则公示',
    fee: '收费规则',
    trade: '交易规则',
    ip: '知识产权规则'
  }
  return Object.keys(map).map(key => ({
    key,
    label: map[key],
    count: rules.value.filter(r => r.type === key || r.category === map[key]).length,
    icon: tabMeta[key].icon,
    gradient: tabMeta[key].gradient
  }))
})

const tabMeta = {
  notice: { icon: 'fa-bullhorn', gradient: 'linear-gradient(135deg,#3B82F6,#2563EB)' },
  fee: { icon: 'fa-coins', gradient: 'linear-gradient(135deg,#F59E0B,#D97706)' },
  trade: { icon: 'fa-exchange-alt', gradient: 'linear-gradient(135deg,#8B5CF6,#6D28D9)' },
  ip: { icon: 'fa-shield-alt', gradient: 'linear-gradient(135deg,#06B6D4,#0891B2)' }
}

const currentTabMeta = computed(() => tabMeta[currentTab.value] || tabMeta.notice)

// 切换分类、筛选条件或搜索关键字时，自动回到第一页并重新加载
watch([currentTab, statusFilter, keyword], () => {
  currentPage.value = 1
  loadData()
})

const onSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadData()
}

const onPageChange = (page) => {
  currentPage.value = page
  loadData()
}

const switchTab = (key) => {
  currentTab.value = key
}

const resetFilters = () => {
  statusFilter.value = ''
  keyword.value = ''
  currentPage.value = 1
  loadData()
}

const goCreate = () => {
  router.push({ name: 'RulesEdit', params: { tab: currentTab.value } })
}

const handleEdit = (rule) => {
  router.push({ name: 'RulesEdit', params: { id: rule.id, tab: rule.type || currentTab.value } })
}

const viewRule = (rule) => {
  currentViewRule.value = rule
  viewVisible.value = true
}

const editFromView = () => {
  if (currentViewRule.value) {
    viewVisible.value = false
    handleEdit(currentViewRule.value)
  }
}

const statusLabel = (status) => {
  const map = { published: '已发布', draft: '草稿', archived: '已归档' }
  return map[status] || '草稿'
}

const statusClass = (status) => {
  const map = { published: 'success', draft: 'warning', archived: 'default' }
  return map[status] || 'warning'
}

const toggleStatus = async (rule, target) => {
  const action = target === 'published' ? '发布' : target === 'draft' ? '下线' : '恢复'
  try {
    await ElMessageBox.confirm(`确定要${action}规则「${rule.title}」吗？`, '提示', { type: 'warning' })
    const now = new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
    await updateRules(rule.id, { ...rule, status: target, updateTime: now })
    ElMessage.success(`已${action}`)
    await loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(`${action}失败`)
  }
}

const handleDelete = async (rule) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除规则「${rule.title}」吗？删除后不可恢复，请谨慎操作。`,
      '确认删除',
      { type: 'warning' }
    )
    await deleteRules(rule.id)
    ElMessage.success('删除成功')
    await loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

// 已下线的规则类型（信用分、飞单认定）不在列表中展示
const REMOVED_CATEGORIES = ['信用分规则', '飞单认定与处理规则', '信用评定']

// 分类映射
const CATEGORY_MAP = {
  notice: '规则公示',
  fee: '收费规则',
  trade: '交易规则',
  ip: '知识产权规则'
}

const loadData = async () => {
  try {
    const res = await listRules({ page: currentPage.value - 1, size: pageSize.value })
    const d = res.data
    const list = Array.isArray(d) ? d : (Array.isArray(res) ? res : [])
    const filtered = list.filter(r => {
      // 去掉已下线的分类
      if (REMOVED_CATEGORIES.includes(r.category)) return false
      // 按当前选中的 tab 分类过滤
      const targetCategory = CATEGORY_MAP[currentTab.value]
      return r.type === currentTab.value || r.category === targetCategory
    })
    rules.value = filtered
    total.value = filtered.length
  } catch (e) {
    console.warn('[Rules] 加载失败:', e)
    rules.value = []
    total.value = 0
  }
}

onMounted(loadData)
</script>

<style scoped>
.rule-tabs {
  display: flex;
  gap: 4px;
  border-bottom: 2px solid var(--border, #E5E7EB);
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.rule-tab {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary, #6B7280);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}
.rule-tab:hover { color: var(--primary, #FF6B35); }
.rule-tab.active {
  color: var(--primary, #FF6B35);
  border-bottom-color: var(--primary, #FF6B35);
}
.rule-tab .tab-icon {
  width: 18px;
  height: 18px;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: #fff;
}
.rule-tab .count-badge {
  display: inline-block;
  padding: 1px 8px;
  border-radius: 10px;
  font-size: 11px;
  background: var(--bg-page, #F9FAFB);
  color: var(--text-muted, #9CA3AF);
  margin-left: 4px;
}
.rule-tab.active .count-badge {
  background: rgba(255,107,53,0.1);
  color: var(--primary, #FF6B35);
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.filter-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary, #6B7280);
}

.category-icon {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #fff;
}
.rule-name {
  display: flex;
  align-items: center;
  gap: 10px;
}
.rule-name-text {
  font-weight: 500;
  font-size: 14px;
  color: var(--text-primary, #111827);
}
.rule-id {
  font-family: monospace;
  color: var(--primary, #FF6B35);
  font-size: 12px;
}
.rule-version {
  font-family: monospace;
  color: var(--text-secondary, #6B7280);
  font-size: 12px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}
.status-badge.success { background: #D1FAE5; color: #059669; }
.status-badge.warning { background: #FEF3C7; color: #D97706; }
.status-badge.default { background: #F3F4F6; color: #6B7280; }

.action-btns {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.table-action {
  background: none;
  border: none;
  padding: 4px 6px;
  color: var(--primary, #FF6B35);
  font-size: 13px;
  cursor: pointer;
}
.table-action:hover { color: var(--primary-dark, #E55A2B); }
.table-action-success { color: #10b981; }
.table-action-success:hover { color: #059669; }
.table-action-warning { color: #f59e0b; }
.table-action-warning:hover { color: #d97706; }
.table-action-danger { color: #ef4444; }
.table-action-danger:hover { color: #dc2626; }

.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
}

.preview-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--text-muted, #9CA3AF);
  flex-wrap: wrap;
}
.preview-content {
  background: var(--bg-page, #F9FAFB);
  border-radius: 8px;
  padding: 16px;
  font-size: 14px;
  line-height: 1.7;
  color: var(--text-secondary, #6B7280);
  max-height: 360px;
  overflow-y: auto;
  white-space: pre-wrap;
}
</style>
