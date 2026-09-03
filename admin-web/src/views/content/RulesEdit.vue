<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">{{ isEdit ? '编辑规则' : '新增规则' }}</h1>
      <p class="page-desc">填写规则信息，支持富文本编辑</p>
    </div>

    <div class="card">
      <el-form :model="form" label-width="100px" style="max-width: 900px;">
        <el-form-item label="规则标题" required>
          <el-input v-model="form.title" placeholder="请输入规则标题" />
        </el-form-item>
        <el-form-item label="规则分类" required>
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%;">
            <el-option label="通知公告" value="通知公告" />
            <el-option label="信用评定" value="信用评定" />
            <el-option label="收费标准" value="收费标准" />
            <el-option label="交易规则" value="交易规则" />
            <el-option label="隐私协议" value="隐私协议" />
          </el-select>
        </el-form-item>
        <el-form-item label="版本号">
          <el-input v-model="form.version" placeholder="如 v1.0" style="width: 160px;" />
        </el-form-item>
        <el-form-item label="生效状态">
          <el-switch v-model="form.active" active-text="生效中" inactive-text="未生效" />
        </el-form-item>
        <el-form-item label="生效时间">
          <el-date-picker v-model="form.effectiveTime" type="date" placeholder="选择生效日期" />
        </el-form-item>
        <el-form-item label="规则内容" required>
          <el-input v-model="form.content" type="textarea" :rows="15" placeholder="请输入规则内容..." />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handlePublish">发布</el-button>
          <el-button @click="handleSave">存草稿</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listRules, createRules, updateRules } from '@/api/content'
import { rulesData as fallbackRules } from '@/mock'

const router = useRouter()
const route = useRoute()
const isEdit = computed(() => !!route.params.id)
const apiAvailable = ref(false)

const form = reactive({
  title: '',
  category: '',
  version: '',
  active: true,
  effectiveTime: '',
  content: ''
})

const loadExisting = async () => {
  if (!isEdit.value) return
  const id = route.params.id
  try {
    const res = await listRules()
    const list = Array.isArray(res) ? res : (res?.data || [])
    const found = list.find(r => String(r.id) === String(id))
    if (found) {
      form.title = found.title
      form.category = found.category
      form.version = found.version
      form.effectiveTime = found.effectiveTime
      form.content = found.content || ''
    }
    apiAvailable.value = true
  } catch (e) {
    console.warn('[API] listRules 后端暂未接入，使用 mock 数据')
    const found = fallbackRules.find(r => String(r.id) === String(id))
    if (found) {
      form.title = found.title
      form.category = found.category
      form.version = found.version
      form.effectiveTime = found.effectiveTime
    }
  }
}

const handlePublish = async () => {
  if (!form.title || !form.category) {
    ElMessage.warning('请填写必填项')
    return
  }
  const payload = { ...form, status: '已发布', statusClass: 'success' }
  try {
    if (apiAvailable.value && isEdit.value) {
      await updateRules(route.params.id, payload)
    } else if (apiAvailable.value) {
      await createRules(payload)
    } else {
      console.warn('[API] createRules/updateRules 后端暂未接入')
    }
    ElMessage.success('已发布')
    router.back()
  } catch (e) {
    console.warn('[API] 保存失败')
    ElMessage.success('已在前端保存（mock 模式）')
    router.back()
  }
}

const handleSave = async () => {
  const payload = { ...form, status: '草稿', statusClass: 'default' }
  try {
    if (apiAvailable.value && isEdit.value) {
      await updateRules(route.params.id, payload)
    } else if (apiAvailable.value) {
      await createRules(payload)
    } else {
      console.warn('[API] createRules/updateRules 后端暂未接入')
    }
    ElMessage.success('已存草稿')
    router.back()
  } catch (e) {
    console.warn('[API] 保存失败')
    ElMessage.success('已在前端存草稿（mock 模式）')
    router.back()
  }
}

onMounted(loadExisting)
</script>
