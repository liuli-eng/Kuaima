<template>
  <view class="container">
    <scroll-view :scroll-y="showHistoryBtn" class="scroll-area" id="scrollArea">
      <view id="scrollContent" class="scroll-content">
        <!-- 头部 -->
        <view
          class="header-bar"
          :style="{
            paddingTop: `${statusBarHeight + 12}px`,
            paddingRight: `${menuSafeRight}px`
          }"
        >
          <view class="title-row">
            <text class="page-title">消息</text>
          </view>
        </view>

        <!-- 消息列表 -->
        <view v-if="loading" class="message-state">消息加载中…</view>
        <view v-else-if="loadError" class="message-state error" @click="loadMessages">加载失败，点击重试</view>
        <view v-else-if="messages.length" class="message-list card-shadow">
          <view v-for="message in messages" :key="message.id" class="message-item" :class="{ unread: !isMessageRead(message) }" @click="handleMessageClick(message)">
            <view class="message-icon" :class="message.type === 'ORDER_APPLY' ? 'icon-green' : 'icon-orange'"><text class="message-icon-text">{{ message.type === 'ORDER_APPLY' ? '＋' : '●' }}</text><view v-if="!isMessageRead(message)" class="badge-dot"></view></view>
            <view class="message-content">
              <text class="message-title">{{ message.title || (message.type === 'ORDER_APPLY' ? '有新的报名待审核' : '系统通知') }}</text>
              <text class="message-desc">{{ message.content || '暂无消息内容' }}</text>
            </view>
            <view class="message-action"><text>›</text></view>
          </view>
        </view>
        <view v-else class="message-state">暂无消息</view>

        <!-- 查看历史消息 -->
        <view v-if="showHistoryBtn" class="view-history">
          <view class="history-btn" @click="navigateTo('history-message')">
            <text>查看历史消息</text>
          </view>
        </view>

        <!-- 底部标语 -->
        <view class="bottom-slogan">
          <text class="slogan-title">招临时工 上快马日结</text>
          <text class="slogan-desc">— 熟练工 上岗快 人靠谱 —</text>
        </view>
      </view>
    </scroll-view>

    <!-- 底部TabBar -->
    <view class="tab-bar">
      <view class="tab-item" @click="switchTab('home')">
        <view class="tab-icon-wrap">
          <image
            class="tab-icon-image"
            src="/static/icons/boss-tabbar/house-gray.svg"
            mode="aspectFit"
          />
        </view>
        <text class="tab-label">首页</text>
      </view>
      <view class="tab-item" @click="switchTab('order')">
        <view class="tab-icon-wrap">
          <image
            class="tab-icon-image"
            src="/static/icons/boss-tabbar/calendar-check-gray.svg"
            mode="aspectFit"
          />
        </view>
        <text class="tab-label">日结订单</text>
      </view>
      <view class="tab-item active" @click="switchTab('message')">
        <view class="tab-icon-wrap">
          <image
            class="tab-icon-image"
            src="/static/icons/boss-tabbar/comment-dots-white.svg"
            mode="aspectFit"
          />
        </view>
        <text class="tab-label">消息</text>
      </view>
      <view class="tab-item" @click="switchTab('profile')">
        <view class="tab-icon-wrap">
          <image
            class="tab-icon-image"
            src="/static/icons/boss-tabbar/face-smile-gray.svg"
            mode="aspectFit"
          />
        </view>
        <text class="tab-label">我的</text>
      </view>
    </view>
  </view>
</template>

<script>
import { listMessages, unreadMessages, readMessage } from '@/api/backend'

export default {
  data() {
    return {
      statusBarHeight: 0,
      menuSafeRight: 16,
      messages: [],
      unreadCount: 0,
      loading: false,
      loadError: false,
      showHistoryBtn: true,
    }
  },
  onLoad() {
    try {
      const info = typeof uni.getWindowInfo === 'function' ? uni.getWindowInfo() : uni.getSystemInfoSync()
      this.statusBarHeight = Number(info.statusBarHeight || 0)
      // #ifdef MP-WEIXIN
      const menu = uni.getMenuButtonBoundingClientRect()
      if (menu?.left) this.menuSafeRight = Math.max(16, info.windowWidth - menu.left + 12)
      // #endif
    } catch (_) {}
  },
  onReady() {
    this.updateHistoryBtnVisibility()
  },
  onShow() {
    this.loadMessages()
  },
  methods: {
    updateHistoryBtnVisibility() {
      this.$nextTick(() => {
        const query = uni.createSelectorQuery().in(this)
        query.select('#scrollArea').boundingClientRect()
        query.select('#scrollContent').boundingClientRect()
        query.select('#scrollArea').scrollOffset()
        query.exec((res) => {
          const area = res?.[0]
          const content = res?.[1]
          const scroll = res?.[2]
          const areaHeight = area?.height
          const contentHeight = content?.height
          if (areaHeight === undefined || contentHeight === undefined) return
          const overflow = contentHeight > areaHeight + 1
          this.showHistoryBtn = overflow
        })
      })
    },
    async loadMessages() {
      if (this.loading) return
      this.loading = true
      this.loadError = false
      const userId = uni.getStorageSync('userId')
      if (!userId) { this.loading = false; return }
      try {
        const [items, unread] = await Promise.all([listMessages(userId, { page: 0, size: 20, role: 'BOSS' }), unreadMessages(userId, 'BOSS')])
        const rows = Array.isArray(items) ? items : items?.records || []
        this.messages = rows
        this.unreadCount = Number(unread || 0)
      } catch (error) {
        this.messages = []
        this.loadError = true
        uni.showToast({ title: error.message || '消息加载失败', icon: 'none' })
      } finally {
        this.loading = false
        this.updateHistoryBtnVisibility()
      }
    },
    isMessageRead(message) {
      return message?.readFlag === true || message?.readFlag === 1 || message?.readFlag === '已读' || message?.readFlag === 'READ'
    },
    async handleMessageClick(message) {
      const userId = uni.getStorageSync('userId')
      if (message?.id && userId && !this.isMessageRead(message)) {
        try {
          await readMessage(message.id, userId)
          message.readFlag = true
        } catch (error) {
          return uni.showToast({ title: error.message || '消息标记已读失败', icon: 'none' })
        }
      }
      if (message?.type === 'ORDER_APPLY' && message?.bizType === 'item' && message?.bizId) {
        return uni.navigateTo({ url: '/pages/boss/signup-notice' })
      }
      this.navigateTo('system-notice')
    },
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

.scroll-area {
  flex: 1;
  min-height: 0;
  width: 100%;
  box-sizing: border-box;
  overflow-y: auto;
  background: #FFF8E6;
}

.scroll-content {
  display: flex;
  flex-direction: column;
}

.message-state {
  margin: 24px 16px;
  padding: 48px 20px;
  border-radius: 12px;
  background: #fff;
  color: #999;
  font-size: 14px;
  text-align: center;
}

.message-state.error {
  color: #ff6b35;
}

.message-item.unread .message-title {
  font-weight: 700;
}

.header-bar {
  background: linear-gradient(180deg, #FFD59E 0%, #FFE4B5 100%);
  padding: 12px 16px 20px;
}

.message-icon-text {
  font-size: 20px;
  line-height: 1;
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
  overflow: hidden;
}

.message-title {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
  line-height: 1.35;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.message-desc {
  display: block;
  font-size: 13px;
  color: #999;
  line-height: 1.35;
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
  min-height: 63px;
  background: rgba(255, 255, 255, 0.98);
  border-top: 0.5px solid rgba(0, 0, 0, 0.05);
  display: flex;
  padding-bottom: env(safe-area-inset-bottom);
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

.tab-icon-image {
  width: 20px;
  height: 20px;
}

.tab-item.active .tab-icon-wrap {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  color: #fff;
  margin-bottom: 2px;
  box-shadow: 0 4px 10px rgba(255, 107, 53, 0.3);
}

.tab-item.active .tab-icon-image {
  width: 21px;
  height: 21px;
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
