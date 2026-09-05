<template>
  <view class="container">
    <!-- 状态栏 -->
    <view class="status-bar">
      <text>19:48</text>
      <view class="status-icons">
        <text>📶</text>
        <text>📡</text>
        <text>🔋</text>
      </view>
    </view>

    <!-- 顶部导航 -->
    <view class="top-nav">
      <view class="nav-bar">
        <view class="nav-back" @click="goBack">
          <text style="color:#333;">←</text>
        </view>
        <text class="nav-title">关注老板</text>
        <view class="nav-right">
          <view class="nav-btn">
            <text style="color:#333;font-size:13px;">⋯</text>
          </view>
          <view style="width:1px;height:14px;background:#ddd;margin:0 2px;"></view>
          <view class="nav-btn">
            <text style="color:#333;font-size:11px;">●</text>
          </view>
        </view>
      </view>
    </view>

    <!-- Tab切换 -->
    <view class="tabs">
      <text class="tab-item" :class="{ active: currentTab === 'follow' }" @click="switchTab('follow')">关注老板</text>
      <text class="tab-item" :class="{ active: currentTab === 'boss' }" @click="switchTab('boss')">老雇主</text>
    </view>

    <!-- 内容区 -->
    <scroll-view scroll-y class="content-area" :style="{ top: '147px', bottom: '130px' }">
      <!-- 提示条 -->
      <view class="tip-banner">
        <text class="tip-title">合作过的老雇主已有 0 人</text>
        <text class="tip-text">
          您有机会优先接老雇主发布的订单，老雇主也可能直接邀请您接单，您可以关注老雇主，及时收到老雇主发布的新岗位；多做事，累积更多老雇主，更有机会接好单。
        </text>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="!hasData">
        <view class="empty-icon">
          <text style="font-size:100px;color:#FFE4B5;">📦</text>
        </view>
        <text class="empty-text">暂无相关内容</text>
      </view>

      <!-- 老雇主列表 -->
      <view class="boss-list" v-if="hasData">
        <view class="boss-item" v-for="(boss, index) in bossList" :key="index">
          <view class="boss-avatar">
            <text class="fas" :class="boss.icon" style="font-size:20px;color:#1890FF;"></text>
          </view>
          <view class="boss-info">
            <text class="boss-name">{{ boss.name }}</text>
            <text class="boss-jobs">合作 {{ boss.cooperateCount }} 次 · 最近合作 {{ boss.lastCooperate }}</text>
          </view>
          <view class="boss-action-btn" @click="viewJobs(boss)">查看岗位</view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部按钮 -->
    <view class="bottom-btn" @click="viewAllJobs">查看全部老雇主岗位</view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      currentTab: 'boss',
      hasData: false,
      bossList: [
        { name: '泗泾餐饮', cooperateCount: 3, lastCooperate: '2026-08-15', icon: 'icon-store' },
        { name: '顺丰分拣中心', cooperateCount: 2, lastCooperate: '2026-08-10', icon: 'icon-warehouse' }
      ]
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    switchTab(tab) {
      this.currentTab = tab
      this.hasData = false
    },
    viewJobs(boss) {
      uni.showToast({ title: `查看${boss.name}发布的岗位`, icon: 'none' })
    },
    viewAllJobs() {
      uni.showToast({ title: '查看全部老雇主岗位', icon: 'none' })
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  width: 100%;
  height: 100vh;
  background: #fff;
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

.top-nav {
  position: absolute;
  top: 47px;
  left: 0;
  right: 0;
  height: 52px;
  background: #fff;
  z-index: 10;
}

.nav-bar {
  padding: 8px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
}

.nav-back {
  display: flex;
  align-items: center;
  width: 32px;
  height: 32px;
}

.nav-title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
}

.nav-right {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  padding: 4px 6px;
  gap: 2px;
}

.nav-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tabs {
  display: flex;
  background: white;
  padding: 0 16px;
  border-bottom: 1px solid #f0f0f0;
  position: absolute;
  top: 99px;
  left: 0;
  right: 0;
  height: 48px;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 14px 0;
  font-size: 16px;
  color: #666;
  position: relative;
}

.tab-item.active {
  color: #FF6B35;
  font-weight: 600;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 30px;
  height: 3px;
  background: #FF6B35;
  border-radius: 2px;
}

.content-area {
  position: absolute;
  left: 0;
  right: 0;
  overflow-y: auto;
  background: #f5f5f5;
}

.tip-banner {
  background: linear-gradient(135deg, #FFE4B5 0%, #FFD59E 100%);
  margin: 12px;
  border-radius: 12px;
  padding: 16px;
}

.tip-title {
  font-size: 16px;
  color: #8B4513;
  font-weight: 700;
  margin-bottom: 8px;
  display: block;
}

.tip-text {
  font-size: 14px;
  color: #8B4513;
  line-height: 1.6;
  display: block;
}

.empty-state {
  text-align: center;
  padding: 80px 40px;
}

.empty-text {
  font-size: 15px;
  color: #999;
}

.boss-list {
  padding: 0 12px;
}

.boss-item {
  display: flex;
  align-items: center;
  padding: 14px;
  background: white;
  border-radius: 12px;
  margin-bottom: 10px;
}

.boss-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #E6F7FF, #BAE7FF);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.boss-info {
  flex: 1;
}

.boss-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.boss-jobs {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.boss-action-btn {
  padding: 8px 16px;
  border-radius: 16px;
  font-size: 13px;
  background: #1890FF;
  color: white;
}

.bottom-btn {
  position: absolute;
  bottom: 34px;
  left: 16px;
  right: 16px;
  background: linear-gradient(135deg, #FFE4B5, #FFD700);
  color: #8B4513;
  padding: 16px;
  border-radius: 28px;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
}
</style>
