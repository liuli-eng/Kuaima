<template>
  <view class="page">
    <view class="nav" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="nav-inner">
        <view class="nav-back" @click="goBack">‹</view>
        <text class="nav-title">发单模版</text>
        <view class="nav-space" />
      </view>
    </view>

    <scroll-view scroll-y class="content">
      <view v-if="loading" class="state">模板信息加载中...</view>
      <view v-else-if="loadError" class="state">{{ loadError }}</view>
      <template v-else>
        <view class="tpl-card">
          <view class="tpl-name-row">模版名称：<text>{{ templateName }}</text></view>
          <view class="tpl-job-row">
            <text class="tpl-job-name">{{ jobName }}</text>
            <view class="tpl-job-price"><text>{{ wage }}</text><small>{{ unit }}</small></view>
          </view>
          <view class="tpl-tags">
            <text class="tpl-tag highlight">{{ tag }}</text>
            <text class="tpl-tag plain">{{ time }}</text>
            <text class="tpl-tag plain">{{ hours }}</text>
            <text class="tpl-tag plain">{{ count }}</text>
          </view>
          <view class="tpl-tags">
            <text class="tpl-tag plain">{{ age }}</text>
            <text class="tpl-tag plain">{{ gender }}</text>
          </view>
          <view class="tpl-actions">
            <text class="tpl-btn ghost" @click="deleteTemplate">删除</text>
            <text class="tpl-btn ghost" @click="renameTemplate">修改模版名称</text>
            <text class="tpl-btn main" @click="useTemplate">使用</text>
          </view>
        </view>
        <view class="no-more">没有更多了</view>
      </template>
    </scroll-view>
  </view>
</template>

<script>
import {
  createBossOrderTemplate,
  deleteBossOrderTemplate,
  getBossOrderTemplate,
  updateBossOrderTemplate,
} from "@/api/backend";
import { handleTokenInvalid } from "@/api/auth";

function getErrorMessage(error, fallback) {
  if (Number(error?.code || error?.statusCode) === 401) return "登录已失效，请重新登录";
  if (Number(error?.code || error?.statusCode) === 403) return "无权操作";
  if (Number(error?.code || error?.statusCode) === 404) return "模板不存在";
  return error?.message || fallback;
}

export default {
  data() {
    return {
      statusBarHeight: 0,
      orderId: "",
      templateId: null,
      templateName: "",
      jobName: "",
      wage: "0",
      unit: "元/天",
      tag: "每天日结",
      time: "时间待定",
      hours: "工时待定",
      count: "招0人",
      age: "18岁-不限",
      gender: "男女不限",
      loading: false,
      loadError: "",
      operating: false,
    };
  },
  onLoad(options = {}) {
    const info = typeof uni.getWindowInfo === "function" ? uni.getWindowInfo() : uni.getSystemInfoSync();
    this.statusBarHeight = Number(info.statusBarHeight || 0);
    this.orderId = options.orderId || "";
    this.templateId = options.templateId || null;
    this.templateName = options.name || "未命名模板";
    this.jobName = options.name || "未命名岗位";
    this.wage = options.wage || "0";
    this.unit = options.unit || "元/天";
    this.tag = options.tag || "每天日结";
    this.time = options.time || "时间待定";
    this.hours = options.hours || "工时待定";
    this.count = options.count || "招0人";
    this.age = options.age || "18岁-不限";
    this.gender = options.gender || "男女不限";
    if (this.templateId) this.loadTemplateDetail(this.templateId);
  },
  methods: {
    async loadTemplateDetail(id) {
      this.loading = true;
      this.loadError = "";
      try {
        const detail = await getBossOrderTemplate(id);
        this.applyTemplateDetail(detail || {});
      } catch (error) {
        this.loadError = getErrorMessage(error, "模板加载失败");
        await this.handleRequestError(error, "模板加载失败");
      } finally {
        this.loading = false;
      }
    },
    applyTemplateDetail(detail) {
      this.templateId = detail.id || detail.templateId || this.templateId;
      this.orderId = detail.sourceOrderId || detail.orderId || this.orderId;
      this.templateName = detail.templateName || detail.orderTitle || this.templateName || "未命名模板";
      this.jobName = detail.orderTitle || detail.postion || this.jobName || "未命名岗位";
      this.wage = detail.salary ?? this.wage ?? "0";
      this.unit = detail.unit || this.unit || "元/天";
      this.tag = Array.isArray(detail.tags) ? detail.tags.join("、") : (detail.tags || this.tag || "每天日结");
      const start = detail.startTime ? String(detail.startTime).replace("T", " ").slice(0, 16) : "";
      const end = detail.endTime ? String(detail.endTime).replace("T", " ").slice(0, 16) : "";
      this.time = start && end ? `${start} ~ ${end}` : (detail.time || this.time || "时间待定");
      this.hours = detail.duration ? `${detail.duration}${String(detail.duration).includes("工") ? "" : "工时"}` : (this.hours || "工时待定");
      this.count = detail.orderNum != null ? `招${detail.orderNum}人` : (this.count || "招0人");
      this.age = detail.experience || this.age || "18岁-不限";
      this.gender = detail.gender || this.gender || "男女不限";
    },
    async handleRequestError(error, fallback) {
      const status = Number(error?.code || error?.statusCode);
      if (status === 401) return handleTokenInvalid({ role: "boss" });
      uni.showToast({ title: getErrorMessage(error, fallback), icon: "none" });
    },
    goBack() {
      uni.navigateBack();
    },
    async saveTemplate(overwrite = false) {
      if (!this.orderId || !this.templateName.trim() || this.operating) {
        if (!this.orderId) uni.showToast({ title: "缺少订单信息", icon: "none" });
        return false;
      }
      this.operating = true;
      try {
        const result = await createBossOrderTemplate(this.orderId, {
          templateName: this.templateName.trim(),
          overwrite,
        });
        this.templateId = result?.id || result?.templateId || this.templateId;
        uni.$emit("bossTemplateChanged");
        uni.showToast({ title: overwrite ? "模板已覆盖" : "模板已收藏", icon: "success" });
        return true;
      } catch (error) {
        const duplicate = Number(error?.code || error?.statusCode) === 400 || /重复|已存在|duplicate/i.test(error?.message || "");
        if (!overwrite && duplicate) {
          uni.showModal({
            title: "模板名称重复",
            content: error?.message || "已存在同名模板，是否覆盖？",
            confirmText: "覆盖",
            success: ({ confirm }) => {
              if (confirm) this.saveTemplate(true);
            },
          });
        } else {
          await this.handleRequestError(error, "模板保存失败");
        }
        return false;
      } finally {
        this.operating = false;
      }
    },
    renameTemplate() {
      if (!this.templateId || this.operating) {
        uni.showToast({ title: "请先保存模板", icon: "none" });
        return;
      }
      uni.showModal({
        title: "修改模版名称",
        editable: true,
        placeholderText: "请输入模版名称",
        content: this.templateName,
        success: ({ confirm, content }) => {
          if (confirm && content?.trim()) this.renameTemplateRequest(content.trim());
        },
      });
    },
    async renameTemplateRequest(name) {
      this.operating = true;
      try {
        await updateBossOrderTemplate(this.templateId, { templateName: name });
        this.templateName = name;
        await this.loadTemplateDetail(this.templateId);
        uni.$emit("bossTemplateChanged");
        uni.showToast({ title: "名称已修改", icon: "success" });
      } catch (error) {
        await this.handleRequestError(error, "模板名称修改失败");
      } finally {
        this.operating = false;
      }
    },
    async deleteTemplate() {
      if (!this.templateId || this.operating) {
        uni.showToast({ title: "请先保存模板", icon: "none" });
        return;
      }
      uni.showModal({
        title: "删除模版",
        content: "确认删除当前模版？",
        confirmColor: "#f5222d",
        success: async ({ confirm }) => {
          if (!confirm) return;
          this.operating = true;
          try {
            await deleteBossOrderTemplate(this.templateId);
            uni.$emit("bossTemplateChanged");
            uni.showToast({ title: "模版已删除", icon: "success" });
            setTimeout(() => uni.navigateBack(), 500);
          } catch (error) {
            await this.handleRequestError(error, "模版删除失败");
          } finally {
            this.operating = false;
          }
        },
      });
    },
    async useTemplate() {
      if (this.templateId) {
        uni.navigateTo({ url: "/pages/boss/home" });
        return;
      }
      const saved = await this.saveTemplate(false);
      if (saved) uni.navigateTo({ url: "/pages/boss/home" });
    },
  },
};
</script>

<style lang="scss" scoped>
.page { height:100vh; display:flex; flex-direction:column; overflow:hidden; background:#f3f4f6; }
.nav { flex-shrink:0; background:#fff; border-bottom:1px solid #f0f0f0; }
.nav-inner { height:50px; display:flex; align-items:center; padding:0 16px; position:relative; }
.nav-back { width:32px; height:32px; display:flex; align-items:center; justify-content:center; color:#333; font-size:26px; }
.nav-title { position:absolute; left:50%; transform:translateX(-50%); color:#222; font-size:17px; font-weight:700; }
.nav-space { width:32px; margin-left:auto; }
.content { flex:1; min-height:0; padding:12px 16px 30px; box-sizing:border-box; }
.tpl-card { padding:16px; margin-bottom:12px; border-radius:14px; background:#fff; box-shadow:0 2px 8px rgba(0,0,0,.04); }
.tpl-name-row { padding-bottom:12px; border-bottom:1px solid #f5f5f5; color:#222; font-size:16px; font-weight:700; }
.tpl-job-row { display:flex; align-items:flex-start; justify-content:space-between; gap:10px; margin-top:12px; }
.tpl-job-name { color:#333; font-size:15px; font-weight:600; }
.tpl-job-price { flex-shrink:0; color:#ff4d1f; font-size:22px; font-weight:800; line-height:1.1; }
.tpl-job-price small { margin-left:2px; font-size:12px; font-weight:600; }
.tpl-tags { display:flex; flex-wrap:wrap; align-items:center; gap:8px; margin-top:10px; }
.tpl-tag { padding:2px 8px; border-radius:4px; font-size:12px; }
.tpl-tag.highlight { color:#fff; background:#ff6b35; font-weight:600; }
.tpl-tag.plain { color:#666; border:1px solid #ebedf0; background:#f7f8fa; }
.tpl-actions { display:flex; justify-content:flex-end; gap:10px; margin-top:14px; padding-top:14px; border-top:1px solid #f5f5f5; }
.tpl-btn { padding:8px 18px; border-radius:999px; font-size:14px; }
.tpl-btn.ghost { color:#333; border:1px solid #e5e5e5; background:#fff; }
.tpl-btn.main { padding:8px 26px; color:#3e2723; background:#ffd96f; font-weight:700; }
.no-more { padding:24px 0; color:#b0b0b0; font-size:14px; text-align:center; }
.state { padding:120px 0; color:#999; font-size:14px; text-align:center; }
</style>
