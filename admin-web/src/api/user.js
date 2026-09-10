import request from './request'

// 零工列表
export function listWorkers({ status, keyword, page = 0, size = 10 } = {}) {
  return request.get('/admin/users/workers', { params: { status, keyword, page, size } })
}

// 雇主列表
export function listBosses({ status, enterpriseStatus, keyword, page = 0, size = 10 } = {}) {
  return request.get('/admin/users/bosses', { params: { status, enterpriseStatus, keyword, page, size } })
}

// 用户详情
export function getUser(id) {
  return request.get(`/admin/users/${id}`)
}

// 冻结
export function freezeUser(id) {
  return request.put(`/admin/users/${id}/freeze`)
}

// 解冻
export function unfreezeUser(id) {
  return request.put(`/admin/users/${id}/unfreeze`)
}

// 企业认证审核通过
export function enterprisePass(id) {
  return request.put(`/admin/users/${id}/enterprise/pass`)
}

// 企业认证审核拒绝
export function enterpriseReject(id) {
  return request.put(`/admin/users/${id}/enterprise/reject`)
}
