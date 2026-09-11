<template>
  <view class="page">
    <view class="top-nav" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="nav-inner">
        <button class="nav-back" @click="goBack">
          <image :src="chevronLeftGrayIcon" mode="aspectFit" class="nav-back-icon" />
        </button>
        <text class="nav-title">任务详情</text>
        <view class="nav-space" />
      </view>
    </view>
    <scroll-view scroll-y class="content">
      <view v-if="loading" class="page-state">任务详情加载中…</view>
      <view v-else-if="loadError" class="page-state error" @click="loadDetail">加载失败，点击重试</view>
      <template v-else>
        <!-- 头部工资区域 -->
        <view class="salary-header">
          <view class="salary-title">{{ job.title }}</view>
          <view class="tags">
            <text class="tag">{{ settlementLabel }}</text>
            <text v-for="tag in jobTags" :key="tag" class="tag">{{ tag }}</text>
          </view>
          <view class="salary-info">
            <view class="salary-item">
              <text class="salary-label">预计时长</text>
              <text class="salary-value">{{ job.duration }}小时</text>
            </view>
            <view class="salary-item">
              <text class="salary-label">预计日收入</text>
              <text class="salary-value salary-value-gray">{{ job.unitPrice }}{{ wageUnit }}</text>
            </view>
          </view>
        </view>

        <!-- 任务信息卡片：任务时间 -->
        <view class="info-card">
          <view class="info-row">
            <view class="info-icon icon-time">
              <image :src="clockOrangeIcon" mode="aspectFit" class="info-icon-img" />
            </view>
            <view class="info-content">
              <view class="info-label">任务时间</view>
              <view class="info-value">{{ formatJobTimeRange(job.startTime, job.endTime) }}</view>
            </view>
          </view>
          <view v-if="isTimeOutdated" class="outdated-tip-row">
            <text class="outdated-tag">已过时 {{ outdatedDuration }}</text>
          </view>
        </view>

        <!-- 任务信息卡片：任务地点 -->
        <view class="info-card">
          <view class="info-row">
            <view class="info-icon icon-location">
              <image :src="mapMarkerBlueIcon" mode="aspectFit" class="info-icon-img" />
            </view>
            <view class="info-content">
              <view class="info-label">任务地点</view>
              <view class="info-value">{{ job.address || "地点待定" }}</view>
              <view class="distance-text">距当前位置直线{{ distanceKm }}公里</view>
            </view>
            <view class="info-link" @click="viewRoute">
              <text>查看路线</text>
              <image :src="chevronRightBlueIcon" mode="aspectFit" class="link-chevron" />
            </view>
          </view>
          <view class="address-note">
            <text>*接单后可查看详细门牌号</text>
          </view>
        </view>

        <!-- 标签页 -->
        <view class="tabs">
          <view
            v-for="tab in tabs"
            :key="tab.key"
            :class="['tab', { active: activeTab === tab.key }]"
            @click="activeTab = tab.key"
          >{{ tab.label }}</view>
        </view>

        <!-- 任务描述内容 -->
        <view v-if="activeTab === 'desc'" class="desc-content">
          <view class="desc-title">
            <image :src="fileAltOrangeIcon" mode="aspectFit" class="desc-title-icon" />
            <text>任务描述</text>
          </view>
          <view class="desc-text">
            <text>{{ job.orderContent || "雇主暂未填写岗位描述" }}</text>
          </view>
        </view>

        <!-- 报名须知 -->
        <view v-else-if="activeTab === 'notice'" class="desc-content">
          <view class="desc-title">
            <image :src="exclamationCircleBlueIcon" mode="aspectFit" class="desc-title-icon" />
            <text>报名须知</text>
          </view>
          <view class="desc-text">
            <view class="desc-list">
              <view v-for="(item, idx) in noticeList" :key="idx" class="desc-list-item"><text>{{ item }}</text></view>
            </view>
          </view>
        </view>

        <!-- 雇主信息 -->
        <view v-else class="desc-content">
          <view class="desc-title">
            <image :src="userGreenIcon" mode="aspectFit" class="desc-title-icon" />
            <text>雇主信息</text>
          </view>
          <view class="desc-text">
            <text>雇主名称：{{ job.employerName || job.createBy || "未发布" }}</text>
            <text class="desc-row">完成订单：{{ job.orderCompletedCount || job.completedCount || "--" }}单</text>
          </view>
        </view>

        <!-- 实名提示条 -->
        <view v-if="!isRealname" class="realname-banner">
          <view class="realname-text"><text>完成</text><strong>实名认证</strong><text>后才能报名</text></view>
          <view class="realname-btn" @click="goRealname">
            <text>实名认证</text>
            <image :src="chevronRightOrangeIcon" mode="aspectFit" class="btn-chevron" />
          </view>
        </view>

      </template>
    </scroll-view>
    <view class="safe-bottom-action" :style="{ paddingBottom: `${bottomInset}px` }">
      <view class="bottom-bar">
        <view class="bottom-action" @click="goHome">
          <image :src="houseGrayIcon" mode="aspectFit" class="bottom-icon" />
          <text class="bottom-label">首页</text>
        </view>
        <view class="bottom-action" @click="share">
          <image :src="shareAltGrayIcon" mode="aspectFit" class="bottom-icon" />
          <text class="bottom-label">分享</text>
        </view>
        <button class="enroll-btn" :disabled="applying || applied || !isRealname" @click="apply">
          <view class="enroll-info">
            <view class="enroll-label">{{ applied ? "已报名" : applying ? "报名中…" : "电话报名" }}</view>
            <view class="enroll-count">剩{{ remainingCount }}个名额</view>
          </view>
        </button>
      </view>
    </view>
  </view>
</template>
<script setup>
import { computed, ref } from "vue";
import { onLoad, onShow } from "@dcloudio/uni-app";
import {
  checkFavoriteJob,
  favoriteJob,
  getCertificationStatus,
  getPublicJob,
  applyPublicJob,
  recordJobBrowse,
  unfavoriteJob,
} from "@/api/backend";
import clockOrangeIcon from "/static/icons/worker-job-detail/clock-orange.svg";
import mapMarkerBlueIcon from "/static/icons/worker-job-detail/map-marker-alt-blue.svg";
import fileAltOrangeIcon from "/static/icons/worker-job-detail/file-alt-orange.svg";
import exclamationCircleBlueIcon from "/static/icons/worker-job-detail/exclamation-circle-blue.svg";
import userGreenIcon from "/static/icons/worker-job-detail/user-green.svg";
import houseGrayIcon from "/static/icons/worker-job-detail/house-gray.svg";
import shareAltGrayIcon from "/static/icons/worker-job-detail/share-alt-gray.svg";
import chevronLeftGrayIcon from "/static/icons/worker-job-detail/chevron-left-gray.svg";
import chevronRightBlueIcon from "/static/icons/worker-job-detail/chevron-right-blue.svg";
import chevronRightOrangeIcon from "/static/icons/worker-job-detail/chevron-right-orange.svg";

const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0;
const bottomInset = uni.getSystemInfoSync().safeAreaInsets?.bottom || 0;
const tabs = [
  { key: "desc", label: "任务描述" },
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
const distanceKm = computed(() => {
  const n = Number(job.value.distanceKm ?? job.value.distance ?? 0);
  return n > 0 ? n.toFixed(1) : "0";
});
const isTimeOutdated = computed(() => {
  if (!job.value.endTime) return false;
  const t = new Date(String(job.value.endTime).replace("Z","")).getTime();
  if (!t) return false;
  return Date.now() > t;
});
const outdatedDuration = computed(() => {
  if (!isTimeOutdated.value || !job.value.endTime) return "";
  const diff = Date.now() - new Date(String(job.value.endTime).replace("Z","")).getTime();
  if (diff <= 0) return "";
  const mins = Math.floor(diff / 60000);
  const hours = Math.floor(mins / 60);
  const remainMins = mins % 60;
  if (hours < 1) return `${mins}分钟`;
  if (remainMins === 0) return `${hours}小时`;
  return `${hours}小时${remainMins}分`;
});
const noticeList = computed(() => {
  const list = [];
  if (job.value.orderRemark) {
    const parts = String(job.value.orderRemark).split(/[\n;；]/).map(s => s.trim()).filter(Boolean);
    if (parts.length > 0) list.push(...parts);
  }
  list.push("需年满18周岁，身体健康");
  list.push("需实名认证通过");
  list.push("工作时间内不可擅自离岗");
  list.push("完成工作内容后方可结算薪资");
  list.push("如遇问题请及时联系雇主或客服");
  return list;
});
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
    .filter(Boolean)
    .filter(tag => !tag.startsWith("时薪:") && !tag.startsWith("计件") && !tag.startsWith("月薪:") && !tag.startsWith("日薪:")),
);
const remainingCount = computed(() =>
  Math.max(0, Number(job.value.orderNum || 0) - appliedCount.value),
);
onShow(async () => {
  isRealname.value =
    ["已通过", "通过", "已认证"].includes(
      uni.getStorageSync("workerCertStatus"),
    ) || uni.getStorageSync("workerRealname") === true;
  try {
    const result = await getCertificationStatus();
    const status = result?.certStatus || result?.status || "未认证";
    uni.setStorageSync("workerCertStatus", status);
    isRealname.value = ["已通过", "通过", "已认证"].includes(status);
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
    const detail = await getPublicJob(id);
    if (detail) job.value = normalizeJob(detail);
    appliedCount.value = Number(detail?.currentApplyCount ?? detail?.applyCount ?? 0);
  } catch (error) {
    loadError.value = true;
    uni.showToast({ title: error.message || "岗位详情加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}
function parseDate(value) {
  if (!value) return null;
  const s = String(value).replace("Z", "").replace("T", " ");
  const d = new Date(s);
  if (isNaN(d.getTime())) return null;
  return d;
}
function pad2(n) { return n < 10 ? `0${n}` : `${n}`; }
function relativeDayLabel(date) {
  const now = new Date();
  const y = date.getFullYear(), m = date.getMonth(), d = date.getDate();
  const ny = now.getFullYear(), nm = now.getMonth(), nd = now.getDate();
  const isSame = (yy, mm, dd) => y === yy && m === mm && d === dd;
  if (isSame(ny, nm, nd)) return "今天";
  const yest = new Date(ny, nm, nd - 1);
  if (isSame(yest.getFullYear(), yest.getMonth(), yest.getDate())) return "昨天";
  const tom = new Date(ny, nm, nd + 1);
  if (isSame(tom.getFullYear(), tom.getMonth(), tom.getDate())) return "明天";
  return "";
}
const weekLabels = ["周日","周一","周二","周三","周四","周五","周六"];
function formatJobTimeRange(startValue, endValue) {
  const start = parseDate(startValue);
  const end = parseDate(endValue);
  if (!start && !end) return "时间待定";
  const base = start || end;
  const dayLabel = relativeDayLabel(base);
  const week = weekLabels[base.getDay()];
  const datePart = dayLabel ? `${dayLabel} ${week}` : `${base.getMonth()+1}月${base.getDate()}日 ${week}`;
  const hhmm = (d) => d ? `${pad2(d.getHours())}:${pad2(d.getMinutes())}` : "";
  const startStr = hhmm(start);
  const endStr = hhmm(end);
  if (startStr && endStr) return `${datePart} ${startStr}-${endStr}`;
  if (startStr) return `${datePart} ${startStr}`;
  return datePart;
}
function goRealname() {
  uni.navigateTo({ url: "/pages/worker/realname" });
}
function goBack() {
  uni.navigateBack();
}
function share() {
  uni.showToast({ title: "任务分享功能开发中", icon: "none" });
}
function viewRoute() {
  uni.showToast({ title: "路线功能开发中", icon: "none" });
}
function goHome() {
  const pages = getCurrentPages();
  if (pages.length > 1) {
    uni.navigateBack();
    return;
  }
  uni.switchTab({ url: "/pages/worker/home" });
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
    await applyPublicJob(job.value.id, { trial: job.value.salaryType === "MONTHLY" });
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
    employerName: item.employerName || item.companyName || item.bossName,
    orderCompletedCount: item.orderCompletedCount ?? item.completedOrderCount ?? item.orderDoneCount,
  };
}
</script>
<style scoped>
.page {
  height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.content {
  flex: 1;
  min-height: 0;
  height: auto;
  box-sizing: border-box;
  background: #f5f5f5;
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
.top-nav {
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
  box-sizing: border-box;
  flex-shrink: 0;
  position: relative;
  z-index: 30;
}
.nav-inner {
  height: 104rpx;
  padding: 16rpx 32rpx 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-sizing: border-box;
}
.nav-back {
  width: 64rpx;
  height: 64rpx;
  margin: 0;
  padding: 0;
  border: 0;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}
.nav-back::after {
  border: 0;
}
.nav-back-icon {
  width: 36rpx;
  height: 36rpx;
}
.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
}
.nav-space {
  width: 64rpx;
  height: 64rpx;
}
/* 头部工资区域 */
.salary-header {
  background: linear-gradient(135deg, #FFF4E6, #FFE4B5);
  padding: 40rpx 32rpx;
}
.salary-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #333;
  margin-bottom: 16rpx;
}
.tags {
  display: flex;
  gap: 16rpx;
  margin-bottom: 24rpx;
}
.tag {
  background: #fff;
  color: #666;
  font-size: 24rpx;
  padding: 8rpx 20rpx;
  border-radius: 24rpx;
}
.salary-info {
  display: flex;
  gap: 40rpx;
  padding: 24rpx 32rpx;
  background: #fff;
  border-radius: 24rpx;
}
.salary-item {
  flex: 1;
  text-align: center;
}
.salary-label {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 8rpx;
  display: block;
}
.salary-value {
  font-size: 36rpx;
  font-weight: 700;
  color: #FF6B35;
}
.salary-value-gray {
  color: #333;
}
/* 任务信息卡片 */
.info-card {
  background: #fff;
  margin: 24rpx 32rpx;
  border-radius: 24rpx;
  padding: 32rpx;
}
.info-row {
  display: flex;
  align-items: center;
  gap: 24rpx;
}
.info-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.icon-time {
  background: #FFF0E6;
  color: #FF6B35;
}
.icon-location {
  background: #E6F4FF;
  color: #1890FF;
}
.info-icon-img {
  width: 36rpx;
  height: 36rpx;
}
.info-content {
  flex: 1;
  min-width: 0;
}
.info-label {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 8rpx;
}
.info-value {
  font-size: 30rpx;
  color: #333;
  font-weight: 500;
}
.distance-text {
  margin-top: 6rpx;
  color: #ccc;
  font-size: 22rpx;
}
.info-link {
  color: #1890FF;
  font-size: 24rpx;
  display: flex;
  align-items: center;
  gap: 4rpx;
  flex-shrink: 0;
}
.link-chevron {
  width: 18rpx;
  height: 28rpx;
}
.outdated-tip-row {
  margin-top: 16rpx;
  margin-left: 104rpx;
}
.outdated-tag {
  color: #FF6B35;
  font-size: 22rpx;
  background: #FFF0E6;
  padding: 6rpx 14rpx;
  border-radius: 10rpx;
}
.address-note {
  margin-top: 20rpx;
  text-align: center;
  color: #ccc;
  font-size: 22rpx;
}
/* 标签页 */
.tabs {
  display: flex;
  background: #fff;
  padding: 0 32rpx;
  border-bottom: 1rpx solid #f5f5f5;
}
.tab {
  flex: 1;
  text-align: center;
  padding: 28rpx 0;
  font-size: 30rpx;
  color: #666;
  position: relative;
}
.tab.active {
  color: #FF6B35;
  font-weight: 600;
}
.tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 48rpx;
  height: 6rpx;
  background: #FF6B35;
  border-radius: 4rpx;
}
/* 描述内容 */
.desc-content {
  background: #fff;
  margin: 24rpx 32rpx;
  border-radius: 24rpx;
  padding: 32rpx;
}
.desc-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 24rpx;
  display: flex;
  align-items: center;
  gap: 12rpx;
}
.desc-title-icon {
  width: 32rpx;
  height: 32rpx;
}
.desc-text {
  font-size: 28rpx;
  color: #666;
  line-height: 1.9;
}
.desc-text .desc-row {
  display: block;
  margin-top: 8rpx;
}
.desc-list {
  padding: 0;
}
.desc-list-item {
  font-size: 28rpx;
  color: #666;
  line-height: 1.9;
  padding-left: 40rpx;
  position: relative;
  margin-bottom: 12rpx;
}
.desc-list-item::before {
  content: '•';
  position: absolute;
  left: 12rpx;
  color: #FF6B35;
}
/* 实名提示条 */
.realname-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #FFF4E6, #FFE4B5);
  padding: 28rpx 32rpx;
  margin: 0 32rpx;
  border-radius: 24rpx;
}
.realname-text {
  font-size: 28rpx;
  color: #8B4513;
}
.realname-text strong {
  font-weight: 600;
}
.realname-btn {
  color: #FF6B35;
  font-size: 28rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8rpx;
}
.btn-chevron {
  width: 24rpx;
  height: 24rpx;
}
/* 底部操作栏 */
.safe-bottom-action {
  width: 100%;
  box-sizing: border-box;
  padding: 0 32rpx;
  background: #fff;
  flex-shrink: 0;
  z-index: 20;
}
.bottom-bar {
  background: #fff;
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-top: 1rpx solid #f0f0f0;
  gap: 20rpx;
}
.bottom-action {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 16rpx;
}
.bottom-icon {
  width: 40rpx;
  height: 40rpx;
}
.bottom-label {
  font-size: 22rpx;
  color: #666;
  margin-top: 4rpx;
}
.enroll-btn {
  flex: 1;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: #8B4513;
  font-size: 34rpx;
  font-weight: 700;
  padding: 28rpx 48rpx;
  border-radius: 56rpx;
  border: none;
  margin: 0 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  box-shadow: 0 8rpx 24rpx rgba(255, 165, 0, 0.3);
  height: auto;
  line-height: 1.2;
}
.enroll-btn[disabled] {
  opacity: 0.65;
}
.enroll-btn::after {
  border: 0;
}
.enroll-info {
  text-align: center;
  line-height: 1.3;
}
.enroll-label {
  font-size: 34rpx;
  font-weight: 700;
}
.enroll-count {
  font-size: 22rpx;
  color: #666;
  margin-top: 4rpx;
}
</style>
