<template>
  <view class="page">
    <view class="nav" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="nav-inner">
        <view class="nav-back" @click="goBack"><image :src="arrowLeftIcon" mode="aspectFit" /></view>
        <text class="nav-title">奖励金</text>
        <view class="nav-space" />
      </view>
    </view>

    <scroll-view scroll-y class="content">
      <view class="content-inner">
        <view class="balance-card">
          <text class="balance-label">我的奖励金</text>
          <text class="balance-value">¥{{ balance }}</text>
          <view class="balance-actions">
            <view class="balance-btn" @click="recharge"><image :src="plusIcon" mode="aspectFit" /><text>充值</text></view>
            <view class="balance-btn" @click="withdraw"><image :src="arrowDownIcon" mode="aspectFit" /><text>提现</text></view>
          </view>
        </view>

        <view class="tab-switch">
          <view v-for="tab in tabs" :key="tab.key" :class="['tab-switch-item', { active: currentTab === tab.key }]" @click="currentTab = tab.key">{{ tab.label }}</view>
        </view>

        <view class="income-section">
          <text class="income-title">收支明细</text>
          <view v-if="filteredRecords.length" class="income-list">
            <view v-for="item in filteredRecords" :key="item.id" class="income-item">
              <view class="income-info">
                <view class="income-icon"><image :src="recordIcon(item.icon)" mode="aspectFit" /></view>
                <view><text class="income-name">{{ item.name }}</text><text class="income-date">{{ item.date }}</text></view>
              </view>
              <text :class="['income-amount', item.type]">{{ item.type === 'income' ? '+' : '-' }}¥{{ formatMoney(item.amount) }}</text>
            </view>
          </view>
          <view v-else class="empty-state"><text>暂无相关记录</text></view>
        </view>

        <view class="rules-card">
          <view class="rules-title"><image :src="infoIcon" mode="aspectFit" /><text>奖励金说明</text></view>
          <text class="rules-item">1. 奖励金可用于发布订单、购买积分等</text>
          <text class="rules-item">2. 1元奖励金 = 1元现金，可提现至微信</text>
          <text class="rules-item">3. 提现最低金额10元，T+1到账</text>
          <text class="rules-item">4. 奖励金有效期永久有效</text>
        </view>
        <view class="safe-space" />
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { getBossProfileAssets } from "@/api/backend";
import arrowLeftIcon from "/static/icons/boss-points/arrow-left-dark.svg";
import plusIcon from "/static/icons/boss-reward/plus-white.svg";
import arrowDownIcon from "/static/icons/boss-reward/arrow-down-white.svg";
import giftIcon from "/static/icons/boss-reward/gift-orange.svg";
import bullhornIcon from "/static/icons/worker-rule/bullhorn-orange.svg";
import trophyIcon from "/static/icons/boss-reward/trophy-orange.svg";
import shareIcon from "/static/icons/boss-reward/share-orange.svg";
import coinsIcon from "/static/icons/worker-home/coins-orange.svg";
import moneyIcon from "/static/icons/boss-reward/money-bill-wave-orange.svg";
import infoIcon from "/static/icons/boss-points/circle-info-orange.svg";

const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0;
const balance = ref("0.00");
const currentTab = ref("all");
const records = ref([]);
const tabs = [{ key: "all", label: "全部" }, { key: "income", label: "收入" }, { key: "expense", label: "支出" }];
const icons = { gift: giftIcon, bullhorn: bullhornIcon, trophy: trophyIcon, share: shareIcon, coins: coinsIcon, money: moneyIcon };
const filteredRecords = computed(() => currentTab.value === "all" ? records.value : records.value.filter(item => item.type === currentTab.value));

onShow(loadData);

async function loadData() {
  const userId = uni.getStorageSync("userId");
  if (!userId) return;
  try {
    const data = await getBossProfileAssets(userId);
    const cents = Number(data?.rewardAmount);
    balance.value = Number.isFinite(cents) ? (cents / 100).toFixed(2) : "0.00";
    const list = data?.rewardRecords || data?.rewardFlows || [];
    records.value = Array.isArray(list) ? list.map(normalizeRecord) : [];
  } catch (error) {
    uni.showToast({ title: error?.message || "奖励金加载失败", icon: "none" });
  }
}
function normalizeRecord(item, index) {
  const rawType = String(item.type || item.direction || "").toLowerCase();
  return {
    id: item.id || `${item.time || item.createTime || "reward"}-${index}`,
    type: ["income", "plus", "in"].includes(rawType) ? "income" : "expense",
    name: item.name || item.title || item.remark || "奖励金变动",
    date: formatTime(item.date || item.time || item.createTime),
    amount: item.amount ?? item.value ?? 0,
    icon: item.icon || iconByName(item.name || item.title || item.remark),
  };
}
function iconByName(name = "") {
  if (name.includes("邀请")) return "gift";
  if (name.includes("发布")) return "bullhorn";
  if (name.includes("新人")) return "trophy";
  if (name.includes("分享")) return "share";
  if (name.includes("积分")) return "coins";
  return "money";
}
function recordIcon(name) { return icons[name] || moneyIcon; }
function formatMoney(value) {
  const amount = Number(value || 0);
  return (Math.abs(amount) >= 100 && Number.isInteger(amount) ? Math.abs(amount) / 100 : Math.abs(amount)).toFixed(2);
}
function formatTime(value) {
  if (!value) return "--";
  return String(value).replace("T", " ").slice(0, 16);
}
function goBack() { uni.navigateBack(); }
function recharge() { uni.showToast({ title: "奖励金充值接口暂未开放", icon: "none" }); }
function withdraw() { uni.showToast({ title: "奖励金提现接口暂未开放", icon: "none" }); }
</script>

<style scoped>
.page { display:flex; flex-direction:column; height:100vh; overflow:hidden; background:#f5f5f5; }
.nav { flex-shrink:0; background:#f5f5f5; }.nav-inner { position:relative; display:flex; align-items:center; justify-content:space-between; height:100rpx; padding:0 32rpx; }.nav-back,.nav-space { width:64rpx; height:64rpx; }.nav-back { display:flex; align-items:center; justify-content:center; }.nav-back image { width:36rpx; height:36rpx; }.nav-title { position:absolute; left:50%; transform:translateX(-50%); color:#333; font-size:34rpx; font-weight:600; }
.content { flex:1; min-height:0; height:0; }.content-inner { padding:32rpx; box-sizing:border-box; }
.balance-card { margin-bottom:32rpx; padding:48rpx; border-radius:32rpx; color:#fff; background:linear-gradient(135deg,#ff6b35,#ff8c5a); box-shadow:0 16rpx 48rpx rgba(255,107,53,.3); }
.balance-label,.balance-value { display:block; }.balance-label { font-size:26rpx; opacity:.9; }.balance-value { margin-top:16rpx; font-size:64rpx; font-weight:700; }
.balance-actions { display:flex; gap:24rpx; margin-top:32rpx; }.balance-btn { display:flex; align-items:center; justify-content:center; flex:1; gap:8rpx; padding:20rpx; border-radius:16rpx; background:rgba(255,255,255,.2); font-size:26rpx; }.balance-btn:active { background:rgba(255,255,255,.3); }.balance-btn image { width:26rpx; height:26rpx; }
.tab-switch { display:flex; margin-bottom:24rpx; padding:8rpx; border-radius:20rpx; background:#fff; }.tab-switch-item { flex:1; padding:16rpx; border-radius:16rpx; color:#666; font-size:28rpx; text-align:center; }.tab-switch-item.active { color:#fff; font-weight:600; background:#ff6b35; }
.income-section { margin-bottom:32rpx; padding:32rpx; border-radius:24rpx; background:#fff; }.income-title { display:block; margin-bottom:24rpx; color:#333; font-size:30rpx; font-weight:600; }.income-list { display:flex; flex-direction:column; gap:24rpx; }.income-item { display:flex; align-items:center; justify-content:space-between; padding:20rpx 0; border-bottom:1rpx solid #f5f5f5; }.income-item:last-child { border-bottom:0; }.income-info { display:flex; align-items:center; gap:20rpx; min-width:0; }.income-icon { display:flex; align-items:center; justify-content:center; width:72rpx; height:72rpx; flex-shrink:0; border-radius:16rpx; background:#fff3e0; }.income-icon image { width:32rpx; height:32rpx; }.income-name,.income-date { display:block; }.income-name { color:#333; font-size:28rpx; font-weight:500; }.income-date { margin-top:4rpx; color:#999; font-size:24rpx; }.income-amount { flex-shrink:0; margin-left:16rpx; font-size:32rpx; font-weight:600; }.income-amount.income { color:#ff6b35; }.income-amount.expense { color:#999; }
.empty-state { padding:60rpx 0 48rpx; color:#bbb; font-size:26rpx; text-align:center; }
.rules-card { padding:32rpx; border-radius:24rpx; background:#fff; }.rules-title { display:flex; align-items:center; gap:8rpx; margin-bottom:20rpx; color:#333; font-size:28rpx; font-weight:600; }.rules-title image { width:28rpx; height:28rpx; }.rules-item { display:block; color:#666; font-size:24rpx; line-height:1.8; }
.safe-space { height:calc(32rpx + env(safe-area-inset-bottom)); }
</style>
