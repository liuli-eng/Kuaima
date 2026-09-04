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
      <text class="nav-title">选择工作时间</text>
      <view class="nav-right">
        <text style="color: #ccc; font-size: 18px">❓</text>
      </view>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <text class="section-title">快捷选择</text>
      <text class="section-desc">选择常用时间段，或自定义起止时间</text>

      <!-- 常用时段 -->
      <view class="quick-select">
        <view class="quick-title">
          <text style="color: #ff6b35">⚡</text>
          <text style="margin-left: 8px">常用时段</text>
        </view>
        <view class="quick-options">
          <text
            class="quick-chip"
            :class="{ selected: quickSelected === item }"
            v-for="(item, index) in quickOptions"
            :key="index"
            @click="selectQuick(item)"
            >{{ item }}</text
          >
        </view>
      </view>

      <!-- 自定义时间 -->
      <view class="time-card">
        <view class="time-card-header">
          <view class="time-card-title">
            <text style="color: #ff6b35">🕐</text>
            <text style="margin-left: 8px">自定义时间</text>
          </view>
          <text style="font-size: 12px; color: #999"
            >{{ startTime }} - {{ endTime }}</text
          >
        </view>
        <view class="time-card-body">
          <view class="time-slot-row">
            <text class="time-label">开始</text>
            <view class="time-picker">
              <picker
                mode="time"
                :value="startTime"
                @change="onStartTimeChange"
              >
                <view class="time-input">{{ startTime }}</view>
              </picker>
              <text class="› time-arrow"></text>
            </view>
          </view>
          <view class="time-slot-row">
            <text class="time-label">结束</text>
            <view class="time-picker">
              <picker mode="time" :value="endTime" @change="onEndTimeChange">
                <view class="time-input">{{ endTime }}</view>
              </picker>
              <text class="› time-arrow"></text>
            </view>
          </view>
        </view>
      </view>

      <!-- 工作时长 -->
      <view class="duration-info">
        <view class="duration-left">
          <view class="duration-icon">
            <text style="font-size: 18px">⏳</text>
          </view>
          <view class="duration-text">
            <text class="label">工作时长</text>
            <text class="value">{{ duration }}</text>
          </view>
        </view>
        <view class="duration-right">
          <text class="label">休息时间</text>
          <text class="value">含1小时午休</text>
        </view>
      </view>

      <!-- 提示 -->
      <view class="notice-bar">
        <text class="ℹ" style="margin-top: 2px; flex-shrink: 0"></text>
        <text>建议选择白天工作时段，报名人数更多。夜间工作需额外补贴。</text>
      </view>

      <view class="custom-toggle">
        <view class="toggle-left">
          <text class="toggle-icon">▣</text>
          <view>
            <text class="toggle-title">多天连续工作</text>
            <text class="toggle-desc">可选择多天连续招工，每天同一时间段</text>
          </view>
        </view>
        <switch
          color="#ff6b35"
          :checked="multiDayEnabled"
          @change="onMultiDayChange"
        />
      </view>

      <view v-if="multiDayEnabled" class="multiple-days">
        <view class="multiple-title">
          <text class="toggle-icon">▣</text>
          <text>选择工作日期</text>
        </view>
        <scroll-view scroll-x class="day-chips">
          <view class="day-chip-row">
            <view
              v-for="(day, index) in days"
              :key="day.date"
              class="day-chip"
              :class="{ selected: day.selected }"
              @click="toggleDay(index)"
            >
              <text class="weekday">{{ day.weekday }}</text>
              <text class="day">{{ day.day }}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <view class="time-list">
        <view
          v-for="slot in timeSlots"
          :key="slot.range"
          class="time-list-item"
          :class="{ selected: selectedSlot === slot.range }"
          @click="selectTimeSlot(slot)"
        >
          <view class="time-list-radio">
            <view v-if="selectedSlot === slot.range" class="radio-dot"></view>
          </view>
          <view class="time-list-content">
            <text class="time-list-time">{{ slot.range }}</text>
            <view class="time-list-meta">
              <text>{{ slot.duration }}</text>
              <text class="time-list-tag" :class="slot.type">{{
                slot.tag
              }}</text>
            </view>
          </view>
          <text class="time-list-arrow">›</text>
        </view>
      </view>

      <view style="height: 20px"></view>
    </scroll-view>

    <!-- 底部按钮 -->
    <view class="bottom-bar">
      <view class="btn-group">
        <button class="btn-cancel" @click="goBack">取消</button>
        <button class="btn-confirm" @click="confirmTime">
          <text style="margin-right: 8px">✓</text>
          确认选择
        </button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      statusBarHeight: 0,
      startTime: "08:00",
      endTime: "18:00",
      quickSelected: null,
      selectedSlot: "08:00 - 18:00",
      multiDayEnabled: false,
      quickOptions: [
        "早班 8:00-18:00",
        "上午 8:00-12:00",
        "下午 13:30-17:30",
        "夜班 18:00-22:00",
        "全天 9小时",
        "全天 12小时",
      ],
      days: [
        { weekday: "周五", day: "21", date: "08-21", selected: true },
        { weekday: "周六", day: "22", date: "08-22", selected: true },
        { weekday: "周日", day: "23", date: "08-23", selected: false },
        { weekday: "周一", day: "24", date: "08-24", selected: false },
        { weekday: "周二", day: "25", date: "08-25", selected: false },
      ],
      timeSlots: [
        {
          range: "08:00 - 18:00",
          duration: "10小时",
          tag: "热门时段",
          type: "peak",
        },
        {
          range: "08:00 - 12:00",
          duration: "4小时",
          tag: "上午时段",
          type: "",
        },
        {
          range: "13:30 - 17:30",
          duration: "4小时",
          tag: "下午时段",
          type: "",
        },
        {
          range: "18:00 - 22:00",
          duration: "4小时",
          tag: "夜班",
          type: "night",
        },
        { range: "09:00 - 18:00", duration: "9小时", tag: "全天", type: "" },
      ],
    };
  },
  computed: {
    duration() {
      const [sh, sm] = this.startTime.split(":").map(Number);
      const [eh, em] = this.endTime.split(":").map(Number);
      let totalMin = eh * 60 + em - (sh * 60 + sm);
      if (totalMin <= 0) totalMin += 24 * 60;
      const hours = Math.floor(totalMin / 60);
      const mins = totalMin % 60;
      return mins > 0 ? `${hours}小时${mins}分钟` : `${hours}小时`;
    },
  },
  onLoad() {
    try {
      const info =
        typeof uni.getWindowInfo === "function"
          ? uni.getWindowInfo()
          : uni.getSystemInfoSync();
      this.statusBarHeight = Number(info.statusBarHeight || 0);
    } catch (_) {}
    const saved = uni.getStorageSync("workTimeSelection");
    if (saved && typeof saved === "object") {
      this.startTime = saved.startTime || this.startTime;
      this.endTime = saved.endTime || this.endTime;
      this.quickSelected = saved.quickSelected || null;
      this.selectedSlot =
        saved.selectedSlot || `${this.startTime} - ${this.endTime}`;
      this.multiDayEnabled = Boolean(saved.multiDayEnabled);
      if (Array.isArray(saved.selectedDates)) {
        this.days.forEach((day) => {
          day.selected = saved.selectedDates.includes(day.date);
        });
      }
    }
  },
  methods: {
    goBack() {
      uni.navigateBack();
    },
    onStartTimeChange(e) {
      this.startTime = e.detail.value;
      this.clearPresetSelection();
    },
    onEndTimeChange(e) {
      this.endTime = e.detail.value;
      this.clearPresetSelection();
    },
    selectQuick(item) {
      this.quickSelected = item;
      const match = item.match(/(\d{1,2}:\d{2})-(\d{1,2}:\d{2})/);
      if (match) {
        this.startTime = match[1];
        this.endTime = match[2];
      } else if (item === "全天 9小时") {
        this.startTime = "09:00";
        this.endTime = "18:00";
      } else if (item === "全天 12小时") {
        this.startTime = "08:00";
        this.endTime = "20:00";
      }
      this.selectedSlot = `${this.startTime} - ${this.endTime}`;
    },
    clearPresetSelection() {
      this.quickSelected = null;
      this.selectedSlot = `${this.startTime} - ${this.endTime}`;
    },
    selectTimeSlot(slot) {
      const [start, end] = slot.range.split(" - ");
      this.startTime = start;
      this.endTime = end;
      this.selectedSlot = slot.range;
      this.quickSelected = null;
    },
    onMultiDayChange(e) {
      this.multiDayEnabled = e.detail.value;
    },
    toggleDay(index) {
      this.days[index].selected = !this.days[index].selected;
    },
    confirmTime() {
      const selectedDates = this.days
        .filter((day) => day.selected)
        .map((day) => day.date);
      const data = {
        startTime: this.startTime,
        endTime: this.endTime,
        duration: this.duration,
        quickSelected: this.quickSelected,
        selectedSlot: `${this.startTime} - ${this.endTime}`,
        multiDayEnabled: this.multiDayEnabled,
        selectedDates,
        display: `${this.startTime} - ${this.endTime}`,
      };
      uni.setStorageSync("workTimeSelection", data);
      uni.$emit("workTimeSelected", data);
      uni.navigateBack();
    },
  },
};
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

.status-bar {
  height: 47px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 28px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  background: #fff;
}

.status-icons {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-bar {
  height: 50px;
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

.nav-right {
  width: 32px;
  text-align: right;
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  padding: 16px 16px 8px;
  display: block;
}

.section-desc {
  font-size: 12px;
  color: #999;
  padding: 0 16px 12px;
  display: block;
}

.quick-select {
  background: #fff;
  margin: 0 12px 12px;
  border-radius: 12px;
  padding: 16px;
}

.quick-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}

.quick-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-chip {
  padding: 8px 14px;
  background: #f5f5f5;
  border-radius: 20px;
  font-size: 13px;
  color: #333;
  border: 1px solid transparent;
}

.quick-chip.selected {
  background: #fff3ed;
  color: #ff6b35;
  border-color: #ff6b35;
  font-weight: 500;
}

.time-card {
  background: #fff;
  margin: 0 12px 12px;
  border-radius: 12px;
  overflow: hidden;
}

.time-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #f5f5f5;
}

.time-card-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
}

.time-card-body {
  padding: 16px;
}

.time-slot-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.time-slot-row:last-child {
  margin-bottom: 0;
}

.time-label {
  font-size: 13px;
  color: #666;
  min-width: 56px;
}

.time-picker {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-input {
  flex: 1;
  padding: 10px 12px;
  background: #f9f9f9;
  border: 1px solid #eee;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  text-align: center;
}

.time-arrow {
  color: #ccc;
  font-size: 12px;
}

.duration-info {
  background: linear-gradient(135deg, #fff8f3, #fff3ed);
  margin: 0 12px 12px;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.duration-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.duration-icon {
  width: 40px;
  height: 40px;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ff6b35;
}

.duration-text .label {
  font-size: 13px;
  color: #666;
  display: block;
}

.duration-text .value {
  font-size: 18px;
  font-weight: 700;
  color: #ff6b35;
  display: block;
}

.duration-right {
  text-align: right;
}

.duration-right .label {
  font-size: 12px;
  color: #999;
  display: block;
}

.duration-right .value {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  display: block;
}

.notice-bar {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 16px;
  background: #fffbeb;
  margin: 0 12px 12px;
  border-radius: 8px;
  font-size: 12px;
  color: #d97706;
  line-height: 1.5;
}

.custom-toggle,
.multiple-days,
.time-list {
  background: #fff;
  margin: 0 12px 12px;
  border-radius: 12px;
}

.custom-toggle {
  padding: 14px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.toggle-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.toggle-icon {
  color: #ff6b35;
  font-size: 16px;
  flex-shrink: 0;
}

.toggle-title,
.toggle-desc {
  display: block;
}

.toggle-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.toggle-desc {
  margin-top: 3px;
  font-size: 12px;
  color: #999;
}

.multiple-days {
  padding: 16px;
}

.multiple-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.day-chips {
  width: 100%;
  white-space: nowrap;
}

.day-chip-row {
  display: flex;
  gap: 8px;
  padding-bottom: 4px;
}

.day-chip {
  width: 56px;
  height: 64px;
  flex-shrink: 0;
  border: 1px solid transparent;
  border-radius: 10px;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.day-chip .weekday {
  margin-bottom: 2px;
  font-size: 11px;
  color: #999;
}

.day-chip .day {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.day-chip.selected {
  border-color: #ff6b35;
  background: #fff3ed;
}

.day-chip.selected .weekday,
.day-chip.selected .day {
  color: #ff6b35;
}

.time-list-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #f5f5f5;
}

.time-list-item:last-child {
  border-bottom: none;
}

.time-list-item.selected {
  background: #fff9f5;
}

.time-list-radio {
  width: 20px;
  height: 20px;
  margin-right: 12px;
  border: 2px solid #ddd;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  flex-shrink: 0;
}

.time-list-item.selected .time-list-radio {
  border-color: #ff6b35;
}

.radio-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ff6b35;
}

.time-list-content {
  flex: 1;
  min-width: 0;
}

.time-list-time {
  display: block;
  margin-bottom: 4px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.time-list-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #999;
}

.time-list-tag {
  padding: 2px 8px;
  border-radius: 4px;
  background: #f0f9ff;
  color: #3b82f6;
  font-size: 11px;
}

.time-list-tag.peak {
  background: #fef3c7;
  color: #d97706;
}

.time-list-tag.night {
  background: #ede9fe;
  color: #7c3aed;
}

.time-list-arrow {
  color: #ccc;
  font-size: 20px;
}

.bottom-bar {
  background: #fff;
  padding: 12px 16px 30px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}

.btn-group {
  display: flex;
  gap: 10px;
}

.btn-cancel {
  flex: 1;
  padding: 14px;
  background: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 500;
}

.btn-confirm {
  flex: 2;
  padding: 14px;
  background: linear-gradient(135deg, #ffd700, #ffa500);
  color: #fff;
  border: none;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
