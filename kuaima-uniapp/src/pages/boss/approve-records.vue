<template>
  <view class="container">
    <!-- 导航栏 -->
    <view class="nav-bar" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="nav-back" @click="goBack">
        <text>←</text>
      </view>
      <text class="nav-title">已审批的记录</text>
      <view class="nav-right">
        <text class="nav-dots">⋯</text>
      </view>
    </view>

    <scroll-view scroll-y class="body">
      <!-- 搜索 -->
      <view class="search-bar">
        <text class="search-ico">🔍</text>
        <input class="search-input" v-model="keyword" placeholder="请输入发薪单标题" @confirm="search" @input="onInput" />
      </view>

      <!-- 状态筛选 -->
      <view class="filter-tabs">
        <view class="filter-tab" :class="{ active: statusFilter === '' }" @click="setStatusFilter('')">全部</view>
        <view class="filter-tab" :class="{ active: statusFilter === 'approved' }" @click="setStatusFilter('approved')">已通过</view>
        <view class="filter-tab" :class="{ active: statusFilter === 'rejected' }" @click="setStatusFilter('rejected')">已驳回</view>
      </view>

      <view v-if="loading" class="page-state">加载中...</view>
      <view v-else-if="!records.length" class="empty-state">
        <text class="empty-ico">📋</text>
        <text class="empty-text">暂无已审批的发薪记录</text>
      </view>
      <view class="record-card" v-for="record in records" :key="record.id" @click="openDetail(record)">
        <view class="record-head">
          <view class="record-title">
            <text class="record-tag" :class="record.type">{{ getTypeText(record.type) }}</text>
            {{ record.title }}
          </view>
          <text class="record-status" :class="record.status">{{ getStatusText(record.status) }}</text>
        </view>
        <view class="record-line"><text class="lab">应发金额</text> {{ record.peopleCount || 0 }}人，<text class="record-amount">¥{{ formatYuan(record.amount) }}</text></view>
        <view class="record-line"><text class="lab">所属项目</text> {{ record.projectName || '—' }}</view>
        <view class="record-line"><text class="lab">制单人员</text> {{ record.creator || '—' }}</view>
        <view class="record-line"><text class="lab">审批日期</text> {{ formatTime(record.reviewTime) }}</view>
      </view>
      <view class="bottom-space" />
    </scroll-view>
  </view>
</template>

<script>
import { listApprovedPayrollOrders, listRejectedPayrollOrders, listReviewedPayrollOrders } from "@/api/backend";

function parsePayload(data) {
  return data?.data ?? data ?? {};
}

export default {
  data() {
    return {
      statusBarHeight: 0,
      keyword: "",
      searchTimer: null,
      loading: false,
      records: [],
      statusFilter: "",
    };
  },
  onLoad() {
    const info = typeof uni.getWindowInfo === "function"
      ? uni.getWindowInfo() : uni.getSystemInfoSync();
    this.statusBarHeight = Number(info.statusBarHeight || 0);
    this.loadRecords();
  },
  onUnload() {
    if (this.searchTimer) clearTimeout(this.searchTimer);
  },
  methods: {
    goBack() {
      uni.navigateBack({
        fail: () => uni.reLaunch({ url: "/pages/boss/approve" }),
      });
    },
    setStatusFilter(status) {
      this.statusFilter = status;
      this.loadRecords();
    },
    async loadRecords() {
      this.loading = true;
      try {
        let data;
        const params = { keyword: this.keyword.trim() || undefined };
        if (this.statusFilter === "approved") {
          data = await listApprovedPayrollOrders(params);
        } else if (this.statusFilter === "rejected") {
          data = await listRejectedPayrollOrders(params);
        } else {
          data = await listReviewedPayrollOrders(params);
        }
        const body = parsePayload(data);
        this.records = Array.isArray(body) ? body : (body.records || []);
      } catch (error) {
        this.records = [];
      } finally {
        this.loading = false;
      }
    },
    search() { this.loadRecords(); },
    onInput() {
      if (this.searchTimer) clearTimeout(this.searchTimer);
      this.searchTimer = setTimeout(() => this.loadRecords(), 350);
    },
    openDetail(record) {
      uni.showToast({ title: `原型演示：查看发薪单详情 ${record.title}`, icon: "none" });
    },
    formatYuan(fen) {
      const n = Number(fen || 0);
      return (n / 100).toFixed(2);
    },
    formatTime(value) {
      if (!value) return "—";
      const s = String(value).replace("T", " ").substring(0, 16);
      return s;
    },
    getTypeText(t) {
      const map = { wage: "工资", advance: "预支", other: "其他" };
      return map[t] || t || "工资";
    },
    getStatusText(s) {
      const map = {
        approved: "审批通过",
        rejected: "已驳回",
        withdrawn: "已撤回",
      };
      return map[s] || s || "";
    },
  },
};
</script>

<style lang="scss" scoped>
.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f3f4f6;
}
.nav-bar {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #fff;
}
.nav-back, .nav-right { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; font-size: 18px; color: #333; }
.nav-dots { font-size: 18px; }
.nav-title { font-size: 17px; font-weight: 600; color: #333; }

.body { flex: 1; padding: 12px 16px 0; }

.search-bar {
  display: flex; align-items: center; gap: 8px; padding: 10px 14px; margin-bottom: 12px;
  background: #fff; border-radius: 22px;
}
.search-ico { font-size: 13px; color: #bbb; }
.search-input { flex: 1; font-size: 13px; color: #333; }

.filter-tabs {
  display: flex; gap: 8px; margin-bottom: 12px;
}
.filter-tab {
  padding: 6px 16px; border-radius: 16px; font-size: 12px; color: #666;
  background: #fff; border: 1px solid #e5e5e5;
}
.filter-tab.active { background: #FFF0E8; border-color: #FF6B35; color: #FF6B35; font-weight: 600; }

.page-state { padding: 32px 0; color: #999; font-size: 14px; text-align: center; }
.empty-state { text-align: center; padding: 80px 40px; }
.empty-ico { font-size: 60px; display: block; margin-bottom: 12px; }
.empty-text { font-size: 13px; color: #999; }

.record-card {
  background: #fff; border-radius: 16px; padding: 14px 16px; margin-bottom: 10px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
}
.record-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.record-title { font-size: 14px; font-weight: 600; color: #333; display: flex; align-items: center; gap: 6px; }
.record-tag { font-size: 11px; padding: 2px 7px; border-radius: 6px; background: #fff3ed; color: #ff6b35; font-weight: 500; }
.record-tag.advance { background: #fff8e6; color: #d48806; }
.record-tag.other { background: #e6f7ff; color: #1890ff; }
.record-status { font-size: 12px; font-weight: 500; }
.record-status.approved { color: #10B981; }
.record-status.rejected { color: #EF4444; }
.record-status.withdrawn { color: #999; }
.record-line { font-size: 12px; color: #888; margin-top: 5px; }
.record-line .lab { color: #B0B0B0; }
.record-amount { color: #FF6B35; font-weight: 600; }
.bottom-space { height: 20px; }
</style>
