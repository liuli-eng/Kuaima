<template>
  <view class="wb-header" :style="{ paddingTop: statusBarHeight + 'px' }">
    <view class="wb-back" @click="onBack">
      <image class="wb-back-icon" src="/static/icons/boss-recruit-settings/chevron-left.svg" mode="aspectFit" />
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
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px 12px;
  background: transparent;
  flex-shrink: 0;
}

.wb-back {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.wb-back-icon {
  width: 14px;
  height: 14px;
}

.wb-title {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  font-size: 17px;
  font-weight: 600;
  color: #1A1A1A;
}

.wb-header-right {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-shrink: 0;
}
</style>
