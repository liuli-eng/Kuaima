<template>
  <view class="container">
    <view :style="{ height: `${statusBarHeight}px` }" />
    <view class="page-bg">
      <!-- 导航栏 -->
      <view class="nav-bar">
        <view class="nav-back" @click="closePage">
          <image src="/static/icons/worker-job-detail/chevron-left-gray.svg" mode="aspectFit" />
        </view>
        <text class="nav-title">设置干活地点</text>
        <view class="nav-placeholder"></view>
      </view>

      <!-- 地图区域 -->
      <view class="map-container">
        <view class="map-bg"></view>
        <view class="map-road map-road-h"></view>
        <view class="map-road map-road-h2"></view>
        <view class="map-road map-road-v"></view>
        <view class="map-road map-road-v2"></view>
        <view class="map-building b1"></view>
        <view class="map-building b2"></view>
        <view class="map-building b3"></view>
        <view class="map-building b4"></view>
        <view class="map-building b5"></view>
        <view class="map-building b6"></view>

        <!-- 搜索栏 -->
        <view class="search-bar">
          <image class="ui-icon" src="/static/icons/boss-location/search-gray.svg" mode="aspectFit" />
          <input type="text" v-model="keyword" placeholder="搜索地址或地点" confirm-type="search" @confirm="searchAddress" />
        </view>

        <!-- 定位按钮 -->
        <view class="locate-btn" @click="locateMe">
          <image class="ui-icon" src="/static/icons/boss-location/crosshairs-orange.svg" mode="aspectFit" />
        </view>

        <!-- 地图标记 -->
        <view class="map-pin">
          <view class="pin-pulse"></view>
          <view class="pin-outer">
            <image class="ui-icon pin-icon" src="/static/icons/boss-location/map-marker-white.svg" mode="aspectFit" />
          </view>
        </view>

        <!-- 地图控制 -->
        <view class="map-controls">
          <view class="map-ctrl-btn" @click="zoomIn">
            <text>+</text>
          </view>
          <view class="map-ctrl-btn" @click="zoomOut">
            <text>−</text>
          </view>
        </view>
      </view>

      <!-- 滚动区域 -->
      <scroll-view
        scroll-y
        class="scroll-area"
        style="flex: 1; overflow-y: auto; min-height: 0"
      >
        <!-- 位置信息 -->
        <view class="location-info">
          <view class="location-title-row">
            <text class="location-title">{{ selectedAddress.name }}</text>
            <text class="location-tag">已选地点</text>
            <text class="location-distance">距离您 {{ distance }}km</text>
          </view>
          <text class="location-address">{{ selectedAddress.address }}</text>
        </view>

        <!-- 常用地点 -->
        <view class="section-header">
          <text class="section-title">常用地点</text>
          <text class="section-action" @click="manageAddress">管理</text>
        </view>

        <!-- 地址列表 -->
        <view class="address-list">
          <view
            class="address-item"
            :class="{ selected: selectedIndex === index }"
            v-for="(item, index) in addresses"
            :key="item.id || index"
            @click="selectAddress(index)"
          >
            <view class="address-icon">
              <text
                class="iconfont"
                :class="item.icon"
                style="font-size: 16px"
              ></text>
            </view>
            <view class="address-info">
              <view class="address-name">
                {{ item.name }}
                <text class="addr-tag hot" v-if="item.tag === '最近'">{{
                  item.tag
                }}</text>
              </view>
              <text class="address-detail">{{ item.address }}</text>
              <view class="address-tags" style="margin-top: 4px">
                <text class="addr-tag" v-for="(t, i) in item.tags" :key="i">{{
                  t
                }}</text>
              </view>
            </view>
            <view v-if="selectedIndex === index" class="address-actions">
              <text @click.stop="editAddress(index)">编辑</text>
              <text class="danger" @click.stop="removeAddress(index)">删除</text>
            </view>
          </view>
        </view>

        <!-- 新增地址 -->
        <view class="add-address-btn" @click="addNewAddress">
          <text style="font-size: 14px">+</text>
          <text>新增工作地点</text>
        </view>
      </scroll-view>

      <!-- 底部按钮 -->
      <view class="bottom-bar">
        <button class="submit-btn" @click="saveLocation">确定使用此地点</button>
      </view>
    </view>

    <view v-if="sheetVisible" class="sheet-mask" @click="closeAddressSheet" />
    <view v-if="sheetVisible" class="address-sheet">
      <view class="sheet-grip" />
      <view class="sheet-header">
        <text class="sheet-title">{{ editingIndex === -1 ? '新增工作地点' : '编辑工作地点' }}</text>
        <button class="sheet-close" @click="closeAddressSheet">×</button>
      </view>
      <scroll-view scroll-y class="sheet-body">
        <view class="mini-map">
          <view class="map-bg" />
          <view class="mini-search"><image class="ui-icon" src="/static/icons/boss-location/search-gray.svg" mode="aspectFit" /><text>点击地图选点或搜索地址</text></view>
          <view class="mini-pin"><image class="ui-icon pin-icon" src="/static/icons/boss-location/map-marker-white.svg" mode="aspectFit" /></view>
        </view>
        <view class="sheet-field">
          <text class="field-label"><text class="required">*</text> 地点名称</text>
          <input v-model="draft.name" class="field-input" placeholder="请输入地点名称，如：观澜电子厂" />
        </view>
        <view class="sheet-field">
          <text class="field-label"><text class="required">*</text> 详细地址</text>
          <input v-model="draft.detail" class="field-input" placeholder="请输入详细地址，如：龙华区观澜街道桂月路316号" />
        </view>
        <view class="sheet-field">
          <text class="field-label">地点标签（可多选）</text>
          <view class="tag-chips">
            <text v-for="tag in tagPresets" :key="tag" :class="['tag-chip', { active: draft.tags.includes(tag) }]" @click="toggleDraftTag(tag)">{{ tag }}</text>
          </view>
        </view>
      </scroll-view>
      <view class="sheet-footer">
        <button class="cancel-btn" @click="closeAddressSheet">取消</button>
        <button class="save-btn" @click="saveAddressDraft">✓ 保存</button>
      </view>
    </view>
  </view>
</template>

<script>
import { listBossAddresses, searchBossAddresses, createBossAddress, updateBossAddress, deleteBossAddress, useBossAddress } from "@/api/backend";
export default {
  data() {
    return {
      selectedIndex: 0,
      statusBarHeight: uni.getSystemInfoSync().statusBarHeight || 0,
      distance: "1.2",
      addresses: [], keyword: "", loading: false,
      sheetVisible: false,
      editingIndex: -1,
      draft: { name: "", detail: "", tags: [] },
      tagPresets: ["电子厂", "有空调", "物流", "仓库", "餐饮", "分拣", "装卸", "包装", "有休息区", "室内"],
    };
  },
  computed: {
    selectedAddress() {
      return this.addresses[this.selectedIndex] || { name: "", address: "" };
    },
  },
  onLoad() {
    this.loadAddresses();
  },
  methods: {
    async loadAddresses() { this.loading = true; try { const result = await listBossAddresses(uni.getStorageSync("userId")); this.addresses = (Array.isArray(result) ? result : []).map((item) => ({ ...item, address: item.detail || "", tags: [] })); const i = this.addresses.findIndex((item) => item.isDefault); this.selectedIndex = i >= 0 ? i : 0; } catch (e) { uni.showToast({ title: e?.message || "地址加载失败", icon: "none" }); } finally { this.loading = false; } },
    async searchAddress() { if (!this.keyword.trim()) return this.loadAddresses(); try { const result = await searchBossAddresses(uni.getStorageSync("userId"), this.keyword.trim()); this.addresses = (Array.isArray(result) ? result : []).map((item) => ({ ...item, address: item.detail || "", tags: [] })); this.selectedIndex = 0; } catch (e) { uni.showToast({ title: e?.message || "地址搜索失败", icon: "none" }); } },
    closePage() {
      uni.navigateBack();
    },
    locateMe() {
      uni.showToast({ title: "正在获取当前位置...", icon: "loading" });
    },
    zoomIn() {
      uni.showToast({ title: "放大地图", icon: "none" });
    },
    zoomOut() {
      uni.showToast({ title: "缩小地图", icon: "none" });
    },
    async selectAddress(index) {
      this.selectedIndex = index;
      if (this.addresses[index]?.id) { try { await useBossAddress(this.addresses[index].id); await this.loadAddresses(); } catch (e) { uni.showToast({ title: e?.message || "地点选择失败", icon: "none" }); } }
    },
    manageAddress() {
      uni.showToast({ title: "管理地址", icon: "none" });
    },
    addNewAddress() { this.editingIndex = -1; this.draft = { name: "", detail: "", tags: [] }; this.sheetVisible = true; },
    editAddress(index) { const a = this.addresses[index]; this.editingIndex = index; this.draft = { name: a.name || "", detail: a.address || a.detail || "", tags: Array.isArray(a.tags) ? [...a.tags] : [] }; this.sheetVisible = true; },
    closeAddressSheet() { this.sheetVisible = false; },
    toggleDraftTag(tag) { const tags = new Set(this.draft.tags); tags.has(tag) ? tags.delete(tag) : tags.add(tag); this.draft.tags = [...tags]; },
    async saveAddressDraft() { const name = this.draft.name.trim(); const detail = this.draft.detail.trim(); if (!name) return uni.showToast({ title: "请输入地点名称", icon: "none" }); if (!detail) return uni.showToast({ title: "请输入详细地址", icon: "none" }); try { if (this.editingIndex === -1) await createBossAddress({ userId: uni.getStorageSync("userId"), name, detail, lat: 0, lng: 0, isDefault: false }); else { const a = this.addresses[this.editingIndex]; await updateBossAddress(a.id, { name, detail, lat: a.lat || 0, lng: a.lng || 0, isDefault: !!a.isDefault }); } this.closeAddressSheet(); await this.loadAddresses(); } catch (e) { uni.showToast({ title: e?.message || "地址保存失败", icon: "none" }); } },
    removeAddress(index) { const a = this.addresses[index]; uni.showModal({ title: "提示", content: `确定删除“${a.name}”吗？`, success: async ({ confirm }) => { if (!confirm) return; try { await deleteBossAddress(a.id); await this.loadAddresses(); } catch (e) { uni.showToast({ title: e?.message || "地址删除失败", icon: "none" }); } } }); },
    saveLocation() {
      const addr = this.selectedAddress;
      const data = {
        name: addr.name,
        address: addr.address,
        tag: addr.tag,
        distance: this.distance,
        display: `${addr.name} ${addr.address}`,
      };
      uni.setStorageSync("workLocationSelection", data);
      uni.$emit("workLocationSelected", data);
      uni.navigateBack();
    },
  },
};
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

.status-bar {
  height: 47px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 28px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  background: transparent;
}

.status-icons {
  display: flex;
  align-items: center;
  gap: 4px;
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

.nav-back image { width: 18px; height: 18px; }

.nav-title {
  font-size: 17px;
  font-weight: 700;
  color: #333;
}

.nav-placeholder {
  width: 32px;
}

.map-container {
  position: relative;
  height: 260px;
  background: linear-gradient(180deg, #e8f4fd 0%, #f0ede4 100%);
  overflow: hidden;
  flex-shrink: 0;
}

.map-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    linear-gradient(rgba(200, 200, 200, 0.3) 1px, transparent 1px),
    linear-gradient(90deg, rgba(200, 200, 200, 0.3) 1px, transparent 1px);
  background-size: 40px 40px;
}

.map-road {
  position: absolute;
  background: #ddd;
  border-radius: 4px;
}

.map-road-h {
  height: 14px;
  top: 60px;
  left: 0;
  right: 0;
}
.map-road-h2 {
  height: 10px;
  top: 160px;
  left: 0;
  right: 0;
}
.map-road-v {
  width: 14px;
  top: 0;
  bottom: 0;
  left: 100px;
}
.map-road-v2 {
  width: 10px;
  top: 0;
  bottom: 0;
  right: 80px;
}

.map-building {
  position: absolute;
  border-radius: 4px;
}

.b1 {
  width: 50px;
  height: 40px;
  background: #b8d4e3;
  top: 80px;
  left: 30px;
}
.b2 {
  width: 45px;
  height: 55px;
  background: #c5ddb0;
  top: 180px;
  left: 40px;
}
.b3 {
  width: 60px;
  height: 35px;
  background: #e8d5b7;
  top: 90px;
  right: 40px;
}
.b4 {
  width: 40px;
  height: 50px;
  background: #d4c5dd;
  top: 180px;
  right: 100px;
}
.b5 {
  width: 35px;
  height: 30px;
  background: #f5d7b5;
  top: 25px;
  left: 180px;
}
.b6 {
  width: 55px;
  height: 45px;
  background: #b8d4e3;
  top: 30px;
  right: 20px;
}

.map-pin {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -100%);
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.pin-outer {
  width: 36px;
  height: 36px;
  background: #ff6b35;
  border: 3px solid white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.4);
}

.pin-pulse {
  position: absolute;
  width: 60px;
  height: 60px;
  background: rgba(255, 107, 53, 0.2);
  border-radius: 50%;
  animation: pulse 2s infinite;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

@keyframes pulse {
  0% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.8;
  }
  100% {
    transform: translate(-50%, -50%) scale(1.5);
    opacity: 0;
  }
}

.map-controls {
  position: absolute;
  right: 12px;
  bottom: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.map-ctrl-btn {
  width: 36px;
  height: 36px;
  background: white;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  color: #333;
}

.locate-btn {
  position: absolute;
  right: 12px;
  top: 12px;
  width: 36px;
  height: 36px;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  color: #ff6b35;
}

.ui-icon { width: 18px; height: 18px; flex-shrink: 0; }
.pin-icon { width: 14px; height: 18px; }

.search-bar {
  position: absolute;
  left: 12px;
  right: 60px;
  top: 12px;
  height: 36px;
  background: white;
  border-radius: 18px;
  display: flex;
  align-items: center;
  padding: 0 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.search-bar input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 13px;
  background: transparent;
}

.search-bar input::placeholder {
  color: #bbb;
}

.location-info {
  background: white;
  margin: -16px 12px 12px;
  border-radius: 14px;
  padding: 14px 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  position: relative;
  z-index: 20;
}

.location-title-row {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
}

.location-title {
  font-size: 15px;
  font-weight: 700;
  color: #333;
  flex: 1;
}

.location-tag {
  font-size: 11px;
  color: #ff6b35;
  background: #fff0e8;
  padding: 2px 8px;
  border-radius: 4px;
  margin-right: 6px;
}

.location-distance {
  font-size: 11px;
  color: #999;
}

.location-address {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  display: block;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px 8px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.section-action {
  font-size: 13px;
  color: #ff6b35;
}

.address-list {
  padding: 0 12px;
}

.address-item {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  background: white;
  border-radius: 10px;
  margin-bottom: 8px;
  border: 1px solid transparent;
}

.address-item.selected {
  background: #fff8e6;
  border-color: #ff6b35;
}

.address-icon {
  width: 36px;
  height: 36px;
  background: #f5f5f5;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ff6b35;
  margin-right: 12px;
  flex-shrink: 0;
}

.address-info {
  flex: 1;
  min-width: 0;
}

.address-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.address-detail {
  font-size: 12px;
  color: #999;
  line-height: 1.4;
  display: block;
}

.address-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.addr-tag {
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 3px;
  background: #f0f0f0;
  color: #666;
}

.addr-tag.hot {
  background: #ff6b35;
  color: white;
}

.add-address-btn {
  margin: 12px;
  height: 44px;
  border: 1.5px dashed #ddd;
  border-radius: 10px;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 14px;
  color: #999;
}

.scroll-area {
  padding-bottom: 12px;
}

.bottom-bar {
  background: white;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
  padding-bottom: 20px;
}

.submit-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: white;
  border-radius: 9999px;
  font-size: 16px;
  font-weight: 600;
  border: none;
  box-shadow: 0 6px 20px rgba(255, 107, 53, 0.3);
}

.sheet-mask { position: fixed; inset: 0; z-index: 80; background: rgba(0, 0, 0, 0.4); }
.address-sheet { position: fixed; left: 0; right: 0; bottom: 0; z-index: 81; max-height: calc(100vh - 120px); display: flex; flex-direction: column; overflow: hidden; border-radius: 20px 20px 0 0; background: #fff; box-shadow: 0 -10px 30px rgba(0, 0, 0, 0.15); }
.sheet-grip { width: 40px; height: 4px; margin: 10px auto 6px; border-radius: 2px; background: #e5e7eb; }
.sheet-header { display: flex; align-items: center; justify-content: space-between; padding: 4px 18px 14px; }
.sheet-title { color: #1f2937; font-size: 17px; font-weight: 700; }
.sheet-close { width: 28px; height: 28px; margin: 0; padding: 0; display: flex; align-items: center; justify-content: center; border: 0; border-radius: 50%; background: #f3f4f6; color: #9ca3af; font-size: 22px; line-height: 28px; }
.sheet-close::after, .cancel-btn::after, .save-btn::after { border: 0; }
.sheet-body { flex: 1; min-height: 0; box-sizing: border-box; padding: 0 18px 18px; }
.mini-map { position: relative; height: 140px; margin-bottom: 16px; overflow: hidden; border: 1px solid #e5e7eb; border-radius: 12px; background: linear-gradient(180deg, #e8f4fd 0%, #f0ede4 100%); }
.mini-map .map-bg { opacity: 0.6; }
.mini-search { position: absolute; top: 10px; left: 12px; right: 12px; z-index: 3; display: flex; align-items: center; gap: 8px; padding: 7px 14px; border-radius: 20px; background: #fff; box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08); color: #6b7280; font-size: 13px; }
.search-symbol { font-size: 20px; line-height: 1; }
.mini-pin { position: absolute; top: 50%; left: 50%; z-index: 2; width: 28px; height: 28px; transform: translate(-50%, -100%); display: flex; align-items: center; justify-content: center; border: 3px solid #fff; border-radius: 50%; background: #ff6b35; color: #fff; font-size: 10px; }
.sheet-field { margin-bottom: 14px; }
.field-label { display: block; margin-bottom: 8px; color: #374151; font-size: 13px; font-weight: 600; }
.required { color: #ef4444; }
.field-input { width: 100%; height: 42px; padding: 0 12px; box-sizing: border-box; border: 1px solid #e5e7eb; border-radius: 10px; background: #f9fafb; color: #374151; font-size: 14px; }
.tag-chips { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 4px; }
.tag-chip { padding: 5px 12px; border: 1px solid #ffe8dc; border-radius: 16px; background: #fff8e6; color: #ff6b35; font-size: 12px; }
.tag-chip.active { border-color: #ff6b35; background: #ff6b35; color: #fff; }
.sheet-footer { flex-shrink: 0; display: flex; gap: 10px; padding: 14px 18px calc(18px + env(safe-area-inset-bottom)); border-top: 1px solid #f3f4f6; background: #fff; }
.cancel-btn, .save-btn { height: 48px; margin: 0; padding: 0; border-radius: 24px; font-size: 14px; font-weight: 600; line-height: 48px; }
.cancel-btn { flex: 1; border: 1px solid #e5e7eb; background: #fff; color: #374151; }
.save-btn { flex: 2; background: linear-gradient(135deg, #ff6b35, #ff8c5a); box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3); color: #fff; }
</style>
