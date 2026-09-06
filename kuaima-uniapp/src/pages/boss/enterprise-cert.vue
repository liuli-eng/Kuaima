<template>
  <view class="page">
    <view
      class="nav-bar"
      :style="{
        paddingTop: `${statusBarHeight}px`,
        height: `${statusBarHeight + 50}px`,
      }"
    >
      <view class="nav-back" @click="goBack">←</view>
      <text class="nav-title">企业认证</text>
      <view class="nav-placeholder" />
    </view>
    <scroll-view scroll-y class="scroll-area">
      <view class="hero-section">
        <view class="hero-icon"><view class="shield">✓</view></view>
        <text class="hero-title">{{ heroTitle }}</text>
        <text class="hero-desc">{{ heroDescription }}</text>
      </view>
      <view class="cert-options">
        <view class="cert-card" @click="openCertification">
          <view class="cert-header">
            <text class="cert-name">企业自招（仅限本公司招工）</text>
            <view class="cert-btn" :class="`status-${status}`">{{
              actionLabel
            }}</view>
          </view>
          <text class="cert-desc">{{ cardDescription }}</text>
          <text v-if="rejectReason" class="reject-reason"
            >驳回原因：{{ rejectReason }}</text
          >
        </view>
      </view>
      <view class="contact-link" @click="openService"
        >认证遇到问题？联系客服 ›</view
      >
      <view class="benefits-section">
        <text class="benefits-title">企业认证后，获得以下权益</text>
        <view class="benefits-grid">
          <view v-for="item in benefits" :key="item.text" class="benefit-item">
            <view class="benefit-icon">{{ item.icon }}</view
            ><text class="benefit-text">{{ item.text }}</text>
          </view>
        </view>
      </view>
      <view class="bottom-space" />
    </scroll-view>
  </view>
</template>

<script>
import { getUser, listCertifications } from "@/api/backend";
export default {
  data() {
    return {
      statusBarHeight: 0,
      status: "unverified",
      rejectReason: "",
      loading: false,
      benefits: [
        { icon: "⚡", text: "快速审核" },
        { icon: "●", text: "优先推荐" },
        { icon: "✓", text: "认证标识" },
      ],
    };
  },
  computed: {
    heroTitle() {
      return {
        pending: "企业认证审核中",
        approved: "企业认证已完成",
        rejected: "企业认证未通过",
        unverified: "请尽快完成企业认证",
      }[this.status];
    },
    heroDescription() {
      return {
        pending: "资料已提交，请耐心等待审核",
        approved: "您已获得企业认证相关权益",
        rejected: "请修改认证资料后重新提交",
        unverified: "否则会影响后续发布招工",
      }[this.status];
    },
    actionLabel() {
      return this.loading
        ? "加载中"
        : {
            pending: "审核中",
            approved: "已认证",
            rejected: "重新认证",
            unverified: "认证",
          }[this.status];
    },
    cardDescription() {
      return this.status === "approved"
        ? "企业认证资料已通过平台审核"
        : "上传营业执照原件或加盖公章复印件";
    },
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
  onShow() {
    this.loadCertification();
  },
  methods: {
    async loadCertification() {
      this.loading = true;
      try {
        const userId = uni.getStorageSync("userId") || "2001";
        const [user, records] = await Promise.all([
          getUser(userId),
          listCertifications(userId).catch(() => []),
        ]);
        const list = Array.isArray(records) ? records : [];
        const latest = list.find(
          (item) =>
            String(item.type || item.certType || "").includes("企业") ||
            String(item.type || item.certType || "").toUpperCase() ===
              "ENTERPRISE",
        );
        const raw =
          latest?.status ||
          (String(user?.certType || "").toUpperCase() === "ENTERPRISE"
            ? user?.certStatus
            : "");
        const value = String(raw || "");
        this.status =
          value.includes("待") || value === "PENDING"
            ? "pending"
            : value.includes("通过") || value === "APPROVED"
              ? "approved"
              : value.includes("拒") || value === "REJECTED"
                ? "rejected"
                : "unverified";
        this.rejectReason = latest?.rejectReason || "";
      } catch (error) {
        uni.showToast({
          title: error.message || "认证状态加载失败",
          icon: "none",
        });
      } finally {
        this.loading = false;
      }
    },
    goBack() {
      uni.navigateBack();
    },
    openCertification() {
      if (this.loading || this.status === "pending") return;
      if (this.status === "approved")
        return uni.showToast({ title: "企业已完成认证", icon: "success" });
      uni.navigateTo({ url: "/pages/boss/enterprise-cert-form" });
    },
    openService() {
      uni.navigateTo({ url: "/pages/boss/service-chat" });
    },
  },
};
</script>

<style lang="scss" scoped>
.page {
  height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.nav-bar {
  box-sizing: border-box;
  display: flex;
  align-items: flex-end;
  padding: 0 32rpx 18rpx;
  background: #fff;
  flex-shrink: 0;
  position: relative;
}
.nav-back,
.nav-placeholder {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.nav-back {
  font-size: 36rpx;
  color: #333;
}
.nav-title {
  position: absolute;
  left: 50%;
  bottom: 24rpx;
  transform: translateX(-50%);
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
}
.nav-placeholder {
  margin-left: auto;
}
.scroll-area {
  flex: 1;
  min-height: 0;
}
.hero-section {
  padding: 60rpx 40rpx 40rpx;
  text-align: center;
}
.hero-icon {
  width: 128rpx;
  height: 128rpx;
  margin: 0 auto 32rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #ffe4b5, #ffdab9);
  display: flex;
  align-items: center;
  justify-content: center;
}
.shield {
  width: 64rpx;
  height: 74rpx;
  border-radius: 18rpx 18rpx 30rpx 30rpx;
  background: #ff6b35;
  color: #fff;
  font-size: 42rpx;
  line-height: 74rpx;
}
.hero-title {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #333;
}
.hero-desc {
  display: block;
  margin-top: 12rpx;
  font-size: 26rpx;
  color: #999;
}
.cert-options {
  padding: 0 32rpx;
}
.cert-card {
  padding: 32rpx;
  border: 2rpx solid #f0f0f0;
  border-radius: 24rpx;
  background: #fff;
}
.cert-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
}
.cert-name {
  min-width: 0;
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}
.cert-btn {
  flex-shrink: 0;
  padding: 12rpx 32rpx;
  border-radius: 40rpx;
  background: linear-gradient(135deg, #ffd700, #ffa500);
  color: #fff;
  font-size: 26rpx;
}
.status-pending,
.status-approved {
  background: #f0f0f0;
  color: #999;
}
.cert-desc,
.reject-reason {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #999;
}
.reject-reason {
  padding: 16rpx 20rpx;
  background: #fff3ed;
  color: #ff6b35;
  border-radius: 12rpx;
}
.contact-link {
  padding: 40rpx 32rpx;
  text-align: center;
  font-size: 26rpx;
  color: #ff6b35;
}
.benefits-section {
  margin-top: 40rpx;
  padding: 40rpx 32rpx;
  background: #fafafa;
}
.benefits-title {
  display: block;
  margin-bottom: 40rpx;
  text-align: center;
  font-size: 28rpx;
  color: #666;
}
.benefits-grid {
  display: flex;
  justify-content: space-around;
}
.benefit-item {
  width: 160rpx;
  text-align: center;
}
.benefit-icon {
  width: 96rpx;
  height: 96rpx;
  margin: 0 auto 16rpx;
  border-radius: 50%;
  background: #fff3ed;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ff6b35;
  font-size: 36rpx;
  font-weight: 700;
}
.benefit-text {
  font-size: 24rpx;
  color: #666;
}
.bottom-space {
  height: calc(60rpx + env(safe-area-inset-bottom));
}
</style>
