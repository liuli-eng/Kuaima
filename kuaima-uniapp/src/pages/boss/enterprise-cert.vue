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
      <text class="nav-title">企业认证</text>
      <view class="nav-right">
        <text>⋯</text>
      </view>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <!-- Hero区域 -->
      <view class="hero-section">
        <view class="hero-icon">
          <text style="font-size:32px;color:#FF6B35;">🛡</text>
        </view>
        <text class="hero-title">请尽快完成企业认证</text>
        <text class="hero-desc">否则会影响后续发布招工</text>
      </view>

      <!-- 认证选项 -->
      <view class="cert-options">
        <view class="cert-card" @click="chooseCert(1)">
          <view class="cert-header">
            <text class="cert-name">企业自招（仅限本公司招工）</text>
            <view class="cert-btn">
              <text>认证</text>
            </view>
          </view>
          <text class="cert-desc">上传营业执照原件或加盖公章复印件</text>
        </view>
      </view>

      <!-- 联系客服 -->
      <view class="contact-link" @click="navigateTo('service-chat')">
        <text>认证遇到问题？联系客服 ›</text>
      </view>

      <!-- 认证权益 -->
      <view class="benefits-section">
        <text class="benefits-title">企业认证后，获得以下权益</text>
        <view class="benefits-grid">
          <view class="benefit-item">
            <view class="benefit-icon">
              <text style="font-size:20px;color:#FF6B35;">⚡</text>
            </view>
            <text class="benefit-text">快速审核</text>
          </view>
          <view class="benefit-item">
            <view class="benefit-icon">
              <text style="font-size:20px;color:#FF6B35;">👍</text>
            </view>
            <text class="benefit-text">优先推荐</text>
          </view>
          <view class="benefit-item">
            <view class="benefit-icon">
              <text style="font-size:20px;color:#FF6B35;">🏅</text>
            </view>
            <text class="benefit-text">认证标识</text>
          </view>
        </view>
      </view>

      <view style="height:30px;"></view>
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
    navigateTo(pageName) {
      const bossPages = [
        'boss-employer', 'boss-home', 'boss-message', 'boss-order', 'boss-profile', 
        'boss-publish', 'search-worker', 'select-job', 'publish-info', 'schedule-stats', 
        'enterprise-cert', 'enterprise-cert-form', 'creditor-score', 'talent-list', 
        'expense-detail', 'payment-detail', 'recruit-manager', 'recruit-address', 
        'sub-account', 'suspend-settle', 'switch-account', 'invite-code', 'blacklist', 
        'all-jobs', 'boss-filter', 'settlement', 'contract', 'system-notice', 'missed-call', 
        'signup-notice', 'invite-friend', 'service-chat', 'insurance'
      ]
      
      let url = `/pages/boss/${pageName}`
      if (!bossPages.includes(pageName)) {
        url = `/pages/${pageName}`
      }
      
      uni.navigateTo({ url })
    },
    chooseCert(type) {
      if (type === 1) {
        this.navigateTo('enterprise-cert-form')
      } else {
        uni.showToast({ title: '即将跳转到劳务中介认证流程', icon: 'none' })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  width: 100%;
  height: 100vh;
  background: #fff;
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

.hero-section {
  padding: 30px 20px 20px;
  text-align: center;
}

.hero-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, #FFE4B5, #FFDAB9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-title {
  font-size: 20px;
  font-weight: 700;
  color: #333;
}

.hero-desc {
  font-size: 13px;
  color: #999;
  margin-top: 6px;
}

.cert-options {
  padding: 0 16px;
}

.cert-card {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
}

.cert-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cert-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.cert-btn {
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: #fff;
  border-radius: 20px;
  padding: 6px 16px;
  font-size: 13px;
  font-weight: 500;
}

.cert-desc {
  font-size: 12px;
  color: #999;
  margin-top: 6px;
}

.contact-link {
  text-align: center;
  margin: 20px 0;
  font-size: 13px;
  color: #FF6B35;
}

.benefits-section {
  background: #fafafa;
  padding: 20px 16px;
  margin-top: 20px;
}

.benefits-title {
  text-align: center;
  font-size: 14px;
  color: #666;
  margin-bottom: 20px;
}

.benefits-grid {
  display: flex;
  justify-content: space-around;
}

.benefit-item {
  text-align: center;
}

.benefit-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto 8px;
  background: #FFF3ED;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.benefit-text {
  font-size: 12px;
  color: #666;
}
</style>
