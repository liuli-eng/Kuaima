<template>
  <view class="container">
    <BossPageHeader title="签到记录" />
    <scroll-view scroll-y class="body">
      <view class="date-line">{{ project.name || "项目" }} · 考勤汇总</view>

      <!-- 日期筛选 -->
      <view class="date-picker">
        <view
          class="date-chip"
          :class="{ active: range === '7' }"
          @click="switchRange('7')"
        >近7天</view>
        <view
          class="date-chip"
          :class="{ active: range === '30' }"
          @click="switchRange('30')"
        >近30天</view>
        <view
          class="date-chip"
          :class="{ active: range === 'custom' }"
          @click="switchRange('custom')"
        >自定义</view>
      </view>

      <view v-if="range === 'custom'" class="custom-range">
        <picker mode="date" :value="start" @change="onStart">
          <view class="range-box">{{ start || "开始日期" }}</view>
        </picker>
        <text class="range-sep">至</text>
        <picker mode="date" :value="end" @change="onEnd">
          <view class="range-box">{{ end || "结束日期" }}</view>
        </picker>
      </view>

      <!-- 每日签到情况 -->
      <view v-for="d in list" :key="d.date" class="day-card">
        <view class="day-head">
          <view class="day-date">
            {{ formatShortDate(d.date) }}
            <text class="day-week">{{ weekdayOf(d.date) }}</text>
          </view>
          <view class="day-badges">
            <text v-if="d.actual" class="day-badge green">实到 {{ d.actual }}</text>
            <text v-if="d.late" class="day-badge orange">迟到 {{ d.late }}</text>
            <text v-if="d.absent" class="day-badge red">缺卡 {{ d.absent }}</text>
          </view>
        </view>
        <view class="stat-mini">
          <view class="mini-item">
            <text class="mini-num" style="color: #10b981;">{{ d.actual || 0 }}</text>
            <text class="mini-label">出勤</text>
          </view>
          <view class="mini-item">
            <text class="mini-num" style="color: #d97706;">{{ d.late || 0 }}</text>
            <text class="mini-label">迟到</text>
          </view>
          <view class="mini-item">
            <text class="mini-num" style="color: #dc2626;">{{ d.absent || 0 }}</text>
            <text class="mini-label">缺卡</text>
          </view>
          <view class="mini-item">
            <text class="mini-num" style="color: #2563eb;">{{ d.leave || 0 }}</text>
            <text class="mini-label">请假</text>
          </view>
        </view>
      </view>

      <view v-if="!list.length" class="empty">
        <text class="empty-ico">📅</text>
        <text class="empty-text">{{ loading ? "加载中…" : "暂无签到记录" }}</text>
      </view>
      <view v-else class="end-tip">—— 已显示近 {{ range === "custom" ? "自定义区间" : range + " 天" }} ——</view>
      <view class="bottom-space" />
    </scroll-view>
  </view>
</template>

<script>
import BossPageHeader from "@/components/BossPageHeader.vue";
import {
  getProject,
  listCheckin,
  formatShortDate,
  weekdayOf,
} from "@/api/project";

function isoDaysAgo(n) {
  const d = new Date();
  d.setDate(d.getDate() - n);
  return d.toISOString().slice(0, 10);
}

export default {
  components: { BossPageHeader },
  data() {
    return {
      projectId: "",
      project: {},
      range: "7",
      start: isoDaysAgo(6),
      end: isoDaysAgo(0),
      list: [],
      loading: false,
    };
  },
  onLoad(query) {
    this.projectId = query.id;
    this.load();
  },
  methods: {
    formatShortDate,
    weekdayOf,
    async load() {
      this.loading = true;
      try {
        const [project, list] = await Promise.all([
          getProject(this.projectId).catch(() => ({})),
          listCheckin(this.projectId, this.start, this.end).catch(() => []),
        ]);
        this.project = project || {};
        this.list = Array.isArray(list) ? list : [];
      } catch (e) {
        console.warn("签到记录加载失败", e);
        this.list = [];
      } finally {
        this.loading = false;
      }
    },
    switchRange(r) {
      this.range = r;
      if (r === "7") {
        this.start = isoDaysAgo(6);
        this.end = isoDaysAgo(0);
      } else if (r === "30") {
        this.start = isoDaysAgo(29);
        this.end = isoDaysAgo(0);
      }
      this.load();
    },
    onStart(e) {
      this.start = e.detail.value;
      this.load();
    },
    onEnd(e) {
      this.end = e.detail.value;
      this.load();
    },
  },
};
</script>

<style lang="scss" scoped>
.container { display: flex; flex-direction: column; height: 100vh; background: #f3f4f6; }
.body { flex: 1; overflow-y: auto; padding: 12px 16px 0; }
.date-line { font-size: 13px; color: #999; margin-bottom: 10px; }
.date-picker { display: flex; gap: 10px; margin-bottom: 12px; }
.date-chip {
  flex: 1;
  text-align: center;
  padding: 9px 0;
  background: #fff;
  border-radius: 20px;
  font-size: 12px;
  color: #666;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
}
.date-chip.active { background: linear-gradient(135deg, #ff6b35, #ff8c5a); color: #fff; font-weight: 600; }
.custom-range { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.range-box {
  flex: 1;
  text-align: center;
  padding: 9px 0;
  background: #fff;
  border-radius: 10px;
  font-size: 13px;
  color: #666;
}
.range-sep { color: #999; font-size: 12px; }
.day-card {
  background: #fff;
  border-radius: 14px;
  padding: 14px;
  margin-bottom: 10px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
}
.day-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.day-date { font-size: 15px; font-weight: 600; color: #333; display: flex; align-items: center; gap: 6px; }
.day-week { font-size: 11px; color: #999; font-weight: 400; }
.day-badges { display: flex; gap: 6px; }
.day-badge { font-size: 10px; padding: 2px 8px; border-radius: 10px; font-weight: 600; }
.day-badge.green { background: #e8f8ef; color: #10b981; }
.day-badge.orange { background: #fef3c7; color: #d97706; }
.day-badge.red { background: #fee2e2; color: #dc2626; }
.stat-mini { display: flex; gap: 10px; padding-top: 8px; border-top: 0.5px dashed #eee; }
.mini-item { flex: 1; text-align: center; }
.mini-num { font-size: 17px; font-weight: 700; color: #333; display: block; }
.mini-label { font-size: 10px; color: #999; margin-top: 2px; display: block; }
.empty { text-align: center; padding: 40px 0; color: #bbb; }
.empty-ico { font-size: 32px; display: block; margin-bottom: 10px; }
.empty-text { font-size: 13px; }
.end-tip { text-align: center; font-size: 12px; color: #bbb; padding: 18px 0; }
.bottom-space { height: 16px; }
</style>
