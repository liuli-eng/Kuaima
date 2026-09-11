import request from './request'

export function listOrders({ status, keyword, type, page = 0, size = 10 } = {}) {
  return request.get('/admin/orders', { params: { status, keyword, type, page, size } })
}
