<template>
  <view class="page"
    ><AppNavBar title="设置" :show-back="true" /><view class="menu"
      ><view class="row" @click="go('/pages/worker/user-agreement')"
        ><text>用户服务协议</text><text>›</text></view
      ><view class="row" @click="go('/pages/worker/privacy')"
        ><text>隐私政策</text><text>›</text></view
      ><view class="row" @click="goSecurity"
        ><text>通知与账号安全</text><text>›</text></view
      ><view class="row" @click="clear"
        ><text>清除本地缓存</text><text>›</text></view
      ></view
    ><view class="menu"
      ><view class="row" @click="go('/pages/worker/blacklist')"
        ><text>黑名单管理</text><text>›</text></view
      ><view class="row danger" @click="go('/pages/worker/account-cancel')"
        ><text>注销账号</text><text>›</text></view
      ><view
        class="row danger"
        :class="{ disabled: logoutLoading }"
        @click="confirmLogout"
        ><text>{{ logoutLoading ? "退出中…" : "退出登录" }}</text
        ><text>›</text></view
      ></view
    ></view
  >
</template>
<script setup>
import { ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import { logout } from "@/api/auth";

const logoutLoading = ref(false);
function go(url) {
  uni.navigateTo({ url });
}
function goSecurity() {
  uni.navigateTo({ url: "/pages/worker/notification-settings" });
}
function clear() {
  uni.showModal({
    title: "清除缓存",
    content: "仅清除本地偏好设置，不会影响账号数据。",
    success: ({ confirm }) => {
      if (confirm) {
        uni.removeStorageSync("workerIntent");
        uni.showToast({ title: "已清除", icon: "success" });
      }
    },
  });
}
function confirmLogout() {
  if (logoutLoading.value) return;
  uni.showModal({
    title: "提示",
    content: "确定要退出登录吗？",
    success: async ({ confirm }) => {
      if (!confirm) return;
      logoutLoading.value = true;
      try {
        await logout();
      } finally {
        logoutLoading.value = false;
      }
    },
  });
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f8f4ed;
}
.menu {
  margin: 24rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 5rpx 16rpx rgba(88, 64, 32, 0.05);
}
.row {
  display: flex;
  justify-content: space-between;
  padding: 30rpx 26rpx;
  border-bottom: 1rpx solid #f1f1f1;
  color: #444;
  font-size: 27rpx;
}
.row:last-child {
  border: 0;
}
.row.danger text:first-child {
  color: #ff6b35;
}
.row.disabled {
  opacity: 0.55;
}
.row text:last-child {
  font-size: 34rpx;
  color: #aaa;
}
</style>
