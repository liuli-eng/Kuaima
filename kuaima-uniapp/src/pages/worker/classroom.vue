<template>
  <view class="page">
    <AppNavBar title="接单课堂" :show-back="true" />

    <scroll-view scroll-y class="content">
      <view class="hero">
        <view class="hero-copy">
          <text class="hero-title">接单课堂</text>
          <text class="hero-subtitle">帮你快速上手找活</text>
        </view>
        <image
          class="hero-image"
          src="/static/icons/worker-home/graduation-cap-brown.svg"
          mode="aspectFit"
        />
      </view>

      <view class="task-card">
        <view class="task-header">
          <text class="task-badge">新人任务</text>
          <text class="task-heading">完成任务，获取接单资格</text>
        </view>

        <view v-for="task in newcomerTasks" :key="task.key" class="task-row">
          <view class="task-info">
            <view class="task-icon-box" :class="task.colorClass">
              <image class="task-icon" :src="task.icon" mode="aspectFit" />
            </view>
            <text class="task-name">{{ task.title }}</text>
          </view>
          <button
            class="task-action"
            :class="{ completed: task.done }"
            :disabled="task.done || loading"
            @click="openTask(task)"
          >
            {{ task.done ? "已完成" : "去完成" }}
          </button>
        </view>
      </view>

      <view class="learn-entry">
        <view class="entry-card" @click="openLearningEntry('order')">
          <view class="entry-icon-box icon-blue">
            <image
              class="entry-icon"
              src="/static/icons/worker-classroom/clipboard-list-blue.svg"
              mode="aspectFit"
            />
          </view>
          <text class="entry-title">如何接单</text>
          <text class="entry-status">{{ entryStatus(orderCourse) }} ›</text>
        </view>

        <view class="entry-card" @click="openLearningEntry('rule')">
          <view class="entry-icon-box icon-green">
            <image
              class="entry-icon"
              src="/static/icons/worker-classroom/book-green.svg"
              mode="aspectFit"
            />
          </view>
          <text class="entry-title">平台规则</text>
          <text class="entry-status">查看规则 ›</text>
        </view>
      </view>

      <view class="section-header">
        <text class="section-title">赚钱接单攻略</text>
      </view>

      <view v-if="loading" class="state">课堂内容加载中…</view>
      <view v-else-if="loadError" class="state error" @click="loadData">
        {{ loadError }}，点击重试
      </view>
      <view v-else-if="!guideCourses.length" class="state">暂无已发布课程</view>
      <view v-else class="course-grid">
        <view
          v-for="(course, index) in guideCourses"
          :key="course.id"
          class="course-card"
          @click="openCourse(course)"
        >
          <view class="course-cover" :class="`cover-${(index % 4) + 1}`">
            <image
              v-if="course.coverUrl"
              class="cover-image"
              :src="course.coverUrl"
              mode="aspectFill"
            />
            <view class="play-button">
              <image
                class="play-icon"
                src="/static/icons/worker-classroom/play-white.svg"
                mode="aspectFit"
              />
            </view>
          </view>
          <view class="course-info">
            <text class="course-title">{{ course.title }}</text>
            <view class="course-meta">
              <view class="duration">
                <image
                  class="clock-icon"
                  src="/static/icons/worker-classroom/clock-gray.svg"
                  mode="aspectFit"
                />
                <text>{{ formatDuration(course.duration) }}</text>
              </view>
              <text>{{ formatLearners(course.learnCount) }}</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import { getWorkerClassroomOverview, listTrainingTasks } from "@/api/backend";

const loading = ref(false);
const loadError = ref("");
const courses = ref([]);
const tasks = ref([]);
const classroom = ref({});

const simulateCourse = computed(() => findCourse(["SIMULATE_ORDER", "SIMULATE", "模拟接单"]));
const learningRuleCourse = computed(() => findCourse(["LEARNING_RULE", "RULE_LEARNING", "学习规则"]));
const examCourse = computed(() => findCourse(["EXAM", "QUIZ", "答题测试"]) || learningRuleCourse.value);
const orderCourse = computed(() => findCourse(["HOW_TO_ORDER", "ORDER_GUIDE", "如何接单"]));
const howToOrder = computed(() => normalizeRows(classroom.value.howToOrder));
const platformRules = computed(() => normalizeRows(classroom.value.platformRules));
const quizQuestions = computed(() => normalizeRows(classroom.value.quiz?.questions));

const reservedCourseIds = computed(() =>
  [simulateCourse.value, learningRuleCourse.value, examCourse.value, orderCourse.value]
    .filter(Boolean)
    .map((item) => String(item.id)),
);

const guideCourses = computed(() =>
  courses.value.filter((item) => !reservedCourseIds.value.includes(String(item.id))),
);

const newcomerTasks = computed(() => [
  {
    key: "simulate",
    title: "模拟接单-体验全流程",
    icon: "/static/icons/worker-classroom/clipboard-list-blue.svg",
    colorClass: "icon-blue",
    course: simulateCourse.value,
    done: isCourseDone(simulateCourse.value),
  },
  {
    key: "rule",
    title: "学习平台规则",
    icon: "/static/icons/worker-classroom/book-green.svg",
    colorClass: "icon-green",
    course: learningRuleCourse.value,
    done: isCourseDone(learningRuleCourse.value),
  },
  {
    key: "exam",
    title: "答题测试",
    icon: "/static/icons/worker-classroom/pen-to-square-orange.svg",
    colorClass: "icon-orange",
    course: examCourse.value,
    done: isCourseDone(examCourse.value, "EXAM"),
  },
]);

onMounted(loadData);

async function loadData() {
  if (loading.value) return;
  loading.value = true;
  loadError.value = "";
  try {
    const userId = uni.getStorageSync("userId");
    const [overviewResult, taskResult] = await Promise.all([
      getWorkerClassroomOverview(),
      userId ? listTrainingTasks(userId).catch(() => []) : Promise.resolve([]),
    ]);
    classroom.value = overviewResult?.data || overviewResult || {};
    courses.value = normalizeRows(classroom.value.courses || classroom.value.simulateOrder)
      .filter((item) => !["下架", "OFFLINE", "DISABLED"].includes(String(item.status || "").toUpperCase()))
      .sort((a, b) => Number(a.sortOrder || 0) - Number(b.sortOrder || 0));
    tasks.value = normalizeRows(taskResult);
  } catch (error) {
    courses.value = [];
    tasks.value = [];
    loadError.value = error?.message || "课堂内容加载失败";
  } finally {
    loading.value = false;
  }
}

function normalizeRows(result) {
  if (Array.isArray(result)) return result;
  return result?.records || result?.content || result?.list || [];
}

function normalizeKeyword(value) {
  return String(value || "").trim().toUpperCase().replace(/[\s-]+/g, "_");
}

function findCourse(keywords) {
  const normalized = keywords.map(normalizeKeyword);
  return courses.value.find((item) => {
    const values = [item.moduleType, item.category, item.title, item.code].map(normalizeKeyword);
    return normalized.some((keyword) => values.some((value) => value === keyword || value.includes(keyword)));
  });
}

function isCourseDone(course, taskType = "COURSE") {
  if (!course) return false;
  return tasks.value.some((task) => {
    const sameCourse = String(task.courseId || "") === String(course.id);
    const sameType = !task.taskType || normalizeKeyword(task.taskType) === taskType;
    const status = String(task.status || "").toUpperCase();
    return sameCourse && sameType && ["COMPLETED", "DONE", "已完成", "PASSED"].includes(status);
  });
}

function openTask(task) {
  if (task.key === "rule") {
    return uni.navigateTo({ url: "/pages/worker/classroom-rule" });
  }
  if (task.key === "exam" && quizQuestions.value.length) {
    return uni.navigateTo({ url: "/pages/worker/quiz" });
  }
  if (!task.course?.id) {
    uni.showToast({ title: "后台暂未发布对应内容", icon: "none" });
    return;
  }
  if (task.key === "simulate") return openCourse(task.course);
  uni.navigateTo({ url: `/pages/worker/quiz?courseId=${task.course.id}` });
}

function openLearningEntry(type) {
  if (type === "rule") {
    if (platformRules.value.length) return openRule(platformRules.value[0]);
    return uni.navigateTo({ url: "/pages/worker/rule" });
  }
  if (howToOrder.value.length) return uni.navigateTo({ url: "/pages/worker/course-order" });
  if (!orderCourse.value?.id) {
    uni.showToast({ title: "后台暂未发布如何接单内容", icon: "none" });
    return;
  }
  uni.navigateTo({ url: "/pages/worker/course-order" });
}

function openCourse(course) {
  uni.navigateTo({ url: `/pages/worker/course-detail?id=${course.id}` });
}

function openRule(rule) {
  if (!rule?.id) {
    uni.showToast({ title: "后台暂未发布对应规则", icon: "none" });
    return;
  }
  uni.navigateTo({ url: `/pages/worker/rule-detail?id=${encodeURIComponent(rule.id)}` });
}

function entryStatus(course) {
  return isCourseDone(course) ? "已学习" : "未学习";
}

function formatDuration(seconds) {
  const value = Number(seconds || 0);
  if (!value) return "待学习";
  const minutes = Math.floor(value / 60);
  const remain = value % 60;
  return `${minutes}:${String(remain).padStart(2, "0")}`;
}

function formatLearners(value) {
  const count = Number(value || 0);
  if (!count) return "开始学习";
  return count >= 10000 ? `${(count / 10000).toFixed(1)}万学习` : `${count}学习`;
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f5f5; }
.content { height: calc(100vh - 176rpx); box-sizing: border-box; }
.hero { position: relative; min-height: 150rpx; padding: 40rpx; overflow: hidden; background: linear-gradient(135deg, #fff4e6, #ffe4b5); }
.hero-copy { position: relative; z-index: 1; }
.hero-title { display: block; color: #8b4513; font-size: 52rpx; font-weight: 700; }
.hero-subtitle { display: block; margin-top: 12rpx; color: #a0522d; font-size: 28rpx; }
.hero-image { position: absolute; right: 28rpx; top: 34rpx; width: 170rpx; height: 130rpx; opacity: 0.22; }
.task-card { margin: 32rpx; padding: 32rpx 40rpx; border-radius: 32rpx; background: #fff; box-shadow: 0 8rpx 40rpx rgba(0, 0, 0, 0.08); }
.task-header { display: flex; align-items: center; margin-bottom: 12rpx; }
.task-badge { margin-right: 16rpx; padding: 8rpx 24rpx; border-radius: 24rpx; color: #fff; background: linear-gradient(135deg, #ff6b35, #ff8c42); font-size: 24rpx; font-weight: 600; }
.task-heading { color: #666; font-size: 28rpx; }
.task-row { min-height: 100rpx; display: flex; align-items: center; justify-content: space-between; border-bottom: 1rpx solid #f5f5f5; }
.task-row:last-child { border-bottom: 0; }
.task-info { min-width: 0; display: flex; align-items: center; }
.task-icon-box { width: 72rpx; height: 72rpx; margin-right: 20rpx; display: flex; align-items: center; justify-content: center; flex-shrink: 0; border-radius: 20rpx; }
.icon-blue { background: #e6f4ff; }
.icon-green { background: #e6ffe6; }
.icon-orange { background: #fff0e6; }
.task-icon { width: 32rpx; height: 32rpx; }
.task-name { color: #333; font-size: 29rpx; font-weight: 500; }
.task-action { width: auto; height: 56rpx; margin: 0; padding: 0 28rpx; border: 0; border-radius: 28rpx; color: #fff; background: linear-gradient(135deg, #ff6b35, #ff8c42); font-size: 24rpx; line-height: 56rpx; }
.task-action::after { border: 0; }
.task-action.completed { color: #999; background: #f5f5f5; }
.learn-entry { display: flex; gap: 24rpx; padding: 0 32rpx; margin-bottom: 40rpx; }
.entry-card { flex: 1; padding: 32rpx; display: flex; flex-direction: column; align-items: center; border-radius: 24rpx; background: #fff; }
.entry-icon-box { width: 96rpx; height: 96rpx; display: flex; align-items: center; justify-content: center; border-radius: 24rpx; }
.entry-icon { width: 48rpx; height: 48rpx; }
.entry-title { margin-top: 20rpx; color: #333; font-size: 30rpx; font-weight: 600; }
.entry-status { margin-top: 12rpx; color: #ff6b35; font-size: 24rpx; }
.section-header { padding: 0 32rpx; margin-bottom: 24rpx; }
.section-title { color: #333; font-size: 36rpx; font-weight: 700; }
.course-grid { display: flex; flex-wrap: wrap; gap: 24rpx; padding: 0 32rpx 40rpx; }
.course-card { width: calc(50% - 12rpx); overflow: hidden; border-radius: 24rpx; background: #fff; box-sizing: border-box; }
.course-cover { position: relative; height: 180rpx; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.cover-1 { background: linear-gradient(135deg, #4a90e2, #357abd); }
.cover-2 { background: linear-gradient(135deg, #f5a623, #e8941a); }
.cover-3 { background: linear-gradient(135deg, #52c41a, #389e0d); }
.cover-4 { background: linear-gradient(135deg, #ff6b35, #e55a2b); }
.cover-image { position: absolute; inset: 0; width: 100%; height: 100%; }
.play-button { position: relative; z-index: 1; width: 72rpx; height: 72rpx; display: flex; align-items: center; justify-content: center; border-radius: 50%; background: rgba(0, 0, 0, 0.5); }
.play-icon { width: 22rpx; height: 28rpx; margin-left: 4rpx; }
.course-info { padding: 24rpx; }
.course-title { height: 72rpx; display: -webkit-box; overflow: hidden; color: #333; font-size: 28rpx; font-weight: 600; line-height: 36rpx; -webkit-box-orient: vertical; -webkit-line-clamp: 2; }
.course-meta { margin-top: 16rpx; display: flex; align-items: center; justify-content: space-between; color: #999; font-size: 22rpx; }
.duration { display: flex; align-items: center; gap: 8rpx; }
.clock-icon { width: 22rpx; height: 22rpx; }
.state { padding: 100rpx 32rpx; color: #999; font-size: 26rpx; text-align: center; }
.state.error { color: #e34d59; }
</style>
