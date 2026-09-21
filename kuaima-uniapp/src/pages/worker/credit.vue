<template>
  <view class="page">
    <view class="status-spacer" :style="{ height: `${statusBarHeight}px` }" />
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <image :src="chevronLeftIcon" mode="aspectFit" />
      </view>
      <text class="nav-title">快马星级</text>
      <view class="nav-placeholder" />
    </view>

    <scroll-view scroll-y class="scroll-area">
      <view class="level-card">
        <text class="level-badge">当前星级</text>
        <text class="level-name">{{ levelName }}</text>
        <text class="level-expire">星级保留至 {{ expireDate }}</text>
        <image class="level-icon" :src="shieldIcon" mode="aspectFit" />
        <view class="level-upgrade">
          <text class="level-upgrade-text">{{ upgradeText }}</text>
          <button class="upgrade-btn" @click="showUpgradeGuide">去升级</button>
        </view>
      </view>

      <view class="score-row">
        <text class="score-label">当前星级分</text>
        <text class="score-value">{{ score }}</text>
        <view class="score-detail" @click="showScoreDetail">
          <text>星级分明细</text>
          <image :src="chevronGrayIcon" mode="aspectFit" />
        </view>
      </view>

      <view class="rule-entry" @click="showRules">
        <view class="rule-label">
          <image :src="gavelIcon" mode="aspectFit" />
          <text>星级分规则</text>
        </view>
        <view class="rule-link">
          <text>查看</text>
          <image :src="chevronOrangeIcon" mode="aspectFit" />
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { getCredit, getStarLevel } from "@/api/backend";
import chevronLeftIcon from "/static/icons/worker-credit/chevron-left-dark.svg";
import chevronGrayIcon from "/static/icons/worker-credit/chevron-right-gray.svg";
import chevronOrangeIcon from "/static/icons/worker-credit/chevron-right-orange.svg";
import gavelIcon from "/static/icons/worker-credit/gavel-orange.svg";
import shieldIcon from "/static/icons/worker-credit/shield-halved-blue.svg";

const statusBarHeight = ref(0);
const level = ref(0);
const score = ref(0);
const nextPoints = ref(200);
const expireDate = ref("2026-09-30");

const levelName = computed(() => `${chineseLevel(level.value)}星零工`);
const upgradeText = computed(() => {
  const remaining = Math.max(0, Number(nextPoints.value || 0));
  return remaining > 0
    ? `距离升级还差 ${remaining} 星级分`
    : "已达到当前星级最高分";
});

onLoad(async () => {
  try {
    const info =
      typeof uni.getWindowInfo === "function"
        ? uni.getWindowInfo()
        : uni.getSystemInfoSync();
    statusBarHeight.value = Number(info.statusBarHeight || 0);
  } catch (_) {}
  await loadStarData();
});

async function loadStarData() {
  const userId = uni.getStorageSync("userId") || "2001";
  const [starResult, creditResult] = await Promise.allSettled([
    getStarLevel(userId),
    getCredit(userId),
  ]);

  if (starResult.status === "fulfilled" && starResult.value) {
    applyStarData(starResult.value);
  }
  if (creditResult.status === "fulfilled" && creditResult.value) {
    const data = creditResult.value;
    score.value = Number(data.points ?? data.current ?? data.score ?? score.value);
  }

  if (
    starResult.status === "rejected" &&
    creditResult.status === "rejected"
  ) {
    uni.showToast({ title: "星级信息加载失败", icon: "none" });
  }
}

function applyStarData(data) {
  level.value = Number(data.level ?? data.starLevel ?? level.value);
  score.value = Number(data.points ?? data.current ?? data.score ?? score.value);
  nextPoints.value = Number(
    data.nextPoints ?? data.pointsToNextLevel ?? nextPoints.value,
  );
  expireDate.value =
    data.expireDate || data.expiredAt || data.validUntil || expireDate.value;
}

function chineseLevel(value) {
  return ["零", "一", "二", "三", "四", "五"][Number(value)] || String(value);
}

function goBack() {
  uni.navigateBack({
    fail: () => uni.redirectTo({ url: "/pages/worker/profile" }),
  });
}

function showUpgradeGuide() {
  uni.showModal({
    title: "星级升级",
    content: "按时到岗、完成订单并保持良好履约记录，可持续获得星级分并提升星级。",
    showCancel: false,
  });
}

function showScoreDetail() {
  uni.navigateTo({ url: "/pages/worker/score-detail" });
}

function showRules() {
  uni.showModal({
    title: "星级分规则",
    content: "按时到岗、完成订单可增加星级分；迟到、早退、失约或违规行为可能扣减星级分，具体以平台审核结果为准。",
    showCancel: false,
  });
}
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: #f5f6f8;
}

.status-spacer,
.nav-bar {
  flex-shrink: 0;
  background: #fff;
}

.nav-bar {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100rpx;
  padding: 0 32rpx;
  border-bottom: 1rpx solid #f0f0f0;
  box-sizing: border-box;
}

.nav-back,
.nav-placeholder {
  width: 64rpx;
  height: 64rpx;
}

.nav-back {
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-back image {
  width: 20rpx;
  height: 32rpx;
}

.nav-title {
  position: absolute;
  left: 50%;
  max-width: 60%;
  overflow: hidden;
  color: #333;
  font-size: 34rpx;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
  transform: translateX(-50%);
}

.scroll-area {
  flex: 1;
  min-height: 0;
}

.level-card {
  position: relative;
  margin: 24rpx;
  padding: 36rpx;
  overflow: hidden;
  border-radius: 28rpx;
  background: linear-gradient(135deg, #bfe3ff 0%, #e0f2ff 100%);
  box-sizing: border-box;
}

.level-badge {
  display: inline-block;
  margin-bottom: 20rpx;
  padding: 6rpx 20rpx;
  border-radius: 20rpx;
  background: #fff;
  color: #1890ff;
  font-size: 22rpx;
  font-weight: 600;
}

.level-name,
.level-expire {
  display: block;
}

.level-name {
  color: #1a3a5c;
  font-size: 56rpx;
  font-weight: 800;
  line-height: 1.25;
}

.level-expire {
  margin-top: 12rpx;
  color: #5a7a9a;
  font-size: 24rpx;
}

.level-icon {
  position: absolute;
  top: 54rpx;
  right: 32rpx;
  width: 144rpx;
  height: 144rpx;
  opacity: 0.5;
}

.level-upgrade {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 32rpx;
}

.level-upgrade-text {
  color: #1a3a5c;
  font-size: 26rpx;
}

.upgrade-btn {
  height: 60rpx;
  margin: 0;
  padding: 0 36rpx;
  border: 0;
  border-radius: 32rpx;
  background: #333;
  color: #fff;
  font-size: 26rpx;
  font-weight: 500;
  line-height: 60rpx;
}

.upgrade-btn::after {
  border: 0;
}

.score-row,
.rule-entry {
  display: flex;
  align-items: center;
  margin: 0 24rpx 24rpx;
  border-radius: 24rpx;
  background: #fff;
  box-sizing: border-box;
}

.score-row {
  justify-content: space-between;
  padding: 28rpx 32rpx;
}

.score-label {
  color: #333;
  font-size: 28rpx;
  font-weight: 600;
}

.score-value {
  flex: 1;
  margin-left: 24rpx;
  color: #ff6b35;
  font-size: 44rpx;
  font-weight: 700;
}

.score-detail,
.rule-label,
.rule-link {
  display: flex;
  align-items: center;
}

.score-detail {
  gap: 8rpx;
  color: #999;
  font-size: 26rpx;
}

.score-detail image,
.rule-link image {
  width: 12rpx;
  height: 20rpx;
}

.rule-entry {
  justify-content: space-between;
  padding: 32rpx;
}

.rule-label {
  gap: 16rpx;
  color: #333;
  font-size: 30rpx;
  font-weight: 600;
}

.rule-label image {
  width: 30rpx;
  height: 30rpx;
}

.rule-link {
  gap: 8rpx;
  color: #ff6b35;
  font-size: 26rpx;
}
</style>
