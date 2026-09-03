import request from './request'

// ============ Settings ============
export function listSettings() { return request.get('/admin/settings') }
export function getSettingsByCategory(cat) { return request.get(`/admin/settings/category/${cat}`) }
export function saveSetting(key, value, description) {
  return request.put(`/admin/settings/${key}`, { settingValue: value, description })
}

// ============ Admin Users ============
export function listAdminUsers({ page = 0, size = 10 } = {}) {
  return request.get('/admin/admin-users', { params: { page, size } })
}
export function createAdminUser(data) { return request.post('/admin/admin-users', data) }
export function updateAdminUser(id, data) { return request.put(`/admin/admin-users/${id}`, data) }
export function resetAdminPassword(id, newPassword = 'admin123') {
  return request.put(`/admin/admin-users/${id}/reset-password`, { newPassword })
}
export function deleteAdminUser(id) { return request.delete(`/admin/admin-users/${id}`) }

// ============ Logs ============
export function listLogs({ page = 0, size = 20 } = {}) {
  return request.get('/admin/logs', { params: { page, size } })
}
