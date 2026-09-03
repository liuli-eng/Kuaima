<template>
  <view class="nav" :style="{ paddingTop: `${statusBarHeight}px` }">
    <view class="nav-inner"
      ><button v-if="showBack" class="back" @click="uni.navigateBack()">
        ‹</button
      ><text class="title">{{ title }}</text
      ><view class="nav-space"
    /></view>
  </view>
</template>
<script setup>
defineProps({
  title: { type: String, default: "快马日结" },
  showBack: Boolean,
});
const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0;
</script>
<style scoped>
.nav {
  background: #fff;
  box-sizing: border-box;
}
.nav-inner {
  height: 88rpx;
  position: relative;
  display: flex;
  align-items: center;
  padding: 0 32rpx;
}
.title {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  max-width: 60%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 34rpx;
  font-weight: 700;
  color: #8b4513;
}
.back {
  width: 64rpx;
  height: 64rpx;
  margin: 0;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: #f8f8f8;
  color: #555;
  font-size: 52rpx;
  line-height: 54rpx;
}
.nav-space {
  width: 64rpx;
  margin-left: auto;
}
</style>
