<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">{{ isEdit ? '编辑招工' : '新增招工' }}</h1>
      <p class="page-desc">填写招工信息，提交后将进入审核流程</p>
    </div>

    <div class="card">
      <el-form :model="form" label-width="120px" style="max-width: 800px;">
        <el-divider content-position="left">基本信息</el-divider>
        
        <el-form-item label="岗位名称" required>
          <el-input v-model="form.name" placeholder="请输入岗位名称" />
        </el-form-item>
        
        <el-form-item label="行业分类" required>
          <el-select v-model="form.category" placeholder="请选择行业分类" style="width: 100%;">
            <el-option label="电子厂" value="电子厂" />
            <el-option label="物流" value="物流" />
            <el-option label="餐饮" value="餐饮" />
            <el-option label="仓储" value="仓储" />
            <el-option label="制造业" value="制造业" />
            <el-option label="汽车" value="汽车" />
            <el-option label="服务业" value="服务业" />
            <el-option label="农业" value="农业" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="薪资标准" required>
          <div style="display: flex; gap: 10px;">
            <el-input v-model="form.price" type="number" placeholder="金额" style="width: 160px;" />
            <el-select v-model="form.priceUnit" placeholder="单位" style="width: 120px;">
              <el-option label="元/天" value="天" />
              <el-option label="元/小时" value="小时" />
              <el-option label="元/件" value="件" />
            </el-select>
          </div>
        </el-form-item>
        
        <el-form-item label="招工人数" required>
          <el-input v-model="form.count" type="number" placeholder="请输入人数" style="width: 200px;" />
        </el-form-item>

        <el-divider content-position="left">工作地点与时间</el-divider>
        
        <el-form-item label="工作地点" required>
          <el-input v-model="form.location" placeholder="请输入详细工作地点" />
        </el-form-item>
        
        <el-form-item label="工作时间" required>
          <el-date-picker v-model="form.timeRange" type="datetimerange" start-placeholder="开始时间" end-placeholder="结束时间" style="width: 100%;" />
        </el-form-item>

        <el-divider content-position="left">岗位要求</el-divider>
        
        <el-form-item label="性别要求" required>
          <el-radio-group v-model="form.gender">
            <el-radio label="不限">不限</el-radio>
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="经验要求" required>
          <el-radio-group v-model="form.experience">
            <el-radio label="不限">不限</el-radio>
            <el-radio label="有经验">有经验</el-radio>
            <el-radio label="无经验">无经验</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="技能要求">
          <el-select v-model="form.skills" multiple placeholder="请选择技能要求" style="width: 100%;">
            <el-option label="装配" value="装配" />
            <el-option label="分拣" value="分拣" />
            <el-option label="搬运" value="搬运" />
            <el-option label="包装" value="包装" />
            <el-option label="质检" value="质检" />
          </el-select>
        </el-form-item>

        <el-divider content-position="left">福利待遇</el-divider>
        
        <el-form-item label="福利待遇">
          <el-checkbox-group v-model="form.benefits">
            <el-checkbox label="包吃住">包吃住</el-checkbox>
            <el-checkbox label="全勤奖">全勤奖</el-checkbox>
            <el-checkbox label="月结">月结</el-checkbox>
            <el-checkbox label="保险">保险</el-checkbox>
            <el-checkbox label="交通补贴">交通补贴</el-checkbox>
            <el-checkbox label="加班补助">加班补助</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-divider content-position="left">岗位描述</el-divider>
        
        <el-form-item label="岗位描述" required>
          <el-input v-model="form.description" type="textarea" :rows="5" placeholder="请详细描述工作内容、要求等" />
        </el-form-item>

        <el-divider content-position="left">联系人信息</el-divider>
        
        <el-form-item label="联系人" required>
          <el-input v-model="form.contact" placeholder="请输入联系人姓名" style="width: 200px;" />
        </el-form-item>
        
        <el-form-item label="联系电话" required>
          <el-input v-model="form.phone" placeholder="请输入联系电话" style="width: 200px;" />
        </el-form-item>

        <el-form-item style="margin-top: 32px;">
          <el-button type="primary" @click="handleSubmit">提交审核</el-button>
          <el-button @click="handleSaveDraft">存草稿</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  name: '',
  category: '',
  price: '',
  priceUnit: '天',
  count: '',
  location: '',
  timeRange: [],
  gender: '不限',
  experience: '不限',
  skills: [],
  benefits: [],
  description: '',
  contact: '',
  phone: ''
})

const handleSubmit = () => {
  ElMessage.success('已提交审核')
  router.push('/job-audit')
}

const handleSaveDraft = () => ElMessage.success('已保存草稿')
const handleCancel = () => router.back()
</script>
