import request from './request'

export function listCoupons(params = {}) { return request.get('/admin/coupons', { params }) }
export function getCouponStats() { return request.get('/admin/coupons/stats') }
export function createCoupon(data) { return request.post('/admin/coupons', data) }
export function updateCoupon(id, data) { return request.put(`/admin/coupons/${id}`, data) }
export function toggleCouponStatus(id) { return request.put(`/admin/coupons/${id}/status/toggle`) }
export function deleteCoupon(id) { return request.delete(`/admin/coupons/${id}`) }
