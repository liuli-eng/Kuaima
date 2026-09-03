<template>
  <view class="page"
    ><AppNavBar title="邀请海报" :show-back="true" /><view class="poster"
      ><text class="brand">快马日结</text
      ><text class="title">真老板 · 真工价 · 真日结</text
      ><view class="code">KM2026</view><text>扫码注册并填写邀请码</text></view
    ><view class="actions"
      ><button @click="save">保存海报</button
      ><button @click="copy">复制邀请文案</button></view
    ></view
  >
</template>
<script setup>
import AppNavBar from "@/components/AppNavBar.vue";
function save() {
  uni.showToast({ title: "海报保存功能待接入图片生成", icon: "none" });
}
function copy() {
  uni.setClipboardData({ data: "快来加入快马日结，邀请码 KM2026" });
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}
.poster {
  margin: 40rpx auto;
  width: 590rpx;
  height: 760rpx;
  box-sizing: border-box;
  padding: 70rpx 40rpx;
  text-align: center;
  border-radius: 30rpx;
  background: linear-gradient(160deg, #fff7e6, #ffbb62);
  color: #8b4513;
}
.brand {
  display: block;
  font-size: 38rpx;
  font-weight: 800;
}
.title {
  display: block;
  margin-top: 160rpx;
  font-size: 42rpx;
  font-weight: 800;
}
.code {
  margin: 90rpx auto 24rpx;
  padding: 22rpx;
  border-radius: 18rpx;
  background: #fff;
  font-size: 48rpx;
  font-weight: 800;
  letter-spacing: 8rpx;
}
.poster text:last-child {
  font-size: 24rpx;
}
.actions {
  display: flex;
  gap: 18rpx;
  padding: 0 32rpx;
}
.actions button {
  flex: 1;
  border-radius: 40rpx;
  background: #ff6b35;
  color: #fff;
  font-size: 25rpx;
}
</style>
