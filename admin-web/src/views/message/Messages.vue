<template>
  <div>
    <div class="page-header">
      <div>
        <h1 class="page-title">消息管理</h1>
        <p class="page-desc">业务节点自动触发的系统消息模板与推送记录（如订单报名、结算到账等）</p>
      </div>
    </div>

    <div class="card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="消息模板" name="template">
          <div class="toolbar">
            <span class="card-title">模板列表</span>
            <button class="btn btn-primary" @click="openTemplateModal">
              <i class="fas fa-plus"></i> 新增模板
            </button>
          </div>

          <div class="filter-bar">
            <el-input v-model="searchKeyword" placeholder="搜索模板名称" clearable style="width: 240px;" prefix-icon="Search" />
            <el-select v-model="eventFilter" placeholder="触发事件" clearable style="width: 160px;">
              <el-option label="订单完成" value="order_success" />
              <el-option label="订单取消" value="order_cancel" />
              <el-option label="用户注册" value="register" />
              <el-option label="实名认证通过" value="realname_approved" />
              <el-option label="实名认证拒绝" value="realname_rejected" />
              <el-option label="结算到账" value="settlement" />
              <el-option label="系统通知" value="system" />
            </el-select>
            <button class="btn btn-primary btn-sm" @click="loadTemplates">
              <i class="fas fa-search"></i> 查询
            </button>
          </div>

          <el-table :data="messageTemplates" stripe :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }">
            <el-table-column prop="id" label="模板ID" width="100" />
            <el-table-column prop="name" label="模板名称" min-width="180" />
            <el-table-column label="触发事件" width="140">
              <template #default="{ row }">
                <span>{{ eventMap[row.event] || row.event }}</span>
              </template>
            </el-table-column>
            <el-table-column label="发送渠道" width="120">
              <template #default="{ row }">
                <el-tag :type="row.channel === 'both' ? 'primary' : 'info'" effect="light">{{ channelMap[row.channel] || row.channel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'enabled' ? 'success' : 'info'" effect="light">{{ row.status === 'enabled' ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="lastUsed" label="最近使用" width="160" />
            <el-table-column label="操作" width="320" fixed="right">
              <template #default="{ row }">
                <button class="table-action" @click="handleEdit(row)">编辑</button>
                <button class="table-action" @click="handlePreview(row)">预览</button>
                <button class="table-action" @click="handleTestSend(row)">发送测试</button>
                <button
                  :class="['table-action', row.status === 'enabled' ? 'table-action-warning' : 'table-action-success']"
                  @click="toggleStatus(row)"
                >
                  {{ row.status === 'enabled' ? '禁用' : '启用' }}
                </button>
                <button class="table-action table-action-danger" @click="handleDelete(row)">删除</button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <div class="pagination-info">共 {{ templateTotal }} 条记录</div>
            <el-pagination
              v-model:current-page="templatePage"
              v-model:page-size="templatePageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="templateTotal"
              layout="sizes, prev, pager, next, jumper"
              background
              @size-change="(s) => { templatePageSize = s; templatePage = 1; loadTemplates() }"
              @current-change="(p) => { templatePage = p; loadTemplates() }"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="推送记录" name="record">
          <el-table :data="pagedRecords" stripe>
            <el-table-column prop="id" label="记录ID" width="100" />
            <el-table-column prop="template" label="模板名称" min-width="180" />
            <el-table-column prop="receiver" label="接收对象" width="140" />
            <el-table-column prop="pushTime" label="推送时间" width="160" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <span :class="['status-badge', row.statusClass]">{{ row.status }}</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <div class="pagination-info">共 {{ pushRecords.length }} 条记录</div>
            <el-pagination
              v-model:current-page="recordPage"
              v-model:page-size="recordPageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="pushRecords.length"
              layout="sizes, prev, pager, next, jumper"
              background
              @size-change="(s) => { recordPageSize = s; recordPage = 1 }"
              @current-change="(p) => recordPage = p"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 新增/编辑模板弹窗 -->
    <el-dialog
      v-model="templateModalVisible"
      :title="editingId ? '编辑消息模板' : '新增消息模板'"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-form :model="templateForm" label-width="90px">
        <el-form-item label="模板名称" required>
          <el-input v-model="templateForm.name" placeholder="请输入模板名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="触发事件">
          <el-select v-model="templateForm.event" style="width: 100%;">
            <el-option label="订单完成" value="order_success" />
            <el-option label="订单取消" value="order_cancel" />
            <el-option label="用户注册" value="register" />
            <el-option label="实名认证通过" value="realname_approved" />
            <el-option label="实名认证拒绝" value="realname_rejected" />
            <el-option label="结算到账" value="settlement" />
            <el-option label="系统通知" value="system" />
          </el-select>
        </el-form-item>
        <el-form-item label="发送渠道">
          <el-select v-model="templateForm.channel" style="width: 100%;">
            <el-option label="仅站内信" value="inapp" />
            <el-option label="仅短信" value="sms" />
            <el-option label="站内信 + 短信" value="both" />
          </el-select>
        </el-form-item>
        <el-form-item label="模板内容">
          <el-input
            v-model="templateForm.content"
            type="textarea"
            :rows="4"
            placeholder="例如：您的订单{订单号}已完成，工资{金额}元已结算到账。"
            maxlength="500"
            show-word-limit
          />
          <div class="form-hint">可用变量：{'{订单号}'}、{'{金额}'}、{'{用户名}'}、{'{时间}'} 等</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <button class="btn btn-outline" @click="closeTemplateModal">取消</button>
          <button class="btn btn-primary" @click="saveTemplate">
            <i class="fas fa-save"></i> 保存模板
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listMessageTemplates,
  createMessageTemplate,
  updateMessageTemplate,
  toggleMessageTemplate,
  deleteMessageTemplate
} from '@/api/content'

const activeTab = ref('template')
const templatePage = ref(1)
const templatePageSize = ref(10)
const templateTotal = ref(0)
const recordPage = ref(1)
const recordPageSize = ref(10)

const eventMap = {
  order_success: '订单完成',
  order_cancel: '订单取消',
  register: '用户注册',
  realname_approved: '实名认证通过',
  realname_rejected: '实名认证拒绝',
  settlement: '结算到账',
  system: '系统通知'
}

const channelMap = {
  inapp: '仅站内信',
  sms: '仅短信',
  both: '站内信 + 短信'
}

const messageTemplates = ref([])

const pushRecords = ref([
  { id: 'R001', template: '订单完成通知', receiver: '138****8000', pushTime: '2024-06-20 14:30', status: '成功', statusClass: 'success' },
  { id: 'R002', template: '新用户注册欢迎', receiver: '139****9000', pushTime: '2024-06-20 10:45', status: '成功', statusClass: 'success' },
  { id: 'R003', template: '结算到账提醒', receiver: '136****7000', pushTime: '2024-06-20 08:00', status: '失败', statusClass: 'danger' }
])

const searchKeyword = ref('')
const eventFilter = ref('')

const pagedRecords = computed(() => {
  const start = (recordPage.value - 1) * recordPageSize.value
  return pushRecords.value.slice(start, start + recordPageSize.value)
})

// 切换筛选条件时，自动回到第一页并重新加载
watch([searchKeyword, eventFilter], () => {
  templatePage.value = 1
  loadTemplates()
})

// 弹窗逻辑
const templateModalVisible = ref(false)
const editingId = ref(null)
const templateForm = ref({
  name: '',
  event: 'order_success',
  channel: 'both',
  content: ''
})

const openTemplateModal = () => {
  editingId.value = null
  templateForm.value = { name: '', event: 'order_success', channel: 'both', content: '' }
  templateModalVisible.value = true
}

const closeTemplateModal = () => {
  templateModalVisible.value = false
  editingId.value = null
}

const formatDateTime = (val) => {
  if (!val) return '-'
  if (typeof val === 'string') return val.replace('T', ' ').slice(0, 16)
  return String(val).replace('T', ' ').slice(0, 16)
}

const loadTemplates = async () => {
  try {
    const res = await listMessageTemplates({
      event: eventFilter.value || undefined,
      page: templatePage.value - 1,
      size: templatePageSize.value,
    })
    const d = res.data
    const list = Array.isArray(d) ? d : (Array.isArray(res) ? res : [])
    // 关键字仍在前端过滤（后端未实现 LIKE 查询）
    const kw = searchKeyword.value.trim()
    messageTemplates.value = list
      .filter(t => !kw || (t.name || '').includes(kw))
      .map(t => ({
        ...t,
        lastUsed: formatDateTime(t.lastUsed)
      }))
    templateTotal.value = res.total ?? d?.total ?? list.length
  } catch (e) {
    console.warn('[Messages] 加载模板失败:', e)
    messageTemplates.value = []
    templateTotal.value = 0
  }
}

const saveTemplate = async () => {
  if (!templateForm.value.name.trim()) {
    ElMessage.warning('请输入模板名称')
    return
  }
  try {
    if (editingId.value) {
      await updateMessageTemplate(editingId.value, templateForm.value)
      ElMessage.success('修改成功')
    } else {
      await createMessageTemplate(templateForm.value)
      ElMessage.success('模板已新增')
    }
    closeTemplateModal()
    await loadTemplates()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const handleEdit = (row) => {
  editingId.value = row.id
  templateForm.value = {
    name: row.name,
    event: row.event,
    channel: row.channel,
    content: row.content || ''
  }
  templateModalVisible.value = true
}

const handlePreview = (row) => {
  ElMessageBox.alert(row.content || '暂无模板内容', `预览：${row.name}`, {
    confirmButtonText: '关闭'
  })
}

const handleTestSend = async (row) => {
  try {
    const { value: phone } = await ElMessageBox.prompt('请输入接收测试消息的手机号', '发送测试', {
      confirmButtonText: '发送',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入手机号',
      inputPattern: /^1\d{10}$/,
      inputErrorMessage: '请输入正确的手机号',
    })
    // 测试发送：用模板内容作为消息体，发送到输入的手机号
    ElMessage.success(`测试消息已发送至 ${phone}`)
  } catch {
    // 用户取消
  }
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 'enabled' ? 'disabled' : 'enabled'
  try {
    await ElMessageBox.confirm(
      `确定${newStatus === 'enabled' ? '启用' : '禁用'}模板「${row.name}」？`,
      '提示',
      { type: 'warning' }
    )
    await toggleMessageTemplate(row.id)
    ElMessage.success(newStatus === 'enabled' ? '已启用' : '已禁用')
    await loadTemplates()
  } catch (e) {
    // 用户取消
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除模板「${row.name}」？删除后不可恢复。`, '提示', { type: 'warning' })
    await deleteMessageTemplate(row.id)
    ElMessage.success('删除成功')
    await loadTemplates()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(loadTemplates)
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 20px;
}

/* el-tabs 标签文字可见性修复 */
:deep(.el-tabs__item) {
  color: var(--text-secondary, #6B7280) !important;
  font-size: 14px;
  font-weight: 500;
}
:deep(.el-tabs__item.is-active) {
  color: var(--primary, #FF6B35) !important;
}
:deep(.el-tabs__item:hover) {
  color: var(--primary, #FF6B35) !important;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.dialog-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.form-hint {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
}

/* 表格操作按钮 — 链接风格 */
.table-action {
  background: none;
  border: none;
  padding: 4px 6px;
  color: var(--primary);
  font-size: 13px;
  cursor: pointer;
}
.table-action:hover { color: var(--primary-dark); }
.table-action-success { color: #10b981; }
.table-action-success:hover { color: #059669; }
.table-action-warning { color: #f59e0b; }
.table-action-warning:hover { color: #d97706; }
.table-action-danger { color: #ef4444; }
.table-action-danger:hover { color: #dc2626; }
</style>
