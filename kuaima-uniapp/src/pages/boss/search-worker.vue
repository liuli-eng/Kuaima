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
      <text class="nav-title">搜索零工</text>
      <view class="nav-right">
        <text>…</text>
      </view>
    </view>

    <!-- 搜索框 -->
    <view class="search-header">
      <view class="search-input-wrap">
        <text style="color:#999;margin-right:8px;font-size:14px;">🔍</text>
        <input type="text" class="search-input" placeholder="搜索姓名/身份证后4位" v-model="searchText" @confirm="doSearch" />
        <button class="search-btn" @click="doSearch">搜索</button>
      </view>
    </view>

    <!-- 筛选条件 -->
    <view class="filter-row" style="justify-content:space-between;padding:12px 16px 8px;">
      <text style="font-size:15px;font-weight:600;color:#333;">零工列表</text>
      <view style="display:flex;gap:16px;">
        <view class="filter-dropdown" @click="toggleDropdown('people')">
          <text>找人</text>
          <text style="font-size:10px;color:#999;margin-left:4px;">↓</text>
        </view>
        <view class="filter-dropdown" @click="toggleDropdown('date')">
          <text>日期筛选</text>
          <text style="font-size:10px;color:#999;margin-left:4px;">↓</text>
        </view>
      </view>
    </view>

    <!-- Tab标签 -->
    <view class="tabs-row">
      <text 
        class="tab-item" 
        :class="{ active: currentTab === tab.value }"
        v-for="tab in tabs" 
        :key="tab.value"
        @click="currentTab = tab.value"
      >{{ tab.label }}</text>
    </view>

    <!-- 空状态 -->
    <view class="empty-state">
      <view class="empty-image">
        <text style="color:#fff;font-size:50px;">🔍</text>
      </view>
      <text class="empty-text">暂无数据</text>
      <text class="empty-desc">输入零工姓名或身份证号后4位进行搜索</text>
    </view>

    <!-- 底部标语 -->
    <view class="bottom-slogan">
      <text class="slogan-title">招临时工 上快马日结</text>
      <text class="slogan-desc">— 熟练工 上岗快 人靠谱 —</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      searchText: '',
      currentTab: 'all',
      tabs: [
        { label: '全部', value: 'all' },
        { label: '报名 0', value: 'signUp' },
        { label: '接单 0', value: 'accept' },
        { label: '到达 0', value: 'arrive' },
        { label: '工作中 0', value: 'working' },
        { label: '待结算 0', value: 'settle' }
      ]
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    doSearch() {
      if (!this.searchText.trim()) {
        uni.showToast({ title: '请输入搜索内容', icon: 'none' })
        return
      }
      uni.showToast({ title: `搜索：${this.searchText}\n\n暂无匹配的零工数据`, icon: 'none' })
    },
    toggleDropdown(type) {
      if (type === 'people') {
        uni.showToast({ title: '选择找人方式', icon: 'none' })
      } else {
        uni.showToast({ title: '选择日期筛选', icon: 'none' })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  width: 100%;
  height: 100vh;
  background: #fff;
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

.search-header {
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #f5f5f5;
}

.search-input-wrap {
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 24px;
  padding: 10px 16px;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 14px;
}

.search-input::placeholder {
  color: #999;
}

.search-btn {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  color: white;
  border: none;
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 13px;
  font-weight: 500;
  margin-left: 8px;
}

.filter-row {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #333;
}

.filter-dropdown {
  display: flex;
  align-items: center;
}

.tabs-row {
  display: flex;
  padding: 0 16px 12px;
  gap: 20px;
  overflow-x: auto;
}

.tab-item {
  font-size: 13px;
  color: #999;
  padding: 6px 12px;
  border-radius: 16px;
  white-space: nowrap;
}

.tab-item.active {
  background: #FFF3ED;
  color: #FF6B35;
  font-weight: 500;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 40px;
}

.empty-image {
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, #FFE4C4 0%, #FFDAB9 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.empty-text {
  font-size: 15px;
  color: #999;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 13px;
  color: #ccc;
}

.bottom-slogan {
  text-align: center;
  padding: 24px 16px;
}

.slogan-title {
  font-size: 28px;
  font-weight: 800;
  color: #E8D5B7;
  letter-spacing: 4px;
}

.slogan-desc {
  font-size: 13px;
  color: #ccc;
  margin-top: 6px;
}
</style>
