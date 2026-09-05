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
                  <div class="bank-card-bank">{{ bankInfo.bankName }}</div>
                </div>
                <div class="bank-card-logo"><i class="fas fa-building"></i></div>
              </div>
              <div class="bank-card-number">{{ bankInfo.cardNumber }}</div>
              <div class="bank-card-footer">
                <div class="bank-card-holder">
                  账户名
                  <span>{{ bankInfo.holder }}</span>
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
                  <button class="account-edit-btn" @click="openBankEdit">
                    <i class="fas fa-edit"></i> 编辑
                  </button>
                </div>
                <div class="account-item-body">
                  <div class="account-field">
                    <span class="account-field-label">开户支行</span>
                    <span class="account-field-value">{{ bankInfo.branch }}</span>
                  </div>
                  <div class="account-field">
                    <span class="account-field-label">SWIFT代码</span>
                    <span class="account-field-value">{{ bankInfo.swiftCode }}</span>
                  </div>
                  <div class="account-field">
                    <span class="account-field-label">联行号</span>
                    <span class="account-field-value">{{ bankInfo.bankCode }}</span>
                  </div>
                  <div class="account-field">
                    <span class="account-field-label">账户类型</span>
                    <span class="account-field-value">{{ bankInfo.accountType }}</span>
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
                <div class="wallet-card-name">
                  支付宝收款账户
                  <span v-if="walletInfo.alipay.isDefault" class="wallet-default-tag">默认</span>
                </div>
                <div class="wallet-card-account">{{ maskWalletAccount('alipay', walletInfo.alipay.account) }}</div>
              </div>
              <div class="wallet-card-actions">
                <button class="account-edit-btn" @click="openWalletEdit('alipay')">
                  <i class="fas fa-edit"></i> 编辑
                </button>
                <button
                  v-if="!walletInfo.alipay.isDefault"
                  class="account-edit-btn account-edit-primary"
                  @click="setWalletDefault('alipay')"
                >
                  <i class="fas fa-star"></i> 默认
                </button>
              </div>
            </div>

            <div class="wallet-card">
              <div class="wallet-card-icon wechat">
                <i class="fab fa-weixin"></i>
              </div>
              <div class="wallet-card-info">
                <div class="wallet-card-name">
                  微信收款账户
                  <span v-if="walletInfo.wechat.isDefault" class="wallet-default-tag">默认</span>
                </div>
                <div class="wallet-card-account">{{ maskWalletAccount('wechat', walletInfo.wechat.account) }}</div>
              </div>
              <div class="wallet-card-actions">
                <button class="account-edit-btn" @click="openWalletEdit('wechat')">
                  <i class="fas fa-edit"></i> 编辑
                </button>
                <button
                  v-if="!walletInfo.wechat.isDefault"
                  class="account-edit-btn account-edit-primary"
                  @click="setWalletDefault('wechat')"
                >
                  <i class="fas fa-star"></i> 默认
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
            <button class="btn btn-primary" @click="savePlatform">
              <i class="fas fa-save"></i> 保存设置
            </button>
            <button class="btn btn-outline" @click="resetPlatform">重置</button>
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
            <button class="btn btn-primary btn-sm" style="margin-left:auto;" @click="goAddAdmin">
              新建账号
            </button>
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
            <button class="btn btn-primary" @click="saveRules">
              <i class="fas fa-save"></i> 保存规则配置
            </button>
            <button class="btn btn-outline" @click="resetRules">恢复默认</button>
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
            <button class="btn btn-primary btn-sm" style="margin-left:auto;" @click="onAddTemplate('短信')">
              <i class="fas fa-plus"></i> 添加模板
            </button>
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
              <button class="btn btn-sm btn-outline" @click="onPreviewTemplate(tpl)">预览</button>
              <button class="btn btn-sm btn-outline" @click="onSendTest(tpl)">发送测试</button>
            </div>
          </div>

          <!-- 站内信模板 -->
          <div class="settings-section-title" style="margin-top:28px;">
            <i class="fas fa-envelope-open"></i> 站内信模板
            <button class="btn btn-primary btn-sm" style="margin-left:auto;" @click="onAddTemplate('站内信')">
              <i class="fas fa-plus"></i> 添加模板
            </button>
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
              <button class="btn btn-sm btn-outline" @click="onPreviewTemplate(tpl)">预览</button>
              <button class="btn btn-sm btn-outline" @click="onPublish(tpl.title)">发布</button>
            </div>
          </div>
        </el-tab-pane>

      </el-tabs>
    </div>

    <!-- 银行账户编辑弹窗 -->
    <el-dialog
      v-model="showBankEditDialog"
      title="编辑银行账户"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form :model="bankEditForm" label-width="100px">
        <el-form-item label="收款银行">
          <el-input v-model="bankEditForm.bankName" placeholder="请输入收款银行" />
        </el-form-item>
        <el-form-item label="银行卡号">
          <el-input v-model="bankEditForm.cardNumber" placeholder="请输入银行卡号" />
        </el-form-item>
        <el-form-item label="账户名">
          <el-input v-model="bankEditForm.holder" placeholder="请输入账户名" />
        </el-form-item>
        <el-form-item label="开户支行">
          <el-input v-model="bankEditForm.branch" placeholder="请输入开户支行" />
        </el-form-item>
        <el-form-item label="SWIFT代码">
          <el-input v-model="bankEditForm.swiftCode" placeholder="请输入SWIFT代码" />
        </el-form-item>
        <el-form-item label="联行号">
          <el-input v-model="bankEditForm.bankCode" placeholder="请输入联行号" />
        </el-form-item>
        <el-form-item label="账户类型">
          <el-select v-model="bankEditForm.accountType" style="width:100%">
            <el-option label="对公账户" value="对公账户" />
            <el-option label="个人账户" value="个人账户" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <button class="btn btn-outline" @click="showBankEditDialog = false">取消</button>
          <button class="btn btn-primary" @click="saveBankEdit">保存</button>
        </div>
      </template>
    </el-dialog>

    <!-- 钱包账户编辑弹窗 -->
    <el-dialog
      v-model="showWalletEditDialog"
      :title="walletMeta[walletEditType].title"
      width="480px"
      :close-on-click-modal="false"
    >
      <!-- 账户预览 -->
      <div class="wallet-edit-preview">
        <div :class="['wallet-edit-icon', walletMeta[walletEditType].iconClass]">
          <i :class="walletMeta[walletEditType].icon"></i>
        </div>
        <div style="min-width:0; flex:1;">
          <div style="font-size:14px; font-weight:600; color:var(--text-primary);">
            {{ walletMeta[walletEditType].name }}
          </div>
          <div style="font-size:13px; color:var(--text-secondary); margin-top:2px;">
            {{ maskWalletAccount(walletEditType, walletEditForm.account) }}
          </div>
        </div>
        <span class="tag tag-green" style="flex-shrink:0;">已认证</span>
      </div>

      <el-form :model="walletEditForm" label-width="120px" style="margin-top:16px;">
        <el-form-item :label="walletMeta[walletEditType].accountLabel">
          <el-input
            v-model="walletEditForm.account"
            :placeholder="walletMeta[walletEditType].placeholder"
          />
          <div style="font-size:12px; color:var(--text-secondary); margin-top:4px;">
            {{ walletMeta[walletEditType].hint }}
          </div>
        </el-form-item>
        <el-form-item :label="walletMeta[walletEditType].holderLabel">
          <el-input v-model="walletEditForm.holder" placeholder="请输入实名认证姓名或企业主体名称" />
          <div style="font-size:12px; color:var(--text-secondary); margin-top:4px;">
            须与账户实名认证信息完全一致，否则将导致收款失败。
          </div>
        </el-form-item>
        <el-form-item label="设为默认收款账户">
          <el-switch v-model="walletEditForm.isDefault" />
          <span style="font-size:13px; color:var(--text-secondary); margin-left:8px;">
            默认账户将优先用于服务费扣款
          </span>
        </el-form-item>
      </el-form>

      <!-- 安全提示 -->
      <div class="wallet-edit-tips">
        <div class="wallet-edit-tip"><i class="fas fa-check-circle"></i> 修改收款账户后将进入 1 个工作日的审核期。</div>
        <div class="wallet-edit-tip"><i class="fas fa-check-circle"></i> 审核期间结算款项仍打入原账户，审核通过后自动切换至新账户。</div>
        <div class="wallet-edit-tip"><i class="fas fa-check-circle"></i> 每次修改账户信息均会记录操作日志，可在「系统设置 - 操作日志」中查看。</div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <button class="btn btn-outline" @click="showWalletEditDialog = false">取消</button>
          <button class="btn btn-primary" @click="saveWalletEdit">保存</button>
        </div>
      </template>
    </el-dialog>

    <!-- 模板预览右侧抽屉 -->
    <el-drawer
      v-model="showPreviewDialog"
      direction="rtl"
      size="440px"
      :with-header="false"
      class="preview-drawer-wrapper"
    >
      <div class="pd-header">
        <div>
          <div class="pd-title"><i class="fas fa-eye"></i> 模板预览</div>
          <div class="pd-sub">
            <span class="tag" :class="previewType === 'sms' ? 'tag-blue' : 'tag-green'">
              {{ previewType === 'sms' ? '短信' : '站内信' }}
            </span>
            <span>{{ previewTpl?.title }}</span>
          </div>
        </div>
        <button class="pd-close" @click="showPreviewDialog = false" title="关闭">
          <i class="fas fa-xmark"></i>
        </button>
      </div>

      <div class="pd-body">
        <!-- 手机预览 -->
        <div class="pd-section pd-section-phone">
          <div class="pd-section-title">
            <i class="fas fa-mobile-screen"></i> 用户端效果
            <span class="pd-hint">变量为示例数据</span>
          </div>
          <div class="pd-phone">
            <div class="pd-phone-screen">
              <div class="pd-phone-island"></div>
              <div class="pd-phone-bar">
                <span>9:41</span>
                <span><i class="fas fa-signal"></i><i class="fas fa-wifi"></i><i class="fas fa-battery-full"></i></span>
              </div>
              <div class="pd-phone-head">{{ previewType === 'sms' ? '信息' : '通知' }}</div>
              <div class="pd-phone-body">
                <!-- 短信气泡 -->
                <div v-if="previewType === 'sms'" class="pd-sms">
                  <div class="pd-sms-avatar"><i class="fas fa-comment-sms"></i></div>
                  <div class="pd-sms-main">
                    <div class="pd-sms-top">
                      <span class="pd-sms-name">快马日结</span>
                      <span class="pd-sms-time">刚刚</span>
                    </div>
                    <div class="pd-sms-bubble" v-html="previewTpl ? renderContent(previewTpl.content, true) : ''"></div>
                  </div>
                </div>
                <!-- 站内信通知卡片 -->
                <div v-else class="pd-notice">
                  <div class="pd-notice-head">
                    <div class="pd-notice-icon"><i class="fas fa-bell"></i></div>
                    <div>
                      <div class="pd-notice-app">快马日结</div>
                      <div class="pd-notice-now">现在</div>
                    </div>
                  </div>
                  <div class="pd-notice-title">{{ previewTpl?.title }}</div>
                  <div class="pd-notice-text" v-html="previewTpl ? renderContent(previewTpl.content, true) : ''"></div>
                  <div class="pd-notice-foot">点击查看详情</div>
                </div>
              </div>
              <div class="pd-phone-home"></div>
            </div>
          </div>
        </div>

        <!-- 原始模板 -->
        <div class="pd-section">
          <div class="pd-section-title"><i class="fas fa-code"></i> 原始模板内容</div>
          <div class="pd-raw" v-html="previewTpl ? renderContent(previewTpl.content, false) : ''"></div>
        </div>

        <!-- 变量示例 -->
        <div class="pd-section">
          <div class="pd-section-title"><i class="fas fa-list-ul"></i> 变量示例值</div>
          <div>
            <div v-for="key in previewTpl ? getTemplateVars(previewTpl.content) : []" :key="key" class="pd-var-row">
              <code class="pd-var-code">{{ '{' + key + '}' }}</code>
              <span class="pd-var-val">{{ sampleValues[key] || '--' }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="pd-footer">
        <button class="btn btn-outline" @click="showPreviewDialog = false">关闭</button>
        <button class="btn btn-outline" @click="showPreviewDialog = false; onEditTemplate(previewTpl?.title)">
          <i class="fas fa-edit"></i> 编辑模板
        </button>
        <button class="btn btn-primary" @click="showPreviewDialog = false; onSendTest(previewTpl)">
          <i class="fas fa-paper-plane"></i> 发送测试
        </button>
      </div>
    </el-drawer>

    <!-- 发送测试右侧抽屉 -->
    <el-drawer
      v-model="showTestDialog"
      direction="rtl"
      size="440px"
      :with-header="false"
      class="preview-drawer-wrapper test-drawer-wrapper"
    >
      <div class="pd-header">
        <div>
          <div class="pd-title"><i class="fas fa-paper-plane"></i> 发送测试</div>
          <div class="pd-sub">
            <span class="tag" :class="testType === 'sms' ? 'tag-blue' : 'tag-green'">
              {{ testType === 'sms' ? '短信' : '站内信' }}
            </span>
            <span>{{ testTpl?.title }}</span>
          </div>
        </div>
        <button class="pd-close" @click="showTestDialog = false" title="关闭">
          <i class="fas fa-xmark"></i>
        </button>
      </div>

      <!-- 表单视图 -->
      <div v-if="!testSuccess" class="pd-body">
        <div class="pd-section">
          <div class="pd-section-title"><i class="fas fa-mobile-screen-button"></i> 接收设置</div>
          <div class="td-field">
            <label class="td-label">
              {{ testType === 'sms' ? '接收手机号' : '接收账号手机号' }}
              <span class="required">*</span>
            </label>
            <el-input
              v-model="testReceiver"
              :placeholder="testType === 'sms' ? '请输入接收测试短信的11位手机号' : '请输入接收测试站内信账号的手机号'"
              maxlength="11"
              class="td-el-input"
            />
            <div class="td-hint">
              {{ testType === 'sms'
                ? '测试短信将通过运营商通道真实下发至该手机号，请注意控制发送频次。'
                : '站内信将推送至该手机号注册的APP账号，请确认账号已注册快马日结。' }}
            </div>
          </div>
        </div>

        <div class="pd-section">
          <div class="pd-section-title">
            <i class="fas fa-comment-dots"></i> 发送内容
            <span class="pd-hint">变量为示例数据</span>
          </div>
          <div class="td-content-preview" v-html="testTpl ? renderContent(testTpl.content, true) : ''"></div>
          <div class="td-content-meta">
            <span v-if="testType === 'sms'">短信签名将自动追加：【快马日结】</span>
            <span>不计入正式发送量</span>
          </div>
        </div>

        <div class="pd-section">
          <div class="pd-section-title"><i class="fas fa-circle-info"></i> 测试说明</div>
          <div class="td-tip"><i class="fas fa-check-circle"></i><span>测试消息用于验证模板内容与变量替换效果，不触发真实业务流程。</span></div>
          <div class="td-tip"><i class="fas fa-check-circle"></i><span>同一管理员账号每天最多发送 10 条测试消息，请勿频繁发送。</span></div>
          <div class="td-tip"><i class="fas fa-check-circle"></i>
            <span v-if="testType === 'sms'">短信按运营商标准计费，签名【快马日结】将自动追加在内容开头。</span>
            <span v-else>站内信测试将下发至该手机号注册的 APP 账号，未注册账号无法接收。</span>
          </div>
        </div>
      </div>

      <!-- 成功视图 -->
      <div v-else class="pd-body">
        <div class="td-success">
          <div class="td-success-icon"><i class="fas fa-check"></i></div>
          <div class="td-success-title">测试消息发送成功</div>
          <div class="td-success-desc">
            <b>【{{ testType === 'sms' ? '短信' : '站内信' }}】{{ testTpl?.title }}</b> 已发送至 <b>{{ testSentReceiver }}</b><br>
            请注意查收，如长时间未收到请检查通道状态或联系客服。
          </div>
        </div>
      </div>

      <div class="pd-footer">
        <template v-if="!testSuccess">
          <button class="btn btn-outline" @click="showTestDialog = false">取消</button>
          <button class="btn btn-primary" @click="submitTest">
            <i class="fas fa-paper-plane"></i> 发送测试
          </button>
        </template>
        <template v-else>
          <button class="btn btn-outline" @click="showTestDialog = false">关闭</button>
          <button class="btn btn-primary" @click="resetTest">
            <i class="fas fa-redo"></i> 再发一条
          </button>
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSettingsByCategory, saveSetting, getBankAccount, saveBankAccount, getWalletAccount, saveWalletAccount, testSendTemplate } from '@/api/system'
import request from '@/api/request'

const router = useRouter()

const activeTab = ref('basic')
const accountTab = ref('bank')

// 银行账户信息（响应式，可编辑）
const bankInfo = reactive({
  bankName: '中国工商银行',
  cardNumber: '6222 **** **** 8888',
  holder: '快马日结科技有限公司',
  branch: '北京海淀支行',
  swiftCode: 'ICBKCNBJ',
  bankCode: '102100000458',
  accountType: '对公账户'
})
// 银行账户编辑弹窗
const showBankEditDialog = ref(false)
const bankEditForm = reactive({ ...bankInfo })
const openBankEdit = () => {
  Object.assign(bankEditForm, bankInfo)
  showBankEditDialog.value = true
}
const loadBankInfo = async () => {
  try {
    const res = await getBankAccount()
    const data = res.data || res
    if (data) {
      const parsed = typeof data === 'string' ? JSON.parse(data) : data
      Object.assign(bankInfo, parsed)
    }
  } catch (e) {
    console.warn('[Settings] 加载银行账户信息失败，使用默认值:', e)
  }
}
const saveBankEdit = async () => {
  try {
    await saveBankAccount({ ...bankEditForm })
    Object.assign(bankInfo, bankEditForm)
    showBankEditDialog.value = false
    ElMessage.success('银行账户信息已更新')
  } catch (e) {
    ElMessage.error('保存失败，请重试')
    console.error('[Settings] 保存银行账户信息失败:', e)
  }
}

// 钱包账户信息（响应式，可编辑）
const walletInfo = reactive({
  alipay: { account: 'kuaima@163.com', holder: '快马日结科技有限公司', isDefault: true },
  wechat: { account: 'k_m_riji001', holder: '快马日结科技有限公司', isDefault: false }
})
const showWalletEditDialog = ref(false)
const walletEditType = ref('alipay')
const walletEditForm = reactive({ account: '', holder: '', isDefault: false })

// 钱包类型元数据
const walletMeta = {
  alipay: {
    title: '编辑支付宝账户',
    badge: '支付宝', badgeClass: 'tag-blue',
    icon: 'fab fa-alipay', iconClass: 'alipay',
    name: '支付宝收款账户',
    accountLabel: '收款支付宝账号',
    holderLabel: '认证姓名 / 企业主体',
    placeholder: '请输入支付宝账号（邮箱或手机号）',
    hint: '用于接收平台服务费的支付宝账号，请确保账号准确。'
  },
  wechat: {
    title: '编辑微信账户',
    badge: '微信', badgeClass: 'tag-green',
    icon: 'fab fa-weixin', iconClass: 'wechat',
    name: '微信收款账户',
    accountLabel: '收款微信号',
    holderLabel: '认证姓名 / 企业主体',
    placeholder: '请输入收款微信号或微信商户号',
    hint: '用于接收平台服务费的微信账户，请确保账号准确。'
  }
}

/** 脱敏钱包账号（移植原型 maskWalletAccount） */
const maskWalletAccount = (type, val) => {
  val = (val || '').trim()
  if (!val) return '—'
  if (type === 'alipay') {
    if (val.indexOf('@') > -1) {
      const parts = val.split('@')
      const u = parts[0], d = parts[1] || ''
      if (u.length <= 3) return u + '****@' + d
      return u.slice(0, 1) + '****' + u.slice(-2) + '@' + d
    }
    if (/^\d{7,}$/.test(val)) return val.replace(/^(\d{3})\d+(\d{4})$/, '$1****$2')
    return val.length > 4 ? val.slice(0, 2) + '****' + val.slice(-2) : val
  }
  if (val.length <= 6) return val
  return val.slice(0, 4) + '****' + val.slice(-3)
}

const openWalletEdit = (type) => {
  walletEditType.value = type
  const d = walletInfo[type]
  walletEditForm.account = d.account
  walletEditForm.holder = d.holder
  walletEditForm.isDefault = d.isDefault
  showWalletEditDialog.value = true
}
const setWalletDefault = async (type) => {
  const other = type === 'alipay' ? 'wechat' : 'alipay'
  walletInfo[type].isDefault = true
  walletInfo[other].isDefault = false
  try {
    await saveWalletAccount({ alipay: walletInfo.alipay, wechat: walletInfo.wechat })
    ElMessage.success(`已将${walletMeta[type].name}设为默认`)
  } catch (e) {
    ElMessage.error('设置失败，请重试')
  }
}
const loadWalletInfo = async () => {
  try {
    const res = await getWalletAccount()
    const data = res.data || res
    if (data) {
      const parsed = typeof data === 'string' ? JSON.parse(data) : data
      if (parsed.alipay) Object.assign(walletInfo.alipay, parsed.alipay)
      if (parsed.wechat) Object.assign(walletInfo.wechat, parsed.wechat)
    }
  } catch (e) {
    console.warn('[Settings] 加载钱包账户信息失败，使用默认值:', e)
  }
}
const saveWalletEdit = async () => {
  try {
    const type = walletEditType.value
    const other = type === 'alipay' ? 'wechat' : 'alipay'
    // 若设为默认，取消另一个的默认
    if (walletEditForm.isDefault) {
      walletInfo[other].isDefault = false
    }
    walletInfo[type].account = walletEditForm.account
    walletInfo[type].holder = walletEditForm.holder
    walletInfo[type].isDefault = walletEditForm.isDefault
    // 确保至少有一个默认账户
    if (!walletInfo.alipay.isDefault && !walletInfo.wechat.isDefault) {
      walletInfo[other].isDefault = true
    }
    await saveWalletAccount({ alipay: walletInfo.alipay, wechat: walletInfo.wechat })
    showWalletEditDialog.value = false
    ElMessage.success(`${walletMeta[type].name}已更新`)
  } catch (e) {
    ElMessage.error('保存失败，请重试')
    console.error('[Settings] 保存钱包账户信息失败:', e)
  }
}

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
    type: 'sms', id: 'T001',
    title: '订单接单通知',
    content: '【快马日结】尊敬的{用户名}，您已成功接单{订单编号}，工作时间：{时间}，地点：{地点}，请准时到岗。'
  },
  {
    type: 'sms', id: 'T002',
    title: '工资到账通知',
    content: '【快马日结】尊敬的{用户名}，您的工资{金额}元已结算到账，订单号：{订单编号}，感谢您的辛勤劳动！'
  },
  {
    type: 'sms', id: 'T003',
    title: '审核结果通知',
    content: '【快马日结】尊敬的{用户名}，您提交的{审核类型}已{审核结果}，详情请登录APP查看。'
  }
]

const inSiteTemplates = [
  {
    type: 'message', id: 'M001',
    title: '系统公告推送',
    content: '尊敬的{用户名}，平台将于{时间}进行系统维护升级，届时服务将暂停使用，敬请谅解。'
  },
  {
    type: 'message', id: 'M002',
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

const onAddTemplate = (type) => {
  ElMessage.info('功能开发中')
}

const onEditTemplate = (name) => {
  ElMessage.info('功能开发中')
}

// ====== 模板预览 & 发送测试 ======
const sampleValues = {
  '用户名': '张师傅',
  '订单编号': 'DD202609010012',
  '时间': '2026-09-06 08:00',
  '地点': '深圳市南山区科技园',
  '金额': '280.00',
  '审核类型': '实名认证',
  '审核结果': '审核通过',
  '积分': '10',
  '次数': '3'
}

/** 渲染模板内容，highlight=true 时用示例值替换并高亮 */
const renderContent = (content, highlight) => {
  return content.replace(/\{([^}]+)\}/g, (match, key) => {
    if (highlight) {
      return `<span class="pd-hl">${sampleValues[key] || match}</span>`
    }
    return `<code>${match}</code>`
  })
}

/** 提取模板中的所有变量名 */
const getTemplateVars = (content) => {
  const vars = []
  const reg = /\{([^}]+)\}/g
  let m
  while ((m = reg.exec(content)) !== null) {
    if (vars.indexOf(m[1]) < 0) vars.push(m[1])
  }
  return vars
}

// 预览弹窗状态
const showPreviewDialog = ref(false)
const previewTpl = ref(null)
const previewType = ref('sms')

const onPreviewTemplate = (tpl) => {
  previewTpl.value = tpl
  previewType.value = tpl.type || 'sms'
  showPreviewDialog.value = true
}

// 发送测试弹窗状态
const showTestDialog = ref(false)
const testTpl = ref(null)
const testType = ref('sms')
const testReceiver = ref('')
const testSuccess = ref(false)
const testSentReceiver = ref('')

const onSendTest = (tpl) => {
  testTpl.value = tpl
  testType.value = tpl.type || 'sms'
  testReceiver.value = ''
  testSuccess.value = false
  showTestDialog.value = true
}

const submitTest = async () => {
  const receiver = testReceiver.value.trim()
  if (!/^1[3-9]\d{9}$/.test(receiver)) {
    ElMessage.warning('请输入正确的11位手机号')
    return
  }
  try {
    const renderedContent = testTpl.value ? testTpl.value.content.replace(/\{([^}]+)\}/g, (m, k) => sampleValues[k] || m) : ''
    const res = await testSendTemplate({
      type: testType.value,
      templateTitle: testTpl.value?.title || '',
      receiver,
      content: renderedContent
    })
    const data = res.data || res
    testSentReceiver.value = data.maskedReceiver || (receiver.substring(0, 3) + '****' + receiver.substring(7))
    testSuccess.value = true
  } catch (e) {
    ElMessage.error(e.message || '发送测试失败，请重试')
  }
}

const resetTest = () => {
  testReceiver.value = ''
  testSuccess.value = false
}

const onPublish = (name) => {
  ElMessage.info('功能开发中')
}

onMounted(() => {
  loadPlatformSettings()
  loadRulesSettings()
  loadBankInfo()
  loadWalletInfo()
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

/* ============ Dialog footer ============ */
.dialog-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

/* ============ Template cards（对齐原型紧凑风格） ============ */
.template-card {
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 12px;
  transition: border-color 0.2s;
}

.template-card:hover {
  border-color: var(--primary);
}

.template-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.template-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.tag {
  display: inline-block;
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 12px;
  font-weight: 500;
}

.tag-blue {
  background: #EFF6FF;
  color: #2563EB;
}

.tag-green {
  background: #F0FDF4;
  color: #10B981;
}

.template-content {
  font-size: 13px;
  color: var(--text-secondary);
  background: var(--bg-page);
  padding: 10px 12px;
  border-radius: 6px;
  line-height: 1.6;
}

.template-actions {
  display: flex;
  gap: 6px;
  margin-top: 10px;
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

/* ============ Button (与原型 admin.css 一致) ============ */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
  text-decoration: none;
  white-space: nowrap;
  font-family: inherit;
}

.btn-primary {
  background: var(--primary);
  color: #fff;
}

.btn-primary:hover {
  background: var(--primary-dark);
}

.btn-outline {
  background: #fff;
  color: var(--text-secondary);
  border: 1px solid var(--border);
}

.btn-outline:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.btn-sm {
  padding: 5px 12px;
  font-size: 12px;
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

/* ============ Wallet default tag ============ */
.wallet-default-tag {
  display: inline-block;
  background: var(--primary, #FF6B35);
  color: #fff;
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 4px;
  margin-left: 6px;
  font-weight: 500;
  vertical-align: middle;
}

/* ============ Wallet edit dialog ============ */
.wallet-edit-preview {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--bg-secondary, #f5f5f5);
  border-radius: 8px;
}
.wallet-edit-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}
.wallet-edit-icon.alipay {
  background: #1677ff;
  color: #fff;
}
.wallet-edit-icon.wechat {
  background: #07c160;
  color: #fff;
}
.wallet-edit-tips {
  margin-top: 8px;
  padding: 12px;
  background: #fffbe6;
  border: 1px solid #ffe58f;
  border-radius: 8px;
}
.wallet-edit-tip {
  font-size: 12px;
  color: #8c6d1f;
  line-height: 1.8;
  display: flex;
  align-items: flex-start;
  gap: 6px;
}
.wallet-edit-tip i {
  color: #52c41a;
  margin-top: 3px;
  flex-shrink: 0;
}

/* ============ 抽屉通用样式（对齐原型 .preview-drawer） ============ */
.preview-drawer-wrapper :deep(.el-drawer) {
  background: #F3F4F6;
}
.preview-drawer-wrapper :deep(.el-drawer__body) {
  padding: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
/* 发送测试抽屉叠加在预览抽屉之上 */
.test-drawer-wrapper :deep(.el-drawer) {
  z-index: 2100;
}
.test-drawer-wrapper :deep(.el-overlay) {
  z-index: 2099;
}

/* 抽屉头部 */
.pd-header {
  flex-shrink: 0;
  background: #fff;
  padding: 18px 20px;
  border-bottom: 1px solid var(--border);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}
.pd-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.pd-title i { color: var(--primary); }
.pd-sub {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary);
}
.pd-close {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  background: #F3F4F6;
  color: var(--text-secondary);
  font-size: 15px;
  cursor: pointer;
  flex-shrink: 0;
  transition: all 0.2s;
}
.pd-close:hover { background: #E5E7EB; color: var(--text-primary); }

/* 抽屉主体 */
.pd-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}
.pd-section {
  background: #fff;
  border-radius: 10px;
  padding: 18px;
  margin-bottom: 16px;
}
.pd-section-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}
.pd-section-title i { color: var(--primary); }
.pd-section-title .pd-hint {
  font-weight: 400;
  font-size: 12px;
  color: var(--text-muted);
  margin-left: 2px;
}
.pd-section-phone {
  background: linear-gradient(180deg, #FAFBFC 0%, #F3F4F6 100%);
}

/* ============ iPhone 模型（对齐原型 .pd-phone） ============ */
.pd-phone {
  --phone-w: 300px;
  width: var(--phone-w);
  margin: 4px auto 0;
  position: relative;
  background: linear-gradient(145deg, #2b313c 0%, #12151b 100%);
  border-radius: 44px;
  padding: 11px;
  box-shadow:
    0 18px 44px rgba(17, 24, 39, 0.28),
    0 4px 12px rgba(17, 24, 39, 0.18),
    inset 0 0 0 2px rgba(255, 255, 255, 0.07);
}
/* 侧边按键：电源键（右）+ 音量键（左） */
.pd-phone::before {
  content: '';
  position: absolute;
  right: -2.5px;
  top: 116px;
  width: 3px;
  height: 64px;
  background: #1a1e26;
  border-radius: 0 3px 3px 0;
}
.pd-phone::after {
  content: '';
  position: absolute;
  left: -2.5px;
  top: 96px;
  width: 3px;
  height: 48px;
  background: #1a1e26;
  border-radius: 3px 0 0 3px;
  box-shadow: 0 56px 0 #1a1e26;
}
.pd-phone-screen {
  position: relative;
  background: #fff;
  border-radius: 34px;
  overflow: hidden;
}
/* 灵动岛 */
.pd-phone-island {
  position: absolute;
  top: 9px;
  left: 50%;
  transform: translateX(-50%);
  width: 84px;
  height: 22px;
  background: #0a0a0c;
  border-radius: 12px;
  z-index: 20;
}
.pd-phone-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 13px 24px 2px;
  font-size: 12px;
  font-weight: 600;
  color: #111827;
  position: relative;
  z-index: 10;
}
.pd-phone-bar i { margin-left: 4px; font-size: 11px; }
.pd-phone-head {
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  padding: 6px 0 10px;
  border-bottom: 1px solid #f0f0f0;
  background: #fff;
}
.pd-phone-body {
  background: #F5F6F8;
  padding: 16px 12px 20px;
  min-height: 300px;
}
/* Home 指示条 */
.pd-phone-home {
  height: 18px;
  background: #F5F6F8;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: 6px;
}
.pd-phone-home::after {
  content: '';
  width: 104px;
  height: 4px;
  border-radius: 2px;
  background: #111827;
  opacity: 0.85;
}

/* 手机内短信气泡 */
.pd-sms { display: flex; gap: 9px; }
.pd-sms-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  flex-shrink: 0;
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  box-shadow: 0 4px 10px rgba(255, 107, 53, 0.3);
}
.pd-sms-main { flex: 1; min-width: 0; }
.pd-sms-top { display: flex; align-items: baseline; gap: 8px; margin-bottom: 5px; }
.pd-sms-name { font-size: 12px; font-weight: 600; color: #1f2937; }
.pd-sms-time { font-size: 10px; color: #9ca3af; }
.pd-sms-bubble {
  background: #fff;
  border-radius: 4px 12px 12px 12px;
  padding: 10px 12px;
  font-size: 12px;
  line-height: 1.8;
  color: #374151;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  word-break: break-all;
}

/* 手机内站内信通知卡片 */
.pd-notice {
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}
.pd-notice-head { display: flex; align-items: center; gap: 9px; margin-bottom: 8px; }
.pd-notice-icon {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}
.pd-notice-app { font-size: 12px; font-weight: 600; color: #1f2937; }
.pd-notice-now { font-size: 10px; color: #9ca3af; margin-top: 2px; }
.pd-notice-title { font-size: 13px; font-weight: 600; color: #1f2937; margin-bottom: 5px; }
.pd-notice-text { font-size: 12px; line-height: 1.8; color: #374151; word-break: break-all; }
.pd-notice-foot {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f3f4f6;
  font-size: 11px;
  color: var(--primary);
  text-align: center;
}
.pd-hl { color: var(--primary); font-weight: 600; }

/* 原始模板 */
.pd-raw {
  background: #F9FAFB;
  border: 1px dashed var(--border);
  border-radius: 8px;
  padding: 12px 14px;
  font-size: 12px;
  line-height: 1.9;
  color: var(--text-secondary);
  word-break: break-all;
}
.pd-raw code {
  color: var(--primary);
  background: rgba(255, 107, 53, 0.08);
  padding: 1px 5px;
  border-radius: 4px;
  font-size: 11px;
}

/* 变量示例 */
.pd-var-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  font-size: 12px;
  border-bottom: 1px solid #f7f7f7;
}
.pd-var-row:last-child { border-bottom: none; }
.pd-var-code {
  color: var(--primary);
  background: rgba(255, 107, 53, 0.08);
  padding: 2px 7px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  white-space: nowrap;
}
.pd-var-val { color: var(--text-secondary); margin-left: auto; text-align: right; word-break: break-all; }

/* 抽屉底部 */
.pd-footer {
  flex-shrink: 0;
  background: #fff;
  border-top: 1px solid var(--border);
  padding: 14px 20px;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
.pd-footer .btn { flex-shrink: 0; }

/* ============ 发送测试抽屉 ============ */
.td-field { margin-bottom: 16px; }
.td-field:last-child { margin-bottom: 0; }
.td-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 8px;
}
.td-label .required { color: #ef4444; }
.td-el-input :deep(.el-input__wrapper) {
  height: 40px;
  border-radius: 8px;
}
.td-hint { font-size: 12px; color: var(--text-muted); margin-top: 7px; line-height: 1.6; }
.td-content-preview {
  background: #FFF7F2;
  border: 1px solid #FFE2D1;
  border-radius: 8px;
  padding: 12px 14px;
  font-size: 12px;
  line-height: 1.9;
  color: var(--text-primary);
  word-break: break-all;
}
.td-content-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 9px;
}
.td-tip {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.7;
  margin-bottom: 10px;
}
.td-tip:last-child { margin-bottom: 0; }
.td-tip i { color: var(--primary); margin-top: 4px; font-size: 11px; flex-shrink: 0; }

/* 发送成功态 */
.td-success {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 40px 24px;
  text-align: center;
}
.td-success-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #ecfdf5;
  color: #10b981;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin: 0 auto 18px;
}
.td-success-title { font-size: 17px; font-weight: 600; color: var(--text-primary); margin-bottom: 10px; }
.td-success-desc { font-size: 13px; color: var(--text-muted); line-height: 1.9; margin-bottom: 26px; }
.td-success-desc b { color: var(--text-primary); }
</style>
