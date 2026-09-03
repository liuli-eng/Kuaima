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
      <text class="nav-title">积分购买</text>
      <view class="nav-right">
        <view class="nav-btn">
          <text style="font-size:10px;color:#555;">⋯</text>
        </view>
        <view class="nav-divider"></view>
        <view class="nav-btn">
          <text style="font-size:10px;color:#555;">−</text>
        </view>
        <view class="nav-divider"></view>
        <view class="nav-btn">
          <text style="font-size:10px;color:#555;">●</text>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="content">
      <!-- 积分卡片 -->
      <view class="points-card">
        <text class="points-label">我的积分</text>
        <text class="points-value">{{ points }}</text>
        <text class="points-desc">积分可用于发布订单、提升曝光</text>
      </view>

      <!-- 选择积分包 -->
      <text class="section-title">选择积分包</text>
      <view class="package-grid">
        <view 
          class="package-item" 
          :class="{ hot: pkg.hot }"
          v-for="(pkg, index) in packages" 
          :key="index"
          @click="buy(pkg)"
        >
          <text class="package-points">{{ pkg.points }}</text>
          <text class="package-unit">积分</text>
          <text class="package-price">¥{{ pkg.price }}</text>
          <text class="package-tag">{{ pkg.tag }}</text>
        </view>
      </view>

      <!-- 积分使用规则 -->
      <view class="rules-card">
        <text class="rules-title">
          <text class="ℹ" style="color:#FF6B35;margin-right:4px;"></text>
          积分使用规则
        </text>
        <text class="rules-item">1. 积分可用于发布日结订单、提升订单曝光</text>
        <text class="rules-item">2. 100积分 = ¥1，积分不可兑换现金</text>
        <text class="rules-item">3. 积分有效期12个月，过期自动清零</text>
        <text class="rules-item">4. 购买后立即到账，可在订单中使用</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      points: 0,
      packages: [
        { points: 1000, price: 10, tag: '适用所有订单', hot: false },
        { points: 5000, price: 45, tag: '省5元 更划算', hot: true },
        { points: 10000, price: 80, tag: '省20元', hot: false },
        { points: 20000, price: 150, tag: '省50元', hot: false }
      ]
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    buy(pkg) {
      uni.showModal({
        title: '购买确认',
        content: `购买 ${pkg.points} 积分\n金额：¥${pkg.price}`,
        success: (res) => {
          if (res.confirm) {
            uni.showToast({ title: '购买成功', icon: 'success' })
            this.points += pkg.points
          }
        }
      })
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

.points-card {
  background: linear-gradient(135deg, #FFD700, #FF8C00);
  border-radius: 16px;
  padding: 24px;
  color: white;
  margin-bottom: 20px;
  box-shadow: 0 8px 24px rgba(255, 140, 0, 0.3);
}

.points-label {
  font-size: 13px;
  opacity: 0.9;
  display: block;
}

.points-value {
  font-size: 36px;
  font-weight: 700;
  margin-top: 8px;
  display: block;
}

.points-desc {
  font-size: 12px;
  opacity: 0.85;
  margin-top: 6px;
  display: block;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  display: block;
}

.package-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.package-item {
  background: white;
  border-radius: 12px;
  padding: 16px;
  text-align: center;
  border: 2px solid transparent;
  position: relative;
}

.package-item.hot::after {
  content: '热门';
  position: absolute;
  top: 8px;
  right: 8px;
  background: #FF6B35;
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 8px;
}

.package-points {
  font-size: 24px;
  font-weight: 700;
  color: #FF8C00;
  display: block;
}

.package-unit {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
  display: block;
}

.package-price {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-top: 10px;
  display: block;
}

.package-tag {
  font-size: 11px;
  color: #666;
  margin-top: 4px;
  display: block;
}

.rules-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  margin-top: 20px;
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
