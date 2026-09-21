<template>
  <view class="page">
    <view class="nav" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="nav-inner">
        <view class="back" @click="goBack">‹</view>
        <text class="title">岗位草稿</text>
        <view class="nav-space" />
      </view>
    </view>
    <scroll-view scroll-y class="content">
      <view v-if="loading" class="state">草稿加载中...</view>
      <view v-else-if="!drafts.length" class="state">暂无岗位草稿</view>
      <view v-for="item in drafts" :key="item.id" class="draft-card" @click="editDraft(item)">
        <text class="draft-title">{{ item.orderTitle || item.postion || "未命名草稿" }}</text>
        <text class="draft-address">{{ item.address || "工作地址未填写" }}</text>
        <text class="draft-time">{{ formatTime(item.updatedAt || item.updateTime || item.createdAt) }}</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { listOrderDrafts } from "@/api/backend";

const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0;
const loading = ref(false);
const drafts = ref([]);

onShow(loadDrafts);

async function loadDrafts() {
  loading.value = true;
  try {
    const result = await listOrderDrafts();
    drafts.value = Array.isArray(result)
      ? result
      : result?.records || result?.content || [];
  } catch (error) {
    drafts.value = [];
    uni.showToast({ title: error?.message || "草稿加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

function goBack() {
  uni.navigateBack();
}

function editDraft(item) {
  if (!item?.id) return;
  uni.setStorageSync("editingOrderDraft", item);
  uni.navigateTo({ url: `/pages/boss/publish?draftId=${encodeURIComponent(item.id)}` });
}

function formatTime(value) {
  if (!value) return "";
  return String(value).replace("T", " ").slice(0, 16);
}
</script>

<style scoped>
.page {
  height: 100vh;
  background: #f5f5f5;
}
.nav {
  background: #fff;
}
.nav-inner {
  position: relative;
  display: flex;
  align-items: center;
  height: 100rpx;
  padding: 0 32rpx;
}
.back,
.nav-space {
  width: 64rpx;
}
.back {
  color: #333;
  font-size: 52rpx;
}
.title {
  position: absolute;
  left: 50%;
  color: #333;
  font-size: 34rpx;
  font-weight: 600;
  transform: translateX(-50%);
}
.nav-space {
  margin-left: auto;
}
.content {
  height: calc(100vh - 160rpx);
  padding: 24rpx;
  box-sizing: border-box;
}
.state {
  padding: 160rpx 0;
  color: #999;
  text-align: center;
}
.draft-card {
  margin-bottom: 20rpx;
  padding: 30rpx;
  border-radius: 20rpx;
  background: #fff;
}
.draft-title,
.draft-address,
.draft-time {
  display: block;
}
.draft-title {
  color: #333;
  font-size: 30rpx;
  font-weight: 600;
}
.draft-address {
  margin-top: 12rpx;
  color: #666;
  font-size: 25rpx;
}
.draft-time {
  margin-top: 12rpx;
  color: #aaa;
  font-size: 22rpx;
}
</style>
