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
      <!-- 头部 -->
      <view class="header-bar">
        <view class="title-row">
          <text class="page-title">消息</text>
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
      </view>

      <!-- 消息列表 -->
      <view class="message-list card-shadow">
        <!-- 企业认证提醒 -->
        <view class="message-item" @click="handleCertClick">
          <view class="message-icon icon-blue">
            <text>🏢</text>
            <view class="badge-dot"></view>
          </view>
          <view class="message-content">
            <text class="message-title">企业认证提醒</text>
            <text class="message-desc">曝光加权·优先推荐熟练零工接单</text>
          </view>
          <view class="message-action">
            <text class="action-btn-small" @click.stop="navigateTo('enterprise-cert')">立即认证</text>
          </view>
        </view>

        <!-- 系统通知 -->
        <view class="message-item" @click="navigateTo('system-notice')">
          <view class="message-icon icon-orange">
            <text>🔔</text>
          </view>
          <view class="message-content">
            <text class="message-title">系统通知</text>
            <text class="message-desc">消息内容</text>
          </view>
          <view class="message-action">
            <text>›</text>
          </view>
        </view>

        <!-- 报名通知 -->
        <view class="message-item" @click="navigateTo('signup-notice')">
          <view class="message-icon icon-green">
            <text>➕</text>
            <view class="badge-dot"></view>
          </view>
          <view class="message-content">
            <text class="message-title">报名通知</text>
            <text class="message-desc">有3位零工报名了您发布的岗位</text>
          </view>
          <view class="message-action">
            <text>›</text>
          </view>
        </view>

        <!-- 结算消息 -->
        <view class="message-item" @click="navigateTo('settlement')">
          <view class="message-icon icon-purple">
            <text>💵</text>
          </view>
          <view class="message-content">
            <text class="message-title">结算消息</text>
            <text class="message-desc">昨日岗位工资已结算完成</text>
          </view>
          <view class="message-action">
            <text>›</text>
          </view>
        </view>
      </view>

      <!-- 查看历史消息 -->
      <view class="view-history">
        <view class="history-btn" @click="navigateTo('history-message')">
          <text>查看历史消息</text>
        </view>
      </view>

      <!-- 底部标语 -->
      <view class="bottom-slogan">
        <text class="slogan-title">招临时工 上快马日结</text>
        <text class="slogan-desc">— 熟练工 上岗快 人靠谱 —</text>
      </view>
    </scroll-view>

    <!-- 底部TabBar -->
    <view class="tab-bar">
      <view class="tab-item" @click="switchTab('home')">
        <view class="tab-icon-wrap">🏠</view>
        <text class="tab-label">首页</text>
      </view>
      <view class="tab-item" @click="switchTab('order')">
        <view class="tab-icon-wrap">📋</view>
        <text class="tab-label">日结订单</text>
      </view>
      <view class="tab-item active" @click="switchTab('message')">
        <view class="tab-icon-wrap">💬</view>
        <text class="tab-label">消息</text>
      </view>
      <view class="tab-item" @click="switchTab('profile')">
        <view class="tab-icon-wrap">😊</view>
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
        'signup-notice', 'invite-friend', 'service-chat', 'insurance', 'realname'
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
    },
    handleCertClick() {
      uni.showToast({ title: '企业认证', icon: 'none' })
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

.header-bar {
  background: linear-gradient(180deg, #FFD59E 0%, #FFE4B5 100%);
  padding: 12px 16px 20px;
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #8B4513;
  position: relative;
}

.page-title::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 20px;
  height: 3px;
  background: #FF6B35;
  border-radius: 2px;
}

.nav-icons {
  display: flex;
  background: rgba(255, 255, 255, 0.8);
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

.message-list {
  background: white;
  margin: 12px 16px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.message-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f5f5f5;
}

.message-item:last-child {
  border-bottom: none;
}

.message-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  margin-right: 12px;
  position: relative;
  flex-shrink: 0;
}

.icon-blue { background: #E6F7FF; color: #1890FF; }
.icon-orange { background: #FFF3E0; color: #FF6B35; }
.icon-green { background: #E6F7EC; color: #52C41A; }
.icon-purple { background: #F9F0FF; color: #722ED1; }

.badge-dot {
  position: absolute;
  top: 0;
  right: 0;
  width: 10px;
  height: 10px;
  background: #FF4D4F;
  border-radius: 50%;
  border: 2px solid white;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.message-desc {
  font-size: 13px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-action {
  flex-shrink: 0;
  color: #ddd;
  font-size: 14px;
}

.action-btn-small {
  padding: 8px 16px;
  background: #FF6B35;
  color: white;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.view-history {
  text-align: center;
  padding: 24px 16px;
}

.history-btn {
  display: inline-block;
  padding: 12px 32px;
  border: 1px solid #ddd;
  border-radius: 24px;
  color: #666;
  font-size: 14px;
  background: white;
}

.bottom-slogan {
  text-align: center;
  padding: 20px 16px;
}

.slogan-title {
  font-size: 28px;
  font-weight: 800;
  color: #E8D5B7;
  letter-spacing: 4px;
}

.slogan-desc {
  font-size: 12px;
  color: #ccc;
  margin-top: 8px;
}

.tab-bar {
  flex-shrink: 0;
  height: 83px;
  background: rgba(255, 255, 255, 0.98);
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
}

.tab-icon-wrap {
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
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
</style>
