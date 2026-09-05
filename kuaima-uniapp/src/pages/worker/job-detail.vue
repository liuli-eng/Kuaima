<template>
  <view class="page">
    <AppNavBar title="岗位详情" :show-back="true" />
    <scroll-view scroll-y class="content">
      <view v-if="loading" class="page-state">岗位详情加载中…</view>
      <view v-else-if="loadError" class="page-state error" @click="loadDetail"
        >加载失败，点击重试</view
      >
      <template v-else>
        <view class="salary-header"
          ><view class="title-row"
            ><text class="title">{{ job.title }}</text
            ><text class="favorite" @click="toggleFavorite">{{
              favorite ? "♥" : "♡"
            }}</text></view
          ><view class="tags"
            ><text class="tag">{{ settlementLabel }}</text
            ><text v-for="tag in jobTags" :key="tag" class="tag">{{
              tag
            }}</text></view
          ><view class="salary-info"
            ><view
              ><text>预计时长</text
              ><text class="value">{{ job.duration }}小时</text></view
            ><view
              ><text>预计收入</text
              ><text class="value dark"
                >{{ job.unitPrice }}{{ wageUnit }}</text
              ></view
            ></view
          ></view
        >
        <view class="info-card"
          ><view class="info-row"
            ><text class="icon orange">◷</text
            ><view class="info-main"
              ><text class="label">任务时间</text
              ><text class="info-value"
                >{{ formatDateTime(job.startTime) }} -
                {{ formatDateTime(job.endTime) }}</text
              ></view
            ></view
          ><view class="info-row"
            ><text class="icon blue">⌖</text
            ><view class="info-main"
              ><text class="label">任务地点</text
              ><text class="info-value">{{ job.address || "地点待定" }}</text
              ><text class="muted">详细地址以岗位发布信息为准</text></view
            ><text class="route" @click="viewRoute">查看路线 ›</text></view
          ></view
        >
        <view class="tabs"
          ><text
            v-for="tab in tabs"
            :key="tab.key"
            :class="['tab', { active: activeTab === tab.key }]"
            @click="activeTab = tab.key"
            >{{ tab.label }}</text
          ></view
        >
        <view class="desc-card"
          ><view class="desc-title"
            ><text class="desc-icon">▤</text
            >{{ tabs.find((i) => i.key === activeTab)?.label }}</view
          ><text v-if="activeTab === 'desc'" class="desc">{{
            job.orderContent || "雇主暂未填写岗位描述"
          }}</text
          ><view v-else-if="activeTab === 'notice'" class="list"
            ><text v-if="job.orderRemark">{{ job.orderRemark }}</text
            ><text v-if="job.tags">岗位标签：{{ job.tags }}</text
            ><text>报名成功后请关注雇主审核结果</text></view
          ><view v-else class="list"
            ><text>发布者编号：{{ job.createBy || "--" }}</text
            ><text>岗位状态：{{ job.orderStatus || "--" }}</text
            ><text>发布时间：{{ job.date || "--" }}</text></view
          ></view
        >
        <view v-if="!isRealname" class="realname"
          ><text>完成实名认证后才能报名</text
          ><text @click="goRealname">去认证 ›</text></view
        >
      </template>
    </scroll-view>
    <SafeBottomAction
      ><view class="bottom-bar"
        ><view class="bottom-link" @click="uni.navigateBack()"
          ><text>⌂</text><text>首页</text></view
        ><view class="bottom-link" @click="share"
          ><text>♣</text><text>分享</text></view
        ><button class="enroll" :disabled="applying || applied" @click="apply">
          <text>{{
            applied ? "已报名" : applying ? "报名中…" : "电话报名"
          }}</text
          ><text>剩{{ remainingCount }}个名额</text>
        </button></view
      ></SafeBottomAction
    >
  </view>
</template>
<script setup>
import { computed, ref } from "vue";
import { onLoad, onShow } from "@dcloudio/uni-app";
import AppNavBar from "@/components/AppNavBar.vue";
import SafeBottomAction from "@/components/SafeBottomAction.vue";
import { request } from "@/api/http";
import {
  checkFavoriteJob,
  favoriteJob,
  getCertificationStatus,
  getOrder,
  listOrderItems,
  recordJobBrowse,
  unfavoriteJob,
} from "@/api/backend";
const tabs = [
  { key: "desc", label: "岗位描述" },
  { key: "notice", label: "报名须知" },
  { key: "boss", label: "雇主信息" },
];
const activeTab = ref("desc");
const applying = ref(false);
const applied = ref(false);
const favorite = ref(false);
const favoriteId = ref("");
const loading = ref(false);
const loadError = ref(false);
const appliedCount = ref(0);
const isRealname = ref(uni.getStorageSync("workerRealname") === true);
const job = ref({});
const wageUnit = computed(() =>
  job.value.wageUnit
    ? job.value.wageUnit
    : job.value.salaryType === "HOURLY"
      ? "元/小时"
      : job.value.salaryType === "MONTHLY"
        ? "元/月"
        : "元/天",
);
const settlementLabel = computed(
  () =>
    ({ DAY: "每天日结", PRESS: "压薪日结", MONTHLY: "月结" })[
      job.value.salaryType
    ] || "日结",
);
const jobTags = computed(() =>
  String(job.value.tags || "")
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean),
);
const remainingCount = computed(() =>
  Math.max(0, Number(job.value.orderNum || 0) - appliedCount.value),
);
onShow(async () => {
  isRealname.value =
    uni.getStorageSync("workerCertStatus") === "已通过" ||
    uni.getStorageSync("workerRealname") === true;
  try {
    const result = await getCertificationStatus();
    const status = result?.status || result?.certStatus || "未认证";
    uni.setStorageSync("workerCertStatus", status);
    isRealname.value = status === "已通过";
    if (isRealname.value) uni.setStorageSync("workerRealname", true);
  } catch (_) {}
});
onLoad(async (options = {}) => {
  const id = options.id;
  if (!id) {
    loadError.value = true;
    return;
  }
  job.value = { id };
  await loadDetail();
  const userId = uni.getStorageSync("userId") || "2001";
  try {
    const result = await checkFavoriteJob(userId, id);
    favorite.value = result?.favorited === true;
    favoriteId.value = result?.favoriteId || result?.id || "";
  } catch (_) {}
  recordJobBrowse({ userId, orderId: id }).catch(() => {});
});
async function loadDetail() {
  const id = job.value.id;
  if (!id) return;
  loading.value = true;
  loadError.value = false;
  try {
    const [detail, items] = await Promise.all([
      getOrder(id),
      listOrderItems(id).catch(() => []),
    ]);
    if (detail) job.value = normalizeJob(detail);
    appliedCount.value = Array.isArray(items)
      ? items.filter((item) => !["取消报名", "取消招工"].includes(item.status))
          .length
      : 0;
  } catch (error) {
    loadError.value = true;
    uni.showToast({ title: error.message || "岗位详情加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}
function formatDateTime(value) {
  if (!value) return "时间待定";
  return String(value).replace("T", " ").slice(0, 16);
}
function goRealname() {
  uni.navigateTo({ url: "/pages/worker/realname" });
}
function share() {
  uni.showToast({ title: "任务分享功能开发中", icon: "none" });
}
function viewRoute() {
  uni.showToast({ title: "路线功能开发中", icon: "none" });
}
async function toggleFavorite() {
  const userId = uni.getStorageSync("userId") || "2001";
  try {
    if (favorite.value) {
      if (favoriteId.value) await unfavoriteJob(favoriteId.value);
      favorite.value = false;
      favoriteId.value = "";
    } else {
      const result = await favoriteJob({ userId, orderId: job.value.id });
      favorite.value = true;
      favoriteId.value = result?.id || result?.favoriteId || "";
    }
    uni.showToast({
      title: favorite.value ? "已收藏" : "已取消收藏",
      icon: "none",
    });
  } catch (error) {
    uni.showToast({ title: error.message || "收藏操作失败", icon: "none" });
  }
}
async function apply() {
  if (!isRealname.value) return goRealname();
  applying.value = true;
  try {
    await request({
      url: `/worker/orders/apply/${job.value.id}`,
      method: "POST",
      data: { trial: job.value.salaryType === "MONTHLY" ? true : undefined },
    });
    applied.value = true;
    uni.showToast({ title: "报名成功", icon: "success" });
  } finally {
    applying.value = false;
  }
}
function normalizeJob(item) {
  const typeMap = { daily: "DAY", heldBack: "PRESS", month: "MONTHLY" };
  const hourlyMatch = String(item.tags || "").match(/时薪:([\d.]+)/);
  const pieceMatch = String(item.tags || "").match(/计件单价:([\d.]+)/);
  const pieceUnitMatch = String(item.tags || "").match(/计件单位:([^,]+)/);
  const salaryType =
    item.salaryType || item.settlementType || typeMap[item.type] || "DAY";
  return {
    ...item,
    title: item.title || item.orderTitle || item.postion || "未命名岗位",
    salaryType,
    unitPrice:
      hourlyMatch?.[1] ??
      pieceMatch?.[1] ??
      item.unitPrice ??
      item.wage ??
      item.salary ??
      item.monthSalary ??
      0,
    wageUnit: hourlyMatch
      ? "元/小时"
      : pieceMatch
        ? `元/${pieceUnitMatch?.[1] || "件"}`
        : item.wageUnit,
    address: item.address || item.workAddress || item.location || "地点待定",
    duration: item.duration ?? item.workHours ?? 0,
  };
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}
.content {
  height: calc(100vh - 176rpx);
  padding-bottom: 40rpx;
  box-sizing: border-box;
}
.page-state {
  padding: 160rpx 32rpx;
  color: #999;
  font-size: 26rpx;
  text-align: center;
}
.page-state.error {
  color: #e34d59;
}
.salary-header {
  padding: 30rpx 24rpx 24rpx;
  background: linear-gradient(135deg, #fff0d6, #ffe5b4);
}
.title-row,
.salary-info,
.info-row,
.tabs,
.bottom-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.title {
  color: #333;
  font-size: 38rpx;
  font-weight: 800;
}
.favorite {
  color: #ff6b35;
  font-size: 52rpx;
}
.tags {
  display: flex;
  gap: 12rpx;
  margin: 18rpx 0;
}
.tag {
  padding: 8rpx 18rpx;
  border-radius: 20rpx;
  background: #fff;
  color: #777;
  font-size: 22rpx;
}
.salary-info {
  padding: 24rpx;
  border-radius: 18rpx;
  background: #fff;
  text-align: center;
}
.salary-info > view {
  flex: 1;
}
.salary-info text {
  display: block;
  color: #999;
  font-size: 22rpx;
}
.salary-info .value {
  margin-top: 8rpx;
  color: #ff6b35;
  font-size: 34rpx;
  font-weight: 800;
}
.salary-info .dark {
  color: #333;
}
.info-card {
  margin: 16rpx 0 0;
  padding: 22rpx 24rpx;
  background: #fff;
}
.info-row {
  align-items: flex-start;
  padding: 10rpx 0;
}
.icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 58rpx;
  height: 58rpx;
  margin-right: 16rpx;
  border-radius: 16rpx;
  font-size: 30rpx;
}
.orange {
  background: #fff0e6;
  color: #ff6b35;
}
.blue {
  background: #e6f4ff;
  color: #1890ff;
}
.info-main {
  flex: 1;
}
.label,
.info-value,
.muted {
  display: block;
}
.label {
  color: #999;
  font-size: 22rpx;
}
.info-value {
  margin-top: 7rpx;
  color: #333;
  font-size: 28rpx;
  font-weight: 600;
}
.muted {
  margin-top: 6rpx;
  color: #aaa;
  font-size: 20rpx;
}
.route {
  color: #1890ff;
  font-size: 22rpx;
}
.tabs {
  margin-top: 14rpx;
  padding: 0 18rpx;
  background: #fff;
}
.tab {
  position: relative;
  flex: 1;
  padding: 24rpx 0;
  text-align: center;
  color: #666;
  font-size: 27rpx;
}
.tab.active {
  color: #ff6b35;
  font-weight: 700;
}
.tab.active:after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 90rpx;
  height: 5rpx;
  transform: translateX(-50%);
  border-radius: 5rpx;
  background: #ff6b35;
}
.desc-card {
  margin: 18rpx 24rpx;
  padding: 26rpx;
  border-radius: 20rpx;
  background: #fff;
}
.desc-title {
  display: flex;
  align-items: center;
  color: #333;
  font-size: 30rpx;
  font-weight: 700;
}
.desc-icon {
  margin-right: 12rpx;
  color: #ff6b35;
}
.desc {
  display: block;
  margin-top: 22rpx;
  white-space: pre-line;
  color: #666;
  font-size: 26rpx;
  line-height: 1.9;
}
.list text {
  display: block;
  margin-top: 18rpx;
  color: #666;
  font-size: 25rpx;
}
.realname {
  display: flex;
  justify-content: space-between;
  margin: 0 24rpx;
  padding: 18rpx 20rpx;
  border-radius: 14rpx;
  background: #fff0d0;
  color: #9a5b1e;
  font-size: 23rpx;
}
.realname text:last-child {
  color: #1890ff;
}
.bottom-bar {
  gap: 18rpx;
}
.bottom-link {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 70rpx;
  color: #666;
  font-size: 20rpx;
}
.bottom-link text:first-child {
  font-size: 40rpx;
  line-height: 42rpx;
}
.enroll {
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 92rpx;
  margin: 0;
  border: 0;
  border-radius: 48rpx;
  background: linear-gradient(135deg, #ffd666, #f0ad3d);
  color: #8b4513;
}
.enroll text:first-child {
  font-size: 31rpx;
  font-weight: 800;
}
.enroll text:last-child {
  margin-top: 4rpx;
  font-size: 22rpx;
}
.enroll[disabled] {
  opacity: 0.65;
}
</style>
