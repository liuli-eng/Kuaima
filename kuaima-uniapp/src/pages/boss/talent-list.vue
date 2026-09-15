<template>
  <view class="container">
    <!-- 导航栏 -->
    <view class="nav-bar" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="nav-back" @click="goBack">
        <text>←</text>
      </view>
      <text class="nav-title">人才库</text>
      <view class="nav-right">
        <text>+</text>
      </view>
    </view>

    <!-- 搜索框 -->
    <view class="search-bar">
      <view class="search-input">
        <text style="color:#999;margin-right:8px;">🔍</text>
        <input
          v-model="searchText"
          type="text"
          placeholder="搜索零工姓名/工种"
          confirm-type="search"
          @confirm="search"
          @input="handleSearchInput"
        />
      </view>
    </view>

    <!-- 筛选标签 -->
    <view class="filter-tabs">
      <text 
        class="filter-tab" 
        :class="{ active: currentTab === tab.value }"
        v-for="tab in tabs" 
        :key="tab.value"
        @click="switchFilter(tab)"
      >{{ tab.label }}</text>
    </view>

    <scroll-view
      scroll-y
      class="scroll-area"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="refresh"
      @scrolltolower="loadMore"
    >
      <view v-if="loading && !workers.length" class="page-state">人才加载中...</view>
      <view v-else-if="!workers.length" class="page-state">暂无人才信息</view>
      <view class="worker-card" v-for="worker in workers" :key="worker.id">
        <image v-if="worker.avatar" class="worker-avatar avatar-image" :src="worker.avatar" mode="aspectFill" />
        <view v-else class="worker-avatar" :style="{ background: worker.avatarBg }">
          {{ worker.initial }}
        </view>
        <view class="worker-info">
          <view class="worker-name">
            {{ worker.name }}
            <text class="worker-tag">{{ worker.tag }}</text>
          </view>
          <text class="worker-meta">{{ worker.meta }}</text>
          <view class="worker-skills">
            <text class="skill-tag" v-for="(skill, i) in worker.skills" :key="i">{{ skill }}</text>
          </view>
        </view>
        <view class="worker-action">
          <button class="btn-sm btn-outline" :disabled="worker.operating" @click="collect(worker)">{{ worker.isFavorite ? "已收藏" : "收藏" }}</button>
          <button class="btn-sm btn-primary" :disabled="worker.inviting" @click="hire(worker)">{{ worker.inviting ? "邀请中" : "雇佣" }}</button>
        </view>
      </view>
      <view v-if="loadingMore" class="list-state">加载中...</view>
      <view v-else-if="workers.length && !hasMore" class="list-state">没有更多了</view>
    </scroll-view>
  </view>
</template>

<script>
import {
  inviteBossTalent,
  listBossTalents,
  toggleBossTalentFavorite,
} from "@/api/backend";
import { handleTokenInvalid } from "@/api/auth";

function normalizeResponse(payload, requestedPage) {
  const body = payload?.data ?? payload ?? {};
  const records = Array.isArray(body)
    ? body
    : body.records || body.content || [];
  const totalValue = body.total ?? payload?.total;
  const total = Number(totalValue);
  const page = Number(body.page ?? payload?.page ?? requestedPage);
  return {
    records: Array.isArray(records) ? records : [],
    total: Number.isFinite(total) ? total : null,
    page: Number.isFinite(page) ? page : requestedPage,
  };
}

export default {
  data() {
    return {
      statusBarHeight: 0,
      orderId: null,
      searchText: "",
      searchTimer: null,
      currentTab: "all",
      tabs: [
        { label: "全部", value: "all" },
        { label: "熟练工", value: "skilled", level: "熟练工" },
        { label: "新零工", value: "new", level: "新零工" },
        { label: "我收藏的", value: "favorite", favoriteOnly: true },
      ],
      workers: [],
      page: 0,
      size: 20,
      total: null,
      hasMore: true,
      loading: false,
      loadingMore: false,
      refreshing: false,
      authRedirecting: false,
    };
  },
  onLoad(options = {}) {
    const info = typeof uni.getWindowInfo === "function"
      ? uni.getWindowInfo()
      : uni.getSystemInfoSync();
    this.statusBarHeight = Number(info.statusBarHeight || 0);
    const orderId = Number(options.orderId);
    this.orderId = Number.isSafeInteger(orderId) && orderId > 0 ? orderId : null;
    this.loadWorkers(true);
  },
  onUnload() {
    if (this.searchTimer) clearTimeout(this.searchTimer);
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    getFilterParams() {
      const tab = this.tabs.find((item) => item.value === this.currentTab) || {};
      return {
        keyword: this.searchText.trim() || undefined,
        level: tab.level,
        favoriteOnly: tab.favoriteOnly || undefined,
      };
    },
    normalizeWorker(item, index) {
      const name = item.nickname || item.name || item.realName || `零工${item.id || ""}`;
      const tags = Array.isArray(item.tags)
        ? item.tags
        : String(item.tags || "").split(/[,，]/).filter(Boolean);
      const rating = item.rating ?? item.goodRate;
      const meta = [
        item.experience,
        item.category || item.jobCategory,
        rating !== undefined && rating !== null && rating !== "" ? `好评率${rating}${String(rating).includes("%") ? "" : "%"}` : "",
      ].filter(Boolean).join(" · ");
      const colors = [
        "linear-gradient(135deg, #FF6B35, #FF8C5A)",
        "linear-gradient(135deg, #52C41A, #73D13D)",
        "linear-gradient(135deg, #1890FF, #40A9FF)",
      ];
      return {
        ...item,
        id: item.workerId || item.userId || item.id,
        name,
        initial: name.slice(0, 1),
        tag: item.level || "零工",
        meta: meta || "暂无工作信息",
        skills: tags,
        avatarBg: colors[index % colors.length],
        isFavorite: item.isFavorite === true || item.favorite === true,
        operating: false,
        inviting: false,
      };
    },
    async loadWorkers(reset = false) {
      if ((this.loading || this.loadingMore) && !reset) return;
      const targetPage = reset ? 0 : this.page;
      if (reset) this.loading = true;
      else this.loadingMore = true;
      try {
        const payload = await listBossTalents({
          page: targetPage,
          size: this.size,
          ...this.getFilterParams(),
        });
        const result = normalizeResponse(payload, targetPage);
        const rows = result.records.map((item, index) => this.normalizeWorker(item, index));
        this.workers = reset ? rows : [...this.workers, ...rows];
        this.total = result.total;
        this.page = result.page + 1;
        this.hasMore = result.total !== null
          ? this.workers.length < result.total
          : rows.length === this.size;
      } catch (error) {
        if (reset) this.workers = [];
        this.handleRequestError(error, "人才加载失败");
      } finally {
        this.loading = false;
        this.loadingMore = false;
        this.refreshing = false;
      }
    },
    search() {
      this.loadWorkers(true);
    },
    handleSearchInput() {
      if (this.searchTimer) clearTimeout(this.searchTimer);
      this.searchTimer = setTimeout(() => this.loadWorkers(true), 350);
    },
    switchFilter(tab) {
      if (this.currentTab === tab.value) return;
      this.currentTab = tab.value;
      this.loadWorkers(true);
    },
    refresh() {
      this.refreshing = true;
      this.loadWorkers(true);
    },
    loadMore() {
      if (this.hasMore) this.loadWorkers(false);
    },
    async collect(worker) {
      if (!worker?.id || worker.operating) return;
      worker.operating = true;
      const target = !worker.isFavorite;
      try {
        const result = await toggleBossTalentFavorite(worker.id, target);
        worker.isFavorite = result?.favorite ?? result?.isFavorite ?? target;
        uni.showToast({ title: worker.isFavorite ? "收藏成功" : "已取消收藏", icon: "success" });
        if (this.currentTab === "favorite" && !worker.isFavorite) {
          this.workers = this.workers.filter((item) => item.id !== worker.id);
        }
      } catch (error) {
        this.handleRequestError(error, "收藏操作失败");
      } finally {
        worker.operating = false;
      }
    },
    async hire(worker) {
      if (!worker?.id || worker.inviting) return;
      worker.inviting = true;
      try {
        await inviteBossTalent(worker.id, this.orderId ? { orderId: this.orderId } : {});
        uni.showToast({ title: "邀请已发送", icon: "success" });
      } catch (error) {
        this.handleRequestError(error, "邀请发送失败");
      } finally {
        worker.inviting = false;
      }
    },
    handleRequestError(error, fallback) {
      const status = Number(error?.code || error?.statusCode);
      if (status === 401) {
        if (!this.authRedirecting) {
          this.authRedirecting = true;
          handleTokenInvalid({ role: "boss" }).finally(() => {
            this.authRedirecting = false;
          });
        }
        return;
      }
      const title = status === 403
        ? "无权操作"
        : status === 404
          ? "人才不存在"
          : error?.message || fallback;
      uni.showToast({ title, icon: "none" });
    },
  },
};
</script>

<style lang="scss" scoped>
.container {
  width: 100%;
  height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.status-bar {
  height: 47px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 28px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  background: #fff;
}

.status-icons {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-bar {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #fff;
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
  gap: 14px;
  color: #333;
}

.search-bar {
  padding: 10px 16px;
  background: #fff;
}

.search-input {
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 20px;
  padding: 8px 14px;
}

.search-input input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
}

.filter-tabs {
  display: flex;
  gap: 8px;
  padding: 0 16px 12px;
  background: #fff;
}

.filter-tab {
  padding: 6px 14px;
  font-size: 13px;
  color: #666;
  background: #f5f5f5;
  border-radius: 16px;
}

.filter-tab.active {
  background: #FFF3ED;
  color: #FF6B35;
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
}

.page-state,
.list-state {
  padding: 32px 16px;
  color: #999;
  font-size: 14px;
  text-align: center;
}

.worker-card {
  background: #fff;
  margin: 8px 16px;
  border-radius: 12px;
  padding: 14px;
  display: flex;
  gap: 12px;
}

.worker-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  flex-shrink: 0;
}

.avatar-image {
  display: block;
}

.worker-info {
  flex: 1;
}

.worker-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
  gap: 6px;
}

.worker-tag {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  background: #FFF3ED;
  color: #FF6B35;
}

.worker-meta {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  display: block;
}

.worker-skills {
  display: flex;
  gap: 6px;
  margin-top: 6px;
  flex-wrap: wrap;
}

.skill-tag {
  font-size: 11px;
  padding: 2px 8px;
  background: #f5f5f5;
  color: #666;
  border-radius: 4px;
}

.worker-action {
  display: flex;
  flex-direction: column;
  gap: 6px;
  justify-content: center;
}

.btn-sm {
  padding: 6px 12px;
  border-radius: 14px;
  font-size: 12px;
  font-weight: 500;
  border: none;
}

.btn-primary {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  color: #fff;
}

.btn-outline {
  background: #fff;
  color: #FF6B35;
  border: 1px solid #FF6B35;
}
</style>
