<template>
  <view class="container">
    <!-- 导航栏 -->
    <view class="nav-bar" :style="{ paddingTop: `${statusBarHeight}px`, height: `${statusBarHeight + 50}px` }">
      <view class="nav-back" @click="goBack">
        <text>←</text>
      </view>
      <text class="nav-title">邀请二维码</text>
      <view class="nav-right">
        <text class="nav-dots">⋯</text>
      </view>
    </view>

    <scroll-view scroll-y class="body">
      <view v-if="loading" class="page-state">加载中...</view>
      <template v-else>
        <!-- 二维码卡片 -->
        <view class="qr-card">
          <view class="qr-title">企业招聘邀请</view>
          <view class="qr-sub">微信扫码填写信息，提交入企申请</view>
          <view class="qr-code-area">
            <image
              v-if="qrImage"
              :src="qrImage"
              class="qr-image"
              mode="aspectFit"
            />
            <view v-else class="qr-placeholder">
              <text class="qr-placeholder-ico">📱</text>
              <text class="qr-placeholder-text">二维码区域</text>
            </view>
          </view>
          <view class="qr-enterprise">{{ enterpriseName }}</view>
          <view class="qr-code-text">邀请码：{{ inviteCode }}</view>
        </view>

        <!-- 邀请链接 -->
        <view class="link-card">
          <view class="link-label">邀请链接</view>
          <view class="link-value">{{ inviteLink }}</view>
          <view class="link-actions">
            <view class="link-btn" @click="copyLink">📋 复制链接</view>
            <view class="link-btn primary" @click="refreshCode">🔄 刷新邀请码</view>
          </view>
        </view>

        <!-- 使用说明 -->
        <view class="tips-card">
          <view class="tips-title">使用说明</view>
          <view class="tips-item">1. 保存二维码图片或复制链接分享给求职者</view>
          <view class="tips-item">2. 求职者微信扫码后填写姓名、手机号提交申请</view>
          <view class="tips-item">3. 提交后可在「成员 - 申请列表」中审批</view>
        </view>
      </template>
      <view class="bottom-space" />
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="footer">
      <view class="footer-btn outline" @click="saveQr">💾 保存图片</view>
      <view class="footer-btn primary" @click="shareQr">📤 分享邀请链接</view>
    </view>
  </view>
</template>

<script>
import { getInviteQr } from "@/api/enterprise";

export default {
  data() {
    return {
      statusBarHeight: 0,
      loading: false,
      inviteCode: "",
      inviteLink: "",
      enterpriseName: "",
      qrImage: "",
    };
  },
  onLoad() {
    const info = typeof uni.getWindowInfo === "function"
      ? uni.getWindowInfo() : uni.getSystemInfoSync();
    this.statusBarHeight = Number(info.statusBarHeight || 0);
    this.loadQr();
  },
  methods: {
    goBack() {
      uni.navigateBack({
        fail: () => uni.reLaunch({ url: "/pages/boss/members" }),
      });
    },
    async loadQr() {
      this.loading = true;
      try {
        const data = await getInviteQr();
        this.inviteCode = data?.inviteCode || "";
        this.inviteLink = data?.link || "";
        this.enterpriseName = data?.enterpriseName || "我的企业";
        // 后端返回的是 data:image/png;base64,xxx 格式
        this.qrImage = data?.qrImage || "";
      } catch (error) {
        uni.showToast({ title: "获取邀请码失败", icon: "none" });
      } finally {
        this.loading = false;
      }
    },
    refreshCode() {
      this.loadQr();
      uni.showToast({ title: "邀请码已刷新", icon: "success" });
    },
    copyLink() {
      if (!this.inviteLink) return;
      uni.setClipboardData({
        data: this.inviteLink,
        success: () => uni.showToast({ title: "链接已复制", icon: "success" }),
      });
    },
    saveQr() {
      if (!this.qrImage) return;
      if (this.qrImage.startsWith("data:image")) {
        const base64 = this.qrImage.split(",")[1];
        const fs = uni.getFileSystemManager();
        const tempPath = `${wx?.env?.USER_DATA_PATH || "/usrdata"}/qr_invite_${Date.now()}.png`;
        try {
          fs.writeFileSync(tempPath, base64, "base64");
          uni.saveImageToPhotosAlbum({
            filePath: tempPath,
            success: () => uni.showToast({ title: "已保存到相册", icon: "success" }),
            fail: () => uni.showToast({ title: "保存失败，请检查相册权限", icon: "none" }),
          });
        } catch (e) {
          uni.showToast({ title: "保存失败", icon: "none" });
        }
      } else {
        uni.downloadFile({
          url: this.qrImage,
          success: (res) => {
            uni.saveImageToPhotosAlbum({
              filePath: res.tempFilePath,
              success: () => uni.showToast({ title: "已保存到相册", icon: "success" }),
              fail: () => uni.showToast({ title: "保存失败", icon: "none" }),
            });
          },
          fail: () => uni.showToast({ title: "下载失败", icon: "none" }),
        });
      }
    },
    shareQr() {
      uni.setClipboardData({
        data: this.inviteLink || "https://kuaima.com/invite",
        success: () => uni.showToast({ title: "邀请链接已复制", icon: "none" }),
      });
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
.nav-back, .nav-right { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; font-size: 18px; color: #333; }
.nav-dots { font-size: 18px; }
.nav-title { font-size: 17px; font-weight: 600; color: #333; }

.body { flex: 1; min-height: 0; padding: 16px; width: 100%; box-sizing: border-box; }
.page-state { padding: 32px 0; color: #999; font-size: 14px; text-align: center; }

.qr-card {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  border-radius: 20px;
  padding: 28px 20px 24px;
  text-align: center;
  color: #fff;
  margin-bottom: 16px;
}
.qr-title { font-size: 18px; font-weight: 700; }
.qr-sub { font-size: 12px; opacity: 0.9; margin-top: 6px; }
.qr-code-area {
  margin: 18px auto 16px; width: 180px; height: 180px;
  background: #fff; border-radius: 16px; padding: 12px;
  display: flex; align-items: center; justify-content: center;
}
.qr-image { width: 100%; height: 100%; }
.qr-placeholder { display: flex; flex-direction: column; align-items: center; gap: 8px; color: #bbb; }
.qr-placeholder-ico { font-size: 40px; }
.qr-placeholder-text { font-size: 12px; }
.qr-enterprise { font-size: 15px; font-weight: 600; }
.qr-code-text { font-size: 13px; opacity: 0.9; margin-top: 6px; letter-spacing: 1px; }

.link-card {
  background: #fff; border-radius: 16px; padding: 16px; margin-bottom: 16px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
}
.link-label { font-size: 13px; color: #999; margin-bottom: 6px; }
.link-value { font-size: 13px; color: #333; word-break: break-all; margin-bottom: 12px; background: #f7f7f7; padding: 10px; border-radius: 8px; }
.link-actions { display: flex; gap: 10px; }
.link-btn {
  flex: 1; text-align: center; padding: 10px 0; border-radius: 18px;
  font-size: 13px; background: #f3f4f6; color: #666;
}
.link-btn.primary { background: #FFF0E8; color: #FF6B35; }

.tips-card {
  background: #fff; border-radius: 16px; padding: 16px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
}
.tips-title { font-size: 14px; font-weight: 600; color: #333; margin-bottom: 10px; }
.tips-item { font-size: 12px; color: #888; line-height: 22px; }

.bottom-space { height: 20px; }

/* 底部操作栏 */
.footer {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 0.5px solid #eee;
  flex-shrink: 0;
  width: 100%;
  box-sizing: border-box;
}
.footer-btn {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  border-radius: 23px;
  font-size: 14px;
  font-weight: 600;
}
.footer-btn.outline {
  background: #f5f5f5;
  color: #666;
}
.footer-btn.primary {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  color: #fff;
}
</style>
