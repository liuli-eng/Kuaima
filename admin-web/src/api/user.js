import request from './request'

// 零工列表
export function listWorkers({ status, keyword, startDate, endDate, page = 0, size = 10 } = {}) {
  return request.get('/admin/users/workers', { params: { status, keyword, startDate, endDate, page, size } })
}

export function getWorkerStats() {
  return request.get('/admin/users/workers/stats')
}

// 雇主列表
export function listBosses({ status, enterpriseStatus, industry, keyword, page = 0, size = 10 } = {}) {
  return request.get('/admin/users/bosses', { params: { status, enterpriseStatus, industry, keyword, page, size } })
}

// 用户详情
export function getUser(id) {
  return request.get(`/admin/users/${id}`)
}

// 老板详情-积分明细
export function getBossPointRecords(id, { type = 'ALL', page = 0, size = 5 } = {}) {
  return request.get(`/admin/users/${id}/points`, { params: { type, page, size } })
}

// 老板详情-奖励金明细
export function getBossRewardRecords(id, { type = 'ALL', page = 0, size = 5 } = {}) {
  return request.get(`/admin/users/${id}/rewards`, { params: { type, page, size } })
}

// 老板详情-优惠券
export function getBossCouponRecords(id, { status = 'ALL', page = 0, size = 5 } = {}) {
  return request.get(`/admin/users/${id}/coupons`, { params: { status, page, size } })
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
