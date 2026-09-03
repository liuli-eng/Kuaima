import request from './request'

// ============ Banner ============
export function listBanners() { return request.get('/admin/banners') }
export function createBanner(data) { return request.post('/admin/banners', data) }
export function updateBanner(id, data) { return request.put(`/admin/banners/${id}`, data) }
export function deleteBanner(id) { return request.delete(`/admin/banners/${id}`) }

// ============ Notice ============
export function listNotices() { return request.get('/admin/notices') }
export function createNotice(data) { return request.post('/admin/notices', data) }
export function updateNotice(id, data) { return request.put(`/admin/notices/${id}`, data) }
export function deleteNotice(id) { return request.delete(`/admin/notices/${id}`) }

// ============ Rules ============
export function listRules() { return request.get('/admin/rules') }
export function createRules(data) { return request.post('/admin/rules', data) }
export function updateRules(id, data) { return request.put(`/admin/rules/${id}`, data) }
export function deleteRules(id) { return request.delete(`/admin/rules/${id}`) }

// ============ Certification ============
export function listCertifications() { return request.get('/admin/certifications') }
export function auditCertPass(id) { return request.put(`/admin/certifications/${id}/pass`) }
export function auditCertReject(id, reason) {
  return request.put(`/admin/certifications/${id}/reject`, null, { params: { reason } })
}
