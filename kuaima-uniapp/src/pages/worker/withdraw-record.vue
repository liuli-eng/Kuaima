<template>
  <view class="page">
    <AppNavBar title="提现记录" :show-back="true" />
    <scroll-view scroll-y class="content">
      <view v-for="item in records" :key="item.id" class="record-item">
        <view class="record-icon">微</view>
        <view class="record-main">
          <text class="title">{{
            item.typeName || "微信提现"
          }}</text>
          <text class="account">{{ item.account || "当前绑定的微信账户" }}</text>
          <text class="time">{{ item.time }}</text>
        </view>
        <view class="right">
          <text class="amount">-¥{{ item.amount }}</text>
          <text :class="['status-tag', item.status]">{{
            item.statusText
          }}</text>
        </view>
      </view>
      <view v-if="loading" class="page-state">加载中...</view>
      <view v-else-if="loadFailed" class="page-state">
        <text>提现记录加载失败</text>
        <text class="retry" @click="loadRecords">重新加载</text>
      </view>
      <view v-else-if="!records.length && !loadFailed" class="empty">
        <text class="empty-icon">⌁</text>
        <text>暂无提现记录</text>
        <text class="empty-hint">完成订单后即可申请提现</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import { request } from "@/api/http";
const records = ref([]);
const loading = ref(false);
const loadFailed = ref(false);
onMounted(loadRecords);
async function loadRecords() {
  loading.value = true;
  loadFailed.value = false;
  try {
    const result = await request({ url: "/worker/wallet/withdraw-records" });
    records.value = Array.isArray(result)
      ? result.map((item) => ({
        ...item,
        typeName: "微信提现",
        account: item.account || "当前绑定的微信账户",
        time: item.applyTime || item.createTime || "",
        amount: Number(item.amount || 0).toFixed(2),
        ...normalizeStatus(item.statusText || item.status),
      }))
      : [];
  } catch (error) {
    loadFailed.value = true;
    records.value = [];
  } finally {
    loading.value = false;
  }
}
function normalizeStatus(status) {
  const value = String(status || "").toUpperCase();
  if (["已打款", "提现成功"].includes(status) || ["SUCCESS", "PAID"].includes(value))
    return { status: "success", statusText: "提现成功" };
  if (["打款失败", "提现失败"].includes(status) || ["FAILED", "FAILURE"].includes(value))
    return { status: "failed", statusText: "提现失败，余额已退回" };
  return { status: "pending", statusText: "处理中" };
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f8f4ed;
}
.content {
  height: calc(100vh - 176rpx);
  padding: 24rpx;
  box-sizing: border-box;
}
.record-item {
  display: flex;
  align-items: center;
  margin-bottom: 14rpx;
  padding: 24rpx 20rpx;
  border-radius: 18rpx;
  background: #fff;
  box-shadow: 0 5rpx 18rpx rgba(88, 64, 32, 0.05);
}
.record-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72rpx;
  height: 72rpx;
  margin-right: 18rpx;
  border-radius: 50%;
  background: #e8f8e8;
  color: #52c41a;
  font-size: 28rpx;
  font-weight: 700;
}
.record-main {
  flex: 1;
  min-width: 0;
}
.title,
.account,
.time {
  display: block;
}
.title {
  color: #333;
  font-size: 27rpx;
  font-weight: 600;
}
.account,
.time {
  margin-top: 6rpx;
  color: #999;
  font-size: 21rpx;
}
.right {
  text-align: right;
}
.amount {
  display: block;
  color: #333;
  font-size: 29rpx;
  font-weight: 700;
}
.status-tag {
  display: inline-block;
  margin-top: 8rpx;
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
  font-size: 20rpx;
}
.success {
  background: #e8f8e8;
  color: #52c41a;
}
.pending {
  background: #fff3e0;
  color: #ff8c00;
}
.failed {
  background: #fff1f0;
  color: #ff4d4f;
}
.processing {
  background: #e6f7ff;
  color: #1890ff;
}
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 190rpx;
  color: #aaa;
  font-size: 25rpx;
}
.empty-icon {
  margin-bottom: 12rpx;
  color: #d8d0c5;
  font-size: 70rpx;
}
.empty-hint {
  margin-top: 10rpx;
  color: #bbb;
  font-size: 21rpx;
}
.page-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 0 20rpx;
  color: #999;
  font-size: 25rpx;
}
.retry {
  margin-top: 20rpx;
  color: #ff6b35;
}
</style>
