import request from './request'

// 招工列表（admin 全量）
export function listJobs({ type, status, title, page = 0, size = 10 } = {}) {
  return request.get('/admin/jobs', { params: { type, status, title, page, size } })
}

// 招工详情
export function getJob(id) {
  return request.get(`/admin/jobs/${id}`)
}

// 审核通过
export function auditJobPass(id) {
  return request.put(`/admin/jobs/${id}/audit/pass`)
}

// 审核拒绝
export function auditJobReject(id, reason) {
  return request.put(`/admin/jobs/${id}/audit/reject`, null, { params: { reason } })
}
