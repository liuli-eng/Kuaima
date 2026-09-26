<template>
  <view class="container">
    <!-- 顶部企业信息区 -->
    <view class="top-header">
      <view class="status-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
        <text>{{ currentTime }}</text>
        <view class="status-icons">
          <text class="status-icon">📶</text>
          <text class="status-icon">📡</text>
          <text class="status-icon">🔋</text>
        </view>
      </view>
      <view class="company-nav">
        <text class="company-name">晴时科技</text>
        <view class="wb-capsule">
          <view class="cap-btn"><text class="cap-ico">⋯</text></view>
          <view class="cap-divider"></view>
          <view class="cap-btn"><text class="cap-ico">○</text></view>
        </view>
      </view>

      <!-- 入口卡片 -->
      <view class="entry-cards">
        <view class="entry-card members" @click="navigateTo('members')">
          <text class="entry-name">企业成员</text>
          <text class="entry-desc">内部员工 团队协作</text>
          <text class="entry-icon">👥</text>
        </view>
        <view class="entry-card projects" @click="navigateTo('projects')">
          <text class="entry-name">项目管理</text>
          <text class="entry-desc">签到考勤 入职离职</text>
          <text class="entry-icon">📊</text>
        </view>
      </view>
    </view>

    <!-- 滚动内容区 -->
    <scroll-view scroll-y class="scroll-area">
      <!-- 智能招聘 -->
      <view class="wb-section-title" style="margin-top:2px;">智能招聘</view>
      <view class="wb-grid">
        <view class="wb-grid-item" @click="navigateTo('resume')">
          <view class="wb-grid-icon" style="background: linear-gradient(135deg, #FF8C5A, #FF6B35)">
            <text class="grid-ico">📄</text>
          </view>
          <view class="wb-grid-info">
            <text class="wb-grid-name">简历库</text>
            <text class="wb-grid-desc">求职人员简历管理</text>
          </view>
        </view>
        <view class="wb-grid-item" @click="navigateTo('position')">
          <view class="wb-grid-icon" style="background: linear-gradient(135deg, #FFB84D, #F09A3E)">
            <text class="grid-ico">💼</text>
          </view>
          <view class="wb-grid-info">
            <text class="wb-grid-name">岗位管理</text>
            <text class="wb-grid-desc">零工岗位发布、编辑</text>
          </view>
        </view>
        <view class="wb-grid-item" @click="navigateTo('live-showcase')">
          <view class="wb-grid-icon" style="background: linear-gradient(135deg, #FF7743, #FF5C33)">
            <text class="grid-ico">📹</text>
          </view>
          <view class="wb-grid-info">
            <text class="wb-grid-name">直播橱窗</text>
            <text class="wb-grid-desc">抖音直播、带岗招聘</text>
          </view>
        </view>
        <view class="wb-grid-item" @click="navigateTo('talent-list')">
          <view class="wb-grid-icon" style="background: linear-gradient(135deg, #FFA94D, #FF8C42)">
            <text class="grid-ico">✅</text>
          </view>
          <view class="wb-grid-info">
            <text class="wb-grid-name">候选人</text>
            <text class="wb-grid-desc">报名人员自动分配</text>
          </view>
        </view>
      </view>

      <!-- 闪电发薪 -->
      <view class="wb-section-title">闪电发薪</view>
      <view class="wb-grid">
        <view class="wb-grid-item" @click="navigateTo('payroll')">
          <view class="wb-grid-icon" style="background: linear-gradient(135deg, #FF8C5A, #FF6B35)">
            <text class="grid-ico">💸</text>
          </view>
          <view class="wb-grid-info">
            <text class="wb-grid-name">批量发薪</text>
            <text class="wb-grid-desc">一键制单、批量发薪</text>
          </view>
        </view>
        <view class="wb-grid-item" @click="navigateTo('approve')">
          <view class="wb-grid-icon" style="background: linear-gradient(135deg, #FFB84D, #F09A3E)">
            <text class="grid-ico">📋</text>
          </view>
          <view class="wb-grid-info">
            <text class="wb-grid-name">待我审批</text>
            <text class="wb-grid-desc">审核转账申请</text>
          </view>
        </view>
        <view class="wb-grid-item" @click="navigateTo('transfers')">
          <view class="wb-grid-icon" style="background: linear-gradient(135deg, #FF7743, #FF5C33)">
            <text class="grid-ico">🔄</text>
          </view>
          <view class="wb-grid-info">
            <text class="wb-grid-name">转账记录</text>
            <text class="wb-grid-desc">转账记录及明细查询</text>
          </view>
        </view>
        <view class="wb-grid-item" @click="navigateTo('balance')">
          <view class="wb-grid-icon" style="background: linear-gradient(135deg, #FFA94D, #FF8C42)">
            <text class="grid-ico">👛</text>
          </view>
          <view class="wb-grid-info">
            <text class="wb-grid-name">余额查询</text>
            <text class="wb-grid-desc">商户号余额管理</text>
          </view>
        </view>
      </view>

      <!-- 员工管理 -->
      <view class="wb-section-title">员工管理</view>
      <view class="wb-grid">
        <view class="wb-grid-item" @click="navigateTo('payroll-employees')">
          <view class="wb-grid-icon" style="background: linear-gradient(135deg, #FF8C5A, #FF6B35)">
            <text class="grid-ico">👥</text>
          </view>
          <view class="wb-grid-info">
            <text class="wb-grid-name">员工列表</text>
            <text class="wb-grid-desc">企业员工信息管理</text>
          </view>
        </view>
        <view class="wb-grid-item" @click="navigateTo('project-detail')">
          <view class="wb-grid-icon" style="background: linear-gradient(135deg, #FFB84D, #F09A3E)">
            <text class="grid-ico">⏰</text>
          </view>
          <view class="wb-grid-info">
            <text class="wb-grid-name">考勤打卡</text>
            <text class="wb-grid-desc">员工签到考勤管理</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部TabBar -->
    <view class="tab-bar">
      <view v-if="canAccess('HOME_VIEW')" class="tab-item" @click="switchTab('home')">
        <view class="tab-icon-wrap">
          <text class="tab-ico">🏠</text>
        </view>
        <text class="tab-label">首页</text>
      </view>
      <view v-if="canAccess('ORDER_VIEW')" class="tab-item" @click="switchTab('order')">
        <view class="tab-icon-wrap">
          <text class="tab-ico">📅</text>
        </view>
        <text class="tab-label">招工订单</text>
      </view>
      <view v-if="canAccess('WORKBENCH_VIEW')" class="tab-item active">
        <view class="tab-icon-wrap active">
          <text class="tab-ico">💼</text>
        </view>
        <text class="tab-label active">工作台</text>
      </view>
      <view v-if="canAccess('MESSAGE_VIEW')" class="tab-item" @click="switchTab('message')">
        <view class="tab-icon-wrap">
          <text class="tab-ico">💬</text>
        </view>
        <text class="tab-label">消息</text>
      </view>
      <view class="tab-item" @click="switchTab('profile')">
        <view class="tab-icon-wrap">
          <text class="tab-ico">😊</text>
        </view>
        <text class="tab-label">我的</text>
      </view>
    </view>
  </view>
</template>

<script>
import { hasEnterprisePermission } from "@/api/enterprise-context";
export default {
  data() {
    return {
      statusBarHeight: 44,
      currentTime: "13:14",
    };
  },
  onLoad() {
    try {
      const sysInfo = uni.getSystemInfoSync();
      this.statusBarHeight = sysInfo.statusBarHeight || 44;
    } catch (e) {
      // ignore
    }
    const now = new Date();
    this.currentTime = `${now.getHours()}:${String(now.getMinutes()).padStart(2, "0")}`;
  },
  methods: {
    canAccess(code) { return hasEnterprisePermission(code); },
    navigateTo(page) {
      const pages = {
        members: "/pages/boss/members",
        projects: "/pages/boss/projects",
        resume: "/pages/boss/resume",
        position: "/pages/boss/position",
        "live-showcase": "/pages/boss/live-showcase",
        "talent-list": "/pages/boss/talent-list",
        payroll: "/pages/boss/payroll",
        approve: "/pages/boss/approve",
        transfers: "/pages/boss/transfers",
        balance: "/pages/boss/balance",
        "payroll-employees": "/pages/boss/payroll-employees",
        "project-detail": "/pages/boss/project-detail",
      };
      if (pages[page]) {
        uni.navigateTo({ url: pages[page] });
      }
    },
    switchTab(tab) {
      const pages = {
        home: "/pages/boss/home",
        order: "/pages/boss/order",
        message: "/pages/boss/message",
        profile: "/pages/boss/profile",
      };
      if (pages[tab]) {
        uni.reLaunch({ url: pages[tab] });
      }
    },
  },
};
</script>

<style lang="scss" scoped>
.container {
  height: 100vh;
  width: 100%;
  display: flex;
  flex-direction: column;
  background: #f3f4f6;
  overflow-x: hidden;
  box-sizing: border-box;
}

.top-header {
  background: linear-gradient(180deg, #ffd96f 0%, #ffe7a3 70%, #f7f7f7 100%);
  padding: 0 16px 20px;
  flex-shrink: 0;
  width: 100%;
  box-sizing: border-box;
}

.status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 20px;
  font-size: 12px;
  color: #4a3500;
}

.status-icons {
  display: flex;
  gap: 5px;
}

.status-icon {
  font-size: 12px;
}

.company-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 0 14px;
}

.company-name {
  font-size: 16px;
  font-weight: 600;
  color: #4a3500;
}

.wb-capsule {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 17px;
  padding: 0 6px;
  height: 34px;
}

.cap-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cap-ico {
  font-size: 16px;
  color: #4a3500;
}

.cap-divider {
  width: 1px;
  height: 16px;
  background: rgba(74, 53, 0, 0.2);
  margin: 0 2px;
}

.entry-cards {
  display: flex;
  gap: 12px;
  width: 100%;
  box-sizing: border-box;
}

.entry-card {
  flex: 1;
  border-radius: 16px;
  padding: 16px 14px;
  position: relative;
  overflow: hidden;
  min-height: 92px;
  cursor: pointer;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.entry-card:active {
  transform: scale(0.97);
}

.entry-card.members {
  background: linear-gradient(135deg, #ff7a45 0%, #ff9a62 100%);
  box-shadow: 0 6px 16px rgba(255, 122, 69, 0.35);
}

.entry-card.projects {
  background: linear-gradient(135deg, #f0a500 0%, #ffc53d 100%);
  box-shadow: 0 6px 16px rgba(240, 165, 0, 0.35);
}

.entry-name {
  font-size: 17px;
  font-weight: 700;
  color: #fff;
}

.entry-desc {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.9);
  margin-top: 6px;
}

.entry-icon {
  position: absolute;
  right: 10px;
  bottom: 8px;
  font-size: 34px;
  color: rgba(255, 255, 255, 0.3);
}

.scroll-area {
  flex: 1;
  min-height: 0;
  height: 0;
  overflow-y: auto;
  padding: 0 16px 20px;
  width: 100%;
  box-sizing: border-box;
  -webkit-overflow-scrolling: touch;
}

.wb-section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin: 16px 0 12px;
  padding-left: 4px;
}

.wb-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  width: 100%;
  box-sizing: border-box;
}

.wb-grid-item {
  background: #fff;
  border-radius: 14px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  box-sizing: border-box;
  min-width: 0;
}

.wb-grid-item:active {
  transform: scale(0.98);
}

.wb-grid-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.grid-ico {
  font-size: 20px;
}

.wb-grid-info {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.wb-grid-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.wb-grid-desc {
  font-size: 11px;
  color: #999;
  margin-top: 3px;
}

.tab-bar {
  flex-shrink: 0;
  height: 83px;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-top: 0.5px solid rgba(0, 0, 0, 0.05);
  display: flex;
  padding-bottom: 20px;
  z-index: 50;
  width: 100%;
  box-sizing: border-box;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.tab-item.active .tab-label {
  color: #FF6B35;
}

.tab-icon-wrap {
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  margin-bottom: 3px;
}

.tab-label {
  font-size: 10px;
  color: #999;
  font-weight: 500;
}

.tab-item.active .tab-icon-wrap {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  border-radius: 50%;
  width: 32px;
  height: 32px;
  color: white;
  margin-bottom: 2px;
  box-shadow: 0 4px 10px rgba(255, 107, 53, 0.3);
}
</style>
