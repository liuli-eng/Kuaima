<template>
  <WorkerFeaturePage
    title="工种徽章"
    icon="★"
    headline="我的工种徽章"
    description="完成对应岗位任务，解锁专属徽章"
    :items="items"
  />
</template>
<script setup>
import { onMounted, ref } from "vue";
import WorkerFeaturePage from "@/components/WorkerFeaturePage.vue";
import { listUserBadges } from "@/api/backend";
const items = ref([
  { title: "服务之星", desc: "累计完成10个订单", icon: "★" },
  { title: "准时达人", desc: "连续7次准时到岗", icon: "✓" },
  { title: "优质零工", desc: "保持良好信用评价", icon: "◆" },
]);
onMounted(async () => {
  try {
    const result = await listUserBadges(uni.getStorageSync("userId") || "2001");
    if (Array.isArray(result) && result.length) items.value = result;
  } catch (_) {}
});
</script>
