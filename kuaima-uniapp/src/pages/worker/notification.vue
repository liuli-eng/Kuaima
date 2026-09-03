<template>
  <view class="page">
    <AppNavBar title="系统通知" :show-back="true" />
    <view class="tabs">
      <text
        v-for="tab in tabs"
        :key="tab.key"
        :class="['tab', { active: activeType === tab.key }]"
        @click="activeType = tab.key"
        >{{ tab.label }}</text
      >
    </view>
    <scroll-view scroll-y class="scroll">
      <text class="date-title">今日</text>
      <view
        v-for="item in todayList"
        :key="item.id"
        class="notice-card"
        @click="open(item)"
      >
        <text :class="['notice-icon', item.type]">{{ item.icon }}</text>
        <view class="notice-main"
          ><view class="notice-head"
            ><text class="notice-title">{{ item.title }}</text
            ><text class="time">{{ item.time }}</text></view
          ><text class="desc">{{ item.desc }}</text
          ><text v-if="item.action" class="action"
            >{{ item.action }} ›</text
          ></view
        >
        <text v-if="!item.read" class="unread" />
      </view>
      <text class="date-title">昨天</text>
      <view
        v-for="item in yesterdayList"
        :key="item.id"
        class="notice-card"
        @click="open(item)"
      >
        <text :class="['notice-icon', item.type]">{{ item.icon }}</text>
        <view class="notice-main"
          ><view class="notice-head"
            ><text class="notice-title">{{ item.title }}</text
            ><text class="time">{{ item.time }}</text></view
          ><text class="desc">{{ item.desc }}</text
          ><text v-if="item.action" class="action"
            >{{ item.action }} ›</text
          ></view
        >
        <text v-if="!item.read" class="unread" />
      </view>
      <view v-if="!todayList.length && !yesterdayList.length" class="empty"
        >暂无相关通知</view
      >
    </scroll-view>
  </view>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import { listMessages, readMessage } from "@/api/backend";

const tabs = [
  { key: "all", label: "全部" },
  { key: "order", label: "订单" },
  { key: "activity", label: "活动" },
  { key: "notice", label: "公告" },
];
const activeType = ref("all");
const notices = ref([
  {
    id: 1,
    day: "today",
    type: "order",
    icon: "🔔",
    title: "订单接单成功",
    time: "10:30",
    desc: "您已成功接单【餐饮清洁工】任务，明天08:00-18:00，请准时到达泗泾镇工作地点。",
    action: "查看订单详情",
    read: false,
  },
  {
    id: 2,
    day: "today",
    type: "activity",
    icon: "🎁",
    title: "新用户福利",
    time: "09:15",
    desc: "恭喜您成为快马日结新用户！完成首个订单可获得额外奖励金20元，快来接单赚钱吧！",
    action: "去完成首单",
    read: false,
  },
  {
    id: 3,
    day: "today",
    type: "notice",
    icon: "✓",
    title: "实名认证审核通过",
    time: "08:00",
    desc: "您的实名认证已审核通过，现在可以正常抢单赚钱啦！",
    read: true,
  },
  {
    id: 4,
    day: "yesterday",
    type: "order",
    icon: "¥",
    title: "工资结算通知",
    time: "18:30",
    desc: "您昨天完成的【快递分拣员】订单工资96元已到账，可在钱包中查看并提现。",
    read: true,
  },
  {
    id: 5,
    day: "yesterday",
    type: "notice",
    icon: "!",
    title: "平台公告",
    time: "15:00",
    desc: "关于平台服务规则更新的通知，请前往平台规则查看详情。",
    action: "查看规则",
    read: true,
  },
]);
onMounted(async () => {
  try {
    const result = await listMessages(uni.getStorageSync("userId") || "2001");
    if (Array.isArray(result)) notices.value = result.map(normalizeNotice);
  } catch (_) {}
});
const visible = computed(() =>
  activeType.value === "all"
    ? notices.value
    : notices.value.filter((item) => item.type === activeType.value),
);
const todayList = computed(() =>
  visible.value.filter((item) => item.day === "today"),
);
const yesterdayList = computed(() =>
  visible.value.filter((item) => item.day === "yesterday"),
);
async function open(item) {
  try { await readMessage(item.id, uni.getStorageSync("userId") || "2001"); } catch (_) {}
  item.read = true;
  if (item.action === "查看订单详情") {
    uni.navigateTo({ url: "/pages/worker/order-detail?id=o1" });
    return;
  }
  if (item.action === "查看规则") {
    uni.navigateTo({ url: "/pages/worker/rule" });
    return;
  }
  if (item.action === "去完成首单") {
    uni.navigateTo({ url: "/pages/worker/home" });
    return;
  }
  uni.navigateTo({
    url: `/pages/worker/notification-detail?id=${item.id}&title=${encodeURIComponent(item.title)}&desc=${encodeURIComponent(item.desc)}`,
  });
}
function normalizeNotice(item) {
  const type = item.bizType === "order" || item.bizType === "item" ? "order" : item.bizType === "settle" ? "activity" : "notice";
  const today = new Date().toDateString() === new Date(item.createTime).toDateString();
  return { ...item, day: today ? "today" : "yesterday", type, icon: type === "order" ? "🔔" : type === "activity" ? "¥" : "!", desc: item.content || "", time: item.createTime || "", read: item.readFlag === true, action: item.bizType === "order" || item.bizType === "item" ? "查看订单详情" : "" };
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
