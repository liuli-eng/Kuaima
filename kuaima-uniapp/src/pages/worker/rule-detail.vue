<template>
  <view class="page">
    <view class="top-nav" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="nav-inner">
        <button class="nav-back" @click="goBack"><image :src="chevronLeftIcon" mode="aspectFit" class="nav-back-icon" /></button>
        <text class="nav-title">{{ categoryTitle }}</text>
        <view class="nav-space" />
      </view>
    </view>
    <scroll-view scroll-y class="content">
      <view v-if="loading" class="state">加载中…</view>
      <view v-else-if="notFound" class="state"><text>规则不存在或尚未发布</text><button class="back-button" @click="goBack">返回</button></view>
      <view v-else-if="loadError" class="state"><text>{{ loadError }}</text><button class="back-button" @click="loadRule">重新加载</button></view>
      <view v-else class="article">
        <template v-if="isRichText">
          <view class="content-card article-card">
            <text class="article-title">{{ rule.title || categoryTitle }}</text>
            <rich-text :nodes="rule.content" class="content-body" />
          </view>
        </template>
        <template v-else>
          <view
            v-for="(section, index) in contentSections"
            :key="`${section.title}-${index}`"
            class="content-card"
          >
            <text v-if="index === 0" class="article-title">{{ rule.title || categoryTitle }}</text>
            <text v-if="section.title" class="section-title">{{ section.title }}</text>
            <text class="content-body plain-text">{{ section.content }}</text>
          </view>
        </template>
        <view class="bottom-space" />
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { getRule, listRules } from "@/api/backend";
import { handleTokenInvalid } from "@/api/auth";
import chevronLeftIcon from "/static/icons/worker-rule/chevron-left-gray.svg";

const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0;
const categoryMap = { notice: "NOTICE", credit: "CREDIT", fee: "FEE", trade: "TRADE", fly: "FLY" };
const categoryTitleMap = { NOTICE: "规则公示", CREDIT: "信用分规则", FEE: "收费规则", TRADE: "交易规则", FLY: "飞单认定与处理规则" };
const routeOptions = ref({});
const rule = ref({ title: "", content: "" });
const loading = ref(true);
const notFound = ref(false);
const loadError = ref("");
const category = computed(() => {
  const value = decodeRouteValue(routeOptions.value.category || routeOptions.value.type);
  return categoryMap[value.toLowerCase()] || value.toUpperCase();
});
const categoryTitle = computed(() => decodeRouteValue(routeOptions.value.title) || categoryTitleMap[category.value] || "规则详情");
const isRichText = computed(() => /<([a-z][\s\S]*?)>/i.test(String(rule.value.content || "")));
const contentSections = computed(() => {
  const content = String(rule.value.content || "").trim();
  if (!content) return [];
  const lines = content.split(/\r?\n/);
  const sections = [];
  let current = { title: "", lines: [] };
  lines.forEach((line) => {
    const text = line.trim();
    if (/^[一二三四五六七八九十百]+[、.．]/.test(text) && current.lines.length) {
      sections.push({ title: current.title, content: current.lines.join("\n").trim() });
      current = { title: text, lines: [] };
    } else if (/^[一二三四五六七八九十百]+[、.．]/.test(text) && !current.lines.length) {
      current.title = text;
    } else if (text || current.lines.length) {
      current.lines.push(line);
    }
  });
  if (current.title || current.lines.length) {
    sections.push({ title: current.title, content: current.lines.join("\n").trim() });
  }
  return sections.filter((section) => section.title || section.content);
});

onLoad((options = {}) => {
  routeOptions.value = options;
  loadRule();
});

async function loadRule() {
  const id = routeOptions.value.id;
  if (!id && !category.value) {
    loading.value = false;
    notFound.value = true;
    return;
  }
  loading.value = true;
  notFound.value = false;
  loadError.value = "";
  rule.value = { title: "", content: "" };
  try {
    if (id) rule.value = (await getRule(id)) || {};
    else {
      const result = await listRules(category.value);
      const rules = normalizeRows(result);
      rule.value = rules[0] || {};
      notFound.value = rules.length === 0;
    }
  } catch (error) {
    const errorCode = Number(error?.code || error?.statusCode);
    if (errorCode === 401) return handleTokenInvalid({ role: "worker" });
    if (errorCode === 404) notFound.value = true;
    else loadError.value = errorCode === 400 ? "规则分类参数错误" : error?.message || "规则加载失败，请稍后重试";
    if (loadError.value) uni.showToast({ title: loadError.value, icon: "none" });
  } finally { loading.value = false; }
}

function decodeRouteValue(value) {
  const text = String(value || "").trim();
  if (!text) return "";
  try { return decodeURIComponent(text); } catch (_) { return text; }
}
function normalizeRows(result) {
  if (Array.isArray(result)) return result;
  if (Array.isArray(result?.records)) return result.records;
  if (Array.isArray(result?.content)) return result.content;
  if (Array.isArray(result?.data)) return result.data;
  return result?.data && typeof result.data === "object" ? [result.data] : [];
}
function goBack() {
  const pages = getCurrentPages();
  if (pages.length > 1) return uni.navigateBack();
  uni.redirectTo({ url: "/pages/worker/rule" });
}
</script>

<style scoped>
.page { height: 100vh; background: #F5F5F5; display: flex; flex-direction: column; overflow: hidden; }
.top-nav { background: #fff; border-bottom: 1rpx solid #f0f0f0; box-sizing: border-box; flex-shrink: 0; }
.nav-inner { position: relative; height: 104rpx; padding: 16rpx 32rpx 24rpx; box-sizing: border-box; display: flex; align-items: center; justify-content: space-between; }
.nav-back { width: 64rpx; height: 64rpx; padding: 0; margin: 0; border: 0; background: transparent; display: flex; align-items: center; justify-content: center; }
.nav-back::after { border: 0; }
.nav-back-icon { width: 32rpx; height: 40rpx; }
.nav-title { position: absolute; left: 50%; max-width: 360rpx; color: #333; font-size: 34rpx; font-weight: 600; text-align: center; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; transform: translateX(-50%); }
.nav-space { width: 116rpx; height: 56rpx; }
.content { flex: 1; min-height: 0; box-sizing: border-box; background: #F5F5F5; }
.article { padding-top: 24rpx; }
.content-card { margin: 0 32rpx 24rpx; padding: 36rpx 40rpx 40rpx; border-radius: 24rpx; box-sizing: border-box; background: #fff; }
.article-card { box-sizing: border-box; }
.article-title { display: block; margin-bottom: 32rpx; color: #333; font-size: 36rpx; font-weight: 700; line-height: 1.5; text-align: center; }
.section-title { position: relative; display: block; margin-bottom: 24rpx; padding-left: 24rpx; color: #333; font-size: 32rpx; font-weight: 700; line-height: 1.5; }
.section-title::before { content: ""; position: absolute; top: 8rpx; bottom: 8rpx; left: 0; width: 8rpx; border-radius: 4rpx; background: linear-gradient(180deg, #ff6b35, #ff8c42); }
.content-body { display: block; color: #666; font-size: 28rpx; line-height: 1.85; }
.plain-text { display: block; white-space: pre-wrap; }
.state { padding: 220rpx 32rpx 0; text-align: center; color: #999; font-size: 28rpx; }
.state > text { display: block; }
.back-button { width: 220rpx; margin-top: 32rpx; border-radius: 12rpx; background: #ff6b35; color: #fff; font-size: 28rpx; }
.back-button::after { border: 0; }
.bottom-space { height: 40rpx; }
</style>
