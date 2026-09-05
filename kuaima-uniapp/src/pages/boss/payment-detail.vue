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
      <text class="nav-title">报酬支付明细</text>
      <view style="width: 32px"></view>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <!-- 汇总卡片 -->
      <view class="summary-card">
        <text class="summary-title">累计支付报酬</text>
        <text class="summary-amount">¥{{ totalAmount }}</text>
        <text class="summary-period">{{ period }}</text>
      </view>

      <!-- 记录列表 -->
      <view v-if="!loading && !records.length" class="empty">暂无支付记录</view>
      <view class="record-card" v-for="record in records" :key="record.id">
        <view class="record-head">
          <text class="record-title">{{ record.title }}</text>
          <text class="record-status" :class="record.statusClass">{{
            record.status
          }}</text>
        </view>
        <text class="record-desc">{{ record.desc }}</text>
        <text class="record-amount">¥{{ record.amount }}</text>
        <text class="record-time">{{ record.time }}</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { listPayments } from "@/api/backend";
export default {
  data() {
    return {
      totalAmount: "0.00",
      records: [],
      period: "暂无支付记录",
      loading: false,
    };
  },
  onLoad() {
    this.load();
  },
  methods: {
    async load() {
      this.loading = true;
      try {
        const result = await listPayments(
          uni.getStorageSync("userId") || "2001",
        );
        const rows = Array.isArray(result)
          ? result
          : result?.records || result?.content || [];
        this.records = rows.map((i) => ({
          ...i,
          title: i.title || i.name || "报酬支付",
          status: i.status || "已支付",
          statusClass: ["PENDING", "待支付"].includes(i.status)
            ? "status-pending"
            : "status-success",
          desc: i.description || i.remark || "",
          amount: Number(i.amount || 0).toFixed(2),
          time: i.time || i.createTime || i.payTime || "",
        }));
        this.totalAmount = this.records
          .reduce((s, i) => s + Number(i.amount || 0), 0)
          .toFixed(2);
      } catch (error) {
        uni.showToast({
          title: error.message || "支付明细加载失败",
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

.scroll-area {
  flex: 1;
  overflow-y: auto;
}

.summary-card {
  background: linear-gradient(135deg, #722ed1, #9254de);
  margin: 12px 16px;
  border-radius: 12px;
  padding: 16px;
  color: #fff;
}

.summary-title {
  font-size: 13px;
  opacity: 0.9;
  display: block;
}

.summary-amount {
  font-size: 28px;
  font-weight: 700;
  margin-top: 6px;
  display: block;
}

.summary-period {
  font-size: 12px;
  opacity: 0.8;
  margin-top: 4px;
  display: block;
}

.record-card {
  background: #fff;
  margin: 8px 16px;
  border-radius: 12px;
  padding: 14px;
}

.record-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.record-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.record-status {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
}

.status-success {
  color: #52c41a;
  background: #f6ffed;
}

.status-pending {
  color: #fa8c16;
  background: #fff8e6;
}

.record-desc {
  font-size: 12px;
  color: #999;
  display: block;
}

.record-amount {
  font-size: 18px;
  font-weight: 700;
  color: #333;
  text-align: right;
  display: block;
}

.record-time {
  font-size: 11px;
  color: #bbb;
  margin-top: 4px;
  text-align: right;
  display: block;
}
.empty {
  padding: 180rpx 0;
  text-align: center;
  color: #aaa;
  font-size: 14px;
}
</style>
