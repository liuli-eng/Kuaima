<template>
  <view class="container">
    <BossPageHeader title="项目设置" />
    <scroll-view scroll-y class="body">
      <!-- 基本信息 -->
      <view class="section-title">基本信息</view>
      <view class="card">
        <view class="row" @click="editField('name', '项目名称', project.name)">
          <view class="row-main">
            <view class="row-title">项目名称</view>
            <view class="row-desc">{{ project.name || "—" }}</view>
          </view>
          <text class="row-arrow">›</text>
        </view>
        <view class="row" @click="editField('companyName', '用工企业', project.companyName)">
          <view class="row-main">
            <view class="row-title">用工企业</view>
            <view class="row-desc">{{ project.companyName || "—" }}</view>
          </view>
          <text class="row-arrow">›</text>
        </view>
        <view class="row" @click="editField('leaderName', '负责人', project.leaderName)">
          <view class="row-main">
            <view class="row-title">负责人</view>
            <view class="row-desc">{{ project.leaderName || "—" }}</view>
          </view>
          <text class="row-arrow">›</text>
        </view>
        <view class="row" @click="editField('location', '项目地点', project.location)">
          <view class="row-main">
            <view class="row-title">项目地点</view>
            <view class="row-desc">{{ project.location || "—" }}</view>
          </view>
          <text class="row-arrow">›</text>
        </view>
      </view>

      <!-- 打卡设置 -->
      <view class="section-title">打卡设置</view>
      <view class="card">
        <view class="row">
          <view class="row-main">
            <view class="row-title">定位打卡</view>
            <view class="row-desc">员工需到达项目地点附近才能打卡</view>
          </view>
          <view class="switch" :class="{ on: project.locationCheckin }" @click="toggleBool('locationCheckin')" />
        </view>
        <view class="row" @click="editField('checkinRadius', '打卡有效范围（米）', project.checkinRadius)">
          <view class="row-main">
            <view class="row-title">打卡有效范围</view>
            <view class="row-desc">默认 {{ project.checkinRadius || 200 }} 米</view>
          </view>
          <text class="row-arrow">›</text>
        </view>
        <view class="row" @click="editField('signCodeExpire', '签到码有效期（分钟）', project.signCodeExpire)">
          <view class="row-main">
            <view class="row-title">签到码有效期</view>
            <view class="row-desc">每次生成 {{ project.signCodeExpire || 60 }} 分钟</view>
          </view>
          <text class="row-arrow">›</text>
        </view>
        <view class="row">
          <view class="row-main">
            <view class="row-title">迟到自动判定</view>
            <view class="row-desc">迟到 {{ project.lateThreshold || 10 }} 分钟以上自动标记</view>
          </view>
          <view class="switch" :class="{ on: project.lateAuto }" @click="toggleBool('lateAuto')" />
        </view>
      </view>

      <!-- 薪资设置 -->
      <view class="section-title">薪资设置</view>
      <view class="card">
        <view class="row" @click="editField('payrollCycle', '发薪周期', project.payrollCycle)">
          <view class="row-main">
            <view class="row-title">发薪周期</view>
            <view class="row-desc">{{ project.payrollCycle || "月结（每月10日发薪）" }}</view>
          </view>
          <text class="row-arrow">›</text>
        </view>
        <view class="row" @click="editField('settleType', '结算方式', project.settleType)">
          <view class="row-main">
            <view class="row-title">结算方式</view>
            <view class="row-desc">{{ project.settleType || "按天结算" }}</view>
          </view>
          <text class="row-arrow">›</text>
        </view>
        <view class="row">
          <view class="row-main">
            <view class="row-title">发薪提醒</view>
            <view class="row-desc">发薪前 1 天提醒负责人</view>
          </view>
          <view class="switch" :class="{ on: project.salaryRemind }" @click="toggleBool('salaryRemind')" />
        </view>
      </view>

      <!-- 危险区域 -->
      <view class="section-title">危险操作</view>
      <view class="card">
        <view class="danger-tip">归档后项目将从项目列表隐藏，但历史数据仍可查询。删除项目将清空所有数据，操作不可恢复。</view>
        <view class="danger-btn" @click="archive">📦 归档项目</view>
        <view class="danger-btn" @click="remove">🗑 删除项目</view>
      </view>
      <view class="bottom-space" />
    </scroll-view>
  </view>
</template>

<script>
import BossPageHeader from "@/components/BossPageHeader.vue";
import {
  getProject,
  updateProject,
  archiveProject,
  deleteProject,
} from "@/api/project";

export default {
  components: { BossPageHeader },
  data() {
    return {
      projectId: "",
      project: {},
    };
  },
  onLoad(query) {
    this.projectId = query.id;
    this.load();
  },
  methods: {
    async load() {
      try {
        this.project = (await getProject(this.projectId).catch(() => ({}))) || {};
      } catch (e) {
        console.warn("项目设置加载失败", e);
      }
    },
    async patch(field, value) {
      try {
        await updateProject(this.projectId, { [field]: value });
      } catch (e) {
        uni.showToast({ title: "保存失败", icon: "none" });
        this.load();
      }
    },
    async toggleBool(field) {
      const next = !this.project[field];
      this.$set(this.project, field, next);
      await this.patch(field, next);
    },
    editField(field, title, current) {
      uni.showModal({
        title: `编辑${title}`,
        editable: true,
        content: current ? String(current) : "",
        placeholderText: "请输入",
        success: async (res) => {
          if (!res.confirm) return;
          const value = (res.content || "").trim();
          if (!value || value === String(current)) return;
          this.$set(this.project, field, value);
          await this.patch(field, value);
          uni.showToast({ title: "已保存", icon: "success" });
        },
      });
    },
    archive() {
      uni.showModal({
        title: "归档项目",
        content: "确认归档此项目？归档后将从列表隐藏，历史数据仍可查询。",
        success: async (res) => {
          if (!res.confirm) return;
          try {
            await archiveProject(this.projectId);
            uni.showToast({ title: "已归档", icon: "success" });
            setTimeout(() => uni.navigateBack(), 600);
          } catch (e) {
            uni.showToast({ title: "操作失败", icon: "none" });
          }
        },
      });
    },
    remove() {
      uni.showModal({
        title: "删除项目",
        content: "⚠ 删除不可恢复，确认删除此项目？",
        success: async (res) => {
          if (!res.confirm) return;
          try {
            await deleteProject(this.projectId);
            uni.showToast({ title: "已删除", icon: "success" });
            setTimeout(() => uni.reLaunch({ url: "/pages/boss/projects" }), 600);
          } catch (e) {
            uni.showToast({ title: "操作失败", icon: "none" });
          }
        },
      });
    },
  },
};
</script>

<style lang="scss" scoped>
.container { display: flex; flex-direction: column; height: 100vh; background: #f3f4f6; }
.body { flex: 1; overflow-y: auto; padding: 12px 16px 0; }
.section-title { font-size: 14px; font-weight: 600; color: #333; padding: 4px 4px 10px; }
.card { background: #fff; border-radius: 14px; padding: 0 16px; margin-bottom: 12px; box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04); }
.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 0;
  border-bottom: 0.5px solid #f0f0f0;
}
.row:last-child { border-bottom: none; }
.row-main { flex: 1; min-width: 0; }
.row-title { font-size: 14px; color: #333; }
.row-desc { font-size: 12px; color: #999; margin-top: 3px; }
.row-arrow { color: #c8c8c8; font-size: 16px; margin-left: 8px; }
.switch {
  width: 46px;
  height: 26px;
  background: #e0e0e0;
  border-radius: 13px;
  position: relative;
  flex-shrink: 0;
  transition: background 0.2s;
}
.switch::after {
  content: "";
  position: absolute;
  top: 3px;
  left: 3px;
  width: 20px;
  height: 20px;
  background: #fff;
  border-radius: 50%;
  transition: left 0.2s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}
.switch.on { background: linear-gradient(135deg, #ff6b35, #ff8c5a); }
.switch.on::after { left: 23px; }
.danger-tip { font-size: 12px; color: #999; line-height: 1.6; padding: 14px 0; }
.danger-btn {
  width: 100%;
  padding: 12px 0;
  border-radius: 12px;
  background: #fff1f1;
  color: #dc2626;
  font-size: 14px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 10px;
}
.bottom-space { height: 16px; }
</style>
