<template>
  <view class="page">
    <AppNavBar title="申请提现" :show-back="true" />
    <scroll-view scroll-y class="content">
      <view class="balance">
        <text>可提现余额(元)</text>
        <text class="balance-value">¥{{ available }}</text>
        <text class="balance-desc">到账时间：1-3 个工作日</text>
      </view>
      <view class="form">
        <text class="label">提现金额</text>
        <view class="amount-row">
          <text class="currency">¥</text>
          <input v-model="amount" type="digit" placeholder="请输入提现金额" @input="idempotencyKey = ''" />
          <text class="all" @click="amount = available; idempotencyKey = ''">全部</text>
        </view>
        <text class="label">提现方式</text>
        <view class="picker">微信零钱<text>微信处理</text></view>
        <text class="tip">提现申请提交后，到账时间以微信处理结果为准。</text>
      </view>
    </scroll-view>
    <SafeBottomAction>
      <button class="submit" :disabled="submitting" @click="submit">
        {{ submitting ? "提交中…" : "确认提现" }}
      </button>
    </SafeBottomAction>
  </view>
</template>
<script setup>
import { onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import SafeBottomAction from "@/components/safe-bottom-action.vue";
import { request } from "@/api/http";
import { withdrawWorkerWallet } from "@/api/backend";
const available = ref("0.00");
const amount = ref("");
const submitting = ref(false);
const idempotencyKey = ref("");
onMounted(async () => {
  try {
    const r = await request({ url: "/worker/wallet" });
    if (r)
      available.value = Number(r.balance ?? r.available ?? r.availableBalance ?? 0).toFixed(2);
  } catch (_) {}
});
async function submit() {
  const value = Number(amount.value);
  if (!/^\d+(\.\d{1,2})?$/.test(String(amount.value || "")) || !value || value < 0.01)
    return uni.showToast({ title: "请输入有效金额，最多两位小数", icon: "none" });
  if (value > Number(available.value))
    return uni.showToast({ title: "提现金额不能超过可提现余额", icon: "none" });
  submitting.value = true;
  if (!idempotencyKey.value) idempotencyKey.value = `worker-wallet-withdraw-${Date.now()}-${Math.random().toString(36).slice(2)}`;
  try {
    await withdrawWorkerWallet({ amount: Number(value.toFixed(2)) }, idempotencyKey.value);
    amount.value = "";
    idempotencyKey.value = "";
    uni.showToast({ title: "提现申请已提交，到账时间以微信处理结果为准", icon: "none", duration: 2500 });
    uni.navigateTo({ url: "/pages/worker/withdraw-record" });
  } catch (e) {
    uni.showToast({ title: e.message || "提现失败", icon: "none" });
  } finally {
    submitting.value = false;
  }
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f8f4ed;
}
.content {
  height: calc(100vh - 176rpx);
}
.balance {
  margin: 24rpx;
  padding: 30rpx;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #ffbd73, #ff8c4d);
  color: #fff;
  font-size: 24rpx;
}
.balance-value {
  display: block;
  margin: 12rpx 0 6rpx;
  font-size: 56rpx;
  font-weight: 800;
}
.balance-desc {
  display: block;
  opacity: 0.9;
  font-size: 22rpx;
}
.form {
  margin: 0 24rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
}
.label {
  display: block;
  color: #333;
  font-size: 27rpx;
  margin-bottom: 16rpx;
}
.amount-row {
  display: flex;
  align-items: center;
  padding: 18rpx 16rpx;
  border: 1rpx solid #e8e3dc;
  border-radius: 14rpx;
  margin-bottom: 28rpx;
  font-size: 38rpx;
}
.amount-row input {
  flex: 1;
  margin: 0 14rpx;
  font-size: 34rpx;
}
.currency {
  font-size: 38rpx;
  color: #333;
}
.all {
  color: #ff6b35;
  font-size: 23rpx;
}
.picker {
  display: flex;
  justify-content: space-between;
  padding: 24rpx 0;
  color: #666;
  border-bottom: 1rpx solid #eee;
  font-size: 26rpx;
}
.tip {
  display: block;
  color: #999;
  font-size: 21rpx;
  line-height: 1.7;
  margin-top: 22rpx;
}
.submit {
  width: 100%;
  height: 84rpx;
  line-height: 84rpx;
  border: 0;
  border-radius: 48rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: #fff;
  font-size: 30rpx;
  font-weight: 800;
}
.submit[disabled] {
  background: #bbb;
  color: #fff;
}
</style>
