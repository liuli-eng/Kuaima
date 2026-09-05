<template>
  <view class="page"
    ><AppNavBar title="快马星级" :show-back="true" /><scroll-view
      scroll-y
      class="content"
      ><view class="level"
        ><text class="tag">当前星级</text><text class="star">{{ level }}星</text
        ><text>星级分 {{ points }}，距离下一星级还差 {{ nextPoints }} 分</text
        ><progress
          :percent="progress"
          activeColor="#ffd700"
          backgroundColor="#ffffff66"
          stroke-width="8" /></view
      ><view class="card"
        ><text class="title">星级权益</text
        ><view v-for="item in benefits" :key="item" class="benefit"
          >✓ {{ item }}</view
        ></view
      ></scroll-view
    ></view
  >
</template>
<script setup>
import { ref, computed, onMounted } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import { getStarLevel } from "@/api/backend";
const level = ref(3);
const points = ref(280);
const nextPoints = ref(200);
const progress = computed(() => Math.min(100, (points.value / 500) * 100));
const benefits = ["优先推荐高薪岗位", "可关注更多优质雇主", "专属客服快速响应"];
onMounted(async () => {
  try {
    const data = await getStarLevel(uni.getStorageSync("userId") || "2001");
    if (data) {
      level.value = data.level ?? level.value;
      points.value = data.points ?? data.current ?? points.value;
      nextPoints.value = data.nextPoints ?? nextPoints.value;
    }
  } catch (_) {}
});
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}
.content {
  height: calc(100vh - 176rpx);
}
.level {
  margin: 24rpx;
  padding: 38rpx 30rpx;
  border-radius: 24rpx;
  background: linear-gradient(135deg, #5346d8, #8d7cff);
  color: #fff;
}
.tag {
  display: inline-block;
  padding: 8rpx 18rpx;
  border-radius: 20rpx;
  background: #ffffff2b;
  font-size: 22rpx;
}
.star {
  display: block;
  font-size: 64rpx;
  font-weight: 800;
  margin: 20rpx 0 8rpx;
  color: #ffd700;
}
.level text:last-of-type {
  font-size: 22rpx;
  display: block;
  margin-bottom: 20rpx;
}
.card {
  margin: 22rpx;
  padding: 28rpx;
  background: #fff;
  border-radius: 20rpx;
}
.title {
  display: block;
  font-size: 29rpx;
  font-weight: 700;
  margin-bottom: 18rpx;
}
.benefit {
  padding: 18rpx 0;
  border-bottom: 1rpx solid #f1f1f1;
  color: #555;
  font-size: 25rpx;
}
</style>
