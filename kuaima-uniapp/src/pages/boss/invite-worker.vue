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
        <text class="nav-title">邀请指定零工接单</text>
        <view class="nav-placeholder"></view>
      </view>

      <!-- 搜索框 -->
      <view class="search-bar">
        <view class="search-input">
          <text style="color:#999;font-size:14px;">🔍</text>
          <input type="text" placeholder="搜索姓名、手机号" v-model="searchText" />
        </view>
      </view>

      <!-- Tab切换 -->
      <view class="tabs">
        <text 
          class="tab" 
          :class="{ active: currentTab === tab.value }"
          v-for="tab in tabs" 
          :key="tab.value"
          @click="currentTab = tab.value"
        >{{ tab.label }}</text>
      </view>

      <!-- 列表区域 -->
      <scroll-view scroll-y class="scroll-area">
        <view class="list-item" :class="{ selected: selectedWorkers.includes(index) }" v-for="(worker, index) in workers" :key="index" @click="toggleSelect(index)">
          <view class="col-left">
            <view class="avatar" :class="worker.avatarClass">{{ worker.initial }}</view>
          </view>
          <view class="col-middle">
            <view class="info-row">
              <text class="info-name">{{ worker.name }}</text>
              <text class="badge" v-if="worker.verified">认证</text>
            </view>
            <view class="info-row">
              <text class="info-phone">{{ worker.phone }}</text>
              <text class="info-tag" :class="worker.tagClass">{{ worker.tag }}</text>
              <text class="info-orders">{{ worker.orders }}</text>
            </view>
            <view class="info-row">
              <text class="info-phone" style="color:#FFB800;">
                <text style="font-size:12px;">⭐</text>
              </text>
              <text class="info-phone" style="color:#333;font-weight:500;">{{ worker.rating }}</text>
              <text class="info-phone" style="color:#999;">· {{ worker.desc }}</text>
            </view>
          </view>
          <view class="col-right">
            <view class="checkbox" :class="{ checked: selectedWorkers.includes(index) }"></view>
          </view>
        </view>
      </scroll-view>

      <!-- 底部按钮 -->
      <view class="bottom-bar">
        <text class="count-info">已选 <text class="num">{{ selectedWorkers.length }}</text> 人</text>
        <button class="confirm-btn" :disabled="selectedWorkers.length === 0" @click="confirmSelect">确认邀请{{ selectedWorkers.length > 0 ? ' ' + selectedWorkers.length + ' 人' : '' }}</button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      searchText: '',
      currentTab: 'recent',
      selectedWorkers: [],
      tabs: [
        { label: '最近合作', value: 'recent' },
        { label: '我的收藏', value: 'favorite' },
        { label: '全部零工', value: 'all' }
      ],
      workers: [
        { avatarClass: 'a1', initial: '张', name: '张师傅', verified: true, phone: '138****5678', tag: '金牌', tagClass: 'gold', orders: '已完成36单', rating: 4.9, desc: '接活快' },
        { avatarClass: 'a2', initial: '李', name: '李大姐', verified: true, phone: '139****1234', tag: '电子厂熟手', tagClass: '', orders: '已完成28单', rating: 4.8, desc: '守时可靠' },
        { avatarClass: 'a3', initial: '王', name: '王大哥', verified: true, phone: '137****9012', tag: '普工', tagClass: 'silver', orders: '已完成15单', rating: 4.7, desc: '干活麻利' },
        { avatarClass: 'a4', initial: '陈', name: '陈阿姨', verified: true, phone: '136****3456', tag: '金牌', tagClass: 'gold', orders: '已完成42单', rating: 5.0, desc: '老员工推荐' },
        { avatarClass: 'a5', initial: '刘', name: '刘师傅', verified: true, phone: '135****7890', tag: '焊锡工', tagClass: '', orders: '已完成22单', rating: 4.6, desc: '技术熟练' },
        { avatarClass: 'a6', initial: '赵', name: '赵小妹', verified: true, phone: '138****2345', tag: '杂工', tagClass: 'silver', orders: '已完成8单', rating: 4.5, desc: '积极主动' }
      ]
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    toggleSelect(index) {
      const idx = this.selectedWorkers.indexOf(index)
      if (idx >= 0) {
        this.selectedWorkers.splice(idx, 1)
      } else {
        this.selectedWorkers.push(index)
      }
    },
    confirmSelect() {
      if (this.selectedWorkers.length === 0) return
      uni.showToast({ title: `已邀请 ${this.selectedWorkers.length} 人`, icon: 'success' })
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
  background: #fff;
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

.search-bar {
  padding: 12px 16px;
  background: #fff;
  flex-shrink: 0;
}

.search-input {
  display: flex;
  align-items: center;
  background: #F5F5F5;
  border-radius: 20px;
  padding: 10px 14px;
  gap: 8px;
}

.search-input input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 14px;
  color: #333;
  outline: none;
}

.tabs {
  display: flex;
  padding: 0 16px 10px;
  background: #fff;
  gap: 0;
  flex-shrink: 0;
}

.tab {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  font-size: 14px;
  color: #666;
  position: relative;
}

.tab.active {
  color: #FF6B35;
  font-weight: 600;
}

.tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 3px;
  background: #FF6B35;
  border-radius: 2px;
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
  background: #F7F8FA;
  padding-top: 4px;
}

.list-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 14px 16px;
  background: #fff;
  margin: 0 12px 10px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  border: 1.5px solid transparent;
  gap: 12px;
}

.list-item.selected {
  border-color: #FF6B35;
  background: #FFFBF7;
}

.col-left {
  flex: 0 0 auto;
  width: 48px;
}

.col-middle {
  flex: 1 1 auto;
  min-width: 0;
  overflow: hidden;
}

.col-right {
  flex: 0 0 auto;
  margin-left: 8px;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
}

.avatar.a1 { background: linear-gradient(135deg, #FF6B35, #FF8C5A); }
.avatar.a2 { background: linear-gradient(135deg, #4A90D9, #6BB5F0); }
.avatar.a3 { background: linear-gradient(135deg, #52C41A, #73D13D); }
.avatar.a4 { background: linear-gradient(135deg, #FA8C16, #FFA940); }
.avatar.a5 { background: linear-gradient(135deg, #722ED1, #9254DE); }
.avatar.a6 { background: linear-gradient(135deg, #EB2F96, #F759AB); }

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.info-row + .info-row {
  margin-top: 4px;
}

.info-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.badge {
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 3px;
  background: #FFF3ED;
  color: #FF6B35;
  font-weight: 400;
  flex-shrink: 0;
}

.info-phone {
  font-size: 12px;
  color: #666;
  flex-shrink: 0;
}

.info-tag {
  background: #F0F9FF;
  color: #3B82F6;
  padding: 1px 6px;
  border-radius: 3px;
  font-size: 10px;
  white-space: nowrap;
  flex-shrink: 0;
}

.info-tag.gold {
  background: #FFFBEB;
  color: #D97706;
}

.info-tag.silver {
  background: #F5F5F5;
  color: #666;
}

.info-orders {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
}

.checkbox {
  width: 22px;
  height: 22px;
  border: 2px solid #DDD;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  flex-shrink: 0;
}

.checkbox.checked {
  border-color: #FF6B35;
  background: #FF6B35;
}

.checkbox.checked::after {
  content: '✓';
  color: #fff;
  font-size: 13px;
  font-weight: bold;
}

.bottom-bar {
  background: #fff;
  padding: 14px 16px 34px;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.count-info {
  font-size: 14px;
  color: #666;
}

.count-info .num {
  color: #FF6B35;
  font-weight: 700;
  font-size: 18px;
}

.confirm-btn {
  flex: 1;
  height: 46px;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: #fff;
  border: none;
  border-radius: 23px;
  font-size: 15px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(255, 165, 0, 0.3);
}

.confirm-btn:disabled {
  opacity: 0.4;
  box-shadow: none;
}
</style>
