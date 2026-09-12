<template>
  <view class="container">
    <scroll-view scroll-y class="scroll-area">
      <view class="top-header-area">
        <view
          class="top-bar"
          :style="{
            paddingTop: `${statusBarHeight + 8}px`,
            paddingRight: `${menuSafeRight}px`,
          }"
        >
          <view class="brand-tag">快马日结</view>
          <!-- #ifndef MP-WEIXIN -->
          <view class="window-controls">
            <text class="window-btn">•••</text>
            <view class="window-divider" />
            <text class="window-btn">●</text>
          </view>
          <!-- #endif -->
        </view>
        <view class="hero-banner" @click="navigateTo('select-job')">
          <view class="hero-banner-icon">
            <image class="icon-svg" src="/static/icons/boss-home/trend.svg" mode="aspectFit" />
          </view>
          <text class="hero-banner-title"><text class="accent">旺季到</text> 工价涨</text>
          <text class="hero-banner-subtitle">两招教你快速招工！</text>
        </view>
      </view>

      <view class="employer-card">
        <view class="employer-card-deco" />
        <view class="employer-top">
          <view class="employer-location">
            <image class="inline-icon location-icon" src="/static/icons/boss-home/location.svg" mode="aspectFit" /><text>松江</text>
          </view>
          <view
            class="employer-service"
            @click="navigateTo('service-chat')"
          >
            <image class="inline-icon service-icon" src="/static/icons/boss-home/headset.svg" mode="aspectFit" /><text>在线客服</text>
          </view>
        </view>
        <view class="employer-main">
          <view class="employer-stat">
            <text class="stat-line"
              >附近有<text class="employer-stat-num">{{ nearbyWorkers }}</text
              ><text class="employer-stat-text">位零工</text></text
            >
            <text class="employer-stat-sub">最快{{ fastestMinutes }}分钟内接单</text>
          </view>
        </view>
        <view class="employer-cta-wrap" @click="navigateTo('select-job')">
          <view class="employer-cta-btn">
            {{ publishChecking ? "资格校验中" : "去发布招工" }}
          </view>
        </view>
      </view>

      <view class="peer-section">
        <view class="peer-features">
          <view
            v-for="item in peerFeatures"
            :key="item.title"
            class="peer-feature"
          >
            <view class="peer-feature-icon" :class="item.theme">
              <image v-if="item.icon === 'bolt'" class="feature-svg" src="/static/icons/boss-home/bolt.svg" mode="aspectFit" />
              <image v-else-if="item.icon === 'wrench'" class="feature-svg" src="/static/icons/boss-home/wrench.svg" mode="aspectFit" />
              <image v-else-if="item.icon === 'user-check'" class="feature-svg" src="/static/icons/boss-home/user-check.svg" mode="aspectFit" />
              <image v-else class="feature-svg" src="/static/icons/boss-home/coins.svg" mode="aspectFit" />
            </view>
            <text class="peer-feature-title">{{ item.title }}</text>
            <text class="peer-feature-desc">{{ item.description }}</text>
          </view>
        </view>
        <view class="bottom-slogan">
          <text class="slogan-title">快 马 日 结</text>
          <text class="slogan-desc">专业日结零工招工平台</text>
        </view>
      </view>

      <view class="scroll-bottom-space" />
    </scroll-view>

    <!-- 底部TabBar -->
    <view class="tab-bar">
      <view class="tab-item active" @click="switchTab('home')">
        <view class="tab-icon-wrap"><image class="tab-svg" src="/static/icons/boss-tabbar/house-white.svg" mode="aspectFit" /></view>
        <text class="tab-label">首页</text>
      </view>
      <view class="tab-item" @click="switchTab('order')">
        <view class="tab-icon-wrap"><image class="tab-svg" src="/static/icons/boss-tabbar/calendar-check-gray.svg" mode="aspectFit" /></view>
        <text class="tab-label">招工订单</text>
      </view>
      <view class="tab-item" @click="switchTab('workbench')">
        <view class="tab-icon-wrap"><image class="tab-svg" src="/static/icons/boss-tabbar/briefcase-gray.svg" mode="aspectFit" /></view>
        <text class="tab-label">工作台</text>
      </view>
      <view class="tab-item" @click="switchTab('message')">
        <view class="tab-icon-wrap"><image class="tab-svg" src="/static/icons/boss-tabbar/comment-dots-gray.svg" mode="aspectFit" /></view>
        <text class="tab-label">消息</text>
      </view>
      <view class="tab-item" @click="switchTab('profile')">
        <view class="tab-icon-wrap"><image class="tab-svg" src="/static/icons/boss-tabbar/face-smile-gray.svg" mode="aspectFit" /></view>
        <text class="tab-label">我的</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getBossStats } from "@/api/backend";
import { checkBossPublishEligibility } from "@/api/publish-eligibility";

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
      peerFeatures: [
        {
          icon: "bolt",
          theme: "orange",
          title: "到岗快",
          description: "3000万临时工在线接单，最快3分钟接单，20分钟到岗",
        },
        {
          icon: "wrench",
          theme: "green",
          title: "熟练工",
          description:
            "工厂、电商、餐饮、酒店、仓储、物流等都在平台招临时工熟手",
        },
        {
          icon: "user-check",
          theme: "blue",
          title: "人靠谱",
          description:
            "零工实名接单，信用分机制筛选，零工星级评定帮您招靠谱临时工",
        },
        {
          icon: "coins",
          theme: "yellow",
          title: "更省钱",
          description: "临时工成本比正式工低30%，熟练临时工效率成本更优",
        },
      ],
      nearbyWorkers: 2326,
      fastestMinutes: 3,
      statsLoading: false,
      publishChecking: false,
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
        const nearbyWorkers = Number(
          result?.nearbyWorkers ??
            result?.workerCount ??
            result?.availableWorkers,
        );
        const fastestMinutes = Number(
          result?.fastestMinutes ??
            result?.estimatedMinutes ??
            result?.expectedMinutes,
        );
        if (Number.isFinite(nearbyWorkers) && nearbyWorkers > 0) {
          this.nearbyWorkers = nearbyWorkers;
        }
        if (Number.isFinite(fastestMinutes) && fastestMinutes > 0) {
          this.fastestMinutes = fastestMinutes;
        }
      } catch (error) {
        console.warn("老板首页统计加载失败，使用默认展示数据", error);
      } finally {
        this.statsLoading = false;
      }
    },
    async navigateTo(pageName) {
      if (pageName === "select-job") {
        if (this.publishChecking) return;
        this.publishChecking = true;
        try {
          const eligibility = await checkBossPublishEligibility();
          if (!eligibility.canPublish) return;
        } finally {
          this.publishChecking = false;
        }
      }
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
      if (tab === "workbench")
        return uni.showToast({ title: "工作台页面暂未开放", icon: "none" });
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

.flow-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 16px;
  padding: 12px 16px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(139, 69, 19, 0.06);
}

.flow-item {
  color: #d2691e;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
}

.flow-arrow {
  color: #ffb380;
  font-size: 20px;
  line-height: 1;
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

.info-icon {
  color: #ff6b35;
}

.info-highlight-bar {
  padding: 0 16px;
  margin-top: -8px;
  margin-bottom: 8px;
}

.nearby-summary {
  color: #8b4513;
  font-size: 14px;
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

.publish-cards {
  display: flex;
  gap: 12px;
}

.publish-card {
  box-sizing: border-box;
  position: relative;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  padding: 16px;
  border-radius: 16px;
  color: #fff;
  text-align: center;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  box-shadow: 0 6px 16px rgba(255, 107, 53, 0.2);
}

.publish-card:active {
  opacity: 0.88;
  transform: scale(0.98);
}

.publish-card.green {
  background: linear-gradient(135deg, #52c41a, #73d13d);
  box-shadow: 0 6px 16px rgba(82, 196, 26, 0.2);
}

.card-days {
  display: block;
  margin: 12px 0 4px;
  font-size: 36px;
  font-weight: 800;
}

.card-label {
  display: block;
  font-size: 14px;
  opacity: 0.9;
}

.card-btn {
  margin-top: 12px;
  padding: 10px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.25);
  font-size: 14px;
  font-weight: 600;
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

.peer-banner-content {
  position: relative;
  z-index: 1;
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

/* 与老板端原型保持一致的首页视觉层 */
.container { background: #f7f7f7; color: #2c1810; }
.scroll-area { background: #f7f7f7; }
.top-header-area { padding: 8px 16px 14px; background: linear-gradient(180deg, #ffd8c4 0%, #ffe8d8 52%, #fff5ea 100%); }
.top-bar { min-height: 42px; padding: 0 0 10px 0; justify-content: space-between; }
.brand-tag { padding: 7px 15px; border-radius: 20px; background: linear-gradient(135deg,#ffe082,#ffd54f); color: #3e2723; font-size: 15px; font-weight: 800; letter-spacing: 1px; box-shadow: 0 2px 6px rgba(255,193,7,.25); }
.window-controls { display: flex; align-items: center; gap: 4px; padding: 3px 8px; border-radius: 999px; background: rgba(255,255,255,.75); box-shadow: 0 2px 6px rgba(0,0,0,.06); }
.window-btn { width: 24px; height: 24px; display: flex; align-items: center; justify-content: center; color: #5d4037; font-size: 12px; }
.window-divider { width: 1px; height: 12px; background: rgba(93,64,55,.2); }
.hero-banner { position: relative; min-height: 76px; padding: 4px 4px 8px; }
.hero-banner-title { display: block; position: relative; z-index: 1; color: #2c1810; font-size: 24px; font-weight: 800; line-height: 1.2; }
.hero-banner-title .accent { color: #ff5722; }
.hero-banner-subtitle { display: block; position: relative; z-index: 1; margin-top: 6px; color: #5d4037; font-size: 14px; font-weight: 500; }
.hero-banner-icon { position: absolute; right: 18px; top: 50%; width: 54px; height: 54px; transform: translateY(-50%); display: flex; align-items: center; justify-content: center; border-radius: 16px; background: linear-gradient(135deg,#ff7043,#ff5722); color: #fff; font-size: 30px; font-weight: 700; box-shadow: 0 4px 12px rgba(255,87,34,.28); }
.icon-svg { width: 28px; height: 28px; }
.employer-card { position: relative; overflow: hidden; margin: 16px 16px 0; padding: 16px 18px 18px; border-radius: 18px; background: linear-gradient(135deg,#fff3c4,#ffe8a0); box-shadow: 0 4px 14px rgba(255,193,7,.18); }
.employer-card-deco { position: absolute; right: -30px; top: -30px; width: 100px; height: 100px; border-radius: 50%; background: rgba(255,255,255,.25); }
.employer-top,.employer-main,.employer-cta-wrap { position: relative; z-index: 1; }
.employer-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.employer-location { display: flex; align-items: center; gap: 6px; color: #5d4037; font-size: 14px; font-weight: 500; }
.location-icon,.service-icon { color: #ff7043; }
.inline-icon { width: 16px; height: 16px; flex: 0 0 16px; }
.employer-service { display: flex; align-items: center; gap: 6px; padding: 8px 14px; border-radius: 20px; background: #fff; color: #5d4037; font-size: 12px; box-shadow: 0 2px 6px rgba(0,0,0,.05); }
.stat-line { display: block; color: #3e2723; font-size: 15px; font-weight: 600; }
.employer-stat-num { margin: 0 3px; color: #ff5722; font-size: 22px; font-weight: 800; }
.employer-stat-sub { display: block; margin-top: 4px; color: #8d6e63; font-size: 12px; }
.employer-cta-wrap { margin-top: 14px; }
.employer-cta-btn { padding: 14px; border: 2px solid #ff7043; border-radius: 7px; background: #fff; color: #000; text-align: center; font-size: 17px; font-weight: 700; letter-spacing: 4px; }
.peer-section { padding: 18px 16px 20px; }
.peer-features { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; background: transparent; }
.peer-feature { padding: 16px 14px; border: 1px solid rgba(255,112,67,.06); border-radius: 14px; background: #fff; box-shadow: 0 2px 10px rgba(44,24,16,.04); }
.peer-feature-icon { width: 36px; height: 36px; margin-bottom: 8px; display: flex; align-items: center; justify-content: center; border-radius: 10px; font-size: 18px; }
.peer-feature-icon.orange { background: #fff4e6; color: #ff7043; }.peer-feature-icon.green { background: #e8f5e9; color: #66bb6a; }.peer-feature-icon.blue { background: #e3f2fd; color: #42a5f5; }.peer-feature-icon.yellow { background: #fff8e1; color: #ffa726; }
.feature-svg { width: 20px; height: 20px; }
.peer-feature-title { display: block; margin-bottom: 6px; color: #2c1810; font-size: 14px; font-weight: 600; }
.peer-feature-desc { color: #6b4423; font-size: 12px; line-height: 1.6; opacity: .85; }
.bottom-slogan { padding: 24px 16px; text-align: center; }
.slogan-title { display: block; color: #ffe4cc; font-size: 32px; font-weight: 800; letter-spacing: 6px; }
.slogan-desc { display: block; margin-top: 8px; color: #d4b896; font-size: 13px; }
.tab-svg { width: 22px; height: 22px; opacity: .55; }
.tab-item.active .tab-svg { opacity: 1; }

</style>
