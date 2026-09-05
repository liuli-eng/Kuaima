<template>
  <view class="page"
    ><AppNavBar title="课程视频" :show-back="true" /><scroll-view
      scroll-y
      class="content"
      ><video
        v-if="video.videoUrl"
        class="video"
        :src="video.videoUrl"
        controls
      />
      <view v-else class="video"><text>暂无可播放视频</text></view
      ><view class="card"
        ><text class="title">{{ video.title || "课程视频" }}</text
        ><text class="desc">{{ durationText }}</text></view
      ><view class="card"
        ><text class="section">课程要点</text
        ><text v-for="item in points" :key="item" class="point"
          >• {{ item }}</text
        ></view
      ></scroll-view
    ><SafeBottomAction
      ><button class="complete" @click="done">
        完成学习
      </button></SafeBottomAction
    ></view
  >
</template>
<script setup>
import { computed, onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import SafeBottomAction from "@/components/SafeBottomAction.vue";
import { listCourseVideos } from "@/api/backend";
const pages = getCurrentPages();
const options = pages[pages.length - 1]?.options || {};
const video = ref({});
const durationText = computed(() => {
  const seconds = Number(video.value.duration || 0);
  return seconds
    ? `视频时长 ${Math.ceil(seconds / 60)} 分钟`
    : "请完成视频学习";
});
const points = [
  "报名前确认工作信息",
  "按约定时间到岗",
  "完工后关注结算进度",
  "异常情况及时保留证据",
];
onMounted(async () => {
  if (!options.courseId) return;
  try {
    const result = await listCourseVideos(options.courseId);
    const rows = Array.isArray(result)
      ? result
      : result?.records || result?.content || [];
    video.value =
      rows.find((item) => String(item.id) === String(options.videoId)) ||
      rows[0] ||
      {};
  } catch (error) {
    uni.showToast({ title: error.message || "视频加载失败", icon: "none" });
  }
});
function done() {
  uni.showToast({ title: "课程学习完成", icon: "success" });
  setTimeout(() => uni.navigateBack(), 500);
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
.video {
  height: 380rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #222;
  color: #fff;
}
.video text {
  color: #fff;
  font-size: 25rpx;
}
.card {
  margin: 22rpx;
  padding: 28rpx;
  background: #fff;
  border-radius: 20rpx;
}
.title,
.section {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  margin-bottom: 16rpx;
}
.desc,
.point {
  display: block;
  color: #666;
  font-size: 24rpx;
  line-height: 1.8;
}
.complete {
  width: 100%;
  height: 84rpx;
  border: 0;
  border-radius: 44rpx;
  background: #ff6b35;
  color: #fff;
  font-size: 29rpx;
}
</style>
