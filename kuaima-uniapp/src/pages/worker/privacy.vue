<template><PolicyArticle title="隐私政策" :sections="sections" /></template>
<script setup>
import PolicyArticle from "@/components/PolicyArticle.vue";
const sections = [
  {
    title: "信息收集",
    content:
      "为提供注册、实名认证、岗位报名、订单履约和结算提现服务，平台会在必要范围内收集相关账号及业务信息。",
  },
  {
    title: "信息使用",
    content:
      "个人信息仅用于实现产品功能、保障交易安全、处理客服申诉及履行法律法规要求。",
  },
  {
    title: "信息保护",
    content:
      "平台采取合理的安全措施保护个人信息，并对访问、存储和传输过程进行必要控制。",
  },
  {
    title: "您的权利",
    content:
      "您可以依法查询、更正或申请删除相关信息，也可以通过客服渠道提出个人信息相关诉求。",
  },
];
</script>
