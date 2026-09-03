<template>
  <view class="page"
    ><AppNavBar title="优惠券与奖励" :show-back="true" /><scroll-view
      scroll-y
      class="content"
      ><view v-for="item in coupons" :key="item.id" class="coupon"
        ><view
          ><text class="money">¥{{ item.amount }}</text
          ><text>{{ item.title }}</text></view
        ><text class="use" @click="use(item)">立即使用</text></view
      ><view v-if="!coupons.length" class="empty"
        >暂无可用优惠券</view
      ></scroll-view
    ></view
  >
</template>
<script setup>
import { ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
const coupons = ref([
  { id: 1, amount: "10", title: "报名奖励券 · 满100可用" },
  { id: 2, amount: "5", title: "新用户奖励券" },
]);
function use() {
  uni.reLaunch({ url: "/pages/worker/home" });
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}
.content {
  height: calc(100vh - 176rpx);
  padding: 22rpx;
  box-sizing: border-box;
}
.coupon {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18rpx;
  padding: 28rpx 24rpx;
  background: linear-gradient(135deg, #fff4e6, #ffe4b5);
  border-radius: 20rpx;
  color: #8b4513;
}
.money {
  font-size: 48rpx;
  font-weight: 800;
  margin-right: 16rpx;
}
.use {
  padding: 12rpx 18rpx;
  border-radius: 24rpx;
  background: #fff;
  color: #ff6b35;
  font-size: 22rpx;
}
.empty {
  text-align: center;
  padding: 220rpx;
  color: #aaa;
  font-size: 25rpx;
}
</style>
