<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">员工管理</h1>
      <p class="page-desc">企业内部员工团队协作管理，包含申请审批、成员邀请与角色权限管理</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-card-header"><span class="stat-card-title">员工总数</span>
          <div class="stat-card-icon"><i class="fas fa-users"></i></div></div>
        <div class="stat-card-value">{{ stats.total }}</div>
        <div class="stat-card-change up"><i class="fas fa-arrow-up"></i><span>企业内部在册员工</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header"><span class="stat-card-title">在职员工</span>
          <div class="stat-card-icon green"><i class="fas fa-user-check"></i></div></div>
        <div class="stat-card-value">{{ stats.active }}</div>
        <div class="stat-card-change up"><i class="fas fa-arrow-up"></i><span>在职率 {{ activeRate }}%</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header"><span class="stat-card-title">本月新增</span>
          <div class="stat-card-icon blue"><i class="fas fa-user-plus"></i></div></div>
        <div class="stat-card-value">{{ stats.monthNew }}</div>
        <div class="stat-card-change up"><i class="fas fa-arrow-up"></i><span>较上月新增</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-card-header"><span class="stat-card-title">待审批申请</span>
          <div class="stat-card-icon yellow"><i class="fas fa-file-signature"></i></div></div>
        <div class="stat-card-value">{{ stats.pending }}</div>
        <div class="stat-card-change down"><i class="fas fa-clock"></i><span>需尽快处理</span></div>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <div class="card-title title-switch-group">
          <span class="title-switch" :class="{ active: view === 'member' }" @click="switchView('member')">员工列表</span>
          <span class="title-switch-divider"></span>
          <span class="title-switch" :class="{ active: view === 'apply' }" @click="switchView('apply')">申请列表<span class="count-pill" v-if="stats.pending">{{ stats.pending }}</span></span>
          <span class="title-switch-divider"></span>
          <span class="title-switch" :class="{ active: view === 'role' }" @click="switchView('role')">角色管理</span>
        </div>
        <div class="card-header-actions" v-if="view === 'role'">
          <button class="btn btn-primary btn-sm" @click="openRoleModal()"><i class="fas fa-plus"></i> 新建角色</button>
        </div>
      </div>

      <!-- 员工列表 -->
      <div v-show="view === 'member'">
        <div class="filter-bar">
          <el-select v-model="empFilters.company" placeholder="所属公司" clearable style="width: 200px;">
            <el-option v-for="c in companyOptions" :key="c" :label="c" :value="c" />
          </el-select>
          <el-select v-model="empFilters.role" placeholder="角色" clearable style="width: 130px;">
            <el-option label="全部角色" value="" />
            <el-option label="超级管理员" value="super" />
            <el-option label="管理员" value="admin" />
            <el-option label="员工" value="staff" />
          </el-select>
          <el-select v-model="empFilters.status" placeholder="状态" clearable style="width: 120px;">
            <el-option label="全部状态" value="" />
            <el-option label="在职" value="active" />
            <el-option label="已停用" value="frozen" />
          </el-select>
          <el-input v-model="empFilters.keyword" placeholder="员工姓名 / 手机号" clearable style="width: 200px;" prefix-icon="Search" />
          <button class="btn btn-primary btn-sm" @click="loadEmployees"><i class="fas fa-search"></i> 查询</button>
          <button class="btn btn-outline btn-sm" @click="resetEmpFilters"><i class="fas fa-rotate-left"></i> 重置</button>
          <button class="btn btn-outline btn-sm" :disabled="!selectedIds.length" @click="batchStatus('active')"><i class="fas fa-user-check"></i> 批量启用</button>
          <button class="btn btn-outline btn-sm" :disabled="!selectedIds.length" @click="batchStatus('frozen')"><i class="fas fa-ban"></i> 批量停用</button>
          <button class="btn btn-primary btn-sm" @click="openPermModal('batch')"><i class="fas fa-key"></i> 批量设置权限</button>
          <button class="btn btn-primary btn-sm" style="margin-left:auto;" @click="inviteVisible = true"><i class="fas fa-user-plus"></i> 邀请新成员</button>
        </div>
        <el-table :data="employees" stripe @selection-change="onSelect"
          :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }">
          <el-table-column type="selection" width="44" />
          <el-table-column label="员工" min-width="150">
            <template #default="{ row }">
              <div class="avatar-cell">
                <div class="mini-avatar" :class="avatarColor(row.name)">{{ (row.name || '?').charAt(0) }}</div>
                <div class="emp-info"><span class="emp-name">{{ row.name }}</span><span class="emp-phone">{{ row.phone }}</span></div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="job" label="职位" show-overflow-tooltip />
          <el-table-column prop="company" label="所属公司" show-overflow-tooltip />
          <el-table-column label="角色" width="110">
            <template #default="{ row }"><span :class="['role-tag', roleCls(row.role)]">{{ row.roleName || roleText(row.role) }}</span></template>
          </el-table-column>
          <el-table-column prop="proj" label="所属项目" show-overflow-tooltip />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <span :class="['status-badge', row.status === 'active' ? 'success' : 'default']">{{ row.status === 'active' ? '在职' : '已停用' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="addTime" label="添加时间" show-overflow-tooltip />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openDetail(row)">详情</el-button>
              <el-button link :type="row.status === 'active' ? 'danger' : 'success'" size="small" @click="toggleStatus(row)">
                {{ row.status === 'active' ? '停用' : '启用' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination">
          <div class="pagination-info">共 {{ empTotal }} 条记录</div>
          <el-pagination v-model:current-page="empPage" v-model:page-size="empSize"
            :page-sizes="[10, 20, 50, 100]" :total="empTotal" layout="sizes, prev, pager, next, jumper" background
            @size-change="loadEmployees" @current-change="loadEmployees" />
        </div>
      </div>

      <!-- 申请列表 -->
      <div v-show="view === 'apply'">
        <div class="filter-bar">
          <el-select v-model="applyFilters.status" placeholder="状态" clearable style="width: 130px;">
            <el-option label="全部状态" value="" />
            <el-option label="待审批" value="pending" />
            <el-option label="已同意" value="approved" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
          <el-date-picker v-model="applyFilters.dateRange" type="daterange" range-separator="至"
            start-placeholder="申请开始" end-placeholder="申请结束" style="width: 240px;" value-format="YYYY-MM-DD" />
          <el-input v-model="applyFilters.keyword" placeholder="申请人姓名 / 手机号" clearable style="width: 200px;" prefix-icon="Search" />
          <button class="btn btn-primary btn-sm" @click="loadApplies"><i class="fas fa-search"></i> 查询</button>
          <button class="btn btn-outline btn-sm" @click="resetApplyFilters"><i class="fas fa-rotate-left"></i> 重置</button>
        </div>
        <el-table :data="applies" stripe :header-cell-style="{ background: '#F9FAFB', color: '#6B7280', fontWeight: 500 }">
          <el-table-column prop="name" label="申请人" show-overflow-tooltip />
          <el-table-column prop="job" label="申请职位" show-overflow-tooltip />
          <el-table-column prop="applyRole" label="申请角色" show-overflow-tooltip />
          <el-table-column prop="intentProject" label="意向项目" show-overflow-tooltip />
          <el-table-column prop="applyTime" label="申请时间" show-overflow-tooltip />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <span :class="['status-badge', applyStatusClass(row.status)]">{{ applyStatusText(row.status) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openApplyDetail(row)">查看</el-button>
              <el-button v-if="row.status === 'pending'" link type="success" size="small" @click="doApproveApply(row)">同意</el-button>
              <el-button v-if="row.status === 'pending'" link type="danger" size="small" @click="doRejectApply(row)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 角色管理 -->
      <div v-show="view === 'role'">
        <div class="role-grid">
          <div class="role-card" v-for="r in roles" :key="r.id">
            <div class="role-card-head">
              <div class="role-icon" :style="{ background: (r.color || '#FF6B35') + '15', color: r.color || '#FF6B35' }">
                <i class="fas" :class="r.icon || 'fa-user-shield'"></i>
              </div>
              <div style="flex:1;">
                <div class="role-name">{{ r.name }}
                  <span v-if="r.system" class="sys-badge">系统内置</span>
                </div>
                <div class="role-desc">{{ r.description }}</div>
              </div>
            </div>
            <div class="role-perm-tags">
              <span v-for="s in roleSectionNames(r)" :key="s" class="perm-tag" :style="{ color: r.color || '#FF6B35', background: (r.color || '#FF6B35') + '15' }">{{ s }}</span>
            </div>
            <div class="role-foot">
              <span class="member-pill">成员 {{ roleMemberCount(r) }}</span>
              <div style="margin-left:auto; display:flex; gap:8px;">
                <el-button link type="primary" size="small" @click="openRoleModal(r)">编辑</el-button>
                <el-button v-if="!r.system" link type="danger" size="small" @click="removeRole(r)">删除</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 邀请新成员 -->
    <el-dialog v-model="inviteVisible" title="邀请新成员" width="520px">
      <el-form :model="inviteForm" label-width="90px">
        <el-form-item label="职位名称"><el-input v-model="inviteForm.job" placeholder="如：项目负责人" /></el-form-item>
        <el-form-item label="角色权限">
          <el-select v-model="inviteForm.role" style="width:100%;">
            <el-option label="员工（仅查看与日常操作）" value="staff" />
            <el-option label="管理员（可管理项目与发薪）" value="admin" />
            <el-option label="超级管理员（拥有全部权限）" value="super" />
          </el-select>
        </el-form-item>
        <el-form-item label="邀请链接">
          <div class="invite-link-box">
            <span>{{ inviteLink }}</span>
            <button class="btn btn-outline btn-sm" @click="copyLink">复制</button>
          </div>
          <div class="invite-qrcode"></div>
          <p class="invite-tip">微信扫码 / 点击链接即可申请加入企业，申请后需管理员审批</p>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inviteVisible = false">取消</el-button>
        <el-button type="primary" @click="sendInvite">发送邀请</el-button>
      </template>
    </el-dialog>

    <!-- 员工详情 -->
    <el-dialog v-model="detailVisible" title="员工详情" width="600px">
      <div v-if="detailEmp" class="detail-wrap">
        <div class="detail-head">
          <div class="mini-avatar" :class="avatarColor(detailEmp.name)" style="width:52px;height:52px;font-size:20px;">{{ (detailEmp.name || '?').charAt(0) }}</div>
          <div>
            <div class="detail-name">{{ detailEmp.name }}</div>
            <div class="detail-sub">{{ detailEmp.roleName || roleText(detailEmp.role) }} · {{ detailEmp.company }}</div>
          </div>
        </div>
        <div class="detail-section-title">基本信息</div>
        <div class="detail-info-grid">
          <div class="detail-info-item"><span class="detail-info-label">手机号</span><span class="detail-info-value">{{ detailEmp.phone || '—' }}</span></div>
          <div class="detail-info-item"><span class="detail-info-label">添加时间</span><span class="detail-info-value">{{ detailEmp.addTime || '—' }}</span></div>
        </div>
        <div class="detail-section-title" style="margin-top:16px;">任职信息</div>
        <div class="detail-info-grid">
          <div class="detail-info-item"><span class="detail-info-label">职位</span><span class="detail-info-value">{{ detailEmp.job || '—' }}</span></div>
          <div class="detail-info-item"><span class="detail-info-label">角色权限</span><span class="detail-info-value">{{ detailEmp.roleName || roleText(detailEmp.role) }}</span></div>
          <div class="detail-info-item"><span class="detail-info-label">所属公司</span><span class="detail-info-value">{{ detailEmp.company || '—' }}</span></div>
          <div class="detail-info-item"><span class="detail-info-label">所属项目</span><span class="detail-info-value">{{ detailEmp.proj || '—' }}</span></div>
        </div>
        <div class="detail-section-title" style="margin-top:16px;">工作台权限</div>
        <div class="perm-tag-list">
          <span v-for="p in detailPermNames" :key="p" class="perm-tag" style="color:#FF6B35;background:#FF6B3515;">{{ p }}</span>
          <span v-if="!detailPermNames.length" style="color:var(--text-muted);font-size:13px;">—</span>
        </div>
      </div>
      <template #footer>
        <el-button type="danger" @click="removeEmp">移除员工</el-button>
        <el-button type="primary" @click="openPermModal('detail', detailEmp)">保存修改</el-button>
      </template>
    </el-dialog>

    <!-- 权限设置（批量 / 详情） -->
    <el-dialog v-model="permVisible" :title="permMode === 'batch' ? '批量设置权限' : '设置权限'" width="560px">
      <p class="perm-tip" v-if="permMode === 'batch'">已选 {{ selectedIds.length }} 名员工，勾选下方模块权限后统一保存。</p>
      <div v-for="grp in PERM_TREE" :key="grp.key" class="perm-group">
        <div class="perm-group-title">{{ grp.section }}</div>
        <el-checkbox-group v-model="permChecked">
          <el-checkbox v-for="it in grp.items" :key="it.key" :value="it.key" border>{{ it.name }}</el-checkbox>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="permVisible = false">取消</el-button>
        <el-button type="primary" @click="savePerm">确认设置</el-button>
      </template>
    </el-dialog>

    <!-- 新建 / 编辑角色 -->
    <el-dialog v-model="roleVisible" :title="editingRole ? '编辑角色' : '新建角色'" width="640px">
      <el-form :model="roleForm" label-width="90px">
        <el-form-item label="角色名称"><el-input v-model="roleForm.name" placeholder="请输入角色名称" /></el-form-item>
        <el-form-item label="角色描述"><el-input v-model="roleForm.description" placeholder="请输入角色描述（选填）" /></el-form-item>
        <el-form-item label="模块权限">
          <div v-for="grp in PERM_TREE" :key="grp.key" class="perm-group">
            <div class="perm-group-title">{{ grp.section }}</div>
            <el-checkbox-group v-model="roleForm.permissions">
              <el-checkbox v-for="it in grp.items" :key="it.key" :value="it.key" border>{{ it.name }}</el-checkbox>
            </el-checkbox-group>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button v-if="editingRole && !editingRole.system" type="danger" @click="removeRole(editingRole)">删除角色</el-button>
        <div style="flex:1;"></div>
        <el-button @click="roleVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRole">保存</el-button>
      </template>
    </el-dialog>

    <!-- 申请详情 -->
    <el-dialog v-model="applyDetailVisible" title="申请人详情" width="600px">
      <div v-if="applyDetail" class="detail-wrap">
        <div class="detail-head">
          <div class="mini-avatar" :class="avatarColor(applyDetail.name)" style="width:52px;height:52px;font-size:20px;">{{ (applyDetail.name || '?').charAt(0) }}</div>
          <div>
            <div class="detail-name">{{ applyDetail.name }}
              <span :class="['status-badge', applyStatusClass(applyDetail.status)]">{{ applyStatusText(applyDetail.status) }}</span>
            </div>
            <div class="detail-sub">{{ applyDetail.phone }} · 申请 {{ applyDetail.job }}</div>
          </div>
        </div>
        <div class="detail-section-title">基本信息</div>
        <div class="detail-info-grid">
          <div class="detail-info-item"><span class="detail-info-label">手机号</span><span class="detail-info-value">{{ applyDetail.phone || '—' }}</span></div>
          <div class="detail-info-item"><span class="detail-info-label">所属公司</span><span class="detail-info-value">{{ applyDetail.company || '—' }}</span></div>
        </div>
        <div class="detail-section-title" style="margin-top:16px;">申请岗位信息</div>
        <div class="detail-info-grid">
          <div class="detail-info-item"><span class="detail-info-label">申请职位</span><span class="detail-info-value">{{ applyDetail.job || '—' }}</span></div>
          <div class="detail-info-item"><span class="detail-info-label">申请角色</span><span class="detail-info-value">{{ applyDetail.applyRole || '—' }}</span></div>
          <div class="detail-info-item"><span class="detail-info-label">意向项目</span><span class="detail-info-value">{{ applyDetail.intentProject || '—' }}</span></div>
          <div class="detail-info-item"><span class="detail-info-label">期望薪资</span><span class="detail-info-value">{{ applyDetail.salaryExpect || '—' }}</span></div>
          <div class="detail-info-item"><span class="detail-info-label">工时类型</span><span class="detail-info-value">{{ applyDetail.workTime || '—' }}</span></div>
          <div class="detail-info-item"><span class="detail-info-label">期望入职</span><span class="detail-info-value">{{ applyDetail.joinDate || '—' }}</span></div>
        </div>
      </div>
      <template #footer>
        <el-button @click="applyDetailVisible = false">关闭</el-button>
        <template v-if="applyDetail && applyDetail.status === 'pending'">
          <el-button type="danger" @click="doRejectApply(applyDetail)">拒绝</el-button>
          <el-button type="primary" @click="doApproveApply(applyDetail)">同意</el-button>
        </template>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  listEmployees, getEmployee, setEmployeeStatus, deleteEmployee, updateEmployee,
  listRoles, permissionTree, createRole, updateRole, deleteRole,
  listJoinApplies, approveApply, rejectApply
} from '@/api/employee'

/* ---------- 权限树（结构对齐后端 defaultPermissionTree：组 key + 叶子 key） ---------- */
// 后端权限以嵌套对象存储：{ permOrg:{org_member:true}, permPayroll:{batch_payroll:true,...} }
const PERM_TREE = [
  { key: 'permOrg', name: '组织管理', items: [
    { key: 'org_member', name: '成员管理' }, { key: 'org_project', name: '项目管理' } ] },
  { key: 'permPayroll', name: '闪电发薪', items: [
    { key: 'batch_payroll', name: '批量发薪' }, { key: 'payroll_approve', name: '待我审批' },
    { key: 'transfer_record', name: '转账记录' }, { key: 'balance_view', name: '余额查询' } ] },
]
const PERM_NAME = {}        // 叶子 key -> 名称
const PERM_GROUP_NAME = {}  // 组 key -> 名称
const rebuildPermMaps = () => {
  for (const k of Object.keys(PERM_NAME)) delete PERM_NAME[k]
  for (const k of Object.keys(PERM_GROUP_NAME)) delete PERM_GROUP_NAME[k]
  PERM_TREE.forEach((g) => {
    PERM_GROUP_NAME[g.key] = g.name
    g.items.forEach((i) => { PERM_NAME[i.key] = i.name })
  })
}
rebuildPermMaps()
// 嵌套对象 <-> 扁平叶子 key 数组
const permsToChecked = (p) => {
  const arr = []
  try {
    const o = typeof p === 'string' ? JSON.parse(p) : p
    if (o && typeof o === 'object' && !Array.isArray(o)) {
      Object.keys(o).forEach((g) => {
        const leaves = o[g]
        if (leaves && typeof leaves === 'object') Object.keys(leaves).forEach((k) => { if (leaves[k]) arr.push(k) })
      })
    }
  } catch { /* ignore */ }
  return arr
}
const checkedToPerms = (arr) => {
  const obj = {}
  PERM_TREE.forEach((g) => {
    const leaves = {}
    g.items.forEach((i) => { leaves[i.key] = Array.isArray(arr) && arr.includes(i.key) })
    obj[g.key] = leaves
  })
  return obj
}
const parsePermObj = (p) => {
  if (!p) return {}
  try { const o = typeof p === 'string' ? JSON.parse(p) : p; return (o && typeof o === 'object' && !Array.isArray(o)) ? o : {} } catch { return {} }
}

/* ---------- 公共 ---------- */
const avatarColors = ['orange', 'blue', 'green', 'purple']
const avatarColor = (seed) => {
  let h = 0
  for (let i = 0; i < (seed || '').length; i++) h = (h * 31 + seed.charCodeAt(i)) >>> 0
  return avatarColors[h % avatarColors.length]
}
const roleText = (r) => ({ super: '超级管理员', admin: '管理员', staff: '员工' }[r] || '员工')
const roleCls = (r) => ({ super: 'super', admin: 'admin', staff: 'staff' }[r] || 'staff')
const applyStatusText = (s) => ({ pending: '待审批', approved: '已同意', rejected: '已拒绝' }[s] || s || '—')
const applyStatusClass = (s) => ({ pending: 'warning', approved: 'success', rejected: 'danger' }[s] || 'default')
const formatTime = (t) => { if (!t) return '—'; const s = String(t).replace('T', ' '); return s.length > 10 ? s.substring(0, 10) : s }

const view = ref('member')
const switchView = (v) => {
  view.value = v
  if (v === 'member') loadEmployees()
  else if (v === 'apply') loadApplies()
  else loadRoles()
}

/* ---------- 统计 ---------- */
const stats = reactive({ total: 0, active: 0, monthNew: 0, pending: 0 })
const activeRate = computed(() => stats.total ? ((stats.active / stats.total) * 100).toFixed(1) : '0.0')

/* ---------- 员工列表 ---------- */
const employees = ref([])
const empTotal = ref(0)
const empPage = ref(1)
const empSize = ref(10)
const empFilters = reactive({ company: '', role: '', status: '', keyword: '' })
const selectedIds = ref([])
const selectedNames = ref([])

const companyOptions = computed(() => {
  const set = new Set(employees.value.map((e) => e.company).filter(Boolean))
  return Array.from(set)
})

const onSelect = (rows) => {
  selectedIds.value = rows.map((r) => r.id)
  selectedNames.value = rows.map((r) => r.name)
}

const normalizeEmp = (e) => ({
  id: e.id,
  name: e.name,
  phone: e.phone || '—',
  job: e.job || '—',
  company: e.company || '—',
  role: e.role || 'staff',
  roleName: e.roleName || '',
  proj: e.projectName || '—',
  status: e.status || 'active',
  addTime: formatTime(e.addTime),
  permissions: parsePermObj(e.permissions),
})

const loadEmployees = async () => {
  try {
    const res = await listEmployees({
      keyword: empFilters.keyword || undefined,
      company: empFilters.company || undefined,
      role: empFilters.role || undefined,
      status: empFilters.status || undefined,
      page: empPage.value - 1,
      size: empSize.value,
    })
    const list = res.data?.content || res.data?.list || (Array.isArray(res.data) ? res.data : [])
    employees.value = list.map(normalizeEmp)
    empTotal.value = res.total ?? list.length
    stats.total = res.total ?? employees.value.length
    stats.active = employees.value.filter((e) => e.status === 'active').length
  } catch (err) {
    console.warn('[EmployeeManage] 员工加载失败:', err.message)
    employees.value = []
    empTotal.value = 0
  }
}
const resetEmpFilters = () => {
  empFilters.company = ''; empFilters.role = ''; empFilters.status = ''; empFilters.keyword = ''
  loadEmployees()
}

const toggleStatus = async (row) => {
  const next = row.status === 'active' ? 'frozen' : 'active'
  try { await setEmployeeStatus(row.id, next); ElMessage.success(next === 'active' ? '已启用' : '已停用'); loadEmployees() }
  catch (err) { ElMessage.error('操作失败') }
}
const batchStatus = async (status) => {
  if (!selectedIds.value.length) return
  try {
    await Promise.all(selectedIds.value.map((id) => setEmployeeStatus(id, status)))
    ElMessage.success(`已${status === 'active' ? '启用' : '停用'} ${selectedIds.value.length} 人`)
    loadEmployees()
  } catch (err) { ElMessage.error('批量操作失败') }
}

/* ---------- 员工详情 ---------- */
const detailVisible = ref(false)
const detailEmp = ref(null)
const detailPermNames = computed(() => {
  const perms = detailEmp.value?.permissions || {}
  const names = []
  Object.keys(perms).forEach((g) => {
    const leaves = perms[g]
    if (leaves && typeof leaves === 'object') {
      Object.keys(leaves).forEach((k) => { if (leaves[k]) names.push(PERM_NAME[k] || k) })
    }
  })
  return names
})

const openDetail = async (row) => {
  try {
    const res = await getEmployee(row.id)
    detailEmp.value = normalizeEmp(res.data || row)
  } catch {
    detailEmp.value = normalizeEmp(row)
  }
  detailVisible.value = true
}
const removeEmp = async () => {
  if (!detailEmp.value) return
  try { await deleteEmployee(detailEmp.value.id); ElMessage.success('已移除员工'); detailVisible.value = false; loadEmployees() }
  catch (err) { ElMessage.error('移除失败') }
}

/* ---------- 邀请 ---------- */
const inviteVisible = ref(false)
const inviteForm = reactive({ job: '', role: 'staff' })
const inviteLink = computed(() => `https://m.kuaima.com/invite/e/${Math.random().toString(36).slice(2, 10)}?from=console`)
const copyLink = () => { navigator.clipboard?.writeText(inviteLink.value); ElMessage.success('邀请链接已复制') }
const sendInvite = () => { ElMessage.success('邀请已发送'); inviteVisible.value = false }

/* ---------- 权限弹窗 ---------- */
const permVisible = ref(false)
const permMode = ref('batch')
const permChecked = ref([])
const permTarget = ref(null)
const openPermModal = (mode, emp) => {
  permMode.value = mode
  permTarget.value = emp || null
  permChecked.value = mode === 'detail' && emp ? permsToChecked(emp.permissions) : []
  permVisible.value = true
}
const savePerm = async () => {
  const payload = { permissions: JSON.stringify(checkedToPerms(permChecked.value)) }
  try {
    if (permMode.value === 'batch') {
      if (!selectedIds.value.length) { ElMessage.warning('请先勾选员工'); return }
      await Promise.all(selectedIds.value.map((id) => updateEmployee(id, payload)))
    } else if (permTarget.value) {
      await updateEmployee(permTarget.value.id, payload)
    }
    ElMessage.success('权限已保存')
    permVisible.value = false
    loadEmployees()
  } catch (err) { ElMessage.error('保存失败') }
}

/* ---------- 角色管理 ---------- */
const roles = ref([])
const roleVisible = ref(false)
const editingRole = ref(null)
const roleForm = reactive({ name: '', description: '', permissions: [], color: '#FF6B35', icon: 'fa-user-shield' })

const loadRoles = async () => {
  try {
    const res = await listRoles()
    roles.value = res.data || []
  } catch (err) {
    console.warn('[EmployeeManage] 角色加载失败:', err.message)
    roles.value = []
  }
}
const roleSectionNames = (r) => {
  const perms = parsePermObj(r.permissions)
  const names = []
  PERM_TREE.forEach((g) => {
    const leaves = perms[g.key]
    if (leaves && typeof leaves === 'object' && g.items.some((i) => leaves[i.key])) names.push(g.name)
  })
  return names.slice(0, 3)
}
const roleMemberCount = (r) => employees.value.filter((e) => e.roleId === r.id || e.role === r.name).length

const openRoleModal = (role) => {
  editingRole.value = role || null
  roleForm.name = role?.name || ''
  roleForm.description = role?.description || ''
  roleForm.permissions = role?.permissions ? permsToChecked(role.permissions) : []
  roleForm.color = role?.color || '#FF6B35'
  roleForm.icon = role?.icon || 'fa-user-shield'
  roleVisible.value = true
}
const saveRole = async () => {
  if (!roleForm.name.trim()) { ElMessage.warning('请输入角色名称'); return }
  const payload = { name: roleForm.name.trim(), description: roleForm.description, color: roleForm.color, icon: roleForm.icon, permissions: JSON.stringify(checkedToPerms(roleForm.permissions)) }
  try {
    if (editingRole.value) await updateRole(editingRole.value.id, payload)
    else await createRole(payload)
    ElMessage.success('角色已保存')
    roleVisible.value = false
    loadRoles()
  } catch (err) { ElMessage.error('保存失败') }
}
const removeRole = async (role) => {
  try { await deleteRole(role.id); ElMessage.success('已删除角色'); loadRoles() }
  catch (err) { ElMessage.error('删除失败') }
}

/* ---------- 申请列表 ---------- */
const applies = ref([])
const applyFilters = reactive({ status: '', keyword: '', dateRange: [] })
const applyDetailVisible = ref(false)
const applyDetail = ref(null)

const normalizeApply = (a) => ({
  id: a.id, name: a.name, phone: a.phone || '—', job: a.job || '—',
  applyRole: a.applyRole || '—', intentProject: a.intentProject || '—',
  applyTime: formatTime(a.applyTime), status: a.status || 'pending',
  salaryExpect: a.salaryExpect || '—', workTime: a.workTime || '—',
  joinDate: a.joinDate || '—', company: a.company || '—',
})
const loadApplies = async () => {
  try {
    const res = await listJoinApplies({ status: applyFilters.status || 'all' })
    let list = res.data || []
    if (applyFilters.keyword) list = list.filter((x) => (x.name || '').includes(applyFilters.keyword) || (x.phone || '').includes(applyFilters.keyword))
    if (applyFilters.dateRange && applyFilters.dateRange.length === 2) {
      const [a, b] = applyFilters.dateRange
      list = list.filter((x) => { const t = formatTime(x.applyTime); return t >= a && t <= b })
    }
    applies.value = list.map(normalizeApply)
    stats.pending = list.filter((x) => x.status === 'pending').length
  } catch (err) {
    console.warn('[EmployeeManage] 申请加载失败:', err.message)
    applies.value = []
  }
}
const resetApplyFilters = () => { applyFilters.status = ''; applyFilters.keyword = ''; applyFilters.dateRange = []; loadApplies() }
const openApplyDetail = (row) => { applyDetail.value = row; applyDetailVisible.value = true }
const doApproveApply = async (row) => {
  try { await approveApply(row.id); ElMessage.success('已同意，自动创建员工'); applyDetailVisible.value = false; loadApplies(); loadEmployees() }
  catch (err) { ElMessage.error('操作失败') }
}
const doRejectApply = async (row) => {
  try { await rejectApply(row.id); ElMessage.success('已拒绝'); applyDetailVisible.value = false; loadApplies() }
  catch (err) { ElMessage.error('操作失败') }
}

/* ---------- 初始化 ---------- */
const loadTree = async () => {
  try {
    const res = await permissionTree()
    const list = res.data
    if (Array.isArray(list) && list.length) {
      // 后端返回嵌套权限树 {key,name,children:[{key,name}]}，覆盖静态兜底
      const tree = list.map((node) => ({
        key: node.key,
        name: node.name,
        items: Array.isArray(node.children) ? node.children.map((c) => ({ key: c.key, name: c.name })) : []
      }))
      if (tree.length) {
        PERM_TREE.splice(0, PERM_TREE.length, ...tree)
        rebuildPermMaps()
      }
    }
  } catch (err) { /* 使用静态兜底 */ }
}

onMounted(() => { loadEmployees(); loadApplies(); loadTree() })
</script>

<style scoped>
.stat-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 16px; }
.stat-card { background: #fff; border: 1px solid var(--border); border-radius: 12px; padding: 18px; }
.stat-card-header { display: flex; align-items: center; justify-content: space-between; }
.stat-card-title { font-size: 13px; color: var(--text-secondary); }
.stat-card-icon { width: 40px; height: 40px; border-radius: 10px; background: linear-gradient(135deg, #FFF0EB 0%, #FFE8DC 100%); color: var(--primary); display: flex; align-items: center; justify-content: center; font-size: 16px; }
.stat-card-icon.blue { background: linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 100%); color: var(--secondary); }
.stat-card-icon.green { background: linear-gradient(135deg, #ECFDF5 0%, #D1FAE5 100%); color: var(--success); }
.stat-card-icon.yellow { background: linear-gradient(135deg, #FFFBEB 0%, #FEF3C7 100%); color: var(--warning); }
.stat-card-value { font-size: 26px; font-weight: 700; margin: 10px 0 4px; color: #1F2937; }
.stat-card-change { font-size: 12px; }
.stat-card-change.up { color: var(--success); }
.stat-card-change.down { color: var(--warning); }

.card-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.title-switch-group { display: flex; align-items: center; gap: 4px; }
.title-switch { font-size: 15px; color: var(--text-secondary); cursor: pointer; padding: 4px 2px; }
.title-switch.active { color: var(--primary); font-weight: 600; }
.title-switch-divider { width: 1px; height: 16px; background: var(--border); margin: 0 10px; }
.count-pill { background: var(--danger); color: #fff; font-size: 11px; padding: 1px 7px; border-radius: 10px; margin-left: 4px; }

.filter-bar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.pagination { display: flex; align-items: center; justify-content: space-between; margin-top: 16px; }
.pagination-info { font-size: 13px; color: var(--text-secondary); }

.avatar-cell { display: flex; align-items: center; gap: 10px; }
.mini-avatar { width: 34px; height: 34px; border-radius: 50%; display: inline-flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 600; color: #fff; flex-shrink: 0; }
.mini-avatar.orange { background: linear-gradient(135deg, #FF8C42 0%, #FF6B35 100%); }
.mini-avatar.blue { background: linear-gradient(135deg, #60A5FA 0%, #2563EB 100%); }
.mini-avatar.green { background: linear-gradient(135deg, #34D399 0%, #10B981 100%); }
.mini-avatar.purple { background: linear-gradient(135deg, #A78BFA 0%, #7C3AED 100%); }
.emp-info { display: flex; flex-direction: column; }
.emp-name { font-weight: 500; font-size: 14px; }
.emp-phone { font-size: 12px; color: var(--text-muted); }
.role-tag { display: inline-block; padding: 3px 8px; border-radius: 4px; font-size: 12px; }
.role-tag.super { background: #FFF0EB; color: var(--primary); }
.role-tag.admin { background: #EFF6FF; color: var(--secondary); }
.role-tag.staff { background: #F3F4F6; color: var(--text-secondary); }

.role-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 16px; }
.role-card { border: 1px solid var(--border); border-radius: 12px; padding: 16px; background: #fff; }
.role-card-head { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.role-icon { width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 16px; }
.role-name { font-size: 15px; font-weight: 600; color: #1F2937; display: flex; align-items: center; gap: 6px; }
.sys-badge { font-size: 11px; color: #9CA3AF; background: #F3F4F6; padding: 1px 6px; border-radius: 8px; font-weight: 400; }
.role-desc { font-size: 12px; color: #9CA3AF; margin-top: 2px; }
.role-perm-tags { display: flex; flex-wrap: wrap; gap: 4px; margin-bottom: 12px; }
.perm-tag { font-size: 11px; padding: 2px 8px; border-radius: 10px; }
.role-foot { display: flex; align-items: center; }
.member-pill { font-size: 12px; color: #6B7280; background: #F3F4F6; padding: 2px 8px; border-radius: 10px; }

.invite-link-box { background: var(--bg-page); border: 1px dashed var(--border); border-radius: 8px; padding: 12px; font-size: 13px; color: var(--text-secondary); word-break: break-all; display: flex; align-items: center; justify-content: space-between; gap: 10px; }
.invite-qrcode { width: 120px; height: 120px; margin: 16px auto 0; background: repeating-conic-gradient(#1F2937 0% 25%, #fff 0% 50%) 0 0/20px 20px; border: 6px solid #fff; box-shadow: 0 0 0 1px var(--border), 0 4px 12px rgba(0,0,0,0.08); border-radius: 8px; position: relative; }
.invite-tip { text-align: center; font-size: 12px; color: var(--text-muted); margin-top: 12px; }

.detail-wrap { padding: 4px 0; }
.detail-head { display: flex; align-items: center; gap: 16px; margin-bottom: 16px; padding-bottom: 16px; border-bottom: 1px solid var(--border); }
.detail-name { font-size: 16px; font-weight: 600; display: flex; align-items: center; gap: 10px; }
.detail-sub { font-size: 12px; color: var(--text-muted); margin-top: 4px; }
.detail-section-title { font-size: 14px; font-weight: 600; margin-bottom: 12px; padding-bottom: 8px; border-bottom: 1px solid var(--border); }
.detail-info-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
.detail-info-item { display: flex; flex-direction: column; gap: 4px; }
.detail-info-label { font-size: 12px; color: var(--text-muted); }
.detail-info-value { font-size: 14px; font-weight: 500; }
.perm-tag-list { display: flex; flex-wrap: wrap; gap: 6px; }

.perm-tip { font-size: 12px; color: var(--text-muted); margin-bottom: 14px; }
.perm-group { margin-bottom: 14px; }
.perm-group-title { font-size: 13px; font-weight: 600; color: var(--text-secondary); margin-bottom: 8px; }
</style>
