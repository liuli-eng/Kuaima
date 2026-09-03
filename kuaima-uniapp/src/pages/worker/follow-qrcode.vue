<template>
  <WorkerFeaturePage
    title="关注快马"
    icon="微"
    headline="关注快马日结公众号"
    description="及时获取附近新岗位和活动通知"
    :items="items"
  />
</template>
<script setup>
import WorkerFeaturePage from "@/components/WorkerFeaturePage.vue";
const items = [
  {
    title: "微信扫一扫关注",
    desc: "打开微信扫描二维码即可关注",
    icon: "▦",
    arrow: false,
  },
];
</script>
