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
      <!-- 实名认证Banner -->
      <view class="auth-banner">
        <view class="auth-left">
          <view class="auth-icon">
            <text style="font-size:20px;color:white;">🛡</text>
          </view>
          <view>
            <view class="auth-title">
              <text style="color:#FFA500;font-size:14px;">🛡</text>
              <text style="margin-left:4px;">实名认证</text>
            </view>
            <text class="auth-desc">完成认证，找活找人更容易！</text>
          </view>
        </view>
        <view class="auth-btn" @click="navigateTo('realname')">立即实名 ›</view>
      </view>

      <!-- 基本信息 -->
      <view class="form-card">
        <view class="form-row" @click="changeAvatar">
          <text class="form-label">头像</text>
          <view class="avatar-wrap">
            <view class="avatar-img"></view>
          </view>
          <text class="› form-arrow"></text>
        </view>
        <view class="form-row" @click="editName">
          <text class="form-label">姓名</text>
          <text class="form-value">{{ userInfo.name }}</text>
          <text class="› form-arrow"></text>
        </view>
        <view class="form-row" @click="editPhone">
          <text class="form-label">手机号</text>
          <text class="form-value">{{ userInfo.phone }}</text>
          <text class="› form-arrow"></text>
        </view>
      </view>

      <!-- 企业信息 -->
      <text class="section-title">企业信息</text>
      <view class="form-card">
        <view class="form-row" @click="navigateTo('enterprise-cert')">
          <text class="form-label">企业认证</text>
          <text class="form-value placeholder">未认证</text>
          <text class="› form-arrow"></text>
        </view>
        <view class="form-row" @click="editCompanyName">
          <text class="form-label">企业名称</text>
          <text class="form-value placeholder">未填写</text>
          <text class="› form-arrow"></text>
        </view>
        <view class="form-row" @click="editCompanyAddress">
          <text class="form-label">企业地址</text>
          <text class="form-value placeholder">未填写</text>
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
        <view class="form-row" @click="logout">
          <text class="form-label" style="color:#FF6B35;">退出登录</text>
          <text class="form-value"></text>
          <text class="› form-arrow"></text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      userInfo: {
        name: '晴时见禾',
        phone: '152******53'
      }
    }
  },
  methods: {
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
      uni.chooseImage({
        count: 1,
        success: () => {
          uni.showToast({ title: '头像更换成功', icon: 'success' })
        }
      })
    },
    editName() {
      uni.showModal({
        title: '修改姓名',
        editable: true,
        placeholderText: this.userInfo.name,
        success: (res) => {
          if (res.confirm && res.content) {
            this.userInfo.name = res.content
          }
        }
      })
    },
    editPhone() {
      uni.showToast({ title: '跳转到手机号修改页面', icon: 'none' })
    },
    editCompanyName() {
      uni.showToast({ title: '编辑企业名称', icon: 'none' })
    },
    editCompanyAddress() {
      uni.showToast({ title: '编辑企业地址', icon: 'none' })
    },
    logout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            uni.reLaunch({ url: '/pages/boss/home' })
          }
        }
      })
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

.auth-banner {
  background: linear-gradient(135deg, #FFF8E6, #FFE4B5);
  padding: 18px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.auth-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.auth-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(255, 165, 0, 0.25);
}

.auth-title {
  font-size: 16px;
  font-weight: 700;
  color: #333;
  display: flex;
  align-items: center;
}

.auth-desc {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.auth-btn {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  color: white;
  padding: 8px 16px;
  border-radius: 9999px;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
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
