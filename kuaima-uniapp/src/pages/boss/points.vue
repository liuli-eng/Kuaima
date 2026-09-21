<template>
  <view class="container">
    <view :style="{ height: `${statusBarHeight}px` }" />

    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <image class="nav-back-icon" src="/static/icons/boss-points/arrow-left-dark.svg" mode="aspectFit" />
      </view>
      <text class="nav-title">积分购买</text>
      <view class="nav-placeholder" />
    </view>

    <scroll-view scroll-y class="content">
      <!-- 积分卡片 -->
      <view class="points-card">
        <text class="points-label">我的积分</text>
        <text class="points-value">{{ points }}</text>
        <text class="points-desc">积分可用于发布订单、提升曝光</text>
      </view>

      <view class="gift-entry" @click="openGiftSheet">
        <view class="gift-icon"><image src="/static/icons/boss-points/gift-white.svg" mode="aspectFit" /></view>
        <view class="gift-info">
          <text class="gift-title">赠送积分给零工</text>
          <text class="gift-desc">激励优质零工，提升接单积极性</text>
        </view>
        <image class="gift-arrow" src="/static/icons/boss-points/chevron-right-gray.svg" mode="aspectFit" />
      </view>

      <view class="gift-entry detail-entry" @click="openPointsDetail">
        <view class="gift-icon detail-icon">
          <image src="/static/icons/boss-profile/clipboard-list.svg" mode="aspectFit" />
        </view>
        <view class="gift-info">
          <text class="gift-title">积分明细</text>
          <text class="gift-desc">查看积分兑换与购买记录</text>
        </view>
        <image class="gift-arrow" src="/static/icons/boss-points/chevron-right-gray.svg" mode="aspectFit" />
      </view>

      <!-- 选择积分包 -->
      <text class="section-title">选择积分包</text>
      <view class="package-grid">
        <view
          class="package-item"
          :class="{ hot: pkg.hot }"
          v-for="(pkg, index) in packages"
          :key="index"
          @click="buy(pkg)"
        >
          <text class="package-points">{{ pkg.points }}</text>
          <text class="package-unit">积分</text>
          <text class="package-price">¥{{ pkg.price }}</text>
          <text class="package-tag">{{ pkg.tag }}</text>
        </view>
      </view>

      <!-- 积分使用规则 -->
      <view class="rules-card">
        <text class="rules-title">
          <image class="rules-icon" src="/static/icons/boss-points/circle-info-orange.svg" mode="aspectFit" />
          积分使用规则
        </text>
        <text class="rules-item">1. 积分可用于发布日结订单、提升订单曝光</text>
        <text class="rules-item">2. 100积分 = ¥1，积分不可兑换现金</text>
        <text class="rules-item">3. 积分有效期12个月，过期自动清零</text>
        <text class="rules-item">4. 购买后立即到账，可在订单中使用</text>
        <text class="rules-item">5. 赠送积分从我的积分余额中扣除，赠送后不可撤回</text>
      </view>
    </scroll-view>

    <view v-if="giftVisible" class="sheet-mask" @click="closeGiftSheet">
      <view class="gift-sheet" @click.stop>
        <view class="sheet-header">
          <text class="sheet-title">赠送积分</text>
          <view class="sheet-close" @click="closeGiftSheet"><image src="/static/icons/boss-points/xmark-gray.svg" mode="aspectFit" /></view>
        </view>
        <text class="sheet-label">选择零工</text>
        <view class="worker-search">
          <image class="search-icon" src="/static/icons/boss-points/search-gray.svg" mode="aspectFit" />
          <input v-model="workerKeyword" placeholder="搜索零工姓名/手机号" />
        </view>
        <scroll-view scroll-y class="worker-list">
          <view
            v-for="worker in filteredWorkers"
            :key="worker.id"
            class="worker-item"
            :class="{ active: selectedWorkerId === worker.id }"
            @click="selectedWorkerId = worker.id"
          >
            <view class="worker-avatar" :style="{ background: worker.color }">{{ worker.name.slice(0, 1) }}</view>
            <view class="worker-info">
              <text>{{ worker.name }}</text>
              <text>{{ worker.phone }}</text>
            </view>
            <image v-if="selectedWorkerId === worker.id" class="worker-check" src="/static/icons/boss-points/circle-check-orange.svg" mode="aspectFit" />
          </view>
          <view v-if="!filteredWorkers.length" class="worker-empty">未找到匹配的零工</view>
        </scroll-view>
        <text class="sheet-label">赠送数量</text>
        <view class="points-input-wrap">
          <input v-model="giftPoints" type="number" placeholder="最低100积分" />
          <text>积分</text>
        </view>
        <view class="quick-chips">
          <text v-for="value in quickPoints" :key="value" class="quick-chip" :class="{ active: Number(giftPoints) === value }" @click="giftPoints = value">{{ value }}</text>
        </view>
        <text class="gift-calc">{{ giftCalcText }}</text>
        <button class="sheet-confirm" :disabled="submitting" @click="submitGift">
          {{ submitting ? "赠送中..." : "确认赠送" }}
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import {
  createBossPointsPurchase,
  getBossPointsOverview,
  giftBossPoints,
  listBossTalents,
} from "@/api/backend";

function normalizeTalentRows(payload) {
  const body = payload?.data ?? payload ?? {};
  const rows = Array.isArray(body) ? body : body.records || body.content || [];
  return Array.isArray(rows) ? rows : [];
}

function normalizeWorker(item, index) {
  const name = item.nickname || item.name || item.realName || `零工${item.id || ""}`;
  const colors = [
    "linear-gradient(135deg,#F59E0B,#D97706)",
    "linear-gradient(135deg,#06B6D4,#0891B2)",
    "linear-gradient(135deg,#8B5CF6,#6D28D9)",
    "linear-gradient(135deg,#F97316,#EA580C)",
  ];
  return {
    id: String(item.workerId || item.userId || item.id),
    name,
    phone: item.phone || "未提供",
    color: colors[index % colors.length],
  };
}

export default {
  data() {
    return {
      points: 0,
      statusBarHeight: 0,
      giftVisible: false,
      workerKeyword: '',
      selectedWorkerId: '',
      giftPoints: '',
      quickPoints: [100, 500, 1000, 2000],
      workers: [],
      packages: [],
      loading: false,
      submitting: false,
    }
  },
  computed: {
    filteredWorkers() {
      const keyword = String(this.workerKeyword || '').trim();
      if (!keyword) return this.workers;
      return this.workers.filter((worker) => `${worker.name}${worker.phone}`.includes(keyword));
    },
    remainingPoints() {
      return Math.max(0, Number(this.points) - Number(this.giftPoints || 0));
    },
    giftCalcText() {
      const amount = Number(this.giftPoints || 0);
      return `可用积分 ${this.points} · 赠送后剩余 ${this.remainingPoints}${amount > 0 ? ` · 折合 ¥${(amount / 100).toFixed(2)}` : ''}`;
    },
  },
  async onLoad() {
    try {
      const info = typeof uni.getWindowInfo === 'function'
        ? uni.getWindowInfo()
        : uni.getSystemInfoSync();
      this.statusBarHeight = Number(info.statusBarHeight || 0);
    } catch (_) {}
    await Promise.all([this.loadOverview(), this.loadWorkers()]);
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    openPointsDetail() {
      uni.navigateTo({ url: "/pages/boss/points-detail" });
    },
    buy(pkg) {
      uni.showModal({
        title: '购买确认',
        content: `购买 ${pkg.points} 积分\n金额：¥${Number(pkg.price || 0).toFixed(2)}`,
        success: async ({ confirm }) => {
          if (!confirm) return;

          // #ifdef MP-WEIXIN
          await this.purchaseWithWechat(pkg);
          // #endif

          // #ifndef MP-WEIXIN
          uni.showToast({ title: '请在微信小程序内完成支付', icon: 'none' });
          // #endif
        }
      })
    }
    ,
    async purchaseWithWechat(pkg) {
      if (this.submitting) return;

      const packageId = pkg?.id ?? pkg?.packageId;
      if (!packageId) {
        uni.showToast({ title: '积分套餐信息无效', icon: 'none' });
        return;
      }

      this.submitting = true;
      try {
        uni.showLoading({ title: '正在创建支付...', mask: true });
        const order = await createBossPointsPurchase(
          {
            packageId: Number(packageId),
            payChannel: 'WECHAT',
          },
          `boss-points-purchase-${packageId}-${Date.now()}`,
        );
        uni.hideLoading();

        const payParams = order?.payParams || {};
        if (!payParams.package || !payParams.paySign || !payParams.nonceStr || !payParams.timeStamp) {
          throw new Error('微信支付参数无效');
        }

        await new Promise((resolve, reject) => {
          uni.requestPayment({
            provider: 'wxpay',
            timeStamp: String(payParams.timeStamp),
            nonceStr: String(payParams.nonceStr),
            package: String(payParams.package),
            signType: String(payParams.signType || 'RSA'),
            paySign: String(payParams.paySign),
            success: resolve,
            fail: reject,
          });
        });

        uni.showToast({ title: '支付成功', icon: 'success' });
        await this.refreshPointsAfterPayment(Number(pkg.points || 0));
      } catch (error) {
        uni.hideLoading();
        const message = String(error?.errMsg || error?.message || error?.msg || '');
        uni.showToast({
          title: /cancel/i.test(message) ? '支付已取消' : (message || '微信支付失败'),
          icon: 'none',
        });
      } finally {
        this.submitting = false;
      }
    },
    async refreshPointsAfterPayment(purchasedPoints) {
      const expectedPoints = this.points + purchasedPoints;
      for (let attempt = 0; attempt < 5; attempt += 1) {
        await new Promise((resolve) => setTimeout(resolve, attempt ? 500 : 200));
        try {
          const result = await getBossPointsOverview();
          this.points = Number(result?.balance ?? result?.points ?? 0);
          if (this.points >= expectedPoints) return;
        } catch (_) {}
      }
    },
    openGiftSheet() {
      this.giftVisible = true;
      this.workerKeyword = '';
      this.selectedWorkerId = '';
      this.giftPoints = '';
    },
    closeGiftSheet() {
      if (this.submitting) return;
      this.giftVisible = false;
    },
    async loadOverview() {
      this.loading = true;
      try {
        const result = await getBossPointsOverview();
        this.points = Number(result?.balance ?? result?.points ?? 0);
        this.packages = Array.isArray(result?.packages)
          ? result.packages.map((item) => ({
              ...item,
              price: Number(item.price || 0),
            }))
          : [];
      } catch (error) {
        uni.showToast({ title: error?.message || "积分信息加载失败", icon: "none" });
      } finally {
        this.loading = false;
      }
    },
    async loadWorkers() {
      try {
        const result = await listBossTalents({ page: 0, size: 50 });
        this.workers = normalizeTalentRows(result).map(normalizeWorker);
      } catch (error) {
        this.workers = [];
        uni.showToast({ title: error?.message || "零工列表加载失败", icon: "none" });
      }
    },
    async submitGift() {
      if (this.submitting) return;
      const amount = Number(this.giftPoints);
      if (!this.selectedWorkerId) return uni.showToast({ title: '请先选择赠送的零工', icon: 'none' });
      if (!amount || amount < 100 || amount % 100 !== 0) return uni.showToast({ title: '赠送数量需为100的整数倍', icon: 'none' });
      if (amount > this.points) return uni.showToast({ title: '可用积分不足', icon: 'none' });
      this.submitting = true;
      try {
        await giftBossPoints({
          workerId: Number(this.selectedWorkerId),
          points: amount,
        }, `boss-points-gift-${Date.now()}-${this.selectedWorkerId}`);
        uni.showToast({ title: '赠送成功', icon: 'success' });
        this.giftVisible = false;
        this.giftPoints = '';
        this.selectedWorkerId = '';
        await this.loadOverview();
      } catch (error) {
        uni.showToast({ title: error?.message || '积分赠送失败', icon: 'none' });
      } finally {
        this.submitting = false;
      }
    },
  }
}
</script>

<style lang="scss" scoped>
.container {
  width: 100%;
  height: 100vh;
  background: #FFF8E6;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.nav-bar {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #FFF8E6;
}

.nav-back {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
}

.nav-right {
  display: flex;
  align-items: center;
  background: rgba(255,255,255,0.8);
  backdrop-filter: blur(10px);
  border-radius: 9999px;
  padding: 3px 6px;
  gap: 2px;
}

.nav-btn {
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-divider {
  width: 1px;
  height: 10px;
  background: #ddd;
}

.content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.points-card {
  background: linear-gradient(135deg, #FFD700, #FF8C00);
  border-radius: 16px;
  padding: 24px;
  color: white;
  margin-bottom: 20px;
  box-shadow: 0 8px 24px rgba(255, 140, 0, 0.3);
}

.points-label {
  font-size: 13px;
  opacity: 0.9;
  display: block;
}

.points-value {
  font-size: 36px;
  font-weight: 700;
  margin-top: 8px;
  display: block;
}

.points-desc {
  font-size: 12px;
  opacity: 0.85;
  margin-top: 6px;
  display: block;
}

.nav-placeholder {
  width: 32px;
}

.gift-entry { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; padding: 14px 16px; border-radius: 12px; background: #fff; }
.gift-icon { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; border-radius: 10px; background: linear-gradient(135deg, #ffd700, #ff8c00); font-size: 20px; }
.gift-icon image { width: 19px; height: 19px; }
.detail-entry { background: #fff8f0; }
.detail-icon { background: #fff0e8; }
.detail-icon image { width: 18px; height: 20px; }
.gift-info { flex: 1; }
.gift-title { display: block; color: #333; font-size: 14px; font-weight: 600; }
.gift-desc { display: block; margin-top: 2px; color: #999; font-size: 12px; }
.gift-arrow { color: #bbb; font-size: 22px; }
.sheet-mask { position: fixed; inset: 0; z-index: 20; display: flex; align-items: flex-end; background: rgba(0, 0, 0, 0.5); }
.gift-sheet { width: 100%; max-height: 85vh; box-sizing: border-box; padding: 16px 16px calc(22px + env(safe-area-inset-bottom)); border-radius: 20px 20px 0 0; background: #fff; }
.sheet-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.sheet-title { color: #333; font-size: 16px; font-weight: 600; }
.sheet-close { width: 28px; height: 28px; color: #999; font-size: 24px; text-align: center; }
.sheet-label { display: block; margin: 10px 0 8px; color: #666; font-size: 13px; }
.worker-search, .points-input-wrap { display: flex; align-items: center; gap: 8px; padding: 0 12px; border: 1px solid #eee; border-radius: 10px; background: #f7f7f7; }
.worker-search input, .points-input-wrap input { flex: 1; height: 42px; border: 0; background: transparent; font-size: 13px; }
.worker-list { max-height: 150px; margin: 10px 0 16px; border: 1px solid #f0f0f0; border-radius: 10px; }
.worker-item { display: flex; align-items: center; gap: 10px; padding: 10px 12px; border-bottom: 1px solid #f5f5f5; }
.worker-item.active { background: #fff8f0; }
.worker-avatar { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; border-radius: 50%; color: #fff; background: #ff8c00; }
.worker-info { flex: 1; }
.worker-info text { display: block; }
.worker-info text:first-child { color: #333; font-size: 14px; }
.worker-info text:last-child { margin-top: 2px; color: #999; font-size: 11px; }
.worker-check { color: #ff8c00; font-size: 18px; }
.worker-empty { padding: 18px; color: #bbb; font-size: 12px; text-align: center; }
.quick-chips { display: flex; gap: 8px; margin: 10px 0 12px; }
.quick-chip { padding: 6px 14px; border: 1px solid #eee; border-radius: 999px; color: #666; background: #f7f7f7; font-size: 12px; }
.gift-calc { display: block; margin-bottom: 16px; color: #999; font-size: 12px; }
.sheet-confirm { width: 100%; height: 46px; border: 0; border-radius: 23px; color: #fff; background: linear-gradient(135deg, #ffd700, #ff8c00); font-size: 15px; font-weight: 600; }

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  display: block;
}

.package-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.package-item {
  background: white;
  border-radius: 12px;
  padding: 16px;
  text-align: center;
  border: 2px solid transparent;
  position: relative;
}

.package-item.hot::after {
  content: '热门';
  position: absolute;
  top: 8px;
  right: 8px;
  background: #FF6B35;
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 8px;
}

.package-points {
  font-size: 24px;
  font-weight: 700;
  color: #FF8C00;
  display: block;
}

.package-unit {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
  display: block;
}

.package-price {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-top: 10px;
  display: block;
}

.package-tag {
  font-size: 11px;
  color: #666;
  margin-top: 4px;
  display: block;
}

.rules-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  margin-top: 20px;
}

.rules-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.rules-item {
  font-size: 12px;
  color: #666;
  line-height: 1.8;
  display: block;
}
/* 原型视觉校准 */
.container { background: #f5f5f5; }
.nav-bar { background: #f5f5f5; }
.nav-back-icon { width: 18px; height: 18px; }
.content { box-sizing: border-box; }
.gift-entry { transition: transform .15s; }
.gift-entry:active { transform: scale(.98); }
.gift-icon { flex-shrink: 0; font-size: 0; }
.gift-icon image { width: 18px; height: 18px; }
.gift-arrow { width: 8px; height: 13px; flex-shrink: 0; }
.sheet-mask { z-index: 100; }
.sheet-close { display: flex; align-items: center; justify-content: center; font-size: 0; }
.sheet-close image { width: 16px; height: 16px; }
.search-icon { width: 13px; height: 13px; flex-shrink: 0; }
.points-input-wrap input { height: 44px; font-size: 16px; font-weight: 600; }
.worker-item:last-child { border-bottom: 0; }
.worker-check { width: 15px; height: 15px; }
.quick-chip.active { color: #ff8c00; background: #fff8f0; border-color: #ff8c00; }
.package-item { transition: transform .2s; }
.package-item:active { transform: scale(.98); }
.rules-icon { width: 14px; height: 14px; margin-right: 4px; flex-shrink: 0; }
</style>
