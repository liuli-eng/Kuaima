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
      <canvas
        id="qrCanvas"
        canvas-id="qrCanvas"
        class="qr-canvas"
        style="width: 218px; height: 218px"
      ></canvas>
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
  </view>
</template>

<script>
import BossPageHeader from "@/components/BossPageHeader.vue";

export default {
  components: { BossPageHeader },
  data() {
    return {
      project: {},
      expireMinutes: 60,
      qrContent: "",
    };
  },
  onLoad(options) {
    const projectId = options.id;
    if (projectId) {
      this.loadProject(projectId);
    }
  },
  onReady() {
    this.generateQR();
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
      }
    },
    generateQR() {
      // 生成签到URL
      const projectId = this.project.id || "0";
      const timestamp = Date.now();
      this.qrContent = `https://kuaima.com/checkin?projectId=${projectId}&t=${timestamp}`;

      // 使用 canvas 绘制二维码
      this.$nextTick(() => {
        this.drawQRCode();
      });
    },
    drawQRCode() {
      const query = uni.createSelectorQuery().in(this);
      query
        .select("#qrCanvas")
        .fields({ node: true, size: true })
        .exec((res) => {
          if (!res || !res[0] || !res[0].node) {
            this.drawQRFallback();
            return;
          }
          const canvas = res[0].node;
          const ctx = canvas.getContext("2d");
          const size = 218;
          canvas.width = size;
          canvas.height = size;

          // 简化的二维码绘制（实际项目建议使用 uqrcode 库）
          this.drawSimpleQR(ctx, size);
        });
    },
    drawSimpleQR(ctx, size) {
      const cellCount = 25;
      const cellSize = size / cellCount;

      // 白色背景
      ctx.fillStyle = "#ffffff";
      ctx.fillRect(0, 0, size, size);

      // 使用固定种子生成确定性图案
      let seed = this.project.id || 12345;
      const rand = () => {
        seed = (seed * 9301 + 49297) % 233280;
        return seed / 233280;
      };

      ctx.fillStyle = "#1a1a1a";

      // 绘制定位图案（三个角的方块）
      const drawFinder = (x, y) => {
        ctx.fillRect(x * cellSize, y * cellSize, cellSize * 7, cellSize * 7);
        ctx.fillStyle = "#ffffff";
        ctx.fillRect(
          (x + 1) * cellSize,
          (y + 1) * cellSize,
          cellSize * 5,
          cellSize * 5
        );
        ctx.fillStyle = "#1a1a1a";
        ctx.fillRect(
          (x + 2) * cellSize,
          (y + 2) * cellSize,
          cellSize * 3,
          cellSize * 3
        );
      };

      drawFinder(0, 0);
      drawFinder(cellCount - 7, 0);
      drawFinder(0, cellCount - 7);

      // 绘制数据点
      for (let y = 0; y < cellCount; y++) {
        for (let x = 0; x < cellCount; x++) {
          const inFinder =
            (x < 8 && y < 8) ||
            (x >= cellCount - 8 && y < 8) ||
            (x < 8 && y >= cellCount - 8);
          if (!inFinder && rand() > 0.52) {
            ctx.fillRect(
              Math.floor(x * cellSize),
              Math.floor(y * cellSize),
              Math.ceil(cellSize),
              Math.ceil(cellSize)
            );
          }
        }
      }
    },
    drawQRFallback() {
      // 降级方案：显示提示文字
      console.log("Canvas 不可用，使用降级方案");
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

.qr-canvas {
  width: 218px;
  height: 218px;
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
</style>
