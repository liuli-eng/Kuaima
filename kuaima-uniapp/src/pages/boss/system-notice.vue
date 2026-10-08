<template>
  <view class="page">
    <view class="top-nav" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="nav-inner">
        <view class="nav-back" @click="goBack">
          <image
            src="/static/icons/boss-points/arrow-left-dark.svg"
            mode="aspectFit"
          />
        </view>
        <text class="nav-title">系统通知</text>
        <view class="nav-placeholder" />
      </view>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <view v-if="loading" class="state">正在加载通知...</view>
      <view v-else-if="error" class="state error" @click="loadNotices">
        {{ error }}，点击重试
      </view>
      <view v-else-if="!notices.length" class="state">暂无通知</view>
      <view
        v-for="notice in notices"
        v-else
        :key="notice.id"
        class="notice-item"
      >
        <text class="notice-time">{{ notice.time }}</text>
        <view class="notice-content">
          <text class="notice-tag" :class="notice.tagClass">{{ notice.tag }}</text>
          <text class="notice-text">{{ notice.content }}</text>
        </view>
      </view>
      <view class="bottom-space" />
    </scroll-view>
  </view>
</template>

<script>
import { listSystemMessages } from "@/api/backend";

export default {
  data() {
    return {
      statusBarHeight: 0,
      loading: false,
      error: "",
      notices: [],
    };
  },
  onLoad() {
    try {
      const info =
        typeof uni.getWindowInfo === "function"
          ? uni.getWindowInfo()
          : uni.getSystemInfoSync();
      this.statusBarHeight = Number(info.statusBarHeight || 0);
    } catch (_) {}
    this.loadNotices();
  },
  methods: {
    async loadNotices() {
      if (this.loading) return;
      this.loading = true;
      this.error = "";
      try {
        const result = await listSystemMessages(uni.getStorageSync("userId"), {
          role: "BOSS",
          page: 0,
          size: 100,
        });
        const rows = Array.isArray(result)
          ? result
          : result?.records || result?.content || result?.list || [];
        this.notices = rows.map((item) => this.normalizeNotice(item));
      } catch (error) {
        this.notices = [];
        this.error = error?.message || "通知加载失败";
      } finally {
        this.loading = false;
      }
    },
    normalizeNotice(item) {
      const tag = this.resolveTag(item);
      return {
        id: item.id || `${item.publishTime || item.createTime}-${item.title || "notice"}`,
        time: this.formatTime(item.publishTime || item.createTime || item.createdAt),
        tag,
        tagClass:
          tag === "结算" || tag === "活动"
            ? "tag-success"
            : tag === "提醒"
              ? "tag-warning"
              : "tag-system",
        content: item.title
          ? `${item.title}${item.content ? `：${item.content}` : ""}`
          : item.content || "",
      };
    },
    resolveTag(item) {
      const raw = String(item.type || item.category || item.bizType || "").toUpperCase();
      if (raw.includes("SETTLE") || raw.includes("WALLET") || raw.includes("结算")) return "结算";
      if (raw.includes("ACTIVITY") || raw.includes("REWARD") || raw.includes("活动")) return "活动";
      if (raw.includes("REMIND") || raw.includes("APPLY") || raw.includes("提醒")) return "提醒";
      return "系统";
    },
    formatTime(value) {
      return value ? String(value).replace("T", " ").slice(0, 16) : "";
    },
    goBack() {
      const pages = getCurrentPages();
      if (pages.length > 1) uni.navigateBack();
      else uni.switchTab({ url: "/pages/boss/profile" });
    },
  },
};
</script>

<style scoped>
.page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #f5f5f5;
}
.top-nav {
  flex-shrink: 0;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
}
.nav-inner {
  position: relative;
  height: 100rpx;
  padding: 0 32rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-sizing: border-box;
}
.nav-back,
.nav-placeholder {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.nav-back image {
  width: 36rpx;
  height: 36rpx;
}
.nav-title {
  position: absolute;
  left: 50%;
  color: #333;
  font-size: 34rpx;
  font-weight: 600;
  transform: translateX(-50%);
}
.scroll-area {
  flex: 1;
  min-height: 0;
  box-sizing: border-box;
}
.notice-item {
  margin: 24rpx 32rpx;
  padding: 32rpx;
  border-radius: 24rpx;
  background: #fff;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}
.notice-time {
  display: block;
  margin-bottom: 16rpx;
  color: #999;
  font-size: 24rpx;
}
.notice-content {
  display: flex;
  align-items: flex-start;
}
.notice-text {
  flex: 1;
  min-width: 0;
  color: #333;
  font-size: 28rpx;
  line-height: 1.65;
}
.notice-tag {
  flex-shrink: 0;
  margin: 3rpx 12rpx 0 0;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
  line-height: 1.5;
}
.tag-system {
  color: #ff6b35;
  background: #fff3ed;
}
.tag-warning {
  color: #fa8c16;
  background: #fff8e6;
}
.tag-success {
  color: #52c41a;
  background: #f6ffed;
}
.state {
  padding: 180rpx 32rpx;
  color: #999;
  font-size: 28rpx;
  text-align: center;
}
.state.error {
  color: #ff6b35;
}
.bottom-space {
  height: 30rpx;
}
</style>
