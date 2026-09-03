<template>
  <view class="page"
    ><AppNavBar title="个人基本信息" :show-back="true" /><scroll-view
      scroll-y
      class="content"
      ><text class="section-title">基本信息</text
      ><view class="form"
        ><view class="row"
          ><text>* 真实姓名</text
          ><input v-model="form.name" placeholder="请填写" /></view
        ><view class="row"
          ><text>* 性别</text
          ><picker
            :range="genders"
            @change="form.gender = genders[$event.detail.value]"
            ><text>{{ form.gender || "请选择" }} ›</text></picker
          ></view
        ><view class="row"
          ><text>* 年龄</text
          ><input v-model="form.age" type="number" placeholder="请填写" /></view
        ><view class="row"
          ><text>* 身份证号</text
          ><input v-model="form.idCard" placeholder="请填写" /></view></view
      ><text class="section-title">联系方式</text
      ><view class="form"
        ><view class="row"><text>* 手机号</text><text>138****8888</text></view
        ><view class="row"
          ><text>微信号</text
          ><input v-model="form.wechat" placeholder="请填写" /></view></view
      ><text class="section-title">地址信息</text
      ><view class="form"
        ><view class="row"
          ><text>* 常驻城市</text
          ><input v-model="form.city" placeholder="请选择" /></view
        ><view class="row"
          ><text>详细地址</text
          ><input
            v-model="form.address"
            placeholder="请填写" /></view></view></scroll-view
    ><SafeBottomAction
      ><button class="submit" @click="save">保存信息</button></SafeBottomAction
    ></view
  >
</template>
<script setup>
import { reactive } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import SafeBottomAction from "@/components/SafeBottomAction.vue";
const genders = ["男", "女"];
const form = reactive({
  name: "",
  gender: "",
  age: "",
  idCard: "",
  wechat: "",
  city: "",
  address: "",
});
function save() {
  if (!form.name || !form.gender || !form.age || !form.idCard || !form.city)
    return uni.showToast({ title: "请完善必填信息", icon: "none" });
  uni.setStorageSync("workerIntentPersonal", form);
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
.section-title {
  display: block;
  padding: 26rpx 28rpx 12rpx;
  color: #999;
  font-size: 23rpx;
}
.form {
  margin: 0 24rpx;
  overflow: hidden;
  border-radius: 20rpx;
  background: #fff;
}
.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 92rpx;
  padding: 0 24rpx;
  border-bottom: 1rpx solid #f2eee8;
  color: #333;
  font-size: 25rpx;
}
.row:last-child {
  border: 0;
}
.row input {
  width: 45%;
  text-align: right;
  font-size: 24rpx;
}
.row > text:last-child,
.row picker text {
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
