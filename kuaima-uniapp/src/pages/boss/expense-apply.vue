<template>
  <view class="page">
    <AppNavBar title="费用报销申请" :show-back="true" />
    <scroll-view scroll-y class="scroll-area">
      <view class="form-card"><text class="form-label">报销类型 <text class="required">*</text></text><view class="type-chips"><view v-for="item in expenseTypes" :key="item.value" class="type-chip" :class="{ active: form.type === item.value }" @click="form.type = item.value">{{ item.label }}</view></view></view>
      <view class="form-card"><text class="form-label">关联订单 <text class="required">*</text></text><view v-if="orders.length" class="order-list"><view v-for="order in orders" :key="order.id" class="order-item" :class="{ active: form.orderId === order.id }" @click="form.orderId = order.id"><view><text class="order-name">{{ order.title }}</text><text class="order-meta">{{ order.meta }}</text></view><view class="order-check">{{ form.orderId === order.id ? "✓" : "" }}</view></view></view><view v-else class="order-empty">暂无可关联的待结算订单</view></view>
      <view class="form-card"><text class="form-label">报销金额（元） <text class="required">*</text></text><view class="amount-wrap"><text class="yen">¥</text><input v-model="form.amount" type="digit" maxlength="12" placeholder="0.00" /></view></view>
      <view class="form-card"><text class="form-label">报销事由 <text class="required">*</text></text><textarea v-model="form.reason" class="form-input" maxlength="300" placeholder="请填写报销事由，如：9月11日夜间订单加班，为6名零工提供夜宵补贴" /></view>
      <view class="form-card"><text class="form-label">上传凭证 <text class="optional">（发票/收据/转账记录）</text></text><view class="upload-row"><view v-for="(image, index) in form.attachments" :key="image" class="upload-preview"><image :src="image" mode="aspectFill" /><text class="remove-image" @click.stop="removeImage(index)">×</text></view><view v-if="form.attachments.length < 6" class="upload-box" @click="chooseImage"><text class="upload-symbol">＋</text><text>选择凭证</text></view></view></view>
      <view class="tip-bar"><text class="tip-icon">ⓘ</text><text>报销申请提交后 1-3 个工作日内完成审核，审核通过后金额将退回至账户余额；单笔超过 500 元需平台人工复核。</text></view><view class="bottom-space" />
    </scroll-view>
    <view class="submit-bar"><button class="submit-btn" :disabled="submitting" @click="submitApply">{{ submitting ? "提交中..." : "提交申请" }}</button></view>
  </view>
</template>

<script setup>
import { reactive, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import { listBossPendingSettlements } from "@/api/backend";

const expenseTypes = [{ value: "TRANSPORT", label: "交通费" }, { value: "MEAL", label: "餐费" }, { value: "MATERIAL", label: "材料费" }, { value: "INSURANCE", label: "保险费" }, { value: "OTHER", label: "其他" }];
const form = reactive({ type: "", orderId: null, amount: "", reason: "", attachments: [] });
const orders = ref([]);
const submitting = ref(false);
loadOrders();

async function loadOrders() {
  try {
    const result = await listBossPendingSettlements();
    const rows = Array.isArray(result) ? result : result?.records || result?.content || [];
    orders.value = rows.map((item) => ({ id: item.settlementId || item.id, title: item.orderTitle || item.title || item.position || "待结算订单", meta: [item.date || item.workDate, item.workerName || item.worker?.name, item.days ? `${item.days}天` : ""].filter(Boolean).join(" · ") })).filter((item) => item.id != null);
  } catch (_) { orders.value = []; }
}
function chooseImage() { uni.chooseImage({ count: 6 - form.attachments.length, sourceType: ["album", "camera"], success: (result) => { form.attachments.push(...(result.tempFilePaths || [])); } }); }
function removeImage(index) { form.attachments.splice(index, 1); }
function validate() {
  if (!form.type) return "请选择报销类型";
  if (!form.orderId) return "请选择关联订单";
  const text = String(form.amount).trim();
  if (!/^\d+(\.\d{1,2})?$/.test(text) || Number(text) <= 0) return "请填写正确的报销金额（最多两位小数）";
  if (!String(form.reason).trim()) return "请填写报销事由";
  return "";
}
function submitApply() {
  if (submitting.value) return;
  const message = validate();
  if (message) { uni.showToast({ title: message, icon: "none" }); return; }
  uni.showToast({ title: "报销申请提交接口待后端发布", icon: "none" });
}
</script>

<style scoped>
.page{display:flex;flex-direction:column;height:100vh;overflow:hidden;background:#f5f5f5}.scroll-area{flex:1;min-height:0;box-sizing:border-box}.form-card{margin:24rpx 32rpx 0;padding:32rpx;border-radius:24rpx;background:#fff}.form-label{display:flex;align-items:center;gap:8rpx;margin-bottom:20rpx;color:#333;font-size:28rpx;font-weight:600}.required{color:#ff4d4f;font-size:24rpx}.optional{color:#999;font-size:22rpx;font-weight:400}.type-chips{display:flex;flex-wrap:wrap;gap:16rpx}.type-chip{padding:14rpx 28rpx;border:2rpx solid transparent;border-radius:32rpx;color:#666;background:#f5f5f5;font-size:26rpx}.type-chip.active{border-color:#ff6b35;color:#ff6b35;background:#fff3ed;font-weight:600}.order-list{display:flex;flex-direction:column;gap:16rpx}.order-item{display:flex;align-items:center;justify-content:space-between;padding:20rpx 24rpx;border:2rpx solid #eee;border-radius:20rpx;background:#fafafa}.order-item.active{border-color:#ff6b35;background:#fff3ed}.order-name{display:block;color:#333;font-size:26rpx;font-weight:600}.order-meta{display:block;margin-top:6rpx;color:#999;font-size:22rpx}.order-check{display:flex;align-items:center;justify-content:center;width:36rpx;height:36rpx;border:3rpx solid #ddd;border-radius:50%;color:#fff;font-size:24rpx}.order-item.active .order-check{border-color:#ff6b35;background:#ff6b35}.order-empty{padding:28rpx 0;color:#999;font-size:25rpx;text-align:center}.amount-wrap{display:flex;align-items:center;gap:12rpx;padding:0 24rpx;border:2rpx solid #eee;border-radius:20rpx;background:#fafafa}.yen{color:#ff6b35;font-size:40rpx;font-weight:700}.amount-wrap input{flex:1;height:88rpx;color:#333;font-size:40rpx;font-weight:700}.form-input{width:100%;height:168rpx;box-sizing:border-box;padding:20rpx 24rpx;border:2rpx solid #eee;border-radius:20rpx;color:#333;background:#fafafa;font-size:26rpx;line-height:1.6}.upload-row{display:flex;flex-wrap:wrap;gap:20rpx}.upload-box,.upload-preview{position:relative;display:flex;flex-direction:column;align-items:center;justify-content:center;width:152rpx;height:152rpx;border-radius:20rpx;box-sizing:border-box}.upload-box{border:3rpx dashed #d9d9d9;color:#999;background:#fafafa;font-size:22rpx}.upload-symbol{font-size:50rpx;line-height:1;color:#999}.upload-preview{overflow:hidden}.upload-preview image{width:100%;height:100%}.remove-image{position:absolute;right:6rpx;top:2rpx;width:32rpx;height:32rpx;border-radius:50%;color:#fff;background:rgba(0,0,0,.55);font-size:28rpx;line-height:29rpx;text-align:center}.tip-bar{display:flex;gap:12rpx;margin:24rpx 32rpx 0;padding:20rpx 24rpx;border:2rpx solid #ffe58f;border-radius:20rpx;color:#8c6a00;background:#fffbe6;font-size:23rpx;line-height:1.6}.tip-icon{flex-shrink:0}.bottom-space{height:32rpx}.submit-bar{flex-shrink:0;padding:24rpx 32rpx calc(24rpx + env(safe-area-inset-bottom));border-top:1rpx solid #f0f0f0;background:#fff}.submit-btn{width:100%;height:92rpx;border:0;border-radius:46rpx;color:#fff;background:linear-gradient(135deg,#ff6b35,#ff8c5a);box-shadow:0 8rpx 24rpx rgba(255,107,53,.3);font-size:30rpx;font-weight:700}.submit-btn[disabled]{opacity:.6}
</style>
