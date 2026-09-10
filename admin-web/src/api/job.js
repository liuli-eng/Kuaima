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

// 关闭招工
export function closeJob(id) {
  return request.put(`/admin/jobs/${id}/close`)
}

// 开启招工
export function openJob(id) {
  return request.put(`/admin/jobs/${id}/open`)
}

// 报名人员列表
export function listApplicants(jobId) {
  return request.get(`/admin/jobs/${jobId}/applicants`)
}

// 录用报名人员
export function hireApplicant(itemId) {
  return request.put(`/admin/jobs/item/${itemId}/hire`)
}

// 拒绝报名人员
export function rejectApplicant(itemId, reason) {
  return request.put(`/admin/jobs/item/${itemId}/reject`, null, { params: { reason } })
}
