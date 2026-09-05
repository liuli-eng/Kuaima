<template>
  <view class="page">
    <AppNavBar title="招工详情" :show-back="true" />
    <scroll-view scroll-y class="content">
      <view v-if="loading" class="state">详情加载中...</view>
      <view v-else-if="error" class="state error" @click="loadDetail"
        >加载失败，点击重试</view
      >
      <template v-else>
        <view class="hero">
          <view class="hero-row">
            <text class="title">{{ detail.title || "未命名岗位" }}</text>
            <text class="status">{{ detail.statusText || "待审核" }}</text>
          </view>
          <text class="settlement">{{ detail.settlementLabel }}</text>
          <view class="salary"
            ><text>{{ detail.salaryText }}</text
            ><text>· {{ detail.orderNum || 0 }} 个名额</text></view
          >
        </view>
        <view class="card">
          <text class="section-title">岗位信息</text>
          <view class="row"
            ><text class="label">工作时间</text
            ><text>{{ detail.timeText }}</text></view
          >
          <view class="row"
            ><text class="label">工作地点</text
            ><text>{{ detail.address || "地点待定" }}</text></view
          >
          <view class="row"
            ><text class="label">当前报名</text
            ><text>{{ detail.applyCount }} 人</text></view
          >
          <view class="row"
            ><text class="label">发布时间</text
            ><text>{{ detail.createTime || "--" }}</text></view
          >
        </view>
        <view class="card">
          <text class="section-title">岗位描述</text>
          <text class="description">{{
            detail.orderContent || "暂无岗位描述"
          }}</text>
        </view>
        <view class="card">
          <text class="section-title">报名须知</text>
          <text class="description">{{
            detail.orderRemark ||
            "请按岗位要求准时到岗，具体安排以雇主通知为准。"
          }}</text>
        </view>
      </template>
    </scroll-view>
  </view>
</template>

<script setup>
import { onLoad } from "@dcloudio/uni-app";
import { ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import { getOrder, listOrderItems } from "@/api/backend";

const detail = ref({});
const loading = ref(false);
const error = ref(false);
let orderId = "";

onLoad((options = {}) => {
  orderId = options.id || "";
  loadDetail();
});

async function loadDetail() {
  if (!orderId) return;
  loading.value = true;
  error.value = false;
  try {
    const [order, items] = await Promise.all([
      getOrder(orderId),
      listOrderItems(orderId).catch(() => []),
    ]);
    const row = order || {};
    const salary = Number(row.salary || 0);
    const statusMap = {
      待审核: "待审核",
      审核拒绝: "审核拒绝",
      招工中: "招工中",
      招工结束: "招工结束",
      待结算: "待结算",
      已完成: "已完成",
      取消招工: "已取消",
    };
    detail.value = {
      ...row,
      title: row.orderTitle || row.postion || row.title,
      statusText: statusMap[row.orderStatus] || row.orderStatus,
      settlementLabel:
        { DAY: "每天日结", PRESS: "压薪日结", MONTHLY: "月结" }[
          row.salaryType
        ] ||
        row.settlementType ||
        "日结",
      salaryText: `${salary}${
        row.salaryType === "HOURLY"
          ? "元/小时"
          : row.salaryType === "MONTHLY"
            ? "元/月"
            : "元/天"
      }`,
      timeText: `${formatTime(row.startTime)} ~ ${formatTime(row.endTime)}`,
      applyCount: Array.isArray(items)
        ? items.filter(
            (item) => !["取消报名", "取消招工"].includes(item.status),
          ).length
        : 0,
      orderContent: row.orderContent || row.content || "",
      orderRemark: row.orderRemark || row.remark || "",
      createTime: formatDate(row.createTime || row.createdAt),
    };
  } catch (e) {
    error.value = true;
    uni.showToast({ title: e.message || "招工详情加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

function formatTime(value) {
  if (!value) return "--";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value).slice(0, 16);
  return `${String(date.getHours()).padStart(2, "0")}:${String(
    date.getMinutes(),
  ).padStart(2, "0")}`;
}

function formatDate(value) {
  return value ? String(value).replace("T", " ").slice(0, 16) : "--";
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #fff8e6;
}
.content {
  height: calc(100vh - 176rpx);
  padding: 24rpx;
  box-sizing: border-box;
}
.hero {
  padding: 30rpx;
  border-radius: 24rpx;
  background: linear-gradient(135deg, #ffd59e, #ff9a62);
  color: #fff;
}
.hero-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
}
.title {
  font-size: 38rpx;
  font-weight: 800;
}
.status {
  padding: 8rpx 18rpx;
  border-radius: 24rpx;
  background: #fff;
  color: #ff6b35;
  font-size: 22rpx;
}
.settlement {
  display: block;
  margin-top: 18rpx;
  font-size: 24rpx;
  opacity: 0.9;
}
.salary {
  display: flex;
  gap: 14rpx;
  margin-top: 12rpx;
  font-size: 27rpx;
  font-weight: 700;
}
.card {
  margin-top: 20rpx;
  padding: 28rpx;
  border-radius: 20rpx;
  background: #fff;
  box-shadow: 0 6rpx 18rpx rgba(88, 64, 32, 0.06);
}
.section-title {
  display: block;
  margin-bottom: 18rpx;
  color: #333;
  font-size: 29rpx;
  font-weight: 700;
}
.row {
  display: flex;
  justify-content: space-between;
  gap: 24rpx;
  padding: 18rpx 0;
  border-bottom: 1rpx solid #f4efe8;
  color: #444;
  font-size: 25rpx;
}
.row:last-child {
  border-bottom: 0;
}
.label {
  color: #999;
}
.description {
  display: block;
  color: #666;
  font-size: 25rpx;
  line-height: 1.9;
  white-space: pre-wrap;
}
.state {
  padding: 220rpx 0;
  text-align: center;
  color: #999;
  font-size: 25rpx;
}
.error {
  color: #ff6b35;
}
</style>
