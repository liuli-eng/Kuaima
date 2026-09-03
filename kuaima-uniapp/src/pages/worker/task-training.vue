<template>
  <view class="page"
    ><AppNavBar title="培训任务" :show-back="true" /><scroll-view
      scroll-y
      class="content"
      ><view class="hero"
        ><text class="title">工作安全与纠纷处理</text
        ><text>完成培训任务可提升接单资格和信用分</text></view
      ><view class="card"
        ><text class="section">学习步骤</text
        ><view v-for="(item, index) in steps" :key="item" class="step"
          ><text>{{ index + 1 }}</text
          ><text>{{ item }}</text></view
        ></view
      ><view class="card video" @click="play"
        ><text class="play">▶</text><text>观看培训视频演示</text></view
      ></scroll-view
    ><SafeBottomAction
      ><button class="done" @click="complete">
        完成培训任务
      </button></SafeBottomAction
    ></view
  >
</template>
<script setup>
import AppNavBar from "@/components/AppNavBar.vue";
import SafeBottomAction from "@/components/SafeBottomAction.vue";
const steps = [
  "了解现场安全规范",
  "学习异常情况处理方式",
  "保留照片、视频和沟通记录",
  "遇到纠纷及时联系平台",
];
function play() {
  uni.navigateTo({ url: "/pages/worker/course-video?id=safety" });
}
function complete() {
  uni.setStorageSync("trainingCompleted", true);
  uni.showToast({ title: "培训已完成", icon: "success" });
  setTimeout(() => uni.navigateBack(), 500);
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}
.content {
  height: calc(100vh - 176rpx);
}
.hero {
  padding: 38rpx 30rpx;
  background: linear-gradient(135deg, #f3e8ff, #e9d5ff);
  color: #5b21b6;
}
.hero .title {
  display: block;
  font-size: 36rpx;
  font-weight: 800;
  margin-bottom: 12rpx;
}
.hero text:last-child {
  font-size: 23rpx;
}
.card {
  margin: 22rpx;
  padding: 28rpx;
  background: #fff;
  border-radius: 20rpx;
}
.section {
  display: block;
  font-size: 29rpx;
  font-weight: 700;
  margin-bottom: 18rpx;
}
.step {
  display: flex;
  gap: 18rpx;
  padding: 18rpx 0;
  color: #555;
  font-size: 25rpx;
}
.step text:first-child {
  width: 38rpx;
  height: 38rpx;
  line-height: 38rpx;
  text-align: center;
  border-radius: 50%;
  background: #722ed1;
  color: #fff;
}
.video {
  text-align: center;
  color: #722ed1;
}
.play {
  display: block;
  font-size: 60rpx;
  margin-bottom: 12rpx;
}
.done {
  width: 100%;
  height: 84rpx;
  border: 0;
  border-radius: 44rpx;
  background: #722ed1;
  color: #fff;
  font-size: 29rpx;
}
</style>
