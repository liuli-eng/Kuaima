<template>
  <view class="page">
    <AppNavBar title="手机号验证" :show-back="true" />
    <scroll-view scroll-y class="content">
      <view class="header-section">
        <view class="header-icon"><text class="phone-icon">⌕</text></view>
        <text class="header-title">验证手机号</text>
        <text class="header-desc"
          >为了保障您的账户安全，需要验证<br />您绑定的手机号 138****5678</text
        >
      </view>
      <view class="form-section">
        <view class="form-item"
          ><text class="form-label">手机号</text
          ><input class="form-input phone" value="138 **** 5678" disabled
        /></view>
        <view class="form-item"
          ><text class="form-label">验证码</text
          ><input
            class="form-input"
            v-model="code"
            type="number"
            maxlength="6"
            placeholder="请输入6位验证码"
          /><button
            class="send-code-btn"
            :disabled="countdown > 0"
            @click="sendCode"
          >
            {{
              countdown
                ? `${countdown}s 后重试`
                : sent
                  ? "重新获取"
                  : "获取验证码"
            }}
          </button></view
        >
      </view>
      <button class="verify-btn" :disabled="code.length !== 6" @click="verify">
        确 认 验 证
      </button>
      <view class="tip-box"
        ><view class="tip-icon">!</view
        ><text class="tip-text"
          >未收到短信？请检查手机号码是否正确，或尝试稍后再获取。</text
        ></view
      >
      <view class="agreement-section"
        ><view
          class="agreement-checkbox"
          :class="{ checked: agreed }"
          @click="agreed = !agreed"
          ><text v-if="agreed">✓</text></view
        ><text class="agreement-text"
          >您已阅读并同意<text class="link" @click.stop="openPrivacy"
            >《快马日结隐私政策》</text
          ></text
        ></view
      >
    </scroll-view>
  </view>
</template>
<script setup>
import { ref, onBeforeUnmount } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
const code = ref("");
const countdown = ref(0);
const sent = ref(false);
const agreed = ref(true);
let timer;
function sendCode() {
  if (countdown.value) return;
  sent.value = true;
  countdown.value = 60;
  timer = setInterval(() => {
    countdown.value -= 1;
    if (countdown.value <= 0) {
      clearInterval(timer);
      timer = null;
    }
  }, 1000);
  uni.showToast({ title: "验证码已发送至 138****5678", icon: "success" });
}
function verify() {
  if (!agreed.value)
    return uni.showToast({ title: "请先阅读并同意隐私政策", icon: "none" });
  if (code.value.length !== 6) return;
  uni.showToast({ title: "验证成功", icon: "success" });
  setTimeout(() => uni.navigateBack(), 600);
}
function openPrivacy() {
  uni.navigateTo({ url: "/pages/worker/user-agreement" });
}
onBeforeUnmount(() => {
  if (timer) clearInterval(timer);
});
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}
.content {
  height: calc(100vh - 176rpx);
  box-sizing: border-box;
  padding-bottom: 24rpx;
}
.header-section {
  text-align: center;
  padding: 64rpx 40rpx 48rpx;
  background: #fff;
}
.header-icon {
  width: 160rpx;
  height: 160rpx;
  margin: 0 auto 32rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #ffd700, #ffa500);
  display: flex;
  align-items: center;
  justify-content: center;
}
.phone-icon {
  color: #fff;
  font-size: 72rpx;
  font-weight: 700;
  transform: rotate(-30deg);
}
.header-title {
  display: block;
  color: #333;
  font-size: 40rpx;
  font-weight: 600;
}
.header-desc {
  display: block;
  margin-top: 16rpx;
  color: #999;
  font-size: 28rpx;
  line-height: 1.6;
}
.form-section {
  margin: 0 32rpx;
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
}
.form-item {
  display: flex;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid #f5f5f5;
  box-sizing: border-box;
}
.form-item:last-child {
  border-bottom: 0;
}
.form-label {
  width: 140rpx;
  flex-shrink: 0;
  color: #333;
  font-size: 30rpx;
}
.form-input {
  flex: 1;
  min-width: 0;
  padding: 0;
  border: 0;
  background: transparent;
  color: #666;
  font-size: 30rpx;
}
.form-input.phone {
  color: #333;
  letter-spacing: 2rpx;
}
.send-code-btn {
  flex-shrink: 0;
  margin: 0 0 0 12rpx;
  padding: 12rpx 20rpx;
  border: 1rpx solid #ff6b35;
  border-radius: 999rpx;
  background: transparent;
  color: #ff6b35;
  font-size: 24rpx;
  line-height: 1.2;
}
.send-code-btn::after,
.verify-btn::after {
  border: 0;
}
.send-code-btn[disabled] {
  border-color: #ddd;
  color: #bbb;
}
.verify-btn {
  display: block;
  width: calc(100% - 64rpx);
  height: 88rpx;
  margin: 40rpx 32rpx 24rpx;
  padding: 0;
  border: 0;
  border-radius: 48rpx;
  background: linear-gradient(135deg, #ffd700, #ffa500);
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
  line-height: 88rpx;
}
.verify-btn[disabled] {
  opacity: 0.5;
}
.tip-box {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  margin: 0 32rpx;
  padding: 24rpx 28rpx;
  border-radius: 20rpx;
  background: #fff8f0;
}
.tip-icon {
  width: 36rpx;
  height: 36rpx;
  flex-shrink: 0;
  border-radius: 50%;
  background: #ff6b35;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  font-weight: 700;
}
.tip-text {
  color: #666;
  font-size: 24rpx;
  line-height: 1.7;
}
.agreement-section {
  display: flex;
  align-items: flex-start;
  padding: 32rpx 40rpx 0;
  color: #999;
  font-size: 24rpx;
  line-height: 1.6;
}
.agreement-checkbox {
  width: 32rpx;
  height: 32rpx;
  flex-shrink: 0;
  margin: 4rpx 16rpx 0 0;
  border: 2rpx solid #ddd;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}
.agreement-checkbox.checked {
  border-color: #ffd000;
  background: #ffd000;
  color: #fff;
}
.link {
  color: #ff6b35;
}
</style>
