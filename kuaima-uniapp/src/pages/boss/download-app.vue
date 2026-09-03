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
      <text class="nav-title">下载APP</text>
      <view class="nav-close" @click="goBack">
        <text>✕</text>
      </view>
    </view>

    <scroll-view scroll-y class="content">
      <!-- 方式一 -->
      <text class="section-title">方式一：<text class="highlight">应用市场搜索【快马日结】下载</text></text>

      <view class="method-card">
        <view class="search-bar">
          <text style="color:#999;font-size:14px;">🔍</text>
          <input type="text" value="快马日结" disabled style="flex:1;border:none;background:transparent;outline:none;font-size:14px;" />
          <text style="color:#999;font-size:14px;">取消</text>
        </view>

        <view class="app-item">
          <view class="app-icon">
            <text style="font-size:22px;color:white;">💼</text>
          </view>
          <view class="app-info">
            <text class="app-name">快马日结-临时工日结...</text>
            <text class="app-desc">仓库工厂建筑工物流招工找...</text>
          </view>
          <view class="app-download" @click="download">
            <text style="font-size:20px;color:#FF6B35;">☁</text>
          </view>
        </view>

        <view class="app-meta">
          <text><text style="color:#FFB800;">⭐</text> 603</text>
          <text><text style="color:#FFB800;">📍</text> Shanghai Anyoux...</text>
          <text><text style="color:#FFB800;">💼</text> 商务</text>
        </view>

        <view class="screenshots">
          <view class="screenshot">预览图1</view>
          <view class="screenshot">预览图2</view>
          <view class="screenshot">预览图3</view>
        </view>
      </view>

      <!-- 方式二 -->
      <text class="section-title">方式二：<text class="highlight">关注公众号，下载app</text></text>

      <view class="method-card">
        <view class="qr-section">
          <view class="qr-box">
            <view class="qr-code">
              <!-- 简化的二维码占位 -->
              <image src="/static/qr-placeholder.png" mode="aspectFit" style="width:110px;height:110px;"></image>
            </view>
            <view class="qr-caption">
              <text class="step">1、长按识别二维码</text>
              <text>关注公众号</text>
            </view>
          </view>

          <view class="wechat-bubble">
            <view class="wechat-header">
              <view class="wechat-avatar">
                <text style="font-size:12px;color:white;">💬</text>
              </view>
              <text class="wechat-name">快马日结</text>
            </view>
            <view class="wechat-text">
              <text>欢迎工友关注快马日结。</text>
              <text>苹果手机取积分请下载APP后进行操作。</text>
              <text class="highlight">点击下载APP ↓↓↓</text>
            </view>
            <view class="wechat-footer">
              <text><text style="margin-right:2px;">➕</text>招工求职</text>
              <text><text style="margin-right:2px;">👥</text>临时工</text>
              <text><text style="margin-right:2px;">⬇</text>下载APP</text>
            </view>
            <text class="step-text">
              <text class="step-num">2、</text>点击下载APP
            </text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {}
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    download() {
      uni.showToast({ title: '开始下载...', icon: 'loading' })
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
  background: #FFF8E6;
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
  justify-content: center;
  padding: 0 16px;
  background: #FFF8E6;
  position: relative;
}

.nav-close {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
}

.content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 20px 0 12px;
}

.highlight {
  color: #FF6B35;
}

.method-card {
  background: white;
  border-radius: 14px;
  padding: 16px;
  margin-bottom: 16px;
}

.search-bar {
  background: #f5f5f5;
  border-radius: 10px;
  padding: 10px 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}

.app-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.app-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.app-info {
  flex: 1;
}

.app-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.app-desc {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.app-download {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.app-meta {
  display: flex;
  gap: 10px;
  margin-top: 10px;
  font-size: 11px;
  color: #999;
  flex-wrap: wrap;
}

.screenshots {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.screenshot {
  flex: 1;
  height: 50px;
  background: linear-gradient(135deg, #FFE4B5, #FFD48A);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: #999;
}

.qr-section {
  display: flex;
  gap: 12px;
}

.qr-box {
  width: 130px;
  height: 160px;
  background: white;
  border-radius: 12px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.qr-code {
  width: 110px;
  height: 110px;
  margin-bottom: 8px;
}

.qr-caption {
  font-size: 10px;
  color: #666;
  text-align: center;
  line-height: 1.3;
}

.step {
  color: #FF6B35;
  font-weight: 600;
}

.wechat-bubble {
  flex: 1;
  background: white;
  border-radius: 12px;
  padding: 12px;
  position: relative;
  min-height: 160px;
}

.wechat-bubble::before {
  content: '';
  position: absolute;
  left: -8px;
  top: 30px;
  border: 8px solid transparent;
  border-right-color: white;
}

.wechat-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
}

.wechat-avatar {
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.wechat-name {
  font-size: 12px;
  font-weight: 600;
  color: #333;
}

.wechat-text {
  font-size: 11px;
  color: #666;
  line-height: 1.5;
  display: flex;
  flex-direction: column;
}

.wechat-footer {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  font-size: 10px;
  color:  #999;
}

.step-text {
  text-align: center;
  font-size: 12px;
  color: #666;
  margin-top: 10px;
}

.step-num {
  color: #FF6B35;
  font-weight: 600;
}
</style>
