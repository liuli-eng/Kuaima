<template>
  <view class="safe-action" :style="{ paddingBottom: `${bottomInset + 24}px` }"
    ><slot
  /></view>
</template>
<script setup>
const bottomInset = uni.getSystemInfoSync().safeAreaInsets?.bottom || 0;
</script>
<style scoped>
.safe-action {
  width: 100%;
  box-sizing: border-box;
  padding-left: 32rpx;
  padding-right: 32rpx;
  background: #fff;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 999;
}
</style>
