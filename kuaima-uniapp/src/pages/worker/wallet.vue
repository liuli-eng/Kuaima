<template>
  <view class="page">
    <view class="hero" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="nav"
        ><view class="back" @click="goBack">‹</view><text>我的钱包</text
        ><view class="back"
      /></view>
      <text class="label">钱包余额 ？</text>
      <view class="amount"
        ><text>¥</text><text class="num">{{ balance }}</text></view
      >
      <text class="tip-white">可提现到微信零钱，到账时间以微信处理结果为准</text>
    </view>
    <scroll-view scroll-y class="content">
      <view class="panel">
        <view class="tabs"
          ><view
            :class="{ active: activeTab === 'withdraw' }"
            @click="activeTab = 'withdraw'"
            >发起提现</view
          ><view
            :class="{ active: activeTab === 'records' }"
            @click="activeTab = 'records'"
            >提现明细</view
          ></view
        >
        <view v-if="activeTab === 'withdraw'" class="body">
          <view class="balance-row"
            ><text>钱包余额（可提现）</text><text>¥{{ balance }}</text></view
          >
          <view class="amount-input"
            ><text>¥</text
            ><input
              v-model="amount"
              type="digit"
              placeholder="请输入提现金额"
              @input="idempotencyKey = ''"
            /><text class="all" @click="amount = balance; idempotencyKey = ''">全部提现</text></view
          >
          <text class="tip">ⓘ 单笔最低提现 ¥0.01，到账时间以微信处理结果为准</text
          ><text class="method-title">提现方式</text>
          <template v-for="item in accounts" :key="item.type"
            ><view
              class="account"
              :class="{ active: selected === item.type }"
              @click="toggleAccount(item.type)"
              ><view class="account-icon" :class="item.type">{{
                item.icon
              }}</view
              ><view class="account-info"
                ><text>{{ item.name }}</text
                ><text class="desc">{{ item.desc }}</text></view
              ><view class="radio"
                ><view v-if="selected === item.type" /></view></view
            ><view v-if="selected === item.type" class="account-detail"
              ><text>{{ item.account }}</text
              ><text>{{ profileName || "待完善实名信息" }}</text></view
            ></template
          >
          <button
            class="submit"
            :disabled="!canSubmit || submitting"
            @click="submit"
          >
            {{ submitting ? "提交中…" : "确认提现" }}
          </button>
        </view>
        <view v-else class="body records"
          ><view v-for="item in withdrawRecords" :key="item.id" class="record"
            ><view class="record-icon">¥</view
            ><view class="record-main"
              ><text>{{ item.name }}</text
              ><text class="desc">{{ item.time }}</text></view
            ><view class="record-right"
              ><text>-¥{{ item.amount }}</text
              ><text :class="{ pending: isPending(item.status) }">{{
                item.status
              }}</text></view
            ></view
          ><text v-if="!withdrawRecords.length" class="empty"
            >暂无提现记录</text
          ></view
        >
      </view>
      <view class="bottom-space" />
    </scroll-view>
  </view>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { request } from "@/api/http";
import { withdrawWorkerWallet } from "@/api/backend";
const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0;
const balance = ref("0.00"),
  amount = ref(""),
  selected = ref("wechat"),
  activeTab = ref("withdraw"),
  submitting = ref(false),
  withdrawRecords = ref([]);
const userInfo = uni.getStorageSync("userInfo") || {},
  profileName = userInfo.realName || userInfo.nickname || userInfo.name || "";
const idempotencyKey = ref("");
const accounts = [
  {
    type: "wechat",
    icon: "微",
    name: "微信账户",
    desc: "提交后由微信处理到账",
    account: "当前绑定的微信账户",
  },
];
const canSubmit = computed(
  () =>
    /^(\d+)(\.\d{1,2})?$/.test(String(amount.value || "")) &&
    Number(amount.value) >= 0.01 &&
    Number(amount.value) <= Number(balance.value) &&
    Boolean(selected.value),
);
onMounted(loadWallet);
async function loadWallet() {
  try {
    const [wallet, records] = await Promise.all([
      request({ url: "/worker/wallet" }),
      request({ url: "/worker/wallet/withdraw-records" }),
    ]);
    if (wallet)
      balance.value = cents(
        wallet.balance ?? wallet.available ?? wallet.availableBalance ?? 0,
      );
    if (Array.isArray(records)) withdrawRecords.value = records.map(normalize);
  } catch (e) {
    uni.showToast({ title: e?.message || "钱包加载失败", icon: "none" });
  }
}
function cents(value) {
  return Number(value || 0).toFixed(2);
}
function normalize(item) {
  const status = normalizeWithdrawStatus(item.statusText || item.status);
  return {
    ...item,
    name: item.accountName || item.account || "提现申请",
    time: item.applyTime || item.createTime || "",
    amount: cents(item.amount),
    status,
  };
}
function normalizeWithdrawStatus(status) {
  const value = String(status || "").toUpperCase();
  if (["已打款", "SUCCESS", "PAID"].includes(status) || ["SUCCESS", "PAID"].includes(value))
    return "提现成功";
  if (["打款失败", "FAILED", "FAILURE"].includes(status) || ["FAILED", "FAILURE"].includes(value))
    return "提现失败，余额已退回";
  return "处理中";
}
function toggleAccount(type) {
  selected.value = type;
}
function isPending(status) {
  return (
    ["处理中", "申请中", "PENDING", "PROCESSING"].includes(
      String(status || "").toUpperCase(),
    ) || ["处理中", "申请中"].includes(status)
  );
}
function goBack() {
  uni.navigateBack();
}
async function submit() {
  const value = Number(amount.value);
  if (!/^(\d+)(\.\d{1,2})?$/.test(String(amount.value || "")) || value < 0.01)
    return uni.showToast({ title: "请输入有效金额，最多两位小数", icon: "none" });
  if (value > Number(balance.value))
    return uni.showToast({ title: "提现金额不能超过钱包余额", icon: "none" });
  if (!selected.value)
    return uni.showToast({ title: "请选择提现方式", icon: "none" });
  submitting.value = true;
  if (!idempotencyKey.value) idempotencyKey.value = `worker-wallet-withdraw-${Date.now()}-${Math.random().toString(36).slice(2)}`;
  try {
    await withdrawWorkerWallet({ amount: Number(value.toFixed(2)) }, idempotencyKey.value);
    amount.value = "";
    idempotencyKey.value = "";
    uni.showToast({ title: "提现申请已提交，到账时间以微信处理结果为准", icon: "none", duration: 2500 });
    await loadWallet();
    activeTab.value = "records";
  } catch (e) {
    uni.showToast({ title: e?.message || "提现失败", icon: "none" });
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}
.hero {
  padding-bottom: 40rpx;
  color: #fff;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  border-radius: 0 0 48rpx 48rpx;
}
.nav {
  height: 100rpx;
  padding: 0 32rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 34rpx;
  font-weight: 600;
}
.back {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 54rpx;
  font-weight: 400;
}
.label,
.tip-white {
  display: block;
  margin-left: 40rpx;
  color: rgba(255, 255, 255, 0.85);
  font-size: 26rpx;
}
.amount {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
  margin: 8rpx 40rpx 0;
  font-size: 36rpx;
  font-weight: 600;
}
.num {
  font-size: 76rpx;
  font-weight: 800;
}
.tip-white {
  margin-top: 10rpx;
  color: rgba(255, 255, 255, 0.72);
  font-size: 24rpx;
}
.content {
  flex: 1;
  min-height: 0;
  margin-top: -24rpx;
}
.panel {
  position: relative;
  z-index: 2;
  margin: 0 32rpx;
  overflow: hidden;
  background: #fff;
  border-radius: 32rpx;
  box-shadow: 0 8rpx 40rpx rgba(0, 0, 0, 0.06);
}
.tabs {
  display: flex;
  border-bottom: 1rpx solid #f0f0f0;
}
.tabs > view {
  position: relative;
  flex: 1;
  padding: 28rpx 0;
  color: #999;
  font-size: 30rpx;
  text-align: center;
}
.tabs .active {
  color: #ff6b35;
  font-weight: 600;
}
.tabs .active:after {
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 56rpx;
  height: 6rpx;
  content: "";
  background: #ff6b35;
  border-radius: 3rpx;
  transform: translateX(-50%);
}
.body {
  padding: 32rpx 36rpx 40rpx;
}
.balance-row {
  display: flex;
  justify-content: space-between;
  padding: 20rpx 0 28rpx;
  color: #666;
  font-size: 26rpx;
  border-bottom: 1rpx dashed #eee;
}
.balance-row text:last-child {
  color: #ff6b35;
  font-weight: 600;
}
.amount-input {
  height: 96rpx;
  margin-top: 28rpx;
  padding: 0 28rpx;
  display: flex;
  align-items: center;
  gap: 18rpx;
  background: #fafafa;
  border-radius: 24rpx;
  font-size: 40rpx;
  font-weight: 700;
}
.amount-input input {
  flex: 1;
  min-width: 0;
  font-size: 42rpx;
  font-weight: 700;
}
.all {
  padding: 8rpx 20rpx;
  color: #ff6b35;
  font-size: 22rpx;
  background: #fff3ed;
  border-radius: 999rpx;
}
.tip,
.desc {
  display: block;
  color: #999;
  font-size: 22rpx;
}
.tip {
  margin-top: 16rpx;
}
.method-title {
  display: block;
  margin: 30rpx 0 18rpx;
  color: #666;
  font-size: 26rpx;
}
.account {
  display: flex;
  align-items: center;
  gap: 24rpx;
  margin-top: 18rpx;
  padding: 24rpx 28rpx;
  border: 3rpx solid #eee;
  border-radius: 24rpx;
}
.account.active {
  background: #fff9f5;
  border-color: #ff6b35;
}
.account-icon {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  border-radius: 20rpx;
  font-size: 32rpx;
}
.wechat {
  background: #10b981;
}
.alipay {
  background: #3b82f6;
}
.bank {
  background: #6b7280;
}
.account-info {
  flex: 1;
  font-size: 28rpx;
}
.account-info .desc {
  margin-top: 6rpx;
}
.radio {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3rpx solid #ddd;
  border-radius: 50%;
}
.active .radio {
  border-color: #ff6b35;
}
.radio view {
  width: 20rpx;
  height: 20rpx;
  background: #ff6b35;
  border-radius: 50%;
}
.account-detail {
  display: flex;
  justify-content: space-between;
  padding: 16rpx 28rpx;
  color: #666;
  font-size: 23rpx;
  background: #fff9f5;
  border: 3rpx solid #ffe0d0;
  border-top: 0;
  border-radius: 0 0 24rpx 24rpx;
}
.submit {
  width: 100%;
  height: 92rpx;
  margin-top: 38rpx;
  color: #fff;
  font-size: 30rpx;
  line-height: 92rpx;
  background: linear-gradient(135deg, #ff8c5a, #ff6b35);
  border: 0;
  border-radius: 46rpx;
}
.submit:after {
  border: 0;
}
.submit[disabled] {
  opacity: 0.5;
}
.record {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 28rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}
.record-icon {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3b82f6;
  background: #e6f0ff;
  border-radius: 24rpx;
  font-size: 30rpx;
}
.record-main {
  flex: 1;
}
.record-right {
  text-align: right;
}
.record-right > text {
  display: block;
}
.record-right > text:first-child {
  font-size: 30rpx;
  font-weight: 700;
}
.record-right > text:last-child {
  margin-top: 4rpx;
  color: #999;
  font-size: 22rpx;
}
.record-right .pending {
  color: #d97706;
}
.empty {
  display: block;
  padding: 120rpx 0;
  color: #999;
  text-align: center;
}
.bottom-space {
  height: 48rpx;
}
</style>
