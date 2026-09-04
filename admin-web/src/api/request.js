import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const BASE_URL = '/api'

const request = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器：注入 token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('admin_token')
    if (token && !config.url.includes('/admin/auth/login')) {
      config.headers.Authorization = 'Bearer ' + token
    }
    return config
  },
  (err) => Promise.reject(err)
)

// 响应拦截器：解析 Result 结构
request.interceptors.response.use(
  (resp) => {
    const body = resp.data
    // 后端统一 Result 格式：{ code, message, data, page, total }
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 200) {
        return body // 原样返回，业务方自行解构 data/page/total
      }
      if (body.code === 401) {
        localStorage.removeItem('admin_token')
        if (router.currentRoute.value.path !== '/login') {
          ElMessage.error('登录已过期，请重新登录')
          router.push('/login')
        }
        return Promise.reject(new Error(body.message || '未授权'))
      }
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    // 非标准响应（例如代理层直接返回）
    return resp.data
  },
  (err) => {
    if (err.response) {
      const status = err.response.status
      if (status === 401) {
        localStorage.removeItem('admin_token')
        if (router.currentRoute.value.path !== '/login') {
          ElMessage.error('登录已过期，请重新登录')
          router.push('/login')
        }
      } else {
        ElMessage.error(`请求失败 (${status})`)
      }
    } else if (err.message?.includes('Network Error')) {
      // 后端未启动时静默一些，不频繁弹窗
      console.warn('[API] 后端服务未启动: ' + err.config?.url)
    } else {
      ElMessage.error(err.message || '网络异常')
    }
    return Promise.reject(err)
  }
)

export default request

export { BASE_URL }
