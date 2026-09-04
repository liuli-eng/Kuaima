<template>
  <view class="login-page">
    <view
      class="phone-frame"
      :class="selectedRole"
      :style="{ paddingTop: `${statusBarHeight}px` }"
    >
      <view class="top-nav"
        ><button v-if="userRole" class="nav-back" @click="goBack">‹</button
        ><view v-else class="nav-placeholder" /><view class="brand-tag"
          >快马日结</view
        ></view
      >
      <view v-if="!userRole" class="role-select-view">
        <text class="section-title">选择身份</text
        ><text class="section-subtitle">选择您的身份，开始使用快马日结</text>
        <button
          class="role-card worker"
          :class="{ active: selectedRole === 'worker' }"
          @click="selectedRole = 'worker'"
        >
          <view class="role-card-left"
            ><text class="role-icon">🐴</text
            ><view
              ><text class="role-name">我要找活</text
              ><text class="role-desc">零工、日结、快速上岗</text></view
            ></view
          ><text class="role-arrow">›</text>
        </button>
        <button
          class="role-card boss"
          :class="{ active: selectedRole === 'boss' }"
          @click="selectedRole = 'boss'"
        >
          <view class="role-card-left"
            ><text class="role-icon">🐎</text
            ><view
              ><text class="role-name">我要招人</text
              ><text class="role-desc">招工、发活、高效管理</text></view
            ></view
          ><text class="role-arrow">›</text>
        </button>
        <SafeBottomAction class="bottom-action"
          ><button class="role-cta" @click="confirmRole">
            确认选择
          </button></SafeBottomAction
        >
      </view>
      <view v-else class="login-view">
        <view class="hero"
          ><text class="hero-title">{{
            selectedRole === "boss" ? "千万老板的选择" : "3000万零工的选择"
          }}</text
          ><text class="hero-subtitle"
            >{{
              selectedRole === "boss"
                ? "近30%完单雇主招工效率提升"
                : "近30%完单零工收入千元以上"
            }}　ⓘ</text
          ><text class="mascot">{{
            selectedRole === "boss" ? "🐎" : "🐴"
          }}</text
          ><text class="hero-tagline">{{
            selectedRole === "boss"
              ? "真零工·真上岗·真高效"
              : "真老板·真工价·真日结"
          }}</text></view
        >
        <view class="mobile-login-card"
          ><text class="login-title">手机号一键登录</text
          ><button class="phone-input-btn" @click="doLogin">
            手机号快捷登录</button
          ><text class="divider-line" @click="goBack">切换身份</text
          ><view class="agreement-row"
            ><view
              class="custom-checkbox"
              :class="{ checked: agreed }"
              @click="agreed = !agreed"
            >
              {{ agreed ? "✓" : "" }}</view
            ><text
              >我已阅读、理解并同意
              <text class="link" @click="openAgreement('user-agreement')"
                >《服务协议》</text
              >
              及
              <text class="link" @click="openAgreement('privacy')"
                >《隐私协议》</text
              ></text
            ></view
          ><text v-if="errorMessage" class="error-message">{{
            errorMessage
          }}</text></view
        >
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from "vue";
import SafeBottomAction from "@/components/SafeBottomAction.vue";
import { wechatLogin, getCurrentUser } from "@/api/auth";
import { USE_MOCK } from "@/api/http";

const pages = getCurrentPages();
const query = pages[pages.length - 1]?.options || {};
const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0;
const userRole = ref(
  query.role === "boss" || query.role === "worker" ? query.role : "",
);
const selectedRole = ref(userRole.value || "worker");
const agreed = ref(false);
const errorMessage = ref("");

function confirmRole() {
  userRole.value = selectedRole.value;
}
function goBack() {
  userRole.value = "";
  agreed.value = false;
  errorMessage.value = "";
}
function openAgreement(page) {
  uni.showToast({
    title: page === "privacy" ? "隐私协议" : "服务协议",
    icon: "none",
  });
}
async function doLogin() {
  if (!agreed.value) {
    errorMessage.value = "请先阅读并同意服务协议及隐私协议";
    return;
  }
  const role = selectedRole.value === "boss" ? "BOSS" : "USER";
  if (!USE_MOCK) {
    // 后端目前只提供微信小程序登录，真实模式下不伪造 H5 登录态。
    // #ifdef MP-WEIXIN
    uni.login({
      provider: "weixin",
      success: async ({ code }) => {
        try {
          const result = await wechatLogin({ code, role });
          uni.setStorageSync("token", result.accessToken);
          uni.setStorageSync("userId", String(result.userId));
          uni.setStorageSync("role", result.role);
          try {
            const user = await getCurrentUser();
            if (user && typeof user === "object") {
              uni.setStorageSync("userInfo", user);
              if (user.certStatus)
                uni.setStorageSync("workerCertStatus", user.certStatus);
            }
          } catch (_) {}
          uni.reLaunch({
            url:
              result.role === "BOSS"
                ? "/pages/boss/home"
                : "/pages/worker/home",
          });
        } catch (e) {
          errorMessage.value = e.message || "微信登录失败";
        }
      },
      fail: () => {
        errorMessage.value = "无法获取微信登录凭证";
      },
    });
    return;
    // #endif
    // #ifdef H5
    errorMessage.value =
      "真实接口暂仅支持微信小程序登录，请在微信开发者工具中运行";
    return;
    // #endif
  }
  const demoRole = selectedRole.value === "boss" ? "BOSS" : "USER";
  uni.setStorageSync("role", demoRole);
  uni.setStorageSync("userId", demoRole === "BOSS" ? "3001" : "2001");
  uni.reLaunch({
    url: demoRole === "BOSS" ? "/pages/boss/home" : "/pages/worker/home",
  });
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  background: #f3f4f6;
}
.phone-frame {
  box-sizing: border-box;
  width: 390px;
  height: 844px;
  padding-top: var(--status-bar-height);
  position: relative;
  overflow: hidden;
  background: linear-gradient(180deg, #fffbf5, #ffe4b5 55%, #ffbe5a);
  color: #333;
}
.phone-frame.boss {
  background: linear-gradient(180deg, #fff5e6, #ffe4b5 55%, #ffd966);
}
.top-nav {
  height: 50px;
  position: relative;
  display: flex;
  align-items: center;
  padding: 0 20px;
}
.nav-placeholder,
.nav-back {
  width: 36px;
  height: 36px;
}
.nav-back {
  border: 0;
  border-radius: 50%;
  background: #ffffffb3;
  font-size: 30px;
  line-height: 28px;
  color: #555;
  padding: 0;
}
.brand-tag {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  background: #fff;
  padding: 8px 20px;
  border-radius: 24px;
  font-weight: 700;
  color: #8b4513;
  box-shadow: 0 2px 10px #00000014;
  white-space: nowrap;
}
.role-select-view,
.login-view {
  height: calc(100% - 44px);
  display: flex;
  flex-direction: column;
}
.role-select-view {
  padding: 16px 24px 0;
}
.section-title {
  font-size: 28px;
  font-weight: 800;
  color: #8b4513;
  margin-bottom: 8px;
}
.section-subtitle,
.hero-subtitle {
  font-size: 14px;
  color: #a0522d;
  margin-bottom: 24px;
}
.role-card {
  width: 100%;
  border: 2px solid transparent;
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  text-align: left;
  box-shadow: 0 4px 20px #0000000f;
}
.role-card.worker.active {
  border-color: #ff6b35;
  background: #fff5f0;
}
.role-card.boss.active {
  border-color: #3b82f6;
  background: #eff6ff;
}
.role-card-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.role-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  background: #fff0e8;
}
.boss .role-icon {
  background: #e8f0ff;
}
.role-name,
.role-desc {
  display: block;
}
.role-name {
  font-size: 18px;
  font-weight: 700;
}
.role-desc {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
}
.role-arrow {
  font-size: 30px;
  color: #999;
}
.bottom-action {
  margin-top: auto;
  width: 100%;
  padding-left: 0;
  padding-right: 0;
}
.role-cta,
.phone-input-btn {
  border: 0;
  color: #fff;
  padding: 16px;
  border-radius: 50px;
  font-weight: 700;
  font-size: 16px;
  box-shadow: 0 8px 24px #ff6b3559;
}
.role-cta {
  width: 100%;
  background: linear-gradient(135deg, #ff6b35, #ff8c42);
}
.hero {
  position: relative;
  padding: 16px 24px 0;
}
.hero-title {
  font-size: 36px;
  letter-spacing: -1px;
  line-height: 1.1;
  font-weight: 800;
  color: #8b4513;
}
.hero-subtitle {
  display: block;
  margin-top: 8px;
}
.hero-tagline {
  display: block;
  font-size: 20px;
  color: #d2691e;
  margin-top: 16px;
  font-weight: 700;
}
.mascot {
  position: absolute;
  right: 12px;
  top: 74px;
  font-size: 96px;
  opacity: 0.28;
}
.mobile-login-card {
  margin-top: 32px;
  background: #fff;
  border-radius: 24px 24px 0 0;
  padding: 28px 24px 16px;
  flex: 1;
}
.login-title {
  font-size: 24px;
  font-weight: 800;
  display: block;
  margin-bottom: 20px;
}
.phone-input-btn {
  width: 100%;
  background: linear-gradient(135deg, #2ecc71, #27ae60);
}
.divider-line {
  display: block;
  margin: 16px auto;
  padding: 0;
  width: 100%;
  text-align: center;
  color: #999;
  font-size: 13px;
  line-height: 24px;
}
.agreement-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  color: #999;
  font-size: 12px;
  line-height: 1.6;
}
.custom-checkbox {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  border: 2px solid #ddd;
  border-radius: 50%;
  background: #fff;
  color: #fff;
  flex: none;
  font-size: 10px;
  box-sizing: border-box;
}
.custom-checkbox.checked {
  background: #ff6b35;
  border-color: #ff6b35;
}
.link {
  color: #ff6b35;
  text-decoration: underline;
}
.error-message {
  color: #e34d59;
  font-size: 12px;
  margin-top: 12px;
}
@media (max-width: 430px) {
  .phone-frame {
    width: 100%;
    height: 100vh;
  }
}
</style>
