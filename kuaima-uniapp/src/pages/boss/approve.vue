<template>
  <view class="container">
    <!-- 导航栏 -->
    <view class="nav-bar" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="nav-back" @click="goBack">
        <text>←</text>
      </view>
      <view class="nav-title">
        <text>待我审批</text>
        <text v-if="pendingCount > 0" class="nav-badge">{{ pendingCount }}</text>
      </view>
      <view class="nav-right">
        <text class="nav-dots">⋯</text>
      </view>
    </view>

    <scroll-view scroll-y class="body">
      <!-- 搜索 -->
      <view class="search-bar">
        <text class="search-ico">🔍</text>
        <input class="search-input" v-model="keyword" placeholder="请输入发薪单标题" @confirm="search" @input="onInput" />
      </view>

      <!-- 已审批的发薪单入口 -->
      <view class="approved-row" @click="goApproveRecords">
        <text class="approved-text">已审批的发薪单</text>
        <text class="row-arrow">›</text>
      </view>

      <!-- 待审批列表 -->
      <view v-if="loading" class="page-state">加载中...</view>
      <view v-else-if="!pendingList.length" class="empty-state">
        <text class="empty-ico">📋</text>
        <text class="empty-text">暂无待审批数据</text>
      </view>
      <view class="record-card" v-for="item in pendingList" :key="item.id">
        <view class="record-head">
          <view class="record-title">
            <text class="record-tag" :class="item.type">{{ getTypeText(item.type) }}</text>
            {{ item.title }}
          </view>
          <text class="record-status pending">待审批</text>
        </view>
        <view class="record-line"><text class="lab">应发金额</text> {{ item.peopleCount || 0 }}人，<text class="record-amount">¥{{ formatYuan(item.amount) }}</text></view>
        <view class="record-line"><text class="lab">所属项目</text> {{ item.projectName || '—' }}</view>
        <view class="record-line"><text class="lab">制单人员</text> {{ item.creator || '—' }}</view>
        <view class="record-line"><text class="lab">提交时间</text> {{ formatTime(item.submitTime) }}</view>
        <view class="record-actions">
          <view class="record-btn reject" @click="rejectApprove(item)">驳回</view>
          <view class="record-btn pass" @click="passApprove(item)">通过</view>
        </view>
      </view>
      <view class="bottom-space" />
    </scroll-view>

    <!-- 驳回原因弹窗 -->
    <view v-if="showRejectModal" class="modal-mask" @click="closeRejectModal">
      <view class="modal-box" @click.stop>
        <view class="modal-header">
          <text class="modal-title">驳回原因</text>
          <text class="modal-close" @click="closeRejectModal">✕</text>
        </view>
        <view class="modal-body">
          <textarea class="reject-textarea" v-model="rejectReason" placeholder="请输入驳回原因（可选）" maxlength="200" />
          <text class="reject-count">{{ rejectReason.length }}/200</text>
        </view>
        <view class="modal-footer">
          <view class="modal-btn outline" @click="closeRejectModal">取消</view>
          <view class="modal-btn danger" :class="{ disabled: rejecting }" @click="confirmReject">{{ rejecting ? '提交中...' : '确认驳回' }}</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { listPendingPayrollOrders, approvePayrollOrder, rejectPayrollOrder } from "@/api/backend";

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
      pendingList: [],
      pendingCount: 0,
      showRejectModal: false,
      rejectReason: "",
      rejecting: false,
      currentItem: null,
    };
  },
  onLoad() {
    const info = typeof uni.getWindowInfo === "function"
      ? uni.getWindowInfo() : uni.getSystemInfoSync();
    this.statusBarHeight = Number(info.statusBarHeight || 0);
    this.loadPendingList();
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
    goApproveRecords() {
      uni.navigateTo({ url: "/pages/boss/approve-records" });
    },
    async loadPendingList() {
      this.loading = true;
      try {
        const data = await listPendingPayrollOrders({ keyword: this.keyword.trim() || undefined });
        const body = parsePayload(data);
        this.pendingList = Array.isArray(body) ? body : (body.records || []);
        this.pendingCount = this.pendingList.length;
      } catch (error) {
        this.pendingList = [];
        this.pendingCount = 0;
      } finally {
        this.loading = false;
      }
    },
    search() { this.loadPendingList(); },
    onInput() {
      if (this.searchTimer) clearTimeout(this.searchTimer);
      this.searchTimer = setTimeout(() => this.loadPendingList(), 350);
    },
    passApprove(item) {
      uni.showModal({
        title: "确认通过",
        content: `确认通过发薪单「${item.title}」？通过后将立即触发批量发薪流程。`,
        confirmColor: "#FF6B35",
        success: async (res) => {
          if (res.confirm) {
            try {
              await approvePayrollOrder(item.id);
              uni.showToast({ title: `已通过「${item.title}」`, icon: "success" });
              this.loadPendingList();
            } catch (error) {
              uni.showToast({ title: error?.message || "操作失败", icon: "none" });
            }
          }
        },
      });
    },
    rejectApprove(item) {
      this.currentItem = item;
      this.rejectReason = "";
      this.showRejectModal = true;
    },
    closeRejectModal() {
      this.showRejectModal = false;
      this.currentItem = null;
      this.rejectReason = "";
    },
    async confirmReject() {
      if (!this.currentItem || this.rejecting) return;
      this.rejecting = true;
      try {
        await rejectPayrollOrder(this.currentItem.id, this.rejectReason.trim());
        uni.showToast({
          title: `已驳回「${this.currentItem.title}」` + (this.rejectReason.trim() ? "" : ""),
          icon: "success",
        });
        this.closeRejectModal();
        this.loadPendingList();
      } catch (error) {
        uni.showToast({ title: error?.message || "操作失败", icon: "none" });
      } finally {
        this.rejecting = false;
      }
    },
    formatYuan(fen) {
      const n = Number(fen || 0);
      return (n / 100).toFixed(2);
    },
    formatTime(value) {
      if (!value) return "—";
      const s = String(value).replace("T", " ").substring(0, 16);
      return s;
    },
    getTypeText(t) {
      const map = { wage: "工资", advance: "预支", other: "其他" };
      return map[t] || t || "工资";
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
.nav-title {
  font-size: 17px; font-weight: 600; color: #333;
  display: flex; align-items: center; gap: 6px;
}
.nav-badge {
  background: #FF6B35; color: #fff; font-size: 11px;
  border-radius: 8px; padding: 1px 6px; font-weight: 500;
}

.body { flex: 1; padding: 12px 16px 0; }

.search-bar {
  display: flex; align-items: center; gap: 8px; padding: 10px 14px; margin-bottom: 12px;
  background: #fff; border-radius: 22px;
}
.search-ico { font-size: 13px; color: #bbb; }
.search-input { flex: 1; font-size: 13px; color: #333; }

.page-state { padding: 32px 0; color: #999; font-size: 14px; text-align: center; }
.empty-state { text-align: center; padding: 80px 40px; }
.empty-ico { font-size: 60px; display: block; margin-bottom: 12px; }
.empty-text { font-size: 13px; color: #999; }

.approved-row {
  background: #fff; border-radius: 16px; margin-bottom: 12px;
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 16px; box-shadow: 0 1px 6px rgba(0,0,0,0.04);
}
.approved-text { font-size: 14px; color: #333; font-weight: 500; }
.row-arrow { color: #C8C8C8; font-size: 18px; }

.record-card {
  background: #fff; border-radius: 16px; padding: 14px 16px; margin-bottom: 10px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
}
.record-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.record-title { font-size: 14px; font-weight: 600; color: #333; display: flex; align-items: center; gap: 6px; }
.record-tag { font-size: 11px; padding: 2px 7px; border-radius: 6px; background: #fff3ed; color: #ff6b35; font-weight: 500; }
.record-tag.advance { background: #fff8e6; color: #d48806; }
.record-tag.other { background: #e6f7ff; color: #1890ff; }
.record-status { font-size: 12px; font-weight: 500; }
.record-status.pending { color: #FF6B35; }
.record-status.approved { color: #10B981; }
.record-status.rejected { color: #EF4444; }
.record-line { font-size: 12px; color: #888; margin-top: 5px; }
.record-line .lab { color: #B0B0B0; }
.record-amount { color: #FF6B35; font-weight: 600; }

.record-actions {
  display: flex; gap: 10px; margin-top: 12px; padding-top: 10px;
  border-top: 1px dashed #F0F0F0;
}
.record-btn {
  flex: 1; padding: 8px 0; border-radius: 8px; font-size: 13px;
  font-weight: 500; cursor: pointer; text-align: center;
}
.record-btn.reject { background: #FFF5F5; color: #EF4444; border: 1px solid #FFD1D1; }
.record-btn.pass { background: #FF6B35; color: #fff; border: none; }

.bottom-space { height: 20px; }

/* 弹窗 */
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 100; display: flex; align-items: center; justify-content: center; }
.modal-box { width: 85%; background: #fff; border-radius: 18px; overflow: hidden; animation: modalIn 0.25s ease-out; }
@keyframes modalIn { from { transform: scale(0.9); opacity: 0; } to { transform: scale(1); opacity: 1; } }
.modal-header { display: flex; align-items: center; justify-content: center; padding: 16px; border-bottom: 0.5px solid #f0f0f0; position: relative; }
.modal-title { font-size: 16px; font-weight: 600; color: #333; }
.modal-close { position: absolute; right: 16px; font-size: 16px; color: #999; }
.modal-body { padding: 16px; }
.reject-textarea {
  width: 100%; height: 100px; padding: 12px 14px; border: 1px solid #ebebeb;
  border-radius: 12px; font-size: 14px; background: #fafafa; box-sizing: border-box; color: #333;
}
.reject-count { display: block; text-align: right; font-size: 11px; color: #999; margin-top: 6px; }
.modal-footer { display: flex; gap: 12px; padding: 16px; border-top: 0.5px solid #f0f0f0; }
.modal-btn { flex: 1; text-align: center; padding: 11px 0; border-radius: 22px; font-size: 14px; font-weight: 600; }
.modal-btn.outline { background: #fff; color: #333; border: 1px solid #e0e0e0; }
.modal-btn.danger { background: #EF4444; color: #fff; }
.modal-btn.disabled { opacity: 0.6; }
</style>
