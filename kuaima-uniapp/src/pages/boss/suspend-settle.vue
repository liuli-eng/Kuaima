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
      <text class="nav-title">待结算</text>
      <view class="nav-right"></view>
    </view>
    <scroll-view scroll-y class="scroll-area">
      <view class="settle-card" v-for="(item, index) in settleList" :key="index">
        <view class="settle-header">
          <text class="settle-title">{{ item.title }}</text>
          <text class="settle-status">{{ item.status }}</text>
        </view>
        <text class="settle-amount">¥{{ item.amount }}</text>
        <text class="settle-info">零工：{{ item.worker }}</text>
        <text class="settle-info">完成时间：{{ item.time }}</text>
        <button class="action-btn" @click="goSettle(item)">去结算</button>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      settleList: [
        { title: '电商分拣打包工', status: '待结算', amount: '180.00', worker: '张师傅', time: '2026-08-21 18:00' },
        { title: '餐饮服务员', status: '待结算', amount: '150.00', worker: '李阿姨', time: '2026-08-21 18:00' },
        { title: '快递搬运装卸工', status: '待结算', amount: '200.00', worker: '王师傅', time: '2026-08-20 18:00' }
      ]
    }
  },
  methods: {
    goBack() { uni.navigateBack() },
    goSettle(item) { uni.navigateTo({ url: '/pages/boss/settle-confirm' }) }
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
.settle-card { background:#fff; margin:12px 16px; border-radius:12px; padding:16px; box-shadow:0 2px 8px rgba(0,0,0,0.04); }
.settle-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; }
.settle-title { font-size:15px; font-weight:600; color:#333; }
.settle-status { font-size:11px; color:#FA8C16; background:#FFF8E6; padding:2px 8px; border-radius:4px; }
.settle-amount { font-size:24px; font-weight:700; color:#FF6B35; margin-bottom:8px; display:block; }
.settle-info { font-size:12px; color:#999; line-height:1.8; display:block; }
.action-btn { margin-top:12px; padding:10px; background:linear-gradient(135deg,#FF6B35,#FF8C5A); color:white; border:none; border-radius:8px; font-size:13px; font-weight:500; width:100%; }
</style>
