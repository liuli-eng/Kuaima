<template>
  <view class="page"
    ><AppNavBar title="通知与安全" :show-back="true" /><view class="card"
      ><view v-for="item in settings" :key="item.key" class="row"
        ><view
          ><text>{{ item.label }}</text
          ><text class="desc">{{ item.desc }}</text></view
        ><switch
          :checked="item.enabled"
          color="#ff6b35"
          @change="
            item.enabled = $event.detail.value;
            save();
          " /></view></view
    ><view class="card"
      ><view class="link"
        ><text>绑定手机号</text><text>138****5678 ›</text></view
      ><view class="link"><text>登录设备管理</text><text>›</text></view></view
    ></view
  >
</template>
<script setup>
import { onMounted, reactive } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import {
  getNotificationSettings,
  updateNotificationSettings,
} from "@/api/backend";
const userId = uni.getStorageSync("userId") || "2001";
const settings = reactive([
  {
    key: "order",
    label: "订单通知",
    desc: "报名、录用、到岗和完工提醒",
    enabled: true,
  },
  {
    key: "wallet",
    label: "钱包通知",
    desc: "结算到账和提现结果提醒",
    enabled: true,
  },
  {
    key: "marketing",
    label: "活动通知",
    desc: "优惠券和平台活动消息",
    enabled: false,
  },
]);
async function save() {
  uni.setStorageSync("workerNotificationSettings", settings);
  try {
    await updateNotificationSettings(userId, {
      orderNotif: settings[0].enabled,
      activityNotif: settings[2].enabled,
      systemNotif: settings[0].enabled,
      sound: settings[1].enabled,
      vibrate: settings[1].enabled,
    });
  } catch (error) {
    uni.showToast({ title: error.message || "通知设置保存失败", icon: "none" });
    return;
  }
  uni.showToast({ title: "设置已保存", icon: "none" });
}
onMounted(async () => {
  try {
    const data = await getNotificationSettings(userId);
    if (data && typeof data === "object") {
      settings[0].enabled = data.orderNotif !== false;
      settings[1].enabled = data.sound !== false;
      settings[2].enabled = data.activityNotif === true;
    }
  } catch (_) {}
});
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: #f8f4ed;
}
.card {
  margin: 24rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 5rpx 16rpx rgba(88, 64, 32, 0.05);
}
.row,
.link {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 24rpx;
  border-bottom: 1rpx solid #f1f1f1;
  font-size: 26rpx;
}
.desc {
  display: block;
  color: #999;
  font-size: 21rpx;
  margin-top: 7rpx;
}
.link text:last-child {
  color: #999;
}
</style>
