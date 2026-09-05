<template>
  <view class="page">
    <AppNavBar title="提现记录" :show-back="true" />
    <scroll-view scroll-y class="content">
      <view v-for="item in records" :key="item.id" class="record-item">
        <view :class="['record-icon', item.type || 'wechat']">{{
          item.type === "alipay" ? "支" : "微"
        }}</view>
        <view class="record-main">
          <text class="title">{{
            item.typeName || `提现到${item.method}`
          }}</text>
          <text class="account">{{ item.account || item.method }}</text>
          <text class="time">{{ item.time }}</text>
        </view>
        <view class="right">
          <text class="amount">-¥{{ item.amount }}</text>
          <text :class="['status-tag', item.status]">{{
            item.statusText
          }}</text>
        </view>
      </view>
      <view v-if="!records.length" class="empty">
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
const records = ref([
  {
    id: 1,
    type: "wechat",
    typeName: "微信支付",
    account: "138****8888",
    method: "微信零钱",
    time: "2026-08-18 10:15",
    amount: "100.00",
    status: "success",
    statusText: "已到账",
  },
]);
onMounted(async () => {
  try {
    const result = await request({ url: "/worker/wallet/withdraw-records" });
    if (Array.isArray(result))
      records.value = result.map((item) => ({
        ...item,
        type: item.account?.includes("支付宝") ? "alipay" : "wechat",
        typeName: item.account || "提现申请",
        method: item.account || item.channel || "",
        time: item.applyTime || item.createTime || "",
        amount: (Number(item.amount || 0) / 100).toFixed(2),
        statusText: item.status || "申请中",
      }));
  } catch (_) {}
});
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
.record-icon.alipay {
  background: #e6f7ff;
  color: #1890ff;
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
</style>
