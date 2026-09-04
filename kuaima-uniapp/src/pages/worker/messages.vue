<template>
  <view class="page">
    <AppNavBar title="消息" :show-back="true" />
    <scroll-view scroll-y class="content">
      <view class="top-actions">
        <view class="history" @click="go('/pages/worker/history-message')">
          查看历史消息
        </view>
      </view>

      <view class="cards">
        <view
          v-for="item in messages"
          :key="item.id"
          class="card"
          @click="read(item)"
        >
          <view class="icon">{{ item.icon }}</view>
          <view class="main">
            <view class="head">
              <text class="title">{{ item.title }}</text>
              <text class="time">{{ item.time }}</text>
            </view>
            <text class="desc">{{ item.content }}</text>
          </view>
          <text v-if="!item.read" class="dot" />
        </view>
      </view>

      <view class="slogan">
        <text class="big">找日结 上快马</text>
        <text class="small">— 真老板 真工价 真日结 —</text>
      </view>
    </scroll-view>

    <WorkerTabBar current="messages" />
  </view>
</template>

<script setup>
import { onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import WorkerTabBar from "@/components/WorkerTabBar.vue";
import { listMessages, readMessage } from "@/api/backend";

const messages = ref([
  {
    id: 1,
    icon: "📣",
    title: "系统通知",
    content: "欢迎使用快马日结，完成实名认证后即可报名岗位。",
    time: "今天 10:20",
    read: false,
  },
  {
    id: 2,
    icon: "✓",
    title: "报名进度提醒",
    content: "你报名的餐饮服务员岗位已被雇主查看。",
    time: "昨天 18:32",
    read: false,
  },
]);

onMounted(async () => {
  try {
    const result = await listMessages(uni.getStorageSync("userId") || "2001");
    if (Array.isArray(result)) messages.value = result.map(normalizeMessage);
  } catch (_) {}
});

function go(url) {
  uni.navigateTo({ url });
}

async function read(item) {
  try {
    await readMessage(item.id, uni.getStorageSync("userId") || "2001");
  } catch (_) {}
  item.read = true;
  if (item.title === "系统通知") {
    uni.navigateTo({ url: "/pages/worker/notification" });
    return;
  }
  uni.showModal({
    title: item.title,
    content: item.content,
    showCancel: false,
  });
}

function normalizeMessage(item) {
  return {
    ...item,
    icon:
      item.bizType === "settle" ? "¥" : item.bizType === "order" ? "🔔" : "📣",
    content: item.content || "",
    time: formatMessageTime(item.createTime),
    read: item.readFlag === true,
  };
}

function formatMessageTime(value) {
  if (!value) return "";
  const match = String(value).match(/(?:T|\s)(\d{1,2}):(\d{2})/);
  return match ? `${match[1].padStart(2, "0")}:${match[2]}` : String(value);
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f8f4ed;
}

.content {
  height: calc(100vh - 176rpx);
  background: #fff8e6;
}

.top-actions {
  padding: 18rpx 22rpx 0;
}

.history {
  text-align: right;
  color: #1890ff;
  font-size: 23rpx;
}

.cards {
  padding: 18rpx 22rpx 0;
}

.card {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
  margin-bottom: 12rpx;
  padding: 24rpx 22rpx;
  border-radius: 16rpx;
  background: #fff;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.icon {
  flex-shrink: 0;
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #fff4e6;
  color: #ff6b35;
  text-align: center;
  line-height: 72rpx;
  font-size: 34rpx;
}

.main {
  flex: 1;
  min-width: 0;
}

.head {
  display: flex;
  justify-content: space-between;
  gap: 12rpx;
}

.title {
  font-size: 28rpx;
  font-weight: 700;
  color: #333;
}

.time {
  flex-shrink: 0;
  color: #aaa;
  font-size: 20rpx;
}

.desc {
  display: block;
  margin-top: 10rpx;
  color: #777;
  font-size: 23rpx;
  line-height: 1.6;
}

.dot {
  position: absolute;
  top: 18rpx;
  right: 18rpx;
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: #ff4757;
}

.slogan {
  padding: 54rpx 20rpx 24rpx;
  text-align: center;
}

.big {
  display: block;
  color: #ddd;
  font-size: 36rpx;
  font-weight: 800;
  letter-spacing: 4rpx;
}

.small {
  display: block;
  margin-top: 10rpx;
  color: #c2c2c2;
  font-size: 22rpx;
}
</style>
