import request from './request'

export function listCreditUsers({ keyword, role, level, page = 0, size = 10 } = {}) {
  return request.get('/admin/credit/users', { params: { keyword, role, level, page, size } })
}

export function getCreditDetail(userId) {
  return request.get(`/admin/credit/${userId}`)
}

export function adjustCredit(userId, data) {
  return request.post(`/admin/credit/${userId}/adjust`, data)
}
