<template>
  <view class="page">
    <view class="nav" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="nav-inner">
        <view class="back" @click="goBack"><text>‹</text></view>
        <text class="nav-title">历史消息</text>
        <view class="nav-placeholder" />
      </view>
    </view>

    <view class="filters">
      <view
        v-for="item in filters"
        :key="item.key"
        :class="['filter', { active: readFilter === item.key }]"
        @click="changeFilter(item.key)"
        >{{ item.label }}</view
      >
    </view>

    <scroll-view
      scroll-y
      class="content"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="refresh"
      @scrolltolower="loadMore"
    >
      <view v-if="loading && !items.length" class="state">加载中…</view>
      <view v-else-if="loadError" class="state error" @click="load">{{
        loadError
      }}</view>
      <view v-else-if="!items.length" class="state">暂无历史消息</view>
      <view v-else>
        <view
          v-for="item in items"
          :key="item.id"
          class="message-card"
          @click="openMessage(item)"
        >
          <view class="message-head">
            <view :class="['message-icon', item.kind]"
              ><image :src="item.icon" mode="aspectFit"
            /></view>
            <view class="message-main">
              <text class="message-title">{{
                item.title || item.type || "消息"
              }}</text>
              <text class="message-time">{{
                formatTime(item.createTime)
              }}</text>
            </view>
            <text v-if="!isRead(item)" class="unread-label">未读</text>
          </view>
          <text v-if="item.content" class="message-content">{{
            item.content
          }}</text>
        </view>
        <view v-if="loading" class="loading-more">加载中…</view>
        <view v-else-if="!hasMore" class="loading-more">没有更多消息</view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { listBossMessageHistory, readMessage } from "@/api/backend";
import bellIcon from "/static/icons/worker-messages/bell-orange.svg";
import checkIcon from "/static/icons/worker-messages/check-green.svg";
import warningIcon from "/static/icons/worker-messages/exclamation-orange.svg";

export default {
  data() {
    return {
      statusBarHeight: 0,
      filters: [
        { key: "all", label: "全部" },
        { key: "unread", label: "未读" },
        { key: "ORDER_APPLY", label: "报名" },
        { key: "SYSTEM_NOTICE", label: "系统" },
      ],
      readFilter: "all",
      items: [],
      page: 0,
      size: 20,
      total: 0,
      loading: false,
      refreshing: false,
      loadError: "",
    };
  },
  computed: {
    hasMore() {
      return this.items.length < this.total;
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
    this.load();
  },
  methods: {
    async load(reset = true) {
      if (this.loading) return;
      if (reset) {
        this.page = 0;
        this.items = [];
      }
      this.loading = true;
      this.loadError = "";
      try {
        const params = { page: this.page, size: this.size };
        if (this.readFilter === "unread") params.read = false;
        else if (this.readFilter !== "all") params.type = this.readFilter;
        const result = await listBossMessageHistory(params);
        const rows = Array.isArray(result?.data)
          ? result.data
          : Array.isArray(result)
            ? result
            : [];
        this.items = reset
          ? rows.map(normalizeMessage)
          : this.items.concat(rows.map(normalizeMessage));
        this.total = Number(result?.total ?? this.items.length);
      } catch (error) {
        this.loadError = error?.message || "历史消息加载失败";
      } finally {
        this.loading = false;
        this.refreshing = false;
      }
    },
    changeFilter(value) {
      this.readFilter = value;
      this.load();
    },
    refresh() {
      this.refreshing = true;
      this.load();
    },
    loadMore() {
      if (this.hasMore && !this.loading) {
        this.page += 1;
        this.load(false);
      }
    },
    async openMessage(item) {
      if (!item?.id || isRead(item)) return;
      try {
        await readMessage(item.id, uni.getStorageSync("userId"));
        item.readFlag = true;
      } catch (error) {
        uni.showToast({
          title: error?.message || "消息标记已读失败",
          icon: "none",
        });
      }
    },
    goBack() {
      uni.navigateBack();
    },
    isRead,
    formatTime,
  },
};

function isRead(item) {
  return (
    item?.readFlag === true ||
    item?.readFlag === 1 ||
    item?.readFlag === "已读" ||
    item?.readFlag === "READ"
  );
}
function formatTime(value) {
  if (!value) return "";
  return String(value).replace("T", " ").slice(0, 16);
}
function normalizeMessage(item = {}) {
  const source =
    `${item.type || ""} ${item.bizType || ""} ${item.title || ""}`.toLowerCase();
  const kind =
    source.includes("apply") || source.includes("报名")
      ? "apply"
      : source.includes("warn") || source.includes("start")
        ? "warn"
        : "system";
  return {
    ...item,
    kind,
    icon:
      kind === "apply" ? checkIcon : kind === "warn" ? warningIcon : bellIcon,
  };
}
</script>

<style scoped>
.page {
  height: 100vh;
  overflow: hidden;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}
.nav,
.filters {
  flex-shrink: 0;
  background: #fff;
}
.nav {
  border-bottom: 1rpx solid #f0f0f0;
}
.nav-inner {
  height: 100rpx;
  padding: 0 32rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-sizing: border-box;
}
.back,
.nav-placeholder {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.back {
  color: #333;
  font-size: 48rpx;
}
.nav-title {
  color: #333;
  font-size: 34rpx;
  font-weight: 600;
}
.filters {
  display: flex;
  padding: 0 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
}
.filter {
  flex: 1;
  padding: 24rpx 0 20rpx;
  color: #999;
  font-size: 28rpx;
  text-align: center;
  border-bottom: 4rpx solid transparent;
}
.filter.active {
  color: #ff6b35;
  font-weight: 600;
  border-bottom-color: #ff6b35;
}
.content {
  flex: 1;
  min-height: 0;
  padding: 20rpx 24rpx;
  box-sizing: border-box;
}
.state {
  padding: 100rpx 20rpx;
  color: #999;
  font-size: 28rpx;
  text-align: center;
}
.state.error {
  color: #ff6b35;
}
.message-card {
  margin-bottom: 20rpx;
  padding: 24rpx;
  border-radius: 20rpx;
  background: #fff;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.04);
}
.message-head {
  display: flex;
  align-items: center;
}
.message-icon {
  width: 72rpx;
  height: 72rpx;
  margin-right: 18rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.message-icon image {
  width: 36rpx;
  height: 36rpx;
}
.message-icon.system {
  background: #fff0e6;
}
.message-icon.apply {
  background: #e6f7ec;
}
.message-icon.warn {
  background: #fff7e6;
}
.message-main {
  flex: 1;
  min-width: 0;
}
.message-title {
  display: block;
  color: #333;
  font-size: 30rpx;
  font-weight: 600;
}
.message-time {
  display: block;
  margin-top: 8rpx;
  color: #aaa;
  font-size: 22rpx;
}
.unread-label {
  color: #ff4d4f;
  font-size: 22rpx;
}
.message-content {
  display: block;
  margin-top: 18rpx;
  color: #666;
  font-size: 26rpx;
  line-height: 1.5;
}
.loading-more {
  padding: 24rpx;
  color: #aaa;
  font-size: 24rpx;
  text-align: center;
}
</style>
