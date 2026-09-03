<template>
  <view class="page">
    <AppNavBar title="报案详情" :show-back="true" />
    <view class="hero"
      ><text class="status">处理中</text
      ><text>平台正在核验材料，请保持电话畅通</text></view
    >
    <view class="card">
      <text class="title">报案信息</text>
      <view class="row"><text>事故类型</text><text>意外受伤</text></view>
      <view class="row"><text>关联订单</text><text>KM20260831002</text></view>
      <view class="row"><text>提交时间</text><text>09月01日 14:20</text></view>
      <view class="row"><text>当前进度</text><text>材料审核</text></view>
    </view>
    <view class="card"
      ><text class="title">处理进度</text
      ><text class="progress"
        >✓ 已提交　→　● 材料审核　→　○ 理赔结果</text
      ></view
    >
  </view>
</template>
<script setup>
import AppNavBar from "@/components/AppNavBar.vue";
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}
.hero {
  padding: 38rpx 30rpx;
  background: linear-gradient(135deg, #fff4e6, #ffe4b5);
  color: #8b4513;
}
.hero text {
  display: block;
  font-size: 23rpx;
}
.hero .status {
  margin-bottom: 12rpx;
  font-size: 38rpx;
  font-weight: 800;
}
.card {
  margin: 22rpx;
  padding: 28rpx;
  border-radius: 20rpx;
  background: #fff;
}
.title {
  display: block;
  margin-bottom: 18rpx;
  font-size: 29rpx;
  font-weight: 700;
}
.row {
  display: flex;
  justify-content: space-between;
  padding: 18rpx 0;
  color: #888;
  font-size: 24rpx;
}
.row text:last-child {
  color: #333;
}
.progress {
  color: #ff8a00;
  font-size: 23rpx;
  line-height: 1.8;
}
</style>
