<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="header">
        <view class="header-row">
          <button class="back" @click="goHome">‹</button>
          <view class="brand">月结试工</view>
        </view>
        <view class="title-section">
          <view
            ><view class="title-main"
              ><text>月结</text><text class="highlight">试工</text></view
            ><text class="desc">先试工再入职 试工也有报酬</text></view
          >
          <view class="mascot">🏢</view>
        </view>
      </view>

      <view class="filter-bar">
        <view class="location"
          ><text class="pin">⌖</text><text>松江洞照路</text
          ><text class="chevron">⌄</text></view
        >
        <view class="spacer" />
        <view class="search-btn" @click="openSearch">⌕ 搜索</view>
      </view>

      <view class="job-list">
        <view
          v-for="job in jobs"
          :key="job.id"
          class="job-card"
          @click="openDetail(job)"
        >
          <view class="job-content">
            <view class="job-time"
              ><text>{{ job.time }}</text
              ><text class="job-tag">试工</text
              ><text class="distance">{{ job.distance }}</text></view
            >
            <text class="job-title">{{ job.title }}</text>
            <view class="job-tags"
              ><text
                v-for="tag in job.tags"
                :key="tag.text"
                class="tag"
                :class="tag.type"
                >{{ tag.text }}</text
              ></view
            >
            <view class="job-footer"
              ><view
                ><text class="salary">月薪:{{ job.salary }}</text
                ><text class="unit">+</text
                ><text class="note">(试工: {{ job.trial }}元/天)</text></view
              ><button class="apply" @click.stop="openDetail(job)">
                我要试工
              </button></view
            >
          </view>
          <view class="thumbnail"
            ><text>{{ job.icon }}</text
            ><view class="play">▶</view></view
          >
        </view>
      </view>
      <view class="bottom-slogan"
        ><text class="slogan-title">找日结 上快马</text
        ><text>— 真老板 真工价 真日结 —</text></view
      >
    </scroll-view>
    <WorkerTabBar current="home" />
  </view>
</template>

<script setup>
import WorkerTabBar from "@/components/WorkerTabBar.vue";

const jobs = [
  {
    id: "m1",
    time: "明天 08:00-20:00",
    distance: "徐泾镇 9.4km",
    title: "餐饮服务员",
    salary: 4500,
    trial: 180,
    icon: "🍽️",
    tags: [
      { text: "保障", type: "green" },
      { text: "12小时", type: "" },
      { text: "30-50岁", type: "blue" },
    ],
  },
  {
    id: "m2",
    time: "明天 05:30-10:30",
    distance: "岳阳街道 10.0km",
    title: "售卖员 外卖打包员",
    salary: 3000,
    trial: 100,
    icon: "📦",
    tags: [
      { text: "保障", type: "green" },
      { text: "5小时", type: "" },
      { text: "26-55岁", type: "blue" },
    ],
  },
  {
    id: "m3",
    time: "明天 11:00-01:30",
    distance: "莘庄镇 12.7km",
    title: "餐饮服务员",
    salary: 4500,
    trial: 330,
    icon: "🍜",
    tags: [
      { text: "保障", type: "green" },
      { text: "11小时", type: "" },
      { text: "22-46岁", type: "blue" },
    ],
  },
  {
    id: "m4",
    time: "明天 11:30-12:30",
    distance: "安亭镇 20.6km",
    title: "餐饮切配/帮厨",
    salary: 5500,
    trial: 25,
    icon: "🔪",
    tags: [
      { text: "保障", type: "green" },
      { text: "1小时", type: "" },
      { text: "25-45岁", type: "blue" },
    ],
  },
];

function goHome() {
  uni.navigateBack({ delta: 1 });
}
function openDetail(job) {
  uni.navigateTo({ url: `/pages/worker/job-detail?id=${job.id}` });
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
  background: linear-gradient(180deg, #d9edff 0%, #edf7ff 55%, #f8fcff 100%);
}
.header-row {
  height: 72rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
}
.back {
  width: 64rpx;
  height: 64rpx;
  margin: 0;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.8);
  color: #555;
  font-size: 52rpx;
  line-height: 56rpx;
}
.brand {
  color: #24527a;
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
  color: #24527a;
  font-size: 40rpx;
  font-weight: 800;
}
.title-main .highlight {
  color: #1890ff;
}
.desc {
  display: block;
  margin-top: 10rpx;
  color: #789;
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
  background: linear-gradient(135deg, #e6f7ff, #bae7ff);
  font-size: 48rpx;
  box-shadow: 0 8rpx 24rpx rgba(24, 144, 255, 0.15);
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
  color: #1890ff;
}
.chevron {
  color: #999;
}
.spacer {
  flex: 1;
}
.search-btn {
  padding: 12rpx 24rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.9);
  color: #666;
  font-size: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
}
.job-list {
  padding: 8rpx 32rpx;
}
.job-card {
  display: flex;
  gap: 18rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  border-radius: 28rpx;
  background: #fff;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}
.job-content {
  flex: 1;
  min-width: 0;
}
.job-time {
  display: flex;
  align-items: center;
  gap: 12rpx;
  color: #666;
  font-size: 22rpx;
}
.job-tag {
  padding: 4rpx 14rpx;
  border-radius: 20rpx;
  background: #e6f7ff;
  color: #1890ff;
}
.distance {
  margin-left: auto;
  color: #999;
  white-space: nowrap;
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
  gap: 10rpx;
}
.salary {
  color: #ff6b35;
  font-size: 34rpx;
  font-weight: 800;
}
.unit {
  color: #666;
  font-size: 24rpx;
}
.note {
  display: block;
  margin-top: 4rpx;
  color: #999;
  font-size: 20rpx;
}
.apply {
  margin: 0;
  padding: 0 26rpx;
  height: 68rpx;
  line-height: 68rpx;
  border: 0;
  border-radius: 36rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: #fff;
  font-size: 25rpx;
  font-weight: 600;
  white-space: nowrap;
}
.apply::after {
  border: 0;
}
.thumbnail {
  position: relative;
  width: 100rpx;
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border-radius: 20rpx;
  background: #f0f8ff;
  font-size: 44rpx;
}
.play {
  position: absolute;
  right: -6rpx;
  bottom: -6rpx;
  width: 36rpx;
  height: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #1890ff;
  color: #fff;
  font-size: 16rpx;
}
.bottom-slogan {
  padding: 20rpx 0 40rpx;
  text-align: center;
  color: #ccc;
  font-size: 22rpx;
}
.slogan-title {
  display: block;
  margin-bottom: 8rpx;
  color: #bbdefb;
  font-size: 40rpx;
  font-weight: 800;
}
</style>
