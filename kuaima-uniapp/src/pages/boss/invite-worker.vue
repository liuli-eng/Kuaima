<template>
  <view class="container">
    <view :style="{ height: `${statusBarHeight}px`, background: '#fff' }" />

    <view class="page-bg">
      <!-- 导航栏 -->
      <view class="nav-bar">
        <view class="nav-back" @click="goBack">
          <image src="/static/icons/worker-credit/chevron-left-dark.svg" mode="aspectFit" />
        </view>
        <text class="nav-title">邀请指定零工接单</text>
        <view class="nav-placeholder"></view>
      </view>

      <view class="top-row">
        <view class="tabs">
          <text class="tab" :class="{ active: currentTab === tab.value }" v-for="tab in tabs" :key="tab.value" @click="switchTab(tab.value)">{{ tab.label }}</text>
        </view>
      </view>

      <view class="search-bar">
        <view class="search-input">
          <image src="/static/icons/boss-location/search-gray.svg" mode="aspectFit" />
          <input
            type="text"
            placeholder="输入姓名搜索零工"
            v-model="searchText"
            confirm-type="search"
            @confirm="onSearch"
          />
        </view>
        <text class="search-btn" @click="onSearch">搜索</text>
        <view class="add-talent" @click="addTalent">
          <image src="/static/icons/boss-invite-worker/user-plus-blue.svg" mode="aspectFit" />
          <text>加人才库</text>
        </view>
      </view>
      <view class="sort-row">
        <view class="sort-item active" @click="sortByOrders">
          <text>合作数</text>
          <image src="/static/icons/boss-invite-worker/sort-orange.svg" mode="aspectFit" />
        </view>
      </view>

      <!-- 列表区域 -->
      <scroll-view scroll-y class="scroll-area">
        <view v-if="loading" class="state-panel">正在加载最近合作记录…</view>
        <view v-else-if="loadError" class="state-panel error-state">
          <text>{{ loadError }}</text>
          <button class="retry-btn" @click="loadWorkers">点击重试</button>
        </view>
        <view v-else-if="filteredWorkers.length === 0" class="state-panel">
          {{ emptyStateText }}
        </view>
        <view
          v-else
          class="list-item"
          :class="{ selected: selectedWorkers.includes(worker.id) }"
          v-for="worker in filteredWorkers"
          :key="worker.id || worker.name"
          @click="toggleSelect(worker)"
        >
          <view class="col-middle">
            <view class="info-row">
              <text class="info-name">{{ worker.name }}</text>
              <text class="badge" v-if="worker.verified">认证</text>
              <text class="added-tag" v-if="worker.inTalent"><image src="/static/icons/boss-invite-worker/check-green.svg" mode="aspectFit" />已加入人才库</text>
            </view>
            <view class="info-row">
              <text class="info-phone">{{ worker.phone }}</text>
              <text class="info-tag" :class="worker.tagClass"><image v-if="worker.tagClass === 'gold'" src="/static/icons/boss-invite-worker/star-gold.svg" mode="aspectFit" />{{ worker.tag }}</text>
              <text class="info-orders">{{ worker.orders }}</text>
            </view>
            <view class="info-row">
              <image class="rating-star" src="/static/icons/boss-invite-worker/star-yellow.svg" mode="aspectFit" />
              <text class="info-phone" style="color: #333; font-weight: 500">{{
                worker.rating
              }}</text>
              <text class="info-phone" style="color: #999"
                >· {{ worker.desc }}</text
              >
            </view>
          </view>
          <view class="col-right">
            <view
              class="checkbox"
              :class="{ checked: selectedWorkers.includes(worker.id) }"
            ><image v-if="selectedWorkers.includes(worker.id)" src="/static/icons/boss-invite-worker/check-white.svg" mode="aspectFit" /></view>
          </view>
        </view>
      </scroll-view>

      <!-- 底部按钮 -->
      <view class="bottom-bar">
        <text class="count-info"
          >已选 <text class="num">{{ selectedWorkers.length }}</text> 人</text
        >
        <button
          class="confirm-btn"
          :disabled="selectedWorkers.length === 0 || inviting"
          @click="confirmSelect"
        >
          {{ inviting ? "处理中…" : "确认邀请" }}{{
            selectedWorkers.length > 0 && !inviting
              ? " " + selectedWorkers.length + " 人"
              : ""
          }}
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import {
  inviteTalent,
  listTalentHistory,
  searchTalents,
} from "@/api/backend";
import { USE_MOCK } from "@/api/http";
import { handleTokenInvalid } from "@/api/auth";

export default {
  data() {
    return {
      searchText: "",
      statusBarHeight: 0,
      currentTab: "recent",
      selectedWorkers: [],
      orderId: "",
      inviting: false,
      loading: false,
      loadError: "",
      queuedTab: "",
      tabs: [
        { label: "最近合作", value: "recent" },
        { label: "最近招工", value: "recruit" },
      ],
      workers: USE_MOCK
        ? [
            {
              avatarClass: "a1",
              initial: "张",
              name: "张师傅",
              verified: true,
              phone: "138****5678",
              tag: "金牌",
              tagClass: "gold",
              orders: "已完成36单",
              rating: 4.9,
              desc: "接活快",
            },
            {
              avatarClass: "a2",
              initial: "李",
              name: "李大姐",
              verified: true,
              phone: "139****1234",
              tag: "电子厂熟手",
              tagClass: "",
              orders: "已完成28单",
              rating: 4.8,
              desc: "守时可靠",
            },
            {
              avatarClass: "a3",
              initial: "王",
              name: "王大哥",
              verified: true,
              phone: "137****9012",
              tag: "普工",
              tagClass: "silver",
              orders: "已完成15单",
              rating: 4.7,
              desc: "干活麻利",
            },
            {
              avatarClass: "a4",
              initial: "陈",
              name: "陈阿姨",
              verified: true,
              phone: "136****3456",
              tag: "金牌",
              tagClass: "gold",
              orders: "已完成42单",
              rating: 5.0,
              desc: "老员工推荐",
            },
            {
              avatarClass: "a5",
              initial: "刘",
              name: "刘师傅",
              verified: true,
              phone: "135****7890",
              tag: "焊锡工",
              tagClass: "",
              orders: "已完成22单",
              rating: 4.6,
              desc: "技术熟练",
            },
            {
              avatarClass: "a6",
              initial: "赵",
              name: "赵小妹",
              verified: true,
              phone: "138****2345",
              tag: "杂工",
              tagClass: "silver",
              orders: "已完成8单",
              rating: 4.5,
              desc: "积极主动",
            },
          ]
        : [],
    };
  },
  computed: {
    emptyStateText() {
      if (this.searchText.trim()) return "暂无匹配零工";
      if (this.currentTab === "recent") return "暂无合作记录";
      if (this.currentTab === "favorite") return "暂无收藏人才";
      return "暂无零工数据";
    },
    filteredWorkers() {
      const keyword = this.searchText.trim().toLowerCase();
      if (!keyword) return this.workers;
      return this.workers.filter((worker) =>
        `${worker.name} ${worker.phone} ${worker.desc}`
          .toLowerCase()
          .includes(keyword),
      );
    },
  },
  onLoad(options) {
    try {
      const info = typeof uni.getWindowInfo === "function" ? uni.getWindowInfo() : uni.getSystemInfoSync();
      this.statusBarHeight = Number(info.statusBarHeight || 0);
    } catch (_) {}
    if (USE_MOCK) {
      this.workers = this.workers.map((worker, index) => ({
        ...worker,
        id: String(worker.id || `mock-worker-${index + 1}`),
      }));
    }
    this.orderId = options?.orderId || "";
    if (!this.orderId) {
      const pendingIds = uni.getStorageSync("pendingInviteWorkerIds");
      this.selectedWorkers = Array.isArray(pendingIds)
        ? pendingIds.map((id) => String(id))
        : [];
    }
    this.loadWorkers();
  },
  methods: {
    goBack() {
      uni.navigateBack();
    },
    async loadWorkers() {
      if (USE_MOCK) return;
      if (this.loading) {
        this.queuedTab = this.currentTab;
        return;
      }
      const tab = this.currentTab;
      this.loading = true;
      this.loadError = "";
      try {
        let result;
        if (tab === "recent") result = await listTalentHistory();
        else
          result = await searchTalents({
            keyword: this.searchText,
            page: 0,
            size: 50,
          });
        const rows = tab === "recent"
          ? (Array.isArray(result) ? result : [])
          : (Array.isArray(result) ? result : result?.records || result?.data || []);
        if (this.currentTab === tab)
          this.workers = rows.map((row) => normalizeWorker(row.worker || row));
      } catch (error) {
        if (this.currentTab === tab) {
          this.workers = [];
          if (error?.statusCode === 401 || error?.code === 401) {
            this.loadError = "登录已失效，请重新登录";
            handleTokenInvalid({ role: "boss", toastTitle: this.loadError });
          } else if (error?.statusCode === 403 || error?.code === 403) {
            this.loadError = "当前账号无权查看最近合作记录";
            uni.showToast({ title: this.loadError, icon: "none" });
          } else {
            this.loadError = "最近合作记录加载失败，请稍后重试";
            uni.showToast({ title: this.loadError, icon: "none" });
          }
        }
      } finally {
        this.loading = false;
        if (this.queuedTab && this.queuedTab !== tab) {
          this.queuedTab = "";
          this.$nextTick(() => this.loadWorkers());
        } else {
          this.queuedTab = "";
        }
      }
    },
    onSearch() {
      if (this.currentTab === "recruit") this.loadWorkers();
    },
    switchTab(tab) {
      this.currentTab = tab;
      this.selectedWorkers = [];
      this.loadWorkers();
    },
    toggleSelect(worker) {
      const workerId = String(worker.id || worker.name);
      const idx = this.selectedWorkers.indexOf(workerId);
      if (idx >= 0) {
        this.selectedWorkers.splice(idx, 1);
      } else {
        this.selectedWorkers.push(workerId);
      }
    },
    sortByOrders() {
      this.workers = [...this.workers].sort((a, b) => Number(b.completedOrders || String(b.orders).match(/\d+/)?.[0] || 0) - Number(a.completedOrders || String(a.orders).match(/\d+/)?.[0] || 0));
    },
    addTalent() {
      if (!this.selectedWorkers.length) return uni.showToast({ title: "请先勾选要加入的零工", icon: "none" });
      this.workers = this.workers.map((worker) => this.selectedWorkers.includes(worker.id) ? { ...worker, inTalent: true } : worker);
      uni.showToast({ title: `已将 ${this.selectedWorkers.length} 位零工加入人才库`, icon: "none" });
    },
    async confirmSelect() {
      if (this.selectedWorkers.length === 0 || this.inviting) return;
      if (!this.orderId) {
        uni.setStorageSync("pendingInviteWorkerIds", this.selectedWorkers);
        uni.$emit("inviteWorkersSelected", this.selectedWorkers);
        uni.showToast({ title: `已选择 ${this.selectedWorkers.length} 人`, icon: "success" });
        setTimeout(() => uni.navigateBack(), 500);
        return;
      }
      this.inviting = true;
      try {
        await Promise.all(
          this.selectedWorkers.map((workerId) =>
            inviteTalent({
              bossId: uni.getStorageSync("userId"),
              workerId,
              orderId: this.orderId,
            }),
          ),
        );
        uni.showToast({
          title: `已邀请 ${this.selectedWorkers.length} 人`,
          icon: "success",
        });
        setTimeout(() => uni.navigateBack(), 800);
      } catch (error) {
        uni.showToast({ title: error.message || "邀请失败", icon: "none" });
      } finally {
        this.inviting = false;
      }
    },
  },
};

function normalizeWorker(worker = {}) {
  const name =
    worker.nickname || worker.name || worker.username || "未命名零工";
  return {
    ...worker,
    id: String(worker.id),
    initial: name.slice(0, 1),
    name,
    phone: worker.phone || "暂无手机号",
    verified: worker.certStatus === "已通过" || worker.certStatus === "通过",
    tag: worker.skills ? String(worker.skills).split(",")[0] : "零工",
    tagClass: "",
    orders: worker.completedOrders ? `已完成${worker.completedOrders}单` : "",
    rating: worker.rating || "暂无",
    desc: worker.remark || "暂无评价",
  };
}
</script>

<style lang="scss" scoped>
.container {
  width: 100%;
  height: 100vh;
  background: #fff8e6;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.page-bg {
  background: #fff;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: white;
  flex-shrink: 0;
}

.nav-back {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #333;
  font-size: 20px;
}
.nav-back image { width: 10px; height: 16px; }

.nav-title {
  font-size: 17px;
  font-weight: 700;
  color: #333;
}

.nav-placeholder {
  width: 32px;
}

.top-row { display: flex; align-items: center; padding: 6px 16px 0; background: #fff; flex-shrink: 0; }
.tabs { display: flex; gap: 22px; }
.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px 8px;
  background: #fff;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 20px;
  padding: 10px 14px;
  gap: 8px;
}
.search-input image { width: 14px; height: 14px; flex-shrink: 0; }

.search-input input {
  flex: 1;
  min-width: 0;
  border: none;
  background: transparent;
  font-size: 14px;
  color: #333;
  outline: none;
}

.tab { padding: 10px 0; font-size: 15px;
  color: #666;
  position: relative;
}

.tab.active {
  color: #1a1a1a;
  font-weight: 600;
}

.tab.active::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 3px;
  background: #ffc400;
  border-radius: 2px;
}

.search-btn { flex-shrink: 0; padding: 8px 16px; border-radius: 20px; background: linear-gradient(135deg, #ffd700, #ffb400); color: #fff; font-size: 14px; font-weight: 500; }
.add-talent { display: flex; align-items: center; gap: 4px; flex-shrink: 0; padding: 8px 10px; border-radius: 8px; background: #eff6ff; color: #3b82f6; font-size: 13px; white-space: nowrap; }
.add-talent image { width: 12px; height: 12px; }
.sort-row { display: flex; justify-content: flex-end; align-items: center; padding: 4px 16px 10px; background: #fff; flex-shrink: 0; }
.sort-item { display: flex; align-items: center; gap: 3px; color: #ff6b35; font-size: 13px; font-weight: 600; }
.sort-item image { width: 10px; height: 10px; }

.scroll-area {
  flex: 1;
  overflow-y: auto;
  background: #f7f8fa;
  padding-top: 4px;
}

.state-panel {
  min-height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 14px;
  color: #999;
  font-size: 14px;
  text-align: center;
}

.error-state {
  color: #d66;
}

.retry-btn {
  margin: 0;
  height: 34px;
  padding: 0 18px;
  border: 1px solid #ff6b35;
  border-radius: 17px;
  background: #fff;
  color: #ff6b35;
  font-size: 13px;
  line-height: 32px;
}

.retry-btn::after {
  border: 0;
}

.list-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 14px 16px;
  background: #fff;
  margin: 0 12px 10px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1.5px solid transparent;
}
.list-item.selected {
  border-color: #ff6b35;
  background: #fffbf7;
}

.col-middle {
  flex: 1 1 auto;
  min-width: 0;
  overflow: hidden;
}

.col-right {
  flex: 0 0 auto;
  margin-left: 8px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.info-row + .info-row {
  margin-top: 4px;
}

.info-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.badge {
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 3px;
  background: #fff3ed;
  color: #ff6b35;
  font-weight: 400;
  flex-shrink: 0;
}

.info-phone {
  font-size: 12px;
  color: #666;
  flex-shrink: 0;
}

.info-tag {
  background: #f0f9ff;
  color: #3b82f6;
  padding: 1px 6px;
  border-radius: 3px;
  font-size: 10px;
  white-space: nowrap;
  flex-shrink: 0;
}
.info-tag image { width: 9px; height: 9px; margin-right: 2px; vertical-align: middle; }
.added-tag { display: inline-flex; align-items: center; gap: 3px; flex-shrink: 0; padding: 1px 5px; border: 1px solid #b7eb8f; border-radius: 3px; background: #f6ffed; color: #52c41a; font-size: 10px; }
.added-tag image { width: 9px; height: 9px; }
.rating-star { width: 12px; height: 12px; flex-shrink: 0; }

.info-tag.gold {
  background: #fffbeb;
  color: #d97706;
}

.info-tag.silver {
  background: #f5f5f5;
  color: #666;
}

.info-orders {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
}

.checkbox {
  width: 22px;
  height: 22px;
  border: 2px solid #ddd;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  flex-shrink: 0;
}

.checkbox.checked {
  border-color: #ff6b35;
  background: #ff6b35;
}

.checkbox image { width: 11px; height: 11px; }

.bottom-bar {
  background: #fff;
  padding: 14px 16px 34px;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.count-info {
  font-size: 14px;
  color: #666;
}

.count-info .num {
  color: #ff6b35;
  font-weight: 700;
  font-size: 18px;
}

.confirm-btn {
  flex: 1;
  height: 46px;
  background: linear-gradient(135deg, #ffd700, #ffa500);
  color: #fff;
  border: none;
  border-radius: 23px;
  font-size: 15px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(255, 165, 0, 0.3);
}

.confirm-btn:disabled {
  opacity: 0.4;
  box-shadow: none;
}
</style>
