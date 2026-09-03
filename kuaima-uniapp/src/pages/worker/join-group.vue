<template>
  <WorkerFeaturePage
    title="添加找活管家"
    icon="群"
    headline="加入找活交流群"
    description="群内每日发布优质日结岗位"
    :items="items"
    button-text="保存二维码"
  />
</template>
<script setup>
import WorkerFeaturePage from "@/components/WorkerFeaturePage.vue";
const items = [
  {
    title: "快马日结找活群",
    desc: "扫码添加找活管家，邀请你进群",
    icon: "▦",
    arrow: false,
  },
];
</script>
