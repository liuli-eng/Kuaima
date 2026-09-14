import request from './request'

// 发薪单列表：tab=submitted(已提交) / reviewed(已审核)
export function listPayrolls({ tab = 'submitted', status, projectId, keyword, page = 0, size = 10 } = {}) {
  return request.get('/admin/payrolls', {
    params: { tab, status, projectId, keyword, page, size }
  })
}

// 发薪管理统计
export function payrollStats() {
  return request.get('/admin/payrolls/stats')
}

// 发薪单详情（含人员明细）
export function getPayroll(id) {
  return request.get(`/admin/payrolls/${id}`)
}

// 新建发薪单
export function createPayroll(payload) {
  return request.post('/admin/payrolls', payload)
}

// 提交发薪单（进入待审批）
export function submitPayroll(id) {
  return request.post(`/admin/payrolls/${id}/submit`)
}

// 审批通过
export function approvePayroll(id) {
  return request.post(`/admin/payrolls/${id}/approve`)
}

// 审批驳回
export function rejectPayroll(id) {
  return request.post(`/admin/payrolls/${id}/reject`)
}

// 撤回发薪单
export function withdrawPayroll(id) {
  return request.post(`/admin/payrolls/${id}/withdraw`)
}
