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
            <image :src="profile.avatar || userIcon" mode="aspectFill" />
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

      <view class="employer-card card-shadow" @click="navigateTo('creditor-score')">
        <view class="employer-top">
          <view class="employer-title-row">
            <view class="employer-badge">
              <image :src="shieldIcon" mode="aspectFit" />
              <text>五星雇主</text>
            </view>
            <view class="employer-score">
              <text>诚意分</text>
              <text class="score-val">{{ stats.integrityScore }}</text>
              <image :src="chevronRightIcon" mode="aspectFit" />
            </view>
          </view>
          <view class="employer-stats">
            <view class="stat-col"><text class="stat-val">{{ stats.goodRate }}%</text><text>好评率</text></view>
            <view class="stat-col"><text class="stat-val">{{ stats.arrivalRate }}%</text><text>到达完成率</text></view>
            <view class="stat-col"><text class="stat-val">{{ stats.settleRate }}%</text><text>24小时结算率</text></view>
          </view>
          <view class="employer-footer">
            <text>累计支付报酬 <b>{{ stats.totalPayment }}</b>元</text>
            <text class="sep">|</text>
            <text>完单数 <b>{{ stats.completedOrders }}</b></text>
          </view>
        </view>
      </view>

      <view class="account-card card-shadow">
        <view class="unauth-card">
          <view class="unauth-icon"><image :src="userPlusOrangeIcon" mode="aspectFit" /></view>
          <text class="unauth-title">暂未授权任何员工</text>
          <text class="unauth-desc">授权员工可帮您招工、发薪、管理订单\n建议授权给您信任的合作伙伴</text>
          <button class="btn-authorize" @click="navigateTo('sub-account')">
            <image :src="userPlusWhiteIcon" mode="aspectFit" />
            <text>去授权</text>
          </button>
        </view>
      </view>
      <view class="asset-card card-shadow"><view v-for="asset in assets" :key="asset.label" class="asset-item" @click="navigateTo(asset.page)"><text class="asset-val">{{ asset.value }}</text><text>{{ asset.label }}</text><image :src="chevronRightIcon" mode="aspectFit" /></view></view>

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

      <!-- 项目管理入口 -->
      <view class="entry-card projects" @click="navigateTo('projects')">
        <view class="entry-name">项目管理</view>
        <view class="entry-desc">签到考勤 入职离职</view>
        <text class="entry-icon">🗂</text>
      </view>

      <!-- 我的服务 -->
      <text class="section-title">我的服务</text>
      <view class="service-grid card-shadow">
        <view class="service-item" @click="navigateTo('talent-list')"><view class="service-icon"><image :src="usersIcon" mode="aspectFit" /></view><text class="service-label">人才库</text></view>
        <view class="service-item" @click="navigateTo('suspend-settle')">
          <view class="service-icon">
            <image :src="banIcon" mode="aspectFit" />
          </view>
          <text class="service-label">暂不结算</text>
        </view>
        <view class="service-item" @click="navigateTo('payment-detail')">
          <view class="service-icon">
            <image :src="fileInvoiceDollarIcon" mode="aspectFit" />
          </view>
          <text class="service-label">报酬支付明细</text>
        </view>
        <view class="service-item" @click="navigateTo('expense-detail')"><view class="service-icon"><image :src="calendarIcon" mode="aspectFit" /></view><text class="service-label">费用报销明细</text></view>
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
            <image :src="userShieldIcon" mode="aspectFit" />
          </view>
          <text class="other-label">实名认证</text>
        </view>
        <view class="other-item" @click="navigateTo('enterprise-cert')">
          <view class="other-icon" style="color: #FF6B35;">
            <image :src="buildingColumnsIcon" mode="aspectFit" />
          </view>
          <text class="other-label">企业认证</text>
        </view>
        <view class="other-item" @click="navigateTo('switch-account')">
          <view class="other-icon">
            <image :src="exchangeDarkIcon" mode="aspectFit" />
          </view>
          <text class="other-label">更换账号</text>
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
          <image src="/static/icons/boss-tabbar/briefcase-gray.svg" mode="aspectFit" />
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
import { getCurrentUser, getUser, getBossProfile, getBossProfileAssets, getBossProfileStats } from '@/api/backend'
import userIcon from '/static/avatars/default-boss-avatar.png'
import chevronRightIcon from '/static/icons/boss-profile/chevron-right.svg'
import exchangeIcon from '/static/icons/boss-profile/exchange.svg'
import buildingIcon from '/static/icons/boss-profile/building.svg'
import headsetIcon from '/static/icons/boss-profile/headset.svg'
import buildingColumnsIcon from '/static/icons/boss-profile/building-columns.svg'
import fileLinesIcon from '/static/icons/boss-profile/file-lines.svg'
import lockIcon from '/static/icons/boss-profile/lock.svg'
import bookOpenIcon from '/static/icons/boss-profile/book-open.svg'
import clipboardListIcon from '/static/icons/boss-profile/clipboard-list.svg'
import houseGrayIcon from '/static/icons/boss-tabbar/house-gray.svg'
import calendarCheckGrayIcon from '/static/icons/boss-tabbar/calendar-check-gray.svg'
import commentDotsGrayIcon from '/static/icons/boss-tabbar/comment-dots-gray.svg'
import faceSmileWhiteIcon from '/static/icons/boss-tabbar/face-smile-white.svg'
import shieldIcon from '/static/icons/boss-profile/shield-check.svg'
import calendarIcon from '/static/icons/boss-profile/calendar-check-solid-gray.svg'
import userPlusOrangeIcon from '/static/icons/boss-profile/user-plus-orange.svg'
import userPlusWhiteIcon from '/static/icons/boss-profile/user-plus-white.svg'
import usersDarkIcon from '/static/icons/boss-profile/users-dark.svg'
import banIcon from '/static/icons/boss-profile/ban-dark.svg'
import fileInvoiceDollarIcon from '/static/icons/boss-profile/file-invoice-dollar-dark.svg'
import userShieldIcon from '/static/icons/boss-profile/user-shield-orange.svg'
import exchangeDarkIcon from '/static/icons/boss-profile/exchange-dark.svg'

export default {
  data() {
    return {
      statusBarHeight: uni.getSystemInfoSync().statusBarHeight || 0,
      userIcon,
      chevronRightIcon,
      exchangeIcon,
      buildingIcon,
      headsetIcon,
      buildingColumnsIcon,
      fileLinesIcon,
      lockIcon,
      bookOpenIcon,
      clipboardListIcon,
      houseGrayIcon,
      calendarCheckGrayIcon,
      commentDotsGrayIcon,
      faceSmileWhiteIcon,
      shieldIcon,
      usersIcon: usersDarkIcon,
      calendarIcon,
      userPlusOrangeIcon,
      userPlusWhiteIcon,
      banIcon,
      fileInvoiceDollarIcon,
      userShieldIcon,
      exchangeDarkIcon,
      assets: [
        { value: '320', label: '积分', page: 'points' },
        { value: '0', label: '券包(个)', page: 'voucher' },
        { value: '0.00', label: '奖励金(元)', page: 'reward' },
      ],
      stats: {
        integrityScore: 709,
        goodRate: 91,
        arrivalRate: 82,
        settleRate: 97,
        totalPayment: '10万+',
        completedOrders: 757,
        authorizedName: '王娜',
        balance: '0',
      },
      profile: {
        name: '用户',
        avatar: '',
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
        const [currentUser, user, bossProfile, assetSummary, profileStats] = await Promise.all([
          getCurrentUser().catch(() => null),
          userId ? getUser(userId).catch(() => null) : Promise.resolve(null),
          userId ? getBossProfile(userId).catch(() => null) : Promise.resolve(null),
          userId ? getBossProfileAssets(userId).catch(() => null) : Promise.resolve(null),
          userId ? getBossProfileStats(userId).catch(() => null) : Promise.resolve(null),
        ])
        const data = { ...(user || {}), ...(currentUser || {}), ...(bossProfile || {}) }
        const cached = uni.getStorageSync('userInfo') || {}
        const name = data.nickname || data.name || data.realName || cached.nickname || cached.name || '用户'
        const enterpriseStatus = String(data.enterpriseStatus || '').toUpperCase()
        this.profile = {
          name,
          avatar: data.avatar || data.avatarUrl || cached.avatar || cached.avatarUrl || '',
          enterpriseApproved: ['APPROVED', 'PASSED', '已通过', '已认证'].includes(enterpriseStatus),
        }
        if (assetSummary) {
          const cents = Number(assetSummary.balance)
          const rewardCents = Number(assetSummary.rewardAmount)
          this.stats.balance = Number.isFinite(cents) ? (cents / 100).toFixed(2) : this.stats.balance
          this.assets = [
            { value: assetSummary.points ?? 0, label: '积分', page: 'points' },
            { value: assetSummary.couponCount ?? 0, label: '券包(个)', page: 'voucher' },
            { value: Number.isFinite(rewardCents) ? (rewardCents / 100).toFixed(2) : '0.00', label: '奖励金(元)', page: 'reward' },
          ]
        }
        if (profileStats) {
          this.stats = {
            ...this.stats,
            integrityScore: profileStats.integrityScore ?? profileStats.creditScore ?? this.stats.integrityScore,
            goodRate: profileStats.goodRate ?? profileStats.positiveRate ?? this.stats.goodRate,
            arrivalRate: profileStats.arrivalRate ?? profileStats.completionRate ?? this.stats.arrivalRate,
            settleRate: profileStats.settleRate ?? profileStats.settlementRate ?? this.stats.settleRate,
            totalPayment: profileStats.totalPayment ?? profileStats.settledAmount ?? this.stats.totalPayment,
            completedOrders: profileStats.completedOrders ?? profileStats.completedCount ?? this.stats.completedOrders,
          }
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
        'signup-notice', 'invite-friend',         'service-chat', 'insurance', 'realname', 
        'personal-info',
        'projects', 'project-detail', 'proj-members', 'proj-attendance',
        'proj-checkin', 'proj-onboard', 'proj-onsite', 'proj-onsite-add', 'proj-settings'
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
      if (tab === 'workbench')
        return uni.showToast({ title: '工作台页面暂未开放', icon: 'none' })
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
  background: #F7F7F7;
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
  background: #F7F7F7;
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

.nav-icons { display: none; }

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
  overflow: hidden;
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

.employer-card { margin: -10px 16px 12px; border-radius: 16px; overflow: hidden; }
.employer-top { background: linear-gradient(135deg,#2D1B4E,#5E35B1 50%,#7B1FA2); padding:16px 18px 14px; color:#fff; }
.employer-top .employer-title-row { margin-bottom:14px; }
.employer-top .employer-badge { color:#fff; }
.employer-top .employer-score { color:#fff; }
.employer-top .employer-stats { background:rgba(255,255,255,.08); border-radius:10px; padding:12px 0; margin:0 0 12px; }
.employer-top .stat-col { border-right:1px solid rgba(255,255,255,.15); }
.employer-top .stat-col:last-child { border-right:0; }
.employer-top .stat-val { color:#fff; font-size:18px; }
.employer-top .employer-footer { color:#fff; }
.account-card,.asset-card { margin: 0 16px 12px; background: #fff; border-radius: 14px; padding: 0; overflow:hidden; }
.unauth-card { padding: 20px 18px; text-align: center; }
.unauth-icon { width: 56px; height: 56px; margin: 0 auto 12px; border-radius: 50%; background: linear-gradient(135deg,#fff3e6,#ffe8cc); display:flex; align-items:center; justify-content:center; }
.unauth-icon image { width: 24px; height: 24px; }
.unauth-title { display:block; margin-bottom:6px; color:#333; font-size:15px; font-weight:600; }
.unauth-desc { display:block; margin-bottom:16px; color:#999; font-size:12px; line-height:1.6; white-space:pre-line; }
.btn-authorize { width:100%; height:44px; margin:0; padding:0; border:0; border-radius:22px; background:linear-gradient(135deg,#ff8c5a,#ff6b35); color:#fff; font-size:14px; font-weight:600; line-height:44px; box-shadow:0 4px 14px rgba(255,107,53,.3); display:flex; align-items:center; justify-content:center; gap:6px; }
.btn-authorize::after { border:0; }
.btn-authorize image { width:16px; height:16px; }
.employer-title-row,.auth-row,.balance-row { display:flex; align-items:center; justify-content:space-between; }
.employer-badge { display:flex; align-items:center; gap:6px; color:#8B4513; font-weight:600; }
.employer-badge image,.employer-score image,.asset-item image { width:16px; height:16px; }
.employer-score { display:flex; align-items:center; gap:5px; color:#888; font-size:12px; }
.score-val { color:#FF6B35; font-size:20px; font-weight:700; }
.employer-stats { display:flex; }
.stat-col { flex:1; text-align:center; display:flex; flex-direction:column; gap:4px; color:#fff; font-size:11px; }
.stat-val { color:#333; font-size:20px; font-weight:700; }
.employer-footer { display:flex; align-items:center; gap:10px; font-size:12px; opacity:.85; }
.employer-footer b { color:#FFD96F; }
.employer-footer .sep { opacity:.4; }
.auth-row { border-bottom:1px solid #F0E6D2; padding:12px 16px; color:#333; font-size:13px; background:#FFF8E7; }
.auth-row em { margin-left:8px; padding:3px 7px; background:#fff2e8; color:#FF6B35; font-size:10px; font-style:normal; border-radius:4px; }
.auth-unbind { color:#999; }
.balance-row { padding:14px 16px; }
.balance-desc { display:block; color:#999; font-size:11px; }
.balance-val { display:block; color:#333; font-size:28px; font-weight:700; margin-top:4px; }
.balance-actions { display:flex; gap:8px; }
.balance-actions button { margin:0; padding:0 10px; height:30px; line-height:28px; border:1px solid #FF6B35; color:#FF6B35; background:#fff; border-radius:15px; font-size:11px; }
.balance-actions button:last-child { color:#fff; background:#FF6B35; }
.asset-card { display:flex; padding:0; }
.asset-item { flex:1; position:relative; display:flex; flex-direction:column; align-items:center; gap:3px; color:#999; font-size:11px; padding:16px 0; border-right:1px solid #f0f0f0; }
.asset-item:last-child { border-right:0; }
.asset-val { color:#333; font-size:19px; font-weight:700; }
.asset-item image { position:absolute; right:8px; top:10px; width:11px; height:11px; }

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
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  background: white;
  margin: 0 16px;
  padding: 20px 12px 12px;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.service-item {
  text-align: center;
}

.service-icon {
  width: 44px;
  height: 44px;
  margin: 0 auto 6px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.service-label {
  font-size: 12px;
  color: #666;
}

.other-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px 8px;
  background: white;
  margin: 12px 16px;
  padding: 20px 12px;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.other-item {
  text-align: center;
}

.other-icon {
  width: 44px;
  height: 44px;
  margin: 0 auto 6px;
  display: flex;
  justify-content: center;
  align-items: center;
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

.status-icons image { width: 16px; height: 16px; }
.nav-icon-item image { width: 16px; height: 16px; }
.user-avatar image { width: 100%; height: 100%; border-radius: 50%; display: block; }
.chevron-icon { width: 12px; height: 12px; }
.switch-btn image { width: 14px; height: 14px; }
.cert-icon image { width: 21px; height: 21px; }
.service-icon image, .other-icon image { width: 22px; height: 22px; }
.tab-icon-wrap image { width: 22px; height: 22px; }
.tab-item.active .tab-icon-wrap image { opacity: 1; }

.entry-card {
  border-radius: 16px;
  padding: 16px 14px;
  margin-bottom: 16px;
  position: relative;
  overflow: hidden;
  min-height: 80px;
  box-shadow: 0 6px 16px rgba(240, 165, 0, 0.22);
}
.entry-card.projects { background: linear-gradient(135deg, #f0a500 0%, #ffc53d 100%); }
.entry-name { font-size: 17px; font-weight: 700; color: #fff; }
.entry-desc { font-size: 11px; color: rgba(255, 255, 255, 0.9); margin-top: 6px; }
.entry-icon {
  position: absolute;
  right: 12px;
  bottom: 8px;
  font-size: 32px;
  color: rgba(255, 255, 255, 0.35);
}
</style>
