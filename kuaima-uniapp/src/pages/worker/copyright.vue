<template><PolicyArticle title="知识产权规则" :sections="sections" /></template>
<script setup>
import PolicyArticle from "@/components/PolicyArticle.vue";
const sections = [
  {
    title: "权利归属",
    content: "平台的文字、图形、界面设计及相关软件内容依法受到知识产权保护。",
  },
  {
    title: "用户内容",
    content:
      "用户上传的内容应具有合法来源，不得侵犯他人的商标权、著作权或其他合法权益。",
  },
  {
    title: "禁止行为",
    content:
      "未经许可不得复制、修改、传播平台内容，也不得利用平台标识进行误导性宣传。",
  },
  {
    title: "投诉处理",
    content: "权利人发现侵权线索时，可通过客服渠道提交权属证明和具体侵权信息。",
  },
];
</script>
