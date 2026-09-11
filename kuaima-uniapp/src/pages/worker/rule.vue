<template>
  <view class="page">
    <view class="top-nav">
      <view class="nav-back" @click="goBack">
        <image :src="chevronLeftIcon" mode="aspectFit" class="nav-back-icon" />
      </view>
      <view class="nav-title">平台规则</view>
      <view class="nav-right-spacer"></view>
    </view>
    <scroll-view scroll-y class="rule-content">
      <view v-for="item in categories" :key="item.category" class="rule-item">
        <view class="rule-card" @click="openCategory(item)">
          <view class="rule-info">
            <view :class="['rule-icon', item.iconClass]">
              <image :src="item.icon" mode="aspectFit" class="rule-icon-image" />
            </view>
            <text class="rule-title">{{ item.title }}</text>
          </view>
          <image :src="chevronRightIcon" mode="aspectFit" class="rule-arrow" />
        </view>
        <view class="rule-desc">{{ item.description }}</view>
      </view>
      <view class="bottom-space" />
    </scroll-view>
  </view>
</template>
<script setup>
import chevronLeftIcon from "/static/icons/worker-rule/chevron-left-gray.svg";
import chevronRightIcon from "/static/icons/worker-rule/chevron-right-gray.svg";
import bullhornIcon from "/static/icons/worker-rule/bullhorn-orange.svg";
import starIcon from "/static/icons/worker-rule/star-blue.svg";
import coinsIcon from "/static/icons/worker-rule/coins-green.svg";
import exchangeIcon from "/static/icons/worker-rule/exchange-pink.svg";
import banIcon from "/static/icons/worker-rule/ban-purple.svg";
const categories = [
  { category: "NOTICE", title: "规则公示", description: "平台公告、重要通知", icon: bullhornIcon, iconClass: "icon-notice" },
  { category: "CREDIT", title: "信用分规则", description: "信用分评定、奖惩机制", icon: starIcon, iconClass: "icon-credit" },
  { category: "FEE", title: "收费规则", description: "服务费、提现规则说明", icon: coinsIcon, iconClass: "icon-fee" },
  { category: "TRADE", title: "交易规则", description: "接单、完工、结算流程", icon: exchangeIcon, iconClass: "icon-trade" },
  { category: "FLY", title: "飞单认定与处理规则", description: "违规认定、处罚措施", icon: banIcon, iconClass: "icon-fly" },
];
function openCategory(category) {
  if (category.category === "CREDIT") {
    uni.navigateTo({ url: "/pages/worker/credit" });
    return;
  }
  uni.navigateTo({
    url: `/pages/worker/rule-detail?category=${encodeURIComponent(category.category)}&title=${encodeURIComponent(category.title)}`,
  });
}
function goBack() {
  const pages = getCurrentPages();
  if (pages.length > 1) {
    uni.navigateBack();
    return;
  }
  uni.switchTab({ url: "/pages/worker/profile" });
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #F3F4F6;
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
  width: 32rpx;
  height: 40rpx;
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
  background: #F3F4F6;
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
.rule-card:active { background: #f9f9f9; }
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
.rule-icon-image { width: 40rpx; height: 40rpx; }
.icon-notice { background: linear-gradient(135deg, #ffe4b5, #ffd966); }
.icon-credit { background: linear-gradient(135deg, #e3f2fd, #bbdefb); }
.icon-fee { background: linear-gradient(135deg, #e8f5e9, #c8e6c9); }
.icon-trade { background: linear-gradient(135deg, #fce4ec, #f8bbd0); }
.icon-fly { background: linear-gradient(135deg, #f3e5f5, #e1bee7); }
.rule-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}
.rule-arrow {
  width: 16rpx;
  height: 28rpx;
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
