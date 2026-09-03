<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <view class="header">
        <view class="brand">
          <text class="brand-title">快马日结</text>
          <text class="slogan">真老板·真工价·真日结</text>
        </view>
      </view>

      <view class="tabs">
        <text
          v-for="tab in tabs"
          :key="tab.key"
          class="tab"
          :class="{ active: activeTab === tab.key }"
          @click="switchTab(tab.key)"
        >
          {{ tab.label }}
        </text>
      </view>

      <view class="tools">
        <view class="location" @click="chooseLocation">
          <text class="location-icon">⌖</text>
          <text class="location-text">{{ location }}</text>
          <text class="location-arrow">⌄</text>
        </view>
        <view class="tool-btn" @click="showFilter">筛选⌄</view>
        <view class="tool-btn" @click="search">⌕ 搜索</view>
      </view>

      <view v-if="loading" class="state">正在加载岗位…</view>
      <view v-else-if="error" class="state error" @click="loadJobs">
        岗位加载失败，点击重试
      </view>
      <view v-else-if="filteredJobs.length === 0" class="state">
        暂无符合条件的岗位
      </view>
      <view v-else class="jobs">
        <JobCard
          v-for="job in filteredJobs"
          :key="job.id"
          :job="job"
          @select="openDetail"
          @apply="applyJob"
        />
      </view>
    </scroll-view>

    <view
      v-if="filterVisible"
      class="filter-mask"
      @click="filterVisible = false"
    >
      <view class="filter-panel" @click.stop>
        <view class="filter-head"
          ><text>筛选</text
          ><text class="filter-close" @click="filterVisible = false"
            >×</text
          ></view
        >
        <scroll-view scroll-y class="filter-content"
          ><view
            v-for="group in filterGroups"
            :key="group.key"
            class="filter-section"
            ><text class="filter-title">{{ group.title }}</text
            ><view class="filter-options"
              ><text
                v-for="option in group.options"
                :key="option"
                class="filter-option"
                :class="{ active: filterForm[group.key] === option }"
                @click="filterForm[group.key] = option"
                >{{ option }}</text
              ></view
            ></view
          ></scroll-view
        >
        <view class="filter-actions"
          ><button class="filter-reset" @click="resetFilter">重置</button
          ><button class="filter-confirm" @click="confirmFilter">
            确定
          </button></view
        >
      </view>
    </view>

    <WorkerTabBar current="home" />
  </view>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import JobCard from "@/components/JobCard.vue";
import WorkerTabBar from "@/components/WorkerTabBar.vue";
import { request, USE_MOCK } from "@/api/http";

const tabs = [
  { key: "DAY", label: "每天日结" },
  { key: "PRESS", label: "压薪日结" },
  { key: "MONTHLY", label: "月结" },
];

const activeTab = ref("DAY");
const location = ref("松江洞照路");
const loading = ref(false);
const error = ref(false);
const jobs = ref([]);
const filterVisible = ref(false);
const dayFilterGroups = [
  { key: "date", title: "日期", options: ["全部", "今天", "明天", "后天"] },
  { key: "type", title: "类型", options: ["全部", "计时", "计件"] },
  { key: "days", title: "天数", options: ["全部", "一天日结", "多天日结"] },
  {
    key: "duration",
    title: "任务时长",
    options: ["全部", "3小时以内", "3~6小时", "6小时以上"],
  },
  {
    key: "sort",
    title: "排序",
    options: ["推荐排序", "近距离优先", "时间优先", "老雇主优先"],
  },
];
const pressFilterGroups = [
  {
    key: "date",
    title: "日期",
    options: ["全部", "今天", "明天", "后天", "本周内"],
  },
  {
    key: "salary",
    title: "薪资范围",
    options: ["不限", "100元以下", "100-200元", "200-300元", "300元以上"],
  },
  {
    key: "distance",
    title: "距离",
    options: ["不限", "3公里内", "5公里内", "10公里内"],
  },
  {
    key: "jobType",
    title: "工种",
    options: [
      "全部",
      "餐饮服务",
      "快递配送",
      "仓储物流",
      "制造业",
      "建筑装修",
      "零售促销",
    ],
  },
  {
    key: "duration",
    title: "工作时长",
    options: ["不限", "3小时内", "3-6小时", "6小时以上"],
  },
  {
    key: "tag",
    title: "特色标签",
    options: ["免服务费", "包吃住", "有空调", "日结", "可长期"],
  },
];
const filterGroups = computed(() =>
  activeTab.value === "PRESS" ? pressFilterGroups : dayFilterGroups,
);
const filterDefaults = {
  date: "全部",
  type: "全部",
  days: "全部",
  duration: "全部",
  sort: "推荐排序",
  salary: "不限",
  distance: "不限",
  jobType: "全部",
  tag: "",
};
const filterForm = reactive({ ...filterDefaults });

const filteredJobs = computed(() =>
  activeTab.value === "DAY"
    ? jobs.value
    : jobs.value.filter(
        (job) =>
          (job.settlementType || job.salaryType || "DAY") === activeTab.value,
      ),
);

const mockJobs = [
  {
    id: "demo-1",
    title: "餐饮清洁工",
    displayTitle: "餐饮清洁工",
    unitPrice: 162,
    wageUnit: "元/单",
    salaryType: "DAY",
    startTime: "08:00",
    endTime: "18:00",
    address: "松江区泗泾镇",
    headcount: 3,
    hiredCount: 5,
    insurance: true,
    duration: 9,
    ageLimit: "22岁以上",
  },
  {
    id: "demo-2",
    title: "餐饮服务员、洗碗员",
    shortTitle: "餐饮服务员、洗...",
    displayTitle: "餐饮服务员",
    unitPrice: 170,
    wageUnit: "元/天",
    salaryType: "PRESS",
    startTime: "10:00",
    endTime: "20:00",
    address: "松江区中山街道",
    headcount: 2,
    hiredCount: 8,
    duration: 10,
    tagText: "压薪日结",
  },
  {
    id: "demo-3",
    title: "氩弧焊和气保焊",
    displayTitle: "氩弧焊焊工",
    unitPrice: 280,
    wageUnit: "元/天",
    salaryType: "DAY",
    startTime: "08:00",
    endTime: "17:00",
    address: "松江区佘山镇",
    headcount: 2,
    hiredCount: 3,
    duration: 8,
    skillText: "有经验",
  },
  {
    id: "demo-monthly-1",
    title: "餐饮服务员",
    displayTitle: "餐饮服务员",
    unitPrice: 4500,
    wageUnit: "元/月",
    salaryType: "MONTHLY",
    interview: "明天 08:00-20:00",
    distance: "徐泾镇 9.4km",
    monthlySalary: 4500,
    trialSalary: 180,
    thumbnail: "🍽️",
    startTime: "08:00",
    endTime: "20:00",
    address: "松江区徐泾镇",
    headcount: 2,
    hiredCount: 1,
    insurance: true,
    duration: 12,
    ageLimit: "30-50岁",
    tagText: "月结试工",
  },
  {
    id: "demo-4",
    title: "快递分拣员",
    displayTitle: "快递分拣员",
    unitPrice: 96,
    wageUnit: "元/单",
    salaryType: "DAY",
    startTime: "13:00",
    endTime: "19:00",
    address: "松江区",
    headcount: 5,
    hiredCount: 12,
    duration: 6,
  },
];

async function loadJobs() {
  loading.value = true;
  error.value = false;
  try {
    const result = await request({ url: "/worker/jobs?page=0&size=20&status=%E6%8B%9B%E5%B7%A5%E4%B8%AD" });
    const records = result?.records || result;
    if (Array.isArray(records) && records.length > 0) {
      const apiJobs = records.map(normalizeJob);
      const categoryJobs = USE_MOCK
        ? mockJobs.filter((item) => item.salaryType !== "DAY").map(normalizeJob)
        : [];
      jobs.value = [...apiJobs, ...categoryJobs];
      return;
    }
    jobs.value = USE_MOCK ? mockJobs.map(normalizeJob) : [];
  } catch (e) {
    jobs.value = USE_MOCK ? mockJobs.map(normalizeJob) : [];
    error.value = true;
  } finally {
    loading.value = false;
  }
}

function normalizeJob(item) {
  const typeMap = { daily: "DAY", heldBack: "PRESS", month: "MONTHLY" };
  return {
    ...item,
    id: item.id,
    title: item.title || item.orderTitle || item.postion || "岗位",
    displayTitle: item.displayTitle || item.postion || item.title || item.orderTitle,
    unitPrice: item.unitPrice ?? item.wage ?? item.salary,
    settlementType: item.settlementType || item.salaryType || typeMap[item.type] || "DAY",
    salaryType: item.salaryType || item.settlementType || typeMap[item.type] || "DAY",
    headcount: item.headcount ?? item.recruitCount ?? item.needCount ?? item.orderNum,
    hiredCount:
      item.hiredCount ?? item.applyCount ?? item.currentApplyCount ?? 0,
  };
}

function chooseLocation() {
  uni.showToast({ title: "定位功能开发中", icon: "none" });
}

function showFilter() {
  filterVisible.value = true;
}
function resetFilter() {
  Object.assign(filterForm, filterDefaults);
}
function confirmFilter() {
  uni.setStorageSync("workerJobFilter", { ...filterForm });
  filterVisible.value = false;
}

function search() {
  uni.navigateTo({ url: "/pages/worker/search" });
}

function switchTab(key) {
  activeTab.value = key;
}

function openDetail(job) {
  uni.navigateTo({
    url: `/pages/worker/job-detail?id=${encodeURIComponent(job.id)}`,
  });
}

async function applyJob(job) {
  if (uni.getStorageSync("workerRealname") !== true) {
    return uni.navigateTo({ url: "/pages/worker/realname" });
  }

  if (uni.getStorageSync("workerQualified") !== true) {
    return uni.navigateTo({ url: "/pages/worker/classroom" });
  }

  try {
    await request({ url: `/worker/orders/apply/${job.id}`, method: "POST" });
    job.applied = true;
    uni.showToast({ title: "报名成功", icon: "success" });
  } catch (e) {
    uni.showToast({ title: e.message || "报名失败", icon: "none" });
  }
}

onMounted(loadJobs);
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}

.scroll {
  height: calc(100vh - 166rpx);
}

.header {
  position: relative;
  min-height: 140rpx;
  box-sizing: border-box;
  padding: calc(16rpx + env(safe-area-inset-top)) 32rpx 40rpx;
  background: linear-gradient(180deg, #ffd59e 0%, #ffe4b5 50%, #fffbf5 100%);
}

.brand-title {
  display: block;
  color: #2f2b28;
  font-size: 40rpx;
  line-height: 1.2;
  font-weight: 800;
}

.slogan {
  display: block;
  margin-top: 4rpx;
  color: #8b4513;
  font-size: 22rpx;
  font-weight: 500;
}

.tabs {
  display: flex;
  background: #fff;
  padding: 0 32rpx;
  margin-top: 16rpx;
  border-bottom: 1rpx solid #f1f1f1;
}

.tab {
  position: relative;
  flex: 1;
  text-align: center;
  padding: 28rpx 0 24rpx;
  color: #999;
  font-size: 28rpx;
  line-height: 1.2;
}

.tab.active {
  color: #333;
  font-weight: 700;
}

.tab.active::after {
  content: "";
  position: absolute;
  left: 50%;
  bottom: 0;
  width: 48rpx;
  height: 6rpx;
  border-radius: 6rpx;
  background: #ff6b35;
  transform: translateX(-50%);
}

.tools {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx 32rpx;
}

.location {
  display: flex;
  align-items: center;
  gap: 6rpx;
  max-width: 50%;
  min-width: 0;
  color: #444;
  font-size: 26rpx;
}

.location-icon {
  color: #ff6b35;
  font-size: 22rpx;
  line-height: 1;
  flex-shrink: 0;
}

.location-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.location-arrow {
  color: #999;
  font-size: 20rpx;
  flex-shrink: 0;
}

.tool-btn {
  background: #fff;
  border-radius: 28rpx;
  padding: 12rpx 24rpx;
  color: #666;
  font-size: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
}

.jobs {
  padding: 0 32rpx 32rpx;
}

.state {
  text-align: center;
  padding: 160rpx 32rpx;
  color: #999;
  font-size: 26rpx;
}

.state.error {
  color: #e34d59;
}
.filter-mask {
  position: fixed;
  inset: 0;
  z-index: 50;
  background: rgba(0, 0, 0, 0.45);
}
.filter-panel {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 88vh;
  overflow: hidden;
  border-radius: 40rpx 40rpx 0 0;
  background: #f5f5f5;
}
.filter-head {
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  background: #fff;
  border-bottom: 1rpx solid #eee;
  color: #222;
  font-size: 36rpx;
  font-weight: 700;
}
.filter-close {
  color: #697386;
  font-size: 64rpx;
  font-weight: 400;
  line-height: 1;
}
.filter-content {
  height: calc(88vh - 220rpx);
}
.filter-section {
  padding: 30rpx 32rpx 34rpx;
  margin-bottom: 12rpx;
  background: #fff;
}
.filter-title {
  display: block;
  margin-bottom: 24rpx;
  color: #333;
  font-size: 32rpx;
  font-weight: 700;
}
.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 18rpx 16rpx;
}
.filter-option {
  min-width: 120rpx;
  padding: 16rpx 22rpx;
  border: 2rpx solid transparent;
  border-radius: 38rpx;
  background: #f5f5f5;
  color: #666;
  font-size: 27rpx;
  text-align: center;
  box-sizing: border-box;
}
.filter-option.active {
  border-color: #ffc84d;
  background: linear-gradient(135deg, #ffedbb, #ffda72);
  color: #d47635;
  font-weight: 600;
}
.filter-actions {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  gap: 20rpx;
  padding: 24rpx 32rpx calc(24rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid #eee;
  background: #fff;
}
.filter-reset,
.filter-confirm {
  height: 84rpx;
  line-height: 84rpx;
  border-radius: 44rpx;
  font-size: 28rpx;
}
.filter-reset {
  width: 34%;
  border: 2rpx solid #ddd;
  background: #fff;
  color: #666;
}
.filter-confirm {
  flex: 1;
  border: 0;
  background: linear-gradient(135deg, #ff6b35, #ff9b42);
  color: #fff;
  font-weight: 700;
}
.filter-reset::after,
.filter-confirm::after {
  border: 0;
}
</style>
