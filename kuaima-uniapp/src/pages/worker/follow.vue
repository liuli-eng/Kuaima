<template>
  <view class="page"
    ><AppNavBar title="关注老板" :show-back="true" /><scroll-view
      scroll-y
      class="content"
      ><view v-for="item in list" :key="item.id" class="card"
        ><view class="avatar">🏪</view
        ><view class="main"
          ><text class="name">{{ item.name }}</text
          ><text class="desc">{{ item.desc }}</text></view
        ><button @click="unfollow(item)">
          {{ item.followed ? "已关注" : "关注" }}
        </button></view
      ><view v-if="!list.length" class="empty"
        >暂无关注的老板</view
      ></scroll-view
    ></view
  >
</template>
<script setup>
import { ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
const list = ref([
  { id: 1, name: "松江餐饮店", desc: "餐饮服务 · 已合作3次", followed: true },
  { id: 2, name: "绿源农业科技", desc: "仓储分拣 · 已合作1次", followed: true },
]);
function unfollow(i) {
  i.followed = !i.followed;
  uni.showToast({
    title: i.followed ? "关注成功" : "已取消关注",
    icon: "none",
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
  background: #fff;
  border-radius: 20rpx;
  padding: 26rpx 22rpx;
  margin-bottom: 18rpx;
}
.avatar {
  width: 76rpx;
  height: 76rpx;
  line-height: 76rpx;
  text-align: center;
  border-radius: 50%;
  background: #fff4e6;
  font-size: 36rpx;
}
.main {
  flex: 1;
  margin-left: 20rpx;
}
.name {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
}
.desc {
  display: block;
  color: #999;
  font-size: 22rpx;
  margin-top: 8rpx;
}
.card button {
  margin: 0;
  border: 1rpx solid #ff6b35;
  background: #fff;
  color: #ff6b35;
  border-radius: 28rpx;
  font-size: 22rpx;
}
.empty {
  text-align: center;
  padding: 220rpx;
  color: #aaa;
  font-size: 25rpx;
}
</style>
