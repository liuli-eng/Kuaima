<template>
  <view class="page">
    <view class="status-spacer" :style="{ height: `${statusBarHeight}px` }" />
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">‹</view>
      <text class="nav-title">诚意分规则</text>
      <view class="nav-placeholder" />
    </view>

    <scroll-view scroll-y class="scroll-area">
      <view v-if="loading" class="state">规则加载中...</view>
      <view v-else-if="error" class="state error" @click="loadRule">{{ error }}，点击重试</view>
      <view v-else-if="!positiveGroups.length && !negativeGroups.length" class="state">暂无规则</view>
      <template v-else>
      <text v-if="positiveGroups.length" class="section-title">加分项规则</text>
      <view v-if="positiveGroups.length" class="rule-group">
        <view v-for="group in positiveGroups" :key="group.label" class="rule-sub">
          <view class="rule-type" :class="group.tone">{{ group.label }}</view>
          <view class="rule-items">
            <view v-for="item in group.items" :key="item.name" class="rule-item">
              <view class="rule-name"><text>{{ item.name }}</text><text class="info-icon">ⓘ</text></view>
              <text class="rule-score add">{{ item.score }}</text>
            </view>
          </view>
        </view>
      </view>
      <text v-if="negativeGroups.length" class="section-title">减分项规则</text>
      <view v-if="negativeGroups.length" class="rule-group">
        <view v-for="group in negativeGroups" :key="group.label" class="rule-sub">
          <view class="rule-type" :class="group.tone">{{ group.label }}</view>
          <view class="rule-items">
            <view v-for="item in group.items" :key="item.name" class="rule-item">
              <view class="rule-name"><text>{{ item.name }}</text><text class="info-icon">ⓘ</text></view>
              <text class="rule-score sub">{{ item.score }}</text>
            </view>
          </view>
        </view>
      </view>
      </template>
      <view class="bottom-space" />
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { listBossCreditRules } from "@/api/backend";

const statusBarHeight = ref(0);
const loading = ref(false);
const error = ref("");
const positiveGroups = ref([]);
const negativeGroups = ref([]);

onLoad(() => {
  try {
    const info = typeof uni.getWindowInfo === "function" ? uni.getWindowInfo() : uni.getSystemInfoSync();
    statusBarHeight.value = Number(info.statusBarHeight || 0);
  } catch (_) {}
  loadRule();
});

async function loadRule() {
  if (loading.value) return;
  loading.value = true;
  error.value = "";
  try {
    const result = await listBossCreditRules();
    const rows = normalizeRows(result);
    const candidates = rows
      .filter((item) => String(item?.status || "").toLowerCase() === "published" || item?.status === "已发布")
      .sort((a, b) => Number(b?.id || 0) - Number(a?.id || 0))
    const bossRules = candidates.filter((item) => /老板|雇主/.test(String(item?.title || "")));
    const rules = bossRules.length ? bossRules : candidates;
    const groups = rules.flatMap((item) => parseRuleContent(item?.content));
    positiveGroups.value = groups.filter((group) => group.direction === "ADD");
    negativeGroups.value = groups.filter((group) => group.direction === "SUB");
    if (rules.length && !positiveGroups.value.length && !negativeGroups.value.length) {
      error.value = "诚意分规则内容格式暂不支持";
    }
  } catch (err) {
    error.value = err?.message || "诚意分规则加载失败";
  } finally {
    loading.value = false;
  }
}

function normalizeRows(result) {
  if (Array.isArray(result)) return result;
  if (Array.isArray(result?.records)) return result.records;
  if (Array.isArray(result?.content)) return result.content;
  if (Array.isArray(result?.data)) return result.data;
  return result?.data && typeof result.data === "object" ? [result.data] : [];
}

function parseRuleContent(content) {
  const groups = [];
  let current = null;
  String(content || "").split(/\r?\n/).forEach((line) => {
    const text = line.trim();
    if (!text) return;
    const section = text.replace(/^[一二三四五六七八九十百]+[、.．]\s*/, "").replace(/[：:]$/, "");
    if (/加分项|加分/.test(section) && !/^\d+[.、]/.test(text)) {
      current = { label: "平台规则", tone: "daily", direction: "ADD", items: [] };
      groups.push(current);
      return;
    }
    if (/减分项|扣分/.test(section) && !/^\d+[.、]/.test(text)) {
      current = { label: "平台规则", tone: "all", direction: "SUB", items: [] };
      groups.push(current);
      return;
    }
    const item = text.replace(/^\d+[.、]\s*/, "");
    const match = item.match(/^(.+?)[：:]\s*(.+)$/);
    if (!match || !current) return;
    const score = match[2].trim();
    current.items.push({ name: match[1].trim(), score: normalizeScore(score) });
  });
  return groups.filter((group) => group.items.length);
}

function normalizeScore(value) {
  const text = String(value || "");
  if (/^[+＋]/.test(text)) return `加${text.slice(1)}`;
  if (/^[-－]/.test(text)) return `扣${text.slice(1)}`;
  return text;
}

function goBack() {
  const pages = getCurrentPages();
  if (pages.length > 1) uni.navigateBack();
  else uni.redirectTo({ url: "/pages/boss/creditor-score" });
}
</script>

<style scoped>
.page { display: flex; flex-direction: column; height: 100vh; overflow: hidden; background: #f5f6f8; }
.status-spacer, .nav-bar { flex-shrink: 0; background: #fff; }
.nav-bar { position: relative; display: flex; align-items: center; justify-content: space-between; height: 50px; padding: 0 16px; border-bottom: 1px solid #f0f0f0; box-sizing: border-box; }
.nav-back, .nav-placeholder { width: 32px; height: 32px; }
.nav-back { color: #333; font-size: 34px; line-height: 27px; text-align: left; }
.nav-title { position: absolute; left: 50%; color: #333; font-size: 17px; font-weight: 700; transform: translateX(-50%); }
.scroll-area { flex: 1; min-height: 0; padding: 12px; box-sizing: border-box; }
.state { padding: 100px 20px; color: #999; font-size: 14px; text-align: center; }
.state.error { color: #ff6b35; }
.section-title { display: block; margin: 4px 4px 10px; color: #333; font-size: 15px; font-weight: 700; }
.rule-group { margin-bottom: 14px; overflow: hidden; border-radius: 12px; background: #fff; }
.rule-sub { display: flex; align-items: stretch; }
.rule-type { display: flex; flex-shrink: 0; align-items: center; justify-content: center; width: 46px; color: #b8860b; background: #fff8e6; font-size: 12px; font-weight: 600; letter-spacing: 3px; writing-mode: vertical-rl; text-orientation: upright; }
.rule-type.press { color: #e67e22; background: #fff3e0; }
.rule-type.all { color: #2e86ab; background: #e8f4fb; }
.rule-items { flex: 1; }
.rule-item { display: flex; align-items: center; justify-content: space-between; min-height: 52px; padding: 0 16px; border-bottom: 1px solid #f5f5f5; box-sizing: border-box; }
.rule-sub:last-child .rule-item:last-child { border-bottom: 0; }
.rule-name { display: flex; align-items: center; gap: 6px; color: #333; font-size: 14px; }
.info-icon { color: #ccc; font-size: 12px; }
.rule-score { flex-shrink: 0; margin-left: 10px; font-size: 14px; font-weight: 600; }
.rule-score.add { color: #52c41a; }
.rule-score.sub { color: #ff4d4f; }
.bottom-space { height: 30px; }
</style>
