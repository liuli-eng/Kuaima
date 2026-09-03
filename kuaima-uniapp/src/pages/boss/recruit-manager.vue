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
      <text class="nav-title">专属招工经理</text>
      <view class="nav-right">
        <text>📞</text>
      </view>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <!-- 经理卡片 -->
      <view class="manager-card">
        <view class="manager-head">
          <view class="manager-avatar">李</view>
          <view class="manager-info">
            <text class="manager-name">李经理</text>
            <text class="manager-title">高级客户经理 · 服务500+企业</text>
          </view>
        </view>
        <view class="manager-contact">
          <button class="contact-btn btn-phone" @click="callPhone">
            <text>📞</text>
            <text>电话</text>
          </button>
          <button class="contact-btn btn-chat" @click="startChat">
            <text>💬</text>
            <text>私信</text>
          </button>
        </view>
      </view>

      <!-- 服务列表 -->
      <view class="service-list">
        <text class="service-title">提供服务</text>
        <view class="service-item">
          <view class="service-icon">
            <text style="color:#FF6B35;">✅</text>
          </view>
          <text class="service-desc"><text style="font-weight:600;color:#333;">岗位推荐：</text>根据企业需求精准推荐合适零工</text>
        </view>
        <view class="service-item">
          <view class="service-icon">
            <text style="color:#FF6B35;">📅</text>
          </view>
          <text class="service-desc"><text style="font-weight:600;color:#333;">招工方案：</text>协助制定招工计划和薪酬方案</text>
        </view>
        <view class="service-item">
          <view class="service-icon">
            <text style="color:#FF6B35;">🎧</text>
          </view>
          <text class="service-desc"><text style="font-weight:600;color:#333;">全程跟踪：</text>订单跟进、纠纷调解、结算保障</text>
        </view>
        <view class="service-item">
          <view class="service-icon">
            <text style="color:#FF6B35;">📈</text>
          </view>
          <text class="service-desc"><text style="font-weight:600;color:#333;">数据支持：</text>提供行业薪资报告和用工分析</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {}
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    callPhone() {
      uni.makePhoneCall({ phoneNumber: '400-188-6610' })
    },
    startChat() {
      uni.showToast({ title: '发起聊天', icon: 'none' })
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

.nav-bar {
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  background: #fff;
}

.nav-back {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
}

.nav-right {
  display: flex;
  gap: 28rpx;
  color: #333;
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
}

.manager-card {
  background: #fff;
  margin: 24rpx 32rpx;
  border-radius: 24rpx;
  padding: 32rpx;
}

.manager-head {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.manager-avatar {
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  font-size: 40rpx;
}

.manager-info {
  flex: 1;
}

.manager-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.manager-title {
  font-size: 24rpx;
  color: #FF6B35;
  margin-top: 4rpx;
}

.manager-contact {
  display: flex;
  gap: 20rpx;
  margin-top: 20rpx;
}

.contact-btn {
  flex: 1;
  padding: 16rpx;
  border-radius: 16rpx;
  font-size: 24rpx;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  border: none;
}

.btn-phone {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  color: #fff;
}

.btn-chat {
  background: #fff;
  color: #666;
  border: 2rpx solid #ddd;
}

.service-list {
  background: #fff;
  margin: 24rpx 32rpx;
  border-radius: 24rpx;
  padding: 32rpx;
}

.service-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 24rpx;
  display: block;
}

.service-item {
  display: flex;
  gap: 20rpx;
  padding: 16rpx 0;
}

.service-icon {
  width: 64rpx;
  height: 64rpx;
  background: #FFF3ED;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.service-desc {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
}
</style>
