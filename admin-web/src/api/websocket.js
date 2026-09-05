/**
 * WebSocket 客户端（admin-web 客服端）
 * 支持自动重连、心跳检测、HTTP 降级
 */
import { BASE_URL } from './request'

class WsClient {
  constructor() {
    this.ws = null
    this.url = ''
    this.listeners = new Map() // type -> [callback]
    this.reconnectAttempts = 0
    this.maxReconnect = 5
    this.heartbeatTimer = null
    this.manualClose = false
  }

  /**
   * 连接 WebSocket
   * @param {string} userId 用户ID
   * @param {string} type USER / AGENT
   */
  connect(userId, type = 'AGENT') {
    this.manualClose = false
    const token = localStorage.getItem('admin_token') || ''
    const proto = location.protocol === 'https:' ? 'wss:' : 'ws:'
    // BASE_URL = '/api'，通过 vite proxy 转发到后端
    this.url = `${proto}//${location.host}${BASE_URL}/ws/chat?token=${encodeURIComponent(token)}&userId=${userId}&type=${type}`

    this.ws = new WebSocket(this.url)

    this.ws.onopen = () => {
      console.log('[WebSocket] 连接成功')
      this.reconnectAttempts = 0
      this._startHeartbeat()
      this._emit('open', {})
    }

    this.ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        this._emit(data.type || 'message', data)
      } catch (e) {
        console.warn('[WebSocket] 消息解析失败:', event.data)
      }
    }

    this.ws.onclose = () => {
      console.log('[WebSocket] 连接关闭')
      this._stopHeartbeat()
      this._emit('close', {})
      if (!this.manualClose && this.reconnectAttempts < this.maxReconnect) {
        this.reconnectAttempts++
        console.log(`[WebSocket] 尝试重连 ${this.reconnectAttempts}/${this.maxReconnect}`)
        setTimeout(() => this.connect(userId, type), 3000 * this.reconnectAttempts)
      }
    }

    this.ws.onerror = (err) => {
      console.error('[WebSocket] 连接错误:', err)
      this._emit('error', err)
    }
  }

  /** 发送消息 */
  send(data) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data))
      return true
    }
    console.warn('[WebSocket] 连接未就绪，消息未发送')
    return false
  }

  /** 订阅消息类型 */
  on(type, callback) {
    if (!this.listeners.has(type)) this.listeners.set(type, [])
    this.listeners.get(type).push(callback)
  }

  /** 取消订阅 */
  off(type, callback) {
    const list = this.listeners.get(type)
    if (list) {
      const idx = list.indexOf(callback)
      if (idx >= 0) list.splice(idx, 1)
    }
  }

  close() {
    this.manualClose = true
    this._stopHeartbeat()
    if (this.ws) this.ws.close()
  }

  _emit(type, data) {
    const list = this.listeners.get(type)
    if (list) list.forEach(cb => cb(data))
  }

  _startHeartbeat() {
    this._stopHeartbeat()
    this.heartbeatTimer = setInterval(() => {
      this.send({ type: 'PING' })
    }, 30000)
  }

  _stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }
}

const wsClient = new WsClient()
export default wsClient
