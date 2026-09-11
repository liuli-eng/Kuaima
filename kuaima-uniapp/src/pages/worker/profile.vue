<template>
  <view class="page">
    <scroll-view scroll-y class="content">
      <view class="header-nav" :style="{ paddingTop: `${statusBarHeight}px` }">
        <text class="nav-title">我的</text>
        <view class="nav-space" />
      </view>
      <view class="user-header">
        <view class="avatar"><image :src="avatarIcon" mode="aspectFill" /></view>
        <view class="user-info" @click="go('/pages/worker/user-info')">
          <view class="name-row"><text class="name">{{ profileName }}</text><image :src="chevronRightIcon" mode="aspectFit" class="chevron" /></view>
        </view>
        <view class="switch-btn" @click="go('/pages/worker/switch-identity?role=worker')">
          <image :src="exchangeIcon" mode="aspectFit" /> 我要招人
        </view>
      </view>

      <view class="wallet-card">
        <view class="wallet-left">
          <image class="wallet-icon" :src="coinsIcon" mode="aspectFit" />
          <view>
            <text class="wallet-label">收入余额(元)</text>
            <text class="wallet-value">{{ wallet.available }}</text>
          </view>
        </view>
        <button class="withdraw" @click="go('/pages/worker/wallet')">
          立即提现
        </button>
      </view>

      <text class="section-title">其他功能</text>
      <view class="menu-grid">
        <view
          v-for="item in menus"
          :key="item.key"
          class="grid-item"
          @click="handle(item)"
        >
          <image class="grid-icon" :src="item.icon" mode="aspectFit" />
          <text class="grid-label">{{ item.label }}</text>
          <text v-if="item.badge" class="badge">{{ item.badge }}</text>
        </view>
      </view>
    </scroll-view>

    <WorkerTabBar current="profile" />
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import WorkerTabBar from "@/components/WorkerTabBar.vue";
import { request } from "@/api/http";
import { getWorkerProfile } from "@/api/backend";
import avatarIcon from "/static/icons/worker-profile/avatar.svg";
import exchangeIcon from "/static/icons/worker-profile/exchange-orange.svg";
import coinsIcon from "/static/icons/worker-profile/coins-orange.svg";
import headsetIcon from "/static/icons/worker-profile/headset.svg";
import balanceIcon from "/static/icons/worker-profile/balance.svg";
import fileIcon from "/static/icons/worker-profile/file.svg";
import userLockIcon from "/static/icons/worker-profile/user-lock.svg";
import copyrightIcon from "/static/icons/worker-profile/copyright.svg";
import mobileIcon from "/static/icons/worker-profile/mobile-orange.svg";
import chevronRightIcon from "/static/icons/worker-profile/chevron-right-brown.svg";

const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0;

const profile = ref({});
const wallet = ref({ available: "0" });
const profileName = computed(() => {
  const name =
    profile.value.nickname ||
    profile.value.name ||
    profile.value.realName ||
    profile.value.username ||
    "未设置昵称";

  return name.includes("零工") ? name : `${name}｜零工`;
});
const menus = [
  { key: "service", label: "联系客服", icon: headsetIcon },
  { key: "rule", label: "平台规则", icon: balanceIcon },
  { key: "agreement", label: "用户服务协议", icon: fileIcon },
  { key: "privacy", label: "隐私协议", icon: userLockIcon },
  { key: "copyright", label: "知识产权规则", icon: copyrightIcon },
  { key: "realname", label: "手机号认证", icon: mobileIcon },
];

onShow(async () => {
  try {
    const [workerResult, authResult, walletResult] = await Promise.all([
      getWorkerProfile().catch(() => null),
      request({ url: "/auth/me" }).catch(() => null),
      request({ url: "/worker/wallet" }),
    ]);
    const cached = uni.getStorageSync("userInfo") || {};
    const worker =
      typeof workerResult === "string" ? { nickname: workerResult } : workerResult || {};
    const auth =
      typeof authResult === "string" ? { name: authResult } : authResult || {};
    profile.value = { ...cached, ...auth, ...worker };
    if (walletResult)
      wallet.value.available = (
        Number(walletResult.balance || 0) / 100
      ).toFixed(2);
  } catch (_) {}
});

function go(url) {
  uni.navigateTo({ url });
}

function toast(title) {
  uni.showToast({ title, icon: "none" });
}

function handle(item) {
  const map = {
    service: "/pages/worker/service",
    rule: "/pages/worker/rule",
    agreement: "/pages/worker/user-agreement",
    privacy: "/pages/worker/privacy",
    copyright: "/pages/worker/copyright",
    realname: "/pages/worker/realname",
  };

  if (map[item.key]) {
    go(map[item.key]);
  } else {
    toast(`${item.label}功能开发中`);
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f7f7f7;
}

.content {
  height: calc(100vh - 83px - env(safe-area-inset-bottom));
  background: #f7f7f7;
  padding-bottom: 32rpx;
  box-sizing: border-box;
}

.header-nav {
  min-height: 88rpx;
  padding: 16rpx 32rpx 16rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-sizing: border-box;
  background: #ffd96f;
}
.nav-title { color: #333; font-size: 34rpx; font-weight: 600; }
.nav-space { width: 64rpx; height: 64rpx; }

.user-header {
  display: flex;
  align-items: center;
  padding: 32rpx;
  background: #ffd96f;
  color: #fff;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border: 4rpx solid rgba(255, 255, 255, 0.5);
  border-radius: 50%;
  background: #fff;
  color: #ff6b35;
  text-align: center;
  line-height: 120rpx;
  font-size: 72rpx;
  font-weight: 800;
}

.user-info {
  flex: 1;
  min-width: 0;
  margin-left: 24rpx;
}

.name {
  display: block;
  overflow: hidden;
  font-size: 34rpx;
  font-weight: 700;
  color: #8b4513;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.switch-btn {
  flex-shrink: 0;
  padding: 18rpx 26rpx;
  border-radius: 36rpx;
  background: #fff;
  color: #ff6b35;
  font-size: 24rpx;
}

.wallet-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 24rpx 24rpx;
  min-height: 132rpx;
  padding: 32rpx;
  border-radius: 24rpx;
  background: #fff;
}

.wallet-left {
  display: flex;
  align-items: center;
  gap: 22rpx;
}

.wallet-icon {
  color: #ff6b35;
  font-size: 54rpx;
  font-weight: 800;
}

.wallet-label {
  display: block;
  color: #666;
  font-size: 24rpx;
}

.wallet-value {
  display: block;
  margin-top: 6rpx;
  color: #333;
  font-size: 42rpx;
  font-weight: 800;
}

.withdraw {
  margin: 0;
  padding: 0 34rpx;
  height: 68rpx;
  line-height: 68rpx;
  border-radius: 34rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: #fff;
  font-size: 24rpx;
}

.withdraw::after {
  border: 0;
}

.section-title {
  display: block;
  padding: 8rpx 32rpx 20rpx;
  color: #333;
  font-size: 30rpx;
  font-weight: 700;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 34rpx 0;
  margin: 0 24rpx 24rpx;
  padding: 34rpx 16rpx;
  border-radius: 24rpx;
  background: #fff;
}

.grid-item {
  position: relative;
  text-align: center;
}

.grid-icon {
  display: block;
  height: 48rpx;
  font-size: 40rpx;
  line-height: 48rpx;
}

.grid-label {
  display: block;
  margin-top: 8rpx;
  color: #666;
  font-size: 22rpx;
}

.badge {
  position: absolute;
  top: -8rpx;
  right: 26rpx;
  padding: 2rpx 8rpx;
  border-radius: 14rpx;
  background: #ff4d4f;
  color: #fff;
  font-size: 18rpx;
}
.avatar image { width: 100%; height: 100%; }
.name-row { display: flex; align-items: center; min-width: 0; }
.name-row .name { min-width: 0; }
.chevron { width: 16rpx; height: 24rpx; margin-left: 8rpx; flex-shrink: 0; }
.switch-btn { display: flex; align-items: center; gap: 8rpx; }
.switch-btn image { width: 28rpx; height: 28rpx; }
.wallet-icon { width: 54rpx; height: 54rpx; }
.grid-icon { width: 48rpx; height: 48rpx; margin: 0 auto; }
</style>
