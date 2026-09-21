<template>
  <view class="page">
    <view class="status-spacer" :style="{ height: `${statusBarHeight}px` }" />
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <image :src="chevronLeftIcon" mode="aspectFit" />
      </view>
      <text class="nav-title">星级分明细</text>
      <view class="nav-placeholder" />
    </view>

    <view class="summary-card">
      <view class="summary-top">
        <text class="summary-score">{{ score }}</text>
        <text class="summary-label">星级分</text>
      </view>
      <text class="summary-period">近30天总计</text>
      <view class="summary-tip">
        <image :src="infoIcon" mode="aspectFit" />
        <text>星级分越高，等级越高，可享受更多平台权益</text>
      </view>
    </view>

    <view class="filter-tabs">
      <text
        v-for="tab in filterTabs"
        :key="tab.value"
        class="filter-tab"
        :class="{ active: activeFilter === tab.value }"
        @click="activeFilter = tab.value"
      >{{ tab.label }}</text>
    </view>

    <scroll-view scroll-y class="log-scroll">
      <view
        v-for="item in filteredRecords"
        :key="item.id"
        class="log-item"
      >
        <view class="log-icon" :class="item.type">
          <image :src="item.icon" mode="aspectFit" />
        </view>
        <view class="log-main">
          <text class="log-title">{{ item.title }}</text>
          <text class="log-desc">{{ item.desc }}</text>
          <text class="log-time">{{ item.time }}</text>
        </view>
        <text class="log-score" :class="item.type">{{ item.scoreText }}</text>
      </view>

      <view v-if="!filteredRecords.length" class="empty">
        <image :src="inboxIcon" mode="aspectFit" />
        <text>暂无明细记录</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { getCredit } from "@/api/backend";
import chevronLeftIcon from "/static/icons/worker-credit/chevron-left-dark.svg";
import infoIcon from "/static/icons/worker-credit/circle-info-orange.svg";
import inboxIcon from "/static/icons/worker-credit/inbox-gray.svg";
import checkIcon from "/static/icons/worker-credit/circle-check-white.svg";
import checkDoubleIcon from "/static/icons/worker-credit/check-double-white.svg";
import clockIcon from "/static/icons/worker-credit/clock-white.svg";
import thumbsUpIcon from "/static/icons/worker-credit/thumbs-up-white.svg";
import thumbsDownIcon from "/static/icons/worker-credit/thumbs-down-white.svg";

const statusBarHeight = ref(0);
const score = ref(0);
const records = ref([]);
const activeFilter = ref("all");
const filterTabs = [
  { label: "全部", value: "all" },
  { label: "加分", value: "add" },
  { label: "减分", value: "sub" },
];

const filteredRecords = computed(() =>
  activeFilter.value === "all"
    ? records.value
    : records.value.filter((item) => item.type === activeFilter.value),
);

onLoad(async () => {
  try {
    const info =
      typeof uni.getWindowInfo === "function"
        ? uni.getWindowInfo()
        : uni.getSystemInfoSync();
    statusBarHeight.value = Number(info.statusBarHeight || 0);
  } catch (_) {}
  await loadCreditDetail();
});

async function loadCreditDetail() {
  try {
    const userId = uni.getStorageSync("userId") || "2001";
    const data = await getCredit(userId);
    const rows = normalizeRows(
      data?.recentFlows || data?.details || data?.flows || [],
    );
    score.value = Number(data?.score ?? data?.starScore ?? data?.points ?? 0);
    records.value = rows.map(normalizeRecord);
  } catch (error) {
    uni.showToast({ title: error?.message || "星级分明细加载失败", icon: "none" });
  }
}

function normalizeRows(payload) {
  if (Array.isArray(payload)) return payload;
  const body = payload?.data ?? payload ?? {};
  return body.records || body.content || body.list || [];
}

function normalizeRecord(item, index) {
  const value = Number(
    item.delta ?? item.value ?? item.amount ?? item.change ?? 0,
  );
  const type = value >= 0 ? "add" : "sub";
  const title =
    item.reason || item.title || item.description || "星级分变动";
  const descSource =
    item.orderTitle ||
    item.jobTitle ||
    item.remark ||
    item.detail ||
    item.bizNo ||
    item.orderNo;
  const keywords = [item.bizType, item.ruleCode, title]
    .filter(Boolean)
    .join(" ")
    .toUpperCase();

  return {
    id: String(item.id ?? `${title}-${item.timestamp ?? index}`),
    type,
    title,
    desc: descSource ? `订单：${descSource}` : "星级分变动",
    time: formatTime(item.timestamp || item.time || item.createTime),
    scoreText: `${value > 0 ? "+" : ""}${value}`,
    icon: chooseIcon(keywords, type),
  };
}

function chooseIcon(keywords, type) {
  if (type === "sub") return thumbsDownIcon;
  if (/REVIEW|好评/.test(keywords)) return thumbsUpIcon;
  if (/SETTLE|结算/.test(keywords)) return clockIcon;
  if (/COMPLETE|完单|完成/.test(keywords) && /DOUBLE|连续|多单/.test(keywords)) {
    return checkDoubleIcon;
  }
  return checkIcon;
}

function formatTime(value) {
  if (!value) return "";
  const date = parseDate(value);
  if (!date) return String(value);
  const pad = (number) => String(number).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

function parseDate(value) {
  if (value instanceof Date) return Number.isNaN(value.getTime()) ? null : value;
  const date = new Date(String(value).replace(/-/g, "/"));
  return Number.isNaN(date.getTime()) ? null : date;
}

function goBack() {
  uni.navigateBack({
    fail: () => uni.redirectTo({ url: "/pages/worker/credit" }),
  });
}
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: #f5f6f8;
}

.status-spacer,
.nav-bar {
  flex-shrink: 0;
  background: #fff;
}

.nav-bar {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100rpx;
  padding: 0 32rpx;
  border-bottom: 1rpx solid #f0f0f0;
  box-sizing: border-box;
}

.nav-back,
.nav-placeholder {
  width: 64rpx;
  height: 64rpx;
}

.nav-back {
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-back image {
  width: 20rpx;
  height: 32rpx;
}

.nav-title {
  position: absolute;
  left: 50%;
  max-width: 60%;
  overflow: hidden;
  color: #333;
  font-size: 34rpx;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
  transform: translateX(-50%);
}

.summary-card {
  flex-shrink: 0;
  margin: 24rpx;
  padding: 36rpx;
  border-radius: 28rpx;
  background: #fff;
  box-sizing: border-box;
}

.summary-top {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
}

.summary-score {
  color: #ff6b35;
  font-size: 64rpx;
  font-weight: 700;
  line-height: 1.1;
}

.summary-label {
  color: #666;
  font-size: 28rpx;
}

.summary-period {
  display: block;
  margin-top: 12rpx;
  color: #999;
  font-size: 24rpx;
}

.summary-tip {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 24rpx;
  padding: 20rpx 24rpx;
  border-radius: 16rpx;
  background: #fff8e6;
  color: #b8860b;
  font-size: 24rpx;
}

.summary-tip image {
  flex-shrink: 0;
  width: 24rpx;
  height: 24rpx;
}

.filter-tabs {
  display: flex;
  flex-shrink: 0;
  gap: 16rpx;
  margin: 0;
  padding: 8rpx 24rpx 20rpx;
}

.filter-tab {
  padding: 12rpx 28rpx;
  border: 1rpx solid #eee;
  border-radius: 28rpx;
  background: #fff;
  color: #666;
  font-size: 26rpx;
}

.filter-tab.active {
  border-color: #ff6b35;
  background: #ff6b35;
  color: #fff;
}

.log-scroll {
  flex: 1;
  min-height: 0;
  padding: 0 24rpx 32rpx;
  box-sizing: border-box;
}

.log-item {
  display: flex;
  align-items: flex-start;
  gap: 24rpx;
  margin-bottom: 20rpx;
  padding: 28rpx;
  border-radius: 24rpx;
  background: #fff;
  box-sizing: border-box;
}

.log-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
}

.log-icon.add {
  background: #52c41a;
}

.log-icon.sub {
  background: #ff4d4f;
}

.log-icon image {
  width: 28rpx;
  height: 28rpx;
}

.log-main {
  flex: 1;
  min-width: 0;
}

.log-title {
  display: block;
  color: #1a1a1a;
  font-size: 28rpx;
  font-weight: 500;
}

.log-desc,
.log-time {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.log-desc {
  margin-top: 8rpx;
  color: #999;
  font-size: 24rpx;
  line-height: 1.5;
}

.log-time {
  margin-top: 8rpx;
  color: #bbb;
  font-size: 22rpx;
}

.log-score {
  flex-shrink: 0;
  color: #333;
  font-size: 32rpx;
  font-weight: 700;
}

.log-score.add {
  color: #52c41a;
}

.log-score.sub {
  color: #ff4d4f;
}

.empty {
  padding: 120rpx 40rpx;
  color: #999;
  font-size: 28rpx;
  text-align: center;
}

.empty image {
  display: block;
  width: 80rpx;
  height: 80rpx;
  margin: 0 auto 20rpx;
}
</style>
