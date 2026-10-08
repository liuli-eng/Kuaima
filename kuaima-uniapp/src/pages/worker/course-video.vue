<template>
  <view class="page">
    <AppNavBar
      :title="isSimulate ? '学习抢日结操作流程' : '课程视频'"
      :show-back="true"
    />

    <scroll-view scroll-y class="content">
      <view v-if="loading" class="video-state">
        <text>视频加载中…</text>
      </view>
      <video
        v-else-if="videoSource"
        :key="video.id || videoSource"
        class="video"
        :src="videoSource"
        controls
      />
      <view v-else class="video-state">
        <text>{{ loadError || emptyText }}</text>
        <text v-if="loadError" class="retry" @click="loadVideo">点击重试</text>
      </view>

      <view class="card">
        <text class="title">{{ videoTitle }}</text>
        <view class="stats">
          <text>{{ durationText }}</text>
          <text v-if="isSimulate && video.learners">{{ video.learners }}人学习</text>
        </view>
      </view>

      <view v-if="isSimulate && videos.length > 1" class="card">
        <text class="section">课程目录</text>
        <view
          v-for="(item, index) in videos"
          :key="item.id || index"
          class="video-item"
          :class="{ active: String(item.id) === String(video.id) }"
          @click="selectVideo(item)"
        >
          <text class="video-index">{{ index + 1 }}</text>
          <text class="video-name">{{ item.title || `模拟接单课程 ${index + 1}` }}</text>
          <text class="video-action">
            {{ String(item.id) === String(video.id) ? "播放中" : "播放" }}
          </text>
        </view>
      </view>

      <view class="card">
        <text class="section">课程要点</text>
        <text v-for="item in points" :key="item" class="point">• {{ item }}</text>
      </view>
    </scroll-view>

    <SafeBottomAction>
      <button class="complete" :disabled="loading || !videoSource" @click="done">
        完成学习
      </button>
    </SafeBottomAction>
  </view>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import SafeBottomAction from "@/components/safe-bottom-action.vue";
import { getCourse, getWorkerClassroomOverview, listCourseVideos } from "@/api/backend";

const pages = getCurrentPages();
const options = pages[pages.length - 1]?.options || {};
const isSimulate = options.type === "simulate";
const orderIndex = String(options.type || "").match(/^order(\d+)$/)?.[1];
const loading = ref(false);
const loadError = ref("");
const videos = ref([]);
const video = ref({});

const videoSource = computed(() => video.value.videoUrl || video.value.url || "");
const videoTitle = computed(
  () => video.value.title || (isSimulate ? "抢日结操作流程" : "课程视频"),
);
const emptyText = computed(() =>
  isSimulate ? "暂无已上架模拟接单视频" : "暂无可播放视频",
);
const durationText = computed(() => {
  const seconds = Number(video.value.duration || 0);
  return seconds ? `视频时长 ${formatDuration(seconds)}` : "请完成视频学习";
});

const points = [
  "报名前确认工作信息",
  "按约定时间到岗",
  "完工后关注结算进度",
  "异常情况及时保留证据",
];

onMounted(loadVideo);

async function loadVideo() {
  if (loading.value) return;
  loading.value = true;
  loadError.value = "";
  try {
    let rows = [];
    if (!options.courseId) {
      const result = await getWorkerClassroomOverview();
      const data = result?.data || result || {};
      rows = normalizeRows(data.simulateVideos).filter(
        (item) => item?.enabled === undefined || item.enabled === true,
      );

      // 兼容历史“如何接单”入口；模拟视频为空时才回退到课程槽位。
      if (!rows.length && orderIndex) {
        const lessons = normalizeRows(data.howToOrder);
        const lesson = lessons[Number(orderIndex) - 1];
        rows = normalizeRows(lesson?.videos || lesson?.videoList);
        if (!rows.length && (lesson?.url || lesson?.video || lesson?.videoUrl)) {
          rows = [lesson];
        }
        if (!rows.length && lesson?.courseId) {
          rows = normalizeRows(await listCourseVideos(lesson.courseId));
        }
        if (!rows.length && lesson?.id && !String(lesson.id).startsWith("order-")) {
          try {
            const courseResult = await getCourse(lesson.id);
            rows = normalizeRows(courseResult?.videos || courseResult?.course?.videos);
          } catch (_) {}
        }
      }
    } else if (options.courseId) {
      rows = normalizeRows(await listCourseVideos(options.courseId));
      // 兼容部分环境中课程详情接口返回视频、但视频列表接口未挂载的情况。
      if (!rows.length) {
        const result = await getCourse(options.courseId);
        rows = normalizeRows(result?.videos || result?.course?.videos);
      }
    }
    videos.value = rows
      .map(normalizeVideo)
      .filter((item) => item.url);
    video.value =
      videos.value.find((item) => String(item.id) === String(options.videoId)) ||
      videos.value[0] ||
      {};
  } catch (error) {
    videos.value = [];
    video.value = {};
    loadError.value = error?.message || "视频加载失败";
  } finally {
    loading.value = false;
  }
}

function normalizeRows(value) {
  if (Array.isArray(value)) return value;
  return value?.records || value?.content || value?.list || [];
}

function normalizeVideo(item = {}) {
  return {
    ...item,
    url: item.url || item.videoUrl || item.video || item.src || item.playUrl || "",
  };
}

function selectVideo(item) {
  if (!item || String(item.id) === String(video.value.id)) return;
  video.value = item;
}

function formatDuration(seconds) {
  const minutes = Math.floor(seconds / 60);
  const remain = seconds % 60;
  return `${minutes}:${String(remain).padStart(2, "0")}`;
}

function done() {
  if (!videoSource.value) return;
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

.video,
.video-state {
  width: 100%;
  height: 380rpx;
  background: #222;
}

.video-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 25rpx;
}

.retry {
  margin-top: 20rpx;
  color: #ff8c42;
}

.card {
  margin: 22rpx;
  padding: 28rpx;
  border-radius: 20rpx;
  background: #fff;
}

.title,
.section {
  display: block;
  margin-bottom: 16rpx;
  font-size: 30rpx;
  font-weight: 700;
}

.stats {
  display: flex;
  gap: 28rpx;
  color: #666;
  font-size: 24rpx;
}

.point {
  display: block;
  color: #666;
  font-size: 24rpx;
  line-height: 1.8;
}

.video-item {
  display: flex;
  align-items: center;
  min-height: 82rpx;
  border-top: 1rpx solid #f0f0f0;
  color: #666;
  font-size: 25rpx;
}

.video-item.active {
  color: #ff6b35;
}

.video-index {
  width: 48rpx;
}

.video-name {
  flex: 1;
}

.video-action {
  margin-left: 20rpx;
  color: #ff6b35;
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

.complete[disabled] {
  background: #ccc;
  color: #fff;
}
</style>
