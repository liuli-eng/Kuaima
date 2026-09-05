<template>
  <view class="page">
    <AppNavBar title="选择工种" :show-back="true" />
    <scroll-view scroll-y class="content">
      <text class="page-title">要招的工种</text>
      <text class="page-desc">选对工种，平台推荐熟手</text>
      <view class="search-box">
        <text class="search-icon">⌕</text>
        <input
          v-model="searchText"
          type="text"
          confirm-type="search"
          placeholder="填写工种名称，如‘普工’"
        />
      </view>
      <view class="section-header"
        ><text class="section-icon">●</text
        ><text class="section-title">{{
          searchText ? "搜索结果" : "热门工种"
        }}</text></view
      >
      <view v-if="loading" class="state">工种加载中...</view>
      <view v-else-if="loadError" class="state error" @click="loadJobs"
        >工种加载失败，点击重试</view
      >
      <view v-else-if="!visibleJobs.length" class="state">未找到相关工种</view>
      <view v-else class="hot-grid">
        <view
          v-for="job in visibleJobs"
          :key="job.id || job.name"
          class="hot-item"
          @click="selectJob(job)"
          >{{ job.name }}</view
        >
      </view>
      <view class="view-all" @click="viewAllJobs"
        >查看全部工种 <text class="view-all-arrow">›</text></view
      >
      <view class="bottom-space" />
    </scroll-view>
    <view class="float-service" @click="openService"
      ><text class="service-icon">◉</text
      ><text class="service-label">客服</text></view
    >
  </view>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import { listJobCategories } from "@/api/backend";

const searchText = ref("");
const jobs = ref([]);
const loading = ref(false);
const loadError = ref(false);
const visibleJobs = computed(() => {
  const keyword = searchText.value.trim();
  return keyword
    ? jobs.value.filter((item) => item.name.includes(keyword))
    : jobs.value.filter((item) => item.hot !== false).slice(0, 8);
});
onMounted(() => {
  // 从“选择工种”进入代表开始新建岗位，不能沿用上次编辑的草稿缓存。
  [
    "taskContent",
    "genderAgeSelection",
    "workLocationSelection",
    "workTimeSelection",
    "recruitSettings",
  ].forEach((key) => uni.removeStorageSync(key));
  loadJobs();
});
async function loadJobs() {
  loading.value = true;
  loadError.value = false;
  try {
    const result = await listJobCategories();
    const rows = Array.isArray(result)
      ? result
      : result?.records || result?.content || result?.list || [];
    jobs.value = flattenJobs(rows);
    if (!jobs.value.length) jobs.value = defaultJobs();
  } catch (error) {
    jobs.value = defaultJobs();
    uni.showToast({ title: error.message || "工种加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}
function flattenJobs(rows) {
  const result = [];
  rows.forEach((item, index) => {
    const children = item.children || item.jobs || item.items;
    if (Array.isArray(children) && children.length) {
      children.forEach((child, childIndex) => {
        const name =
          child.name || child.title || child.jobName || child.categoryName;
        if (name)
          result.push({
            ...child,
            id: child.id || `${index}-${childIndex}`,
            name,
          });
      });
      return;
    }
    const name = item.name || item.title || item.jobName || item.categoryName;
    if (name) result.push({ ...item, id: item.id || index, name });
  });
  return result;
}
function defaultJobs() {
  return [
    "电子厂普工",
    "五金厂CNC操作工",
    "注塑厂注塑工",
    "快递分拣打包工",
    "快递搬运装卸工",
    "电商分拣打包工",
    "电商手工活",
    "餐饮服务员",
  ].map((name, index) => ({ id: `default-${index}`, name, hot: true }));
}
function selectJob(job) {
  uni.navigateTo({
    url: `/pages/boss/publish-info?job=${encodeURIComponent(job.name)}${job.id ? `&jobId=${encodeURIComponent(job.id)}` : ""}`,
  });
}
function viewAllJobs() {
  uni.navigateTo({ url: "/pages/boss/all-jobs" });
}
function openService() {
  uni.navigateTo({ url: "/pages/boss/service-chat" });
}
</script>

<style scoped>
.page {
  position: relative;
  min-height: 100vh;
  background: #fff;
}
.content {
  height: calc(100vh - 176rpx);
  padding: 32rpx;
  box-sizing: border-box;
}
.page-title {
  display: block;
  color: #333;
  font-size: 36rpx;
  font-weight: 700;
}
.page-desc {
  display: block;
  margin-top: 8rpx;
  color: #999;
  font-size: 26rpx;
}
.search-box {
  display: flex;
  align-items: center;
  margin: 32rpx 0 48rpx;
  padding: 0 36rpx;
  height: 88rpx;
  border-radius: 44rpx;
  background: #f5f5f5;
}
.search-icon {
  margin-right: 20rpx;
  color: #999;
  font-size: 32rpx;
}
.search-box input {
  flex: 1;
  height: 88rpx;
  color: #333;
  font-size: 28rpx;
}
.section-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 28rpx;
}
.section-icon {
  color: #ff6b35;
  font-size: 24rpx;
}
.section-title {
  color: #333;
  font-size: 32rpx;
  font-weight: 600;
}
.hot-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}
.hot-item {
  box-sizing: border-box;
  width: calc(50% - 10rpx);
  padding: 32rpx 12rpx;
  border-radius: 20rpx;
  background: #f8f8f8;
  color: #333;
  text-align: center;
  font-size: 28rpx;
}
.hot-item:active {
  background: #fff3ed;
  color: #ff6b35;
}
.state {
  padding: 100rpx 0;
  color: #999;
  text-align: center;
  font-size: 26rpx;
}
.state.error {
  color: #ff6b35;
}
.view-all {
  margin-top: 48rpx;
  color: #ff6b35;
  text-align: center;
  font-size: 28rpx;
}
.view-all-arrow {
  margin-left: 6rpx;
  font-size: 24rpx;
}
.bottom-space {
  height: 160rpx;
}
.float-service {
  position: fixed;
  right: 32rpx;
  bottom: calc(40rpx + env(safe-area-inset-bottom));
  z-index: 20;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.12);
}
.service-icon {
  color: #ff6b35;
  font-size: 36rpx;
}
.service-label {
  margin-top: 4rpx;
  color: #ff6b35;
  font-size: 20rpx;
}
</style>
