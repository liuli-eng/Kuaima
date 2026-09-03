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
      <text class="nav-title">报名通知</text>
      <view class="nav-right">
        <text>…</text>
      </view>
    </view>
    <scroll-view scroll-y class="scroll-area">
      <view class="stats-card">
        <view class="stat-item">
          <text class="stat-num">{{ stats.pending }}</text>
          <text class="stat-label">待处理</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ stats.approved }}</text>
          <text class="stat-label">已通过</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ stats.rejected }}</text>
          <text class="stat-label">已拒绝</text>
        </view>
      </view>
      <view class="apply-item" v-for="(item, index) in applies" :key="index">
        <view class="apply-header">
          <view class="apply-avatar" :style="{ background: item.avatarBg }">{{ item.initial }}</view>
          <view class="apply-info">
            <view class="apply-name">{{ item.name }}<text class="apply-badge">{{ item.tag }}</text></view>
            <text class="apply-meta">报名时间：{{ item.time }}</text>
          </view>
        </view>
        <text class="apply-job">应聘岗位：<text style="font-weight:600;">{{ item.job }}</text> · {{ item.date }} · {{ item.count }}人</text>
        <view class="apply-actions">
          <button class="btn-approve" @click="approve(item)">通过</button>
          <button class="btn-reject" @click="reject(item)">拒绝</button>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      stats: { pending: 3, approved: 12, rejected: 2 },
      applies: [
        { initial: '赵', name: '赵师傅', tag: '熟练工', time: '2026-08-21 14:30', job: '电商分拣打包工', date: '8月22日 08:00', count: 3, avatarBg: 'linear-gradient(135deg, #52C41A, #73D13D)' },
        { initial: '孙', name: '孙阿姨', tag: '新零工', time: '2026-08-21 11:20', job: '餐饮服务员', date: '8月22日 10:00', count: 2, avatarBg: 'linear-gradient(135deg, #1890FF, #40A9FF)' },
        { initial: '周', name: '周师傅', tag: '熟练工', time: '2026-08-20 16:45', job: '快递搬运装卸工', date: '8月21日 07:00', count: 4, avatarBg: 'linear-gradient(135deg, #FA8C16, #FFC53D)' }
      ]
    }
  },
  methods: {
    goBack() { uni.navigateBack() },
    approve(item) { uni.showToast({ title: `已通过${item.name}`, icon: 'success' }) },
    reject(item) { uni.showToast({ title: `已拒绝${item.name}`, icon: 'none' }) }
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
.nav-right { display:flex; gap:14px; color:#333; }
.scroll-area { flex:1; overflow-y:auto; }
.stats-card { background:linear-gradient(135deg,#FF6B35,#FF8C5A); margin:12px 16px; border-radius:12px; padding:16px; color:#fff; display:flex; justify-content:space-around; }
.stat-item { text-align:center; }
.stat-num { font-size:24px; font-weight:700; display:block; }
.stat-label { font-size:12px; opacity:0.9; margin-top:4px; display:block; }
.apply-item { background:#fff; margin:12px 16px; border-radius:12px; padding:16px; box-shadow:0 2px 8px rgba(0,0,0,0.04); }
.apply-header { display:flex; align-items:center; gap:10px; margin-bottom:10px; }
.apply-avatar { width:44px; height:44px; border-radius:50%; display:flex; align-items:center; justify-content:center; color:#fff; font-weight:600; }
.apply-info { flex:1; }
.apply-name { font-size:15px; font-weight:600; color:#333; display:flex; align-items:center; gap:6px; }
.apply-badge { font-size:11px; padding:2px 6px; background:#FFF3ED; color:#FF6B35; border-radius:4px; }
.apply-meta { font-size:12px; color:#999; margin-top:2px; }
.apply-job { font-size:13px; color:#666; line-height:1.5; margin-bottom:12px; display:block; }
.apply-actions { display:flex; gap:10px; }
.btn-approve { flex:1; padding:10px; background:linear-gradient(135deg,#FF6B35,#FF8C5A); color:white; border:none; border-radius:8px; font-size:13px; font-weight:500; }
.btn-reject { flex:1; padding:10px; background:#fff; color:#666; border:1px solid #ddd; border-radius:8px; font-size:13px; font-weight:500; }
</style>
