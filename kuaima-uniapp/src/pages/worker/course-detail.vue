<template>
  <view class="page"
    ><AppNavBar title="学习规则" :show-back="true" /><scroll-view
      scroll-y
      class="content"
      ><view class="card"
        ><text class="title">{{
          type === "safety" ? "工作安全须知" : "平台交易规则"
        }}</text
        ><text class="para">1. 报名岗位前请确认工作时间、地点和结算方式；</text
        ><text class="para"
          >2. 录用后请按时到岗，遇到异常情况及时联系雇主和平台；</text
        ><text class="para"
          >3. 完工后由雇主确认，平台根据实际完成情况发起结算；</text
        ><text class="para"
          >4. 禁止发布虚假信息、私下收费或冒用他人身份。</text
        ></view
      ></scroll-view
    ><SafeBottomAction
      ><button class="done" @click="complete">
        我已学习完成
      </button></SafeBottomAction
    ></view
  >
</template>
<script setup>
import AppNavBar from "@/components/AppNavBar.vue";
import SafeBottomAction from "@/components/SafeBottomAction.vue";
const pages = getCurrentPages();
const type = pages[pages.length - 1]?.options?.type || "rule";
function complete() {
  uni.setStorageSync(`course_${type}`, true);
  uni.showToast({ title: "学习完成", icon: "success" });
  setTimeout(() => uni.navigateBack(), 500);
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
.card {
  margin: 24rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
}
.title {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  margin-bottom: 24rpx;
}
.para {
  display: block;
  color: #666;
  font-size: 26rpx;
  line-height: 2;
  margin-bottom: 12rpx;
}
.done {
  width: 100%;
  height: 84rpx;
  line-height: 84rpx;
  border: 0;
  border-radius: 44rpx;
  background: #ff6b35;
  color: #fff;
  font-size: 29rpx;
}
</style>
