<template>
  <view class="container">
    <view class="nav-bar" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="nav-inner">
        <button class="nav-back" @click="goBack">‹</button>
        <text class="nav-title">切换身份</text>
        <view class="nav-space" />
      </view>
    </view>

    <scroll-view scroll-y class="content-area">
      <view class="avatar-section">
        <view class="avatar-circle"><text class="avatar-text">晴</text></view>
      </view>
      <text class="identity-text">你当前身份是老板</text>
      <view class="btn-section">
        <button
          class="btn-primary"
          :disabled="switching"
          @click="switchToWorker"
        >
          切换为零工身份
        </button>
        <button class="btn-secondary" @click="stayHere">暂不切换</button>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { wechatLogin } from "@/api/auth";
import { USE_MOCK } from "@/api/http";

export default {
  data() {
    return { statusBarHeight: 0, switching: false };
  },
  onLoad() {
    try {
      const info =
        typeof uni.getWindowInfo === "function"
          ? uni.getWindowInfo()
          : uni.getSystemInfoSync();
      this.statusBarHeight = Number(info.statusBarHeight || 0);
    } catch (_) {}
  },
  methods: {
    goBack() {
      uni.navigateBack();
    },
    switchToWorker() {
      if (this.switching) return;
      this.switching = true;
      if (USE_MOCK) {
        this.finishSwitch({
          role: "USER",
          userId: uni.getStorageSync("userId") || "2001",
          accessToken: uni.getStorageSync("token"),
        });
        return;
      }
      // API.md 规定身份切换复用微信登录接口，必须用新的 wx.login code 换取带目标角色的 JWT。
      // #ifdef MP-WEIXIN
      uni.login({
        provider: "weixin",
        success: async ({ code }) => {
          try {
            const result = await wechatLogin({ code, role: "USER" });
            this.finishSwitch(result);
          } catch (error) {
            this.switching = false;
            uni.showToast({
              title: error.message || "身份切换失败",
              icon: "none",
            });
          }
        },
        fail: () => {
          this.switching = false;
          uni.showToast({ title: "无法获取微信登录凭证", icon: "none" });
        },
      });
      return;
      // #endif
      // #ifdef H5
      this.switching = false;
      uni.showToast({ title: "请在微信小程序中完成身份切换", icon: "none" });
      // #endif
    },
    finishSwitch(result = {}) {
      if (result.accessToken) uni.setStorageSync("token", result.accessToken);
      if (result.userId !== undefined)
        uni.setStorageSync("userId", String(result.userId));
      uni.setStorageSync("role", result.role || "USER");
      uni.showToast({ title: "已切换为零工身份", icon: "success" });
      setTimeout(() => uni.reLaunch({ url: "/pages/worker/home" }), 500);
    },
    stayHere() {
      uni.navigateBack();
    },
  },
};
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
.btn-primary::after,
.btn-secondary::after {
  border: 0;
}
.content-area {
  flex: 1;
  height: 0;
  min-height: 0;
  box-sizing: border-box;
  background: #fff;
  padding-bottom: calc(24px + env(safe-area-inset-bottom));
}
.avatar-section {
  display: flex;
  justify-content: center;
  padding: 60px 0 30px;
}
.avatar-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ffe4b5, #ffd966);
  display: flex;
  align-items: center;
  justify-content: center;
}
.avatar-text {
  color: #ff6b35;
  font-size: 48px;
  font-weight: 700;
}
.identity-text {
  display: block;
  margin-bottom: 60px;
  color: #333;
  font-size: 22px;
  font-weight: 600;
  text-align: center;
}
.btn-section {
  padding: 0 24px;
}
.btn-primary,
.btn-secondary {
  width: 100%;
  height: 56px;
  margin: 0 0 16px;
  padding: 0;
  border-radius: 28px;
  font-size: 16px;
  line-height: 56px;
  text-align: center;
  box-sizing: border-box;
}
.btn-primary {
  color: #fff;
  background: linear-gradient(135deg, #ffd700, #ffa500);
  font-weight: 600;
  box-shadow: 0 8px 20px rgba(255, 180, 50, 0.35);
}
.btn-primary[disabled] {
  opacity: 0.6;
}
.btn-secondary {
  color: #666;
  background: #fff;
  border: 1px solid #eee;
  font-weight: 500;
}
</style>
