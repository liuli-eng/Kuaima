<template>
  <view class="page"
    ><AppNavBar title="注销账号" :show-back="true" /><view class="warning"
      ><text class="title">注销前请确认</text
      ><text
        >账号注销后，身份信息、订单记录和钱包服务将无法继续使用。请先完成所有订单并提现余额。</text
      ></view
    ><view class="checks"
      ><text v-for="item in checks" :key="item">✓ {{ item }}</text></view
    ><SafeBottomAction
      ><button class="cancel" @click="submit">
        申请注销账号
      </button></SafeBottomAction
    ></view
  >
</template>
<script setup>
import AppNavBar from "@/components/AppNavBar.vue";
import SafeBottomAction from "@/components/SafeBottomAction.vue";
import { cancelAccount } from "@/api/backend";
const checks = [
  "当前没有进行中的订单",
  "钱包余额已经处理",
  "已阅读账号注销说明",
];
function submit() {
  uni.showModal({
    title: "确认注销",
    content: "账号注销属于不可恢复操作，确定继续提交申请吗？",
    confirmColor: "#e34d59",
    success: async ({ confirm }) => {
      if (!confirm) return;
      try {
        await cancelAccount(
          uni.getStorageSync("userId") || "2001",
          "用户主动注销",
        );
        uni.showToast({ title: "注销申请已提交", icon: "success" });
      } catch (error) {
        uni.showToast({ title: error.message || "注销失败", icon: "none" });
      }
    },
  });
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}
.warning,
.checks {
  margin: 24rpx;
  padding: 28rpx;
  border-radius: 20rpx;
  background: #fff;
}
.warning {
  background: #fff1f0;
  color: #8c1d18;
}
.title {
  display: block;
  margin-bottom: 16rpx;
  font-size: 32rpx;
  font-weight: 800;
}
.warning text:last-child {
  display: block;
  font-size: 24rpx;
  line-height: 1.8;
}
.checks text {
  display: block;
  padding: 18rpx 0;
  color: #555;
  font-size: 25rpx;
}
.cancel {
  width: 100%;
  height: 84rpx;
  border: 0;
  border-radius: 44rpx;
  background: #e34d59;
  color: #fff;
  font-size: 29rpx;
}
</style>
