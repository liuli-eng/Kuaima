<template>
  <view class="container">
    <view class="page-bg">
      <!-- 头部 -->
      <view class="modal-header" :style="{ paddingTop: `${statusBarHeight + 20}px` }">
        <view class="modal-close" :style="{ top: `${statusBarHeight + 20}px` }" @click="goBack">
          <text class="close-icon">×</text>
        </view>
        <text class="modal-title">更多工种</text>
        <text class="modal-desc">选对工种，免费推荐更多熟手</text>
      </view>

      <!-- Tab切换 -->
      <view class="tabs">
        <text class="tab-item" :class="{ active: currentTab === 'industry' }" @click="switchTab('industry')">请选择行业</text>
        <text class="tab-item" :class="{ active: currentTab === 'type' }" @click="switchTab('type')">请选择企业类型</text>
        <text class="tab-item" :class="{ active: currentTab === 'job' }" @click="switchTab('job')">请选择工种(多选)</text>
      </view>

      <!-- Tab内容 -->
      <scroll-view scroll-y class="tab-content">
        <!-- 行业选择 -->
        <view class="tab-pane" :class="{ active: currentTab === 'industry' }">
          <view class="industry-item" v-for="(item, index) in industries" :key="index" @click="selectIndustry(item)">
            <text>{{ item }}</text>
            <text class="arrow-icon">›</text>
          </view>
        </view>

        <!-- 企业类型选择 -->
        <view class="tab-pane" :class="{ active: currentTab === 'type' }">
          <view class="type-item" :class="{ selected: selectedTypes.includes(item) }" v-for="(item, index) in types" :key="index" @click="selectType(item)">
            <text>{{ item }}</text>
            <view class="check" v-if="selectedTypes.includes(item)">
              <text class="check-icon">✓</text>
            </view>
          </view>
        </view>

        <!-- 工种选择 -->
        <view class="tab-pane" :class="{ active: currentTab === 'job' }">
          <view class="job-row" :class="{ selected: selectedJobs.includes(index) }" v-for="(job, index) in jobs" :key="index" @click="toggleJob(index)">
            <view class="job-info">
              <text class="job-name">{{ job.name }}</text>
              <text class="job-desc">{{ job.desc }}</text>
            </view>
            <view class="checkbox">
              <text v-if="selectedJobs.includes(index)" class="checkbox-icon">✓</text>
            </view>
          </view>
        </view>
      </scroll-view>

      <!-- 底部按钮 -->
      <view class="bottom-bar" :style="{ paddingBottom: `calc(12px + ${safeBottom}px)` }">
        <button class="btn-prev" @click="prevStep">{{ prevLabel }}</button>
        <button class="btn-next" :disabled="!canNext" @click="nextStep">{{ nextLabel }}</button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      statusBarHeight: 0,
      safeBottom: 0,
      currentTab: 'industry',
      selectedIndustry: '',
      selectedTypes: [],
      selectedJobs: [],
      industries: [
        '制造业工厂',
        '电商/仓储/物流/运输',
        '服务业',
        '建筑/装修',
        '农业/渔业/林业',
        '其他行业'
      ],
      types: [
        '电子厂',
        '五金厂',
        '注塑厂',
        '快递公司',
        '电商仓库',
        '餐饮/酒店',
        '建筑工地'
      ],
      jobs: [
        { name: '普工', desc: '操作工/流水线/组装/打螺丝/打包/包装/测试' },
        { name: '焊锡工', desc: '流水线/电路板/焊锡/点焊' },
        { name: 'SMT操作工', desc: 'SMT操作/贴料/上料/检板/测试/调试/维修' },
        { name: '搬运装卸工', desc: '搬运/装卸/仓库/物流' },
        { name: '杂工', desc: '杂事/临时/辅助工作' },
        { name: '清洁工', desc: '车间清洁/卫生打扫' },
        { name: '叉车工', desc: '叉车操作/仓库搬运' },
        { name: '仓管', desc: '仓库管理/出入库/盘点' }
      ]
    }
  },
  onLoad() {
    try {
      const info = typeof uni.getWindowInfo === 'function' ? uni.getWindowInfo() : uni.getSystemInfoSync()
      this.statusBarHeight = Number(info.statusBarHeight || 0)
      this.safeBottom = Number(info.safeAreaInsets?.bottom || 0)
    } catch (_) {}
  },
  computed: {
    canNext() {
      if (this.currentTab === 'industry') {
        return !!this.selectedIndustry
      } else if (this.currentTab === 'type') {
        return this.selectedTypes.length > 0
      } else {
        return this.selectedJobs.length > 0
      }
    },
    prevLabel() {
      return this.currentTab === 'industry' ? '取消' : '上一步'
    },
    nextLabel() {
      if (this.currentTab !== 'job') return '下一步'
      return this.selectedJobs.length ? `完成 (${this.selectedJobs.length})` : '完成'
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    switchTab(tab) {
      this.currentTab = tab
    },
    selectIndustry(name) {
      this.selectedIndustry = name
      this.currentTab = 'type'
    },
    selectType(name) {
      const idx = this.selectedTypes.indexOf(name)
      if (idx >= 0) {
        this.selectedTypes.splice(idx, 1)
      } else {
        this.selectedTypes.push(name)
      }
      // 原型中选择企业类型后直接进入工种多选
      this.currentTab = 'job'
    },
    toggleJob(index) {
      const idx = this.selectedJobs.indexOf(index)
      if (idx >= 0) {
        this.selectedJobs.splice(idx, 1)
      } else {
        this.selectedJobs.push(index)
      }
    },
    prevStep() {
      if (this.currentTab === 'industry') {
        this.goBack()
      } else if (this.currentTab === 'type') {
        this.currentTab = 'industry'
      } else {
        this.currentTab = 'type'
      }
    },
    nextStep() {
      if (this.currentTab === 'industry') {
        this.currentTab = 'type'
      } else if (this.currentTab === 'type') {
        this.currentTab = 'job'
      } else {
        const selectedJobNames = this.selectedJobs.map(i => this.jobs[i].name)
        const data = {
          industry: this.selectedIndustry,
          types: this.selectedTypes,
          jobs: selectedJobNames
        }
        uni.$emit('jobsSelected', data)
        uni.navigateTo({
          url: `/pages/boss/publish-info?job=${encodeURIComponent(selectedJobNames.join('、'))}`
        })
      }
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

.page-bg {
  background: #fff;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

.modal-header {
  text-align: center;
  padding: 40rpx 32rpx 0;
  position: relative;
}

.modal-close {
  position: absolute;
  right: 32rpx;
  top: 40rpx;
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-icon {
  color: #999;
  font-size: 44rpx;
  font-weight: 300;
  line-height: 1;
}

.modal-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #333;
}

.modal-desc {
  font-size: 26rpx;
  color: #999;
  margin-top: 8rpx;
}

.tabs {
  display: flex;
  border-bottom: 2rpx solid #f0f0f0;
  margin: 32rpx 32rpx 0;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  font-size: 28rpx;
  color: #999;
  position: relative;
  font-weight: 500;
}

.tab-item.active {
  color: #FF6B35;
  font-weight: 600;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -2rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 80rpx;
  height: 6rpx;
  background: #FF6B35;
  border-radius: 4rpx;
}

.tab-content {
  flex: 1;
  box-sizing: border-box;
  padding: 0 32rpx 200rpx;
}

.tab-pane {
  display: none;
}

.tab-pane.active {
  display: block;
}

.industry-item {
  padding: 36rpx 0;
  border-bottom: 2rpx solid #f5f5f5;
  font-size: 32rpx;
  color: #333;
  font-weight: 500;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.industry-item:last-child,
.type-item:last-child,
.job-row:last-child {
  border-bottom: none;
}

.industry-item:active {
  color: #FF6B35;
}

.arrow-icon {
  color: #ccc;
  font-size: 24rpx;
}

.type-item {
  padding: 36rpx 0;
  border-bottom: 2rpx solid #f5f5f5;
  font-size: 32rpx;
  color: #333;
  font-weight: 500;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.type-item.selected {
  color: #FF6B35;
}

.check {
  display: flex;
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: #FF6B35;
  align-items: center;
  justify-content: center;
}

.check-icon {
  color: #fff;
  font-size: 24rpx;
}

.job-row {
  display: flex;
  align-items: center;
  padding: 32rpx 0;
  border-bottom: 2rpx solid #f5f5f5;
}

.job-info {
  flex: 1;
}

.job-name {
  font-size: 30rpx;
  font-weight: 500;
  color: #333;
}

.job-desc {
  font-size: 24rpx;
  color: #999;
  margin-top: 6rpx;
}

.checkbox {
  width: 44rpx;
  height: 44rpx;
  border-radius: 8rpx;
  border: 4rpx solid #ddd;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.job-row.selected .checkbox {
  border-color: #FF6B35;
  background: #FF6B35;
}

.checkbox-icon {
  color: #fff;
  font-size: 24rpx;
}

.bottom-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 24rpx 32rpx;
  display: flex;
  gap: 24rpx;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.05);
}

.btn-prev {
  flex: 1;
  padding: 24rpx;
  background: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 48rpx;
  font-size: 30rpx;
  font-weight: 500;
}

.btn-next {
  flex: 2;
  padding: 24rpx;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: #fff;
  border: none;
  border-radius: 48rpx;
  font-size: 30rpx;
  font-weight: 600;
}

.btn-next:disabled {
  opacity: 0.5;
}

.btn-prev,
.btn-next {
  margin: 0;
  line-height: 1.4;
}

.btn-prev::after,
.btn-next::after {
  border: none;
}
</style>
