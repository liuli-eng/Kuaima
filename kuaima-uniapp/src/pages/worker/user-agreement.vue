<template><PolicyArticle title="用户协议" :sections="sections" /></template>
<script setup>
import PolicyArticle from "@/components/PolicyArticle.vue";
const sections = [
  {
    title: "定义",
    content:
      "快马日结平台指网站、移动应用及微信小程序等服务载体；用户是完成注册并使用平台服务的自然人。",
  },
  {
    title: "协议的接受与修改",
    content:
      "用户注册、登录或使用平台服务，即表示已阅读、理解并接受本协议。平台将依法公示重要修订内容。",
  },
  {
    title: "账号注册与使用",
    content:
      "用户应使用真实手机号注册并妥善保管账号信息，涉及接单等业务时应按平台要求完成实名认证。",
  },
  {
    title: "服务规范",
    content:
      "用户应如实提供信息、诚信履约，不得发布违法信息、恶意取消订单或实施其他损害平台及他人权益的行为。",
  },
];
</script>
