<template>
  <view class="page"
    ><AppNavBar title="岗位收藏" :show-back="true" /><scroll-view
      scroll-y
      class="content"
      ><JobCard
        v-for="job in jobs"
        :key="job.id"
        :job="job"
        @select="open"
        @apply="apply"
      /><view v-if="!jobs.length" class="empty">暂无收藏岗位</view></scroll-view
    ></view
  >
</template>
<script setup>
import { ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import JobCard from "@/components/JobCard.vue";
const jobs = ref(uni.getStorageSync("workerFavorites") || []);
function open(j) {
  uni.navigateTo({ url: `/pages/worker/job-detail?id=${j.id}` });
}
function apply() {
  uni.showToast({ title: "请在详情页报名", icon: "none" });
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
.empty {
  text-align: center;
  padding: 220rpx;
  color: #aaa;
  font-size: 25rpx;
}
</style>
