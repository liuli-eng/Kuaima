<template>
  <view class="container">
    <!-- 导航栏 -->
    <view class="nav-bar" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="nav-back" @click="goBack">
        <text>←</text>
      </view>
      <text class="nav-title">入企申请</text>
      <view style="width: 32px;"></view>
    </view>

    <scroll-view scroll-y class="body">
      <view v-if="loading" class="page-state">加载中...</view>
      <template v-else>
        <!-- 无邀请码时：先输入邀请码 -->
        <view v-if="!submitted && !inviteCode" class="code-card">
          <text class="code-title">请输入邀请码</text>
          <text class="code-desc">向邀请您的企业索取6位邀请码</text>
          <view class="code-input-row">
            <input class="code-input" v-model="manualCode" placeholder="邀请码" maxlength="20" />
            <view class="code-btn" @click="onEnterCode">查询</view>
          </view>
        </view>

        <!-- 头部提示 -->
        <view class="header-card" v-if="!submitted && inviteCode">
          <text class="header-ico">🏢</text>
          <text class="header-text">您正在加入</text>
          <text class="header-name">{{ enterpriseName }}</text>
          <text class="header-tip">请填写以下信息提交入企申请</text>
        </view>

        <!-- 提交成功 -->
        <view v-if="submitted" class="success-card">
          <text class="success-ico">✅</text>
          <text class="success-title">申请已提交</text>
          <text class="success-tip">{{ successMsg }}</text>
          <view class="success-btn" @click="goHome">返回首页</view>
        </view>

        <!-- 申请表单 -->
        <view v-if="!submitted && inviteCode" class="form-card">
          <view class="form-title">填写申请信息</view>
          <view class="form-item">
            <text class="form-label">姓名 <text class="required">*</text></text>
            <input class="form-input" v-model="form.name" placeholder="请输入真实姓名" maxlength="20" />
          </view>
          <view class="form-item">
            <text class="form-label">手机号 <text class="required">*</text></text>
            <input class="form-input" v-model="form.phone" type="number" placeholder="请输入11位手机号" maxlength="11" />
          </view>
          <view class="form-item" style="margin-bottom:4px;">
            <text class="form-label">申请留言</text>
            <textarea
              class="form-textarea"
              v-model="form.note"
              placeholder="可选：填写自我介绍或申请理由"
              maxlength="200"
            />
            <text class="form-count">{{ (form.note || "").length }}/200</text>
          </view>
        </view>

        <view v-if="!submitted && inviteCode" class="submit-btn" :class="{ disabled: submitting }" @click="submitApply">
          {{ submitting ? "提交中..." : "提交申请" }}
        </view>
      </template>
      <view class="bottom-space" />
    </scroll-view>
  </view>
</template>

<script>
import { getPublicInviteInfo, submitEnterpriseApply } from "@/api/enterprise";

export default {
  data() {
    return {
      statusBarHeight: 0,
      loading: false,
      submitting: false,
      submitted: false,
      successMsg: "",
      inviteCode: "",
      manualCode: "",
      enterpriseName: "",
      form: { name: "", phone: "", note: "" },
    };
  },
  onLoad(query) {
    const info = typeof uni.getWindowInfo === "function"
      ? uni.getWindowInfo() : uni.getSystemInfoSync();
    this.statusBarHeight = Number(info.statusBarHeight || 0);
    this.inviteCode = query.code || "";
    if (this.inviteCode) {
      this.loadInviteInfo();
    }
  },
  methods: {
    goBack() {
      if (this.submitted) {
        this.goHome();
      } else {
        uni.reLaunch({ url: "/pages/worker/home" });
      }
    },
    goHome() {
      uni.reLaunch({ url: "/pages/worker/home" });
    },
    async loadInviteInfo() {
      this.loading = true;
      try {
        const data = await getPublicInviteInfo(this.inviteCode);
        this.enterpriseName = data?.enterpriseName || "该企业";
      } catch (error) {
        uni.showToast({ title: "邀请码无效或已失效", icon: "none" });
      } finally {
        this.loading = false;
      }
    },
    onEnterCode() {
      const v = (this.manualCode || "").trim();
      if (!v) {
        uni.showToast({ title: "请输入邀请码", icon: "none" });
        return;
      }
      this.inviteCode = v;
      this.loadInviteInfo();
    },
    async submitApply() {
      if (this.submitting) return;
      if (!this.form.name.trim()) {
        uni.showToast({ title: "请输入姓名", icon: "none" });
        return;
      }
      if (!/^1\d{10}$/.test(this.form.phone.trim())) {
        uni.showToast({ title: "请输入正确的11位手机号", icon: "none" });
        return;
      }
      this.submitting = true;
      try {
        const data = await submitEnterpriseApply({
          code: this.inviteCode,
          name: this.form.name.trim(),
          phone: this.form.phone.trim(),
          note: this.form.note.trim(),
        });
        this.successMsg = data?.message || "申请已提交，请等待企业审核";
        this.submitted = true;
      } catch (error) {
        uni.showToast({ title: error?.message || "提交失败", icon: "none" });
      } finally {
        this.submitting = false;
      }
    },
  },
};
</script>

<style lang="scss" scoped>
.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f3f4f6;
}
.nav-bar {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #fff;
}
.nav-back { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; font-size: 18px; color: #333; }
.nav-title { font-size: 17px; font-weight: 600; color: #333; }

.body { flex: 1; padding: 16px; }
.page-state { padding: 32px 0; color: #999; font-size: 14px; text-align: center; }

.header-card {
  background: #fff; border-radius: 16px; padding: 24px 16px; text-align: center;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04); margin-bottom: 16px;
}
.header-ico { font-size: 36px; display: block; margin-bottom: 8px; }
.header-text { font-size: 13px; color: #999; display: block; }
.header-name { font-size: 20px; font-weight: 700; color: #FF6B35; display: block; margin-top: 6px; }
.header-tip { font-size: 12px; color: #bbb; display: block; margin-top: 8px; }

.form-card {
  background: #fff; border-radius: 16px; padding: 16px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04); margin-bottom: 16px;
}
.form-title { font-size: 15px; font-weight: 600; color: #333; margin-bottom: 14px; }
.form-item { margin-bottom: 16px; }
.form-label { display: block; font-size: 13px; color: #666; margin-bottom: 8px; }
.required { color: #EF4444; }
.form-input {
  width: 100%; padding: 12px 14px; border: 1px solid #ebebeb; border-radius: 12px;
  font-size: 14px; background: #fafafa; box-sizing: border-box; color: #333;
}
.form-textarea {
  width: 100%; height: 80px; padding: 12px 14px; border: 1px solid #ebebeb; border-radius: 12px;
  font-size: 14px; background: #fafafa; box-sizing: border-box; color: #333;
}
.form-count { display: block; text-align: right; font-size: 11px; color: #999; margin-top: 4px; }

.submit-btn {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A); color: #fff;
  text-align: center; padding: 13px 0; border-radius: 24px;
  font-size: 15px; font-weight: 600; margin-bottom: 16px;
  box-shadow: 0 6px 16px rgba(255, 107, 53, 0.3);
}
.submit-btn.disabled { opacity: 0.6; }

.success-card {
  background: #fff; border-radius: 16px; padding: 40px 16px; text-align: center;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
}
.success-ico { font-size: 50px; display: block; margin-bottom: 12px; }
.success-title { font-size: 18px; font-weight: 700; color: #333; display: block; }
.success-tip { font-size: 13px; color: #999; display: block; margin-top: 8px; line-height: 20px; }
.success-btn {
  margin: 24px auto 0; width: 60%; background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  color: #fff; text-align: center; padding: 11px 0; border-radius: 22px;
  font-size: 14px; font-weight: 600;
}

.bottom-space { height: 20px; }

/* 邀请码输入卡片 */
.code-card {
  background: #fff; border-radius: 16px; padding: 28px 20px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04); margin-bottom: 16px; text-align: center;
}
.code-title { font-size: 17px; font-weight: 700; color: #333; display: block; }
.code-desc { font-size: 12px; color: #999; display: block; margin-top: 6px; }
.code-input-row { display: flex; gap: 10px; margin-top: 18px; }
.code-input {
  flex: 1; padding: 12px 14px; border: 1.5px solid #ebebeb; border-radius: 12px;
  font-size: 15px; background: #fafafa; color: #333; text-align: center; letter-spacing: 2px;
}
.code-btn {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A); color: #fff;
  font-size: 14px; font-weight: 600; padding: 0 22px; border-radius: 12px;
  display: flex; align-items: center;
}
</style>
