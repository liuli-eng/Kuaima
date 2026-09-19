<template>
  <view class="container">
    <!-- 导航栏 -->
    <view class="nav-bar" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="nav-back" @click="goBack">
        <text>←</text>
      </view>
      <text class="nav-title">发薪</text>
      <view class="nav-right">
        <text class="nav-dots">⋯</text>
      </view>
    </view>

    <!-- 内容区 -->
    <scroll-view scroll-y class="body">
      <!-- 一键批量发薪 -->
      <view class="pay-hero">
        <view class="pay-hero-title">一键批量发薪</view>
        <view class="pay-steps">
          <text>①开始发薪</text><text>②添加人员</text><text>③设置薪资</text><text>④提交支付</text>
        </view>
        <view class="pay-start" @click="openCreateModal">开始发薪</view>
      </view>

      <!-- 已提交的发薪单 -->
      <view class="submitted-row" @click="goApproveRecords">
        <text class="submitted-text">已提交的发薪单</text>
        <text class="row-arrow">›</text>
      </view>

      <!-- 发薪单列表 -->
      <view v-if="orders.length" class="order-section">
        <view class="section-title">最近发薪单</view>
        <view class="order-card" v-for="order in orders" :key="order.id" @click="openOrderDetail(order)">
          <view class="order-head">
            <view class="order-title">
              <text class="order-tag" :class="order.type">{{ getTypeText(order.type) }}</text>
              {{ order.title }}
            </view>
            <text class="order-status" :class="order.status">{{ getStatusText(order.status) }}</text>
          </view>
          <view class="order-line"><text class="lab">应发金额</text> {{ order.peopleCount || 0 }}人，<text class="amount">¥{{ formatYuan(order.amount) }}</text></view>
          <view class="order-line"><text class="lab">所属项目</text> {{ order.projectName || '未指定' }}</view>
          <view class="order-line"><text class="lab">制单人员</text> {{ order.creator || '—' }}</view>
          <view class="order-line"><text class="lab">提交时间</text> {{ formatTime(order.submitTime) }}</view>
        </view>
      </view>

      <view v-else class="empty-state">
        <text class="empty-ico">🔍</text>
        <text class="empty-text">暂无数据</text>
      </view>
      <view class="bottom-space" />
    </scroll-view>

    <!-- 底部Tab -->
    <view class="module-tabs">
      <view class="module-tab active" @click="goPayroll">
        <text class="tab-ico">💰</text><text class="tab-label">发薪</text>
      </view>
      <view class="module-tab" @click="goEmployees">
        <text class="tab-ico">👥</text><text class="tab-label">员工</text>
      </view>
    </view>

    <!-- 创建发薪单弹窗 -->
    <view v-if="showCreateModal" class="modal-mask" @click="closeCreateModal">
      <view class="modal-box" @click.stop>
        <view class="modal-header">
          <text class="modal-title">创建发薪单</text>
          <text class="modal-close" @click="closeCreateModal">✕</text>
        </view>
        <view class="modal-body">
          <view class="form-item">
            <text class="form-label">转账标题</text>
            <input class="form-input" v-model="form.title" placeholder="请输入转账标题" />
          </view>
          <view class="form-item">
            <text class="form-label">关联项目</text>
            <picker mode="selector" :range="projectOptions" :range-key="'name'" @change="onPickProject">
              <view class="form-input picker-text">{{ form.projectName || '请选择关联项目' }}</view>
            </picker>
          </view>
          <view class="form-item">
            <text class="form-label">发薪类型</text>
            <view class="type-chips">
              <text
                v-for="t in typeChips"
                :key="t.value"
                class="type-chip"
                :class="{ active: form.type === t.value }"
                @click="form.type = t.value"
              >{{ t.label }}</text>
            </view>
          </view>
        </view>
        <view class="modal-footer">
          <view class="modal-btn outline" @click="closeCreateModal">取消</view>
          <view class="modal-btn primary" :class="{ disabled: submitting }" @click="confirmCreate">
            {{ submitting ? '创建中...' : '确定' }}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { listBossPayrollOrders, createBossPayrollOrder } from "@/api/backend";

function parsePayload(data) {
  return data?.data ?? data ?? {};
}

export default {
  data() {
    return {
      statusBarHeight: 0,
      orders: [],
      showCreateModal: false,
      submitting: false,
      form: { title: "", projectId: null, projectName: "", type: "wage" },
      projectOptions: [
        { id: 1, name: "菜鸟·云联日结（Gefield）" },
        { id: 2, name: "菜鸟·沙溪日结（真实）" },
        { id: 3, name: "邮政·茶山日结（真实）" },
        { id: 4, name: "鸿康·分拣中心日结" },
      ],
      typeChips: [
        { label: "工资", value: "wage" },
        { label: "预支", value: "advance" },
        { label: "其他", value: "other" },
      ],
    };
  },
  onLoad() {
    const info = typeof uni.getWindowInfo === "function"
      ? uni.getWindowInfo() : uni.getSystemInfoSync();
    this.statusBarHeight = Number(info.statusBarHeight || 0);
    this.loadOrders();
  },
  methods: {
    goBack() {
      uni.navigateBack({
        fail: () => uni.reLaunch({ url: "/pages/boss/workbench" }),
      });
    },
    goPayroll() {
      // 当前页
    },
    goEmployees() {
      uni.redirectTo({ url: "/pages/boss/payroll-employees" });
    },
    goApproveRecords() {
      uni.navigateTo({ url: "/pages/boss/approve-records" });
    },
    openOrderDetail(order) {
      uni.showToast({ title: `原型演示：查看发薪单 ${order.title}`, icon: "none" });
    },
    async loadOrders() {
      try {
        const data = await listBossPayrollOrders();
        const body = parsePayload(data);
        this.orders = Array.isArray(body) ? body : (body.records || []);
      } catch (error) {
        // 原型可空数据，静默失败
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
      return map[t] || t || "";
    },
    getStatusText(s) {
      const map = {
        pending: "待审批",
        approved: "审批通过",
        rejected: "已驳回",
        withdrawn: "已撤回",
      };
      return map[s] || s || "";
    },
    openCreateModal() {
      this.form = { title: "", projectId: null, projectName: "", type: "wage" };
      this.showCreateModal = true;
    },
    closeCreateModal() {
      this.showCreateModal = false;
    },
    onPickProject(e) {
      const idx = Number(e.detail.value);
      const proj = this.projectOptions[idx];
      this.form.projectId = proj.id;
      this.form.projectName = proj.name;
    },
    async confirmCreate() {
      if (!this.form.title.trim()) {
        uni.showToast({ title: "请输入转账标题", icon: "none" });
        return;
      }
      if (this.submitting) return;
      this.submitting = true;
      try {
        await createBossPayrollOrder({
          title: this.form.title.trim(),
          projectId: this.form.projectId,
          projectName: this.form.projectName,
          type: this.form.type,
        });
        uni.showToast({ title: "发薪单创建成功，请继续添加人员", icon: "success" });
        this.closeCreateModal();
        this.loadOrders();
      } catch (error) {
        uni.showToast({ title: error?.message || "创建失败", icon: "none" });
      } finally {
        this.submitting = false;
      }
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
.nav-back, .nav-right {
  width: 32px; height: 32px;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px; color: #333;
}
.nav-dots { font-size: 18px; }
.nav-title { font-size: 17px; font-weight: 600; color: #333; }
.body { flex: 1; padding: 12px 16px 0; }

.pay-hero {
  background: #fff;
  border-radius: 16px;
  padding: 20px 16px;
  text-align: center;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
}
.pay-hero-title { font-size: 17px; font-weight: 700; color: #333; }
.pay-steps {
  display: flex; justify-content: center; gap: 4px; margin-top: 10px;
  font-size: 10px; color: #999;
}
.pay-steps text { white-space: nowrap; }
.pay-start {
  margin: 16px auto 0; width: 70%; background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  color: #fff; border-radius: 24px; padding: 12px 0; text-align: center;
  font-size: 15px; font-weight: 600; cursor: pointer;
  box-shadow: 0 6px 16px rgba(255, 107, 53, 0.3);
}

.submitted-row {
  background: #fff; border-radius: 16px; margin-top: 12px;
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 16px; box-shadow: 0 1px 6px rgba(0,0,0,0.04);
}
.submitted-text { font-size: 14px; color: #333; font-weight: 500; }
.row-arrow { color: #C8C8C8; font-size: 18px; }

.order-section { margin-top: 12px; }
.section-title { font-size: 13px; color: #999; margin: 0 0 8px 2px; }
.order-card {
  background: #fff; border-radius: 14px; padding: 14px 16px; margin-bottom: 10px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
}
.order-head {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px;
}
.order-title { font-size: 14px; font-weight: 600; color: #333; display: flex; align-items: center; gap: 6px; }
.order-tag {
  font-size: 11px; padding: 2px 7px; border-radius: 6px; background: #fff3ed; color: #ff6b35;
}
.order-tag.advance { background: #fff8e6; color: #d48806; }
.order-tag.other { background: #e6f7ff; color: #1890ff; }
.order-status { font-size: 12px; font-weight: 500; }
.order-status.pending { color: #d48806; }
.order-status.approved { color: #10b981; }
.order-status.rejected { color: #dc2626; }
.order-status.withdrawn { color: #999; }
.order-line { font-size: 12px; color: #888; margin-top: 4px; }
.order-line .lab { color: #B0B0B0; }
.amount { color: #FF6B35; font-weight: 600; }

.empty-state { text-align: center; padding: 60px 0; }
.empty-ico { font-size: 40px; display: block; margin-bottom: 10px; }
.empty-text { font-size: 13px; color: #999; }
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

/* 弹窗 */
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 100; display: flex; align-items: center; justify-content: center; padding: 0 20px; }
.modal-box { width: 100%; max-width: 360px; background: #fff; border-radius: 18px; overflow: hidden; animation: modalIn 0.25s ease-out; }
@keyframes modalIn { from { transform: scale(0.9); opacity: 0; } to { transform: scale(1); opacity: 1; } }
.modal-header { display: flex; align-items: center; justify-content: center; padding: 16px; border-bottom: 0.5px solid #f0f0f0; position: relative; }
.modal-title { font-size: 16px; font-weight: 600; color: #333; }
.modal-close { position: absolute; right: 16px; font-size: 16px; color: #999; }
.modal-body { padding: 20px 14px; }
.form-item { margin-bottom: 18px; }
.form-item:last-child { margin-bottom: 0; }
.form-label { display: block; font-size: 14px; color: #333; font-weight: 500; margin-bottom: 10px; }
.form-input {
  width: 100%; padding: 13px 16px; border: 1.5px solid #e8e8e8; border-radius: 14px;
  font-size: 15px; background: #fff; box-sizing: border-box; color: #333;
  transition: border-color 0.2s, box-shadow 0.2s; overflow: visible;
}
.form-input:focus { border-color: #FF6B35; box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.12); outline: none; }
.form-input::placeholder { color: #c0c0c0; overflow: visible; text-overflow: clip; }
.picker-text { line-height: 1.4; }
.type-chips { display: flex; gap: 10px; }
.type-chip {
  padding: 7px 18px; border-radius: 18px; font-size: 13px; color: #666;
  border: 1px solid #E5E5E5;
}
.type-chip.active { background: #FFF0E8; border-color: #FF6B35; color: #FF6B35; font-weight: 600; }
.modal-footer { display: flex; gap: 12px; padding: 16px; border-top: 0.5px solid #f0f0f0; }
.modal-btn { flex: 1; text-align: center; padding: 11px 0; border-radius: 22px; font-size: 14px; font-weight: 600; }
.modal-btn.outline { background: #fff; color: #333; border: 1px solid #e0e0e0; }
.modal-btn.primary { background: linear-gradient(135deg, #ff6b35, #ff8c5a); color: #fff; }
.modal-btn.disabled { opacity: 0.6; }
</style>
