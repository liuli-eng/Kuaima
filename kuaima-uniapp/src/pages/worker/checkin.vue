<template>
  <view class="container">
    <view class="header">
      <view class="back-btn" @click="goBack">
        <text class="back-icon">‹</text>
      </view>
      <view class="title">扫码签到</view>
      <view class="placeholder"></view>
    </view>

    <view class="content">
      <!-- 加载中 -->
      <view v-if="loading" class="loading">
        <text class="loading-text">加载中...</text>
      </view>

      <!-- 签到表单 -->
      <view v-else class="checkin-card">
        <view class="project-name">{{ project.name || "项目签到" }}</view>
        <view class="project-company">{{ project.companyName || "" }}</view>

        <view class="info-section">
          <view class="info-item">
            <text class="info-label">用工企业</text>
            <text class="info-value">{{ project.companyName || "—" }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">负责人</text>
            <text class="info-value">{{ project.leaderName || "—" }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">项目地点</text>
            <text class="info-value">{{ project.location || "—" }}</text>
          </view>
        </view>

        <!-- 签到状态 -->
        <view v-if="alreadyCheckedIn" class="status-box success">
          <text class="status-icon">✓</text>
          <text class="status-text">今日已签到</text>
          <text class="status-time">签到时间：{{ checkInTime }}</text>
        </view>

        <!-- 签到按钮 -->
        <view v-else class="checkin-actions">
          <view class="btn-sign-in" @click="handleSignIn">
            <text class="btn-icon">👆</text>
            <text class="btn-text">签到打卡</text>
          </view>
        </view>
      </view>

      <!-- 提示 -->
      <view class="tips">
        <text class="tips-text">• 请确保已到达项目地点后再签到</text>
        <text class="tips-text">• 签到后请等待管理员确认</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      projectId: "",
      userId: "",
      project: {},
      loading: true,
      alreadyCheckedIn: false,
      checkInTime: "",
    };
  },
  onLoad(options) {
    this.projectId = options.projectId || options.id || "";
    // 获取用户ID（实际应从登录状态获取）
    this.userId = uni.getStorageSync("userId") || "";
    if (this.projectId) {
      this.loadProject();
      this.checkTodayStatus();
    } else {
      this.loading = false;
      uni.showToast({ title: "缺少项目信息", icon: "none" });
    }
  },
  methods: {
    async loadProject() {
      const { getProject } = await import("@/api/project");
      try {
        const res = await getProject(this.projectId);
        this.project = res.data || {};
      } catch (e) {
        console.warn("加载项目失败", e);
      } finally {
        this.loading = false;
      }
    },
    async checkTodayStatus() {
      // 检查今日是否已签到
      const today = new Date().toISOString().split("T")[0];
      const { listAttendance } = await import("@/api/project");
      try {
        const res = await listAttendance(this.projectId, today);
        const list = res.data || [];
        const myRecord = list.find((r) => r.userId === this.userId);
        if (myRecord) {
          this.alreadyCheckedIn = true;
          this.checkInTime = myRecord.signInTime || "";
        }
      } catch (e) {
        console.warn("检查签到状态失败", e);
      }
    },
    async handleSignIn() {
      if (!this.userId) {
        uni.showToast({ title: "请先登录", icon: "none" });
        return;
      }

      const { clock } = await import("@/api/project");
      try {
        await clock(this.projectId, {
          userId: this.userId,
          name: uni.getStorageSync("nickname") || "",
        });
        uni.showToast({ title: "签到成功", icon: "success" });
        this.alreadyCheckedIn = true;
        this.checkInTime = new Date().toLocaleTimeString();
      } catch (e) {
        uni.showToast({ title: "签到失败，请重试", icon: "none" });
      }
    },
    goBack() {
      uni.navigateBack();
    },
  },
};
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: #f3f4f6;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  height: 44px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.back-btn {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 24px;
  color: #333;
  font-weight: 700;
}

.title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
}

.placeholder {
  width: 34px;
}

.content {
  padding: 16px;
}

.loading {
  text-align: center;
  padding: 60px 0;
  color: #999;
}

.checkin-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.project-name {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  text-align: center;
}

.project-company {
  font-size: 13px;
  color: #999;
  text-align: center;
  margin-top: 6px;
}

.info-section {
  margin-top: 20px;
  padding: 16px 0;
  border-top: 1px solid #f5f5f5;
  border-bottom: 1px solid #f5f5f5;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
}

.info-label {
  font-size: 14px;
  color: #999;
}

.info-value {
  font-size: 14px;
  color: #333;
}

.status-box {
  margin-top: 20px;
  padding: 20px;
  border-radius: 12px;
  text-align: center;
}

.status-box.success {
  background: #e8f8ef;
}

.status-icon {
  font-size: 32px;
  color: #10b981;
  display: block;
}

.status-text {
  font-size: 16px;
  font-weight: 600;
  color: #10b981;
  display: block;
  margin-top: 8px;
}

.status-time {
  font-size: 12px;
  color: #666;
  display: block;
  margin-top: 4px;
}

.checkin-actions {
  margin-top: 20px;
}

.btn-sign-in {
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  border-radius: 14px;
  padding: 16px 0;
  text-align: center;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
}

.btn-icon {
  font-size: 20px;
  margin-right: 8px;
}

.btn-text {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.tips {
  margin-top: 20px;
  padding: 0 8px;
}

.tips-text {
  font-size: 12px;
  color: #999;
  display: block;
  line-height: 1.8;
}
</style>
