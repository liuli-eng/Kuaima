<template>
  <view class="page">
    <view class="top-nav" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="nav-inner">
        <button class="nav-back" @click="goBack"><image :src="chevronLeftIcon" mode="aspectFit" class="nav-back-icon" /></button>
        <text class="nav-title">规则详情</text>
        <view class="nav-space" />
      </view>
    </view>
    <scroll-view scroll-y class="content">
      <view v-if="loading" class="state">加载中…</view>
      <view v-else-if="notFound" class="state"><text>规则不存在或尚未发布</text><button class="back-button" @click="goBack">返回</button></view>
      <view v-else-if="loadError" class="state"><text>{{ loadError }}</text><button class="back-button" @click="loadRule">重新加载</button></view>
      <view v-else class="article">
        <view class="content-card article-head">
          <text class="article-title">{{ rule.title || categoryTitle }}</text>
          <view class="meta-row">
            <text v-if="rule.version" class="version-tag">{{ rule.version }}</text>
            <text v-if="rule.effectiveTime" class="meta">生效时间：{{ formatTime(rule.effectiveTime) }}</text>
          </view>
        </view>
        <view class="content-card">
          <view class="content-title"><view class="title-mark" /><text>规则内容</text></view>
          <rich-text v-if="isRichText" :nodes="rule.content" class="content-body" />
          <text v-else class="content-body plain-text">{{ rule.content || "暂无规则内容" }}</text>
        </view>
        <text v-if="rule.updateTime" class="update-time">更新时间：{{ formatTime(rule.updateTime) }}</text>
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
  const value = String(routeOptions.value.category || routeOptions.value.type || "").trim();
  return categoryMap[value.toLowerCase()] || value.toUpperCase();
});
const categoryTitle = computed(() => routeOptions.value.title || categoryTitleMap[category.value] || "规则详情");
const isRichText = computed(() => /<([a-z][\s\S]*?)>/i.test(String(rule.value.content || "")));

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
  try {
    if (id) rule.value = (await getRule(id)) || {};
    else {
      const result = await listRules(category.value);
      const rules = Array.isArray(result) ? result : [];
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

function formatTime(value) { return value ? String(value).replace("T", " ").slice(0, 16) : "-"; }
function goBack() {
  const pages = getCurrentPages();
  if (pages.length > 1) return uni.navigateBack();
  uni.redirectTo({ url: "/pages/worker/rule" });
}
</script>

<style scoped>
.page { height: 100vh; background: #F3F4F6; display: flex; flex-direction: column; overflow: hidden; }
.top-nav { background: #fff; border-bottom: 1rpx solid #f0f0f0; box-sizing: border-box; flex-shrink: 0; }
.nav-inner { height: 104rpx; padding: 16rpx 32rpx 24rpx; box-sizing: border-box; display: flex; align-items: center; justify-content: space-between; }
.nav-back { width: 64rpx; height: 64rpx; padding: 0; margin: 0; border: 0; background: transparent; display: flex; align-items: center; justify-content: center; }
.nav-back::after { border: 0; }
.nav-back-icon { width: 32rpx; height: 40rpx; }
.nav-title { color: #333; font-size: 34rpx; font-weight: 600; }
.nav-space { width: 64rpx; height: 64rpx; }
.content { flex: 1; min-height: 0; box-sizing: border-box; background: #F3F4F6; }
.content-card { margin: 24rpx 32rpx; padding: 32rpx 40rpx; border-radius: 24rpx; background: #fff; }
.article-head { text-align: center; }
.article-title { display: block; color: #333; font-size: 36rpx; font-weight: 700; line-height: 1.5; }
.meta-row { margin-top: 20rpx; display: flex; align-items: center; justify-content: center; flex-wrap: wrap; gap: 12rpx; }
.version-tag { padding: 4rpx 20rpx; border-radius: 20rpx; background: #fff0e6; color: #ff6b35; font-size: 24rpx; }
.meta { color: #999; font-size: 24rpx; }
.content-title { margin-bottom: 24rpx; display: flex; align-items: center; gap: 16rpx; color: #333; font-size: 32rpx; font-weight: 600; }
.title-mark { width: 8rpx; height: 32rpx; border-radius: 4rpx; background: linear-gradient(180deg, #ff6b35, #ff8c42); }
.content-body { color: #666; font-size: 28rpx; line-height: 1.8; }
.plain-text { display: block; white-space: pre-wrap; }
.update-time { display: block; padding: 0 40rpx; text-align: right; color: #aaa; font-size: 23rpx; }
.state { padding: 220rpx 32rpx 0; text-align: center; color: #999; font-size: 28rpx; }
.state > text { display: block; }
.back-button { width: 220rpx; margin-top: 32rpx; border-radius: 12rpx; background: #ff6b35; color: #fff; font-size: 28rpx; }
.back-button::after { border: 0; }
.bottom-space { height: 40rpx; }
</style>
