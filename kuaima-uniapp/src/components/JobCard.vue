<template>
  <view class="job-card" @click="$emit('select', job)">
    <view v-if="job.salaryType === 'MONTHLY'" class="monthly-card">
      <view class="monthly-main"
        ><view class="monthly-meta"
          ><text>{{
            job.interview || `${job.startTime || "明天"}-${job.endTime || ""}`
          }}</text
          ><text class="monthly-tag">试工</text
          ><text class="monthly-distance">{{
            job.distance || "徐泾镇 9.4km"
          }}</text></view
        ><text class="monthly-title">{{ job.title }}</text
        ><view class="tags"
          ><text
            v-for="tag in tags"
            :key="tag.text"
            class="tag"
            :class="tag.type"
            >{{ tag.text }}</text
          ></view
        ><view class="monthly-footer"
          ><view
            ><text class="monthly-salary"
              >月薪:{{ job.monthlySalary || job.unitPrice }}</text
            ><text class="monthly-plus">+</text
            ><text class="monthly-note"
              >(试工: {{ job.trialSalary || 180 }}元/天)</text
            ></view
          ><button
            class="apply monthly-apply"
            @click.stop="$emit('apply', job)"
          >
            我要试工
          </button></view
        ></view
      ><view class="monthly-thumb"
        >{{ job.thumbnail || "🍽️" }}<text>▶</text></view
      >
    </view>
    <view v-else class="card-head">
      <view class="head-main">
        <text v-if="job.tagText" class="job-type">{{ job.tagText }}</text>
        <text class="job-title">{{ job.shortTitle || job.title }}</text>
        <view v-if="tags.length" class="tags">
          <text
            v-for="tag in tags"
            :key="tag.text"
            class="tag"
            :class="tag.type"
          >
            {{ tag.text }}
          </text>
        </view>
      </view>

      <view class="wage-box">
        <view class="wage-line">
          <text class="wage">{{ job.unitPrice ?? "--" }}</text>
          <text class="unit">{{ wageUnit }}</text>
        </view>
        <button
          class="apply"
          :disabled="job.applied"
          @click.stop="$emit('apply', job)"
        >
          {{ job.applied ? "已报名" : actionText }}
        </button>
      </view>
    </view>

    <view v-if="job.salaryType !== 'MONTHLY'" class="info-grid">
      <view class="info-item">
        <text class="info-icon">▣</text>
        <text class="info-label">招聘岗位：</text>
        <text class="info-value">{{ job.displayTitle || job.title }}</text>
      </view>
      <view class="info-item">
        <text class="info-icon">◷</text>
        <text class="info-label">工作时间：</text>
        <text class="info-value">
          {{ job.startTime || "--" }} ~ {{ job.endTime || "--" }}
        </text>
      </view>
      <view class="info-item">
        <text class="info-icon">⌖</text>
        <text class="info-label">工作地点：</text>
        <text class="info-value">{{ job.address || "待确认" }}</text>
      </view>
      <view class="info-item wage-row">
        <text class="info-icon">￥</text>
        <text class="info-label">报酬：</text>
        <text class="info-value"
          >{{ job.unitPrice ?? "--" }}{{ wageUnit }}</text
        >
      </view>
      <view class="info-item">
        <text class="info-icon">☷</text>
        <text class="info-label">招募人数：</text>
        <text class="info-value">{{ job.headcount ?? "--" }}人</text>
      </view>
      <view class="info-item">
        <text class="info-icon">✓</text>
        <text class="info-label">当前报名：</text>
        <text class="info-value">{{ job.hiredCount ?? 0 }}人</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  job: { type: Object, required: true },
});

defineEmits(["select", "apply"]);

const wageUnit = computed(() => {
  if (props.job.wageUnit) {
    return props.job.wageUnit;
  }

  if (props.job.salaryType === "HOURLY") {
    return "元/小时";
  }

  if (props.job.salaryType === "MONTHLY") {
    return "元/月";
  }

  return "元/天";
});

const actionText = computed(() => {
  if (props.job.salaryType === "PRESS") return "报名面试";
  if (props.job.salaryType === "MONTHLY") return "我要试工";
  return "抢新单";
});

const tags = computed(() => {
  if (props.job.tagText) {
    return [];
  }

  const list = [];

  if (props.job.insurance) {
    list.push({ text: "保障", type: "blue" });
  } else if (props.job.salaryType === "DAY") {
    list.push({ text: "日结", type: "green" });
  }

  if (props.job.duration) {
    list.push({ text: `${props.job.duration}小时`, type: "" });
  }

  if (props.job.skillText) {
    list.push({ text: props.job.skillText, type: "blue" });
  }

  if (props.job.ageLimit) {
    list.push({ text: props.job.ageLimit, type: "orange" });
  }

  return list;
});
</script>

<style scoped>
.job-card {
  background: #fff;
  border-radius: 14rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.04);
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20rpx;
  padding-bottom: 16rpx;
  margin-bottom: 16rpx;
  border-bottom: 1rpx solid #f1f1f1;
}

.head-main {
  min-width: 0;
  flex: 1;
}

.job-type {
  display: block;
  margin-bottom: 4rpx;
  color: #666;
  font-size: 22rpx;
  font-weight: 600;
}

.job-title {
  display: block;
  max-width: 360rpx;
  overflow: hidden;
  color: #333;
  font-size: 32rpx;
  font-weight: 700;
  line-height: 1.3;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 8rpx;
}

.tag {
  padding: 6rpx 12rpx;
  border-radius: 10rpx;
  background: #fff3ee;
  color: #ff6b35;
  font-size: 22rpx;
  line-height: 1.2;
}

.tag.blue {
  background: #e6f4ff;
  color: #1890ff;
}

.tag.green {
  background: #e8f8ed;
  color: #16a34a;
}

.tag.orange {
  background: #fff7e6;
  color: #fa8c16;
}

.wage-box {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.wage-line {
  line-height: 1;
}

.wage {
  color: #ff4757;
  font-size: 40rpx;
  font-weight: 800;
}

.unit {
  color: #ff4757;
  font-size: 20rpx;
  font-weight: 600;
}

.apply {
  height: 64rpx;
  line-height: 64rpx;
  margin-top: 16rpx;
  padding: 0 40rpx;
  border-radius: 30rpx;
  background: #ff6b35;
  color: #fff;
  font-size: 28rpx;
  font-weight: 500;
}

.apply::after {
  border: 0;
}

.apply[disabled] {
  background: #b7b7b7;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8rpx 12rpx;
  margin-bottom: 10rpx;
}

.info-item {
  display: flex;
  align-items: center;
  min-width: 0;
  color: #6b7280;
  font-size: 24rpx;
  line-height: 1.5;
}

.info-icon {
  flex-shrink: 0;
  width: 24rpx;
  margin-right: 6rpx;
  color: #9ca3af;
  font-size: 18rpx;
}

.info-label {
  flex-shrink: 0;
}

.info-value {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.wage-row,
.wage-row .info-icon {
  color: #ff6b35;
}
.monthly-card {
  display: flex;
  gap: 20rpx;
}
.monthly-main {
  flex: 1;
  min-width: 0;
}
.monthly-meta {
  display: flex;
  align-items: center;
  gap: 12rpx;
  color: #666;
  font-size: 22rpx;
}
.monthly-tag {
  padding: 6rpx 16rpx;
  border-radius: 18rpx;
  background: #e6f4ff;
  color: #1890ff;
}
.monthly-distance {
  margin-left: auto;
  color: #999;
}
.monthly-title {
  display: block;
  margin: 16rpx 0;
  color: #333;
  font-size: 34rpx;
  font-weight: 700;
}
.monthly-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
  margin-top: 24rpx;
}
.monthly-salary {
  color: #ff6b35;
  font-size: 34rpx;
  font-weight: 800;
}
.monthly-plus {
  color: #333;
  font-size: 26rpx;
}
.monthly-note {
  display: block;
  margin-top: 6rpx;
  color: #999;
  font-size: 21rpx;
}
.monthly-apply {
  flex-shrink: 0;
}
.monthly-thumb {
  position: relative;
  width: 112rpx;
  height: 112rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border-radius: 20rpx;
  background: #fff0d5;
  font-size: 46rpx;
}
.monthly-thumb text {
  position: absolute;
  right: 4rpx;
  bottom: 4rpx;
  width: 34rpx;
  height: 34rpx;
  border-radius: 50%;
  background: #665d4f;
  color: #fff;
  text-align: center;
  line-height: 34rpx;
  font-size: 16rpx;
}
</style>
