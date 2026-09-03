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
          :key="item"
          class="question"
          @click="openFaq(item)"
        >
          <text>{{ item }}</text
          ><text class="arrow">›</text>
        </view>
      </view>
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
import AppNavBar from "@/components/AppNavBar.vue";
const questions = [
  "老板还没有结算报酬",
  "这个订单有事去不了/不小心取消",
  "到达地方联系不上老板",
  "我申诉了，多久会处理？",
  "订单有没有固定的刷新时间",
];
function openFaq(title) {
  uni.navigateTo({
    url: `/pages/worker/faq-detail?title=${encodeURIComponent(title)}`,
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
</style>
