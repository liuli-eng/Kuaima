<template>
  <view class="page">
    <AppNavBar title="去授权" :show-back="true" />
    <scroll-view scroll-y class="content">
      <view class="intro-card">
        <view class="intro-head"><view class="intro-icon">盾</view><view><text class="intro-title">授权说明</text><text class="intro-desc">将操作权限授予信任的人</text></view></view>
        <text class="intro-line">· 被授权人可代您进行招工、发薪等操作</text>
        <text class="intro-line">· 您可随时查看授权状态并解除授权</text>
        <text class="intro-line">· 建议仅授权给您信任的合作伙伴</text>
      </view>

      <view class="form-card">
        <view class="form-row"><text class="form-label">手机号</text><input v-model="form.phone" class="form-input" maxlength="11" type="number" placeholder="请输入对方手机号" /></view>
        <view class="form-row"><text class="form-label">验证码</text><input v-model="form.code" class="form-input" maxlength="6" type="number" placeholder="请输入验证码" /><text class="code-btn" :class="{ disabled: countdown }" @click="sendCode">{{ countdown ? `${countdown}s 后重发` : "获取验证码" }}</text></view>
        <view class="form-row"><text class="form-label">备注名称</text><input v-model="form.nickname" class="form-input" maxlength="20" placeholder="给授权人起个名称（选填）" /></view>
      </view>

      <view class="perm-card">
        <text class="perm-title">🔑 授权权限范围</text>
        <view v-for="item in permissions" :key="item.value" class="perm-item" :class="{ active: selectedPermissions.includes(item.value) }" @click="togglePermission(item.value)">
          <view class="perm-main"><text class="perm-name">{{ item.label }}</text><text class="perm-desc">{{ item.desc }}</text></view><view class="check">{{ selectedPermissions.includes(item.value) ? "✓" : "" }}</view>
        </view>
      </view>
      <view class="bottom-space" />
    </scroll-view>
    <view class="footer"><button class="confirm" :disabled="submitting" @click="submit">{{ submitting ? "授权中…" : "确认授权" }}</button><text class="agree">点击确认即表示同意《授权服务协议》</text></view>
  </view>
</template>

<script setup>
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import AppNavBar from "@/components/AppNavBar.vue";
import { createBossSubAccount } from "@/api/backend";

const form = ref({ phone: "", code: "", nickname: "" });
const submitting = ref(false);
const countdown = ref(0);
const permissions = [
  { value: "HOME", label: "首页", desc: "查看数据概览、待办提醒" },
  { value: "ORDER", label: "招工订单", desc: "发布岗位、管理报名、订单结算" },
  { value: "MESSAGE", label: "消息", desc: "系统通知、员工沟通" },
  { value: "WORKBENCH", label: "工作台", desc: "发薪结算、考勤管理、报表查看" },
];
const selectedPermissions = ref(["HOME", "ORDER", "MESSAGE"]);

onLoad(() => {});

function togglePermission(value) {
  if (value === "WORKBENCH") {
    selectedPermissions.value = selectedPermissions.value.includes(value) ? [] : permissions.map((item) => item.value);
    return;
  }
  const next = selectedPermissions.value.filter((item) => item !== value);
  if (!selectedPermissions.value.includes(value)) next.push(value);
  selectedPermissions.value = next;
}

function sendCode() {
  if (countdown.value) return;
  if (!/^1\d{10}$/.test(form.value.phone)) return uni.showToast({ title: "请输入正确手机号", icon: "none" });
  uni.showToast({ title: "验证码发送功能待后端短信服务接入", icon: "none" });
  countdown.value = 60;
  const timer = setInterval(() => {
    countdown.value -= 1;
    if (countdown.value <= 0) clearInterval(timer);
  }, 1000);
}

async function submit() {
  if (!/^1\d{10}$/.test(form.value.phone)) return uni.showToast({ title: "请输入正确手机号", icon: "none" });
  if (!form.value.code.trim()) return uni.showToast({ title: "请输入验证码", icon: "none" });
  if (!selectedPermissions.value.length) return uni.showToast({ title: "请选择授权权限", icon: "none" });
  if (submitting.value) return;
  submitting.value = true;
  try {
    await createBossSubAccount({ phone: form.value.phone, code: form.value.code, nickname: form.value.nickname, role: selectedPermissions.value.includes("WORKBENCH") ? "ADMIN" : "OPERATOR" });
    uni.showToast({ title: "授权成功", icon: "success" });
    setTimeout(() => uni.navigateBack(), 600);
  } catch (error) {
    uni.showToast({ title: error?.message || "授权失败，请重试", icon: "none" });
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f3f4f6; display: flex; flex-direction: column; }
.content { flex: 1; min-height: 0; padding-top: 12rpx; box-sizing: border-box; }
.intro-card, .form-card, .perm-card { margin: 0 32rpx 24rpx; background: #fff; border-radius: 24rpx; box-sizing: border-box; }
.intro-card { padding: 28rpx 32rpx; background: linear-gradient(135deg, #fff3e6, #ffe8cc); border: 1rpx solid #ffe0b2; }
.intro-head { display: flex; align-items: center; gap: 20rpx; margin-bottom: 16rpx; }
.intro-icon { width: 72rpx; height: 72rpx; border-radius: 20rpx; color: #fff; background: linear-gradient(135deg, #ff8c5a, #ff6b35); display: flex; align-items: center; justify-content: center; font-size: 26rpx; }
.intro-title, .intro-desc, .intro-line { display: block; }.intro-title { color: #8b4513; font-size: 30rpx; font-weight: 600; }.intro-desc, .intro-line { color: #a0522d; font-size: 22rpx; }.intro-desc { margin-top: 4rpx; }.intro-line { line-height: 1.8; }
.form-card { padding: 4rpx 32rpx; }.form-row { min-height: 100rpx; display: flex; align-items: center; border-bottom: 1rpx solid #f5f5f5; }.form-row:last-child { border-bottom: 0; }.form-label { width: 150rpx; color: #333; font-size: 28rpx; }.form-input { flex: 1; min-width: 0; color: #333; font-size: 28rpx; }.code-btn { margin-left: 12rpx; color: #2563eb; font-size: 24rpx; white-space: nowrap; }.code-btn.disabled { color: #aaa; }
.perm-card { padding: 28rpx 32rpx; }.perm-title { display: block; margin-bottom: 16rpx; color: #333; font-size: 30rpx; font-weight: 600; }.perm-item { display: flex; align-items: center; padding: 22rpx 24rpx; margin-top: 8rpx; background: #fafafa; border: 2rpx solid transparent; border-radius: 16rpx; }.perm-item.active { background: #fff3ed; border-color: #ff6b35; }.perm-main { flex: 1; }.perm-name, .perm-desc { display: block; }.perm-name { color: #333; font-size: 27rpx; }.perm-item.active .perm-name { color: #ff6b35; }.perm-desc { margin-top: 4rpx; color: #999; font-size: 22rpx; }.check { width: 40rpx; height: 40rpx; border: 2rpx solid #d8d8d8; border-radius: 50%; color: #fff; text-align: center; line-height: 40rpx; }.active .check { background: #ff6b35; border-color: #ff6b35; }
.bottom-space { height: 180rpx; }.footer { padding: 20rpx 32rpx calc(28rpx + env(safe-area-inset-bottom)); background: #fff; box-shadow: 0 -2rpx 10rpx rgba(0,0,0,.05); }.confirm { width: 100%; height: 88rpx; margin: 0; color: #fff; background: linear-gradient(135deg, #ff8c5a, #ff6b35); border: 0; border-radius: 44rpx; font-size: 30rpx; }.confirm[disabled] { opacity: .6; }.confirm::after { border: 0; }.agree { display: block; margin-top: 14rpx; color: #999; font-size: 21rpx; text-align: center; }
</style>
