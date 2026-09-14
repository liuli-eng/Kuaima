<template>
  <view class="safe-action" :style="{ paddingBottom: `${bottomInset + 24}px` }">
    <slot />
  </view>
</template>

<script setup>
function getBottomInset() {
  try {
    return Number(uni.getWindowInfo().safeAreaInsets?.bottom || 0);
  } catch (_) {
    return 0;
  }
}

const bottomInset = getBottomInset();
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
