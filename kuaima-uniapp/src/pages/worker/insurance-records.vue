<template>
  <view class="page">
    <AppNavBar title="保险报案记录" :show-back="true" />
    <scroll-view scroll-y class="content">
      <view
        v-for="item in records"
        :key="item.id"
        class="card"
        @click="open(item)"
      >
        <view class="head"
          ><text class="title">{{ item.type }}</text
          ><text :class="['status', item.status]">{{
            item.statusText
          }}</text></view
        >
        <text class="line">订单号：{{ item.orderNo }}</text>
        <text class="line">{{ item.time }}</text>
      </view>
      <view v-if="!records.length" class="empty">暂无报案记录</view>
    </scroll-view>
    <SafeBottomAction
      ><button class="submit" @click="create">
        新增报案
      </button></SafeBottomAction
    >
  </view>
</template>
<script setup>
import { onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import SafeBottomAction from "@/components/SafeBottomAction.vue";
import { listInsurance } from "@/api/backend";
const records = ref([
  {
    id: "i1",
    type: "意外受伤",
    orderNo: "KM20260831002",
    time: "09月01日 14:20",
    status: "processing",
    statusText: "处理中",
  },
]);
onMounted(async () => {
  try {
    const result = await listInsurance(uni.getStorageSync("userId") || "2001");
    const rows = Array.isArray(result) ? result : result?.records || [];
    records.value = rows.map((item) => ({
      ...item,
      orderNo: item.orderNo || item.orderId || "",
      time: item.startTime || item.createTime || "",
      statusText: item.status || "处理中",
    }));
  } catch (_) {
    records.value = [];
  }
});
function open(item) {
  uni.navigateTo({ url: `/pages/worker/insurance-detail?id=${item.id}` });
}
function create() {
  uni.navigateTo({ url: "/pages/worker/insurance" });
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}
.content {
  height: calc(100vh - 176rpx);
  padding: 22rpx;
  box-sizing: border-box;
}
.card {
  padding: 26rpx;
  border-radius: 20rpx;
  background: #fff;
  margin-bottom: 18rpx;
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
  color: #ff8a00;
  font-size: 22rpx;
}
.line {
  display: block;
  margin-top: 12rpx;
  color: #888;
  font-size: 22rpx;
}
.empty {
  padding: 220rpx 0;
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
