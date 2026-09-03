<template>
  <view class="container">
    <!-- 状态栏 -->
    <view class="status-bar">
      <text>19:53</text>
      <view class="status-icons">
        <text>📶</text>
        <text>🔋</text>
      </view>
    </view>

    <view class="page-bg">
      <!-- 导航栏 -->
      <view class="nav-bar">
        <view class="nav-back" @click="goBack">
          <text>←</text>
        </view>
        <text class="nav-title">联系电话设置</text>
        <view class="nav-placeholder"></view>
      </view>

      <scroll-view scroll-y class="scroll-area">
        <!-- 主要联系电话 -->
        <view class="section-card">
          <text class="section-title">主要联系电话</text>
          <view class="phone-item">
            <view class="phone-icon primary">
              <text style="font-size:32rpx;">📞</text>
            </view>
            <view class="phone-info">
              <text class="phone-number">13698756321 <text class="phone-tag">主</text></text>
              <text class="phone-desc">零工电话报名、问路等</text>
            </view>
          </view>
        </view>

        <!-- 备用联系电话 -->
        <view class="section-card">
          <text class="section-title">备用联系电话 <text style="color:#FF4D4F;">*</text>（最多添加3个）</text>
          <view class="phone-item" v-for="(item, index) in phoneList" :key="index">
            <view class="phone-icon">
              <text style="font-size:32rpx;">📞</text>
            </view>
            <view class="phone-info">
              <text class="phone-number">{{ item.phone }}</text>
              <text class="phone-desc">备用联系人：{{ item.name }}</text>
            </view>
            <view class="phone-actions">
              <button @click="editPhone(index)">编辑</button>
              <view class="divider"></view>
              <button class="danger" @click="deletePhone(index)">删除</button>
            </view>
          </view>
          <view class="add-btn" v-if="phoneList.length < 3" @click="openAddModal">
            <text>+</text>
            <text>添加备用联系电话</text>
          </view>
        </view>

        <!-- 提示信息 -->
        <view class="tip-box">
          <text style="color:#FF6B35;margin-top:4rpx;">ℹ</text>
          <text>备用电话用于零工无法联系时的补充沟通，确保招工信息传达顺畅。</text>
        </view>
      </scroll-view>

      <!-- 底部按钮 -->
      <view class="bottom-btn">
        <button class="btn-save" @click="saveAndReturn">保存设置</button>
      </view>
    </view>

    <!-- 添加/编辑弹窗 -->
    <view class="modal" v-if="showPhoneModal">
      <view class="modal-content">
        <text class="modal-title">{{ editingIndex >= 0 ? '编辑联系电话' : '添加备用联系电话' }}</text>
        <view class="form-group">
          <text>联系人姓名</text>
          <input type="text" placeholder="请输入姓名" v-model="formData.name" />
        </view>
        <view class="form-group">
          <text>手机号码</text>
          <input type="tel" placeholder="请输入11位手机号" maxlength="11" v-model="formData.phone" />
        </view>
        <view class="modal-actions">
          <button class="btn-cancel" @click="closeModal">取消</button>
          <button class="btn-confirm" @click="savePhone">保存</button>
        </view>
      </view>
    </view>

    <!-- 删除确认弹窗 -->
    <view class="modal" v-if="showDeleteModal">
      <view class="modal-content">
        <text class="modal-title">确认删除</text>
        <text style="text-align:center;font-size:28rpx;color:#666;margin-bottom:32rpx;display:block;">
          删除后该备用联系人将无法接收零工相关通知
        </text>
        <view class="modal-actions">
          <button class="btn-cancel" @click="closeDeleteModal">取消</button>
          <button class="btn-confirm" style="background:linear-gradient(135deg,#FF6B35,#FF4D4F);" @click="confirmDelete">确认删除</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      phoneList: [
        { name: '张先生', phone: '13888888888' },
        { name: '李女士', phone: '13666666666' }
      ],
      editingIndex: -1,
      deletingIndex: -1,
      showPhoneModal: false,
      showDeleteModal: false,
      formData: {
        name: '',
        phone: ''
      }
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    openAddModal() {
      this.editingIndex = -1
      this.formData = { name: '', phone: '' }
      this.showPhoneModal = true
    },
    editPhone(index) {
      this.editingIndex = index
      const p = this.phoneList[index]
      this.formData = { name: p.name, phone: p.phone }
      this.showPhoneModal = true
    },
    closeModal() {
      this.showPhoneModal = false
    },
    savePhone() {
      if (!this.formData.name.trim()) {
        uni.showToast({ title: '请输入联系人姓名', icon: 'none' })
        return
      }
      if (!/^1\d{10}$/.test(this.formData.phone.trim())) {
        uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
        return
      }

      if (this.editingIndex >= 0) {
        this.phoneList[this.editingIndex] = { ...this.formData }
      } else {
        if (this.phoneList.length >= 3) {
          uni.showToast({ title: '最多添加3个备用联系人', icon: 'none' })
          return
        }
        this.phoneList.push({ ...this.formData })
      }

      this.closeModal()
    },
    deletePhone(index) {
      this.deletingIndex = index
      this.showDeleteModal = true
    },
    closeDeleteModal() {
      this.showDeleteModal = false
    },
    confirmDelete() {
      if (this.deletingIndex >= 0) {
        this.phoneList.splice(this.deletingIndex, 1)
      }
      this.closeDeleteModal()
    },
    saveAndReturn() {
      uni.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 500)
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
  height: 94rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 56rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  background: #fff;
}

.status-icons {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.page-bg {
  background: #fff;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.nav-bar {
  padding: 28rpx 32rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  flex-shrink: 0;
}

.nav-back {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #333;
  font-size: 40rpx;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #333;
}

.nav-placeholder {
  width: 64rpx;
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
  background: #F7F8FA;
}

.section-card {
  background: #fff;
  margin: 24rpx;
  border-radius: 24rpx;
  padding: 32rpx;
}

.section-title {
  font-size: 26rpx;
  color: #999;
  margin-bottom: 20rpx;
  font-weight: 500;
  display: block;
}

.phone-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 2rpx solid #f5f5f5;
  gap: 24rpx;
}

.phone-item:last-child {
  border-bottom: none;
}

.phone-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  background: #EBF5FF;
  color: #1E88E5;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.phone-icon.primary {
  background: #FFF3ED;
  color: #FF6B35;
}

.phone-info {
  flex: 1;
}

.phone-number {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.phone-tag {
  display: inline-block;
  font-size: 22rpx;
  padding: 2rpx 12rpx;
  background: #FFF3ED;
  color: #FF6B35;
  border-radius: 6rpx;
  margin-left: 12rpx;
}

.phone-desc {
  font-size: 24rpx;
  color: #999;
  margin-top: 4rpx;
}

.phone-actions {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.phone-actions button {
  font-size: 24rpx;
  color: #1E88E5;
  background: none;
  border: none;
  padding: 0;
}

.phone-actions button.danger {
  color: #FF4D4F;
}

.phone-actions .divider {
  width: 2rpx;
  height: 24rpx;
  background: #e0e0e0;
}

.add-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  padding: 28rpx;
  background: #fff;
  margin: 24rpx;
  border-radius: 24rpx;
  border: 2rpx dashed #ccc;
  color: #1E88E5;
  font-size: 30rpx;
  font-weight: 500;
}

.tip-box {
  background: #FFF8E6;
  margin: 0 24rpx 24rpx;
  border-radius: 24rpx;
  padding: 24rpx 28rpx;
  display: flex;
  gap: 16rpx;
}

.tip-box text {
  font-size: 24rpx;
  color: #666;
  line-height: 1.6;
}

.bottom-btn {
  flex-shrink: 0;
  padding: 24rpx 32rpx 68rpx;
  background: #fff;
}

.btn-save {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: #fff;
  border: none;
  border-radius: 48rpx;
  font-size: 32rpx;
  font-weight: 600;
  box-shadow: 0 8rpx 24rpx rgba(255, 165, 0, 0.3);
}

.modal {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal-content {
  width: 640rpx;
  background: #fff;
  border-radius: 32rpx;
  padding: 40rpx;
}

.modal-title {
  font-size: 34rpx;
  font-weight: 600;
  text-align: center;
  margin-bottom: 32rpx;
  display: block;
}

.form-group {
  margin-bottom: 28rpx;
}

.form-group text {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 12rpx;
}

.form-group input {
  width: 100%;
  padding: 20rpx 24rpx;
  border: 2rpx solid #ddd;
  border-radius: 16rpx;
  font-size: 30rpx;
  outline: none;
  box-sizing: border-box;
}

.form-group input:focus {
  border-color: #1E88E5;
}

.modal-actions {
  display: flex;
  gap: 20rpx;
  margin-top: 16rpx;
}

.modal-actions button {
  flex: 1;
  height: 84rpx;
  border-radius: 42rpx;
  font-size: 30rpx;
  font-weight: 500;
  border: none;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-confirm {
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: #fff;
}
</style>
