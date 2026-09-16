<template>
  <view class="container">
    <view class="nav-bar" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="nav-inner">
        <button class="nav-back" @click="goBack">‹</button>
        <text class="nav-title">更换账号</text>
        <view class="nav-space" />
      </view>
    </view>

    <scroll-view scroll-y class="content-area">
      <view class="account-card">
        <text class="account-title">已登录账号</text>
        <view v-for="account in accounts" :key="account.id" class="account-item">
          <view class="account-avatar" :class="account.current ? 'orange' : 'blue'">{{ (account.name || '账').slice(0, 1) }}</view>
          <view class="account-info">
            <text class="account-name">{{ account.name || '招聘账号' }} <text v-if="account.current" class="current-badge">当前账号</text></text>
            <text class="account-phone">{{ account.workCode ? `开工码 ${account.workCode}` : formatAuthorization(account.authorizationType) }}</text>
          </view>
        </view>
        <view v-if="!accounts.length" class="account-empty">暂无可切换账号</view>
      </view>
      <view class="add-btn" @click="addAccount">＋ 添加新账号</view>
      <button class="logout-btn" :disabled="loggingOut" @click="logoutAccount">{{ loggingOut ? '退出中…' : '退出登录' }}</button>
      <text class="tips">切换账号将保留各自的订单、消息和设置数据</text>
    </scroll-view>
  </view>
</template>

<script>
import { getCurrentUser, logout } from "@/api/auth";

export default {
  data() {
    return { statusBarHeight: 0, loggingOut: false, accounts: [] };
  },
  onLoad() {
    try {
      const info =
        typeof uni.getWindowInfo === "function"
          ? uni.getWindowInfo()
          : uni.getSystemInfoSync();
      this.statusBarHeight = Number(info.statusBarHeight || 0);
    } catch (_) {}
    this.loadAccounts();
  },
  methods: {
    goBack() {
      uni.navigateBack();
    },
    async loadAccounts() {
      try {
        const cached = uni.getStorageSync("userInfo") || {};
        const result = await getCurrentUser().catch(() => cached);
        const account = { ...cached, ...(result || {}) };
        const name = account.nickname || account.name || account.realName || "当前账号";
        const phone = account.phone || account.phoneNumber || uni.getStorageSync("userPhone") || "";
        this.accounts = [{
          id: account.id || uni.getStorageSync("userId") || "current",
          name,
          phone: maskPhone(phone),
          current: true,
        }];
      } catch (error) {
        uni.showToast({ title: error?.message || "账号加载失败", icon: "none" });
      }
    },
    addAccount() {
      uni.showToast({ title: "添加账号功能暂未开放", icon: "none" });
    },
    async logoutAccount() {
      if (this.loggingOut) return;
      uni.showModal({ title: "退出登录", content: "确定退出当前账号吗？", success: async ({ confirm }) => {
        if (!confirm) return;
        this.loggingOut = true;
        await logout();
        this.loggingOut = false;
      }});
    },
  },
};

function maskPhone(value) {
  const phone = String(value || "");
  return /^(\d{3})\d{4}(\d{4})$/.test(phone)
    ? phone.replace(/^(\d{3})\d{4}(\d{4})$/, "$1****$2")
    : phone || "手机号未绑定";
}
</script>

<style lang="scss" scoped>
.container {
  width: 100%;
  height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.nav-bar {
  flex-shrink: 0;
  background: #fff;
  box-sizing: border-box;
}
.nav-inner {
  height: 50px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
}
.nav-title {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  color: #333;
  font-size: 17px;
  font-weight: 600;
}
.nav-back,
.nav-space {
  width: 32px;
  height: 32px;
}
.nav-back {
  margin: 0;
  padding: 0;
  border: 0;
  background: transparent;
  color: #333;
  font-size: 30px;
  line-height: 28px;
}
.nav-back::after,
.switch-btn::after,
.logout-btn::after {
  border: 0;
}
.content-area {
  flex: 1;
  height: 0;
  min-height: 0;
  box-sizing: border-box;
  background: #f5f5f5;
  padding-bottom: calc(24px + env(safe-area-inset-bottom));
}
.account-card { margin: 12px 16px; padding: 16px; border-radius: 12px; background: #fff; }
.account-title { display: block; margin-bottom: 12px; color: #333; font-size: 15px; font-weight: 600; }
.account-item { display: flex; align-items: center; gap: 12px; padding: 12px 0; border-bottom: 1px solid #f5f5f5; }
.account-item:last-child { border-bottom: 0; }
.account-avatar { width: 44px; height: 44px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; border-radius: 50%; color: #fff; font-weight: 600; }
.account-avatar.orange { background: linear-gradient(135deg, #ff6b35, #ff8c5a); }
.account-avatar.blue { background: linear-gradient(135deg, #1890ff, #40a9ff); }
.account-info { flex: 1; min-width: 0; }
.account-name { display: block; color: #333; font-size: 14px; font-weight: 600; }
.account-phone { display: block; margin-top: 2px; color: #999; font-size: 12px; }
.current-badge { padding: 2px 6px; border-radius: 4px; color: #ff6b35; background: #fff3ed; font-size: 11px; font-weight: 400; }
.switch-btn { width: auto; height: 30px; margin: 0; padding: 0 12px; border: 0; border-radius: 15px; color: #ff6b35; background: #fff3ed; font-size: 12px; line-height: 30px; }
.account-empty { padding: 24px 0; color: #aaa; font-size: 13px; text-align: center; }
.add-btn { display: flex; align-items: center; justify-content: center; margin: 12px 16px; padding: 14px; border: 1px dashed #ddd; border-radius: 10px; color: #666; background: #fff; font-size: 14px; }
.logout-btn { width: calc(100% - 32px); height: 46px; margin: 24px 16px 12px; padding: 0; border: 0; border-radius: 23px; color: #ff4d4f; background: #fff; font-size: 15px; line-height: 46px; }
.tips { display: block; padding: 0 16px; color: #bbb; font-size: 12px; line-height: 1.6; text-align: center; }
</style>
