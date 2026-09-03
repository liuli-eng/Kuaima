<template>
  <view class="page"
    ><AppNavBar title="个人信息" :show-back="true" /><scroll-view
      scroll-y
      class="content"
      ><view class="avatar-section" @click="changeAvatar"
        ><view class="avatar">👤<text class="edit">⌕</text></view
        ><text>点击更换头像</text></view
      ><view class="card"
        ><view v-for="item in basic" :key="item.label" class="row"
          ><text class="label">{{ item.label }}</text
          ><input
            v-if="item.edit"
            v-model="form[item.key]"
            class="value"
          /><text v-else class="value">{{ item.value }}</text
          ><text class="arrow">›</text></view
        ></view
      ><view class="card"
        ><view v-for="item in work" :key="item.label" class="row"
          ><text class="label">{{ item.label }}</text
          ><input
            v-if="item.edit"
            v-model="form[item.key]"
            class="value"
          /><text
            v-else
            class="value"
            :class="{ placeholder: item.placeholder }"
            >{{ item.value }}</text
          ><text class="arrow">›</text></view
        ></view
      ></scroll-view
    ></view
  >
</template>
<script setup>
import { reactive } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
const form = reactive({
  nickname: "晴时见禾",
  gender: "男",
  birthday: "1990-01-01",
  city: "上海市浦东新区",
  skills: "分拣打包、搬运装卸",
  years: "3年",
  night: "是",
  intro: "",
});
const basic = [
  { label: "昵称", key: "nickname", edit: true },
  { label: "手机号", value: "138****5678" },
  { label: "性别", key: "gender", edit: true },
  { label: "生日", key: "birthday", edit: true },
  { label: "居住地", key: "city", edit: true },
];
const work = [
  { label: "擅长工种", key: "skills", edit: true },
  { label: "工作年限", key: "years", edit: true },
  { label: "是否接受夜班", key: "night", edit: true },
  {
    label: "个人简介",
    key: "intro",
    value: "介绍一下自己...",
    placeholder: true,
  },
];
function changeAvatar() {
  uni.showToast({ title: "更换头像功能开发中", icon: "none" });
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}
.content {
  height: calc(100vh - 176rpx);
}
.avatar-section {
  padding: 48rpx 32rpx 34rpx;
  text-align: center;
  background: #fff;
  color: #666;
  font-size: 24rpx;
}
.avatar {
  position: relative;
  width: 144rpx;
  height: 144rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16rpx;
  border-radius: 50%;
  background: #ffe4b5;
  color: #ff6b35;
  font-size: 70rpx;
}
.edit {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: #ff6b35;
  color: #fff;
  font-size: 24rpx;
  line-height: 44rpx;
}
.card {
  margin: 24rpx 32rpx 0;
  border-radius: 24rpx;
  background: #fff;
  overflow: hidden;
}
.row {
  display: flex;
  align-items: center;
  min-height: 96rpx;
  padding: 0 28rpx;
  border-bottom: 1rpx solid #f2f2f2;
}
.row:last-child {
  border: 0;
}
.label {
  width: 180rpx;
  color: #333;
  font-size: 28rpx;
}
.value {
  flex: 1;
  color: #666;
  font-size: 28rpx;
}
.value.placeholder {
  color: #ccc;
}
.arrow {
  color: #ddd;
  font-size: 34rpx;
}
</style>
