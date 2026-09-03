<template>
  <view class="page"
    ><AppNavBar title="学习中心" :show-back="true" /><scroll-view
      scroll-y
      class="content"
      ><view class="hero"
        ><text class="title">完成学习，获取接单资格</text
        ><text>实名认证、规则学习和考试合格后即可报名更多岗位</text
        ><progress
          :percent="progress"
          activeColor="#ff6b35"
          backgroundColor="#ffffff66"
          stroke-width="8" /></view
      ><view class="task-card"
        ><view class="task-header"
          ><text class="task-badge">新人任务</text
          ><text class="task-title">完成任务，获取接单资格</text></view
        ><view v-for="task in tasks" :key="task.name" class="task-row"
          ><view class="task-name"
            ><text class="task-icon">{{ task.icon }}</text
            ><text>{{ task.name }}</text></view
          ><text class="task-btn" @click="openTask(task)">{{
            task.done ? "已完成" : "去完成"
          }}</text></view
        ></view
      ><view class="section-heading">赚钱接单攻略</view
      ><view
        v-for="item in courses"
        :key="item.key"
        class="card"
        @click="open(item)"
        ><view class="icon">{{ item.icon }}</view
        ><view class="main"
          ><text class="name">{{ item.name }}</text
          ><text class="desc">{{ item.desc }}</text
          ><text class="state"
            >{{ item.done ? "已完成" : "去学习" }} ›</text
          ></view
        ></view
      ></scroll-view
    ></view
  >
</template>
<script setup>
import { computed, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
const courses = ref([
  {
    key: "course",
    icon: "🎬",
    name: "新手接单课程",
    desc: "系统学习从报名到结算的完整流程",
    done: false,
  },
  {
    key: "rule",
    icon: "📖",
    name: "平台规则",
    desc: "了解接单、取消和结算规则",
    done: false,
  },
  {
    key: "safety",
    icon: "🛡",
    name: "安全须知",
    desc: "学习工作现场安全规范",
    done: false,
  },
  {
    key: "exam",
    icon: "📝",
    name: "接单资格考试",
    desc: "完成考试后解锁接单资格",
    done: false,
  },
]);
const tasks = ref([
  {
    name: "模拟接单-体验全流程",
    icon: "☷",
    done: false,
    path: "/pages/worker/simulate-order",
  },
  {
    name: "学习平台规则",
    icon: "▤",
    done: false,
    path: "/pages/worker/course-detail?type=rule",
  },
  { name: "答题测试", icon: "✓", done: false, path: "/pages/worker/exam" },
]);
const progress = computed(
  () =>
    (courses.value.filter((i) => i.done).length / courses.value.length) * 100,
);
function open(item) {
  if (item.key === "course") {
    uni.navigateTo({ url: "/pages/worker/course-order" });
    return;
  }
  uni.navigateTo({
    url:
      item.key === "exam"
        ? "/pages/worker/exam"
        : `/pages/worker/course-detail?type=${item.key}`,
  });
}
function openTask(task) {
  uni.navigateTo({ url: task.path });
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #fff8e6;
}
.content {
  height: calc(100vh - 176rpx);
  padding: 22rpx;
  box-sizing: border-box;
}
.hero {
  padding: 34rpx 28rpx;
  background: linear-gradient(135deg, #fff4e6, #ffe4b5);
  border-radius: 22rpx;
  margin-bottom: 20rpx;
  color: #8b4513;
  box-shadow: 0 8rpx 20rpx rgba(255, 140, 0, 0.1);
}
.hero .title {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  margin-bottom: 12rpx;
}
.hero text:nth-child(2) {
  font-size: 22rpx;
}
.hero progress {
  display: block;
  margin-top: 22rpx;
}
.task-card {
  margin-bottom: 20rpx;
  padding: 24rpx;
  border-radius: 20rpx;
  background: #fff;
  box-shadow: 0 5rpx 16rpx rgba(88, 64, 32, 0.05);
}
.task-header {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}
.task-badge {
  margin-right: 10rpx;
  padding: 5rpx 12rpx;
  border-radius: 16rpx;
  background: #fff0e6;
  color: #ff6b35;
  font-size: 20rpx;
}
.task-title,
.section-heading {
  color: #333;
  font-size: 28rpx;
  font-weight: 700;
}
.task-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18rpx 0;
  border-top: 1rpx solid #f5f1eb;
}
.task-name {
  display: flex;
  align-items: center;
  color: #444;
  font-size: 25rpx;
}
.task-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48rpx;
  height: 48rpx;
  margin-right: 12rpx;
  border-radius: 50%;
  background: #fff4e6;
  color: #ff6b35;
}
.task-btn {
  color: #ff6b35;
  font-size: 23rpx;
}
.section-heading {
  margin: 28rpx 4rpx 16rpx;
}
.card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx 24rpx;
  margin-bottom: 18rpx;
  box-shadow: 0 5rpx 16rpx rgba(88, 64, 32, 0.05);
}
.icon {
  width: 78rpx;
  height: 78rpx;
  line-height: 78rpx;
  text-align: center;
  background: #fff4e6;
  border-radius: 18rpx;
  font-size: 40rpx;
}
.main {
  flex: 1;
  margin-left: 20rpx;
}
.name {
  display: block;
  font-size: 29rpx;
  font-weight: 700;
}
.desc {
  display: block;
  color: #999;
  font-size: 22rpx;
  margin-top: 8rpx;
}
.state {
  float: right;
  color: #ff6b35;
  font-size: 23rpx;
}
</style>
