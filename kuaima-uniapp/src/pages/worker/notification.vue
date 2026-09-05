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
      <view v-if="loading" class="empty">正在加载公告...</view>
      <view v-else-if="!notices.length" class="empty">暂无公告</view>
    </scroll-view>
  </view>
</template>

<script setup>
import { onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import { listNotices } from "@/api/backend";

const notices = ref([]);
const loading = ref(false);

onMounted(async () => {
  loading.value = true;
  try {
    const result = await listNotices({ scope: "零工" });
    if (Array.isArray(result)) notices.value = result.map(normalizeNotice);
  } catch (error) {
    notices.value = [];
    uni.showToast({ title: error.message || "公告加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
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
.scroll {
  height: calc(100vh - 250rpx);
  background: #fff8e6;
  padding-bottom: 24rpx;
  box-sizing: border-box;
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
.empty {
  padding-top: 220rpx;
  text-align: center;
  color: #aaa;
  font-size: 25rpx;
}
</style>
