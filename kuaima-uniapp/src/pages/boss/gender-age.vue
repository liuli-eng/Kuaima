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
      <!-- 头部 -->
      <view class="modal-header">
        <text class="modal-title">性别年龄</text>
        <view class="modal-close" @click="closePage">
          <text>✕</text>
        </view>
      </view>

      <!-- 内容区 -->
      <scroll-view scroll-y class="modal-content">
        <text class="section-title">年龄要求</text>
        <view class="age-display">
          <text class="age-range-text">
            {{ minAge }}岁~<text :style="maxAge >= 60 ? 'color:#999;' : ''">{{ maxAge >= 60 ? '不限' : maxAge }}</text>
          </text>
        </view>

        <view class="slider-container">
          <view class="slider-track">
            <view class="slider-fill" :style="sliderFillStyle"></view>
            <view class="slider-handle left" :style="leftHandleStyle" @touchstart="onTouchStart('min', $event)" @touchmove="onTouchMove('min', $event)" @touchend="onTouchEnd">
              <text style="font-size:10px;color:#FF6B35;">☰</text>
            </view>
            <view class="slider-handle right" :style="rightHandleStyle" @touchstart="onTouchStart('max', $event)" @touchmove="onTouchMove('max', $event)" @touchend="onTouchEnd">
              <text style="font-size:10px;color:#FF6B35;">☰</text>
            </view>
          </view>
          <view class="slider-labels">
            <text>18</text>
            <text>20</text>
            <text>30</text>
            <text>40</text>
            <text>50</text>
            <text>不限</text>
          </view>
        </view>
      </scroll-view>

      <!-- 底部按钮 -->
      <view class="bottom-bar">
        <button class="submit-btn" @click="saveAndClose">确定</button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      maxAgeLimit: 60,
      minAge: 18,
      maxAge: 60,
      selectedGender: '不限',
      activeHandle: null,
      startX: 0
    }
  },
  computed: {
    leftPercent() {
      return ((this.minAge - 18) / (this.maxAgeLimit - 18)) * 100
    },
    rightPercent() {
      return ((this.maxAge - 18) / (this.maxAgeLimit - 18)) * 100
    },
    leftHandleStyle() {
      return `left: ${this.leftPercent}%;`
    },
    rightHandleStyle() {
      return `left: ${this.rightPercent}%;`
    },
    sliderFillStyle() {
      return `left: ${this.leftPercent}%; right: ${100 - this.rightPercent}%;`
    }
  },
  methods: {
    closePage() {
      uni.navigateBack()
    },
    onTouchStart(type, e) {
      this.activeHandle = type
      this.startX = e.touches[0].clientX
    },
    onTouchMove(type, e) {
      if (this.activeHandle !== type) return
      const trackWidth = 300 // 滑块轨道宽度
      const deltaX = e.touches[0].clientX - this.startX
      const deltaPercent = deltaX / trackWidth
      const deltaAge = Math.round(deltaPercent * (this.maxAgeLimit - 18))

      if (type === 'min') {
        let newAge = this.minAge + deltaAge
        newAge = Math.max(18, Math.min(this.maxAge - 1, newAge))
        this.minAge = newAge
      } else {
        let newAge = this.maxAge + deltaAge
        newAge = Math.max(this.minAge + 1, Math.min(this.maxAgeLimit, newAge))
        this.maxAge = newAge
      }
      this.startX = e.touches[0].clientX
    },
    onTouchEnd() {
      this.activeHandle = null
    },
    saveAndClose() {
      const display = `${this.selectedGender}、${this.minAge}岁~${this.maxAge >= this.maxAgeLimit ? '不限' : this.maxAge}岁`
      const data = {
        gender: this.selectedGender,
        ageMin: this.minAge,
        ageMax: this.maxAge >= this.maxAgeLimit ? '不限' : this.maxAge,
        display: display
      }
      uni.$emit('genderAgeSelected', data)
      uni.navigateBack()
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

.modal-header {
  text-align: center;
  padding: 18px 16px 0;
  position: relative;
  flex-shrink: 0;
}

.modal-title {
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

.modal-close {
  position: absolute;
  right: 16px;
  top: 14px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #999;
}

.modal-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px 16px;
}

.section-title {
  font-size: 14px;
  color: #999;
  margin-bottom: 14px;
  display: block;
}

.age-display {
  text-align: center;
  margin-bottom: 24px;
}

.age-range-text {
  font-size: 20px;
  font-weight: 700;
  color: #333;
}

.slider-container {
  position: relative;
  padding: 0 12px;
}

.slider-track {
  position: relative;
  height: 4px;
  background: #e8e8e8;
  border-radius: 2px;
  margin: 16px 0 20px;
}

.slider-fill {
  position: absolute;
  height: 100%;
  background: #FF6B35;
  border-radius: 2px;
}

.slider-handle {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 28px;
  height: 28px;
  background: white;
  border: 2px solid #FF6B35;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(255, 107, 53, 0.3);
}

.slider-labels {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
  padding: 0 4px;
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
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  color: white;
  border-radius: 9999px;
  font-size: 16px;
  font-weight: 600;
  border: none;
  box-shadow: 0 6px 20px rgba(255, 107, 53, 0.3);
}
</style>
