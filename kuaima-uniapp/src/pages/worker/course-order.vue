<template>
  <view class="page"
    ><AppNavBar title="新手接单课程" :show-back="true" /><scroll-view
      scroll-y
      class="content"
      ><view
        v-for="(item, index) in courses"
        :key="item.id"
        class="card"
        @click="open(item)"
        ><view class="cover">{{ index + 1 }}</view
        ><view class="main"
          ><text class="title">{{ item.title }}</text
          ><text class="desc">{{ item.desc }}</text
          ><text class="state"
            >{{ item.done ? "已完成" : "开始学习" }} ›</text
          ></view
        ></view
      ></scroll-view
    ></view
  >
</template>
<script setup>
import { ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
const courses = ref([
  {
    id: 1,
    title: "如何挑选合适岗位",
    desc: "了解岗位类型与结算方式",
    done: true,
  },
  {
    id: 2,
    title: "报名与到岗流程",
    desc: "掌握报名、录用和到岗步骤",
    done: false,
  },
  {
    id: 3,
    title: "完工与结算流程",
    desc: "了解完工确认和收入到账",
    done: false,
  },
]);
function open(item) {
  uni.navigateTo({ url: `/pages/worker/course-video?id=${item.id}` });
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
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 18rpx;
}
.cover {
  width: 120rpx;
  height: 120rpx;
  line-height: 120rpx;
  text-align: center;
  border-radius: 18rpx;
  background: linear-gradient(135deg, #ffd700, #ffa500);
  color: #fff;
  font-size: 48rpx;
  font-weight: 800;
}
.main {
  flex: 1;
  margin-left: 20rpx;
}
.title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
}
.desc {
  display: block;
  color: #999;
  font-size: 22rpx;
  margin-top: 10rpx;
}
.state {
  display: block;
  text-align: right;
  color: #ff6b35;
  font-size: 22rpx;
  margin-top: 18rpx;
}
</style>
