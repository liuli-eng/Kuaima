import request from './request'

/* ============ 员工 ============ */
export function listEmployees({ keyword, company, role, status, page = 0, size = 10 } = {}) {
  return request.get('/admin/employees', {
    params: { keyword, company, role, status, page, size }
  })
}

export function getEmployee(id) {
  return request.get(`/admin/employees/${id}`)
}

export function createEmployee(payload) {
  return request.post('/admin/employees', payload)
}

export function updateEmployee(id, payload) {
  return request.put(`/admin/employees/${id}`, payload)
}

// 变更状态：active 在职 / frozen 已停用
export function setEmployeeStatus(id, status) {
  return request.post(`/admin/employees/${id}/status`, null, { params: { status } })
}

export function deleteEmployee(id) {
  return request.delete(`/admin/employees/${id}`)
}

/* ============ 角色 ============ */
export function listRoles() {
  return request.get('/admin/employee-roles')
}

export function permissionTree() {
  return request.get('/admin/employee-roles/permission-tree')
}

export function createRole(payload) {
  return request.post('/admin/employee-roles', payload)
}

export function updateRole(id, payload) {
  return request.put(`/admin/employee-roles/${id}`, payload)
}

export function deleteRole(id) {
  return request.delete(`/admin/employee-roles/${id}`)
}

export function initRoles() {
  return request.post('/admin/employee-roles/init')
}

/* ============ 加入申请 ============ */
// 列表已合并 join_apply（后台表单）与 enterprise_join_apply（扫码/邀请链接），
// 审批时需回传该行 source，后端据此写回对应申请表。
export function listJoinApplies({ status = 'all' } = {}) {
  return request.get('/admin/join-applies', { params: { status } })
}

export function approveApply(id, source) {
  return request.post(`/admin/join-applies/${id}/approve`, { source })
}

export function rejectApply(id, source) {
  return request.post(`/admin/join-applies/${id}/reject`, { source })
}

/* ============ 邀请二维码 ============ */
// 后台为平台级账号，没有「当前企业」上下文，需先选择企业再由后端生成真实二维码
export function listEnterprises() {
  return request.get('/admin/enterprises')
}

export function getInviteQr(enterpriseId) {
  return request.get(`/admin/enterprises/${enterpriseId}/invite-qr`)
}
