<template>
  <view class="container">
    <BossPageHeader title="岗位管理">
      <template #right>
        <view class="header-icon" @click="goStats">
          <text class="header-ico">📊</text>
        </view>
      </template>
    </BossPageHeader>

    <scroll-view scroll-y class="body" :style="{ paddingBottom: safeBottom + 70 + 'px' }">
      <!-- 搜索 -->
      <view class="search-bar">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          v-model="keyword"
          placeholder="搜索岗位名称、岗位编码..."
          confirm-type="search"
          @confirm="reload"
        />
      </view>

      <!-- Tab -->
      <view class="pos-tabs">
        <view
          class="pos-tab"
          :class="{ active: tab === 'all' }"
          @click="switchTab('all')"
        >
          全部
        </view>
        <view
          class="pos-tab"
          :class="{ active: tab === 'on' }"
          @click="switchTab('on')"
        >
          进行中
        </view>
        <view
          class="pos-tab"
          :class="{ active: tab === 'off' }"
          @click="switchTab('off')"
        >
          已停用
        </view>
      </view>

      <!-- 统计 -->
      <view class="stat-bar">
        <view class="stat-cell">
          <text class="stat-cell-num">{{ stats.totalCount || 0 }}</text>
          <text class="stat-cell-label">岗位总数</text>
        </view>
        <view class="stat-cell">
          <text class="stat-cell-num orange">{{ stats.activeCount || 0 }}</text>
          <text class="stat-cell-label">在招岗位</text>
        </view>
        <view class="stat-cell">
          <text class="stat-cell-num gray">{{ stats.inactiveCount || 0 }}</text>
          <text class="stat-cell-label">停用岗位</text>
        </view>
      </view>

      <!-- 岗位列表 -->
      <view v-if="filteredList.length" class="pos-list">
        <view
          v-for="item in filteredList"
          :key="item.id"
          class="pos-card"
          @click="goDetail(item)"
        >
          <view class="pos-icon">
            <text class="pos-ico">💼</text>
          </view>
          <view class="pos-main">
            <view class="pos-name-line">
              <text class="pos-name">{{ item.name }}</text>
              <text v-if="item.status === 'on'" class="wb-tag green">在招中</text>
              <text v-else class="wb-tag gray">已停用</text>
            </view>
            <text class="pos-code">岗位编码：{{ item.code }}</text>
          </view>
          <view class="pos-right">
            <text class="pos-arrow">›</text>
            <text class="pos-hire"><text class="pos-hire-num">{{ item.applyCount || 0 }}</text> 人投递</text>
          </view>
        </view>
      </view>
      <view v-else class="empty">
        <text class="empty-ico">💼</text>
        <text class="empty-text">{{ loading ? "加载中…" : "暂无符合条件的岗位" }}</text>
      </view>
      <view class="bottom-space" />
    </scroll-view>

    <!-- 底部按钮 -->
    <view class="footer">
      <view class="primary-btn" @click="goCreate">
        <text class="primary-btn-ico">＋</text>新建岗位
      </view>
    </view>
  </view>
</template>

<script>
import BossPageHeader from "@/components/BossPageHeader.vue";
import { listPositions, getPositionStats, POSITION_STATUS_TEXT } from "@/api/position";

export default {
  components: { BossPageHeader },
  data() {
    return {
      keyword: "",
      tab: "all",
      list: [],
      stats: {},
      loading: false,
      safeBottom: 0,
    };
  },
  computed: {
    filteredList() {
      const kw = this.keyword.trim().toLowerCase();
      return this.list.filter((p) => {
        if (this.tab !== "all" && p.status !== this.tab) return false;
        if (kw) {
          const name = (p.name || "").toLowerCase();
          const code = (p.code || "").toLowerCase();
          if (!name.includes(kw) && !code.includes(kw)) return false;
        }
        return true;
      });
    },
  },
  onShow() {
    this.loadData();
  },
  onLoad() {
    try {
      const sysInfo = uni.getSystemInfoSync();
      this.safeBottom = sysInfo.safeAreaInsets?.bottom || 0;
    } catch (e) {
      // ignore
    }
  },
  methods: {
    POSITION_STATUS_TEXT,
    async loadData() {
      this.loading = true;
      try {
        const [list, stats] = await Promise.all([
          listPositions({ keyword: this.keyword, status: this.tab === "all" ? "" : this.tab }).catch(() => []),
          getPositionStats().catch(() => ({})),
        ]);
        this.list = Array.isArray(list) ? list : [];
        this.stats = stats || {};
      } catch (e) {
        console.warn("岗位列表加载失败", e);
        this.list = [];
      } finally {
        this.loading = false;
      }
    },
    async reload() {
      await this.loadData();
    },
    switchTab(tab) {
      if (this.tab === tab) return;
      this.tab = tab;
      this.loadData();
    },
    goDetail(item) {
      uni.navigateTo({ url: `/pages/boss/position-detail?id=${item.id}` });
    },
    goCreate() {
      uni.navigateTo({ url: "/pages/boss/position-form?mode=create" });
    },
    goStats() {
      uni.navigateTo({ url: "/pages/boss/position-stats" });
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
.header-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.header-ico {
  font-size: 18px;
}
.search-bar {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 22px;
  padding: 0 14px;
  height: 40px;
  margin-bottom: 12px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
}
.search-icon {
  font-size: 14px;
  color: #bbb;
  margin-right: 8px;
}
.search-input {
  flex: 1;
  font-size: 13px;
  color: #333;
}
.pos-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}
.pos-tab {
  padding: 7px 20px;
  border-radius: 18px;
  background: #fff;
  color: #666;
  font-size: 13.5px;
  cursor: pointer;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
  font-weight: 500;
  transition: all 0.2s;
}
.pos-tab.active {
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: #fff;
  font-weight: 600;
}
.stat-bar {
  background: #fff;
  border-radius: 14px;
  padding: 16px 8px;
  margin-bottom: 12px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
  display: flex;
}
.stat-cell {
  flex: 1;
  text-align: center;
  position: relative;
}
.stat-cell + .stat-cell::before {
  content: "";
  position: absolute;
  left: 0;
  top: 20%;
  height: 60%;
  width: 0.5px;
  background: #eee;
}
.stat-cell-num {
  font-size: 22px;
  font-weight: 700;
  color: #333;
}
.stat-cell-num.orange {
  color: #ff6b35;
}
.stat-cell-num.gray {
  color: #999;
}
.stat-cell-label {
  font-size: 11.5px;
  color: #999;
  margin-top: 4px;
}
.pos-list {
  margin-bottom: 12px;
}
.pos-card {
  background: #fff;
  border-radius: 14px;
  padding: 14px 15px;
  margin-bottom: 11px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
  gap: 12px;
}
.pos-card:active {
  transform: scale(0.99);
}
.pos-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #ffb84d, #f09a3e);
  display: flex;
  align-items: center;
  justify-content: center;
}
.pos-ico {
  font-size: 18px;
}
.pos-main {
  flex: 1;
  min-width: 0;
}
.pos-name-line {
  display: flex;
  align-items: center;
  gap: 8px;
}
.pos-name {
  font-size: 15px;
  font-weight: 600;
  color: #222;
}
.wb-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;
}
.wb-tag.green {
  background: #e8f5e9;
  color: #4caf50;
}
.wb-tag.gray {
  background: #f3f4f6;
  color: #999;
}
.pos-code {
  font-size: 11.5px;
  color: #aaa;
  margin-top: 4px;
}
.pos-right {
  text-align: right;
  flex-shrink: 0;
}
.pos-arrow {
  color: #ddd;
  font-size: 18px;
}
.pos-hire {
  font-size: 12px;
  color: #666;
  margin-top: 6px;
}
.pos-hire-num {
  color: #ff6b35;
  font-weight: 600;
}
.empty {
  text-align: center;
  padding: 60px 0;
}
.empty-ico {
  font-size: 40px;
  display: block;
  margin-bottom: 10px;
}
.empty-text {
  font-size: 13px;
  color: #999;
}
.bottom-space {
  height: 20px;
}
.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 10px 16px;
  padding-bottom: calc(10px + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 0.5px solid #f0f0f0;
}
.primary-btn {
  flex: 1;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: #fff;
  border-radius: 22px;
  padding: 11px 0;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
}
.primary-btn-ico {
  margin-right: 6px;
  font-size: 16px;
}
</style>
