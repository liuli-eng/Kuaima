<template>
  <view class="page"
    ><AppNavBar title="保险报案" :show-back="true" /><scroll-view
      scroll-y
      class="content"
      ><view class="tip"
        >如发生工作意外，请先确保人身安全并及时就医，再提交报案信息。</view
      ><view class="form"
        ><input v-model="orderNo" placeholder="请输入关联订单号" /><picker
          :range="types"
          @change="type = types[$event.detail.value]"
          ><view class="row"
            >事故类型 <text>{{ type || "请选择" }} ›</text></view
          ></picker
        ><textarea
          v-model="detail"
          maxlength="300"
          placeholder="请描述事故时间、地点和经过"
        /></view></scroll-view
    ><text
      class="records-link"
      @click="uni.navigateTo({ url: '/pages/worker/insurance-records' })"
      >查看历史报案记录 ›</text
    ><SafeBottomAction
      ><button class="submit" :disabled="submitting" @click="submit">
        {{ submitting ? "提交中…" : "提交报案" }}
      </button></SafeBottomAction
    ></view
  >
</template>
<script setup>
import { ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import SafeBottomAction from "@/components/SafeBottomAction.vue";
import { request } from "@/api/http";
const types = ["意外受伤", "财产损失", "其他"];
const orderNo = ref("");
const type = ref("");
const detail = ref("");
const submitting = ref(false);
async function submit() {
  if (!orderNo.value || !type.value || !detail.value.trim())
    return uni.showToast({ title: "请完善报案信息", icon: "none" });
  submitting.value = true;
  try {
    await request({
      url: "/worker/insurance/reports",
      method: "POST",
      data: { orderNo: orderNo.value, type: type.value, detail: detail.value },
    });
    uni.showToast({ title: "报案已提交", icon: "success" });
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
.tip {
  margin: 24rpx;
  padding: 22rpx;
  background: #fff4e6;
  color: #8b4513;
  border-radius: 18rpx;
  font-size: 23rpx;
}
.records-link {
  display: block;
  margin: 0 24rpx 18rpx;
  color: #1890ff;
  text-align: right;
  font-size: 23rpx;
}
.form {
  margin: 24rpx;
  padding: 26rpx;
  background: #fff;
  border-radius: 20rpx;
}
.form input,
.row {
  padding: 22rpx 0;
  border-bottom: 1rpx solid #eee;
  font-size: 26rpx;
}
.row {
  display: flex;
  justify-content: space-between;
}
.form textarea {
  width: 100%;
  height: 250rpx;
  margin-top: 24rpx;
}
.submit {
  width: 100%;
  height: 84rpx;
  border: 0;
  border-radius: 44rpx;
  background: #ff6b35;
  color: #fff;
  font-size: 29rpx;
}
</style>
