<template>
  <view class="page">
    <AppNavBar title="我的" />
    <scroll-view scroll-y class="content">
      <view class="user-header">
        <view class="avatar">👨</view>
        <view class="user-info" @click="go('/pages/worker/user-info')">
          <text class="name">{{ profile.name || "晴时见禾｜零工" }} ›</text>
        </view>
        <view class="switch-btn" @click="go('/pages/worker/switch-identity')">
          ⇄ 我要招人
        </view>
      </view>

      <view class="wallet-card">
        <view class="wallet-left">
          <text class="wallet-icon">¥</text>
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
          <text class="grid-icon" :style="{ color: item.color }">{{
            item.icon
          }}</text>
          <text class="grid-label">{{ item.label }}</text>
          <text v-if="item.badge" class="badge">{{ item.badge }}</text>
        </view>
      </view>
    </scroll-view>

    <WorkerTabBar current="profile" />
  </view>
</template>

<script setup>
import { onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import WorkerTabBar from "@/components/WorkerTabBar.vue";
import { request } from "@/api/http";

const profile = ref({});
const wallet = ref({ available: "0" });
const menus = [
  { key: "service", label: "联系客服", icon: "◉", color: "#1890ff" },
  { key: "rule", label: "平台规则", icon: "⚖", color: "#52c41a" },
  { key: "agreement", label: "用户服务协议", icon: "▤", color: "#999" },
  { key: "privacy", label: "隐私协议", icon: "♙", color: "#999" },
  { key: "copyright", label: "知识产权规则", icon: "©", color: "#999" },
  { key: "realname", label: "手机号认证", icon: "▯", color: "#52c41a" },
];

onMounted(async () => {
  try {
    const [result, walletResult] = await Promise.all([
      request({ url: "/auth/me" }),
      request({ url: "/worker/wallet" }),
    ]);
    if (result) {
      profile.value = typeof result === "string" ? { name: result } : result;
    }
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
  background: #fff8e6;
}

.content {
  height: calc(100vh - 176rpx);
  padding-bottom: 32rpx;
  box-sizing: border-box;
}

.user-header {
  display: flex;
  align-items: center;
  padding: 32rpx 36rpx;
  background: linear-gradient(135deg, #ffd59e 0%, #ffa94d 100%);
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
  text-overflow: ellipsis;
  white-space: nowrap;
}

.switch-btn {
  flex-shrink: 0;
  padding: 18rpx 26rpx;
  border-radius: 36rpx;
  background: rgba(255, 255, 255, 0.25);
  font-size: 24rpx;
}

.wallet-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 24rpx;
  min-height: 152rpx;
  padding: 28rpx 30rpx;
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
</style>
