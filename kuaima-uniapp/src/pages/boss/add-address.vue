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
      <text class="nav-title">添加招工地址</text>
      <view class="nav-right"></view>
    </view>

    <!-- 地图区域 -->
    <view class="map-container" @click="selectLocation">
      <view class="map-grid"></view>
      <view class="map-pin">
        <text style="font-size:36px;color:#FF6B35;">📍</text>
      </view>
      <view class="map-search-btn">
        <text style="color:#FF6B35;font-size:18px;">🔍</text>
      </view>
    </view>

    <!-- 表单区域 -->
    <view class="form-container">
      <view class="form-card">
        <view class="form-item">
          <text class="form-label">地址名称</text>
          <input class="form-input" placeholder="如：公司仓库、工厂" v-model="addressName" />
        </view>
        <view class="form-item">
          <text class="form-label">联系人</text>
          <input class="form-input" placeholder="请输入联系人姓名" v-model="contactName" />
        </view>
        <view class="form-item">
          <text class="form-label">联系电话</text>
          <input type="text" class="form-input" placeholder="请输入联系电话" v-model="contactPhone" />
        </view>
      </view>

      <view class="form-card">
        <view class="form-item" @click="selectCity">
          <text class="form-label">所在城市</text>
          <view class="form-selector">
            <text :class="city ? 'value' : 'placeholder'">{{ city || '请选择城市' }}</text>
            <text style="color:#ccc;font-size:12px;">›</text>
          </view>
        </view>
        <view class="form-item" @click="selectDistrict">
          <text class="form-label">所在区域</text>
          <view class="form-selector">
            <text :class="district ? 'value' : 'placeholder'">{{ district || '请选择区域' }}</text>
            <text style="color:#ccc;font-size:12px;">›</text>
          </view>
        </view>
        <view class="form-item">
          <text class="form-label">详细地址</text>
          <input class="form-input" placeholder="请输入详细地址" v-model="detailAddress" />
        </view>
      </view>

      <view class="form-card">
        <view class="checkbox-row" @click="toggleDefault">
          <view class="checkbox-custom" :class="{ checked: isDefault }"></view>
          <text class="checkbox-label">设为默认地址</text>
        </view>
      </view>

      <button class="save-btn" @click="saveAddress">保存地址</button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      addressName: '',
      contactName: '',
      contactPhone: '',
      city: '',
      district: '',
      detailAddress: '',
      isDefault: false
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    selectLocation() {
      uni.showToast({ title: '打开地图选点', icon: 'none' })
    },
    selectCity() {
      uni.showToast({ title: '选择城市', icon: 'none' })
    },
    selectDistrict() {
      uni.showToast({ title: '选择区域', icon: 'none' })
    },
    toggleDefault() {
      this.isDefault = !this.isDefault
    },
    saveAddress() {
      uni.showToast({ title: '地址保存成功', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 500)
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
}

.map-container {
  height: 200px;
  background: linear-gradient(135deg, #E8F5E9, #C8E6C9);
  position: relative;
  overflow: hidden;
}

.map-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255,255,255,0.3) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.3) 1px, transparent 1px);
  background-size: 30px 30px;
}

.map-pin {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -100%);
}

.map-search-btn {
  position: absolute;
  bottom: 16px;
  right: 16px;
  width: 44px;
  height: 44px;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.form-container {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.form-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #f5f5f5;
}

.form-item:last-child {
  border-bottom: none;
}

.form-label {
  font-size: 15px;
  color: #333;
  font-weight: 500;
  width: 80px;
  flex-shrink: 0;
}

.form-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
  color: #333;
  background: transparent;
}

.form-input::placeholder {
  color: #ccc;
}

.form-selector {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-selector .placeholder {
  color: #ccc;
}

.form-selector .value {
  color: #333;
}

.checkbox-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 0;
}

.checkbox-custom {
  width: 18px;
  height: 18px;
  border: 2px solid #ddd;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.checkbox-custom.checked {
  background: #FF6B35;
  border-color: #FF6B35;
}

.checkbox-custom.checked::after {
  content: '✓';
  color: white;
  font-size: 11px;
}

.checkbox-label {
  font-size: 14px;
  color: #333;
}

.save-btn {
  width: 100%;
  margin-top: 24px;
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  color: white;
  padding: 16px;
  border-radius: 28px;
  font-size: 17px;
  font-weight: 600;
  border: none;
  box-shadow: 0 8px 20px rgba(255, 107, 53, 0.3);
}
</style>
