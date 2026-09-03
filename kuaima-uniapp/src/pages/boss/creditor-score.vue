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
      <text class="nav-title">快马雇主星级</text>
      <view class="nav-right">
        <text>…</text>
      </view>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <!-- 星级横幅 -->
      <view class="level-banner">
        <view class="level-icon">
          <text style="color:white;font-size:24px;">⭐</text>
        </view>
        <text class="level-title">一星雇主</text>
        <text class="level-desc">多完单·不飞单·结算快·好评多</text>
        <view class="benefit-btn" @click="showBenefits">
          <text style="margin-right:4px;">🎁</text>
          <text>查看星级权益</text>
        </view>
      </view>

      <!-- 诚意分卡片 -->
      <view class="score-card">
        <view class="score-header">
          <view>
            <view>
              <text class="score-value">30</text>
              <text class="score-unit">诚意分</text>
            </view>
            <text class="score-period">(5月23日~8月21日总计)</text>
          </view>
          <text class="score-detail-link" @click="showScoreDetail">查看诚意分明细 ›</text>
        </view>
        <view class="score-scale">
          <text>0</text>
          <text>40</text>
          <text>100</text>
          <text>200</text>
          <text>600</text>
        </view>
        <view class="score-bar">
          <view class="score-bar-fill" style="width: 7.5%;"></view>
        </view>
        <view class="star-levels">
          <view class="star-item">
            <text class="star-label">零星</text>
            <text class="star-value">禁止招工</text>
          </view>
          <view class="star-item active">
            <text class="star-label">一星</text>
            <text class="star-value">无权益</text>
          </view>
          <view class="star-item">
            <text class="star-label">二星</text>
            <text class="star-value">无权益</text>
          </view>
          <view class="star-item">
            <text class="star-label">三星</text>
            <text class="star-value">1项权益</text>
          </view>
          <view class="star-item">
            <text class="star-label">四星</text>
            <text class="star-value">2项权益</text>
          </view>
        </view>
      </view>

      <!-- 如何提升诚意分 -->
      <view class="rule-card">
        <view class="rule-header">
          <view class="rule-title">
            <text style="color:#FFD700;margin-right:4px;">💡</text>
            <text>如何提升诚意分？</text>
          </view>
          <text class="rule-link" @click="showRules">查看规则 ›</text>
        </view>
      </view>

      <!-- 到达完单率 -->
      <view class="rule-card">
        <view class="rule-header">
          <text class="rule-title">到达完单率 -</text>
          <text class="rule-link" @click="showHowToImprove">ⓘ 如何提升</text>
        </view>
        <text class="rule-desc">到达完单率=完单结算数/到达现场数</text>
        <text class="rule-desc">近期完单数不足4单，暂不统计到达完单率，请多发单并在平台完成结算即可加分</text>
        <view class="metric-bar">
          <view class="metric-header">
            <text>100%</text>
            <text>90%</text>
            <text>70%</text>
            <text>0%</text>
          </view>
          <view class="metric-ranges">
            <view class="metric-range excellent">优秀</view>
            <view class="metric-range normal">普通</view>
            <view class="metric-range normal">普通</view>
            <view class="metric-range poor">差</view>
          </view>
          <view class="metric-labels">
            <text>到达完单 +6分</text>
            <text>到达完单 +3分</text>
            <text>到达未完结 -10分</text>
          </view>
        </view>
      </view>

      <!-- 请多发单并及时结算 -->
      <view class="rule-card">
        <view class="rule-header">
          <text class="rule-title">请多发单并及时结算</text>
          <text class="rule-link" @click="showHowToImprove">ⓘ 如何提升</text>
        </view>
        <text class="rule-desc">近期无订单结算，请多发单并在零工完工后1小时内结算即可加分</text>
        <view class="metric-bar">
          <view class="metric-header">
            <text>完单</text>
            <text>1小时</text>
            <text>24小时</text>
          </view>
          <view class="metric-ranges">
            <view class="metric-range excellent">优秀</view>
            <view class="metric-range excellent">优秀</view>
            <view class="metric-range poor">差</view>
          </view>
          <view class="metric-labels">
            <text>1小时内结算 +2分</text>
            <text>24小时内结算 +0分</text>
            <text>超24小时结算 -5分</text>
          </view>
        </view>
      </view>

      <!-- 请多发单并让零工好评 -->
      <view class="rule-card">
        <view class="rule-header">
          <text class="rule-title">请多发单并让零工好评</text>
          <text class="rule-link" @click="showHowToImprove">ⓘ 如何提升</text>
        </view>
        <text class="rule-desc">近期无评价订单，请多发单并让零工主动好评，将每单加5分</text>
        <view class="metric-bar">
          <view class="metric-ranges">
            <view class="metric-range excellent">优秀</view>
            <view class="metric-range normal">普通</view>
            <view class="metric-range poor">差</view>
          </view>
          <view class="metric-labels">
            <text>零工主动好评 +5分</text>
            <text>未收到评价 +0分</text>
            <text>零工差评 -10分</text>
          </view>
        </view>
      </view>

      <!-- 请避免取消订单 -->
      <view class="rule-card">
        <view class="rule-header">
          <text class="rule-title">请避免取消订单</text>
          <text class="rule-link" @click="showHowToImprove">ⓘ 如何提升</text>
        </view>
        <text class="rule-desc">零工接单后，您取消订单或开除零工可能导致零工白跑并错失其它接单机会，引发纠纷或投诉</text>
      </view>

      <!-- 诚意分明细 -->
      <view class="rule-card">
        <text class="rule-title" style="margin-bottom: 12px;">诚意分明细</text>
        <view class="empty-section">
          <view class="empty-icon">
            <text style="font-size:32px;color:#ccc;">📥</text>
          </view>
          <text class="empty-text">暂无诚意分变动记录</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {}
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    showBenefits() {
      uni.showToast({ title: '查看星级权益', icon: 'none' })
    },
    showScoreDetail() {
      uni.showToast({ title: '查看诚意分明细', icon: 'none' })
    },
    showRules() {
      uni.showToast({ title: '查看规则', icon: 'none' })
    },
    showHowToImprove() {
      uni.showToast({ title: '如何提升', icon: 'none' })
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
  padding-bottom: 20px;
}

.level-banner {
  background: linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%);
  padding: 20px 16px;
  text-align: center;
}

.level-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #66BB6A, #43A047);
  border-radius: 16px;
  margin: 0 auto 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.level-title {
  font-size: 24px;
  font-weight: 700;
  color: #33691E;
  margin-bottom: 6px;
}

.level-desc {
  font-size: 13px;
  color: #558B2F;
  margin-bottom: 12px;
}

.benefit-btn {
  display: inline-block;
  padding: 6px 14px;
  background: #fff;
  color: #33691E;
  border-radius: 14px;
  font-size: 12px;
  font-weight: 500;
}

.score-card {
  background: #fff;
  margin: 12px 16px;
  border-radius: 12px;
  padding: 16px;
}

.score-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 12px;
}

.score-value {
  font-size: 36px;
  font-weight: 700;
  color: #333;
}

.score-unit {
  font-size: 14px;
  color: #666;
  margin-left: 4px;
}

.score-period {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.score-detail-link {
  font-size: 13px;
  color: #FF6B35;
}

.score-scale {
  display: flex;
  justify-content: space-between;
  font-size: 10px;
  color: #999;
  margin-bottom: 4px;
}

.score-bar {
  position: relative;
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  margin: 16px 0 8px;
}

.score-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #FF6B35, #FF8C5A);
  border-radius: 4px;
}

.star-levels {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
}

.star-item {
  text-align: center;
  flex: 1;
}

.star-label {
  font-size: 11px;
  color: #666;
  font-weight: 500;
}

.star-value {
  font-size: 10px;
  color: #999;
  margin-top: 2px;
}

.star-item.active .star-label {
  color: #FF6B35;
  font-weight: 600;
}

.star-item.active {
  background: #FFF3ED;
  margin: 0 2px;
  border-radius: 6px;
  padding: 4px 2px;
}

.rule-card {
  background: #fff;
  margin: 12px 16px;
  border-radius: 12px;
  padding: 16px;
}

.rule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.rule-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
}

.rule-link {
  font-size: 12px;
  color: #FF6B35;
}

.rule-desc {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 4px;
}

.metric-bar {
  margin-top: 12px;
}

.metric-header {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #666;
  margin-bottom: 6px;
}

.metric-ranges {
  display: flex;
  height: 20px;
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 6px;
}

.metric-range {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: #fff;
  font-weight: 500;
}

.metric-range.excellent {
  background: #52C41A;
}

.metric-range.normal {
  background: #FAAD14;
}

.metric-range.poor {
  background: #FF4D4F;
}

.metric-labels {
  display: flex;
  font-size: 10px;
  color: #999;
}

.metric-labels text {
  flex: 1;
  text-align: center;
}

.empty-section {
  text-align: center;
  padding: 24px 0;
}

.empty-text {
  font-size: 13px;
  color: #999;
  margin-top: 8px;
}
</style>
