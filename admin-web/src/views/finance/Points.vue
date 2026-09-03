<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">积分管理</h1>
      <p class="page-desc">管理用户积分流水、充值、消费和赠送记录</p>
    </div>

    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">积分总量</span>
          <div class="stat-card-icon yellow"><i class="fas fa-star"></i></div>
        </div>
        <div class="stat-card-value">856,450</div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">本月充值</span>
          <div class="stat-card-icon green"><i class="fas fa-plus"></i></div>
        </div>
        <div class="stat-card-value">28,500</div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">本月消费</span>
          <div class="stat-card-icon"><i class="fas fa-minus"></i></div>
        </div>
        <div class="stat-card-value">15,200</div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">本月赠送</span>
          <div class="stat-card-icon blue"><i class="fas fa-gift"></i></div>
        </div>
        <div class="stat-card-value">8,600</div>
      </div>
    </div>

    <div class="card">
      <div class="filter-bar">
        <el-select v-model="typeFilter" placeholder="积分类型" clearable style="width: 140px;">
          <el-option label="完成订单" value="完成订单" />
          <el-option label="充值" value="充值" />
          <el-option label="消费" value="消费" />
          <el-option label="奖励赠送" value="奖励赠送" />
          <el-option label="信用扣分" value="信用扣分" />
        </el-select>
        <el-select v-model="userFilter" placeholder="用户类型" clearable style="width: 120px;">
          <el-option label="零工" value="零工" />
          <el-option label="雇主" value="雇主" />
        </el-select>
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
        <el-button type="primary">查询</el-button>
        <el-button>重置</el-button>
        <div style="margin-left: auto; display: flex; gap: 8px;">
          <el-button>积分充值</el-button>
          <el-button>积分扣除</el-button>
          <el-button><i class="fas fa-download" style="margin-right:4px;"></i>导出</el-button>
        </div>
      </div>

      <el-table :data="pointsData" stripe :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }">
        <el-table-column prop="id" label="流水ID" width="120" />
        <el-table-column prop="user" label="用户" min-width="160" />
        <el-table-column label="积分变动" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.change.startsWith('+') ? '#10B981' : '#EF4444', fontWeight: 600 }">{{ row.change }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="积分类型" width="120" />
        <el-table-column prop="orderId" label="关联订单" width="140">
          <template #default="{ row }">
            <span v-if="row.orderId !== '-'" style="color: var(--primary); font-family: monospace;">{{ row.orderId }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="变动后余额" width="120">
          <template #default="{ row }">
            <span style="font-weight: 600;">{{ row.balance }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="time" label="时间" width="160" />
      </el-table>

      <div class="pagination">
        <div class="pagination-info">共 {{ pointsData.length }} 条记录</div>
        <el-pagination background layout="total, prev, pager, next, jumper" :total="8560" :page-size="10" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { pointsData } from '@/mock'

console.warn('[Points] 积分管理页面暂无后端 API 覆盖，当前使用 mock 数据。待后端提供 /admin/points/records 接口后可接入。')

const typeFilter = ref('')
const userFilter = ref('')
const dateRange = ref([])
</script>

<style scoped>
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
}
</style>
