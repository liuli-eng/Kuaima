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
      <text class="nav-title">邀请好友</text>
      <view class="nav-right">
        <text>…</text>
      </view>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <!-- Banner -->
      <view class="banner">
        <view class="banner-icon">
          <text style="font-size:40px;">🎁</text>
        </view>
        <text class="banner-title">邀请好友 各得50元</text>
        <text class="banner-desc">好友成功注册并完成第一单，您和好友各得50元奖励</text>
      </view>

      <!-- 奖励统计 -->
      <view class="reward-row">
        <view class="reward-item">
          <text class="reward-amount">50</text>
          <text class="reward-label">邀请奖励(元)</text>
        </view>
        <view class="reward-item">
          <text class="reward-amount">12</text>
          <text class="reward-label">已邀请人数</text>
        </view>
        <view class="reward-item">
          <text class="reward-amount">¥600</text>
          <text class="reward-label">累计奖励</text>
        </view>
      </view>

      <!-- 活动规则 -->
      <view class="rule-card">
        <text class="rule-title">活动规则</text>
        <text class="rule-item">邀请好友注册快马日结平台</text>
        <text class="rule-item">好友完成第一单日结后，双方各得50元奖励</text>
        <text class="rule-item">奖励直接进入账户余额，可用于平台消费</text>
        <text class="rule-item">本活动长期有效，多邀多得，上不封顶</text>
        <text class="rule-item">如有作弊行为，平台有权取消奖励资格</text>
      </view>

      <!-- 邀请按钮 -->
      <view class="invite-btns">
        <button class="invite-btn btn-wx" @click="shareWechat">
          <text>💬</text>
          <text>微信分享</text>
        </button>
        <button class="invite-btn btn-link" @click="copyLink">
          <text>🔗</text>
          <text>复制链接</text>
        </button>
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
    shareWechat() {
      uni.showToast({ title: '调起微信分享', icon: 'none' })
    },
    copyLink() {
      uni.setClipboardData({
        data: 'https://kuaima.com/invite?code=KMS8X2',
        success: () => {
          uni.showToast({ title: '邀请链接已复制', icon: 'success' })
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
}

.banner {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  margin: 12px 16px;
  border-radius: 16px;
  padding: 24px;
  color: #fff;
  text-align: center;
}

.banner-icon {
  margin-bottom: 12px;
}

.banner-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 8px;
  display: block;
}

.banner-desc {
  font-size: 13px;
  opacity: 0.9;
  display: block;
}

.reward-row {
  display: flex;
  justify-content: space-around;
  margin: 16px;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
}

.reward-item {
  text-align: center;
}

.reward-amount {
  font-size: 24px;
  font-weight: 700;
  color: #FF6B35;
  display: block;
}

.reward-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  display: block;
}

.rule-card {
  background: #fff;
  margin: 12px 16px;
  border-radius: 12px;
  padding: 16px;
}

.rule-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  display: block;
}

.rule-item {
  font-size: 13px;
  color: #666;
  line-height: 1.8;
  display: block;
  padding-left: 12px;
  position: relative;
}

.rule-item::before {
  content: '•';
  color: #FF6B35;
  position: absolute;
  left: 0;
}

.invite-btns {
  display: flex;
  gap: 12px;
  margin: 16px;
}

.invite-btn {
  flex: 1;
  padding: 14px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: none;
}

.btn-wx {
  background: #07C160;
  color: #fff;
}

.btn-link {
  background: #fff;
  color: #FF6B35;
  border: 1px solid #FF6B35;
}
</style>
