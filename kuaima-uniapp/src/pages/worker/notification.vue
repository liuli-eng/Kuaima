<template>
  <view class="page">
    <AppNavBar title="平台公告" :show-back="true" />
    <scroll-view scroll-y class="scroll">
      <view
        v-for="item in notices"
        :key="item.id"
        class="notice-card"
        @click="open(item)"
      >
        <text class="notice-icon">{{ typeIcon(item.type) }}</text>
        <view class="notice-main"
          ><view class="notice-head"
            ><text class="notice-title">{{ item.title }}</text
            ><text class="time">{{ item.time }}</text></view
          ><text class="desc">{{ item.content }}</text></view
        >
      </view>
      <view v-if="!notices.length" class="empty">暂无公告</view>
    </scroll-view>
  </view>
</template>

<script setup>
import { onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import { listNotices } from "@/api/backend";

const notices = ref([]);

onMounted(async () => {
  try {
    const result = await listNotices({ scope: "零工" });
    if (Array.isArray(result)) notices.value = result.map(normalizeNotice);
  } catch (_) {}
});

function open(item) {
  uni.navigateTo({
    url: `/pages/worker/notification-detail?id=${item.id}&title=${encodeURIComponent(item.title)}&desc=${encodeURIComponent(item.content)}`,
  });
}

function typeIcon(type) {
  if (type === "活动") return "🎁";
  if (type === "政策") return "📋";
  return "📢";
}

function normalizeNotice(item) {
  return {
    id: item.id,
    title: item.title || "",
    content: item.content || "",
    time: formatNoticeTime(item.publishTime),
    type: item.type || "系统",
  };
}

function formatNoticeTime(value) {
  if (!value) return "";
  const match = String(value).match(/(\d{4})-(\d{2})-(\d{2})/);
  return match ? `${match[1]}-${match[2]}-${match[3]}` : String(value).slice(0, 10);
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #fff8e6;
}
.tabs {
  display: flex;
  gap: 12rpx;
  padding: 18rpx 24rpx;
  background: #ffe4b5;
}
.tab {
  flex: 1;
  padding: 12rpx 0;
  border-radius: 24rpx;
  background: #fff;
  color: #666;
  text-align: center;
  font-size: 23rpx;
}
.tab.active {
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: #fff;
  font-weight: 600;
}
.scroll {
  height: calc(100vh - 250rpx);
  background: #fff8e6;
  padding-bottom: 24rpx;
  box-sizing: border-box;
}
.date-title {
  display: block;
  padding: 20rpx 24rpx 8rpx;
  color: #999;
  font-size: 22rpx;
}
.notice-card {
  position: relative;
  display: flex;
  gap: 16rpx;
  margin: 10rpx 24rpx;
  padding: 24rpx 20rpx;
  border-radius: 18rpx;
  background: #fff;
  box-shadow: 0 4rpx 14rpx rgba(88, 64, 32, 0.04);
}
.notice-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 68rpx;
  height: 68rpx;
  border-radius: 50%;
  background: #fff1e6;
  color: #ff6b35;
  font-size: 24rpx;
  font-weight: 700;
}
.activity {
  background: #e6f7ff;
  color: #1890ff;
}
.system {
  background: #f6ffed;
  color: #52c41a;
}
.notice {
  background: #f6ffed;
  color: #52c41a;
}
.notice-main {
  flex: 1;
  min-width: 0;
}
.notice-head {
  display: flex;
  justify-content: space-between;
  gap: 10rpx;
}
.notice-title {
  color: #333;
  font-size: 27rpx;
  font-weight: 700;
}
.time {
  color: #aaa;
  font-size: 20rpx;
}
.desc {
  display: block;
  margin-top: 8rpx;
  color: #666;
  font-size: 22rpx;
  line-height: 1.6;
}
.action {
  display: block;
  margin-top: 12rpx;
  color: #ff6b35;
  font-size: 22rpx;
}
.unread {
  position: absolute;
  top: 14rpx;
  right: 14rpx;
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: #ff4d4f;
}
.empty {
  padding-top: 220rpx;
  text-align: center;
  color: #aaa;
  font-size: 25rpx;
}
</style>
