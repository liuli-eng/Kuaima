<template>
  <view class="page">
    <view class="nav-header" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="nav-back" @click="goBack"><text class="nav-back-glyph">‹</text></view>
      <text class="nav-title">招工设置</text>
      <view class="nav-placeholder"><text class="help-icon">?</text></view>
    </view>

    <scroll-view scroll-y class="page-content">
      <view class="group">
        <view class="group-title">♙ 报名设置</view>
        <view class="card">
          <view class="icon-row">
            <view class="icon-box orange">♟</view>
            <view class="icon-info"><text class="icon-title">报名方式</text><text class="icon-desc">零工报名时需要审核</text></view>
            <view class="seg-control">
              <text class="seg-item" :class="{ on: signMode === 'auto' }" @click="setSignMode('auto')">自动通过</text>
              <text class="seg-item" :class="{ on: signMode === 'manual' }" @click="setSignMode('manual')">手动审核</text>
            </view>
          </view>
          <view class="icon-row" @click="toggleSetting('phoneNotify')">
            <view class="icon-box blue">☎</view>
            <view class="icon-info"><text class="icon-title">报名电话通知</text><text class="icon-desc">零工报名后自动发送短信通知</text></view>
            <view class="toggle-switch" :class="{ on: phoneNotify }"><view class="toggle-dot" /></view>
          </view>
        </view>
      </view>

      <view class="group">
        <view class="group-title">▣ 结算设置</view>
        <view class="card"><view class="nav-row" @click="chooseSettlement"><text class="row-label">结算方式</text><text class="row-value">{{ settleMode }}　›</text></view></view>
      </view>

      <view class="group">
        <view class="group-title">♢ 通知设置</view>
        <view class="card">
          <view class="icon-row" @click="toggleSetting('signNotify')"><view class="icon-box orange">♟</view><view class="icon-info"><text class="icon-title">零工报名通知</text><text class="icon-desc">零工报名时推送通知给老板</text></view><view class="toggle-switch" :class="{ on: signNotify }"><view class="toggle-dot" /></view></view>
          <view class="icon-row" @click="toggleSetting('startRemind')"><view class="icon-box blue">✓</view><view class="icon-info"><text class="icon-title">开工提醒</text><text class="icon-desc">开工前1小时推送提醒</text></view><view class="toggle-switch" :class="{ on: startRemind }"><view class="toggle-dot" /></view></view>
          <view class="icon-row" @click="toggleSetting('settleNotify')"><view class="icon-box green">￥</view><view class="icon-info"><text class="icon-title">结算通知</text><text class="icon-desc">结算完成后通知老板</text></view><view class="toggle-switch" :class="{ on: settleNotify }"><view class="toggle-dot" /></view></view>
        </view>
      </view>
      <view class="content-space" />
    </scroll-view>

    <view class="bottom-bar"><button class="save-btn" :disabled="saving" @click="saveSettings">{{ saving ? "保存中..." : "保存设置" }}</button></view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      statusBarHeight: 0,
      saving: false,
      settleMode: "日结",
      settleType: "daily",
      signMode: "auto",
      phoneNotify: true,
      signNotify: true,
      startRemind: true,
      settleNotify: true,
    };
  },
  onLoad() {
    const info = typeof uni.getWindowInfo === "function"
      ? uni.getWindowInfo()
      : uni.getSystemInfoSync();
    this.statusBarHeight = Number(info.statusBarHeight || 0);
    this.applySavedSettings(
      uni.getStorageSync("recruitDraftSettings") ||
        uni.getStorageSync("recruitSettings"),
    );
  },
  methods: {
    applySavedSettings(saved) {
      if (!saved || typeof saved !== "object") return;
      this.settleMode = saved.settleMode || this.settleMode;
      this.settleType = saved.type || this.settleType;
      if (["auto", "manual"].includes(saved.signMode)) this.signMode = saved.signMode;
      ["phoneNotify", "signNotify", "startRemind", "settleNotify"].forEach((key) => {
        if (typeof saved[key] === "boolean") this[key] = saved[key];
      });
    },
    goBack() {
      uni.navigateBack();
    },
    setSignMode(mode) {
      if (this.signMode === mode || this.saving) return;
      this.signMode = mode;
    },
    toggleSetting(field) {
      if (this.saving || typeof this[field] !== "boolean") return;
      this[field] = !this[field];
    },
    chooseSettlement() {
      if (this.saving) return;
      uni.showActionSheet({
        itemList: ["日结", "压薪日结", "月结"],
        success: ({ tapIndex }) => {
          const values = [
            { label: "日结", type: "daily" },
            { label: "压薪日结", type: "heldBack" },
            { label: "月结", type: "month" },
          ];
          this.settleMode = values[tapIndex].label;
          this.settleType = values[tapIndex].type;
        },
      });
    },
    saveSettings() {
      const data = {
        settleMode: this.settleMode,
        type: this.settleType,
        signMode: this.signMode,
        phoneNotify: this.phoneNotify,
        signNotify: this.signNotify,
        startRemind: this.startRemind,
        settleNotify: this.settleNotify,
      };
      uni.setStorageSync("recruitDraftSettings", {
        ...(uni.getStorageSync("recruitDraftSettings") || {}),
        ...data,
      });
      uni.$emit("recruitSettingsSaved", data);
      uni.showToast({ title: "设置已保存", icon: "success" });
      setTimeout(() => uni.navigateBack(), 350);
    },
  },
};
</script>

<style lang="scss" scoped>
.page {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #f5f6f8;
  color: #222;
}

.nav-header {
  box-sizing: border-box;
  min-height: 64px;
  padding-right: 16px;
  padding-bottom: 0;
  padding-left: 16px;
  display: flex;
  align-items: center;
  position: relative;
  flex-shrink: 0;
}

.nav-back,
.nav-placeholder {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-back-glyph {
  display: block;
  color: #222;
  font-size: 30px;
  font-weight: 300;
  line-height: 34px;
}
.nav-placeholder { margin-left: auto; }
.help-icon { color: #ccc; font-size: 18px; font-weight: 700; }
.nav-title { font-size: 17px; line-height: 22px; font-weight: 700; }

.page-content {
  flex: 1;
  min-height: 0;
  box-sizing: border-box;
  padding: 0 0 calc(16px + env(safe-area-inset-bottom));
  background: #f7f8fa;
}

.group { margin-top: 12px; }
.group-title { padding: 12px 16px 6px; color: #999; font-size: 13px; }
.card { margin: 0 12px; overflow: hidden; border-radius: 12px; background: #fff; }
.icon-row, .nav-row { display: flex; align-items: center; padding: 12px 16px; border-bottom: 1px solid #f5f5f5; }
.icon-row:last-child, .nav-row:last-child { border-bottom: none; }
.icon-box { width: 36px; height: 36px; margin-right: 12px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 16px; flex-shrink: 0; }
.icon-box.orange { background: #fff3ed; color: #ff6b35; }
.icon-box.blue { background: #ebf5ff; color: #1677ff; }
.icon-box.green { background: #e8f8ef; color: #52c41a; }
.icon-info { flex: 1; min-width: 0; }
.icon-title { display: block; color: #333; font-size: 14px; font-weight: 500; }
.icon-desc { display: block; margin-top: 2px; color: #999; font-size: 12px; }
.seg-control { display: flex; padding: 3px; border-radius: 6px; background: #f5f5f5; }
.seg-item { padding: 6px 8px; border-radius: 4px; color: #666; font-size: 12px; white-space: nowrap; }
.seg-item.on { color: #ff6b35; background: #fff; box-shadow: 0 1px 2px rgba(0,0,0,.1); }
.toggle-switch { position: relative; width: 44px; height: 24px; flex-shrink: 0; border-radius: 12px; background: #e0e0e0; }
.toggle-switch.on { background: #ff6b35; }
.toggle-dot { position: absolute; top: 2px; left: 2px; width: 20px; height: 20px; border-radius: 50%; background: #fff; box-shadow: 0 2px 4px rgba(0,0,0,.15); transition: left .2s; }
.toggle-switch.on .toggle-dot { left: 22px; }
.nav-row { justify-content: space-between; }
.row-label { color: #333; font-size: 14px; }
.row-value { color: #333; font-size: 14px; }
.content-space { height: 16px; }
.bottom-bar { flex-shrink: 0; padding: 12px 16px calc(30px + env(safe-area-inset-bottom)); border-top: 1px solid #f0f0f0; background: #fff; }
.save-btn { width: 100%; height: 46px; border: none; border-radius: 23px; color: #fff; background: linear-gradient(135deg, #ffd700, #ffa500); font-size: 15px; font-weight: 600; }
</style>
