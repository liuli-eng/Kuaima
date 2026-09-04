<template>
  <view class="container">
    <!-- 导航栏 -->
    <view class="nav-bar" :style="{ paddingTop: `${statusBarHeight}px`, height: `${50 + statusBarHeight}px` }">
      <view class="nav-back" @click="goBack">
        <text>←</text>
      </view>
      <text class="nav-title">手机号验证</text>
      <view class="nav-space"></view>
    </view>

    <scroll-view scroll-y class="content">
      <!-- 图标 -->
      <view class="phone-icon">
        <text style="font-size:32px;color:#FF6B35;">📱</text>
      </view>

      <text class="page-title">验证手机号</text>
      <text class="page-desc">为了保障您的账户安全，需要验证<br>您绑定的手机号 138****5678</text>

      <!-- 表单 -->
      <view class="form-card">
        <view class="form-item">
          <text class="form-label">手机号</text>
          <input type="tel" class="form-input phone" value="138 **** 5678" disabled />
        </view>
        <view class="form-item">
          <text class="form-label">验证码</text>
          <input type="text" class="form-input" placeholder="请输入6位验证码" v-model="code" maxlength="6" />
          <button class="send-code-btn" :class="{ disabled: countdown > 0 }" @click="sendCode">
            {{ countdown > 0 ? countdown + 's 后重试' : '获取验证码' }}
          </button>
        </view>
      </view>

      <!-- 验证码输入框 -->
      <view class="code-input-wrap">
        <view 
          class="code-box" 
          :class="{ filled: code.length >= i, active: code.length === i - 1 }"
          v-for="i in 6" 
          :key="i"
        >{{ code[i-1] || '' }}</view>
      </view>

      <button class="verify-btn" :disabled="code.length !== 6" @click="doVerify">确 认 验 证</button>

      <!-- 提示 -->
      <view class="tip-box">
        <view class="tip-icon">!</view>
        <text class="tip-text">未收到短信？请检查手机号码是否正确，或尝试稍后再获取。</text>
      </view>

      <!-- 协议 -->
      <view class="agreement">
        <view class="checkbox-custom" :class="{ checked: agreed }" @click="agreed = !agreed"></view>
        <text class="agreement-text">
          您已阅读并同意<text style="color:#FF6B35;" @click.stop="navigateTo('user-agreement')">《快马日结隐私政策》</text>
        </text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      statusBarHeight: 0,
      code: '',
      countdown: 0,
      agreed: false
    }
  },
  onLoad() {
    try {
      const info = typeof uni.getWindowInfo === 'function' ? uni.getWindowInfo() : uni.getSystemInfoSync()
      this.statusBarHeight = Number(info.statusBarHeight || 0)
    } catch (_) {}
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    navigateTo(page) {
      const url = page === 'user-agreement' ? '/pages/worker/user-agreement' : `/pages/${page}`
      uni.navigateTo({ url })
    },
    sendCode() {
      if (this.countdown > 0) return
      this.countdown = 60
      const timer = setInterval(() => {
        this.countdown--
        if (this.countdown <= 0) {
          clearInterval(timer)
        }
      }, 1000)
      uni.showToast({ title: '验证码已发送至 138****5678', icon: 'success' })
    },
    doVerify() {
      if (!this.agreed) {
        uni.showToast({ title: '请先阅读并同意隐私政策', icon: 'none' })
        return
      }
      if (this.code.length !== 6) {
        uni.showToast({ title: '请输入6位验证码', icon: 'none' })
        return
      }
      uni.showToast({ title: '验证成功！', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  }
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

.status-bar {
  height: 47px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 28px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  background: transparent;
}

.status-icons {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-bar {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #fff;
  position: relative;
  z-index: 10;
  box-sizing: border-box;
}

.nav-back {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-space {
  width: 32px;
  height: 32px;
}

.nav-title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
}

.nav-right {
  display: flex;
  align-items: center;
  background: rgba(255,255,255,0.8);
  backdrop-filter: blur(10px);
  border-radius: 9999px;
  padding: 3px 6px;
  gap: 2px;
}

.nav-btn {
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-divider {
  width: 1px;
  height: 10px;
  background: #ddd;
}

.content {
  flex: 1;
  height: 0;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  box-sizing: border-box;
  padding: 20px 20px calc(24px + env(safe-area-inset-bottom));
}

.phone-icon {
  width: 72px;
  height: 72px;
  margin: 10px auto 16px;
  background: linear-gradient(135deg, #FFE4B5, #FFD966);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(255, 180, 50, 0.3);
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #333;
  text-align: center;
  margin-bottom: 8px;
  display: block;
}

.page-desc {
  font-size: 13px;
  color: #999;
  text-align: center;
  margin-bottom: 28px;
  line-height: 1.6;
  display: block;
}

.form-card {
  background: #FAFAFA;
  border-radius: 16px;
  padding: 8px 20px;
  margin-bottom: 24px;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #EFEFEF;
}

.form-item:last-child {
  border-bottom: none;
}

.form-label {
  font-size: 15px;
  color: #333;
  font-weight: 500;
  width: 70px;
  flex-shrink: 0;
}

.form-input {
  flex: 1;
  min-width: 0;
  font-size: 15px;
  color: #333;
  border: none;
  outline: none;
  background: transparent;
}

.form-input::placeholder {
  color: #bbb;
}

.form-input.phone {
  letter-spacing: 1px;
}

.send-code-btn {
  flex-shrink: 0;
  font-size: 13px;
  color: #FF6B35;
  font-weight: 500;
  padding: 7px 12px;
  margin: 0 0 0 8px;
  border: 1px solid #FF6B35;
  border-radius: 999px;
  background: transparent;
  white-space: nowrap;
}

.send-code-btn::after,
.verify-btn::after {
  border: none;
}

.send-code-btn.disabled {
  color: #bbb;
  border-color: #ddd;
}

.verify-btn {
  width: 100%;
  margin-top: 32px;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: #fff;
  padding: 16px;
  border-radius: 28px;
  font-size: 17px;
  font-weight: 600;
  box-shadow: 0 8px 20px rgba(255, 180, 50, 0.4);
  border: none;
}

.verify-btn:disabled {
  opacity: 0.6;
}

.code-input-wrap {
  display: flex;
  gap: 10px;
  margin-top: 8px;
  justify-content: center;
}

.code-box {
  width: 44px;
  height: 52px;
  background: #FAFAFA;
  border: 1.5px solid #E0E0E0;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 600;
  color: #333;
}

.code-box.filled {
  border-color: #FF6B35;
}

.code-box.active {
  border-color: #FF6B35;
  box-shadow: 0 0 0 3px rgba(255,107,53,0.1);
}

.tip-box {
  background: #FFF8F0;
  border-radius: 12px;
  padding: 14px 16px;
  margin-top: 20px;
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.tip-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  background: #FF6B35;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  margin-top: 1px;
}

.tip-text {
  font-size: 12px;
  color: #666;
  line-height: 1.7;
}

.agreement {
  display: flex;
  align-items: flex-start;
  justify-content: center;
  margin-top: 20px;
  gap: 6px;
}

.checkbox-custom {
  width: 16px;
  height: 16px;
  border: 2px solid #ddd;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 1px;
}

.checkbox-custom.checked {
  background: #FF6B35;
  border-color: #FF6B35;
}

.checkbox-custom.checked::after {
  content: '✓';
  color: #fff;
  font-size: 10px;
  font-weight: bold;
}

.agreement-text {
  font-size: 12px;
  color: #999;
  line-height: 1.6;
}
</style>
