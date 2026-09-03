<template>
  <view class="page">
    <AppNavBar title="月结订单" :show-back="true" />
    <view class="summary"
      ><text>本月预计收入</text><text class="amount">¥{{ total }}</text
      ><text>共 {{ orders.length }} 个订单</text></view
    >
    <scroll-view scroll-y class="content">
      <view
        v-for="item in orders"
        :key="item.id"
        class="card"
        @click="open(item)"
      >
        <view class="head"
          ><text class="badge">月结</text
          ><text class="status">{{ item.statusText }}</text></view
        >
        <text class="title">{{ item.title }}</text
        ><text class="line">{{ item.time }}</text
        ><text class="line">{{ item.address }}</text>
        <view class="footer"
          ><text>{{ item.employer }}</text
          ><text class="money">¥{{ item.amount }}/月</text></view
        >
      </view>
    </scroll-view>
  </view>
</template>
<script setup>
import { computed, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
const orders = ref([
  {
    id: "m1",
    title: "仓储管理员",
    employer: "物流仓储中心",
    time: "08:30-17:30 每周5天",
    address: "松江区车墩镇",
    amount: 5500,
    statusText: "已录用",
  },
  {
    id: "m2",
    title: "餐饮长期服务员",
    employer: "连锁餐饮门店",
    time: "09:00-18:00",
    address: "松江区方松街道",
    amount: 4800,
    statusText: "已报名",
  },
]);
const total = computed(() =>
  orders.value.reduce((sum, item) => sum + Number(item.amount || 0), 0),
);
function open(item) {
  uni.navigateTo({ url: `/pages/worker/order-detail?id=${item.id}` });
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}
.summary {
  margin: 22rpx;
  padding: 30rpx;
  border-radius: 22rpx;
  background: linear-gradient(135deg, #597ef7, #85a5ff);
  color: #fff;
}
.summary text {
  display: block;
  font-size: 23rpx;
}
.summary .amount {
  font-size: 52rpx;
  font-weight: 800;
  margin: 12rpx 0;
}
.content {
  height: calc(100vh - 390rpx);
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
  background: #e6f4ff;
  color: #1890ff;
  border-radius: 12rpx;
  font-size: 21rpx;
}
.status {
  color: #ff8a00;
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
