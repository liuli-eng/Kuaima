<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">认证审核</h1>
      <p class="page-desc">审核用户实名认证和企业认证申请</p>
    </div>

    <div class="card">
      <el-tabs v-model="activeTab">
        <el-tab-pane :label="`零工认证 (${workerCount})`" name="worker">
          <CertificationList type="worker" />
        </el-tab-pane>
        <el-tab-pane :label="`雇主认证 (${bossCount})`" name="boss">
          <CertificationList type="boss" />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listCertifications } from '@/api/content'
import CertificationList from './components/CertificationList.vue'

const activeTab = ref('worker')
const workerCount = ref(56)
const bossCount = ref(33)

const loadCounts = async () => {
  try {
    const res = await listCertifications()
    const list = Array.isArray(res) ? res : (res?.data || [])
    workerCount.value = list.filter(c => c.type === '零工认证').length || 56
    bossCount.value = list.filter(c => c.type === '雇主认证').length || 33
  } catch (e) {
    console.warn('[API] listCertifications 后端暂未接入，使用默认计数')
  }
}

onMounted(loadCounts)
</script>
