<template>
  <view class="sign-wrap">
    <!-- 顶部导航 -->
    <view class="wb-header" style="position: relative; width: 100%">
      <view class="wb-back" @click="goBack"><text class="back-icon">‹</text></view>
      <view class="wb-title" style="color: #4a3500">签到码</view>
      <view class="wb-capsule">
        <view class="cap-btn"><text class="cap-ico">⋯</text></view>
        <view class="cap-divider"></view>
        <view class="cap-btn"><text class="cap-ico">○</text></view>
      </view>
    </view>

    <view class="sign-title">签到码</view>
    <view class="sign-sub">
      <text class="shield-ico">🛡</text>
      <text>安全加密·隐私保护</text>
    </view>

    <!-- 二维码 -->
    <view class="qr-card">
      <image v-if="qrImageUrl" :src="qrImageUrl" class="qr-image" mode="aspectFit"></image>
      <view v-else class="qr-loading">
        <text>生成中...</text>
      </view>
      <view class="qr-mask">
        <text class="fingerprint-ico">👆</text>
      </view>
    </view>

    <view class="qr-tip">
      <text class="wechat-ico">💬</text>
      <text>微信扫一扫 开始签到</text>
    </view>

    <view class="expire-tip">
      <text>签到码有效期：{{ expireMinutes }}分钟</text>
    </view>

    <!-- 刷新按钮 -->
    <view class="refresh-btn" @click="refreshQR">
      <text>刷新签到码</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      project: {},
      expireMinutes: 60,
      qrContent: "",
      qrImageUrl: "",
    };
  },
  onLoad(options) {
    const projectId = options.id;
    if (projectId) {
      this.loadProject(projectId);
    }
  },
  methods: {
    async loadProject(id) {
      const { getProject } = await import("@/api/project");
      try {
        const res = await getProject(id);
        this.project = res.data || {};
        this.expireMinutes = this.project.signCodeExpire || 60;
        this.generateQR();
      } catch (e) {
        console.warn("加载项目失败", e);
        // 即使加载失败也生成二维码
        this.generateQR();
      }
    },
    generateQR() {
      // 生成签到URL - 包含项目ID和用户信息
      const projectId = this.project.id || "0";
      const timestamp = Date.now();
      // 使用微信小程序的URL Scheme或网页链接
      this.qrContent = `https://kuaima.com/checkin?projectId=${projectId}&t=${timestamp}&auto=1`;
      
      // 使用在线QR码生成服务
      this.generateQRImageUrl();
    },
    generateQRImageUrl() {
      // 使用 Google Charts API 生成二维码
      const content = encodeURIComponent(this.qrContent);
      const size = 300;
      this.qrImageUrl = `https://api.qrserver.com/v1/create-qr-code/?size=${size}x${size}&data=${content}`;
    },
    refreshQR() {
      this.generateQR();
      uni.showToast({ title: "签到码已刷新", icon: "success" });
    },
    goBack() {
      uni.navigateBack();
    },
  },
};
</script>

<style lang="scss" scoped>
.sign-wrap {
  position: absolute;
  top: 47px;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, #ffd96f 0%, #ffc53d 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.wb-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  height: 44px;
  flex-shrink: 0;
}

.wb-back {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 50%;
}

.back-icon {
  font-size: 22px;
  color: #4a3500;
  font-weight: 700;
}

.wb-title {
  font-size: 17px;
  font-weight: 600;
}

.wb-capsule {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 17px;
  padding: 0 6px;
  height: 34px;
}

.cap-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cap-ico {
  font-size: 16px;
  color: #4a3500;
}

.cap-divider {
  width: 1px;
  height: 16px;
  background: rgba(74, 53, 0, 0.2);
  margin: 0 2px;
}

.sign-title {
  font-size: 20px;
  font-weight: 700;
  color: #4a3500;
  margin-top: 26px;
}

.sign-sub {
  font-size: 12px;
  color: rgba(74, 53, 0, 0.7);
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.shield-ico {
  font-size: 14px;
}

.qr-card {
  background: #fff;
  border-radius: 20px;
  padding: 22px;
  margin-top: 30px;
  box-shadow: 0 12px 30px rgba(120, 80, 0, 0.25);
  position: relative;
}

.qr-image {
  width: 218px;
  height: 218px;
}

.qr-loading {
  width: 218px;
  height: 218px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 14px;
}

.qr-mask {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 46px;
  height: 46px;
  background: #fff;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ff6b35;
  font-size: 22px;
  box-shadow: 0 0 0 4px #fff;
}

.fingerprint-ico {
  font-size: 24px;
}

.qr-tip {
  font-size: 13px;
  color: #666;
  margin-top: 28px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.wechat-ico {
  font-size: 16px;
}

.expire-tip {
  font-size: 12px;
  color: rgba(74, 53, 0, 0.6);
  margin-top: 16px;
}

.refresh-btn {
  margin-top: 20px;
  padding: 10px 24px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 20px;
  font-size: 13px;
  color: #4a3500;
  border: 1px solid rgba(74, 53, 0, 0.2);
}

.refresh-btn:active {
  background: rgba(255, 255, 255, 0.5);
}
</style>
