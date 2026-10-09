import request from './request'

export function listOrders({ status, keyword, type, enterpriseTypeId, startDate, endDate, page = 0, size = 10 } = {}) {
  return request.get('/admin/orders', { params: { status, keyword, type, enterpriseTypeId, startDate, endDate, page, size } })
}

export function getOrderStats({ status, keyword, type, enterpriseTypeId, startDate, endDate } = {}) {
  return request.get('/admin/orders/stats', { params: { status, keyword, type, enterpriseTypeId, startDate, endDate } })
}

export function getJobCategoryTree() { return request.get('/job-categories/tree') }

export function cancelOrder(id, reason) {
  return request.put(`/admin/orders/${id}/cancel`, { reason })
}
