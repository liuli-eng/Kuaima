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

    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text>←</text>
      </view>
      <text class="nav-title">企业自招认证</text>
      <view class="nav-right">
        <text>⋯</text>
      </view>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <!-- 认证类型 -->
      <view class="cert-type">
        <text class="cert-type-label">认证类型</text>
        <text class="cert-type-value">企业自招（仅限本公司招工）</text>
      </view>

      <!-- 企业信息 -->
      <view class="form-card">
        <text class="form-title">企业信息</text>
        <view class="form-item">
          <text class="form-label">企业名称</text>
          <input class="form-input" placeholder="请输入营业执照上的全称" v-model="companyName" />
        </view>
        <view class="form-item">
          <text class="form-label">统一社会信用代码</text>
          <input class="form-input" placeholder="请输入18位信用代码" v-model="creditCode" />
        </view>
        <view class="form-item">
          <text class="form-label">法定代表人</text>
          <input class="form-input" placeholder="请输入法人姓名" v-model="legalPerson" />
        </view>
        <view class="form-item">
          <text class="form-label">联系电话</text>
          <input class="form-input" placeholder="请输入联系电话" v-model="contactPhone" />
        </view>
      </view>

      <!-- 上传营业执照 -->
      <view class="form-card">
        <text class="form-title">上传营业执照</text>
        <view class="upload-area" @click="uploadLicense">
          <view class="upload-icon">
            <text style="font-size:36px;color:#FF6B35;">☁</text>
          </view>
          <text class="upload-text">点击上传营业执照</text>
          <text class="upload-hint">请上传原件或加盖公章的复印件，支持JPG/PNG格式，不超过5M</text>
        </view>
      </view>

      <!-- 同意协议 -->
      <view class="agree-row">
        <view class="agree-checkbox" :class="{ checked: agreed }" @click="toggleAgree"></view>
        <text class="agree-text">
          我已阅读并同意<text class="agree-link" @click.stop="showAgreement('service')">《企业认证服务协议》</text>和<text class="agree-link" @click.stop="showAgreement('rule')">《信息发布规则》</text>，承诺所提交的材料真实有效。
        </text>
      </view>

      <!-- 提交按钮 -->
      <button class="submit-btn" :disabled="!agreed" @click="submitForm">提交认证</button>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      companyName: '',
      creditCode: '',
      legalPerson: '',
      contactPhone: '',
      agreed: false
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    toggleAgree() {
      this.agreed = !this.agreed
    },
    uploadLicense() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          uni.showToast({ title: '上传成功', icon: 'success' })
        }
      })
    },
    showAgreement(type) {
      if (type === 'service') {
        uni.showToast({ title: '《企业认证服务协议》', icon: 'none' })
      } else {
        uni.showToast({ title: '《信息发布规则》', icon: 'none' })
      }
    },
    submitForm() {
      if (!this.companyName.trim()) {
        uni.showToast({ title: '请填写企业名称', icon: 'none' })
        return
      }
      uni.showToast({ title: '提交成功！\n\n审核周期约1-3个工作日，请耐心等待。', icon: 'success', duration: 3000 })
    }
  }
}
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

.scroll-area {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 20px;
}

.cert-type {
  background: #fff;
  margin: 12px 16px;
  border-radius: 12px;
  padding: 16px;
}

.cert-type-label {
  font-size: 13px;
  color: #999;
}

.cert-type-value {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-top: 4px;
}

.form-card {
  background: #fff;
  margin: 12px 16px;
  border-radius: 12px;
  padding: 16px;
}

.form-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}

.form-item:last-child {
  border-bottom: none;
}

.form-label {
  font-size: 14px;
  color: #333;
  width: 130px;
}

.form-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  color: #666;
}

.form-input::placeholder {
  color: #ccc;
}

.upload-area {
  border: 1px dashed #FF6B35;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  background: #FFF8F5;
}

.upload-text {
  font-size: 14px;
  color: #333;
  margin-top: 8px;
  font-weight: 500;
}

.upload-hint {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.agree-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 16px;
}

.agree-checkbox {
  width: 16px;
  height: 16px;
  border: 1.5px solid #ddd;
  border-radius: 3px;
  flex-shrink: 0;
  margin-top: 2px;
}

.agree-checkbox.checked {
  background: #FF6B35;
  border-color: #FF6B35;
}

.agree-text {
  font-size: 12px;
  color: #666;
  line-height: 1.5;
}

.agree-link {
  color: #FF6B35;
}

.submit-btn {
  width: calc(100% - 32px);
  padding: 14px;
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  color: #fff;
  border: none;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 600;
  margin: 20px 16px 30px;
}

.submit-btn:disabled {
  opacity: 0.5;
}
</style>
