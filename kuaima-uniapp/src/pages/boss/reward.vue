<template>
  <view class="container">
    <!-- 状态栏 -->
    <view class="status-bar">
      <text>19:53</text>
      <view class="status-icons">
        <text class="fa-solid fa-signal"></text>
        <text class="fa-solid fa-wifi"></text>
        <text class="fa-solid fa-battery-full"></text>
      </view>
    </view>

    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="fa-solid fa-arrow-left"></text>
      </view>
      <text class="nav-title">奖励金</text>
      <view class="nav-right">
        <view class="nav-btn">
          <text class="fa-solid fa-ellipsis" style="font-size:20rpx;color:#555;"></text>
        </view>
        <view class="nav-divider"></view>
        <view class="nav-btn">
          <text class="fa-solid fa-circle" style="font-size:20rpx;color:#555;"></text>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="content">
      <!-- 余额卡片 -->
      <view class="balance-card">
        <text class="balance-label">我的奖励金</text>
        <text class="balance-value">¥{{ balance }}</text>
        <view class="balance-actions">
          <view class="balance-btn" @click="recharge">
            <text class="fa-solid fa-plus" style="margin-right:8rpx;"></text>
            <text>充值</text>
          </view>
          <view class="balance-btn" @click="withdraw">
            <text class="fa-solid fa-arrow-down" style="margin-right:8rpx;"></text>
            <text>提现</text>
          </view>
        </view>
      </view>

      <!-- Tab切换 -->
      <view class="tab-switch">
        <text
          class="tab-switch-item"
          :class="{ active: currentTab === 'all' }"
          @click="currentTab = 'all'"
        >全部</text>
        <text
          class="tab-switch-item"
          :class="{ active: currentTab === 'income' }"
          @click="currentTab = 'income'"
        >收入</text>
        <text
          class="tab-switch-item"
          :class="{ active: currentTab === 'expense' }"
          @click="currentTab = 'expense'"
        >支出</text>
      </view>

      <!-- 收支明细 -->
      <view class="income-section">
        <text class="income-title">收支明细</text>
        <view class="income-list">
          <view class="income-item" v-for="(item, index) in filteredRecords" :key="index">
            <view class="income-info">
              <view class="income-icon">
                <text class="fa-solid" :class="item.icon" style="font-size:32rpx;color:#FF6B35;"></text>
              </view>
              <view>
                <text class="income-name">{{ item.name }}</text>
                <text class="income-date">{{ item.date }}</text>
              </view>
            </view>
            <text class="income-amount" :class="item.type === 'plus' ? 'plus' : 'minus'">
              {{ item.type === 'plus' ? '+' : '' }}¥{{ item.amount }}
            </text>
          </view>
        </view>
      </view>

      <!-- 奖励金说明 -->
      <view class="rules-card">
        <text class="rules-title">
          <text class="fa-solid fa-circle-info" style="color:#FF6B35;margin-right:8rpx;"></text>
          奖励金说明
        </text>
        <text class="rules-item">1. 奖励金可用于发布订单、购买积分等</text>
        <text class="rules-item">2. 1元奖励金 = 1元现金，可提现至微信</text>
        <text class="rules-item">3. 提现最低金额10元，T+1到账</text>
        <text class="rules-item">4. 奖励金有效期永久有效</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      balance: '0.00',
      currentTab: 'all',
      records: [
        { name: '邀请好友奖励', date: '2026-08-20 14:30', amount: '10.00', type: 'plus', icon: 'icon-gift' },
        { name: '发布订单消耗', date: '2026-08-19 10:15', amount: '5.00', type: 'minus', icon: 'icon-bullhorn' },
        { name: '新人奖励', date: '2026-08-18 09:00', amount: '20.00', type: 'plus', icon: 'icon-trophy' },
        { name: '分享奖励', date: '2026-08-17 16:45', amount: '5.00', type: 'plus', icon: 'icon-share' }
      ]
    }
  },
  computed: {
    filteredRecords() {
      if (this.currentTab === 'all') return this.records
      if (this.currentTab === 'income') return this.records.filter(r => r.type === 'plus')
      return this.records.filter(r => r.type === 'minus')
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    recharge() {
      uni.showToast({ title: '跳转到充值页面', icon: 'none' })
    },
    withdraw() {
      uni.showToast({ title: '跳转到提现页面', icon: 'none' })
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  width: 100%;
  height: 100vh;
  background: #FFF8E6;
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
  background: transparent;
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
  background: #FFF8E6;
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
  align-items: center;
  background: rgba(255,255,255,0.8);
  backdrop-filter: blur(10px);
  border-radius: 9999px;
  padding: 3px 6px;
  gap: 2px;
}

.nav-btn {
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-divider {
  width: 1px;
  height: 10px;
  background: #ddd;
}

.content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.balance-card {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  border-radius: 16px;
  padding: 24px;
  color: white;
  margin-bottom: 16px;
  box-shadow: 0 8px 24px rgba(255, 107, 53, 0.3);
}

.balance-label {
  font-size: 13px;
  opacity: 0.9;
  display: block;
}

.balance-value {
  font-size: 32px;
  font-weight: 700;
  margin-top: 8px;
  display: block;
}

.balance-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.balance-btn {
  flex: 1;
  padding: 10px;
  background: rgba(255,255,255,0.2);
  border-radius: 8px;
  text-align: center;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tab-switch {
  display: flex;
  background: white;
  border-radius: 10px;
  padding: 4px;
  margin-bottom: 12px;
}

.tab-switch-item {
  flex: 1;
  text-align: center;
  padding: 8px;
  font-size: 14px;
  color: #666;
  border-radius: 8px;
}

.tab-switch-item.active {
  background: #FF6B35;
  color: white;
  font-weight: 600;
}

.income-section {
  background: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}

.income-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  display: block;
}

.income-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.income-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.income-item:last-child {
  border-bottom: none;
}

.income-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.income-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #FFF3E0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.income-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  display: block;
}

.income-date {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
  display: block;
}

.income-amount {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.income-amount.plus {
  color: #FF6B35;
}

.income-amount.minus {
  color: #999;
}

.rules-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
}

.rules-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.rules-item {
  font-size: 12px;
  color: #666;
  line-height: 1.8;
  display: block;
}
</style>
