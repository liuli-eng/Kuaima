<template>
  <view class="container">
    <!-- 导航栏 -->
    <view class="nav-bar" :style="{ paddingTop: `${statusBarHeight}px`, height: `${statusBarHeight + 50}px` }">
      <view class="nav-back" @click="goBack">
        <image class="nav-svg" src="/static/icons/boss-recruit-settings/chevron-left.svg" mode="aspectFit" />
      </view>
      <text class="nav-title">发薪</text>
      <view class="nav-right">
        <image class="nav-dots-svg" src="/static/icons/boss-profile/ellipsis.svg" mode="aspectFit" />
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
        <image class="row-arrow-svg" src="/static/icons/boss-points/chevron-right-gray.svg" mode="aspectFit" />
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
        <view class="empty-icon"><image class="empty-svg" src="/static/icons/boss-workbench/search-empty.svg" mode="aspectFit" /></view>
        <text class="empty-text">暂无数据</text>
      </view>
      <view class="bottom-space" />
    </scroll-view>

    <!-- 底部Tab -->
    <view class="module-tabs">
      <view class="module-tab active" @click="goPayroll">
        <image class="module-svg" src="/static/icons/boss-reward/money-bill-wave-orange.svg" mode="aspectFit" /><text class="tab-label">发薪</text>
      </view>
      <view class="module-tab" @click="goEmployees">
        <image class="module-svg" src="/static/icons/boss-workbench/user-group-gray.svg" mode="aspectFit" /><text class="tab-label">员工</text>
      </view>
    </view>

    <!-- 创建发薪单弹窗 -->
    <view v-if="showCreateModal" class="modal-mask" @click="closeCreateModal">
      <view class="modal-box" @click.stop>
        <view class="modal-header">
          <text class="modal-title">创建发薪单</text>
          <view class="modal-close" @click="closeCreateModal"><image class="modal-close-svg" src="/static/icons/boss-points/xmark-gray.svg" mode="aspectFit" /></view>
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
          <view class="form-item">
            <text class="form-label">发薪成员</text>
            <view v-if="!form.projectId" class="member-empty">请先选择关联项目</view>
            <view v-else-if="membersLoading" class="member-empty">成员加载中...</view>
            <view v-else-if="!memberRows.length" class="member-empty">该项目暂无在职成员</view>
            <view v-else class="member-list">
              <view v-for="(m, idx) in memberRows" :key="idx" class="member-row" :class="{ off: !m.checked }">
                <view class="member-head" @click="toggleMember(m)">
                  <view class="member-check" :class="{ on: m.checked }">
                    <image v-if="m.checked" class="member-check-svg" src="/static/icons/boss-recruit-settings/check-white.svg" mode="aspectFit" />
                  </view>
                  <text class="member-name">{{ m.name || '未填写姓名' }}</text>
                  <text v-if="m.job" class="member-job">{{ m.job }}</text>
                </view>
                <view class="member-inputs">
                  <view class="member-field">
                    <text class="member-field-label">日薪(元)</text>
                    <input class="member-input" type="digit" v-model="m.dailyWageYuan" placeholder="0" @input="amountTick++" />
                  </view>
                  <view class="member-field">
                    <text class="member-field-label">出勤天数</text>
                    <input class="member-input" type="number" v-model="m.attendDays" placeholder="0" @input="amountTick++" />
                  </view>
                  <text class="member-subtotal">¥{{ memberSubtotal(m) }}</text>
                </view>
              </view>
              <view class="member-total">已选 {{ selectedMemberCount }} 人 · 合计 ¥{{ memberTotalYuan }}</view>
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
import { listBossPayrollOrders, createBossPayrollOrder, listBossProjects, listBossPayrollMembers } from "@/api/backend";

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
      projectOptions: [],
      memberRows: [],
      membersLoading: false,
      amountTick: 0,
      typeChips: [
        { label: "工资", value: "wage" },
        { label: "预支", value: "advance" },
        { label: "其他", value: "other" },
      ],
    };
  },
  computed: {
    selectedMemberCount() {
      return this.memberRows.filter((m) => m.checked).length;
    },
    memberTotalYuan() {
      // 依赖 amountTick 强制在输入时重算（小程序 input 事件下更稳）
      // eslint-disable-next-line no-unused-expressions
      this.amountTick;
      const total = this.memberRows
        .filter((m) => m.checked)
        .reduce((sum, m) => sum + this.memberSubtotalValue(m), 0);
      return total.toFixed(2);
    },
  },
  onLoad() {
    const info = typeof uni.getWindowInfo === "function"
      ? uni.getWindowInfo() : uni.getSystemInfoSync();
    this.statusBarHeight = Number(info.statusBarHeight || 0);
    this.loadOrders();
    this.loadProjects();
  },
  onShow() {
    // 从项目管理页返回后能立刻看到新建的项目
    this.loadProjects();
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
    /** 加载项目管理模块创建的项目，作为「关联项目」下拉选项。 */
    async loadProjects() {
      try {
        const data = await listBossProjects({ page: 0, size: 100 });
        const body = parsePayload(data);
        const list = Array.isArray(body) ? body : (body.records || body.content || []);
        this.projectOptions = list.map((p) => ({ id: p.id, name: p.name || p.projectName || `项目${p.id}` }));
      } catch (error) {
        this.projectOptions = [];
        uni.showToast({ title: "项目加载失败，请稍后重试", icon: "none" });
      }
    },
    openCreateModal() {
      this.form = { title: "", projectId: null, projectName: "", type: "wage" };
      this.memberRows = [];
      this.showCreateModal = true;
    },
    closeCreateModal() {
      this.showCreateModal = false;
    },
    onPickProject(e) {
      const idx = Number(e.detail.value);
      const proj = this.projectOptions[idx];
      if (!proj) return;
      this.form.projectId = proj.id;
      this.form.projectName = proj.name;
      this.loadMembers(proj.id);
    },
    /** 拉取该项目在职成员，默认全选；日薪按发薪员工库预填（分→元）。 */
    async loadMembers(projectId) {
      this.memberRows = [];
      this.membersLoading = true;
      try {
        const data = await listBossPayrollMembers(projectId);
        const body = parsePayload(data);
        const list = Array.isArray(body) ? body : (body.records || body.content || []);
        this.memberRows = list.map((m) => {
          const wageFen = Number(m.dailyWage || 0);
          return {
            userId: m.userId || null,
            name: m.name || "",
            phone: m.phone || "",
            job: m.job || "",
            checked: true,
            dailyWageYuan: wageFen > 0 ? (wageFen / 100).toFixed(2) : "",
            attendDays: "",
          };
        });
        this.amountTick++;
      } catch (error) {
        uni.showToast({ title: "成员加载失败，请重试", icon: "none" });
      } finally {
        this.membersLoading = false;
      }
    },
    toggleMember(m) {
      m.checked = !m.checked;
      this.amountTick++;
    },
    /** 单人小计（元）。 */
    memberSubtotalValue(m) {
      const wage = Number(m.dailyWageYuan || 0);
      const days = Number(m.attendDays || 0);
      if (!Number.isFinite(wage) || !Number.isFinite(days)) return 0;
      return wage * days;
    },
    memberSubtotal(m) {
      // 依赖 amountTick 强制重算
      // eslint-disable-next-line no-unused-expressions
      this.amountTick;
      return this.memberSubtotalValue(m).toFixed(2);
    },
    /** 校验并生成提交用的明细（金额统一转「分」）。 */
    buildDetails() {
      const picked = this.memberRows.filter((m) => m.checked);
      if (!picked.length) return { error: "请至少勾选一名发薪成员" };
      const details = [];
      for (const m of picked) {
        const wage = Number(m.dailyWageYuan || 0);
        const days = Number(m.attendDays || 0);
        if (!(wage > 0)) return { error: `请填写「${m.name || "未填写姓名"}」的日薪` };
        if (!(days > 0) || !Number.isInteger(days)) {
          return { error: `请填写「${m.name || "未填写姓名"}」的出勤天数（正整数）` };
        }
        details.push({
          userId: m.userId,
          name: m.name,
          phone: m.phone || null,
          job: m.job || null,
          dailyWage: Math.round(wage * 100),
          attendDays: days,
        });
      }
      return { details };
    },
    async confirmCreate() {
      if (!this.form.title.trim()) {
        uni.showToast({ title: "请输入转账标题", icon: "none" });
        return;
      }
      if (!this.form.projectId) {
        uni.showToast({ title: "请选择关联项目", icon: "none" });
        return;
      }
      const built = this.buildDetails();
      if (built.error) {
        uni.showToast({ title: built.error, icon: "none" });
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
          details: built.details,
        });
        uni.showToast({ title: "发薪单创建成功，待审批后自动扣款发放", icon: "none" });
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
  width: 100%;
  background: #f3f4f6;
  overflow-x: hidden;
  box-sizing: border-box;
}
.nav-bar {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding: 0 16px 8px;
  background: #fff;
  flex-shrink: 0;
  width: 100%;
  box-sizing: border-box;
}
.nav-back, .nav-right {
  width: 32px; height: 32px;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px; color: #333;
}
.nav-dots { font-size: 18px; }
.nav-svg, .nav-dots-svg { width: 16px; height: 16px; }
.nav-title { font-size: 17px; font-weight: 600; color: #333; }
.body {
  flex: 1;
  min-height: 0;
  padding: 12px 16px 0;
  width: 100%;
  box-sizing: border-box;
}

.pay-hero {
  background: #fff;
  border-radius: 16px;
  padding: 20px 16px;
  text-align: center;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
  width: 100%;
  box-sizing: border-box;
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
  width: 100%;
  box-sizing: border-box;
}
.submitted-text { font-size: 14px; color: #333; font-weight: 500; }
.row-arrow { color: #C8C8C8; font-size: 18px; }
.row-arrow-svg { width: 13px; height: 13px; flex-shrink: 0; }

.order-section { margin-top: 12px; width: 100%; }
.section-title { font-size: 13px; color: #999; margin: 0 0 8px 2px; }
.order-card {
  background: #fff; border-radius: 14px; padding: 14px 16px; margin-bottom: 10px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
  box-sizing: border-box;
}
.order-head {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px;
}
.order-title { font-size: 14px; font-weight: 600; color: #333; display: flex; align-items: center; gap: 6px; min-width: 0; flex: 1; }
.order-title text:first-child { flex-shrink: 0; }
.order-tag {
  font-size: 11px; padding: 2px 7px; border-radius: 6px; background: #fff3ed; color: #ff6b35; flex-shrink: 0;
}
.order-tag.advance { background: #fff8e6; color: #d48806; }
.order-tag.other { background: #e6f7ff; color: #1890ff; }
.order-status { font-size: 12px; font-weight: 500; flex-shrink: 0; }
.order-status.pending { color: #d48806; }
.order-status.approved { color: #10b981; }
.order-status.rejected { color: #dc2626; }
.order-status.withdrawn { color: #999; }
.order-line { font-size: 12px; color: #888; margin-top: 4px; }
.order-line .lab { color: #B0B0B0; }
.amount { color: #FF6B35; font-weight: 600; }

.empty-state { text-align: center; padding: 60px 0; width: 100%; }
.empty-ico { font-size: 40px; display: block; margin-bottom: 10px; }
.empty-icon {
  width: 84px; height: 84px; margin: 0 auto 12px; border-radius: 50%;
  background: #F5F0EA; display: flex; align-items: center; justify-content: center;
}
.empty-svg { width: 36px; height: 36px; }
.empty-text { font-size: 13px; color: #999; }
.bottom-space { height: 20px; }

.module-tabs {
  display: flex; gap: 12px; padding: 10px 16px; padding-bottom: calc(10px + env(safe-area-inset-bottom));
  background: #fff; border-top: 0.5px solid #eee;
  flex-shrink: 0;
  width: 100%;
  box-sizing: border-box;
}
.module-tab {
  flex: 1; display: flex; flex-direction: column; align-items: center; gap: 4px;
  padding: 8px 0; border-radius: 12px; font-size: 12px; color: #999;
}
.module-tab.active { color: #ff6b35; background: #fff3ed; }
.tab-ico { font-size: 18px; }
.module-svg { width: 20px; height: 20px; }
.tab-label { font-size: 12px; }

/* 弹窗 */
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 100; display: flex; align-items: center; justify-content: center; padding: 0 20px; }
.modal-box { width: 100%; max-width: 360px; background: #fff; border-radius: 18px; overflow: hidden; animation: modalIn 0.25s ease-out; box-sizing: border-box; }
@keyframes modalIn { from { transform: scale(0.9); opacity: 0; } to { transform: scale(1); opacity: 1; } }
.modal-header { display: flex; align-items: center; justify-content: center; padding: 16px; border-bottom: 0.5px solid #f0f0f0; position: relative; }
.modal-title { font-size: 16px; font-weight: 600; color: #333; }
.modal-close { position: absolute; right: 16px; font-size: 16px; color: #999; }
.modal-close-svg { width: 14px; height: 14px; }
.modal-body { padding: 20px 14px; }
.form-item { margin-bottom: 18px; }
.form-item:last-child { margin-bottom: 0; }
.form-label { display: block; font-size: 14px; color: #333; font-weight: 500; margin-bottom: 10px; }
.form-input {
  width: 100%; height: 48px; padding: 0 16px; border: 1.5px solid #e8e8e8; border-radius: 14px;
  font-size: 15px; background: #fff; box-sizing: border-box; color: #333;
}
.picker-text { height: 48px; display: flex; align-items: center; }
.type-chips { display: flex; gap: 10px; flex-wrap: wrap; }
.type-chip {
  padding: 7px 18px; border-radius: 18px; font-size: 13px; color: #666;
  border: 1px solid #E5E5E5;
}
.type-chip.active { background: #FFF0E8; border-color: #FF6B35; color: #FF6B35; font-weight: 600; }

/* 发薪成员 */
.member-empty {
  font-size: 13px; color: #999; text-align: center;
  padding: 18px 0; background: #fafafa; border-radius: 14px;
}
.member-list { width: 100%; box-sizing: border-box; }
.member-row {
  width: 100%; box-sizing: border-box;
  border: 1.5px solid #e8e8e8; border-radius: 14px;
  padding: 12px; margin-bottom: 10px; background: #fff;
}
.member-row.off { opacity: 0.5; }
.member-head { display: flex; align-items: center; gap: 8px; min-width: 0; }
.member-check {
  width: 18px; height: 18px; border-radius: 50%; flex-shrink: 0;
  border: 1.5px solid #d0d0d0; display: flex; align-items: center; justify-content: center;
  box-sizing: border-box;
}
.member-check.on { background: #FF6B35; border-color: #FF6B35; }
.member-check-svg { width: 10px; height: 10px; }
.member-name { font-size: 14px; color: #333; font-weight: 600; flex-shrink: 0; }
.member-job { font-size: 12px; color: #999; flex: 1; min-width: 0; overflow: hidden; white-space: nowrap; }
.member-inputs { display: flex; align-items: flex-end; gap: 8px; margin-top: 10px; }
.member-field { flex: 1; min-width: 0; }
.member-field-label { display: block; font-size: 11px; color: #999; margin-bottom: 4px; }
.member-input {
  width: 100%; height: 36px; padding: 0 10px; box-sizing: border-box;
  border: 1px solid #e8e8e8; border-radius: 10px; font-size: 13px; color: #333; background: #fafafa;
}
.member-subtotal {
  font-size: 13px; color: #FF6B35; font-weight: 600;
  flex-shrink: 0; min-width: 62px; text-align: right; padding-bottom: 9px;
}
.member-total {
  font-size: 13px; color: #FF6B35; font-weight: 600; text-align: right; padding: 4px 2px 0;
}
.modal-footer { display: flex; gap: 12px; padding: 16px; border-top: 0.5px solid #f0f0f0; }
.modal-btn { flex: 1; text-align: center; padding: 11px 0; border-radius: 22px; font-size: 14px; font-weight: 600; }
.modal-btn.outline { background: #fff; color: #333; border: 1px solid #e0e0e0; }
.modal-btn.primary { background: linear-gradient(135deg, #ff6b35, #ff8c5a); color: #fff; }
.modal-btn.disabled { opacity: 0.6; }
</style>
