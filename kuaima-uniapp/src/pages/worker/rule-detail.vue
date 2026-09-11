<template>
  <view class="page"><AppNavBar :title="title" :show-back="true" /><scroll-view scroll-y class="content">
    <view v-if="loading" class="state">加载中…</view>
    <view v-else-if="notFound" class="state"><text>规则不存在或尚未发布</text><button class="back-button" @click="goBack">返回</button></view>
    <view v-else-if="loadError" class="state"><text>{{ loadError }}</text><button class="back-button" @click="goBack">返回</button></view>
    <view v-else class="article"><text class="article-title">{{ rule.title }}</text><text class="meta">版本：{{ rule.version || "-" }}</text><text class="meta">生效时间：{{ formatTime(rule.effectiveTime) }}</text><text class="meta">更新时间：{{ formatTime(rule.updateTime) }}</text><rich-text v-if="isRichText" :nodes="rule.content" class="content-body" /><text v-else class="content-body">{{ rule.content || "暂无规则内容" }}</text></view>
  </scroll-view></view>
</template>
<script setup>
import { computed, onMounted, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import { getRule } from "@/api/backend";
import { handleTokenInvalid } from "@/api/auth";
const pages = getCurrentPages();
const options = pages[pages.length - 1]?.options || {};
const title = ref("规则详情");
const rule = ref({ title: "规则详情", content: "" });
const loading = ref(true);
const notFound = ref(false);
const loadError = ref("");
const isRichText = computed(() => /<([a-z][\s\S]*?)>/i.test(String(rule.value.content || "")));
onMounted(async () => {
  if (!options.id) { notFound.value = true; loading.value = false; return; }
  try { const result = await getRule(options.id); rule.value = result || {}; title.value = result?.title || title.value; }
  catch (error) {
    const errorCode = Number(error?.code || error?.statusCode);
    if (errorCode === 401) return handleTokenInvalid({ role: "worker" });
    if (errorCode === 404) {
      notFound.value = true;
      uni.showToast({ title: "规则不存在或尚未发布", icon: "none" });
      setTimeout(() => uni.navigateBack(), 800);
    }
    else {
      loadError.value = errorCode === 400 ? "规则分类参数错误" : error?.message || "规则加载失败，请稍后重试";
      uni.showToast({ title: loadError.value, icon: "none" });
    }
  } finally { loading.value = false; }
});
function formatTime(value) { return value ? String(value).replace("T", " ").slice(0, 16) : "-"; }
function goBack() { uni.navigateBack(); }
</script>
<style scoped>
.page { min-height: 100vh; background: #fff; }.content { height: calc(100vh - 176rpx); padding: 32rpx; box-sizing: border-box; }.article-title { display: block; color: #333; font-size: 38rpx; font-weight: 700; line-height: 1.5; }.meta { display: block; margin-top: 12rpx; color: #999; font-size: 23rpx; }.content-body { display: block; margin-top: 32rpx; color: #555; font-size: 28rpx; line-height: 1.9; white-space: pre-wrap; }.state { padding: 220rpx 0; text-align: center; color: #999; font-size: 28rpx; }.back-button { width: 220rpx; margin-top: 32rpx; background: #ff6b35; color: #fff; font-size: 28rpx; }
</style>
