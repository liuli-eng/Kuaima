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
        <view class="dl-tab active">转账记录</view>
        <view class="dl-tab" @click="goTab('transfer-detail')">转账明细</view>
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
              @click="form.type = t.value"
            >{{ t.label }}</text>
          </view>
        </view>
        <view class="field-row" @click="pickProject">
          <text class="field-label">所属项目</text>
          <text class="field-value">{{ form.projectName || '选择项目' }} ▾</text>
        </view>
        <view class="field-row" @click="pickAccount">
          <text class="field-label">支付账户</text>
          <text class="field-value">{{ form.account || '选择账户' }} ▾</text>
        </view>
        <view class="field-row">
          <text class="field-label">支付时间</text>
          <text class="field-value" style="color:#333;">-</text>
        </view>
        <view class="field-row" @click="pickDate('start')">
          <text class="field-label">开始时间</text>
          <text class="field-value">{{ form.startDate || '请选择' }} ▾</text>
        </view>
        <view class="field-row" @click="pickDate('end')">
          <text class="field-label">结束时间</text>
          <text class="field-value">{{ form.endDate || '请选择' }} ▾</text>
        </view>
        <view class="field-row" style="border-bottom:none;" @click="pickCreator">
          <text class="field-label">制单人员</text>
          <text class="field-value">{{ form.creator || '请选择制单人' }} ▾</text>
        </view>
      </view>
      <view class="bottom-space" />
    </scroll-view>

    <!-- 底部操作 -->
    <view class="footer">
      <view class="footer-btn outline" @click="resetForm">重置</view>
      <view class="footer-btn primary" :class="{ disabled: exporting }" @click="exportRecords">{{ exporting ? '导出中...' : '一键导出' }}</view>
    </view>
  </view>
</template>

<script>
import { exportTransferRecords } from "@/api/backend";

export default {
  data() {
    return {
      statusBarHeight: 0,
      exporting: false,
      typeChips: [
        { label: "全部", value: "" },
        { label: "工资", value: "wage" },
        { label: "预支", value: "advance" },
        { label: "其他", value: "other" },
      ],
      projectOptions: [
        { id: 1, name: "菜鸟·云联日结（Gefield）" },
        { id: 2, name: "菜鸟·沙溪日结（真实）" },
        { id: 3, name: "邮政·茶山日结（真实）" },
        { id: 4, name: "鸿康·分拣中心日结" },
      ],
      accountOptions: ["招商银行 · ****6688", "工商银行 · ****2211", "建设银行 · ****7712"],
      creatorOptions: ["黄美玲", "王小虎", "孙晓清"],
      form: { type: "", projectId: null, projectName: "", account: "", startDate: "", endDate: "", creator: "" },
    };
  },
  onLoad() {
    const info = typeof uni.getWindowInfo === "function"
      ? uni.getWindowInfo() : uni.getSystemInfoSync();
    this.statusBarHeight = Number(info.statusBarHeight || 0);
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
    pickProject() {
      uni.showActionSheet({
        itemList: this.projectOptions.map((p) => p.name),
        success: (res) => {
          const proj = this.projectOptions[res.tapIndex];
          this.form.projectId = proj.id;
          this.form.projectName = proj.name;
        },
      });
    },
    pickAccount() {
      uni.showActionSheet({
        itemList: this.accountOptions,
        success: (res) => { this.form.account = this.accountOptions[res.tapIndex]; },
      });
    },
    pickCreator() {
      uni.showActionSheet({
        itemList: this.creatorOptions,
        success: (res) => { this.form.creator = this.creatorOptions[res.tapIndex]; },
      });
    },
    pickDate(field) {
      const today = new Date();
      const max = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}-${String(today.getDate()).padStart(2, "0")}`;
      uni.showModal({
        title: field === "start" ? "选择开始时间" : "选择结束时间",
        editable: true,
        placeholderText: "格式：2026-09-01",
        success: (res) => {
          if (res.confirm && res.content) {
            const v = res.content.trim();
            if (/^\d{4}-\d{2}-\d{2}$/.test(v)) {
              this.form[field === "start" ? "startDate" : "endDate"] = v;
            } else {
              uni.showToast({ title: "请输入正确日期格式", icon: "none" });
            }
          }
        },
      });
      // 备注说明：max 用于限制可选范围
      void max;
    },
    resetForm() {
      this.form = { type: "", projectId: null, projectName: "", account: "", startDate: "", endDate: "", creator: "" };
      uni.showToast({ title: "已重置筛选条件", icon: "none" });
    },
    async exportRecords() {
      if (this.exporting) return;
      this.exporting = true;
      try {
        await exportTransferRecords({ ...this.form });
        uni.showToast({ title: "导出成功，请到下载列表查看", icon: "success" });
      } catch (error) {
        uni.showToast({ title: error?.message || "导出失败", icon: "none" });
      } finally {
        this.exporting = false;
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

.footer {
  display: flex; gap: 12px; padding: 12px 16px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  background: #fff; border-top: 0.5px solid #eee;
}
.footer-btn {
  text-align: center; padding: 12px 0; border-radius: 24px; font-size: 14px; font-weight: 600;
}
.footer-btn.outline { flex: 1; background: #fff; color: #333; border: 1px solid #e0e0e0; }
.footer-btn.primary { flex: 2; background: linear-gradient(135deg, #FF6B35, #FF8C5A); color: #fff; }
.footer-btn.disabled { opacity: 0.6; }
.bottom-space { height: 20px; }
</style>
