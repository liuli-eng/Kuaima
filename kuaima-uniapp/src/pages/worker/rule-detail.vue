<template><PolicyArticle :title="title" :sections="sections" /></template>
<script setup>
import { ref } from "vue";
import PolicyArticle from "@/components/PolicyArticle.vue";
const pages = getCurrentPages();
const title = ref(
  decodeURIComponent(pages[pages.length - 1]?.options?.title || "规则详情"),
);
const sections = [
  {
    title: "适用范围",
    content:
      "本规则适用于快马日结平台内的岗位浏览、报名、履约、评价、结算及申诉等相关行为。",
  },
  {
    title: "用户义务",
    content:
      "用户应提供真实信息、按约到岗并诚信完成任务；因特殊情况无法履约时，应及时按照平台流程处理。",
  },
  {
    title: "违规处理",
    content:
      "平台将结合事实、证据和规则对违规行为进行提醒、限制功能、信用分调整等处理，用户可依法发起申诉。",
  },
];
</script>
