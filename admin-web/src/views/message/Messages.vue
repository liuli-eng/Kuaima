<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">消息管理</h1>
      <p class="page-desc">管理消息模板和推送记录</p>
    </div>

    <div class="card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="消息模板" name="template">
          <div style="margin-bottom: 16px; display: flex; justify-content: space-between;">
            <span class="card-title" style="font-size: 16px;">模板列表</span>
            <el-button type="primary"><i class="fas fa-plus" style="margin-right:4px;"></i>新增模板</el-button>
          </div>
          <el-table :data="messageTemplates" stripe>
            <el-table-column prop="id" label="模板ID" width="100" />
            <el-table-column prop="name" label="模板名称" min-width="180" />
            <el-table-column prop="event" label="触发事件" width="140" />
            <el-table-column prop="channel" label="发送渠道" width="120">
              <template #default="{ row }">
                <el-tag :type="row.channel === '双渠道' ? 'primary' : 'info'" effect="light">{{ row.channel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === '启用' ? 'success' : 'info'" effect="light">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="lastUsed" label="最近使用" width="140" />
            <el-table-column label="操作" width="220">
              <template #default>
                <el-button link type="primary" size="small">编辑</el-button>
                <el-button link type="primary" size="small">预览</el-button>
                <el-button link type="success" size="small">立即推送</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="推送记录" name="record">
          <el-table :data="pushRecords" stripe>
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
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const activeTab = ref('template')

const messageTemplates = ref([])

const pushRecords = ref([])
</script>
