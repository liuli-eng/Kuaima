<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="header">
        <view class="header-row"
          ><button class="back" @click="goHome">‹</button
          ><view class="brand">压薪日结</view></view
        >
        <view class="title-section"
          ><view
            ><view class="title-main"
              ><text>压薪</text><text class="highlight">日结</text></view
            ><text class="desc">压薪N天后日结 干满工期可续单</text></view
          ><view class="mascot">￥</view></view
        >
      </view>
      <view class="filter-bar"
        ><view class="location"
          ><text class="pin">⌖</text><text>松江洞照路</text
          ><text class="chevron">⌄</text></view
        ><view class="spacer" /><view class="filter-btn" @click="openFilter"
          >⌕ 筛选⌄</view
        ><view class="filter-btn" @click="openSearch">⌕ 搜索</view></view
      >
      <view class="job-list"
        ><view
          v-for="job in jobs"
          :key="job.id"
          class="job-card"
          @click="openDetail(job)"
          ><view class="job-time"
            ><text>{{ job.interview }}</text
            ><text class="job-tag">{{ job.term }}</text
            ><text class="distance">{{ job.distance }}</text></view
          ><text class="job-title">{{ job.title }}</text
          ><view class="job-tags"
            ><text
              v-for="tag in job.tags"
              :key="tag.text"
              class="tag"
              :class="tag.type"
              >{{ tag.text }}</text
            ></view
          ><view class="job-footer"
            ><view
              ><text class="salary">{{ job.salary }}</text
              ><text class="unit">元/天</text
              ><text class="note"
                >（压薪{{ job.pressDays }}天后日结）</text
              ></view
            ><button class="apply" @click.stop="openDetail(job)">
              报名面试
            </button></view
          ></view
        ></view
      >
      <view class="bottom-slogan"
        ><text class="slogan-title">找日结 上快马</text
        ><text>— 真老板 真工价 真日结 —</text></view
      > </scroll-view
    ><WorkerTabBar current="home" />
  </view>
</template>
<script setup>
import WorkerTabBar from "@/components/WorkerTabBar.vue";
const jobs = [
  {
    id: "p1",
    interview: "明天 09:00面试",
    term: "7天单",
    distance: "方松街道 6.6km",
    title: "餐饮服务员、洗碗工、切配/帮厨",
    salary: 170,
    pressDays: 1,
    tags: [
      { text: "免服务费", type: "green" },
      { text: "包吃", type: "" },
      { text: "有空调", type: "blue" },
    ],
  },
  {
    id: "p2",
    interview: "明天 07:00面试",
    term: "30天单",
    distance: "香花桥街道 13.6km",
    title: "快递卸车，只卸不装，相对轻松",
    salary: 66,
    pressDays: 1,
    tags: [
      { text: "免服务费", type: "green" },
      { text: "包吃住", type: "" },
    ],
  },
  {
    id: "p3",
    interview: "明天 09:00面试",
    term: "30天单",
    distance: "古美街道 15.4km",
    title: "后厨阿姨，杂工",
    salary: 221,
    pressDays: 1,
    tags: [
      { text: "免服务费", type: "green" },
      { text: "包吃住", type: "" },
      { text: "有空调", type: "blue" },
    ],
  },
  {
    id: "p4",
    interview: "明天 11:00面试",
    term: "22天单",
    distance: "虹梅路街道 17.5km",
    title: "餐饮服务员",
    salary: 225,
    pressDays: 5,
    tags: [
      { text: "免服务费", type: "green" },
      { text: "包吃", type: "" },
    ],
  },
];
function goHome() {
  uni.navigateBack({ delta: 1 });
}
function openDetail(job) {
  uni.navigateTo({ url: `/pages/worker/job-detail?id=${job.id}` });
}
function openFilter() {
  uni.navigateTo({ url: "/pages/worker/filter" });
}
function openSearch() {
  uni.navigateTo({ url: "/pages/worker/search" });
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}
.scroll {
  height: calc(100vh - 166rpx);
}
.header {
  padding: calc(8rpx + env(safe-area-inset-top)) 32rpx 24rpx;
  background: linear-gradient(180deg, #ffd59e 0%, #ffe4b5 55%, #fffbf5 100%);
}
.header-row { height: 72rpx; display: flex; align-items: center; gap: 20rpx; }
.back {
  width: 64rpx;
  height: 64rpx;
  margin: 0;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.72);
  color: #555;
  font-size: 52rpx;
  line-height: 56rpx;
}
.brand {
  color: #8b4513;
  font-size: 32rpx;
  font-weight: 800;
}
.title-section {
  display: flex;
  align-items: center;
  gap: 24rpx;
  margin-top: 24rpx;
}
.title-main text {
  font-size: 40rpx;
  font-weight: 800;
  color: #8b4513;
}
.title-main .highlight {
  color: #ff6b35;
}
.desc {
  display: block;
  margin-top: 10rpx;
  color: #999;
  font-size: 24rpx;
}
.mascot {
  width: 112rpx;
  height: 112rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: auto;
  border-radius: 32rpx;
  background: linear-gradient(135deg, #ffe4b5, #ffd48a);
  color: #ff6b35;
  font-size: 48rpx;
  box-shadow: 0 8rpx 24rpx rgba(255, 180, 100, 0.3);
}
.filter-bar {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx 32rpx 16rpx;
}
.location {
  display: flex;
  align-items: center;
  gap: 6rpx;
  color: #333;
  font-size: 28rpx;
  font-weight: 500;
}
.pin {
  color: #ff9a6b;
}
.chevron {
  color: #999;
}
.spacer {
  flex: 1;
}
.filter-btn {
  padding: 12rpx 24rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.85);
  color: #666;
  font-size: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
}
.job-list {
  padding: 8rpx 32rpx;
}
.job-card {
  padding: 28rpx;
  margin-bottom: 20rpx;
  border-radius: 28rpx;
  background: #fff;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}
.job-time {
  display: flex;
  align-items: center;
  gap: 14rpx;
  color: #666;
  font-size: 24rpx;
}
.job-tag {
  padding: 4rpx 14rpx;
  border-radius: 20rpx;
  background: #fff3e0;
  color: #ff6b35;
  font-size: 22rpx;
}
.distance {
  margin-left: auto;
  color: #999;
  font-size: 22rpx;
}
.job-title {
  display: block;
  margin: 12rpx 0 14rpx;
  color: #333;
  font-size: 34rpx;
  font-weight: 700;
}
.job-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-bottom: 18rpx;
}
.tag {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  background: #f5f5f5;
  color: #666;
  font-size: 22rpx;
}
.tag.green {
  background: #e8f5e9;
  color: #52c41a;
}
.tag.blue {
  background: #e6f7ff;
  color: #1890ff;
}
.job-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}
.salary {
  color: #ff6b35;
  font-size: 44rpx;
  font-weight: 800;
}
.unit {
  color: #999;
  font-size: 26rpx;
}
.note {
  margin-left: 8rpx;
  color: #999;
  font-size: 20rpx;
}
.apply {
  margin: 0;
  padding: 0 30rpx;
  height: 68rpx;
  line-height: 68rpx;
  border: 0;
  border-radius: 36rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: #fff;
  font-size: 26rpx;
  font-weight: 600;
  white-space: nowrap;
}
.apply::after {
  border: 0;
}
.bottom-slogan {
  padding: 20rpx 0 40rpx;
  text-align: center;
  color: #ddd;
  font-size: 22rpx;
}
.slogan-title {
  display: block;
  color: #e8d5b7;
  font-size: 40rpx;
  font-weight: 800;
  margin-bottom: 8rpx;
}
</style>
