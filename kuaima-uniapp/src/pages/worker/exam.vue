<template>
  <view class="page"
    ><AppNavBar title="接单资格考试" :show-back="true" /><scroll-view
      scroll-y
      class="content"
      ><view class="tip"
        >共 {{ questions.length }} 道题，答对 {{ passScore }} 道即可通过</view
      ><view v-for="(q, index) in questions" :key="q.id" class="card"
        ><text class="question">{{ index + 1 }}. {{ q.content }}</text
        ><view
          v-for="option in q.options"
          :key="option"
          :class="['option', { active: answers[q.id] === option.value }]"
          @click="answers[q.id] = option.value"
          >{{ option.label }}</view
        ></view
      ></scroll-view
    ><SafeBottomAction
      ><button class="submit" :disabled="submitting" @click="submit">
        {{ submitting ? "提交中…" : "提交考试" }}
      </button></SafeBottomAction
    ></view
  >
</template>
<script setup>
import { onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import SafeBottomAction from "@/components/SafeBottomAction.vue";
import { getExam, submitExam } from "@/api/backend";
const pages = getCurrentPages();
const courseId =
  pages[pages.length - 1]?.options?.courseId ||
  pages[pages.length - 1]?.options?.id;
const questions = ref([]);
const examId = ref("");
const answers = ref({});
const passScore = ref(0);
const submitting = ref(false);
onMounted(async () => {
  if (!courseId) return uni.showToast({ title: "缺少课程信息", icon: "none" });
  try {
    const result = await getExam(courseId);
    const exam = result?.exam || result;
    examId.value = exam?.id || result?.examId || "";
    passScore.value = exam?.passScore || exam?.passingScore || 0;
    const rows = result?.questions || exam?.questions || [];
    questions.value = rows.map((item) => ({
      ...item,
      options: normalizeOptions(item.options),
    }));
  } catch (error) {
    uni.showToast({ title: error.message || "考试加载失败", icon: "none" });
  }
});
function normalizeOptions(options) {
  let value = options;
  if (typeof value === "string") {
    try {
      value = JSON.parse(value);
    } catch (_) {
      value = value.split("|");
    }
  }
  if (Array.isArray(value))
    return value.map((item, index) =>
      typeof item === "object"
        ? {
            value: item.value || item.key || String.fromCharCode(65 + index),
            label: item.label || item.text || item.value,
          }
        : { value: String.fromCharCode(65 + index), label: item },
    );
  return Object.entries(value || {}).map(([key, label]) => ({
    value: key,
    label,
  }));
}
async function submit() {
  if (Object.keys(answers.value).length < questions.value.length)
    return uni.showToast({ title: "请完成所有题目", icon: "none" });
  submitting.value = true;
  try {
    const result = await submitExam(courseId, {
      userId: uni.getStorageSync("userId") || "2001",
      answers: answers.value,
    });
    uni.redirectTo({
      url: `/pages/worker/exam-result?score=${result?.score || 0}&passed=${Boolean(result?.passed)}&examId=${result?.examId || examId.value}`,
    });
  } catch (error) {
    uni.showToast({ title: error.message || "考试提交失败", icon: "none" });
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
  padding: 22rpx;
  box-sizing: border-box;
}
.tip {
  padding: 24rpx;
  background: #fff4e6;
  color: #8b4513;
  border-radius: 18rpx;
  font-size: 24rpx;
}
.card {
  margin-top: 20rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 26rpx;
}
.question {
  display: block;
  font-size: 27rpx;
  font-weight: 700;
  margin-bottom: 20rpx;
}
.option {
  padding: 20rpx;
  background: #f7f7f7;
  border-radius: 12rpx;
  margin-top: 12rpx;
  color: #666;
  font-size: 24rpx;
}
.option.active {
  background: #fff1e8;
  color: #ff6b35;
  border: 1rpx solid #ff6b35;
}
.submit {
  width: 100%;
  height: 84rpx;
  line-height: 84rpx;
  border: 0;
  border-radius: 44rpx;
  background: #ff6b35;
  color: #fff;
  font-size: 29rpx;
}
.submit[disabled] {
  background: #bbb;
}
</style>
