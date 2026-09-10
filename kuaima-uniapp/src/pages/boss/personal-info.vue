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
      <text class="nav-title">个人信息</text>
      <view class="nav-right">
        <view class="nav-btn" @click="showMenu">
          <text style="font-size:10px;color:#555;">⋯</text>
        </view>
        <view class="nav-divider"></view>
        <view class="nav-btn" @click="minimize">
          <text style="font-size:10px;color:#555;">−</text>
        </view>
        <view class="nav-divider"></view>
        <view class="nav-btn" @click="showMore">
          <text style="font-size:10px;color:#555;">●</text>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="content">
      <!-- 基本信息 -->
      <view class="form-card">
        <view class="form-row" @click="changeAvatar">
          <text class="form-label">头像</text>
          <view class="avatar-wrap">
            <image v-if="userInfo.avatar" class="avatar-img" :src="userInfo.avatar" mode="aspectFill"></image>
            <view v-else class="avatar-img"></view>
          </view>
          <text class="› form-arrow"></text>
        </view>
        <view class="form-row" @click="editName">
          <text class="form-label">姓名</text>
          <text class="form-value" :class="{ placeholder: !userInfo.name }">{{ userInfo.name || '未填写' }}</text>
          <text class="› form-arrow"></text>
        </view>
        <view class="form-row" @click="editPhone">
          <text class="form-label">手机号</text>
          <text class="form-value" :class="{ placeholder: !userInfo.phone }">{{ userInfo.phone || '未绑定' }}</text>
          <text class="› form-arrow"></text>
        </view>
      </view>

      <!-- 企业信息 -->
      <text class="section-title">企业信息</text>
      <view class="form-card">
        <view class="form-row" @click="navigateTo('enterprise-cert')">
          <text class="form-label">企业认证</text>
          <text class="form-value" :class="{ placeholder: userInfo.enterpriseStatusText === '未认证' }">{{ userInfo.enterpriseStatusText }}</text>
          <text class="› form-arrow"></text>
        </view>
        <view class="form-row" @click="editCompanyName">
          <text class="form-label">企业名称</text>
          <text class="form-value" :class="{ placeholder: !userInfo.companyName }">{{ userInfo.companyName || '未填写' }}</text>
          <text class="› form-arrow"></text>
        </view>
        <view class="form-row" @click="editCompanyAddress">
          <text class="form-label">企业地址</text>
          <text class="form-value placeholder">后端暂未支持</text>
          <text class="› form-arrow"></text>
        </view>
      </view>

      <!-- 账号设置 -->
      <text class="section-title">账号设置</text>
      <view class="form-card">
        <view class="form-row" @click="navigateTo('switch-account')">
          <text class="form-label">更换账号</text>
          <text class="form-value">切换账号登录</text>
          <text class="› form-arrow"></text>
        </view>
        <view
          class="form-row"
          :class="{ disabled: logoutLoading }"
          @click="confirmLogout"
        >
          <text class="form-label" style="color:#FF6B35;">{{ logoutLoading ? '退出中…' : '退出登录' }}</text>
          <text class="form-value"></text>
          <text class="› form-arrow"></text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { getCurrentUser, getUser, updateUser } from '@/api/backend'
import { logout } from '@/api/auth'

export default {
  data() {
    return {
      loading: false,
      saving: false,
      logoutLoading: false,
      userInfo: {
        id: '',
        name: '',
        phone: '',
        avatar: '',
        companyName: '',
        enterpriseStatusText: '未认证',
      }
    }
  },
  onShow() {
    this.loadUserInfo()
  },

  methods: {
    getUserId() {
      return uni.getStorageSync('userId') || ''
    },
    async loadUserInfo() {
      const userId = this.getUserId()
      if (!userId) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      this.loading = true
      try {
        const [currentUser, user] = await Promise.all([
          getCurrentUser().catch(() => null),
          getUser(userId).catch(() => null),
        ])
        if (!currentUser && !user) throw new Error('个人信息加载失败')
        const profile = { ...(user || {}), ...(currentUser || {}) }
        const cached = uni.getStorageSync('userInfo') || {}
        const name = profile.nickname || profile.name || profile.realName || cached.nickname || cached.name || ''
        const phone = profile.phone || profile.phoneNumber || cached.phone || cached.phoneNumber || ''
        const enterpriseStatus = normalizeStatus(profile.enterpriseStatus)
        this.userInfo = {
          ...this.userInfo,
          id: profile.id || userId,
          name,
          phone,
          avatar: profile.avatar || profile.avatarUrl || cached.avatar || cached.avatarUrl || '',
          companyName: profile.companyName || '',
          enterpriseStatusText: statusText(enterpriseStatus),
        }
        uni.setStorageSync('userInfo', { ...cached, ...profile, nickname: name, phone })
      } catch (error) {
        uni.showToast({ title: error.message || '个人信息加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    goBack() {
      uni.navigateBack()
    },
    navigateTo(page) {
      uni.navigateTo({ url: `/pages/boss/${page}` })
    },
    showMenu() {
      uni.showToast({ title: '菜单', icon: 'none' })
    },
    minimize() {
      uni.showToast({ title: '最小化', icon: 'none' })
    },
    showMore() {
      uni.showToast({ title: '更多', icon: 'none' })
    },
    changeAvatar() {
      uni.showToast({ title: '暂缺头像上传接口', icon: 'none' })
    },
    editName() {
      uni.showModal({
        title: '修改姓名',
        editable: true,
        placeholderText: this.userInfo.name,
        success: (res) => {
          if (res.confirm && res.content && !this.saving) {
            this.saveUserField({ nickname: res.content.trim() }, 'name', res.content.trim())
          }
        }
      })
    },
    editPhone() {
      uni.showModal({
        title: '修改手机号',
        editable: true,
        placeholderText: this.userInfo.phone || '请输入手机号',
        success: (res) => {
          const phone = String(res.content || '').trim()
          if (res.confirm && phone) {
            if (!/^1\d{10}$/.test(phone)) return uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
            this.saveUserField({ phone }, 'phone', phone)
          }
        }
      })
    },
    async saveUserField(payload, field, value) {
      if (this.saving) return
      this.saving = true
      try {
        await updateUser(this.userInfo.id || this.getUserId(), payload)
        this.userInfo[field] = value
        const cached = uni.getStorageSync('userInfo') || {}
        uni.setStorageSync('userInfo', { ...cached, ...payload })
        uni.showToast({ title: '保存成功', icon: 'success' })
      } catch (error) {
        uni.showToast({ title: error.message || '保存失败', icon: 'none' })
      } finally {
        this.saving = false
      }
    },
    editCompanyName() {
      uni.showModal({
        title: '修改企业名称',
        editable: true,
        placeholderText: this.userInfo.companyName || '请输入企业名称',
        success: (res) => {
          const companyName = String(res.content || '').trim()
          if (res.confirm && companyName) this.saveUserField({ companyName }, 'companyName', companyName)
        }
      })
    },
    editCompanyAddress() {
      uni.showToast({ title: '后端暂未提供企业地址字段', icon: 'none' })
    },
    confirmLogout() {
      if (this.logoutLoading) return
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: async (res) => {
          if (res.confirm) {
            this.logoutLoading = true
            try {
              await logout()
            } finally {
              this.logoutLoading = false
            }
          }
        }
      })
    }
  }
}

function normalizeStatus(value) {
  const status = String(value || '').toUpperCase()
  if (['APPROVED', 'PASSED', '已通过', '通过', '已认证'].includes(status)) return 'APPROVED'
  if (['PENDING', '审核中'].includes(status)) return 'PENDING'
  if (['REJECTED', '已拒绝', '拒绝'].includes(status)) return 'REJECTED'
  return 'UNVERIFIED'
}

function statusText(status) {
  return { APPROVED: '已认证', PENDING: '审核中', REJECTED: '认证未通过', UNVERIFIED: '未认证' }[status] || '未认证'
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

.nav-bar {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #FFF8E6;
  position: relative;
  z-index: 10;
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
  align-items: center;
  background: rgba(255,255,255,0.8);
  backdrop-filter: blur(10px);
  border-radius: 9999px;
  padding: 3px 6px;
  gap: 2px;
}

.nav-btn {
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-divider {
  width: 1px;
  height: 10px;
  background: #ddd;
}

.content {
  flex: 1;
  overflow-y: auto;
}

.form-card {
  background: white;
  margin: 16px 16px 0;
  border-radius: 14px;
  overflow: hidden;
}

.form-row {
  display: flex;
  align-items: center;
  padding: 16px 18px;
  border-bottom: 1px solid #f5f5f5;
}

.form-row:last-child {
  border-bottom: none;
}

.form-label {
  font-size: 15px;
  color: #333;
  font-weight: 500;
  min-width: 70px;
}

.form-value {
  flex: 1;
  text-align: right;
  font-size: 15px;
  color: #333;
  margin-right: 8px;
}

.form-value.placeholder {
  color: #ccc;
}

.form-arrow {
  color: #ccc;
  font-size: 12px;
}

.avatar-wrap {
  display: flex;
  align-items: center;
  flex: 1;
  justify-content: flex-end;
  gap: 8px;
  margin-right: 8px;
}

.avatar-img {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFE4B5, #FFD48A);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.avatar-img::after {
  content: '👤';
  position: absolute;
  font-size: 24px;
}

.section-title {
  font-size: 13px;
  color: #999;
  padding: 20px 20px 10px;
  font-weight: 500;
  display: block;
}
</style>
