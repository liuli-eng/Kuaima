<template>
  <view class="container">
    <!-- 导航栏 -->
    <view class="nav-bar" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="nav-back" @click="goBack">
        <text>←</text>
      </view>
      <text class="nav-title">余额查询</text>
      <view class="nav-right">
        <text class="nav-dots">⋯</text>
      </view>
    </view>

    <scroll-view scroll-y class="body">
      <view v-if="loading" class="page-state">加载中...</view>
      <template v-else>
        <!-- 账户卡片 -->
        <view class="account-card">
          <view class="account-head">
            <text class="account-name">{{ account.accountName || '企业账户' }}</text>
            <text v-if="account.isDefault" class="account-tag">默认账户</text>
          </view>
          <view class="info-row">
            <text class="info-label">主体全称</text>
            <text class="info-value">{{ account.subjectName || '—' }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">商户号</text>
            <text class="info-value">{{ maskMerchantNo(account.merchantNo) }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">账户余额</text>
            <text class="balance-value">{{ showBalance ? `¥${formatYuan(account.balance)}` : '¥ **,***.**' }}</text>
          </view>
        </view>

        <!-- 提示 -->
        <view class="tip-card">
          <text>ℹ️ 余额为商户号可用余额，实际到账以银行流水为准；如需充值对公转账后请联系客服处理。</text>
        </view>
      </template>
      <view class="bottom-space" />
    </scroll-view>

    <!-- 底部充值 -->
    <view class="footer">
      <view class="footer-btn primary" @click="recharge">充值</view>
    </view>
  </view>
</template>

<script>
import { getBossBalance } from "@/api/backend";

function parsePayload(data) {
  return data?.data ?? data ?? {};
}

export default {
  data() {
    return {
      statusBarHeight: 0,
      loading: false,
      showBalance: false,
      account: {
        accountName: "",
        subjectName: "",
        merchantNo: "",
        balance: 0,
        isDefault: true,
      },
    };
  },
  onLoad() {
    const info = typeof uni.getWindowInfo === "function"
      ? uni.getWindowInfo() : uni.getSystemInfoSync();
    this.statusBarHeight = Number(info.statusBarHeight || 0);
    this.loadBalance();
  },
  methods: {
    goBack() {
      uni.navigateBack({
        fail: () => uni.reLaunch({ url: "/pages/boss/workbench" }),
      });
    },
    async loadBalance() {
      this.loading = true;
      try {
        const data = await getBossBalance();
        const body = parsePayload(data);
        this.account = { ...this.account, ...body };
        this.showBalance = true;
      } catch (error) {
        // 查询失败时保持脱敏展示
        this.showBalance = false;
      } finally {
        this.loading = false;
      }
    },
    recharge() {
      uni.showModal({
        title: "充值提示",
        content: "请对公转账后联系客服处理充值，客服电话 400-000-0000。",
        showCancel: false,
        confirmColor: "#FF6B35",
      });
    },
    maskMerchantNo(no) {
      const n = String(no || "");
      if (!n) return "—";
      if (n.length <= 6) return n;
      return `${n.slice(0, 5)}**${n.slice(-3)}`;
    },
    formatYuan(fen) {
      const n = Number(fen || 0);
      return n.toLocaleString("zh-CN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
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

.page-state { padding: 32px 0; color: #999; font-size: 14px; text-align: center; }

.account-card {
  background: #fff; border-radius: 16px; padding: 20px 18px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
}
.account-head { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; }
.account-name { font-size: 16px; font-weight: 700; color: #333; }
.account-tag {
  font-size: 11px; padding: 2px 8px; border-radius: 6px;
  background: #e6f7ff; color: #1890ff; font-weight: 500;
}
.info-row {
  display: flex; justify-content: space-between; font-size: 13px;
  padding: 10px 0; border-bottom: 0.5px solid #F5F5F5;
}
.info-row:last-child { border-bottom: none; }
.info-label { color: #999; }
.info-value { color: #333; font-weight: 500; }
.balance-value { color: #FF6B35; font-weight: 700; font-size: 15px; }

.tip-card {
  margin-top: 14px; background: #FFF7E8; border-radius: 12px;
  padding: 12px 14px; font-size: 12px; color: #A0620D; line-height: 1.7;
}
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
</style>
