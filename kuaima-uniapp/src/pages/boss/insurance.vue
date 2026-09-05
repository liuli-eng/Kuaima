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

    <view class="page-bg">
      <!-- 导航栏 -->
      <view class="nav-bar">
        <view class="nav-back" @click="goBack">
          <text>←</text>
        </view>
        <text class="nav-title">零工意外保障</text>
        <view class="nav-placeholder"></view>
      </view>

      <scroll-view scroll-y class="scroll-area">
        <!-- Banner -->
        <view class="banner">
          <view class="banner-title">
            <text style="margin-right:6px;">🛡</text>
            快马日结保障计划
          </view>
          <text class="banner-desc">接单期间享受意外伤害保障，让您的零工更安心、更放心</text>
          <view class="banner-stats">
            <view class="stat-item">
              <text class="stat-value">33万</text>
              <text class="stat-label">最高保额</text>
            </view>
            <view class="stat-item">
              <text class="stat-value">100%</text>
              <text class="stat-label">平台承担</text>
            </view>
            <view class="stat-item">
              <text class="stat-value">24h</text>
              <text class="stat-label">快速理赔</text>
            </view>
          </view>
        </view>

        <!-- 选择保障方案 -->
        <view class="section-title">
          <text>选择保障方案</text>
          <text class="link" @click="showCompare">方案对比 ›</text>
        </view>

        <!-- 基础保障 -->
        <view class="plan-card" :class="{ selected: selectedPlan === 'basic' }" @click="selectPlan('basic')">
          <view class="check-mark" v-if="selectedPlan === 'basic'"></view>
          <view class="plan-header">
            <view class="plan-icon green">
              <text style="font-size:18px;">🍃</text>
            </view>
            <view class="plan-info">
              <view class="plan-name">基础保障 <text class="badge">默认</text></view>
              <view class="plan-price"><text class="free">免费</text> · 平台承担费用</view>
            </view>
          </view>
          <view class="plan-features">
            <view class="plan-feature"><text style="color:#52C41A;font-size:10px;">✅</text> 意外伤害医疗 3万</view>
            <view class="plan-feature"><text style="color:#52C41A;font-size:10px;">✅</text> 意外身故 10万</view>
            <view class="plan-feature"><text style="color:#52C41A;font-size:10px;">✅</text> 住院津贴 50元/天</view>
          </view>
          <text class="plan-desc">适用于普工、杂工等基础工种，接单期间全程保障，无需额外付费。</text>
        </view>

        <!-- 标准保障 -->
        <view class="plan-card" :class="{ selected: selectedPlan === 'standard' }" @click="selectPlan('standard')">
          <view class="check-mark" v-if="selectedPlan === 'standard'"></view>
          <view class="plan-header">
            <view class="plan-icon orange">
              <text style="font-size:18px;">⚡</text>
            </view>
            <view class="plan-info">
              <view class="plan-name">标准保障 <text class="badge">推荐</text></view>
              <view class="plan-price"><text class="free">+0.5元/天</text> · 从工价扣除</view>
            </view>
          </view>
          <view class="plan-features">
            <view class="plan-feature"><text style="color:#52C41A;font-size:10px;">✅</text> 意外伤害医疗 10万</view>
            <view class="plan-feature"><text style="color:#52C41A;font-size:10px;">✅</text> 意外身故 33万</view>
            <view class="plan-feature"><text style="color:#52C41A;font-size:10px;">✅</text> 住院津贴 100元/天</view>
            <view class="plan-feature"><text style="color:#52C41A;font-size:10px;">✅</text> 24小时快速理赔</view>
          </view>
          <text class="plan-desc">适用于普工、焊锡工等常见工种，保障更全面，保费低。</text>
        </view>

        <!-- 尊享保障 -->
        <view class="plan-card" :class="{ selected: selectedPlan === 'premium' }" @click="selectPlan('premium')">
          <view class="check-mark" v-if="selectedPlan === 'premium'"></view>
          <view class="plan-header">
            <view class="plan-icon purple">
              <text style="font-size:18px;">⭐</text>
            </view>
            <view class="plan-info">
              <view class="plan-name">尊享保障</view>
              <view class="plan-price"><text class="free">+1元/天</text> · 从工价扣除</view>
            </view>
          </view>
          <view class="plan-features">
            <view class="plan-feature"><text style="color:#52C41A;font-size:10px;">✅</text> 意外伤害医疗 30万</view>
            <view class="plan-feature"><text style="color:#52C41A;font-size:10px;">✅</text> 意外身故 100万</view>
            <view class="plan-feature"><text style="color:#52C41A;font-size:10px;">✅</text> 住院津贴 200元/天</view>
            <view class="plan-feature"><text style="color:#52C41A;font-size:10px;">✅</text> 家政协助服务</view>
          </view>
          <text class="plan-desc">适用于高空作业、重型机械等高危工种，提供最高级别保障。</text>
        </view>

        <!-- 自定义保障 -->
        <view class="plan-card" :class="{ selected: selectedPlan === 'custom' }" @click="selectPlan('custom')">
          <view class="check-mark" v-if="selectedPlan === 'custom'"></view>
          <view class="plan-header">
            <view class="plan-icon blue">
              <text style="font-size:18px;">⚙</text>
            </view>
            <view class="plan-info">
              <view class="plan-name">自定义保障</view>
              <view class="plan-price"><text class="free">按需定价</text> · 联系客服定制</view>
            </view>
          </view>
          <view class="plan-features">
            <view class="plan-feature"><text style="color:#52C41A;font-size:10px;">✅</text> 自由组合保额</view>
            <view class="plan-feature"><text style="color:#52C41A;font-size:10px;">✅</text> 特殊工种覆盖</view>
            <view class="plan-feature"><text style="color:#52C41A;font-size:10px;">✅</text> 家庭附加保障</view>
          </view>
          <text class="plan-desc">适用于有特殊保障需求的雇主，可根据实际情况定制方案。</text>
        </view>

        <!-- 保障说明 -->
        <view class="guide-card">
          <view class="guide-title">
            <text style="color:#FF6B35;">💡</text>
            保障说明
          </view>
          <view class="guide-list">
            <view class="guide-item">
              <view class="num">1</view>
              <text>保障期间：从零工签到开工起，至签退收工止，全程保障</text>
            </view>
            <view class="guide-item">
              <view class="num">2</view>
              <text>理赔流程：事故发生后24小时内报案，提交相关材料</text>
            </view>
            <view class="guide-item">
              <view class="num">3</view>
              <text>保费说明：按天计费，从工价中自动扣除，结算时一并结算</text>
            </view>
            <view class="guide-item">
              <view class="num">4</view>
              <text>详细条款请查看《快马日结保险协议》</text>
            </view>
          </view>
        </view>

        <view style="height: 16px;"></view>
      </scroll-view>

      <!-- 底部按钮 -->
      <view class="bottom-bar">
        <button class="save-btn" @click="savePlan">确认选择</button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      selectedPlan: 'standard'
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    selectPlan(type) {
      this.selectedPlan = type
    },
    showCompare() {
      uni.showToast({ title: '查看对比详情', icon: 'none' })
    },
    savePlan() {
      const planNames = {
        basic: '基础保障',
        standard: '标准保障',
        premium: '尊享保障',
        custom: '自定义保障'
      }
      uni.showToast({ title: `保障方案已选择：${planNames[this.selectedPlan]}`, icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
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
  background: #fff;
}

.status-icons {
  display: flex;
  align-items: center;
  gap: 4px;
}

.page-bg {
  background: #fff;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.nav-bar {
  padding: 14px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  flex-shrink: 0;
}

.nav-back {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #333;
  font-size: 20px;
}

.nav-title {
  font-size: 17px;
  font-weight: 700;
  color: #333;
}

.nav-placeholder {
  width: 32px;
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
  background: #F7F8FA;
}

.banner {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  margin: 12px;
  border-radius: 12px;
  padding: 18px;
  color: #fff;
}

.banner-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 6px;
}

.banner-desc {
  font-size: 13px;
  opacity: 0.9;
  line-height: 1.5;
}

.banner-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid rgba(255,255,255,0.2);
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
}

.stat-label {
  font-size: 11px;
  opacity: 0.8;
  margin-top: 2px;
}

.section-title {
  font-size: 13px;
  color: #999;
  padding: 10px 16px 6px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-title .link {
  color: #FF6B35;
  font-size: 12px;
}

.plan-card {
  background: #fff;
  margin: 0 12px 10px;
  border-radius: 12px;
  padding: 16px;
  position: relative;
}

.plan-card.selected {
  background: linear-gradient(135deg, #FFF8E6, #FFF3ED);
  border: 2px solid #FF6B35;
  padding: 14px;
}

.check-mark {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #FF6B35;
  display: flex;
  align-items: center;
  justify-content: center;
}

.check-mark::after {
  content: '✓';
  color: #fff;
  font-size: 13px;
  font-weight: bold;
}

.plan-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.plan-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.plan-icon.green { background: #E8F5E9; color: #52C41A; }
.plan-icon.orange { background: #FFF3ED; color: #FF6B35; }
.plan-icon.purple { background: #F3EEFB; color: #722ED1; }
.plan-icon.blue { background: #EBF5FF; color: #1677FF; }

.plan-info {
  flex: 1;
}

.plan-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
  gap: 6px;
}

.plan-name .badge {
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 3px;
  background: #FFF3ED;
  color: #FF6B35;
  font-weight: 400;
}

.plan-price {
  font-size: 12px;
  color: #666;
  margin-top: 3px;
}

.plan-price .free {
  color: #FF6B35;
  font-weight: 500;
}

.plan-features {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 12px;
}

.plan-feature {
  font-size: 12px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 3px;
}

.plan-desc {
  font-size: 12px;
  color: #999;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f5f5f5;
  line-height: 1.6;
}

.guide-card {
  background: #fff;
  margin: 0 12px 12px;
  border-radius: 12px;
  padding: 14px 16px;
}

.guide-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 10px;
}

.guide-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.guide-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 12px;
  color: #666;
  line-height: 1.5;
}

.guide-item .num {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #FFF3ED;
  color: #FF6B35;
  font-size: 10px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 1px;
}

.bottom-bar {
  background: #fff;
  padding: 12px 16px 30px;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.save-btn {
  width: 100%;
  height: 46px;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: #fff;
  border: none;
  border-radius: 23px;
  font-size: 15px;
  font-weight: 600;
}
</style>
