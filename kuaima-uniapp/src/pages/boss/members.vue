<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="wb-header" :style="{ paddingTop: statusBarHeight + 8 + 'px' }">
      <view class="wb-back" @click="goBack"><text class="back-ico">‹</text></view>
      <text class="wb-title">企业成员({{ members.length }})</text>
      <view class="wb-capsule">
        <view class="cap-btn"><text class="cap-ico">⋯</text></view>
        <view class="cap-divider"></view>
        <view class="cap-btn"><text class="cap-ico">○</text></view>
      </view>
    </view>

    <view class="wb-body">
      <!-- 搜索 -->
      <view class="wb-search">
        <text class="search-ico">🔍</text>
        <input type="text" v-model="keyword" placeholder="搜索成员" @confirm="loadMembers" />
      </view>

      <!-- 功能列表 -->
      <view class="wb-card">
        <view class="wb-row" @click="navigateTo('member-apply-list')">
          <view class="wb-row-icon" style="background: linear-gradient(135deg, #FFB84D, #F09A3E)"><text class="row-ico">📝</text></view>
          <view class="wb-row-main"><text class="wb-row-title">申请列表</text></view>
          <text class="wb-row-arrow">›</text>
        </view>
        <view class="wb-row" @click="navigateTo('member-invite')">
          <view class="wb-row-icon" style="background: linear-gradient(135deg, #FF8C5A, #FF6B35)"><text class="row-ico">👤</text></view>
          <view class="wb-row-main"><text class="wb-row-title">邀请新成员</text></view>
          <text class="wb-row-arrow">›</text>
        </view>
        <view class="wb-row" @click="navigateTo('invite-qrcode')">
          <view class="wb-row-icon" style="background: linear-gradient(135deg, #A78BFA, #7C3AED)"><text class="row-ico">📱</text></view>
          <view class="wb-row-main"><text class="wb-row-title">邀请二维码</text></view>
          <text class="wb-row-arrow">›</text>
        </view>
        <view class="wb-row" @click="navigateTo('member-exit')">
          <view class="wb-row-icon" style="background: linear-gradient(135deg, #FF7743, #FF5C33)"><text class="row-ico">🚪</text></view>
          <view class="wb-row-main"><text class="wb-row-title">退出当前企业</text></view>
          <text class="wb-row-arrow">›</text>
        </view>
      </view>

      <!-- 成员列表 -->
      <view class="wb-card" style="margin-top: 12px">
        <view v-if="loading" class="wb-empty"><text class="wb-empty-text">加载中...</text></view>
        <view v-else-if="!members.length" class="wb-empty">
          <view class="wb-empty-icon"><text>👥</text></view>
          <text class="wb-empty-text">暂无企业成员</text>
        </view>
        <view v-for="m in members" :key="m.id" class="wb-row" @click="goDetail(m)">
          <view class="wb-avatar" :style="{ background: avatarColor(m.userId + '') }">{{ memberName(m).charAt(0) }}</view>
          <view class="wb-row-main">
            <text class="wb-row-title">{{ memberName(m) }}</text>
            <text class="wb-row-desc">{{ roleDesc(m) }}</text>
          </view>
          <text class="wb-row-arrow">›</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getMembers } from "@/api/enterprise";
import { avatarColor } from "@/api/project";

export default {
  data() {
    return {
      statusBarHeight: 44,
      keyword: "",
      members: [],
      loading: false,
      nameCache: {},
    };
  },
  onLoad() {
    try {
      const info = uni.getSystemInfoSync();
      this.statusBarHeight = info.statusBarHeight || 44;
    } catch (e) {}
    this.loadMembers();
  },
  onShow() {
    this.loadMembers();
  },
  methods: {
    avatarColor,
    async loadMembers() {
      this.loading = true;
      try {
        const data = await getMembers(this.keyword);
        this.members = data || [];
      } catch (e) {
        uni.showToast({ title: e.message || "加载失败", icon: "none" });
      } finally {
        this.loading = false;
      }
    },
    memberName(m) {
      return m.name || `成员${m.userId}`;
    },
    roleDesc(m) {
      const roleText = { OWNER: "超级管理员", ADMIN: "管理员", STAFF: "成员" }[m.role] || m.role;
      return m.title ? `${roleText} · ${m.title}` : roleText;
    },
    goDetail(m) {
      uni.navigateTo({ url: `/pages/boss/member-detail?id=${m.id}&name=${encodeURIComponent(this.memberName(m))}` });
    },
    navigateTo(page) {
      uni.navigateTo({ url: `/pages/boss/${page}` });
    },
    goBack() {
      uni.navigateBack({ fail: () => uni.reLaunch({ url: "/pages/boss/workbench" }) });
    },
  },
};
</script>

<style lang="scss" scoped>
.container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f3f4f6;
}

.wb-header {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff;
  padding: 6px 16px 12px;
  flex-shrink: 0;
}

.wb-back {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-ico {
  font-size: 22px;
  color: #333;
}

.wb-title {
  flex: 1;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.wb-capsule {
  display: flex;
  align-items: center;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 17px;
  padding: 0 6px;
  height: 32px;
}

.cap-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cap-ico {
  font-size: 14px;
  color: #666;
}

.cap-divider {
  width: 1px;
  height: 16px;
  background: rgba(0, 0, 0, 0.15);
  margin: 0 2px;
}

.wb-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px 20px;
}

.wb-search {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  border-radius: 12px;
  padding: 11px 14px;
  margin-bottom: 12px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
}

.search-ico {
  font-size: 14px;
}

.wb-search input {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.wb-card {
  background: #fff;
  border-radius: 16px;
  padding: 4px 16px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
}

.wb-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 13px 0;
  border-bottom: 1px solid #f5f5f5;
}

.wb-row:last-child {
  border-bottom: none;
}

.wb-row-icon {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.row-ico {
  font-size: 17px;
}

.wb-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.wb-row-main {
  flex: 1;
  min-width: 0;
}

.wb-row-title {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  display: block;
}

.wb-row-desc {
  font-size: 12px;
  color: #999;
  margin-top: 3px;
  display: block;
}

.wb-row-arrow {
  font-size: 16px;
  color: #ccc;
}

.wb-empty {
  padding: 30px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.wb-empty-icon {
  font-size: 34px;
  color: #ddd;
  margin-bottom: 10px;
}

.wb-empty-text {
  font-size: 13px;
  color: #999;
}
</style>
