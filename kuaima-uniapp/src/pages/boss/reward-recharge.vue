<template>
  <view class="page">
    <view class="status-spacer" :style="{ height: `${statusBarHeight}px` }" />
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">‹</view>
      <text class="nav-title">账户充值</text>
      <view class="nav-space" />
    </view>

    <scroll-view scroll-y class="content">
      <view class="balance-hero">
        <text class="balance-label">当前余额（元）</text>
        <text class="balance-value"><text>¥</text>{{ money(balance) }}</text>
        <text class="balance-tip">充值后可用于支付零工报酬、购买积分等</text>
      </view>

      <view class="promo-row">
        <view class="promo-item"><text class="promo-badge">充</text><text>充得多送得多</text></view>
        <view class="promo-item"><text class="promo-badge">安</text><text>资金安全保障</text></view>
        <view class="promo-item"><text class="promo-badge">快</text><text>支付结果确认后到账</text></view>
      </view>

      <view v-if="configError" class="config-error" @click="loadConfig">
        {{ configError }}，点击重试
      </view>

      <view class="section-card">
        <text class="section-title">选择充值金额</text>
        <view class="amount-grid">
          <view
            v-for="item in quickAmounts"
            :key="item.amount"
            class="amount-option"
            :class="{ active: selectedAmount === item.amount && !customAmount }"
            @click="selectAmount(item.amount)"
          >
            <text class="amount-num">¥ {{ formatInteger(item.amount) }}</text>
            <text v-if="item.bonusAmount > 0" class="amount-bonus">送 {{ money(item.bonusAmount) }} 元</text>
          </view>
        </view>
        <view class="custom-row">
          <text class="custom-label">自定义</text>
          <text class="custom-yuan">¥</text>
          <input
            v-model="customAmount"
            type="digit"
            class="custom-input"
            :placeholder="`最低${money(minAmount)}元`"
            @input="onCustomInput"
          />
        </view>
      </view>

      <view class="section-card">
        <text class="section-title">选择支付方式</text>
        <view
          v-for="method in payMethods"
          :key="method.code"
          class="pay-item"
          :class="{ active: selectedPayMethod === method.code, disabled: !method.enabled }"
          @click="selectPayMethod(method)"
        >
          <view class="pay-left">
            <view class="pay-icon" :class="method.code === 'WECHAT' ? 'wechat' : 'bank'">
              {{ method.code === "WECHAT" ? "微" : "企" }}
            </view>
            <view class="pay-info">
              <text>{{ method.name }}</text>
              <text v-if="method.description">{{ method.description }}</text>
            </view>
          </view>
          <view class="pay-check"><view v-if="selectedPayMethod === method.code" /></view>
        </view>
      </view>

      <button class="recharge-btn" :disabled="submitting" @click="submitRecharge">
        {{ submitting ? "处理中..." : `立即充值 ¥${money(payAmount)}` }}
      </button>
      <text class="payment-tip">微信收银台完成支付后，以后端支付结果和实际入账为准</text>
      <view class="safe-space" />
    </scroll-view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad, onUnload } from "@dcloudio/uni-app";
import {
  createBossRewardRecharge,
  getBossRewardRecharge,
  getBossRewardRechargeConfig,
} from "@/api/backend";

const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0;
const balance = ref(0);
const minAmount = ref(0.01);
const quickAmounts = ref([
  { amount: 500, bonusAmount: 0 },
  { amount: 1000, bonusAmount: 30 },
  { amount: 2000, bonusAmount: 80 },
  { amount: 5000, bonusAmount: 260 },
  { amount: 10000, bonusAmount: 600 },
  { amount: 20000, bonusAmount: 1400 },
]);
const payMethods = ref([
  { code: "WECHAT", name: "微信支付", enabled: true, description: "微信支付，支付完成后自动入账" },
  { code: "ENTERPRISE_BANK", name: "企业银行账户", enabled: false, description: "充值接口开发中" },
]);
const selectedAmount = ref(2000);
const customAmount = ref("");
const selectedPayMethod = ref("WECHAT");
const submitting = ref(false);
const idempotencyKey = ref("");
const configError = ref("");
let pollingTimer = null;
let pollingActive = false;

const payAmount = computed(() => {
  if (customAmount.value !== "") return Number(customAmount.value) || 0;
  return Number(selectedAmount.value) || 0;
});

onLoad(loadConfig);
onUnload(stopPolling);

async function loadConfig() {
  configError.value = "";
  try {
    const data = await getBossRewardRechargeConfig();
    balance.value = number(data?.balance);
    minAmount.value = Math.max(0.01, number(data?.minAmount, 0.01));
    if (Array.isArray(data?.quickAmounts) && data.quickAmounts.length) {
      quickAmounts.value = data.quickAmounts
        .map((item) => ({ amount: number(item?.amount), bonusAmount: number(item?.bonusAmount) }))
        .filter((item) => item.amount > 0);
      if (!quickAmounts.value.some((item) => item.amount === selectedAmount.value)) {
        selectedAmount.value = quickAmounts.value[0]?.amount || 0;
      }
    }
    if (Array.isArray(data?.payMethods) && data.payMethods.length) {
      payMethods.value = data.payMethods.map((item) => ({
        code: String(item?.code || "").toUpperCase(),
        name: item?.name || item?.code || "支付方式",
        enabled: item?.enabled === true,
        description: item?.description || "",
      }));
      const selected = payMethods.value.find((item) => item.enabled);
      selectedPayMethod.value = selected?.code || "";
    }
  } catch (error) {
    configError.value = error?.message || "充值配置加载失败";
  }
}

function selectAmount(value) {
  selectedAmount.value = number(value);
  customAmount.value = "";
  idempotencyKey.value = "";
}

function onCustomInput(event) {
  const value = String(event?.detail?.value || "").replace(/[^\d.]/g, "");
  const dot = value.indexOf(".");
  customAmount.value = dot < 0
    ? value
    : `${value.slice(0, dot)}.${value.slice(dot + 1).replace(/\./g, "").slice(0, 2)}`;
  idempotencyKey.value = "";
}

function selectPayMethod(method) {
  if (!method.enabled) {
    uni.showToast({ title: method.description || "该支付方式暂不可用", icon: "none" });
    return;
  }
  selectedPayMethod.value = method.code;
  idempotencyKey.value = "";
}

async function submitRecharge() {
  if (submitting.value) return;
  if (!/^\d+(\.\d{1,2})?$/.test(String(payAmount.value)) || payAmount.value < minAmount.value) {
    uni.showToast({ title: `充值金额不能低于${money(minAmount.value)}元`, icon: "none" });
    return;
  }
  if (!selectedPayMethod.value) {
    uni.showToast({ title: "请选择支付方式", icon: "none" });
    return;
  }
  if (selectedPayMethod.value !== "WECHAT") {
    uni.showToast({ title: "企业银行账户充值接口开发中", icon: "none" });
    return;
  }
  // #ifndef MP-WEIXIN
  uni.showToast({ title: "请在微信小程序内完成支付", icon: "none" });
  return;
  // #endif
  if (!idempotencyKey.value) idempotencyKey.value = createIdempotencyKey();
  submitting.value = true;
  try {
    const order = await createBossRewardRecharge(
      Number(payAmount.value.toFixed(2)),
      idempotencyKey.value,
      selectedPayMethod.value,
    );
    const payParams = order?.payParams || {};
    if (!order?.orderNo) throw new Error("充值订单号无效");
    if (!payParams.package || !payParams.paySign || !payParams.nonceStr || !payParams.timeStamp) {
      throw new Error("微信支付参数无效");
    }
    // #ifdef MP-WEIXIN
    await requestPayment(payParams);
    uni.showToast({ title: "支付结果确认中", icon: "none" });
    startPolling(order.orderNo);
    // #endif
  } catch (error) {
    const message = String(error?.errMsg || error?.message || "充值失败");
    uni.showToast({ title: /cancel/i.test(message) ? "支付已取消" : message, icon: "none" });
  } finally {
    submitting.value = false;
  }
}

function requestPayment(payParams) {
  return new Promise((resolve, reject) => uni.requestPayment({
    provider: "wxpay",
    timeStamp: String(payParams.timeStamp),
    nonceStr: String(payParams.nonceStr),
    package: String(payParams.package),
    signType: String(payParams.signType || "RSA"),
    paySign: String(payParams.paySign),
    success: resolve,
    fail: reject,
  }));
}

function startPolling(orderNo) {
  stopPolling();
  pollingActive = true;
  let attempts = 0;
  const poll = async () => {
    if (!pollingActive) return;
    attempts += 1;
    try {
      const order = await getBossRewardRecharge(orderNo);
      const status = String(order?.status || "").toUpperCase();
      if (["PAID", "SUCCESS", "支付成功"].includes(status)) {
        stopPolling();
        idempotencyKey.value = "";
        uni.showToast({ title: "充值成功", icon: "success" });
        await loadConfig();
        return;
      }
      if (["FAILED", "CLOSED", "REFUNDED"].includes(status)) {
        stopPolling();
        uni.showToast({ title: order?.failureReason || "充值未完成", icon: "none" });
        return;
      }
    } catch (_) {}
    if (attempts >= 15) {
      stopPolling();
      uni.showToast({ title: "支付结果确认中，请稍后查看充值记录", icon: "none" });
      return;
    }
    pollingTimer = setTimeout(poll, 2000);
  };
  poll();
}

function stopPolling() {
  pollingActive = false;
  if (pollingTimer) clearTimeout(pollingTimer);
  pollingTimer = null;
}

function createIdempotencyKey() {
  return `boss-reward-recharge-${Date.now()}-${Math.random().toString(36).slice(2)}`;
}
function number(value, fallback = 0) {
  const result = Number(value);
  return Number.isFinite(result) ? result : fallback;
}
function money(value) { return number(value).toFixed(2); }
function formatInteger(value) { return number(value).toLocaleString("zh-CN", { maximumFractionDigits: 2 }); }
function goBack() { uni.navigateBack(); }
</script>

<style scoped>
.page { display:flex; flex-direction:column; height:100vh; overflow:hidden; background:#f3f4f6; }
.status-spacer,.nav-bar { flex-shrink:0; background:#fff; }
.nav-bar { position:relative; display:flex; align-items:center; justify-content:space-between; height:100rpx; padding:0 32rpx; box-sizing:border-box; }
.nav-back,.nav-space { display:flex; align-items:center; width:64rpx; height:64rpx; color:#333; font-size:58rpx; }
.nav-title { position:absolute; left:50%; color:#333; font-size:34rpx; font-weight:600; transform:translateX(-50%); }
.content { flex:1; min-height:0; }
.balance-hero { margin:24rpx 32rpx; padding:44rpx 40rpx; border-radius:32rpx; background:linear-gradient(135deg,#ffe8b0,#ffd96f); box-shadow:0 8rpx 32rpx rgba(255,180,100,.2); }
.balance-label,.balance-value,.balance-tip { display:block; color:#8b4513; }
.balance-label { font-size:24rpx; }.balance-value { margin-top:12rpx; font-size:68rpx; font-weight:800; }.balance-value text { margin-right:4rpx; font-size:36rpx; }.balance-tip { margin-top:20rpx; color:#a0522d; font-size:22rpx; }
.promo-row { display:flex; justify-content:space-around; padding:0 32rpx 28rpx; }.promo-item { display:flex; flex-direction:column; align-items:center; color:#999; font-size:20rpx; }.promo-badge { display:flex; align-items:center; justify-content:center; width:56rpx; height:56rpx; margin-bottom:8rpx; border-radius:50%; color:#ff6b35; background:#ffe8b0; font-weight:700; }
.config-error { margin:0 32rpx 20rpx; padding:18rpx; border-radius:14rpx; color:#ff6b35; background:#fff3ed; font-size:23rpx; text-align:center; }
.section-card { margin:0 32rpx 24rpx; padding:32rpx; border-radius:28rpx; background:#fff; box-shadow:0 2rpx 12rpx rgba(0,0,0,.04); }
.section-title { display:block; margin-bottom:24rpx; color:#333; font-size:26rpx; font-weight:600; }
.amount-grid { display:flex; flex-wrap:wrap; gap:20rpx; }.amount-option { display:flex; flex-direction:column; align-items:center; justify-content:center; width:calc((100% - 40rpx) / 3); min-height:104rpx; border:3rpx solid #eee; border-radius:20rpx; background:#f9fafb; box-sizing:border-box; }.amount-option.active { border-color:#ff6b35; background:#fff3e6; }.amount-num { color:#333; font-size:28rpx; font-weight:700; }.amount-option.active .amount-num { color:#ff6b35; }.amount-bonus { margin-top:6rpx; color:#ff6b35; font-size:20rpx; }
.custom-row { display:flex; align-items:center; gap:14rpx; margin-top:28rpx; padding-top:28rpx; border-top:1rpx solid #f5f5f5; }.custom-label { color:#666; font-size:26rpx; }.custom-yuan { color:#333; font-size:32rpx; font-weight:700; }.custom-input { flex:1; min-width:0; font-size:34rpx; font-weight:700; }
.pay-item { display:flex; align-items:center; justify-content:space-between; padding:20rpx 0; border-bottom:1rpx solid #f5f5f5; }.pay-item:last-child { border-bottom:0; }.pay-item.disabled { opacity:.5; }.pay-left { display:flex; align-items:center; gap:20rpx; }.pay-icon { display:flex; align-items:center; justify-content:center; width:64rpx; height:64rpx; border-radius:16rpx; color:#fff; font-size:24rpx; font-weight:700; }.pay-icon.wechat { background:#07c160; }.pay-icon.bank { background:#1677ff; }.pay-info text { display:block; color:#333; font-size:26rpx; }.pay-info text:last-child { margin-top:4rpx; color:#999; font-size:21rpx; }.pay-check { display:flex; align-items:center; justify-content:center; width:36rpx; height:36rpx; border:3rpx solid #ddd; border-radius:50%; box-sizing:border-box; }.pay-item.active .pay-check { border-color:#ff6b35; background:#ff6b35; }.pay-check view { width:12rpx; height:12rpx; border-radius:50%; background:#fff; }
.recharge-btn { width:calc(100% - 64rpx); height:92rpx; margin:16rpx 32rpx 0; border:0; border-radius:46rpx; color:#fff; background:linear-gradient(135deg,#ff8c5a,#ff6b35); box-shadow:0 8rpx 32rpx rgba(255,107,53,.3); font-size:30rpx; font-weight:600; line-height:92rpx; }.recharge-btn::after { border:0; }.recharge-btn[disabled] { opacity:.6; }
.payment-tip { display:block; margin:20rpx 32rpx 0; color:#999; font-size:21rpx; text-align:center; }.safe-space { height:calc(32rpx + env(safe-area-inset-bottom)); }
</style>
