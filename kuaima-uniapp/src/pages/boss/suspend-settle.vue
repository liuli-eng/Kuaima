<template>
  <view class="container">
    <view class="nav-bar" :style="{ paddingTop: `${statusBarHeight}px`, height: `${50 + statusBarHeight}px` }">
      <view class="nav-back" @click="goBack">
        <text>‹</text>
      </view>
      <text class="nav-title">待结算</text>
      <view class="nav-right"></view>
    </view>
    <scroll-view scroll-y class="scroll-area">
      <view class="summary-card">
        <view class="summary-header">
          <view><text class="summary-label">待结算总金额</text><text class="summary-amount">¥{{ totalAmount.toFixed(2) }}</text><text class="summary-desc">共{{ orders.length }}笔订单 · 涉及{{ totalWorkers }}位零工</text></view>
          <text class="status-tag">处理中</text>
        </view>
      </view>
      <view class="select-all-bar">
        <view class="select-all-left" @click="toggleSelectAll"><view class="checkbox" :class="{ checked: allSelected }"><text v-if="allSelected">✓</text></view><text>全选（<text class="count">{{ orders.length }}</text>笔）</text></view>
        <text class="select-all-btn" @click="toggleSelectAll">全部选择</text>
      </view>
      <view class="order-card" v-for="item in orders" :key="item.id">
        <view class="checkbox" :class="{ checked: selectedIds.includes(item.id) }" @click="toggleOrder(item.id)"><text v-if="selectedIds.includes(item.id)">✓</text></view>
        <view class="order-content">
          <view class="order-header"><text class="order-job">{{ item.date }} {{ item.job }}</text><text class="order-amount">¥{{ item.amount.toFixed(2) }}</text></view>
          <text class="order-info">♟ {{ item.workerCount }}位零工</text>
          <text class="order-workers">{{ item.workers.join('、') }} 等{{ item.workerCount }}人</text>
          <text class="order-status" :class="item.status">{{ item.statusText }}</text>
        </view>
      </view>
      <view class="empty-state" v-if="!orders.length"><text class="empty-icon">¥</text><text>暂无待结算订单</text></view>
    </scroll-view>
    <view class="bottom-bar"><view class="bar-info"><text class="bar-label">已选金额</text><text class="bar-amount">¥{{ selectedAmount.toFixed(2) }}</text><text class="bar-count">{{ selectedIds.length ? `已选${selectedIds.length}笔订单` : '未选择任何订单' }}</text></view><button class="settle-btn" :disabled="!selectedIds.length" @click="settleSelected">结算所选{{ selectedIds.length ? `（${selectedIds.length}笔）` : '' }}</button></view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      statusBarHeight: 0,
      selectedIds: [],
      orders: [
        { id: 1, job: '电商分拣打包工', date: '8月21日', workers: ['张师傅', '李师傅', '王师傅'], workerCount: 3, amount: 800, status: 'waiting', statusText: '待结算' },
        { id: 2, job: '餐饮服务员', date: '8月20日', workers: ['陈师傅'], workerCount: 1, amount: 600, status: 'waiting', statusText: '待结算' },
        { id: 3, job: '快递搬运装卸工', date: '8月19日', workers: ['刘师傅', '赵师傅'], workerCount: 2, amount: 700, status: 'partial', statusText: '部分结算' },
        { id: 4, job: '冷库分拣员', date: '8月18日', workers: ['孙师傅'], workerCount: 1, amount: 900, status: 'waiting', statusText: '待结算' },
        { id: 5, job: '装配工', date: '8月17日', workers: ['吴师傅'], workerCount: 1, amount: 600, status: 'waiting', statusText: '待结算' }
      ]
    }
  },
  computed: {
    totalAmount() { return this.orders.reduce((sum, item) => sum + item.amount, 0) },
    totalWorkers() { return this.orders.reduce((sum, item) => sum + item.workerCount, 0) },
    allSelected() { return this.orders.length > 0 && this.selectedIds.length === this.orders.length },
    selectedAmount() { return this.orders.filter(item => this.selectedIds.includes(item.id)).reduce((sum, item) => sum + item.amount, 0) }
  },
  onLoad() {
    try { const info = typeof uni.getWindowInfo === 'function' ? uni.getWindowInfo() : uni.getSystemInfoSync(); this.statusBarHeight = Number(info.statusBarHeight || 0) } catch (_) {}
  },
  methods: {
    goBack() { uni.navigateBack() },
    toggleOrder(id) { const index = this.selectedIds.indexOf(id); if (index >= 0) this.selectedIds.splice(index, 1); else this.selectedIds.push(id) },
    toggleSelectAll() { this.selectedIds = this.allSelected ? [] : this.orders.map(item => item.id) },
    settleSelected() { if (!this.selectedIds.length) return; uni.navigateTo({ url: `/pages/boss/settle-confirm?amount=${this.selectedAmount.toFixed(2)}&count=${this.selectedIds.length}&ids=${this.selectedIds.join(',')}` }) }
  }
}
</script>

<style lang="scss" scoped>
.container { width:100%; height:100vh; background:#f5f5f5; display:flex; flex-direction:column; overflow:hidden; }
.nav-bar { display:flex; align-items:center; justify-content:space-between; padding:0 16px; box-sizing:border-box; background:#fff; flex-shrink:0; }
.nav-back { width:32px; height:32px; display:flex; align-items:center; justify-content:center; font-size:24px; color:#333; }
.nav-title { font-size:17px; font-weight:600; color:#333; }
.scroll-area { flex:1; height:0; min-height:0; overflow-y:auto; padding-bottom:90px; }
.summary-card,.select-all-bar,.order-card { background:#fff; margin:12px 16px; border-radius:12px; padding:16px; box-sizing:border-box; }
.summary-header,.order-header,.select-all-left { display:flex; align-items:center; }
.summary-header,.order-header { justify-content:space-between; }
.summary-label,.summary-desc,.bar-label,.bar-count { display:block; color:#999; font-size:12px; }
.summary-amount { display:block; color:#333; font-size:24px; font-weight:700; margin-top:4px; }
.summary-desc { margin-top:4px; }
.status-tag,.order-status { color:#FA8C16; background:#FFF8E6; padding:4px 10px; border-radius:10px; font-size:12px; }
.select-all-bar { display:flex; justify-content:space-between; align-items:center; padding:12px 16px; }
.select-all-left { gap:8px; color:#666; font-size:14px; }
.count,.select-all-btn { color:#FF6B35; font-weight:600; }
.select-all-btn { font-size:13px; }
.checkbox { width:20px; height:20px; border:2px solid #ddd; border-radius:50%; display:flex; align-items:center; justify-content:center; color:#fff; font-size:12px; flex-shrink:0; }
.checkbox.checked { border-color:#FF6B35; background:#FF6B35; }
.order-card { display:flex; gap:12px; align-items:flex-start; padding:14px 16px; }
.order-content { flex:1; min-width:0; }
.order-job { font-size:14px; font-weight:600; color:#333; }
.order-amount { font-size:16px; font-weight:700; color:#FF6B35; }
.order-info,.order-workers { display:block; color:#999; font-size:12px; margin-top:6px; }
.order-workers { color:#666; }
.order-status { display:inline-block; margin-top:6px; padding:2px 8px; font-size:11px; }
.bottom-bar { position:absolute; bottom:0; left:0; right:0; min-height:70px; background:#fff; border-top:1px solid #f0f0f0; display:flex; align-items:center; padding:10px 16px calc(10px + env(safe-area-inset-bottom)); gap:12px; box-sizing:border-box; z-index:20; }
.bar-info { flex:1; min-width:0; }
.bar-amount { display:block; color:#FF6B35; font-size:20px; font-weight:700; }
.settle-btn { min-width:140px; padding:12px 20px; background:linear-gradient(135deg,#FF6B35,#FF8C5A); color:#fff; border:none; border-radius:24px; font-size:15px; font-weight:600; margin:0; line-height:1.4; }
.settle-btn:disabled { opacity:.5; }
.settle-btn::after { border:none; }
.empty-state { text-align:center; padding:80px 40px; color:#999; font-size:14px; }
.empty-icon { display:block; font-size:48px; color:#ddd; margin-bottom:12px; }
</style>
