import request from './request'

export function login(username, password) {
  return request.post('/admin/auth/login', { username, password })
}

export function getCurrentAdmin() {
  return request.get('/admin/auth/me')
}
