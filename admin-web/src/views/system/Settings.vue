<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">系统设置</h1>
      <p class="page-desc">配置平台基本参数和运营规则</p>
    </div>

    <div class="card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="平台设置" name="platform">
          <el-form :model="platformForm" label-width="120px" style="max-width: 600px; margin-top: 20px;">
            <el-form-item label="平台名称">
              <el-input v-model="platformForm.name" />
            </el-form-item>
            <el-form-item label="客服电话">
              <el-input v-model="platformForm.phone" />
            </el-form-item>
            <el-form-item label="客服邮箱">
              <el-input v-model="platformForm.email" />
            </el-form-item>
            <el-form-item label="工作时间">
              <el-time-picker is-range v-model="platformForm.workTime" range-separator="至" start-placeholder="开始" end-placeholder="结束" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSavePlatform">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="通知设置" name="notification">
          <el-form :model="notificationForm" label-width="120px" style="max-width: 600px; margin-top: 20px;">
            <el-form-item label="短信通知">
              <el-switch v-model="notificationForm.sms" />
            </el-form-item>
            <el-form-item label="站内信通知">
              <el-switch v-model="notificationForm.inSite" />
            </el-form-item>
            <el-form-item label="邮件通知">
              <el-switch v-model="notificationForm.email" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveNotification">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="安全设置" name="security">
          <el-form :model="securityForm" label-width="120px" style="max-width: 600px; margin-top: 20px;">
            <el-form-item label="密码最小长度">
              <el-input-number v-model="securityForm.pwdMinLength" :min="6" :max="20" />
            </el-form-item>
            <el-form-item label="验证码开关">
              <el-switch v-model="securityForm.captcha" />
            </el-form-item>
            <el-form-item label="登录失败锁定">
              <el-switch v-model="securityForm.lockOnFail" />
            </el-form-item>
            <el-form-item v-if="securityForm.lockOnFail" label="锁定阈值">
              <el-input-number v-model="securityForm.lockThreshold" :min="3" :max="10" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveSecurity">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="积分设置" name="points">
          <el-form :model="pointsForm" label-width="120px" style="max-width: 600px; margin-top: 20px;">
            <el-form-item label="积分比例">
              <el-input v-model="pointsForm.ratio" />
              <div class="text-muted" style="font-size: 12px; margin-top: 4px;">1元 = ? 积分</div>
            </el-form-item>
            <el-form-item label="积分有效期">
              <el-input v-model="pointsForm.validity" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSavePoints">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSettingsByCategory, saveSetting } from '@/api/system'

const activeTab = ref('platform')
const apiAvailable = ref(false)

const platformForm = reactive({
  name: '快马日结',
  phone: '400-888-8888',
  email: 'service@kuaima.com',
  workTime: ['09:00', '22:00']
})

const notificationForm = reactive({
  sms: true,
  inSite: true,
  email: false
})

const securityForm = reactive({
  pwdMinLength: 8,
  captcha: true,
  lockOnFail: true,
  lockThreshold: 5
})

const pointsForm = reactive({
  ratio: '1元 = 10积分',
  validity: '积分永久有效'
})

const loadSettings = async () => {
  try {
    const res = await getSettingsByCategory('platform')
    if (res && typeof res === 'object') {
      platformForm.name = res.name || platformForm.name
      platformForm.phone = res.phone || platformForm.phone
      platformForm.email = res.email || platformForm.email
    }
    apiAvailable.value = true
  } catch (e) {
    console.warn('[API] getSettingsByCategory 后端暂未接入，使用默认设置')
  }
}

const handleSave = async (category, form, keyMappings) => {
  try {
    if (apiAvailable.value) {
      for (const [formKey, apiKey] of Object.entries(keyMappings)) {
        await saveSetting(apiKey, String(form[formKey]), '')
      }
    } else {
      console.warn(`[API] saveSetting(${category}) 后端暂未接入`)
    }
    ElMessage.success('保存成功')
  } catch (e) {
    console.warn(`[API] saveSetting(${category}) 请求失败`)
    ElMessage.success('已在前端保存（mock 模式）')
  }
}

const handleSavePlatform = () => handleSave('platform', platformForm, {
  name: 'platform.name',
  phone: 'platform.phone',
  email: 'platform.email'
})
const handleSaveNotification = () => handleSave('notification', notificationForm, {
  sms: 'notification.sms',
  inSite: 'notification.inSite',
  email: 'notification.email'
})
const handleSaveSecurity = () => handleSave('security', securityForm, {
  pwdMinLength: 'security.pwdMinLength',
  captcha: 'security.captcha',
  lockOnFail: 'security.lockOnFail',
  lockThreshold: 'security.lockThreshold'
})
const handleSavePoints = () => handleSave('points', pointsForm, {
  ratio: 'points.ratio',
  validity: 'points.validity'
})

onMounted(loadSettings)
</script>
