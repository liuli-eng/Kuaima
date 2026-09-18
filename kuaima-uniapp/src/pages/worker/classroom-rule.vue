<template>
  <view class="page">
    <AppNavBar title="学习平台规则" :show-back="true" />

    <scroll-view scroll-y class="content">
      <view class="hero-lines">
        <text><text class="highlight">学习注意事项</text></text>
        <text><text class="highlight">接单避免踩坑</text></text>
        <text><text class="highlight">熟记平台规则</text></text>
        <text><text class="highlight">多接高价好单</text></text>
      </view>

      <view class="intro-bubble">
        <text>以下为快马新手零工必学注意事项及平台规则，</text>
        <text class="emphasis">请在测试前认真学习。</text>
      </view>

      <view class="group-pill"><text>{{ course.title || "接单全流程注意事项" }}</text></view>

      <view v-if="loading" class="state">规则内容加载中…</view>
      <view v-else-if="loadError" class="state error" @click="loadCourse">
        {{ loadError }}，点击重试
      </view>
      <view v-else-if="sections.length" class="sections">
        <view v-for="(section, index) in sections" :key="section.id || index" class="rule-section">
          <text class="section-head">{{ section.title || `学习内容 ${index + 1}` }}</text>
          <view class="section-body">
            <rich-text v-if="section.html" class="rich-content" :nodes="section.html" />
            <text v-else class="plain-content">{{ section.content }}</text>
          </view>
        </view>
      </view>
      <view v-else class="state">后台暂未发布学习规则</view>
    </scroll-view>
  </view>
</template>

<script setup>
import { onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import { getCourse } from "@/api/backend";

const pages = getCurrentPages();
const id = pages[pages.length - 1]?.options?.id;
const loading = ref(false);
const loadError = ref("");
const course = ref({});
const sections = ref([]);

onMounted(loadCourse);

async function loadCourse() {
  if (!id) {
    loadError.value = "缺少规则课程信息";
    return;
  }
  loading.value = true;
  loadError.value = "";
  try {
    const result = await getCourse(id);
    course.value = result?.course || result || {};
    sections.value = normalizeSections(course.value);
  } catch (error) {
    course.value = {};
    sections.value = [];
    loadError.value = error?.message || "学习规则加载失败";
  } finally {
    loading.value = false;
  }
}

function normalizeSections(value) {
  let rows = value.sections || value.contentSections || value.chapters;
  if (typeof rows === "string") {
    try {
      rows = JSON.parse(rows);
    } catch (_) {
      rows = null;
    }
  }
  if (Array.isArray(rows) && rows.length) {
    return rows.map((item, index) => normalizeSection(item, index));
  }
  const content = value.content || value.body || value.details || value.intro || "";
  if (!String(content).trim()) return [];
  return [normalizeSection({ title: value.title, content }, 0)];
}

function normalizeSection(item, index) {
  const content = String(item.content || item.body || item.text || "").trim();
  return {
    id: item.id || index,
    title: item.title || item.name || "接单注意事项",
    content,
    html: /<([a-z][\s\S]*?)>/i.test(content) ? content : "",
  };
}
</script>

<style scoped>
.page { min-height: 100vh; background: #fff8e1; }
.content { height: calc(100vh - 176rpx); padding-bottom: calc(40rpx + env(safe-area-inset-bottom)); box-sizing: border-box; }
.hero-lines { padding: 28rpx 36rpx 20rpx; display: flex; flex-direction: column; align-items: center; gap: 4rpx; }
.hero-lines > text { color: #333; font-size: 44rpx; font-weight: 800; line-height: 1.35; }
.highlight { padding: 0 8rpx; background: linear-gradient(transparent 58%, #ffde59 58%); }
.intro-bubble { position: relative; margin: 0 32rpx 34rpx; padding: 28rpx 32rpx; border-radius: 24rpx; background: #fff; color: #555; font-size: 29rpx; line-height: 1.75; text-align: center; box-shadow: 0 4rpx 20rpx rgba(180, 140, 0, 0.08); }
.intro-bubble::after { content: ""; position: absolute; left: 50%; bottom: -16rpx; width: 0; height: 0; transform: translateX(-50%); border-left: 16rpx solid transparent; border-right: 16rpx solid transparent; border-top: 18rpx solid #fff; }
.emphasis { color: #e87900; font-weight: 700; }
.group-pill { margin: 0 auto 26rpx; text-align: center; }
.group-pill text { display: inline-block; padding: 16rpx 52rpx; border-radius: 999rpx; color: #5a4100; background: #ffe47a; font-size: 30rpx; font-weight: 700; }
.sections { padding-bottom: 20rpx; }
.rule-section { margin: 0 24rpx 36rpx; overflow: hidden; border-radius: 24rpx; background: #fff; box-shadow: 0 4rpx 18rpx rgba(180, 140, 0, 0.08); }
.section-head { display: block; padding: 26rpx 32rpx; color: #5a4100; background: linear-gradient(180deg, #ffde59 0%, #ffc81f 100%); font-size: 34rpx; font-weight: 800; }
.section-body { padding: 30rpx 32rpx; }
.plain-content,
.rich-content { color: #555; font-size: 29rpx; line-height: 1.9; }
.plain-content { white-space: pre-wrap; }
.state { padding: 120rpx 32rpx; color: #999; font-size: 26rpx; text-align: center; }
.state.error { color: #e34d59; }
</style>
