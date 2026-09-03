<template>
  <view class="page"><view class="mask" @click="close" /><view class="panel"
    ><view class="header"
      ><text>筛选</text><text class="close" @click="close">×</text></view
    ><scroll-view scroll-y class="content"
      ><view v-for="group in groups" :key="group.key" class="section"
        ><text class="title">{{ group.title }}</text
        ><view class="options"
          ><text
            v-for="option in group.options"
            :key="option"
            class="option"
            :class="{ active: form[group.key] === option }"
            @click="form[group.key] = option"
            >{{ option }}</text
          ></view
        ></view
      ></scroll-view
    ><view class="bottom-action"><view class="actions"
        ><button class="reset" @click="reset">重置</button
        ><button class="confirm" @click="confirm">确定</button></view
      ></view></view></view
  >
</template>
<script setup>
import { reactive } from "vue";
const groups = [
  { key: "date", title: "日期", options: ["全部", "今天", "明天", "后天"] },
  { key: "type", title: "类型", options: ["全部", "计时", "计件"] },
  { key: "days", title: "天数", options: ["全部", "一天日结", "多天日结"] },
  {
    key: "duration",
    title: "任务时长",
    options: ["全部", "3小时以内", "3~6小时", "6小时以上"],
  },
  {
    key: "sort",
    title: "排序",
    options: ["推荐排序", "近距离优先", "时间优先", "老雇主优先"],
  },
];
const defaults = {
  date: "全部",
  type: "全部",
  days: "全部",
  duration: "全部",
  sort: "推荐排序",
};
const form = reactive({ ...defaults });
function reset() {
  Object.assign(form, defaults);
}
function close() {
  uni.navigateBack();
}
function confirm() {
  uni.setStorageSync("workerJobFilter", { ...form });
  uni.navigateBack();
}
</script>
<style scoped>
.page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background: rgba(0, 0, 0, 0.45);
}
.mask {
  position: absolute;
  inset: 0;
}
.panel {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 88vh;
  overflow: hidden;
  border-radius: 40rpx 40rpx 0 0;
  background: #f5f5f5;
}
.header {
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  background: #fff;
  border-bottom: 1rpx solid #eee;
  color: #333;
  font-size: 36rpx;
  font-weight: 700;
}
.close {
  color: #6b7280;
  font-size: 64rpx;
  font-weight: 400;
}
.content {
  height: calc(88vh - 260rpx - env(safe-area-inset-bottom));
}
.section {
  padding: 30rpx 32rpx 34rpx;
  margin-bottom: 12rpx;
  background: #fff;
}
.title {
  display: block;
  margin-bottom: 24rpx;
  color: #333;
  font-size: 32rpx;
  font-weight: 700;
}
.options {
  display: flex;
  flex-wrap: wrap;
  gap: 18rpx 16rpx;
}
.option {
  min-width: 120rpx;
  padding: 16rpx 22rpx;
  border: 2rpx solid transparent;
  border-radius: 38rpx;
  background: #f5f5f5;
  color: #666;
  font-size: 27rpx;
  text-align: center;
  box-sizing: border-box;
}
.option.active {
  border-color: #ffc84d;
  background: linear-gradient(135deg, #ffedbb, #ffda72);
  color: #d47635;
  font-weight: 600;
}
.actions {
  display: flex;
  gap: 20rpx;
}
.bottom-action {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 24rpx 32rpx calc(24rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid #eee;
  background: #fff;
}
.reset,
.confirm {
  height: 84rpx;
  line-height: 84rpx;
  border-radius: 44rpx;
  font-size: 28rpx;
}
.reset {
  width: 34%;
  border: 2rpx solid #ddd;
  background: #fff;
  color: #666;
}
.confirm {
  flex: 1;
  border: 0;
  background: linear-gradient(135deg, #ff6b35, #ff9b42);
  color: #fff;
  font-weight: 700;
}
.reset::after,
.confirm::after {
  border: 0;
}
</style>
