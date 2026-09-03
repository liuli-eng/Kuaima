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
      <text class="nav-title">人才库</text>
      <view class="nav-right">
        <text>+</text>
      </view>
    </view>

    <!-- 搜索框 -->
    <view class="search-bar">
      <view class="search-input">
        <text style="color:#999;margin-right:8px;">🔍</text>
        <input type="text" placeholder="搜索零工姓名/工种" v-model="searchText" />
      </view>
    </view>

    <!-- 筛选标签 -->
    <view class="filter-tabs">
      <text 
        class="filter-tab" 
        :class="{ active: currentTab === tab }"
        v-for="tab in tabs" 
        :key="tab"
        @click="currentTab = tab"
      >{{ tab }}</text>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <view class="worker-card" v-for="(worker, index) in workers" :key="index">
        <view class="worker-avatar" :style="{ background: worker.avatarBg }">
          {{ worker.initial }}
        </view>
        <view class="worker-info">
          <view class="worker-name">
            {{ worker.name }}
            <text class="worker-tag">{{ worker.tag }}</text>
          </view>
          <text class="worker-meta">{{ worker.meta }}</text>
          <view class="worker-skills">
            <text class="skill-tag" v-for="(skill, i) in worker.skills" :key="i">{{ skill }}</text>
          </view>
        </view>
        <view class="worker-action">
          <button class="btn-sm btn-outline" @click="collect(worker)">收藏</button>
          <button class="btn-sm btn-primary" @click="hire(worker)">雇佣</button>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      searchText: '',
      currentTab: '全部',
      tabs: ['全部', '熟练工', '新零工', '我收藏的'],
      workers: [
        { initial: '张', name: '张师傅', tag: '熟练工', meta: '3年经验 · 电商分拣 · 好评率98%', skills: ['分拣', '打包'], avatarBg: 'linear-gradient(135deg, #FF6B35, #FF8C5A)' },
        { initial: '李', name: '李阿姨', tag: '熟练工', meta: '5年经验 · 餐饮服务 · 好评率100%', skills: ['餐饮', '服务员'], avatarBg: 'linear-gradient(135deg, #52C41A, #73D13D)' },
        { initial: '王', name: '王师傅', tag: '熟练工', meta: '4年经验 · 快递搬运 · 好评率95%', skills: ['搬运', '装卸'], avatarBg: 'linear-gradient(135deg, #1890FF, #40A9FF)' }
      ]
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    collect(worker) {
      uni.showToast({ title: `已收藏${worker.name}`, icon: 'success' })
    },
    hire(worker) {
      uni.showToast({ title: `雇佣${worker.name}`, icon: 'none' })
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

.search-bar {
  padding: 10px 16px;
  background: #fff;
}

.search-input {
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 20px;
  padding: 8px 14px;
}

.search-input input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
}

.filter-tabs {
  display: flex;
  gap: 8px;
  padding: 0 16px 12px;
  background: #fff;
}

.filter-tab {
  padding: 6px 14px;
  font-size: 13px;
  color: #666;
  background: #f5f5f5;
  border-radius: 16px;
}

.filter-tab.active {
  background: #FFF3ED;
  color: #FF6B35;
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
}

.worker-card {
  background: #fff;
  margin: 8px 16px;
  border-radius: 12px;
  padding: 14px;
  display: flex;
  gap: 12px;
}

.worker-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  flex-shrink: 0;
}

.worker-info {
  flex: 1;
}

.worker-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
  gap: 6px;
}

.worker-tag {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  background: #FFF3ED;
  color: #FF6B35;
}

.worker-meta {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  display: block;
}

.worker-skills {
  display: flex;
  gap: 6px;
  margin-top: 6px;
  flex-wrap: wrap;
}

.skill-tag {
  font-size: 11px;
  padding: 2px 8px;
  background: #f5f5f5;
  color: #666;
  border-radius: 4px;
}

.worker-action {
  display: flex;
  flex-direction: column;
  gap: 6px;
  justify-content: center;
}

.btn-sm {
  padding: 6px 12px;
  border-radius: 14px;
  font-size: 12px;
  font-weight: 500;
  border: none;
}

.btn-primary {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  color: #fff;
}

.btn-outline {
  background: #fff;
  color: #FF6B35;
  border: 1px solid #FF6B35;
}
</style>
