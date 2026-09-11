<template>
  <view class="container">
    <view class="status-spacer" :style="{ height: `${statusBarHeight}px` }" />
    <scroll-view scroll-y class="scroll-area">
      <!-- 顶部导航 -->
      <view class="top-header">
        <view class="nav-bar">
          <view class="brand-tag">快马日结</view>
        </view>

        <!-- 用户信息 -->
        <view class="user-info" @click="navigateTo('personal-info')">
          <view class="user-avatar">
            <image :src="userIcon" mode="aspectFit" />
          </view>
          <view class="user-info-main">
            <text class="user-name">{{ profile.name }} | 老板</text>
            <image class="chevron-icon" :src="chevronRightIcon" mode="aspectFit" />
          </view>
          <view class="switch-btn" @click.stop="navigateTo('switch-account')">
            <image :src="exchangeIcon" mode="aspectFit" />
            <text>我要找工作</text>
          </view>
        </view>
      </view>

      <!-- 企业认证Banner -->
      <view class="cert-banner card-shadow" @click="navigateTo('enterprise-cert')">
        <view class="cert-icon">
          <image :src="buildingIcon" mode="aspectFit" />
        </view>
        <view class="cert-content">
          <text class="cert-title">{{ profile.enterpriseApproved ? '企业认证已通过' : '完成企业认证 解锁权益' }}</text>
          <text class="cert-desc">{{ profile.enterpriseApproved ? '认证信息已生效' : '曝光加权·优先推荐熟练零工接单' }}</text>
        </view>
        <text class="cert-btn">{{ profile.enterpriseApproved ? '查看认证' : '立即认证' }}</text>
      </view>

      <!-- 我的服务 -->
      <text class="section-title">我的服务</text>
      <view class="service-grid card-shadow">
        <view class="service-item" @click="navigateTo('suspend-settle')">
          <view class="service-icon" style="color: #FF6B35;">
            <image :src="pauseIcon" mode="aspectFit" />
          </view>
          <text class="service-label">待结算</text>
        </view>
        <view class="service-item" @click="navigateTo('payment-detail')">
          <view class="service-icon" style="color: #FF6B35;">
            <image :src="sackDollarIcon" mode="aspectFit" />
          </view>
          <text class="service-label">报酬支付明细</text>
        </view>
      </view>

      <!-- 其他功能 -->
      <text class="section-title">其他功能</text>
      <view class="other-grid card-shadow">
        <view class="other-item" @click="navigateTo('service-chat')">
          <view class="other-icon" style="color: #FF6B35;">
            <image :src="headsetIcon" mode="aspectFit" />
          </view>
          <text class="other-label">联系客服</text>
        </view>
        <view class="other-item" @click="navigateTo('realname')">
          <view class="other-icon" style="color: #FF6B35;">
            <image :src="mobileIcon" mode="aspectFit" />
          </view>
          <text class="other-label">手机号认证</text>
        </view>
        <view class="other-item" @click="navigateTo('enterprise-cert')">
          <view class="other-icon" style="color: #FF6B35;">
            <image :src="buildingColumnsIcon" mode="aspectFit" />
          </view>
          <text class="other-label">企业认证</text>
        </view>
        <view class="other-item" @click="navigateTo('user-agreement')">
          <view class="other-icon" style="color: #FF6B35;">
            <image :src="fileLinesIcon" mode="aspectFit" />
          </view>
          <text class="other-label">用户服务协议</text>
        </view>
        <view class="other-item" @click="navigateTo('privacy')">
          <view class="other-icon" style="color: #FF6B35;">
            <image :src="lockIcon" mode="aspectFit" />
          </view>
          <text class="other-label">隐私协议</text>
        </view>
        <view class="other-item" @click="navigateTo('copyright')">
          <view class="other-icon" style="color: #FF6B35;">
            <image :src="bookOpenIcon" mode="aspectFit" />
          </view>
          <text class="other-label">知识产权规则</text>
        </view>
        <view class="other-item" @click="navigateTo('rule')">
          <view class="other-icon" style="color: #FF6B35;">
            <image :src="clipboardListIcon" mode="aspectFit" />
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
          <image :src="houseGrayIcon" mode="aspectFit" />
        </view>
        <text class="tab-label">首页</text>
      </view>
      <view class="tab-item" @click="switchTab('order')">
        <view class="tab-icon-wrap">
          <image :src="calendarCheckGrayIcon" mode="aspectFit" />
        </view>
        <text class="tab-label">招工订单</text>
      </view>
      <view class="tab-item" @click="switchTab('workbench')">
        <view class="tab-icon-wrap">
          <image :src="briefcaseGrayIcon" mode="aspectFit" />
        </view>
        <text class="tab-label">工作台</text>
      </view>
      <view class="tab-item" @click="switchTab('message')">
        <view class="tab-icon-wrap">
          <image :src="commentDotsGrayIcon" mode="aspectFit" />
        </view>
        <text class="tab-label">消息</text>
      </view>
      <view class="tab-item active" @click="switchTab('profile')">
        <view class="tab-icon-wrap">
          <image :src="faceSmileWhiteIcon" mode="aspectFit" />
        </view>
        <text class="tab-label">我的</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getCurrentUser, getUser } from '@/api/backend'
import userIcon from '/static/icons/boss-profile/user.svg'
import chevronRightIcon from '/static/icons/boss-profile/chevron-right.svg'
import exchangeIcon from '/static/icons/boss-profile/exchange.svg'
import buildingIcon from '/static/icons/boss-profile/building.svg'
import pauseIcon from '/static/icons/boss-profile/pause.svg'
import sackDollarIcon from '/static/icons/boss-profile/sack-dollar.svg'
import headsetIcon from '/static/icons/boss-profile/headset.svg'
import mobileIcon from '/static/icons/boss-profile/mobile.svg'
import buildingColumnsIcon from '/static/icons/boss-profile/building-columns.svg'
import fileLinesIcon from '/static/icons/boss-profile/file-lines.svg'
import lockIcon from '/static/icons/boss-profile/lock.svg'
import bookOpenIcon from '/static/icons/boss-profile/book-open.svg'
import clipboardListIcon from '/static/icons/boss-profile/clipboard-list.svg'
import houseGrayIcon from '/static/icons/boss-tabbar/house-gray.svg'
import calendarCheckGrayIcon from '/static/icons/boss-tabbar/calendar-check-gray.svg'
import briefcaseGrayIcon from '/static/icons/boss-tabbar/briefcase-gray.svg'
import commentDotsGrayIcon from '/static/icons/boss-tabbar/comment-dots-gray.svg'
import faceSmileWhiteIcon from '/static/icons/boss-tabbar/face-smile-white.svg'

export default {
  data() {
    return {
      statusBarHeight: uni.getSystemInfoSync().statusBarHeight || 0,
      userIcon,
      chevronRightIcon,
      exchangeIcon,
      buildingIcon,
      pauseIcon,
      sackDollarIcon,
      headsetIcon,
      mobileIcon,
      buildingColumnsIcon,
      fileLinesIcon,
      lockIcon,
      bookOpenIcon,
      clipboardListIcon,
      houseGrayIcon,
      calendarCheckGrayIcon,
      briefcaseGrayIcon,
      commentDotsGrayIcon,
      faceSmileWhiteIcon,
      profile: {
        name: '用户',
        enterpriseApproved: false,
      },
    }
  },
  onShow() {
    this.loadProfile()
  },
  methods: {
    async loadProfile() {
      try {
        const userId = uni.getStorageSync('userId')
        const [currentUser, user] = await Promise.all([
          getCurrentUser().catch(() => null),
          userId ? getUser(userId).catch(() => null) : Promise.resolve(null),
        ])
        const data = { ...(user || {}), ...(currentUser || {}) }
        const cached = uni.getStorageSync('userInfo') || {}
        const name = data.nickname || data.name || data.realName || cached.nickname || cached.name || '用户'
        const enterpriseStatus = String(data.enterpriseStatus || '').toUpperCase()
        this.profile = {
          name,
          enterpriseApproved: ['APPROVED', 'PASSED', '已通过', '已认证'].includes(enterpriseStatus),
        }
      } catch (_) {
        // 保留安全默认值，避免接口失败影响页面渲染。
      }
    },
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
      const sharedPageMap = {
        'rule': '/pages/worker/rule',
        'user-agreement': '/pages/worker/user-agreement',
        'privacy': '/pages/worker/privacy',
        'copyright': '/pages/worker/copyright'
      }
      const url = bossPages.includes(pageName)
        ? `/pages/boss/${pageName}`
        : sharedPageMap[pageName]

      if (!url) {
        uni.showToast({ title: '页面暂未开放', icon: 'none' })
        return
      }

      uni.navigateTo({
        url,
        fail: () => {
          uni.showToast({ title: '页面打开失败，请重试', icon: 'none' })
        }
      })
    },
    switchTab(tab) {
      if (tab === 'workbench') {
        uni.showToast({ title: '工作台页面暂未开放', icon: 'none' })
        return
      }
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
  background: #F3F4F6;
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
  background: #F3F4F6;
}

.status-spacer {
  flex-shrink: 0;
  background: #F7F7F7;
}

.top-header {
  background: #FFD96F;
  padding: 12px 16px 20px;
}

.nav-bar {
  min-height: 36px;
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

.footer-info text {
  display: block;
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

.status-icons image { width: 16px; height: 16px; }
.user-avatar image { width: 34px; height: 34px; }
.chevron-icon { width: 12px; height: 12px; }
.switch-btn image { width: 14px; height: 14px; }
.cert-icon image { width: 21px; height: 21px; }
.service-icon image, .other-icon image { width: 24px; height: 24px; }
.tab-icon-wrap image { width: 22px; height: 22px; }
.tab-item.active .tab-icon-wrap image { opacity: 1; }
</style>
