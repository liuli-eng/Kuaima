<template>
  <view class="container">
    <view class="wb-header" :style="{ paddingTop: statusBarHeight + 8 + 'px' }">
      <view class="wb-back" @click="goBack"><text class="back-ico">‹</text></view>
      <text class="wb-title">简历导入</text>
      <view class="wb-capsule">
        <view class="cap-btn"><text class="cap-ico">⋯</text></view>
        <view class="cap-divider"></view>
        <view class="cap-btn"><text class="cap-ico">○</text></view>
      </view>
    </view>

    <view class="wb-body">
      <!-- 上传区 -->
      <view class="upload-card">
        <view class="upload-icon">☁️</view>
        <text class="upload-title">支持多种方式导入简历</text>
        <text class="upload-desc">上传简历文件后系统将自动解析入库\n便于统一筛选与管理</text>
        <view class="upload-btn" @click="chooseFile">📤 上传简历</view>
        <view class="format-tags">
          <text class="format-tag">PDF</text>
          <text class="format-tag">DOC</text>
          <text class="format-tag">DOCX</text>
          <text class="format-tag">JPG</text>
          <text class="format-tag">PNG</text>
        </view>
      </view>

      <!-- 导入记录 -->
      <view class="section-title">导入记录</view>
      <view v-if="loading" class="wb-empty"><text class="wb-empty-text">加载中...</text></view>
      <view v-else-if="!records.length" class="wb-empty">
        <view class="wb-empty-icon"><text>📋</text></view>
        <text class="wb-empty-text">暂无导入记录</text>
      </view>
      <view v-for="r in records" :key="r.id" class="record-item">
        <view class="file-icon" :class="r.status === 'FAILED' ? 'fail' : r.status === 'SUCCESS' ? 'ok' : ''">📄</view>
        <view class="file-main">
          <text class="file-name">{{ r.fileName }}</text>
          <text class="file-time">{{ r.timestamp || '' }}</text>
        </view>
        <text class="file-status" :class="r.status === 'FAILED' ? 'fail' : 'ok'">{{ r.status === 'FAILED' ? '解析失败' : '导入成功' }}</text>
        <text class="file-view" @click="openResume(r)">查看</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getResumeImports, createResumeImport } from "@/api/resume";

export default {
  data() {
    return {
      statusBarHeight: 44,
      loading: false,
      records: [],
    };
  },
  onLoad() {
    try {
      const info = uni.getSystemInfoSync();
      this.statusBarHeight = info.statusBarHeight || 44;
    } catch (e) {}
    this.loadRecords();
  },
  methods: {
    async loadRecords() {
      this.loading = true;
      try {
        this.records = (await getResumeImports()) || [];
      } catch (e) {
        // 暂不展示错误
      } finally {
        this.loading = false;
      }
    },
    chooseFile() {
      uni.chooseMessageFile({
        count: 1,
        type: "file",
        extension: ["pdf", "doc", "docx", "jpg", "jpeg", "png"],
        success: async (res) => {
          const file = res.tempFiles?.[0];
          if (!file) return;
          uni.showLoading({ title: "上传中..." });
          try {
            // 调用后端新增导入记录（真实文件解析需后端对接文件服务，前端这里先 mock 一个成功的记录）
            await createResumeImport({
              fileName: file.name,
              fileUrl: file.path,
              success: true,
            });
            uni.hideLoading();
            uni.showToast({ title: "上传成功", icon: "none" });
            this.loadRecords();
          } catch (e) {
            uni.hideLoading();
            uni.showToast({ title: e.message || "上传失败", icon: "none" });
          }
        },
        fail: () => uni.showToast({ title: "已取消选择", icon: "none" }),
      });
    },
    openResume(r) {
      if (r.resumeId) uni.navigateTo({ url: `/pages/boss/resume-detail?id=${r.resumeId}` });
      else uni.showToast({ title: "简历尚未解析完成", icon: "none" });
    },
    goBack() {
      uni.navigateBack({ fail: () => uni.reLaunch({ url: "/pages/boss/resume" }) });
    },
  },
};
</script>

<style lang="scss" scoped>
.container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f3f4f6;
}

.wb-header {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff;
  padding: 6px 16px 12px;
  flex-shrink: 0;
}

.wb-back {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-ico {
  font-size: 22px;
  color: #333;
}

.wb-title {
  flex: 1;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.wb-capsule {
  display: flex;
  align-items: center;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 17px;
  padding: 0 6px;
  height: 32px;
}

.cap-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cap-ico {
  font-size: 14px;
  color: #666;
}

.cap-divider {
  width: 1px;
  height: 16px;
  background: rgba(0, 0, 0, 0.15);
  margin: 0 2px;
}

.wb-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px 20px;
}

.upload-card {
  background: #fff;
  border-radius: 16px;
  padding: 34px 20px 30px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
  text-align: center;
  margin-bottom: 18px;
}

.upload-icon {
  width: 74px;
  height: 74px;
  border-radius: 50%;
  margin: 0 auto 14px;
  background: linear-gradient(135deg, #ffe8d6, #ffd2bc);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
}

.upload-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  display: block;
}

.upload-desc {
  font-size: 12px;
  color: #999;
  margin-top: 7px;
  line-height: 1.6;
  display: block;
  white-space: pre-line;
}

.upload-btn {
  display: inline-block;
  margin-top: 18px;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: #fff;
  padding: 11px 42px;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 600;
  box-shadow: 0 6px 16px rgba(255, 107, 53, 0.3);
}

.upload-btn:active {
  transform: scale(0.97);
}

.format-tags {
  display: flex;
  justify-content: center;
  gap: 7px;
  margin-top: 16px;
  flex-wrap: wrap;
}

.format-tag {
  font-size: 11px;
  color: #999;
  background: #f5f6f8;
  padding: 3px 10px;
  border-radius: 6px;
}

.section-title {
  font-size: 15px;
  font-weight: 700;
  color: #333;
  margin: 4px 0 10px;
}

.record-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: #fff;
  border-radius: 14px;
  margin-bottom: 10px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
}

.file-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  flex-shrink: 0;
  background: #fff0e8;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ff6b35;
  font-size: 17px;
}

.file-icon.fail {
  background: #fef2f2;
  color: #ff4d4f;
}

.file-icon.ok {
  background: #e8f8ef;
  color: #10b981;
}

.file-main {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  display: block;
}

.file-time {
  font-size: 11.5px;
  color: #aaa;
  margin-top: 4px;
  display: block;
}

.file-status {
  font-size: 12px;
  font-weight: 500;
  flex-shrink: 0;
}

.file-status.ok {
  color: #10b981;
}

.file-status.fail {
  color: #ff4d4f;
}

.file-view {
  font-size: 12.5px;
  color: #ff6b35;
  flex-shrink: 0;
  margin-left: 4px;
}

.wb-empty {
  padding: 40px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.wb-empty-icon {
  font-size: 34px;
  color: #ddd;
  margin-bottom: 10px;
}

.wb-empty-text {
  font-size: 13px;
  color: #999;
}
</style>
