<template>
  <view class="container">
    <!-- 状态栏 -->
    <view class="status-bar">
      <text>19:53</text>
      <view class="status-icons">
        <text>📶</text>
        <text>📡</text>
        <text>🔋</text>
      </view>
    </view>

    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text>←</text>
      </view>
      <text class="nav-title">系统通知</text>
      <view class="nav-right">
        <text>…</text>
      </view>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <view class="notice-item" v-for="notice in notices" :key="notice.id">
        <text class="notice-time">{{ notice.time }}</text>
        <text class="notice-content">
          <text class="notice-tag" :class="notice.tagClass">{{
            notice.tag
          }}</text>
          {{ notice.content }}
        </text>
      </view>
      <view v-if="loading" class="empty-state">正在加载通知...</view>
      <view v-else-if="!notices.length" class="empty-state">暂无系统通知</view>
    </scroll-view>
  </view>
</template>

<script>
import { listSystemMessages } from "@/api/backend";

export default {
  data() {
    return {
      loading: false,
      notices: [],
    };
  },
  onLoad() {
    this.loadNotices();
  },
  methods: {
    async loadNotices() {
      this.loading = true;
      try {
        const result = await listSystemMessages(
          uni.getStorageSync("userId") || "2001",
        );
        const rows = Array.isArray(result)
          ? result
          : result?.records || result?.content || [];
        this.notices = rows.map((item) => {
          const type = item.type || item.category || "SYSTEM_NOTICE";
          return {
            ...item,
            time: item.createTime || item.sendTime || item.time || "",
            content: item.content || item.description || "",
            tag: this.tagText(type),
            tagClass: this.tagClass(type),
          };
        });
      } catch (error) {
        this.notices = [];
        uni.showToast({
          title: error.message || "系统通知加载失败",
          icon: "none",
        });
      } finally {
        this.loading = false;
      }
    },
    tagText(type) {
      if (String(type).includes("SETTLE")) return "结算";
      if (String(type).includes("APPLY")) return "报名";
      if (String(type).includes("ACTIVITY")) return "活动";
      return "系统";
    },
    tagClass(type) {
      if (String(type).includes("SETTLE") || String(type).includes("ACTIVITY"))
        return "tag-success";
      if (String(type).includes("APPLY") || String(type).includes("REMIND"))
        return "tag-warning";
      return "tag-system";
    },
    goBack() {
      uni.navigateBack();
    },
  },
};
</script>

<style lang="scss" scoped>
.container {
  width: 100%;
  height: 100vh;
  background: #f5f5f5;
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
  background: #fff;
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
}

.nav-back {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
}

.nav-right {
  display: flex;
  gap: 14px;
  color: #333;
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
}

.notice-item {
  background: #fff;
  margin: 12px 16px;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.notice-time {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
  display: block;
}

.notice-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
}

.notice-tag {
  display: inline-block;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  margin-right: 6px;
}

.tag-system {
  background: #fff3ed;
  color: #ff6b35;
}
.tag-warning {
  background: #fff8e6;
  color: #fa8c16;
}
.tag-success {
  background: #f6ffed;
  color: #52c41a;
}
.empty-state {
  padding: 200rpx 0;
  text-align: center;
  color: #999;
  font-size: 28rpx;
}
</style>
