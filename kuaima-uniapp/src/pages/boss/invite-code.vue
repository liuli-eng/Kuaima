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
      <text class="nav-title">邀请码</text>
      <view style="width:32px;"></view>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <!-- 邀请码卡片 -->
      <view class="code-card">
        <text class="code-label">我的邀请码</text>
        <text class="code-value">{{ inviteCode }}</text>
        <view class="code-btn" @click="copyCode">
          <text>📋</text>
          <text>复制邀请码</text>
        </view>
      </view>

      <!-- 邀请奖励 -->
      <view class="reward-card">
        <text class="reward-title">邀请奖励</text>
        <view class="reward-row">
          <view class="reward-item">
            <text class="num">50</text>
            <text class="label">每邀1人(元)</text>
          </view>
          <view class="reward-item">
            <text class="num">12</text>
            <text class="label">已邀请人数</text>
          </view>
          <view class="reward-item">
            <text class="num">600</text>
            <text class="label">累计奖励(元)</text>
          </view>
        </view>
      </view>

      <!-- 分享方式 -->
      <view class="share-card">
        <text class="share-title">邀请好友</text>
        <view class="share-icons">
          <view class="share-item" @click="shareTo('wechat')">
            <view class="share-icon wechat">
              <text style="font-size:20px;">💬</text>
            </view>
            <text class="share-label">微信</text>
          </view>
          <view class="share-item" @click="shareTo('qq')">
            <view class="share-icon qq">
              <text style="font-size:20px;">🐧</text>
            </view>
            <text class="share-label">QQ</text>
          </view>
          <view class="share-item" @click="shareTo('weibo')">
            <view class="share-icon weibo">
              <text style="font-size:20px;">📢</text>
            </view>
            <text class="share-label">微博</text>
          </view>
          <view class="share-item" @click="shareTo('link')">
            <view class="share-icon link">
              <text style="font-size:20px;">🔗</text>
            </view>
            <text class="share-label">复制</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      inviteCode: 'KMS8X2'
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    copyCode() {
      uni.setClipboardData({
        data: this.inviteCode,
        success: () => {
          uni.showToast({ title: '邀请码已复制', icon: 'success' })
        }
      })
    },
    shareTo(type) {
      const names = { wechat: '微信', qq: 'QQ', weibo: '微博', link: '复制链接' }
      uni.showToast({ title: names[type] + '分享', icon: 'none' })
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

.scroll-area {
  flex: 1;
  overflow-y: auto;
}

.code-card {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  margin: 20px 16px;
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  color: #fff;
}

.code-label {
  font-size: 13px;
  opacity: 0.9;
  display: block;
}

.code-value {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 4px;
  margin: 12px 0;
  display: block;
}

.code-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: rgba(255,255,255,0.25);
  border-radius: 16px;
  font-size: 13px;
}

.reward-card {
  background: #fff;
  margin: 12px 16px;
  border-radius: 12px;
  padding: 16px;
}

.reward-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  display: block;
}

.reward-row {
  display: flex;
  justify-content: space-around;
  text-align: center;
}

.reward-item .num {
  font-size: 22px;
  font-weight: 700;
  color: #FF6B35;
  display: block;
}

.reward-item .label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  display: block;
}

.share-card {
  background: #fff;
  margin: 12px 16px;
  border-radius: 12px;
  padding: 16px;
}

.share-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  display: block;
}

.share-icons {
  display: flex;
  justify-content: space-around;
}

.share-item {
  text-align: center;
}

.share-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 6px;
}

.share-icon.wechat { background: #E8F5E9; color: #07C160; }
.share-icon.qq { background: #E3F2FD; color: #12B7F5; }
.share-icon.weibo { background: #FCE4EC; color: #E6162D; }
.share-icon.link { background: #FFF3ED; color: #FF6B35; }

.share-label {
  font-size: 12px;
  color: #666;
}
</style>
