<template>
  <view class="page">
    <AppNavBar title="收入明细" :show-back="true" />
    <scroll-view scroll-y class="content">
      <view v-for="item in records" :key="item.id" class="row">
        <view>
          <text class="title">{{ item.title }}</text>
          <text class="time">{{ item.time }}</text>
        </view>
        <text :class="item.type">
          {{ item.type === "income" ? "+" : "-" }}¥{{ item.amount }}
        </text>
      </view>
      <view v-if="!records.length" class="empty">暂无收入明细</view>
    </scroll-view>
  </view>
</template>

<script setup>
import { onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import { request } from "@/api/http";

const records = ref([
  {
    id: 1,
    title: "餐饮服务员订单结算",
    time: "08月30日 18:20",
    amount: "180.00",
    type: "income",
  },
  {
    id: 2,
    title: "提现到微信零钱",
    time: "08月29日 11:06",
    amount: "100.00",
    type: "withdraw",
  },
]);

onMounted(async () => {
  try {
    const result = await request({ url: "/worker/wallet/records" });
    if (Array.isArray(result)) records.value = result.map((item) => ({
      ...item,
      title: item.remark || (item.bizType === "WAGE" ? "订单收入" : "钱包变动"),
      time: item.createTime || item.updateTime || "",
      amount: (Number(item.amount || 0) / 100).toFixed(2),
      type: item.direction === "income" ? "income" : "withdraw",
    }));
  } catch (_) {}
});
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f8f4ed;
}

.content {
  height: calc(100vh - 176rpx);
  padding: 22rpx 24rpx 0;
  box-sizing: border-box;
}

.row {
  display: flex;
  justify-content: space-between;
  padding: 24rpx;
  margin-bottom: 12rpx;
  border-radius: 16rpx;
  background: #fff;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.04);
}

.title {
  display: block;
  color: #333;
  font-size: 27rpx;
}

.time {
  display: block;
  margin-top: 8rpx;
  color: #aaa;
  font-size: 21rpx;
}

.income {
  color: #ff6b35;
  font-size: 30rpx;
}

.withdraw {
  color: #666;
  font-size: 30rpx;
}

.empty {
  padding: 220rpx 0;
  color: #aaa;
  font-size: 25rpx;
  text-align: center;
}
</style>
