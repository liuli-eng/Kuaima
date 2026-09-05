<template>
  <view class="page"
    ><AppNavBar title="学习规则" :show-back="true" /><scroll-view
      scroll-y
      class="content"
      ><view v-if="course" class="card"
        ><image
          v-if="course.coverUrl"
          class="cover"
          :src="course.coverUrl"
          mode="aspectFill"
        />
        <text class="title">{{ course.title }}</text
        ><text class="para">{{ course.intro || "暂无课程介绍" }}</text
        ><view
          v-for="video in videos"
          :key="video.id"
          class="video-row"
          @click="openVideo(video)"
        >
          <text>{{ video.title }}</text
          ><text>去学习 ›</text>
        </view></view
      ><view v-else-if="!loading" class="empty"
        >课程不存在或已下架</view
      ></scroll-view
    ><SafeBottomAction
      ><button class="done" @click="complete">
        我已学习完成
      </button></SafeBottomAction
    ></view
  >
</template>
<script setup>
import { onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import SafeBottomAction from "@/components/SafeBottomAction.vue";
import { getCourse } from "@/api/backend";
const pages = getCurrentPages();
const id = pages[pages.length - 1]?.options?.id;
const loading = ref(false);
const course = ref(null);
const videos = ref([]);
onMounted(async () => {
  if (!id) return;
  loading.value = true;
  try {
    const result = await getCourse(id);
    course.value = result?.course || result;
    videos.value = result?.videos || course.value?.videos || [];
  } catch (error) {
    uni.showToast({ title: error.message || "课程加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
});
function openVideo(video) {
  uni.navigateTo({
    url: `/pages/worker/course-video?courseId=${id}&videoId=${video.id}`,
  });
}
function complete() {
  uni.showToast({ title: "学习完成", icon: "success" });
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
.card {
  margin: 24rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
}
.title {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  margin-bottom: 24rpx;
}
.para {
  display: block;
  color: #666;
  font-size: 26rpx;
  line-height: 2;
  margin-bottom: 12rpx;
}
.cover {
  width: 100%;
  height: 280rpx;
  margin-bottom: 24rpx;
  border-radius: 16rpx;
}
.video-row {
  display: flex;
  justify-content: space-between;
  padding: 22rpx 0;
  border-top: 1rpx solid #eee;
  color: #555;
  font-size: 25rpx;
}
.video-row text:last-child {
  color: #ff6b35;
}
.empty {
  padding: 220rpx 0;
  text-align: center;
  color: #aaa;
}
.done {
  width: 100%;
  height: 84rpx;
  line-height: 84rpx;
  border: 0;
  border-radius: 44rpx;
  background: #ff6b35;
  color: #fff;
  font-size: 29rpx;
}
</style>
