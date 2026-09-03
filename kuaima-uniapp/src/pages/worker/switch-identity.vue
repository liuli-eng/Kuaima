<template>
  <view class="page">
    <AppNavBar title="切换身份" :show-back="true" />
    <view class="content">
      <view class="avatar-section"><view class="avatar">👨</view></view>
      <text class="identity"
        >你当前身份是{{ current === "worker" ? "零工" : "老板" }}</text
      >
      <view class="buttons">
        <button class="primary" @click="switchRole">
          切换为{{ current === "worker" ? "老板" : "零工" }}身份
        </button>
        <button class="secondary" @click="cancel">暂不切换</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";

const pages = getCurrentPages();
const query = pages[pages.length - 1]?.options || {};
const current = ref(query.role === "boss" ? "boss" : "worker");

function switchRole() {
  const target = current.value === "worker" ? "boss" : "worker";
  uni.setStorageSync("currentRole", target);
  uni.setStorageSync("role", target === "boss" ? "BOSS" : "WORKER");
  uni.reLaunch({
    url: target === "boss" ? "/pages/boss/home" : "/pages/worker/home",
  });
}

function cancel() {
  uni.redirectTo({
    url:
      current.value === "worker"
        ? "/pages/worker/profile"
        : "/pages/boss/profile",
  });
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #fff;
}
.content {
  min-height: calc(100vh - 176rpx);
  background: #fff;
}
.avatar-section {
  display: flex;
  justify-content: center;
  padding: 120rpx 0 60rpx;
}
.avatar {
  width: 240rpx;
  height: 240rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 50%;
  background: #ffe4b5;
  font-size: 120rpx;
}
.identity {
  display: block;
  margin-bottom: 120rpx;
  text-align: center;
  color: #333;
  font-size: 44rpx;
  font-weight: 600;
}
.buttons {
  padding: 0 48rpx;
}
.primary,
.secondary {
  width: 100%;
  height: 112rpx;
  line-height: 112rpx;
  margin: 0 0 32rpx;
  padding: 0;
  border-radius: 56rpx;
  font-size: 32rpx;
}
.primary {
  border: 0;
  background: linear-gradient(135deg, #ffd700, #ffa500);
  color: #fff;
  font-weight: 600;
}
.secondary {
  border: 2rpx solid #eee;
  background: #fff;
  color: #666;
  font-weight: 500;
}
.primary::after,
.secondary::after {
  border: 0;
}
</style>
