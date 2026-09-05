<template>
  <view class="page">
    <view class="chat-header" :style="{ paddingTop: `${statusBarHeight}px` }">
      <text class="back" @click="uni.navigateBack()">‹</text
      ><text class="avatar">☎</text>
      <view class="info"
        ><text class="name">快马日结客服</text
        ><text class="status"
          ><text class="dot" />在线 · 平均1分钟回复</text
        ></view
      >
      <text class="more">•••</text>
    </view>
    <view class="security">请勿向任何人透露验证码、银行卡密码等敏感信息。</view>
    <scroll-view scroll-y class="messages" :scroll-into-view="lastId">
      <text class="time">今天 19:48</text>
      <view
        v-for="(item, index) in messages"
        :id="`msg-${index}`"
        :key="item.id || index"
        :class="['message-row', { self: item.self }]"
      >
        <text v-if="!item.self" class="small-avatar">快</text
        ><text class="bubble">{{ item.text }}</text>
      </view>
      <view v-if="!messages.length" class="hot"
        ><text class="hot-title">常见问题</text
        ><text v-for="item in hotQuestions" :key="item" @click="sendText(item)"
          >{{ item }} ›</text
        ></view
      >
    </scroll-view>
    <view class="composer" :style="{ paddingBottom: `${bottomInset + 12}px` }">
      <input
        v-model="input"
        confirm-type="send"
        placeholder="请输入您的问题"
        @confirm="send"
      /><button @click="send">发送</button>
    </view>
  </view>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import {
  createChatSession,
  listChatMessages,
  sendChatMessage,
} from "@/api/backend";
const system = uni.getSystemInfoSync();
const statusBarHeight = system.statusBarHeight || 0;
const bottomInset = system.safeAreaInsets?.bottom || 0;
const input = ref("");
const userId = uni.getStorageSync("userId") || "2001";
const sessionId = ref("");
const sending = ref(false);
const messages = ref([]);
const hotQuestions = [
  "老板还没有结算报酬",
  "订单临时去不了怎么办？",
  "提现多久到账？",
];
const lastId = computed(() => `msg-${messages.value.length - 1}`);
onMounted(loadSession);
async function loadSession() {
  try {
    const session = await createChatSession({ userId });
    sessionId.value = session.sessionId || session.id;
    if (!sessionId.value) throw new Error("客服会话创建失败");
    const result = await listChatMessages(sessionId.value);
    const rows = Array.isArray(result)
      ? result
      : result?.records || result?.content || [];
    messages.value = rows.map(normalizeMessage);
  } catch (error) {
    uni.showToast({ title: error.message || "客服连接失败", icon: "none" });
  }
}
function normalizeMessage(item) {
  return {
    ...item,
    text: item.content || item.text || "",
    self: String(item.fromId) === String(userId) || item.fromType === "USER",
  };
}
function sendText(text) {
  input.value = text;
  send();
}
async function send() {
  const text = input.value.trim();
  if (!text || sending.value) return;
  if (!sessionId.value)
    return uni.showToast({ title: "客服会话尚未建立", icon: "none" });
  sending.value = true;
  input.value = "";
  messages.value.push({ text, self: true });
  try {
    const result = await sendChatMessage(sessionId.value, {
      fromId: userId,
      content: text,
    });
    if (result) messages.value.push(normalizeMessage(result));
  } catch (error) {
    messages.value.pop();
    input.value = text;
    uni.showToast({ title: error.message || "发送失败", icon: "none" });
  } finally {
    sending.value = false;
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}
.chat-header {
  display: flex;
  align-items: center;
  gap: 14rpx;
  padding-left: 24rpx;
  padding-right: 24rpx;
  padding-bottom: 20rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: #fff;
}
.back {
  font-size: 48rpx;
}
.avatar,
.small-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #fff;
  color: #ff6b35;
}
.avatar {
  width: 72rpx;
  height: 72rpx;
  font-size: 30rpx;
}
.info {
  flex: 1;
}
.name,
.status {
  display: block;
}
.name {
  font-size: 28rpx;
  font-weight: 700;
}
.status {
  margin-top: 5rpx;
  font-size: 20rpx;
  opacity: 0.9;
}
.dot {
  display: inline-block;
  width: 10rpx;
  height: 10rpx;
  margin-right: 8rpx;
  border-radius: 50%;
  background: #52c41a;
}
.more {
  letter-spacing: 2rpx;
}
.security {
  padding: 16rpx 24rpx;
  background: #fff8e6;
  color: #d48806;
  font-size: 21rpx;
}
.messages {
  height: calc(100vh - 300rpx);
  padding: 20rpx 24rpx;
  box-sizing: border-box;
}
.time {
  display: block;
  margin: 12rpx 0 22rpx;
  text-align: center;
  color: #aaa;
  font-size: 20rpx;
}
.message-row {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  margin-bottom: 22rpx;
}
.message-row.self {
  justify-content: flex-end;
}
.small-avatar {
  flex-shrink: 0;
  width: 58rpx;
  height: 58rpx;
  font-size: 21rpx;
}
.bubble {
  max-width: 72%;
  padding: 18rpx 20rpx;
  border-radius: 8rpx 20rpx 20rpx;
  background: #fff;
  color: #444;
  font-size: 24rpx;
  line-height: 1.6;
}
.self .bubble {
  border-radius: 20rpx 8rpx 20rpx 20rpx;
  background: #ff8c5a;
  color: #fff;
}
.hot {
  margin: 10rpx 70rpx 30rpx;
  padding: 22rpx;
  border-radius: 18rpx;
  background: #fff;
}
.hot-title {
  display: block;
  margin-bottom: 10rpx;
  font-size: 25rpx;
  font-weight: 700;
}
.hot > text:not(.hot-title) {
  display: block;
  padding: 15rpx 0;
  border-top: 1rpx solid #f1f1f1;
  color: #666;
  font-size: 22rpx;
}
.composer {
  display: flex;
  gap: 14rpx;
  padding: 14rpx 20rpx;
  background: #fff;
}
.composer input {
  flex: 1;
  height: 72rpx;
  padding: 0 20rpx;
  border-radius: 36rpx;
  background: #f5f5f5;
  font-size: 24rpx;
}
.composer button {
  width: 120rpx;
  height: 72rpx;
  line-height: 72rpx;
  margin: 0;
  padding: 0;
  border-radius: 36rpx;
  background: #ff6b35;
  color: #fff;
  font-size: 24rpx;
}
</style>
