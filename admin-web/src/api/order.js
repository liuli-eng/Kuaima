import request from './request'

export function listOrders({ status } = {}) {
  return request.get('/admin/orders', { params: { status } })
}
