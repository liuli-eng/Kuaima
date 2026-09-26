<template>
  <view class="page">
    <AppNavBar title="选择企业" :show-back="true" />
    <scroll-view scroll-y class="content">
      <view v-if="loading" class="state">企业加载中...</view>
      <view v-else-if="!contexts.length" class="state">暂无可进入的企业</view>
      <view v-else v-for="item in contexts" :key="item.enterpriseId" class="enterprise-card" :class="{ current: item.enterpriseId === currentId }" @click="switchContext(item)">
        <view class="enterprise-icon">企</view><view class="enterprise-main"><text class="enterprise-name">{{ item.enterpriseName || "未命名企业" }}</text><text class="enterprise-role">{{ roleText(item.memberRole) }}</text></view><text class="enterprise-action">{{ switchingId === item.enterpriseId ? "切换中..." : item.enterpriseId === currentId ? "当前企业" : "进入" }}</text>
      </view>
    </scroll-view>
  </view>
</template>
<script setup>
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import AppNavBar from "@/components/AppNavBar.vue";
import { getEnterpriseContexts, saveEnterpriseContext, switchEnterpriseContext } from "@/api/auth";
const contexts = ref([]); const loading = ref(false); const switchingId = ref(null); const currentId = ref(Number(uni.getStorageSync("enterpriseId")) || null);
onShow(loadContexts);
async function loadContexts() { loading.value = true; try { const result = await getEnterpriseContexts(); const rows = Array.isArray(result) ? result : result?.contexts || result?.enterprises || result?.records || []; contexts.value = Array.isArray(rows) ? rows : []; } catch (error) { uni.showToast({ title: error?.message || "企业列表加载失败", icon: "none" }); } finally { loading.value = false; } }
async function switchContext(item) { if (!item?.enterpriseId || switchingId.value || item.enterpriseId === currentId.value) return; switchingId.value = item.enterpriseId; try { const result = await switchEnterpriseContext(item.enterpriseId); const data = result?.data || result || {}; if (!data.accessToken) throw new Error("企业切换未返回新的登录凭证"); saveEnterpriseContext(data); currentId.value = Number(data.enterpriseId || item.enterpriseId); uni.showToast({ title: "企业切换成功", icon: "success" }); setTimeout(() => uni.reLaunch({ url: "/pages/boss/home" }), 400); } catch (error) { uni.showToast({ title: error?.message || "企业切换失败", icon: "none" }); } finally { switchingId.value = null; } }
function roleText(role) { return { OWNER: "企业负责人", ADMIN: "管理员", OPERATOR: "操作员", FINANCE: "财务" }[role] || role || "企业成员"; }
</script>
<style scoped>
.page{display:flex;flex-direction:column;height:100vh;background:#f5f5f5}.content{flex:1;min-height:0;padding:24rpx 32rpx;box-sizing:border-box}.enterprise-card{display:flex;align-items:center;padding:28rpx 24rpx;margin-bottom:20rpx;border-radius:24rpx;background:#fff}.enterprise-card.current{border:2rpx solid #ff6b35}.enterprise-icon{display:flex;align-items:center;justify-content:center;width:76rpx;height:76rpx;margin-right:20rpx;border-radius:20rpx;color:#fff;background:linear-gradient(135deg,#ff8c5a,#ff6b35);font-size:32rpx;font-weight:700}.enterprise-main{flex:1;min-width:0}.enterprise-name,.enterprise-role{display:block}.enterprise-name{color:#333;font-size:30rpx;font-weight:600}.enterprise-role{margin-top:8rpx;color:#999;font-size:24rpx}.enterprise-action{color:#ff6b35;font-size:24rpx}.state{padding:160rpx 0;color:#999;font-size:26rpx;text-align:center}
</style>
