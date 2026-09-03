<template>
  <view class="page"
    ><AppNavBar title="通知详情" :show-back="true" /><scroll-view
      scroll-y
      class="content"
      ><text class="title">{{ title }}</text
      ><text class="time">今天 10:30</text><text class="body">{{ desc }}</text
      ><view class="tip"
        >如需进一步帮助，可前往客服中心咨询。</view
      ></scroll-view
    ></view
  >
</template>
<script setup>
import { ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
const p = getCurrentPages();
const o = p[p.length - 1]?.options || {};
const title = ref(decodeURIComponent(o.title || "系统通知"));
const desc = ref(decodeURIComponent(o.desc || "暂无通知内容"));
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #fff;
}
.content {
  height: calc(100vh - 176rpx);
  padding: 36rpx;
  box-sizing: border-box;
}
.title {
  display: block;
  color: #333;
  font-size: 36rpx;
  font-weight: 700;
}
.time {
  display: block;
  margin-top: 14rpx;
  color: #aaa;
  font-size: 21rpx;
}
.body {
  display: block;
  margin-top: 36rpx;
  color: #555;
  font-size: 27rpx;
  line-height: 2;
}
.tip {
  margin-top: 40rpx;
  padding: 22rpx;
  border-radius: 14rpx;
  background: #fff8e6;
  color: #8b4513;
  font-size: 22rpx;
  line-height: 1.7;
}
</style>
