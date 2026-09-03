<template>
  <div>
    <div class="page-header">
      <div style="display: flex; align-items: center; gap: 12px;">
        <el-button text @click="router.back()"><i class="fas fa-arrow-left"></i></el-button>
        <div>
          <h1 class="page-title">认证详情</h1>
          <p class="page-desc">审核ID：{{ route.params.id }}</p>
        </div>
      </div>
    </div>

    <div class="content-grid" style="grid-template-columns: 2fr 1fr;">
      <div class="card">
        <div class="card-header">
          <span class="card-title">申请人信息</span>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ detail.applicant }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detail.phone || '138****8888' }}</el-descriptions-item>
          <el-descriptions-item label="认证类型">{{ detail.type }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ detail.applyTime }}</el-descriptions-item>
          <el-descriptions-item label="身份证号" :span="2">{{ detail.idCard || '440***********1234' }}</el-descriptions-item>
          <el-descriptions-item label="身份证正面">
            <div class="id-card-box"><i class="fas fa-id-card"></i></div>
          </el-descriptions-item>
          <el-descriptions-item label="身份证反面">
            <div class="id-card-box"><i class="fas fa-id-card"></i></div>
          </el-descriptions-item>
          <el-descriptions-item label="人脸识别" :span="2">
            <div class="id-card-box" style="width: 120px;"><i class="fas fa-user-circle"></i></div>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="card">
        <div class="card-header">
          <span class="card-title">认证流程</span>
        </div>
        <el-steps direction="vertical" :active="2" process-status="success" finish-status="success">
          <el-step title="提交申请" :description="detail.applyTime" />
          <el-step title="证件上传" description="自动完成" />
          <el-step title="人脸识别" description="自动完成" />
          <el-step title="人工审核" description="进行中" />
        </el-steps>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <span class="card-title">审核操作</span>
      </div>
      <el-form :model="auditForm" label-width="100px" style="max-width: 600px;">
        <el-form-item label="审核备注">
          <el-input type="textarea" v-model="auditForm.remark" :rows="3" placeholder="请输入审核备注（可选）" />
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="handleApprove">通过</el-button>
          <el-button type="danger" @click="handleReject">拒绝</el-button>
          <el-button @click="router.back()">返回</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listCertifications, auditCertPass, auditCertReject } from '@/api/content'
import { certificationData as fallbackCerts } from '@/mock'

const router = useRouter()
const route = useRoute()
const apiAvailable = ref(false)

const detail = reactive({
  applicant: '张建国',
  phone: '138****8888',
  type: '零工实名认证',
  applyTime: '2024-03-15 10:30',
  idCard: '440***********1234'
})

const auditForm = reactive({ remark: '' })

const loadDetail = async () => {
  const id = route.params.id
  try {
    const res = await listCertifications()
    const list = Array.isArray(res) ? res : (res?.data || [])
    const found = list.find(c => String(c.id) === String(id))
    if (found) {
      detail.applicant = found.applicant
      detail.type = found.type
      detail.applyTime = found.applyTime
    }
    apiAvailable.value = true
  } catch (e) {
    console.warn('[API] listCertifications 后端暂未接入，使用 mock 数据')
    const found = fallbackCerts.find(c => String(c.id) === String(id))
    if (found) {
      detail.applicant = found.applicant
      detail.type = found.type
      detail.applyTime = found.applyTime
    }
  }
}

const handleApprove = async () => {
  try {
    if (apiAvailable.value) {
      await auditCertPass(route.params.id)
    } else {
      console.warn('[API] auditCertPass 后端暂未接入')
    }
    ElMessage.success('审核通过')
    router.back()
  } catch (e) {
    console.warn('[API] auditCertPass 请求失败')
    ElMessage.success('审核通过（mock 模式）')
    router.back()
  }
}

const handleReject = async () => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝审核', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入拒绝原因'
    }).catch(() => ({ value: '' }))
    if (apiAvailable.value) {
      await auditCertReject(route.params.id, reason || '不符合认证要求')
    } else {
      console.warn('[API] auditCertReject 后端暂未接入')
    }
    ElMessage.success('已拒绝')
    router.back()
  } catch (e) {
    if (e !== 'cancel') {
      console.warn('[API] auditCertReject 请求失败')
      ElMessage.success('已拒绝（mock 模式）')
      router.back()
    }
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.id-card-box {
  width: 200px;
  height: 120px;
  border: 1px dashed var(--border);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-page);
  color: var(--text-muted);
  font-size: 32px;
}
</style>
