<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">账户设置</h1>
      <p class="page-desc">管理账户绑定、通知偏好和系统设置</p>
    </div>

    <!-- 账号绑定 -->
    <div class="settings-card">
      <div class="settings-card-title">
        <i class="fas fa-link"></i> 账号绑定
      </div>
      <div class="bind-item">
        <div class="bind-info">
          <div class="bind-icon email">
            <i class="fas fa-envelope"></i>
          </div>
          <div class="bind-content">
            <div class="bind-name">邮箱</div>
            <div class="bind-value">{{ email }}</div>
            <div class="bind-desc">用于接收重要通知和找回密码</div>
          </div>
        </div>
        <div class="bind-status">
          <span class="status-tag bound">已绑定</span>
          <a class="bind-action" @click="handleAction('修改邮箱')">修改</a>
        </div>
      </div>
      <div class="bind-item">
        <div class="bind-info">
          <div class="bind-icon phone">
            <i class="fas fa-mobile-alt"></i>
          </div>
          <div class="bind-content">
            <div class="bind-name">手机号</div>
            <div class="bind-value">{{ phone || '未绑定' }}</div>
            <div class="bind-desc">已实名认证，可用于登录和接收短信</div>
          </div>
        </div>
        <div class="bind-status">
          <span class="status-tag bound">已绑定</span>
          <a class="bind-action" @click="handleAction('修改手机号')">修改</a>
        </div>
      </div>
      <div class="bind-item">
        <div class="bind-info">
          <div class="bind-icon wechat">
            <i class="fab fa-weixin"></i>
          </div>
          <div class="bind-content">
            <div class="bind-name">企业微信</div>
            <div class="bind-desc">绑定后可接收企业微信消息通知</div>
          </div>
        </div>
        <div class="bind-status">
          <span class="status-tag unbound">未绑定</span>
          <a class="bind-action" @click="handleAction('绑定企业微信')">去绑定</a>
        </div>
      </div>
      <div class="bind-item">
        <div class="bind-info">
          <div class="bind-icon dingtalk">
            <i class="fab fa-dochub"></i>
          </div>
          <div class="bind-content">
            <div class="bind-name">钉钉</div>
            <div class="bind-desc">绑定后可通过钉钉接收审批通知</div>
          </div>
        </div>
        <div class="bind-status">
          <span class="status-tag unbound">未绑定</span>
          <a class="bind-action" @click="handleAction('绑定钉钉')">去绑定</a>
        </div>
      </div>
    </div>

    <!-- 通知偏好 -->
    <div class="settings-card">
      <div class="settings-card-title">
        <i class="fas fa-bell"></i> 通知偏好
      </div>
      <div class="notify-group">
        <div class="notify-group-title">
          <i class="fas fa-circle"></i> 系统通知
        </div>
        <div class="notify-item">
          <div class="notify-info">
            <div class="notify-name">系统公告</div>
            <div class="notify-desc">平台维护、版本更新等重要通知</div>
          </div>
          <el-switch v-model="notify.systemNotice" />
        </div>
        <div class="notify-item">
          <div class="notify-info">
            <div class="notify-name">安全提醒</div>
            <div class="notify-desc">异常登录、密码修改等安全相关提醒</div>
          </div>
          <el-switch v-model="notify.securityAlert" />
        </div>
      </div>
      <div class="notify-group">
        <div class="notify-group-title">
          <i class="fas fa-circle"></i> 业务通知
        </div>
        <div class="notify-item">
          <div class="notify-info">
            <div class="notify-name">审核通知</div>
            <div class="notify-desc">新的认证审核、招工审核待处理时提醒</div>
          </div>
          <el-switch v-model="notify.auditNotice" />
        </div>
        <div class="notify-item">
          <div class="notify-info">
            <div class="notify-name">客服消息</div>
            <div class="notify-desc">用户反馈、客服会话需要回复时提醒</div>
          </div>
          <el-switch v-model="notify.serviceMessage" />
        </div>
      </div>
      <div class="notify-group">
        <div class="notify-group-title">
          <i class="fas fa-circle"></i> 数据报表
        </div>
        <div class="notify-item">
          <div class="notify-info">
            <div class="notify-name">每日数据报表</div>
            <div class="notify-desc">每日9点推送前一日运营数据概览</div>
          </div>
          <el-switch v-model="notify.dailyReport" />
        </div>
        <div class="notify-item">
          <div class="notify-info">
            <div class="notify-name">每周运营报告</div>
            <div class="notify-desc">每周一早10点汇总上周运营分析报告</div>
          </div>
          <el-switch v-model="notify.weeklyReport" />
        </div>
      </div>
    </div>

    <!-- 语言与地区 -->
    <div class="settings-card">
      <div class="settings-card-title">
        <i class="fas fa-language"></i> 语言与地区
      </div>
      <div class="lang-group">
        <div
          class="lang-option"
          :class="{ active: language === 'zh-CN' }"
          @click="language = 'zh-CN'"
        >
          <div class="lang-flag">🇨🇳</div>
          <div class="lang-name">简体中文</div>
          <div class="lang-desc">中国大陆</div>
        </div>
        <div
          class="lang-option"
          :class="{ active: language === 'zh-HK' }"
          @click="language = 'zh-HK'"
        >
          <div class="lang-flag">🇭🇰</div>
          <div class="lang-name">繁體中文</div>
          <div class="lang-desc">香港 / 澳门 / 台湾</div>
        </div>
        <div
          class="lang-option"
          :class="{ active: language === 'en' }"
          @click="language = 'en'"
        >
          <div class="lang-flag">🇬🇧</div>
          <div class="lang-name">English</div>
          <div class="lang-desc">Global</div>
        </div>
      </div>
      <div class="form-group">
        <label class="form-label">时区</label>
        <el-select v-model="timezone" placeholder="请选择时区" style="width: 100%;">
          <el-option label="(GMT+08:00) 北京，上海" value="GMT+08:00" />
          <el-option label="(GMT+09:00) 东京，大阪" value="GMT+09:00" />
          <el-option label="(GMT+00:00) 伦敦" value="GMT+00:00" />
          <el-option label="(GMT-05:00) 纽约，多伦多" value="GMT-05:00" />
        </el-select>
      </div>
      <div class="form-group">
        <label class="form-label">货币单位</label>
        <el-select v-model="currency" placeholder="请选择货币单位" style="width: 100%;">
          <el-option label="人民币 (CNY ¥)" value="CNY" />
          <el-option label="美元 (USD $)" value="USD" />
          <el-option label="港币 (HKD HK$)" value="HKD" />
        </el-select>
      </div>
    </div>

    <!-- 外观设置 -->
    <div class="settings-card">
      <div class="settings-card-title">
        <i class="fas fa-palette"></i> 外观设置
      </div>
      <div class="form-group">
        <label class="form-label">主题模式</label>
        <div class="lang-group">
          <div
            class="lang-option"
            :class="{ active: theme === 'light' }"
            @click="theme = 'light'"
          >
            <div class="lang-flag theme-flag">☀️</div>
            <div class="lang-name">浅色主题</div>
          </div>
          <div
            class="lang-option"
            :class="{ active: theme === 'dark' }"
            @click="theme = 'dark'"
          >
            <div class="lang-flag theme-flag">🌙</div>
            <div class="lang-name">深色主题</div>
          </div>
          <div
            class="lang-option"
            :class="{ active: theme === 'auto' }"
            @click="theme = 'auto'"
          >
            <div class="lang-flag theme-flag">⚙️</div>
            <div class="lang-name">跟随系统</div>
          </div>
        </div>
      </div>
      <div class="form-group">
        <label class="form-label">侧边栏样式</label>
        <div class="lang-group">
          <div
            class="lang-option"
            :class="{ active: sidebarStyle === 'expanded' }"
            @click="sidebarStyle = 'expanded'"
          >
            <div class="lang-flag theme-flag">📐</div>
            <div class="lang-name">展开模式</div>
          </div>
          <div
            class="lang-option"
            :class="{ active: sidebarStyle === 'collapsed' }"
            @click="sidebarStyle = 'collapsed'"
          >
            <div class="lang-flag theme-flag">📏</div>
            <div class="lang-name">紧凑模式</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 危险操作 -->
    <div class="danger-zone">
      <div class="danger-title">
        <i class="fas fa-exclamation-triangle"></i> 危险操作
      </div>
      <div class="danger-desc">
        以下操作不可恢复，请谨慎操作。如需注销账户或清空数据，请联系平台运营主管审批。
      </div>
      <ul class="danger-list">
        <li>
          <span>导出所有个人操作记录</span>
          <span class="danger-action" @click="handleAction('申请导出操作记录')">申请导出 →</span>
        </li>
        <li>
          <span>清除所有个人偏好设置</span>
          <span class="danger-action" @click="handleAction('重置偏好设置')">重置设置 →</span>
        </li>
        <li>
          <span>注销管理员账户</span>
          <span class="danger-action" @click="handleAction('申请注销管理员账户')">申请注销 →</span>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'

const userStore = useUserStore()

const phone = ref('')

const email = computed(() => userStore.userInfo.email || 'admin@kuaima.com')

onMounted(async () => {
  const adminId = Number(userStore.userInfo.adminId)
  if (!adminId) return
  try {
    const res = await request.get(`/admin/admin-users/${adminId}`)
    phone.value = res?.data?.phone || ''
  } catch (e) {
    // ignore
  }
})

const notify = reactive({
  systemNotice: true,
  securityAlert: true,
  auditNotice: true,
  reportNotice: true,
  serviceMessage: false,
  dailyReport: true,
  weeklyReport: false
})

const language = ref('zh-CN')
const timezone = ref('GMT+08:00')
const currency = ref('CNY')
const theme = ref('light')
const sidebarStyle = ref('expanded')

const handleAction = (name) => {
  ElMessage.info('功能开发中')
}
</script>

<style scoped>
.settings-card {
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.settings-card-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-primary);
}

.settings-card-title i {
  color: var(--primary);
}

.bind-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid var(--border);
}

.bind-item:last-child {
  border-bottom: none;
}

.bind-info {
  display: flex;
  align-items: center;
  gap: 14px;
}

.bind-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.bind-icon.email { background: #EFF6FF; color: #3B82F6; }
.bind-icon.phone { background: #F0FDF4; color: #10B981; }
.bind-icon.wechat { background: #FEF3C7; color: #F59E0B; }
.bind-icon.dingtalk { background: #F0F4FF; color: #6366F1; }

.bind-content .bind-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 2px;
}

.bind-content .bind-desc {
  font-size: 12px;
  color: var(--text-muted);
}

.bind-content .bind-value {
  font-size: 13px;
  color: var(--primary);
}

.bind-status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-tag {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 10px;
}

.status-tag.bound { background: #ECFDF5; color: #059669; }
.status-tag.unbound { background: #F3F4F6; color: #6B7280; }

.bind-action {
  font-size: 13px;
  cursor: pointer;
  color: var(--primary);
}

.bind-action:hover {
  text-decoration: underline;
}

.notify-group {
  margin-bottom: 20px;
}

.notify-group:last-child {
  margin-bottom: 0;
}

.notify-group-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.notify-group-title i {
  color: var(--primary);
  font-size: 12px;
}

.notify-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid var(--border);
}

.notify-item:last-child {
  border-bottom: none;
}

.notify-info .notify-name {
  font-size: 14px;
  color: var(--text-primary);
}

.notify-info .notify-desc {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 2px;
}

.lang-group {
  display: flex;
  gap: 12px;
}

.lang-option {
  flex: 1;
  padding: 16px;
  border: 2px solid var(--border);
  border-radius: 12px;
  cursor: pointer;
  text-align: center;
  transition: all 0.2s;
}

.lang-option:hover {
  border-color: var(--primary);
}

.lang-option.active {
  border-color: var(--primary);
  background: #FFF8E6;
}

.lang-option .lang-flag {
  font-size: 28px;
  margin-bottom: 6px;
}

.lang-option .lang-flag.theme-flag {
  font-size: 24px;
}

.lang-option .lang-name {
  font-size: 14px;
  font-weight: 500;
}

.lang-option .lang-desc {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 2px;
}

.form-group {
  margin-top: 20px;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.danger-zone {
  background: #FEF2F2;
  border: 1px solid #FECACA;
  border-radius: 16px;
  padding: 28px;
}

.danger-title {
  font-size: 16px;
  font-weight: 600;
  color: #DC2626;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.danger-title i {
  color: #DC2626;
}

.danger-desc {
  font-size: 13px;
  color: #991B1B;
  margin-bottom: 20px;
  line-height: 1.6;
}

.danger-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.danger-list li {
  padding: 10px 0;
  border-bottom: 1px solid rgba(220, 38, 38, 0.15);
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.danger-list li:last-child {
  border-bottom: none;
}

.danger-list li span:first-child {
  color: #7F1D1D;
}

.danger-action {
  color: #DC2626;
  font-weight: 500;
  cursor: pointer;
}

.danger-action:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .lang-group {
    flex-direction: column;
  }

  .bind-status {
    flex-direction: column;
    align-items: flex-end;
    gap: 6px;
  }
}
</style>
