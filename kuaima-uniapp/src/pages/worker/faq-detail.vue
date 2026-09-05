<template>
  <view class="page"
    ><AppNavBar title="问题详情" :show-back="true" /><view class="card"
      ><text class="question">{{ question }}</text
      ><text class="answer">{{ answer }}</text></view
    ><view class="help"
      ><text>问题是否解决？</text
      ><view
        ><button @click="feedback(true)">已解决</button
        ><button @click="feedback(false)">未解决</button></view
      ></view
    ></view
  >
</template>
<script setup>
import AppNavBar from "@/components/AppNavBar.vue";
const pages = getCurrentPages();
const question = decodeURIComponent(
  pages[pages.length - 1]?.options?.question || "问题详情",
);
const answer = decodeURIComponent(
  pages[pages.length - 1]?.options?.answer || "暂无问题解答",
);
function feedback(ok) {
  uni.showToast({
    title: ok ? "感谢反馈" : "已记录，可联系客服",
    icon: "none",
  });
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}
.card,
.help {
  margin: 24rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 20rpx;
}
.question {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  margin-bottom: 28rpx;
}
.answer {
  display: block;
  color: #666;
  font-size: 26rpx;
  line-height: 1.9;
}
.help {
  text-align: center;
  color: #777;
  font-size: 24rpx;
}
.help view {
  display: flex;
  gap: 20rpx;
  margin-top: 22rpx;
}
.help button {
  flex: 1;
  border: 1rpx solid #ff6b35;
  background: #fff;
  color: #ff6b35;
  border-radius: 36rpx;
  font-size: 24rpx;
}
</style>
