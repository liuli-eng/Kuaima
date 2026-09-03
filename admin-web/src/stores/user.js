import { defineStore } from 'pinia'
import { login } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('admin_token') || '',
    userInfo: {
      name: localStorage.getItem('admin_name') || '管理员',
      role: localStorage.getItem('admin_role') || '超级管理员',
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
          name: res.data.name || res.data.username || '管理员',
          role: res.data.role || '超级管理员',
          avatar: (res.data.name || res.data.username || '管').charAt(0)
        }
        localStorage.setItem('admin_token', this.token)
        localStorage.setItem('admin_name', this.userInfo.name)
        localStorage.setItem('admin_role', this.userInfo.role)
        return { success: true }
      }
      return { success: false, message: res?.message || '登录失败' }
    },
    logout() {
      this.token = ''
      this.userInfo = { name: '管理员', role: '超级管理员', avatar: '管' }
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_name')
      localStorage.removeItem('admin_role')
      sessionStorage.removeItem('admin_token')
    }
  }
})
