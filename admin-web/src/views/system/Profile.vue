<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">个人资料</h1>
      <p class="page-desc">查看和修改您的个人信息</p>
    </div>

    <div class="card" style="max-width: 600px;">
      <el-form :model="form" label-width="100px">
        <el-form-item label="头像">
          <div class="avatar-uploader">
            <div class="avatar-preview">
              <span class="avatar-icon">管</span>
            </div>
            <el-button size="small" style="margin-left: 16px;">更换头像</el-button>
          </div>
        </el-form-item>
        <el-form-item label="管理员账号">
          <el-input v-model="form.account" disabled />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="角色">
          <el-input v-model="form.role" disabled />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="form.dept" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary">保存修改</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const form = reactive({
  account: 'admin',
  name: '管理员',
  role: '超级管理员',
  phone: '138****8888',
  email: 'admin@kuaima.com',
  dept: '技术部'
})
</script>

<style scoped>
.avatar-uploader {
  display: flex;
  align-items: center;
}

.avatar-preview {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF8C42, #FF6B35);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-icon {
  font-size: 28px;
  font-weight: 600;
  color: #fff;
}
</style>
