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
      <text class="nav-title">招工地址</text>
      <view class="nav-right">
        <text class="+" @click="navigateTo('add-address')"></text>
      </view>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <!-- 添加地址按钮 -->
      <view class="add-btn" @click="navigateTo('add-address')">
        <text>📍</text>
        <text>添加招工地址</text>
      </view>

      <!-- 地址列表 -->
      <view class="address-card" v-for="(addr, index) in addresses" :key="index">
        <view class="address-head">
          <text class="address-name">{{ addr.name }}</text>
          <text class="address-default" v-if="addr.isDefault">默认</text>
        </view>
        <text class="address-detail">{{ addr.detail }}</text>
        <view class="address-actions">
          <view class="action-btn" @click="navigate">
            <text style="margin-right:4px;">📍</text>
            <text>导航</text>
          </view>
          <view class="action-btn" @click="navigateTo('add-address')">
            <text style="margin-right:4px;">✏</text>
            <text>编辑</text>
          </view>
          <view class="action-btn danger" @click="deleteAddress(index)" v-if="!addr.isDefault">
            <text style="margin-right:4px;">🗑</text>
            <text>删除</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      addresses: [
        { name: '公司仓库', detail: '上海市浦东新区张江高科技园区科苑路88号', isDefault: true },
        { name: '闵行工厂', detail: '上海市闵行区莘庄工业区申富路366号', isDefault: false }
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
    navigate() {
      uni.showToast({ title: '导航', icon: 'none' })
    },
    deleteAddress(index) {
      uni.showModal({
        title: '提示',
        content: '确定要删除该地址吗？',
        success: (res) => {
          if (res.confirm) {
            this.addresses.splice(index, 1)
          }
        }
      })
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

.add-btn {
  margin: 12px 16px;
  padding: 12px;
  background: #fff;
  border: 1px dashed #FF6B35;
  border-radius: 10px;
  color: #FF6B35;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.address-card {
  background: #fff;
  margin: 8px 16px;
  border-radius: 12px;
  padding: 14px;
}

.address-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.address-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.address-default {
  font-size: 11px;
  color: #FF6B35;
  background: #FFF3ED;
  padding: 2px 8px;
  border-radius: 4px;
}

.address-detail {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  display: block;
}

.address-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f5f5f5;
}

.action-btn {
  padding: 4px 10px;
  font-size: 12px;
  color: #666;
  background: #f5f5f5;
  border-radius: 4px;
  display: flex;
  align-items: center;
}

.action-btn.danger {
  color: #FF4D4F;
}
</style>
