<template>
  <view class="container">
    <BossPageHeader title="考勤打卡" />
    <scroll-view scroll-y class="body">
      <view class="date-line">{{ project.name || "项目" }} · 今日 {{ today }}</view>

      <!-- 今日统计 -->
      <view class="stats">
        <view class="stat">
          <text class="stat-value">{{ stats.shouldArrive || 0 }}</text>
          <text class="stat-label">应到</text>
        </view>
        <view class="stat">
          <text class="stat-value orange">{{ stats.actual || 0 }}</text>
          <text class="stat-label">实到</text>
        </view>
        <view class="stat">
          <text class="stat-value late">{{ stats.late || 0 }}</text>
          <text class="stat-label">迟到</text>
        </view>
        <view class="stat">
          <text class="stat-value absent">{{ stats.absent || 0 }}</text>
          <text class="stat-label">缺卡</text>
        </view>
      </view>

      <!-- 筛选 -->
      <view class="tab-bar">
        <view
          v-for="t in tabs"
          :key="t.key"
          class="tab-item"
          :class="{ active: filter === t.key }"
          @click="switchFilter(t.key)"
        >{{ t.label }}</view>
      </view>

      <!-- 打卡列表 -->
      <view class="card">
        <view v-for="r in filteredList" :key="r.id" class="row">
          <view class="avatar" :style="{ background: avatarColor(r.name) }">
            {{ (r.name || "?").charAt(0) }}
          </view>
          <view class="row-main">
            <view class="row-title">
              {{ r.name }}
              <text class="pill" :class="pillClass(r.status)">{{ ATTEND_STATUS_TEXT[r.status] || r.status }}</text>
            </view>
            <view class="row-desc">
              签到 {{ formatTime(r.signInTime) || "—" }} · 签退 {{ formatTime(r.signOutTime) || "—" }}
            </view>
          </view>
          <view class="row-right" :class="rightClass(r.status)">
            {{ rightText(r) }}
          </view>
        </view>
        <view v-if="!filteredList.length" class="empty-text">暂无考勤记录</view>
      </view>
      <view class="bottom-space" />
    </scroll-view>
  </view>
</template>

<script>
import BossPageHeader from "@/components/BossPageHeader.vue";
import {
  getProject,
  getAttendanceStats,
  listAttendance,
  avatarColor,
  ATTEND_STATUS_TEXT,
  formatShortDate,
} from "@/api/project";

export default {
  components: { BossPageHeader },
  data() {
    return {
      ATTEND_STATUS_TEXT,
      projectId: "",
      project: {},
      today: formatShortDate(new Date().toISOString().slice(0, 10)),
      stats: {},
      filter: "all",
      tabs: [
        { key: "all", label: "全部" },
        { key: "on", label: "出勤" },
        { key: "late", label: "迟到" },
        { key: "absent", label: "缺卡" },
      ],
      list: [],
    };
  },
  computed: {
    filteredList() {
      if (this.filter === "all") return this.list;
      return this.list.filter((r) => r.status === this.filter);
    },
  },
  onLoad(query) {
    this.projectId = query.id;
    this.load();
  },
  methods: {
    avatarColor,
    formatShortDate,
    async load() {
      try {
        const [project, stats, list] = await Promise.all([
          getProject(this.projectId).catch(() => ({})),
          getAttendanceStats(this.projectId).catch(() => ({})),
          listAttendance(this.projectId).catch(() => []),
        ]);
        this.project = project || {};
        this.stats = stats || {};
        this.list = Array.isArray(list) ? list : [];
      } catch (e) {
        console.warn("考勤加载失败", e);
        this.list = [];
      }
    },
    switchFilter(key) {
      this.filter = key;
    },
    formatTime(v) {
      if (!v) return "";
      const s = String(v).replace("T", " ");
      const m = s.match(/\d{4}-\d{2}-\d{2} (\d{2}:\d{2})/);
      return m ? m[1] : s.slice(11, 16);
    },
    pillClass(status) {
      return {
        on: "status-on",
        late: "status-late",
        absent: "status-absent",
        leave: "status-leave",
      }[status] || "status-on";
    },
    rightClass(status) {
      if (status === "late") return "c-late";
      if (status === "absent") return "c-absent";
      return "c-on";
    },
    rightText(r) {
      if (r.status === "late") return `迟到${r.lateMinutes || "?"}分`;
      if (r.status === "absent") return "缺卡";
      return "已完成";
    },
  },
};
</script>

<style lang="scss" scoped>
.container { display: flex; flex-direction: column; height: 100vh; background: #f3f4f6; }
.body { flex: 1; overflow-y: auto; padding: 12px 16px 0; }
.date-line { font-size: 13px; color: #999; margin-bottom: 10px; }
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
.stat-value.late { color: #d97706; }
.stat-value.absent { color: #dc2626; }
.stat-label { font-size: 11px; color: #999; margin-top: 3px; display: block; }
.tab-bar {
  display: flex;
  background: #fff;
  padding: 4px;
  border-radius: 12px;
  margin-bottom: 12px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
}
.tab-item {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  font-size: 13px;
  color: #888;
  border-radius: 10px;
}
.tab-item.active { background: linear-gradient(135deg, #fff0e8, #ffe0d0); color: #ff6b35; font-weight: 600; }
.card { background: #fff; border-radius: 14px; padding: 4px 16px; box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04); }
.row { display: flex; align-items: center; gap: 12px; padding: 13px 0; border-bottom: 0.5px solid #f5f5f5; }
.row:last-child { border-bottom: none; }
.avatar {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
}
.row-main { flex: 1; min-width: 0; }
.row-title { font-size: 14px; font-weight: 600; color: #333; display: flex; align-items: center; gap: 6px; }
.row-desc { font-size: 12px; color: #999; margin-top: 4px; }
.row-right { font-size: 11px; color: #999; }
.row-right.c-on { color: #999; }
.row-right.c-late { color: #d97706; }
.row-right.c-absent { color: #dc2626; }
.pill {
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 600;
}
.status-on { background: #e8f8ef; color: #10b981; }
.status-late { background: #fef3c7; color: #d97706; }
.status-absent { background: #fee2e2; color: #dc2626; }
.status-leave { background: #e0edff; color: #2563eb; }
.empty-text { text-align: center; padding: 24px 0; color: #bbb; font-size: 13px; }
.bottom-space { height: 16px; }
</style>
