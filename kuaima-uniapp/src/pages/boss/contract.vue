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
      <text class="nav-title">电子合同</text>
      <view class="nav-right">
        <text>…</text>
      </view>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <!-- 合同状态 -->
      <view class="contract-header">
        <text class="contract-name">2026年8月日结劳务协议</text>
        <view class="status-tag">
          <view class="status-dot"></view>
          <text>已签署</text>
        </view>
      </view>

      <!-- 合同内容 -->
      <view class="contract-page">
        <view class="contract-content">
          <text class="contract-title">日结劳务协议</text>
          <text class="contract-subtitle">合同编号：KM202608001234</text>

          <view class="contract-clause">
            <text class="clause-title">甲方（雇主）：</text>
            <text>快马日结科技有限公司</text>
            <text class="clause-title" style="margin-top:8px;">乙方（劳动者）：</text>
            <text>2026年8月入职全部日结工人</text>
          </view>

          <view class="contract-clause">
            <text class="clause-title">第一条 工作内容</text>
            <text>甲方根据业务需要，安排乙方从事日结临时工作，具体岗位包括但不限于：电商分拣、餐饮服务、快递搬运、冷库分拣、装配等。乙方应服从甲方合理的工作安排。</text>
          </view>

          <view class="contract-clause">
            <text class="clause-title">第二条 工作时间与地点</text>
            <text>1. 工作时间：以每日实际排班为准，单次工作时长不超过12小时。</text>
            <text>2. 工作地点：甲方指定的仓库、门店、工地等经营场所。</text>
          </view>

          <view class="contract-clause">
            <text class="clause-title">第三条 劳动报酬</text>
            <text>1. 日结薪酬按岗位计价，具体标准为：分拣打包工¥180/天、餐饮服务员¥150/天、快递搬运工¥200/天、冷库分拣员¥220/天、装配工¥250/天。</text>
            <text>2. 报酬结算周期为自然月，次月5日前完成上月结算。</text>
            <text>3. 结算金额：¥12,560.00（已结清）。</text>
          </view>

          <view class="contract-clause">
            <text class="clause-title">第四条 劳动保护</text>
            <text>甲方应为乙方提供符合国家规定的劳动安全卫生条件和必要的劳动防护用品。乙方在工作过程中发生的意外伤害，由甲方依法承担相应责任。</text>
          </view>

          <view class="contract-clause">
            <text class="clause-title">第五条 合同期限</text>
            <text>本协议自2026年8月1日起生效，至2026年8月31日止。期满后双方可协商续约。</text>
          </view>

          <view class="contract-clause">
            <text class="clause-title">第六条 其他约定</text>
            <text>1. 乙方应遵守甲方各项规章制度，服从管理。</text>
            <text>2. 本协议一式两份，甲乙双方各执一份，具有同等法律效力。</text>
            <text>3. 因本协议产生的争议，双方协商解决；协商不成的，可向甲方所在地人民法院提起诉讼。</text>
          </view>

          <view class="contract-sign">
            <view class="sign-area">
              <text class="sign-label">甲方（盖章）</text>
              <view class="sign-line"></view>
              <text class="sign-name">快马日结科技有限公司</text>
              <text class="sign-date">2026-08-31</text>
            </view>
            <view class="sign-area">
              <text class="sign-label">乙方（签字）</text>
              <view class="sign-line"></view>
              <text class="sign-name">（批量签署）</text>
              <text class="sign-date">2026-08-31</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 合同信息 -->
      <view class="contract-meta">
        <text class="meta-title">合同信息</text>
        <view class="meta-row">
          <text class="meta-label">合同编号</text>
          <text class="meta-value">KM202608001234</text>
        </view>
        <view class="meta-row">
          <text class="meta-label">合同类型</text>
          <text class="meta-value">日结劳务协议</text>
        </view>
        <view class="meta-row">
          <text class="meta-label">生效日期</text>
          <text class="meta-value">2026-08-01</text>
        </view>
        <view class="meta-row">
          <text class="meta-label">终止日期</text>
          <text class="meta-value">2026-08-31</text>
        </view>
        <view class="meta-row">
          <text class="meta-label">结算金额</text>
          <text class="meta-value highlight">¥12,560.00</text>
        </view>
        <view class="meta-row">
          <text class="meta-label">签署方式</text>
          <text class="meta-value">电子签名</text>
        </view>
        <view class="meta-row">
          <text class="meta-label">签署时间</text>
          <text class="meta-value">2026-08-31 18:00</text>
        </view>
        <view class="meta-row">
          <text class="meta-label">合同状态</text>
          <text class="meta-value" style="color:#4CAF50;">已生效</text>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="action-area">
        <view class="download-btn" @click="downloadContract">
          <text>⬇</text>
          <text>下载PDF</text>
        </view>
        <view class="share-btn" @click="shareContract">
          <text>📤</text>
          <text>分享合同</text>
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
    downloadContract() {
      uni.showToast({ title: '合同PDF下载中...', icon: 'loading' })
    },
    shareContract() {
      uni.showToast({ title: '分享合同链接已复制到剪贴板', icon: 'success' })
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

.contract-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
}

.contract-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 10px;
  background: #E8F5E9;
  color: #4CAF50;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #4CAF50;
}

.contract-page {
  background: #fff;
  margin: 0 16px 12px;
  border-radius: 12px;
  padding: 24px 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  border: 1px solid #e8e8e8;
  position: relative;
}

.contract-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: repeating-linear-gradient(0deg, transparent, transparent 27px, #e8e8e8 27px, #e8e8e8 28px);
  border-radius: 11px;
  pointer-events: none;
  z-index: 0;
}

.contract-content {
  position: relative;
  z-index: 1;
}

.contract-title {
  text-align: center;
  font-size: 18px;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
}

.contract-subtitle {
  text-align: center;
  font-size: 13px;
  color: #999;
  margin-bottom: 20px;
}

.contract-clause {
  font-size: 12px;
  color: #555;
  line-height: 28px;
  text-align: justify;
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
}

.clause-title {
  color: #333;
  font-weight: 600;
}

.contract-sign {
  display: flex;
  justify-content: space-between;
  margin-top: 24px;
}

.sign-area {
  text-align: center;
}

.sign-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.sign-line {
  width: 100px;
  border-bottom: 2px solid #333;
  margin-bottom: 6px;
}

.sign-name {
  font-size: 13px;
  color: #333;
  font-weight: 600;
}

.sign-date {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
}

.contract-meta {
  background: #fff;
  margin: 12px 16px;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.meta-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  padding: 6px 0;
}

.meta-label {
  color: #999;
}

.meta-value {
  color: #333;
  font-weight: 500;
}

.meta-value.highlight {
  color: #FF6B35;
  font-weight: 600;
}

.action-area {
  padding: 16px;
  display: flex;
  gap: 12px;
}

.download-btn {
  flex: 1;
  padding: 12px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.share-btn {
  flex: 1;
  padding: 12px;
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
</style>
