<template>
  <view class="page"><AppNavBar title="新手如何接单" :show-back="true" /><scroll-view scroll-y class="content"><text class="subtitle">熟悉接单流程，找活不迷茫</text><view v-if="loading" class="state">课程加载中…</view><view v-else-if="!lessons.length" class="state">暂无已发布接单课程</view><view v-else v-for="(item,index) in lessons" :key="item.id || index" class="lesson" @click="open(item,index)"><text class="title">{{ item.title }}</text><text class="action">去学习 ›</text></view></scroll-view></view>
</template>
<script setup>
import { onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import { getWorkerClassroomOverview, listCourses } from "@/api/backend";
const lessons = ref([]); const loading = ref(true);
onMounted(async () => { try { const result = await getWorkerClassroomOverview(); const data = result?.data || result || {}; lessons.value = normalize(data.howToOrder); if (!lessons.value.length) lessons.value = normalize(await listCourses({ category: "HOW_TO_ORDER" })); } catch (error) { uni.showToast({ title: error?.message || "课程加载失败", icon: "none" }); } finally { loading.value = false; } });
function normalize(value) { const rows = Array.isArray(value) ? value : value?.records || value?.content || value?.list || []; return rows.map((item,index) => ({ ...item, id: item.id || `order-${index}`, title: item.title || item.name || ["新手如何报名接单？","如何结算、如何提现？","信用分有什么用？"][index] })); }
function open(item,index) { const params = item.courseId ? `courseId=${item.courseId}` : item.id && !String(item.id).startsWith("order-") ? `courseId=${item.id}` : `type=order${index + 1}`; uni.navigateTo({ url: `/pages/worker/course-video?${params}` }); }
</script>
<style scoped>
.page{min-height:100vh;background:#f5f5f5}.content{height:calc(100vh - 176rpx);padding:30rpx 24rpx;box-sizing:border-box}.subtitle{display:block;margin:0 10rpx 28rpx;color:#777;font-size:27rpx}.lesson{display:flex;align-items:center;justify-content:space-between;margin-bottom:20rpx;padding:36rpx 28rpx;border:1rpx solid #eee;border-radius:22rpx;background:#fff}.title{color:#333;font-size:30rpx;font-weight:600}.action{color:#ff6b35;font-size:26rpx}.state{padding:180rpx 32rpx;color:#999;text-align:center}
</style>
