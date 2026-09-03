<template>
  <view class="container">
    <!-- 状态栏 -->
    <view class="status-bar">
      <text>19:53</text>
      <view class="status-icons">
        <text class="fa-solid fa-signal"></text>
        <text class="fa-solid fa-wifi"></text>
        <text class="fa-solid fa-battery-full"></text>
      </view>
    </view>

    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="fa-solid fa-arrow-left"></text>
      </view>
      <text class="nav-title">报名信息</text>
      <view class="nav-right"></view>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <!-- 搜索框 -->
      <view class="search-bar">
        <view class="search-input-wrap">
          <text class="fa-solid fa-search" style="color:#999;font-size:26rpx;margin-right:16rpx;"></text>
          <input type="text" placeholder="搜索岗位名称" v-model="searchText" @input="handleSearch" />
          <view class="search-clear" :class="{ show: searchText }" @click="clearSearch">
            <text class="fa-solid fa-xmark" style="font-size:20rpx;color:#fff;"></text>
          </view>
        </view>
      </view>

      <!-- 筛选Tab -->
      <view class="filter-tabs">
        <text
          v-for="(tab, index) in filterTabs"
          :key="index"
          class="filter-tab"
          :class="{ active: currentFilter === tab.value }"
          @click="switchFilter(tab.value)"
        >{{ tab.label }}</text>
      </view>

      <!-- 报名列表 -->
      <view class="applicant-list">
        <view class="applicant-record" v-for="record in filteredRecords" :key="record.id">
          <view class="record-header">
            <view class="record-job">
              <view class="record-job-icon">
                <text class="fa-solid" :class="getJobIcon(record.jobId)" style="font-size:32rpx;color:#FF6B35;"></text>
              </view>
              <view class="record-job-info">
                <text class="record-job-title">{{ jobs[record.jobId].title }}</text>
                <text class="record-job-meta">
                  <text class="fa-solid fa-calendar" style="margin-right:6rpx;color:#FF6B35;font-size:20rpx;"></text>
                  {{ jobs[record.jobId].date }} · 需{{ jobs[record.jobId].workers }}人
                </text>
              </view>
            </view>
            <text class="record-wage">¥{{ jobs[record.jobId].wage }}/天</text>
          </view>

          <view class="record-applicant">
            <view class="record-avatar">{{ applicants[record.applicantId].name.charAt(0) }}</view>
            <view class="record-applicant-info">
              <view class="record-applicant-name">
                {{ applicants[record.applicantId].name }}
                <text class="record-star">★ {{ applicants[record.applicantId].star }}</text>
                <text class="record-tag" :class="getTagClass(record.status)" v-if="applicants[record.applicantId].tag">
                  {{ applicants[record.applicantId].tag }}
                </text>
              </view>
              <view class="record-meta">
                <text><text class="fa-solid fa-phone" style="color:#FF6B35;font-size:20rpx;"></text>{{ applicants[record.applicantId].phone }}</text>
                <text><text class="fa-solid fa-location-dot" style="color:#FF6B35;font-size:20rpx;"></text>松江区</text>
              </view>
            </view>
          </view>

          <view class="record-footer">
            <text class="record-status" :class="statusMap[record.status].color">{{ statusMap[record.status].text }}</text>
            <view class="record-actions">
              <template v-if="record.status === 'pending'">
                <text class="record-btn primary" @click="handleRecord(record.id, 'accept')">录用</text>
                <text class="record-btn secondary" @click="handleRecord(record.id, 'reject')">拒绝</text>
              </template>
              <template v-else-if="record.status === 'accepted'">
                <text class="record-btn secondary" @click="handleRecord(record.id, 'arrive')">确认到岗</text>
              </template>
              <template v-else-if="record.status === 'arrived'">
                <text class="record-btn primary" @click="handleRecord(record.id, 'complete')">确认完工</text>
              </template>
            </view>
          </view>
        </view>
      </view>

      <view style="height: 40rpx;"></view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      searchText: '',
      currentFilter: 'all',
      filterTabs: [
        { label: '全部', value: 'all' },
        { label: '待录用', value: 'pending' },
        { label: '已录用', value: 'accepted' },
        { label: '已到岗', value: 'arrived' },
        { label: '已完成', value: 'completed' },
        { label: '已拒绝', value: 'rejected' }
      ],
      records: [
        { id: 1, applicantId: 1, jobId: 1, status: 'pending' },
        { id: 2, applicantId: 2, jobId: 1, status: 'accepted' },
        { id: 3, applicantId: 3, jobId: 2, status: 'accepted' },
        { id: 4, applicantId: 4, jobId: 3, status: 'arrived' },
        { id: 5, applicantId: 5, jobId: 1, status: 'pending' },
        { id: 6, applicantId: 6, jobId: 2, status: 'rejected' },
        { id: 7, applicantId: 7, jobId: 4, status: 'pending' },
        { id: 8, applicantId: 8, jobId: 3, status: 'completed' },
        { id: 9, applicantId: 9, jobId: 5, status: 'expired' },
        { id: 10, applicantId: 10, jobId: 4, status: 'pending' }
      ],
      applicants: {
        1: { name: '张伟', phone: '138****5678', star: 4.8, tag: '熟练' },
        2: { name: '李强', phone: '139****1234', star: 4.5, tag: '熟练' },
        3: { name: '陈静', phone: '138****5555', star: 4.3, tag: '' },
        4: { name: '赵敏', phone: '137****4321', star: 4.6, tag: '老员工' },
        5: { name: '王芳', phone: '136****9876', star: 4.2, tag: '' },
        6: { name: '孙丽', phone: '136****7777', star: 4.1, tag: '' },
        7: { name: '卫华', phone: '138****6666', star: 4.5, tag: '熟练' },
        8: { name: '周鹏', phone: '138****1111', star: 4.9, tag: '优秀' },
        9: { name: '沈涛', phone: '138****8888', star: 4.7, tag: '' },
        10: { name: '蒋磊', phone: '139****7777', star: 4.3, tag: '' }
      },
      jobs: {
        1: { title: '电商分拣打包工', date: '2026-08-21', workers: 3, wage: 180, status: 'recruiting' },
        2: { title: '餐饮服务员', date: '2026-08-21', workers: 2, wage: 150, status: 'full' },
        3: { title: '快递搬运装卸工', date: '2026-08-20', workers: 4, wage: 200, status: 'done' },
        4: { title: '冷库分拣员', date: '2026-08-22', workers: 2, wage: 220, status: 'recruiting' },
        5: { title: '装配工', date: '2026-08-19', workers: 5, wage: 250, status: 'expired' }
      },
      statusMap: {
        pending: { text: '待录用', color: 'pending' },
        accepted: { text: '已录用', color: 'accepted' },
        arrived: { text: '已到岗', color: 'arrived' },
        completed: { text: '已完成', color: 'completed' },
        rejected: { text: '已拒绝', color: 'rejected' },
        expired: { text: '已过期', color: 'expired' }
      },
      jobIconMap: {
        1: 'fa-box',
        2: 'fa-utensils',
        3: 'fa-truck',
        4: 'fa-snowflake',
        5: 'fa-tools'
      }
    }
  },
  computed: {
    filteredRecords() {
      let data = this.currentFilter === 'all' 
        ? this.records 
        : this.records.filter(r => r.status === this.currentFilter)
      
      if (this.searchText) {
        data = data.filter(r => {
          const job = this.jobs[r.jobId]
          return job.title.includes(this.searchText)
        })
      }
      
      return data
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    handleSearch() {
      // 搜索逻辑已通过computed实现
    },
    clearSearch() {
      this.searchText = ''
    },
    switchFilter(filter) {
      this.currentFilter = filter
    },
    getJobIcon(jobId) {
      return this.jobIconMap[jobId] || 'icon-briefcase'
    },
    getTagClass(status) {
      if (status === 'accepted' || status === 'arrived' || status === 'completed') {
        return 'green'
      }
      return ''
    },
    handleRecord(id, action) {
      const record = this.records.find(r => r.id === id)
      if (!record) return
      
      const actionText = { accept: '录用', reject: '拒绝', arrive: '确认到岗', complete: '确认完工' }[action]
      const applicant = this.applicants[record.applicantId]
      
      uni.showToast({ title: `已对「${applicant.name}」执行：${actionText}`, icon: 'success' })
      
      if (action === 'accept') record.status = 'accepted'
      else if (action === 'reject') record.status = 'rejected'
      else if (action === 'arrive') record.status = 'arrived'
      else if (action === 'complete') record.status = 'completed'
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
  height: 94rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 56rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}

.status-icons {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.nav-bar {
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  background: #fff;
}

.nav-back {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
}

.nav-right {
  width: 64rpx;
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
  background: #FFF8E6;
}

.search-bar {
  padding: 24rpx 32rpx;
  background: #fff;
}

.search-input-wrap {
  display: flex;
  align-items: center;
  background: #F5F5F5;
  border-radius: 40rpx;
  padding: 16rpx 28rpx;
}

.search-input-wrap input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 26rpx;
  color: #333;
}

.search-input-wrap input::placeholder {
  color: #BBB;
}

.search-clear {
  display: none;
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
  background: #CCC;
  align-items: center;
  justify-content: center;
}

.search-clear.show {
  display: flex;
}

.filter-tabs {
  display: flex;
  gap: 16rpx;
  padding: 0 32rpx 24rpx;
  overflow-x: auto;
}

.filter-tab {
  white-space: nowrap;
  padding: 12rpx 28rpx;
  font-size: 26rpx;
  color: #666;
  background: rgba(255,255,255,0.6);
  border-radius: 32rpx;
  flex-shrink: 0;
}

.filter-tab.active {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  color: white;
  font-weight: 600;
}

.applicant-list {
  padding: 0 32rpx 32rpx;
}

.applicant-record {
  background: #fff;
  border-radius: 28rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.04);
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 24rpx;
  border-bottom: 2rpx dashed #f0f0f0;
  margin-bottom: 24rpx;
}

.record-job {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.record-job-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #FFE4B5, #FFDAB9);
  display: flex;
  align-items: center;
  justify-content: center;
}

.record-job-info {
  flex: 1;
}

.record-job-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}

.record-job-meta {
  font-size: 22rpx;
  color: #999;
  margin-top: 4rpx;
}

.record-wage {
  font-size: 28rpx;
  font-weight: 600;
  color: #FF6B35;
}

.record-applicant {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
}

.record-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 30rpx;
  flex-shrink: 0;
}

.record-applicant-info {
  flex: 1;
}

.record-applicant-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.record-star {
  color: #FA8C16;
  font-size: 22rpx;
}

.record-tag {
  font-size: 20rpx;
  padding: 2rpx 12rpx;
  border-radius: 12rpx;
  background: #FFF8E6;
  color: #FA8C16;
}

.record-tag.green {
  background: #F6FFED;
  color: #52C41A;
}

.record-meta {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
  display: flex;
  gap: 20rpx;
  flex-wrap: wrap;
}

.record-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24rpx;
  padding-top: 20rpx;
  border-top: 2rpx solid #f5f5f5;
}

.record-status {
  font-size: 22rpx;
  padding: 6rpx 20rpx;
  border-radius: 20rpx;
  font-weight: 500;
}

.record-status.pending { background: #FFF8E6; color: #FA8C16; }
.record-status.accepted { background: #E6F7FF; color: #1890FF; }
.record-status.arrived { background: #F6FFED; color: #52C41A; }
.record-status.rejected { background: #FFF1F0; color: #FF4D4F; }
.record-status.completed { background: #F6FFED; color: #52C41A; }
.record-status.expired { background: #F5F5F5; color: #999; }

.record-actions {
  display: flex;
  gap: 16rpx;
}

.record-btn {
  padding: 10rpx 28rpx;
  border-radius: 28rpx;
  font-size: 24rpx;
  font-weight: 500;
  border: 2rpx solid;
}

.record-btn.primary { background: #FF6B35; color: white; border-color: #FF6B35; }
.record-btn.secondary { background: #fff; color: #666; border-color: #E8E8E8; }
</style>
