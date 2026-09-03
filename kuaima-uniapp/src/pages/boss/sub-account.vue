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
      <text class="nav-title">开通子账号</text>
      <view class="nav-right">
        <text style="color:#999;">❓</text>
      </view>
    </view>
    <scroll-view scroll-y class="scroll-area">
      <view class="info-card">
        <text class="info-title">子账号信息</text>
        <view class="info-row"><text class="info-label">子账号数量</text><text class="info-value">0 / 3</text></view>
        <view class="info-row"><text class="info-label">可分配角色</text><text class="info-value">管理员、操作员</text></view>
      </view>
      <view class="info-card">
        <text class="info-title">添加子账号</text>
        <view class="form-item"><text class="form-label">手机号</text><input class="form-input" placeholder="请输入手机号" v-model="phone" /></view>
        <view class="form-item"><text class="form-label">验证码</text><input class="form-input" placeholder="请输入验证码" v-model="code" /></view>
        <view class="form-item" style="border:none;"><text class="form-label">角色权限</text></view>
        <view class="role-select">
          <text class="role-tag" :class="{ active: roles.includes(role) }" v-for="role in roleList" :key="role" @click="toggleRole(role)">{{ role }}</text>
        </view>
      </view>
      <button class="submit-btn" @click="submit">确认开通</button>
      <view class="empty-state">
        <view class="empty-icon"><text style="font-size:48px;color:#ddd;">➕</text></view>
        <text class="empty-text">暂无子账号</text>
        <text class="empty-text" style="margin-top:4px;">最多可开通3个子账号</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      phone: '',
      code: '',
      roles: ['全部权限'],
      roleList: ['查看订单', '发布岗位', '结算工资', '全部权限']
    }
  },
  methods: {
    goBack() { uni.navigateBack() },
    toggleRole(role) {
      const idx = this.roles.indexOf(role)
      if (idx >= 0) { this.roles.splice(idx, 1) } else { this.roles.push(role) }
    },
    submit() {
      if (!this.phone || !this.code) { uni.showToast({ title: '请填写完整信息', icon: 'none' }); return }
      uni.showToast({ title: '提交成功', icon: 'success' })
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
.nav-right { display:flex; gap:14px; color:#333; }
.scroll-area { flex:1; overflow-y:auto; }
.info-card { background:#fff; margin:12px 16px; border-radius:12px; padding:16px; }
.info-title { font-size:15px; font-weight:600; color:#333; margin-bottom:12px; display:block; }
.info-row { display:flex; justify-content:space-between; padding:10px 0; border-bottom:1px solid #f5f5f5; font-size:13px; }
.info-row:last-child { border-bottom:none; }
.info-label { color:#666; }
.info-value { color:#333; }
.form-item { display:flex; align-items:center; padding:12px 0; border-bottom:1px solid #f5f5f5; }
.form-label { font-size:14px; color:#333; width:100px; }
.form-input { flex:1; border:none; outline:none; background:transparent; font-size:14px; color:#666; }
.form-input::placeholder { color:#ccc; }
.role-select { display:flex; gap:8px; flex-wrap:wrap; margin-top:10px; }
.role-tag { padding:6px 12px; font-size:12px; color:#666; background:#f5f5f5; border-radius:14px; border:1px solid transparent; }
.role-tag.active { background:#FFF3ED; color:#FF6B35; border-color:#FF6B35; }
.submit-btn { width:calc(100% - 32px); padding:13px; background:linear-gradient(135deg,#FF6B35,#FF8C5A); color:#fff; border:none; border-radius:24px; font-size:15px; font-weight:600; margin:24px 16px 0; }
.empty-state { text-align:center; padding:60px 40px; }
.empty-text { font-size:14px; color:#999; display:block; }
</style>
