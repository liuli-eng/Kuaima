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
import { onMounted, ref } from "vue";
import WorkerFeaturePage from "@/components/WorkerFeaturePage.vue";
import { listSocialGroups } from "@/api/backend";
const items = ref([]);
onMounted(async () => {
  try {
    const result = await listSocialGroups();
    const rows = Array.isArray(result)
      ? result
      : result?.records || result?.content || [];
    items.value = rows.map((item) => ({
      title: item.name,
      desc: `${item.category || "找活交流"}${item.memberCount ? ` · ${item.memberCount}人` : ""}`,
      icon: item.qrcodeUrl || "▦",
      arrow: false,
    }));
  } catch (error) {
    uni.showToast({ title: error.message || "社群加载失败", icon: "none" });
  }
});
</script>
