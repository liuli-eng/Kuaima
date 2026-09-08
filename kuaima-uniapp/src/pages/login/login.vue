<template>
  <view class="login-page">
    <view
      class="phone-frame"
      :class="selectedRole"
      :style="{ paddingTop: `${statusBarHeight}px` }"
    >
      <view class="top-nav">
        <button v-if="userRole" class="nav-back" @click="goBack">‹</button>
        <view v-else class="nav-placeholder"></view>
        <view v-if="userRole" class="brand-tag">快马日结</view>
      </view>
      <view v-if="!userRole" class="role-select-view">
        <view class="welcome-copy">
          <text class="welcome-subtitle">很高兴见到你</text>
          <text class="welcome-title">请选择你的身份</text>
        </view>
        <view class="role-grid">
          <button
            class="role-card worker"
            :class="{ active: selectedRole === 'worker' }"
            @click="selectedRole = 'worker'"
          >
            <view v-if="selectedRole === 'worker'" class="selected-mark">✓</view>
            <text class="role-name">零工找活</text>
            <text class="role-desc">真老板</text>
            <text class="role-desc">真工价</text>
            <text class="role-desc">真日结</text>
            <image
              class="role-art"
              src="/static/login-worker.png"
              mode="widthFix"
            />
          </button>
          <button
            class="role-card boss"
            :class="{ active: selectedRole === 'boss' }"
            @click="selectedRole = 'boss'"
          >
            <view v-if="selectedRole === 'boss'" class="selected-mark">✓</view>
            <text class="role-name">老板招工</text>
            <text class="role-desc">熟练工</text>
            <text class="role-desc">上岗快</text>
            <text class="role-desc">人靠谱</text>
            <image
              class="role-art"
              src="/static/login-boss.png"
              mode="widthFix"
            />
          </button>
        </view>
        <SafeBottomAction class="bottom-action"
          ><button class="role-cta" @click="confirmRole">
            {{ selectedRole === "worker" ? "我是零工，去找活" : "我是老板，去招工" }}
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
          >
          <!-- #ifdef MP-WEIXIN -->
          <button
            class="phone-input-btn"
            open-type="getPhoneNumber"
            @getphonenumber="handlePhoneLogin"
          >
            手机号快捷登录
          </button>
          <!-- #endif -->
          <!-- #ifndef MP-WEIXIN -->
          <button class="phone-input-btn" @click="doLogin()">
            手机号快捷登录
          </button>
          <!-- #endif -->
          <text class="divider-line" @click="goBack">切换身份</text
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
      <view class="page-indicator"></view>
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
function handlePhoneLogin(event) {
  const phoneCode = event?.detail?.code;
  if (!phoneCode) {
    errorMessage.value = "需要授权手机号后才能快捷登录";
    return;
  }
  doLogin(phoneCode);
}

async function doLogin(phoneCode = "") {
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
          const result = await wechatLogin({
            code,
            role,
            ...(phoneCode ? { phoneCode } : {}),
          });
          uni.setStorageSync("token", result.accessToken);
          uni.setStorageSync("userId", String(result.userId));
          uni.setStorageSync("role", result.role);
          if (result.phone) uni.setStorageSync("userPhone", result.phone);
          if (result.certStatus) {
            uni.setStorageSync("workerCertStatus", result.certStatus);
            uni.setStorageSync("certStatus", result.certStatus);
          }
          try {
            const user = await getCurrentUser();
            if (user && typeof user === "object") {
              uni.setStorageSync("userInfo", user);
              if (user.phone) uni.setStorageSync("userPhone", user.phone);
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
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #fffbf5 0%, #ffe4b5 50%, #ffbe5a 100%);
  color: #333;
}
.phone-frame.boss {
  background: linear-gradient(180deg, #fff5e6 0%, #ffe4b5 50%, #ffd966 100%);
}
.top-nav {
  height: 50px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-sizing: border-box;
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
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}
.role-select-view {
  padding: 18px 20px 24px;
  box-sizing: border-box;
  background: linear-gradient(180deg, #fff8d6 0%, #ffec99 100%);
}
.welcome-copy { text-align: center; margin-bottom: 22px; }
.welcome-subtitle { display: block; font-size: 13px; font-weight: 500; color: #8b6f00; margin-bottom: 8px; }
.welcome-title { display: block; font-size: 25px; line-height: 1.3; font-weight: 800; color: #2d2200; }
.role-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; align-items: stretch; }
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
  position: relative;
  width: auto;
  min-width: 0;
  min-height: 380px;
  border: 2px solid transparent;
  background: #f8e387;
  border-radius: 14px;
  padding: 18px 14px 14px;
  margin: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: left;
  box-shadow: none;
  box-sizing: border-box;
  overflow: hidden;
}
.role-card.worker { background: #ffd93d; border-color: #ffbf00; }
.role-card.boss { background: #f8e387; border-color: #e9c74c; }
.role-card::after { border: none; }
.role-card.worker.active {
  border-color: #2f2d27;
  background: #ffd93d;
  box-shadow: 0 10px 22px rgba(45, 34, 0, 0.35);
}
.role-card.boss.active {
  border-color: #2f2d27;
  background: #f8e387;
  box-shadow: 0 10px 22px rgba(45, 34, 0, 0.25);
}
.role-name, .role-desc {
  display: block;
}
.role-name {
  font-size: 23px;
  font-weight: 900;
  color: #2d2200;
  margin-bottom: 12px;
}
.role-desc {
  width: 100%;
  text-align: center;
  background: #fff;
  color: #4a3500;
  font-size: 13px;
  line-height: 34px;
  height: 34px;
  margin-top: 6px;
  border-radius: 12px;
  font-weight: 600;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}
.role-art {
  display: block;
  width: 100%;
  height: auto;
  margin-top: auto;
  margin-bottom: -14px;
}
.selected-mark {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 38px;
  height: 38px;
  border-radius: 0 12px 0 12px;
  background: #303036;
  color: #fff;
  text-align: center;
  line-height: 38px;
  font-size: 24px;
  font-weight: 800;
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
  padding: 16px 20px;
  border-radius: 50px;
  font-weight: 700;
  font-size: 16px;
  box-shadow: 0 8px 24px #ff6b3559;
  line-height: normal;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}
.role-cta::after,
.phone-input-btn::after { border: none; }
.role-cta {
  width: 100%;
  margin-top: 28px;
  background: linear-gradient(135deg, #ffd93d 0%, #f5b700 100%);
  color: #4a3500;
  font-weight: 800;
  letter-spacing: 1px;
  box-shadow: 0 8px 24px rgba(255, 200, 30, 0.45);
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
.page-indicator {
  position: absolute;
  left: 50%;
  bottom: 8px;
  width: 134px;
  height: 5px;
  border-radius: 3px;
  background: #000;
  transform: translateX(-50%);
  z-index: 20;
}
@media (max-width: 430px) {
  .phone-frame {
    width: 100%;
    height: 100vh;
  }
}
</style>
