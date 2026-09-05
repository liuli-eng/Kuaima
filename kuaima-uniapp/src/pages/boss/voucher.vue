<template>
  <view class="container">
    <!-- 状态栏 -->
    <view class="status-bar">
      <text>19:53</text>
      <view class="status-icons">
        <text>📶</text>
        <text>📡</text>
        <text>🔋</text>
      </view>
    </view>

    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text>←</text>
      </view>
      <text class="nav-title">券包</text>
      <view class="nav-right">
        <view class="nav-btn">
          <text style="font-size: 10px; color: #555">⋯</text>
        </view>
        <view class="nav-divider"></view>
        <view class="nav-btn">
          <text style="font-size: 10px; color: #555">●</text>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="content">
      <!-- 汇总卡片 -->
      <view class="summary-card">
        <view class="summary-item">
          <text class="summary-value">{{ summary.available }}</text>
          <text class="summary-label">可用券</text>
        </view>
        <view class="summary-divider"></view>
        <view class="summary-item">
          <text class="summary-value">{{ summary.used }}</text>
          <text class="summary-label">已使用</text>
        </view>
        <view class="summary-divider"></view>
        <view class="summary-item">
          <text class="summary-value">{{ summary.expired }}</text>
          <text class="summary-label">已过期</text>
        </view>
      </view>

      <!-- Tab切换 -->
      <view class="tab-switch">
        <text
          class="tab-switch-item"
          :class="{ active: currentTab === 'available' }"
          @click="currentTab = 'available'"
          >待使用</text
        >
        <text
          class="tab-switch-item"
          :class="{ active: currentTab === 'used' }"
          @click="currentTab = 'used'"
          >已使用</text
        >
        <text
          class="tab-switch-item"
          :class="{ active: currentTab === 'expired' }"
          @click="currentTab = 'expired'"
          >已过期</text
        >
      </view>

      <!-- 优惠券列表 -->
      <view class="coupon-list">
        <view
          class="coupon-item"
          v-for="coupon in filteredCoupons"
          :key="coupon.id"
          :class="{ disabled: coupon.disabled }"
        >
          <view class="coupon-left">
            <view class="coupon-amount">
              <text style="font-size: 14px">¥</text>{{ coupon.amount }}
            </view>
            <text class="coupon-condition">{{
              coupon.condition || `满${coupon.minAmount || 0}元可用`
            }}</text>
            <text class="coupon-name">{{
              coupon.name || coupon.title || "优惠券"
            }}</text>
            <text class="coupon-expire">{{ coupon.expire }}</text>
          </view>
          <button
            class="coupon-use"
            :disabled="coupon.disabled"
            @click="useCoupon(coupon)"
          >
            {{ coupon.disabled ? "已过期" : "去使用" }}
          </button>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { listCoupons } from "@/api/backend";
export default {
  data() {
    return {
      currentTab: "available",
      summary: { available: 0, used: 0, expired: 0 },
      coupons: [],
    };
  },
  computed: {
    filteredCoupons() {
      if (this.currentTab === "available")
        return this.coupons.filter((c) => !c.disabled);
      if (this.currentTab === "expired")
        return this.coupons.filter((c) => c.disabled);
      return [];
    },
  },
  onLoad() {
    this.loadCoupons();
  },
  methods: {
    async loadCoupons() {
      try {
        const result = await listCoupons(
          uni.getStorageSync("userId") || "2001",
        );
        const rows = Array.isArray(result)
          ? result
          : result?.records || result?.content || [];
        this.coupons = rows.map((item) => ({
          ...item,
          amount: item.amount ?? item.value ?? "0.00",
          expire:
            item.expire || `有效期至 ${item.endTime || item.expireTime || "-"}`,
          disabled:
            item.disabled ??
            ["USED", "EXPIRED", "已使用", "已过期"].includes(item.status),
        }));
        this.summary = {
          available: this.coupons.filter((c) => !c.disabled).length,
          used: this.coupons.filter((c) =>
            ["USED", "已使用"].includes(c.status),
          ).length,
          expired: this.coupons.filter(
            (c) => c.disabled && !["USED", "已使用"].includes(c.status),
          ).length,
        };
      } catch (error) {
        uni.showToast({
          title: error.message || "优惠券加载失败",
          icon: "none",
        });
      }
    },
    goBack() {
      uni.navigateBack();
    },
    useCoupon(coupon) {
      uni.showToast({ title: "跳转发布订单页面使用优惠券", icon: "none" });
    },
  },
};
</script>

<style lang="scss" scoped>
.container {
  width: 100%;
  height: 100vh;
  background: #fff8e6;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.status-bar {
  height: 47px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 28px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  background: transparent;
}

.status-icons {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-bar {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #fff8e6;
}

.nav-back {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
}

.nav-right {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 9999px;
  padding: 3px 6px;
  gap: 2px;
}

.nav-btn {
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-divider {
  width: 1px;
  height: 10px;
  background: #ddd;
}

.content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.summary-card {
  background: white;
  border-radius: 14px;
  padding: 20px;
  display: flex;
  justify-content: space-around;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.summary-item {
  text-align: center;
}

.summary-value {
  font-size: 22px;
  font-weight: 700;
  color: #ff6b35;
  display: block;
}

.summary-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  display: block;
}

.summary-divider {
  width: 1px;
  background: #f0f0f0;
}

.tab-switch {
  display: flex;
  background: white;
  border-radius: 10px;
  padding: 4px;
  margin-bottom: 16px;
}

.tab-switch-item {
  flex: 1;
  text-align: center;
  padding: 8px;
  font-size: 14px;
  color: #666;
  border-radius: 8px;
}

.tab-switch-item.active {
  background: #ff6b35;
  color: white;
  font-weight: 600;
}

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.coupon-item {
  background: white;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: relative;
  overflow: hidden;
}

.coupon-item.disabled {
  opacity: 0.5;
}

.coupon-left {
  flex: 1;
  padding-right: 16px;
}

.coupon-amount {
  font-size: 24px;
  font-weight: 700;
  color: #ff6b35;
}

.coupon-condition {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  display: block;
}

.coupon-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-top: 6px;
  display: block;
}

.coupon-expire {
  font-size: 11px;
  color: #bbb;
  margin-top: 4px;
  display: block;
}

.coupon-use {
  width: 64px;
  height: 32px;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: white;
  border: none;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
  flex-shrink: 0;
}

.coupon-item.disabled .coupon-use {
  background: #ccc;
}
</style>
