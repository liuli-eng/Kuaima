<template>
  <view class="page">
    <view class="top-nav">
      <view class="nav-back" @click="goBack">
        <text class="nav-back-icon">‹</text>
      </view>
      <view class="nav-title">平台规则</view>
      <view class="nav-right-spacer"></view>
    </view>
    <scroll-view scroll-y class="rule-content">
      <template v-for="item in rules" :key="item.type">
        <view class="rule-card" @click="open(item)">
          <view class="rule-info">
            <view :class="['rule-icon', item.iconClass]">
              <text class="rule-icon-glyph">{{ item.icon }}</text>
            </view>
            <text class="rule-title">{{ item.title }}</text>
          </view>
          <text class="rule-arrow">›</text>
        </view>
        <view class="rule-desc">{{ item.description }}</view>
      </template>
      <view class="bottom-space" />
    </scroll-view>
  </view>
</template>
<script setup>
const rules = [
  { type: "notice", title: "规则公示", description: "平台公告、重要通知", icon: "📢", iconClass: "icon-notice" },
  { type: "credit", title: "信用分规则", description: "信用分评定、奖惩机制", icon: "★", iconClass: "icon-credit" },
  { type: "fee", title: "收费规则", description: "服务费、提现规则说明", icon: "🪙", iconClass: "icon-fee" },
  { type: "trade", title: "交易规则", description: "接单、完工、结算流程", icon: "🔁", iconClass: "icon-trade" },
  { type: "fly", title: "飞单认定与处理规则", description: "违规认定、处罚措施", icon: "🚫", iconClass: "icon-fly" },
];
function goBack() {
  const pages = getCurrentPages();
  if (pages.length > 1) {
    uni.navigateBack();
    return;
  }
  uni.switchTab({ url: "/pages/worker/profile" });
}
function open(item) {
  if (item.type === "credit") {
    uni.navigateTo({ url: "/pages/worker/credit" });
    return;
  }
  uni.navigateTo({
    url: `/pages/worker/rule-detail?type=${item.type}&title=${encodeURIComponent(item.title)}`,
  });
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}
.top-nav {
  padding: 16rpx 32rpx 24rpx;
  padding-top: calc(16rpx + var(--status-bar-height, 44rpx));
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
}
.nav-back {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.nav-back-icon {
  font-size: 56rpx;
  color: #333;
  line-height: 1;
  font-weight: 500;
}
.nav-title {
  flex: 1;
  text-align: center;
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
}
.nav-right-spacer {
  width: 64rpx;
  height: 64rpx;
}
.rule-content {
  flex: 1;
  box-sizing: border-box;
  background: #f5f5f5;
}
.rule-card {
  background: #fff;
  margin: 24rpx 32rpx;
  border-radius: 24rpx;
  padding: 36rpx 40rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.rule-info {
  display: flex;
  align-items: center;
  gap: 28rpx;
}
.rule-icon {
  width: 88rpx;
  height: 88rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.rule-icon-glyph {
  font-size: 40rpx;
  line-height: 1;
}
.icon-notice { background: linear-gradient(135deg, #ffe4b5, #ffd966); }
.icon-notice .rule-icon-glyph { color: #d2691e; }
.icon-credit { background: linear-gradient(135deg, #e3f2fd, #bbdefb); }
.icon-credit .rule-icon-glyph { color: #1976d2; }
.icon-fee { background: linear-gradient(135deg, #e8f5e9, #c8e6c9); }
.icon-fee .rule-icon-glyph { color: #388e3c; }
.icon-trade { background: linear-gradient(135deg, #fce4ec, #f8bbd0); }
.icon-trade .rule-icon-glyph { color: #c2185b; }
.icon-fly { background: linear-gradient(135deg, #f3e5f5, #e1bee7); }
.icon-fly .rule-icon-glyph { color: #7b1fa2; }
.rule-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}
.rule-arrow {
  color: #ccc;
  font-size: 36rpx;
  line-height: 1;
}
.rule-desc {
  padding: 0 40rpx;
  font-size: 24rpx;
  color: #999;
  margin-top: -8rpx;
  margin-bottom: 24rpx;
}
.bottom-space { height: 40rpx; }
</style>
