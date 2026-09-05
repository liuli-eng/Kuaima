<template>
  <div>
    <!-- 页面头部：与原型 admin-user.html 一致 -->
    <div class="af-page-header">
      <div style="display:flex; align-items:center; gap:12px;">
        <button class="back-btn" @click="goBack">
          <i class="fas fa-arrow-left"></i> 返回
        </button>
        <div>
          <h1 class="page-title">{{ isEdit ? '编辑管理员' : '新建账号' }}</h1>
          <p class="page-desc">{{ isEdit ? '修改管理员信息和权限配置' : '创建新的管理员账号并分配权限' }}</p>
        </div>
      </div>
    </div>

    <div class="edit-layout">
      <div style="display:flex; flex-direction:column; gap:20px;">
        <!-- 分区 1：基本信息 -->
        <div class="form-section">
          <div class="form-section-title">
            <span class="section-num">1</span> 基本信息
          </div>
          <div class="form-group">
            <div>
              <label class="form-label">管理员姓名<span class="required">*</span></label>
              <input type="text" class="form-input" v-model.trim="form.name" maxlength="50" placeholder="请输入真实姓名">
            </div>
            <div>
              <label class="form-label">登录账号<span class="required">*</span></label>
              <input type="text" class="form-input" v-model.trim="form.username" :disabled="isEdit" maxlength="50" placeholder="字母+数字组合，3-16位">
            </div>
            <div>
              <label class="form-label">手机号码<span class="required">*</span></label>
              <input type="text" class="form-input" v-model.trim="form.phone" maxlength="20" placeholder="请输入11位手机号">
            </div>
            <div>
              <label class="form-label">电子邮箱</label>
              <input type="email" class="form-input" v-model.trim="form.email" maxlength="100" placeholder="请输入邮箱地址">
            </div>
            <div v-if="!isEdit">
              <label class="form-label">登录密码<span class="required">*</span></label>
              <input type="password" class="form-input" v-model="form.password" maxlength="50" placeholder="至少8位，包含字母和数字">
            </div>
            <div v-if="!isEdit">
              <label class="form-label">确认密码<span class="required">*</span></label>
              <input type="password" class="form-input" v-model="form.password2" maxlength="50" placeholder="请再次输入密码">
            </div>
            <div class="full-width">
              <label class="form-label">备注说明</label>
              <textarea class="form-textarea" v-model.trim="form.remark" maxlength="255" placeholder="可填写该管理员的职责说明等信息"></textarea>
            </div>
          </div>
        </div>

        <!-- 分区 2：角色与权限 -->
        <div class="form-section">
          <div class="form-section-title">
            <span class="section-num">2</span> 角色与权限
          </div>
          <div style="margin-bottom:16px;">
            <label class="form-label">选择角色<span class="required">*</span></label>
            <select class="form-input" v-model="roleValue" @change="onRoleChange(roleValue)">
              <option v-for="r in roleOptions" :key="r.v" :value="r.v">{{ r.label }}</option>
            </select>
          </div>

          <div style="margin-top:20px;">
            <label class="form-label">权限分配<span class="required">*</span></label>
            <div class="permission-tree">
              <div class="perm-module" v-for="m in modules" :key="m.key" :class="{ open: m.open }">
                <div class="perm-module-header">
                  <div class="perm-module-info">
                    <input type="checkbox" :checked="moduleAllChecked(m)" @change="toggleAllModule(m, $event.target.checked)" @click.stop>
                    <label @click="toggleModule(m)"><i :class="m.icon"></i> {{ m.name }}</label>
                  </div>
                  <i class="fas fa-chevron-right perm-expand" @click="toggleModule(m)"></i>
                </div>
                <div class="perm-module-body" v-show="m.open">
                  <label class="perm-item" v-for="item in m.items" :key="item.key">
                    <input type="checkbox" v-model="item.checked"> {{ item.label }}
                  </label>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧信息卡 -->
      <div>
        <div class="sidebar-card">
          <div class="avatar-upload">
            <div class="avatar-preview" id="avatarPreview">{{ avatarLetter }}</div>
            <button class="avatar-upload-btn" @click="onUploadAvatar">
              <i class="fas fa-camera"></i> 上传头像
            </button>
          </div>

          <div style="margin-top:20px; padding-top:16px; border-top:1px solid var(--border);">
            <div class="status-row">
              <span class="status-row-label">账号状态</span>
              <div class="toggle-switch" :class="{ active: form.status === '启用' }" @click="toggleStatus"></div>
            </div>
            <div class="status-row">
              <span class="status-row-label">允许登录</span>
              <div class="toggle-switch" :class="{ active: allowLogin }" @click="allowLogin = !allowLogin"></div>
            </div>
            <div class="status-row">
              <span class="status-row-label">接收通知</span>
              <div class="toggle-switch" :class="{ active: receiveNoti }" @click="receiveNoti = !receiveNoti"></div>
            </div>
            <div class="status-row">
              <span class="status-row-label">最后登录</span>
              <span class="status-row-value" style="color:var(--text-muted);">{{ form.lastLoginTime ? fmtTime(form.lastLoginTime) : '--' }}</span>
            </div>
            <div class="status-row">
              <span class="status-row-label">创建时间</span>
              <span class="status-row-value" style="color:var(--text-muted);">{{ fmtTime(form.createTime) || '--' }}</span>
            </div>
          </div>

          <div class="action-buttons">
            <button class="btn btn-primary" :disabled="saving" @click="handleSave">
              <i class="fas fa-save"></i> {{ saving ? '保存中...' : (isEdit ? '保存修改' : '保存管理员') }}
            </button>
            <button class="btn btn-outline" @click="goBack">取消</button>
          </div>
          <div style="margin-top:10px;" v-if="isEdit">
            <button class="btn btn-danger" style="width:100%;" @click="handleDelete">
              <i class="fas fa-trash-alt"></i> 删除管理员
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminUser, createAdminUser, updateAdminUser, deleteAdminUser } from '@/api/system'

const router = useRouter()
const route = useRoute()

const isEdit = computed(() => route.query.mode === 'edit' || !!route.query.id)
const saving = ref(false)
const allowLogin = ref(true)
const receiveNoti = ref(true)

const form = reactive({
  name: '',
  username: '',
  phone: '',
  email: '',
  password: '',
  password2: '',
  remark: '',
  status: '启用',
  lastLoginTime: '',
  createTime: ''
})

// ====== 角色 ======
// 原型下拉框 6 种角色
const roleOptions = [
  { v: 'super', label: '超级管理员（全部权限）' },
  { v: 'admin', label: '管理员（核心管理权限）' },
  { v: 'audit', label: '审核员（审核相关权限）' },
  { v: 'oper', label: '运营（运营内容管理）' },
  { v: 'viewer', label: '查看员（仅查看权限）' },
  { v: 'custom', label: '自定义（手动分配）' }
]
const BACKEND_ROLE = { super: 'SUPER_ADMIN', admin: 'ADMIN', audit: 'EDITOR', viewer: 'VIEWER' }
const ROLE_FROM_BACKEND = Object.fromEntries(Object.entries(BACKEND_ROLE).map(([k, v]) => [v, k]))
const roleValue = ref('admin')

// ====== 权限树 ======
const modules = reactive([
  {
    key: 'permUser', name: '用户管理', icon: 'fas fa-users', open: false,
    items: [
      { key: 'u1', label: '零工管理', checked: false },
      { key: 'u2', label: '老板管理', checked: false },
      { key: 'u3', label: '账号编辑', checked: false },
      { key: 'u4', label: '账号删除', checked: false }
    ]
  },
  {
    key: 'permJob', name: '招工管理', icon: 'fas fa-briefcase', open: false,
    items: [
      { key: 'j1', label: '招工信息管理', checked: false },
      { key: 'j2', label: '招工审核', checked: false },
      { key: 'j3', label: '报名人员查看', checked: false },
      { key: 'j4', label: '上下架操作', checked: false }
    ]
  },
  {
    key: 'permOrder', name: '订单结算', icon: 'fas fa-clipboard-list', open: false,
    items: [
      { key: 'o1', label: '订单查看', checked: false },
      { key: 'o2', label: '订单处理', checked: false },
      { key: 'o3', label: '结算管理', checked: false },
      { key: 'o4', label: '财务报表', checked: false }
    ]
  },
  {
    key: 'permContent', name: '内容管理', icon: 'fas fa-cog', open: false,
    items: [
      { key: 'c1', label: '认证审核', checked: false },
      { key: 'c2', label: 'Banner管理', checked: false },
      { key: 'c3', label: '公告管理', checked: false },
      { key: 'c4', label: '规则管理', checked: false }
    ]
  },
  {
    key: 'permService', name: '消息客服', icon: 'fas fa-headset', open: false,
    items: [
      { key: 's1', label: '消息管理', checked: false },
      { key: 's2', label: '客服聊天', checked: false },
      { key: 's3', label: '举报处理', checked: false }
    ]
  },
  {
    key: 'permSystem', name: '系统管理', icon: 'fas fa-shield-alt', open: false,
    items: [
      { key: 'sys1', label: '积分管理', checked: false },
      { key: 'sys2', label: '黑名单管理', checked: false },
      { key: 'sys3', label: '系统设置', checked: false },
      { key: 'sys4', label: '操作日志', checked: false }
    ]
  }
])

const moduleAllChecked = (m) => m.items.every(i => i.checked)
const toggleModule = (m) => { m.open = !m.open }
const toggleAllModule = (m, checked) => {
  m.items.forEach(i => { i.checked = checked })
}

const onRoleChange = (v) => {
  roleValue.value = v
  if (v === 'super') {
    // 原型：超级管理员自动全选并展开所有模块
    modules.forEach(m => {
      m.items.forEach(i => { i.checked = true })
      m.open = true
    })
  }
}

// ====== 头像 ======
const avatarLetter = computed(() => (form.name || '').charAt(0) || '管')
const onUploadAvatar = () => {
  ElMessage.info('头像上传功能暂未开放，当前展示姓名首字头像')
}

// ====== 工具 ======
const fmtTime = (v) => {
  if (!v) return ''
  const s = String(v)
  if (s.includes('T')) return s.replace('T', ' ').slice(0, 16)
  return s.length > 16 ? s.slice(0, s.lastIndexOf(':')) : s
}
const goBack = () => {
  router.push('/admin/admin-user')
}
const toggleStatus = () => {
  form.status = form.status === '启用' ? '禁用' : '启用'
}

// ====== 数据加载（编辑模式） ======
const loadDetail = async () => {
  try {
    const res = await getAdminUser(route.query.id)
    const d = res?.data || {}
    form.name = d.name || ''
    form.username = d.username || ''
    form.phone = d.phone || ''
    form.email = d.email || ''
    form.remark = d.remark || ''
    form.status = d.status || '启用'
    form.lastLoginTime = d.lastLoginTime || ''
    form.createTime = d.createTime || ''
    roleValue.value = ROLE_FROM_BACKEND[d.role] || 'viewer'
    // 回显权限树
    if (d.permissions) {
      try {
        const permData = typeof d.permissions === 'string' ? JSON.parse(d.permissions) : d.permissions
        modules.forEach(m => {
          const modPerm = permData[m.key]
          if (modPerm) {
            m.items.forEach(i => { i.checked = !!modPerm[i.key] })
          }
        })
      } catch (e) {
        console.warn('[AdminUserForm] 解析权限数据失败:', e)
      }
    }
  } catch (e) {
    console.warn('[AdminUserForm] 加载详情失败:', e)
  }
}

/** 收集权限树勾选状态为 JSON 对象 */
const collectPermissions = () => {
  const result = {}
  modules.forEach(m => {
    const modPerm = {}
    m.items.forEach(i => { modPerm[i.key] = i.checked })
    result[m.key] = modPerm
  })
  return result
}

// ====== 提交 ======
const validate = () => {
  if (!form.name) return '请输入管理员姓名'
  if (!form.username) return '请输入登录账号'
  if (!/^[A-Za-z0-9]{3,16}$/.test(form.username)) return '登录账号需为 3-16 位字母或数字组合'
  if (!form.phone) return '请输入手机号码'
  if (!/^1\d{10}$/.test(form.phone)) return '请输入正确的11位手机号'
  if (form.email && !/^[\w.+-]+@[\w-]+(\.[\w-]+)+$/.test(form.email)) return '请输入正确的邮箱地址'
  // oper/custom 为原型预留角色，后端暂映射为 ADMIN
  const role = BACKEND_ROLE[roleValue.value] || 'ADMIN'
  if (!isEdit.value) {
    if (!form.password) return '请设置登录密码'
    if (form.password.length < 8) return '密码至少8位'
    if (form.password !== form.password2) return '两次输入的密码不一致'
  }
  return ''
}

const handleSave = async () => {
  const msg = validate()
  if (msg) {
    ElMessage.warning(msg)
    return
  }
  saving.value = true
  try {
    const payload = {
      name: form.name,
      phone: form.phone,
      email: form.email || null,
      remark: form.remark || null,
      role: BACKEND_ROLE[roleValue.value] || 'ADMIN',
      status: form.status,
      permissions: JSON.stringify(collectPermissions())
    }
    if (isEdit.value) {
      if (form.password) payload.password = form.password
      await updateAdminUser(route.query.id, payload)
      ElMessage.success('管理员信息保存成功！')
    } else {
      payload.username = form.username
      payload.password = form.password
      await createAdminUser(payload)
      ElMessage.success('管理员创建成功！')
    }
    router.push('/admin/admin-user')
  } catch (e) {
    console.warn('[AdminUserForm] 保存失败:', e)
    // 具体错误信息已由请求拦截器提示
  } finally {
    saving.value = false
  }
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除该管理员吗？此操作不可恢复。', '删除确认', { type: 'warning' })
    await deleteAdminUser(route.query.id)
    ElMessage.success('管理员已删除')
    router.push('/admin/admin-user')
  } catch (e) {
    if (e !== 'cancel') console.warn('[AdminUserForm] 删除失败:', e)
  }
}

onMounted(() => {
  if (isEdit.value) {
    loadDetail()
  }
})
</script>

<style scoped>
.af-page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}
.back-btn:hover {
  color: var(--primary);
  border-color: var(--primary);
}
.edit-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 20px;
  align-items: start;
}
.form-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}
.form-section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  gap: 8px;
}
.form-section-title .section-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: var(--primary);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}
.form-group {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px 24px;
}
.form-group .full-width {
  grid-column: 1 / -1;
}
.form-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 6px;
}
.form-label .required {
  color: var(--danger);
  margin-left: 2px;
}
.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: 9px 12px;
  border: 1px solid var(--border);
  border-radius: 6px;
  font-size: 14px;
  color: var(--text-primary);
  background: #fff;
  transition: border-color 0.2s, box-shadow 0.2s;
  font-family: inherit;
}
.form-input:disabled {
  background: var(--bg-page);
  color: var(--text-muted);
  cursor: not-allowed;
}
.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.1);
}
.form-textarea {
  resize: vertical;
  min-height: 80px;
}
.permission-tree {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.perm-module {
  border: 1px solid var(--border);
  border-radius: 6px;
  overflow: hidden;
}
.perm-module-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  background: var(--bg-page);
}
.perm-module-info {
  display: flex;
  align-items: center;
  gap: 8px;
}
.perm-module-info input[type='checkbox'] {
  accent-color: var(--primary);
  cursor: pointer;
}
.perm-module-info label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.perm-module-info i {
  color: var(--primary);
}
.perm-expand {
  font-size: 12px;
  color: var(--text-muted);
  cursor: pointer;
  transition: transform 0.2s;
  padding: 4px;
}
.perm-module.open .perm-expand {
  transform: rotate(90deg);
}
.perm-module-body {
  padding: 10px 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
}
.perm-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.15s;
}
.perm-item:hover {
  background: var(--bg-page);
}
.perm-item input {
  accent-color: var(--primary);
}
.sidebar-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  position: sticky;
  top: 20px;
}
.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.avatar-preview {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6b35, #ff8c42);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 32px;
  font-weight: 600;
}
.avatar-upload-btn {
  padding: 6px 14px;
  font-size: 12px;
  color: var(--primary);
  border: 1px dashed var(--primary);
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
}
.avatar-upload-btn:hover {
  background: #fff8f3;
}
.status-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid var(--border);
}
.status-row:last-child {
  border-bottom: none;
}
.status-row-label {
  font-size: 13px;
  color: var(--text-secondary);
}
.status-row-value {
  font-size: 13px;
  font-weight: 500;
}
.toggle-switch {
  position: relative;
  width: 44px;
  height: 24px;
  background: #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s;
  flex-shrink: 0;
}
.toggle-switch.active {
  background: var(--primary);
}
.toggle-switch::after {
  content: '';
  position: absolute;
  width: 20px;
  height: 20px;
  background: #fff;
  border-radius: 50%;
  top: 2px;
  left: 2px;
  transition: left 0.2s;
}
.toggle-switch.active::after {
  left: 22px;
}
.action-buttons {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 9px 20px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 6px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.15s;
  white-space: nowrap;
  font-family: inherit;
}
.btn-primary {
  background: var(--primary);
  color: #fff;
  flex: 1;
}
.btn-primary:hover {
  background: var(--primary-dark);
}
.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
.btn-outline {
  background: #fff;
  color: var(--text-secondary);
  border-color: var(--border);
}
.btn-outline:hover {
  color: var(--primary);
  border-color: var(--primary);
}
.btn-danger {
  background: #fff;
  color: var(--danger);
  border-color: var(--danger);
}
.btn-danger:hover {
  background: var(--danger);
  color: #fff;
}

@media (max-width: 1200px) {
  .edit-layout {
    grid-template-columns: 1fr;
  }
  .sidebar-card {
    position: static;
  }
}
</style>
