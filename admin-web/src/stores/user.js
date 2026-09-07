import { defineStore } from 'pinia'
import { login, getCurrentAdmin } from '@/api/auth'
import { parsePermissions } from '@/utils/permission'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('admin_token') || '',
    userInfo: {
      adminId: Number(localStorage.getItem('admin_id') || 0),
      username: localStorage.getItem('admin_username') || '',
      name: localStorage.getItem('admin_name') || '管理员',
      role: localStorage.getItem('admin_role') || '超级管理员',
      email: localStorage.getItem('admin_email') || '',
      avatar: '管',
      // 权限树，结构 { permUser: { u1: true }, permJob: { j1: true }, ... }
      permissions: parsePermissions(localStorage.getItem('admin_permissions') || null)
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
          avatar: (res.data.name || res.data.username || '管').charAt(0),
          permissions: parsePermissions(res.data.permissions || null)
        }
        localStorage.setItem('admin_token', this.token)
        localStorage.setItem('admin_id', String(this.userInfo.adminId))
        localStorage.setItem('admin_username', this.userInfo.username)
        localStorage.setItem('admin_name', this.userInfo.name)
        localStorage.setItem('admin_role', this.userInfo.role)
        localStorage.setItem('admin_email', this.userInfo.email)
        localStorage.setItem('admin_permissions', JSON.stringify(this.userInfo.permissions || {}))
        // 登录后再请求一次 /me，确保权限为最新（防止登录时数据未更新）
        this.fetchUserInfo()
        return { success: true }
      }
      return { success: false, message: res?.message || '登录失败' }
    },

    /** 调用 /admin/auth/me 刷新当前管理员信息（含最新权限） */
    async fetchUserInfo() {
      try {
        const res = await getCurrentAdmin()
        if (res?.code === 200 && res.data) {
          const d = res.data
          this.userInfo.permissions = parsePermissions(d.permissions || null)
          localStorage.setItem('admin_permissions', JSON.stringify(this.userInfo.permissions || {}))
          if (d.role) {
            this.userInfo.role = d.role
            localStorage.setItem('admin_role', d.role)
          }
          if (d.email) {
            this.userInfo.email = d.email
            localStorage.setItem('admin_email', d.email)
          }
        }
      } catch (e) {
        console.warn('[userStore] 获取管理员信息失败:', e)
      }
    },

    logout() {
      this.token = ''
      this.userInfo = {
        adminId: 0,
        username: '',
        name: '管理员',
        role: '超级管理员',
        email: '',
        avatar: '管',
        permissions: {}
      }
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_id')
      localStorage.removeItem('admin_username')
      localStorage.removeItem('admin_name')
      localStorage.removeItem('admin_role')
      localStorage.removeItem('admin_email')
      localStorage.removeItem('admin_permissions')
      sessionStorage.removeItem('admin_token')
    }
  }
})
