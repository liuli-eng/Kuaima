<template>
  <view class="container">
    <BossPageHeader title="驻场管理">
      <view slot="right" class="cap-btn" @click="goAdd">
        <text class="cap-plus">＋</text>
      </view>
    </BossPageHeader>
    <scroll-view scroll-y class="body">
      <view class="tip">项目负责人可管理驻场人员，驻场人员拥有项目最高管理权限</view>

      <view class="count-row">
        <text>共 <text class="count-num">{{ list.length }}</text> 名驻场人员</text>
        <text class="count-more" @click="goAdd">添加驻场 ›</text>
      </view>

      <view v-for="s in list" :key="s.id" class="onsite-card">
        <view class="onsite-avatar" :style="{ background: avatarColor(s.name) }">
          {{ (s.name || "?").charAt(0) }}
        </view>
        <view class="onsite-info">
          <view class="onsite-name">
            {{ s.name }}
            <text v-if="s.onsiteRole === 'leader'" class="onsite-tag">项目负责人</text>
          </view>
          <view class="onsite-sub">{{ s.company || "—" }}</view>
          <view class="onsite-meta">
            <text class="meta-item">驻场 <text class="meta-num">{{ s.onsiteDays || 0 }}</text> 天</text>
            <text class="meta-item">{{ s.phone || "—" }}</text>
          </view>
        </view>
        <view class="onsite-action">
          <view class="onsite-btn danger" @click="remove(s)">移除</view>
        </view>
      </view>

      <view v-if="!list.length" class="empty">
        <text class="empty-ico">📍</text>
        <text class="empty-text">{{ loading ? "加载中…" : "暂无驻场人员" }}</text>
      </view>
      <view class="bottom-space" />
    </scroll-view>

    <view class="footer">
      <view class="primary-btn" @click="goAdd">
        <text class="primary-btn-ico">＋</text>添加驻场人员
      </view>
    </view>
  </view>
</template>

<script>
import BossPageHeader from "@/components/BossPageHeader.vue";
import {
  listOnsite,
  removeOnsite,
  avatarColor,
} from "@/api/project";

export default {
  components: { BossPageHeader },
  data() {
    return {
      projectId: "",
      list: [],
      loading: false,
    };
  },
  onLoad(query) {
    this.projectId = query.id;
  },
  onShow() {
    this.load();
  },
  methods: {
    avatarColor,
    async load() {
      this.loading = true;
      try {
        const list = await listOnsite(this.projectId).catch(() => []);
        this.list = Array.isArray(list) ? list : [];
      } catch (e) {
        console.warn("驻场列表加载失败", e);
        this.list = [];
      } finally {
        this.loading = false;
      }
    },
    goAdd() {
      uni.navigateTo({ url: `/pages/boss/proj-onsite-add?id=${this.projectId}` });
    },
    async remove(s) {
      uni.showModal({
        title: "确认移除",
        content: `确认移除驻场人员「${s.name}」？`,
        success: async (res) => {
          if (!res.confirm) return;
          try {
            await removeOnsite(this.projectId, s.id);
            uni.showToast({ title: "已移除", icon: "success" });
            this.load();
          } catch (e) {
            uni.showToast({ title: "移除失败", icon: "none" });
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
.tip { font-size: 13px; color: #999; margin-bottom: 10px; }
.count-row {
  font-size: 12px;
  color: #666;
  padding: 0 2px 8px;
  display: flex;
  justify-content: space-between;
}
.count-num { color: #ff6b35; font-weight: 700; }
.count-more { color: #999; }
.onsite-card {
  background: #fff;
  border-radius: 14px;
  padding: 14px;
  margin-bottom: 10px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
  display: flex;
  gap: 12px;
  align-items: center;
}
.onsite-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 17px;
  font-weight: 600;
}
.onsite-info { flex: 1; min-width: 0; }
.onsite-name { font-size: 15px; font-weight: 600; color: #333; display: flex; align-items: center; gap: 6px; }
.onsite-tag { font-size: 10px; padding: 1px 6px; border-radius: 4px; background: #fff0e8; color: #ff6b35; }
.onsite-sub { font-size: 12px; color: #999; margin-top: 4px; }
.onsite-meta { display: flex; gap: 12px; margin-top: 6px; }
.meta-item { font-size: 11px; color: #666; }
.meta-num { color: #ff6b35; font-weight: 600; }
.onsite-action { display: flex; flex-direction: column; gap: 6px; }
.onsite-btn { width: 44px; height: 28px; border-radius: 14px; font-size: 11px; display: flex; align-items: center; justify-content: center; }
.onsite-btn.danger { background: #fee2e2; color: #dc2626; }
.empty { text-align: center; padding: 40px 0; color: #bbb; }
.empty-ico { font-size: 32px; display: block; margin-bottom: 10px; }
.empty-text { font-size: 13px; }
.bottom-space { height: 16px; }
.footer {
  flex-shrink: 0;
  padding: 10px 16px calc(10px + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 0.5px solid #f0f0f0;
}
.primary-btn {
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: #fff;
  border-radius: 24px;
  text-align: center;
  padding: 13px 0;
  font-size: 15px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
}
.primary-btn-ico { margin-right: 4px; font-weight: 700; }
</style>
