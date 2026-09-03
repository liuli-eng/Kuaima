<template>
  <WorkerFeaturePage
    title="活动中心"
    icon="🎁"
    headline="新用户专享福利"
    description="完成首单即可领取奖励"
    :items="items"
    button-text="立即去接单"
  />
</template>
<script setup>
import WorkerFeaturePage from "@/components/WorkerFeaturePage.vue";
const items = [
  { title: "首单奖励", desc: "完成首个订单，额外奖励20元", icon: "¥" },
  { title: "每日签到", desc: "连续签到赢积分", icon: "✓" },
];
</script>
