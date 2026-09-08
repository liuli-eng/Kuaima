<template>
  <view class="container">
    <!-- 导航栏 -->
    <view
      class="nav-bar"
      :style="{
        paddingTop: `${statusBarHeight}px`,
        height: `${50 + statusBarHeight}px`,
      }"
    >
      <view class="nav-back" @click="goBack">
        <text>←</text>
      </view>
      <text class="nav-title">发布招工</text>
      <view style="width: 32px"></view>
    </view>

    <!-- 步骤条 -->
    <view class="stepper">
      <view class="step-dot">
        <text style="font-size: 9px">✓</text>
      </view>
      <text class="step-label">基础信息</text>
      <view class="step-line"></view>
      <view class="step-dot-pending">
        <text style="font-size: 6px">●</text>
      </view>
      <text class="step-label-pending">招工需求</text>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <!-- 基础信息 -->
      <view class="form-section">
        <view class="form-item" @click="editJob">
          <text class="form-label">工种</text>
          <text class="form-value" :class="{ placeholder: !jobValue }">{{ jobValue || "请选择" }}</text>
          <text class="› form-arrow"></text>
        </view>
        <view class="form-item" @click="navigateTo('task-content')">
          <text class="form-label">干活内容</text>
          <text class="form-value" :class="{ placeholder: !workContent }">{{
            workContent || "请选择"
          }}</text>
          <text class="› form-arrow"></text>
        </view>
        <view class="form-item" @click="navigateTo('task-content')">
          <text class="form-label">任务详情</text>
          <text class="form-value" :class="{ placeholder: !taskDetail }">{{
            taskDetail || "未填写详情"
          }}</text>
          <text class="› form-arrow"></text>
        </view>
        <view class="form-item" @click="navigateTo('gender-age')">
          <text class="form-label">性别年龄</text>
          <text class="form-value">{{ genderAgeValue }}</text>
          <text class="› form-arrow"></text>
        </view>
      </view>

      <!-- 工作日期 -->
      <view class="date-section">
        <text class="date-section-title">工作日期</text>
        <scroll-view scroll-x class="date-options">
          <view
            class="date-chip"
            :class="{ selected: date.selected }"
            v-for="(date, index) in dates"
            :key="date.value"
            @click="toggleDate(index)"
          >
            <text class="weekday">{{ date.weekday }}</text>
            <text class="date">{{ date.date }}</text>
          </view>
        </scroll-view>
      </view>

      <!-- 工作时间和地点 -->
      <view class="form-section">
        <view class="form-item" @click="navigateTo('select-work-time')">
          <text class="form-label">工作时间</text>
          <text class="form-value" :class="{ link: !workTimeValue }">
            {{ workTimeValue || "选择工作时间" }} ›
          </text>
        </view>
        <view class="form-item" @click="navigateTo('location')">
          <text class="form-label">干活地点</text>
          <text class="form-value" :class="{ link: !workLocationValue }">
            {{ workLocationValue || "设置干活地点" }}
            <text style="color: #ff6b35; font-size: 14px; margin-left: 4px"
              >📍</text
            >
          </text>
        </view>
      </view>

      <view style="height: 20px"></view>
    </scroll-view>

    <!-- 浮动客服 -->
    <view class="service-fab" @click="navigateTo('service-chat')">
      <text style="font-size: 18px; color: #ff6b35; margin-bottom: 2px"
        >🎧</text
      >
      <text style="font-size: 10px; color: #ff6b35">客服</text>
    </view>

    <!-- 底部按钮 -->
    <view class="bottom-bar">
      <button class="submit-btn" @click="nextStep">下一步</button>
    </view>
  </view>
</template>

<script>
import { getOrder } from "@/api/backend";

function buildDateOptions() {
  const weekdays = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];
  const result = [];
  const today = new Date();
  const currentWeek = weekStart(today).getTime();
  for (let index = 0; index < 7; index += 1) {
    const date = new Date(today);
    date.setDate(today.getDate() + index);
    const weekLabel = `${weekStart(date).getTime() === currentWeek ? "本周" : "下周"}${weekdays[date.getDay()]}`;
    result.push({
      value: formatLocalDate(date),
      weekday: weekLabel,
      date: `${date.getMonth() + 1}月${date.getDate()}日`,
      selected: index < 2,
    });
  }
  return result;
}

function weekStart(value) {
  const date = new Date(value);
  const weekday = date.getDay() || 7;
  date.setHours(0, 0, 0, 0);
  date.setDate(date.getDate() - weekday + 1);
  return date;
}

function formatLocalDate(date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
}

export default {
  data() {
    return {
      statusBarHeight: 0,
      jobValue: "",
      workContent: "",
      taskDetail: "",
      genderAgeValue: "性别不限、18岁~不限",
      workLocationValue: "",
      workTimeValue: "",
      jobName: "",
      primaryJobName: "",
      publishType: "",
      orderId: "",
      industryId: "",
      enterpriseTypeIds: [],
      jobIds: [],
      dates: buildDateOptions(),
    };
  },
  onLoad(options) {
    try {
      const info =
        typeof uni.getWindowInfo === "function"
          ? uni.getWindowInfo()
          : uni.getSystemInfoSync();
      this.statusBarHeight = Number(info.statusBarHeight || 0);
    } catch (_) {}
    uni.$on("taskContentSaved", this.applyTaskContent);
    uni.$on("genderAgeSelected", this.applyGenderAge);
    uni.$on("workLocationSelected", this.applyWorkLocation);
    uni.$on("workTimeSelected", this.applyWorkTime);
    uni.$on("jobsSelected", this.applyJobsSelected);
    const saved = uni.getStorageSync("taskContent");
    if (saved) this.applyTaskContent(saved);
    const savedGenderAge = uni.getStorageSync("genderAgeSelection");
    if (savedGenderAge) this.applyGenderAge(savedGenderAge);
    const savedWorkLocation = uni.getStorageSync("workLocationSelection");
    if (savedWorkLocation) this.applyWorkLocation(savedWorkLocation);
    const savedWorkTime = uni.getStorageSync("workTimeSelection");
    if (savedWorkTime) this.applyWorkTime(savedWorkTime);
    if (savedWorkTime?.selectedDates?.length)
      this.applySelectedDates(savedWorkTime.selectedDates);
    if (options?.job) {
      this.jobName = decodeURIComponent(options.job);
      this.jobValue = this.jobName;
      this.primaryJobName = this.jobName.split("、").filter(Boolean)[0] || "";
    }
    this.industryId = options?.industryId || "";
    this.enterpriseTypeIds = parseIdList(options?.enterpriseTypeIds);
    this.jobIds = parseIdList(options?.jobIds || options?.jobId);
    if (this.jobName || this.jobIds.length) this.saveJobCategorySelection();
    this.publishType = options?.type || "";
    this.orderId = options?.id || "";
    if (this.orderId) this.loadOrder(this.orderId);
  },
  onUnload() {
    uni.$off("taskContentSaved", this.applyTaskContent);
    uni.$off("genderAgeSelected", this.applyGenderAge);
    uni.$off("workLocationSelected", this.applyWorkLocation);
    uni.$off("workTimeSelected", this.applyWorkTime);
    uni.$off("jobsSelected", this.applyJobsSelected);
  },
  onShow() {
    // 编辑已有订单时，岗位详情接口是唯一数据源，避免旧草稿覆盖接口回显。
    if (!this.orderId) {
      const saved = uni.getStorageSync("taskContent");
      if (saved) this.applyTaskContent(saved);
    }
    const savedGenderAge = uni.getStorageSync("genderAgeSelection");
    if (savedGenderAge) this.applyGenderAge(savedGenderAge);
    const savedWorkLocation = uni.getStorageSync("workLocationSelection");
    if (savedWorkLocation) this.applyWorkLocation(savedWorkLocation);
    const savedWorkTime = uni.getStorageSync("workTimeSelection");
    if (savedWorkTime) this.applyWorkTime(savedWorkTime);
    if (savedWorkTime?.selectedDates?.length)
      this.applySelectedDates(savedWorkTime.selectedDates);
  },
  methods: {
    async loadOrder(id) {
      try {
        const detail = await getOrder(id);
        if (!detail || typeof detail !== "object") return;
        this.jobName = detail.orderTitle || detail.postion || this.jobName;
        this.jobValue = this.jobName;
        this.primaryJobName = detail.postion || this.jobName.split("、")[0] || "";
        this.industryId = detail.industryId || this.industryId;
        this.enterpriseTypeIds = parseIdList(
          detail.enterpriseTypeIds || this.enterpriseTypeIds,
        );
        this.jobIds = parseIdList(
          detail.jobIds || detail.jobCategoryIds || detail.jobCategoryId || this.jobIds,
        );
        this.saveJobCategorySelection();
        this.workContent = detail.orderContent || "";
        this.taskDetail = detail.orderContent || "";
        this.workLocationValue = detail.address || "";
        this.workTimeValue = formatWorkTime(detail.startTime, detail.endTime);
        const orderDate = String(detail.startTime || "").slice(0, 10);
        if (orderDate) this.applySelectedDates([orderDate]);
        this.publishType = detail.type || this.publishType;
        if (detail.orderContent) {
          uni.setStorageSync("taskContent", {
            ...(uni.getStorageSync("taskContent") || {}),
            title: this.workContent,
            desc: detail.orderContent,
          });
        }
        if (detail.address) {
          uni.setStorageSync("workLocationSelection", {
            address: detail.address,
            display: detail.address,
          });
        }
        if (detail.startTime || detail.endTime) {
          const time = {
            startTime: extractTime(detail.startTime) || "08:00",
            endTime: extractTime(detail.endTime) || "18:00",
            display: formatWorkTime(detail.startTime, detail.endTime),
          };
          uni.setStorageSync("workTimeSelection", time);
        }
      } catch (error) {
        uni.showToast({
          title: error.message || "岗位信息加载失败",
          icon: "none",
        });
      }
    },
    goBack() {
      uni.navigateBack();
    },
    navigateTo(page) {
      if (page === "select-work-time") {
        const workTime = uni.getStorageSync("workTimeSelection") || {};
        const selectedDates = this.dates
          .filter((item) => item.selected)
          .map((item) => item.value);
        uni.setStorageSync("workTimeSelection", {
          ...workTime,
          selectedDates,
          dateOptions: this.dates.map((item) => ({
            value: item.value,
            weekday: item.weekday,
            date: item.date,
            selected: item.selected,
          })),
        });
      }
      uni.navigateTo({ url: `/pages/boss/${page}` });
    },
    editJob() {
      const query = this.orderId
        ? `?id=${encodeURIComponent(this.orderId)}`
        : "";
      uni.navigateTo({ url: `/pages/boss/all-jobs${query}` });
    },
    applyTaskContent(data = {}) {
      this.workContent = data.title || data.desc || this.workContent;
      this.taskDetail = data.desc || this.taskDetail;
    },
    applyJobsSelected(data = {}) {
      const names = Array.isArray(data.jobs) ? data.jobs.filter(Boolean) : [];
      if (names.length) {
        this.jobName = names.join("、");
        this.jobValue = this.jobName;
        this.primaryJobName = names[0];
      }
      this.industryId = data.industryId || this.industryId;
      this.enterpriseTypeIds = parseIdList(data.enterpriseTypeIds);
      this.jobIds = parseIdList(data.jobIds);
      this.saveJobCategorySelection(data);
    },
    saveJobCategorySelection(extra = {}) {
      const previous = uni.getStorageSync("jobCategorySelection") || {};
      uni.setStorageSync("jobCategorySelection", {
        ...previous,
        ...extra,
        industryId: this.industryId || extra.industryId || "",
        enterpriseTypeIds: [...this.enterpriseTypeIds],
        jobIds: [...this.jobIds],
        jobs: this.jobName ? this.jobName.split("、").filter(Boolean) : [],
      });
    },
    applyGenderAge(data = {}) {
      if (data.display) this.genderAgeValue = data.display;
    },
    applyWorkLocation(data = {}) {
      if (data.display) this.workLocationValue = data.display;
    },
    applyWorkTime(data = {}) {
      if (data.display) this.workTimeValue = data.display;
      if (Array.isArray(data.selectedDates))
        this.applySelectedDates(data.selectedDates);
    },
    applySelectedDates(values = []) {
      const selected = new Set(values.map(String));
      this.dates.forEach((item) => {
        item.selected =
          selected.has(String(item.value)) || selected.has(String(item.date));
      });
    },
    toggleDate(index) {
      this.dates[index].selected = !this.dates[index].selected;
      const workTime = uni.getStorageSync("workTimeSelection") || {};
      const selectedDates = this.dates
        .filter((item) => item.selected)
        .map((item) => item.value);
      const data = { ...workTime, selectedDates };
      uni.setStorageSync("workTimeSelection", data);
      uni.$emit("workTimeSelected", data);
    },
    nextStep() {
      if (!this.jobName) {
        uni.showToast({ title: "请先选择工种", icon: "none" });
        return;
      }
      if (!this.workContent) {
        uni.showToast({ title: "请先选择干活内容", icon: "none" });
        return;
      }
      const settings = uni.getStorageSync("recruitSettings") || {};
      const type = this.publishType || settings.type || "daily";
      const categoryQuery = [
        this.industryId ? `&industryId=${encodeURIComponent(this.industryId)}` : "",
        this.enterpriseTypeIds.length
          ? `&enterpriseTypeIds=${encodeURIComponent(this.enterpriseTypeIds.join(","))}`
          : "",
        this.jobIds.length
          ? `&jobIds=${encodeURIComponent(this.jobIds.join(","))}`
          : "",
      ].join("");
      uni.navigateTo({
        url: `/pages/boss/recruit-demand?job=${encodeURIComponent(this.primaryJobName || this.jobName.split("、")[0])}&type=${encodeURIComponent(type)}${this.orderId ? `&id=${encodeURIComponent(this.orderId)}` : ""}${categoryQuery}`,
      });
    },
  },
};

function extractTime(value) {
  const match = String(value || "").match(/(?:T|\s)(\d{1,2}:\d{2})/);
  return match ? match[1] : "";
}

function parseIdList(value) {
  if (Array.isArray(value)) return value.filter((item) => item !== "" && item != null);
  if (value === undefined || value === null || value === "") return [];
  return String(value)
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);
}

function formatWorkTime(start, end) {
  const left = extractTime(start);
  const right = extractTime(end);
  return left && right ? `${left} - ${right}` : left || right || "";
}
</script>

<style lang="scss" scoped>
.container {
  width: 100%;
  height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.nav-bar {
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #fff;
}

.nav-back {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
}

.stepper {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 14px 16px;
  background: #fff;
  gap: 4px;
}

.step-dot {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
}

.step-label {
  font-size: 13px;
  color: #ff6b35;
  font-weight: 500;
}

.step-line {
  width: 30px;
  height: 2px;
  background: #ff6b35;
}

.step-label-pending {
  font-size: 13px;
  color: #999;
}

.step-dot-pending {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #e0e0e0;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
  padding-bottom: 100px;
}

.form-section {
  margin-top: 10px;
  background: #fff;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f5f5f5;
}

.form-item:last-child {
  border-bottom: none;
}

.form-label {
  font-size: 15px;
  color: #333;
  font-weight: 500;
  min-width: 80px;
}

.form-value {
  flex: 1;
  text-align: right;
  font-size: 14px;
  color: #333;
  margin-right: 4px;
}

.form-value.placeholder {
  color: #999;
}

.form-value.link {
  color: #ff6b35;
}

.form-arrow {
  color: #ccc;
  font-size: 12px;
}

.date-section {
  padding: 16px;
  background: #fff;
  margin-top: 10px;
}

.date-section-title {
  font-size: 15px;
  color: #333;
  font-weight: 500;
  margin-bottom: 12px;
  display: block;
}

.date-options {
  display: flex;
  gap: 8px;
  width: 100%;
  white-space: nowrap;
  box-sizing: border-box;
}

.date-chip {
  display: inline-block;
  padding: 8px 12px;
  background: #f5f5f5;
  border-radius: 8px;
  text-align: center;
  font-size: 12px;
  color: #333;
  min-width: 70px;
  margin-right: 8px;
}

.date-chip.selected {
  background: #fff3ed;
  color: #ff6b35;
}

.date-chip .weekday {
  font-weight: 500;
  display: block;
}

.date-chip .date {
  color: #999;
  font-size: 11px;
  display: block;
}

.service-fab {
  position: absolute;
  right: 16px;
  bottom: 100px;
  width: 48px;
  height: 48px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 20;
}

.bottom-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 12px 16px calc(12px + env(safe-area-inset-bottom));
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}

.submit-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #ffd700, #ffa500);
  color: #fff;
  border: none;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  line-height: 1.4;
}

.submit-btn::after {
  border: none;
}
</style>
