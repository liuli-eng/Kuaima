<template>
  <view class="container">
    <view class="page-bg">
      <!-- 导航栏 -->
      <view
        class="nav-bar"
        :style="{ paddingTop: `${statusBarHeight + 14}px` }"
      >
        <view class="nav-back" @click="goBack">
          <text>‹</text>
        </view>
        <text class="nav-title">招工设置</text>
        <view class="nav-right">
          <text class="help-icon">?</text>
        </view>
      </view>

      <scroll-view scroll-y class="scroll-area">
        <!-- 报名设置 -->
        <view class="group">
          <text class="group-title">
            <text class="group-icon">✓</text>
            报名设置
          </text>
          <view class="card">
            <view class="icon-row">
              <view class="icon-box orange">
                <text>♟</text>
              </view>
              <view class="icon-info">
                <text class="icon-title">报名方式</text>
                <text class="icon-desc">零工报名时需要审核</text>
              </view>
              <view class="seg-control">
                <text
                  class="seg-item"
                  :class="{ on: signMode === 'auto' }"
                  @click="signMode = 'auto'"
                  >自动通过</text
                >
                <text
                  class="seg-item"
                  :class="{ on: signMode === 'manual' }"
                  @click="signMode = 'manual'"
                  >手动审核</text
                >
              </view>
            </view>
            <view class="icon-row">
              <view class="icon-box blue">
                <text>☎</text>
              </view>
              <view class="icon-info">
                <text class="icon-title">报名电话通知</text>
                <text class="icon-desc">零工报名后自动发送短信通知</text>
              </view>
              <view
                class="toggle-switch"
                :class="{ on: phoneNotify }"
                @click="phoneNotify = !phoneNotify"
              ></view>
            </view>
          </view>
        </view>

        <!-- 结算设置 -->
        <view class="group">
          <text class="group-title">
            <text class="group-icon">¥</text>
            结算设置
          </text>
          <view class="card">
            <view class="nav-row" @click="selectSettleMode">
              <text class="label">结算方式</text>
              <view class="right">
                <text>{{ settleMode }}</text>
                <text class="arrow">›</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 通知设置 -->
        <view class="group">
          <text class="group-title">
            <text class="group-icon">●</text>
            通知设置
          </text>
          <view class="card">
            <view class="icon-row">
              <view class="icon-box orange">
                <text>!</text>
              </view>
              <view class="icon-info">
                <text class="icon-title">零工报名通知</text>
                <text class="icon-desc">零工报名时推送通知给老板</text>
              </view>
              <view
                class="toggle-switch"
                :class="{ on: signNotify }"
                @click="signNotify = !signNotify"
              ></view>
            </view>
            <view class="icon-row">
              <view class="icon-box blue">
                <text>✓</text>
              </view>
              <view class="icon-info">
                <text class="icon-title">开工提醒</text>
                <text class="icon-desc">开工前1小时推送提醒</text>
              </view>
              <view
                class="toggle-switch"
                :class="{ on: startRemind }"
                @click="startRemind = !startRemind"
              ></view>
            </view>
            <view class="icon-row">
              <view class="icon-box green">
                <text>¥</text>
              </view>
              <view class="icon-info">
                <text class="icon-title">结算通知</text>
                <text class="icon-desc">结算完成后通知老板</text>
              </view>
              <view
                class="toggle-switch"
                :class="{ on: settleNotify }"
                @click="settleNotify = !settleNotify"
              ></view>
            </view>
          </view>
        </view>

        <view style="height: 32rpx"></view>
      </scroll-view>

      <!-- 底部按钮 -->
      <view class="bottom-bar">
        <button class="save-btn" @click="saveSettings">保存设置</button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      statusBarHeight: 0,
      signMode: "auto",
      phoneNotify: true,
      settleMode: "日结",
      settleType: "daily",
      signNotify: true,
      startRemind: true,
      settleNotify: true,
    };
  },
  onLoad() {
    try {
      const info =
        typeof uni.getWindowInfo === "function"
          ? uni.getWindowInfo()
          : uni.getSystemInfoSync();
      this.statusBarHeight = Number(info.statusBarHeight || 0);
    } catch (_) {}
    const saved = uni.getStorageSync("recruitSettings");
    if (saved && typeof saved === "object") {
      this.settleMode = saved.settleMode || this.settleMode;
      this.settleType = saved.type || this.modeToType(this.settleMode);
    }
  },
  methods: {
    goBack() {
      uni.navigateBack();
    },
    selectSettleMode() {
      uni.showActionSheet({
        itemList: ["日结", "月结", "压薪日结"],
        success: (res) => {
          const modes = ["日结", "月结", "压薪日结"];
          this.settleMode = modes[res.tapIndex];
          this.settleType = this.modeToType(this.settleMode);
        },
      });
    },
    modeToType(mode) {
      return { 日结: "daily", 压薪日结: "heldBack", 月结: "month" }[mode] || "";
    },
    saveSettings() {
      if (!this.settleType) {
        uni.showToast({ title: "当前结算方式暂不支持发布", icon: "none" });
        return;
      }
      const data = {
        settleMode: this.settleMode,
        type: this.settleType || this.modeToType(this.settleMode),
      };
      uni.setStorageSync("recruitSettings", data);
      uni.$emit("recruitSettingsSaved", data);
      uni.showToast({ title: "设置保存成功！", icon: "success" });
      setTimeout(() => uni.navigateBack(), 500);
    },
  },
};
</script>

<style lang="scss" scoped>
.container {
  width: 100%;
  height: 100vh;
  background: #fff8e6;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.page-bg {
  background: #fff;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 32rpx;
  background: white;
  flex-shrink: 0;
}

.nav-back {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #333;
  font-size: 40rpx;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #333;
}

.nav-right {
  width: 64rpx;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
  background: #f7f8fa;
}

.group {
  margin-top: 24rpx;
}

.group-title {
  font-size: 26rpx;
  color: #999;
  padding: 24rpx 32rpx 12rpx;
  display: flex;
  align-items: center;
}

.card {
  background: #fff;
  margin: 0 24rpx;
  border-radius: 24rpx;
  overflow: hidden;
}

.icon-row {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 24rpx 32rpx;
  border-bottom: 2rpx solid #f5f5f5;
}

.icon-row:last-child {
  border-bottom: none;
}

.icon-box {
  width: 72rpx;
  height: 72rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-box.orange {
  background: #fff3ed;
  color: #ff6b35;
}
.icon-box.blue {
  background: #ebf5ff;
  color: #1677ff;
}
.icon-box.green {
  background: #e8f8ef;
  color: #52c41a;
}

.icon-info {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.icon-title {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  display: block;
  line-height: 1.35;
  white-space: normal;
}

.icon-desc {
  font-size: 24rpx;
  color: #999;
  margin-top: 4rpx;
  display: block;
  line-height: 1.4;
  white-space: normal;
  word-break: break-all;
}

.seg-control {
  display: flex;
  flex-shrink: 0;
  min-width: 0;
  background: #f5f5f5;
  border-radius: 12rpx;
  padding: 6rpx;
}

.seg-item {
  padding: 12rpx 24rpx;
  font-size: 26rpx;
  color: #666;
  border-radius: 8rpx;
}

.seg-item.on {
  background: #fff;
  color: #ff6b35;
  font-weight: 500;
  box-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.1);
}

.toggle-switch {
  width: 88rpx;
  height: 48rpx;
  background: #e0e0e0;
  border-radius: 24rpx;
  position: relative;
  transition: background 0.3s;
  flex-shrink: 0;
}

.toggle-switch.on {
  background: #ff6b35;
}

.toggle-switch::after {
  content: "";
  width: 40rpx;
  height: 40rpx;
  background: #fff;
  border-radius: 50%;
  position: absolute;
  top: 4rpx;
  left: 4rpx;
  transition: left 0.3s;
  box-shadow: 0 4rpx 8rpx rgba(0, 0, 0, 0.15);
}

.toggle-switch.on::after {
  left: 44rpx;
}

.nav-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 32rpx;
  border-bottom: 2rpx solid #f5f5f5;
}

.nav-row .label {
  font-size: 28rpx;
  color: #333;
}

.nav-row .right {
  display: flex;
  align-items: center;
  font-size: 26rpx;
  color: #999;
}

.bottom-bar {
  background: #fff;
  padding: 24rpx 32rpx 60rpx;
  border-top: 2rpx solid #f0f0f0;
  flex-shrink: 0;
}

.save-btn {
  width: 100%;
  height: 92rpx;
  background: linear-gradient(135deg, #ffd700, #ffa500);
  color: #fff;
  border: none;
  border-radius: 46rpx;
  font-size: 30rpx;
  font-weight: 600;
}

/* 原型尺寸与跨端安全区修正 */
.page-bg {
  width: 100%;
  min-width: 0;
  position: relative;
}
.nav-bar {
  padding: 14px 16px;
  box-sizing: border-box;
}
.nav-back {
  width: 32px;
  height: 32px;
  font-size: 24px;
}
.nav-title {
  font-size: 17px;
}
.nav-right {
  width: 32px;
}
.scroll-area {
  min-height: 0;
  padding-bottom: 100px;
}
.group {
  margin-top: 12px;
}
.group-title {
  font-size: 13px;
  padding: 12px 16px 6px;
}
.card {
  margin: 0 12px;
  border-radius: 12px;
}
.icon-row {
  gap: 12px;
  padding: 12px 16px;
  border-bottom-width: 1px;
}
.icon-box {
  width: 36px;
  height: 36px;
  border-radius: 8px;
}
.icon-title {
  font-size: 14px;
}
.icon-desc {
  font-size: 12px;
  margin-top: 2px;
}
.seg-control {
  border-radius: 6px;
  padding: 3px;
}
.seg-item {
  padding: 6px 8px;
  font-size: 12px;
  border-radius: 4px;
  white-space: nowrap;
}
.toggle-switch {
  width: 44px;
  height: 24px;
  border-radius: 12px;
}
.toggle-switch::after {
  width: 20px;
  height: 20px;
  top: 2px;
  left: 2px;
}
.toggle-switch.on::after {
  left: 22px;
}
.nav-row {
  padding: 14px 16px;
  border-bottom-width: 1px;
}
.nav-row .label {
  font-size: 14px;
}
.nav-row .right {
  font-size: 13px;
}
.bottom-bar {
  padding: 12px 16px calc(12px + env(safe-area-inset-bottom));
  box-sizing: border-box;
}
.save-btn {
  height: 46px;
  border-radius: 23px;
  font-size: 15px;
  margin: 0;
  line-height: 1.4;
}
.save-btn::after {
  border: none;
}
.help-icon {
  color: #ccc;
  font-size: 18px;
}
.group-icon {
  color: #999;
  margin-right: 4px;
}
.arrow {
  color: #ccc;
  font-size: 12px;
  margin-left: 8px;
}
</style>
