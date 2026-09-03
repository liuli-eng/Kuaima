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
      <text class="nav-title">系统通知</text>
      <view class="nav-right">
        <text>…</text>
      </view>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <view class="notice-item" v-for="(notice, index) in notices" :key="index">
        <text class="notice-time">{{ notice.time }}</text>
        <text class="notice-content">
          <text class="notice-tag" :class="notice.tagClass">{{ notice.tag }}</text>
          {{ notice.content }}
        </text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      notices: [
        { time: '2026-08-21 15:30', tag: '系统', tagClass: 'tag-system', content: '您的企业认证审核已通过，恭喜您获得企业认证标识，将获得更多曝光机会！' },
        { time: '2026-08-20 10:15', tag: '结算', tagClass: 'tag-success', content: '您发布的「电商分拣打包工」岗位工资已完成结算，共计¥1,800元已到账。' },
        { time: '2026-08-19 09:00', tag: '提醒', tagClass: 'tag-warning', content: '您发布的「餐饮服务员」岗位已有5位零工报名，请及时处理报名申请。' },
        { time: '2026-08-18 14:20', tag: '系统', tagClass: 'tag-system', content: '平台升级通知：8月25日凌晨2点将进行系统维护，预计维护1小时，期间无法发布招聘。' },
        { time: '2026-08-17 11:00', tag: '活动', tagClass: 'tag-success', content: '邀请好友注册可获得50元奖励，多邀多得，上不封顶！' }
      ]
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
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

.notice-item {
  background: #fff;
  margin: 12px 16px;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.notice-time {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
  display: block;
}

.notice-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
}

.notice-tag {
  display: inline-block;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  margin-right: 6px;
}

.tag-system { background: #FFF3ED; color: #FF6B35; }
.tag-warning { background: #FFF8E6; color: #FA8C16; }
.tag-success { background: #F6FFED; color: #52C41A; }
</style>
