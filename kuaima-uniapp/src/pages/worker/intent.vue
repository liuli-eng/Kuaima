<template>
  <view class="page">
    <AppNavBar title="完善找活意向" :show-back="true" />
    <scroll-view scroll-y class="content">
      <view class="hero"
        ><text class="tag">填写后</text><text class="title">完善找活意向</text
        ><text class="tip">找活成功率提升200%</text></view
      >
      <view class="chart"
        ><view class="line"><text class="start" /><text class="end" /></view
        ><view class="labels"
          ><text>填写前</text><text>成功率 <text>+200%</text></text></view
        ></view
      >
      <view class="form-list">
        <view
          v-for="item in forms"
          :key="item.path"
          class="form-item"
          @click="go(item.path)"
          ><text>{{ item.title }}</text
          ><text>{{ item.status }} ›</text></view
        >
      </view>
    </scroll-view>
  </view>
</template>
<script setup>
import AppNavBar from "@/components/AppNavBar.vue";
const forms = [
  {
    title: "个人基本信息",
    status: "去完善",
    path: "/pages/worker/intent-personal",
  },
  {
    title: "期望工种与薪资",
    status: "已填写",
    path: "/pages/worker/intent-job",
  },
  {
    title: "工作偏好",
    status: "去完善",
    path: "/pages/worker/intent-preference",
  },
];
function go(url) {
  uni.navigateTo({ url });
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f8f4ed;
}
.content {
  height: calc(100vh - 176rpx);
}
.hero {
  padding: 34rpx 32rpx 22rpx;
  background: linear-gradient(135deg, #ffe4b5, #ffd59e);
}
.tag {
  display: inline-block;
  padding: 4rpx 12rpx;
  border-radius: 14rpx;
  background: #ff6b35;
  color: #fff;
  font-size: 19rpx;
}
.title {
  display: block;
  margin-top: 12rpx;
  color: #333;
  font-size: 38rpx;
  font-weight: 800;
}
.tip {
  display: block;
  margin-top: 8rpx;
  color: #d2691e;
  font-size: 23rpx;
}
.chart {
  margin: 24rpx;
  padding: 30rpx;
  border-radius: 20rpx;
  background: #fff;
}
.line {
  position: relative;
  height: 80rpx;
  border-bottom: 4rpx solid #f1f1f1;
  transform: skewY(-12deg);
}
.line::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  bottom: 10rpx;
  height: 5rpx;
  border-radius: 5rpx;
  background: #ff6b35;
}
.start,
.end {
  position: absolute;
  z-index: 1;
  width: 20rpx;
  height: 20rpx;
  border: 6rpx solid #ffe4b5;
  border-radius: 50%;
  background: #ff6b35;
}
.start {
  left: 0;
  bottom: 0;
}
.end {
  right: 0;
  bottom: 20rpx;
}
.labels {
  display: flex;
  justify-content: space-between;
  margin-top: 25rpx;
  color: #999;
  font-size: 22rpx;
}
.labels > text:last-child,
.labels > text:last-child text {
  color: #ff6b35;
  font-weight: 700;
}
.form-list {
  margin: 24rpx;
  overflow: hidden;
  border-radius: 20rpx;
  background: #fff;
}
.form-item {
  display: flex;
  justify-content: space-between;
  padding: 30rpx 26rpx;
  border-bottom: 1rpx solid #f2eee8;
  color: #333;
  font-size: 26rpx;
}
.form-item:last-child {
  border-bottom: 0;
}
.form-item text:last-child {
  color: #ff6b35;
  font-size: 23rpx;
}
</style>
