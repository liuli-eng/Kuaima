<template>
  <view class="page">
    <AppNavBar :title="title" :show-back="true" />
    <scroll-view scroll-y class="content"
      ><view class="hero"
        ><text class="hero-icon">{{ icon }}</text
        ><text class="hero-title">{{ headline }}</text
        ><text class="hero-desc">{{ description }}</text></view
      ><view
        v-for="item in items"
        :key="item.title"
        class="card"
        @click="item.action && item.action()"
        ><text class="card-icon">{{ item.icon || "•" }}</text
        ><view class="card-main"
          ><text class="card-title">{{ item.title }}</text
          ><text v-if="item.desc" class="card-desc">{{ item.desc }}</text></view
        ><text v-if="item.arrow !== false" class="arrow">›</text></view
      ><button v-if="buttonText" class="primary" @click="submit">
        {{ buttonText }}
      </button></scroll-view
    >
  </view>
</template>
<script setup>
import AppNavBar from "@/components/AppNavBar.vue";
const props = defineProps({
  title: String,
  icon: { type: String, default: "✦" },
  headline: String,
  description: String,
  items: { type: Array, default: () => [] },
  buttonText: String,
});
function submit() {
  uni.showToast({ title: "操作成功", icon: "success" });
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f8f4ed;
}
.content {
  height: calc(100vh - 176rpx);
  padding: 24rpx;
  box-sizing: border-box;
}
.hero {
  padding: 36rpx 28rpx;
  border-radius: 24rpx;
  background: linear-gradient(135deg, #ffbd73, #ff8c4d);
  color: #fff;
  box-shadow: 0 10rpx 24rpx rgba(255, 107, 53, 0.16);
}
.hero-icon {
  display: block;
  font-size: 58rpx;
}
.hero-title {
  display: block;
  margin-top: 10rpx;
  font-size: 34rpx;
  font-weight: 800;
}
.hero-desc {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  opacity: 0.9;
}
.card {
  display: flex;
  align-items: center;
  margin-top: 18rpx;
  padding: 26rpx 22rpx;
  border-radius: 20rpx;
  background: #fff;
  box-shadow: 0 5rpx 16rpx rgba(88, 64, 32, 0.05);
}
.card-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64rpx;
  height: 64rpx;
  margin-right: 18rpx;
  border-radius: 18rpx;
  background: #fff1e6;
  color: #ff6b35;
  font-size: 28rpx;
}
.card-main {
  flex: 1;
}
.card-title {
  display: block;
  color: #333;
  font-size: 27rpx;
  font-weight: 600;
}
.card-desc {
  display: block;
  margin-top: 7rpx;
  color: #999;
  font-size: 21rpx;
}
.arrow {
  color: #aaa;
  font-size: 34rpx;
}
.primary {
  margin-top: 28rpx;
  width: 100%;
  height: 84rpx;
  border: 0;
  border-radius: 44rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: #fff;
  font-size: 29rpx;
}
</style>
