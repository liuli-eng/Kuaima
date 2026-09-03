<template>
  <view class="tabbar">
    <view
      v-for="item in items"
      :key="item.key"
      :class="['item', { active: current === item.key }]"
      @click="go(item)"
    >
      <view class="icon-wrap"
        ><view class="icon" :class="`icon-${item.key}`"
          ><text v-if="item.key === 'messages'">•••</text></view
        ></view
      >
      <text>{{ item.label }}</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({ current: { type: String, required: true } });

const items = [
  { key: "home", label: "抢日结", url: "/pages/worker/home" },
  { key: "orders", label: "接单", url: "/pages/worker/orders" },
  {
    key: "messages",
    label: "消息",
    url: "/pages/worker/messages",
  },
  { key: "profile", label: "我的", url: "/pages/worker/profile" },
];

const current = computed(() => props.current);

function go(item) {
  if (item.key !== current.value) {
    uni.redirectTo({ url: item.url });
  }
}
</script>

<style scoped>
.tabbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  height: 83px;
  padding: 0 12rpx calc(20px + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.98);
  border-top: 0.5px solid rgba(0, 0, 0, 0.05);
  backdrop-filter: blur(20px);
  z-index: 20;
}

.item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 10px;
}

.item.active {
  color: #ff6b35;
  font-weight: 700;
}

.icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64rpx;
  height: 64rpx;
  margin-bottom: 4rpx;
  border-radius: 50%;
}

.item.active .icon-wrap {
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  box-shadow: 0 8rpx 20rpx rgba(255, 107, 53, 0.3);
}

.icon {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44rpx;
  height: 44rpx;
  color: #999;
  font-size: 34rpx;
  line-height: 1;
}

.item.active .icon {
  color: #fff;
}
.icon-home {
  width: 30rpx;
  height: 26rpx;
  margin-top: 8rpx;
  border: 4rpx solid currentColor;
  border-top: 0;
  border-radius: 3rpx;
  box-sizing: border-box;
}
.icon-home::before {
  content: "";
  position: absolute;
  left: 2rpx;
  top: -12rpx;
  width: 20rpx;
  height: 20rpx;
  border-top: 4rpx solid currentColor;
  border-left: 4rpx solid currentColor;
  transform: rotate(45deg);
}
.icon-orders {
  border: 4rpx solid currentColor;
  border-radius: 5rpx;
  box-sizing: border-box;
}
.icon-orders::before {
  content: "";
  position: absolute;
  left: 6rpx;
  right: 6rpx;
  top: 8rpx;
  height: 3rpx;
  background: currentColor;
  box-shadow:
    0 8rpx 0 currentColor,
    0 16rpx 0 currentColor;
}
.icon-messages {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38rpx;
  height: 30rpx;
  border: 4rpx solid currentColor;
  border-radius: 16rpx;
  box-sizing: border-box;
  font-size: 18rpx;
  line-height: 1;
}
.icon-messages::after {
  content: "";
  position: absolute;
  left: 4rpx;
  bottom: -7rpx;
  width: 10rpx;
  height: 10rpx;
  border-left: 4rpx solid currentColor;
  transform: skew(-28deg);
}
.icon-profile {
  border: 4rpx solid currentColor;
  border-radius: 50%;
  box-sizing: border-box;
}
.icon-profile::before {
  content: "••";
  position: absolute;
  left: 5rpx;
  top: -3rpx;
  letter-spacing: 4rpx;
  font-size: 16rpx;
}
.icon-profile::after {
  content: "⌣";
  position: absolute;
  left: 7rpx;
  top: 10rpx;
  font-size: 20rpx;
  font-weight: 700;
}
</style>
