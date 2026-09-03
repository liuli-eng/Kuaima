import request from './request'

export function listSettlements({ page = 0, size = 10 } = {}) {
  return request.get('/admin/settlements', { params: { page, size } })
}

export function settlePay(id) {
  return request.post(`/admin/settlements/${id}/pay`)
}
