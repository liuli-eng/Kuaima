<template>
  <view class="page">
    <AppNavBar title="请选择咨询内容" :show-back="true" />
    <scroll-view scroll-y class="content">
      <view class="top-tip"
        >请文明沟通，禁止辱骂客服或人身攻击，否则平台将按规则处理。</view
      >
      <view class="questions">
        <view
          v-for="item in questions"
          :key="item.id"
          class="question"
          @click="openFaq(item)"
        >
          <text>{{ item.question }}</text
          ><text class="arrow">›</text>
        </view>
      </view>
      <view v-if="!loading && !questions.length" class="empty"
        >暂无常见问题</view
      >
      <view class="contact">
        <text>以上内容未能解决您的问题？</text>
        <button @click="uni.navigateTo({ url: '/pages/worker/service-chat' })">
          ☎ 联系客服
        </button>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import { listFaq } from "@/api/backend";
const loading = ref(false);
const questions = ref([]);
onMounted(async () => {
  loading.value = true;
  try {
    const result = await listFaq();
    questions.value = Array.isArray(result)
      ? result
      : result?.records || result?.content || [];
  } catch (error) {
    uni.showToast({ title: error.message || "常见问题加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
});
function openFaq(item) {
  uni.navigateTo({
    url: `/pages/worker/faq-detail?question=${encodeURIComponent(item.question || "")}&answer=${encodeURIComponent(item.answer || "")}`,
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
}
.top-tip {
  padding: 18rpx 24rpx;
  background: #fff8e6;
  color: #d48806;
  font-size: 22rpx;
  line-height: 1.6;
}
.questions {
  margin: 24rpx;
  overflow: hidden;
  border-radius: 20rpx;
  background: #fff;
  box-shadow: 0 5rpx 16rpx rgba(88, 64, 32, 0.05);
}
.question {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx 26rpx;
  border-bottom: 1rpx solid #f2eee8;
  color: #333;
  font-size: 26rpx;
}
.question:last-child {
  border-bottom: 0;
}
.arrow {
  color: #aaa;
  font-size: 34rpx;
}
.contact {
  margin: 40rpx 24rpx;
  text-align: center;
  color: #999;
  font-size: 23rpx;
}
.contact button {
  margin-top: 20rpx;
  border: 0;
  border-radius: 44rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: #fff;
  font-size: 28rpx;
}
.empty {
  padding: 100rpx 0;
  text-align: center;
  color: #aaa;
  font-size: 24rpx;
}
</style>
