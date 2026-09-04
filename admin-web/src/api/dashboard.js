import request from './request'

export function getStats() {
  return request.get('/admin/dashboard/stats')
}

export function getTrend() {
  return request.get('/admin/dashboard/trend')
}

export function getDistribution() {
  return request.get('/admin/dashboard/distribution')
}

export function getRecentOrders() {
  return request.get('/admin/dashboard/recent-orders')
}
