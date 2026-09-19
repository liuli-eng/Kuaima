<template>
  <view class="container">
    <!-- 导航栏 -->
    <view class="nav-bar" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="nav-back" @click="goBack">
        <text>←</text>
      </view>
      <text class="nav-title">直播橱窗</text>
      <view class="nav-right">
        <text class="nav-dots">⋯</text>
      </view>
    </view>

    <scroll-view scroll-y class="body">
      <!-- 搜索 -->
      <view class="search-bar">
        <text class="search-ico">🔍</text>
        <input class="search-input" v-model="keyword" placeholder="请输入橱窗名称/主播姓名" />
      </view>

      <!-- 提示 -->
      <view class="live-notice">
        <text class="notice-ico">⚠️</text>
        <text class="notice-text">务必尽量挂载贵司的抖音小程序到直播间同步操作，否则抖音官方无法扶持直播间，也无法判断是否是专项诚招工的直播间。</text>
      </view>

      <!-- 直播橱窗列表 -->
      <view v-if="!filteredList.length" class="empty-state">
        <text class="empty-ico">📺</text>
        <text class="empty-text">暂无直播橱窗</text>
      </view>
      <view class="live-card" v-for="item in filteredList" :key="item.id">
        <view class="live-body">
          <view class="live-info-row">
            <text class="li-label">名称</text>
            <text class="li-value bold">{{ item.name }}</text>
          </view>
          <view class="live-info-row">
            <text class="li-label">主播人员</text>
            <text class="li-value">{{ item.anchor }}</text>
          </view>
          <view class="live-info-row">
            <text class="li-label">直播账号</text>
            <text class="li-value"><text class="tiktok-ico">🎵</text>{{ item.account }}</text>
          </view>
          <view class="live-info-row">
            <text class="li-label">创建时间</text>
            <text class="li-value">{{ item.createTime }}</text>
          </view>
        </view>
      </view>
      <view class="bottom-space" />
    </scroll-view>

    <!-- 底部创建按钮 -->
    <view class="footer">
      <view class="footer-btn primary" @click="openCreateModal">＋ 创建直播橱窗</view>
    </view>

    <!-- 创建直播橱窗弹窗 -->
    <view v-if="showCreateModal" class="modal-mask" @click="closeCreateModal">
      <view class="modal-box" @click.stop>
        <view class="modal-header">
          <text class="modal-title">创建直播橱窗</text>
          <text class="modal-close" @click="closeCreateModal">✕</text>
        </view>
        <view class="modal-body">
          <view class="form-item">
            <text class="form-label">名称</text>
            <input class="form-input" v-model="form.name" placeholder="请输入名称（如：XX的橱窗）" />
          </view>
          <view class="form-item">
            <text class="form-label">主播人员</text>
            <input class="form-input" v-model="form.anchor" placeholder="请输入主播人员姓名" />
          </view>
          <view class="form-item" style="margin-bottom:0;">
            <text class="form-label">直播账号</text>
            <view class="select-row" :class="{ picked: form.account }" @click="openAccountSheet">
              <text>{{ form.account || '请选择直播账号' }}</text>
              <text class="select-arrow">›</text>
            </view>
          </view>
        </view>
        <view class="modal-footer">
          <view class="modal-btn outline" @click="closeCreateModal">取消</view>
          <view class="modal-btn primary" @click="confirmCreate">确定</view>
        </view>
      </view>
    </view>

    <!-- 直播账号选择底部抽屉 -->
    <view v-if="showAccountSheet" class="sheet-mask" @click="closeAccountSheet">
      <view class="acc-sheet" @click.stop>
        <view class="sheet-title">
          <text>选择直播账号</text>
          <text class="sheet-close" @click="closeAccountSheet">✕</text>
        </view>
        <view class="acc-item" v-for="acc in accounts" :key="acc.id" @click="pickAccount(acc)">
          <view class="acc-avatar" :style="{ background: acc.avatarBg }">{{ acc.initial }}</view>
          <view class="acc-info">
            <view class="acc-name-line">
              <text class="acc-name">{{ acc.name }}</text>
              <text class="acc-bind" :class="{ none: !acc.authorized }">{{ acc.authorized ? '已授权' : '未授权' }}</text>
            </view>
            <text class="acc-id">{{ acc.desc }}</text>
          </view>
          <text v-if="form.accountName === acc.name" class="acc-check">✓</text>
        </view>
        <view class="acc-item" @click="bindNewAccount">
          <view class="acc-avatar add">＋</view>
          <view class="acc-info">
            <text class="acc-name add-name">绑定新的抖音直播账号</text>
            <text class="acc-id">授权后即可用于直播带岗</text>
          </view>
          <text class="select-arrow">›</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      statusBarHeight: 0,
      keyword: "",
      showCreateModal: false,
      showAccountSheet: false,
      form: { name: "", anchor: "", account: "", accountName: "" },
      list: [
        { id: 1, name: "菜鸟仓库日结招工专场", anchor: "张晴", account: "晴时招工", createTime: "2026-09-15 14:30" },
        { id: 2, name: "肯德基周末兼职专场", anchor: "王晓", account: "晴时兼职", createTime: "2026-09-16 10:00" },
        { id: 3, name: "顺丰分拣夜班急招", anchor: "李播", account: "晴时招工", createTime: "2026-09-10 09:15" },
      ],
      accounts: [
        { id: 1, name: "晴时招工", authorized: true, desc: "抖音号：qingshi_job · 已绑定小程序", initial: "晴", avatarBg: "linear-gradient(135deg,#FF6B35,#FF8C5A)" },
        { id: 2, name: "晴时兼职", authorized: true, desc: "抖音号：qingshi_part · 已绑定小程序", initial: "兼", avatarBg: "linear-gradient(135deg,#FFB84D,#F09A3E)" },
        { id: 3, name: "晴时蓝领", authorized: false, desc: "抖音号：qingshi_work · 点击后需完成授权", initial: "蓝", avatarBg: "linear-gradient(135deg,#8B5CF6,#6D28D9)" },
      ],
    };
  },
  computed: {
    filteredList() {
      const kw = this.keyword.trim();
      if (!kw) return this.list;
      return this.list.filter(
        (item) => item.name.includes(kw) || item.anchor.includes(kw)
      );
    },
  },
  onLoad() {
    const info = typeof uni.getWindowInfo === "function"
      ? uni.getWindowInfo() : uni.getSystemInfoSync();
    this.statusBarHeight = Number(info.statusBarHeight || 0);
  },
  methods: {
    goBack() {
      uni.navigateBack({
        fail: () => uni.reLaunch({ url: "/pages/boss/workbench" }),
      });
    },
    openCreateModal() {
      this.form = { name: "", anchor: "", account: "", accountName: "" };
      this.showCreateModal = true;
    },
    closeCreateModal() {
      this.showCreateModal = false;
      this.closeAccountSheet();
    },
    confirmCreate() {
      if (!this.form.name.trim()) {
        uni.showToast({ title: "请输入橱窗名称", icon: "none" });
        return;
      }
      if (!this.form.anchor.trim()) {
        uni.showToast({ title: "请输入主播人员姓名", icon: "none" });
        return;
      }
      if (!this.form.account) {
        uni.showToast({ title: "请选择直播账号", icon: "none" });
        return;
      }
      uni.showToast({ title: `直播橱窗「${this.form.name.trim()}」创建成功！`, icon: "success" });
      this.closeCreateModal();
    },
    openAccountSheet() {
      this.showAccountSheet = true;
    },
    closeAccountSheet() {
      this.showAccountSheet = false;
    },
    pickAccount(acc) {
      this.form.account = `${acc.name}（抖音号 ${acc.desc.split("：")[1]?.split(" ")[0] || ""}）`;
      this.form.accountName = acc.name;
      this.closeAccountSheet();
    },
    bindNewAccount() {
      this.closeAccountSheet();
      uni.showToast({ title: "原型演示：授权绑定新抖音账号", icon: "none" });
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

.search-bar {
  display: flex; align-items: center; gap: 8px; padding: 10px 14px; margin-bottom: 12px;
  background: #fff; border-radius: 22px;
}
.search-ico { font-size: 13px; color: #bbb; }
.search-input { flex: 1; font-size: 13px; color: #333; }

.live-notice {
  display: flex; gap: 9px; background: #FFF8EC; border: 1px solid #FFE9BD;
  border-radius: 12px; padding: 11px 13px; margin-bottom: 12px;
}
.notice-ico { font-size: 14px; margin-top: 1px; flex-shrink: 0; }
.notice-text { font-size: 11.5px; color: #9A7B3F; line-height: 1.65; }

.empty-state { text-align: center; padding: 60px 40px; }
.empty-ico { font-size: 60px; display: block; margin-bottom: 12px; }
.empty-text { font-size: 13px; color: #999; }

.live-card {
  background: #fff; border-radius: 16px; margin-bottom: 12px; overflow: hidden;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
}
.live-body { padding: 14px 14px 16px; display: flex; flex-direction: column; gap: 8px; }
.live-info-row { display: flex; align-items: center; font-size: 13px; }
.li-label { color: #999; width: 68px; flex-shrink: 0; }
.li-value { color: #333; flex: 1; }
.li-value.bold { font-weight: 600; color: #1a1a1a; font-size: 14px; }
.tiktok-ico { margin-right: 4px; }

.bottom-space { height: 20px; }

.footer {
  display: flex; padding: 12px 16px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  background: #fff; border-top: 0.5px solid #eee;
}
.footer-btn {
  flex: 1; text-align: center; padding: 12px 0; border-radius: 24px;
  font-size: 14px; font-weight: 600;
}
.footer-btn.primary { background: linear-gradient(135deg, #FF6B35, #FF8C5A); color: #fff; }

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
.select-row {
  width: 100%; padding: 13px 16px; border: 1.5px solid #e8e8e8; border-radius: 14px;
  font-size: 15px; background: #fff; color: #B8B8B8; overflow: visible;
  display: flex; align-items: center; justify-content: space-between; box-sizing: border-box;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.select-row text { overflow: visible; text-overflow: clip; }
.select-row.picked { color: #333; }
.select-arrow { color: #C8C8C8; font-size: 14px; }
.modal-footer { display: flex; gap: 12px; padding: 16px; border-top: 0.5px solid #f0f0f0; }
.modal-btn { flex: 1; text-align: center; padding: 11px 0; border-radius: 22px; font-size: 14px; font-weight: 600; }
.modal-btn.outline { background: #fff; color: #333; border: 1px solid #e0e0e0; }
.modal-btn.primary { background: linear-gradient(135deg, #ff6b35, #ff8c5a); color: #fff; }

/* 账号选择抽屉 */
.sheet-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 105; display: flex; align-items: flex-end; }
.acc-sheet {
  left: 0; right: 0; bottom: 0; width: 100%; background: #fff;
  border-radius: 18px 18px 0 0; padding: 10px 0 24px; z-index: 110;
  animation: slideUp 0.25s ease-out; padding-bottom: calc(24px + env(safe-area-inset-bottom));
}
@keyframes slideUp { from { transform: translateY(100%); } to { transform: translateY(0); } }
.sheet-title {
  text-align: center; font-size: 15px; font-weight: 600; color: #333;
  padding: 8px 0 12px; position: relative;
}
.sheet-close { position: absolute; right: 16px; top: 9px; color: #999; font-size: 15px; }
.acc-item {
  display: flex; align-items: center; gap: 11px; padding: 12px 18px;
}
.acc-item:active { background: #F7F7F7; }
.acc-avatar {
  width: 40px; height: 40px; border-radius: 50%; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 16px; font-weight: 600;
}
.acc-avatar.add { background: #F0F0F0; color: #999; }
.acc-info { flex: 1; min-width: 0; }
.acc-name-line { display: flex; align-items: center; gap: 6px; }
.acc-name { font-size: 14px; font-weight: 500; color: #333; }
.acc-name.add-name { color: #FF6B35; }
.acc-id { font-size: 12px; color: #999; margin-top: 2px; display: block; }
.acc-bind { font-size: 11px; color: #10B981; background: #E8F8EF; padding: 2px 7px; border-radius: 6px; }
.acc-bind.none { color: #FF6B35; background: #FFF0E8; }
.acc-check { color: #FF6B35; font-size: 14px; }
</style>
