<template>
  <view class="page">
    <AppNavBar title="模拟接单" :show-back="true" />
    <scroll-view scroll-y class="content">
      <view class="hero"
        ><text>🎮 模拟接单训练营</text
        ><text>体验从报名到结算的完整流程</text></view
      >
      <view v-for="(step, index) in steps" :key="step.title" class="step">
        <text class="num">{{ index + 1 }}</text>
        <view class="step-main"
          ><text class="title">{{ step.title }}</text
          ><text class="desc">{{ step.desc }}</text></view
        >
        <text :class="['state', { done: index < current }]">{{
          index < current ? "已完成" : index === current ? "去完成" : "未解锁"
        }}</text>
      </view>
      <button class="action" @click="next">
        {{ current >= steps.length ? "训练已完成" : "完成当前步骤" }}
      </button>
    </scroll-view>
  </view>
</template>
<script setup>
import { ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
const current = ref(0);
const steps = [
  { title: "浏览岗位", desc: "查看岗位时间、工资和工作地点" },
  { title: "报名岗位", desc: "确认信息后提交报名" },
  { title: "到岗确认", desc: "按时到岗并完成签到" },
  { title: "完工结算", desc: "完成任务并查看工资到账" },
];
function next() {
  if (current.value < steps.length) current.value += 1;
  uni.showToast({
    title: current.value >= steps.length ? "恭喜完成训练" : "步骤已完成",
    icon: "success",
  });
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f8f4ed;
}
.content {
  height: calc(100vh - 176rpx);
  padding: 24rpx;
  box-sizing: border-box;
}
.hero {
  padding: 36rpx 28rpx;
  border-radius: 24rpx;
  background: linear-gradient(135deg, #ffbd73, #ff8c4d);
  color: #fff;
}
.hero text {
  display: block;
}
.hero text:first-child {
  font-size: 34rpx;
  font-weight: 800;
}
.hero text:last-child {
  margin-top: 8rpx;
  font-size: 22rpx;
}
.step {
  display: flex;
  align-items: center;
  margin-top: 18rpx;
  padding: 24rpx 20rpx;
  border-radius: 18rpx;
  background: #fff;
}
.num {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52rpx;
  height: 52rpx;
  margin-right: 16rpx;
  border-radius: 50%;
  background: #fff1e6;
  color: #ff6b35;
  font-weight: 700;
}
.step-main {
  flex: 1;
}
.title,
.desc {
  display: block;
}
.title {
  font-size: 26rpx;
  font-weight: 700;
}
.desc {
  margin-top: 5rpx;
  color: #999;
  font-size: 20rpx;
}
.state {
  color: #aaa;
  font-size: 21rpx;
}
.done {
  color: #52c41a;
}
.action {
  margin-top: 28rpx;
  border-radius: 44rpx;
  background: #ff6b35;
  color: #fff;
  font-size: 28rpx;
}
</style>
