<template>
  <view class="container">
    <BossPageHeader title="项目成员">
      <view slot="right" class="cap-btn" @click="openAdd">
        <text class="cap-plus">＋</text>
      </view>
    </BossPageHeader>
    <scroll-view scroll-y class="body">
      <!-- 当前项目 banner -->
      <view class="pm-banner">
        <view class="pm-icon"><text class="pm-emoji">💼</text></view>
        <view class="pm-info">
          <text class="pm-name">{{ project.name || "项目" }}</text>
          <text class="pm-sub">用工企业：{{ project.companyName || "—" }} · 负责人：{{ project.leaderName || "—" }}</text>
        </view>
      </view>

      <!-- 统计 -->
      <view class="pm-stats">
        <view class="pm-stat">
          <text class="pm-stat-value">{{ stats.all || 0 }}</text>
          <text class="pm-stat-label">全部</text>
        </view>
        <view class="pm-stat">
          <text class="pm-stat-value">{{ stats.active || 0 }}</text>
          <text class="pm-stat-label">在职</text>
        </view>
        <view class="pm-stat">
          <text class="pm-stat-value">{{ stats.left || 0 }}</text>
          <text class="pm-stat-label">离职</text>
        </view>
        <view class="pm-stat">
          <text class="pm-stat-value">{{ stats.temp || 0 }}</text>
          <text class="pm-stat-label">临时</text>
        </view>
      </view>

      <!-- Tab 过滤 -->
      <view class="pm-tabs">
        <view
          v-for="t in tabs"
          :key="t.key"
          class="pm-tab"
          :class="{ active: status === t.key }"
          @click="switchStatus(t.key)"
        >{{ t.label }}</view>
      </view>

      <!-- 成员列表 -->
      <view v-if="list.length" class="pm-list">
        <view v-for="m in list" :key="m.id" class="pm-card">
          <view class="pm-avatar" :style="{ background: avatarColor(m.name) }">
            {{ (m.name || "?").charAt(0) }}
          </view>
          <view class="pm-info">
            <view class="pm-name-line">
              <text class="pm-name">{{ m.name }}</text>
              <text class="pm-role">{{ m.role || "成员" }}</text>
            </view>
            <view class="pm-sub">
              <text>{{ m.phone || "—" }}</text>
              <text>入职 {{ formatCnDate(m.joinDate) || "—" }}</text>
            </view>
          </view>
          <view class="pm-actions">
            <view class="pm-btn outline" @click="showDetail(m)">详情</view>
          </view>
        </view>
      </view>
      <view v-else class="empty">
        <text class="empty-ico">👥</text>
        <text class="empty-text">{{ loading ? "加载中…" : "该状态下暂无成员" }}</text>
      </view>
      <view class="bottom-space" />
    </scroll-view>

    <!-- 添加成员弹层 -->
    <view v-if="showAdd" class="mask" @click="closeAdd">
      <view class="sheet" @click.stop>
        <view class="sheet-title">添加项目成员</view>
        <view class="form-item">
          <text class="form-label">姓名<text class="req">*</text></text>
          <input class="form-input" v-model="form.name" placeholder="成员姓名" />
        </view>
        <view class="form-item">
          <text class="form-label">手机号</text>
          <input class="form-input" v-model="form.phone" placeholder="手机号" />
        </view>
        <view class="form-item">
          <text class="form-label">岗位</text>
          <input class="form-input" v-model="form.role" placeholder="如：分拣员" />
        </view>
        <view class="sheet-actions">
          <view class="sheet-btn cancel" @click="closeAdd">取消</view>
          <view class="sheet-btn confirm" @click="submitAdd">确认添加</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import BossPageHeader from "@/components/BossPageHeader.vue";
import {
  getProject,
  getMemberStats,
  listMembers,
  addMember,
  avatarColor,
  formatCnDate,
} from "@/api/project";

export default {
  components: { BossPageHeader },
  data() {
    return {
      projectId: "",
      project: {},
      stats: {},
      status: "all",
      tabs: [
        { key: "all", label: "全部" },
        { key: "active", label: "在职" },
        { key: "temp", label: "临时" },
        { key: "left", label: "已离职" },
      ],
      list: [],
      loading: false,
      showAdd: false,
      form: { name: "", phone: "", role: "" },
    };
  },
  onLoad(query) {
    this.projectId = query.id;
    this.load();
  },
  methods: {
    avatarColor,
    formatCnDate,
    async load() {
      this.loading = true;
      try {
        const [project, stats, list] = await Promise.all([
          getProject(this.projectId).catch(() => ({})),
          getMemberStats(this.projectId).catch(() => ({})),
          listMembers(this.projectId, this.status).catch(() => []),
        ]);
        this.project = project || {};
        this.stats = stats || {};
        this.list = Array.isArray(list) ? list : [];
      } catch (e) {
        console.warn("成员列表加载失败", e);
        this.list = [];
      } finally {
        this.loading = false;
      }
    },
    switchStatus(key) {
      if (this.status === key) return;
      this.status = key;
      this.load();
    },
    showDetail(m) {
      uni.showToast({ title: `${m.name} · ${m.role || "成员"}`, icon: "none" });
    },
    openAdd() {
      this.form = { name: "", phone: "", role: "" };
      this.showAdd = true;
    },
    closeAdd() {
      this.showAdd = false;
    },
    async submitAdd() {
      if (!this.form.name.trim()) {
        uni.showToast({ title: "请填写成员姓名", icon: "none" });
        return;
      }
      try {
        await addMember(this.projectId, {
          name: this.form.name.trim(),
          phone: this.form.phone.trim(),
          role: this.form.role.trim(),
          status: "temp",
        });
        uni.showToast({ title: "添加成功", icon: "success" });
        this.showAdd = false;
        this.load();
      } catch (e) {
        uni.showToast({ title: "添加失败，请重试", icon: "none" });
      }
    },
  },
};
</script>

<style lang="scss" scoped>
.container { display: flex; flex-direction: column; height: 100vh; background: #f3f4f6; }
.body { flex: 1; overflow-y: auto; padding: 12px 16px 0; }
.cap-btn {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #fff0e8;
  color: #ff6b35;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cap-plus { font-size: 18px; font-weight: 700; }
.pm-banner {
  background: linear-gradient(135deg, #fff7f0, #ffe9d8);
  border-radius: 14px;
  padding: 14px 16px;
  margin-bottom: 14px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.pm-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.pm-emoji { font-size: 20px; }
.pm-info { flex: 1; min-width: 0; }
.pm-name { font-size: 15px; font-weight: 600; color: #333; display: block; }
.pm-sub { font-size: 12px; color: #999; margin-top: 3px; display: block; }
.pm-stats {
  display: flex;
  background: #fff;
  border-radius: 12px;
  padding: 12px 0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  margin-bottom: 14px;
}
.pm-stat { flex: 1; text-align: center; position: relative; }
.pm-stat:not(:last-child)::after {
  content: "";
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  height: 20px;
  width: 0.5px;
  background: #f0f0f0;
}
.pm-stat-value { font-size: 18px; font-weight: 700; color: #ff6b35; display: block; }
.pm-stat-label { font-size: 11px; color: #999; margin-top: 3px; display: block; }
.pm-tabs { display: flex; gap: 8px; margin-bottom: 12px; }
.pm-tab {
  padding: 6px 14px;
  border-radius: 14px;
  font-size: 12px;
  color: #666;
  background: #f5f5f5;
}
.pm-tab.active { background: linear-gradient(135deg, #ff6b35, #ff8c5a); color: #fff; font-weight: 600; }
.pm-card {
  background: #fff;
  border-radius: 14px;
  padding: 14px 16px;
  margin-bottom: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  display: flex;
  gap: 12px;
  align-items: center;
}
.pm-avatar {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 17px;
  font-weight: 600;
}
.pm-info { flex: 1; min-width: 0; }
.pm-name-line { display: flex; align-items: center; gap: 6px; }
.pm-name { font-size: 15px; font-weight: 600; color: #333; }
.pm-role { font-size: 11px; padding: 1px 6px; border-radius: 4px; background: #fff0e8; color: #ff6b35; }
.pm-sub { font-size: 12px; color: #999; margin-top: 4px; display: flex; gap: 10px; }
.pm-actions { display: flex; flex-direction: column; gap: 6px; }
.pm-btn {
  padding: 4px 10px;
  border-radius: 10px;
  font-size: 11px;
}
.pm-btn.outline { background: #fff0e8; color: #ff6b35; }
.empty { text-align: center; padding: 40px 0; color: #bbb; }
.empty-ico { font-size: 32px; display: block; margin-bottom: 10px; }
.empty-text { font-size: 13px; }
.bottom-space { height: 16px; }
.mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 90;
  display: flex;
  align-items: flex-end;
}
.sheet { width: 100%; background: #fff; border-radius: 18px 18px 0 0; padding: 20px 16px calc(20px + env(safe-area-inset-bottom)); }
.sheet-title { font-size: 17px; font-weight: 700; color: #222; margin-bottom: 14px; }
.form-item { display: flex; align-items: center; padding: 12px 0; border-bottom: 1px solid #f5f5f5; }
.form-label { font-size: 14px; color: #333; width: 80px; flex-shrink: 0; }
.req { color: #ff3b30; margin-left: 2px; }
.form-input { flex: 1; border: none; outline: none; font-size: 14px; color: #333; text-align: right; }
.sheet-actions { display: flex; gap: 12px; margin-top: 18px; }
.sheet-btn { flex: 1; text-align: center; padding: 12px 0; border-radius: 24px; font-size: 15px; font-weight: 600; }
.sheet-btn.cancel { background: #f3f4f6; color: #666; }
.sheet-btn.confirm { background: linear-gradient(135deg, #ff6b35, #ff8c5a); color: #fff; }
</style>
