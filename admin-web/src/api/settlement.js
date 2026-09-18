import request from './request'

export function listSettlements({
  status,
  cycle,
  startDate,
  endDate,
  page = 0,
  size = 10
} = {}) {
  return request.get('/admin/settlements', { params: { status, cycle, startDate, endDate, page, size } })
}

export function getSettlementStats() {
  return request.get('/admin/settlements/stats')
}

export function getSettlementDetail(id) {
  return request.get(`/admin/settlements/${id}`)
}

export function settlePay(id) {
  return request.post(`/admin/settlements/${id}/pay`)
}
