<template>
  <view class="container">
    <scroll-view scroll-y class="scroll-area">
      <!-- 头部 -->
      <view
        class="header-bar"
        :style="{
          paddingTop: `${statusBarHeight + 12}px`,
          paddingRight: `${menuSafeRight}px`,
        }"
      >
        <view class="title-row">
          <text class="page-title">招工订单</text>
        </view>
      </view>

      <!-- 提示条 -->
      <view class="notice-bar">
        <text class="notice-icon">●</text>
        <text class="notice-text"
          >通话将被录音，禁止交换电话号码和微信；零工完工后...</text
        >
      </view>

      <!-- 筛选标签 -->
      <view class="filter-tabs">
        <text class="filter-tab active">全部</text>
      </view>

      <!-- 实名认证Banner -->
      <view
        v-if="!isCertified"
        class="cert-bar"
        @click="navigateTo('realname')"
      >
        <view class="cert-left">
          <text class="cert-title">完成实名认证后获取更多招人权益</text>
        </view>
        <text class="cert-btn">立即实名 ›</text>
      </view>

      <!-- 列表头部 -->
      <view class="list-header">
        <text class="list-title">招工列表</text>
        <view style="display: flex; gap: 10px; align-items: center">
          <view class="filter-btn" @click="navigateTo('boss-filter')">
            <text class="filter-icon">⌕</text>
            <text>筛选</text>
            <text class="filter-arrow">⌄</text>
          </view>
          <view class="applicant-btn" @click="navigateTo('applicant-info')">
            <text>✓</text>
            <text>报名信息</text>
          </view>
        </view>
      </view>

      <!-- 招工卡片列表 -->
      <view id="jobList">
        <view class="job-card" v-for="job in filteredJobs" :key="job.id">
          <view class="job-card-header">
            <view class="job-title-wrap">
              <text class="job-title">{{ job.title }}</text>
            </view>
            <text class="job-status" :class="statusMap[job.status]?.class">
              {{ statusMap[job.status]?.text || job.statusText }}
            </text>
          </view>
          <view class="job-info-grid">
            <view class="info-item">
              <text class="info-icon">▣</text>
              <text class="info-label">招聘岗位：</text>
              <text class="info-value">{{ job.title }}</text>
            </view>
            <view class="info-item">
              <text class="info-icon">◷</text>
              <text class="info-label">工作时间：</text>
              <text class="info-value">{{ job.workTime }}</text>
            </view>
            <view class="info-item">
              <text class="info-icon">⌖</text>
              <text class="info-label">工作地点：</text>
              <text class="info-value">{{ job.location }}</text>
            </view>
            <view class="info-item wage">
              <text class="info-icon">¥</text>
              <text class="info-label">报酬：</text>
              <text class="info-value">{{ job.wage }}</text>
            </view>
            <view class="info-item">
              <text class="info-icon">♟</text>
              <text class="info-label">招募人数：</text>
              <text class="info-value">{{ job.recruitCount }}人</text>
            </view>
            <view class="info-item">
              <text class="info-icon">✓</text>
              <text class="info-label">当前报名：</text>
              <text class="info-value">{{ job.currentApply }}人</text>
            </view>
          </view>
          <view class="job-actions">
            <template v-if="job.status === 'recruiting'">
              <text
                class="job-btn btn-primary"
                @click="navigateTo('applicant-info', { orderId: job.id })"
                >查看报名</text
              >
              <text
                class="job-btn btn-secondary"
                @click="navigateTo('publish-info', { id: job.id })"
                >编辑招工</text
              >
              <text class="job-btn btn-danger" @click="cancelJob(job.id)"
                >取消招工</text
              >
            </template>
            <template v-else-if="job.status === 'ended'">
              <text
                class="job-btn btn-secondary"
                @click="confirmArrival(job.id)"
                >确认到岗</text
              >
              <text
                class="job-btn btn-link"
                @click="navigateTo('publish', { id: job.id, readonly: 1 })"
                >详情</text
              >
            </template>
            <template v-else-if="job.status === 'settling'">
              <text
                class="job-btn btn-primary"
                @click="navigateTo('suspend-settle', { orderId: job.id })"
                >去结算</text
              >
              <text
                class="job-btn btn-link"
                @click="navigateTo('publish', { id: job.id, readonly: 1 })"
                >详情</text
              >
            </template>
            <template v-else-if="job.status === 'completed'">
              <text
                class="job-btn btn-link"
                @click="navigateTo('publish', { id: job.id, readonly: 1 })"
                >详情</text
              >
            </template>
            <template v-else-if="job.status === 'cancelled'">
              <text
                class="job-btn btn-link"
                @click="navigateTo('publish', { id: job.id, readonly: 1 })"
                >详情</text
              >
            </template>
          </view>
        </view>
      </view>

      <view v-if="loading" class="list-state">正在加载招工订单...</view>
      <view v-else-if="filteredJobs.length === 0" class="empty-state">
        <text class="empty-icon">▣</text>
        <text>暂无招工订单</text>
      </view>

      <view style="height: 20px"></view>
    </scroll-view>

    <!-- 底部TabBar -->
    <view class="tab-bar">
      <view class="tab-item" @click="switchTab('home')">
        <view class="tab-icon-wrap">
          <text class="tab-icon">⌂</text>
        </view>
        <text class="tab-label">首页</text>
      </view>
      <view class="tab-item active" @click="switchTab('order')">
        <view class="tab-icon-wrap">
          <text class="tab-icon">▣</text>
        </view>
        <text class="tab-label">招工订单</text>
      </view>
      <view class="tab-item" @click="switchTab('message')">
        <view class="tab-icon-wrap">
          <text class="tab-icon">●</text>
        </view>
        <text class="tab-label">消息</text>
      </view>
      <view class="tab-item" @click="switchTab('profile')">
        <view class="tab-icon-wrap">
          <text class="tab-icon">☺</text>
        </view>
        <text class="tab-label">我的</text>
      </view>
    </view>

    <!-- 取消确认弹窗 -->
    <view
      class="modal-mask"
      v-if="showCancelModal"
      @click="showCancelModal = false"
    >
      <view class="modal-content" @click.stop>
        <text class="modal-title">确认取消招工？</text>
        <text class="modal-subtitle">{{ cancelTargetTitle }}</text>
        <text class="modal-desc"
          >取消后将不再接收零工报名，已报名的零工将收到通知</text
        >
        <view class="modal-actions">
          <text
            class="modal-btn modal-btn-cancel"
            @click="showCancelModal = false"
            >再想想</text
          >
          <text class="modal-btn modal-btn-confirm" @click="confirmCancel"
            >确认取消</text
          >
        </view>
      </view>
    </view>

    <!-- 确认到岗弹窗 -->
    <view
      class="modal-mask"
      v-if="showConfirmModal"
      @click="showConfirmModal = false"
    >
      <view class="modal-content" @click.stop>
        <text class="modal-title">确认全部零工已到岗？</text>
        <text class="modal-subtitle">{{ confirmTargetTitle }}</text>
        <text class="modal-desc"
          >确认后状态将变更为待结算，可前往结算页面进行付款</text
        >
        <view class="modal-actions">
          <text
            class="modal-btn modal-btn-cancel"
            @click="showConfirmModal = false"
            >再想想</text
          >
          <text class="modal-btn modal-btn-confirm" @click="confirmArrive"
            >确认到岗</text
          >
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import {
  changeOrderStatus,
  getCurrentUser,
  listOrderItems,
  listOrders as fetchOrders,
} from "@/api/backend";

function getSafeArea() {
  try {
    const info =
      typeof uni.getWindowInfo === "function"
        ? uni.getWindowInfo()
        : uni.getSystemInfoSync();
    let menuSafeRight = 16;
    // #ifdef MP-WEIXIN
    const menu = uni.getMenuButtonBoundingClientRect();
    if (menu?.left)
      menuSafeRight = Math.max(16, info.windowWidth - menu.left + 12);
    // #endif
    return {
      statusBarHeight: Number(info.statusBarHeight || 0),
      menuSafeRight,
    };
  } catch (_) {
    return { statusBarHeight: 0, menuSafeRight: 16 };
  }
}

const demoJobs = [
  {
    id: 1,
    title: "电商分拣打包",
    workTime: "08:00 ~ 18:00",
    location: "松江区车墩镇",
    wage: "180元/天",
    recruitCount: 5,
    currentApply: 13,
    status: "recruiting",
  },
  {
    id: 2,
    title: "餐饮服务员",
    workTime: "10:00 ~ 20:00",
    location: "松江区中山街道",
    wage: "150元/天",
    recruitCount: 2,
    currentApply: 5,
    status: "ended",
  },
  {
    id: 3,
    title: "快递搬运装卸工",
    workTime: "07:00 ~ 17:00",
    location: "松江区九亭镇",
    wage: "200元/天",
    recruitCount: 4,
    currentApply: 8,
    status: "settling",
  },
  {
    id: 4,
    title: "冷库分拣员",
    workTime: "06:00 ~ 16:00",
    location: "松江区泗泾镇",
    wage: "220元/天",
    recruitCount: 3,
    currentApply: 6,
    status: "completed",
  },
  {
    id: 5,
    title: "装配工",
    workTime: "08:00 ~ 18:00",
    location: "松江区新桥镇",
    wage: "250元/天",
    recruitCount: 5,
    currentApply: 11,
    status: "cancelled",
  },
  {
    id: 6,
    title: "超市理货员",
    workTime: "09:00 ~ 19:00",
    location: "松江区洞泾镇",
    wage: "160元/天",
    recruitCount: 2,
    currentApply: 4,
    status: "recruiting",
  },
];

const statusByBackend = {
  待审核: "pending",
  审核拒绝: "rejected",
  招工中: "recruiting",
  招工结束: "ended",
  待结算: "settling",
  已完成: "completed",
  取消招工: "cancelled",
};

function formatTime(value) {
  if (!value) return "--";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  return `${String(date.getHours()).padStart(2, "0")}:${String(date.getMinutes()).padStart(2, "0")}`;
}

function normalizeOrder(order, currentApply = 0) {
  const hourlyMatch = String(order.tags || "").match(/时薪:([\d.]+)/);
  const pieceMatch = String(order.tags || "").match(/计件单价:([\d.]+)/);
  const pieceUnitMatch = String(order.tags || "").match(/计件单位:([^,]+)/);
  const wage = hourlyMatch
    ? `${hourlyMatch[1]}元/小时`
    : pieceMatch
      ? `${pieceMatch[1]}元/${pieceUnitMatch?.[1] || "件"}`
      : `${Number(order.salary || 0)}元/天`;
  return {
    ...order,
    title: order.orderTitle || order.postion || "未命名岗位",
    workTime: `${formatTime(order.startTime)} ~ ${formatTime(order.endTime)}`,
    location: order.address || "地点待定",
    wage,
    recruitCount: Number(order.orderNum || 0),
    currentApply,
    status: statusByBackend[order.orderStatus] || "pending",
    statusText: order.orderStatus || "待审核",
  };
}

export default {
  data() {
    const safeArea = getSafeArea();
    return {
      ...safeArea,
      jobList: [],
      statusMap: {
        recruiting: { text: "招工中", class: "status-recruiting" },
        pending: { text: "待审核", class: "status-ended" },
        rejected: { text: "审核拒绝", class: "status-cancelled" },
        ended: { text: "招工结束", class: "status-ended" },
        settling: { text: "待结算", class: "status-settling" },
        completed: { text: "已完成", class: "status-completed" },
        cancelled: { text: "取消招工", class: "status-cancelled" },
      },
      currentFilter: "all",
      showCancelModal: false,
      showConfirmModal: false,
      cancelTargetId: null,
      cancelTargetTitle: "",
      confirmTargetId: null,
      confirmTargetTitle: "",
      loading: false,
      operating: false,
      isCertified: false,
      orderFilter: null,
    };
  },
  onShow() {
    this.loadOrderFilter();
    this.loadCertificationStatus();
    this.loadOrders();
  },
  onLoad() {
    uni.$on("filterChanged", this.applyOrderFilter);
  },
  onUnload() {
    uni.$off("filterChanged", this.applyOrderFilter);
  },
  computed: {
    filteredJobs() {
      return this.jobList.filter((job) => {
        if (this.currentFilter !== "all" && job.status !== this.currentFilter)
          return false;
        return this.matchesOrderFilter(job, this.orderFilter);
      });
    },
  },
  methods: {
    loadOrderFilter() {
      const saved = uni.getStorageSync("bossOrderFilter");
      this.orderFilter = saved && typeof saved === "object" ? saved : null;
    },
    applyOrderFilter(filter) {
      this.orderFilter = filter && typeof filter === "object" ? filter : null;
    },
    matchesOrderFilter(job, filter) {
      if (!filter) return true;
      if (filter.jobType && filter.jobType !== "全部") {
        const text = `${job.title || ""} ${job.tags || ""}`;
        const categoryMap = {
          餐饮服务: ["餐饮", "服务员", "厨师", "洗碗"],
          快递配送: ["快递", "配送", "分拣"],
          仓储物流: ["仓储", "物流", "仓库", "搬运"],
          制造业: ["电子", "工厂", "制造", "装配", "焊"],
          建筑装修: ["建筑", "装修", "施工"],
          零售促销: ["零售", "促销", "超市", "营业员"],
          家政服务: ["家政", "保洁", "保姆"],
        };
        const keywords = categoryMap[filter.jobType] || [filter.jobType];
        if (!keywords.some((keyword) => text.includes(keyword))) return false;
      }
      if (filter.salary && !["不限", "日结周结"].includes(filter.salary)) {
        const amount = Number(String(job.wage || "").match(/[\d.]+/)?.[0] || 0);
        if (filter.salary === "100元以下" && amount > 100) return false;
        if (filter.salary === "100-200元" && (amount < 100 || amount > 200))
          return false;
        if (filter.salary === "200-300元" && (amount < 200 || amount > 300))
          return false;
        if (filter.salary === "300元以上" && amount < 300) return false;
      }
      if (
        filter.location &&
        filter.location !== "不限" &&
        filter.location !== "同城"
      ) {
        const distance = Number(
          String(job.distance || "").match(/[\d.]+/)?.[0] || 0,
        );
        const limit = Number(String(filter.location).match(/[\d.]+/)?.[0] || 0);
        if (distance && limit && distance > limit) return false;
      }
      if (Array.isArray(filter.tags) && filter.tags.length) {
        const text = `${job.title || ""} ${job.wage || ""} ${job.tags || ""}`;
        if (!filter.tags.every((tag) => text.includes(tag))) return false;
      }
      return true;
    },
    applyCertificationStatus(user = {}) {
      const status = String(
        user.certStatus || user.certificationStatus || "",
      ).trim();
      this.isCertified =
        status === "已通过" ||
        status === "通过" ||
        status.toUpperCase() === "APPROVED";
    },
    async loadCertificationStatus() {
      const cachedUser = uni.getStorageSync("userInfo");
      if (cachedUser && typeof cachedUser === "object") {
        this.applyCertificationStatus(cachedUser);
      }
      try {
        const user = await getCurrentUser();
        if (user && typeof user === "object") {
          uni.setStorageSync("userInfo", user);
          this.applyCertificationStatus(user);
        }
      } catch (_) {
        // 用户资料加载失败时保留本地缓存状态，不影响订单列表使用。
      }
    },
    navigateTo(pageName, params = {}) {
      const bossPages = [
        "boss-employer",
        "boss-home",
        "boss-message",
        "boss-order",
        "boss-profile",
        "boss-publish",
        "publish",
        "search-worker",
        "select-job",
        "publish-info",
        "schedule-stats",
        "enterprise-cert",
        "enterprise-cert-form",
        "creditor-score",
        "talent-list",
        "expense-detail",
        "payment-detail",
        "recruit-manager",
        "recruit-address",
        "sub-account",
        "suspend-settle",
        "switch-account",
        "invite-code",
        "blacklist",
        "all-jobs",
        "boss-filter",
        "settlement",
        "contract",
        "system-notice",
        "missed-call",
        "signup-notice",
        "invite-friend",
        "service-chat",
        "insurance",
        "realname",
        "applicant-info",
      ];

      let url = `/pages/boss/${pageName}`;
      if (!bossPages.includes(pageName)) {
        url = `/pages/${pageName}`;
      }

      const query = Object.entries(params)
        .map(
          ([key, value]) =>
            `${encodeURIComponent(key)}=${encodeURIComponent(value)}`,
        )
        .join("&");
      uni.navigateTo({ url: query ? `${url}?${query}` : url });
    },
    async loadOrders() {
      if (this.loading) return;
      this.loading = true;
      try {
        const result = await fetchOrders({ page: 0, size: 100 });
        const orders = Array.isArray(result) ? result : result?.records || [];
        this.jobList = await Promise.all(
          orders.map(async (order) => {
            try {
              const items = await listOrderItems(order.id);
              return normalizeOrder(
                order,
                Array.isArray(items) ? items.length : 0,
              );
            } catch (_) {
              return normalizeOrder(order);
            }
          }),
        );
      } catch (error) {
        this.jobList = [];
        uni.showToast({
          title: error.message || "招工列表加载失败",
          icon: "none",
        });
      } finally {
        this.loading = false;
      }
    },
    switchTab(tab) {
      const tabPages = {
        home: "/pages/boss/home",
        order: "/pages/boss/order",
        message: "/pages/boss/message",
        profile: "/pages/boss/profile",
      };
      const target = tabPages[tab];
      const currentRoute = getCurrentPages().slice(-1)[0]?.route;
      if (!target || `/${currentRoute}` === target) return;
      uni.redirectTo({
        url: target,
        fail: (error) => {
          console.error("Boss 主导航跳转失败", error);
          uni.reLaunch({
            url: target,
            fail: () =>
              uni.showToast({ title: "页面跳转失败，请重试", icon: "none" }),
          });
        },
      });
    },
    cancelJob(id) {
      const job = this.jobList.find((j) => j.id === id);
      this.cancelTargetId = id;
      this.cancelTargetTitle = job ? job.title : "";
      this.showCancelModal = true;
    },
    async confirmCancel() {
      if (!this.cancelTargetId || this.operating) return;
      this.operating = true;
      try {
        await changeOrderStatus(this.cancelTargetId, "取消招工");
        uni.showToast({ title: "招工已取消", icon: "success" });
        this.showCancelModal = false;
        await this.loadOrders();
      } catch (error) {
        uni.showToast({ title: error.message || "取消失败", icon: "none" });
      } finally {
        this.operating = false;
      }
    },
    confirmArrival(id) {
      const job = this.jobList.find((j) => j.id === id);
      this.confirmTargetId = id;
      this.confirmTargetTitle = job ? job.title : "";
      this.showConfirmModal = true;
    },
    async confirmArrive() {
      if (!this.confirmTargetId || this.operating) return;
      this.operating = true;
      try {
        await changeOrderStatus(this.confirmTargetId, "待结算");
        uni.showToast({ title: "订单已进入待结算", icon: "success" });
        this.showConfirmModal = false;
        await this.loadOrders();
      } catch (error) {
        uni.showToast({ title: error.message || "状态更新失败", icon: "none" });
      } finally {
        this.operating = false;
      }
    },
  },
};
</script>

<style lang="scss" scoped>
.container {
  width: 100%;
  height: 100vh;
  background: #fff8e6;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
  background: #fff8e6;
}

.header-bar {
  background: linear-gradient(180deg, #ffd59e 0%, #ffe4b5 100%);
  padding: 12px 16px 16px;
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #8b4513;
}

.nav-icons {
  display: flex;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border-radius: 50px;
  padding: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.nav-icon-item {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-divider {
  width: 1px;
  height: 16px;
  background: #ddd;
  margin: 0 4px;
}

.notice-bar {
  background: rgba(255, 107, 53, 0.1);
  border-radius: 12px;
  padding: 10px 14px;
  margin: -2px 16px 12px;
  font-size: 12px;
  color: #d2691e;
  display: flex;
  align-items: center;
  gap: 8px;
}

.notice-icon,
.info-icon {
  color: #ff6b35;
  flex-shrink: 0;
  font-weight: 600;
}

.notice-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.filter-tabs {
  display: flex;
  gap: 16px;
  padding: 0 16px;
}

.filter-tab {
  padding: 8px 0;
  font-size: 14px;
  color: #666;
  position: relative;
}

.filter-tab.active {
  color: #ff6b35;
  font-weight: 600;
}

.filter-tab.active::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 3px;
  background: #ff6b35;
  border-radius: 2px;
}

.cert-bar {
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  margin: 16px;
  padding: 16px;
  border-radius: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: white;
}

.cert-title {
  font-size: 15px;
  font-weight: 600;
}

.cert-btn {
  background: white;
  color: #ff6b35;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px 12px;
}

.list-title {
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: white;
  border-radius: 16px;
  font-size: 12px;
  color: #666;
}

.filter-icon,
.filter-arrow {
  color: #999;
}

.filter-arrow {
  font-size: 12px;
}

.applicant-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  border-radius: 14px;
  font-size: 12px;
  color: white;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(255, 107, 53, 0.25);
}

/* 招工卡片 */
.job-card {
  background: #fff;
  margin: 0 16px 12px;
  border-radius: 14px;
  padding: 16px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.job-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-bottom: 12px;
  border-bottom: 1px solid #f5f5f5;
  margin-bottom: 12px;
}

.job-title-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.job-title {
  font-size: 16px;
  font-weight: 700;
  color: #333;
}

.job-status {
  font-size: 11px;
  padding: 3px 10px;
  border-radius: 10px;
  font-weight: 500;
  white-space: nowrap;
}

.status-recruiting {
  background: #fff3e0;
  color: #ff6b35;
}
.status-ended {
  background: #e6f7ff;
  color: #1890ff;
}
.status-settling {
  background: #fff8e6;
  color: #fa8c16;
}
.status-completed {
  background: #f6ffed;
  color: #52c41a;
}
.status-cancelled {
  background: #f5f5f5;
  color: #999;
}

.job-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px 16px;
  margin-bottom: 14px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #666;
  line-height: 1.5;
}

.info-icon {
  width: 14px;
  text-align: center;
  font-size: 12px;
}

.info-item .info-label {
  color: #999;
  font-size: 11px;
}

.info-item .info-value {
  color: #333;
  font-weight: 500;
  margin-left: 2px;
}

.info-item.wage .info-value {
  color: #ff6b35;
  font-weight: 600;
}

.info-item.wage .info-label {
  color: #ffb088;
}

.info-item:nth-child(odd) {
  padding-right: 8px;
  border-right: 1px dashed #f0f0f0;
}

.job-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f5f5f5;
}

.job-btn {
  padding: 6px 14px;
  border-radius: 14px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid transparent;
  white-space: nowrap;
}

.list-state,
.empty-state {
  text-align: center;
  color: #999;
  font-size: 14px;
  padding: 48px 20px;
}

.empty-icon {
  display: block;
  color: #d8d8d8;
  font-size: 48px;
  line-height: 1;
  margin-bottom: 14px;
}

.btn-primary {
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: white;
}

.btn-secondary {
  background: #fff;
  color: #666;
  border-color: #e0e0e0;
}

.btn-danger {
  background: #fff;
  color: #f5222d;
  border-color: #ffece8;
}

.btn-link {
  background: transparent;
  color: #1890ff;
  border: none;
  padding: 6px 4px;
}

/* TabBar样式 */
.tab-bar {
  flex-shrink: 0;
  min-height: 63px;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-top: 0.5px solid rgba(0, 0, 0, 0.05);
  display: flex;
  padding-bottom: env(safe-area-inset-bottom);
  z-index: 50;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
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

.tab-item.active .tab-label {
  color: #ff6b35;
}

.tab-item.active .tab-icon-wrap {
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  border-radius: 50%;
  width: 32px;
  height: 32px;
  color: white;
  margin-bottom: 2px;
  box-shadow: 0 4px 10px rgba(255, 107, 53, 0.3);
}

/* 弹窗样式 */
.modal-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-content {
  width: 280px;
  background: #fff;
  border-radius: 16px;
  padding: 24px 20px;
  text-align: center;
}

.modal-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.modal-subtitle {
  font-size: 13px;
  color: #999;
  margin-bottom: 16px;
}

.modal-desc {
  font-size: 12px;
  color: #666;
  margin-bottom: 20px;
  line-height: 1.6;
}

.modal-actions {
  display: flex;
  gap: 12px;
}

.modal-btn {
  flex: 1;
  padding: 10px 0;
  border-radius: 20px;
  font-size: 14px;
  text-align: center;
}

.modal-btn-cancel {
  border: 1px solid #e0e0e0;
  color: #666;
}

.modal-btn-confirm {
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: #fff;
}
</style>
