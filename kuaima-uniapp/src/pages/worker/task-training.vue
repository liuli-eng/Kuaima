<template>
  <view class="page"
    ><AppNavBar title="培训任务" :show-back="true" /><scroll-view
      scroll-y
      class="content"
      ><view class="hero"
        ><text class="title">{{
          task.title || task.courseTitle || "培训任务"
        }}</text
        ><text>{{
          task.dueDate
            ? `请于 ${task.dueDate} 前完成`
            : "完成培训任务可提升接单资格"
        }}</text></view
      ><view class="card"
        ><text class="section">任务信息</text>
        <view class="step"
          ><text>1</text><text>打开对应课程并完成学习</text></view
        >
        <view class="step"
          ><text>2</text><text>学习结束后返回本页提交完成</text></view
        >
      </view>
      <view class="card video" @click="play"
        ><text class="play">▶</text><text>进入课程学习</text></view
      ></scroll-view
    ><SafeBottomAction
      ><button
        class="done"
        :disabled="submitting || completed"
        @click="complete"
      >
        {{ completed ? "已完成" : submitting ? "提交中…" : "完成培训任务" }}
      </button></SafeBottomAction
    ></view
  >
</template>
<script setup>
import { onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import SafeBottomAction from "@/components/SafeBottomAction.vue";
import { completeTrainingTask, listTrainingTasks } from "@/api/backend";
const pages = getCurrentPages();
const options = pages[pages.length - 1]?.options || {};
const task = ref({ id: options.id, courseId: options.courseId });
const submitting = ref(false);
const completed = ref(false);
onMounted(async () => {
  try {
    const result = await listTrainingTasks(
      uni.getStorageSync("userId") || "2001",
    );
    const rows = Array.isArray(result)
      ? result
      : result?.records || result?.content || [];
    task.value =
      rows.find((item) => String(item.id) === String(options.id)) || task.value;
    completed.value = ["COMPLETED", "DONE", "已完成"].includes(
      task.value.status,
    );
  } catch (_) {}
});
function play() {
  if (!task.value.courseId)
    return uni.showToast({ title: "任务未关联课程", icon: "none" });
  uni.navigateTo({
    url: `/pages/worker/course-detail?id=${task.value.courseId}`,
  });
}
async function complete() {
  if (!task.value.id || submitting.value || completed.value) return;
  submitting.value = true;
  try {
    await completeTrainingTask(task.value.id);
    completed.value = true;
    uni.showToast({ title: "培训已完成", icon: "success" });
    setTimeout(() => uni.navigateBack(), 500);
  } catch (error) {
    uni.showToast({ title: error.message || "提交失败", icon: "none" });
  } finally {
    submitting.value = false;
  }
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
.hero {
  padding: 38rpx 30rpx;
  background: linear-gradient(135deg, #f3e8ff, #e9d5ff);
  color: #5b21b6;
}
.hero .title {
  display: block;
  font-size: 36rpx;
  font-weight: 800;
  margin-bottom: 12rpx;
}
.hero text:last-child {
  font-size: 23rpx;
}
.card {
  margin: 22rpx;
  padding: 28rpx;
  background: #fff;
  border-radius: 20rpx;
}
.section {
  display: block;
  font-size: 29rpx;
  font-weight: 700;
  margin-bottom: 18rpx;
}
.step {
  display: flex;
  gap: 18rpx;
  padding: 18rpx 0;
  color: #555;
  font-size: 25rpx;
}
.step text:first-child {
  width: 38rpx;
  height: 38rpx;
  line-height: 38rpx;
  text-align: center;
  border-radius: 50%;
  background: #722ed1;
  color: #fff;
}
.video {
  text-align: center;
  color: #722ed1;
}
.play {
  display: block;
  font-size: 60rpx;
  margin-bottom: 12rpx;
}
.done {
  width: 100%;
  height: 84rpx;
  border: 0;
  border-radius: 44rpx;
  background: #722ed1;
  color: #fff;
  font-size: 29rpx;
}
.done[disabled] {
  background: #bbb;
}
</style>
