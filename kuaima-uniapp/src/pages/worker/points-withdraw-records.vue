<template>
  <view class="page">
    <AppNavBar title="积分提现记录" :show-back="true" />
    <scroll-view scroll-x class="tabs" :show-scrollbar="false">
      <view class="tabs-inner">
        <view v-for="tab in tabs" :key="tab.key" class="tab" :class="{ active: status === tab.key }" @click="switchStatus(tab.key)">{{ tab.label }}</view>
      </view>
    </scroll-view>
    <scroll-view scroll-y class="content" @scrolltolower="loadMore">
      <view class="summary-card"><view class="summary-item"><text class="summary-num">{{ formatPoints(summary.points) }}</text><text class="summary-label">累计提现（积分）</text></view><view class="summary-item"><text class="summary-num"><text class="summary-currency">¥</text>{{ formatMoney(summary.amount) }}</text><text class="summary-label">累计到账金额</text></view><view class="summary-item"><text class="summary-num">{{ formatPoints(summary.processing) }}</text><text class="summary-label">处理中（积分）</text></view></view>
      <view v-if="loading && !records.length" class="state">加载中...</view>
      <view v-else-if="error && !records.length" class="state error" @click="loadRecords(true)">{{ error }}，点击重试</view>
      <view v-else-if="!records.length" class="state">暂无积分提现记录</view>
      <view v-for="item in records" v-else :key="item.id" class="record-card" @click="openDetail(item)">
        <view class="record-top"><view class="channel-icon" :class="String(item.channel || '').toLowerCase()">{{ item.channel === 'ALIPAY' ? '支' : '微' }}</view><view class="record-main"><text class="record-title">提现到{{ channelText(item.channel) }}</text><text class="record-no">{{ item.withdrawNo || '--' }} · {{ formatDate(item.appliedAt) }}</text></view><view class="record-right"><text class="points">-{{ formatPoints(item.points) }}</text><text class="status" :class="statusClass(item.status)">{{ statusText(item.status) }}</text></view></view>
        <view v-if="item.status === 'PENDING' || item.status === 'PROCESSING'" class="progress">● 平台审核中，预计到账时间以审核结果为准</view>
        <view v-if="item.failureReason" class="failure">失败原因：{{ item.failureReason }}</view>
        <view class="record-foot"><text>{{ item.status === 'SUCCESS' ? '到账金额' : '预计到账' }} <text class="money">¥{{ formatMoney(item.amount) }}</text></text><text>{{ item.paidAt ? `${formatDate(item.paidAt)} 到账` : '免手续费' }}</text></view>
      </view>
      <view v-if="loading && records.length" class="load-more">加载中...</view>
      <view v-else-if="hasMore && records.length" class="load-more" @click="loadMore">加载更多</view>
      <view class="bottom-space" />
    </scroll-view>

    <view v-if="detailVisible" class="mask" @click="detailVisible = false"><view class="detail-sheet" @click.stop><view class="handle" /><text class="detail-title">提现详情</text><view v-if="detailLoading" class="state">加载中...</view><template v-else><view class="detail-row"><text>提现单号</text><text>{{ detail.withdrawNo || '--' }}</text></view><view class="detail-row"><text>提现积分</text><text>{{ formatPoints(detail.points) }} 积分</text></view><view class="detail-row"><text>到账金额</text><text>¥{{ formatMoney(detail.amount) }}</text></view><view class="detail-row"><text>提现渠道</text><text>{{ channelText(detail.channel) }}</text></view><view class="detail-row"><text>状态</text><text>{{ statusText(detail.status) }}</text></view><view class="detail-row"><text>申请时间</text><text>{{ formatDate(detail.appliedAt) }}</text></view><view class="detail-row"><text>到账时间</text><text>{{ formatDate(detail.paidAt) }}</text></view><view v-if="detail.failureReason" class="detail-row"><text>失败原因</text><text class="reason">{{ detail.failureReason }}</text></view></template><button class="close-btn" @click="detailVisible = false">关闭</button></view></view>
  </view>
</template>

<script setup>
import { ref } from "vue";
import { onLoad, onUnload } from "@dcloudio/uni-app";
import AppNavBar from "@/components/AppNavBar.vue";
import { getWorkerPointsWithdrawalDetail, listWorkerPointsWithdrawals } from "@/api/backend";

const tabs = [{ key: "ALL", label: "全部" }, { key: "PROCESSING", label: "处理中" }, { key: "SUCCESS", label: "已到账" }, { key: "FAILED", label: "已驳回" }];
const status = ref("ALL"), records = ref([]), page = ref(0), total = ref(0), loading = ref(false), error = ref("");
const detailVisible = ref(false), detailLoading = ref(false), detail = ref({});
const summary = ref({ points: 0, amount: 0, processing: 0 });
let active = true, requestVersion = 0;
const size = 20;
const hasMore = ref(false);

onLoad(() => loadRecords(true));
onUnload(() => { active = false; requestVersion += 1; });

async function loadRecords(reset = false) {
  if (loading.value) return;
  if (reset) { page.value = 0; records.value = []; total.value = 0; hasMore.value = false; }
  loading.value = true; error.value = "";
  const version = ++requestVersion;
  try {
    const data = await listWorkerPointsWithdrawals({ page: page.value, size, status: status.value });
    if (!active || version !== requestVersion) return;
    const rows = Array.isArray(data?.records) ? data.records : Array.isArray(data?.content) ? data.content : Array.isArray(data) ? data : [];
    records.value = reset ? rows : records.value.concat(rows);
    if (reset) updateSummary(rows, data);
    total.value = Number(data?.total ?? data?.totalElements ?? records.value.length);
    hasMore.value = records.value.length < total.value || data?.hasMore === true;
  } catch (err) {
    if (active && version === requestVersion) {
      if (!reset && page.value > 0) page.value -= 1;
      error.value = err?.message || "记录加载失败";
    }
  } finally {
    if (active && version === requestVersion) loading.value = false;
  }
}
function switchStatus(value) { if (status.value === value) return; status.value = value; requestVersion += 1; loading.value = false; loadRecords(true); }
function loadMore() { if (!hasMore.value || loading.value) return; page.value += 1; loadRecords(false); }
async function openDetail(item) { detailVisible.value = true; detailLoading.value = true; detail.value = item; try { detail.value = await getWorkerPointsWithdrawalDetail(item.id); } catch (err) { uni.showToast({ title: err?.message || "详情加载失败", icon: "none" }); } finally { detailLoading.value = false; } }
function updateSummary(rows, data) {
  const all = Array.isArray(data?.summary?.records) ? data.summary.records : rows;
  summary.value = {
    points: Number(data?.summary?.points ?? all.filter((item) => ["SUCCESS", "PENDING", "PROCESSING"].includes(item.status)).reduce((sum, item) => sum + Number(item.points || 0), 0)),
    amount: Number(data?.summary?.amount ?? all.filter((item) => item.status === "SUCCESS").reduce((sum, item) => sum + Number(item.amount || 0), 0)),
    processing: Number(data?.summary?.processing ?? all.filter((item) => ["PENDING", "PROCESSING"].includes(item.status)).reduce((sum, item) => sum + Number(item.points || 0), 0)),
  };
}
function statusText(value) { return { PENDING: "处理中", PROCESSING: "处理中", SUCCESS: "已到账", FAILED: "已驳回", CANCELED: "已取消" }[value] || value || "--"; }
function statusClass(value) { return String(value || "").toLowerCase(); }
function channelText(value) { return { WECHAT: "微信零钱", ALIPAY: "支付宝" }[value] || value || "--"; }
function formatPoints(value) { return Number(value || 0).toLocaleString("zh-CN"); }
function formatMoney(value) { const number = Number(value); return Number.isFinite(number) ? number.toFixed(2) : "0.00"; }
function formatDate(value) { return value ? String(value).replace("T", " ").slice(0, 16) : "--"; }
</script>

<style scoped>
.page{display:flex;flex-direction:column;height:100vh;overflow:hidden;background:#f7f7f7}.tabs{flex-shrink:0;width:100%;background:#fff;white-space:nowrap}.tabs-inner{display:inline-flex;padding:0 20rpx}.tab{position:relative;padding:24rpx 22rpx;color:#777;font-size:25rpx}.tab.active{color:#ff6b35;font-weight:600}.tab.active::after{position:absolute;right:24rpx;bottom:0;left:24rpx;height:5rpx;border-radius:4rpx;background:#ff6b35;content:""}.content{flex:1;min-height:0;padding:24rpx;box-sizing:border-box}.record-card{margin-bottom:20rpx;padding:26rpx 28rpx;border-radius:22rpx;background:#fff;box-shadow:0 5rpx 18rpx rgba(80,60,40,.04)}.record-head,.record-body,.record-foot,.detail-row{display:flex;align-items:center;justify-content:space-between}.record-no{color:#777;font-size:22rpx}.status{padding:5rpx 14rpx;border-radius:18rpx;font-size:21rpx}.pending{color:#f59e0b;background:#fff7e6}.processing{color:#1677ff;background:#e6f4ff}.success{color:#10b981;background:#ecfdf5}.failed{color:#e64340;background:#fff1f0}.canceled{color:#888;background:#f2f2f2}.record-body{padding:24rpx 0 20rpx}.points,.channel{display:block}.points{color:#333;font-size:31rpx;font-weight:700}.channel{margin-top:7rpx;color:#999;font-size:22rpx}.money{color:#ff6b35;font-size:36rpx;font-weight:700}.record-foot{padding-top:18rpx;border-top:1rpx solid #f5f5f5;color:#aaa;font-size:21rpx}.failure{display:block;margin-top:14rpx;color:#e64340;font-size:22rpx}.state,.load-more{padding:100rpx 20rpx;color:#999;font-size:26rpx;text-align:center}.state.error{color:#ff6b35}.load-more{padding:26rpx}.bottom-space{height:30rpx}.mask{position:fixed;z-index:20;inset:0;display:flex;align-items:flex-end;background:rgba(0,0,0,.45)}.detail-sheet{width:100%;padding:20rpx 36rpx calc(36rpx + env(safe-area-inset-bottom));border-radius:30rpx 30rpx 0 0;background:#fff;box-sizing:border-box}.handle{width:70rpx;height:8rpx;margin:0 auto 20rpx;border-radius:8rpx;background:#ddd}.detail-title{display:block;margin-bottom:20rpx;color:#333;font-size:32rpx;font-weight:700;text-align:center}.detail-row{padding:18rpx 0;border-bottom:1rpx solid #f6f6f6;color:#777;font-size:25rpx}.detail-row text:last-child{max-width:65%;color:#333;text-align:right}.detail-row .reason{color:#e64340}.close-btn{height:84rpx;margin:28rpx 0 0;border:0;border-radius:42rpx;color:#fff;background:linear-gradient(135deg,#ff6b35,#ff8c5a);font-size:28rpx;line-height:84rpx}.close-btn::after{border:0}
.summary-card{display:flex;margin-bottom:24rpx;padding:32rpx 20rpx;border-radius:24rpx;color:#fff;background:linear-gradient(135deg,#ff8c5a,#ff6b35);box-shadow:0 8rpx 24rpx rgba(255,107,53,.2)}.summary-item{flex:1;text-align:center}.summary-item+.summary-item{border-left:1rpx solid rgba(255,255,255,.25)}.summary-num{display:block;font-size:34rpx;font-weight:700}.summary-currency{display:inline;font-size:22rpx}.summary-label{display:block;margin-top:8rpx;color:rgba(255,255,255,.88);font-size:21rpx}.record-top{display:flex;align-items:center;gap:18rpx}.channel-icon{display:flex;align-items:center;justify-content:center;width:72rpx;height:72rpx;border-radius:18rpx;color:#fff;font-size:30rpx;font-weight:700}.channel-icon.wechat{background:#07c160}.channel-icon.alipay{background:#1677ff}.record-main{flex:1;min-width:0}.record-title{display:block;color:#333;font-size:27rpx;font-weight:600}.record-no{display:block;margin-top:6rpx;overflow:hidden;color:#bbb;font-size:21rpx;text-overflow:ellipsis;white-space:nowrap}.record-right{flex-shrink:0;text-align:right}.record-right .points{font-size:28rpx}.record-right .status{display:block;margin-top:6rpx}.progress{margin-top:18rpx;color:#ff6b35;font-size:22rpx}.record-foot{margin-top:18rpx}.record-foot .money{font-size:25rpx;color:#333}.record-foot text:last-child{color:#aaa;font-size:21rpx}
</style>
