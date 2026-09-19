<template>
  <view class="container">
    <!-- 导航栏 -->
    <view class="nav-bar" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="nav-back" @click="goBack">
        <text>←</text>
      </view>
      <text class="nav-title">员工</text>
      <view class="nav-right">
        <text class="nav-dots">⋯</text>
      </view>
    </view>

    <scroll-view scroll-y class="body">
      <!-- 搜索 -->
      <view class="search-bar">
        <text class="search-ico">🔍</text>
        <input class="search-input" v-model="keyword" placeholder="请输入员工姓名" @confirm="search" @input="onInput" />
      </view>

      <!-- 员工统计 -->
      <view class="section-title">员工统计</view>
      <view class="stat-cards-row">
        <view class="stat-card">
          <text class="stat-num">{{ stats.total }}</text>
          <text class="stat-label">总人数</text>
        </view>
        <view class="stat-card">
          <text class="stat-num">{{ stats.monthNew }}</text>
          <text class="stat-label">本月新增</text>
        </view>
        <view class="stat-card">
          <text class="stat-num">{{ stats.todayNew }}</text>
          <text class="stat-label">今日新增</text>
        </view>
      </view>

      <!-- 员工列表 -->
      <view class="section-title">员工列表</view>
      <view v-if="loading" class="page-state">加载中...</view>
      <view v-else-if="!employees.length" class="empty-state">
        <text class="empty-ico">👥</text>
        <text class="empty-text">暂无员工数据</text>
      </view>
      <view class="emp-card" v-for="emp in employees" :key="emp.id" @click="goDetail(emp)">
        <view class="emp-avatar" :style="{ background: emp.avatarBg }">{{ emp.initial }}</view>
        <view class="emp-main">
          <view class="emp-name">{{ emp.name }}</view>
          <view class="emp-info">{{ emp.gender || '—' }} | {{ emp.age || '—' }}岁 | {{ maskPhone(emp.phone) }}</view>
          <view class="emp-time">添加时间：{{ formatDateTime(emp.addTime) }}</view>
        </view>
        <text class="emp-status" :class="emp.status">{{ getStatusText(emp.status) }}</text>
        <text class="row-arrow">›</text>
      </view>
      <view class="bottom-space" />
    </scroll-view>

    <!-- 底部Tab -->
    <view class="module-tabs">
      <view class="module-tab" @click="goPayroll">
        <text class="tab-ico">💰</text><text class="tab-label">发薪</text>
      </view>
      <view class="module-tab active" @click="goEmployees">
        <text class="tab-ico">👥</text><text class="tab-label">员工</text>
      </view>
    </view>
  </view>
</template>

<script>
import { listPayrollEmployees } from "@/api/backend";

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
      employees: [],
      stats: { total: 0, monthNew: 0, todayNew: 0 },
      avatarColors: [
        "linear-gradient(135deg, #FF6B35, #FF8C5A)",
        "linear-gradient(135deg, #FF7743, #FF5C33)",
        "linear-gradient(135deg, #FFB84D, #F09A3E)",
        "linear-gradient(135deg, #FFA94D, #FF8C42)",
        "linear-gradient(135deg, #F09A3E, #FFB84D)",
        "linear-gradient(135deg, #52C41A, #73D13D)",
        "linear-gradient(135deg, #1890FF, #40A9FF)",
      ],
    };
  },
  onLoad() {
    const info = typeof uni.getWindowInfo === "function"
      ? uni.getWindowInfo() : uni.getSystemInfoSync();
    this.statusBarHeight = Number(info.statusBarHeight || 0);
    this.loadEmployees();
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
    goPayroll() {
      uni.redirectTo({ url: "/pages/boss/payroll" });
    },
    goEmployees() {},
    goDetail(emp) {
      uni.navigateTo({ url: `/pages/boss/payroll-employee-detail?id=${emp.id}` });
    },
    async loadEmployees() {
      this.loading = true;
      try {
        const data = await listPayrollEmployees({ keyword: this.keyword.trim() || undefined });
        const body = parsePayload(data);
        const list = Array.isArray(body) ? body : (body.employees || []);
        this.stats = {
          total: body.total ?? (Array.isArray(body) ? body.length : 0),
          monthNew: body.monthNew ?? 0,
          todayNew: body.todayNew ?? 0,
        };
        this.employees = list.map((emp, idx) => {
          const initial = (emp.name || "?").slice(0, 1);
          let avatarBg = this.avatarColors[idx % this.avatarColors.length];
          if (emp.status === "temp") {
            avatarBg = "linear-gradient(135deg, #FFA94D, #FF8C42)";
          } else if (emp.status === "left") {
            avatarBg = "linear-gradient(135deg, #BFBFBF, #8C8C8C)";
          }
          return { ...emp, initial, avatarBg };
        });
      } catch (error) {
        this.employees = [];
      } finally {
        this.loading = false;
      }
    },
    search() {
      this.loadEmployees();
    },
    onInput() {
      if (this.searchTimer) clearTimeout(this.searchTimer);
      this.searchTimer = setTimeout(() => this.loadEmployees(), 350);
    },
    maskPhone(phone) {
      if (!phone || phone.length < 7) return phone || "—";
      return `${phone.substring(0, 3)}****${phone.substring(phone.length - 4)}`;
    },
    formatDateTime(value) {
      if (!value) return "—";
      const s = String(value).replace("T", " ").substring(0, 16);
      return s;
    },
    getStatusText(s) {
      const map = { active: "在职", temp: "临时", left: "离职" };
      return map[s] || "在职";
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

.search-bar {
  display: flex; align-items: center; gap: 8px; padding: 10px 14px;
  background: #fff; border-radius: 22px;
}
.search-ico { font-size: 13px; color: #bbb; }
.search-input { flex: 1; font-size: 13px; color: #333; }

.section-title { font-size: 12px; color: #999; margin: 14px 0 8px 2px; }

.stat-cards-row { display: flex; gap: 10px; margin-bottom: 4px; }
.stat-card {
  flex: 1; background: #fff; border-radius: 14px; padding: 13px 0;
  text-align: center; box-shadow: 0 1px 6px rgba(0,0,0,0.04);
}
.stat-num { font-size: 20px; font-weight: 700; color: #FF6B35; }
.stat-label { font-size: 11px; color: #999; margin-top: 3px; }

.page-state { padding: 32px 0; color: #999; font-size: 14px; text-align: center; }
.empty-state { text-align: center; padding: 60px 40px; }
.empty-ico { font-size: 60px; display: block; margin-bottom: 12px; }
.empty-text { font-size: 13px; color: #999; }

.emp-card {
  background: #fff; border-radius: 14px; padding: 13px 14px; margin-bottom: 10px;
  display: flex; align-items: center; gap: 12px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
}
.emp-avatar {
  width: 44px; height: 44px; border-radius: 12px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 18px; font-weight: 700;
}
.emp-main { flex: 1; min-width: 0; }
.emp-name { font-size: 15px; font-weight: 600; color: #333; }
.emp-info { font-size: 12px; color: #888; margin-top: 4px; }
.emp-time { font-size: 11px; color: #B0B0B0; margin-top: 4px; }

.emp-status {
  font-size: 11px; padding: 2px 8px; border-radius: 10px; font-weight: 500;
}
.emp-status.active { background: #E8F7EF; color: #16A34A; }
.emp-status.temp { background: #FFF1DB; color: #B45309; }
.emp-status.left { background: #F5F5F5; color: #8C8C8C; }

.row-arrow { color: #C8C8C8; font-size: 18px; }
.bottom-space { height: 20px; }

.module-tabs {
  display: flex; gap: 12px; padding: 10px 16px; padding-bottom: calc(10px + env(safe-area-inset-bottom));
  background: #fff; border-top: 0.5px solid #eee;
}
.module-tab {
  flex: 1; display: flex; flex-direction: column; align-items: center; gap: 4px;
  padding: 8px 0; border-radius: 12px; font-size: 12px; color: #999;
}
.module-tab.active { color: #ff6b35; background: #fff3ed; }
.tab-ico { font-size: 18px; }
.tab-label { font-size: 12px; }
</style>
