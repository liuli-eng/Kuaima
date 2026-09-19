<template>
  <view class="container">
    <!-- 导航栏 -->
    <view class="nav-bar" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="nav-back" @click="goBack">
        <text>←</text>
      </view>
      <text class="nav-title">转账记录</text>
      <view class="nav-right">
        <text class="nav-dots">⋯</text>
      </view>
    </view>

    <scroll-view scroll-y class="body">
      <!-- 搜索行：下载 + 筛选 + 搜索 -->
      <view class="action-row">
        <view class="dl-btn" @click="goExport">
          <text class="dl-ico">⬇</text>
          <text>下载</text>
        </view>
        <view class="filter-btn" @click="goExport">
          <text>筛选</text>
          <text class="filter-arrow">▾</text>
        </view>
        <view class="search-bar">
          <text class="search-ico">🔍</text>
          <input class="search-input" v-model="keyword" placeholder="请输入转账标题" @confirm="loadRecords" @input="onInput" />
        </view>
      </view>

      <!-- 转账统计 -->
      <view class="stat-card">
        <view class="stat-title">
          <text>转账统计</text>
          <text class="stat-range">( {{ statRange }} )</text>
        </view>
        <view class="stat-row">
          <view class="stat-item">
            <text class="stat-value">{{ stats.count || 0 }}</text>
            <text class="stat-label">转账笔数</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ stats.people || 0 }}</text>
            <text class="stat-label">转账人数</text>
          </view>
          <view class="stat-item">
            <text class="stat-value orange">¥{{ formatYuan(stats.amount) }}</text>
            <text class="stat-label">转账金额</text>
          </view>
        </view>
      </view>

      <!-- 记录列表 -->
      <view v-if="loading" class="page-state">加载中...</view>
      <view v-else-if="!records.length" class="empty-state">
        <text class="empty-ico">💰</text>
        <text class="empty-text">暂无转账记录</text>
      </view>
      <view class="record-card" v-for="record in records" :key="record.id">
        <view class="record-head">
          <view class="record-title">
            <text class="record-tag" :class="record.type">{{ getTypeText(record.type) }}</text>
            {{ record.title }}
          </view>
          <text class="record-amount">¥{{ formatYuan(record.amount) }}</text>
        </view>
        <view class="record-line"><text class="lab">应发金额</text> {{ record.peopleCount || 0 }}人，¥{{ formatYuan(record.amount) }}</view>
        <view class="record-line"><text class="lab">所属项目</text> {{ record.projectName || '—' }}</view>
        <view class="record-line"><text class="lab">制单人员</text> {{ record.creator || '—' }}</view>
        <view class="record-line"><text class="lab">支付日期</text> {{ formatTime(record.payTime || record.reviewTime) }}</view>
      </view>
      <view class="bottom-space" />
    </scroll-view>
  </view>
</template>

<script>
import { listTransferRecords } from "@/api/backend";

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
      stats: { count: 0, people: 0, amount: 0 },
      statRange: "",
    };
  },
  onLoad() {
    const info = typeof uni.getWindowInfo === "function"
      ? uni.getWindowInfo() : uni.getSystemInfoSync();
    this.statusBarHeight = Number(info.statusBarHeight || 0);
    this.loadRecords();
  },
  onShow() {
    if (this.statusBarHeight) this.loadRecords();
  },
  onUnload() {
    if (this.searchTimer) clearTimeout(this.searchTimer);
  },
  methods: {
    goBack() {
      uni.navigateBack({
        fail: () => uni.reLaunch({ url: "/pages/boss/workbench" }),
      });
    },
    goExport() {
      uni.navigateTo({ url: "/pages/boss/transfer-export" });
    },
    async loadRecords() {
      this.loading = true;
      try {
        const data = await listTransferRecords({ keyword: this.keyword.trim() || undefined });
        const body = parsePayload(data);
        this.records = Array.isArray(body.records) ? body.records : (Array.isArray(body) ? body : []);
        this.stats = body.stats || { count: 0, people: 0, amount: 0 };
        this.statRange = body.range || "";
      } catch (error) {
        this.records = [];
      } finally {
        this.loading = false;
      }
    },
    onInput() {
      if (this.searchTimer) clearTimeout(this.searchTimer);
      this.searchTimer = setTimeout(() => this.loadRecords(), 350);
    },
    formatYuan(fen) {
      const n = Number(fen || 0);
      return (n / 100).toFixed(2);
    },
    formatTime(value) {
      if (!value) return "—";
      return String(value).replace("T", " ").substring(0, 16);
    },
    getTypeText(t) {
      const map = { wage: "工资", advance: "预支", other: "其他" };
      return map[t] || t || "工资";
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

.action-row {
  display: flex; align-items: center; gap: 10px; margin-bottom: 12px;
}
.dl-btn {
  display: inline-flex; align-items: center; gap: 5px; background: #fff;
  border-radius: 16px; padding: 8px 14px; font-size: 12px; color: #FF6B35;
  box-shadow: 0 1px 6px rgba(0,0,0,0.06); flex-shrink: 0;
}
.dl-ico { font-size: 12px; }
.filter-btn {
  display: inline-flex; align-items: center; gap: 4px; font-size: 12px;
  color: #666; flex-shrink: 0;
}
.filter-arrow { font-size: 9px; color: #999; }
.search-bar {
  flex: 1; display: flex; align-items: center; gap: 8px; padding: 8px 14px;
  background: #fff; border-radius: 22px;
}
.search-ico { font-size: 13px; color: #bbb; }
.search-input { flex: 1; font-size: 13px; color: #333; }

.stat-card {
  background: #fff; border-radius: 16px; padding: 14px 16px; margin-bottom: 12px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
}
.stat-title {
  font-size: 14px; font-weight: 600; color: #333;
  display: flex; align-items: center; justify-content: space-between;
}
.stat-range { font-size: 12px; color: #999; font-weight: 400; }
.stat-row { display: flex; margin-top: 14px; }
.stat-item { flex: 1; text-align: center; }
.stat-value { font-size: 18px; font-weight: 700; color: #333; display: block; }
.stat-value.orange { color: #FF6B35; }
.stat-label { font-size: 11px; color: #999; display: block; margin-top: 4px; }

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
.record-tag { font-size: 11px; padding: 2px 7px; border-radius: 6px; background: #fff3ed; color: #ff6b35; font-weight: 500; flex-shrink: 0; }
.record-tag.advance { background: #fff8e6; color: #d48806; }
.record-tag.other { background: #e6f7ff; color: #1890ff; }
.record-amount { color: #FF6B35; font-weight: 700; font-size: 15px; }
.record-line { font-size: 12px; color: #888; margin-top: 5px; }
.record-line .lab { color: #B0B0B0; }
.bottom-space { height: 20px; }
</style>
