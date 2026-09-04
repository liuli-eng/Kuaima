<template>
  <view class="page"
    ><AppNavBar title="历史消息" :show-back="true" /><view class="filters"
      ><text
        v-for="item in types"
        :key="item.key"
        :class="{ active: type === item.key }"
        @click="type = item.key"
        >{{ item.label }}</text
      ></view
    ><scroll-view scroll-y class="content"
      ><view v-for="item in filtered" :key="item.id" class="card"
        ><text class="title">{{ item.title }}</text
        ><text class="content-text">{{ item.content }}</text
        ><text class="time">{{ item.time }}</text></view
      ><view v-if="!filtered.length" class="empty"
        >暂无历史消息</view
      ></scroll-view
    ></view
  >
</template>
<script setup>
import { computed, onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import { listMessages } from "@/api/backend";
const types = [
  { key: "all", label: "全部" },
  { key: "order", label: "订单" },
  { key: "wallet", label: "钱包" },
  { key: "system", label: "系统" },
];
const type = ref("all");
const list = ref([
  {
    id: 1,
    type: "order",
    title: "录用通知",
    content: "你报名的岗位已被录用。",
    time: "08月30日",
  },
  {
    id: 2,
    type: "wallet",
    title: "结算到账",
    content: "订单收入已进入钱包。",
    time: "08月29日",
  },
  {
    id: 3,
    type: "system",
    title: "平台公告",
    content: "请注意保护个人账号安全。",
    time: "08月28日",
  },
]);
const filtered = computed(() =>
  type.value === "all"
    ? list.value
    : list.value.filter((i) => i.type === type.value),
);
onMounted(async () => {
  try {
    const result = await listMessages(uni.getStorageSync("userId") || "2001", {
      page: 0,
      size: 100,
      read: true,
    });
    if (Array.isArray(result))
      list.value = result.map((item) => ({
        id: item.id,
        type:
          item.bizType === "order"
            ? "order"
            : item.bizType === "withdraw" || item.bizType === "settle"
              ? "wallet"
              : "system",
        title: item.title,
        content: item.content,
        time: formatMessageTime(item.createTime),
      }));
  } catch (_) {}
});
function formatMessageTime(value) {
  if (!value) return "";
  const match = String(value).match(/(?:T|\s)(\d{1,2}):(\d{2})/);
  return match ? `${match[1].padStart(2, "0")}:${match[2]}` : String(value);
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}
.filters {
  display: flex;
  background: #fff;
  padding: 0 20rpx;
}
.filters text {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  color: #888;
  font-size: 24rpx;
}
.filters .active {
  color: #ff6b35;
  border-bottom: 4rpx solid #ff6b35;
}
.content {
  height: calc(100vh - 270rpx);
  padding: 22rpx;
  box-sizing: border-box;
}
.card {
  padding: 26rpx;
  background: #fff;
  border-radius: 20rpx;
  margin-bottom: 18rpx;
}
.title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
}
.content-text {
  display: block;
  color: #777;
  font-size: 23rpx;
  margin: 12rpx 0;
}
.time {
  color: #aaa;
  font-size: 20rpx;
}
.empty {
  text-align: center;
  padding: 220rpx;
  color: #aaa;
}
</style>
