<template>
  <view class="page">
    <AppNavBar title="订单" :show-back="true" />
    <view class="notice">ⓘ 订单状态分为：已报名、已录用、已到岗、已完成</view>
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
          @click="status = item.key"
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
          ><text :class="['order-status', order.status]">{{
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
            ><button class="detail" @click="open(order)">查看详情</button
            ><button
              v-if="order.status === 'applied'"
              class="cancel"
              @click="cancel(order)"
            >
              取消报名
            </button>
            <button
              v-if="order.status === 'hired'"
              class="cancel"
              @click="updateStatus(order, 'work')"
            >
              确认到岗
            </button>
            <button
              v-if="order.status === 'arrived'"
              class="cancel"
              @click="updateStatus(order, 'finish')"
            >
              确认完工
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
import { request } from "@/api/http";
const types = [
  { key: "day", label: "日结" },
  { key: "press", label: "压薪日结" },
  { key: "monthly", label: "月结" },
];
const dates = createDateTabs();
const statuses = [
  { key: "all", label: "全部状态" },
  { key: "applied", label: "已报名" },
  { key: "hired", label: "已录用" },
  { key: "arrived", label: "已到岗" },
  { key: "done", label: "已完成" },
];
const orderType = ref("day"),
  date = ref("all"),
  status = ref("all");
const mock = ref([]);
const filtered = computed(() =>
  mock.value.filter(
    (i) =>
      i.group === orderType.value &&
      (date.value === "all" || i.date === date.value) &&
      (status.value === "all" || i.status === status.value),
  ),
);
onMounted(async () => {
  try {
    const r = await request({ url: "/worker/orders?pageNo=1&pageSize=50" });
    const items = Array.isArray(r) ? r : r?.records;
    if (Array.isArray(items)) {
      mock.value = await Promise.all(
        items.map(async (item) => {
          try {
            const order = await request({
              url: `/worker/jobs/${item.orderId}`,
            });
            // 岗位详情提供类型、工资、地点等主数据；报名记录保留自身状态和关联 ID。
            return normalizeOrder({
              ...item,
              ...order,
              id: item.id,
              orderId: item.orderId,
              status: item.status,
              workDate: item.workDate,
              applyDate: item.applyDate,
              hireDate: item.hireDate,
              finishDate: item.finishDate,
            });
          } catch (_) {
            return normalizeOrder(item);
          }
        }),
      );
    }
  } catch (error) {
    mock.value = [];
    uni.showToast({ title: error.message || "订单列表加载失败", icon: "none" });
  }
});
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
      if (confirm) {
        try {
          await request({ url: `/boss/item/${o.id}/cancel`, method: "PUT" });
          o.status = "cancelled";
          o.statusText = "取消报名";
          uni.showToast({ title: "已取消报名", icon: "success" });
        } catch (e) {
          uni.showToast({ title: e.message || "取消失败", icon: "none" });
        }
      }
    },
  });
}
async function updateStatus(order, action) {
  try {
    await request({ url: `/boss/item/${order.id}/${action}`, method: "PUT" });
    order.status = action === "work" ? "arrived" : "done";
    order.statusText = action === "work" ? "已到岗" : "已完成";
    uni.showToast({ title: order.statusText, icon: "success" });
  } catch (e) {
    uni.showToast({ title: e.message || "操作失败", icon: "none" });
  }
}
function normalizeOrder(item) {
  const statusMap = {
    已报名: "applied",
    已录用: "hired",
    已到岗: "arrived",
    已完成: "done",
    取消招工: "cancelled",
    取消报名: "cancelled",
  };
  const hourlyMatch = String(item.tags || "").match(/时薪:([\d.]+)/);
  const pieceMatch = String(item.tags || "").match(/计件单价:([\d.]+)/);
  const pieceUnitMatch = String(item.tags || "").match(/计件单位:([^,]+)/);
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
    status: statusMap[item.status] || "applied",
    statusText: item.status || "已报名",
    date: normalizeDateKey(item.workDate || item.startTime),
  };
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
.detail {
  background: #eb8a59;
  color: #fff;
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
