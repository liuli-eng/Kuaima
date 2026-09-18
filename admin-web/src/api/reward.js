import request from './request'

export function listRewards(params = {}) { return request.get('/admin/rewards', { params }) }
export function getRewardStats() { return request.get('/admin/rewards/stats') }
export function listRewardRecipients(params = {}) { return request.get('/admin/rewards/recipients', { params }) }
export function createReward(data) { return request.post('/admin/rewards', data) }
export function getReward(id) { return request.get(`/admin/rewards/${id}`) }
export function cancelReward(id) { return request.put(`/admin/rewards/${id}/cancel`) }
