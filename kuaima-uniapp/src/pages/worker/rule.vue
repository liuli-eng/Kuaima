<template>
  <view class="page">
    <AppNavBar title="平台规则" :show-back="true" />
    <view class="list">
      <view
        v-for="item in rules"
        :key="item.id"
        class="row"
        @click="open(item)"
      >
        <text :class="['icon', item.color || 'orange']">{{
          item.icon || "规"
        }}</text
        ><text class="title">{{ item.title }}</text
        ><text class="arrow">›</text>
      </view>
    </view>
    <view v-if="!loading && !rules.length" class="empty">暂无平台规则</view>
  </view>
</template>
<script setup>
import { onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import { listRules } from "@/api/backend";
const rules = ref([]);
const loading = ref(false);
onMounted(async () => {
  loading.value = true;
  try {
    const result = await listRules();
    rules.value = Array.isArray(result)
      ? result
      : result?.records || result?.content || [];
  } catch (error) {
    uni.showToast({ title: error.message || "规则加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
});
function open(item) {
  uni.navigateTo({
    url: `/pages/worker/rule-detail?id=${item.id}&title=${encodeURIComponent(item.title || "规则详情")}`,
  });
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f8f4ed;
}
.list {
  margin: 24rpx;
  overflow: hidden;
  border-radius: 20rpx;
  background: #fff;
  box-shadow: 0 5rpx 16rpx rgba(88, 64, 32, 0.05);
}
.row {
  display: flex;
  align-items: center;
  padding: 26rpx 24rpx;
  border-bottom: 1rpx solid #f2eee8;
}
.row:last-child {
  border-bottom: 0;
}
.icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 62rpx;
  height: 62rpx;
  margin-right: 18rpx;
  border-radius: 18rpx;
  font-size: 23rpx;
  font-weight: 700;
}
.orange {
  background: #fff1e6;
  color: #ff6b35;
}
.yellow {
  background: #fffbe6;
  color: #d4a106;
}
.green {
  background: #e8f8e8;
  color: #52c41a;
}
.blue {
  background: #e6f7ff;
  color: #1890ff;
}
.red {
  background: #fff1f0;
  color: #ff4d4f;
}
.title {
  flex: 1;
  color: #333;
  font-size: 27rpx;
  font-weight: 600;
}
.arrow {
  color: #aaa;
  font-size: 34rpx;
}
.empty {
  padding: 220rpx 0;
  text-align: center;
  color: #aaa;
  font-size: 25rpx;
}
</style>
