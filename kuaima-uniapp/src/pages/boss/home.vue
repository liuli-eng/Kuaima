<template>
  <view class="container">
    <scroll-view scroll-y class="scroll-area">
      <!-- 顶部导航 -->
      <view
        class="top-bar"
        :style="{
          paddingTop: `${statusBarHeight + 12}px`,
          paddingRight: `${menuSafeRight}px`,
        }"
      >
        <view class="brand-tag">快马日结</view>
      </view>

      <!-- Hero区域 -->
      <view class="hero-section">
        <text class="hero-title">招临时工</text>
        <text class="hero-subtitle">像叫网约车一样省心</text>
        <text class="hero-mascot" aria-hidden="true">🐴</text>
      </view>

      <!-- 信息条 -->
      <view class="info-bar">
        <view class="info-item">
          <text style="color: #ff6b35">📍</text>
          <text>松江</text>
        </view>
        <view class="info-right" @click="navigateTo('service-chat')">
          <text style="color: #ff6b35">🎧</text>
          <text>在线客服</text>
        </view>
      </view>
      <view class="info-highlight-bar">
        <text class="text-sm" style="color: #8b4513">
          附近有<text class="info-highlight">{{ nearbyWorkers }}位零工</text>
          最快<text class="info-highlight">{{ fastestMinutes }}分钟内</text>接单
        </text>
      </view>

      <!-- 每天日结区域 -->
      <view class="publish-section">
        <text class="section-title">每天日结</text>
        <text class="section-subtitle">每天完工 当面结清报酬</text>
        <view class="publish-entry" @click="navigateTo('select-job')">
          <view class="publish-entry-deco" />
          <text class="publish-entry-title">立即发布招工信息</text>
          <view class="publish-entry-btn">
            <text>发布招工</text>
          </view>
        </view>
      </view>

      <!-- 同行老板们区域 -->
      <view class="peer-section">
        <!-- 顶部横幅 -->
        <view class="peer-banner">
          <text class="peer-title">同行老板们</text>
          <text class="peer-title-sub">都在用快马日结招工</text>
        </view>

        <!-- 4个特性卡片 -->
        <view class="peer-features">
          <view class="peer-feature">
            <text class="peer-feature-icon">⚡</text>
            <view class="peer-feature-title">
              <text>⚡</text>
              <text>到岗快</text>
            </view>
            <text class="peer-feature-desc"
              >3000万临时工在线接单，最快3分钟接单，20分钟到岗</text
            >
          </view>
          <view class="peer-feature">
            <text class="peer-feature-icon">🔧</text>
            <view class="peer-feature-title">
              <text>🔧</text>
              <text>熟练工</text>
            </view>
            <text class="peer-feature-desc"
              >工厂、电商、餐饮、酒店、仓储、物流等都在平台招临时工熟手</text
            >
          </view>
          <view class="peer-feature">
            <text class="peer-feature-icon">👤</text>
            <view class="peer-feature-title">
              <text>👤</text>
              <text>人靠谱</text>
            </view>
            <text class="peer-feature-desc"
              >零工实名接单，信用分机制筛选，零工星级评定帮您招靠谱临时工</text
            >
          </view>
          <view class="peer-feature">
            <text class="peer-feature-icon">💰</text>
            <view class="peer-feature-title">
              <text>💰</text>
              <text>更省钱</text>
            </view>
            <text class="peer-feature-desc"
              >临时工成本比正式工低30%，熟练临时工效率成本更优</text
            >
          </view>
        </view>
      </view>

      <view class="scroll-bottom-space" />
    </scroll-view>

    <!-- 底部TabBar -->
    <view class="tab-bar">
      <view class="tab-item active" @click="switchTab('home')">
        <view class="tab-icon-wrap"><text class="tab-icon">⌂</text></view>
        <text class="tab-label">首页</text>
      </view>
      <view class="tab-item" @click="switchTab('order')">
        <view class="tab-icon-wrap"><text class="tab-icon">▣</text></view>
        <text class="tab-label">招工订单</text>
      </view>
      <view class="tab-item" @click="switchTab('message')">
        <view class="tab-icon-wrap"><text class="tab-icon">●</text></view>
        <text class="tab-label">消息</text>
      </view>
      <view class="tab-item" @click="switchTab('profile')">
        <view class="tab-icon-wrap"><text class="tab-icon">☺</text></view>
        <text class="tab-label">我的</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getBossStats } from "@/api/backend";

function getSafeArea() {
  try {
    const info =
      typeof uni.getWindowInfo === "function"
        ? uni.getWindowInfo()
        : uni.getSystemInfoSync();
    let menuSafeRight = 16;
    // #ifdef MP-WEIXIN
    const menu = uni.getMenuButtonBoundingClientRect();
    if (menu?.left)
      menuSafeRight = Math.max(16, info.windowWidth - menu.left + 12);
    // #endif
    return {
      statusBarHeight: Number(info.statusBarHeight || 0),
      menuSafeRight,
    };
  } catch (_) {
    return { statusBarHeight: 0, menuSafeRight: 16 };
  }
}

export default {
  data() {
    return {
      ...getSafeArea(),
      nearbyWorkers: 0,
      fastestMinutes: 0,
      statsLoading: false,
    };
  },
  onLoad() {
    this.loadStats();
  },
  methods: {
    async loadStats() {
      this.statsLoading = true;
      try {
        const result = await getBossStats(
          uni.getStorageSync("userId") || "2001",
        );
        this.nearbyWorkers = Number(
          result?.nearbyWorkers ??
            result?.workerCount ??
            result?.availableWorkers ??
            0,
        );
        this.fastestMinutes = Number(
          result?.fastestMinutes ??
            result?.estimatedMinutes ??
            result?.expectedMinutes ??
            0,
        );
      } catch (error) {
        uni.showToast({
          title: error.message || "首页统计加载失败",
          icon: "none",
        });
      } finally {
        this.statsLoading = false;
      }
    },
    navigateTo(pageName) {
      const bossPages = [
        "boss-employer",
        "boss-home",
        "boss-message",
        "boss-order",
        "boss-profile",
        "boss-publish",
        "search-worker",
        "select-job",
        "publish-info",
        "schedule-stats",
        "enterprise-cert",
        "enterprise-cert-form",
        "creditor-score",
        "talent-list",
        "expense-detail",
        "payment-detail",
        "recruit-manager",
        "recruit-address",
        "sub-account",
        "suspend-settle",
        "switch-account",
        "invite-code",
        "blacklist",
        "all-jobs",
        "boss-filter",
        "settlement",
        "contract",
        "system-notice",
        "missed-call",
        "signup-notice",
        "invite-friend",
        "service-chat",
        "insurance",
        "realname",
      ];
      let url = `/pages/boss/${pageName}`;
      if (!bossPages.includes(pageName)) {
        url = `/pages/${pageName}`;
      }
      uni.navigateTo({ url });
    },
    switchTab(tab) {
      const tabPages = {
        home: "/pages/boss/home",
        order: "/pages/boss/order",
        message: "/pages/boss/message",
        profile: "/pages/boss/profile",
      };
      const target = tabPages[tab];
      const currentRoute = getCurrentPages().slice(-1)[0]?.route;
      if (!target || `/${currentRoute}` === target) return;
      uni.redirectTo({
        url: target,
        fail: (error) => {
          console.error("Boss 主导航跳转失败", error);
          uni.reLaunch({
            url: target,
            fail: () =>
              uni.showToast({ title: "页面跳转失败，请重试", icon: "none" }),
          });
        },
      });
    },
  },
};
</script>

<style lang="scss" scoped>
.container {
  box-sizing: border-box;
  width: 100%;
  height: 100vh;
  min-height: 100vh;
  background: #fff8e6;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  color: #333;
  font-family:
    -apple-system, BlinkMacSystemFont, "PingFang SC", "Helvetica Neue", Arial,
    sans-serif;
}

.scroll-area {
  flex: 1;
  min-height: 0;
  background: #fff8e6;
}

.top-bar {
  box-sizing: border-box;
  min-height: 56px;
  display: flex;
  align-items: center;
  padding-left: 16px;
  padding-bottom: 12px;
}

.brand-tag {
  background: #fff;
  padding: 8px 16px;
  border-radius: 24px;
  font-weight: 700;
  font-size: 15px;
  color: #8b4513;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}

.hero-section {
  box-sizing: border-box;
  min-height: 142px;
  padding: 16px 16px 20px;
  position: relative;
}

.hero-title {
  display: block;
  font-size: 38px;
  font-weight: 800;
  line-height: 1.1;
  color: #8b4513;
}

.hero-subtitle {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: #d2691e;
  margin-top: 8px;
}

.hero-mascot {
  position: absolute;
  right: 10px;
  top: 30px;
  font-size: 108px;
  line-height: 1;
  opacity: 0.6;
  pointer-events: none;
}

.info-bar {
  display: flex;
  padding: 16px;
  justify-content: space-between;
  font-size: 14px;
  color: #8b4513;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #8b4513;
}

.info-right {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #8b4513;
}

.info-highlight-bar {
  padding: 0 16px;
  margin-top: -8px;
  margin-bottom: 8px;
}

.info-highlight {
  color: #ff6b35;
  font-weight: 600;
}

.publish-section {
  padding: 0 16px;
}

.section-title {
  display: block;
  font-size: 28px;
  font-weight: 800;
  color: #8b4513;
  margin-bottom: 6px;
  letter-spacing: 1px;
}

.section-subtitle {
  display: block;
  font-size: 15px;
  font-weight: 500;
  color: #ff6b35;
  margin-bottom: 16px;
  letter-spacing: 0.5px;
}

.publish-entry {
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 18px;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  border-radius: 20px;
  padding: 26px 24px;
  color: #fff;
  box-shadow: 0 8px 20px rgba(255, 107, 53, 0.28);
}

.publish-entry:active {
  transform: scale(0.98);
}

.publish-entry-deco {
  position: absolute;
  top: -40px;
  right: -34px;
  width: 130px;
  height: 130px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
}

.publish-entry-title {
  position: relative;
  z-index: 1;
  display: block;
  color: #fff;
  text-align: center;
  font-size: 24px;
  font-weight: 800;
  line-height: 1.2;
  letter-spacing: 2px;
}

.publish-entry-btn {
  position: relative;
  z-index: 1;
  padding: 15px;
  border-radius: 30px;
  background: #fff;
  color: #ff6b35;
  text-align: center;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 2px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.peer-section {
  padding: 20px 16px;
}

.peer-banner {
  box-sizing: border-box;
  background: linear-gradient(135deg, #ffe4c4 0%, #ffdab9 100%);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 16px;
}

.peer-title {
  display: block;
  font-size: 22px;
  font-weight: 700;
  color: #8b4513;
  margin-bottom: 4px;
}

.peer-title-sub {
  display: block;
  font-size: 22px;
  font-weight: 700;
  color: #8b4513;
}

.peer-features {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1px;
  background: #f0f0f0;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 16px;
}

.peer-feature {
  box-sizing: border-box;
  min-width: 0;
  background: #fff;
  padding: 16px;
}

.peer-feature-icon {
  display: block;
  font-size: 20px;
  margin-bottom: 8px;
}

.peer-feature-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.peer-feature-desc {
  display: block;
  font-size: 12px;
  color: #666;
  line-height: 1.6;
}

.scroll-bottom-space {
  height: 16px;
}

.tab-bar {
  box-sizing: content-box;
  flex-shrink: 0;
  height: 63px;
  background: rgba(255, 255, 255, 0.98);
  border-top: 0.5px solid rgba(0, 0, 0, 0.05);
  display: flex;
  padding-bottom: env(safe-area-inset-bottom);
  z-index: 50;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.tab-icon-wrap {
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  margin-bottom: 3px;
}

.tab-icon {
  font-size: 20px;
  line-height: 1;
}

.tab-label {
  font-size: 10px;
  color: #999;
  font-weight: 500;
}

.tab-item.active .tab-label {
  color: #ff6b35;
}

.tab-item.active .tab-icon-wrap {
  width: 32px;
  height: 32px;
  margin-bottom: 2px;
  border-radius: 50%;
  color: #fff;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  box-shadow: 0 4px 10px rgba(255, 107, 53, 0.3);
}

.text-sm {
  font-size: 14px;
}
</style>
