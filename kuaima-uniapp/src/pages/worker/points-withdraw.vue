<template>
  <view class="page">
    <view class="status-spacer" :style="{ height: `${statusBarHeight}px` }" />
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <image src="/static/icons/boss-points/arrow-left-dark.svg" mode="aspectFit" />
      </view>
      <text class="nav-title">积分提现</text>
      <text class="nav-link" @click="openRecords">提现记录</text>
    </view>
    <scroll-view scroll-y class="page-scroll">
      <view v-if="configLoading" class="page-state">提现配置加载中...</view>
      <view v-else-if="configError" class="page-state error" @click="loadConfig">{{ configError }}，点击重试</view>
      <template v-else>
      <view class="balance-card">
        <view class="balance-top">
          <text>可提现积分</text>
        </view>
        <view class="balance-value">{{ formatPoints(balance) }}<text>积分</text></view>
        <text class="balance-sub">约可提现 ¥{{ balanceMoney }}</text>
        <view class="balance-rate"><image src="/static/icons/worker-points-withdraw/coins-white.svg" mode="aspectFit" />兑换比例 {{ exchangeRate }}积分 = {{ cashRate }}元，手续费 ¥{{ formatMoney(fee) }}</view>
      </view>

      <view class="records-entry" @click="openRecords">
        <view><text class="records-entry-title">积分提现记录</text><text class="records-entry-desc">查看处理中、已到账和失败记录</text></view>
        <text class="records-entry-arrow">›</text>
      </view>

      <view class="form-card">
        <text class="field-label">到账方式</text>
        <view
          v-for="channel in channels"
          :key="channel.code"
          class="channel-item"
          :class="{ active: selectedChannel === channel.code, unavailable: !channel.available }"
          @click="selectChannel(channel)"
        >
          <view class="channel-icon" :class="String(channel.code || '').toLowerCase()">
            <image :src="channel.icon" mode="aspectFit" />
          </view>
          <view class="channel-info">
            <text class="channel-name">{{ channel.name }}</text>
            <text class="channel-desc">{{ channel.available ? (channel.accountMasked || '已绑定账户') : '暂不可用' }} · 预计T+1到账</text>
          </view>
          <view class="radio-dot" />
        </view>
      </view>

      <view class="form-card">
        <text class="field-label">提现积分</text>
        <view class="amount-box" :class="{ focused: amount }">
          <view class="amount-row">
            <input
              v-model="amountInput"
              class="amount-input"
              type="number"
              :placeholder="`最低${formatPoints(minPoints)}积分起提`"
              placeholder-style="font-size: 15px; font-weight: 600; color: #cccccc;"
              @input="onAmountInput"
            />
            <text class="amount-unit">积分</text>
            <text class="amount-all" @click="useAll">全部提现</text>
          </view>
          <view class="amount-meta">
            <text>需为{{ formatPoints(multiple) }}的整数倍</text>
            <text>可提现 {{ formatPoints(balance) }} 积分</text>
          </view>
        </view>
        <view class="quick-row">
          <text
            v-for="value in quickValues"
            :key="value"
            class="quick-chip"
            :class="{ active: amount === value }"
            @click="pickAmount(value)"
          >{{ formatPoints(value) }}</text>
          <text class="quick-chip" :class="{ active: amount === maxWithdrawPoints }" @click="useAll">全部</text>
        </view>

        <view class="calc-block">
          <view class="calc-row highlight"><text>预计到账金额</text><text class="money">¥{{ moneyValue }}</text></view>
          <view class="calc-row"><text>兑换比例</text><text>{{ exchangeRate }}积分 = {{ cashRate }}元</text></view>
          <view class="calc-row"><text>提现积分</text><text>{{ formatPoints(amount) }} 积分</text></view>
          <view class="calc-row"><view class="fee-label"><text>手续费</text><image src="/static/icons/worker-points-withdraw/circle-info-gray.svg" mode="aspectFit" /></view><text class="free">¥{{ formatMoney(fee) }}</text></view>
        </view>
      </view>

      <view class="rule-card">
        <view class="rule-title"><image src="/static/icons/worker-points-withdraw/lightbulb-orange.svg" mode="aspectFit" />提现规则</view>
        <view class="rule-item"><image src="/static/icons/worker-points-withdraw/circle-peach.svg" mode="aspectFit" /><text>最低 {{ formatPoints(minPoints) }} 积分起提，提现积分须为 {{ formatPoints(multiple) }} 的整数倍；</text></view>
        <view class="rule-item"><image src="/static/icons/worker-points-withdraw/circle-peach.svg" mode="aspectFit" /><text>兑换比例 {{ exchangeRate }}积分={{ cashRate }}元，手续费 ¥{{ formatMoney(fee) }}；</text></view>
        <view class="rule-item"><image src="/static/icons/worker-points-withdraw/circle-peach.svg" mode="aspectFit" /><text>申请后积分立即冻结，预计 T+1（明日24:00前）到账，节假日顺延；</text></view>
        <view class="rule-item"><image src="/static/icons/worker-points-withdraw/circle-peach.svg" mode="aspectFit" /><text>每日最多提现 {{ dailyLimit }} 次；提现失败时积分将原路退回，可在提现记录查看。</text></view>
      </view>
      <view class="bottom-space" />
      </template>
    </scroll-view>

    <SafeBottomAction>
      <button class="submit-btn" :class="{ disabled: !canSubmit }" :disabled="!canSubmit" @click="openConfirm">{{ todayApplied ? '今日提现次数已用完' : '立即提现' }}</button>
    </SafeBottomAction>

    <view v-if="confirmVisible || successVisible" class="modal-mask" @click="closeSheets" />
    <view v-if="confirmVisible" class="confirm-sheet">
      <view class="sheet-handle" />
      <text class="sheet-title">确认提现信息</text>
      <view class="confirm-money"><text class="currency">¥</text>{{ moneyValue }}<text>{{ formatPoints(amount) }} 积分</text></view>
      <view class="confirm-row"><text>到账方式</text><text>{{ selectedChannelItem.name }}（{{ selectedChannelItem.accountMasked || '已绑定账户' }}）</text></view>
      <view class="confirm-row"><text>手续费</text><text class="free">¥{{ formatMoney(fee) }}</text></view>
      <view class="confirm-row"><text>预计到账</text><text>T+1 · 明日24:00前</text></view>
      <view class="sheet-actions">
        <button class="line-btn" @click="confirmVisible = false">再想想</button>
        <button class="main-btn" :disabled="submitting" @click="submitWithdraw">{{ submitting ? "提交中…" : "确认提现" }}</button>
      </view>
    </view>

    <view v-if="successVisible" class="success-sheet">
      <view class="success-wrap">
        <view class="success-circle"><image src="/static/icons/worker-messages/check-green.svg" mode="aspectFit" /></view>
        <text class="success-title">提现申请已提交</text>
        <text class="success-desc">积分已冻结，预计 <text class="success-orange">{{ formatDate(successResult.expectedArrivalAt) }}</text> 前到账<br />到账结果将通过站内消息通知你</text>
        <view class="success-card">
          <view class="confirm-row"><text>提现积分</text><text>{{ formatPoints(successResult.points) }} 积分</text></view>
          <view class="confirm-row"><text>到账金额</text><text class="success-orange">¥{{ formatMoney(successResult.amount) }}</text></view>
          <view class="confirm-row"><text>到账方式</text><text>{{ channelName(successResult.channel) }}</text></view>
          <view class="confirm-row"><text>申请单号</text><text>{{ successResult.withdrawNo }}</text></view>
        </view>
        <view class="sheet-actions">
          <button class="line-btn" @click="openRecords">查看记录</button>
          <button class="main-btn" @click="finish">完成</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad, onShow } from "@dcloudio/uni-app";
import SafeBottomAction from "@/components/safe-bottom-action.vue";
import { getWorkerPointsWithdrawConfig, createWorkerPointsWithdrawal } from "@/api/backend";

const balance = ref(0);
const statusBarHeight = ref(0);
const amountInput = ref("");
const exchangeRate = ref(100);
const cashRate = ref(1);
const minPoints = ref(1000);
const multiple = ref(100);
const fee = ref(0);
const dailyLimit = ref(1);
const todayApplied = ref(false);
const channels = ref([]);
const selectedChannel = ref("");
const configLoading = ref(false);
const configError = ref("");
const confirmVisible = ref(false);
const submitting = ref(false);
const successVisible = ref(false);
const successResult = ref({});
const idempotencyKey = ref("");
const amount = computed(() => Number(String(amountInput.value).replace(/[^\d]/g, "")) || 0);
const maxWithdrawPoints = computed(() => Math.floor(balance.value / multiple.value) * multiple.value);
const quickValues = computed(() => [minPoints.value, minPoints.value + multiple.value * 10, minPoints.value + multiple.value * 20].filter((v, i, a) => v <= maxWithdrawPoints.value && a.indexOf(v) === i));
const moneyValue = computed(() => formatMoney(amount.value / exchangeRate.value * cashRate.value - fee.value));
const balanceMoney = computed(() => formatMoney(balance.value / exchangeRate.value * cashRate.value - fee.value));
const canSubmit = computed(() => !configLoading.value && !configError.value && !todayApplied.value && !!selectedChannelItem.value?.available && amount.value >= minPoints.value && amount.value <= balance.value && amount.value % multiple.value === 0 && !submitting.value);
const selectedChannelItem = computed(() => channels.value.find((item) => item.code === selectedChannel.value) || null);

try {
  const info = typeof uni.getWindowInfo === "function" ? uni.getWindowInfo() : uni.getSystemInfoSync();
  statusBarHeight.value = Number(info.statusBarHeight || 0);
} catch (_) {}

onLoad(loadConfig);
onShow(loadConfig);

async function loadConfig() {
  if (configLoading.value) return;
  configLoading.value = true;
  configError.value = "";
  try {
    const data = await getWorkerPointsWithdrawConfig();
    balance.value = number(data?.balance);
    exchangeRate.value = number(data?.exchangeRate, 100);
    cashRate.value = number(data?.cashRate, 1);
    minPoints.value = number(data?.minPoints, 1000);
    multiple.value = number(data?.multiple, 100) || 1;
    fee.value = number(data?.fee);
    dailyLimit.value = number(data?.dailyLimit, 1);
    todayApplied.value = data?.todayApplied === true;
    channels.value = (Array.isArray(data?.channels) ? data.channels : []).map(normalizeChannel);
    if (!channels.value.some((item) => item.code === selectedChannel.value && item.available)) selectedChannel.value = channels.value.find((item) => item.available)?.code || "";
  } catch (error) {
    configError.value = error?.message || "提现配置加载失败";
  } finally {
    configLoading.value = false;
  }
}

function normalizeChannel(channel) {
  return { ...channel, icon: channel.code === "ALIPAY" ? "/static/icons/worker-points-withdraw/alipay-white.svg" : "/static/icons/worker-points-withdraw/weixin-white.svg" };
}

function number(value, fallback = 0) {
  const result = Number(value);
  return Number.isFinite(result) ? result : fallback;
}

function formatPoints(value) {
  return Number(value || 0).toLocaleString("zh-CN");
}
function formatMoney(value) { return number(value).toFixed(2); }
function formatDate(value) { return value ? String(value).replace("T", " ").slice(0, 16) : "待定"; }
function channelName(code) { return channels.value.find((item) => item.code === code)?.name || code || ""; }
function onAmountInput(event) {
  amountInput.value = String(event.detail?.value || "").replace(/[^\d]/g, "");
  idempotencyKey.value = "";
}
function pickAmount(value) {
  amountInput.value = String(Math.min(value, maxWithdrawPoints.value));
  idempotencyKey.value = "";
}
function useAll() {
  amountInput.value = String(maxWithdrawPoints.value);
  idempotencyKey.value = "";
}
function selectChannel(channel) {
  if (!channel.available) return;
  selectedChannel.value = channel.code;
  idempotencyKey.value = "";
}
function openRecords() {
  uni.navigateTo({ url: "/pages/worker/points-withdraw-records" });
}
function goBack() {
  const pages = getCurrentPages();
  if (pages.length > 1) uni.navigateBack();
  else uni.redirectTo({ url: "/pages/worker/points" });
}
function openConfirm() {
  if (!selectedChannelItem.value?.available) return uni.showToast({ title: "请选择提现方式", icon: "none" });
  if (todayApplied.value) return uni.showToast({ title: `每天最多提交${dailyLimit.value}次积分提现`, icon: "none" });
  if (amount.value < minPoints.value) return uni.showToast({ title: `最低${formatPoints(minPoints.value)}积分起提`, icon: "none" });
  if (amount.value % multiple.value !== 0) return uni.showToast({ title: `提现积分必须是${formatPoints(multiple.value)}的整数倍`, icon: "none" });
  if (amount.value > balance.value) return uni.showToast({ title: "积分余额不足", icon: "none" });
  confirmVisible.value = true;
}
async function submitWithdraw() {
  if (submitting.value || !canSubmit.value) return;
  if (!idempotencyKey.value) idempotencyKey.value = `points-withdraw-${Date.now()}-${Math.random().toString(36).slice(2)}`;
  submitting.value = true;
  try {
    const result = await createWorkerPointsWithdrawal({ points: amount.value, channel: selectedChannel.value }, idempotencyKey.value);
    successResult.value = result || {};
    confirmVisible.value = false;
    successVisible.value = true;
    todayApplied.value = true;
    balance.value = Math.max(0, balance.value - amount.value);
    idempotencyKey.value = "";
  } catch (error) {
    uni.showToast({ title: error?.message || "提现申请失败", icon: "none" });
  } finally {
    submitting.value = false;
  }
}
function closeSheets() {
  confirmVisible.value = false;
  successVisible.value = false;
}
function finish() {
  successVisible.value = false;
  uni.navigateBack();
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f7f7f7; }
.page-state { margin: 24rpx; padding: 80rpx 24rpx; border-radius: 24rpx; color: #999; background: #fff; text-align: center; font-size: 26rpx; }
.page-state.error { color: #ff6b35; }
.status-spacer { background: #fff; }
.nav-bar { position: relative; display: flex; align-items: center; justify-content: space-between; height: 100rpx; padding: 0 32rpx; background: #fff; }
.nav-back { width: 64rpx; height: 64rpx; display: flex; align-items: center; justify-content: center; }
.nav-back image { width: 36rpx; height: 36rpx; }
.nav-title { position: absolute; left: 50%; color: #333; font-size: 34rpx; font-weight: 600; transform: translateX(-50%); }
.nav-link { color: #666; font-size: 26rpx; }
.page-scroll { height: calc(100vh - 200rpx); box-sizing: border-box; padding-bottom: 120rpx; }
.balance-card { margin: 24rpx; padding: 36rpx; border-radius: 32rpx; color: #fff; background: linear-gradient(135deg, #ff8c5a, #ff6b35); box-shadow: 0 12rpx 36rpx rgba(255, 107, 53, .25); }
.balance-top { display: flex; color: rgba(255,255,255,.92); font-size: 26rpx; }
.balance-value { margin-top: 12rpx; font-size: 68rpx; font-weight: 700; }.balance-value text { margin-left: 8rpx; font-size: 28rpx; font-weight: 500; }
.balance-sub { display: block; margin-top: 4rpx; color: rgba(255,255,255,.85); font-size: 24rpx; }.balance-rate { display: inline-flex; align-items: center; gap: 8rpx; margin-top: 24rpx; padding: 8rpx 20rpx; border-radius: 22rpx; color: rgba(255,255,255,.95); background: rgba(255,255,255,.18); font-size: 22rpx; }.balance-rate image { width: 22rpx; height: 22rpx; }
.form-card { margin: 0 24rpx 24rpx; padding: 32rpx; border-radius: 24rpx; background: #fff; }.field-label { display: block; color: #333; font-size: 28rpx; font-weight: 600; }
.records-entry { display: flex; align-items: center; justify-content: space-between; margin: 0 24rpx 24rpx; padding: 28rpx 32rpx; border-radius: 24rpx; background: #fff; }.records-entry-title,.records-entry-desc { display: block; }.records-entry-title { color: #333; font-size: 28rpx; font-weight: 600; }.records-entry-desc { margin-top: 8rpx; color: #999; font-size: 23rpx; }.records-entry-arrow { color: #999; font-size: 44rpx; font-weight: 300; }
.channel-item { display: flex; align-items: center; gap: 24rpx; padding: 26rpx 0; border-bottom: 1rpx solid #f5f5f5; }.channel-item:last-child { border-bottom: 0; }.channel-icon { display: flex; align-items: center; justify-content: center; width: 80rpx; height: 80rpx; border-radius: 20rpx; }.channel-icon image { width: 44rpx; height: 44rpx; }.channel-icon.wechat { background: #07c160; }.channel-icon.alipay { background: #1677ff; }.channel-info { flex: 1; }.channel-name,.channel-desc { display: block; }.channel-name { color: #333; font-size: 28rpx; font-weight: 500; }.channel-desc { margin-top: 6rpx; color: #999; font-size: 23rpx; }.radio-dot { width: 40rpx; height: 40rpx; border: 4rpx solid #ddd; border-radius: 50%; box-sizing: border-box; }.channel-item.active .radio-dot { border-color: #ff6b35; box-shadow: inset 0 0 0 8rpx #fff; background: #ff6b35; }
.channel-item.unavailable { opacity: .55; }
.amount-box { margin-top: 24rpx; padding: 24rpx; border: 2rpx solid #f0e3da; border-radius: 24rpx; }.amount-box.focused { border-color: #ff6b35; }.amount-row { display: flex; align-items: center; }.amount-input { flex: 1; min-width: 0; font-size: 52rpx; font-weight: 700; }.amount-unit { margin-left: 10rpx; color: #999; font-size: 26rpx; }.amount-all { margin-left: 18rpx; color: #ff6b35; font-size: 26rpx; font-weight: 600; }.amount-meta { display: flex; justify-content: space-between; margin-top: 14rpx; color: #999; font-size: 22rpx; }.quick-row { display: flex; gap: 12rpx; margin-top: 20rpx; }.quick-chip { flex: 1; padding: 14rpx 0; border-radius: 12rpx; color: #666; background: #f7f7f7; font-size: 24rpx; text-align: center; }.quick-chip.active { color: #ff6b35; background: #fff3ed; font-weight: 600; }
.calc-block { margin-top: 28rpx; }.calc-row { display: flex; justify-content: space-between; padding: 16rpx 0; border-top: 1rpx dashed #f0f0f0; color: #666; font-size: 25rpx; }.calc-row.highlight { padding-top: 8rpx; border-top: 0; color: #333; font-size: 28rpx; }.money { color: #ff6b35; font-size: 44rpx; font-weight: 700; }.free { color: #10b981 !important; }.fee-label { display: flex; align-items: center; gap: 8rpx; }.fee-label image { width: 24rpx; height: 24rpx; }
.rule-card { margin: 0 24rpx 24rpx; padding: 28rpx 32rpx; border-radius: 24rpx; background: #fffaf7; }.rule-title { display: flex; align-items: center; gap: 10rpx; margin-bottom: 12rpx; color: #ff6b35; font-size: 26rpx; font-weight: 600; }.rule-title image { width: 28rpx; height: 28rpx; }.rule-item { display: flex; gap: 10rpx; color: #999; font-size: 23rpx; line-height: 1.9; }.rule-item image { width: 10rpx; height: 10rpx; margin-top: 16rpx; flex-shrink: 0; }.bottom-space { height: 48rpx; }
.submit-btn { width: 100%; height: 92rpx; margin: 0; border: 0; border-radius: 46rpx; color: #fff; background: linear-gradient(135deg, #ff6b35, #ff8c5a); font-size: 30rpx; font-weight: 700; }.submit-btn.disabled { color: #fff; background: #ddd; }.submit-btn::after,.line-btn::after,.main-btn::after { border: 0; }
.modal-mask { position: fixed; inset: 0; z-index: 10; background: rgba(0,0,0,.45); }.confirm-sheet,.success-sheet { position: fixed; right: 0; bottom: 0; left: 0; z-index: 11; padding: 20rpx 40rpx calc(36rpx + env(safe-area-inset-bottom)); border-radius: 32rpx 32rpx 0 0; background: #fff; }.sheet-handle { width: 72rpx; height: 8rpx; margin: 0 auto 18rpx; border-radius: 8rpx; background: #e0e0e0; }.sheet-title { display: block; color: #333; font-size: 32rpx; font-weight: 700; text-align: center; }.confirm-money { padding: 28rpx 0 18rpx; color: #ff6b35; font-size: 64rpx; font-weight: 700; text-align: center; }.confirm-money > text:last-child { display: block; margin-top: 6rpx; color: #999; font-size: 24rpx; font-weight: 400; }.currency { font-size: 32rpx; }
.confirm-row { display: flex; justify-content: space-between; padding: 20rpx 0; border-bottom: 1rpx solid #f7f7f7; color: #666; font-size: 26rpx; }.confirm-row text:last-child { max-width: 65%; color: #333; text-align: right; }.sheet-actions { display: flex; gap: 20rpx; margin-top: 28rpx; }.line-btn,.main-btn { flex: 1; height: 88rpx; line-height: 88rpx; margin: 0; border-radius: 44rpx; font-size: 28rpx; font-weight: 600; }.line-btn { border: 2rpx solid #ddd; color: #666; background: #fff; }.main-btn { border: 0; color: #fff; background: linear-gradient(135deg, #ff6b35, #ff8c5a); }
.success-wrap { text-align: center; padding: 26rpx 24rpx 30rpx; }.success-circle { width: 144rpx; height: 144rpx; margin: 0 auto 28rpx; border-radius: 50%; background: #ecfdf5; display: flex; align-items: center; justify-content: center; }.success-circle image { width: 72rpx; height: 72rpx; }.success-title { display: block; color: #333; font-size: 36rpx; font-weight: 700; }.success-desc { display: block; margin-top: 16rpx; color: #999; font-size: 26rpx; line-height: 1.8; }.success-orange { color: #ff6b35 !important; }.success-card { margin-top: 28rpx; padding: 16rpx 28rpx; border-radius: 20rpx; background: #f9f9f9; text-align: left; }.success-card .confirm-row { padding: 14rpx 0; border-bottom: 0; font-size: 25rpx; }
</style>
