<template>
  <view class="container">
    <view class="page-bg">
      <!-- 头部 -->
      <view
        class="modal-header"
        :style="{ paddingTop: `${statusBarHeight + 18}px` }"
      >
        <text class="modal-title">任务要求</text>
        <view
          class="modal-close"
          :style="{ top: `${statusBarHeight + 14}px` }"
          @click="closePage"
        >
          <text class="close-icon">×</text>
        </view>
      </view>

      <scroll-view scroll-y class="modal-content">
        <!-- 招工标题 -->
        <view class="section-card">
          <text class="field-label">招工标题</text>
          <input
            class="title-input"
            v-model="jobTitle"
            maxlength="20"
            placeholder="请输入招工标题"
          />
          <text class="title-count">{{ jobTitle.length }}/20</text>
        </view>

        <!-- 详情描述 -->
        <view class="section-card">
          <view class="field-label">
            <text
              >详情描述
              <text style="color: #999; font-weight: 400">(非必填)</text></text
            >
          </view>
          <textarea
            class="desc-textarea"
            v-model="jobDesc"
            placeholder="补充工作要求或详细内容，请勿填写手机号或微信号"
            maxlength="200"
          ></textarea>
          <text class="desc-count">{{ jobDesc.length }}/200</text>

          <view class="upload-row">
            <view class="upload-btn" @click="uploadVideo">
              <text class="upload-icon">▶</text>
              <text class="upload-label">上传视频</text>
            </view>
            <view class="upload-btn" @click="uploadPhoto">
              <text class="upload-icon">▧</text>
              <text class="upload-label">上传照片</text>
            </view>
          </view>
          <view class="upload-hint">
            <view>
              <text>上传有工作内容的视频或照片，</text>
              <text class="upload-highlight">增加50%的接单率</text>
            </view>
            <view class="upload-examples">
              <text class="upload-link" @click="showExample('图片')"
                >图片示例</text
              >
              <text class="upload-link" @click="showExample('视频')"
                >视频示例</text
              >
            </view>
          </view>
        </view>

        <!-- 工作福利 -->
        <view class="section-card">
          <view class="field-label">
            <text>工作福利</text>
            <text style="font-size: 12px; color: #999; font-weight: 400"
              >可多选</text
            >
          </view>
          <view class="tag-group">
            <text
              class="tag-item"
              :class="{ selected: benefits.includes(tag) }"
              v-for="tag in benefitTags"
              :key="tag"
              @click="toggleTag('benefits', tag)"
              >{{ tag }}</text
            >
          </view>
        </view>

        <!-- 经验要求 -->
        <view class="section-card">
          <text class="field-label">经验要求</text>
          <view class="tag-group">
            <text
              class="tag-item"
              :class="{ selected: exp.includes(tag) }"
              v-for="tag in expTags"
              :key="tag"
              @click="toggleTag('exp', tag)"
              >{{ tag }}</text
            >
          </view>
        </view>

        <!-- 工作要求 -->
        <view class="section-card">
          <view class="field-label">
            <text>工作要求</text>
            <text style="font-size: 12px; color: #999; font-weight: 400"
              >可多选</text
            >
          </view>
          <view class="tag-group">
            <text
              class="tag-item"
              :class="{ selected: requirements.includes(tag) }"
              v-for="tag in requirementTags"
              :key="tag"
              @click="toggleTag('requirements', tag)"
              >{{ tag }}</text
            >
          </view>
        </view>
      </scroll-view>

      <!-- 底部按钮 -->
      <view class="bottom-bar">
        <view class="customer-service" @click="contactService">
          <text class="service-icon">◉</text>
        </view>
        <button class="submit-btn" @click="saveContent">完成</button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      statusBarHeight: 0,
      jobTitle: "",
      jobDesc: "",
      benefits: [],
      exp: [],
      requirements: [],
      benefitTags: [
        "包工作餐",
        "中午包饭",
        "晚上包饭",
        "有空调",
        "有风扇",
        "室内工作",
      ],
      expTags: ["只要熟手", "欢迎新手"],
      requirementTags: [
        "禁止吸烟",
        "认真负责",
        "手脚麻利",
        "提前到场",
        "不磨洋工",
        "禁穿凉鞋短裤",
      ],
    };
  },
  onLoad() {
    try {
      const info =
        typeof uni.getWindowInfo === "function"
          ? uni.getWindowInfo()
          : uni.getSystemInfoSync();
      this.statusBarHeight = Number(info.statusBarHeight || 0);
    } catch (_) {}
    const saved = uni.getStorageSync("taskContent");
    if (saved && typeof saved === "object") {
      this.jobTitle = saved.title || this.jobTitle;
      this.jobDesc = saved.desc || this.jobDesc;
      this.benefits = saved.benefits
        ? String(saved.benefits).split("、").filter(Boolean)
        : this.benefits;
      this.exp = saved.exp
        ? String(saved.exp).split("、").filter(Boolean)
        : this.exp;
      this.requirements = saved.requirements
        ? String(saved.requirements).split("、").filter(Boolean)
        : this.requirements;
    }
  },
  methods: {
    closePage() {
      uni.navigateBack();
    },
    contactService() {
      uni.showToast({ title: "客服", icon: "none" });
    },
    showExample(type) {
      uni.showModal({
        title: `${type}示例`,
        content: `请上传清晰的${type}，突出工作环境、工作内容和现场要求。`,
        showCancel: false,
      });
    },
    uploadVideo() {
      uni.showToast({ title: "选择视频文件", icon: "none" });
    },
    uploadPhoto() {
      uni.chooseImage({ count: 9, success: () => {} });
    },
    toggleTag(type, tag) {
      const arr = this[type];
      const idx = arr.indexOf(tag);
      if (idx >= 0) {
        arr.splice(idx, 1);
      } else {
        arr.push(tag);
      }
    },
    saveContent() {
      const data = {
        title: this.jobTitle,
        desc: this.jobDesc,
        benefits: this.benefits.join("、"),
        exp: this.exp.join("、"),
        requirements: this.requirements.join("、"),
      };
      uni.setStorageSync("taskContent", data);
      uni.$emit("taskContentSaved", data);
      uni.showToast({ title: "保存成功", icon: "success" });
      setTimeout(() => {
        uni.navigateBack();
      }, 1500);
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

.page-bg {
  background: #fff;
  flex: 1;
  width: 100%;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

.modal-header {
  text-align: center;
  padding: 18px 16px 0;
  position: relative;
  flex-shrink: 0;
}

.modal-title {
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

.modal-close {
  position: absolute;
  right: 16px;
  top: 14px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-icon {
  color: #999;
  font-size: 24px;
  line-height: 1;
  font-weight: 300;
}

.modal-content {
  flex: 1;
  width: 100%;
  min-width: 0;
  box-sizing: border-box;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 16px 16px 24px;
  min-height: 0;
}

.section-card {
  background: white;
  width: 100%;
  box-sizing: border-box;
  min-width: 0;
  border-radius: 14px;
  padding: 16px;
  margin-bottom: 14px;
}

.field-label {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-width: 0;
}

.field-label > text {
  min-width: 0;
}

.title-input {
  width: 100%;
  height: 42px;
  line-height: 18px;
  display: block;
  background: #f9f9f9;
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 0 14px;
  font-size: 15px;
  color: #333;
  outline: none;
  margin-bottom: 8px;
  box-sizing: border-box;
}

.title-input::placeholder,
.desc-textarea::placeholder {
  color: #bbb;
}

.title-count {
  text-align: right;
  font-size: 11px;
  color: #bbb;
  margin-bottom: 8px;
  display: block;
}

.desc-textarea {
  width: 100%;
  display: block;
  min-width: 0;
  min-height: 90px;
  background: #f9f9f9;
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 12px 14px;
  font-size: 14px;
  color: #333;
  outline: none;
  line-height: 1.6;
  height: 210px;
  box-sizing: border-box;
}

.desc-count {
  text-align: right;
  font-size: 11px;
  color: #bbb;
  margin-top: 6px;
  display: block;
}

.upload-row {
  display: flex;
  width: 100%;
  min-width: 0;
  gap: 10px;
  margin-top: 12px;
}

.upload-btn {
  flex: 1;
  min-width: 0;
  height: 70px;
  background: #f9f9f9;
  border: 2px dashed #e0e0e0;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.upload-btn:active {
  border-color: #ff6b35;
  background: #fff8e6;
}

.upload-icon {
  color: #ff6b35;
  font-size: 22px;
  line-height: 1;
  margin-bottom: 4px;
}

.upload-label {
  color: #666;
  font-size: 12px;
}

.upload-hint {
  font-size: 12px;
  color: #999;
  margin-top: 10px;
  display: block;
}

.upload-highlight {
  color: #ff6b35;
  font-weight: 500;
}

.upload-examples {
  margin-top: 6px;
}

.upload-link {
  color: #ff6b35;
  font-size: 12px;
  margin-right: 12px;
}

.tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  padding: 8px 16px;
  border-radius: 9999px;
  border: 1px solid #eee;
  background: #f9f9f9;
  font-size: 13px;
  color: #666;
}

.tag-item:active {
  opacity: 0.8;
}

.tag-item.selected {
  background: #fff8e6;
  border-color: #ff6b35;
  color: #ff6b35;
  font-weight: 500;
}

.bottom-bar {
  background: white;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
  padding-bottom: env(safe-area-inset-bottom);
  box-sizing: border-box;
}

.customer-service {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fff8e6, #ffe4b5);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.service-icon {
  color: #ff6b35;
  font-size: 18px;
}

.submit-btn {
  flex: 1;
  height: 48px;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: white;
  border-radius: 9999px;
  font-size: 16px;
  font-weight: 600;
  border: none;
  box-shadow: 0 6px 20px rgba(255, 107, 53, 0.3);
  margin: 0;
  line-height: 48px;
}

.submit-btn::after {
  border: none;
}
</style>
