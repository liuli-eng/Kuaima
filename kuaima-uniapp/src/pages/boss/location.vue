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
        <view class="nav-back" @click="closePage">
          <text>←</text>
        </view>
        <text class="nav-title">设置干活地点</text>
        <view class="nav-placeholder"></view>
      </view>

      <!-- 地图区域 -->
      <view class="map-container">
        <view class="map-bg"></view>
        <view class="map-road map-road-h"></view>
        <view class="map-road map-road-h2"></view>
        <view class="map-road map-road-v"></view>
        <view class="map-road map-road-v2"></view>
        <view class="map-building b1"></view>
        <view class="map-building b2"></view>
        <view class="map-building b3"></view>
        <view class="map-building b4"></view>
        <view class="map-building b5"></view>
        <view class="map-building b6"></view>

        <!-- 搜索栏 -->
        <view class="search-bar">
          <text style="color:#999;font-size:14px;">🔍</text>
          <input type="text" placeholder="搜索地址或地点" />
        </view>

        <!-- 定位按钮 -->
        <view class="locate-btn" @click="locateMe">
          <text style="font-size:14px;">🎯</text>
        </view>

        <!-- 地图标记 -->
        <view class="map-pin">
          <view class="pin-pulse"></view>
          <view class="pin-outer">
            <text style="font-size:16px;">📍</text>
          </view>
        </view>

        <!-- 地图控制 -->
        <view class="map-controls">
          <view class="map-ctrl-btn" @click="zoomIn">
            <text style="font-size:14px;">+</text>
          </view>
          <view class="map-ctrl-btn" @click="zoomOut">
            <text style="font-size:14px;">−</text>
          </view>
        </view>
      </view>

      <!-- 滚动区域 -->
      <scroll-view scroll-y class="scroll-area" style="flex:1;overflow-y:auto;min-height:0;">
        <!-- 位置信息 -->
        <view class="location-info">
          <view class="location-title-row">
            <text class="location-title">{{ selectedAddress.name }}</text>
            <text class="location-tag">已选地点</text>
            <text class="location-distance">距离您 {{ distance }}km</text>
          </view>
          <text class="location-address">{{ selectedAddress.address }}</text>
        </view>

        <!-- 常用地点 -->
        <view class="section-header">
          <text class="section-title">常用地点</text>
          <text class="section-action" @click="manageAddress">管理</text>
        </view>

        <!-- 地址列表 -->
        <view class="address-list">
          <view 
            class="address-item" 
            :class="{ selected: selectedIndex === index }"
            v-for="(item, index) in addresses" 
            :key="index"
            @click="selectAddress(index)"
          >
            <view class="address-icon">
              <text class="iconfont" :class="item.icon" style="font-size:16px;"></text>
            </view>
            <view class="address-info">
              <view class="address-name">
                {{ item.name }}
                <text class="addr-tag hot" v-if="item.tag === '最近'">{{ item.tag }}</text>
              </view>
              <text class="address-detail">{{ item.address }}</text>
              <view class="address-tags" style="margin-top:4px;">
                <text class="addr-tag" v-for="(t, i) in item.tags" :key="i">{{ t }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 新增地址 -->
        <view class="add-address-btn" @click="addNewAddress">
          <text style="font-size:14px;">+</text>
          <text>新增工作地点</text>
        </view>
      </scroll-view>

      <!-- 底部按钮 -->
      <view class="bottom-bar">
        <button class="submit-btn" @click="saveLocation">确定使用此地点</button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      selectedIndex: 0,
      distance: '1.2',
      addresses: [
        { name: '观澜电子厂', address: '龙华区观澜街道桂月路316号', icon: 'icon-industry', tag: '最近', tags: ['电子厂', '有空调'] },
        { name: '龙华物流园', address: '龙华区民治街道华旺路88号', icon: 'icon-warehouse', tag: '', tags: ['物流', '仓库'] },
        { name: '宝安美食广场', address: '宝安区福永街道兴围社区美食街18号', icon: 'icon-hotel', tag: '', tags: ['餐饮'] }
      ]
    }
  },
  computed: {
    selectedAddress() {
      return this.addresses[this.selectedIndex] || { name: '', address: '' }
    }
  },
  methods: {
    closePage() {
      uni.navigateBack()
    },
    locateMe() {
      uni.showToast({ title: '正在获取当前位置...', icon: 'loading' })
    },
    zoomIn() {
      uni.showToast({ title: '放大地图', icon: 'none' })
    },
    zoomOut() {
      uni.showToast({ title: '缩小地图', icon: 'none' })
    },
    selectAddress(index) {
      this.selectedIndex = index
      this.distance = (Math.random() * 3 + 0.5).toFixed(1)
    },
    manageAddress() {
      uni.showToast({ title: '管理地址', icon: 'none' })
    },
    addNewAddress() {
      uni.showToast({ title: '新增工作地点', icon: 'none' })
    },
    saveLocation() {
      const addr = this.selectedAddress
      const data = {
        name: addr.name,
        address: addr.address,
        tag: addr.tag,
        display: `${addr.name} ${addr.address}`
      }
      uni.$emit('workLocationSelected', data)
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

.map-container {
  position: relative;
  height: 260px;
  background: linear-gradient(180deg, #E8F4FD 0%, #F0EDE4 100%);
  overflow: hidden;
  flex-shrink: 0;
}

.map-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    linear-gradient(rgba(200,200,200,0.3) 1px, transparent 1px),
    linear-gradient(90deg, rgba(200,200,200,0.3) 1px, transparent 1px);
  background-size: 40px 40px;
}

.map-road {
  position: absolute;
  background: #ddd;
  border-radius: 4px;
}

.map-road-h { height: 14px; top: 60px; left: 0; right: 0; }
.map-road-h2 { height: 10px; top: 160px; left: 0; right: 0; }
.map-road-v { width: 14px; top: 0; bottom: 0; left: 100px; }
.map-road-v2 { width: 10px; top: 0; bottom: 0; right: 80px; }

.map-building {
  position: absolute;
  border-radius: 4px;
}

.b1 { width: 50px; height: 40px; background: #B8D4E3; top: 80px; left: 30px; }
.b2 { width: 45px; height: 55px; background: #C5DDB0; top: 180px; left: 40px; }
.b3 { width: 60px; height: 35px; background: #E8D5B7; top: 90px; right: 40px; }
.b4 { width: 40px; height: 50px; background: #D4C5DD; top: 180px; right: 100px; }
.b5 { width: 35px; height: 30px; background: #F5D7B5; top: 25px; left: 180px; }
.b6 { width: 55px; height: 45px; background: #B8D4E3; top: 30px; right: 20px; }

.map-pin {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -100%);
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.pin-outer {
  width: 36px;
  height: 36px;
  background: #FF6B35;
  border: 3px solid white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.4);
}

.pin-pulse {
  position: absolute;
  width: 60px;
  height: 60px;
  background: rgba(255, 107, 53, 0.2);
  border-radius: 50%;
  animation: pulse 2s infinite;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

@keyframes pulse {
  0% { transform: translate(-50%, -50%) scale(1); opacity: 0.8; }
  100% { transform: translate(-50%, -50%) scale(1.5); opacity: 0; }
}

.map-controls {
  position: absolute;
  right: 12px;
  bottom: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.map-ctrl-btn {
  width: 36px;
  height: 36px;
  background: white;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  color: #333;
}

.locate-btn {
  position: absolute;
  right: 12px;
  top: 12px;
  width: 36px;
  height: 36px;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  color: #FF6B35;
}

.search-bar {
  position: absolute;
  left: 12px;
  right: 60px;
  top: 12px;
  height: 36px;
  background: white;
  border-radius: 18px;
  display: flex;
  align-items: center;
  padding: 0 14px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.search-bar input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 13px;
  background: transparent;
}

.search-bar input::placeholder {
  color: #bbb;
}

.location-info {
  background: white;
  margin: -16px 12px 12px;
  border-radius: 14px;
  padding: 14px 16px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
  position: relative;
  z-index: 20;
}

.location-title-row {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
}

.location-title {
  font-size: 15px;
  font-weight: 700;
  color: #333;
  flex: 1;
}

.location-tag {
  font-size: 11px;
  color: #FF6B35;
  background: #FFF0E8;
  padding: 2px 8px;
  border-radius: 4px;
  margin-right: 6px;
}

.location-distance {
  font-size: 11px;
  color: #999;
}

.location-address {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  display: block;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px 8px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.section-action {
  font-size: 13px;
  color: #FF6B35;
}

.address-list {
  padding: 0 12px;
}

.address-item {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  background: white;
  border-radius: 10px;
  margin-bottom: 8px;
  border: 1px solid transparent;
}

.address-item.selected {
  background: #FFF8E6;
  border-color: #FF6B35;
}

.address-icon {
  width: 36px;
  height: 36px;
  background: #F5F5F5;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FF6B35;
  margin-right: 12px;
  flex-shrink: 0;
}

.address-info {
  flex: 1;
  min-width: 0;
}

.address-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.address-detail {
  font-size: 12px;
  color: #999;
  line-height: 1.4;
  display: block;
}

.address-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.addr-tag {
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 3px;
  background: #F0F0F0;
  color: #666;
}

.addr-tag.hot {
  background: #FF6B35;
  color: white;
}

.add-address-btn {
  margin: 12px;
  height: 44px;
  border: 1.5px dashed #ddd;
  border-radius: 10px;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 14px;
  color: #999;
}

.scroll-area {
  padding-bottom: 12px;
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
