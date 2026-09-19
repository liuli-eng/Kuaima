<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="wb-header" :style="{ paddingTop: statusBarHeight + 8 + 'px' }">
      <view class="wb-back" @click="goBack"><text class="back-ico">‹</text></view>
      <text class="wb-title">成员详情</text>
      <view class="wb-capsule">
        <view class="cap-btn"><text class="cap-ico">⋯</text></view>
        <view class="cap-divider"></view>
        <view class="cap-btn"><text class="cap-ico">○</text></view>
      </view>
    </view>

    <view class="wb-body has-footer">
      <view v-if="loading" class="wb-empty"><text class="wb-empty-text">加载中...</text></view>
      <template v-else>
        <!-- 成员信息头 -->
        <view class="md-head">
          <view class="md-avatar" :style="{ background: avatarColor(detail.userId + '') }">{{ (detail.name || '成').charAt(0) }}</view>
          <view class="md-head-info">
            <view class="md-name-line">
              <text class="md-name">{{ detail.name }}</text>
              <text class="md-role-tag" :class="{ boss: detail.role === 'OWNER' }">{{ roleText(detail.role) }}</text>
            </view>
            <text class="md-sub">{{ detail.title ? detail.title + ' · ' : '' }}{{ detail.phone }}</text>
          </view>
        </view>

        <!-- 自我账号提示 -->
        <view v-if="isSelf" class="self-tip">
          <text>ℹ️ 这是当前登录账号，超级管理员不支持编辑角色或移出企业</text>
        </view>

        <!-- 基本信息 -->
        <view class="md-block-title"><text class="block-ico">🪪</text>基本信息</view>
        <view class="md-info">
          <view class="md-line"><text class="md-key">手机号</text><text class="md-val">{{ detail.phone }}</text></view>
          <view class="md-line"><text class="md-key">企业角色</text><text class="md-val">{{ roleText(detail.role) }}{{ detail.title ? ' · ' + detail.title : '' }}</text></view>
          <view class="md-line"><text class="md-key">加入时间</text><text class="md-val">{{ detail.joinDate || '—' }}</text></view>
          <view class="md-line"><text class="md-key">邀请来源</text><text class="md-val">{{ detail.invitedByName || '—' }}</text></view>
          <view class="md-line"><text class="md-key">负责岗位</text><text class="md-val">{{ detail.jobCount || 0 }} 个</text></view>
        </view>

        <!-- 权限明细 -->
        <view class="md-block-title"><text class="block-ico">🛡</text>权限明细</view>
        <view class="pm-card">
          <view v-for="(p, i) in detail.permissions || []" :key="p" class="pm-item">
            <text :class="hasPerm(i) ? 'ok' : 'off'">{{ hasPerm(i) ? '✓' : '○' }}</text>
            <text class="pm-name">{{ p }}</text>
            <text class="pm-desc">{{ hasPerm(i) ? permDescs[i] : '无权限' }}</text>
          </view>
        </view>
      </template>
    </view>

    <!-- 底部操作 -->
    <view v-if="!loading && !isSelf" class="wb-footer">
      <view class="wb-btn-outline-red" @click="openRemove">➖ 移出企业</view>
      <view class="wb-btn-primary" @click="openRole">✏️ 编辑角色</view>
    </view>

    <!-- 编辑角色弹窗 -->
    <view class="wb-modal" v-if="roleModalShow" @click.self="closeRole">
      <view class="wb-modal-box">
        <view class="wb-modal-header">
          <text class="wb-modal-title">编辑角色</text>
          <text class="wb-modal-close" @click="closeRole">✕</text>
        </view>
        <view class="wb-modal-body">
          <view class="rd-opt" :class="{ active: pickedRole === 'ADMIN' }" @click="pickedRole = 'ADMIN'">
            <view class="rd-radio"></view>
            <view class="rd-info">
              <text class="rd-name">管理员</text>
              <text class="rd-desc">可管理岗位与成员、制单发薪、审批考勤，可协助企业日常运营</text>
            </view>
          </view>
          <view class="rd-opt" :class="{ active: pickedRole === 'STAFF' }" @click="pickedRole = 'STAFF'">
            <view class="rd-radio"></view>
            <view class="rd-info">
              <text class="rd-name">成员</text>
              <text class="rd-desc">仅可打卡考勤、查看自己的工资单与排班安排</text>
            </view>
          </view>
        </view>
        <view class="wb-modal-footer">
          <view class="wb-btn-outline" @click="closeRole">取消</view>
          <view class="wb-btn-primary" @click="saveRole">保存</view>
        </view>
      </view>
    </view>

    <!-- 移出确认弹窗 -->
    <view class="wb-modal" v-if="removeModalShow" @click.self="closeRemove">
      <view class="wb-modal-box">
        <view class="wb-modal-body">
          <text class="modal-icon red">➖</text>
          <text class="modal-title">将该成员移出企业？</text>
          <text class="modal-desc">移出后其账号将无法访问企业数据\n历史考勤与发薪记录将保留</text>
        </view>
        <view class="wb-modal-footer">
          <view class="wb-btn-outline" @click="closeRemove">取消</view>
          <view class="wb-btn-danger" @click="confirmRemove">确认移出</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getMemberDetail, updateMemberRole, removeMember } from "@/api/enterprise";
import { avatarColor } from "@/api/project";

export default {
  data() {
    return {
      statusBarHeight: 44,
      memberId: "",
      detail: {},
      loading: false,
      isSelf: false,
      roleModalShow: false,
      pickedRole: "ADMIN",
      removeModalShow: false,
      permDescs: ["发布/编辑零工岗位", "邀请成员、分配角色", "创建发薪单并转账", "审核转账发薪申请", "查看驻场考勤数据", "导出发薪/考勤记录"],
    };
  },
  onLoad(options) {
    this.memberId = options.id || "";
    try {
      const info = uni.getSystemInfoSync();
      this.statusBarHeight = info.statusBarHeight || 44;
    } catch (e) {}
    this.loadDetail();
  },
  methods: {
    avatarColor,
    roleText(role) {
      return { OWNER: "超级管理员", ADMIN: "管理员", STAFF: "成员" }[role] || role || "成员";
    },
    hasPerm(i) {
      return (this.detail.permIndexes || []).includes(i);
    },
    async loadDetail() {
      if (!this.memberId) return;
      this.loading = true;
      try {
        this.detail = (await getMemberDetail(this.memberId)) || {};
      } catch (e) {
        uni.showToast({ title: e.message || "加载失败", icon: "none" });
      } finally {
        this.loading = false;
      }
    },
    openRole() {
      this.pickedRole = this.detail.role === "ADMIN" ? "ADMIN" : "STAFF";
      this.roleModalShow = true;
    },
    closeRole() {
      this.roleModalShow = false;
    },
    async saveRole() {
      try {
        await updateMemberRole(this.memberId, this.pickedRole);
        uni.showToast({ title: `已更新角色为：${this.roleText(this.pickedRole)}`, icon: "none" });
        this.closeRole();
        this.loadDetail();
      } catch (e) {
        uni.showToast({ title: e.message || "操作失败", icon: "none" });
      }
    },
    openRemove() {
      this.removeModalShow = true;
    },
    closeRemove() {
      this.removeModalShow = false;
    },
    async confirmRemove() {
      try {
        await removeMember(this.memberId);
        uni.showToast({ title: "已将该成员移出企业", icon: "none" });
        this.closeRemove();
        setTimeout(() => uni.navigateBack({ fail: () => uni.reLaunch({ url: "/pages/boss/members" }) }), 600);
      } catch (e) {
        uni.showToast({ title: e.message || "操作失败", icon: "none" });
      }
    },
    goBack() {
      uni.navigateBack({ fail: () => uni.reLaunch({ url: "/pages/boss/members" }) });
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

.wb-body.has-footer {
  padding-bottom: 90px;
}

.md-head {
  background: #fff;
  border-radius: 16px;
  padding: 18px 16px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 13px;
}

.md-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  color: #fff;
  font-size: 20px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.md-head-info {
  flex: 1;
  min-width: 0;
}

.md-name-line {
  display: flex;
  align-items: center;
  gap: 7px;
  flex-wrap: wrap;
}

.md-name {
  font-size: 17px;
  font-weight: 700;
  color: #333;
}

.md-role-tag {
  background: #fff0e8;
  color: #ff6b35;
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 5px;
  font-weight: 600;
}

.md-role-tag.boss {
  background: #fff7e0;
  color: #d97706;
}

.md-sub {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  display: block;
}

.self-tip {
  display: flex;
  align-items: center;
  gap: 9px;
  background: #fff7e0;
  border-radius: 12px;
  padding: 11px 14px;
  font-size: 12px;
  color: #92600a;
  margin-bottom: 14px;
}

.md-block-title {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 14px;
  font-weight: 700;
  color: #333;
  margin: 16px 2px 10px;
}

.block-ico {
  font-size: 13px;
}

.md-info {
  background: #fff;
  border-radius: 16px;
  padding: 4px 16px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
}

.md-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 13px 0;
  border-bottom: 1px solid #f5f5f5;
  font-size: 13px;
}

.md-line:last-child {
  border-bottom: none;
}

.md-key {
  color: #999;
}

.md-val {
  color: #333;
  font-weight: 500;
}

.pm-card {
  background: #fff;
  border-radius: 16px;
  padding: 6px 16px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
}

.pm-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 0;
  border-bottom: 1px solid #f5f5f5;
  font-size: 13px;
  color: #333;
}

.pm-item:last-child {
  border-bottom: none;
}

.ok {
  color: #10b981;
  font-size: 14px;
}

.off {
  color: #d8d8d8;
  font-size: 14px;
}

.pm-name {
  flex-shrink: 0;
}

.pm-desc {
  margin-left: auto;
  font-size: 11px;
  color: #b0b0b0;
}

.wb-empty {
  padding: 40px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.wb-empty-text {
  font-size: 13px;
  color: #999;
}

.wb-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 10px;
  padding: 12px 16px calc(12px + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.98);
  border-top: 0.5px solid rgba(0, 0, 0, 0.05);
}

.wb-footer view {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  border-radius: 9999px;
  font-size: 14px;
  font-weight: 600;
}

.wb-btn-outline-red {
  color: #ff5c33;
  border: 1px solid #ffd0d0;
  background: #fff;
}

.wb-btn-primary {
  flex: 1.3 !important;
  color: #fff;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  box-shadow: 0 3px 8px rgba(255, 107, 53, 0.25);
}

.wb-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
  padding: 32px;
}

.wb-modal-box {
  background: #fff;
  border-radius: 18px;
  width: 100%;
  overflow: hidden;
}

.wb-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px 0;
}

.wb-modal-title {
  font-size: 16px;
  font-weight: 700;
  color: #333;
}

.wb-modal-close {
  font-size: 16px;
  color: #999;
  padding: 4px;
}

.wb-modal-body {
  padding: 16px 18px;
}

.wb-modal-body.center {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 24px;
}

.modal-icon {
  font-size: 32px;
}

.modal-icon.red {
  color: #ff5c33;
}

.modal-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-top: 12px;
}

.modal-desc {
  font-size: 13px;
  color: #999;
  margin-top: 8px;
  line-height: 20px;
  white-space: pre-line;
}

.rd-opt {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  border: 1.5px solid #eee;
  border-radius: 14px;
  padding: 14px;
  margin-bottom: 10px;
}

.rd-opt.active {
  border-color: #ff6b35;
  background: #fff5ee;
}

.rd-radio {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 1.5px solid #d8d8d8;
  margin-top: 2px;
  flex-shrink: 0;
}

.rd-opt.active .rd-radio {
  border-color: #ff6b35;
  background: #ff6b35;
  position: relative;
}

.rd-opt.active .rd-radio::after {
  content: '';
  position: absolute;
  top: 5px;
  left: 5px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #fff;
}

.rd-info {
  flex: 1;
}

.rd-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  display: block;
}

.rd-desc {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
  line-height: 17px;
  display: block;
}

.wb-modal-footer {
  display: flex;
  border-top: 1px solid #f5f5f5;
}

.wb-modal-footer view {
  flex: 1;
  text-align: center;
  padding: 14px 0;
  font-size: 15px;
}

.wb-btn-outline {
  color: #666;
  border-right: 1px solid #f5f5f5;
}

.wb-btn-danger {
  background: linear-gradient(135deg, #ff7743, #ff5c33);
  color: #fff;
  font-weight: 600;
}
</style>
