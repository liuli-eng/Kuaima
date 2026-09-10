<template>
  <view class="tabbar">
    <view
      v-for="item in items"
      :key="item.key"
      :class="['item', { active: current === item.key }]"
      @click="go(item)"
    >
      <view class="icon-wrap"
        ><image
          class="tab-icon-image"
          :src="current === item.key ? item.activeIcon : item.icon"
          mode="aspectFit"
        /></view
      >
      <text>{{ item.label }}</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({ current: { type: String, required: true } });

const items = [
  { key: "home", label: "抢日结", url: "/pages/worker/home", icon: "/static/icons/boss-profile/house-gray.svg", activeIcon: "/static/icons/boss-home/house.svg" },
  { key: "orders", label: "接单", url: "/pages/worker/orders", icon: "/static/icons/worker-profile/clipboard-gray.svg", activeIcon: "/static/icons/worker-profile/clipboard-white.svg" },
  {
    key: "messages",
    label: "消息",
    url: "/pages/worker/messages",
    icon: "/static/icons/boss-home/comment.svg",
    activeIcon: "/static/icons/worker-profile/comment-white.svg",
  },
  { key: "profile", label: "我的", url: "/pages/worker/profile", icon: "/static/icons/boss-home/smile.svg", activeIcon: "/static/icons/boss-profile/smile-active.svg" },
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

.tab-icon-image {
  width: 44rpx;
  height: 44rpx;
}
</style>
