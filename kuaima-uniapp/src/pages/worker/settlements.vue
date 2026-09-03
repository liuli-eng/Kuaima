<template>
  <view class="page">
    <AppNavBar title="待结算" :show-back="true" />
    <scroll-view scroll-y class="content">
      <view class="summary-card">
        <text class="summary-label">待结算总额</text>
        <text class="summary-amount">¥{{ totalAmount }}</text>
        <text class="summary-desc">完成工作确认后，预计 1-3 个工作日到账</text>
      </view>

      <text class="section-title">待结算明细</text>
      <view
        v-for="item in records"
        :key="item.id"
        class="card"
        @click="open(item)"
      >
        <view class="head">
          <view class="title-wrap">
            <text class="title-icon">¥</text>
            <text class="title">{{ item.title }}</text>
          </view>
          <text class="status">待结算</text>
        </view>
        <text class="time">工作日期：{{ item.time }}</text>
        <view class="footer">
          <text class="date">预计到账：{{ item.date }}</text>
          <text class="amount">¥{{ item.amount }}</text>
        </view>
      </view>

      <view v-if="!records.length" class="empty">
        <text class="empty-icon">⌁</text>
        <text>暂无待结算记录</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import { request } from "@/api/http";

const records = ref([
  {
    id: "s1",
    title: "餐饮服务员",
    time: "2026-08-31",
    date: "完成确认后",
    amount: "162.00",
  },
]);
const totalAmount = computed(() =>
  records.value
    .reduce((sum, item) => sum + Number(item.amount || 0), 0)
    .toFixed(2),
);

onMounted(async () => {
  try {
    const result = await request({ url: `/settle/worker/${encodeURIComponent(uni.getStorageSync("userId") || "2001")}` });
    if (Array.isArray(result)) records.value = result.map(normalizeSettlement).filter((item) => item.status !== "已支付");
  } catch (_) {}
});

function normalizeSettlement(item) {
  return { ...item, title: item.orderTitle || `结算单 #${item.id}`, time: item.workDate || item.createTime || "", date: item.payTime || "待支付", amount: (Number(item.wage ?? item.totalAmount ?? 0) / 100).toFixed(2), statusText: item.status || "待支付" };
}

function open(item) {
  uni.navigateTo({ url: `/pages/worker/settlement-detail?id=${item.id}` });
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
.summary-card {
  padding: 30rpx;
  border-radius: 24rpx;
  background: linear-gradient(135deg, #ffbd73, #ff8c4d);
  color: #fff;
  box-shadow: 0 12rpx 28rpx rgba(255, 107, 53, 0.16);
}
.summary-label,
.summary-desc {
  display: block;
  font-size: 24rpx;
}
.summary-amount {
  display: block;
  margin: 10rpx 0;
  font-size: 56rpx;
  font-weight: 800;
}
.summary-desc {
  opacity: 0.9;
  font-size: 22rpx;
}
.section-title {
  display: block;
  margin: 30rpx 4rpx 16rpx;
  color: #333;
  font-size: 29rpx;
  font-weight: 700;
}
.card {
  margin-bottom: 18rpx;
  padding: 26rpx 24rpx;
  border-radius: 20rpx;
  background: #fff;
  box-shadow: 0 5rpx 18rpx rgba(88, 64, 32, 0.06);
}
.head,
.footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.title-wrap {
  display: flex;
  align-items: center;
}
.title-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44rpx;
  height: 44rpx;
  margin-right: 12rpx;
  border-radius: 50%;
  background: #fff0e6;
  color: #ff6b35;
  font-size: 25rpx;
  font-weight: 700;
}
.title {
  color: #333;
  font-size: 29rpx;
  font-weight: 700;
}
.status {
  padding: 6rpx 14rpx;
  border-radius: 18rpx;
  background: #fff3e0;
  color: #ff8c00;
  font-size: 21rpx;
}
.time,
.date {
  display: block;
  color: #999;
  font-size: 22rpx;
}
.time {
  margin-top: 16rpx;
}
.footer {
  margin-top: 22rpx;
}
.amount {
  color: #ff6b35;
  font-size: 32rpx;
  font-weight: 800;
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
</style>
