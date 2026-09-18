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
        <text class="account-title">已关联账号</text>
        <view v-for="account in accounts" :key="account.id" class="account-item">
          <view class="account-avatar" :class="account.current ? 'orange' : 'blue'">
            {{ (account.name || "账").slice(0, 1) }}
          </view>
          <view class="account-info">
            <text class="account-name">
              {{ account.name || "老板账号" }}
              <text v-if="account.current" class="current-badge">当前账号</text>
            </text>
            <text class="account-phone">{{ account.phone || "手机号未绑定" }}</text>
          </view>
          <button
            v-if="!account.current"
            class="switch-btn"
            :disabled="switchingAccountId === account.id"
            @click="quickLogin(account)"
          >
            {{ switchingAccountId === account.id ? "登录中…" : "切换" }}
          </button>
        </view>
        <view v-if="!loadingAccounts && !accounts.length" class="account-empty">暂无已关联账号</view>
      </view>

      <view class="add-btn" @click="openAddPopup">＋ 添加新账号</view>
      <button class="logout-btn" :disabled="loggingOut" @click="logoutAccount">
        {{ loggingOut ? "退出中…" : "退出登录" }}
      </button>
      <text class="tips">手机号验证仅用于注册或关联账号，关联后可直接快速登录</text>
    </scroll-view>

    <view v-if="showAddPopup" class="popup-mask" @click="closeAddPopup">
      <view class="popup-panel" @click.stop>
        <view class="popup-handle" />
        <view class="popup-header">
          <text class="popup-title">添加新账号</text>
          <text class="popup-close" @click="closeAddPopup">×</text>
        </view>
        <text class="popup-desc">未注册手机号验证通过后将自动注册并关联</text>

        <view class="form-row">
          <text class="form-label">手机号</text>
          <input v-model="addForm.phone" class="form-input" type="number" maxlength="11" placeholder="请输入手机号" />
        </view>
        <view class="form-row">
          <text class="form-label">验证码</text>
          <input v-model="addForm.smsCode" class="form-input" type="number" maxlength="6" placeholder="请输入6位验证码" />
          <button class="code-btn" :disabled="sendingCode || countdown > 0" @click="sendCode">
            {{ sendingCode ? "发送中…" : countdown > 0 ? `${countdown}s` : "获取验证码" }}
          </button>
        </view>

        <view class="agreement-row" @click="addForm.agreementAccepted = !addForm.agreementAccepted">
          <view class="checkbox" :class="{ checked: addForm.agreementAccepted }">
            {{ addForm.agreementAccepted ? "✓" : "" }}
          </view>
          <text class="agreement-text">我已阅读并同意</text>
          <text class="agreement-link" @click.stop="openAgreement('user-agreement')">《用户服务协议》</text>
          <text class="agreement-link" @click.stop="openAgreement('privacy')">《隐私协议》</text>
        </view>

        <button class="confirm-btn" :disabled="addingAccount" @click="confirmAddAccount">
          {{ addingAccount ? "关联中…" : "确认添加" }}
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import { getCurrentUser, logout, sendSmsCode } from "@/api/auth";
import {
  addBossRecruitAccount,
  getBossRecruitAccounts,
  quickLoginBossRecruitAccount,
} from "@/api/backend";

export default {
  data() {
    return {
      statusBarHeight: 0,
      accounts: [],
      loadingAccounts: false,
      switchingAccountId: null,
      loggingOut: false,
      showAddPopup: false,
      sendingCode: false,
      addingAccount: false,
      countdown: 0,
      countdownTimer: null,
      addForm: { phone: "", smsCode: "", agreementAccepted: false },
    };
  },
  onLoad() {
    try {
      const info = typeof uni.getWindowInfo === "function" ? uni.getWindowInfo() : uni.getSystemInfoSync();
      this.statusBarHeight = Number(info.statusBarHeight || 0);
    } catch (_) {}
    this.loadAccounts();
  },
  onUnload() {
    this.clearCountdown();
  },
  methods: {
    goBack() {
      uni.navigateBack();
    },
    async loadAccounts() {
      if (this.loadingAccounts) return;
      this.loadingAccounts = true;
      try {
        const [result, currentUser] = await Promise.all([
          getBossRecruitAccounts(),
          getCurrentUser().catch(() => null),
        ]);
        const rows = Array.isArray(result) ? result : result?.accounts;
        const currentAccountId = result?.currentAccountId;
        this.accounts = (Array.isArray(rows) ? rows : []).map((account) => ({
          ...account,
          current: Boolean(account.current || account.id === currentAccountId),
        }));
        if (!this.accounts.some((account) => account.current) && currentUser) {
          this.accounts.unshift({
            id: `current-${currentUser.id || "user"}`,
            name: currentUser.nickname || currentUser.name || currentUser.realName || "当前账号",
            phone: maskPhone(currentUser.phone || currentUser.phoneNumber),
            current: true,
          });
        }
      } catch (error) {
        uni.showToast({ title: error?.message || "账号加载失败", icon: "none" });
      } finally {
        this.loadingAccounts = false;
      }
    },
    async quickLogin(account) {
      if (!account?.id || account.current || this.switchingAccountId) return;
      this.switchingAccountId = account.id;
      try {
        const result = await quickLoginBossRecruitAccount(account.id);
        if (!result?.accessToken || !result?.userId || result?.role !== "BOSS") {
          throw new Error("快速登录接口返回数据不完整");
        }
        uni.setStorageSync("token", result.accessToken);
        uni.setStorageSync("userId", String(result.userId));
        uni.setStorageSync("role", result.role);
        uni.setStorageSync("currentRole", "boss");
        if (result.phone) uni.setStorageSync("userPhone", result.phone);
        if (result.certStatus) uni.setStorageSync("bossCertStatus", result.certStatus);
        uni.removeStorageSync("userInfo");
        uni.showToast({ title: "账号切换成功", icon: "success" });
        setTimeout(() => uni.reLaunch({ url: "/pages/boss/home" }), 400);
      } catch (error) {
        uni.showToast({ title: error?.message || "快速登录失败", icon: "none" });
      } finally {
        this.switchingAccountId = null;
      }
    },
    openAddPopup() {
      this.showAddPopup = true;
    },
    closeAddPopup() {
      if (!this.addingAccount) this.showAddPopup = false;
    },
    clearCountdown() {
      if (this.countdownTimer) clearInterval(this.countdownTimer);
      this.countdownTimer = null;
    },
    startCountdown() {
      this.clearCountdown();
      this.countdown = 60;
      this.countdownTimer = setInterval(() => {
        this.countdown -= 1;
        if (this.countdown <= 0) this.clearCountdown();
      }, 1000);
    },
    async sendCode() {
      if (this.sendingCode || this.countdown > 0) return;
      const phone = String(this.addForm.phone || "").trim();
      if (!/^1[3-9]\d{9}$/.test(phone)) return this.showError("请输入正确的手机号");
      this.sendingCode = true;
      try {
        await sendSmsCode(phone);
        this.startCountdown();
        uni.showToast({ title: "验证码已发送", icon: "success" });
      } catch (error) {
        this.showError(error?.message || "验证码发送失败");
      } finally {
        this.sendingCode = false;
      }
    },
    async confirmAddAccount() {
      if (this.addingAccount) return;
      const phone = String(this.addForm.phone || "").trim();
      const smsCode = String(this.addForm.smsCode || "").trim();
      if (!/^1[3-9]\d{9}$/.test(phone)) return this.showError("请输入正确的手机号");
      if (!/^\d{6}$/.test(smsCode)) return this.showError("请输入6位验证码");
      if (!this.addForm.agreementAccepted) return this.showError("请先阅读并同意相关协议");
      this.addingAccount = true;
      try {
        await addBossRecruitAccount({ phone, smsCode, agreementAccepted: true });
        this.showAddPopup = false;
        this.addForm = { phone: "", smsCode: "", agreementAccepted: false };
        this.countdown = 0;
        this.clearCountdown();
        await this.loadAccounts();
        uni.showToast({ title: "账号关联成功", icon: "success" });
      } catch (error) {
        this.showError(error?.message || "账号关联失败");
      } finally {
        this.addingAccount = false;
      }
    },
    showError(title) {
      uni.showToast({ title, icon: "none" });
    },
    openAgreement(page) {
      uni.navigateTo({ url: `/pages/worker/${page}` });
    },
    logoutAccount() {
      if (this.loggingOut) return;
      uni.showModal({
        title: "退出登录",
        content: "确定退出当前账号吗？",
        success: async ({ confirm }) => {
          if (!confirm) return;
          this.loggingOut = true;
          try {
            await logout();
          } finally {
            this.loggingOut = false;
          }
        },
      });
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
.container { width: 100%; height: 100vh; display: flex; flex-direction: column; overflow: hidden; background: #fff; }
.nav-bar { flex-shrink: 0; box-sizing: border-box; background: #fff; }
.nav-inner { position: relative; height: 50px; padding: 0 16px; display: flex; align-items: center; justify-content: space-between; }
.nav-title { position: absolute; left: 50%; transform: translateX(-50%); color: #333; font-size: 17px; font-weight: 600; }
.nav-back,
.nav-space { width: 32px; height: 32px; }
.nav-back { margin: 0; padding: 0; border: 0; color: #333; background: transparent; font-size: 30px; line-height: 28px; }
.nav-back::after,
.switch-btn::after,
.logout-btn::after,
.code-btn::after,
.confirm-btn::after { border: 0; }
.content-area { flex: 1; height: 0; min-height: 0; box-sizing: border-box; padding-bottom: calc(24px + env(safe-area-inset-bottom)); background: #f5f5f5; }
.account-card { margin: 12px 16px; padding: 16px; border-radius: 12px; background: #fff; }
.account-title { display: block; margin-bottom: 12px; color: #333; font-size: 15px; font-weight: 600; }
.account-item { display: flex; align-items: center; gap: 12px; padding: 12px 0; border-bottom: 1px solid #f5f5f5; }
.account-item:last-child { border-bottom: 0; }
.account-avatar { width: 44px; height: 44px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; border-radius: 50%; color: #fff; font-weight: 600; }
.account-avatar.orange { background: linear-gradient(135deg, #ff6b35, #ff8c5a); }
.account-avatar.blue { background: linear-gradient(135deg, #1890ff, #40a9ff); }
.account-info { flex: 1; min-width: 0; }
.account-name { display: block; color: #333; font-size: 14px; font-weight: 600; }
.account-phone { display: block; margin-top: 3px; color: #999; font-size: 12px; }
.current-badge { padding: 2px 6px; border-radius: 4px; color: #ff6b35; background: #fff3ed; font-size: 11px; font-weight: 400; }
.switch-btn { width: auto; height: 30px; margin: 0; padding: 0 12px; border: 0; border-radius: 15px; color: #ff6b35; background: #fff3ed; font-size: 12px; line-height: 30px; }
.account-empty { padding: 24px 0; color: #aaa; font-size: 13px; text-align: center; }
.add-btn { display: flex; align-items: center; justify-content: center; margin: 12px 16px; padding: 14px; border: 1px dashed #ddd; border-radius: 10px; color: #666; background: #fff; font-size: 14px; }
.logout-btn { width: calc(100% - 32px); height: 46px; margin: 24px 16px 12px; padding: 0; border: 0; border-radius: 23px; color: #ff4d4f; background: #fff; font-size: 15px; line-height: 46px; }
.tips { display: block; padding: 0 24px; color: #bbb; font-size: 12px; line-height: 1.6; text-align: center; }
.popup-mask { position: fixed; inset: 0; z-index: 20; display: flex; align-items: flex-end; background: rgba(0, 0, 0, 0.45); }
.popup-panel { width: 100%; padding: 8px 20px calc(20px + env(safe-area-inset-bottom)); box-sizing: border-box; border-radius: 20px 20px 0 0; background: #fff; }
.popup-handle { width: 38px; height: 4px; margin: 0 auto 12px; border-radius: 2px; background: #ddd; }
.popup-header { display: flex; align-items: center; justify-content: space-between; }
.popup-title { color: #222; font-size: 18px; font-weight: 600; }
.popup-close { padding: 0 2px 4px 16px; color: #999; font-size: 28px; line-height: 1; }
.popup-desc { display: block; margin: 8px 0 20px; color: #999; font-size: 13px; }
.form-row { height: 54px; display: flex; align-items: center; border-bottom: 1px solid #eee; }
.form-label { width: 70px; flex-shrink: 0; color: #333; font-size: 14px; }
.form-input { flex: 1; min-width: 0; color: #333; font-size: 14px; }
.code-btn { width: auto; height: 32px; margin: 0; padding: 0 10px; border: 0; color: #ff6b35; background: #fff3ed; font-size: 12px; line-height: 32px; }
.code-btn[disabled] { color: #aaa; background: #f5f5f5; }
.agreement-row { display: flex; align-items: center; flex-wrap: wrap; margin-top: 18px; }
.checkbox { width: 17px; height: 17px; margin-right: 6px; display: flex; align-items: center; justify-content: center; box-sizing: border-box; border: 1px solid #ccc; border-radius: 4px; color: #fff; font-size: 12px; }
.checkbox.checked { border-color: #ff6b35; background: #ff6b35; }
.agreement-text,
.agreement-link { font-size: 12px; line-height: 22px; }
.agreement-text { color: #999; }
.agreement-link { color: #ff6b35; }
.confirm-btn { height: 46px; margin: 20px 0 0; border: 0; border-radius: 23px; color: #fff; background: linear-gradient(135deg, #ff6b35, #ff8c5a); font-size: 15px; line-height: 46px; }
.confirm-btn[disabled] { opacity: 0.65; }
</style>
