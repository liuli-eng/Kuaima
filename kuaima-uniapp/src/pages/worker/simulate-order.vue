<template>
  <view class="page">
    <AppNavBar title="模拟接单" :show-back="true" />
    <view class="loading">正在进入模拟接单课程…</view>
  </view>
</template>

<script setup>
import { onMounted } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";

onMounted(() => {
  uni.redirectTo({ url: "/pages/worker/course-video?type=simulate" });
});
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}

.loading {
  padding-top: 220rpx;
  color: #999;
  font-size: 26rpx;
  text-align: center;
}
</style>
