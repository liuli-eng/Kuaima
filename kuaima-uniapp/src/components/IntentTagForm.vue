<template>
  <view class="page">
    <AppNavBar :title="title" :show-back="true" />
    <scroll-view scroll-y class="content">
      <view v-for="group in groups" :key="group.key" class="group">
        <text class="section-title"
          >{{ group.required ? "* " : "" }}{{ group.title }}</text
        >
        <view class="tags"
          ><text
            v-for="item in group.items"
            :key="item"
            :class="['tag', { active: selected[group.key].includes(item) }]"
            @click="toggle(group.key, item)"
            >{{ item }}</text
          ></view
        >
      </view>
      <view v-if="rows.length" class="form"
        ><view v-for="row in rows" :key="row.label" class="row"
          ><text>{{ row.label }}</text
          ><text>{{ row.value }} ›</text></view
        ></view
      >
    </scroll-view>
    <SafeBottomAction
      ><button class="submit" @click="save">
        {{ buttonText }}
      </button></SafeBottomAction
    >
  </view>
</template>
<script setup>
import { reactive } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import SafeBottomAction from "@/components/SafeBottomAction.vue";
const props = defineProps({
  title: String,
  buttonText: { type: String, default: "保存信息" },
  groups: { type: Array, default: () => [] },
  rows: { type: Array, default: () => [] },
  storageKey: String,
});
const selected = reactive(
  Object.fromEntries(
    props.groups.map((group) => [group.key, [...(group.selected || [])]]),
  ),
);
function toggle(key, item) {
  const list = selected[key];
  const index = list.indexOf(item);
  index >= 0 ? list.splice(index, 1) : list.push(item);
}
function save() {
  if (props.storageKey) uni.setStorageSync(props.storageKey, selected);
  uni.showToast({ title: "保存成功", icon: "success" });
  setTimeout(() => uni.navigateBack(), 500);
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f8f4ed;
}
.content {
  height: calc(100vh - 176rpx);
  padding-bottom: 190rpx;
  box-sizing: border-box;
}
.group {
  padding-top: 26rpx;
}
.section-title {
  display: block;
  padding: 0 28rpx 16rpx;
  color: #666;
  font-size: 24rpx;
}
.section-title::first-letter {
  color: #ff4757;
}
.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  padding: 0 28rpx;
}
.tag {
  padding: 14rpx 24rpx;
  border: 1rpx solid #e8e3dc;
  border-radius: 32rpx;
  background: #fff;
  color: #666;
  font-size: 23rpx;
}
.tag.active {
  border-color: #ffd966;
  background: linear-gradient(135deg, #ffe4b5, #ffd966);
  color: #d2691e;
  font-weight: 600;
}
.form {
  margin: 28rpx 24rpx;
  overflow: hidden;
  border-radius: 20rpx;
  background: #fff;
}
.row {
  display: flex;
  justify-content: space-between;
  padding: 28rpx 24rpx;
  border-bottom: 1rpx solid #f2eee8;
  color: #333;
  font-size: 25rpx;
}
.row:last-child {
  border: 0;
}
.row text:last-child {
  color: #999;
}
.submit {
  width: 100%;
  height: 84rpx;
  border: 0;
  border-radius: 44rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: #fff;
  font-size: 29rpx;
  font-weight: 700;
}
</style>
