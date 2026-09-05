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
          <input v-model="amount" type="digit" placeholder="请输入提现金额" />
          <text class="all" @click="amount = available">全部</text>
        </view>
        <text class="label">提现方式</text>
        <picker
          :range="methods"
          @change="method = methods[$event.detail.value]"
        >
          <view class="picker">{{ method }}<text>›</text></view>
        </picker>
        <text class="tip">提现将在1-3个工作日内到账，请确认账号信息准确。</text>
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
import SafeBottomAction from "@/components/SafeBottomAction.vue";
import { request } from "@/api/http";
const available = ref("0.00");
const amount = ref("");
const methods = ["微信零钱", "支付宝", "银行卡"];
const method = ref(methods[0]);
const submitting = ref(false);
onMounted(async () => {
  try {
    const r = await request({ url: "/worker/wallet" });
    if (r)
      available.value = (Number(r.balance ?? r.available ?? 0) / 100).toFixed(
        2,
      );
  } catch (_) {}
});
async function submit() {
  const value = Number(amount.value);
  if (!value || value <= 0)
    return uni.showToast({ title: "请输入有效金额", icon: "none" });
  if (value > Number(available.value))
    return uni.showToast({ title: "提现金额不能超过可提现余额", icon: "none" });
  submitting.value = true;
  try {
    await request({
      url: "/worker/wallet/withdraw",
      method: "POST",
      data: { amount: value, account: method.value },
    });
    uni.showToast({ title: "提现申请已提交", icon: "success" });
    setTimeout(
      () => uni.navigateTo({ url: "/pages/worker/withdraw-record" }),
      500,
    );
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
