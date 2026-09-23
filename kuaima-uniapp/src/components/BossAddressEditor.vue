<template>
  <view v-if="visible" class="editor-mask" @click="$emit('cancel')">
    <view class="editor-sheet" @click.stop>
      <view class="sheet-grip" />
      <view class="sheet-header">
        <text class="sheet-title">{{ editing ? "编辑招工地址" : "新增招工地址" }}</text>
        <button class="sheet-close" @click="$emit('cancel')">×</button>
      </view>
      <scroll-view scroll-y class="sheet-body">
        <view class="address-minimap" @click="$emit('choose-location')">
          <view class="minimap-grid" />
          <view class="minimap-road horizontal" />
          <view class="minimap-road vertical" />
          <view class="minimap-search">
            <image src="/static/icons/boss-location/search-gray.svg" mode="aspectFit" />
            <text>{{ draft.detail || "点击地图选点或搜索地址" }}</text>
          </view>
          <view class="minimap-pin"><image src="/static/icons/boss-location/map-marker-white.svg" mode="aspectFit" /></view>
        </view>
        <view class="form-item"><text class="form-label"><text class="required">*</text> 地点名称</text><input v-model="draft.name" class="form-input" maxlength="50" placeholder="请输入地点名称，如：观澜电子厂" /></view>
        <view class="form-item"><text class="form-label"><text class="required">*</text> 详细地址</text><input v-model="draft.detail" class="form-input" maxlength="100" placeholder="请输入详细地址" /></view>
        <view class="form-item"><text class="form-label"><text class="required">*</text> 所在城市</text><input v-model="draft.city" class="form-input" maxlength="30" placeholder="请输入所在城市，如：深圳市" /></view>
        <view class="form-item"><text class="form-label"><text class="required">*</text> 所在区县</text><input v-model="draft.district" class="form-input" maxlength="30" placeholder="请输入所在区县，如：龙华区" /></view>
        <view class="form-item"><text class="form-label">地点标签（可多选）</text><view class="tag-chips"><text v-for="tag in tags" :key="tag" class="tag-chip" :class="{ active: draft.tags.includes(tag) }" @click="$emit('toggle-tag', tag)">{{ tag }}</text></view></view>
        <view class="form-item"><text class="form-label"><text class="required">*</text> 联系人</text><input v-model="draft.contactName" class="form-input" maxlength="20" placeholder="请输入联系人姓名" /></view>
        <view class="form-item"><text class="form-label"><text class="required">*</text> 联系电话</text><input v-model="draft.contactPhone" class="form-input" type="number" maxlength="11" placeholder="请输入联系电话" /></view>
        <view class="default-row" @click="draft.isDefault = !draft.isDefault"><view class="checkbox" :class="{ checked: draft.isDefault }">✓</view><text>设为默认地址</text></view>
      </scroll-view>
      <view class="sheet-footer"><button class="cancel-btn" :disabled="saving" @click="$emit('cancel')">取消</button><button class="save-btn" :disabled="saving" @click="$emit('save')">{{ saving ? "保存中..." : "保存" }}</button></view>
    </view>
  </view>
</template>

<script>
export default {
  name: "BossAddressEditor",
  props: {
    visible: Boolean,
    editing: Boolean,
    saving: Boolean,
    draft: { type: Object, required: true },
    tags: { type: Array, default: () => [] },
  },
};
</script>

<style scoped>
.editor-mask { position:fixed; z-index:50; inset:0; display:flex; align-items:flex-end; background:rgba(0,0,0,.45); }
.editor-sheet { width:100%; max-height:88vh; padding:20rpx 32rpx calc(28rpx + env(safe-area-inset-bottom)); border-radius:28rpx 28rpx 0 0; background:#fff; box-sizing:border-box; }
.sheet-grip { width:72rpx; height:8rpx; margin:0 auto 20rpx; border-radius:8rpx; background:#ddd; }.sheet-header { display:flex; align-items:center; justify-content:space-between; }.sheet-title { color:#333; font-size:32rpx; font-weight:600; }.sheet-close { width:60rpx; height:60rpx; padding:0; border:0; color:#999; background:transparent; font-size:42rpx; line-height:60rpx; }.sheet-close::after,.cancel-btn::after,.save-btn::after { border:0; }
.sheet-body { max-height:65vh; margin-top:16rpx; }.address-minimap { position:relative; height:170rpx; margin-bottom:20rpx; overflow:hidden; border-radius:16rpx; background:linear-gradient(135deg,#e8f5e9,#c8e6c9); }.minimap-grid { position:absolute; inset:0; background-image:linear-gradient(rgba(255,255,255,.35) 1px,transparent 1px),linear-gradient(90deg,rgba(255,255,255,.35) 1px,transparent 1px); background-size:30px 30px; }.minimap-road { position:absolute; background:rgba(255,255,255,.6); }.minimap-road.horizontal { top:70rpx; left:0; right:0; height:12rpx; }.minimap-road.vertical { top:0; bottom:0; left:48%; width:12rpx; }.minimap-search { position:absolute; top:18rpx; left:20rpx; right:20rpx; display:flex; align-items:center; gap:10rpx; padding:12rpx 16rpx; border-radius:12rpx; background:rgba(255,255,255,.9); color:#999; font-size:22rpx; }.minimap-search image { width:26rpx; height:26rpx; }.minimap-pin { position:absolute; top:82rpx; left:50%; display:flex; width:48rpx; height:48rpx; align-items:center; justify-content:center; border-radius:50% 50% 50% 0; background:#ff6b35; transform:translate(-50%,-50%) rotate(-45deg); }.minimap-pin image { width:26rpx; height:26rpx; transform:rotate(45deg); }
.form-item { display:flex; align-items:flex-start; min-height:76rpx; padding:18rpx 0; border-bottom:1rpx solid #f5f5f5; box-sizing:border-box; }.form-label { width:190rpx; flex-shrink:0; color:#333; font-size:25rpx; line-height:48rpx; }.required { color:#ff4d4f; }.form-input { flex:1; min-width:0; height:48rpx; color:#333; font-size:25rpx; }.tag-chips { display:flex; flex:1; flex-wrap:wrap; gap:12rpx; }.tag-chip { padding:8rpx 16rpx; border-radius:22rpx; color:#666; background:#f5f5f5; font-size:21rpx; }.tag-chip.active { color:#ff6b35; background:#fff0e8; }.default-row { display:flex; align-items:center; gap:14rpx; padding:24rpx 0 8rpx; color:#666; font-size:24rpx; }.checkbox { display:flex; align-items:center; justify-content:center; width:34rpx; height:34rpx; border:2rpx solid #ddd; border-radius:8rpx; color:transparent; font-size:22rpx; }.checkbox.checked { border-color:#ff6b35; color:#fff; background:#ff6b35; }
.sheet-footer { display:flex; gap:20rpx; margin-top:18rpx; }.cancel-btn,.save-btn { flex:1; height:80rpx; border:0; border-radius:40rpx; font-size:28rpx; line-height:80rpx; }.cancel-btn { color:#666; background:#f5f5f5; }.save-btn { color:#fff; background:linear-gradient(135deg,#ff8c5a,#ff6b35); }.save-btn[disabled],.cancel-btn[disabled] { opacity:.6; }
</style>
