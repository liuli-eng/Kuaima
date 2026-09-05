<template>
  <view class="container">
    <!-- 状态栏 -->
    <view class="status-bar">
      <text>19:53</text>
      <view class="status-icons">
        <text>📶</text>
        <text>📡</text>
        <text>🔋</text>
      </view>
    </view>

    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text>←</text>
      </view>
      <text class="nav-title">结算消息</text>
      <view class="nav-right">
        <text>…</text>
      </view>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <!-- 结算卡片 -->
      <view class="settle-card">
        <view class="settle-header">
          <text class="settle-title">2026年8月结算</text>
          <text class="settle-status">已结算</text>
        </view>
        <text class="settle-amount">¥12,560.00</text>
        <text class="settle-info">结算时间：2026-08-31 18:00</text>
        <text class="settle-info">结算周期：2026-08-01 至 2026-08-31</text>
      </view>

      <!-- 结算明细 -->
      <view class="settle-detail">
        <text class="detail-title">结算明细</text>
        <view class="detail-row" v-for="(item, index) in details" :key="index">
          <text class="label">{{ item.label }}</text>
          <text class="value">{{ item.calc }} = <text class="money">{{ item.amount }}</text></text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      details: [
        { label: '电商分拣打包工', calc: '5人天 × ¥180', amount: '¥900' },
        { label: '餐饮服务员', calc: '3人天 × ¥150', amount: '¥450' },
        { label: '快递搬运装卸工', calc: '4人天 × ¥200', amount: '¥800' },
        { label: '冷库分拣员', calc: '2人天 × ¥220', amount: '¥440' },
        { label: '装配工', calc: '8人天 × ¥250', amount: '¥2,000' },
        { label: '其他岗位', calc: '', amount: '¥7,970' }
      ]
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
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

.status-bar {
  height: 47px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 28px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  background: #fff;
}

.status-icons {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-bar {
  height: 50px;
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
}

.nav-title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
}

.nav-right {
  display: flex;
  gap: 14px;
  color: #333;
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
}

.settle-card {
  background: linear-gradient(135deg, #722ED1, #9254DE);
  margin: 12px 16px;
  border-radius: 12px;
  padding: 20px;
  color: #fff;
}

.settle-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.settle-title {
  font-size: 16px;
  font-weight: 600;
}

.settle-status {
  font-size: 12px;
  background: rgba(255,255,255,0.2);
  padding: 4px 10px;
  border-radius: 10px;
}

.settle-amount {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
  display: block;
}

.settle-info {
  font-size: 13px;
  opacity: 0.9;
  line-height: 1.8;
  display: block;
}

.settle-detail {
  background: #fff;
  margin: 12px 16px;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.detail-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  display: block;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #666;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-row .label {
  color: #999;
}

.detail-row .value {
  color: #333;
}

.detail-row .value .money {
  color: #FF6B35;
  font-weight: 600;
}
</style>
