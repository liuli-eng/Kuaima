<template>
  <view class="page"
    ><AppNavBar title="黑名单" :show-back="true" /><scroll-view
      scroll-y
      class="content"
      ><view v-for="item in list" :key="item.id" class="card"
        ><view
          ><text class="title">{{ item.name }}</text
          ><text class="reason">{{ item.reason }}</text></view
        ><button @click="remove(item)">移出</button></view
      ><view v-if="!list.length" class="empty">黑名单为空</view></scroll-view
    ></view
  >
</template>
<script setup>
import { ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
const list = ref([
  { id: 1, name: "某招聘账号", reason: "不再接收该雇主岗位推荐" },
]);
function remove(item) {
  uni.showModal({
    title: "移出黑名单",
    content: `确定将“${item.name}”移出黑名单吗？`,
    success: ({ confirm }) => {
      if (confirm) list.value = list.value.filter((i) => i.id !== item.id);
    },
  });
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
.card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 26rpx;
  border-radius: 20rpx;
  background: #fff;
}
.title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
}
.reason {
  display: block;
  margin-top: 8rpx;
  color: #999;
  font-size: 22rpx;
}
.card button {
  margin: 0;
  border: 1rpx solid #ddd;
  background: #fff;
  color: #666;
  border-radius: 28rpx;
  font-size: 22rpx;
}
.empty {
  padding: 220rpx 0;
  text-align: center;
  color: #aaa;
}
</style>
