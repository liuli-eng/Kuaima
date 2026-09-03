<template>
  <view class="container">
    <!-- 状态栏 -->
    <view class="status-bar">
      <text>19:53</text>
      <view class="status-icons">
        <text class="fa-solid fa-signal"></text>
        <text class="fa-solid fa-wifi"></text>
        <text class="fa-solid fa-battery-full"></text>
      </view>
    </view>

    <view class="page-bg">
      <!-- 导航栏 -->
      <view class="nav-bar">
        <view class="nav-back" @click="goBack">
          <text class="fa-solid fa-chevron-left"></text>
        </view>
        <text class="nav-title">招工设置</text>
        <view class="nav-right">
          <text class="fa-solid fa-circle-question" style="color:#CCC;font-size:36rpx;"></text>
        </view>
      </view>

      <scroll-view scroll-y class="scroll-area">
        <!-- 报名设置 -->
        <view class="group">
          <text class="group-title">
            <text class="fa-solid fa-user-check" style="font-size:24rpx;margin-right:8rpx;"></text>
            报名设置
          </text>
          <view class="card">
            <view class="icon-row">
              <view class="icon-box orange">
                <text class="fa-solid fa-users" style="font-size:32rpx;"></text>
              </view>
              <view class="icon-info">
                <text class="icon-title">报名方式</text>
                <text class="icon-desc">零工报名时需要审核</text>
              </view>
              <view class="seg-control">
                <text class="seg-item" :class="{ on: signMode === 'auto' }" @click="signMode = 'auto'">自动通过</text>
                <text class="seg-item" :class="{ on: signMode === 'manual' }" @click="signMode = 'manual'">手动审核</text>
              </view>
            </view>
            <view class="icon-row">
              <view class="icon-box blue">
                <text class="fa-solid fa-phone" style="font-size:32rpx;"></text>
              </view>
              <view class="icon-info">
                <text class="icon-title">报名电话通知</text>
                <text class="icon-desc">零工报名后自动发送短信通知</text>
              </view>
              <view class="toggle-switch" :class="{ on: phoneNotify }" @click="phoneNotify = !phoneNotify"></view>
            </view>
          </view>
        </view>

        <!-- 结算设置 -->
        <view class="group">
          <text class="group-title">
            <text class="fa-solid fa-coins" style="font-size:24rpx;margin-right:8rpx;"></text>
            结算设置
          </text>
          <view class="card">
            <view class="nav-row" @click="selectSettleMode">
              <text class="label">结算方式</text>
              <view class="right">
                <text>{{ settleMode }}</text>
                <text class="fa-solid fa-chevron-right" style="color:#CCC;font-size:24rpx;margin-left:8rpx;"></text>
              </view>
            </view>
          </view>
        </view>

        <!-- 通知设置 -->
        <view class="group">
          <text class="group-title">
            <text class="fa-solid fa-bell" style="font-size:24rpx;margin-right:8rpx;"></text>
            通知设置
          </text>
          <view class="card">
            <view class="icon-row">
              <view class="icon-box orange">
                <text class="fa-solid fa-bullhorn" style="font-size:32rpx;"></text>
              </view>
              <view class="icon-info">
                <text class="icon-title">零工报名通知</text>
                <text class="icon-desc">零工报名时推送通知给老板</text>
              </view>
              <view class="toggle-switch" :class="{ on: signNotify }" @click="signNotify = !signNotify"></view>
            </view>
            <view class="icon-row">
              <view class="icon-box blue">
                <text class="fa-solid fa-circle-check" style="font-size:32rpx;"></text>
              </view>
              <view class="icon-info">
                <text class="icon-title">开工提醒</text>
                <text class="icon-desc">开工前1小时推送提醒</text>
              </view>
              <view class="toggle-switch" :class="{ on: startRemind }" @click="startRemind = !startRemind"></view>
            </view>
            <view class="icon-row">
              <view class="icon-box green">
                <text class="fa-solid fa-money-check-dollar" style="font-size:32rpx;"></text>
              </view>
              <view class="icon-info">
                <text class="icon-title">结算通知</text>
                <text class="icon-desc">结算完成后通知老板</text>
              </view>
              <view class="toggle-switch" :class="{ on: settleNotify }" @click="settleNotify = !settleNotify"></view>
            </view>
          </view>
        </view>

        <view style="height: 32rpx;"></view>
      </scroll-view>

      <!-- 底部按钮 -->
      <view class="bottom-bar">
        <button class="save-btn" @click="saveSettings">保存设置</button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      signMode: 'auto',
      phoneNotify: true,
      settleMode: '日结',
      signNotify: true,
      startRemind: true,
      settleNotify: true
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    selectSettleMode() {
      uni.showActionSheet({
        itemList: ['日结', '周结', '月结', '完工结算'],
        success: (res) => {
          const modes = ['日结', '周结', '月结', '完工结算']
          this.settleMode = modes[res.tapIndex]
        }
      })
    },
    saveSettings() {
      uni.showToast({ title: '设置保存成功！', icon: 'success' })
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

.status-bar {
  height: 94rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 56rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  background: #fff;
}

.status-icons {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.page-bg {
  background: #fff;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 32rpx;
  background: white;
  flex-shrink: 0;
}

.nav-back {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #333;
  font-size: 40rpx;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #333;
}

.nav-right {
  width: 64rpx;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
  background: #F7F8FA;
}

.group {
  margin-top: 24rpx;
}

.group-title {
  font-size: 26rpx;
  color: #999;
  padding: 24rpx 32rpx 12rpx;
  display: flex;
  align-items: center;
}

.card {
  background: #fff;
  margin: 0 24rpx;
  border-radius: 24rpx;
  overflow: hidden;
}

.icon-row {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 24rpx 32rpx;
  border-bottom: 2rpx solid #f5f5f5;
}

.icon-row:last-child {
  border-bottom: none;
}

.icon-box {
  width: 72rpx;
  height: 72rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-box.orange { background: #FFF3ED; color: #FF6B35; }
.icon-box.blue { background: #EBF5FF; color: #1677FF; }
.icon-box.green { background: #E8F8EF; color: #52C41A; }

.icon-info {
  flex: 1;
}

.icon-title {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.icon-desc {
  font-size: 24rpx;
  color: #999;
  margin-top: 4rpx;
}

.seg-control {
  display: flex;
  background: #F5F5F5;
  border-radius: 12rpx;
  padding: 6rpx;
}

.seg-item {
  padding: 12rpx 24rpx;
  font-size: 26rpx;
  color: #666;
  border-radius: 8rpx;
}

.seg-item.on {
  background: #fff;
  color: #FF6B35;
  font-weight: 500;
  box-shadow: 0 2rpx 4rpx rgba(0,0,0,0.1);
}

.toggle-switch {
  width: 88rpx;
  height: 48rpx;
  background: #E0E0E0;
  border-radius: 24rpx;
  position: relative;
  transition: background 0.3s;
  flex-shrink: 0;
}

.toggle-switch.on {
  background: #FF6B35;
}

.toggle-switch::after {
  content: '';
  width: 40rpx;
  height: 40rpx;
  background: #fff;
  border-radius: 50%;
  position: absolute;
  top: 4rpx;
  left: 4rpx;
  transition: left 0.3s;
  box-shadow: 0 4rpx 8rpx rgba(0,0,0,0.15);
}

.toggle-switch.on::after {
  left: 44rpx;
}

.nav-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 32rpx;
  border-bottom: 2rpx solid #f5f5f5;
}

.nav-row .label {
  font-size: 28rpx;
  color: #333;
}

.nav-row .right {
  display: flex;
  align-items: center;
  font-size: 26rpx;
  color: #999;
}

.bottom-bar {
  background: #fff;
  padding: 24rpx 32rpx 60rpx;
  border-top: 2rpx solid #f0f0f0;
  flex-shrink: 0;
}

.save-btn {
  width: 100%;
  height: 92rpx;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: #fff;
  border: none;
  border-radius: 46rpx;
  font-size: 30rpx;
  font-weight: 600;
}
</style>
