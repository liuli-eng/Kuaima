<template>
  <view class="page">
    <AppNavBar title="订单" :show-back="true" />
    <view class="notice">ⓘ 订单状态分为：工作中、已完成、已取消</view>
    <view class="filter-panel">
      <view class="type-tabs"
        ><text
          v-for="item in types"
          :key="item.key"
          :class="{ active: orderType === item.key }"
          @click="orderType = item.key"
          >{{ item.label }}</text
        ></view
      >
      <scroll-view scroll-x class="date-tabs"
        ><view
          v-for="item in dates"
          :key="item.key"
          :class="['date-tab', { active: date === item.key }]"
          @click="date = item.key"
          ><text>{{ item.label }}</text
          ><text>{{ item.sub }}</text></view
        ></scroll-view
      >
      <scroll-view scroll-x class="status-tabs"
        ><text
          v-for="item in statuses"
          :key="item.key"
          :class="['status-tab', { active: status === item.key }]"
          @click="changeStatus(item.key)"
          >{{ item.label }}</text
        ></scroll-view
      >
    </view>
    <scroll-view scroll-y class="content">
      <view v-for="order in filtered" :key="order.id" class="card">
        <view class="card-head"
          ><view
            ><text :class="['type-badge', order.typeClass]">{{
              order.type
            }}</text
            ><text class="title">{{ order.title }}</text></view
          ><text :class="['order-status', order.statusGroup]">{{
            order.statusText
          }}</text></view
        >
        <view class="tags"
          ><text
            v-for="tag in order.tags"
            :key="tag.text"
            :class="['tag', tag.class]"
            >{{ tag.text }}</text
          ></view
        >
        <view class="info-grid"
          ><text>◷ <text class="label">时间：</text>{{ order.time }}</text
          ><text>⌖ <text class="label">地点：</text>{{ order.address }}</text
          ><text>♟ <text class="label">雇主：</text>{{ order.employer }}</text
          ><text
            >♧ <text class="label">距离：</text>{{ order.distance }}</text
          ></view
        >
        <view class="footer"
          ><text class="wage"
            >¥{{ order.amount }} <text> {{ order.unit }}</text></text
          ><view class="actions"
            ><button
              v-if="order.canCancel"
              class="cancel"
              :disabled="operatingIds.includes(order.id)"
              @click="cancel(order)"
            >
              {{ operatingIds.includes(order.id) ? "取消中" : "取消报名" }}
            </button>
            <button class="detail" @click="open(order)">详情</button>
            <button
              v-if="order.action === 'check-in'"
              class="attendance"
              :disabled="operatingIds.includes(order.id)"
              @click="enterAttendanceCode(order, 'work')"
            >
              输入开工码
            </button>
            <button
              v-if="order.action === 'early-leave'"
              class="attendance"
              :disabled="operatingIds.includes(order.id)"
              @click="enterAttendanceCode(order, 'leave')"
            >
              输入早退码
            </button>
            <button
              v-if="order.action === 'review'"
              class="review"
              @click="reviewEmployer(order)"
            >
              评价老板
            </button></view
          ></view
        >
      </view>
      <view v-if="!filtered.length" class="empty">暂无相关订单</view>
      <view class="slogan"
        ><text>找日结 上快马</text><text>— 真老板 真工价 真日结 —</text></view
      >
    </scroll-view>
    <WorkerTabBar current="orders" />
  </view>
</template>
<script setup>
import { computed, onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import WorkerTabBar from "@/components/WorkerTabBar.vue";
import {
  cancelOrderItem,
  listWorkerOrders,
  workerCheckIn,
  workerEarlyLeave,
} from "@/api/backend";
const types = [
  { key: "day", label: "日结" },
  { key: "press", label: "压薪日结" },
  { key: "monthly", label: "月结" },
];
const dates = createDateTabs();
const statuses = [
  { key: "all", label: "全部状态" },
  { key: "working", label: "工作中" },
  { key: "done", label: "已完成" },
  { key: "cancelled", label: "已取消" },
];
const orderType = ref("day"),
  date = ref("all"),
  status = ref("all");
const mock = ref([]);
const operatingIds = ref([]);
const filtered = computed(() =>
  mock.value.filter(
    (i) =>
      i.group === orderType.value &&
      (date.value === "all" || i.date === date.value) &&
      (status.value === "all" || i.statusGroup === status.value),
  ),
);
onMounted(loadOrders);
async function loadOrders() {
  try {
    const params = { page: 0, size: 50 };
    if (status.value !== "all") {
      params.status =
        status.value === "working"
          ? "工作中"
          : status.value === "done"
            ? "已完成"
            : "已取消";
    }
    const r = await listWorkerOrders(params);
    const items = Array.isArray(r) ? r : r?.records || r?.content || [];
    mock.value = Array.isArray(items) ? items.map(normalizeOrder) : [];
  } catch (error) {
    mock.value = [];
    uni.showToast({ title: error.message || "订单列表加载失败", icon: "none" });
  }
}
async function changeStatus(value) {
  if (status.value === value) return;
  status.value = value;
  await loadOrders();
}
function open(o) {
  const orderId = o.orderId || "";
  uni.navigateTo({
    url: `/pages/worker/order-detail?id=${o.id}&orderId=${orderId}&status=${encodeURIComponent(o.statusText || "")}`,
  });
}
function cancel(o) {
  uni.showModal({
    title: "取消报名",
    content: "确定取消本次报名吗？",
    success: async ({ confirm }) => {
      if (!confirm || operatingIds.value.includes(o.id)) return;
      operatingIds.value.push(o.id);
      try {
        await cancelOrderItem(o.id, "零工主动取消报名");
        uni.showToast({ title: "已取消报名", icon: "success" });
        await loadOrders();
      } catch (e) {
        uni.showToast({ title: e.message || "取消失败", icon: "none" });
      } finally {
        operatingIds.value = operatingIds.value.filter((id) => id !== o.id);
      }
    },
  });
}

function enterAttendanceCode(order, type) {
  const isWork = type === "work";
  const label = isWork ? "开工码" : "早退码";
  uni.showModal({
    title: `输入${label}`,
    editable: true,
    placeholderText: `请输入${label}`,
    success: async ({ confirm, content }) => {
      if (!confirm) return;
      const code = String(content || "").trim();
      if (!code) return uni.showToast({ title: `请输入${label}`, icon: "none" });
      if (operatingIds.value.includes(order.id)) return;
      operatingIds.value.push(order.id);
      try {
        if (isWork) await workerCheckIn(order.orderId, code);
        else await workerEarlyLeave(order.orderId, code);
        uni.showToast({ title: `${label}成功`, icon: "success" });
        await loadOrders();
      } catch (error) {
        uni.showToast({ title: error?.message || `${label}失败`, icon: "none" });
      } finally {
        operatingIds.value = operatingIds.value.filter((id) => id !== order.id);
      }
    },
  });
}

function reviewEmployer() {
  uni.showToast({ title: "评价功能开发中", icon: "none" });
}

function normalizeOrder(item) {
  const hourlyMatch = String(item.tags || "").match(/时薪:([\d.]+)/);
  const pieceMatch = String(item.tags || "").match(/计件单价:([\d.]+)/);
  const pieceUnitMatch = String(item.tags || "").match(/计件单位:([^,]+)/);
  const orderStatus = String(item.status || "");
  return {
    ...item,
    title: item.orderTitle || item.title || `订单 ${item.orderId || item.id}`,
    type:
      item.type === "month"
        ? "月结"
        : item.type === "heldBack"
          ? "压薪日结"
          : "日结",
    typeClass:
      item.type === "month"
        ? "monthly"
        : item.type === "heldBack"
          ? "press"
          : "day",
    group:
      item.type === "month"
        ? "monthly"
        : item.type === "heldBack"
          ? "press"
          : "day",
    tags: [],
    employer: item.bossName || item.companyName || "平台认证雇主",
    time:
      [item.startTime, item.endTime].filter(Boolean).join("-") ||
      item.date ||
      "",
    address: item.address || item.workAddress || "",
    distance: item.distance || "",
    amount:
      hourlyMatch?.[1] ??
      pieceMatch?.[1] ??
      (item.wage ? (Number(item.wage) / 100).toFixed(2) : item.salary || 0),
    unit: hourlyMatch
      ? "元/小时"
      : pieceMatch
        ? `元/${pieceUnitMatch?.[1] || "件"}`
        : item.type === "month"
          ? "元/月"
          : "元/天",
    status: orderStatus,
    statusText: orderStatus || "状态未知",
    statusGroup: getStatusGroup(orderStatus),
    action: getStatusAction(orderStatus),
    canCancel: ["已报名", "已录用"].includes(orderStatus),
    date: normalizeDateKey(item.workDate || item.startTime),
  };
}

function getStatusGroup(status) {
  if (status === "已完成") return "done";
  if (status === "已取消") return "cancelled";
  return "working";
}

function getStatusAction(status) {
  const value = String(status || "");
  if (value === "已录用") return "check-in";
  if (value === "工作中") return "early-leave";
  if (value === "已完成") return "review";
  return "detail";
}

function createDateTabs() {
  const today = startOfDay(new Date());
  return [
    { key: "all", label: "全部", sub: "订单" },
    createDateTab(today, -1, "昨天"),
    createDateTab(today, 0, "今天"),
    createDateTab(today, 1, "明天"),
    createDateTab(today, 2, "后天"),
  ];
}

function createDateTab(base, offset, label) {
  const target = new Date(base);
  target.setDate(target.getDate() + offset);
  return {
    key: toDateKey(target),
    label,
    sub: `${target.getMonth() + 1}/${target.getDate()}`,
  };
}

function normalizeDateKey(value) {
  if (!value) return "";
  const match = String(value).match(/^(\d{4})-(\d{1,2})-(\d{1,2})/);
  if (!match) return "";
  return `${match[1]}-${match[2].padStart(2, "0")}-${match[3].padStart(2, "0")}`;
}

function startOfDay(date) {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}

function toDateKey(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #fff8e6;
}
.notice {
  padding: 20rpx 30rpx;
  background: #fff8e6;
  color: #9a6b2f;
  font-size: 24rpx;
}
.filter-panel {
  padding: 0 24rpx 22rpx;
  background: #fff8e6;
}
.type-tabs,
.status-tabs {
  display: flex;
  gap: 20rpx;
}
.type-tabs text {
  padding: 16rpx 34rpx;
  border-radius: 34rpx;
  color: #666;
  font-size: 28rpx;
}
.type-tabs text.active {
  background: #fff;
  color: #333;
  font-weight: 700;
}
.date-tabs {
  margin-top: 18rpx;
  white-space: nowrap;
}
.date-tab {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 130rpx;
  height: 110rpx;
  margin-right: 14rpx;
  border-radius: 24rpx;
  background: #fff;
  color: #666;
  font-size: 28rpx;
}
.date-tab text + text {
  margin-top: 6rpx;
  font-size: 22rpx;
}
.date-tab.active {
  background: #eb8a59;
  color: #fff;
  box-shadow: 0 10rpx 20rpx rgba(235, 138, 89, 0.22);
}
.status-tabs {
  margin-top: 22rpx;
  overflow-x: auto;
  white-space: nowrap;
}
.status-tab {
  display: inline-block;
  padding: 16rpx 28rpx;
  border-radius: 28rpx;
  color: #666;
  font-size: 26rpx;
}
.status-tab.active {
  background: #fff1eb;
  color: #eb7950;
  font-weight: 700;
}
.content {
  height: calc(100vh - 490rpx);
  padding: 24rpx;
  box-sizing: border-box;
}
.card {
  margin-bottom: 20rpx;
  padding: 28rpx 24rpx;
  border-radius: 28rpx;
  background: #fff;
  box-shadow: 0 8rpx 20rpx rgba(90, 60, 30, 0.06);
}
.card-head,
.footer,
.actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.type-badge {
  display: inline-block;
  margin-right: 14rpx;
  padding: 8rpx 14rpx;
  border-radius: 8rpx;
  color: #eb7950;
  background: #fff1eb;
  font-size: 23rpx;
}
.title {
  color: #333;
  font-size: 34rpx;
  font-weight: 700;
}
.order-status {
  padding: 12rpx 22rpx;
  border-radius: 28rpx;
  font-size: 25rpx;
}
.applied {
  background: #fff3e0;
  color: #ff9800;
}
.hired {
  background: #e3f2fd;
  color: #2196f3;
}
.arrived {
  background: #f3e5f5;
  color: #9c27b0;
}
.done {
  background: #e8f5e9;
  color: #4caf50;
}
.cancelled,
.unknown {
  background: #f5f5f5;
  color: #999;
}
.tags {
  display: flex;
  gap: 12rpx;
  margin: 18rpx 0;
}
.tag {
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
}
.green {
  background: #edf7e9;
  color: #6aaa5b;
}
.blue {
  background: #e7f1ff;
  color: #4d92e8;
}
.orange {
  background: #fff4e1;
  color: #e89b42;
}
.red {
  background: #ffebee;
  color: #f44336;
}
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18rpx;
  color: #444;
  font-size: 23rpx;
}
.info-grid > text:first-letter {
  color: #eb7950;
}
.label {
  color: #999;
}
.footer {
  margin-top: 24rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #eee;
}
.wage {
  color: #eb7950;
  font-size: 38rpx;
  font-weight: 800;
}
.wage text {
  color: #999;
  font-size: 22rpx;
  font-weight: 400;
}
.actions {
  gap: 14rpx;
}
.actions button {
  margin: 0;
  padding: 0 24rpx;
  height: 64rpx;
  line-height: 64rpx;
  border-radius: 34rpx;
  font-size: 23rpx;
}
.actions button[disabled] {
  opacity: 0.55;
}
.detail {
  background: #eb8a59;
  color: #fff;
}
.attendance {
  border: 1rpx solid #eb8a59;
  background: #fff;
  color: #eb7950;
}
.review {
  background: #fff1eb;
  color: #eb7950;
}
.cancel {
  border: 1rpx solid #ddd;
  background: #fff;
  color: #777;
}
.empty {
  padding: 180rpx 0;
  text-align: center;
  color: #aaa;
  font-size: 28rpx;
}
.slogan {
  padding: 50rpx 0;
  text-align: center;
  color: #ccc;
}
.slogan text {
  display: block;
}
.slogan text:first-child {
  font-size: 38rpx;
  font-weight: 800;
  letter-spacing: 6rpx;
}
.slogan text + text {
  margin-top: 12rpx;
  font-size: 22rpx;
}
</style>
