import { defineStore } from 'pinia'
import { login } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('admin_token') || '',
    userInfo: {
      adminId: Number(localStorage.getItem('admin_id') || 0),
      username: localStorage.getItem('admin_username') || '',
      name: localStorage.getItem('admin_name') || '管理员',
      role: localStorage.getItem('admin_role') || '超级管理员',
      email: localStorage.getItem('admin_email') || '',
      avatar: '管'
    }
  }),
  getters: {
    isLoggedIn: (state) => !!state.token
  },
  actions: {
    async loginAction(username, password) {
      const res = await login(username, password)
      if (res?.code === 200 && res.data?.accessToken) {
        this.token = res.data.accessToken
        this.userInfo = {
          adminId: res.data.adminId || 0,
          username: res.data.username || username || '',
          name: res.data.name || res.data.username || '管理员',
          role: res.data.role || '超级管理员',
          email: res.data.email || '',
          avatar: (res.data.name || res.data.username || '管').charAt(0)
        }
        localStorage.setItem('admin_token', this.token)
        localStorage.setItem('admin_id', String(this.userInfo.adminId))
        localStorage.setItem('admin_username', this.userInfo.username)
        localStorage.setItem('admin_name', this.userInfo.name)
        localStorage.setItem('admin_role', this.userInfo.role)
        localStorage.setItem('admin_email', this.userInfo.email)
        return { success: true }
      }
      return { success: false, message: res?.message || '登录失败' }
    },
    logout() {
      this.token = ''
      this.userInfo = { adminId: 0, username: '', name: '管理员', role: '超级管理员', email: '', avatar: '管' }
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_id')
      localStorage.removeItem('admin_username')
      localStorage.removeItem('admin_name')
      localStorage.removeItem('admin_role')
      localStorage.removeItem('admin_email')
      sessionStorage.removeItem('admin_token')
    }
  }
})
