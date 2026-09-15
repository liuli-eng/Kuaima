<template>
  <view class="page">
    <view class="nav-header" :style="{ paddingTop: `${statusBarHeight + 4}px` }">
      <view class="nav-back" @click="goBack">‹</view>
      <view class="nav-center">
        <text class="nav-title">招工设置</text>
        <text class="nav-sub">发布招工将默认按如下设置</text>
      </view>
      <view class="nav-placeholder" />
    </view>

    <scroll-view scroll-y class="page-content">
      <view v-if="loading" class="state">设置加载中...</view>
      <template v-else>
        <text class="section-title">零工报名时</text>
        <view class="set-grid">
          <view v-for="item in signupSettings" :key="item.key" class="set-card" @click="openSetting(item.key)">
            <image class="set-icon" :src="item.icon" mode="aspectFit" />
            <text class="set-name">{{ item.name }}</text>
            <text class="set-desc">{{ item.desc }}</text>
            <text class="set-value" :class="{ active: item.active }">{{ item.value }}</text>
          </view>
        </view>

        <text class="section-title work-title">零工开工时</text>
        <view class="set-grid bottom-grid">
          <view v-for="item in workSettings" :key="item.key" class="set-card" @click="openSetting(item.key)">
            <image class="set-icon" :src="item.icon" mode="aspectFit" />
            <text class="set-name">{{ item.name }}</text>
            <text class="set-desc">{{ item.desc }}</text>
            <text class="set-value" :class="{ active: item.active }">{{ item.value }}</text>
          </view>
        </view>
      </template>
    </scroll-view>
  </view>
</template>

<script>
import {
  getBossRecruitSettings,
  updateBossRecruitSettings,
} from "@/api/backend";
import { handleTokenInvalid } from "@/api/auth";

const ICON_ROOT = "/static/icons/boss-recruit-settings";

export default {
  data() {
    return {
      statusBarHeight: 0,
      loading: false,
      saving: false,
      phoneMode: "零工需打电话",
      backupPhone: "未设置",
      stopTime: "开工后3小时",
      dndEnabled: false,
      startCodeEnabled: false,
      earlyCodeEnabled: false,
      attendanceMode: "按工作设定时间",
      faceClockEnabled: true,
      settleMode: "日结",
      settleType: "daily",
      signMode: "auto",
      phoneNotify: true,
      signNotify: true,
      startRemind: true,
      settleNotify: true,
    };
  },
  computed: {
    signupSettings() {
      return [
        { key: "phone", icon: `${ICON_ROOT}/phone.svg`, name: "是否接听电话", desc: "接单时是否需要电话沟通", value: this.phoneMode, active: true },
        { key: "contact", icon: `${ICON_ROOT}/mobile-screen.svg`, name: "备用联系人", desc: "零工电话报名、问路等", value: this.backupPhone, active: this.backupPhone !== "未设置" },
        { key: "stop", icon: `${ICON_ROOT}/circle-user.svg`, name: "自动停招时间", desc: "到时间自动暂停招人", value: this.stopTime, active: true },
        { key: "dnd", icon: `${ICON_ROOT}/bell-slash.svg`, name: "夜间免打扰", desc: "禁止拨打电话时间", value: this.dndEnabled ? "已开启" : "未开启", active: this.dndEnabled },
      ];
    },
    workSettings() {
      return [
        { key: "startCode", icon: `${ICON_ROOT}/flag.svg`, name: "开工码", desc: "防止提前开工、欺诈打卡", value: this.startCodeEnabled ? "已开启" : "未开启", active: this.startCodeEnabled },
        { key: "earlyCode", icon: `${ICON_ROOT}/person-running.svg`, name: "早退码", desc: "防止不打招呼早退跑路", value: this.earlyCodeEnabled ? "已开启" : "未开启", active: this.earlyCodeEnabled },
        { key: "attendance", icon: `${ICON_ROOT}/stopwatch.svg`, name: "考勤计时方式", desc: "选择报酬计算方式", value: this.attendanceMode, active: true },
        { key: "face", icon: `${ICON_ROOT}/face-smile.svg`, name: "人脸打卡", desc: "防止代打卡", value: this.faceClockEnabled ? "已开启" : "未开启", active: this.faceClockEnabled },
      ];
    },
  },
  async onLoad() {
    const info = typeof uni.getWindowInfo === "function" ? uni.getWindowInfo() : uni.getSystemInfoSync();
    this.statusBarHeight = Number(info.statusBarHeight || 0);
    await this.loadSettings();
  },
  methods: {
    goBack() {
      uni.navigateBack();
    },
    applySettings(settings = {}) {
      ["phoneMode", "backupPhone", "stopTime", "attendanceMode", "settleMode", "signMode"].forEach((key) => {
        if (settings[key] !== undefined && settings[key] !== null) this[key] = settings[key];
      });
      ["dndEnabled", "startCodeEnabled", "earlyCodeEnabled", "faceClockEnabled", "phoneNotify", "signNotify", "startRemind", "settleNotify"].forEach((key) => {
        if (typeof settings[key] === "boolean") this[key] = settings[key];
      });
      if (settings.type) this.settleType = settings.type;
    },
    async loadSettings() {
      this.loading = true;
      try {
        const settings = await getBossRecruitSettings();
        this.applySettings(settings || {});
        uni.setStorageSync("recruitSettings", settings || {});
      } catch (error) {
        this.handleError(error, "招工设置加载失败");
      } finally {
        this.loading = false;
      }
    },
    openSetting(key) {
      if (key === "contact") {
        uni.navigateTo({ url: "/pages/boss/backup-phone" });
        return;
      }
      const choices = {
        phone: { field: "phoneMode", items: ["零工需打电话", "零工无需打电话"] },
        stop: { field: "stopTime", items: ["开工时", "开工后1小时", "开工后3小时", "手动停招"] },
        attendance: { field: "attendanceMode", items: ["按工作设定时间", "按实际打卡时间"] },
      };
      if (choices[key]) {
        const config = choices[key];
        uni.showActionSheet({
          itemList: config.items,
          success: ({ tapIndex }) => {
            this[config.field] = config.items[tapIndex];
            this.saveSettings();
          },
        });
        return;
      }
      const toggles = { dnd: "dndEnabled", startCode: "startCodeEnabled", earlyCode: "earlyCodeEnabled", face: "faceClockEnabled" };
      if (toggles[key]) {
        this[toggles[key]] = !this[toggles[key]];
        this.saveSettings();
      }
    },
    async saveSettings() {
      if (this.saving) return;
      const data = {
        phoneMode: this.phoneMode,
        backupPhone: this.backupPhone,
        stopTime: this.stopTime,
        dndEnabled: this.dndEnabled,
        startCodeEnabled: this.startCodeEnabled,
        earlyCodeEnabled: this.earlyCodeEnabled,
        attendanceMode: this.attendanceMode,
        faceClockEnabled: this.faceClockEnabled,
        settleMode: this.settleMode,
        type: this.settleType,
        signMode: this.signMode,
        phoneNotify: this.phoneNotify,
        signNotify: this.signNotify,
        startRemind: this.startRemind,
        settleNotify: this.settleNotify,
      };
      this.saving = true;
      try {
        const saved = await updateBossRecruitSettings(data);
        const result = saved || data;
        this.applySettings(result);
        uni.setStorageSync("recruitSettings", result);
        uni.$emit("recruitSettingsSaved", result);
        uni.showToast({ title: "设置已保存", icon: "success" });
      } catch (error) {
        this.handleError(error, "设置保存失败");
      } finally {
        this.saving = false;
      }
    },
    handleError(error, fallback) {
      const status = Number(error?.code || error?.statusCode);
      if (status === 401) return handleTokenInvalid({ role: "boss" });
      uni.showToast({ title: status === 403 ? "无权操作" : error?.message || fallback, icon: "none" });
    },
  },
};
</script>

<style lang="scss" scoped>
.page { width: 100%; height: 100vh; display: flex; flex-direction: column; overflow: hidden; background: #f5f6f8; color: #222; }
.nav-header { min-height: 59px; padding: 4px 16px; box-sizing: border-box; display: flex; align-items: center; position: relative; flex-shrink: 0; }
.nav-back, .nav-placeholder { width: 34px; height: 34px; display: flex; align-items: center; justify-content: center; font-size: 30px; }
.nav-placeholder { margin-left: auto; }
.nav-center { position: absolute; left: 50%; transform: translateX(-50%); text-align: center; display: flex; flex-direction: column; white-space: nowrap; }
.nav-title { font-size: 17px; font-weight: 700; }
.nav-sub { margin-top: 1px; color: #999; font-size: 11px; }
.page-content { flex: 1; min-height: 0; padding: 4px 16px calc(20px + env(safe-area-inset-bottom)); box-sizing: border-box; }
.state { padding: 100px 0; color: #999; text-align: center; }
.section-title { display: block; margin: 8px 2px; font-size: 14px; font-weight: 700; }
.work-title { margin-top: 18px; }
.set-grid { display: flex; flex-wrap: wrap; gap: 10px; }
.set-card { width: calc((100% - 10px) / 2); min-height: 139px; padding: 14px 10px 10px; box-sizing: border-box; border-radius: 14px; background: #fff; display: flex; flex-direction: column; align-items: center; text-align: center; }
.set-icon { width: 24px; height: 28px; margin-bottom: 6px; }
.set-name { font-size: 14px; font-weight: 700; }
.set-desc { margin: 3px 0 8px; color: #b0b0b0; font-size: 10px; line-height: 13px; }
.set-value { width: 100%; margin-top: auto; padding: 6px 4px; box-sizing: border-box; border-radius: 8px; background: #f5f6f8; color: #999; font-size: 12px; }
.set-value.active { color: #ff8c00; }
.bottom-grid { padding-bottom: 20px; }
</style>
