<template>
  <view class="container">
    <BossPageHeader title="岗位统计" />

    <scroll-view scroll-y class="body">
      <!-- 时间切换 -->
      <view class="range-tabs">
        <view
          class="range-tab"
          :class="{ active: range === 'week' }"
          @click="switchRange('week')"
        >
          本周
        </view>
        <view
          class="range-tab"
          :class="{ active: range === 'month' }"
          @click="switchRange('month')"
        >
          本月
        </view>
        <view
          class="range-tab"
          :class="{ active: range === 'quarter' }"
          @click="switchRange('quarter')"
        >
          本季度
        </view>
        <view
          class="range-tab"
          :class="{ active: range === 'custom' }"
          @click="switchRange('custom')"
        >
          自定义
        </view>
      </view>

      <!-- 4格统计 -->
      <view class="stat-grid4">
        <view class="stat4">
          <text class="stat4-num">{{ stats.hireCount || 0 }}</text>
          <text class="stat4-label">招聘总数</text>
        </view>
        <view class="stat4">
          <text class="stat4-num orange">{{ stats.applyCount || 0 }}</text>
          <text class="stat4-label">投递人数</text>
        </view>
        <view class="stat4">
          <text class="stat4-num">{{ stats.interviewCount || 0 }}</text>
          <text class="stat4-label">面试人数</text>
        </view>
        <view class="stat4">
          <text class="stat4-num">{{ stats.hiredCount || 0 }}</text>
          <text class="stat4-label">录用人数</text>
        </view>
      </view>

      <!-- 岗位热度排行 -->
      <view class="card">
        <view class="card-title">
          岗位热度排行
          <text class="more-link">更多 ›</text>
        </view>
        <view v-if="rankings.length">
          <view
            v-for="(item, idx) in rankings"
            :key="item.id"
            class="rank-item"
            @click="goDetail(item)"
          >
            <text class="rank-no" :class="{ top: idx < 3 }">{{ idx + 1 }}</text>
            <text class="rank-name">{{ item.name }}</text>
            <view class="rank-bar-wrap">
              <view class="rank-bar" :style="{ width: getBarWidth(item.hotCount) + '%' }" />
            </view>
            <text class="rank-num">{{ item.hotCount || 0 }}人</text>
          </view>
        </view>
        <view v-else class="empty-card">
          <text class="empty-card-text">暂无排行数据</text>
        </view>
      </view>

      <!-- 招聘进度趋势 -->
      <view class="card">
        <view class="card-title">招聘进度趋势</view>
        <view class="legend">
          <view class="legend-item">
            <view class="legend-dot" style="background: #ff6b35" />
            <text class="legend-text">投递</text>
          </view>
          <view class="legend-item">
            <view class="legend-dot" style="background: #3b82f6" />
            <text class="legend-text">录用</text>
          </view>
        </view>
        <view class="chart-wrap">
          <!-- 简化的趋势图 -->
          <view class="trend-chart">
            <view class="chart-grid">
              <view v-for="i in 4" :key="i" class="grid-line" />
            </view>
            <view class="chart-data">
              <view class="chart-row">
                <text class="chart-num">60</text>
              </view>
              <view class="chart-row">
                <text class="chart-num">40</text>
              </view>
              <view class="chart-row">
                <text class="chart-num">20</text>
              </view>
              <view class="chart-row">
                <text class="chart-num">0</text>
              </view>
            </view>
            <view class="chart-labels">
              <text class="chart-date">06-20</text>
              <text class="chart-date">06-21</text>
              <text class="chart-date">06-22</text>
              <text class="chart-date">06-23</text>
              <text class="chart-date">06-24</text>
              <text class="chart-date">06-25</text>
              <text class="chart-date">06-26</text>
            </view>
          </view>
        </view>
      </view>
      <view class="bottom-space" />
    </scroll-view>
  </view>
</template>

<script>
import BossPageHeader from "@/components/BossPageHeader.vue";
import { getPositionStats, getHotRankings } from "@/api/position";

export default {
  components: { BossPageHeader },
  data() {
    return {
      range: "week",
      stats: {},
      rankings: [],
    };
  },
  onShow() {
    this.loadData();
  },
  methods: {
    async loadData() {
      try {
        const [stats, rankings] = await Promise.all([
          getPositionStats().catch(() => ({})),
          getHotRankings(10).catch(() => []),
        ]);
        this.stats = stats || {};
        this.rankings = Array.isArray(rankings) ? rankings : [];
      } catch (e) {
        console.warn("岗位统计加载失败", e);
      }
    },
    switchRange(range) {
      this.range = range;
      if (range === "custom") {
        uni.showToast({ title: "原型演示：自定义统计时间区间", icon: "none" });
      }
    },
    getBarWidth(count) {
      if (!this.rankings.length) return 0;
      const max = Math.max(...this.rankings.map((r) => r.hotCount || 0), 1);
      return Math.round((count / max) * 100);
    },
    goDetail(item) {
      uni.navigateTo({ url: `/pages/boss/position-detail?id=${item.id}` });
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
.body {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px 0;
}
.range-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.range-tab {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  border-radius: 18px;
  background: #fff;
  color: #666;
  font-size: 13px;
  cursor: pointer;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
  font-weight: 500;
  transition: all 0.2s;
}
.range-tab.active {
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: #fff;
  font-weight: 600;
}
.stat-grid4 {
  background: #fff;
  border-radius: 14px;
  padding: 16px 4px;
  margin-bottom: 12px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
  display: flex;
}
.stat4 {
  flex: 1;
  text-align: center;
  position: relative;
}
.stat4 + .stat4::before {
  content: "";
  position: absolute;
  left: 0;
  top: 18%;
  height: 64%;
  width: 0.5px;
  background: #eee;
}
.stat4-num {
  font-size: 22px;
  font-weight: 700;
  color: #333;
}
.stat4-num.orange {
  color: #ff6b35;
}
.stat4-label {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
}
.card {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
}
.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.more-link {
  font-size: 12px;
  color: #999;
  cursor: pointer;
  font-weight: 400;
}
.rank-item {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 9px 0;
}
.rank-no {
  width: 20px;
  height: 20px;
  border-radius: 6px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11.5px;
  font-weight: 700;
  color: #999;
  background: #f3f4f6;
}
.rank-no.top {
  background: linear-gradient(135deg, #ff8c5a, #ff6b35);
  color: #fff;
}
.rank-name {
  font-size: 13px;
  color: #333;
  width: 104px;
  flex-shrink: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.rank-bar-wrap {
  flex: 1;
  height: 7px;
  background: #f3f4f6;
  border-radius: 4px;
  overflow: hidden;
}
.rank-bar {
  height: 100%;
  border-radius: 4px;
  background: linear-gradient(90deg, #ffb07a, #ff6b35);
  transition: width 0.3s;
}
.rank-num {
  font-size: 12px;
  color: #888;
  width: 38px;
  text-align: right;
  flex-shrink: 0;
}
.legend {
  display: flex;
  gap: 18px;
  justify-content: flex-end;
  margin-bottom: 6px;
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
}
.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.legend-text {
  font-size: 11.5px;
  color: #888;
}
.chart-wrap {
  width: 100%;
}
.trend-chart {
  position: relative;
  height: 180px;
}
.chart-grid {
  position: absolute;
  left: 30px;
  right: 0;
  top: 0;
  bottom: 20px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.grid-line {
  height: 1px;
  background: #f0f0f0;
}
.chart-data {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 20px;
}
.chart-row {
  display: flex;
  align-items: center;
  height: 25%;
}
.chart-num {
  font-size: 9px;
  color: #bbb;
  width: 24px;
  text-align: right;
}
.chart-labels {
  position: absolute;
  left: 30px;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: space-between;
}
.chart-date {
  font-size: 8.5px;
  color: #bbb;
}
.empty-card {
  text-align: center;
  padding: 20px 0;
}
.empty-card-text {
  font-size: 13px;
  color: #999;
}
.bottom-space {
  height: 20px;
}
</style>
