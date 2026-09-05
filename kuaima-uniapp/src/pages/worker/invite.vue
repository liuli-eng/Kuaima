<template>
  <view class="page"
    ><AppNavBar title="邀请好友" :show-back="true" /><view class="hero"
      ><text class="title">邀请好友，各得奖励</text
      ><text>好友注册并完成首单后，你可获得奖励</text
      ><view class="code">KM2026</view><button @click="copy">复制邀请码</button
      ><button class="poster-btn" @click="poster">生成邀请海报</button></view
    ><view class="card"
      ><text class="section">邀请记录</text
      ><view v-for="item in records" :key="item.id" class="row"
        ><text>{{ item.phone }}</text
        ><text :class="item.done ? 'done' : 'pending'">{{
          item.done ? "已完成" : "待完成"
        }}</text></view
      ></view
    ></view
  >
</template>
<script setup>
import { onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import { getInviteCode, listInviteRelations } from "@/api/backend";
const records = ref([
  { id: 1, phone: "138****5678", done: true },
  { id: 2, phone: "139****1234", done: false },
]);
const inviteCode = ref("KM2026");
onMounted(async () => {
  const userId = uni.getStorageSync("userId") || "2001";
  try {
    const code = await getInviteCode(userId);
    if (code?.code) inviteCode.value = code.code;
  } catch (_) {}
  try {
    const result = await listInviteRelations(userId);
    if (Array.isArray(result)) records.value = result;
  } catch (_) {}
});
function copy() {
  uni.setClipboardData({
    data: inviteCode.value,
    success: () => uni.showToast({ title: "邀请码已复制", icon: "success" }),
  });
}
function poster() {
  uni.navigateTo({ url: "/pages/worker/invite-poster" });
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #fff8e6;
}
.hero {
  margin: 24rpx;
  padding: 46rpx 30rpx;
  text-align: center;
  border-radius: 24rpx;
  background: linear-gradient(135deg, #ff8a45, #ffbd62);
  color: #fff;
  box-shadow: 0 10rpx 24rpx rgba(255, 107, 53, 0.18);
}
.title {
  display: block;
  font-size: 38rpx;
  font-weight: 800;
  margin-bottom: 14rpx;
}
.hero text:nth-child(2) {
  font-size: 23rpx;
}
.code {
  margin: 34rpx auto 24rpx;
  padding: 18rpx;
  width: 280rpx;
  border-radius: 12rpx;
  background: #ffffff33;
  font-size: 38rpx;
  letter-spacing: 8rpx;
  font-weight: 800;
}
.hero button {
  background: #fff;
  color: #ff6b35;
  border: 0;
  border-radius: 40rpx;
}
.card {
  margin: 24rpx;
  padding: 28rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 5rpx 16rpx rgba(88, 64, 32, 0.05);
}
.section {
  display: block;
  font-size: 29rpx;
  font-weight: 700;
  margin-bottom: 18rpx;
}
.row {
  display: flex;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f1f1f1;
  font-size: 25rpx;
}
.done {
  color: #52c41a;
}
.pending {
  color: #ff8a00;
}
</style>
