<template>
  <IntentTagForm
    title="工作偏好"
    button-text="保存偏好"
    storage-key="workerIntentPreference"
    :groups="groups"
    :rows="rows"
  />
</template>
<script setup>
import IntentTagForm from "@/components/IntentTagForm.vue";
const groups = [
  {
    key: "times",
    title: "能做的时段",
    selected: ["早班(6-14点)", "中班(14-22点)"],
    items: [
      "早班(6-14点)",
      "中班(14-22点)",
      "夜班(22-6点)",
      "全天",
      "周末",
      "灵活时段",
    ],
  },
  {
    key: "conditions",
    title: "工作条件",
    selected: ["包吃", "包住"],
    items: ["包吃", "包住", "有空调", "提供交通", "有加班费", "可带手机"],
  },
  {
    key: "preferences",
    title: "其他偏好",
    selected: ["近距离(3km内)"],
    items: [
      "近距离(3km内)",
      "有经验优先",
      "新手可做",
      "有保障岗位",
      "当天结算",
      "接受培训",
    ],
  },
];
const rows = [
  { label: "最远通勤距离", value: "5km" },
  { label: "步行/骑行时间", value: "30分钟内" },
];
</script>
