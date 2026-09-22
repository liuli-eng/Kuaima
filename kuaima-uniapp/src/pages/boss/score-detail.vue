<template>
  <view class="page">
    <view class="status-spacer" :style="{ height: `${statusBarHeight}px` }" />
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <image src="/static/icons/worker-credit/chevron-left-dark.svg" mode="aspectFit" />
      </view>
      <text class="nav-title">诚意分明细</text>
      <view class="nav-placeholder" />
    </view>

    <view class="summary-card">
      <view class="summary-top">
        <text class="summary-score">{{ score }}</text>
        <text class="summary-label">诚意分</text>
      </view>
      <text class="summary-period">{{ periodText }}</text>
      <view class="summary-tip">
        <image src="/static/icons/worker-credit/circle-info-orange.svg" mode="aspectFit" />
        <text>诚意分越高，雇主星级越高，可享受更多平台权益</text>
      </view>
    </view>

    <view class="filter-tabs">
      <text v-for="tab in filterTabs" :key="tab.value" class="filter-tab" :class="{ active: activeFilter === tab.value }" @click="activeFilter = tab.value">{{ tab.label }}</text>
    </view>

    <scroll-view scroll-y class="log-scroll">
      <view v-for="item in filteredRecords" :key="item.id" class="log-item">
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
      <view v-if="!loading && !filteredRecords.length" class="empty">
        <image src="/static/icons/worker-credit/inbox-gray.svg" mode="aspectFit" />
        <text>暂无明细记录</text>
      </view>
      <view v-if="loading" class="empty"><text>明细加载中...</text></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { getCredit, listCreditFlows } from "@/api/backend";
import checkIcon from "/static/icons/worker-credit/circle-check-white.svg";
import checkDoubleIcon from "/static/icons/worker-credit/check-double-white.svg";
import clockIcon from "/static/icons/worker-credit/clock-white.svg";
import thumbsUpIcon from "/static/icons/worker-credit/thumbs-up-white.svg";
import thumbsDownIcon from "/static/icons/worker-credit/thumbs-down-white.svg";

const statusBarHeight = ref(0);
const score = ref(0);
const records = ref([]);
const activeFilter = ref("all");
const loading = ref(false);
const filterTabs = [
  { label: "全部", value: "all" },
  { label: "加分", value: "add" },
  { label: "减分", value: "sub" },
];

const filteredRecords = computed(() => activeFilter.value === "all" ? records.value : records.value.filter((item) => item.type === activeFilter.value));
const periodText = computed(() => {
  if (!records.value.length) return "近90天总计";
  const dates = records.value.map((item) => item.date).filter(Boolean).sort((a, b) => a - b);
  if (!dates.length) return "近90天总计";
  return `${formatShortDate(dates[0])} ~ ${formatShortDate(dates[dates.length - 1])} 总计`;
});

onLoad(async () => {
  try {
    const info = typeof uni.getWindowInfo === "function" ? uni.getWindowInfo() : uni.getSystemInfoSync();
    statusBarHeight.value = Number(info.statusBarHeight || 0);
  } catch (_) {}
  await loadDetail();
});

async function loadDetail() {
  loading.value = true;
  try {
    const userId = uni.getStorageSync("userId");
    const [summary, flows] = await Promise.all([
      getCredit(userId),
      listCreditFlows(userId, { page: 0, size: 100 }).catch(() => null),
    ]);
    score.value = Number(summary?.score ?? summary?.creditScore ?? summary?.points ?? 0);
    const rows = normalizeRows(flows || summary?.recentFlows || summary?.details || summary?.flows || []);
    records.value = rows.map(normalizeRecord);
  } catch (error) {
    uni.showToast({ title: error?.message || "诚意分明细加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

function normalizeRows(payload) {
  const body = payload?.data ?? payload ?? {};
  if (Array.isArray(body)) return body;
  return body.records || body.content || body.list || [];
}

function normalizeRecord(item, index) {
  const value = Number(item.delta ?? item.value ?? item.amount ?? item.change ?? 0);
  const type = value >= 0 ? "add" : "sub";
  const title = item.reason || item.title || item.description || item.remark || "诚意分变动";
  const orderTitle = item.orderTitle || item.jobTitle || item.detail || item.bizNo || item.orderNo;
  const keywords = [item.bizType, item.ruleCode, title].filter(Boolean).join(" ").toUpperCase();
  const rawTime = item.timestamp || item.time || item.createTime;
  return {
    id: String(item.id ?? `${title}-${rawTime ?? index}`),
    type,
    title,
    desc: orderTitle ? `订单：${orderTitle}` : "平台诚意分规则记录",
    time: formatTime(rawTime),
    date: parseDate(rawTime),
    scoreText: `${value > 0 ? "+" : ""}${value}`,
    icon: chooseIcon(keywords, type),
  };
}

function chooseIcon(keywords, type) {
  if (type === "sub") return thumbsDownIcon;
  if (/REVIEW|好评/.test(keywords)) return thumbsUpIcon;
  if (/SETTLE|结算|TIME|按时/.test(keywords)) return clockIcon;
  if (/COMPLETE|完单|完成/.test(keywords) && /DOUBLE|连续|多单|100%/.test(keywords)) return checkDoubleIcon;
  return checkIcon;
}

function parseDate(value) {
  if (!value) return null;
  const date = value instanceof Date ? value : new Date(String(value).replace(/-/g, "/"));
  return Number.isNaN(date.getTime()) ? null : date;
}

function formatTime(value) {
  const date = parseDate(value);
  if (!date) return value ? String(value) : "";
  const pad = (number) => String(number).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

function formatShortDate(date) {
  return `${date.getMonth() + 1}月${date.getDate()}日`;
}

function goBack() {
  uni.navigateBack({ fail: () => uni.redirectTo({ url: "/pages/boss/creditor-score" }) });
}
</script>

<style scoped>
.page { display: flex; flex-direction: column; height: 100vh; overflow: hidden; background: #f5f6f8; }
.status-spacer, .nav-bar { flex-shrink: 0; background: #fff; }
.nav-bar { position: relative; display: flex; align-items: center; justify-content: space-between; height: 50px; padding: 0 16px; box-sizing: border-box; }
.nav-back, .nav-placeholder { width: 32px; height: 32px; }
.nav-back { display: flex; align-items: center; justify-content: center; }
.nav-back image { width: 10px; height: 16px; }
.nav-title { position: absolute; left: 50%; color: #333; font-size: 17px; font-weight: 700; transform: translateX(-50%); }
.summary-card { flex-shrink: 0; margin: 12px; padding: 18px; border-radius: 14px; background: #fff; box-sizing: border-box; }
.summary-top { display: flex; align-items: baseline; gap: 6px; }
.summary-score { color: #1a1a1a; font-size: 32px; font-weight: 700; }
.summary-label { color: #666; font-size: 14px; }
.summary-period { display: block; margin-top: 6px; color: #999; font-size: 12px; }
.summary-tip { display: flex; align-items: center; gap: 6px; margin-top: 12px; padding: 10px 12px; border-radius: 8px; background: #fff8e6; color: #b8860b; font-size: 12px; }
.summary-tip image { flex-shrink: 0; width: 13px; height: 13px; }
.filter-tabs { display: flex; flex-shrink: 0; gap: 8px; padding: 4px 12px 10px; }
.filter-tab { padding: 6px 14px; border: 1px solid #eee; border-radius: 14px; background: #fff; color: #666; font-size: 13px; }
.filter-tab.active { border-color: #ff6b35; background: #ff6b35; color: #fff; }
.log-scroll { flex: 1; min-height: 0; padding: 0 12px 16px; box-sizing: border-box; }
.log-item { display: flex; align-items: flex-start; gap: 12px; margin-bottom: 10px; padding: 14px; border-radius: 12px; background: #fff; box-sizing: border-box; }
.log-icon { display: flex; align-items: center; justify-content: center; flex-shrink: 0; width: 36px; height: 36px; border-radius: 50%; }
.log-icon.add { background: #52c41a; }
.log-icon.sub { background: #ff4d4f; }
.log-icon image { width: 14px; height: 14px; }
.log-main { flex: 1; min-width: 0; }
.log-title, .log-desc, .log-time { display: block; }
.log-title { color: #1a1a1a; font-size: 14px; font-weight: 500; }
.log-desc { margin-top: 4px; color: #999; font-size: 12px; line-height: 1.5; }
.log-time { margin-top: 4px; color: #bbb; font-size: 11px; }
.log-score { flex-shrink: 0; font-size: 16px; font-weight: 700; }
.log-score.add { color: #52c41a; }
.log-score.sub { color: #ff4d4f; }
.empty { padding: 60px 20px; color: #999; font-size: 14px; text-align: center; }
.empty image { display: block; width: 40px; height: 40px; margin: 0 auto 10px; }
</style>
