<template>
  <div>
    <!-- 编辑头部 -->
    <div class="edit-header">
      <div class="edit-header-left">
        <div class="edit-header-info">
          <h2>{{ isEdit ? '编辑模板' : '新增模板' }}</h2>
          <div class="edit-header-meta">
            <span class="edit-id-badge">{{ isEdit ? 'ID: ' + (templateId || '--') : '新模板' }}</span>
            <span class="tag" :class="form.type === 'sms' ? 'tag-blue' : 'tag-green'">
              {{ form.type === 'sms' ? '短信模板' : '站内信模板' }}
            </span>
            <span class="status-badge" :class="form.status === 'enabled' ? 'success' : 'disabled'">
              {{ form.status === 'enabled' ? '已启用' : '已停用' }}
            </span>
          </div>
        </div>
      </div>
      <div class="header-actions">
        <button class="btn btn-outline" @click="goBack">
          <i class="fas fa-arrow-left"></i> 返回列表
        </button>
        <button class="btn btn-outline" @click="openPreview">
          <i class="fas fa-eye"></i> 预览
        </button>
        <button class="btn btn-primary" :disabled="saving" @click="saveTemplate">
          <i class="fas fa-check"></i> {{ saving ? '保存中...' : '保存模板' }}
        </button>
      </div>
    </div>

    <div style="height:16px;"></div>

    <div v-loading="loading" class="edit-layout">
      <!-- 左侧列 -->
      <div class="left-col">
        <!-- 基本信息 -->
        <div class="card">
          <div class="form-section">
            <div class="form-section-title">
              <span class="section-num">1</span> 基本信息
            </div>

            <div style="margin-bottom:18px;">
              <label class="form-label">模板类型 <span class="required">*</span></label>
              <el-select v-model="form.type" style="width:100%;" @change="onTypeChange">
                <el-option label="短信模板" value="sms" />
                <el-option label="站内信模板" value="message" />
              </el-select>
            </div>

            <div class="form-grid">
              <div>
                <label class="form-label">模板名称 <span class="required">*</span></label>
                <el-input v-model="form.name" placeholder="请输入模板名称，如：订单接单通知" maxlength="50" show-word-limit />
              </div>
              <div>
                <label class="form-label">模板编码</label>
                <el-input :model-value="templateCode" readonly style="background:var(--bg-page);" />
              </div>
              <div>
                <label class="form-label">触发场景</label>
                <el-select v-model="form.scene" style="width:100%;">
                  <el-option label="接单成功" value="接单成功" />
                  <el-option label="工资结算" value="工资结算" />
                  <el-option label="审核结果" value="审核结果" />
                  <el-option label="系统公告" value="系统公告" />
                  <el-option label="飞单提醒" value="飞单提醒" />
                  <el-option label="手动发送" value="手动发送" />
                </el-select>
              </div>
              <div>
                <label class="form-label">模板状态</label>
                <div class="status-radio">
                  <label><el-radio v-model="form.status" value="enabled">启用</el-radio></label>
                  <label><el-radio v-model="form.status" value="disabled">停用</el-radio></label>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 模板内容 -->
        <div class="card">
          <div class="form-section">
            <div class="form-section-title">
              <span class="section-num">2</span> 模板内容
            </div>

            <div>
              <label class="form-label">模板内容 <span class="required">*</span></label>
              <div class="content-toolbar">
                <span class="toolbar-label">点击插入变量：</span>
                <span v-for="v in variableList" :key="v.code" class="var-chip" @click="insertVariable(v.code)">
                  + {{ v.desc }}
                </span>
              </div>
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="6"
                placeholder="请输入模板内容，可使用 {变量} 占位符..."
                maxlength="500"
                show-word-limit
                class="content-editor"
                @input="updateCount"
              />
              <div class="content-footer">
                <span v-if="form.type === 'sms'">短信签名将自动追加：【快马日结】</span>
                <span v-else style="visibility:hidden;">占位</span>
                <span :class="{ 'over-limit': contentLength > 500 }">{{ contentLength }}/500 字</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 发送设置（仅短信） -->
        <div v-if="form.type === 'sms'" class="card">
          <div class="form-section">
            <div class="form-section-title">
              <span class="section-num">3</span> 发送设置
            </div>

            <div class="form-grid">
              <div>
                <label class="form-label">短信签名</label>
                <el-input model-value="【快马日结】" readonly style="background:var(--bg-page);" />
              </div>
              <div>
                <label class="form-label">发送方式</label>
                <el-select v-model="form.sendWay" style="width:100%;">
                  <el-option label="事件触发即时发送" value="即时" />
                  <el-option label="定时批量发送" value="定时" />
                </el-select>
              </div>
              <div v-if="form.sendWay === '定时'">
                <label class="form-label">定时发送时间 <span class="required">*</span></label>
                <el-select v-model="form.scheduledTime" placeholder="选择整点时间" style="width:100%;">
                  <el-option v-for="h in 11" :key="h" :label="String(h + 8).padStart(2, '0') + ':00'" :value="String(h + 8).padStart(2, '0') + ':00'" />
                </el-select>
              </div>
              <div>
                <label class="form-label">频率限制</label>
                <el-select v-model="form.freqLimit" style="width:100%;">
                  <el-option label="同一用户每天最多 5 条" value="5" />
                  <el-option label="同一用户每天最多 10 条" value="10" />
                  <el-option label="不限制" value="不限制" />
                </el-select>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部操作栏 -->
        <div class="action-bar">
          <button class="btn btn-outline" @click="goBack">
            <i class="fas fa-arrow-left"></i> 返回
          </button>
          <button class="btn btn-primary" @click="saveTemplate">
            <i class="fas fa-paper-plane"></i> 保存模板
          </button>
        </div>
      </div>

      <!-- 右侧列 -->
      <div class="right-col">
        <!-- 模板信息 -->
        <div class="card info-card">
          <div class="info-card-title">
            <i class="fas fa-info-circle"></i> 模板信息
          </div>
          <div class="info-row">
            <span class="info-row-label">模板ID</span>
            <span class="info-row-value">{{ isEdit ? templateId : '自动生成' }}</span>
          </div>
          <div class="info-row">
            <span class="info-row-label">创建人</span>
            <span class="info-row-value">超级管理员</span>
          </div>
          <div class="info-row">
            <span class="info-row-label">创建时间</span>
            <span class="info-row-value">{{ createTime }}</span>
          </div>
          <div class="info-row">
            <span class="info-row-label">更新时间</span>
            <span class="info-row-value">{{ updateTime }}</span>
          </div>
          <div class="info-row">
            <span class="info-row-label">状态</span>
            <span class="info-row-value">{{ form.status === 'enabled' ? '已启用' : '已停用' }}</span>
          </div>
        </div>

        <!-- 可用变量 -->
        <div class="card info-card">
          <div class="info-card-title">
            <i class="fas fa-code"></i> 可用变量
          </div>
          <div class="var-list">
            <div v-for="v in variableList" :key="v.code" class="var-row">
              <span class="var-code" @click="insertVariable(v.code)">{{ v.code }}</span>
              <span class="var-desc">{{ v.desc }}</span>
            </div>
          </div>
        </div>

        <!-- 填写提示 -->
        <div class="card info-card">
          <div class="info-card-title">
            <i class="fas fa-lightbulb"></i> 填写提示
          </div>
          <div class="tip-list">
            <div class="tip-item"><i class="fas fa-check-circle"></i><span>模板名称建议简洁明确，便于在列表中识别</span></div>
            <div class="tip-item"><i class="fas fa-check-circle"></i><span>短信内容（含签名）请控制在 70 字以内，超长将按多条计费</span></div>
            <div class="tip-item"><i class="fas fa-check-circle"></i><span>变量使用英文花括号包裹，发送时自动替换为实际内容</span></div>
            <div class="tip-item"><i class="fas fa-check-circle"></i><span>保存前请使用"预览"检查变量替换效果</span></div>
            <div class="tip-item"><i class="fas fa-check-circle"></i><span>停用模板后对应场景将不再发送通知</span></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 预览弹窗 -->
    <el-dialog v-model="previewVisible" title="模板预览" width="400px">
      <div class="phone-preview">
        <div class="preview-meta">
          <span>{{ form.type === 'sms' ? '短信 · 快马日结' : '站内信 · 系统' }}</span>
          <span>刚刚</span>
        </div>
        <div class="preview-bubble" v-html="previewContent"></div>
      </div>
      <template #footer>
        <button class="btn btn-outline" @click="previewVisible = false">关闭</button>
        <button class="btn btn-primary" :disabled="saving" @click="previewVisible = false; saveTemplate()">
          <i class="fas fa-check"></i> 确认无误，保存
        </button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  createMessageTemplate,
  updateMessageTemplate,
  getMessageTemplate
} from '@/api/content'

const route = useRoute()
const router = useRouter()

const templateType = route.query.type || 'sms'
const templateId = route.query.id
const isEdit = computed(() => !!templateId)
const loading = ref(false)
const saving = ref(false)

// 触发场景 → 事件编码映射
const sceneToEvent = {
  '接单成功': 'order_success',
  '工资结算': 'settlement',
  '审核结果': 'realname_approved',
  '系统公告': 'system',
  '飞单提醒': 'system',
  '手动发送': 'system'
}

const form = ref({
  type: templateType,
  name: '',
  scene: '手动发送',
  status: 'enabled',
  content: '',
  sendWay: '即时',
  scheduledTime: '',
  sendTime: '全天',
  freqLimit: '5'
})

const templateCode = computed(() => {
  if (templateId) return `${form.value.type.toUpperCase()}-${templateId}`
  return '保存后自动生成'
})

const contentLength = computed(() => (form.value.content || '').length)

const variableList = [
  { code: '{用户名}', desc: '接收通知的用户昵称' },
  { code: '{订单编号}', desc: '关联订单的编号' },
  { code: '{时间}', desc: '事件发生时间或工作时间' },
  { code: '{地点}', desc: '工作地点地址' },
  { code: '{金额}', desc: '结算金额（元）' },
  { code: '{审核类型}', desc: '提交审核的类型名称' },
  { code: '{审核结果}', desc: '审核通过 / 审核不通过' },
  { code: '{积分}', desc: '扣除的积分数量' },
  { code: '{次数}', desc: '累计发生次数' }
]

const sampleValues = {
  '用户名': '张师傅',
  '订单编号': 'DD202609010012',
  '时间': '2026-09-02 08:00',
  '地点': '深圳市南山区科技园',
  '金额': '280.00',
  '审核类型': '实名认证',
  '审核结果': '审核通过',
  '积分': '10',
  '次数': '3'
}

const createTime = computed(() => new Date().toLocaleString('zh-CN'))
const updateTime = computed(() => new Date().toLocaleString('zh-CN'))

const previewVisible = ref(false)
const previewContent = computed(() => {
  const content = form.value.content || ''
  return content
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/\{([^}]+)\}/g, (match, key) => {
      const val = sampleValues[key]
      return `<span class="pd-hl">${val !== undefined ? val : match}</span>`
    })
})

const onTypeChange = () => {
  // 类型切换时重置部分状态
}

const insertVariable = (variable) => {
  form.value.content = (form.value.content || '') + variable
}

const updateCount = () => {
  // v-model 自动同步，此处保留用于扩展
}

const openPreview = () => {
  if (!form.value.content.trim()) {
    ElMessage.warning('请先输入模板内容')
    return
  }
  previewVisible.value = true
}

const goBack = () => {
  router.push('/admin/settings?tab=notice')
}

const saveTemplate = async () => {
  if (!form.value.name.trim()) {
    ElMessage.warning('请输入模板名称')
    return
  }
  if (!form.value.content.trim()) {
    ElMessage.warning('请输入模板内容')
    return
  }
  if (contentLength.value > 500) {
    ElMessage.warning('模板内容不能超过500字')
    return
  }
  if (saving.value) return
  saving.value = true
  try {
    const payload = {
      name: form.value.name,
      channel: form.value.type === 'sms' ? 'sms' : 'inapp',
      event: sceneToEvent[form.value.scene] || 'system',
      scene: form.value.scene,
      content: form.value.content,
      status: form.value.status,
      sendWay: form.value.sendWay,
      scheduledTime: form.value.sendWay === '定时' ? form.value.scheduledTime : null,
      sendTime: form.value.sendTime,
      freqLimit: form.value.freqLimit
    }
    if (isEdit.value) {
      await updateMessageTemplate(templateId, payload)
      ElMessage.success('模板更新成功')
    } else {
      await createMessageTemplate(payload)
      ElMessage.success('模板创建成功')
    }
    goBack()
  } catch (e) {
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const loadTemplate = async () => {
  if (!isEdit.value) return
  loading.value = true
  try {
    const res = await getMessageTemplate(templateId)
    const tpl = res.data || res
    if (tpl) {
      form.value = {
        ...form.value,
        type: tpl.channel === 'sms' ? 'sms' : 'message',
        name: tpl.name || '',
        scene: tpl.scene || '手动发送',
        content: tpl.content || '',
        status: tpl.status || 'enabled',
        sendWay: tpl.sendWay || '即时',
        scheduledTime: tpl.scheduledTime || '',
        sendTime: tpl.sendTime || '全天',
        freqLimit: tpl.freqLimit || '5'
      }
    }
  } catch (e) {
    console.warn('[TemplateEdit] 加载模板失败:', e)
    ElMessage.error('加载模板失败，请返回重试')
  } finally {
    loading.value = false
  }
}

onMounted(loadTemplate)
</script>

<style scoped>
.edit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.edit-header-left { display: flex; align-items: center; gap: 12px; }
.edit-header-info h2 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 4px 0;
}
.edit-header-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 8px;
}
.edit-id-badge {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  background: #F3F4F6;
  color: var(--text-secondary);
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.5;
}
.tag {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.5;
}
.tag-blue { background: #EFF6FF; color: #2563EB; }
.tag-green { background: #F0FDF4; color: #10B981; }
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.5;
}
.status-badge::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
  flex-shrink: 0;
}
.status-badge.success { background: #D1FAE5; color: #059669; }
.status-badge.disabled { background: #E5E7EB; color: #6B7280; }

.header-actions { display: flex; gap: 10px; }

.edit-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 20px;
  align-items: start;
}
.left-col, .right-col { display: flex; flex-direction: column; gap: 16px; }

.card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.form-section { padding: 20px 24px; }
.form-section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  gap: 8px;
}
.section-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: var(--primary);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px 24px;
}
.form-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 6px;
}
.form-label .required { color: #ef4444; margin-left: 2px; }

.status-radio { display: flex; gap: 20px; margin-top: 4px; }
.status-radio label { display: flex; align-items: center; gap: 6px; cursor: pointer; }

.content-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding: 10px;
  border: 1px solid var(--border);
  border-bottom: none;
  border-radius: 6px 6px 0 0;
  background: var(--bg-page);
}
.toolbar-label { font-size: 12px; color: var(--text-muted); }
.var-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  font-size: 12px;
  color: var(--primary);
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.15s;
}
.var-chip:hover { border-color: var(--primary); background: #FFF8F3; }

.content-editor :deep(.el-textarea__inner) {
  border-radius: 0 0 6px 6px;
  min-height: 150px;
  line-height: 1.8;
  font-size: 14px;
}
.content-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 6px;
}
.over-limit { color: #ef4444; }

.action-bar {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding: 18px 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.info-card { padding: 18px 20px; }
.info-card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.info-card-title i { color: var(--primary); }
.info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid var(--border);
  font-size: 13px;
}
.info-row:last-child { border-bottom: none; }
.info-row-label { color: var(--text-muted); }
.info-row-value { color: var(--text-primary); font-weight: 500; }

.var-list { display: flex; flex-direction: column; gap: 10px; }
.var-row { display: flex; align-items: center; gap: 10px; }
.var-code {
  font-family: monospace;
  font-size: 12px;
  color: var(--primary);
  background: #FFF8F3;
  border: 1px dashed var(--primary);
  border-radius: 4px;
  padding: 2px 8px;
  cursor: pointer;
  white-space: nowrap;
}
.var-desc { font-size: 12px; color: var(--text-secondary); }

.tip-list { display: flex; flex-direction: column; gap: 10px; }
.tip-item { display: flex; gap: 8px; font-size: 12px; color: var(--text-secondary); line-height: 1.5; }
.tip-item i { color: var(--primary); margin-top: 3px; flex-shrink: 0; }

/* 预览弹窗 */
.phone-preview {
  background: #F5F5F5;
  border-radius: 12px;
  padding: 16px;
}
.preview-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 10px;
}
.preview-bubble {
  background: #fff;
  border-radius: 8px;
  padding: 12px 14px;
  font-size: 14px;
  line-height: 1.7;
  color: var(--text-primary);
  box-shadow: 0 1px 2px rgba(0,0,0,0.06);
  word-break: break-all;
}

/* 响应式 */
@media (max-width: 1024px) {
  .edit-layout { grid-template-columns: 1fr; }
}
</style>
