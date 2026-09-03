<template>
  <view class="page"
    ><AppNavBar title="工资详情" :show-back="true" /><scroll-view
      scroll-y
      class="content"
      ><view class="hero"
        ><text>本月工资</text><text class="amount">¥{{ detail.amount }}</text
        ><text>{{ detail.status }}</text></view
      ><view class="card"
        ><text class="card-title">工作信息</text
        ><view class="row"
          ><text>岗位</text><text>{{ detail.title }}</text></view
        ><view class="row"
          ><text>工作周期</text><text>{{ detail.period }}</text></view
        ><view class="row"
          ><text>工作地点</text><text>{{ detail.address }}</text></view
        ><view class="row"
          ><text>雇主</text><text>{{ detail.employer }}</text></view
        ></view
      ><view class="card"
        ><text class="card-title">工资明细</text
        ><view class="row"
          ><text>基本工资</text><text>¥{{ detail.amount }}</text></view
        ><view class="row"><text>补贴及奖励</text><text>¥0.00</text></view
        ><view class="row total"
          ><text>实发工资</text><text>¥{{ detail.amount }}</text></view
        ></view
      ><button
        class="wallet"
        @click="uni.navigateTo({ url: '/pages/worker/wallet' })"
      >
        查看钱包
      </button></scroll-view
    ></view
  >
</template>
<script setup>
import { ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
const detail = ref({
  title: "仓储管理员",
  period: "2026年8月",
  address: "松江区车墩镇",
  employer: "物流仓储中心",
  amount: "5,500.00",
  status: "已结算",
});
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
.hero,
.card {
  margin-bottom: 20rpx;
  padding: 30rpx 26rpx;
  border-radius: 22rpx;
  background: #fff;
  box-shadow: 0 5rpx 16rpx rgba(88, 64, 32, 0.05);
}
.hero {
  background: linear-gradient(135deg, #ffbd73, #ff8c4d);
  color: #fff;
}
.hero text {
  display: block;
  font-size: 24rpx;
}
.hero .amount {
  margin: 12rpx 0;
  font-size: 58rpx;
  font-weight: 800;
}
.card-title {
  display: block;
  margin-bottom: 8rpx;
  color: #333;
  font-size: 29rpx;
  font-weight: 700;
}
.row {
  display: flex;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f2eee8;
  color: #888;
  font-size: 24rpx;
}
.row:last-child {
  border: 0;
}
.row text:last-child {
  color: #333;
}
.total text {
  color: #ff6b35 !important;
  font-weight: 700;
}
.wallet {
  border: 1rpx solid #ff6b35;
  border-radius: 44rpx;
  background: #fff;
  color: #ff6b35;
  font-size: 28rpx;
}
</style>
