<template>
  <view class="container">
    <BossPageHeader title="项目详情" />
    <scroll-view scroll-y class="body">
      <!-- 项目信息 -->
      <view class="card">
        <view class="detail-name">{{ project.name || "项目详情" }}</view>
        <view class="detail-row">
          <text class="detail-label">用工企业：</text>
          <text class="detail-value">{{ project.companyName || "—" }}</text>
        </view>
        <view class="detail-row">
          <text class="detail-label">负责人：</text>
          <text class="detail-value">{{ project.leaderName || "—" }}</text>
        </view>
        <view class="detail-row">
          <text class="detail-label">立项日期：</text>
          <text class="detail-value">{{ formatCnDate(project.establishDate) || "—" }}</text>
        </view>
        <view class="sign-btn" @click="showSignCode">
          <text class="sign-btn-ico">📷</text>签到码
        </view>
      </view>

      <!-- 快捷功能 -->
      <view class="card">
        <view class="quick-row">
          <view class="quick-item" @click="goAttendance">
            <view class="quick-icon"><text class="quick-emoji">👆</text></view>
            <text class="quick-label">考勤打卡</text>
          </view>
          <view class="quick-item" @click="goOnsite">
            <view class="quick-icon orange"><text class="quick-emoji">📍</text></view>
            <text class="quick-label">驻场管理</text>
          </view>
          <view class="quick-item" @click="goSettings">
            <view class="quick-icon red"><text class="quick-emoji">⚙️</text></view>
            <text class="quick-label">项目设置</text>
          </view>
        </view>
      </view>

      <!-- 项目成员 -->
      <view class="card">
        <view class="card-head">
          <text class="card-head-title">项目成员</text>
          <text class="card-head-more" @click="goMembers">查看 ›</text>
        </view>
        <view class="stats-row">
          <view class="stat">
            <text class="stat-value">{{ memberStats.all || 0 }}</text>
            <text class="stat-label">全部</text>
          </view>
          <view class="stat">
            <text class="stat-value">{{ memberStats.active || 0 }}</text>
            <text class="stat-label">在职</text>
          </view>
          <view class="stat">
            <text class="stat-value">{{ memberStats.left || 0 }}</text>
            <text class="stat-label">离职</text>
          </view>
          <view class="stat">
            <text class="stat-value">{{ memberStats.temp || 0 }}</text>
            <text class="stat-label">临时</text>
          </view>
        </view>
      </view>

      <!-- 人事管理 -->
      <view class="section-title">人事管理</view>
      <view class="card row-card">
        <view class="row-item" @click="goCheckin">
          <view class="row-icon"><text class="row-emoji">📋</text></view>
          <text class="row-title">签到记录</text>
          <text class="row-arrow">›</text>
        </view>
        <view class="row-item" @click="goOnboard">
          <view class="row-icon orange"><text class="row-emoji">🤝</text></view>
          <text class="row-title">入职记录</text>
          <text class="row-arrow">›</text>
        </view>
      </view>
      <view class="bottom-space" />
    </scroll-view>
  </view>
</template>

<script>
import BossPageHeader from "@/components/BossPageHeader.vue";
import {
  getProject,
  getMemberStats,
  formatCnDate,
} from "@/api/project";

export default {
  components: { BossPageHeader },
  data() {
    return {
      projectId: "",
      project: {},
      memberStats: {},
    };
  },
  onLoad(query) {
    this.projectId = query.id;
    this.load();
  },
  methods: {
    formatCnDate,
    async load() {
      try {
        const [project, stats] = await Promise.all([
          getProject(this.projectId).catch(() => ({})),
          getMemberStats(this.projectId).catch(() => ({})),
        ]);
        this.project = project || {};
        this.memberStats = stats || {};
      } catch (e) {
        console.warn("项目详情加载失败", e);
      }
    },
    goAttendance() {
      uni.navigateTo({ url: `/pages/boss/proj-attendance?id=${this.projectId}` });
    },
    goOnsite() {
      uni.navigateTo({ url: `/pages/boss/proj-onsite?id=${this.projectId}` });
    },
    goSettings() {
      uni.navigateTo({ url: `/pages/boss/proj-settings?id=${this.projectId}` });
    },
    goMembers() {
      uni.navigateTo({ url: `/pages/boss/proj-members?id=${this.projectId}` });
    },
    goCheckin() {
      uni.navigateTo({ url: `/pages/boss/proj-checkin?id=${this.projectId}` });
    },
    goOnboard() {
      uni.navigateTo({ url: `/pages/boss/proj-onboard?id=${this.projectId}` });
    },
    showSignCode() {
      uni.navigateTo({ url: `/pages/boss/sign-code?id=${this.projectId}` });
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
.body { flex: 1; overflow-y: auto; padding: 12px 16px 0; }
.card {
  background: #fff;
  border-radius: 16px;
  padding: 14px 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
}
.detail-name { font-size: 17px; font-weight: 700; color: #333; margin-bottom: 10px; }
.detail-row { display: flex; font-size: 13px; margin-top: 7px; }
.detail-label { color: #999; width: 70px; flex-shrink: 0; }
.detail-value { color: #333; flex: 1; }
.sign-btn {
  margin-top: 14px;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: #fff;
  border-radius: 12px;
  padding: 11px 0;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
}
.sign-btn-ico { margin-right: 6px; }
.quick-row { display: flex; padding: 6px 0 2px; }
.quick-item { flex: 1; text-align: center; }
.quick-icon {
  width: 46px;
  height: 46px;
  margin: 0 auto 7px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 19px;
  color: #fff;
  background: linear-gradient(135deg, #ff8c5a, #ff6b35);
}
.quick-icon.orange { background: linear-gradient(135deg, #ffb84d, #f09a3e); }
.quick-icon.red { background: linear-gradient(135deg, #ff7743, #ff5c33); }
.quick-emoji { font-size: 20px; }
.quick-label { font-size: 12px; color: #666; }
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.card-head-title { font-size: 15px; font-weight: 600; color: #333; }
.card-head-more { font-size: 12px; color: #ff6b35; }
.stats-row { display: flex; }
.stat { flex: 1; text-align: center; position: relative; }
.stat:not(:last-child)::after {
  content: "";
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  height: 20px;
  width: 0.5px;
  background: #f0f0f0;
}
.stat-value { font-size: 18px; font-weight: 700; color: #ff6b35; display: block; }
.stat-label { font-size: 11px; color: #999; margin-top: 3px; display: block; }
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  padding: 4px 4px 10px;
}
.row-card { padding: 4px 16px; }
.row-item {
  display: flex;
  align-items: center;
  padding: 13px 0;
  border-bottom: 0.5px solid #f5f5f5;
}
.row-item:last-child { border-bottom: none; }
.row-icon {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  background: linear-gradient(135deg, #ff8c5a, #ff6b35);
}
.row-icon.orange { background: linear-gradient(135deg, #ffb84d, #f09a3e); }
.row-emoji { font-size: 16px; }
.row-title { flex: 1; font-size: 14px; color: #333; }
.row-arrow { color: #c8c8c8; font-size: 18px; }
.bottom-space { height: 16px; }
</style>
