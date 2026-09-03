<template>
  <view class="page"
    ><AppNavBar title="课程视频" :show-back="true" /><scroll-view
      scroll-y
      class="content"
      ><view class="video" @click="playing = !playing"
        ><text>{{ playing ? "❚❚" : "▶" }}</text
        ><text>{{ playing ? "播放中" : "点击播放课程" }}</text></view
      ><view class="card"
        ><text class="title">新手接单操作流程</text
        ><text class="desc"
          >学习岗位筛选、报名、到岗确认、完工结算和异常处理流程。</text
        ></view
      ><view class="card"
        ><text class="section">课程要点</text
        ><text v-for="item in points" :key="item" class="point"
          >• {{ item }}</text
        ></view
      ></scroll-view
    ><SafeBottomAction
      ><button class="complete" @click="done">
        完成学习
      </button></SafeBottomAction
    ></view
  >
</template>
<script setup>
import { ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import SafeBottomAction from "@/components/SafeBottomAction.vue";
const playing = ref(false);
const points = [
  "报名前确认工作信息",
  "按约定时间到岗",
  "完工后关注结算进度",
  "异常情况及时保留证据",
];
function done() {
  uni.showToast({ title: "课程学习完成", icon: "success" });
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
.video {
  height: 380rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #222;
  color: #fff;
}
.video text:first-child {
  font-size: 80rpx;
}
.video text:last-child {
  font-size: 24rpx;
  margin-top: 16rpx;
}
.card {
  margin: 22rpx;
  padding: 28rpx;
  background: #fff;
  border-radius: 20rpx;
}
.title,
.section {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  margin-bottom: 16rpx;
}
.desc,
.point {
  display: block;
  color: #666;
  font-size: 24rpx;
  line-height: 1.8;
}
.complete {
  width: 100%;
  height: 84rpx;
  border: 0;
  border-radius: 44rpx;
  background: #ff6b35;
  color: #fff;
  font-size: 29rpx;
}
</style>
