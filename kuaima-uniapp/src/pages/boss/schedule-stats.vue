<template>
  <view class="container">
    <!-- 状态栏 -->
    <view class="status-bar">
      <text>19:53</text>
      <view class="status-icons">
        <text>📶</text>
        <text>📡</text>
        <text>🔋</text>
      </view>
    </view>

    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text>←</text>
      </view>
      <text class="nav-title">招工统计</text>
      <view class="nav-right">
        <text>…</text>
      </view>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <!-- 日期选择 -->
      <view class="date-tabs">
        <view 
          class="date-tab" 
          :class="{ active: activeDate === index }"
          v-for="(tab, index) in dateTabs" 
          :key="index"
          @click="activeDate = index"
        >
          <text class="day">{{ tab.day }}</text>
          <text class="date">{{ tab.date }}</text>
          <text class="demand">需求{{ tab.demand }}</text>
        </view>
      </view>

      <!-- 内容区 -->
      <view class="content-area">
        <view class="stats-header">
          <text class="stats-title">需求人数 {{ demandCount }}</text>
          <view class="filter-btn" @click="showFilter">
            <text>筛选</text>
            <text style="font-size:10px;">↓</text>
          </view>
        </view>

        <view class="stats-list">
          <view class="stats-item">
            <text class="stats-label">接单</text>
            <text class="stats-value">0</text>
          </view>
          <view class="stats-item">
            <text class="stats-label">到达</text>
            <text class="stats-value">0</text>
          </view>
          <view class="stats-item">
            <text class="stats-label">开工</text>
            <text class="stats-value">0</text>
          </view>
          <view class="stats-item">
            <text class="stats-label">完工</text>
            <text class="stats-value">0</text>
          </view>
          <view class="stats-item">
            <text class="stats-label">结算</text>
            <text class="stats-value">0</text>
          </view>
        </view>

        <view class="bottom-slogan">
          <text class="slogan-title">招临时工 上快马日结</text>
          <text class="slogan-desc">— 熟练工 上岗快 人靠谱 —</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      activeDate: 0,
      demandCount: 0,
      dateTabs: [
        { day: '周四', date: '20', demand: 0 },
        { day: '周五', date: '21', demand: 0 },
        { day: '周六', date: '22', demand: 0 },
        { day: '周日', date: '23', demand: 0 }
      ]
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    showFilter() {
      uni.showToast({ title: '筛选统计维度', icon: 'none' })
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  width: 100%;
  height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.status-bar {
  height: 47px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 28px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  background: #fff;
}

.status-icons {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-bar {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #fff;
}

.nav-back {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
}

.nav-right {
  display: flex;
  gap: 14px;
  color: #333;
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
}

.date-tabs {
  display: flex;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  padding: 16px 12px 20px;
  gap: 8px;
  overflow-x: auto;
}

.date-tab {
  flex: 1;
  min-width: 70px;
  background: rgba(255,255,255,0.9);
  border-radius: 10px;
  padding: 10px 8px;
  text-align: center;
}

.date-tab.active {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.date-tab .day {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  display: block;
}

.date-tab .date {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
  display: block;
}

.date-tab .demand {
  font-size: 11px;
  color: #FF6B35;
  margin-top: 4px;
  display: block;
}

.date-tab.active .day {
  color: #FF6B35;
}

.content-area {
  margin-top: -8px;
  background: #f5f5f5;
  border-radius: 16px 16px 0 0;
  padding: 16px;
  position: relative;
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.stats-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #FF6B35;
}

.stats-list {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.stats-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f5f5f5;
}

.stats-item:last-child {
  border-bottom: none;
}

.stats-label {
  font-size: 14px;
  color: #666;
}

.stats-value {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.bottom-slogan {
  text-align: center;
  padding: 40px 16px 20px;
}

.slogan-title {
  font-size: 28px;
  font-weight: 800;
  color: #E8D5B7;
  letter-spacing: 4px;
}

.slogan-desc {
  font-size: 13px;
  color: #ccc;
  margin-top: 6px;
}
</style>
