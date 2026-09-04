<template>
  <div class="settings-page">
    <div class="page-header">
      <h1 class="page-title">系统设置</h1>
      <p class="page-desc">平台参数、账号权限、规则配置</p>
    </div>

    <div class="card">
      <el-tabs v-model="activeTab" class="settings-tabs">

        <!-- ============ Tab 1: 基础设置 ============ -->
        <el-tab-pane name="basic">
          <template #label>
            <span class="tab-label">
              <i class="fas fa-cog"></i> 基础设置
            </span>
          </template>

          <!-- 平台基础信息 -->
          <div class="settings-section-title">
            <i class="fas fa-info-circle"></i> 平台基础信息
          </div>
          <div class="settings-form-grid">
            <div class="form-group full-width">
              <label class="form-label">平台名称</label>
              <el-input v-model="platformForm.name" placeholder="请输入平台名称" />
            </div>
            <div class="form-group">
              <label class="form-label">平台Logo</label>
              <div class="logo-upload">
                <i class="fas fa-cloud-upload-alt"></i>
                <span>上传Logo</span>
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">客服电话</label>
              <el-input v-model="platformForm.phone" placeholder="请输入客服电话" />
            </div>
            <div class="form-group">
              <label class="form-label">营业时间</label>
              <div class="time-range">
                <el-time-picker
                  v-model="platformForm.workStart"
                  format="HH:mm"
                  value-format="HH:mm"
                  placeholder="开始时间"
                  style="flex:1;"
                />
                <span class="time-sep">至</span>
                <el-time-picker
                  v-model="platformForm.workEnd"
                  format="HH:mm"
                  value-format="HH:mm"
                  placeholder="结束时间"
                  style="flex:1;"
                />
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">版本号</label>
              <el-input v-model="platformForm.version" readonly class="readonly-input" />
            </div>
            <div class="form-group full-width">
              <label class="form-label">平台简介</label>
              <el-input
                v-model="platformForm.intro"
                type="textarea"
                :rows="4"
                placeholder="请输入平台简介..."
              />
            </div>
          </div>

          <!-- 平台服务费收取账号 -->
          <div class="settings-section-title" style="margin-top:28px;">
            <i class="fas fa-university"></i> 平台服务费收取账号
          </div>
          <div class="account-desc">服务费将从订单结算金额中扣除，收取至以下指定账户</div>

          <div class="account-tabs">
            <div
              :class="['account-tab', { active: accountTab === 'bank' }]"
              @click="accountTab = 'bank'"
            >
              <i class="fas fa-credit-card"></i> 银行账户
            </div>
            <div
              :class="['account-tab', { active: accountTab === 'wallet' }]"
              @click="accountTab = 'wallet'"
            >
              <i class="fas fa-wallet"></i> 电子钱包
            </div>
          </div>

          <!-- Bank panel -->
          <div v-show="accountTab === 'bank'" class="account-panel">
            <div class="bank-card">
              <div class="bank-card-header">
                <div>
                  <div class="bank-card-title">收款银行</div>
                  <div class="bank-card-bank">中国工商银行</div>
                </div>
                <div class="bank-card-logo"><i class="fas fa-building"></i></div>
              </div>
              <div class="bank-card-number">6222 **** **** 8888</div>
              <div class="bank-card-footer">
                <div class="bank-card-holder">
                  账户名
                  <span>快马日结科技有限公司</span>
                </div>
                <div class="bank-card-status">
                  <i class="fas fa-check-circle"></i> 已认证
                </div>
              </div>
            </div>

            <div class="account-list">
              <div class="account-item">
                <div class="account-item-header">
                  <div class="account-item-title">
                    <i class="fas fa-info-circle" style="color:var(--primary);"></i> 账户详细信息
                  </div>
                  <button class="account-edit-btn" @click="onEditAccount('银行账户')">
                    <i class="fas fa-edit"></i> 编辑
                  </button>
                </div>
                <div class="account-item-body">
                  <div class="account-field">
                    <span class="account-field-label">开户支行</span>
                    <span class="account-field-value">北京海淀支行</span>
                  </div>
                  <div class="account-field">
                    <span class="account-field-label">SWIFT代码</span>
                    <span class="account-field-value">ICBKCNBJ</span>
                  </div>
                  <div class="account-field">
                    <span class="account-field-label">联行号</span>
                    <span class="account-field-value">102100000458</span>
                  </div>
                  <div class="account-field">
                    <span class="account-field-label">账户类型</span>
                    <span class="account-field-value">对公账户</span>
                  </div>
                </div>
              </div>

              <div class="account-item">
                <div class="account-item-header">
                  <div class="account-item-title">
                    <i class="fas fa-shield-alt" style="color:var(--primary);"></i> 账户状态
                  </div>
                </div>
                <div class="account-item-body">
                  <div class="account-field">
                    <span class="account-field-label">启用状态</span>
                    <span class="account-field-value" style="color:var(--success);">
                      <i class="fas fa-check-circle"></i> 已启用
                    </span>
                  </div>
                  <div class="account-field">
                    <span class="account-field-label">验证时间</span>
                    <span class="account-field-value">2024-01-15</span>
                  </div>
                  <div class="account-field">
                    <span class="account-field-label">累计收取服务费</span>
                    <span class="account-field-value">¥ 1,258,960.00</span>
                  </div>
                  <div class="account-field">
                    <span class="account-field-label">本月收取</span>
                    <span class="account-field-value">¥ 86,420.00</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Wallet panel -->
          <div v-show="accountTab === 'wallet'" class="account-panel">
            <div class="wallet-card">
              <div class="wallet-card-icon alipay">
                <i class="fab fa-alipay"></i>
              </div>
              <div class="wallet-card-info">
                <div class="wallet-card-name">支付宝收款账户</div>
                <div class="wallet-card-account">k****ma@163.com</div>
              </div>
              <div class="wallet-card-actions">
                <button class="account-edit-btn" @click="onEditAccount('支付宝账户')">
                  <i class="fas fa-edit"></i> 编辑
                </button>
                <button class="account-edit-btn account-edit-primary" @click="onEditAccount('默认账户')">
                  <i class="fas fa-star"></i> 默认
                </button>
              </div>
            </div>

            <div class="wallet-card">
              <div class="wallet-card-icon wechat">
                <i class="fab fa-weixin"></i>
              </div>
              <div class="wallet-card-info">
                <div class="wallet-card-name">微信收款账户</div>
                <div class="wallet-card-account">k_m_a****001</div>
              </div>
              <div class="wallet-card-actions">
                <button class="account-edit-btn" @click="onEditAccount('微信账户')">
                  <i class="fas fa-edit"></i> 编辑
                </button>
              </div>
            </div>

            <div class="account-item" style="margin-top:12px;">
              <div class="account-item-header">
                <div class="account-item-title">
                  <i class="fas fa-exclamation-triangle" style="color:var(--warning);"></i> 注意事项
                </div>
              </div>
              <div class="account-notice">
                1. 平台服务费将在订单结算时自动扣除至指定账户<br>
                2. 请确保收款账户信息准确，避免资金转错<br>
                3. 如需更换收款账户，请提前通知财务部门并完成验证<br>
                4. 服务费收取记录可在"结算管理"中查看详细明细
              </div>
            </div>
          </div>

          <div class="settings-actions">
            <el-button type="primary" @click="savePlatform">
              <i class="fas fa-save"></i> 保存设置
            </el-button>
            <el-button @click="resetPlatform">重置</el-button>
          </div>
        </el-tab-pane>

        <!-- ============ Tab 2: 权限管理 ============ -->
        <el-tab-pane name="permission">
          <template #label>
            <span class="tab-label">
              <i class="fas fa-user-shield"></i> 权限管理
            </span>
          </template>

          <div class="settings-section-title">
            <i class="fas fa-users-cog"></i> 管理员列表
            <el-button
              type="primary"
              size="small"
              style="margin-left:auto;"
              @click="goAddAdmin"
            >
              <i class="fas fa-plus"></i> 添加管理员
            </el-button>
          </div>

          <div class="table-container">
            <table class="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>姓名</th>
                  <th>角色</th>
                  <th>权限组</th>
                  <th>最近登录</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="u in adminUsers" :key="u.id">
                  <td class="mono-cell">{{ formatId(u.id) }}</td>
                  <td>
                    <div class="user-cell">
                      <span class="avatar-circle" :style="{ background: avatarBg(u) }">
                        {{ (u.name || 'A').charAt(0) }}
                      </span>
                      <span>{{ u.name || '-' }}</span>
                    </div>
                  </td>
                  <td>
                    <span :class="['role-tag', roleClass(u.role)]">{{ roleLabel(u.role) }}</span>
                  </td>
                  <td>{{ u.dept || u.permissionGroup || '-' }}</td>
                  <td>{{ formatTime(u.lastLoginTime || u.lastLogin) }}</td>
                  <td>
                    <span :class="['status-badge', isOnline(u) ? 'success' : 'default']">
                      {{ isOnline(u) ? '在线' : '离线' }}
                    </span>
                  </td>
                  <td>
                    <a class="card-action" @click="goEditAdmin(u)">编辑</a>
                  </td>
                </tr>
                <tr v-if="adminUsers.length === 0">
                  <td colspan="7" class="empty-cell">暂无管理员数据</td>
                </tr>
              </tbody>
            </table>
          </div>
        </el-tab-pane>

        <!-- ============ Tab 3: 规则配置 ============ -->
        <el-tab-pane name="rules">
          <template #label>
            <span class="tab-label">
              <i class="fas fa-sliders-h"></i> 规则配置
            </span>
          </template>

          <!-- 招工规则 -->
          <div class="settings-section-title">
            <i class="fas fa-briefcase"></i> 招工规则
          </div>
          <div class="settings-form-grid">
            <div class="toggle-row full-width">
              <div class="toggle-label">
                <span class="toggle-label-title">招工审核</span>
                <span class="toggle-label-desc">雇主发布招工信息需经平台审核后方可展示</span>
              </div>
              <el-switch v-model="rulesForm.jobAudit" />
            </div>
            <div class="toggle-row full-width">
              <div class="toggle-label">
                <span class="toggle-label-title">实名认证要求</span>
                <span class="toggle-label-desc">零工必须完成实名认证方可接单</span>
              </div>
              <el-switch v-model="rulesForm.realNameRequired" />
            </div>
            <div class="form-group">
              <label class="form-label">最低日薪标准（元）</label>
              <el-input-number v-model="rulesForm.minDailyWage" :min="0" :step="10" controls-position="right" style="width:100%;" />
            </div>
            <div class="form-group">
              <label class="form-label">最长招工有效期（天）</label>
              <el-input-number v-model="rulesForm.maxJobValidity" :min="1" :step="1" controls-position="right" style="width:100%;" />
            </div>
          </div>

          <!-- 结算规则 -->
          <div class="settings-section-title" style="margin-top:28px;">
            <i class="fas fa-coins"></i> 结算规则
          </div>
          <div class="settings-form-grid">
            <div class="toggle-row full-width">
              <div class="toggle-label">
                <span class="toggle-label-title">日结模式</span>
                <span class="toggle-label-desc">订单完成后当日结算至零工账户</span>
              </div>
              <el-switch v-model="rulesForm.dailySettle" />
            </div>
            <div class="toggle-row full-width">
              <div class="toggle-label">
                <span class="toggle-label-title">平台服务费</span>
                <span class="toggle-label-desc">从每笔订单中扣除一定比例作为平台服务费</span>
              </div>
              <el-switch v-model="rulesForm.platformFeeEnabled" />
            </div>
            <div class="form-group">
              <label class="form-label">服务费比例（%）</label>
              <el-input-number v-model="rulesForm.feeRate" :min="0" :max="100" :step="0.5" controls-position="right" style="width:100%;" />
            </div>
            <div class="form-group">
              <label class="form-label">最低结算金额（元）</label>
              <el-input-number v-model="rulesForm.minSettleAmount" :min="0" :step="10" controls-position="right" style="width:100%;" />
            </div>
            <div class="form-group full-width">
              <label class="form-label">结算规则公式</label>
              <div class="rule-formula">
                <span class="comment">// 结算金额计算公式</span><br>
                <span class="highlight">结算金额</span> = <span class="highlight">订单金额</span> × (1 - <span class="highlight">服务费率</span>)<br>
                <span class="comment">// 示例：订单280元，服务费率5%</span><br>
                <span class="highlight">结算金额</span> = 280 × (1 - 0.05) = 266 元
              </div>
            </div>
          </div>

          <div class="settings-actions">
            <el-button type="primary" @click="saveRules">
              <i class="fas fa-save"></i> 保存规则配置
            </el-button>
            <el-button @click="resetRules">恢复默认</el-button>
          </div>
        </el-tab-pane>

        <!-- ============ Tab 4: 通知设置 ============ -->
        <el-tab-pane name="notice">
          <template #label>
            <span class="tab-label">
              <i class="fas fa-bell"></i> 通知设置
            </span>
          </template>

          <!-- 短信通知模板 -->
          <div class="settings-section-title">
            <i class="fas fa-sms"></i> 短信通知模板
            <el-button
              type="primary"
              size="small"
              style="margin-left:auto;"
              @click="onAddTemplate('短信')"
            >
              <i class="fas fa-plus"></i> 添加模板
            </el-button>
          </div>

          <div
            v-for="tpl in smsTemplates"
            :key="tpl.title"
            class="template-card"
          >
            <div class="template-header">
              <div class="template-title">
                <i class="fas fa-mobile-alt" style="color:var(--primary);"></i>
                {{ tpl.title }}
                <span class="tag tag-blue">短信</span>
              </div>
              <a class="card-action" @click="onEditTemplate(tpl.title)">编辑</a>
            </div>
            <div class="template-content">{{ tpl.content }}</div>
            <div class="template-actions">
              <el-button size="small" @click="onPreviewTemplate(tpl.title)">预览</el-button>
              <el-button size="small" @click="onSendTest">发送测试</el-button>
            </div>
          </div>

          <!-- 站内信模板 -->
          <div class="settings-section-title" style="margin-top:28px;">
            <i class="fas fa-envelope-open"></i> 站内信模板
            <el-button
              type="primary"
              size="small"
              style="margin-left:auto;"
              @click="onAddTemplate('站内信')"
            >
              <i class="fas fa-plus"></i> 添加模板
            </el-button>
          </div>

          <div
            v-for="tpl in inSiteTemplates"
            :key="tpl.title"
            class="template-card"
          >
            <div class="template-header">
              <div class="template-title">
                <i class="fas fa-envelope" style="color:var(--secondary);"></i>
                {{ tpl.title }}
                <span class="tag tag-green">站内信</span>
              </div>
              <a class="card-action" @click="onEditTemplate(tpl.title)">编辑</a>
            </div>
            <div class="template-content">{{ tpl.content }}</div>
            <div class="template-actions">
              <el-button size="small" @click="onPreviewTemplate(tpl.title)">预览</el-button>
              <el-button size="small" @click="onPublish(tpl.title)">发布</el-button>
            </div>
          </div>
        </el-tab-pane>

      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSettingsByCategory, saveSetting } from '@/api/system'
import request from '@/api/request'

const router = useRouter()

const activeTab = ref('basic')
const accountTab = ref('bank')

const platformForm = reactive({
  name: '快马日结',
  phone: '400-888-6666',
  workStart: '08:00',
  workEnd: '22:00',
  version: 'v2.0.3',
  intro: '快马日结是一款专注于零工经济的日结平台，为雇主和零工提供高效、便捷、安全的撮合服务。'
})

const rulesForm = reactive({
  jobAudit: true,
  realNameRequired: true,
  minDailyWage: 150,
  maxJobValidity: 30,
  dailySettle: true,
  platformFeeEnabled: true,
  feeRate: 5,
  minSettleAmount: 50
})

const adminUsers = ref([])

const smsTemplates = [
  {
    title: '订单接单通知',
    content: '【快马日结】尊敬的{用户名}，您已成功接单{订单编号}，工作时间：{时间}，地点：{地点}，请准时到岗。'
  },
  {
    title: '工资到账通知',
    content: '【快马日结】尊敬的{用户名}，您的工资{金额}元已结算到账，订单号：{订单编号}，感谢您的辛勤劳动！'
  },
  {
    title: '审核结果通知',
    content: '【快马日结】尊敬的{用户名}，您提交的{审核类型}已{审核结果}，详情请登录APP查看。'
  }
]

const inSiteTemplates = [
  {
    title: '系统公告推送',
    content: '尊敬的{用户名}，平台将于{时间}进行系统维护升级，届时服务将暂停使用，敬请谅解。'
  },
  {
    title: '飞单提醒通知',
    content: '尊敬的{用户名}，您本次{订单编号}的订单标记为飞单，已扣除{积分}积分，累计{次数}次将面临封禁处理。'
  }
]

// ====== Helpers ======
const roleClass = (role) => {
  if (role === 'SUPER_ADMIN') return 'role-super'
  if (role === 'ADMIN') return 'role-admin'
  if (role === 'EDITOR') return 'role-editor'
  return 'role-viewer'
}

const roleLabel = (role) => {
  const map = {
    SUPER_ADMIN: '超级管理员',
    ADMIN: '管理员',
    EDITOR: '审核员',
    VIEWER: '查看员'
  }
  return map[role] || role || '查看员'
}

const formatId = (id) => 'A' + String(id || 0).padStart(3, '0')

const formatTime = (t) => {
  if (!t) return '-'
  const s = String(t).replace('T', ' ')
  return s.length > 16 ? s.substring(0, 16) : s
}

const avatarBg = (u) => {
  const map = {
    SUPER_ADMIN: 'linear-gradient(135deg,#FF6B35,#FF8C42)',
    ADMIN: 'linear-gradient(135deg,#2563EB,#3B82F6)',
    EDITOR: 'linear-gradient(135deg,#10B981,#059669)',
    VIEWER: 'linear-gradient(135deg,#8B5CF6,#6D28D9)'
  }
  return map[u.role] || 'linear-gradient(135deg,#F59E0B,#D97706)'
}

const isOnline = (u) => {
  // 后端没有在线状态时，依据 status / enabled 字段或最近登录推断
  if (u.status === '启用' || u.enabled === true) {
    // 近30分钟内登录视为在线
    const last = u.lastLoginTime || u.lastLogin
    if (!last) return true
    const ts = new Date(String(last).replace(/-/g, '/')).getTime()
    if (isNaN(ts)) return true
    return Date.now() - ts < 30 * 60 * 1000
  }
  return false
}

// ====== Load ======
const applyPlatformSettings = (list) => {
  const map = {}
  ;(list || []).forEach(item => {
    map[item.settingKey] = item.settingValue
  })
  if (map['platform.name']) platformForm.name = map['platform.name']
  if (map['platform.phone']) platformForm.phone = map['platform.phone']
  if (map['platform.workStart']) platformForm.workStart = map['platform.workStart']
  if (map['platform.workEnd']) platformForm.workEnd = map['platform.workEnd']
  if (map['platform.version']) platformForm.version = map['platform.version']
  if (map['platform.intro']) platformForm.intro = map['platform.intro']
}

const applyRulesSettings = (list) => {
  const map = {}
  ;(list || []).forEach(item => {
    map[item.settingKey] = item.settingValue
  })
  if ('rules.jobAudit' in map) rulesForm.jobAudit = map['rules.jobAudit'] === 'true' || map['rules.jobAudit'] === true
  if ('rules.realNameRequired' in map) rulesForm.realNameRequired = map['rules.realNameRequired'] === 'true' || map['rules.realNameRequired'] === true
  if ('rules.minDailyWage' in map) rulesForm.minDailyWage = Number(map['rules.minDailyWage']) || rulesForm.minDailyWage
  if ('rules.maxJobValidity' in map) rulesForm.maxJobValidity = Number(map['rules.maxJobValidity']) || rulesForm.maxJobValidity
  if ('rules.dailySettle' in map) rulesForm.dailySettle = map['rules.dailySettle'] === 'true' || map['rules.dailySettle'] === true
  if ('rules.platformFeeEnabled' in map) rulesForm.platformFeeEnabled = map['rules.platformFeeEnabled'] === 'true' || map['rules.platformFeeEnabled'] === true
  if ('rules.feeRate' in map) rulesForm.feeRate = Number(map['rules.feeRate']) || rulesForm.feeRate
  if ('rules.minSettleAmount' in map) rulesForm.minSettleAmount = Number(map['rules.minSettleAmount']) || rulesForm.minSettleAmount
}

const loadPlatformSettings = async () => {
  try {
    const res = await getSettingsByCategory('platform')
    const d = res?.data
    const list = Array.isArray(d) ? d : (d?.content || d?.list || [])
    if (Array.isArray(list)) applyPlatformSettings(list)
  } catch (e) {
    console.warn('[Settings] 加载平台设置失败:', e)
  }
}

const loadRulesSettings = async () => {
  try {
    const res = await getSettingsByCategory('security')
    const d = res?.data
    const list = Array.isArray(d) ? d : (d?.content || d?.list || [])
    if (Array.isArray(list)) applyRulesSettings(list)
  } catch (e) {
    console.warn('[Settings] 加载规则设置失败:', e)
  }
  try {
    const res = await getSettingsByCategory('points')
    const d = res?.data
    const list = Array.isArray(d) ? d : (d?.content || d?.list || [])
    if (Array.isArray(list)) applyRulesSettings(list)
  } catch (e) {
    console.warn('[Settings] 加载积分设置失败:', e)
  }
}

const loadAdminUsers = async () => {
  try {
    const res = await request.get('/admin/admin-users', { params: { page: 0, size: 20 } })
    const d = res?.data
    const list = Array.isArray(d) ? d : (d?.content || d?.list || [])
    adminUsers.value = list || []
  } catch (e) {
    console.warn('[Settings] 加载管理员列表失败:', e)
    adminUsers.value = []
  }
}

// ====== Save ======
const savePlatform = async () => {
  try {
    await saveSetting('platform.name', platformForm.name, '平台名称')
    await saveSetting('platform.phone', platformForm.phone, '客服电话')
    await saveSetting('platform.workStart', platformForm.workStart, '营业开始时间')
    await saveSetting('platform.workEnd', platformForm.workEnd, '营业结束时间')
    await saveSetting('platform.intro', platformForm.intro, '平台简介')
    ElMessage.success('保存成功')
  } catch (e) {
    console.warn('[Settings] 保存平台设置失败:', e)
    ElMessage.error('保存失败')
  }
}

const resetPlatform = () => {
  platformForm.name = '快马日结'
  platformForm.phone = '400-888-6666'
  platformForm.workStart = '08:00'
  platformForm.workEnd = '22:00'
  platformForm.intro = '快马日结是一款专注于零工经济的日结平台，为雇主和零工提供高效、便捷、安全的撮合服务。'
  ElMessage.info('已重置为默认设置')
}

const saveRules = async () => {
  try {
    await saveSetting('rules.jobAudit', String(rulesForm.jobAudit), '招工审核')
    await saveSetting('rules.realNameRequired', String(rulesForm.realNameRequired), '实名认证要求')
    await saveSetting('rules.minDailyWage', String(rulesForm.minDailyWage), '最低日薪标准')
    await saveSetting('rules.maxJobValidity', String(rulesForm.maxJobValidity), '最长招工有效期')
    await saveSetting('rules.dailySettle', String(rulesForm.dailySettle), '日结模式')
    await saveSetting('rules.platformFeeEnabled', String(rulesForm.platformFeeEnabled), '平台服务费')
    await saveSetting('rules.feeRate', String(rulesForm.feeRate), '服务费比例')
    await saveSetting('rules.minSettleAmount', String(rulesForm.minSettleAmount), '最低结算金额')
    ElMessage.success('规则配置保存成功')
  } catch (e) {
    console.warn('[Settings] 保存规则失败:', e)
    ElMessage.error('保存失败')
  }
}

const resetRules = () => {
  rulesForm.jobAudit = true
  rulesForm.realNameRequired = true
  rulesForm.minDailyWage = 150
  rulesForm.maxJobValidity = 30
  rulesForm.dailySettle = true
  rulesForm.platformFeeEnabled = true
  rulesForm.feeRate = 5
  rulesForm.minSettleAmount = 50
  ElMessage.info('已恢复为默认设置')
}

// ====== Actions ======
const goAddAdmin = () => {
  router.push('/admin/admin-user/form')
}

const goEditAdmin = (row) => {
  router.push(`/admin/admin-user/form?mode=edit&id=${row.id}`)
}

const onEditAccount = (name) => {
  ElMessage.info('功能开发中')
}

const onAddTemplate = (type) => {
  ElMessage.info('功能开发中')
}

const onEditTemplate = (name) => {
  ElMessage.info('功能开发中')
}

const onPreviewTemplate = (name) => {
  ElMessage.info('功能开发中')
}

const onSendTest = () => {
  ElMessage.info('功能开发中')
}

const onPublish = (name) => {
  ElMessage.info('功能开发中')
}

onMounted(() => {
  loadPlatformSettings()
  loadRulesSettings()
  loadAdminUsers()
})
</script>

<style scoped>
.settings-page {
  width: 100%;
}

/* ============ Tab navigation ============ */
.settings-tabs :deep(.el-tabs__header) {
  margin-bottom: 24px;
  border-bottom: 2px solid var(--border);
}

.settings-tabs :deep(.el-tabs__nav-wrap) {
  border: none;
}

.settings-tabs :deep(.el-tabs__item) {
  padding: 14px 24px;
  height: auto;
  line-height: 1.2;
  font-size: 15px;
  color: var(--text-secondary);
  border: none;
  transition: color 0.2s;
}

.settings-tabs :deep(.el-tabs__item:hover) {
  color: var(--primary);
}

.settings-tabs :deep(.el-tabs__item.is-active) {
  color: var(--primary);
  font-weight: 500;
}

.settings-tabs :deep(.el-tabs__active-bar) {
  background-color: var(--primary);
  height: 2px;
}

.settings-tabs :deep(.el-tabs__content) {
  overflow: visible;
}

.tab-label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

/* ============ Section title ============ */
.settings-section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-primary);
}

.settings-section-title i {
  color: var(--primary);
}

/* ============ Form grid ============ */
.settings-form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px 24px;
}

.settings-form-grid .full-width {
  grid-column: 1 / -1;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.time-range {
  display: flex;
  gap: 10px;
  align-items: center;
}

.time-sep {
  flex-shrink: 0;
  color: var(--text-secondary);
  font-size: 13px;
}

/* readonly version input */
.readonly-input :deep(.el-input__wrapper) {
  background: var(--bg-page);
  box-shadow: 0 0 0 1px var(--border) inset;
}

/* ============ Logo upload ============ */
.logo-upload {
  width: 120px;
  height: 120px;
  border: 2px dashed var(--border);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-muted);
  gap: 8px;
}

.logo-upload:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.logo-upload i {
  font-size: 28px;
}

.logo-upload span {
  font-size: 13px;
}

/* ============ Account description ============ */
.account-desc {
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--text-muted);
}

/* ============ Account sub-tabs ============ */
.account-tabs {
  display: flex;
  gap: 0;
  margin-bottom: 16px;
  border-bottom: 1px solid var(--border);
}

.account-tab {
  padding: 12px 24px;
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.account-tab:hover {
  color: var(--primary);
}

.account-tab.active {
  color: var(--primary);
  border-bottom-color: var(--primary);
  font-weight: 500;
}

/* ============ Bank card ============ */
.bank-card {
  background: linear-gradient(135deg, #1E3A8A 0%, #4338CA 100%);
  border-radius: 16px;
  padding: 28px;
  color: white;
  margin-bottom: 16px;
  position: relative;
  overflow: hidden;
}

.bank-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 200px;
  height: 200px;
  background: rgba(255,255,255,0.1);
  border-radius: 50%;
}

.bank-card::after {
  content: '';
  position: absolute;
  bottom: -30%;
  left: -10%;
  width: 150px;
  height: 150px;
  background: rgba(255,255,255,0.08);
  border-radius: 50%;
}

.bank-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  position: relative;
  z-index: 1;
}

.bank-card-title {
  font-size: 12px;
  opacity: 0.8;
}

.bank-card-bank {
  font-size: 18px;
  font-weight: 600;
  margin-top: 4px;
}

.bank-card-logo {
  width: 44px;
  height: 44px;
  background: rgba(255,255,255,0.15);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.bank-card-number {
  font-size: 22px;
  font-family: 'Courier New', monospace;
  letter-spacing: 2px;
  margin: 20px 0;
  position: relative;
  z-index: 1;
}

.bank-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.bank-card-holder {
  font-size: 13px;
  opacity: 0.8;
}

.bank-card-holder span {
  display: block;
  font-size: 16px;
  font-weight: 500;
  margin-top: 2px;
  opacity: 1;
}

.bank-card-status {
  font-size: 12px;
  padding: 4px 10px;
  background: rgba(255,255,255,0.2);
  border-radius: 12px;
}

/* ============ Account items ============ */
.account-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.account-item {
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 12px;
  margin-bottom: 12px;
  transition: border-color 0.2s;
}

.account-item:hover {
  border-color: var(--primary);
}

.account-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);
}

.account-item-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.account-item-body {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px 24px;
  padding: 16px 20px;
}

.account-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px 0;
}

.account-field-label {
  font-size: 13px;
  color: var(--text-muted);
}

.account-field-value {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

.account-notice {
  padding: 16px 20px;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.8;
}

/* ============ Wallet cards ============ */
.wallet-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border: 1px solid var(--border);
  border-radius: 12px;
  margin-bottom: 12px;
  transition: all 0.2s;
}

.wallet-card:hover {
  border-color: var(--primary);
  box-shadow: 0 4px 12px rgba(255,107,53,0.1);
}

.wallet-card-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.wallet-card-icon.alipay {
  background: #EFF6FF;
  color: #3B82F6;
}

.wallet-card-icon.wechat {
  background: #F0FDF4;
  color: #10B981;
}

.wallet-card-info {
  flex: 1;
}

.wallet-card-name {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 4px;
}

.wallet-card-account {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.wallet-card-actions {
  display: flex;
  gap: 8px;
}

.account-edit-btn {
  padding: 6px 12px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: #fff;
  font-size: 12px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.account-edit-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.account-edit-primary {
  color: var(--primary);
  border-color: var(--primary);
}

/* ============ Toggle row ============ */
.toggle-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid var(--border);
}

.toggle-row:last-child {
  border-bottom: none;
}

.toggle-label {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.toggle-label-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.toggle-label-desc {
  font-size: 12px;
  color: var(--text-muted);
}

/* ============ Rule formula ============ */
.rule-formula {
  background: #F9FAFB;
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 20px;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  line-height: 2;
  color: var(--text-primary);
}

.rule-formula .comment {
  color: var(--text-muted);
}

.rule-formula .highlight {
  color: var(--primary);
  font-weight: 600;
}

/* ============ Settings actions ============ */
.settings-actions {
  margin-top: 20px;
  display: flex;
  gap: 12px;
}

/* ============ Template cards ============ */
.template-card {
  border: 1px solid var(--border);
  border-radius: 12px;
  margin-bottom: 16px;
  transition: border-color 0.2s;
}

.template-card:hover {
  border-color: var(--primary);
}

.template-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);
}

.template-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.tag-blue {
  background: #EFF6FF;
  color: #3B82F6;
}

.tag-green {
  background: #F0FDF4;
  color: #10B981;
}

.template-content {
  padding: 16px 20px;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.template-actions {
  padding: 12px 20px;
  border-top: 1px solid var(--border);
  display: flex;
  gap: 8px;
}

/* ============ Data table ============ */
.table-container {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--border);
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  padding: 12px 16px;
  text-align: left;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-muted);
  border-bottom: 2px solid var(--border);
  background: #F9FAFB;
}

.data-table td {
  padding: 12px 16px;
  font-size: 14px;
  border-bottom: 1px solid var(--border);
  color: var(--text-primary);
}

.data-table tbody tr:last-child td {
  border-bottom: none;
}

.data-table tbody tr:hover {
  background: #F9FAFB;
}

.mono-cell {
  font-family: 'Courier New', monospace;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.avatar-circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  flex-shrink: 0;
}

.role-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}

.role-super {
  background: #FFF3E6;
  color: #FF6B35;
}

.role-admin {
  background: #EFF6FF;
  color: #3B82F6;
}

.role-editor {
  background: #FFF8E6;
  color: #F59E0B;
}

.role-viewer {
  background: #F3F4F6;
  color: #6B7280;
}

.status-badge {
  display: inline-block;
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 10px;
  font-weight: 500;
}

.status-badge.success {
  background: #F0FDF4;
  color: #10B981;
}

.status-badge.default {
  background: #F3F4F6;
  color: #6B7280;
}

.empty-cell {
  text-align: center;
  color: var(--text-muted);
  padding: 24px !important;
  font-size: 13px;
}

.card-action {
  font-size: 13px;
  color: var(--primary);
  cursor: pointer;
}

.card-action:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .settings-form-grid,
  .account-item-body {
    grid-template-columns: 1fr;
  }
}
</style>
