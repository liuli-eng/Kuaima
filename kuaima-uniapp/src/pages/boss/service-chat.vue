<template>
  <view class="container">
    <!-- 状态栏 -->
    <view class="status-bar">
      <text>19:48</text>
      <view class="status-icons">
        <text>📶</text>
        <text>📡</text>
        <text>🔋</text>
      </view>
    </view>

    <!-- 客服头部 -->
    <view class="chat-header">
      <view class="chat-close" @click="goBack">
        <text style="color:#fff;font-size:18px;">←</text>
      </view>
      <view class="chat-avatar">
        <text style="color:#FF6B35;font-size:20px;">🎧</text>
      </view>
      <view class="chat-info">
        <text class="chat-name">快马日结小助手</text>
        <view class="chat-status">
          <view class="status-dot"></view>
          <text style="margin-left:4px;">{{ statusText }}</text>
        </view>
      </view>
      <view class="chat-close">
        <text style="color:#fff;font-size:16px;">…</text>
      </view>
    </view>

    <!-- 安全提示 -->
    <view class="security-tip" v-if="showSecurityTip">
      <text style="color:#FA8C16;">🛡</text>
      <text style="flex:1;">平台禁止飞单行为，检测到将进行封号处理</text>
      <text class="✕" style="color:#FA8C16;" @click="showSecurityTip = false"></text>
    </view>

    <!-- 消息列表 -->
    <scroll-view scroll-y class="chat-messages" :scroll-top="scrollTop" :show-scrollbar="false">
      <text class="msg-time">今天 19:48</text>

      <!-- 客服欢迎消息 -->
      <view class="msg-row">
        <view class="msg-avatar service">
          <text style="font-size:14px;">🎧</text>
        </view>
        <view class="msg-content">
          <view class="msg-bubble">您好，请问有什么可以帮助您？</view>
        </view>
      </view>

      <!-- 热门问题 -->
      <view class="msg-row">
        <view class="msg-avatar service">
          <text style="font-size:14px;">🎧</text>
        </view>
        <view class="msg-content">
          <view class="msg-bubble" style="background:transparent;padding:0;">
            <view class="hot-questions">
              <text class="hot-title">热门问题</text>
              <view class="hot-list">
                <view class="hot-item" v-for="(item, index) in hotQuestions" :key="index" @click="selectHotQuestion(item)">
                  <text style="font-size:6px;">●</text>
                  <text>{{ item }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 用户消息 -->
      <view
        class="msg-row"
        :class="{ self: msg.self }"
        v-for="(msg, index) in messages"
        :key="index"
      >
        <view class="msg-avatar" :class="msg.self ? 'user' : 'service'">
          <text style="font-size:14px;">{{ msg.self ? "👤" : "🎧" }}</text>
        </view>
        <view class="msg-content">
          <view class="msg-bubble">{{ msg.text }}</view>
        </view>
      </view>
    </scroll-view>

    <!-- 输入区域 -->
    <view class="input-area">
      <view class="input-btn">
        <text style="font-size:20px;color:#666;">🖼</text>
      </view>
      <view class="input-wrap">
        <input type="text" class="input-field" v-model="inputText" placeholder="请输入您的问题" @confirm="sendMessage" />
      </view>
      <button class="send-btn" :disabled="!inputText.trim() || sending" @click="sendMessage">发送</button>
    </view>
  </view>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from "vue";
import { onHide, onShow } from "@dcloudio/uni-app";
import { createChatSession, listChatMessages, sendChatMessage } from "@/api/backend";
import wsClient from "@/api/chat-websocket";

const showSecurityTip = ref(true);
const inputText = ref("");
const messages = ref([]);
const sessionId = ref("");
const scrollTop = ref(0);
const sending = ref(false);
const connected = ref(false);
const userId = String(uni.getStorageSync("userId") || "2001");
const hotQuestions = ["零工中途跑了怎么办？", "临时有变化不需要招工了怎么办？", "工作地址填错了怎么办？", "招工信息需要修改怎么办？", "零工完工后没有点击收工，一直没法结算报酬怎么办？"];
const wsBaseUrl = resolveWsBaseUrl();
let syncTimer = null;

const statusText = computed(() => connected.value ? "在线 · 平均1分钟回复" : "连接中…");
onMounted(loadSession);
onShow(() => { if (sessionId.value) { connectIm(); refreshMessages(); startMessageSync(); } });
onHide(stopMessageSync);
onUnmounted(() => { stopMessageSync(); removeSocketListeners(); wsClient.close(); });

function resolveWsBaseUrl() {
  const explicit = import.meta.env.VITE_WS_BASE_URL;
  if (explicit) return explicit.replace(/\/$/, "");
  const configured = import.meta.env.VITE_MP_API_BASE_URL || import.meta.env.VITE_PROXY_TARGET || "http://127.0.0.1:8080";
  return configured.replace(/^https:/, "wss:").replace(/^http:/, "ws:").replace(/\/$/, "");
}
async function loadSession() {
  try {
    const session = await createChatSession({ userId: Number(userId) });
    sessionId.value = String(session.sessionId || session.id || "");
    if (!sessionId.value) throw new Error("客服会话创建失败");
    await refreshMessages(true);
    connectIm();
    startMessageSync();
  } catch (error) {
    uni.showToast({ title: error.message || "客服连接失败", icon: "none" });
  }
}
function removeSocketListeners() {
  wsClient.off("open", handleOpen); wsClient.off("close", handleClose);
  wsClient.off("MESSAGE", handleSocketMessage); wsClient.off("message", handleSocketMessage);
}
function connectIm() {
  removeSocketListeners();
  wsClient.on("open", handleOpen); wsClient.on("close", handleClose);
  wsClient.on("MESSAGE", handleSocketMessage); wsClient.on("message", handleSocketMessage);
  if (!wsClient.connected) wsClient.connect(wsBaseUrl, userId, "USER");
}
function handleOpen() { connected.value = true; wsClient.send({ type: "JOIN", sessionId: Number(sessionId.value) }); }
function handleClose() { connected.value = false; }
function handleSocketMessage(data) {
  if (data?.type && data.type !== "MESSAGE") return;
  if (data?.sessionId && String(data.sessionId) !== String(sessionId.value)) return;
  appendMessage(data);
}
async function refreshMessages(initial = false) {
  if (!sessionId.value) return;
  try {
    const result = await listChatMessages(sessionId.value, { page: 0, size: 50 });
    const rows = Array.isArray(result) ? result : result?.records || result?.content || [];
    const incoming = rows.map(normalizeMessage).filter((item) => item.text);
    if (initial) messages.value = incoming; else incoming.forEach(appendMessage);
    if (initial && incoming.length) scrollToLatest();
  } catch (_) {}
}
function normalizeMessage(item = {}) {
  const timestamp = item.timestamp || item.createTime;
  return { ...item, id: item.id || item.messageId, text: item.content || item.text || "", self: String(item.fromId) === userId || String(item.fromType || "").toUpperCase() === "USER", time: timestamp ? formatTime(timestamp) : "" };
}
function appendMessage(item) {
  const normalized = normalizeMessage(item);
  if (!normalized.text) return;
  const key = String(normalized.id || normalized.messageId || "");
  if (key && messages.value.some((message) => String(message.id || message.messageId || "") === key)) return;
  messages.value.push(normalized); scrollToLatest();
}
function formatTime(value) { const date = new Date(value); return Number.isNaN(date.getTime()) ? "" : `${String(date.getHours()).padStart(2, "0")}:${String(date.getMinutes()).padStart(2, "0")}`; }
function scrollToLatest() { scrollTop.value = 0; setTimeout(() => { scrollTop.value = 999999; }, 30); }
function startMessageSync() { stopMessageSync(); syncTimer = setInterval(() => refreshMessages(), 3000); }
function stopMessageSync() { if (syncTimer) clearInterval(syncTimer); syncTimer = null; }
function goBack() { uni.navigateBack(); }
function selectHotQuestion(question) { inputText.value = question; sendMessage(); }
async function sendMessage() {
  const text = inputText.value.trim();
  if (!text || sending.value) return;
  if (!sessionId.value) return uni.showToast({ title: "客服会话尚未建立", icon: "none" });
  sending.value = true; inputText.value = "";
  const sentBySocket = wsClient.send({ type: "MESSAGE", sessionId: Number(sessionId.value), content: text, contentType: "TEXT" });
  if (sentBySocket) { sending.value = false; scrollToLatest(); return; }
  try { const result = await sendChatMessage(sessionId.value, { fromId: Number(userId), content: text }); if (result) appendMessage(result); } catch (error) { inputText.value = text; uni.showToast({ title: error.message || "发送失败", icon: "none" }); } finally { sending.value = false; }
}
</script>

<style lang="scss" scoped>
.container {
  width: 100%;
  height: 100vh;
  min-height: 0;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-sizing: border-box;
}

.status-bar {
  height: 47px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 28px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  background: #fff;
}

.status-icons {
  display: flex;
  align-items: center;
  gap: 4px;
}

.chat-header {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.chat-avatar {
  width: 40px;
  height: 40px;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-info {
  flex: 1;
}

.chat-name {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
}

.chat-status {
  font-size: 12px;
  color: rgba(255,255,255,0.8);
  display: flex;
  align-items: center;
}

.status-dot {
  width: 6px;
  height: 6px;
  background: #52C41A;
  border-radius: 50%;
}

.chat-close {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.security-tip {
  background: #FFF8E6;
  padding: 8px 14px;
  font-size: 12px;
  color: #B8860B;
  display: flex;
  align-items: center;
  gap: 6px;
  border-bottom: 1px solid #FFE4A0;
}

.chat-messages {
  flex: 1;
  min-height: 0;
  width: 100%;
  box-sizing: border-box;
  overflow-y: auto;
  padding: 16px 14px 24px;
  background: #f5f5f5;
}

.msg-time {
  text-align: center;
  font-size: 11px;
  color: #999;
  margin: 12px 0;
  display: block;
}

.msg-row {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.msg-row.self {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
}

.msg-avatar.service {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  color: white;
}

.msg-avatar.user {
  background: #52C41A;
  color: white;
}

.msg-content {
  min-width: 0;
  max-width: calc(100% - 40px);
}

.msg-bubble {
  padding: 12px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
}

.msg-row:not(.self) .msg-bubble {
  background: #fff;
  color: #333;
  border-radius: 4px 12px 12px 12px;
}

.msg-row.self .msg-bubble {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  color: white;
  border-radius: 12px 4px 12px 12px;
}

.hot-questions {
  background: #fff;
  border-radius: 12px;
  padding: 14px;
  margin-top: 10px;
}

.hot-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
  display: block;
}

.hot-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.hot-item {
  padding: 10px 12px;
  background: #FFF8F5;
  border-radius: 8px;
  font-size: 13px;
  color: #FF6B35;
  display: flex;
  align-items: center;
  gap: 6px;
}

.input-area {
  box-sizing: border-box;
  width: 100%;
  min-height: 72px;
  flex: 0 0 auto;
  background: #fff;
  /* 同时兼容微信开发者工具、iOS 真机和不支持 env() 的旧运行时。 */
  padding: 10px 14px;
  padding-bottom: calc(10px + constant(safe-area-inset-bottom));
  padding-bottom: calc(10px + env(safe-area-inset-bottom));
  border-top: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.input-btn {
  width: 40px;
  height: 40px;
  flex: 0 0 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.input-wrap {
  min-width: 0;
  flex: 1;
}

.input-field {
  width: 100%;
  height: 40px;
  line-height: 40px;
  padding: 0 14px;
  background: #f5f5f5;
  border-radius: 20px;
  font-size: 14px;
  border: none;
  outline: none;
  box-sizing: border-box;
}

.send-btn {
  flex: 0 0 auto;
  box-sizing: border-box;
  min-width: 68px;
  height: 40px;
  margin: 0;
  padding: 0 16px;
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  line-height: normal;
  display: flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
}

.send-btn[disabled] {
  opacity: .5;
}
</style>
