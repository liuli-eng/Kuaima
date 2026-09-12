<template>
  <view class="container">
    <scroll-view scroll-y class="scroll-area">
      <view class="scroll-content">
        <view
          class="header-bar"
          :style="{ paddingTop: `${statusBarHeight + 12}px` }"
        >
          <view class="title-row">
            <text class="page-title">消息</text>
          </view>
        </view>
        <view class="message-list card-shadow">
          <view
            v-for="item in messageItems"
            :key="item.key"
            class="message-item"
            @click="openMessage(item)"
          >
            <view class="message-icon" :class="item.iconClass">
              <image :src="item.icon" mode="aspectFit" />
              <view v-if="item.unread" class="badge-dot" />
            </view>
            <view class="message-content">
              <text class="message-title">{{ item.title }}</text>
              <text class="message-desc">{{ item.content || "消息内容" }}</text>
            </view>
            <view
              v-if="item.key === 'enterprise-certification'"
              class="message-action action-btn-small"
            >
              {{ item.action || "立即认证" }}
            </view>
            <image
              v-else
              class="chevron-icon"
              :src="chevronIcon"
              mode="aspectFit"
            />
          </view>
          <view v-if="!messageItems.length" class="empty-message">暂无消息</view>
        </view>
        <view class="view-history" @click="openHistory"
          ><view class="history-btn"><text>查看历史消息</text></view></view
        >
        <view class="bottom-slogan"
          ><text class="slogan-title">招临时工 上快马日结</text
          ><text class="slogan-desc">— 熟练工 上岗快 人靠谱 —</text></view
        >
      </view>
    </scroll-view>
    <view class="tab-bar">
      <view class="tab-item" @click="switchTab('home')"
        ><view class="tab-icon-wrap"
          ><image
            src="/static/icons/boss-tabbar/house-gray.svg"
            mode="aspectFit" /></view
        ><text class="tab-label">首页</text></view
      >
      <view class="tab-item" @click="switchTab('order')"
        ><view class="tab-icon-wrap"
          ><image
            src="/static/icons/boss-tabbar/calendar-check-gray.svg"
            mode="aspectFit" /></view
        ><text class="tab-label">日结订单</text></view
      >
      <view class="tab-item" @click="switchTab('workbench')"
        ><view class="tab-icon-wrap"
          ><image
            src="/static/icons/boss-tabbar/briefcase-gray.svg"
            mode="aspectFit" /></view
        ><text class="tab-label">工作台</text></view
      >
      <view class="tab-item active"
        ><view class="tab-icon-wrap"
          ><image
            src="/static/icons/boss-tabbar/comment-dots-white.svg"
            mode="aspectFit" /></view
        ><text class="tab-label">消息</text></view
      >
      <view class="tab-item" @click="switchTab('profile')"
        ><view class="tab-icon-wrap"
          ><image
            src="/static/icons/boss-tabbar/face-smile-gray.svg"
            mode="aspectFit" /></view
        ><text class="tab-label">我的</text></view
      >
    </view>
  </view>
</template>

<script>
import buildingIcon from "/static/icons/boss-message/building-blue.svg";
import bellIcon from "/static/icons/boss-message/bell-orange.svg";
import userPlusIcon from "/static/icons/boss-message/user-plus-green.svg";
import moneyIcon from "/static/icons/boss-message/money-bill-wave-purple.svg";
import chevronIcon from "/static/icons/boss-message/chevron-right-gray.svg";
import { getBossMessageSummary } from "@/api/backend";

export default {
  data() {
    return {
      statusBarHeight: 0,
      summary: {
        enterpriseCertUnread: false,
        signupUnreadCount: 0,
        signupCount: 0,
        latestSystemNotice: null,
        latestSignup: null,
        latestSettlement: null,
        items: [],
      },
      buildingIcon,
      bellIcon,
      userPlusIcon,
      moneyIcon,
      chevronIcon,
    };
  },
  computed: {
    messageItems() {
      const items = Array.isArray(this.summary.items) ? this.summary.items : [];
      return items.map((item) => {
        const key = String(item.key || item.type || "")
          .toLowerCase()
          .replace(/_/g, "-");
        const iconConfig = {
          "enterprise-certification": [buildingIcon, "icon-blue"],
          "system-notice": [bellIcon, "icon-orange"],
          "signup-notice": [userPlusIcon, "icon-green"],
          settlement: [moneyIcon, "icon-purple"],
        }[key] || [bellIcon, "icon-orange"];
        return {
          ...item,
          key,
          icon: iconConfig[0],
          iconClass: iconConfig[1],
        };
      });
    },
  },
  onLoad() {
    try {
      const info =
        typeof uni.getWindowInfo === "function"
          ? uni.getWindowInfo()
          : uni.getSystemInfoSync();
      this.statusBarHeight = Number(info.statusBarHeight || 0);
    } catch (_) {}
    this.loadSummary();
  },
  onShow() {
    this.loadSummary();
  },
  methods: {
    openMessage(item) {
      const target = {
        "enterprise-certification": "/pages/boss/enterprise-cert",
        "system-notice": "/pages/boss/system-notice",
        "signup-notice": "/pages/boss/signup-notice",
        settlement: "/pages/boss/settlement",
      }[item.key];
      if (target) uni.navigateTo({ url: target });
    },
    async loadSummary() {
      try {
        const result = await getBossMessageSummary();
        if (result) this.summary = { ...this.summary, ...result };
      } catch (error) {
        uni.showToast({
          title: error?.message || "消息摘要加载失败",
          icon: "none",
        });
      }
    },
    openHistory() {
      uni.navigateTo({ url: "/pages/boss/history-message" });
    },
    switchTab(tab) {
      if (tab === "workbench")
        return uni.showToast({ title: "工作台页面暂未开放", icon: "none" });
      const target = {
        home: "/pages/boss/home",
        order: "/pages/boss/order",
        profile: "/pages/boss/profile",
      }[tab];
      if (!target) return;
      uni.redirectTo({
        url: target,
        fail: () => uni.reLaunch({ url: target }),
      });
    },
  },
};
</script>

<style lang="scss" scoped>
.container {
  width: 100%;
  height: 100vh;
  background: #f7f7f7;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.scroll-area {
  flex: 1;
  min-height: 0;
  background: #f7f7f7;
}
.scroll-content {
  display: flex;
  flex-direction: column;
  min-height: 100%;
}
.header-bar {
  background: linear-gradient(180deg, #ffd59e 0%, #ffe4b5 100%);
  padding: 12px 16px 20px;
}
.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #8b4513;
  position: relative;
}
.page-title::after {
  content: "";
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 20px;
  height: 3px;
  background: #ff6b35;
  border-radius: 2px;
}
.header-menu {
  display: flex;
  align-items: center;
  padding: 4px;
  border-radius: 50px;
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
.menu-icon {
  width: 28px;
  height: 28px;
  padding: 6px;
  box-sizing: border-box;
}
.menu-dot {
  padding: 8px;
}
.menu-divider {
  width: 1px;
  height: 16px;
  background: #ddd;
}
.message-list {
  margin: 12px 16px;
  border-radius: 16px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.empty-message {
  padding: 36px 16px;
  color: #999;
  font-size: 14px;
  text-align: center;
}
.message-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f5f5f5;
}
.message-item:last-child {
  border-bottom: none;
}
.message-icon {
  width: 48px;
  height: 48px;
  margin-right: 12px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  flex-shrink: 0;
}
.message-icon image {
  width: 22px;
  height: 22px;
}
.icon-blue {
  background: #e6f7ff;
}
.icon-orange {
  background: #fff3e0;
}
.icon-green {
  background: #e6f7ec;
}
.icon-purple {
  background: #f9f0ff;
}
.badge-dot {
  position: absolute;
  top: 0;
  right: 0;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ff4d4f;
  border: 2px solid #fff;
  box-sizing: border-box;
}
.message-content {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}
.message-title {
  display: block;
  margin-bottom: 4px;
  color: #333;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.35;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.message-desc {
  display: block;
  color: #999;
  font-size: 13px;
  line-height: 1.35;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.message-action {
  flex-shrink: 0;
}
.action-btn-small {
  padding: 8px 16px;
  border-radius: 20px;
  background: #ff6b35;
  color: #fff;
  font-size: 13px;
  font-weight: 500;
}
.chevron-icon {
  width: 8px;
  height: 14px;
  margin-left: 12px;
}
.view-history {
  padding: 24px 16px;
  text-align: center;
}
.history-btn {
  display: inline-block;
  padding: 12px 32px;
  border: 1px solid #ddd;
  border-radius: 24px;
  background: #fff;
  color: #666;
  font-size: 14px;
}
.bottom-slogan {
  padding: 20px 16px;
  text-align: center;
}
.slogan-title {
  display: block;
  color: #e8d5b7;
  font-size: 28px;
  font-weight: 800;
  letter-spacing: 4px;
}
.slogan-desc {
  display: block;
  margin-top: 8px;
  color: #ccc;
  font-size: 12px;
}
.tab-bar {
  flex-shrink: 0;
  min-height: 63px;
  display: flex;
  padding-bottom: env(safe-area-inset-bottom);
  background: rgba(255, 255, 255, 0.98);
  border-top: 0.5px solid rgba(0, 0, 0, 0.05);
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
  margin-bottom: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.tab-icon-wrap image {
  width: 20px;
  height: 20px;
}
.tab-item.active .tab-icon-wrap {
  width: 32px;
  height: 32px;
  margin-bottom: 2px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  box-shadow: 0 4px 10px rgba(255, 107, 53, 0.3);
}
.tab-item.active .tab-icon-wrap image {
  width: 21px;
  height: 21px;
}
.tab-label {
  color: #999;
  font-size: 10px;
  font-weight: 500;
}
.tab-item.active .tab-label {
  color: #ff6b35;
}
</style>
