<template>
  <view class="page"
    ><AppNavBar title="实名认证" :show-back="true" /><scroll-view
      scroll-y
      class="content"
      ><view class="hero"
        ><text class="hero-icon">✓</text
        ><text class="hero-title">完成实名认证</text
        ><text class="hero-desc"
          >实名认证后即可报名岗位，保障账号和收入安全</text
        ></view
      ><view class="form"
        ><view class="row"
          ><text class="label">真实姓名</text
          ><input
            v-model="form.name"
            placeholder="请输入真实姓名"
            maxlength="20" /></view
        ><view class="row"
          ><text class="label">身份证号</text
          ><input
            v-model="form.idCard"
            placeholder="请输入身份证号码"
            maxlength="18" /></view></view
      ><view class="tip"
        >请填写本人真实信息，认证信息仅用于平台身份核验</view
      ></scroll-view
    ><SafeBottomAction
      ><button class="submit" :disabled="submitting" @click="submit">
        {{ submitting ? "提交中…" : "提交认证" }}
      </button></SafeBottomAction
    ></view
  >
</template>
<script setup>
import { reactive, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import SafeBottomAction from "@/components/SafeBottomAction.vue";
import { request } from "@/api/http";
const form = reactive({ name: "", idCard: "" });
const submitting = ref(false);
async function submit() {
  if (!form.name.trim())
    return uni.showToast({ title: "请输入真实姓名", icon: "none" });
  if (!/^[1-9]\\d{16}[\\dXx]$/.test(form.idCard))
    return uni.showToast({ title: "请输入正确的身份证号", icon: "none" });
  submitting.value = true;
  try {
    await request({ url: "/worker/realname", method: "POST", data: form });
    uni.setStorageSync("workerRealname", true);
    uni.showToast({ title: "认证成功", icon: "success" });
    setTimeout(() => uni.navigateBack(), 500);
  } catch (error) {
    uni.showToast({ title: error.message || "认证失败", icon: "none" });
  } finally {
    submitting.value = false;
  }
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}
.content {
  height: calc(100vh - 176rpx);
}
.hero {
  text-align: center;
  background: #fff;
  padding: 54rpx 32rpx 44rpx;
}
.hero-icon {
  display: block;
  width: 120rpx;
  height: 120rpx;
  line-height: 120rpx;
  margin: 0 auto 24rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #ffd700, #ffa500);
  color: #fff;
  font-size: 72rpx;
  font-weight: 800;
}
.hero-title {
  display: block;
  font-size: 38rpx;
  font-weight: 800;
  color: #333;
}
.hero-desc {
  display: block;
  margin-top: 14rpx;
  color: #999;
  font-size: 24rpx;
}
.form {
  margin: 24rpx;
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
}
.row {
  display: flex;
  align-items: center;
  padding: 30rpx 26rpx;
  border-bottom: 1rpx solid #f2f2f2;
}
.row:last-child {
  border-bottom: 0;
}
.label {
  width: 160rpx;
  color: #333;
  font-size: 28rpx;
}
.row input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}
.tip {
  margin: 0 36rpx;
  color: #999;
  font-size: 22rpx;
  line-height: 1.7;
}
.submit {
  width: 100%;
  height: 84rpx;
  line-height: 84rpx;
  border: 0;
  border-radius: 48rpx;
  background: linear-gradient(135deg, #ffd700, #ffa500);
  color: #8b4513;
  font-size: 30rpx;
  font-weight: 800;
}
.submit[disabled] {
  background: #bbb;
  color: #fff;
}
</style>
