<template>
  <view class="page">
    <AppNavBar title="学习平台规则" :show-back="true" />

    <scroll-view scroll-y class="content">
      <view
        v-for="rule in rules"
        :key="rule.type"
        class="rule-card"
        hover-class="rule-card-active"
        hover-stay-time="100"
        @click="openRule(rule)"
      >
        <text class="rule-name">{{ rule.title }}</text>
        <view class="rule-arrow" />
      </view>
      <view class="bottom-space" />
    </scroll-view>
  </view>
</template>

<script setup>
import AppNavBar from "@/components/AppNavBar.vue";

const rules = [
  { type: "credit", title: "信用分规则" },
  { type: "trade", title: "交易规则" },
  { type: "fly", title: "飞单认定与处理" },
  { type: "fee", title: "收费规则" },
];

function openRule(rule) {
  uni.navigateTo({
    url: `/pages/worker/rule-detail?type=${encodeURIComponent(rule.type)}&title=${encodeURIComponent(rule.title)}`,
  });
}
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: #f5f5f5;
}

.content {
  flex: 1;
  min-height: 0;
  box-sizing: border-box;
}

.rule-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 24rpx 28rpx;
  padding: 40rpx;
  border-radius: 28rpx;
  background: #fff;
  box-sizing: border-box;
}

.rule-card:first-child {
  margin-top: 24rpx;
}

.rule-card-active {
  background: #f7f7f7;
}

.rule-name {
  color: #1a1a1a;
  font-size: 32rpx;
  font-weight: 500;
  line-height: 1.5;
}

.rule-arrow {
  width: 16rpx;
  height: 16rpx;
  margin-right: 4rpx;
  border-top: 3rpx solid #999;
  border-right: 3rpx solid #999;
  transform: rotate(45deg);
  flex-shrink: 0;
}

.bottom-space {
  height: calc(40rpx + env(safe-area-inset-bottom));
}
</style>
