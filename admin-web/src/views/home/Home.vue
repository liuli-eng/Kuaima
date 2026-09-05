<template>
  <div class="home-page">
    <!-- Hero 区 -->
    <section class="hero-section">
      <div class="hero-inner">
        <div class="hero-top">
          <div class="hero-badge">
            <i class="fas fa-rocket"></i>
            <span>管理后台原型 v2.0</span>
          </div>
          <div class="hero-login-btn" @click="goLogin">
            <i class="fas fa-user-shield"></i>
            <span>登录系统</span>
          </div>
        </div>
        <h1 class="hero-title">快马日结<br>企业级管理后台</h1>
        <p class="hero-desc">
          一站式日结平台管理解决方案，覆盖用户管理、招工审核、订单结算、内容管理、消息客服等全模块功能，助力平台高效运营。
        </p>
        <div class="hero-stats">
          <div class="hero-stat" v-for="s in stats" :key="s.label">
            <div class="hero-stat-value">{{ s.value }}</div>
            <div class="hero-stat-label">{{ s.label }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 导航分类 -->
    <nav class="nav-tabs">
      <div
        v-for="tab in tabs"
        :key="tab.group"
        class="nav-tab"
        :class="{ active: activeTab === tab.group }"
        @click="filterPreviews(tab.group)"
      >
        {{ tab.label }}
      </div>
    </nav>

    <!-- 登录入口横幅 -->
    <!-- <div class="login-banner-wrap">
      <div class="login-banner">
        <div class="login-banner-glow"></div>
        <div class="login-banner-left">
          <div class="login-banner-badge">
            <i class="fas fa-lock"></i>访问入口
          </div>
          <div class="login-banner-title">管理后台登录</div>
          <div class="login-banner-desc">管理员需登录后才能访问全部功能模块，保障平台数据安全</div>
          <div class="login-banner-btn" @click="goLogin">
            <i class="fas fa-arrow-right-to-bracket"></i>
            <span>登录系统</span>
          </div>
        </div>
        <div class="login-banner-right">
          <iframe src="/admin-preview/admin-login.html" class="login-banner-iframe" scrolling="no"></iframe>
        </div>
      </div>
    </div> -->

    <!-- 页面预览 -->
    <h2 class="section-title">页面预览</h2>
    <p class="section-desc">点击任意卡片可跳转到完整功能页面</p>
    <div class="preview-grid">
      <div
        v-for="card in filteredCards"
        :key="card.id"
        class="preview-card"
        :class="{ 'preview-card-full': card.id === 'logs' }"
        :data-group="card.group"
        @click="openPage(card)"
      >
        <div class="preview-frame">
          <iframe :src="`/admin-preview/${card.id}.html`" loading="lazy" scrolling="no" class="preview-iframe"></iframe>
        </div>
        <div class="preview-info">
          <div>
            <div class="preview-title">{{ card.title }}</div>
            <div class="preview-desc">{{ card.desc }}</div>
          </div>
          <div class="preview-action">
            <span>查看</span>
            <i class="fas fa-arrow-right"></i>
          </div>
        </div>
      </div>
    </div>

    <!-- 核心特色 -->
    <h2 class="section-title">核心特色</h2>
    <p class="section-desc">为运营团队量身打造的管理工具</p>
    <div class="feature-cards">
      <div v-for="f in features" :key="f.title" class="feature-card">
        <div class="feature-icon" :style="f.iconStyle">
          <i :class="f.icon"></i>
        </div>
        <div class="feature-title">{{ f.title }}</div>
        <div class="feature-desc">{{ f.desc }}</div>
      </div>
    </div>

    <!-- 页脚 -->
    <footer class="home-footer">
      <p>© 2024 快马日结管理后台 · 本页面为原型设计，仅供演示</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const stats = [
  { value: '17', label: '核心功能页面' },
  { value: '8', label: '功能模块' },
  { value: '100%', label: '高保真还原' },
  { value: '10+', label: '数据可视化图表' }
]

const tabs = [
  { group: 'all', label: '全部' },
  { group: 'dashboard', label: '数据统计' },
  { group: 'user', label: '用户管理' },
  { group: 'job', label: '招工管理' },
  { group: 'order', label: '订单结算' },
  { group: 'content', label: '内容管理' },
  { group: 'message', label: '消息客服' },
  { group: 'system', label: '系统设置' }
]

const cards = [
  { id: 'dashboard', group: 'dashboard', title: '数据概览', desc: '实时数据、趋势图表', icon: 'fas fa-chart-line', path: '/dashboard' },
  { id: 'workers', group: 'user', title: '零工管理', desc: '零工列表、状态管理', icon: 'fas fa-user', path: '/workers' },
  { id: 'bosses', group: 'user', title: '老板管理', desc: '雇主列表、企业信息', icon: 'fas fa-building', path: '/bosses' },
  { id: 'jobs', group: 'job', title: '招工管理', desc: '招工列表、详情查看', icon: 'fas fa-briefcase', path: '/jobs' },
  { id: 'job-audit', group: 'job', title: '招工审核', desc: '待审核、已通过、已拒绝', icon: 'fas fa-check-circle', path: '/job-audit' },
  { id: 'orders', group: 'order', title: '订单管理', desc: '日结订单、状态追踪', icon: 'fas fa-clipboard-list', path: '/orders' },
  { id: 'settlement', group: 'order', title: '结算管理', desc: '结算记录、明细查看', icon: 'fas fa-coins', path: '/settlement' },
  { id: 'certification', group: 'content', title: '认证审核', desc: '实名认证、企业认证', icon: 'fas fa-id-card', path: '/certification' },
  { id: 'banners', group: 'content', title: 'Banner管理', desc: '首页轮播、活动推广', icon: 'fas fa-image', path: '/banners' },
  { id: 'notices', group: 'content', title: '公告管理', desc: '系统公告、消息推送', icon: 'fas fa-bullhorn', path: '/notices' },
  { id: 'rules', group: 'content', title: '规则管理', desc: '公示/信用/收费/交易/飞单', icon: 'fas fa-book', path: '/rules' },
  { id: 'messages', group: 'message', title: '消息管理', desc: '消息模板、推送记录', icon: 'fas fa-envelope', path: '/messages' },
  { id: 'service', group: 'message', title: '客服管理', desc: '在线客服、会话记录', icon: 'fas fa-headset', path: '/service' },
  { id: 'settings', group: 'system', title: '系统设置', desc: '账号权限、平台规则', icon: 'fas fa-cog', path: '/settings' },
  { id: 'logs', group: 'system', title: '操作日志', desc: '操作记录、日志查询', icon: 'fas fa-file-alt', path: '/logs' }
]

const features = [
  {
    title: '实时数据看板',
    desc: '多维度数据可视化，实时监控平台运营核心指标，辅助决策。',
    icon: 'fas fa-chart-line',
    iconStyle: 'background:linear-gradient(135deg,#FFF0EB,#FFE8DC); color:#FF6B35;'
  },
  {
    title: '高效客服中心',
    desc: '在线客服、会话记录、消息触达，及时响应用户各类诉求。',
    icon: 'fas fa-headset',
    iconStyle: 'background:linear-gradient(135deg,#EFF6FF,#DBEAFE); color:#2563EB;'
  },
  {
    title: '全链路结算',
    desc: '订单结算、资金流向、明细追溯，全链路清晰可查。',
    icon: 'fas fa-coins',
    iconStyle: 'background:linear-gradient(135deg,#ECFDF5,#D1FAE5); color:#10B981;'
  },
  {
    title: '内容运营中心',
    desc: 'Banner管理、公告发布、消息推送，活动运营得心应手。',
    icon: 'fas fa-bullhorn',
    iconStyle: 'background:linear-gradient(135deg,#FFFBEB,#FEF3C7); color:#F59E0B;'
  },
  {
    title: '严格身份认证',
    desc: '实名认证、企业认证审核，确保平台用户真实可信。',
    icon: 'fas fa-user-check',
    iconStyle: 'background:linear-gradient(135deg,#FEF2F2,#FEE2E2); color:#EF4444;'
  },
  {
    title: '精细化运营',
    desc: '用户分层管理、权限分级、数据分析，精准触达目标用户。',
    icon: 'fas fa-users-cog',
    iconStyle: 'background:linear-gradient(135deg,#F3F4F6,#E5E7EB); color:#6B7280;'
  }
]

const activeTab = ref('all')

const filteredCards = computed(() => {
  if (activeTab.value === 'all') return cards
  return cards.filter(c => c.group === activeTab.value)
})

function filterPreviews(group) {
  activeTab.value = group
}

function goLogin() {
  router.push('/login')
}

function openPage(card) {
  // 未登录会被路由守卫拦截到登录页
  router.push(card.path)
}
</script>

<style scoped>
.home-page {
  background: linear-gradient(135deg, #F5F7FA 0%, #E4E8EC 100%);
  min-height: 100vh;
}

/* Hero */
.hero-section {
  background: linear-gradient(135deg, #1E293B 0%, #334155 100%);
  color: #fff;
  padding: 60px 40px 80px;
  position: relative;
  overflow: hidden;
}
.hero-section::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(255, 107, 53, 0.2) 0%, transparent 70%);
}
.hero-inner {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 40px;
  position: relative;
  z-index: 1;
}
.hero-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}
.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 107, 53, 0.2);
  color: #FF8C42;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}
.hero-login-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C5A 100%);
  color: #fff;
  padding: 10px 24px;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(255, 107, 53, 0.3);
  transition: transform 0.2s;
}
.hero-login-btn:hover {
  transform: translateY(-1px);
}
.hero-title {
  font-size: 42px;
  font-weight: 700;
  margin-bottom: 16px;
  line-height: 1.2;
}
.hero-desc {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 32px;
  max-width: 600px;
  line-height: 1.6;
}
.hero-stats {
  display: flex;
  gap: 48px;
}
.hero-stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #FF8C42;
}
.hero-stat-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
}

/* Nav tabs */
.nav-tabs {
  display: flex;
  justify-content: center;
  gap: 8px;
  padding: 20px 40px;
  background: #fff;
  border-bottom: 1px solid #EBEEF5;
  position: sticky;
  top: 0;
  z-index: 100;
}
.nav-tab {
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  color: #909399;
}
.nav-tab:hover {
  background: #F5F7FA;
}
.nav-tab.active {
  background: #FF6B35;
  color: #fff;
}

/* Section */
.section-title {
  font-size: 28px;
  font-weight: 700;
  text-align: center;
  margin: 60px 0 8px;
  color: #303133;
}
.section-desc {
  font-size: 15px;
  color: #909399;
  text-align: center;
  margin-bottom: 40px;
}

/* Login banner */
.login-banner-wrap {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 40px 20px;
}
.login-banner {
  background: linear-gradient(135deg, #1E293B 0%, #334155 100%);
  border-radius: 20px;
  padding: 32px;
  display: flex;
  align-items: center;
  gap: 32px;
  color: #fff;
  position: relative;
  overflow: hidden;
}
.login-banner-glow {
  position: absolute;
  top: -50%;
  right: -10%;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(255, 107, 53, 0.3) 0%, transparent 70%);
  pointer-events: none;
}
.login-banner-left {
  flex: 1;
  position: relative;
  z-index: 1;
}
.login-banner-badge {
  display: inline-block;
  background: rgba(255, 107, 53, 0.2);
  color: #FF8C42;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  margin-bottom: 12px;
}
.login-banner-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 8px;
}
.login-banner-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 20px;
}
.login-banner-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C5A 100%);
  color: #fff;
  padding: 12px 32px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(255, 107, 53, 0.3);
  transition: transform 0.2s;
}
.login-banner-btn:hover {
  transform: translateY(-1px);
}
.login-banner-right {
  width: 420px;
  height: 240px;
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid rgba(255, 255, 255, 0.15);
  flex-shrink: 0;
  position: relative;
  z-index: 1;
  background: #fff;
}
.login-banner-iframe {
  width: 200%;
  height: 200%;
  transform: scale(0.5);
  transform-origin: top left;
  border: none;
  pointer-events: none;
}

/* Preview grid */
.preview-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 28px;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 40px 60px;
}
.preview-card-full {
  grid-column: 1 / -1;
}
.preview-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
}
.preview-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.12);
}
.preview-frame {
  height: 350px;
  overflow: hidden;
  border-bottom: 1px solid #EBEEF5;
  background: #fff;
  position: relative;
}
.preview-iframe {
  width: 200%;
  height: 200%;
  transform: scale(0.5);
  transform-origin: top left;
  border: none;
  pointer-events: none;
}
.preview-info {
  padding: 16px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.preview-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.preview-desc {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}
.preview-action {
  color: #FF6B35;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* Feature cards */
.feature-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 40px 60px;
}
.feature-card {
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  text-align: center;
}
.feature-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin: 0 auto 16px;
}
.feature-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #303133;
}
.feature-desc {
  font-size: 14px;
  color: #909399;
  line-height: 1.6;
}

/* Footer */
.home-footer {
  background: #fff;
  padding: 30px 40px;
  text-align: center;
  color: #C0C4CC;
  font-size: 13px;
  border-top: 1px solid #EBEEF5;
}

@media (max-width: 900px) {
  .preview-grid {
    grid-template-columns: 1fr;
  }
  .feature-cards {
    grid-template-columns: 1fr;
  }
  .hero-stats {
    flex-wrap: wrap;
    gap: 24px;
  }
  .login-banner {
    flex-direction: column;
  }
  .login-banner-right {
    width: 100%;
    height: 200px;
  }
}
</style>
