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

// ============ Message Template ============
export function listMessageTemplates(params) { return request.get('/admin/message-templates', { params }) }
export function getMessageTemplate(id) { return request.get(`/admin/message-templates/${id}`) }
export function createMessageTemplate(data) { return request.post('/admin/message-templates', data) }
export function updateMessageTemplate(id, data) { return request.put(`/admin/message-templates/${id}`, data) }
export function toggleMessageTemplate(id) { return request.put(`/admin/message-templates/${id}/toggle`) }
export function deleteMessageTemplate(id) { return request.delete(`/admin/message-templates/${id}`) }
