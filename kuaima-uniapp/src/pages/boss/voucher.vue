<template>
  <view class="container">
    <view :style="{ height: `${statusBarHeight}px` }" />
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <image class="nav-back-icon" :src="arrowLeftIcon" mode="aspectFit" />
      </view>
      <text class="nav-title">券包</text>
      <view class="nav-placeholder" />
    </view>

    <scroll-view
      scroll-y
      class="content"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="refreshCoupons"
    >
      <view class="summary-card">
        <view class="summary-item">
          <text class="summary-value">{{ summary.available }}</text>
          <text class="summary-label">可用券</text>
        </view>
        <view class="summary-divider" />
        <view class="summary-item">
          <text class="summary-value">{{ summary.used }}</text>
          <text class="summary-label">已使用</text>
        </view>
        <view class="summary-divider" />
        <view class="summary-item">
          <text class="summary-value">{{ summary.expired }}</text>
          <text class="summary-label">已过期</text>
        </view>
      </view>

      <view class="tab-switch">
        <text
          v-for="tab in tabs"
          :key="tab.value"
          class="tab-switch-item"
          :class="{ active: currentTab === tab.value }"
          @click="currentTab = tab.value"
        >
          {{ tab.label }}
        </text>
      </view>

      <view v-if="loading" class="page-state">券包加载中...</view>
      <view v-else-if="loadError" class="page-state error" @click="loadCoupons">
        {{ loadError }}，点击重试
      </view>
      <view v-else-if="!filteredCoupons.length" class="empty-state">
        <text class="empty-text">{{ emptyText }}</text>
      </view>

      <view v-else class="coupon-list">
        <view
          v-for="coupon in filteredCoupons"
          :key="coupon.id"
          class="coupon-item"
          :class="{ disabled: coupon.status !== 'available' }"
        >
          <view class="coupon-left">
            <view class="coupon-amount">
              <text class="currency">¥</text>
              <text>{{ coupon.amountText }}</text>
            </view>
            <text class="coupon-condition">{{ coupon.condition }}</text>
            <text class="coupon-name">{{ coupon.name }}</text>
            <text class="coupon-expire">{{ coupon.expireText }}</text>
          </view>
          <button
            class="coupon-use"
            :disabled="coupon.status !== 'available'"
            @click="useCoupon(coupon)"
          >
            {{ coupon.actionText }}
          </button>
        </view>
      </view>
      <view class="content-bottom" />
    </scroll-view>
  </view>
</template>

<script>
import { getCurrentUser, listCoupons } from "@/api/backend";
import { handleTokenInvalid } from "@/api/auth";
import arrowLeftIcon from "/static/icons/boss-points/arrow-left-dark.svg";

function unwrapRows(result) {
  if (Array.isArray(result)) return result;
  return result?.records || result?.content || result?.data || [];
}

function normalizeStatus(value, expireAt) {
  const status = String(value || "UNUSED").toUpperCase();
  if (["USED", "已使用"].includes(status)) return "used";
  if (["EXPIRED", "已过期"].includes(status)) return "expired";
  if (expireAt) {
    const expiry = new Date(String(expireAt).replace(/-/g, "/"));
    if (!Number.isNaN(expiry.getTime()) && expiry.getTime() < Date.now()) {
      return "expired";
    }
  }
  return "available";
}

function formatDate(value) {
  return value ? String(value).slice(0, 10) : "-";
}

function normalizeCoupon(item = {}) {
  const detail = item.coupon || item.couponInfo || item.template || {};
  const expireAt = item.expireAt || item.expireTime || detail.validEnd || detail.endTime;
  const status = normalizeStatus(item.status, expireAt);
  const amount = detail.amount ?? item.amount ?? detail.value ?? item.value;
  const threshold = detail.threshold ?? detail.minSpend ?? item.threshold ?? item.minAmount;
  const couponId = item.couponId || detail.id || item.id;
  const userCouponId = item.userCouponId || item.id;
  const amountNumber = Number(amount);
  const thresholdNumber = Number(threshold);
  return {
    ...item,
    id: userCouponId || couponId,
    couponId,
    userCouponId,
    status,
    amount: Number.isFinite(amountNumber) ? amountNumber : null,
    amountText: Number.isFinite(amountNumber) ? amountNumber : "--",
    condition: Number.isFinite(thresholdNumber)
      ? `满${thresholdNumber}元可用`
      : detail.condition || item.condition || "使用条件以结算页为准",
    name:
      detail.name ||
      detail.title ||
      item.name ||
      item.title ||
      `优惠券${couponId ? ` #${couponId}` : ""}`,
    expireAt,
    expireText:
      status === "expired"
        ? `已过期 ${formatDate(expireAt)}`
        : status === "used"
          ? `已使用${item.usedAt ? ` ${formatDate(item.usedAt)}` : ""}`
          : expireAt
            ? `有效期至 ${formatDate(expireAt)}`
            : "长期有效",
    actionText: status === "used" ? "已使用" : status === "expired" ? "已过期" : "去使用",
  };
}

export default {
  data() {
    return {
      arrowLeftIcon,
      statusBarHeight: 0,
      currentTab: "available",
      tabs: [
        { label: "待使用", value: "available" },
        { label: "已使用", value: "used" },
        { label: "已过期", value: "expired" },
      ],
      coupons: [],
      loading: false,
      refreshing: false,
      loadError: "",
    };
  },
  computed: {
    summary() {
      return {
        available: this.coupons.filter((item) => item.status === "available").length,
        used: this.coupons.filter((item) => item.status === "used").length,
        expired: this.coupons.filter((item) => item.status === "expired").length,
      };
    },
    filteredCoupons() {
      return this.coupons.filter((item) => item.status === this.currentTab);
    },
    emptyText() {
      return {
        available: "暂无待使用优惠券",
        used: "暂无已使用优惠券",
        expired: "暂无已过期优惠券",
      }[this.currentTab];
    },
  },
  onLoad() {
    try {
      const info = typeof uni.getWindowInfo === "function"
        ? uni.getWindowInfo()
        : uni.getSystemInfoSync();
      this.statusBarHeight = Number(info.statusBarHeight || 0);
    } catch (_) {}
    this.loadCoupons();
  },
  methods: {
    async loadCoupons() {
      if (this.loading) return;
      this.loading = !this.refreshing;
      this.loadError = "";
      try {
        const currentUser = await getCurrentUser();
        const userId = currentUser?.id || currentUser?.userId;
        if (!userId) throw new Error("未获取到当前老板账号");
        const result = await listCoupons(userId);
        const rows = unwrapRows(result);
        this.coupons = Array.isArray(rows) ? rows.map(normalizeCoupon) : [];
      } catch (error) {
        this.coupons = [];
        if (error?.statusCode === 401) {
          await handleTokenInvalid({ role: "boss" });
          return;
        }
        this.loadError = error?.statusCode === 403
          ? "无权查看券包"
          : error?.message || "优惠券加载失败";
        uni.showToast({ title: this.loadError, icon: "none" });
      } finally {
        this.loading = false;
        this.refreshing = false;
      }
    },
    refreshCoupons() {
      if (this.loading || this.refreshing) return;
      this.refreshing = true;
      this.loadCoupons();
    },
    goBack() {
      uni.navigateBack();
    },
    useCoupon(coupon) {
      const selection = {
        couponId: coupon.couponId,
        userCouponId: coupon.userCouponId,
        name: coupon.name,
        amount: coupon.amount,
        condition: coupon.condition,
        expireAt: coupon.expireAt,
      };
      uni.setStorageSync("bossCouponSelection", selection);
      const query = [
        coupon.couponId ? `couponId=${encodeURIComponent(coupon.couponId)}` : "",
        coupon.userCouponId
          ? `userCouponId=${encodeURIComponent(coupon.userCouponId)}`
          : "",
      ].filter(Boolean).join("&");
      uni.navigateTo({
        url: `/pages/boss/publish-info${query ? `?${query}` : ""}`,
        fail: () => uni.showToast({ title: "发布页面打开失败", icon: "none" }),
      });
    },
  },
};
</script>

<style lang="scss" scoped>
.container { width: 100%; height: 100vh; display: flex; flex-direction: column; overflow: hidden; background: #f5f5f5; }
.nav-bar { position: relative; height: 50px; display: flex; align-items: center; padding: 0 16px; flex-shrink: 0; background: #f5f5f5; box-sizing: border-box; }
.nav-back, .nav-placeholder { width: 32px; height: 32px; }
.nav-back { display: flex; align-items: center; justify-content: center; }
.nav-back-icon { width: 18px; height: 18px; }
.nav-title { position: absolute; left: 50%; transform: translateX(-50%); color: #333; font-size: 17px; font-weight: 600; }
.nav-placeholder { margin-left: auto; }
.content { flex: 1; min-height: 0; padding: 16px; box-sizing: border-box; }
.summary-card { display: flex; justify-content: space-around; margin-bottom: 16px; padding: 20px; background: #fff; border-radius: 14px; box-shadow: 0 2px 8px rgba(0, 0, 0, .06); }
.summary-item { flex: 1; text-align: center; }
.summary-value, .summary-label { display: block; }
.summary-value { color: #ff6b35; font-size: 22px; font-weight: 700; }
.summary-label { margin-top: 4px; color: #999; font-size: 12px; }
.summary-divider { width: 1px; background: #f0f0f0; }
.tab-switch { display: flex; margin-bottom: 16px; padding: 4px; background: #fff; border-radius: 10px; }
.tab-switch-item { flex: 1; padding: 8px; color: #666; font-size: 14px; text-align: center; border-radius: 8px; }
.tab-switch-item.active { color: #fff; font-weight: 600; background: #ff6b35; }
.coupon-list { display: flex; flex-direction: column; gap: 12px; }
.coupon-item { position: relative; display: flex; align-items: center; min-height: 120px; padding: 16px; overflow: hidden; background: #fff; border-radius: 12px; box-shadow: 0 2px 8px rgba(0, 0, 0, .06); box-sizing: border-box; }
.coupon-item::before { position: absolute; top: 0; bottom: 0; left: 80px; width: 1px; content: ""; background-image: radial-gradient(circle, #ddd 3px, transparent 3px); background-size: 1px 8px; }
.coupon-item.disabled { opacity: .5; }
.coupon-left { flex: 1; min-width: 0; padding-right: 16px; }
.coupon-amount { display: flex; align-items: baseline; color: #ff6b35; font-size: 24px; font-weight: 700; }
.currency { margin-right: 2px; font-size: 14px; }
.coupon-condition, .coupon-name, .coupon-expire { display: block; }
.coupon-condition { margin-top: 4px; color: #999; font-size: 12px; }
.coupon-name { margin-top: 6px; overflow: hidden; color: #333; font-size: 14px; font-weight: 500; text-overflow: ellipsis; white-space: nowrap; }
.coupon-expire { margin-top: 4px; color: #bbb; font-size: 11px; }
.coupon-use { width: 64px; height: 32px; margin: 0; padding: 0; flex-shrink: 0; color: #fff; font-size: 13px; font-weight: 500; line-height: 32px; background: linear-gradient(135deg, #ff6b35, #ff8c5a); border: 0; border-radius: 16px; }
.coupon-use::after { border: 0; }
.coupon-use[disabled] { color: #fff; background: #ccc; }
.page-state, .empty-state { padding: 60px 0; text-align: center; }
.page-state { color: #999; font-size: 14px; }
.page-state.error { color: #ff6b35; }
.empty-text { color: #999; font-size: 14px; }
.content-bottom { height: calc(16px + env(safe-area-inset-bottom)); }
</style>
