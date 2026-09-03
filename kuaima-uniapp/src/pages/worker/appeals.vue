<template>
  <view class="page">
    <AppNavBar title="信用分申诉" :show-back="true" />
    <scroll-view scroll-y class="content">
      <view class="tip"
        >如认为信用分扣减有误，请在 7 个工作日内提交申诉和相关证据。</view
      >
      <view v-for="item in records" :key="item.id" class="card">
        <view class="head">
          <text class="title">{{ item.reason }}</text>
          <text :class="['status', item.status]">{{ item.statusText }}</text>
        </view>
        <text class="order">关联订单：{{ item.orderNo }}</text>
        <text class="time">{{ item.time }}</text>
      </view>
      <view v-if="!records.length" class="empty">暂无申诉记录</view>
    </scroll-view>
    <SafeBottomAction
      ><button class="submit" @click="create">
        提交新申诉
      </button></SafeBottomAction
    >
  </view>
</template>
<script setup>
import { onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import SafeBottomAction from "@/components/SafeBottomAction.vue";
import { request } from "@/api/http";

const records = ref([
  {
    id: 1,
    reason: "订单误取消申诉",
    orderNo: "KM20260830001",
    time: "08月31日",
    status: "approved",
    statusText: "申诉通过",
  },
  {
    id: 2,
    reason: "迟到扣分申诉",
    orderNo: "KM20260828006",
    time: "08月29日",
    status: "pending",
    statusText: "审核中",
  },
]);
onMounted(async () => {
  try {
    const result = await request({ url: "/worker/appeals" });
    if (result) records.value = result.records || result;
  } catch (_) {}
});
function create() {
  uni.navigateTo({ url: "/pages/worker/appeal-submit" });
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #fff8e6;
}
.content {
  height: calc(100vh - 176rpx);
  padding: 22rpx;
  box-sizing: border-box;
}
.tip {
  padding: 22rpx;
  border-radius: 18rpx;
  background: #fff4e6;
  color: #8b4513;
  font-size: 23rpx;
  line-height: 1.7;
}
.card {
  margin-top: 18rpx;
  padding: 26rpx;
  border-radius: 20rpx;
  background: #fff;
  box-shadow: 0 5rpx 16rpx rgba(88, 64, 32, 0.05);
}
.head {
  display: flex;
  justify-content: space-between;
}
.title {
  font-size: 28rpx;
  font-weight: 700;
}
.status {
  font-size: 22rpx;
}
.approved {
  color: #52c41a;
}
.pending {
  color: #ff8a00;
}
.order,
.time {
  display: block;
  margin-top: 12rpx;
  color: #888;
  font-size: 22rpx;
}
.empty {
  padding: 200rpx 0;
  text-align: center;
  color: #aaa;
}
.submit {
  width: 100%;
  height: 84rpx;
  border: 0;
  border-radius: 44rpx;
  background: #ff6b35;
  color: #fff;
  font-size: 29rpx;
}
</style>
