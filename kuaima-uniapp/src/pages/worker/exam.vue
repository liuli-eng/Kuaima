<template>
  <view class="page"
    ><AppNavBar title="接单资格考试" :show-back="true" /><scroll-view
      scroll-y
      class="content"
      ><view class="tip"
        >共 {{ questions.length }} 道题，答对 {{ passScore }} 道即可通过</view
      ><view v-for="(q, index) in questions" :key="q.id" class="card"
        ><text class="question">{{ index + 1 }}. {{ q.question }}</text
        ><view
          v-for="option in q.options"
          :key="option"
          :class="['option', { active: answers[index] === option }]"
          @click="answers[index] = option"
          >{{ option }}</view
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
import { ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import SafeBottomAction from "@/components/SafeBottomAction.vue";
import { request } from "@/api/http";
const questions = [
  {
    id: 1,
    question: "报名成功后，应该如何做？",
    options: ["按时到岗并遵守现场安排", "随意取消订单", "转让给他人"],
  },
  {
    id: 2,
    question: "完成工作后，结算由谁确认？",
    options: ["雇主确认后平台结算", "无需确认自动到账", "零工自行填写"],
  },
  {
    id: 3,
    question: "遇到工作纠纷应联系？",
    options: ["平台客服和雇主", "私下收费中介", "不处理"],
  },
];
const answers = ref([]);
const passScore = 2;
const submitting = ref(false);
async function submit() {
  if (answers.value.filter(Boolean).length < questions.length)
    return uni.showToast({ title: "请完成所有题目", icon: "none" });
  const score = answers.value.filter(
    (a, i) => a === questions[i].options[0],
  ).length;
  submitting.value = true;
  try {
    await request({
      url: "/worker/exam/submit",
      method: "POST",
      data: { score },
    });
  } catch (_) {
  } finally {
    submitting.value = false;
  }
  uni.navigateTo({
    url: `/pages/worker/exam-result?score=${score}&passed=${score >= passScore}`,
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
