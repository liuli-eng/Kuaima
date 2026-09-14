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
export function listJoinApplies({ status = 'all' } = {}) {
  return request.get('/admin/join-applies', { params: { status } })
}

export function approveApply(id) {
  return request.post(`/admin/join-applies/${id}/approve`)
}

export function rejectApply(id) {
  return request.post(`/admin/join-applies/${id}/reject`)
}
