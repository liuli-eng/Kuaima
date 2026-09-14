<template>
  <view class="container">
    <BossPageHeader title="入职记录" />
    <scroll-view scroll-y class="body">
      <!-- 统计 -->
      <view class="stats">
        <view class="stat">
          <text class="stat-value">{{ stats.all || 0 }}</text>
          <text class="stat-label">全部</text>
        </view>
        <view class="stat">
          <text class="stat-value orange">{{ stats.pending || 0 }}</text>
          <text class="stat-label">审核中</text>
        </view>
        <view class="stat">
          <text class="stat-value">{{ stats.passed || 0 }}</text>
          <text class="stat-label">已通过</text>
        </view>
        <view class="stat">
          <text class="stat-value reject">{{ stats.rejected || 0 }}</text>
          <text class="stat-label">已拒绝</text>
        </view>
      </view>

      <!-- Tab -->
      <view class="tab-bar">
        <view
          v-for="t in tabs"
          :key="t.key"
          class="tab-item"
          :class="{ active: status === t.key }"
          @click="switchStatus(t.key)"
        >
          {{ t.label }}
          <text v-if="t.key === 'pending' && stats.pending" class="tab-badge">{{ stats.pending }}</text>
        </view>
      </view>

      <!-- 列表 -->
      <view v-for="a in list" :key="a.id" class="apply-card">
        <view class="apply-head">
          <view class="apply-avatar" :style="{ background: avatarColor(a.name) }">
            {{ (a.name || "?").charAt(0) }}
          </view>
          <view class="apply-info">
            <view class="apply-name">
              {{ a.name }}
              <text class="apply-badge" :class="badgeClass(a.status)">{{ ONBOARD_STATUS_TEXT[a.status] || a.status }}</text>
            </view>
            <view class="apply-sub">{{ a.job || "岗位待定" }} · 申请加入本项目</view>
          </view>
        </view>
        <view class="apply-meta">
          <text>🕒 {{ formatTime(a.applyTime) || "—" }}</text>
          <text>📞 {{ a.phone || "—" }}</text>
        </view>
        <view v-if="a.status === 'pending'" class="apply-actions">
          <view class="apply-btn danger" @click="reject(a)">拒绝</view>
          <view class="apply-btn primary" @click="pass(a)">通过入职</view>
        </view>
        <view v-else-if="a.status === 'passed'" class="apply-meta">
          <text>✅ 已于 {{ formatCnDate(a.joinDate) || "—" }} 入职</text>
        </view>
      </view>

      <view v-if="!list.length" class="empty">
        <text class="empty-ico">📝</text>
        <text class="empty-text">{{ loading ? "加载中…" : "该状态下暂无入职申请" }}</text>
      </view>
      <view class="bottom-space" />
    </scroll-view>
  </view>
</template>

<script>
import BossPageHeader from "@/components/BossPageHeader.vue";
import {
  getOnboardStats,
  listOnboard,
  passOnboard,
  rejectOnboard,
  avatarColor,
  ONBOARD_STATUS_TEXT,
  formatCnDate,
} from "@/api/project";

export default {
  components: { BossPageHeader },
  data() {
    return {
      ONBOARD_STATUS_TEXT,
      projectId: "",
      stats: {},
      status: "pending",
      tabs: [
        { key: "pending", label: "审核中" },
        { key: "passed", label: "已通过" },
        { key: "rejected", label: "已拒绝" },
      ],
      list: [],
      loading: false,
    };
  },
  onLoad(query) {
    this.projectId = query.id;
    this.load();
  },
  methods: {
    avatarColor,
    formatCnDate,
    async load() {
      this.loading = true;
      try {
        const [stats, list] = await Promise.all([
          getOnboardStats(this.projectId).catch(() => ({})),
          listOnboard(this.projectId, this.status).catch(() => []),
        ]);
        this.stats = stats || {};
        this.list = Array.isArray(list) ? list : [];
      } catch (e) {
        console.warn("入职记录加载失败", e);
        this.list = [];
      } finally {
        this.loading = false;
      }
    },
    switchStatus(key) {
      if (this.status === key) return;
      this.status = key;
      this.load();
    },
    badgeClass(status) {
      return {
        pending: "pending",
        passed: "passed",
        rejected: "rejected",
      }[status] || "pending";
    },
    formatTime(v) {
      if (!v) return "";
      const s = String(v).replace("T", " ");
      return s.slice(0, 16);
    },
    async pass(a) {
      try {
        await passOnboard(this.projectId, a.id);
        uni.showToast({ title: "已通过入职", icon: "success" });
        this.load();
      } catch (e) {
        uni.showToast({ title: "操作失败", icon: "none" });
      }
    },
    async reject(a) {
      try {
        await rejectOnboard(this.projectId, a.id);
        uni.showToast({ title: "已拒绝", icon: "none" });
        this.load();
      } catch (e) {
        uni.showToast({ title: "操作失败", icon: "none" });
      }
    },
  },
};
</script>

<style lang="scss" scoped>
.container { display: flex; flex-direction: column; height: 100vh; background: #f3f4f6; }
.body { flex: 1; overflow-y: auto; padding: 12px 16px 0; }
.stats {
  display: flex;
  background: #fff;
  border-radius: 12px;
  padding: 14px 0;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
  margin-bottom: 12px;
}
.stat { flex: 1; text-align: center; }
.stat-value { font-size: 20px; font-weight: 700; color: #333; display: block; }
.stat-value.orange { color: #ff6b35; }
.stat-value.reject { color: #888; }
.stat-label { font-size: 11px; color: #999; margin-top: 3px; display: block; }
.tab-bar { display: flex; background: #fff; padding: 4px; border-radius: 12px; margin-bottom: 12px; box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04); }
.tab-item {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  font-size: 13px;
  color: #888;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}
.tab-item.active { background: linear-gradient(135deg, #fff0e8, #ffe0d0); color: #ff6b35; font-weight: 600; }
.tab-badge { background: #ffe0d0; color: #ff6b35; font-size: 10px; padding: 1px 6px; border-radius: 8px; }
.tab-item.active .tab-badge { background: #fff; color: #ff6b35; }
.apply-card { background: #fff; border-radius: 14px; padding: 14px; margin-bottom: 10px; box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04); }
.apply-head { display: flex; gap: 12px; align-items: center; margin-bottom: 10px; }
.apply-avatar {
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
.apply-info { flex: 1; min-width: 0; }
.apply-name { font-size: 15px; font-weight: 600; color: #333; display: flex; align-items: center; gap: 6px; }
.apply-sub { font-size: 12px; color: #999; margin-top: 3px; }
.apply-meta { display: flex; gap: 12px; font-size: 11px; color: #999; padding-top: 10px; border-top: 0.5px solid #f5f5f5; }
.apply-actions { display: flex; gap: 8px; margin-top: 10px; }
.apply-btn { flex: 1; padding: 8px 0; border-radius: 18px; font-size: 12px; border: none; font-weight: 500; }
.apply-btn.primary { background: linear-gradient(135deg, #ff6b35, #ff8c5a); color: #fff; }
.apply-btn.danger { background: #fee2e2; color: #dc2626; }
.apply-badge { font-size: 10px; padding: 2px 8px; border-radius: 10px; font-weight: 600; }
.apply-badge.pending { background: #ebf3ff; color: #2563eb; }
.apply-badge.passed { background: #e8f8ef; color: #10b981; }
.apply-badge.rejected { background: #f3f4f6; color: #888; }
.empty { text-align: center; padding: 40px 0; color: #bbb; }
.empty-ico { font-size: 32px; display: block; margin-bottom: 10px; }
.empty-text { font-size: 13px; }
.bottom-space { height: 16px; }
</style>
