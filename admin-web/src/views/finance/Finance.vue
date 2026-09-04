<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">财务报表</h1>
      <p class="page-desc">平台营收统计、趋势分析和月度对比</p>
    </div>

    <div class="stat-cards">
      <div v-for="stat in financeStats" :key="stat.title" class="stat-card">
        <div class="stat-card-header">
          <span class="stat-card-title">{{ stat.title }}</span>
        </div>
        <div class="stat-card-value">{{ stat.value }}</div>
        <div :class="['stat-card-change', stat.up ? 'up' : 'down']">
          <i :class="['fas', stat.up ? 'fa-arrow-up' : 'fa-arrow-down']"></i>
          <span>较上月 {{ stat.change }}</span>
        </div>
      </div>
    </div>

    <div class="content-grid-2col">
      <div class="card">
        <div class="card-header">
          <span class="card-title">营收趋势</span>
          <el-radio-group v-model="timeRange" size="small">
            <el-radio-button value="week">本周</el-radio-button>
            <el-radio-button value="month">本月</el-radio-button>
            <el-radio-button value="quarter">本季</el-radio-button>
            <el-radio-button value="year">本年</el-radio-button>
          </el-radio-group>
        </div>
        <div ref="revenueChart" class="chart-container"></div>
      </div>

      <div class="card">
        <div class="card-header">
          <span class="card-title">收入类型占比</span>
        </div>
        <div ref="pieChart" class="chart-container"></div>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <span class="card-title">月度营收对比</span>
      </div>
      <div ref="barChart" class="chart-container" style="height: 320px;"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'

const financeStats = ref([
  { title: '累计营收', value: '-', change: '-', up: true },
  { title: '平台收入', value: '-', change: '-', up: true },
  { title: '零工收入', value: '-', change: '-', up: true },
  { title: '雇主支出', value: '-', change: '-', up: true }
])

const revenueTrendData = ref({ labels: [], platform: [], total: [] })

const timeRange = ref('month')
const revenueChart = ref(null)
const pieChart = ref(null)
const barChart = ref(null)

let revenueChartInstance = null
let pieChartInstance = null
let barChartInstance = null

const initRevenueChart = () => {
  if (!revenueChart.value) return
  revenueChartInstance = echarts.init(revenueChart.value)
  revenueChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['平台收入', '总营收'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: revenueTrendData.value.labels },
    yAxis: { type: 'value', name: '万元' },
    series: [
      {
        name: '平台收入',
        type: 'line',
        smooth: true,
        data: revenueTrendData.value.platform,
        itemStyle: { color: '#FF6B35' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255,107,53,0.3)' },
            { offset: 1, color: 'rgba(255,107,53,0)' }
          ])
        }
      },
      {
        name: '总营收',
        type: 'line',
        smooth: true,
        data: revenueTrendData.value.total,
        itemStyle: { color: '#2563EB' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(37,99,235,0.3)' },
            { offset: 1, color: 'rgba(37,99,235,0)' }
          ])
        }
      }
    ]
  })
}

const initPieChart = () => {
  if (!pieChart.value) return
  pieChartInstance = echarts.init(pieChart.value)
  pieChartInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}万元 ({d}%)' },
    legend: { orient: 'vertical', right: '5%', top: 'center' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['35%', '50%'],
      data: []
    }]
  })
}

const initBarChart = () => {
  if (!barChart.value) return
  barChartInstance = echarts.init(barChart.value)
  barChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['2023年', '2024年'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: revenueTrendData.value.labels },
    yAxis: { type: 'value', name: '万元' },
    series: [
      {
        name: '2023年',
        type: 'bar',
        data: [],
        itemStyle: { color: '#E5E7EB', borderRadius: [4, 4, 0, 0] }
      },
      {
        name: '2024年',
        type: 'bar',
        data: revenueTrendData.value.total,
        itemStyle: { color: '#FF6B35', borderRadius: [4, 4, 0, 0] }
      }
    ]
  })
}

const handleResize = () => {
  revenueChartInstance?.resize()
  pieChartInstance?.resize()
  barChartInstance?.resize()
}

onMounted(() => {
  initRevenueChart()
  initPieChart()
  initBarChart()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  revenueChartInstance?.dispose()
  pieChartInstance?.dispose()
  barChartInstance?.dispose()
})
</script>

<style scoped>
.chart-container { height: 280px; }
</style>
