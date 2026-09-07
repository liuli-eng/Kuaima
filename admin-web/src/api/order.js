import request from './request'

export function listOrders({ status, page = 0, size = 10 } = {}) {
  return request.get('/admin/orders', { params: { status, page, size } })
}
