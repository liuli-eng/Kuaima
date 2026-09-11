<template>
  <view class="page">
    <scroll-view scroll-y class="content">
      <view class="page-heading" :style="{ paddingTop: `${statusBarHeight}px`, height: `calc(88rpx + ${statusBarHeight}px)` }">
        <view class="heading-space" />
        <text class="heading-title">消息</text>
        <view class="heading-space" />
      </view>
      <view class="heading-gap" />

      <view class="entries">
        <view class="entry system-entry" @click="go('/pages/worker/notification')">
          <view class="entry-icon"><image :src="bellIcon" mode="aspectFit" /></view>
          <view class="entry-main">
            <text class="entry-title">系统通知</text>
            <text class="entry-desc">消息内容</text>
          </view>
        </view>
        <view class="entry history-entry" @click="go('/pages/worker/history-message')">
          <text class="history-title">查看历史消息</text>
        </view>
      </view>

      <view class="slogan">
        <text class="slogan-big">找日结 上快马</text>
        <text class="slogan-small">— 真老板 真工价 真日结 —</text>
      </view>
    </scroll-view>
    <WorkerTabBar current="messages" />
  </view>
</template>

<script setup>
import WorkerTabBar from "@/components/WorkerTabBar.vue";
import bellIcon from "/static/icons/worker-messages/bell-orange.svg";

const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0;

function go(url) {
  uni.navigateTo({ url });
}
</script>

<style scoped>
.page { height: 100vh; overflow: hidden; background: #f7f7f7; }
.content {
  height: calc(100vh - 83px - env(safe-area-inset-bottom));
  box-sizing: border-box;
  background: #f7f7f7;
}
.page-heading {
  height: 88rpx;
  padding: 0 32rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-sizing: border-box;
}
.heading-space { width: 64rpx; height: 64rpx; }
.heading-title { color: #333; font-size: 36rpx; font-weight: 700; }
.heading-gap { height: 16rpx; }
.entries { padding: 32rpx; }
.entry {
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  box-sizing: border-box;
}
.system-entry {
  min-height: 144rpx;
  padding: 32rpx;
  display: flex;
  align-items: flex-start;
  gap: 24rpx;
}
.system-entry:active, .history-entry:active { background: #f9f9f9; }
.entry-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: #fff0e6;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.entry-icon image { width: 40rpx; height: 44rpx; }
.entry-main { flex: 1; min-width: 0; padding-top: 2rpx; }
.entry-title { display: block; color: #333; font-size: 32rpx; font-weight: 600; margin-bottom: 8rpx; }
.entry-desc { display: block; color: #777; font-size: 28rpx; line-height: 1.5; }
.history-entry { margin-top: 24rpx; padding: 40rpx 32rpx; display: flex; align-items: center; justify-content: center; }
.history-title { color: #333; font-size: 28rpx; font-weight: 500; }
.slogan { padding: 64rpx 32rpx 32rpx; text-align: center; }
.slogan-big { display: block; color: #d3d3d3; font-size: 48rpx; font-weight: 800; letter-spacing: 6rpx; }
.slogan-small { display: block; margin-top: 16rpx; color: #aaa; font-size: 28rpx; }
</style>
