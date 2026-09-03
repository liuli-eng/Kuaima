<template>
  <view class="page"
    ><AppNavBar title="考试结果" :show-back="true" /><view class="result"
      ><text class="icon">{{ passed ? "✓" : "!" }}</text
      ><text class="title">{{ passed ? "恭喜，考试通过" : "考试未通过" }}</text
      ><text class="score">本次得分 {{ score }} / 3</text
      ><text class="desc">{{
        passed
          ? "已获得接单资格，可继续报名岗位。"
          : "请重新学习平台规则后再次参加考试。"
      }}</text
      ><button @click="done">
        {{ passed ? "去找工作" : "重新考试" }}
      </button></view
    ></view
  >
</template>
<script setup>
import AppNavBar from "@/components/AppNavBar.vue";
const pages = getCurrentPages();
const o = pages[pages.length - 1]?.options || {};
const score = Number(o.score || 0);
const passed = o.passed === "true";
if (passed) uni.setStorageSync("workerQualified", true);
function done() {
  if (passed) uni.reLaunch({ url: "/pages/worker/home" });
  else uni.redirectTo({ url: "/pages/worker/exam" });
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}
.result {
  margin: 60rpx 24rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 70rpx 30rpx;
  text-align: center;
}
.icon {
  display: block;
  width: 120rpx;
  height: 120rpx;
  line-height: 120rpx;
  margin: 0 auto 24rpx;
  border-radius: 50%;
  background: #e8f8e8;
  color: #52c41a;
  font-size: 76rpx;
  font-weight: 800;
}
.title {
  display: block;
  font-size: 38rpx;
  font-weight: 800;
}
.score {
  display: block;
  color: #ff6b35;
  font-size: 34rpx;
  margin: 18rpx 0;
}
.desc {
  display: block;
  color: #888;
  font-size: 24rpx;
}
.result button {
  margin-top: 38rpx;
  background: #ff6b35;
  color: #fff;
  border: 0;
  border-radius: 44rpx;
  font-size: 28rpx;
}
</style>
