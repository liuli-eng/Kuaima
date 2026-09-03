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
      <text class="nav-title">报酬支付明细</text>
      <view style="width:32px;"></view>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <!-- 汇总卡片 -->
      <view class="summary-card">
        <text class="summary-title">累计支付报酬</text>
        <text class="summary-amount">{{ totalAmount }}</text>
        <text class="summary-period">2026年5月23日 ~ 2026年8月21日</text>
      </view>

      <!-- 记录列表 -->
      <view class="record-card" v-for="(record, index) in records" :key="index">
        <view class="record-head">
          <text class="record-title">{{ record.title }}</text>
          <text class="record-status" :class="record.statusClass">{{ record.status }}</text>
        </view>
        <text class="record-desc">{{ record.desc }}</text>
        <text class="record-amount">¥{{ record.amount }}</text>
        <text class="record-time">{{ record.time }}</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      totalAmount: '12,560.00',
      records: [
        { title: '8月工资结算', status: '已支付', statusClass: 'status-success', desc: '结算周期：2026-08-01 至 2026-08-21', amount: '3,600.00', time: '2026-08-21 18:00 已到账' },
        { title: '7月工资结算', status: '已支付', statusClass: 'status-success', desc: '结算周期：2026-07-01 至 2026-07-31', amount: '8,960.00', time: '2026-07-31 18:00 已到账' },
        { title: '6月工资结算', status: '已支付', statusClass: 'status-success', desc: '结算周期：2026-06-01 至 2026-06-30', amount: '0.00', time: '无订单' }
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

.scroll-area {
  flex: 1;
  overflow-y: auto;
}

.summary-card {
  background: linear-gradient(135deg, #722ED1, #9254DE);
  margin: 12px 16px;
  border-radius: 12px;
  padding: 16px;
  color: #fff;
}

.summary-title {
  font-size: 13px;
  opacity: 0.9;
  display: block;
}

.summary-amount {
  font-size: 28px;
  font-weight: 700;
  margin-top: 6px;
  display: block;
}

.summary-period {
  font-size: 12px;
  opacity: 0.8;
  margin-top: 4px;
  display: block;
}

.record-card {
  background: #fff;
  margin: 8px 16px;
  border-radius: 12px;
  padding: 14px;
}

.record-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.record-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.record-status {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
}

.status-success {
  color: #52C41A;
  background: #F6FFED;
}

.status-pending {
  color: #FA8C16;
  background: #FFF8E6;
}

.record-desc {
  font-size: 12px;
  color: #999;
  display: block;
}

.record-amount {
  font-size: 18px;
  font-weight: 700;
  color: #333;
  text-align: right;
  display: block;
}

.record-time {
  font-size: 11px;
  color: #bbb;
  margin-top: 4px;
  text-align: right;
  display: block;
}
</style>
