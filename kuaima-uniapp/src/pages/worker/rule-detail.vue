<template><PolicyArticle :title="title" :sections="sections" /></template>
<script setup>
import { onMounted, ref } from "vue";
import PolicyArticle from "@/components/PolicyArticle.vue";
import { getRule } from "@/api/backend";
const pages = getCurrentPages();
const options = pages[pages.length - 1]?.options || {};
const title = ref(decodeURIComponent(options.title || "规则详情"));
const sections = ref([]);
onMounted(async () => {
  if (!options.id) return;
  try {
    const rule = await getRule(options.id);
    title.value = rule.title || title.value;
    const content =
      rule.content || rule.body || rule.description || "暂无规则内容";
    sections.value = [{ title: rule.category || "规则内容", content }];
  } catch (error) {
    sections.value = [
      { title: "规则内容", content: error.message || "规则加载失败" },
    ];
  }
});
</script>
