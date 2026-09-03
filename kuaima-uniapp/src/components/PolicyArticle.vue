<template>
  <view class="page">
    <AppNavBar :title="title" :show-back="true" />
    <scroll-view scroll-y class="content">
      <text class="meta">更新日期：2026年7月21日\n生效时间：2026年7月21日</text>
      <text class="welcome">欢迎您使用快马日结平台服务。</text>
      <view class="tip"
        ><text>特别提示：</text
        >请认真阅读并充分理解各项条款，涉及责任限制、争议解决及个人信息处理的内容请重点关注。</view
      >
      <view
        v-for="(section, index) in sections"
        :key="section.title"
        class="section"
      >
        <text class="section-title"
          >{{ numberText[index] }}、{{ section.title }}</text
        >
        <text class="section-content">{{ section.content }}</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import AppNavBar from "@/components/AppNavBar.vue";
defineProps({
  title: { type: String, required: true },
  sections: { type: Array, required: true },
});
const numberText = ["一", "二", "三", "四", "五", "六"];
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #fff;
}
.content {
  height: calc(100vh - 176rpx);
  padding: 28rpx 36rpx 60rpx;
  box-sizing: border-box;
}
.meta {
  display: block;
  text-align: right;
  white-space: pre-line;
  color: #999;
  font-size: 21rpx;
  line-height: 1.7;
}
.welcome {
  display: block;
  margin-top: 22rpx;
  color: #333;
  font-size: 25rpx;
  line-height: 1.8;
}
.tip {
  margin: 24rpx 0;
  padding: 22rpx 24rpx;
  border-left: 6rpx solid #ffd666;
  background: #fff8e6;
  color: #666;
  font-size: 23rpx;
  line-height: 1.8;
}
.tip text {
  color: #fa8c16;
  font-weight: 700;
}
.section {
  margin-top: 26rpx;
}
.section-title {
  display: block;
  margin-bottom: 12rpx;
  color: #333;
  font-size: 27rpx;
  font-weight: 700;
}
.section-content {
  display: block;
  color: #666;
  font-size: 24rpx;
  line-height: 1.9;
}
</style>
