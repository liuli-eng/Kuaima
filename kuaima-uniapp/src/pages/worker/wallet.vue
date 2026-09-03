<template>
  <view class="page"
    ><AppNavBar title="钱包" :show-back="true" /><scroll-view
      scroll-y
      class="content"
    >
      <view class="balance"
        ><view
          ><text class="label">可提现余额(元)</text
          ><text class="amount">{{ balance }}</text
          ><text class="desc"
            >累计收入 ¥{{ total }} · 可提现 ¥{{ balance }}</text
          ></view
        ><button class="withdraw" @click="submit">↓ 立即提现</button></view
      >
      <view class="tabs"
        ><view
          class="tab"
          :class="{ active: activeTab === 'withdraw' }"
          @click="activeTab = 'withdraw'"
          >申请提现</view
        ><view
          class="tab"
          :class="{ active: activeTab === 'records' }"
          @click="activeTab = 'records'"
          >提现记录</view
        ></view
      >
      <view v-if="activeTab === 'withdraw'" class="withdraw-view"
        ><view class="form"
          ><text class="form-title">填写提现信息</text
          ><text class="form-label"
            >提现金额<text class="required">*</text></text
          ><view class="amount-input"
            ><text>¥</text
            ><input
              v-model="amount"
              type="number"
              placeholder="请输入提现金额"
            /><text class="all" @click="amount = balance">全部提现</text></view
          ><text class="form-label"
            >提现账号<text class="required">*</text></text
          ><view
            v-for="item in accounts"
            :key="item.type"
            class="account"
            @click="selected = item.type"
            ><text class="account-icon" :class="item.type">{{ item.icon }}</text
            ><view
              ><text class="account-name">{{ item.name }}</text
              ><text class="account-desc">{{ item.desc }}</text></view
            ><text class="radio" :class="{ checked: selected === item.type }"
              >●</text
            ></view
          ><view class="tip"
            >ⓘ 提现金额将在1-3个工作日内到账，请确保填写的账号信息正确。</view
          ><button class="submit" @click="submit">确认提现</button></view
        ><view class="detail-section"
          ><text class="detail-title">收支明细</text
          ><view v-for="item in details" :key="item.id" class="detail-item"
            ><text class="detail-icon" :class="item.type">{{
              item.type === "income" ? "●" : "↓"
            }}</text
            ><view class="detail-info"
              ><text>{{ item.title }}</text
              ><text>{{ item.time }}</text></view
            ><text class="detail-amount" :class="item.type"
              >{{ item.type === "income" ? "+" : "-" }}{{ item.amount }}</text
            ></view
          ></view
        ></view
      >
      <view v-else class="records"
        ><view
          v-for="item in withdrawRecords"
          :key="item.id"
          class="record-item"
          ><text class="record-icon">↓</text
          ><view
            ><text class="record-name">{{ item.name }}</text
            ><text class="record-time">{{ item.time }}</text></view
          ><view class="record-right"
            ><text>-{{ item.amount }}</text
            ><text>{{ item.status }}</text></view
          ></view
        ><text v-if="!withdrawRecords.length" class="empty"
          >暂无提现记录</text
        ></view
      >
    </scroll-view></view
  >
</template>
<script setup>
import { onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import { request } from "@/api/http";
const balance = ref("0.00"),
  total = ref("0.00"),
  amount = ref(""),
  selected = ref("wechat"),
  activeTab = ref("withdraw");
const details = ref([]);
const withdrawRecords = ref([]);
const accounts = [
  { type: "wechat", icon: "微", name: "微信支付账号", desc: "提现到微信零钱" },
  { type: "alipay", icon: "支", name: "支付宝账号", desc: "提现到支付宝余额" },
  { type: "bank", icon: "行", name: "银行账号", desc: "提现到绑定的银行卡" },
];
onMounted(async () => {
  try {
    const data = await request({ url: "/worker/wallet" });
    if (data) {
      balance.value = fromCents(data.balance ?? data.available ?? 0);
      total.value = fromCents(data.totalIncome ?? data.total ?? 0);
    }
    const [flows, withdraws] = await Promise.all([
      request({ url: "/worker/wallet/records" }),
      request({ url: "/worker/wallet/withdraw-records" }),
    ]);
    if (Array.isArray(flows)) details.value = flows.map(normalizeFlow);
    if (Array.isArray(withdraws)) withdrawRecords.value = withdraws.map(normalizeWithdraw);
  } catch (_) {}
});
function fromCents(value) { return (Number(value || 0) / 100).toFixed(2); }
function normalizeFlow(item) {
  const income = item.direction === "income";
  return { ...item, title: item.remark || (item.bizType === "WAGE" ? "订单收入" : "提现"), time: item.createTime || item.updateTime || "", amount: fromCents(item.amount), type: income ? "income" : "out" };
}
function normalizeWithdraw(item) {
  return { ...item, name: item.account || "提现申请", time: item.applyTime || item.createTime || "", amount: fromCents(item.amount), status: item.status, statusText: item.status || "申请中" };
}
function submit() {
  uni.showToast({ title: "当前无可提现余额", icon: "none" });
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}
.content {
  height: calc(100vh - 176rpx);
  padding: 24rpx;
  box-sizing: border-box;
}
.balance {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 34rpx 32rpx;
  min-height: 210rpx;
  border-radius: 28rpx;
  background: linear-gradient(135deg, #ffe4b5, #ffd700);
}
.label {
  display: block;
  color: #666;
  font-size: 26rpx;
}
.amount {
  display: block;
  margin: 10rpx 0;
  color: #333;
  font-size: 60rpx;
  font-weight: 700;
}
.desc {
  color: #888;
  font-size: 22rpx;
}
.withdraw,
.submit {
  border: 0;
  border-radius: 40rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: #fff;
  font-size: 28rpx;
  font-weight: 600;
}
.withdraw {
  margin: 0;
  padding: 0 30rpx;
  height: 72rpx;
  line-height: 72rpx;
}
.withdraw::after,
.submit::after {
  border: 0;
}
.tabs {
  display: flex;
  margin-top: 24rpx;
  border-radius: 20rpx 20rpx 0 0;
  background: #fff;
}
.tab {
  position: relative;
  flex: 1;
  padding: 28rpx 0;
  text-align: center;
  color: #666;
  font-size: 30rpx;
}
.tab.active {
  color: #ff6b35;
  font-weight: 600;
}
.tab.active:after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 60rpx;
  height: 6rpx;
  border-radius: 6rpx;
  background: #ff6b35;
  transform: translateX(-50%);
}
.form {
  padding: 32rpx;
  border-radius: 0 0 20rpx 20rpx;
  background: #fff;
}
.form-title {
  display: block;
  margin-bottom: 28rpx;
  color: #333;
  font-size: 34rpx;
  font-weight: 700;
}
.form-label {
  display: block;
  margin: 22rpx 0 12rpx;
  color: #666;
  font-size: 26rpx;
}
.required {
  color: #ff4d4f;
}
.amount-input {
  display: flex;
  align-items: center;
  height: 96rpx;
  padding: 0 24rpx;
  border: 2rpx solid #ddd;
  border-radius: 20rpx;
  color: #333;
  font-size: 40rpx;
}
.amount-input input {
  flex: 1;
  margin-left: 16rpx;
  font-size: 30rpx;
}
.all {
  color: #ff6b35;
  font-size: 22rpx;
}
.account {
  display: flex;
  align-items: center;
  padding: 24rpx;
  border: 2rpx solid #eee;
  border-radius: 20rpx;
  margin-top: 16rpx;
}
.account-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 88rpx;
  height: 88rpx;
  margin-right: 20rpx;
  border-radius: 22rpx;
  font-size: 34rpx;
}
.wechat {
  background: #e8f8e8;
  color: #09bb07;
}
.alipay {
  background: #e6f0ff;
  color: #1677ff;
}
.bank {
  background: #fff5e6;
  color: #ff8c00;
}
.account-name,
.account-desc {
  display: block;
}
.account-name {
  color: #333;
  font-size: 30rpx;
}
.account-desc {
  margin-top: 6rpx;
  color: #999;
  font-size: 22rpx;
}
.radio {
  margin-left: auto;
  color: #ddd;
  font-size: 34rpx;
}
.radio.checked {
  color: #ff6b35;
}
.tip {
  margin: 24rpx 0;
  color: #999;
  font-size: 22rpx;
  line-height: 1.6;
}
.submit {
  width: 100%;
  height: 84rpx;
  line-height: 84rpx;
}
.records {
  min-height: 360rpx;
  padding: 80rpx 0;
  border-radius: 0 0 20rpx 20rpx;
  background: #fff;
}
.detail-section {
  margin-top: 24rpx;
  padding: 28rpx 24rpx;
  background: #fff;
}
.detail-title {
  display: block;
  margin-bottom: 16rpx;
  color: #333;
  font-size: 32rpx;
  font-weight: 700;
}
.detail-item {
  display: flex;
  align-items: center;
  padding: 22rpx 0;
  border-bottom: 1rpx solid #f2f2f2;
}
.detail-icon {
  width: 64rpx;
  height: 64rpx;
  margin-right: 18rpx;
  border-radius: 50%;
  text-align: center;
  line-height: 64rpx;
  background: #e6f7ff;
  color: #1890ff;
  font-size: 28rpx;
}
.detail-icon.out {
  background: #fff1f0;
  color: #ff4d4f;
}
.detail-info {
  flex: 1;
}
.detail-info text {
  display: block;
}
.detail-info text:first-child {
  color: #333;
  font-size: 27rpx;
}
.detail-info text:last-child {
  margin-top: 6rpx;
  color: #999;
  font-size: 22rpx;
}
.detail-amount {
  color: #ff6b35;
  font-size: 30rpx;
  font-weight: 600;
}
.detail-amount.out {
  color: #999;
}
.record-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f2f2f2;
}
.record-icon {
  width: 64rpx;
  height: 64rpx;
  margin-right: 18rpx;
  border-radius: 50%;
  background: #fff1f0;
  color: #ff4d4f;
  text-align: center;
  line-height: 64rpx;
  font-size: 30rpx;
}
.record-name,
.record-time,
.record-right text {
  display: block;
}
.record-name {
  color: #333;
  font-size: 27rpx;
}
.record-time {
  margin-top: 6rpx;
  color: #999;
  font-size: 22rpx;
}
.record-right {
  margin-left: auto;
  text-align: right;
  color: #999;
  font-size: 24rpx;
}
.record-right text:last-child {
  margin-top: 6rpx;
  color: #52c41a;
  font-size: 21rpx;
}
.empty {
  display: block;
  text-align: center;
  color: #999;
  font-size: 26rpx;
}
</style>
