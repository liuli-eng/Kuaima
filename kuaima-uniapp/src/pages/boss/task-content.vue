<template>
  <view class="container">
    <!-- 状态栏 -->
    <view class="status-bar">
      <text>19:53</text>
      <view class="status-icons">
        <text>📶</text>
        <text>📡</text>
        <text>🔋</text>
      </view>
    </view>

    <view class="page-bg">
      <!-- 头部 -->
      <view class="modal-header">
        <text class="modal-title">任务要求</text>
        <view class="modal-close" @click="closePage">
          <text style="font-size:18px;color:#999;">✕</text>
        </view>
      </view>

      <scroll-view scroll-y class="modal-content">
        <!-- 招工标题 -->
        <view class="section-card">
          <text class="field-label">招工标题</text>
          <input class="title-input" v-model="jobTitle" maxlength="20" placeholder="请输入招工标题" />
          <text class="title-count">{{ jobTitle.length }}/20</text>
        </view>

        <!-- 详情描述 -->
        <view class="section-card">
          <view class="field-label">
            <text>详情描述 <text style="color:#999;font-weight:400;">(非必填)</text></text>
          </view>
          <textarea class="desc-textarea" v-model="jobDesc" placeholder="补充工作要求或详细内容，请勿填写手机号或微信号" maxlength="200"></textarea>
          <text class="desc-count">{{ jobDesc.length }}/200</text>

          <view class="upload-row">
            <view class="upload-btn" @click="uploadVideo">
              <text style="font-size:22px;color:#FF6B35;margin-bottom:4px;">🎬</text>
              <text style="font-size:12px;color:#666;">上传视频</text>
            </view>
            <view class="upload-btn" @click="uploadPhoto">
              <text style="font-size:22px;color:#FF6B35;margin-bottom:4px;">📷</text>
              <text style="font-size:12px;color:#666;">上传照片</text>
            </view>
          </view>
          <text class="upload-hint">
            上传有工作内容的视频或照片，<text style="color:#FF6B35;font-weight:500;">增加50%的接单率</text>
          </text>
        </view>

        <!-- 工作福利 -->
        <view class="section-card">
          <view class="field-label">
            <text>工作福利</text>
            <text style="font-size:12px;color:#999;font-weight:400;">可多选</text>
          </view>
          <view class="tag-group">
            <text 
              class="tag-item" 
              :class="{ selected: benefits.includes(tag) }"
              v-for="tag in benefitTags" 
              :key="tag"
              @click="toggleTag('benefits', tag)"
            >{{ tag }}</text>
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
            >{{ tag }}</text>
          </view>
        </view>

        <!-- 工作要求 -->
        <view class="section-card">
          <view class="field-label">
            <text>工作要求</text>
            <text style="font-size:12px;color:#999;font-weight:400;">可多选</text>
          </view>
          <view class="tag-group">
            <text 
              class="tag-item" 
              :class="{ selected: requirements.includes(tag) }"
              v-for="tag in requirementTags" 
              :key="tag"
              @click="toggleTag('requirements', tag)"
            >{{ tag }}</text>
          </view>
        </view>
      </scroll-view>

      <!-- 底部按钮 -->
      <view class="bottom-bar">
        <view class="customer-service" @click="contactService">
          <text style="font-size:18px;color:#FF6B35;">🎧</text>
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
      jobTitle: '电子厂普工',
      jobDesc: '',
      benefits: ['室内工作'],
      exp: ['欢迎新手'],
      requirements: [],
      benefitTags: ['包工作餐', '中午包饭', '晚上包饭', '有空调', '有风扇', '室内工作'],
      expTags: ['只要熟手', '欢迎新手'],
      requirementTags: ['禁止吸烟', '认真负责', '手脚麻利', '提前到场', '不磨洋工', '禁穿凉鞋短裤']
    }
  },
  methods: {
    closePage() {
      uni.navigateBack()
    },
    contactService() {
      uni.showToast({ title: '客服', icon: 'none' })
    },
    uploadVideo() {
      uni.showToast({ title: '选择视频文件', icon: 'none' })
    },
    uploadPhoto() {
      uni.chooseImage({ count: 9, success: () => {} })
    },
    toggleTag(type, tag) {
      const arr = this[type]
      const idx = arr.indexOf(tag)
      if (idx >= 0) {
        arr.splice(idx, 1)
      } else {
        arr.push(tag)
      }
    },
    saveContent() {
      const data = {
        title: this.jobTitle,
        desc: this.jobDesc,
        benefits: this.benefits.join('、'),
        exp: this.exp.join('、'),
        requirements: this.requirements.join('、')
      }
      uni.$emit('taskContentSaved', data)
      uni.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => { uni.navigateBack() }, 1500)
    }
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

.modal-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.section-card {
  background: white;
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
}

.title-input {
  width: 100%;
  background: #f9f9f9;
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 12px 14px;
  font-size: 15px;
  color: #333;
  outline: none;
  margin-bottom: 8px;
  box-sizing: border-box;
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
  min-height: 90px;
  background: #f9f9f9;
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 12px 14px;
  font-size: 14px;
  color: #333;
  outline: none;
  line-height: 1.6;
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
  gap: 10px;
  margin-top: 12px;
}

.upload-btn {
  flex: 1;
  height: 70px;
  background: #f9f9f9;
  border: 2px dashed #e0e0e0;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.upload-hint {
  font-size: 12px;
  color: #999;
  margin-top: 10px;
  display: block;
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

.tag-item.selected {
  background: #FFF8E6;
  border-color: #FF6B35;
  color: #FF6B35;
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
  padding-bottom: 20px;
}

.customer-service {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFF8E6, #FFE4B5);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.submit-btn {
  flex: 1;
  height: 48px;
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  color: white;
  border-radius: 9999px;
  font-size: 16px;
  font-weight: 600;
  border: none;
  box-shadow: 0 6px 20px rgba(255, 107, 53, 0.3);
}
</style>
