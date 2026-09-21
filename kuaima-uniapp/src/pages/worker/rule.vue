<template>
  <view class="page">
    <view class="top-nav" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="nav-inner">
        <view class="nav-back" @click="goBack">
          <image
            class="nav-back-icon"
            src="/static/icons/worker-rule/chevron-left-gray.svg"
            mode="aspectFit"
          />
        </view>
        <text class="nav-title">平台规则</text>
        <view class="nav-right-spacer" />
      </view>
    </view>

    <scroll-view scroll-y class="rule-content">
      <view
        v-for="item in rules"
        :key="item.type"
        class="rule-card"
        @click="open(item)"
      >
        <text class="rule-title">{{ item.title }}</text>
        <image
          class="rule-arrow"
          src="/static/icons/worker-rule/chevron-right-gray.svg"
          mode="aspectFit"
        />
      </view>
      <view class="bottom-space" />
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";

const statusBarHeight = ref(0);
const rules = [
  { type: "notice", title: "规则公示" },
  { type: "credit", title: "信用分规则" },
  { type: "fee", title: "收费规则" },
  { type: "trade", title: "交易规则" },
  { type: "fly", title: "飞单认定与处理规则" },
];

onLoad(() => {
  try {
    const info =
      typeof uni.getWindowInfo === "function"
        ? uni.getWindowInfo()
        : uni.getSystemInfoSync();
    statusBarHeight.value = Number(info.statusBarHeight || 0);
  } catch (_) {
    statusBarHeight.value = 0;
  }
});

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
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
}

.nav-inner {
  height: 104rpx;
  padding: 0 32rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-sizing: border-box;
}

.nav-back,
.nav-right-spacer {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-back-icon {
  width: 20rpx;
  height: 32rpx;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
}

.rule-content {
  flex: 1;
  box-sizing: border-box;
  background: #f5f5f5;
  padding-top: 24rpx;
}

.rule-card {
  min-height: 112rpx;
  margin: 0 24rpx 28rpx;
  padding: 0 40rpx;
  box-sizing: border-box;
  background: #fff;
  border-radius: 28rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.rule-title {
  font-size: 32rpx;
  font-weight: 500;
  color: #1a1a1a;
}

.rule-arrow {
  width: 10rpx;
  height: 18rpx;
  flex-shrink: 0;
}

.bottom-space {
  height: 24rpx;
}
</style>
