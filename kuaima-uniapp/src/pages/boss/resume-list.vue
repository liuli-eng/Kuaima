<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="wb-header" :style="{ paddingTop: statusBarHeight + 8 + 'px' }">
      <view class="wb-back" @click="goBack"><text class="back-ico">‹</text></view>
      <text class="wb-title">简历库</text>
      <view class="wb-capsule">
        <view class="cap-btn" @click="switchBatch"><text class="cap-ico">{{ batchMode ? '✕' : '⋯' }}</text></view>
        <view class="cap-divider"></view>
        <view class="cap-btn"><text class="cap-ico">○</text></view>
      </view>
    </view>

    <view class="wb-body" :class="{ 'has-footer': batchMode }">
      <!-- 搜索 + 筛选 -->
      <view class="search-row">
        <view class="wb-search flex">
          <text class="search-ico">🔍</text>
          <input type="text" v-model="keyword" placeholder="搜索姓名、职位、技能等" @confirm="loadList" />
        </view>
        <view class="filter-btn" @click="openFilter">
          <text class="filter-ico">⚙️</text>
        </view>
      </view>

      <!-- Tab -->
      <view class="rs-tabs">
        <view v-for="t in tabs" :key="t.key" class="rs-tab" :class="{ active: currentTab === t.key }" @click="switchTab(t.key)">
          {{ t.label }}
        </view>
      </view>
      <view class="list-meta">
        <text>{{ totalText }}</text>
        <view class="filter-entry" @click="openFilter">⚙️ 筛选</view>
      </view>

      <!-- 列表 -->
      <view v-if="loading" class="wb-empty"><text class="wb-empty-text">加载中...</text></view>
      <view v-else-if="!list.length" class="wb-empty">
        <view class="wb-empty-icon"><text>📄</text></view>
        <text class="wb-empty-text">暂无符合条件的简历</text>
      </view>

      <view v-for="r in list" :key="r.id" class="rs-card" :class="{ 'batch-mode': batchMode }" @click="onCardClick(r)">
        <view v-if="batchMode" class="rs-check" :class="{ on: selected[r.id] }" @click.stop="toggleSel(r.id)">
          <text v-if="selected[r.id]">✓</text>
        </view>
        <view class="rs-avatar" :style="{ background: avatarColor(r.name) }">{{ (r.name || '人').charAt(0) }}</view>
        <view class="rs-info">
          <view class="rs-name-line">
            <text class="rs-name">{{ r.name }}</text>
            <text class="rs-pos">{{ r.position || '' }}</text>
            <text class="fav-star" :class="{ on: r.favorite }" @click.stop="toggleFav(r)">★</text>
          </view>
          <text class="rs-sub">{{ r.education || '不限' }} · {{ r.experience || '—' }} · {{ r.age || '—' }}岁</text>
        </view>
        <view class="rs-right">
          <text class="rs-date">{{ r.date || '' }}</text>
          <text class="wb-tag" :class="statusCls(r.status)">{{ statusText(r.status) }}</text>
        </view>
      </view>
    </view>

    <!-- 批量操作底部条 -->
    <view class="batch-bar" v-if="batchMode">
      <text class="b-info">已选择 <text class="b-num">{{ Object.keys(selected).length }}</text> 份简历</text>
      <view class="b-btn" @click="batchAction('read')">✓ 已读</view>
      <view class="b-btn" @click="batchAction('fav')">★ 收藏</view>
      <view class="b-btn danger" @click="batchAction('del')">🗑 删除</view>
      <view class="b-btn primary" @click="batchAction('process')">📨 批量处理</view>
    </view>

    <!-- 筛选弹层 -->
    <view class="wb-modal" v-if="filterShow" @click.self="closeFilter">
      <view class="filter-sheet">
        <view class="filter-head">
          <text class="filter-title">简历筛选</text>
          <text class="filter-close" @click="closeFilter">✕</text>
        </view>
        <scroll-view scroll-y class="filter-body">
          <view class="filter-group">
            <text class="filter-group-title">职位类型</text>
            <view class="filter-opts">
              <view v-for="o in jobCategoryOpts" :key="o" class="filter-opt" :class="{ active: filter.jobCategory === o }" @click="filter.jobCategory = o">{{ o }}</view>
            </view>
          </view>
          <view class="filter-group">
            <text class="filter-group-title">工作经验</text>
            <view class="filter-opts">
              <view v-for="o in experienceOpts" :key="o" class="filter-opt" :class="{ active: filter.experience === o }" @click="filter.experience = o">{{ o }}</view>
            </view>
          </view>
          <view class="filter-group">
            <text class="filter-group-title">学历要求</text>
            <view class="filter-opts">
              <view v-for="o in educationOpts" :key="o" class="filter-opt" :class="{ active: filter.education === o }" @click="filter.education = o">{{ o }}</view>
            </view>
          </view>
          <view class="filter-group">
            <text class="filter-group-title">期望薪资</text>
            <view class="filter-opts">
              <view v-for="o in salaryOpts" :key="o" class="filter-opt" :class="{ active: filter.expectedSalary === o }" @click="filter.expectedSalary = o">{{ o }}</view>
            </view>
          </view>
        </scroll-view>
        <view class="filter-foot">
          <view class="wb-btn-outline" @click="resetFilter">重置</view>
          <view class="wb-btn-primary" @click="applyFilter">确定</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getResumes, toggleResumeFavorite, batchResumeAction, RESUME_STATUS_TEXT, RESUME_STATUS_CLS } from "@/api/resume";
import { avatarColor } from "@/api/project";

const EMPTY_FILTER = () => ({ jobCategory: "", experience: "", education: "", expectedSalary: "" });

export default {
  data() {
    return {
      statusBarHeight: 44,
      tabs: [
        { key: "all", label: "全部" },
        { key: "pending", label: "待处理" },
        { key: "viewed", label: "已查看" },
        { key: "sent", label: "已投递" },
        { key: "fav", label: "收藏" },
      ],
      currentTab: "all",
      keyword: "",
      filter: EMPTY_FILTER(),
      filterShow: false,
      filterDraft: EMPTY_FILTER(),
      list: [],
      total: 0,
      loading: false,
      batchMode: false,
      selected: {},
      jobCategoryOpts: ["全部", "分拣打包", "搬运装卸", "餐饮服务", "仓储理货", "生产制造", "物流快递", "其他"],
      experienceOpts: ["全部", "无经验可做", "1个月以内", "1-3个月", "3-6个月", "6个月以上"],
      educationOpts: ["全部", "初中及以上", "高中/中专", "大专及以上"],
      salaryOpts: ["全部", "150元以下", "150-200元/天", "200-300元/天", "300元以上"],
    };
  },
  computed: {
    totalText() {
      if (this.currentTab === "fav") return `共 ${this.total} 份收藏简历`;
      return `共 ${this.total} 份简历`;
    },
  },
  onLoad(options) {
    try {
      const info = uni.getSystemInfoSync();
      this.statusBarHeight = info.statusBarHeight || 44;
    } catch (e) {}
    if (options.tab) this.currentTab = options.tab;
    this.loadList();
  },
  methods: {
    avatarColor,
    statusText: (s) => RESUME_STATUS_TEXT[s] || s,
    statusCls: (s) => RESUME_STATUS_CLS[s] || "gray",
    async loadList() {
      this.loading = true;
      try {
        const filters = { tab: this.currentTab, keyword: this.keyword };
        Object.assign(filters, this.filter);
        const data = await getResumes(filters);
        this.list = data?.content || data || [];
        this.total = data?.totalElements || this.list.length;
      } catch (e) {
        uni.showToast({ title: e.message || "加载失败", icon: "none" });
      } finally {
        this.loading = false;
      }
    },
    switchTab(key) {
      this.currentTab = key;
      this.selected = {};
      this.loadList();
    },
    async toggleFav(r) {
      try {
        const updated = await toggleResumeFavorite(r.id);
        r.favorite = updated?.favorite ?? !r.favorite;
        uni.showToast({ title: r.favorite ? "已加入收藏" : "已取消收藏", icon: "none" });
      } catch (e) {
        uni.showToast({ title: e.message || "操作失败", icon: "none" });
      }
    },
    onCardClick(r) {
      if (this.batchMode) this.toggleSel(r.id);
      else uni.navigateTo({ url: `/pages/boss/resume-detail?id=${r.id}` });
    },
    toggleSel(id) {
      if (this.selected[id]) delete this.selected[id];
      else this.selected[id] = true;
      this.$forceUpdate();
    },
    switchBatch() {
      this.batchMode = !this.batchMode;
      this.selected = {};
    },
    async batchAction(action) {
      const ids = Object.keys(this.selected).map(Number);
      if (!ids.length) {
        uni.showToast({ title: "请先选择简历", icon: "none" });
        return;
      }
      const labels = { read: "已标记为已读", fav: "已加入收藏", del: "已删除所选简历", process: "已批量投递" };
      try {
        await batchResumeAction(ids, action);
        uni.showToast({ title: labels[action] || "操作成功", icon: "none" });
        this.selected = {};
        this.batchMode = false;
        this.loadList();
      } catch (e) {
        uni.showToast({ title: e.message || "操作失败", icon: "none" });
      }
    },
    openFilter() {
      this.filterDraft = { ...this.filter };
      this.filterShow = true;
    },
    closeFilter() {
      this.filterShow = false;
    },
    resetFilter() {
      this.filterDraft = EMPTY_FILTER();
    },
    applyFilter() {
      this.filter = this.filterDraft;
      this.closeFilter();
      this.loadList();
    },
    goBack() {
      if (this.batchMode) {
        this.batchMode = false;
        this.selected = {};
        return;
      }
      uni.navigateBack({ fail: () => uni.reLaunch({ url: "/pages/boss/resume" }) });
    },
  },
};
</script>

<style lang="scss" scoped>
.container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f3f4f6;
}

.wb-header {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff;
  padding: 6px 16px 12px;
  flex-shrink: 0;
}

.wb-back {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-ico {
  font-size: 22px;
  color: #333;
}

.wb-title {
  flex: 1;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.wb-capsule {
  display: flex;
  align-items: center;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 17px;
  padding: 0 6px;
  height: 32px;
}

.cap-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cap-ico {
  font-size: 14px;
  color: #666;
}

.cap-divider {
  width: 1px;
  height: 16px;
  background: rgba(0, 0, 0, 0.15);
  margin: 0 2px;
}

.wb-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px 20px;
}

.wb-body.has-footer {
  padding-bottom: 80px;
}

.search-row {
  display: flex;
  gap: 9px;
  margin-bottom: 12px;
}

.wb-search {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  border-radius: 12px;
  padding: 10px 14px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
}

.wb-search.flex {
  flex: 1;
}

.search-ico {
  font-size: 14px;
}

.wb-search input {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.filter-btn {
  width: 42px;
  justify-content: center;
  background: #fff;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
}

.filter-ico {
  font-size: 15px;
}

.rs-tabs {
  display: flex;
  background: #fff;
  border-radius: 14px 14px 0 0;
  padding: 0 6px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
}

.rs-tab {
  flex: 1;
  text-align: center;
  padding: 12px 2px;
  font-size: 14px;
  color: #888;
  position: relative;
  font-weight: 500;
}

.rs-tab.active {
  color: #ff6b35;
  font-weight: 600;
}

.rs-tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 22px;
  height: 3px;
  border-radius: 2px;
  background: #ff6b35;
}

.list-meta {
  background: #fff;
  padding: 10px 16px;
  font-size: 12px;
  color: #999;
  border-radius: 0 0 14px 14px;
  margin-bottom: 12px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.filter-entry {
  color: #666;
  display: flex;
  align-items: center;
  gap: 4px;
}

.rs-card {
  background: #fff;
  border-radius: 14px;
  padding: 13px 14px;
  margin-bottom: 10px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
  gap: 11px;
}

.rs-check {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 1.5px solid #ddd;
  flex-shrink: 0;
  display: none;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 11px;
}

.rs-card.batch-mode .rs-check {
  display: flex;
}

.rs-check.on {
  background: #ff6b35;
  border-color: #ff6b35;
}

.rs-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
}

.rs-info {
  flex: 1;
  min-width: 0;
}

.rs-name-line {
  display: flex;
  align-items: center;
  gap: 6px;
}

.rs-name {
  font-size: 15px;
  font-weight: 600;
  color: #222;
}

.rs-pos {
  font-size: 13px;
  color: #666;
}

.rs-sub {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
  display: block;
}

.fav-star {
  color: #ddd;
  font-size: 15px;
  margin-left: auto;
  flex-shrink: 0;
}

.fav-star.on {
  color: #ffb020;
}

.rs-right {
  text-align: right;
  flex-shrink: 0;
}

.rs-date {
  font-size: 11px;
  color: #bbb;
  margin-bottom: 6px;
  display: block;
}

.wb-tag {
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 5px;
  font-weight: 600;
}

.wb-tag.blue {
  background: #ebf3ff;
  color: #357abd;
}

.wb-tag.green {
  background: #e8f8ef;
  color: #10b981;
}

.wb-tag.orange {
  background: #fff7e0;
  color: #d97706;
}

.wb-tag.gray {
  background: #f5f5f5;
  color: #999;
}

.wb-empty {
  padding: 40px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.wb-empty-icon {
  font-size: 34px;
  color: #ddd;
  margin-bottom: 10px;
}

.wb-empty-text {
  font-size: 13px;
  color: #999;
}

.batch-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  padding: 12px 14px calc(12px + env(safe-area-inset-bottom));
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.b-info {
  font-size: 13px;
  color: #666;
  flex-shrink: 0;
  margin-right: auto;
}

.b-num {
  color: #ff6b35;
}

.b-btn {
  padding: 7px 11px;
  border-radius: 18px;
  font-size: 12px;
  background: #f5f6f8;
  color: #555;
}

.b-btn.danger {
  background: #fef2f2;
  color: #ff4d4f;
}

.b-btn.primary {
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: #fff;
  font-weight: 600;
}

/* 筛选弹层 */
.wb-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 90;
}

.filter-sheet {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 88%;
  max-width: 340px;
  background: #fff;
  z-index: 95;
  display: flex;
  flex-direction: column;
}

.filter-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 16px;
  border-bottom: 0.5px solid #f0f0f0;
}

.filter-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.filter-close {
  font-size: 17px;
  color: #999;
  padding: 4px;
}

.filter-body {
  flex: 1;
  padding: 16px;
}

.filter-group {
  margin-bottom: 20px;
}

.filter-group-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 11px;
  display: block;
}

.filter-opts {
  display: flex;
  flex-wrap: wrap;
  gap: 9px;
}

.filter-opt {
  padding: 7px 15px;
  border-radius: 16px;
  background: #f5f6f8;
  color: #666;
  font-size: 13px;
  border: 1px solid transparent;
}

.filter-opt.active {
  background: #fff0e8;
  color: #ff6b35;
  border-color: #ffd2bc;
  font-weight: 500;
}

.filter-foot {
  display: flex;
  gap: 12px;
  padding: 12px 16px 26px;
  border-top: 0.5px solid #f0f0f0;
}

.wb-btn-outline {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  border: 1px solid #e5e5e5;
  color: #666;
  border-radius: 20px;
  font-size: 15px;
}

.wb-btn-primary {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  border-radius: 20px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: #fff;
  box-shadow: 0 3px 8px rgba(255, 107, 53, 0.25);
}
</style>
