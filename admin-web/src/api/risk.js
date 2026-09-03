import request from './request'

// ============ Report ============
export function listReports() { return request.get('/admin/reports') }
export function handleReport(id, result) {
  return request.post(`/admin/reports/${id}/handle`, null, { params: { result } })
}

// ============ Blacklist ============
export function listBlacklist() { return request.get('/admin/blacklists') }
export function addBlacklist(data) { return request.post('/admin/blacklists', data) }
export function unfreezeBlacklist(id) { return request.put(`/admin/blacklists/${id}/unfreeze`) }
export function deleteBlacklist(id) { return request.delete(`/admin/blacklists/${id}`) }
