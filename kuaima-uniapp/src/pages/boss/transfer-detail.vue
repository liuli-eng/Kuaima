<template>
  <view class="container">
    <!-- 导航栏 -->
    <view class="nav-bar" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="nav-back" @click="goBack">
        <text>←</text>
      </view>
      <text class="nav-title">下载</text>
      <view class="nav-right">
        <text class="nav-dots">⋯</text>
      </view>
    </view>

    <scroll-view scroll-y class="body">
      <!-- Tab切换 -->
      <view class="dl-tabs">
        <view class="dl-tab" @click="goTab('transfer-export')">转账记录</view>
        <view class="dl-tab active">转账明细</view>
        <view class="dl-tab" @click="goTab('transfer-summary')">明细汇总</view>
      </view>

      <!-- 筛选表单 -->
      <view class="form-card">
        <view class="form-item">
          <text class="form-label">转账类型</text>
          <view class="type-chips">
            <text
              v-for="t in typeChips"
              :key="t.value"
              class="type-chip"
              :class="{ active: form.type === t.value }"
              @click="onTypeChange(t.value)"
            >{{ t.label }}</text>
          </view>
        </view>
        <view class="field-row" @click="pickDate('start')">
          <text class="field-label">开始时间</text>
          <text class="field-value">{{ form.startDate || '请选择' }} ▾</text>
        </view>
        <view class="field-row" style="border-bottom:none;" @click="pickDate('end')">
          <text class="field-label">结束时间</text>
          <text class="field-value">{{ form.endDate || '请选择' }} ▾</text>
        </view>
      </view>

      <!-- 列表标题 -->
      <view class="list-title">
        <text>共 <text class="hl">{{ total }}</text> 条转账明细</text>
        <text>已成功 {{ successCount }} 笔 · 失败 {{ failedCount }} 笔</text>
      </view>

      <!-- 明细卡片列表 -->
      <view v-if="loading" class="page-state">加载中...</view>
      <view v-else-if="!details.length" class="empty-state">
        <text class="empty-ico">📋</text>
        <text class="empty-text">暂无转账明细</text>
      </view>
      <view class="detail-card" v-for="d in details" :key="d.id">
        <view class="detail-head">
          <view class="detail-title">
            <text class="detail-tag" :class="d.type">{{ getTypeText(d.type) }}</text>
            {{ d.name }}
            <text class="detail-phone">{{ maskPhone(d.phone) }}</text>
          </view>
          <text class="detail-status" :class="d.status">{{ d.status === 'success' ? '转账成功' : '转账失败' }}</text>
        </view>
        <view class="detail-row">
          <text><text class="lab">转账单号</text> {{ d.orderNo || '—' }}</text>
          <text class="detail-amount">¥{{ formatYuan(d.amount) }}</text>
        </view>
        <view class="detail-row"><text class="lab">支付账户</text> {{ d.account || '—' }}</view>
        <view class="detail-foot">
          <text class="proj">💼 {{ d.projectName || '—' }}</text>
          <text class="date">🕐 {{ formatTime(d.payTime) }}</text>
        </view>
      </view>
      <view class="bottom-space" />
    </scroll-view>

    <!-- 底部操作 -->
    <view class="footer">
      <view class="footer-btn outline" @click="resetForm">重置</view>
      <view class="footer-btn primary" :class="{ disabled: exporting }" @click="exportDetails">{{ exporting ? '导出中...' : '一键导出' }}</view>
    </view>
  </view>
</template>

<script>
import { listTransferDetails, exportTransferDetails } from "@/api/backend";

function parsePayload(data) {
  return data?.data ?? data ?? {};
}

export default {
  data() {
    return {
      statusBarHeight: 0,
      loading: false,
      exporting: false,
      details: [],
      total: 0,
      successCount: 0,
      failedCount: 0,
      typeChips: [
        { label: "全部", value: "" },
        { label: "工资", value: "wage" },
        { label: "预支", value: "advance" },
        { label: "其他", value: "other" },
      ],
      form: { type: "", startDate: "", endDate: "" },
    };
  },
  onLoad() {
    const info = typeof uni.getWindowInfo === "function"
      ? uni.getWindowInfo() : uni.getSystemInfoSync();
    this.statusBarHeight = Number(info.statusBarHeight || 0);
    this.loadDetails();
  },
  methods: {
    goBack() {
      uni.navigateBack({
        fail: () => uni.reLaunch({ url: "/pages/boss/transfers" }),
      });
    },
    goTab(page) {
      uni.redirectTo({ url: `/pages/boss/${page}` });
    },
    onTypeChange(type) {
      this.form.type = type;
      this.loadDetails();
    },
    pickDate(field) {
      uni.showModal({
        title: field === "start" ? "选择开始时间" : "选择结束时间",
        editable: true,
        placeholderText: "格式：2026-09-01",
        success: (res) => {
          if (res.confirm && res.content) {
            const v = res.content.trim();
            if (/^\d{4}-\d{2}-\d{2}$/.test(v)) {
              this.form[field === "start" ? "startDate" : "endDate"] = v;
              this.loadDetails();
            } else {
              uni.showToast({ title: "请输入正确日期格式", icon: "none" });
            }
          }
        },
      });
    },
    resetForm() {
      this.form = { type: "", startDate: "", endDate: "" };
      this.loadDetails();
      uni.showToast({ title: "已重置筛选条件", icon: "none" });
    },
    async loadDetails() {
      this.loading = true;
      try {
        const data = await listTransferDetails({
          type: this.form.type || undefined,
          startDate: this.form.startDate || undefined,
          endDate: this.form.endDate || undefined,
        });
        const body = parsePayload(data);
        this.details = Array.isArray(body) ? body : (body.details || []);
        this.total = body.total ?? this.details.length;
        this.successCount = body.successCount ?? this.details.filter((d) => d.status === "success").length;
        this.failedCount = body.failedCount ?? this.details.filter((d) => d.status === "failed").length;
      } catch (error) {
        this.details = [];
      } finally {
        this.loading = false;
      }
    },
    async exportDetails() {
      if (this.exporting) return;
      this.exporting = true;
      try {
        await exportTransferDetails({ ...this.form });
        uni.showToast({ title: "导出转账明细成功", icon: "success" });
      } catch (error) {
        uni.showToast({ title: error?.message || "导出失败", icon: "none" });
      } finally {
        this.exporting = false;
      }
    },
    maskPhone(phone) {
      const p = String(phone || "");
      if (p.length >= 11) return `${p.slice(0, 3)}****${p.slice(-4)}`;
      return p;
    },
    formatYuan(fen) {
      const n = Number(fen || 0);
      return (n / 100).toFixed(2);
    },
    formatTime(value) {
      if (!value) return "—";
      return String(value).replace("T", " ").substring(0, 16);
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
.nav-title { font-size: 17px; font-weight: 600; color: #333; }

.body { flex: 1; padding: 12px 16px 0; }

.dl-tabs {
  display: flex; background: #EBEBEB; border-radius: 12px; padding: 3px; margin-bottom: 16px;
}
.dl-tab {
  flex: 1; text-align: center; padding: 8px 0; font-size: 13px; color: #666; border-radius: 10px;
}
.dl-tab.active { background: #fff; color: #FF6B35; font-weight: 600; }

.form-card {
  background: #fff; border-radius: 16px; padding: 4px 16px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
}
.form-item { padding: 13px 0; border-bottom: 0.5px solid #F0F0F0; }
.form-label { display: block; font-size: 13px; color: #333; margin-bottom: 10px; }
.type-chips { display: flex; gap: 8px; flex-wrap: wrap; }
.type-chip {
  padding: 6px 16px; border-radius: 16px; font-size: 12px; color: #666;
  border: 1px solid #E5E5E5;
}
.type-chip.active {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  border-color: transparent; color: #fff; font-weight: 600;
}
.field-row {
  display: flex; align-items: center; justify-content: space-between;
  padding: 13px 0; border-bottom: 0.5px solid #F0F0F0; font-size: 13px;
}
.field-label { color: #333; }
.field-value { color: #999; }

.list-title {
  display: flex; justify-content: space-between; align-items: center;
  padding: 4px 2px 10px; font-size: 12px; color: #999;
}
.hl { color: #FF6B35; font-weight: 600; }

.page-state { padding: 32px 0; color: #999; font-size: 14px; text-align: center; }
.empty-state { text-align: center; padding: 80px 40px; }
.empty-ico { font-size: 60px; display: block; margin-bottom: 12px; }
.empty-text { font-size: 13px; color: #999; }

.detail-card {
  background: #fff; border-radius: 14px; padding: 14px 16px; margin-bottom: 10px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
}
.detail-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.detail-title { font-size: 14px; font-weight: 600; color: #333; display: flex; align-items: center; gap: 6px; }
.detail-phone { font-size: 12px; color: #999; font-weight: 400; }
.detail-tag { font-size: 11px; padding: 2px 7px; border-radius: 6px; background: #fff3ed; color: #ff6b35; font-weight: 500; flex-shrink: 0; }
.detail-tag.advance { background: #fff8e6; color: #d48806; }
.detail-tag.other { background: #e6f7ff; color: #1890ff; }
.detail-status { font-size: 11px; padding: 2px 8px; border-radius: 4px; background: #E8F8EF; color: #10B981; font-weight: 500; flex-shrink: 0; }
.detail-status.failed { background: #FEE2E2; color: #DC2626; }
.detail-row { font-size: 12px; color: #888; margin-top: 4px; display: flex; justify-content: space-between; }
.detail-row .lab { color: #B0B0B0; }
.detail-amount { color: #FF6B35; font-weight: 600; font-size: 14px; }
.detail-foot {
  margin-top: 10px; padding-top: 10px; border-top: 1px dashed #F0F0F0;
  display: flex; justify-content: space-between; align-items: center;
}
.detail-foot .proj { font-size: 11px; color: #999; }
.detail-foot .date { font-size: 11px; color: #BBB; }

.footer {
  display: flex; gap: 12px; padding: 12px 16px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  background: #fff; border-top: 0.5px solid #eee;
}
.footer-btn { text-align: center; padding: 12px 0; border-radius: 24px; font-size: 14px; font-weight: 600; }
.footer-btn.outline { flex: 1; background: #fff; color: #333; border: 1px solid #e0e0e0; }
.footer-btn.primary { flex: 2; background: linear-gradient(135deg, #FF6B35, #FF8C5A); color: #fff; }
.footer-btn.disabled { opacity: 0.6; }
.bottom-space { height: 20px; }
</style>
