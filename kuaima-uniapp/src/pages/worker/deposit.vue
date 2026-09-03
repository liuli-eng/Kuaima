<template>
  <WorkerFeaturePage
    title="积分中心"
    icon="积分"
    headline="我的积分"
    description="积分可用于兑换平台权益"
    :items="items"
    button-text="去赚积分"
  />
</template>
<script setup>
import WorkerFeaturePage from "@/components/WorkerFeaturePage.vue";
const items = [
  { title: "当前积分", desc: "0 分", icon: "✦" },
  { title: "积分明细", desc: "查看积分获取与使用记录", icon: "☷" },
  { title: "积分规则", desc: "了解积分使用说明", icon: "?" },
];
</script>
