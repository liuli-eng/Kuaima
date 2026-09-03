<template>
  <view class="container">
    <!-- 状态栏 -->
    <view class="status-bar">
      <text>19:48</text>
      <view class="status-icons">
        <text class="fa-solid fa-signal"></text>
        <text class="fa-solid fa-wifi"></text>
        <text class="fa-solid fa-battery-full"></text>
      </view>
    </view>

    <!-- 顶部导航 -->
    <view class="top-nav">
      <view class="nav-bar">
        <view class="nav-back" @click="goBack">
          <text class="fa-solid fa-chevron-left" style="color:#333;"></text>
        </view>
        <text class="nav-title">黑名单</text>
        <view class="nav-right">
          <view class="nav-btn">
            <text class="fa-solid fa-ellipsis" style="color:#333;font-size:26rpx;"></text>
          </view>
          <view style="width:2rpx;height:28rpx;background:#ddd;margin:0 4rpx;"></view>
          <view class="nav-btn">
            <text class="fa-solid fa-circle" style="color:#333;font-size:22rpx;"></text>
          </view>
        </view>
      </view>
    </view>

    <!-- 内容区 -->
    <scroll-view scroll-y class="content-area">
      <!-- 空状态 -->
      <view class="empty-state" v-if="blacklist.length === 0">
        <view class="empty-icon">
          <text class="fa-solid fa-box-open" style="font-size:200rpx;color:#FFE4B5;"></text>
        </view>
        <text class="empty-text">暂无数据</text>
      </view>

      <!-- 黑名单列表 -->
      <view class="blacklist-container" v-else>
        <view class="blacklist-item" v-for="(item, index) in blacklist" :key="index">
          <view class="item-avatar">
            <text class="fa-solid" :class="item.icon" style="font-size:40rpx;color:#FF6B35;"></text>
          </view>
          <view class="item-info">
            <text class="item-name">{{ item.name }}</text>
            <text class="item-reason">拉黑原因：{{ item.reason }}</text>
          </view>
          <view class="item-remove-btn" @click="removeItem(index)">移除</view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      blacklist: [
        { name: '泗泾餐饮', reason: '爽约', icon: 'fa-store' },
        { name: '顺丰分拣中心', reason: '拖欠工资', icon: 'fa-warehouse' }
      ]
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    removeItem(index) {
      uni.showModal({
        title: '提示',
        content: '确定要移除该黑名单吗？',
        success: (res) => {
          if (res.confirm) {
            this.blacklist.splice(index, 1)
          }
        }
      })
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
  height: 94rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 56rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  background: #fff;
}

.status-icons {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.top-nav {
  position: absolute;
  top: 94rpx;
  left: 0;
  right: 0;
  height: 104rpx;
  background: #fff;
  z-index: 10;
}

.nav-bar {
  padding: 16rpx 32rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
}

.nav-back {
  display: flex;
  align-items: center;
  width: 64rpx;
  height: 64rpx;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
}

.nav-right {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 40rpx;
  padding: 8rpx 12rpx;
  gap: 4rpx;
}

.nav-btn {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.content-area {
  position: absolute;
  top: 198rpx;
  left: 0;
  right: 0;
  bottom: 0;
  overflow-y: auto;
  background: #fff;
}

.empty-state {
  text-align: center;
  padding: 240rpx 80rpx 80rpx;
}

.empty-text {
  font-size: 30rpx;
  color: #999;
}

.blacklist-container {
  padding: 24rpx;
}

.blacklist-item {
  display: flex;
  align-items: center;
  padding: 28rpx;
  background: white;
  border-radius: 24rpx;
  margin-bottom: 20rpx;
  border: 2rpx solid #f0f0f0;
}

.item-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFE4B5, #FFD59E);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}

.item-reason {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
}

.item-remove-btn {
  padding: 16rpx 32rpx;
  border-radius: 32rpx;
  font-size: 26rpx;
  background: #FFF1F0;
  color: #FF4D4F;
}
</style>
