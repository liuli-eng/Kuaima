<template>
  <view class="container">
    <!-- 导航栏 -->
    <view class="nav-bar" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="nav-back" @click="goBack">
        <text>←</text>
      </view>
      <text class="nav-title">下载</text>
      <view class="nav-right">
        <text class="nav-dots">⋯</text>
      </view>
    </view>

    <scroll-view scroll-y class="body">
      <!-- Tab切换 -->
      <view class="dl-tabs">
        <view class="dl-tab" @click="goTab('transfer-export')">转账记录</view>
        <view class="dl-tab" @click="goTab('transfer-detail')">转账明细</view>
        <view class="dl-tab active">明细汇总</view>
      </view>

      <!-- 时间范围摘要 -->
      <view class="range-card">
        <view class="range-row">
          <text class="range-label">📅 汇总周期</text>
          <text class="range-value" @click="pickRange">{{ rangeText }} ▾</text>
        </view>
      </view>

      <!-- 整体概览 -->
      <view v-if="loading" class="page-state">加载中...</view>
      <template v-else>
        <view class="summary-grid">
          <view class="summary-cell">
            <text class="slabel">💵 累计转账总额</text>
            <text class="svalue primary">¥{{ formatYuan(overview.totalAmount) }}</text>
            <text class="sub" v-if="overview.monthGrowth !== undefined">较上月 {{ overview.monthGrowth >= 0 ? '+' : '' }}{{ overview.monthGrowth }}%</text>
          </view>
          <view class="summary-cell">
            <text class="slabel">🧾 转账总笔数</text>
            <text class="svalue">{{ overview.totalCount }}</text>
            <text class="sub">成功 {{ overview.successCount }} · 失败 {{ overview.failedCount }}</text>
          </view>
          <view class="summary-cell">
            <text class="slabel">👥 覆盖员工数</text>
            <text class="svalue">{{ overview.peopleCount }}</text>
            <text class="sub" v-if="overview.peopleCount">平均 ¥{{ formatYuan(overview.avgPerPeople) }} / 人</text>
          </view>
          <view class="summary-cell">
            <text class="slabel">🏢 涉及项目</text>
            <text class="svalue">{{ overview.projectCount }}</text>
            <text class="sub" v-if="overview.projectCount">{{ overview.activeProjects }} 进行中 · {{ overview.projectCount - overview.activeProjects }} 已完成</text>
          </view>
        </view>

        <!-- 项目维度汇总 -->
        <view class="section-title">📊 项目维度汇总</view>
        <view class="proj-summary" v-for="p in projects" :key="p.projectId || p.projectName">
          <view class="proj-head">
            <view>
              <text class="proj-name">{{ p.projectName }}</text>
              <text class="proj-desc">{{ p.projectDesc || '—' }}</text>
            </view>
            <view class="proj-total">
              <text class="amt">¥{{ formatYuan(p.totalAmount) }}</text>
            </view>
          </view>
          <view class="bar-row">
            <view class="bar-label">
              <text>项目支出占比</text>
              <text>{{ p.percent }}%</text>
            </view>
            <view class="bar-track">
              <view class="bar-fill" :style="{ width: p.percent + '%' }"></view>
            </view>
          </view>
          <view class="stats-row">
            <view class="stats-item">
              <text class="sv">{{ p.orderCount }}</text>
              <text class="sl">转账笔数</text>
            </view>
            <view class="stats-item">
              <text class="sv">{{ p.peopleCount }}</text>
              <text class="sl">涉及人数</text>
            </view>
            <view class="stats-item">
              <text class="sv" :style="{ color: rateColor(p.successRate) }">{{ p.successRate }}%</text>
              <text class="sl">成功率</text>
            </view>
          </view>
        </view>
        <view v-if="!projects.length" class="empty-state">
          <text class="empty-ico">📊</text>
          <text class="empty-text">暂无汇总数据</text>
        </view>
      </template>
      <view class="bottom-space" />
    </scroll-view>

    <!-- 底部操作 -->
    <view class="footer">
      <view class="footer-btn outline" @click="pickRange">更换周期</view>
      <view class="footer-btn primary" :class="{ disabled: exporting }" @click="exportSummary">{{ exporting ? '导出中...' : '导出汇总报告' }}</view>
    </view>
  </view>
</template>

<script>
import { getTransferSummary, exportTransferSummary } from "@/api/backend";

function parsePayload(data) {
  return data?.data ?? data ?? {};
}

export default {
  data() {
    return {
      statusBarHeight: 0,
      loading: false,
      exporting: false,
      rangeText: "本月",
      startDate: "",
      endDate: "",
      overview: {
        totalAmount: 0, totalCount: 0, successCount: 0, failedCount: 0,
        peopleCount: 0, avgPerPeople: 0, projectCount: 0, activeProjects: 0,
      },
      projects: [],
    };
  },
  onLoad() {
    const info = typeof uni.getWindowInfo === "function"
      ? uni.getWindowInfo() : uni.getSystemInfoSync();
    this.statusBarHeight = Number(info.statusBarHeight || 0);
    this.loadSummary();
  },
  methods: {
    goBack() {
      uni.navigateBack({
        fail: () => uni.reLaunch({ url: "/pages/boss/transfers" }),
      });
    },
    goTab(page) {
      uni.redirectTo({ url: `/pages/boss/${page}` });
    },
    pickRange() {
      const now = new Date();
      const ym = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
      uni.showModal({
        title: "选择汇总周期",
        editable: true,
        placeholderText: `格式：${ym}-01 ~ ${ym}-30`,
        success: (res) => {
          if (res.confirm && res.content) {
            const m = res.content.trim().match(/^(\d{4}-\d{2}-\d{2})\s*~\s*(\d{4}-\d{2}-\d{2})$/);
            if (m) {
              this.startDate = m[1];
              this.endDate = m[2];
              this.rangeText = `${m[1]} ~ ${m[2]}`;
              this.loadSummary();
            } else {
              uni.showToast({ title: "请输入正确周期格式", icon: "none" });
            }
          }
        },
      });
    },
    async loadSummary() {
      this.loading = true;
      try {
        const data = await getTransferSummary({
          startDate: this.startDate || undefined,
          endDate: this.endDate || undefined,
        });
        const body = parsePayload(data);
        this.overview = { ...this.overview, ...(body.overview || {}) };
        this.projects = Array.isArray(body.projects) ? body.projects : [];
        if (!this.rangeText || this.rangeText === "本月") {
          this.rangeText = body.range || this.rangeText;
        }
      } catch (error) {
        this.projects = [];
      } finally {
        this.loading = false;
      }
    },
    async exportSummary() {
      if (this.exporting) return;
      this.exporting = true;
      try {
        await exportTransferSummary({
          startDate: this.startDate || undefined,
          endDate: this.endDate || undefined,
        });
        uni.showToast({ title: "导出汇总报告成功", icon: "success" });
      } catch (error) {
        uni.showToast({ title: error?.message || "导出失败", icon: "none" });
      } finally {
        this.exporting = false;
      }
    },
    rateColor(rate) {
      if (rate >= 98) return "#10B981";
      if (rate >= 95) return "#FF6B35";
      return "#DC2626";
    },
    formatYuan(fen) {
      const n = Number(fen || 0);
      return (n / 100).toFixed(2);
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

.dl-tabs {
  display: flex; background: #EBEBEB; border-radius: 12px; padding: 3px; margin-bottom: 16px;
}
.dl-tab {
  flex: 1; text-align: center; padding: 8px 0; font-size: 13px; color: #666; border-radius: 10px;
}
.dl-tab.active { background: #fff; color: #FF6B35; font-weight: 600; }

.range-card {
  background: #fff; border-radius: 16px; padding: 12px 16px; margin-bottom: 14px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
}
.range-row { display: flex; align-items: center; justify-content: space-between; font-size: 13px; }
.range-label { color: #333; }
.range-value { color: #999; }

.summary-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; margin-bottom: 14px; }
.summary-cell {
  background: #fff; border-radius: 12px; padding: 14px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
  display: flex; flex-direction: column;
}
.slabel { font-size: 12px; color: #999; }
.svalue { font-size: 20px; font-weight: 700; color: #333; margin-top: 6px; }
.svalue.primary { color: #FF6B35; }
.sub { font-size: 11px; color: #BBB; margin-top: 4px; }

.section-title {
  font-size: 12px; color: #666; font-weight: 600;
  margin: 4px 2px 10px;
}

.proj-summary {
  background: #fff; border-radius: 14px; padding: 14px 16px; margin-bottom: 10px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
}
.proj-head { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 10px; }
.proj-name { font-size: 14px; font-weight: 600; color: #333; display: block; }
.proj-desc { font-size: 11px; color: #999; margin-top: 3px; display: block; }
.proj-total { text-align: right; }
.proj-total .amt { font-size: 18px; font-weight: 700; color: #FF6B35; }

.bar-row { margin-top: 10px; }
.bar-label { font-size: 11px; color: #999; display: flex; justify-content: space-between; margin-bottom: 4px; }
.bar-track { height: 6px; background: #F3F4F6; border-radius: 3px; overflow: hidden; }
.bar-fill { height: 100%; background: linear-gradient(90deg, #FF6B35, #FF8C5A); border-radius: 3px; }

.stats-row {
  display: flex; gap: 10px; margin-top: 10px; padding-top: 10px;
  border-top: 1px dashed #F0F0F0;
}
.stats-item { flex: 1; text-align: center; }
.stats-item .sv { font-size: 15px; font-weight: 600; color: #333; display: block; }
.stats-item .sl { font-size: 11px; color: #999; margin-top: 2px; display: block; }

.page-state { padding: 32px 0; color: #999; font-size: 14px; text-align: center; }
.empty-state { text-align: center; padding: 60px 40px; }
.empty-ico { font-size: 60px; display: block; margin-bottom: 12px; }
.empty-text { font-size: 13px; color: #999; }

.footer {
  display: flex; gap: 12px; padding: 12px 16px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  background: #fff; border-top: 0.5px solid #eee;
}
.footer-btn { text-align: center; padding: 12px 0; border-radius: 24px; font-size: 14px; font-weight: 600; }
.footer-btn.outline { flex: 1; background: #fff; color: #333; border: 1px solid #e0e0e0; }
.footer-btn.primary { flex: 2; background: linear-gradient(135deg, #FF6B35, #FF8C5A); color: #fff; }
.footer-btn.disabled { opacity: 0.6; }
.bottom-space { height: 20px; }
</style>
