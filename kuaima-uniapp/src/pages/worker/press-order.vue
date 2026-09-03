<template>
  <view class="page"
    ><AppNavBar title="压薪日结订单" :show-back="true" /><view class="tip"
      >先工作后结算，工期结束后按约定方式结算</view
    ><scroll-view scroll-y class="content"
      ><view
        v-for="item in orders"
        :key="item.id"
        class="card"
        @click="open(item)"
        ><view class="head"
          ><text class="badge">压薪{{ item.pressDays }}天</text
          ><text class="status">{{ item.statusText }}</text></view
        ><text class="title">{{ item.title }}</text
        ><text class="line">{{ item.time }}</text
        ><text class="line">{{ item.address }}</text
        ><view class="footer"
          ><text>{{ item.worked }}/{{ item.duration }}天</text
          ><text class="money">¥{{ item.amount }}/天</text></view
        ></view
      ></scroll-view
    ></view
  >
</template>
<script setup>
import { ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
const orders = ref([
  {
    id: "p1",
    title: "餐饮服务员",
    time: "09:00-18:00",
    address: "松江区方松街道",
    amount: 170,
    pressDays: 1,
    worked: 3,
    duration: 7,
    statusText: "进行中",
  },
  {
    id: "p2",
    title: "电商打包员",
    time: "10:00-20:00",
    address: "松江区洞泾镇",
    amount: 190,
    pressDays: 3,
    worked: 7,
    duration: 7,
    statusText: "待结算",
  },
]);
function open(item) {
  uni.navigateTo({ url: `/pages/worker/order-detail?id=${item.id}` });
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}
.tip {
  margin: 22rpx;
  padding: 22rpx;
  border-radius: 18rpx;
  background: #fff4e6;
  color: #8b4513;
  font-size: 23rpx;
}
.content {
  height: calc(100vh - 280rpx);
  padding: 0 22rpx;
  box-sizing: border-box;
}
.card {
  background: #fff;
  border-radius: 20rpx;
  padding: 26rpx;
  margin-bottom: 18rpx;
}
.head,
.footer {
  display: flex;
  justify-content: space-between;
}
.badge {
  padding: 6rpx 14rpx;
  background: #fff1e8;
  color: #ff6b35;
  border-radius: 12rpx;
  font-size: 21rpx;
}
.status {
  color: #722ed1;
  font-size: 23rpx;
}
.title {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  margin: 20rpx 0;
}
.line {
  display: block;
  color: #777;
  font-size: 23rpx;
  margin-top: 10rpx;
}
.footer {
  margin-top: 20rpx;
  padding-top: 18rpx;
  border-top: 1rpx solid #eee;
  color: #888;
  font-size: 22rpx;
}
.money {
  color: #ff4757;
  font-size: 29rpx;
  font-weight: 700;
}
</style>
