<template>
  <view class="page"
    ><AppNavBar title="提交申诉" :show-back="true" /><scroll-view
      scroll-y
      class="content"
      ><view class="tips"
        ><text class="tips-title">提交前请注意</text
        ><text>请如实填写申诉原因，并提供清晰、有效的证明材料。</text
        ><text>平台将在提交后 1-3 个工作日内审核。</text></view
      ><view class="form"
        ><text class="label">选择申诉订单 *</text
        ><view class="order-card"
          ><view
            ><text class="order-title">餐饮服务员</text
            ><text class="order-no">订单号：KM20260830001</text></view
          ><text class="selected">✓</text></view
        ><text class="label">申诉原因 *</text
        ><picker
          :range="reasons"
          @change="reason = reasons[$event.detail.value]"
          ><view class="row"
            >申诉原因 <text>{{ reason || "请选择" }} ›</text></view
          ></picker
        ><text class="label detail-label">详细说明 *</text
        ><textarea
          v-model="detail"
          maxlength="300"
          placeholder="请详细描述申诉情况和相关证据" /><text class="count"
          >{{ detail.length }}/300</text
        ><text class="label"
          >上传证据 <text class="optional">（选填，最多3张）</text></text
        ><view class="uploads"
          ><view
            v-for="(image, index) in images"
            :key="image"
            class="upload-image"
            ><image :src="image" mode="aspectFill" /><text
              @click="images.splice(index, 1)"
              >×</text
            ></view
          ><view
            v-if="images.length < 3"
            class="upload-add"
            @click="chooseImage"
            >＋<text>上传图片</text></view
          ></view
        ><text class="label phone-label"
          >联系电话 <text class="optional">（选填）</text></text
        ><input
          v-model="phone"
          class="phone"
          type="number"
          placeholder="方便客服联系您" /></view></scroll-view
    ><SafeBottomAction
      ><button class="submit" :disabled="submitting" @click="submit">
        {{ submitting ? "提交中…" : "提交申诉" }}
      </button></SafeBottomAction
    ></view
  >
</template>
<script setup>
import { ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import SafeBottomAction from "@/components/SafeBottomAction.vue";
import { request } from "@/api/http";
const reasons = ["订单误取消", "迟到扣分", "差评申诉", "其他"];
const reason = ref("");
const detail = ref("");
const phone = ref("");
const images = ref([]);
const submitting = ref(false);
async function submit() {
  if (!reason.value || !detail.value.trim())
    return uni.showToast({ title: "请完善申诉信息", icon: "none" });
  submitting.value = true;
  try {
    await request({
      url: "/worker/appeals",
      method: "POST",
      data: {
        reason: reason.value,
        detail: detail.value,
        phone: phone.value,
        images: images.value,
      },
    });
    uni.showToast({ title: "申诉已提交", icon: "success" });
    setTimeout(() => uni.navigateBack(), 500);
  } catch (e) {
    uni.showToast({ title: e.message || "提交失败", icon: "none" });
  } finally {
    submitting.value = false;
  }
}
function chooseImage() {
  uni.chooseImage({
    count: 3 - images.value.length,
    success: ({ tempFilePaths }) => {
      images.value.push(...tempFilePaths);
    },
  });
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #fff8e6;
}
.content {
  height: calc(100vh - 176rpx);
}
.tips {
  margin: 24rpx;
  padding: 22rpx;
  border-radius: 18rpx;
  background: #fff4e6;
  color: #8b4513;
  font-size: 22rpx;
  line-height: 1.7;
}
.tips text {
  display: block;
}
.tips-title {
  margin-bottom: 6rpx;
  font-size: 25rpx;
  font-weight: 700;
}
.form {
  margin: 24rpx;
  padding: 26rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 5rpx 16rpx rgba(88, 64, 32, 0.05);
}
.row {
  display: flex;
  justify-content: space-between;
  padding: 22rpx 0;
  border-bottom: 1rpx solid #eee;
  color: #333;
  font-size: 27rpx;
}
.row text {
  color: #999;
}
.label {
  display: block;
  margin-bottom: 14rpx;
  color: #333;
  font-size: 25rpx;
  font-weight: 600;
}
.detail-label,
.phone-label {
  margin-top: 26rpx;
}
.optional {
  color: #999;
  font-size: 20rpx;
  font-weight: 400;
}
.order-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
  padding: 20rpx;
  border: 1rpx solid #ffb38f;
  border-radius: 14rpx;
  background: #fff9f5;
}
.order-title,
.order-no {
  display: block;
}
.order-title {
  color: #333;
  font-size: 25rpx;
  font-weight: 600;
}
.order-no {
  margin-top: 6rpx;
  color: #999;
  font-size: 20rpx;
}
.selected {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38rpx;
  height: 38rpx;
  border-radius: 50%;
  background: #ff6b35;
  color: #fff;
}
.form textarea {
  width: 100%;
  height: 260rpx;
  margin-top: 24rpx;
  font-size: 26rpx;
}
.count {
  display: block;
  text-align: right;
  color: #aaa;
  font-size: 20rpx;
}
.uploads {
  display: flex;
  gap: 14rpx;
  margin-bottom: 4rpx;
}
.upload-image,
.upload-add {
  position: relative;
  width: 132rpx;
  height: 132rpx;
  border-radius: 12rpx;
  overflow: hidden;
}
.upload-image image {
  width: 100%;
  height: 100%;
}
.upload-image > text {
  position: absolute;
  top: 4rpx;
  right: 6rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34rpx;
  height: 34rpx;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
}
.upload-add {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 1rpx dashed #d8d0c5;
  color: #aaa;
  font-size: 36rpx;
}
.upload-add text {
  margin-top: 4rpx;
  font-size: 19rpx;
}
.phone {
  height: 82rpx;
  padding: 0 18rpx;
  border: 1rpx solid #e8e3dc;
  border-radius: 12rpx;
  background: #fafafa;
  font-size: 24rpx;
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
