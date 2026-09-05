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
      <text class="nav-title">未接来电</text>
      <view class="nav-right">
        <text>…</text>
      </view>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <!-- 未接来电列表 -->
      <view class="call-item" v-for="(call, index) in calls" :key="index">
        <view class="call-header">
          <view class="call-avatar" :style="{ background: call.avatarBg }">
            {{ call.initial }}
          </view>
          <view class="call-info">
            <text class="call-name">{{ call.name }}</text>
            <text class="call-time">{{ call.time }}</text>
          </view>
        </view>
        <text class="call-reason">{{ call.reason }}</text>
        <view class="call-actions">
          <button class="btn-primary" @click="callBack(call)">
            <text style="margin-right:6px;">📞</text>
            <text>回拨</text>
          </button>
          <button class="btn-secondary" @click="sendMessage(call)">
            <text style="margin-right:6px;">💬</text>
            <text>消息</text>
          </button>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      calls: [
        { initial: '张', name: '张师傅', time: '2026-08-21 14:20', reason: '报名「电商分拣打包工」岗位咨询详情', avatarBg: 'linear-gradient(135deg, #FF6B35, #FF8C5A)' },
        { initial: '李', name: '李阿姨', time: '2026-08-20 09:15', reason: '报名「餐饮服务员」岗位确认面试时间', avatarBg: 'linear-gradient(135deg, #52C41A, #73D13D)' },
        { initial: '王', name: '王师傅', time: '2026-08-19 16:40', reason: '咨询「快递搬运装卸工」岗位工作地点', avatarBg: 'linear-gradient(135deg, #1890FF, #40A9FF)' }
      ]
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    callBack(call) {
      uni.makePhoneCall({ phoneNumber: '138****5678' })
    },
    sendMessage(call) {
      uni.showToast({ title: `发送消息给${call.name}`, icon: 'none' })
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

.call-item {
  background: #fff;
  margin: 12px 16px;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.call-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.call-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
}

.call-info {
  flex: 1;
}

.call-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  display: block;
}

.call-time {
  font-size: 12px;
  color: #999;
  display: block;
}

.call-reason {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 12px;
  display: block;
}

.call-actions {
  display: flex;
  gap: 10px;
}

.btn-primary {
  flex: 1;
  padding: 10px;
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-secondary {
  flex: 1;
  padding: 10px;
  background: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
