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
          <text class="form-value">{{ jobValue }}</text>
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
        <view class="date-options">
          <view
            class="date-chip"
            :class="{ selected: date.selected }"
            v-for="(date, index) in dates"
            :key="index"
            @click="toggleDate(index)"
          >
            <text class="weekday">{{ date.weekday }}</text>
            <text class="date">{{ date.date }}</text>
          </view>
        </view>
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

export default {
  data() {
    return {
      statusBarHeight: 0,
      jobValue: "普工、焊锡工",
      workContent: "",
      taskDetail: "",
      genderAgeValue: "性别不限、18岁~不限",
      workLocationValue: "",
      workTimeValue: "",
      jobName: "普工、焊锡工",
      publishType: "",
      orderId: "",
      dates: [
        { weekday: "本周五", date: "8月21日", selected: true },
        { weekday: "明周六", date: "8月22日", selected: true },
        { weekday: "后周日", date: "8月23日", selected: false },
      ],
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
    const saved = uni.getStorageSync("taskContent");
    if (saved) this.applyTaskContent(saved);
    const savedGenderAge = uni.getStorageSync("genderAgeSelection");
    if (savedGenderAge) this.applyGenderAge(savedGenderAge);
    const savedWorkLocation = uni.getStorageSync("workLocationSelection");
    if (savedWorkLocation) this.applyWorkLocation(savedWorkLocation);
    const savedWorkTime = uni.getStorageSync("workTimeSelection");
    if (savedWorkTime) this.applyWorkTime(savedWorkTime);
    if (options?.job) {
      this.jobName = decodeURIComponent(options.job);
      this.jobValue = this.jobName;
    }
    this.publishType = options?.type || "";
    this.orderId = options?.id || "";
    if (this.orderId) this.loadOrder(this.orderId);
  },
  onUnload() {
    uni.$off("taskContentSaved", this.applyTaskContent);
    uni.$off("genderAgeSelected", this.applyGenderAge);
    uni.$off("workLocationSelected", this.applyWorkLocation);
    uni.$off("workTimeSelected", this.applyWorkTime);
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
  },
  methods: {
    async loadOrder(id) {
      try {
        const detail = await getOrder(id);
        if (!detail || typeof detail !== "object") return;
        this.jobName = detail.orderTitle || detail.postion || this.jobName;
        this.jobValue = this.jobName;
        this.workContent = detail.orderContent || "";
        this.taskDetail = detail.orderContent || "";
        this.workLocationValue = detail.address || "";
        this.workTimeValue = formatWorkTime(detail.startTime, detail.endTime);
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
      uni.navigateTo({ url: `/pages/boss/${page}` });
    },
    editJob() {
      uni.navigateTo({ url: "/pages/boss/all-jobs" });
    },
    applyTaskContent(data = {}) {
      this.workContent = data.title || data.desc || this.workContent;
      this.taskDetail = data.desc || this.taskDetail;
    },
    applyGenderAge(data = {}) {
      if (data.display) this.genderAgeValue = data.display;
    },
    applyWorkLocation(data = {}) {
      if (data.display) this.workLocationValue = data.display;
    },
    applyWorkTime(data = {}) {
      if (data.display) this.workTimeValue = data.display;
    },
    toggleDate(index) {
      this.dates[index].selected = !this.dates[index].selected;
    },
    nextStep() {
      if (!this.workContent) {
        uni.showToast({ title: "请先选择干活内容", icon: "none" });
        return;
      }
      const settings = uni.getStorageSync("recruitSettings") || {};
      const type = this.publishType || settings.type || "daily";
      uni.navigateTo({
        url: `/pages/boss/recruit-demand?job=${encodeURIComponent(this.jobName)}&type=${encodeURIComponent(type)}${this.orderId ? `&id=${encodeURIComponent(this.orderId)}` : ""}`,
      });
    },
  },
};

function extractTime(value) {
  const match = String(value || "").match(/(?:T|\s)(\d{1,2}:\d{2})/);
  return match ? match[1] : "";
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
  flex-wrap: wrap;
}

.date-chip {
  padding: 8px 12px;
  background: #f5f5f5;
  border-radius: 8px;
  text-align: center;
  font-size: 12px;
  color: #333;
  min-width: 70px;
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
