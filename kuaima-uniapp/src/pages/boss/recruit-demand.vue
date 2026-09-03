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

    <view class="page-bg">
      <!-- 导航栏 -->
      <view class="nav-bar">
        <view class="nav-back" @click="goBack">
          <text>←</text>
        </view>
        <text class="nav-title">发布招工</text>
        <view class="nav-placeholder"></view>
      </view>

      <!-- 步骤条 -->
      <view class="stepper">
        <view class="step active">
          <view class="dot">
            <text style="font-size:10px;">✓</text>
          </view>
          <text>基础信息</text>
        </view>
        <view class="step-line active"></view>
        <view class="step active">
          <view class="dot">2</view>
          <text>招工需求</text>
        </view>
      </view>

      <scroll-view scroll-y class="scroll-area">
        <!-- 需求人数 -->
        <view class="section-card">
          <view class="section-header">
            <text class="section-title">需求人数</text>
          </view>
          <view class="counter-row">
            <view class="counter-btn" @click="changeCount(-1)">
              <text>−</text>
            </view>
            <view>
              <text class="counter-value">{{ count }}</text>
              <text class="counter-unit">人</text>
            </view>
            <view class="counter-btn active" @click="changeCount(1)">
              <text>+</text>
            </view>
          </view>
        </view>

        <!-- 工价设置 -->
        <view class="section-card">
          <view class="section-header">
            <text class="section-title">工价设置</text>
          </view>
          <view class="tab-group">
            <text class="tab-item" :class="{ active: payType === 'hourly' }" @click="payType = 'hourly'">计时</text>
            <text class="tab-item" :class="{ active: payType === 'piece' }" @click="payType = 'piece'">计件</text>
          </view>

          <!-- 计时面板 -->
          <view v-if="payType === 'hourly'">
            <view class="form-row" @click="setSalary">
              <view class="form-value placeholder">
                <text>{{ hasSalary ? salary + '元/小时' : '请设置工价' }}</text>
                <text class="› arrow"></text>
              </view>
            </view>
            <view class="salary-row">
              <text class="salary-label">预计报酬</text>
              <view class="salary-content">
                <view class="salary-value">
                  <text>{{ hasSalary ? salary * 8 : 800 }}</text>
                  <text class="salary-unit">元/天</text>
                </view>
                <text class="salary-sub">每个零工{{ salary }}元/小时*8小时</text>
              </view>
            </view>
          </view>

          <!-- 计件面板 -->
          <view v-if="payType === 'piece'">
            <view class="form-row" @click="setPiecePrice">
              <text style="font-size:14px;color:#333;">单价</text>
              <view class="form-value placeholder">
                <text>请设置单价</text>
                <text class="› arrow"></text>
              </view>
            </view>
            <view class="salary-row">
              <text class="salary-label">预计报酬</text>
              <view class="salary-content">
                <view class="salary-value">
                  <text>-</text>
                  <text class="salary-unit">元/天</text>
                </view>
                <text class="salary-sub">单价×预估日产量</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 联系电话 -->
        <view class="section-card">
          <view class="section-header" style="justify-content:space-between;">
            <text class="section-title">联系电话</text>
            <text style="font-size:14px;color:#333;">13698756321</text>
          </view>
          <view class="form-row" style="border-bottom:none;">
            <text style="font-size:12px;color:#999;">零工电话报名、问路等</text>
            <view style="display:flex;align-items:center;gap:4px;margin-left:auto;" @click="navigateTo('backup-phone')">
              <text class="link-btn">
                <text style="margin-right:4px;">+</text>
                添加备用联系人
              </text>
              <text style="color:#1E88E5;font-size:12px;">›</text>
            </view>
          </view>
        </view>

        <!-- 邀请指定零工 -->
        <view class="section-card">
          <view class="form-row" @click="navigateTo('invite-worker')" style="border-bottom:none;">
            <text style="font-size:14px;color:#333;">邀请指定零工接单</text>
            <view class="form-value placeholder" style="flex:1;justify-content:flex-end;">
              <text>请选择</text>
              <text class="› arrow"></text>
            </view>
          </view>
        </view>

        <!-- 招工设置 -->
        <view class="section-card" @click="navigateTo('recruit-settings')">
          <view class="single-row">
            <text class="label">招工设置 <text style="color:#FF6B35;">•</text></text>
            <view class="desc">
              <text>报名、结算等设置</text>
              <text class="› arrow"></text>
            </view>
          </view>
        </view>

        <view style="height: 16px;"></view>
      </scroll-view>

      <!-- 浮动客服 -->
      <view class="service-fab" @click="navigateTo('service-chat')">
        <text style="font-size:18px;color:#FF6B35;margin-bottom:2px;">🎧</text>
        <text style="font-size:10px;color:#666;">客服</text>
      </view>

      <!-- 底部按钮 -->
      <view class="bottom-bar">
        <button class="submit-btn" @click="publishJob">发布招工</button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      count: 1,
      payType: 'hourly',
      hasSalary: true,
      salary: 100
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    navigateTo(page) {
      uni.navigateTo({ url: `/pages/boss/${page}` })
    },
    changeCount(delta) {
      this.count = Math.max(1, Math.min(100, this.count + delta))
    },
    setSalary() {
      uni.showModal({
        title: '设置工价',
        editable: true,
        placeholderText: '100',
        success: (res) => {
          if (res.confirm && res.content) {
            this.salary = parseFloat(res.content) || 100
            this.hasSalary = true
          }
        }
      })
    },
    setPiecePrice() {
      uni.showToast({ title: '设置单价', icon: 'none' })
    },
    publishJob() {
      if (!this.hasSalary) {
        uni.showToast({ title: '请设置工价', icon: 'none' })
        return
      }
      uni.showToast({ title: '发布成功', icon: 'success' })
      setTimeout(() => {
        uni.navigateTo({ url: '/pages/boss/publish-success' })
      }, 1500)
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
  height: 47px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 28px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  background: transparent;
}

.status-icons {
  display: flex;
  align-items: center;
  gap: 4px;
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
  padding: 14px 16px;
  background: white;
  flex-shrink: 0;
}

.nav-back {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #333;
  font-size: 20px;
}

.nav-title {
  font-size: 17px;
  font-weight: 700;
  color: #333;
}

.nav-placeholder {
  width: 32px;
}

.stepper {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 16px 16px;
  gap: 4px;
  flex-shrink: 0;
}

.step {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #999;
}

.step .dot {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #ddd;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 11px;
}

.step.active {
  color: #FF6B35;
  font-weight: 600;
}

.step.active .dot {
  background: #FF6B35;
}

.step-line {
  width: 40px;
  height: 2px;
  background: #ddd;
}

.step-line.active {
  background: #FF6B35;
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
  background: #FFF8E6;
}

.section-card {
  background: white;
  margin: 12px;
  border-radius: 12px;
  padding: 16px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.counter-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.counter-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #F5F5F5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #999;
}

.counter-btn.active {
  background: #FF6B35;
  color: white;
}

.counter-value {
  font-size: 32px;
  font-weight: 700;
  color: #333;
  min-width: 60px;
  text-align: center;
}

.counter-unit {
  font-size: 15px;
  color: #666;
  margin-left: 4px;
}

.tab-group {
  display: flex;
  gap: 0;
  background: #F5F5F5;
  border-radius: 8px;
  padding: 4px;
  margin-bottom: 12px;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  font-size: 14px;
  color: #666;
  border-radius: 6px;
}

.tab-item.active {
  background: white;
  color: #FF6B35;
  font-weight: 600;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.form-row {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}

.form-row:last-child {
  border-bottom: none;
}

.form-value {
  flex: 1;
  font-size: 14px;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
}

.form-value.placeholder {
  color: #BBB;
}

.form-value .arrow {
  color: #CCC;
  font-size: 12px;
}

.link-btn {
  color: #1E88E5;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 2px;
}

.salary-row {
  display: flex;
  align-items: flex-start;
  padding: 12px 0;
  gap: 12px;
}

.salary-label {
  font-size: 14px;
  color: #333;
  flex-shrink: 0;
  padding-top: 2px;
}

.salary-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.salary-value {
  font-size: 16px;
  font-weight: 700;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
}

.salary-unit {
  font-size: 13px;
  color: #666;
  font-weight: 400;
}

.salary-sub {
  font-size: 12px;
  color: #999;
}

.single-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
}

.single-row .label {
  font-size: 14px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 4px;
}

.single-row .desc {
  font-size: 13px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 6px;
}

.single-row .desc .arrow {
  color: #CCC;
  font-size: 12px;
}

.service-fab {
  position: absolute;
  right: 16px;
  bottom: 80px;
  width: 48px;
  height: 48px;
  background: white;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(0,0,0,0.12);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: #666;
  z-index: 10;
}

.bottom-bar {
  background: white;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
  padding-bottom: 20px;
}

.submit-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
}
</style>
