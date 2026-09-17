<template>
  <view class="container">
    <view class="status-spacer" :style="{ height: `${statusBarHeight}px` }" />
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <image src="/static/icons/boss-points/arrow-left-dark.svg" mode="aspectFit" />
      </view>
      <text class="nav-title">积分明细</text>
      <view class="nav-placeholder" />
    </view>

    <scroll-view scroll-y class="content" @scrolltolower="loadMore">
      <view class="balance-card">
        <view>
          <text class="balance-label">当前可用积分</text>
          <text class="balance-value">{{ points }}</text>
          <text class="balance-sub">100 积分 = ¥1.00</text>
        </view>
        <view class="buy-link" @click="goPoints">＋ 购买积分</view>
      </view>

      <view class="tab-bar">
        <view class="tab-item" :class="{ active: activeTab === 'exchange' }" @click="switchTab('exchange')">
          <text>积分兑换</text><text class="tab-badge">{{ counts.exchange }}</text>
        </view>
        <view class="tab-item" :class="{ active: activeTab === 'purchase' }" @click="switchTab('purchase')">
          <text>积分购买</text><text class="tab-badge">{{ counts.purchase }}</text>
        </view>
      </view>

      <view v-if="groupedRecords.length" class="records-wrap">
        <view v-for="group in groupedRecords" :key="group.date" class="list-group">
          <text class="group-title">{{ group.date }}</text>
          <view v-for="record in group.records" :key="record.id" class="record-card">
            <view class="record-icon" :style="{ background: record.iconBg }">
              <image :src="record.icon" mode="aspectFit" />
            </view>
            <view class="record-main">
              <text class="record-title">{{ record.title }}</text>
              <text class="record-sub">{{ record.sub }}</text>
              <text class="record-time">◷ {{ record.time }}</text>
            </view>
            <view class="record-right">
              <text class="record-amount" :class="record.amount > 0 ? 'plus' : 'minus'">{{ record.amount > 0 ? '+' : '' }}{{ record.amount }}<text class="record-unit">积分</text></text>
              <text class="record-balance">余额 {{ record.balance }}</text>
            </view>
          </view>
        </view>
      </view>
      <view v-else class="empty-state">
        <text v-if="loading">明细加载中...</text>
        <template v-else>
          <text class="empty-icon">⌁</text>
          <text>暂无记录</text>
        </template>
      </view>
      <text v-if="records.length && loading" class="load-more">加载中...</text>
      <text v-else-if="records.length && !hasMore" class="load-more">没有更多记录了</text>
    </scroll-view>
  </view>
</template>

<script>
import { getBossPointsOverview, listBossPointRecords } from "@/api/backend";

const icons = {
  exchange: "/static/icons/boss-home/bullhorn.svg",
  purchase: "/static/icons/boss-home/square-plus-orange.svg",
  gift: "/static/icons/boss-reward/gift-orange.svg",
  expiry: "/static/icons/worker-job-detail/clock-orange.svg",
};

function normalizeRows(payload) {
  const body = payload?.data?.records ? payload.data : payload?.data ?? payload ?? {};
  const rows = Array.isArray(body) ? body : body.records || body.content || body.list || [];
  return Array.isArray(rows) ? rows : [];
}

function formatDateTime(value) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return { date: "—", time: String(value || "") };
  const pad = (n) => String(n).padStart(2, "0");
  return {
    date: `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`,
    time: `${pad(date.getHours())}:${pad(date.getMinutes())}`,
  };
}

function normalizeRecord(item, index) {
  const rawAmount = Number(item.amount ?? item.points ?? item.change ?? item.delta ?? 0);
  const amount = Number.isFinite(rawAmount) ? rawAmount : 0;
  const type = String(item.type || item.flowType || item.bizType || "").toLowerCase();
  const isPurchase = type.includes("purchase") || type.includes("buy") || type.includes("recharge") || type.includes("购买");
  const isGift = type.includes("gift") || type.includes("赠送");
  const isExpiry = type.includes("expire") || type.includes("过期");
  const when = formatDateTime(item.createdAt || item.createTime || item.time || item.occurredAt);
  return {
    id: item.id || `${when.date}-${when.time}-${index}`,
    title: item.title || item.description || (isPurchase ? `购买积分·${Math.abs(amount)}` : isGift ? "发放积分给零工" : isExpiry ? "积分过期清零" : amount >= 0 ? "积分到账" : "积分使用"),
    sub: item.subtitle || item.sub || item.remark || item.detail || item.orderTitle || (isPurchase ? "积分购买到账" : isGift ? "赠送积分给零工" : "积分账户变动"),
    date: when.date,
    time: when.time,
    amount,
    balance: item.balanceAfter ?? item.balance ?? item.afterBalance ?? item.remaining ?? "—",
    category: String(item.category || "").toLowerCase() || (isPurchase ? "purchase" : "exchange"),
    icon: isPurchase ? icons.purchase : isGift ? icons.gift : isExpiry ? icons.expiry : icons.exchange,
    iconBg: isPurchase ? "#E8F3FE" : isGift ? "#F3ECFE" : isExpiry ? "#FEF4E5" : "#FFF1E8",
  };
}

export default {
  data() {
    return {
      statusBarHeight: 0,
      points: 0,
      activeTab: "exchange",
      records: [],
      counts: { exchange: 0, purchase: 0 },
      page: 0,
      size: 20,
      total: 0,
      loading: false,
    };
  },
  computed: {
    hasMore() {
      return this.records.length < this.total;
    },
    groupedRecords() {
      const groups = {};
      this.records.forEach((record) => {
        if (!groups[record.date]) groups[record.date] = [];
        groups[record.date].push(record);
      });
      return Object.keys(groups).sort().reverse().map((date) => ({ date, records: groups[date] }));
    },
  },
  async onLoad() {
    try {
      const info = typeof uni.getWindowInfo === "function" ? uni.getWindowInfo() : uni.getSystemInfoSync();
      this.statusBarHeight = Number(info.statusBarHeight || 0);
    } catch (_) {}
    await Promise.all([this.loadOverview(), this.loadRecords(true)]);
  },
  methods: {
    goBack() { uni.navigateBack(); },
    goPoints() {
      uni.navigateBack({
        delta: 1,
        fail: () => uni.redirectTo({ url: "/pages/boss/points" }),
      });
    },
    switchTab(tab) {
      if (this.activeTab === tab || this.loading) return;
      this.activeTab = tab;
      this.loadRecords(true);
    },
    async loadOverview() {
      try {
        const result = await getBossPointsOverview();
        this.points = Number(result?.balance ?? result?.points ?? 0);
      } catch (_) {}
    },
    async loadRecords(reset = false) {
      if (this.loading) return;
      if (reset) {
        this.page = 0;
        this.total = 0;
        this.records = [];
      }
      this.loading = true;
      try {
        const result = await listBossPointRecords({
          page: this.page,
          size: this.size,
          category: this.activeTab.toUpperCase(),
        });
        const body = result?.data && !Array.isArray(result.data) ? result.data : {};
        const rows = normalizeRows(result).map(normalizeRecord);
        this.records = reset ? rows : [...this.records, ...rows];
        this.points = Number(body.balance ?? body.points ?? this.points);
        this.counts = {
          exchange: Number(body.exchangeCount ?? this.counts.exchange),
          purchase: Number(body.purchaseCount ?? this.counts.purchase),
        };
        this.total = Number(body.total ?? result?.total ?? rows.length);
        if (!body.exchangeCount && this.activeTab === "exchange") this.counts.exchange = this.total;
        if (!body.purchaseCount && this.activeTab === "purchase") this.counts.purchase = this.total;
      } catch (error) {
        uni.showToast({ title: error?.message || "积分明细加载失败", icon: "none" });
      } finally {
        this.loading = false;
      }
    },
    loadMore() {
      if (this.loading || !this.hasMore) return;
      this.page += 1;
      this.loadRecords();
    },
  },
};
</script>

<style lang="scss" scoped>
.container { width: 100%; height: 100vh; background: #f5f5f5; display: flex; flex-direction: column; overflow: hidden; }
.status-spacer { flex-shrink: 0; background: #f5f5f5; }
.nav-bar { position: relative; height: 50px; padding: 0 16px; display: flex; align-items: center; justify-content: space-between; background: #f5f5f5; }
.nav-back, .nav-placeholder { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; }
.nav-back image { width: 18px; height: 18px; }
.nav-title { position: absolute; left: 50%; color: #333; font-size: 17px; font-weight: 600; transform: translateX(-50%); }
.content { flex: 1; min-height: 0; box-sizing: border-box; padding: 0 0 20px; }
.balance-card { margin: 12px 16px; padding: 20px; border-radius: 16px; background: linear-gradient(135deg, #ffd700, #ff8c00); color: #fff; box-shadow: 0 6px 20px rgba(255, 140, 0, .28); display: flex; align-items: center; justify-content: space-between; }
.balance-label, .balance-sub { display: block; font-size: 12px; opacity: .9; }
.balance-value { display: block; margin-top: 6px; font-size: 32px; font-weight: 700; }
.buy-link { padding: 8px 14px; border-radius: 18px; background: rgba(255,255,255,.25); font-size: 13px; font-weight: 600; }
.tab-bar { display: flex; margin: 0 16px 10px; padding: 0 8px; border-radius: 14px; background: #fff; }
.tab-item { position: relative; flex: 1; display: flex; align-items: center; justify-content: center; gap: 6px; padding: 13px 0 11px; color: #888; font-size: 15px; }
.tab-item.active { color: #ff6b35; font-weight: 700; }
.tab-item.active::after { position: absolute; bottom: 0; left: 50%; width: 26px; height: 3px; border-radius: 2px; background: linear-gradient(90deg, #ff8c5a, #ff6b35); content: ""; transform: translateX(-50%); }
.tab-badge { min-width: 17px; height: 17px; padding: 0 5px; border-radius: 9px; background: #ffe0d2; color: #ff6b35; font-size: 10px; line-height: 17px; text-align: center; }
.list-group { margin: 0 16px 10px; padding: 2px 0 8px; border-radius: 14px; background: #fff; }
.group-title { display: block; padding: 10px 14px 5px; color: #aaa; font-size: 12px; }
.record-card { position: relative; display: flex; align-items: center; gap: 11px; padding: 12px 14px; }
.record-card + .record-card::before { position: absolute; top: 0; right: 14px; left: 65px; height: 1px; background: #f2f2f2; content: ""; }
.record-icon { display: flex; width: 40px; height: 40px; flex-shrink: 0; align-items: center; justify-content: center; border-radius: 50%; }
.record-icon image { width: 19px; height: 19px; }
.record-main { flex: 1; min-width: 0; }
.record-title, .record-sub, .record-time, .record-balance { display: block; overflow: hidden; white-space: nowrap; text-overflow: ellipsis; }
.record-title { color: #222; font-size: 14px; font-weight: 600; }
.record-sub { margin-top: 3px; color: #999; font-size: 12px; }
.record-time { margin-top: 5px; color: #bbb; font-size: 11px; }
.record-right { flex-shrink: 0; text-align: right; }
.record-amount { display: block; font-size: 18px; font-weight: 700; white-space: nowrap; }
.record-amount.plus { color: #059669; }
.record-amount.minus { color: #222; }
.record-unit { margin-left: 2px; color: #999; font-size: 11px; font-weight: 400; }
.record-balance { margin-top: 3px; color: #bbb; font-size: 11px; }
.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 70px 20px; color: #bbb; font-size: 13px; }
.empty-icon { margin-bottom: 12px; color: #ddd; font-size: 48px; }
.load-more { display: block; padding: 10px 0 18px; color: #bbb; font-size: 12px; text-align: center; }
</style>
