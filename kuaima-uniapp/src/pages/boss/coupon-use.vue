<template>
  <view class="container">
    <view :style="{ height: `${statusBarHeight}px` }" />
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">←</view>
      <text class="nav-title">使用优惠券</text>
      <view class="nav-placeholder" />
    </view>

    <scroll-view scroll-y class="content">
      <view class="coupon-card">
        <view>
          <view class="coupon-amount"><text class="currency">¥</text>{{ coupon.amount }}</view>
          <text class="coupon-name">{{ coupon.name }}</text>
        </view>
        <view class="coupon-meta">
          <text>{{ coupon.condition }}</text>
          <text>有效期至 {{ coupon.expire }}</text>
        </view>
      </view>

      <view class="section-title">
        <text>选择待结算的岗位订单</text>
        <text class="section-tip">服务费满 {{ minAmount }} 元可用此券</text>
      </view>

      <view class="job-list">
        <view
          v-for="job in jobs"
          :key="job.id"
          class="job-item"
          :class="{ selected: selectedJob && selectedJob.id === job.id, unavailable: !canUse(job) }"
          @click="selectJob(job)"
        >
          <view class="job-icon">▦</view>
          <view class="job-info">
            <text class="job-name">{{ job.name }}</text>
            <text class="job-meta">{{ job.meta }}</text>
          </view>
          <view class="job-fee-wrap">
            <view class="job-fee">
              <view class="fee-block"><text class="fee-label">岗位费用</text><text class="fee-value">¥{{ job.jobFee }}</text></view>
              <view class="fee-block" :class="{ orange: canUse(job) }"><text class="fee-label">服务费</text><text class="fee-value">¥{{ job.serviceFee }}</text></view>
            </view>
            <text class="fee-tip" :class="{ no: !canUse(job) }">{{ canUse(job) ? "可用本券" : `未满${minAmount}元不可用` }}</text>
          </view>
        </view>
      </view>

      <view class="bill-card">
        <view class="bill-row"><text>订单岗位费用</text><text>¥{{ selectedJob ? selectedJob.jobFee : 0 }}</text></view>
        <view class="bill-row"><text>订单服务费</text><text>¥{{ selectedJob ? selectedJob.serviceFee : 0 }}</text></view>
        <view class="bill-row discount"><text>优惠券抵扣</text><text>{{ discount ? `-¥${discount}` : "未达门槛不可用" }}</text></view>
        <view class="bill-row total"><text>还需支付</text><text>¥{{ payAmount }}</text></view>
      </view>
      <view class="content-bottom" />
    </scroll-view>

    <view class="pay-footer">
      <view class="pay-total">
        <text class="pay-label">还需支付</text>
        <text class="pay-value">¥{{ payAmount }}</text>
      </view>
      <button class="pay-btn" :disabled="!selectedJob || !canUse(selectedJob)" @click="useCoupon">立即使用并抵扣</button>
    </view>

  </view>
</template>

<script>
function safeParam(value, fallback) {
  return value === undefined || value === null || value === "" || value === "undefined"
    ? fallback
    : decodeURIComponent(String(value));
}

export default {
  data() {
    return {
      statusBarHeight: 0,
      coupon: { amount: 20, condition: "满100元可用", name: "新人优惠券", expire: "2026-12-31" },
      jobs: [
        { id: 1, name: "电商分拣打包", meta: "3人 · 日结 · 松江区车墩镇", jobFee: 600, serviceFee: 120 },
        { id: 2, name: "餐饮服务员", meta: "2人 · 日结 · 松江区泗泾镇", jobFee: 1200, serviceFee: 320 },
        { id: 3, name: "仓库搬运装卸工", meta: "4人 · 日结 · 松江区余山镇", jobFee: 960, serviceFee: 150 },
        { id: 4, name: "活动现场协助", meta: "2人 · 日结 · 闵行区七宝镇", jobFee: 480, serviceFee: 88 },
      ],
      selectedJob: null,
      couponId: "",
      userCouponId: "",
    };
  },
  computed: {
    minAmount() {
      const match = String(this.coupon.condition).match(/\d+(?:\.\d+)?/);
      return match ? Number(match[0]) : 100;
    },
    discount() {
      return this.selectedJob && this.canUse(this.selectedJob) ? Math.min(Number(this.coupon.amount) || 0, this.selectedJob.serviceFee) : 0;
    },
    payAmount() {
      return Math.max((this.selectedJob?.jobFee || 0) + (this.selectedJob?.serviceFee || 0) - this.discount, 0);
    },
  },
  onLoad(query = {}) {
    try {
      const info = typeof uni.getWindowInfo === "function" ? uni.getWindowInfo() : uni.getSystemInfoSync();
      this.statusBarHeight = Number(info.statusBarHeight || 0);
    } catch (_) {}
    this.coupon = {
      amount: Number(safeParam(query.amount, 20)) || 20,
      condition: safeParam(query.condition, "满100元可用"),
      name: safeParam(query.name, "新人优惠券"),
      expire: safeParam(query.expire, "2026-12-31"),
    };
    this.couponId = safeParam(query.couponId, "");
    this.userCouponId = safeParam(query.userCouponId, "");
    this.selectedJob = this.jobs.find((job) => this.canUse(job)) || null;
  },
  methods: {
    canUse(job) {
      return Number(job?.fee || 0) >= this.minAmount;
    },
    selectJob(job) {
      if (!this.canUse(job)) return;
      this.selectedJob = job;
    },
    goBack() {
      uni.navigateBack();
    },
    useCoupon() {
      if (!this.selectedJob || !this.canUse(this.selectedJob)) return;
      const query = [
        `orderId=${encodeURIComponent(this.selectedJob.id)}`,
        `amount=${encodeURIComponent(this.payAmount)}`,
        "count=1",
        this.userCouponId ? `userCouponId=${encodeURIComponent(this.userCouponId)}` : "",
      ].filter(Boolean).join("&");
      uni.navigateTo({
        url: `/pages/boss/settle-confirm?${query}`,
        fail: () => uni.showToast({ title: "确认付款页面打开失败", icon: "none" }),
      });
    },
  },
};
</script>

<style lang="scss" scoped>
.container { width: 100%; height: 100vh; display: flex; flex-direction: column; overflow: hidden; background: #f5f5f5; }
.nav-bar { height: 50px; display: flex; align-items: center; justify-content: space-between; padding: 0 16px; background: #f5f5f5; }
.nav-back, .nav-placeholder { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; }
.nav-back { color: #333; font-size: 22px; }
.nav-title { color: #333; font-size: 17px; font-weight: 600; }
.content { flex: 1; min-height: 0; padding: 16px; box-sizing: border-box; }
.coupon-card { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; padding: 18px; color: #fff; background: linear-gradient(135deg, #ff7a45, #ff4d2e); border-radius: 16px; box-shadow: 0 8px 20px rgba(255, 77, 46, .25); }
.coupon-amount { font-size: 30px; font-weight: 800; }
.currency { margin-right: 2px; font-size: 14px; }
.coupon-name { display: block; margin-top: 4px; font-size: 14px; font-weight: 600; }
.coupon-meta { display: flex; flex-direction: column; gap: 4px; color: rgba(255,255,255,.9); font-size: 11px; text-align: right; }
.section-title { display: flex; flex-direction: column; gap: 3px; margin: 4px 0 10px; color: #333; font-size: 14px; font-weight: 600; }
.section-tip { color: #999; font-size: 11px; font-weight: 400; }
.job-list { display: flex; flex-direction: column; gap: 10px; }
.job-item { display: flex; align-items: flex-start; gap: 14px; padding: 14px 16px; background: #fff; border: 2px solid transparent; border-radius: 14px; }
.job-item.selected { background: #fff9f5; border-color: #ff6b35; }
.job-item.unavailable { opacity: .55; }
.job-icon { width: 44px; height: 44px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; color: #ff6b35; font-size: 22px; background: #fff3ed; border-radius: 12px; }
.job-info { flex: 1; min-width: 0; }
.job-name, .job-meta, .fee-label, .fee-value, .fee-tip { display: block; }
.job-name { color: #333; font-size: 15px; font-weight: 600; }
.job-meta, .fee-label { margin-top: 4px; color: #999; font-size: 12px; }
.job-fee-wrap { flex-shrink: 0; }
.job-fee { display: flex; gap: 12px; }
.fee-block { min-width: 52px; text-align: center; }
.fee-value { display: block; margin-top: 2px; color: #333; font-size: 15px; font-weight: 700; }
.fee-block.orange .fee-value { color: #ff6b35; }
.fee-tip { display: block; margin-top: 4px; color: #ff6b35; font-size: 10px; text-align: right; }
.fee-tip.no { color: #bbb; }
.bill-card { margin-top: 14px; padding: 14px 16px; background: #fff; border-radius: 14px; }
.bill-row { display: flex; justify-content: space-between; padding: 6px 0; color: #666; font-size: 13px; }
.bill-row.discount { color: #ff6b35; }
.bill-row.total { margin-top: 6px; padding-top: 12px; color: #333; border-top: 1px solid #f0f0f0; font-weight: 600; }
.bill-row.total text:last-child { color: #ff6b35; font-size: 20px; font-weight: 800; }
.content-bottom { height: 100px; }
.pay-footer { display: flex; align-items: center; gap: 12px; padding: 12px 16px 26px; background: #f5f5f5; }
.pay-total { flex: 1; }
.pay-label, .pay-value { display: block; }
.pay-label { color: #999; font-size: 11px; }
.pay-value { color: #ff6b35; font-size: 20px; font-weight: 800; }
.pay-btn { flex: 1.2; height: 46px; margin: 0; padding: 0 12px; color: #fff; font-size: 14px; font-weight: 600; line-height: 46px; background: linear-gradient(135deg, #ff6b35, #ff8c5a); border: 0; border-radius: 23px; }
.pay-btn[disabled] { background: #ffc9a8; }
.pay-btn::after, .success-btn::after { border: 0; }
</style>
