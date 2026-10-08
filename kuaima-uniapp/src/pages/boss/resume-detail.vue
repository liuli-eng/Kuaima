<template>
  <view class="container">
    <view class="wb-header" :style="{ paddingTop: statusBarHeight + 8 + 'px' }">
      <view class="wb-back" @click="goBack"><image class="back-svg" src="/static/icons/boss-recruit-settings/chevron-left.svg" mode="aspectFit" /></view>
      <text class="wb-title">简历详情</text>
    </view>

    <view class="wb-body has-footer">
      <view v-if="loading" class="wb-empty"><text class="wb-empty-text">加载中...</text></view>
      <template v-else-if="resume">
        <!-- 候选人头部 -->
        <view class="profile-head">
          <view class="profile-avatar" :style="{ background: avatarColor(resume.name) }">{{ (resume.name || '人').charAt(0) }}</view>
          <view class="profile-main">
            <view class="profile-name-line">
              <text class="profile-name">{{ resume.name }}</text>
              <text class="wb-tag" :class="statusCls(resume.status)">{{ statusText(resume.status) }}</text>
            </view>
            <text class="profile-tags">
              <text>{{ resume.position || '—' }}</text>
              <text>{{ resume.experience || '—' }}</text>
              <text>{{ resume.gender || '—' }}</text>
              <text>{{ resume.age || '—' }}岁</text>
            </text>
            <view class="salary-line"><image class="salary-svg" src="/static/icons/worker-profile/coins-orange.svg" mode="aspectFit" /><text>期望薪资：{{ resume.expectedSalary || '—' }}</text></view>
          </view>
        </view>

        <!-- 信息Tab -->
        <view class="info-tabs">
          <view v-for="t in infoTabs" :key="t.key" class="info-tab" :class="{ active: currentInfoTab === t.key }" @click="currentInfoTab = t.key">{{ t.label }}</view>
        </view>

        <view class="info-panel">
          <!-- 基本信息 -->
          <view v-if="currentInfoTab === 'basic'">
            <view class="info-block-title"><image class="block-svg" src="/static/icons/worker-profile/id-card-dark.svg" mode="aspectFit" />基本信息</view>
            <view class="info-line"><text class="info-key">姓名</text><text class="info-val">{{ resume.name }}</text></view>
            <view class="info-line"><text class="info-key">性别</text><text class="info-val">{{ resume.gender || '—' }}</text></view>
            <view class="info-line"><text class="info-key">年龄</text><text class="info-val">{{ resume.age || '—' }}岁</text></view>
            <view class="info-line"><text class="info-key">电话</text><text class="info-val mono">{{ resume.phone || '—' }}</text></view>
            <view class="info-line"><text class="info-key">邮箱</text><text class="info-val">{{ resume.email || '—' }}</text></view>
            <view class="info-line"><text class="info-key">居住地</text><text class="info-val">{{ resume.city || '—' }}</text></view>
            <view class="info-line"><text class="info-key">身份证</text><text class="info-val mono">{{ resume.idCard || '—' }}</text></view>
            <view class="info-block-title"><image class="block-svg" src="/static/icons/boss-location/crosshairs-orange.svg" mode="aspectFit" />求职意向</view>
            <view class="info-line"><text class="info-key">意向岗位</text><text class="info-val">{{ resume.position || '—' }}</text></view>
            <view class="info-line"><text class="info-key">期望薪资</text><text class="info-val">{{ resume.expectedSalary || '—' }}</text></view>
            <view class="info-line"><text class="info-key">工作地点</text><text class="info-val">{{ resume.workLocation || '—' }}</text></view>
            <template v-if="filePreviewUrl">
              <view class="info-block-title"><image class="block-svg" src="/static/icons/worker-job-detail/file-alt-orange.svg" mode="aspectFit" />原始简历</view>
              <view v-if="filePreviewIsImage" class="resume-preview" @click="openResumeFile">
                <image class="resume-preview-img" :src="filePreviewUrl" mode="widthFix" />
                <text class="resume-preview-tip">点击查看大图</text>
              </view>
              <view v-else class="info-line file-line" @click="openResumeFile">
                <text class="info-key">文件</text>
                <text class="info-val file-val">点击查看原件</text>
              </view>
            </template>
          </view>

          <!-- 教育经历 -->
          <view v-if="currentInfoTab === 'edu'">
            <view class="info-block-title"><image class="block-svg" src="/static/icons/worker-home/graduation-cap-brown.svg" mode="aspectFit" />教育经历</view>
            <view v-if="!edu.length" class="wb-empty-sm">暂无教育经历</view>
            <view v-for="e in edu" :key="e.id" class="exp-item">
              <view class="exp-top">
                <view>
                  <text class="exp-title">{{ e.title }}</text>
                  <text class="exp-org">{{ e.org || '' }}</text>
                </view>
                <text class="exp-date">{{ e.startDate || '' }} - {{ e.endDate || '' }}</text>
              </view>
              <text v-if="e.description" class="exp-desc">{{ e.description }}</text>
              <view v-if="e.tags" class="exp-tags">
                <text v-for="tag in splitTags(e.tags)" :key="tag" class="exp-tag">{{ tag }}</text>
              </view>
            </view>
          </view>

          <!-- 工作经历 -->
          <view v-if="currentInfoTab === 'work'">
            <view class="info-block-title"><image class="block-svg" src="/static/icons/worker-home/briefcase-orange.svg" mode="aspectFit" />工作经历</view>
            <view v-if="!work.length" class="wb-empty-sm">暂无工作经历</view>
            <view v-for="w in work" :key="w.id" class="exp-item">
              <view class="exp-top">
                <view>
                  <text class="exp-title">{{ w.title }}</text>
                  <text class="exp-org">{{ w.org || '' }}</text>
                </view>
                <text class="exp-date">{{ w.startDate || '' }} - {{ w.endDate || '' }}</text>
              </view>
              <text v-if="w.description" class="exp-desc">{{ w.description }}</text>
              <view v-if="w.tags" class="exp-tags">
                <text v-for="tag in splitTags(w.tags)" :key="tag" class="exp-tag">{{ tag }}</text>
              </view>
            </view>
          </view>

          <!-- 项目经验 -->
          <view v-if="currentInfoTab === 'project'">
            <view class="info-block-title"><image class="block-svg" src="/static/icons/boss-resume/diagram-project-orange.svg" mode="aspectFit" />项目经验</view>
            <view v-if="!project.length" class="wb-empty-sm">暂无项目经验</view>
            <view v-for="p in project" :key="p.id" class="exp-item">
              <view class="exp-top">
                <view>
                  <text class="exp-title">{{ p.title }}</text>
                  <text class="exp-org">{{ p.org || '' }}</text>
                </view>
                <text class="exp-date">{{ p.startDate || '' }} - {{ p.endDate || '' }}</text>
              </view>
              <text v-if="p.description" class="exp-desc">{{ p.description }}</text>
              <view v-if="p.tags" class="exp-tags">
                <text v-for="tag in splitTags(p.tags)" :key="tag" class="exp-tag">{{ tag }}</text>
              </view>
            </view>
          </view>
        </view>
      </template>
    </view>

    <!-- 底部操作条 -->
    <view class="wb-footer">
      <!-- 收藏：原在头部自绘胶囊里，会与微信原生胶囊重叠；移到页面内保留功能 -->
      <view class="wb-btn-fav" :class="{ active: favorite }" @click="toggleFav">
        <image class="footer-svg" :src="favorite ? '/static/icons/worker-orders/star-active.svg' : '/static/icons/worker-orders/star-inactive.svg'" mode="aspectFit" />
        <text>{{ favorite ? '已收藏' : '收藏' }}</text>
      </view>
      <view class="wb-btn-outline-full" @click="callResume"><image class="footer-svg" src="/static/icons/boss-recruit-settings/phone.svg" mode="aspectFit" /><text>联系TA</text></view>
      <view class="wb-btn-primary-full" @click="sendResume"><image class="footer-svg footer-svg-white" src="/static/icons/boss-resume/paper-plane-white.svg" mode="aspectFit" /><text>安排投递</text></view>
    </view>
  </view>
</template>

<script>
import { getResumeDetail, toggleResumeFavorite, updateResumeStatus, RESUME_STATUS_TEXT, RESUME_STATUS_CLS } from "@/api/resume";
import { avatarColor } from "@/api/project";

export default {
  data() {
    return {
      statusBarHeight: 44,
      resumeId: "",
      loading: false,
      resume: null,
      edu: [],
      work: [],
      project: [],
      favorite: false,
      filePreviewUrl: "",
      filePreviewIsImage: false,
      currentInfoTab: "basic",
      infoTabs: [
        { key: "basic", label: "基本信息" },
        { key: "edu", label: "教育经历" },
        { key: "work", label: "工作经历" },
        { key: "project", label: "项目经验" },
      ],
    };
  },
  onLoad(options) {
    this.resumeId = options.id || "";
    try {
      const info = uni.getSystemInfoSync();
      this.statusBarHeight = info.statusBarHeight || 44;
    } catch (e) {}
    this.loadDetail();
  },
  methods: {
    avatarColor,
    statusText: (s) => RESUME_STATUS_TEXT[s] || s,
    statusCls: (s) => RESUME_STATUS_CLS[s] || "gray",
    /** 打开导入的原始简历：预览图（图片）直接放大看；其他类型先下载再用 openDocument 打开。 */
    openResumeFile() {
      const url = this.filePreviewUrl;
      if (!url) return;
      if (this.filePreviewIsImage) {
        uni.previewImage({ urls: [url] });
        return;
      }
      uni.showLoading({ title: "打开中...", mask: true });
      uni.downloadFile({
        url,
        success: (res) => {
          uni.hideLoading();
          if (res.statusCode !== 200) {
            uni.showToast({ title: "文件下载失败", icon: "none" });
            return;
          }
          uni.openDocument({
            filePath: res.tempFilePath,
            showMenu: true,
            fail: () => uni.showToast({ title: "该文件类型暂不支持预览", icon: "none" }),
          });
        },
        fail: () => {
          uni.hideLoading();
          uni.showToast({ title: "文件下载失败", icon: "none" });
        },
      });
    },
    splitTags(str) {
      return (str || "").split(",").map((s) => s.trim()).filter(Boolean);
    },
    async loadDetail() {
      if (!this.resumeId) return;
      this.loading = true;
      try {
        const data = await getResumeDetail(this.resumeId);
        this.resume = data?.resume || null;
        this.edu = data?.edu || [];
        this.work = data?.work || [];
        this.project = data?.project || [];
        this.favorite = !!this.resume?.favorite;
        // 导入来源的简历带原始文件；PDF 由后端渲染首页为预览图，私有 Bucket 会返回签名地址
        this.filePreviewUrl = data?.filePreviewUrl || this.resume?.fileUrl || "";
        this.filePreviewIsImage = !!data?.filePreviewIsImage;
        // 自动标记已查看
        if (this.resume && this.resume.status !== "VIEWED" && this.resume.status !== "SENT") {
          updateResumeStatus(this.resumeId, "VIEWED").catch(() => {});
        }
      } catch (e) {
        uni.showToast({ title: e.message || "加载失败", icon: "none" });
      } finally {
        this.loading = false;
      }
    },
    async toggleFav() {
      try {
        const r = await toggleResumeFavorite(this.resumeId);
        this.favorite = !!r?.favorite;
        uni.showToast({ title: this.favorite ? "已加入收藏" : "已取消收藏", icon: "none" });
      } catch (e) {
        uni.showToast({ title: e.message || "操作失败", icon: "none" });
      }
    },
    callResume() {
      if (this.resume?.phone) {
        uni.makePhoneCall({ phoneNumber: this.resume.phone, fail: () => {} });
      } else {
        uni.showToast({ title: "该简历无手机号", icon: "none" });
      }
    },
    async sendResume() {
      try {
        await updateResumeStatus(this.resumeId, "SENT");
        uni.showToast({ title: "已安排投递", icon: "none" });
        if (this.resume) this.resume.status = "SENT";
      } catch (e) {
        uni.showToast({ title: e.message || "操作失败", icon: "none" });
      }
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
  padding: 8px 16px 12px;
  flex-shrink: 0;
}

.wb-back {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.back-svg {
  width: 16px;
  height: 16px;
}

.wb-title {
  flex: 1;
  font-size: 17px;
  font-weight: 600;
  color: #1a1a1a;
}

.wb-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px 20px;
}

.wb-body.has-footer {
  padding-bottom: 100px;
}

.profile-head {
  background: #fff;
  border-radius: 16px;
  padding: 18px 16px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
  gap: 14px;
}

.profile-avatar {
  width: 58px;
  height: 58px;
  border-radius: 50%;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
  font-weight: 600;
}

.profile-main {
  flex: 1;
  min-width: 0;
}

.profile-name-line {
  display: flex;
  align-items: center;
  gap: 8px;
}

.profile-name {
  font-size: 18px;
  font-weight: 700;
  color: #222;
}

.profile-tags {
  font-size: 12px;
  color: #888;
  margin-top: 5px;
  display: block;
}

.profile-tags text + text {
  margin-left: 6px;
}

.salary-line {
  font-size: 12.5px;
  color: #ff6b35;
  margin-top: 6px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

.salary-svg {
  width: 13px;
  height: 13px;
}

.wb-tag {
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 5px;
  font-weight: 600;
}

.wb-tag.blue {
  background: #ebf3ff;
  color: #357abd;
}

.wb-tag.green {
  background: #e8f8ef;
  color: #10b981;
}

.wb-tag.orange {
  background: #fff7e0;
  color: #d97706;
}

.wb-tag.gray {
  background: #f5f5f5;
  color: #999;
}

.info-tabs {
  display: flex;
  background: #fff;
  border-radius: 14px 14px 0 0;
  margin-top: 12px;
  padding: 0 4px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
}

.info-tab {
  flex: 1;
  text-align: center;
  padding: 13px 0;
  font-size: 13.5px;
  color: #888;
  position: relative;
  font-weight: 500;
}

.info-tab.active {
  color: #ff6b35;
  font-weight: 600;
}

.info-tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 22px;
  height: 3px;
  border-radius: 2px;
  background: #ff6b35;
}

.info-panel {
  background: #fff;
  border-radius: 0 0 14px 14px;
  padding: 6px 16px 18px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
  margin-bottom: 12px;
}

.info-block-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  padding: 14px 0 4px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.block-svg {
  width: 13px;
  height: 13px;
}

.info-line {
  display: flex;
  padding: 10px 0;
  border-bottom: 0.5px solid #f6f6f6;
  font-size: 13.5px;
}

.info-line:last-child {
  border-bottom: none;
}

.info-key {
  width: 88px;
  color: #999;
  flex-shrink: 0;
}

.info-val {
  flex: 1;
  color: #333;
  word-break: break-all;
}

/* 原始简历入口 */
.file-line {
  align-items: center;
}

.file-val {
  color: #ff6b35;
  font-weight: 500;
}

/* 原始简历预览图（PDF 首页渲染） */
.resume-preview {
  margin-top: 4px;
}

.resume-preview-img {
  width: 100%;
  border-radius: 12px;
  border: 1px solid #eee;
  background: #fafafa;
}

.resume-preview-tip {
  display: block;
  text-align: center;
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.info-val.mono {
  letter-spacing: 0.3px;
}

.exp-item {
  padding: 13px 0;
  border-bottom: 0.5px solid #f6f6f6;
}

.exp-item:last-child {
  border-bottom: none;
}

.exp-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.exp-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  display: block;
}

.exp-org {
  font-size: 12.5px;
  color: #666;
  margin-top: 3px;
  display: block;
}

.exp-date {
  font-size: 11.5px;
  color: #aaa;
  flex-shrink: 0;
  margin-top: 2px;
}

.exp-desc {
  font-size: 12.5px;
  color: #888;
  line-height: 1.7;
  margin-top: 8px;
  display: block;
}

.exp-tags {
  margin-top: 7px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.exp-tag {
  font-size: 11px;
  color: #ff6b35;
  background: #fff0e8;
  padding: 2px 8px;
  border-radius: 6px;
}

.wb-empty-sm {
  font-size: 12.5px;
  color: #bbb;
  padding: 10px 0;
  text-align: center;
}

.wb-empty {
  padding: 40px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.wb-empty-text {
  font-size: 13px;
  color: #999;
}

.wb-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 10px;
  padding: 12px 16px calc(12px + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.98);
  border-top: 0.5px solid rgba(0, 0, 0, 0.05);
}

/* 收藏按钮：原在头部自绘胶囊内，移到页面内避免与微信原生胶囊重叠；宽度比两个主按钮窄以免挤压 */
.wb-btn-fav {
  flex: 0 0 auto;
  width: 62px;
  padding: 12px 0;
  border: 1px solid #e5e5e5;
  border-radius: 24px;
  font-size: 11px;
  color: #666;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 3px;
}

.wb-btn-fav.active {
  color: #ff6b35;
  border-color: #ffd2c0;
}

.wb-btn-outline-full {
  flex: 1;
  padding: 12px 0;
  border: 1px solid #e5e5e5;
  color: #666;
  border-radius: 24px;
  font-size: 14px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.wb-btn-primary-full {
  flex: 1.4;
  padding: 12px 0;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 600;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: #fff;
  box-shadow: 0 3px 8px rgba(255, 107, 53, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.footer-svg {
  width: 15px;
  height: 15px;
}
</style>
