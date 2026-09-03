<template>
  <view class="page"
    ><AppNavBar title="违规举报" :show-back="true" /><scroll-view
      scroll-y
      class="content"
      ><view class="form"
        ><picker :range="types" @change="type = types[$event.detail.value]"
          ><view class="row"
            >举报类型 <text>{{ type || "请选择" }} ›</text></view
          ></picker
        ><input v-model="target" placeholder="请输入岗位或雇主名称" /><textarea
          v-model="detail"
          maxlength="300"
          placeholder="请描述问题并提供必要线索"
        /></view></scroll-view
    ><SafeBottomAction
      ><button class="submit" :disabled="submitting" @click="submit">
        {{ submitting ? "提交中…" : "提交举报" }}
      </button></SafeBottomAction
    ></view
  >
</template>
<script setup>
import { ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import SafeBottomAction from "@/components/SafeBottomAction.vue";
import { request } from "@/api/http";
const types = ["虚假岗位", "私下收费", "飞单/绕平台", "其他"];
const type = ref("");
const target = ref("");
const detail = ref("");
const submitting = ref(false);
async function submit() {
  if (!type.value || !detail.value.trim())
    return uni.showToast({ title: "请完善举报信息", icon: "none" });
  submitting.value = true;
  try {
    await request({
      url: "/worker/reports",
      method: "POST",
      data: { type: type.value, target: target.value, detail: detail.value },
    });
    uni.showToast({ title: "举报已提交", icon: "success" });
    setTimeout(() => uni.navigateBack(), 500);
  } catch (e) {
    uni.showToast({ title: e.message || "提交失败", icon: "none" });
  } finally {
    submitting.value = false;
  }
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
.form {
  margin: 24rpx;
  padding: 26rpx;
  background: #fff;
  border-radius: 20rpx;
}
.row {
  display: flex;
  justify-content: space-between;
  padding: 22rpx 0;
  border-bottom: 1rpx solid #eee;
  font-size: 27rpx;
}
.row text {
  color: #999;
}
.form input {
  padding: 25rpx 0;
  border-bottom: 1rpx solid #eee;
  font-size: 26rpx;
}
.form textarea {
  width: 100%;
  height: 260rpx;
  margin-top: 24rpx;
  font-size: 26rpx;
}
.submit {
  width: 100%;
  height: 84rpx;
  line-height: 84rpx;
  border: 0;
  border-radius: 44rpx;
  background: #ff6b35;
  color: #fff;
  font-size: 29rpx;
}
.submit[disabled] {
  background: #bbb;
}
</style>
