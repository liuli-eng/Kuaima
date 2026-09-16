import request from './request'

// 积分购买订单
export function listPointPurchaseOrders(params = {}) {
  return request.get('/admin/point-purchase/orders', { params })
}

export function getPointPurchaseStats(params = {}) {
  return request.get('/admin/point-purchase/stats', { params })
}

export function createPointPurchaseOrder(data) {
  return request.post('/admin/point-purchase/orders', data)
}

export function exportPointPurchaseOrders(params = {}) {
  return request.get('/admin/point-purchase/orders/export', { params, responseType: 'blob' })
}
