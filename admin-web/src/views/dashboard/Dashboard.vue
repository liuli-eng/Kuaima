<template>
  <div>
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">数据概览</h1>
      <p class="page-desc">实时监控平台核心运营数据，掌握业务动态趋势</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div v-for="card in statCards" :key="card.title" class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">{{ card.title }}</span>
          <div :class="['stat-card-icon', card.iconClass]">
            <i :class="['fas', card.icon]"></i>
          </div>
        </div>
        <div class="stat-card-value">{{ card.value }}</div>
        <div :class="['stat-card-change', card.up ? 'up' : 'down']">
          <i :class="['fas', card.up ? 'fa-arrow-up' : 'fa-arrow-down']"></i>
          <span>较上月 {{ card.change }}</span>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="content-grid-2col">
      <!-- 订单趋势 -->
      <div class="card">
        <div class="card-header">
          <span class="card-title">订单趋势</span>
          <el-select v-model="trendRange" size="small" style="width: 100px;">
            <el-option label="近7天" value="7d" />
            <el-option label="近30天" value="30d" />
            <el-option label="本月" value="month" />
          </el-select>
        </div>
        <div ref="trendChart" class="chart-container"></div>
      </div>

      <!-- 工种分布 -->
      <div class="card">
        <div class="card-header">
          <span class="card-title">工种分布</span>
          <router-link to="/jobs" class="card-action">查看详情</router-link>
        </div>
        <div class="donut-wrapper">
          <div ref="donutChart" class="donut-chart"></div>
          <div class="chart-legend">
            <div v-for="item in jobTypeData" :key="item.name" class="chart-legend-item">
              <div class="legend-left">
                <span class="legend-dot" :style="{ background: item.color }"></span>
                <span>{{ item.name }}</span>
              </div>
              <span class="legend-value">{{ item.value }}%</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 实时订单表格 -->
    <div class="card">
      <div class="card-header">
        <span class="card-title">实时订单</span>
        <div style="display: flex; gap: 12px; align-items: center;">
          <el-button link type="primary" :underline="false">刷新</el-button>
          <router-link to="/orders" class="card-action">
            查看全部 <i class="fas fa-arrow-right" style="font-size:11px; margin-left:4px;"></i>
          </router-link>
        </div>
      </div>
      
      <el-table :data="recentOrders" stripe style="width: 100%" :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }">
        <el-table-column prop="id" label="订单号" width="160">
          <template #default="{ row }">
            <span style="color: var(--primary); font-family: monospace;">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="employer" label="雇主" min-width="180" />
        <el-table-column prop="job" label="工种" min-width="140" />
        <el-table-column prop="worker" label="零工" width="140">
          <template #default="{ row }">
            <div class="worker-cell">
              <span class="mini-avatar" :style="{ background: row.avatarColor }">{{ row.avatarLetter }}</span>
              <span>{{ row.worker }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">
            <span style="font-weight: 600;">{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <span :class="['status-badge', row.statusClass]">{{ row.status }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="时间" width="160" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default>
            <el-button link type="primary" size="small">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <div class="pagination-info">共 1,856 条订单记录</div>
        <el-pagination
          background
          layout="prev, pager, next"
          :total="1856"
          :page-size="10"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { getStats } from '@/api/dashboard'

// statCards：后端接口填充 value，其余字段保留原展示元数据
// 后端 Dashboard 字段：workerTotal, bossTotal, orderTotal, settledTotal, pendingAudit
const statCards = reactive([
  { title: '零工总数', value: '-', change: '+12.5%', up: true, icon: 'fa-user-friends', iconClass: '' },
  { title: '雇主总数', value: '-', change: '+8.3%', up: true, icon: 'fa-building', iconClass: 'blue' },
  { title: '今日订单', value: '-', change: '+23.1%', up: true, icon: 'fa-clipboard-check', iconClass: 'green' },
  // 注意：后端 settledTotal 是「已结算订单数」，不是营收金额；暂无营收接口
  { title: '已结算', value: '-', change: '+5.2%', up: true, icon: 'fa-coins', iconClass: 'yellow' },
  { title: '待审核', value: '-', change: '需及时处理', up: true, icon: 'fa-clock', iconClass: 'yellow' }
])

// 以下数据暂未有后端 API，保留为本地常量占位
const orderTrendData = {
  labels: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
  values: [120, 145, 168, 142, 190, 210, 185]
}
const jobTypeData = [
  { name: '电子厂', value: 40, color: '#FF6B35' },
  { name: '物流', value: 25, color: '#2563EB' },
  { name: '餐饮', value: 20, color: '#10B981' },
  { name: '其他', value: 15, color: '#F59E0B' }
]
const recentOrders = [
  { id: 'KM20240315001', employer: '深圳富士康科技集团', job: '电子厂装配工', worker: '张建国', amount: '¥280', status: '已完成', statusClass: 'success', time: '2024-03-15 18:30', avatarColor: 'linear-gradient(135deg,#FF8C42,#FF6B35)', avatarLetter: '张' },
  { id: 'KM20240315002', employer: '顺丰速运有限公司', job: '快递分拣员', worker: '李美丽', amount: '¥220', status: '进行中', statusClass: 'info', time: '2024-03-15 17:45', avatarColor: 'linear-gradient(135deg,#3B82F6,#2563EB)', avatarLetter: '李' },
  { id: 'KM20240315003', employer: '肯德基餐饮管理', job: '餐厅服务员', worker: '王小刚', amount: '¥180', status: '待处理', statusClass: 'warning', time: '2024-03-15 17:20', avatarColor: 'linear-gradient(135deg,#10B981,#059669)', avatarLetter: '王' },
  { id: 'KM20240315004', employer: '京东物流仓储', job: '仓库理货员', worker: '陈大海', amount: '¥320', status: '已完成', statusClass: 'success', time: '2024-03-15 16:50', avatarColor: 'linear-gradient(135deg,#8B5CF6,#6D28D9)', avatarLetter: '陈' },
  { id: 'KM20240315005', employer: '比亚迪汽车工业', job: '电子厂操作工', worker: '刘芳', amount: '¥260', status: '进行中', statusClass: 'info', time: '2024-03-15 16:15', avatarColor: 'linear-gradient(135deg,#F59E0B,#D97706)', avatarLetter: '刘' },
  { id: 'KM20240315006', employer: '麦当劳食品有限公司', job: '后厨助手', worker: '赵小红', amount: '¥160', status: '待处理', statusClass: 'warning', time: '2024-03-15 15:40', avatarColor: 'linear-gradient(135deg,#EC4899,#BE185D)', avatarLetter: '赵' },
  { id: 'KM20240315007', employer: '顺丰速运有限公司', job: '司机', worker: '周志华', amount: '¥450', status: '已完成', statusClass: 'success', time: '2024-03-15 14:30', avatarColor: 'linear-gradient(135deg,#06B6D4,#0891B2)', avatarLetter: '周' },
  { id: 'KM20240315008', employer: '格力电器制造', job: '装配钳工', worker: '吴志强', amount: '¥300', status: '已取消', statusClass: 'danger', time: '2024-03-15 13:20', avatarColor: 'linear-gradient(135deg,#64748B,#475569)', avatarLetter: '吴' }
]

const trendRange = ref('7d')
const trendChart = ref(null)
const donutChart = ref(null)

let trendChartInstance = null
let donutChartInstance = null

// 加载统计卡片真实数据
const loadStats = async () => {
  try {
    const result = await getStats()
    const d = result.data || {}
    // 映射后端字段 → statCards.value
    statCards[0].value = formatNumber(d.workerTotal)   // 零工总数
    statCards[1].value = formatNumber(d.bossTotal)     // 雇主总数
    statCards[2].value = formatNumber(d.orderTotal)    // 今日订单
    statCards[3].value = formatNumber(d.settledTotal)  // 已结算（后端 settledTotal）
    statCards[4].value = formatNumber(d.pendingAudit)  // 待审核
  } catch (e) {
    console.warn('[Dashboard] 加载统计数据失败:', e)
  }
}

const formatNumber = (n) => {
  if (n == null || n === undefined || Number.isNaN(Number(n))) return '-'
  return Number(n).toLocaleString()
}

const initTrendChart = () => {
  if (!trendChart.value) return
  
  trendChartInstance = echarts.init(trendChart.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(0,0,0,0.8)',
      borderWidth: 0,
      textStyle: { color: '#fff', fontSize: 12 }
    },
    grid: {
      left: '10%',
      right: '5%',
      top: '5%',
      bottom: '15%'
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: orderTrendData.labels,
      axisLine: { lineStyle: { color: '#E5E7EB' } },
      axisLabel: { color: '#9CA3AF', fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#F3F4F6' } },
      axisLabel: { color: '#9CA3AF', fontSize: 12 }
    },
    series: [
      {
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { color: '#FF6B35', width: 2.5 },
        itemStyle: { color: '#FF6B35', borderColor: '#fff', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255,107,53,0.35)' },
            { offset: 1, color: 'rgba(255,107,53,0)' }
          ])
        },
        label: {
          show: true,
          position: 'top',
          color: '#6B7280',
          fontSize: 11,
          fontWeight: 500
        },
        data: orderTrendData.values
      }
    ]
  }
  trendChartInstance.setOption(option)
}

const initDonutChart = () => {
  if (!donutChart.value) return
  
  donutChartInstance = echarts.init(donutChart.value)
  const option = {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(0,0,0,0.8)',
      borderWidth: 0,
      textStyle: { color: '#fff', fontSize: 12 },
      formatter: '{b}: {c}%'
    },
    series: [
      {
        type: 'pie',
        radius: ['55%', '75%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: false,
        label: { show: false },
        labelLine: { show: false },
        itemStyle: {
          borderColor: '#fff',
          borderWidth: 2
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0,0,0,0.2)'
          }
        },
        data: jobTypeData
      }
    ]
  }
  donutChartInstance.setOption(option)
}

const handleResize = () => {
  trendChartInstance?.resize()
  donutChartInstance?.resize()
}

onMounted(() => {
  loadStats()
  initTrendChart()
  initDonutChart()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChartInstance?.dispose()
  donutChartInstance?.dispose()
})
</script>

<style scoped>
.chart-container {
  height: 280px;
}

.donut-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-around;
  gap: 20px;
  padding: 10px 0;
}

.donut-chart {
  width: 200px;
  height: 200px;
}

.chart-legend {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
}

.chart-legend-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
}

.legend-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 3px;
}

.legend-value {
  font-weight: 600;
  color: var(--text-primary);
}

.mini-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  margin-right: 8px;
}

.worker-cell {
  display: flex;
  align-items: center;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
  
  .pagination-info {
    font-size: 13px;
    color: var(--text-secondary);
  }
}
</style>
