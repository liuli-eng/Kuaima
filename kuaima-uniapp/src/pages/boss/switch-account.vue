<template>
  <view class="container">
    <view class="status-bar">
      <text>19:53</text>
      <view class="status-icons">
        <text>📶</text>
        <text>📡</text>
        <text>🔋</text>
      </view>
    </view>
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text>←</text>
      </view>
      <text class="nav-title">更换账号</text>
      <view style="width:32px;"></view>
    </view>
    <scroll-view scroll-y class="scroll-area">
      <view class="account-card">
        <text class="account-title">已登录账号</text>
        <view class="account-item" v-for="(account, index) in accounts" :key="index">
          <view class="account-avatar" :style="{ background: account.avatarBg }">{{ account.initial }}</view>
          <view class="account-info">
            <view class="account-name">
              {{ account.name }}
              <text class="current-badge" v-if="account.isCurrent">当前账号</text>
            </view>
            <text class="account-phone">{{ account.phone }}</text>
          </view>
          <button class="switch-btn" v-if="!account.isCurrent" @click="switchAccount(account)">切换</button>
        </view>
      </view>
      <view class="add-btn" @click="addAccount">
        <text style="margin-right:6px;">+</text>
        <text>添加新账号</text>
      </view>
      <button class="logout-btn" @click="logout">退出登录</button>
      <text class="tips">切换账号将保留各自的订单、消息和设置数据</text>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      accounts: [
        { initial: '晴', name: '晴时见禾', phone: '138****5678', isCurrent: true, avatarBg: 'linear-gradient(135deg, #FF6B35, #FF8C5A)' },
        { initial: '管', name: '企业管理员', phone: '139****1234', isCurrent: false, avatarBg: 'linear-gradient(135deg, #1890FF, #40A9FF)' }
      ]
    }
  },
  methods: {
    goBack() { uni.navigateBack() },
    switchAccount(account) { uni.showToast({ title: `切换到${account.name}`, icon: 'none' }) },
    addAccount() { uni.showToast({ title: '添加新账号', icon: 'none' }) },
    logout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: (res) => { if (res.confirm) { uni.reLaunch({ url: '/pages/boss/home' }) } }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.container { width:100%; height:100vh; background:#f5f5f5; display:flex; flex-direction:column; overflow:hidden; }
.status-bar { height:47px; display:flex; justify-content:space-between; align-items:center; padding:0 28px; font-size:15px; font-weight:600; color:#333; background:#fff; }
.status-icons { display:flex; align-items:center; gap:4px; }
.nav-bar { height:50px; display:flex; align-items:center; justify-content:space-between; padding:0 16px; background:#fff; }
.nav-back { width:32px; height:32px; display:flex; align-items:center; justify-content:center; }
.nav-title { font-size:17px; font-weight:600; color:#333; }
.scroll-area { flex:1; overflow-y:auto; }
.account-card { background:#fff; margin:12px 16px; border-radius:12px; padding:16px; }
.account-title { font-size:15px; font-weight:600; color:#333; margin-bottom:12px; display:block; }
.account-item { display:flex; align-items:center; gap:12px; padding:12px 0; border-bottom:1px solid #f5f5f5; }
.account-item:last-child { border-bottom:none; }
.account-avatar { width:44px; height:44px; border-radius:50%; display:flex; align-items:center; justify-content:center; color:#fff; font-weight:600; flex-shrink:0; }
.account-info { flex:1; }
.account-name { font-size:14px; font-weight:600; color:#333; display:flex; align-items:center; gap:6px; }
.current-badge { font-size:11px; color:#FF6B35; background:#FFF3ED; padding:2px 6px; border-radius:4px; }
.account-phone { font-size:12px; color:#999; margin-top:2px; display:block; }
.switch-btn { padding:6px 12px; font-size:12px; color:#FF6B35; background:#FFF3ED; border-radius:14px; border:none; }
.add-btn { display:flex; align-items:center; justify-content:center; gap:6px; padding:14px; margin:12px 16px; background:#fff; border:1px dashed #ddd; border-radius:10px; color:#666; font-size:14px; }
.logout-btn { margin:24px 16px; padding:13px; background:#fff; color:#FF4D4F; border:none; border-radius:24px; font-size:15px; font-weight:500; width:calc(100% - 32px); }
.tips { text-align:center; padding:0 16px; font-size:12px; color:#bbb; line-height:1.6; display:block; }
</style>
