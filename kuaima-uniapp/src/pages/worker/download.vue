<template>
  <WorkerFeaturePage
    title="下载APP"
    icon="↓"
    headline="快马日结 APP"
    description="更多岗位、更快接单、更便捷的工作体验"
    :items="items"
    button-text="下载客户端"
  />
</template>
<script setup>
import WorkerFeaturePage from "@/components/WorkerFeaturePage.vue";
const items = [
  { title: "安卓客户端下载", desc: "支持 Android 设备", icon: "A" },
  { title: "关注公众号", desc: "扫码获取最新岗位通知", icon: "微" },
];
</script>
