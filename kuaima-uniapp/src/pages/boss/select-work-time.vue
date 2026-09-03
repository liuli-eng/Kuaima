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
      <text class="nav-title">选择工作时间</text>
      <view class="nav-right">
        <text style="color:#ccc;font-size:18px;">❓</text>
      </view>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <text class="section-title">快捷选择</text>
      <text class="section-desc">选择常用时间段，或自定义起止时间</text>

      <!-- 常用时段 -->
      <view class="quick-select">
        <view class="quick-title">
          <text style="color:#FF6B35;">⚡</text>
          <text style="margin-left:8px;">常用时段</text>
        </view>
        <view class="quick-options">
          <text 
            class="quick-chip" 
            :class="{ selected: quickSelected === item }"
            v-for="(item, index) in quickOptions" 
            :key="index"
            @click="selectQuick(item)"
          >{{ item }}</text>
        </view>
      </view>

      <!-- 自定义时间 -->
      <view class="time-card">
        <view class="time-card-header">
          <view class="time-card-title">
            <text style="color:#FF6B35;">🕐</text>
            <text style="margin-left:8px;">自定义时间</text>
          </view>
          <text style="font-size:12px;color:#999;">{{ startTime }} - {{ endTime }}</text>
        </view>
        <view class="time-card-body">
          <view class="time-slot-row">
            <text class="time-label">开始</text>
            <view class="time-picker">
              <picker mode="time" :value="startTime" @change="onStartTimeChange">
                <view class="time-input">{{ startTime }}</view>
              </picker>
              <text class="› time-arrow"></text>
            </view>
          </view>
          <view class="time-slot-row">
            <text class="time-label">结束</text>
            <view class="time-picker">
              <picker mode="time" :value="endTime" @change="onEndTimeChange">
                <view class="time-input">{{ endTime }}</view>
              </picker>
              <text class="› time-arrow"></text>
            </view>
          </view>
        </view>
      </view>

      <!-- 工作时长 -->
      <view class="duration-info">
        <view class="duration-left">
          <view class="duration-icon">
            <text style="font-size:18px;">⏳</text>
          </view>
          <view class="duration-text">
            <text class="label">工作时长</text>
            <text class="value">{{ duration }}</text>
          </view>
        </view>
        <view class="duration-right">
          <text class="label">休息时间</text>
          <text class="value">含1小时午休</text>
        </view>
      </view>

      <!-- 提示 -->
      <view class="notice-bar">
        <text class="ℹ" style="margin-top:2px;flex-shrink:0;"></text>
        <text>建议选择白天工作时段，报名人数更多。夜间工作需额外补贴。</text>
      </view>

      <!-- 底部按钮 -->
      <view class="bottom-bar">
        <view class="btn-group">
          <button class="btn-cancel" @click="goBack">取消</button>
          <button class="btn-confirm" @click="confirmTime">
            <text style="margin-right:8px;">✓</text>
            确认选择
          </button>
        </view>
      </view>

      <view style="height: 20px;"></view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      startTime: '08:00',
      endTime: '18:00',
      quickSelected: null,
      quickOptions: [
        '早班 8:00-18:00',
        '上午 8:00-12:00',
        '下午 13:30-17:30',
        '夜班 18:00-22:00',
        '全天 9小时',
        '全天 12小时'
      ]
    }
  },
  computed: {
    duration() {
      const [sh, sm] = this.startTime.split(':').map(Number)
      const [eh, em] = this.endTime.split(':').map(Number)
      let totalMin = (eh * 60 + em) - (sh * 60 + sm)
      if (totalMin <= 0) totalMin += 24 * 60
      const hours = Math.floor(totalMin / 60)
      const mins = totalMin % 60
      return mins > 0 ? `${hours}小时${mins}分钟` : `${hours}小时`
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    onStartTimeChange(e) {
      this.startTime = e.detail.value
    },
    onEndTimeChange(e) {
      this.endTime = e.detail.value
    },
    selectQuick(item) {
      this.quickSelected = item
      const match = item.match(/(\d{2}:\d{2})-(\d{2}:\d{2})/)
      if (match) {
        this.startTime = match[1]
        this.endTime = match[2]
      }
    },
    confirmTime() {
      const result = `工作时间：${this.startTime} - ${this.endTime}`
      uni.showToast({ title: result, icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
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
  width: 32px;
  text-align: right;
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  padding: 16px 16px 8px;
  display: block;
}

.section-desc {
  font-size: 12px;
  color: #999;
  padding: 0 16px 12px;
  display: block;
}

.quick-select {
  background: #fff;
  margin: 0 12px 12px;
  border-radius: 12px;
  padding: 16px;
}

.quick-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}

.quick-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-chip {
  padding: 8px 14px;
  background: #f5f5f5;
  border-radius: 20px;
  font-size: 13px;
  color: #333;
  border: 1px solid transparent;
}

.quick-chip.selected {
  background: #FFF3ED;
  color: #FF6B35;
  border-color: #FF6B35;
  font-weight: 500;
}

.time-card {
  background: #fff;
  margin: 0 12px 12px;
  border-radius: 12px;
  overflow: hidden;
}

.time-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #f5f5f5;
}

.time-card-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
}

.time-card-body {
  padding: 16px;
}

.time-slot-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.time-slot-row:last-child {
  margin-bottom: 0;
}

.time-label {
  font-size: 13px;
  color: #666;
  min-width: 56px;
}

.time-picker {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-input {
  flex: 1;
  padding: 10px 12px;
  background: #f9f9f9;
  border: 1px solid #eee;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  text-align: center;
}

.time-arrow {
  color: #ccc;
  font-size: 12px;
}

.duration-info {
  background: linear-gradient(135deg, #FFF8F3, #FFF3ED);
  margin: 0 12px 12px;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.duration-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.duration-icon {
  width: 40px;
  height: 40px;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FF6B35;
}

.duration-text .label {
  font-size: 13px;
  color: #666;
  display: block;
}

.duration-text .value {
  font-size: 18px;
  font-weight: 700;
  color: #FF6B35;
  display: block;
}

.duration-right {
  text-align: right;
}

.duration-right .label {
  font-size: 12px;
  color: #999;
  display: block;
}

.duration-right .value {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  display: block;
}

.notice-bar {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 16px;
  background: #FFFBEB;
  margin: 0 12px 12px;
  border-radius: 8px;
  font-size: 12px;
  color: #D97706;
  line-height: 1.5;
}

.bottom-bar {
  background: #fff;
  padding: 12px 16px 30px;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
}

.btn-group {
  display: flex;
  gap: 10px;
}

.btn-cancel {
  flex: 1;
  padding: 14px;
  background: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 500;
}

.btn-confirm {
  flex: 2;
  padding: 14px;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: #fff;
  border: none;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
