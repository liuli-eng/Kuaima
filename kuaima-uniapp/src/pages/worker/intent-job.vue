<template>
  <IntentTagForm
    title="期望工种与薪资"
    button-text="保存信息"
    storage-key="workerIntentJob"
    :groups="groups"
    :rows="rows"
  />
</template>
<script setup>
import IntentTagForm from "@/components/IntentTagForm.vue";
const groups = [
  {
    key: "jobs",
    title: "期望工种（可多选）",
    required: true,
    selected: ["电子厂普工", "餐饮服务员"],
    items: [
      "电子厂普工",
      "餐饮服务员",
      "快递分拣",
      "仓储物流",
      "建筑工人",
      "焊工/技工",
      "活动引导",
      "家政保洁",
      "其他",
    ],
  },
  {
    key: "salary",
    title: "期望薪资",
    required: true,
    selected: ["100-200元/天"],
    items: [
      "100元以下/天",
      "100-200元/天",
      "200-300元/天",
      "300元以上/天",
      "面议",
    ],
  },
  {
    key: "mode",
    title: "期望工作方式",
    required: true,
    selected: ["每天日结"],
    items: ["每天日结", "压薪日结", "月结", "长期稳定"],
  },
];
const rows = [
  { label: "所在城市", value: "上海" },
  { label: "期望区域", value: "请选择" },
];
</script>
