<template>
  <view class="page"
    ><AppNavBar title="浏览记录" :show-back="true" /><scroll-view
      scroll-y
      class="content"
      ><JobCard
        v-for="job in records"
        :key="job.id"
        :job="job"
        @select="open"
        @apply="apply"
      /><view v-if="!records.length" class="empty"
        >暂无浏览记录</view
      ></scroll-view
    ></view
  >
</template>
<script setup>
import { onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import JobCard from "@/components/JobCard.vue";
import { listBrowseHistory } from "@/api/backend";
const records = ref([
  {
    id: "r1",
    title: "餐饮服务员",
    unitPrice: 180,
    salaryType: "DAY",
    startTime: "09:00",
    endTime: "18:00",
    address: "松江区方松街道",
    duration: 8,
  },
]);
onMounted(async () => {
  try {
    const result = await listBrowseHistory(
      uni.getStorageSync("userId") || "2001",
    );
    const rows = Array.isArray(result) ? result : result?.records || [];
    if (rows.length)
      records.value = rows.map((item) => item.order || item.job || item);
  } catch (_) {}
});
function open(j) {
  uni.navigateTo({ url: `/pages/worker/job-detail?id=${j.id}` });
}
function apply() {
  uni.showToast({ title: "请在岗位详情中报名", icon: "none" });
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
