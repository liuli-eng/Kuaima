<template>
  <view class="container">
    <!-- 导航栏 -->
    <view
      class="nav-bar"
      :style="{
        paddingTop: `${statusBarHeight}px`,
        height: `${50 + statusBarHeight}px`,
      }"
    >
      <view class="nav-back" @click="goBack">
        <text>‹</text>
      </view>
      <text class="nav-title">报名信息</text>
      <view class="nav-right"></view>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <!-- 搜索框 -->
      <view class="search-bar">
        <view class="search-input-wrap">
          <text
            class="fa-solid fa-search"
            style="color: #999; font-size: 26rpx; margin-right: 16rpx"
          ></text>
          <input
            type="text"
            placeholder="搜索岗位名称"
            v-model="searchText"
            @input="handleSearch"
          />
          <view
            class="search-clear"
            :class="{ show: searchText }"
            @click="clearSearch"
          >
            <text
              class="fa-solid fa-xmark"
              style="font-size: 20rpx; color: #fff"
            ></text>
          </view>
        </view>
      </view>

      <!-- 筛选Tab -->
      <view class="filter-tabs">
        <text
          v-for="(tab, index) in filterTabs"
          :key="index"
          class="filter-tab"
          :class="{ active: currentFilter === tab.value }"
          @click="switchFilter(tab.value)"
          >{{ tab.label }}</text
        >
      </view>

      <!-- 报名列表 -->
      <view class="applicant-list">
        <view
          class="applicant-record"
          v-for="record in filteredRecords"
          :key="record.id"
        >
          <view class="record-header">
            <view class="record-job">
              <view class="record-job-icon">
                <text class="job-icon-text">▣</text>
              </view>
              <view class="record-job-info">
                <text class="record-job-title">{{
                  jobs[record.jobId]?.title || "招工岗位"
                }}</text>
                <text class="record-job-meta">
                  <text class="meta-icon">◷</text>
                  {{ jobs[record.jobId]?.date || "日期待定" }} · 需{{
                    jobs[record.jobId]?.workers || "-"
                  }}人
                </text>
              </view>
            </view>
            <text class="record-wage"
              >¥{{ jobs[record.jobId]?.wage || 0 }}/天</text
            >
          </view>

          <view class="record-applicant">
            <view class="record-avatar">{{
              (applicants[record.applicantId]?.name || "零工").charAt(0)
            }}</view>
            <view class="record-applicant-info">
              <view class="record-applicant-name">
                {{ applicants[record.applicantId]?.name || "零工" }}
                <text class="record-star"
                  >★ {{ applicants[record.applicantId]?.star || "-" }}</text
                >
                <text
                  class="record-tag"
                  :class="getTagClass(record.status)"
                  v-if="applicants[record.applicantId]?.tag"
                >
                  {{ applicants[record.applicantId]?.tag }}
                </text>
              </view>
              <view class="record-meta">
                <text
                  ><text class="meta-icon">☎</text
                  >{{ applicants[record.applicantId]?.phone || "未提供" }}</text
                >
                <text><text class="meta-icon">⌖</text>松江区</text>
              </view>
            </view>
          </view>

          <view class="record-footer">
            <text
              class="record-status"
              :class="statusMap[record.status].color"
              >{{ statusMap[record.status].text }}</text
            >
            <view class="record-actions">
              <template v-if="record.status === 'pending'">
                <text
                  class="record-btn primary"
                  @click="handleRecord(record.id, 'accept')"
                  >录用</text
                >
                <text
                  class="record-btn secondary"
                  @click="handleRecord(record.id, 'reject')"
                  >拒绝</text
                >
              </template>
              <template v-else-if="record.status === 'accepted'">
                <text
                  class="record-btn secondary"
                  @click="handleRecord(record.id, 'arrive')"
                  >确认到岗</text
                >
              </template>
              <template v-else-if="record.status === 'arrived'">
                <text
                  class="record-btn primary"
                  @click="handleRecord(record.id, 'complete')"
                  >确认完工</text
                >
              </template>
            </view>
          </view>
        </view>
      </view>

      <view v-if="filteredRecords.length === 0" class="empty-state">
        暂无报名信息
      </view>

      <view style="height: 40rpx"></view>
    </scroll-view>
  </view>
</template>

<script>
import {
  listOrders,
  listOrderItems,
  hireOrderItem,
  confirmOrderItemWork,
  finishOrderItem,
} from "@/api/backend";

export default {
  data() {
    return {
      statusBarHeight: 0,
      searchText: "",
      currentFilter: "all",
      filterTabs: [
        { label: "全部", value: "all" },
        { label: "待录用", value: "pending" },
        { label: "已录用", value: "accepted" },
        { label: "已到岗", value: "arrived" },
        { label: "已完成", value: "completed" },
        { label: "已拒绝", value: "rejected" },
      ],
      records: [],
      applicants: {},
      jobs: {},
      statusMap: {
        pending: { text: "待录用", color: "pending" },
        accepted: { text: "已录用", color: "accepted" },
        arrived: { text: "已到岗", color: "arrived" },
        completed: { text: "已完成", color: "completed" },
        rejected: { text: "已拒绝", color: "rejected" },
        expired: { text: "已过期", color: "expired" },
      },
      jobIconMap: {
        1: "fa-box",
        2: "fa-utensils",
        3: "fa-truck",
        4: "fa-snowflake",
        5: "fa-tools",
      },
    };
  },
  onLoad() {
    try {
      const info =
        typeof uni.getWindowInfo === "function"
          ? uni.getWindowInfo()
          : uni.getSystemInfoSync();
      this.statusBarHeight = Number(info.statusBarHeight || 0);
    } catch (_) {}
    this.loadApplicants();
  },
  computed: {
    filteredRecords() {
      let data =
        this.currentFilter === "all"
          ? this.records
          : this.records.filter((r) => r.status === this.currentFilter);

      if (this.searchText) {
        data = data.filter((r) => {
          const job = this.jobs[r.jobId];
          return job.title.includes(this.searchText);
        });
      }

      return data;
    },
  },
  methods: {
    async loadApplicants() {
      try {
        const pages = getCurrentPages();
        const options = pages[pages.length - 1]?.options || {};
        const result = await listOrders({ page: 0, size: 50 });
        let orders = Array.isArray(result) ? result : result?.records || [];
        if (options.orderId) {
          orders = orders.filter(
            (order) => String(order.id) === String(options.orderId),
          );
          if (!orders.length) orders = [{ id: options.orderId }];
        }
        const orderMap = Object.fromEntries(
          orders.map((order) => [String(order.id), order]),
        );
        const itemGroups = await Promise.all(
          orders.map((order) => listOrderItems(order.id).catch(() => [])),
        );
        const items = itemGroups.flat();
        if (!items.length) {
          this.records = [];
          this.applicants = {};
          this.jobs = {};
          return;
        }
        const statusMap = {
          已报名: "pending",
          已录用: "accepted",
          已到岗: "arrived",
          已完成: "completed",
          取消报名: "rejected",
        };
        this.records = items.map((item, index) => {
          const worker = item.user || item.worker || {};
          const order = orderMap[String(item.orderId)] || {};
          const applicantId = `api-${item.id || index}`;
          const jobId = `api-job-${item.orderId || index}`;
          this.applicants[applicantId] = {
            name:
              worker.nickname ||
              worker.name ||
              (item.userId ? `零工${item.userId}` : "零工"),
            phone: worker.phone || "未提供",
            star: worker.creditScore || "-",
            tag: worker.skills || "",
          };
          this.jobs[jobId] = {
            title:
              item.order?.orderTitle ||
              item.orderTitle ||
              order.orderTitle ||
              order.postion ||
              "招工岗位",
            date: item.workDate || item.applyDate || order.startTime || "",
            workers: item.order?.orderNum || order.orderNum || "-",
            wage: item.order?.salary || order.salary || 0,
          };
          return {
            id: item.id,
            applicantId,
            jobId,
            status: statusMap[item.status] || "pending",
          };
        });
      } catch (error) {
        this.records = [];
        this.applicants = {};
        this.jobs = {};
        uni.showToast({
          title: error.message || "报名列表加载失败",
          icon: "none",
        });
      }
    },
    goBack() {
      uni.navigateBack();
    },
    handleSearch() {
      // 搜索逻辑已通过computed实现
    },
    clearSearch() {
      this.searchText = "";
    },
    switchFilter(filter) {
      this.currentFilter = filter;
    },
    getJobIcon(jobId) {
      return this.jobIconMap[jobId] || "icon-briefcase";
    },
    getTagClass(status) {
      if (
        status === "accepted" ||
        status === "arrived" ||
        status === "completed"
      ) {
        return "green";
      }
      return "";
    },
    async handleRecord(id, action) {
      const record = this.records.find((r) => r.id === id);
      if (!record) return;

      const actionText = {
        accept: "录用",
        reject: "拒绝",
        arrive: "确认到岗",
        complete: "确认完工",
      }[action];
      const applicant = this.applicants[record.applicantId];

      try {
        if (action === "accept") await hireOrderItem(id);
        else if (action === "arrive") await confirmOrderItemWork(id);
        else if (action === "complete") await finishOrderItem(id);
      } catch (error) {
        return uni.showToast({
          title: error.message || "操作失败",
          icon: "none",
        });
      }
      uni.showToast({
        title: `已对「${applicant.name}」执行：${actionText}`,
        icon: "success",
      });

      if (action === "accept") record.status = "accepted";
      else if (action === "reject") record.status = "rejected";
      else if (action === "arrive") record.status = "arrived";
      else if (action === "complete") record.status = "completed";
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

.empty-state {
  padding: 180rpx 32rpx;
  color: #999;
  font-size: 26rpx;
  text-align: center;
}

.status-bar {
  height: 94rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 56rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}

.status-icons {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.nav-bar {
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  background: #fff;
}

.nav-back {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
}

.nav-right {
  width: 64rpx;
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
  background: #fff8e6;
}

.search-bar {
  padding: 24rpx 32rpx;
  background: #fff;
}

.search-input-wrap {
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 40rpx;
  padding: 16rpx 28rpx;
}

.search-input-wrap input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 26rpx;
  color: #333;
}

.search-input-wrap input::placeholder {
  color: #bbb;
}

.search-clear {
  display: none;
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
  background: #ccc;
  align-items: center;
  justify-content: center;
}

.search-clear.show {
  display: flex;
}

.filter-tabs {
  display: flex;
  gap: 16rpx;
  padding: 0 32rpx 24rpx;
  overflow-x: auto;
}

.filter-tab {
  white-space: nowrap;
  padding: 12rpx 28rpx;
  font-size: 26rpx;
  color: #666;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 32rpx;
  flex-shrink: 0;
}

.filter-tab.active {
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: white;
  font-weight: 600;
}

.applicant-list {
  padding: 0 32rpx 32rpx;
}

.applicant-record {
  background: #fff;
  border-radius: 28rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 24rpx;
  border-bottom: 2rpx dashed #f0f0f0;
  margin-bottom: 24rpx;
}

.record-job {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.record-job-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #ffe4b5, #ffdab9);
  display: flex;
  align-items: center;
  justify-content: center;
}

.record-job-info {
  flex: 1;
}

.record-job-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}

.record-job-meta {
  font-size: 22rpx;
  color: #999;
  margin-top: 4rpx;
}

.record-wage {
  font-size: 28rpx;
  font-weight: 600;
  color: #ff6b35;
}

.record-applicant {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
}

.record-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 30rpx;
  flex-shrink: 0;
}

.record-applicant-info {
  flex: 1;
}

.record-applicant-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.record-star {
  color: #fa8c16;
  font-size: 22rpx;
}

.record-tag {
  font-size: 20rpx;
  padding: 2rpx 12rpx;
  border-radius: 12rpx;
  background: #fff8e6;
  color: #fa8c16;
}

.record-tag.green {
  background: #f6ffed;
  color: #52c41a;
}

.record-meta {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
  display: flex;
  gap: 20rpx;
  flex-wrap: wrap;
}

.record-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24rpx;
  padding-top: 20rpx;
  border-top: 2rpx solid #f5f5f5;
}

.record-status {
  font-size: 22rpx;
  padding: 6rpx 20rpx;
  border-radius: 20rpx;
  font-weight: 500;
}

.record-status.pending {
  background: #fff8e6;
  color: #fa8c16;
}
.record-status.accepted {
  background: #e6f7ff;
  color: #1890ff;
}
.record-status.arrived {
  background: #f6ffed;
  color: #52c41a;
}
.record-status.rejected {
  background: #fff1f0;
  color: #ff4d4f;
}
.record-status.completed {
  background: #f6ffed;
  color: #52c41a;
}
.record-status.expired {
  background: #f5f5f5;
  color: #999;
}

.record-actions {
  display: flex;
  gap: 16rpx;
}

.record-btn {
  padding: 10rpx 28rpx;
  border-radius: 28rpx;
  font-size: 24rpx;
  font-weight: 500;
  border: 2rpx solid;
}

.record-btn.primary {
  background: #ff6b35;
  color: white;
  border-color: #ff6b35;
}
.record-btn.secondary {
  background: #fff;
  color: #666;
  border-color: #e8e8e8;
}
/* 原型尺寸与跨端适配 */
.nav-bar {
  box-sizing: border-box;
}
.nav-back {
  width: 32px;
  height: 32px;
  font-size: 24px;
}
.nav-title {
  font-size: 17px;
}
.nav-right {
  width: 32px;
}
.scroll-area {
  height: 0;
  min-height: 0;
}
.search-bar {
  padding: 12px 16px;
}
.search-input-wrap {
  border-radius: 20px;
  padding: 8px 14px;
}
.search-input-wrap input {
  font-size: 13px;
}
.filter-tabs {
  gap: 8px;
  padding: 0 16px 12px;
}
.filter-tab {
  padding: 6px 14px;
  font-size: 13px;
  border-radius: 16px;
}
.applicant-list {
  padding: 0 16px 16px;
}
.applicant-record {
  border-radius: 14px;
  padding: 14px;
  margin-bottom: 10px;
}
.record-header {
  padding-bottom: 12px;
  margin-bottom: 12px;
}
.record-job {
  gap: 8px;
}
.record-job-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
}
.record-job-title {
  font-size: 14px;
}
.record-job-meta {
  font-size: 11px;
}
.record-wage {
  font-size: 14px;
}
.record-applicant {
  gap: 10px;
}
.record-avatar {
  width: 40px;
  height: 40px;
  font-size: 15px;
}
.record-applicant-name {
  font-size: 14px;
}
.record-meta {
  font-size: 12px;
}
.record-footer {
  margin-top: 12px;
  padding-top: 10px;
}
.record-btn {
  padding: 5px 14px;
  border-radius: 14px;
  font-size: 12px;
}
.job-icon-text {
  color: #ff6b35;
  font-size: 16px;
}
.meta-icon {
  color: #ff6b35;
  font-size: 11px;
  margin-right: 3px;
}
.btn-approve,
.btn-reject {
  margin: 0;
  line-height: 1.4;
}
.btn-approve::after,
.btn-reject::after {
  border: none;
}
</style>
