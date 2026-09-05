/**
 * WebSocket 客户端（uniapp 用户端）
 * 基于 uni.connectSocket，支持自动重连、HTTP 降级
 */

class UniappWsClient {
  constructor() {
    this.socketTask = null
    this.listeners = new Map()
    this.reconnectAttempts = 0
    this.maxReconnect = 5
    this.manualClose = false
    this.baseUrl = ''
  }

  /**
   * 连接 WebSocket
   * @param {string} baseUrl 后端 WebSocket 基础地址（如 ws://host:8080）
   * @param {string} userId 用户ID
   * @param {string} type USER / AGENT
   */
  connect(baseUrl, userId, type = 'USER') {
    this.manualClose = false
    const token = uni.getStorageSync('token') || ''
    const url = `${baseUrl}/ws/chat?token=${encodeURIComponent(token)}&userId=${userId}&type=${type}`
    this.baseUrl = baseUrl

    this.socketTask = uni.connectSocket({
      url,
      success: () => console.log('[WS] 连接请求已发送'),
      fail: (err) => console.error('[WS] 连接失败:', err)
    })

    this.socketTask.onOpen(() => {
      console.log('[WS] 连接成功')
      this.reconnectAttempts = 0
      this._emit('open', {})
    })

    this.socketTask.onMessage((event) => {
      try {
        const data = JSON.parse(event.data)
        this._emit(data.type || 'message', data)
      } catch (e) {
        console.warn('[WS] 消息解析失败:', event.data)
      }
    })

    this.socketTask.onClose(() => {
      console.log('[WS] 连接关闭')
      this._emit('close', {})
      if (!this.manualClose && this.reconnectAttempts < this.maxReconnect) {
        this.reconnectAttempts++
        console.log(`[WS] 尝试重连 ${this.reconnectAttempts}/${this.maxReconnect}`)
        setTimeout(() => this.connect(this.baseUrl, userId, type), 3000 * this.reconnectAttempts)
      }
    })

    this.socketTask.onError((err) => {
      console.error('[WS] 连接错误:', err)
      this._emit('error', err)
    })
  }

  /** 发送消息 */
  send(data) {
    if (this.socketTask) {
      this.socketTask.send({
        data: JSON.stringify(data),
        fail: (err) => console.error('[WS] 发送失败:', err)
      })
      return true
    }
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
    if (this.socketTask) {
      this.socketTask.close()
      this.socketTask = null
    }
  }

  _emit(type, data) {
    const list = this.listeners.get(type)
    if (list) list.forEach(cb => cb(data))
  }
}

const wsClient = new UniappWsClient()
export default wsClient
