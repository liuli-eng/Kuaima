<template>
  <view class="container">
    <!-- 状态栏 -->
    <view class="status-bar">
      <text>19:53</text>
      <view class="status-icons">
        <text>📶</text>
        <text>📡</text>
        <text>🔋</text>
      </view>
    </view>

    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text>←</text>
      </view>
      <text class="nav-title">费用报销明细</text>
      <view class="nav-right">
        <text>+</text>
      </view>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <!-- 汇总卡片 -->
      <view class="summary-card">
        <view class="summary-item">
          <text class="summary-value">¥{{ summary.paid.toFixed(2) }}</text>
          <text class="summary-label">已报销</text>
        </view>
        <view class="summary-item">
          <text class="summary-value">{{ summary.pending }}</text>
          <text class="summary-label">待审核</text>
        </view>
        <view class="summary-item">
          <text class="summary-value">{{ summary.rejected }}</text>
          <text class="summary-label">已拒绝</text>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="!loading && !records.length" class="empty-state">
        <view class="empty-icon">
          <text style="font-size: 48px; color: #ddd">🧾</text>
        </view>
        <text class="empty-text">暂无报销记录</text>
        <text class="empty-text" style="margin-top: 4px">暂无费用记录</text>
      </view>
      <view v-for="item in records" :key="item.id" class="record"
        ><text>{{ item.description || item.name || "费用明细" }}</text
        ><text>¥{{ item.amount || 0 }}</text></view
      >
    </scroll-view>
  </view>
</template>

<script>
import { listExpenses } from "@/api/backend";
export default {
  data() {
    return {
      records: [],
      loading: false,
      summary: { paid: 0, pending: 0, rejected: 0 },
    };
  },
  onLoad() {
    this.load();
  },
  methods: {
    async load() {
      this.loading = true;
      try {
        const result = await listExpenses(
          uni.getStorageSync("userId") || "2001",
        );
        const rows = Array.isArray(result)
          ? result
          : result?.records || result?.content || [];
        this.records = rows;
        this.summary = {
          paid: rows
            .filter((i) => ["PAID", "已报销", "SUCCESS"].includes(i.status))
            .reduce((s, i) => s + Number(i.amount || 0), 0),
          pending: rows.filter((i) => ["PENDING", "待审核"].includes(i.status))
            .length,
          rejected: rows.filter((i) =>
            ["REJECTED", "已拒绝"].includes(i.status),
          ).length,
        };
      } catch (error) {
        uni.showToast({
          title: error.message || "费用明细加载失败",
          icon: "none",
        });
      } finally {
        this.loading = false;
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
  width: 100%;
  height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.status-bar {
  height: 47px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 28px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  background: #fff;
}

.status-icons {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-bar {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #fff;
}

.nav-back {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
}

.nav-right {
  display: flex;
  gap: 14px;
  color: #333;
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
}

.summary-card {
  background: linear-gradient(135deg, #1890ff, #40a9ff);
  margin: 12px 16px;
  border-radius: 12px;
  padding: 16px;
  color: #fff;
  display: flex;
  justify-content: space-around;
}

.summary-item {
  text-align: center;
}

.summary-value {
  font-size: 22px;
  font-weight: 700;
}

.summary-label {
  font-size: 12px;
  opacity: 0.9;
  margin-top: 4px;
}

.empty-state {
  text-align: center;
  padding: 80px 40px;
}

.empty-text {
  font-size: 14px;
  color: #999;
  display: block;
}
.record {
  display: flex;
  justify-content: space-between;
  margin: 8px 16px;
  padding: 14px;
  border-radius: 12px;
  background: #fff;
  color: #555;
  font-size: 14px;
}
</style>
