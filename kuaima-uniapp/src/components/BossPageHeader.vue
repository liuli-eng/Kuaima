<template>
  <view class="wb-header" :style="{ paddingTop: statusBarHeight + 'px' }">
    <view class="wb-back" @click="onBack">
      <text class="wb-back-icon">‹</text>
    </view>
    <view class="wb-title">{{ title }}</view>
    <view class="wb-header-right">
      <slot name="right" />
    </view>
  </view>
</template>

<script>
export default {
  name: "BossPageHeader",
  props: {
    title: { type: String, default: "" },
  },
  data() {
    return {
      statusBarHeight: (uni.getSystemInfoSync().statusBarHeight || 0),
    };
  },
  methods: {
    onBack() {
      if (this.$attrs.onBack) {
        this.$emit("back");
        return;
      }
      uni.navigateBack({
        delta: 1,
        fail: () => uni.reLaunch({ url: "/pages/boss/projects" }),
      });
    },
  },
};
</script>

<style lang="scss" scoped>
.wb-header {
  box-sizing: border-box;
  display: flex;
  align-items: center;
  height: 54px;
  padding: 0 12px 0 8px;
  background: #fff;
  border-bottom: 0.5px solid #f0f0f0;
  flex-shrink: 0;
}

.wb-back {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.wb-back-icon {
  font-size: 30px;
  line-height: 1;
  color: #333;
  font-weight: 700;
}

.wb-title {
  flex: 1;
  text-align: center;
  font-size: 17px;
  font-weight: 600;
  color: #222;
}

.wb-header-right {
  width: 36px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-shrink: 0;
}
</style>
