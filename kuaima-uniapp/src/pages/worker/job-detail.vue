<template>
  <view class="page">
    <AppNavBar title="岗位详情" :show-back="true" />
    <scroll-view scroll-y class="content">
      <view class="salary-header"
        ><view class="title-row"
          ><text class="title">{{ job.title }}</text
          ><text class="favorite" @click="toggleFavorite">{{
            favorite ? "♥" : "♡"
          }}</text></view
        ><view class="tags"
          ><text class="tag">{{ wageUnit }}</text
          ><text class="tag">审核上岗</text><text class="tag">保障</text></view
        ><view class="salary-info"
          ><view
            ><text>预计时长</text
            ><text class="value">{{ job.duration }}小时</text></view
          ><view
            ><text>预计收入</text
            ><text class="value dark">{{ job.unitPrice }}元/天</text></view
          ></view
        ></view
      >
      <view class="info-card"
        ><view class="info-row"
          ><text class="icon orange">◷</text
          ><view class="info-main"
            ><text class="label">任务时间</text
            ><text class="info-value"
              >{{ job.startTime }} - {{ job.endTime }}</text
            ></view
          ></view
        ><view class="info-row"
          ><text class="icon blue">⌖</text
          ><view class="info-main"
            ><text class="label">任务地点</text
            ><text class="info-value">{{ job.address }}</text
            ><text class="muted">距当前位置直线8.7公里</text
            ><text class="muted">*接单后可查看详细门牌号</text></view
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
        ><text v-if="activeTab === 'desc'" class="desc"
          >本次招聘为餐饮行业短期清洁工作，工作内容包括：\n\n·
          负责餐厅大堂及包间的卫生清洁\n· 协助整理餐桌、餐具清洗\n·
          保持工作区域整洁卫生\n· 服从店长安排，配合团队工作</text
        ><view v-else-if="activeTab === 'notice'" class="list"
          ><text>· 需年满18周岁，身体健康</text><text>· 需实名认证通过</text
          ><text>· 请按约定时间准时到岗</text
          ><text>· 报名成功后请关注雇主审核结果</text></view
        ><view v-else class="list"
          ><text>雇主名称：松江区新桥餐饮服务有限公司</text
          ><text>完成订单：156单</text><text>平台认证雇主，信用良好</text></view
        ></view
      >
      <view v-if="!isRealname" class="realname"
        ><text>完成实名认证后才能报名</text
        ><text @click="goRealname">去认证 ›</text></view
      >
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
          ><text>剩2个名额</text>
        </button></view
      ></SafeBottomAction
    >
  </view>
</template>
<script setup>
import { computed, onMounted, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import AppNavBar from "@/components/AppNavBar.vue";
import SafeBottomAction from "@/components/SafeBottomAction.vue";
import { request } from "@/api/http";
const pages = getCurrentPages();
const options = pages[pages.length - 1]?.options || {};
const tabs = [
  { key: "desc", label: "岗位描述" },
  { key: "notice", label: "报名须知" },
  { key: "boss", label: "雇主信息" },
];
const activeTab = ref("desc");
const applying = ref(false);
const applied = ref(false);
const favorite = ref(false);
const isRealname = ref(uni.getStorageSync("workerRealname") === true);
const fallback = {
  id: options.id || "demo-1",
  title: "餐饮清洁工",
  unitPrice: 162,
  salaryType: "DAY",
  startTime: "08:00",
  endTime: "18:00",
  address: "松江区泗泾镇",
  duration: 9,
  insurance: true,
};
const job = ref(fallback);
const wageUnit = computed(() =>
  job.value.salaryType === "HOURLY"
    ? "元/小时"
    : job.value.salaryType === "MONTHLY"
      ? "元/月"
      : "元/天",
);
onShow(() => {
  isRealname.value = uni.getStorageSync("workerRealname") === true;
});
onMounted(async () => {
  if (options.id && !String(options.id).startsWith("demo-")) {
    try {
      const r = await request({ url: `/worker/jobs/${options.id}` });
      if (r) job.value = normalizeJob(r);
    } catch (_) {}
  }
});
function goRealname() {
  uni.navigateTo({ url: "/pages/worker/realname" });
}
function share() {
  uni.showToast({ title: "任务分享功能开发中", icon: "none" });
}
function viewRoute() {
  uni.showToast({ title: "路线功能开发中", icon: "none" });
}
function toggleFavorite() {
  favorite.value = !favorite.value;
  uni.showToast({
    title: favorite.value ? "已收藏" : "已取消收藏",
    icon: "none",
  });
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
  const salaryType = item.salaryType || item.settlementType || typeMap[item.type] || "DAY";
  return {
    ...fallback,
    ...item,
    title: item.title || item.orderTitle || item.postion || fallback.title,
    salaryType,
    unitPrice: item.unitPrice ?? item.wage ?? item.salary ?? item.monthSalary ?? fallback.unitPrice,
    address: item.address || item.workAddress || item.location || fallback.address,
    duration: item.duration ?? item.workHours ?? fallback.duration,
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
