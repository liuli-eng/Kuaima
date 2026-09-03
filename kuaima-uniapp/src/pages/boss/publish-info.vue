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
      <text class="nav-title">发布招工</text>
      <view style="width:32px;"></view>
    </view>

    <!-- 步骤条 -->
    <view class="stepper">
      <view class="step-dot">
        <text style="font-size:9px;">✓</text>
      </view>
      <text class="step-label">基础信息</text>
      <view class="step-line"></view>
      <view class="step-dot-pending">
        <text style="font-size:6px;">●</text>
      </view>
      <text class="step-label-pending">招工需求</text>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <!-- 基础信息 -->
      <view class="form-section">
        <view class="form-item" @click="editJob">
          <text class="form-label">工种</text>
          <text class="form-value">{{ jobValue }}</text>
          <text class="› form-arrow"></text>
        </view>
        <view class="form-item" @click="navigateTo('task-content')">
          <text class="form-label">干活内容</text>
          <text class="form-value" :class="{ placeholder: !workContent }">{{ workContent || '请选择' }}</text>
          <text class="› form-arrow"></text>
        </view>
        <view class="form-item" @click="navigateTo('task-content')">
          <text class="form-label">任务详情</text>
          <text class="form-value placeholder">电子厂普工、焊锡工 未填写详情</text>
          <text class="› form-arrow"></text>
        </view>
        <view class="form-item" @click="navigateTo('gender-age')">
          <text class="form-label">性别年龄</text>
          <text class="form-value">{{ genderAgeValue }}</text>
          <text class="› form-arrow"></text>
        </view>
      </view>

      <!-- 工作日期 -->
      <view class="date-section">
        <text class="date-section-title">工作日期</text>
        <view class="date-options">
          <view 
            class="date-chip" 
            :class="{ selected: date.selected }"
            v-for="(date, index) in dates" 
            :key="index"
            @click="toggleDate(index)"
          >
            <text class="weekday">{{ date.weekday }}</text>
            <text class="date">{{ date.date }}</text>
          </view>
        </view>
      </view>

      <!-- 工作时间和地点 -->
      <view class="form-section">
        <view class="form-item" @click="navigateTo('select-work-time')">
          <text class="form-label">工作时间</text>
          <text class="form-value link">选择工作时间 ›</text>
        </view>
        <view class="form-item" @click="navigateTo('location')">
          <text class="form-label">干活地点</text>
          <text class="form-value link">设置干活地点 <text style="color:#FF6B35;font-size:14px;margin-left:4px;">📍</text></text>
        </view>
      </view>

      <view style="height:20px;"></view>
    </scroll-view>

    <!-- 浮动客服 -->
    <view class="service-fab" @click="navigateTo('service-chat')">
      <text style="font-size:18px;color:#FF6B35;margin-bottom:2px;">🎧</text>
      <text style="font-size:10px;color:#FF6B35;">客服</text>
    </view>

    <!-- 底部按钮 -->
    <view class="bottom-bar">
      <button class="submit-btn" @click="nextStep">下一步</button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      jobValue: '普工、焊锡工',
      workContent: '',
      genderAgeValue: '性别不限、18岁~不限',
      dates: [
        { weekday: '本周五', date: '8月21日', selected: true },
        { weekday: '明周六', date: '8月22日', selected: true },
        { weekday: '后周日', date: '8月23日', selected: false }
      ]
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    navigateTo(page) {
      uni.navigateTo({ url: `/pages/boss/${page}` })
    },
    editJob() {
      uni.navigateTo({ url: '/pages/boss/all-jobs' })
    },
    toggleDate(index) {
      this.dates[index].selected = !this.dates[index].selected
    },
    nextStep() {
      if (!this.workContent) {
        uni.showToast({ title: '请先选择干活内容', icon: 'none' })
        return
      }
      uni.navigateTo({ url: '/pages/boss/recruit-demand' })
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

.stepper {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 14px 16px;
  background: #fff;
  gap: 4px;
}

.step-dot {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
}

.step-label {
  font-size: 13px;
  color: #FF6B35;
  font-weight: 500;
}

.step-line {
  width: 30px;
  height: 2px;
  background: #FF6B35;
}

.step-label-pending {
  font-size: 13px;
  color: #999;
}

.step-dot-pending {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #e0e0e0;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
}

.form-section {
  margin-top: 10px;
  background: #fff;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f5f5f5;
}

.form-item:last-child {
  border-bottom: none;
}

.form-label {
  font-size: 15px;
  color: #333;
  font-weight: 500;
  min-width: 80px;
}

.form-value {
  flex: 1;
  text-align: right;
  font-size: 14px;
  color: #333;
  margin-right: 4px;
}

.form-value.placeholder {
  color: #999;
}

.form-value.link {
  color: #FF6B35;
}

.form-arrow {
  color: #ccc;
  font-size: 12px;
}

.date-section {
  padding: 16px;
  background: #fff;
  margin-top: 10px;
}

.date-section-title {
  font-size: 15px;
  color: #333;
  font-weight: 500;
  margin-bottom: 12px;
  display: block;
}

.date-options {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.date-chip {
  padding: 8px 12px;
  background: #f5f5f5;
  border-radius: 8px;
  text-align: center;
  font-size: 12px;
  color: #333;
  min-width: 70px;
}

.date-chip.selected {
  background: #FFF3ED;
  color: #FF6B35;
}

.date-chip .weekday {
  font-weight: 500;
  display: block;
}

.date-chip .date {
  color: #999;
  font-size: 11px;
  display: block;
}

.service-fab {
  position: absolute;
  right: 16px;
  bottom: 100px;
  width: 48px;
  height: 48px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 20;
}

.bottom-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 12px 16px 30px;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
}

.submit-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: #fff;
  border: none;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 600;
}
</style>
