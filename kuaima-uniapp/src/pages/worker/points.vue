<template>
  <view class="page">
    <view class="status-spacer" :style="{ height: `${statusBarHeight}px` }" />
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <image src="/static/icons/boss-points/arrow-left-dark.svg" mode="aspectFit" />
      </view>
      <text class="nav-title">我的积分</text>
      <view class="nav-placeholder" />
    </view>

    <scroll-view scroll-y class="page-scroll">
      <view class="balance-card">
        <text class="balance-label">当前积分</text>
        <text class="balance-value">{{ formatPoints(balance) }}</text>
        <text class="balance-sub">约可提现 ¥{{ cashValue }} · 100积分=1元</text>
        <view class="balance-tip">
          <image src="/static/icons/boss-points/circle-info-orange.svg" mode="aspectFit" />
          <text>积分可提现到微信/支付宝，也可兑换好礼</text>
        </view>
        <button class="withdraw-btn" @click="openWithdraw">积分提现</button>
      </view>

      <view class="section-card">
        <view class="section-title">
          <text class="section-star">★</text>
          <text>如何获得积分</text>
        </view>
        <view v-for="item in earnWays" :key="item.name" class="earn-item">
          <view class="earn-icon" :style="{ background: item.bg }">
            <image :src="item.icon" mode="aspectFit" />
          </view>
          <view class="earn-info">
            <text class="earn-name">{{ item.name }}</text>
            <text class="earn-desc">{{ item.desc }}</text>
          </view>
          <text class="earn-value">{{ item.value }}</text>
        </view>
      </view>

      <view class="detail-tabs">
        <view v-for="tab in tabs" :key="tab.value" class="detail-tab" :class="{ active: activeTab === tab.value }" @click="activeTab = tab.value">
          {{ tab.label }}
        </view>
      </view>
      <view class="detail-list">
        <view v-for="record in visibleRecords" :key="record.id" class="detail-item">
          <view class="detail-icon" :style="{ background: record.iconBg }">
            <image :src="record.icon" mode="aspectFit" />
          </view>
          <view class="detail-info">
            <text class="detail-name">{{ record.title }}</text>
            <text class="detail-time">{{ record.time }}<text v-if="record.remark"> · {{ record.remark }}</text></text>
          </view>
          <view class="detail-right">
            <text class="detail-amount" :class="record.amount >= 0 ? 'income' : 'expense'">{{ record.amount > 0 ? '+' : '' }}{{ formatPoints(record.amount) }}</text>
            <text v-if="record.pending" class="pending-tag">处理中</text>
          </view>
        </view>
        <view v-if="loading" class="empty-state">积分明细加载中...</view>
        <view v-else-if="!visibleRecords.length" class="empty-state">
          <text class="empty-icon">⌁</text>
          <text>暂无记录</text>
        </view>
      </view>
      <view class="bottom-space" />
    </scroll-view>
  </view>
</template>

<script>
import { getPoints, listPointFlows } from "@/api/backend";

const flowIcons = {
  order: "/static/icons/worker-profile/clipboard-gray.svg",
  sign: "/static/icons/boss-profile/calendar-check-solid-gray.svg",
  invite: "/static/icons/boss-message/user-plus-green.svg",
  gift: "/static/icons/boss-reward/gift-orange.svg",
  wallet: "/static/icons/boss-home/square-plus-orange.svg",
};

function formatDateTime(value) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value || "");
  const pad = (number) => String(number).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

function normalizeFlow(item, index) {
  const type = String(item.bizType || item.type || item.flowType || "").toUpperCase();
  const amount = Number(item.delta ?? item.amount ?? item.points ?? item.change ?? 0);
  const isWithdraw = type.includes("WITHDRAW") || /提现/.test(String(item.remark || ""));
  const isInvite = type.includes("INVITE") || /邀请/.test(String(item.remark || ""));
  const isSign = type.includes("SIGN") || /签到/.test(String(item.remark || ""));
  const isGift = type.includes("GIFT") || type.includes("RECEIVED") || /赠送|奖励/.test(String(item.remark || ""));
  const isOrder = type.includes("ORDER") || type.includes("FINISH") || /订单|完工/.test(String(item.remark || ""));
  return {
    id: item.id || `${item.timestamp || item.createdAt || "flow"}-${index}`,
    amount: Number.isFinite(amount) ? amount : 0,
    title: item.title || (isWithdraw ? "积分提现" : isInvite ? "邀请好友奖励" : isSign ? "每日签到" : isGift ? "积分奖励" : isOrder ? "完成日结订单" : amount >= 0 ? "积分收入" : "积分支出"),
    remark: item.subtitle || item.description || item.remark || item.bizNo || "",
    time: formatDateTime(item.timestamp || item.createdAt || item.createTime || item.time),
    pending: String(item.status || "").toUpperCase() === "PENDING" || /处理中/.test(String(item.remark || "")),
    icon: isWithdraw ? flowIcons.wallet : isInvite ? flowIcons.invite : isSign ? flowIcons.sign : isGift ? flowIcons.gift : flowIcons.order,
    iconBg: isInvite ? "#ECFDF5" : isSign ? "#EFF6FF" : isGift ? "#FEF3C7" : "#FFF3ED",
  };
}

export default {
  data() {
    return {
      statusBarHeight: 0,
      balance: 0,
      loading: false,
      activeTab: "all",
      records: [],
      tabs: [
        { label: "全部", value: "all" },
        { label: "收入", value: "income" },
        { label: "支出", value: "expense" },
      ],
      earnWays: [
        { name: "完成日结订单", desc: "订单确认完成且无差评", value: "+50/单", icon: flowIcons.order, bg: "#FFF3ED" },
        { name: "每日签到", desc: "连续签到奖励更多", value: "+5/天", icon: flowIcons.sign, bg: "#EFF6FF" },
        { name: "邀请好友", desc: "好友完成首单后发放", value: "+200/人", icon: flowIcons.invite, bg: "#ECFDF5" },
      ],
    };
  },
  computed: {
    cashValue() {
      return (Number(this.balance || 0) / 100).toFixed(2);
    },
    visibleRecords() {
      if (this.activeTab === "income") return this.records.filter((item) => item.amount >= 0);
      if (this.activeTab === "expense") return this.records.filter((item) => item.amount < 0);
      return this.records;
    },
  },
  async onLoad() {
    try {
      const info = typeof uni.getWindowInfo === "function" ? uni.getWindowInfo() : uni.getSystemInfoSync();
      this.statusBarHeight = Number(info.statusBarHeight || 0);
    } catch (_) {}
    await this.loadPoints();
  },
  methods: {
    goBack() {
      uni.navigateBack();
    },
    formatPoints(value) {
      return Number(value || 0).toLocaleString("zh-CN");
    },
    openWithdraw() {
      uni.showToast({ title: "积分提现接口暂未开放", icon: "none" });
    },
    async loadPoints() {
      const userId = uni.getStorageSync("userId");
      if (!userId) {
        uni.showToast({ title: "请先登录", icon: "none" });
        return;
      }
      this.loading = true;
      try {
        const [account, flows] = await Promise.all([
          getPoints(userId),
          listPointFlows(userId, { page: 0, size: 100 }),
        ]);
        this.balance = Number(account?.balance ?? account?.points ?? 0);
        this.records = (Array.isArray(flows) ? flows : []).map(normalizeFlow);
      } catch (error) {
        uni.showToast({ title: error?.message || "积分信息加载失败", icon: "none" });
      } finally {
        this.loading = false;
      }
    },
  },
};
</script>

<style scoped>
.page { display:flex; flex-direction:column; width:100%; height:100vh; overflow:hidden; background:#f7f7f7; }
.status-spacer { flex-shrink:0; background:#fff; }
.nav-bar { position:relative; display:flex; align-items:center; justify-content:space-between; height:100rpx; padding:0 32rpx; background:#fff; }
.nav-back,.nav-placeholder { display:flex; align-items:center; justify-content:center; width:64rpx; height:64rpx; }.nav-back image { width:36rpx; height:36rpx; }
.nav-title { position:absolute; left:50%; color:#333; font-size:34rpx; font-weight:600; transform:translateX(-50%); }
.page-scroll { flex:1; min-height:0; box-sizing:border-box; }
.balance-card { margin:24rpx; padding:40rpx; border-radius:32rpx; background:linear-gradient(135deg,#ff8c5a,#ff6b35); color:#fff; box-shadow:0 12rpx 36rpx rgba(255,107,53,.25); }
.balance-label,.balance-value,.balance-sub { display:block; }.balance-label { font-size:26rpx; opacity:.9; }.balance-value { margin-top:12rpx; font-size:72rpx; font-weight:700; }.balance-sub { margin-top:4rpx; font-size:24rpx; opacity:.85; }
.balance-tip { display:flex; align-items:center; gap:12rpx; margin-top:24rpx; font-size:24rpx; opacity:.9; }.balance-tip image { width:28rpx; height:28rpx; filter:brightness(0) invert(1); }
.withdraw-btn { display:flex; align-items:center; justify-content:center; height:84rpx; margin:28rpx 0 0; padding:0; border:0; border-radius:42rpx; background:rgba(255,255,255,.96); color:#ff6b35; font-size:30rpx; font-weight:700; line-height:84rpx; }.withdraw-btn::after { border:0; }
.section-card { margin:0 24rpx 24rpx; padding:32rpx; border-radius:24rpx; background:#fff; }
.section-title { display:flex; align-items:center; gap:12rpx; margin-bottom:20rpx; color:#333; font-size:30rpx; font-weight:600; }.section-star { color:#ff6b35; }
.earn-item { display:flex; align-items:center; gap:24rpx; padding:20rpx 0; }.earn-icon { display:flex; align-items:center; justify-content:center; width:80rpx; height:80rpx; flex-shrink:0; border-radius:20rpx; }.earn-icon image { width:38rpx; height:38rpx; }.earn-info { flex:1; min-width:0; }.earn-name,.earn-desc { display:block; }.earn-name { color:#333; font-size:28rpx; font-weight:500; }.earn-desc { margin-top:4rpx; color:#999; font-size:24rpx; }.earn-value { color:#ff6b35; font-size:26rpx; font-weight:600; }
.detail-tabs { display:flex; margin:0 24rpx; padding:0 24rpx; border-radius:24rpx 24rpx 0 0; background:#fff; }.detail-tab { position:relative; flex:1; padding:26rpx 0; color:#666; font-size:28rpx; text-align:center; }.detail-tab.active { color:#ff6b35; font-weight:600; }.detail-tab.active::after { position:absolute; bottom:0; left:50%; width:48rpx; height:6rpx; border-radius:4rpx; background:#ff6b35; content:""; transform:translateX(-50%); }
.detail-list { min-height:240rpx; margin:0 24rpx 24rpx; padding:8rpx 32rpx 16rpx; border-radius:0 0 24rpx 24rpx; background:#fff; }
.detail-item { display:flex; align-items:center; gap:24rpx; padding:26rpx 0; border-bottom:1rpx solid #f5f5f5; }.detail-item:last-child { border-bottom:0; }.detail-icon { display:flex; align-items:center; justify-content:center; width:76rpx; height:76rpx; flex-shrink:0; border-radius:50%; }.detail-icon image { width:34rpx; height:34rpx; }.detail-info { flex:1; min-width:0; }.detail-name,.detail-time { display:block; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }.detail-name { color:#333; font-size:28rpx; font-weight:500; }.detail-time { margin-top:6rpx; color:#999; font-size:22rpx; }.detail-right { flex-shrink:0; text-align:right; }.detail-amount { display:block; font-size:30rpx; font-weight:700; }.detail-amount.income { color:#52c41a; }.detail-amount.expense { color:#333; }.pending-tag { display:block; margin-top:6rpx; color:#ff6b35; font-size:20rpx; }
.empty-state { display:flex; flex-direction:column; align-items:center; justify-content:center; padding:80rpx 40rpx; color:#999; font-size:26rpx; }.empty-icon { margin-bottom:20rpx; color:#ddd; font-size:80rpx; }.bottom-space { height:calc(30rpx + env(safe-area-inset-bottom)); }
</style>
