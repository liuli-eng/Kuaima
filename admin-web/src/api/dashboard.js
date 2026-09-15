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

/* ============ 员工管理统计（用于首页卡片） ============ */
export function getEmployeeStats() {
  return request.get('/admin/employees/stats')
}

/* ============ 发薪管理统计（用于首页卡片） ============ */
export function getPayrollStats() {
  return request.get('/admin/payrolls/stats')
}
