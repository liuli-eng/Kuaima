<template>
  <view class="page">
    <AppNavBar title="平台规则" :show-back="true" />
    <view class="list">
      <view
        v-for="item in rules"
        :key="item.title"
        class="row"
        @click="open(item)"
      >
        <text :class="['icon', item.color]">{{ item.icon }}</text
        ><text class="title">{{ item.title }}</text
        ><text class="arrow">›</text>
      </view>
    </view>
  </view>
</template>
<script setup>
import AppNavBar from "@/components/AppNavBar.vue";
const rules = [
  { title: "规则公示", icon: "公", color: "orange" },
  { title: "信用分规则", icon: "信", color: "yellow" },
  { title: "收费规则", icon: "¥", color: "green" },
  { title: "交易规则", icon: "易", color: "blue" },
  { title: "飞单认定与处理规则", icon: "禁", color: "red" },
];
function open(item) {
  uni.navigateTo({
    url: `/pages/worker/rule-detail?title=${encodeURIComponent(item.title)}`,
  });
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f8f4ed;
}
.list {
  margin: 24rpx;
  overflow: hidden;
  border-radius: 20rpx;
  background: #fff;
  box-shadow: 0 5rpx 16rpx rgba(88, 64, 32, 0.05);
}
.row {
  display: flex;
  align-items: center;
  padding: 26rpx 24rpx;
  border-bottom: 1rpx solid #f2eee8;
}
.row:last-child {
  border-bottom: 0;
}
.icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 62rpx;
  height: 62rpx;
  margin-right: 18rpx;
  border-radius: 18rpx;
  font-size: 23rpx;
  font-weight: 700;
}
.orange {
  background: #fff1e6;
  color: #ff6b35;
}
.yellow {
  background: #fffbe6;
  color: #d4a106;
}
.green {
  background: #e8f8e8;
  color: #52c41a;
}
.blue {
  background: #e6f7ff;
  color: #1890ff;
}
.red {
  background: #fff1f0;
  color: #ff4d4f;
}
.title {
  flex: 1;
  color: #333;
  font-size: 27rpx;
  font-weight: 600;
}
.arrow {
  color: #aaa;
  font-size: 34rpx;
}
</style>
