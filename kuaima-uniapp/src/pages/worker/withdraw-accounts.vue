<template>
  <view class="page">
    <AppNavBar title="提现账号" :show-back="true" />
    <view class="card" v-for="item in accounts" :key="item.id">
      <view
        ><text class="title">{{ item.typeName }}</text
        ><text class="account">{{ item.account }}</text></view
      >
      <text class="default">{{ item.default ? "默认" : "" }}</text>
    </view>
    <view class="form">
      <picker :range="types" @change="type = types[$event.detail.value]"
        ><view class="row"
          >账号类型 <text>{{ type }} ›</text></view
        ></picker
      >
      <input v-model="account" placeholder="请输入提现账号" />
      <input v-model="name" placeholder="请输入真实姓名" />
      <button :disabled="saving" @click="add">
        {{ saving ? "保存中…" : "添加提现账号" }}
      </button>
    </view>
  </view>
</template>
<script setup>
import { ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import { request } from "@/api/http";
const accounts = ref([
  { id: 1, typeName: "微信零钱", account: "138****5678", default: true },
]);
const types = ["微信零钱", "支付宝", "银行卡"];
const type = ref(types[0]);
const account = ref("");
const name = ref("");
const saving = ref(false);
async function add() {
  if (!account.value.trim() || !name.value.trim())
    return uni.showToast({ title: "请完善账号信息", icon: "none" });
  saving.value = true;
  try {
    await request({
      url: "/worker/wallet/accounts",
      method: "POST",
      data: { type: type.value, account: account.value, name: name.value },
    });
    accounts.value.push({
      id: Date.now(),
      typeName: type.value,
      account: account.value,
      default: false,
    });
    account.value = "";
    name.value = "";
    uni.showToast({ title: "添加成功", icon: "success" });
  } catch (error) {
    uni.showToast({ title: error.message || "添加失败", icon: "none" });
  } finally {
    saving.value = false;
  }
}
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}
.card,
.form {
  margin: 22rpx;
  padding: 28rpx;
  border-radius: 20rpx;
  background: #fff;
}
.card {
  display: flex;
  justify-content: space-between;
}
.title {
  display: block;
  font-size: 27rpx;
  font-weight: 700;
}
.account {
  display: block;
  margin-top: 8rpx;
  color: #888;
  font-size: 22rpx;
}
.default {
  color: #52c41a;
  font-size: 22rpx;
}
.row,
.form input {
  padding: 22rpx 0;
  border-bottom: 1rpx solid #eee;
  font-size: 25rpx;
}
.row {
  display: flex;
  justify-content: space-between;
}
.form button {
  margin-top: 28rpx;
  background: #ff6b35;
  color: #fff;
  border: 0;
  border-radius: 40rpx;
  font-size: 27rpx;
}
</style>
