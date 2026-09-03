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

    <scroll-view scroll-y class="scroll-area">
      <!-- 顶部导航 -->
      <view class="top-header">
        <view class="nav-bar">
          <view class="brand-tag">快马日结</view>
          <view class="nav-icons">
            <view class="nav-icon-item">
              <text>⋯</text>
            </view>
            <view class="nav-divider"></view>
            <view class="nav-icon-item">
              <text>●</text>
            </view>
          </view>
        </view>

        <!-- 用户信息 -->
        <view class="user-info" @click="navigateTo('personal-info')">
          <view class="user-avatar">
            <text style="font-size:32px;color:white;">👤</text>
          </view>
          <view class="user-info-main">
            <text class="user-name">晴时见禾 | 老板</text>
            <text style="color:#999;font-size:12px;">›</text>
          </view>
          <view class="switch-btn" @click.stop="navigateTo('switch-account')">
            <text style="font-size:10px;">🔄</text>
            <text>我要找工作</text>
          </view>
        </view>
      </view>

      <!-- 企业认证Banner -->
      <view class="cert-banner card-shadow" @click="navigateTo('enterprise-cert')">
        <view class="cert-icon">
          <text style="font-size:18px;">🏢</text>
        </view>
        <view class="cert-content">
          <text class="cert-title">完成企业认证 解锁权益</text>
          <text class="cert-desc">曝光加权·优先推荐熟练零工接单</text>
        </view>
        <text class="cert-btn">立即认证</text>
      </view>

      <!-- 我的服务 -->
      <text class="section-title">我的服务</text>
      <view class="service-grid card-shadow">
        <view class="service-item" @click="navigateTo('suspend-settle')">
          <view class="service-icon" style="color: #FF6B35;">
            <text>⏸</text>
          </view>
          <text class="service-label">待结算</text>
        </view>
        <view class="service-item" @click="navigateTo('payment-detail')">
          <view class="service-icon" style="color: #FF6B35;">
            <text>💲</text>
          </view>
          <text class="service-label">报酬支付明细</text>
        </view>
      </view>

      <!-- 其他功能 -->
      <text class="section-title">其他功能</text>
      <view class="other-grid card-shadow">
        <view class="other-item" @click="navigateTo('service-chat')">
          <view class="other-icon" style="color: #FF6B35;">
            <text>🎧</text>
          </view>
          <text class="other-label">联系客服</text>
        </view>
        <view class="other-item" @click="navigateTo('realname')">
          <view class="other-icon" style="color: #FF6B35;">
            <text>📱</text>
          </view>
          <text class="other-label">手机号认证</text>
        </view>
        <view class="other-item" @click="navigateTo('enterprise-cert')">
          <view class="other-icon" style="color: #FF6B35;">
            <text>🏛</text>
          </view>
          <text class="other-label">企业认证</text>
        </view>
        <view class="other-item" @click="navigateTo('user-agreement')">
          <view class="other-icon" style="color: #FF6B35;">
            <text>📄</text>
          </view>
          <text class="other-label">用户服务协议</text>
        </view>
        <view class="other-item" @click="navigateTo('privacy')">
          <view class="other-icon" style="color: #FF6B35;">
            <text>🔒</text>
          </view>
          <text class="other-label">隐私协议</text>
        </view>
        <view class="other-item" @click="navigateTo('copyright')">
          <view class="other-icon" style="color: #FF6B35;">
            <text>📖</text>
          </view>
          <text class="other-label">知识产权规则</text>
        </view>
        <view class="other-item" @click="navigateTo('rule')">
          <view class="other-icon" style="color: #FF6B35;">
            <text>📋</text>
          </view>
          <text class="other-label">平台规则</text>
        </view>
      </view>

      <!-- 底部信息 -->
      <view class="footer-info">
        <text>客服电话 400-188-6610  工作时间 9:00-18:00</text>
        <text>推荐算法举报与未成年人举报渠道同上</text>
        <text>营业执照 人力资源服务许可证</text>
        <text>ICP证书号: 合字 B2-20250310</text>
        <text>上海市人社局备案 （001）123456</text>
      </view>
    </scroll-view>

    <!-- 底部TabBar -->
    <view class="tab-bar">
      <view class="tab-item" @click="switchTab('home')">
        <view class="tab-icon-wrap">
          <text>🏠</text>
        </view>
        <text class="tab-label">首页</text>
      </view>
      <view class="tab-item" @click="switchTab('order')">
        <view class="tab-icon-wrap">
          <text>📋</text>
        </view>
        <text class="tab-label">招工订单</text>
      </view>
      <view class="tab-item" @click="switchTab('message')">
        <view class="tab-icon-wrap">
          <text>💬</text>
        </view>
        <text class="tab-label">消息</text>
      </view>
      <view class="tab-item active" @click="switchTab('profile')">
        <view class="tab-icon-wrap">
          <text>😊</text>
        </view>
        <text class="tab-label">我的</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {}
  },
  methods: {
    navigateTo(pageName) {
      const bossPages = [
        'boss-employer', 'boss-home', 'boss-message', 'boss-order', 'boss-profile', 
        'boss-publish', 'search-worker', 'select-job', 'publish-info', 'schedule-stats', 
        'enterprise-cert', 'enterprise-cert-form', 'creditor-score', 'talent-list', 
        'expense-detail', 'payment-detail', 'recruit-manager', 'recruit-address', 
        'sub-account', 'suspend-settle', 'switch-account', 'invite-code', 'blacklist', 
        'all-jobs', 'boss-filter', 'settlement', 'contract', 'system-notice', 'missed-call', 
        'signup-notice', 'invite-friend', 'service-chat', 'insurance', 'realname', 
        'personal-info'
      ]
      
      let url = `/pages/boss/${pageName}`
      if (!bossPages.includes(pageName)) {
        url = `/pages/${pageName}`
      }
      
      uni.navigateTo({ url })
    },
    switchTab(tab) {
      const tabPages = {
        'home': '/pages/boss/home',
        'order': '/pages/boss/order',
        'message': '/pages/boss/message',
        'profile': '/pages/boss/profile'
      }
      const target = tabPages[tab]
      const currentRoute = getCurrentPages().slice(-1)[0]?.route
      if (!target || `/${currentRoute}` === target) return
      uni.redirectTo({
        url: target,
        fail: (error) => {
          console.error('Boss 主导航跳转失败', error)
          uni.reLaunch({
            url: target,
            fail: () => uni.showToast({ title: '页面跳转失败，请重试', icon: 'none' })
          })
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
}

.status-icons {
  display: flex;
  align-items: center;
  gap: 4px;
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
  background: #FFF8E6;
}

.top-header {
  background: linear-gradient(180deg, #FFD59E 0%, #FFE4B5 100%);
  padding: 12px 16px 20px;
}

.nav-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.brand-tag {
  background: #fff;
  padding: 6px 14px;
  border-radius: 20px;
  font-weight: 700;
  font-size: 14px;
  color: #8B4513;
}

.nav-icons {
  display: flex;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border-radius: 50px;
  padding: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.nav-icon-item {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-divider {
  width: 1px;
  height: 16px;
  background: #ddd;
  margin: 0 4px;
}

.user-info {
  display: flex;
  align-items: center;
  padding: 12px 0;
}

.user-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFB347, #FF6B35);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 14px;
  border: 3px solid rgba(255,255,255,0.5);
}

.user-info-main {
  display: flex;
  align-items: center;
  gap: 4px;
}

.user-name {
  font-size: 18px;
  font-weight: 700;
  color: #8B4513;
}

.switch-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: rgba(255,255,255,0.7);
  border-radius: 16px;
  font-size: 12px;
  color: #FF6B35;
  margin-left: auto;
}

.cert-banner {
  background: linear-gradient(135deg, #E3F2FD, #BBDEFB);
  margin: 0 16px 16px;
  border-radius: 16px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.cert-icon {
  width: 40px;
  height: 40px;
  background: #1976D2;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.cert-content {
  flex: 1;
}

.cert-title {
  font-size: 14px;
  font-weight: 600;
  color: #1565C0;
}

.cert-desc {
  font-size: 12px;
  color: #1976D2;
  margin-top: 2px;
}

.cert-btn {
  padding: 6px 14px;
  background: #1976D2;
  color: white;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  padding: 0 16px 12px;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  background: white;
  margin: 0 16px;
  padding: 20px 16px;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.service-item {
  text-align: center;
}

.service-icon {
  font-size: 24px;
  margin-bottom: 6px;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 32px;
}

.service-label {
  font-size: 12px;
  color: #666;
}

.other-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  background: white;
  margin: 12px 16px;
  padding: 20px 16px;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.other-item {
  text-align: center;
}

.other-icon {
  font-size: 24px;
  margin-bottom: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 32px;
}

.other-label {
  font-size: 12px;
  color: #666;
}

.footer-info {
  text-align: center;
  padding: 20px 16px;
  font-size: 11px;
  color: #bbb;
  line-height: 1.8;
}

/* TabBar样式 */
.tab-bar {
  flex-shrink: 0;
  height: 83px;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-top: 0.5px solid rgba(0, 0, 0, 0.05);
  display: flex;
  padding-bottom: 20px;
  z-index: 50;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.tab-icon-wrap {
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  margin-bottom: 3px;
}

.tab-label {
  font-size: 10px;
  color: #999;
  font-weight: 500;
}

.tab-item.active .tab-label {
  color: #FF6B35;
}

.tab-item.active .tab-icon-wrap {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  border-radius: 50%;
  width: 32px;
  height: 32px;
  color: white;
  margin-bottom: 2px;
  box-shadow: 0 4px 10px rgba(255, 107, 53, 0.3);
}
</style>
