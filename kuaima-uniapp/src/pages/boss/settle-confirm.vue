<template>
  <view class="container">
    <!-- 导航栏 -->
    <view class="nav-bar" :style="{ paddingTop: `${statusBarHeight}px`, height: `${50 + statusBarHeight}px` }">
      <view class="nav-back" @click="goBack">
        <text>‹</text>
      </view>
      <text class="nav-title">确认付款</text>
      <view class="nav-right"></view>
    </view>

    <scroll-view scroll-y class="content-area" style="flex:1;overflow-y:auto;padding-bottom:80px;">
      <!-- 付款金额 -->
      <view class="amount-card">
        <text class="amount-label">付款金额</text>
        <text class="amount-value">¥{{ finalAmount.toFixed(2) }}</text>
        <text class="amount-desc">共{{ count }}笔订单 · 待付款</text>
      </view>

      <!-- 付款订单 -->
      <view class="section-card">
        <text class="section-title">付款订单</text>
        <view class="order-summary">
          <text class="summary-tag" v-for="i in Math.min(count, 5)" :key="i">订单{{ i }}</text>
          <text class="summary-tag" v-if="count > 5">等{{ count }}笔</text>
        </view>
      </view>

      <!-- 费用明细 -->
      <view class="section-card">
        <text class="section-title">费用明细</text>
        <view style="display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px dashed #f0f0f0;">
          <text style="color:#666;font-size:14px;">订单总金额</text>
          <text style="font-weight:500;color:#333;">¥{{ amount.toFixed(2) }}</text>
        </view>
        <view style="display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px dashed #f0f0f0;">
          <text style="color:#666;font-size:14px;">平台服务费 (10%)</text>
          <text style="font-weight:500;color:#FF6B35;">¥{{ serviceFee.toFixed(2) }}</text>
        </view>
        <view style="display:flex;justify-content:space-between;padding:8px 0;margin-top:4px;">
          <text style="font-weight:600;color:#333;font-size:15px;">实际支付金额</text>
          <text style="font-weight:700;color:#FF6B35;font-size:18px;">¥{{ finalAmount.toFixed(2) }}</text>
        </view>
      </view>

      <!-- 服务费收款账户 -->
      <view class="section-card">
        <text class="section-title">服务费收款账户</text>
        <view style="display:flex;align-items:center;gap:12px;margin-bottom:12px;">
          <view style="width:40px;height:40px;background:linear-gradient(135deg,#FF6B35,#FF8C5A);border-radius:10px;display:flex;align-items:center;justify-content:center;">
            <text style="color:white;font-size:18px;">🏛</text>
          </view>
          <view>
            <text style="font-weight:600;color:#333;font-size:14px;">快马日结平台</text>
            <text style="font-size:12px;color:#999;display:block;">平台服务费将汇入以下账户</text>
          </view>
        </view>
        <view style="background:#FAFAFA;border-radius:10px;padding:12px;">
          <view style="display:flex;justify-content:space-between;padding:6px 0;font-size:13px;">
            <text style="color:#999;">收款账户</text>
            <text style="color:#333;">快马日结科技有限公司</text>
          </view>
          <view style="display:flex;justify-content:space-between;padding:6px 0;font-size:13px;">
            <text style="color:#999;">收款银行</text>
            <text style="color:#333;">工商银行上海分行</text>
          </view>
          <view style="display:flex;justify-content:space-between;padding:6px 0;font-size:13px;">
            <text style="color:#999;">银行账号</text>
            <text style="color:#333;font-family:monospace;">6222 **** **** 8888</text>
          </view>
        </view>
      </view>

      <!-- 付款账号 -->
      <view class="section-card">
        <text class="section-title">付款账号</text>
        <view class="account-type-list">
          <view 
            class="account-type-item" 
            :class="{ active: selectedAccount === type.value }"
            v-for="type in accountTypes" 
            :key="type.value"
            @click="selectedAccount = type.value"
          >
            <view class="account-type-icon" :class="type.value">
              <text class="account-icon-text">{{ type.value === 'wechat' ? '微' : type.value === 'alipay' ? '支' : '¥' }}</text>
            </view>
            <view class="account-type-info">
              <text class="account-type-name">{{ type.name }}</text>
              <text class="account-type-desc">{{ type.desc }}</text>
            </view>
            <view class="account-type-check"></view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部按钮 -->
    <view style="background:#fff;padding:12px 16px;border-top:1px solid #f0f0f0;">
      <button class="confirm-btn" :disabled="!selectedAccount" @click="submitSettle">确认付款{{ selectedAccount ? ` ¥${finalAmount.toFixed(2)}` : '' }}</button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      statusBarHeight: 0,
      amount: 0,
      count: 0,
      serviceFee: 0,
      finalAmount: 0,
      selectedAccount: null,
      accountTypes: [
        { value: 'wechat', name: '微信支付账号', desc: '使用微信支付付款', icon: 'icon-wechat' },
        { value: 'alipay', name: '支付宝账号', desc: '使用支付宝付款', icon: 'icon-alipay' },
        { value: 'bank', name: '银行账号', desc: '使用银行卡付款', icon: 'icon-university' }
      ]
    }
  },
  onLoad(options = {}) {
    try { const info = typeof uni.getWindowInfo === 'function' ? uni.getWindowInfo() : uni.getSystemInfoSync(); this.statusBarHeight = Number(info.statusBarHeight || 0) } catch (_) {}
    this.amount = Number(options.amount || 0)
    this.count = Number(options.count || 0)
    this.serviceFee = this.amount * 0.1
    this.finalAmount = this.amount + this.serviceFee
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    submitSettle() {
      if (!this.selectedAccount) {
        uni.showToast({ title: '请选择付款账号', icon: 'none' })
        return
      }
      uni.showModal({
        title: '付款成功',
        content: `付款金额 ¥${this.finalAmount.toFixed(2)} 已成功支付`,
        showCancel: false,
        success: () => { uni.navigateBack() }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  width: 100%;
  height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.nav-bar {
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #fff;
}

.nav-back {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #333;
}

.nav-title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
}

.amount-card {
  background: linear-gradient(135deg, #FFE4B5 0%, #FFD700 100%);
  margin: 12px 16px;
  border-radius: 16px;
  padding: 20px;
  text-align: center;
}

.content-area {
  min-height: 0;
  height: 0;
  box-sizing: border-box;
}

.amount-label {
  font-size: 13px;
  color: #666;
  display: block;
}

.amount-value {
  font-size: 36px;
  font-weight: 700;
  margin-top: 8px;
  color: #333;
  display: block;
}

.amount-desc {
  font-size: 12px;
  color: #888;
  margin-top: 6px;
  display: block;
}

.section-card {
  background: white;
  margin: 12px 16px;
  border-radius: 16px;
  padding: 16px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 14px;
  display: block;
}

.order-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.summary-tag {
  background: #FFF8F5;
  color: #FF6B35;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 10px;
}

.account-type-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.account-type-item {
  display: flex;
  align-items: center;
  padding: 14px;
  border: 1.5px solid #eee;
  border-radius: 12px;
}

.account-type-item.active {
  border-color: #FF6B35;
  background: #FFF8F5;
}

.account-type-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
}

.account-type-icon.wechat { background: #E8F8E8; color: #09BB07; }
.account-type-icon.alipay { background: #E6F0FF; color: #1677FF; }
.account-type-icon.bank { background: #FFF5E6; color: #FF8C00; }

.account-type-info {
  flex: 1;
}

.account-type-name {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  display: block;
}

.account-type-desc {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
  display: block;
}

.account-type-check {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid #ddd;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.account-type-item.active .account-type-check {
  border-color: #FF6B35;
  background: #FF6B35;
}

.account-type-item.active .account-type-check::after {
  content: '';
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
}

.confirm-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  color: white;
  border: none;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  line-height: 48px;
}

.confirm-btn::after { border: none; }

.confirm-btn:disabled {
  opacity: 0.5;
}
</style>
