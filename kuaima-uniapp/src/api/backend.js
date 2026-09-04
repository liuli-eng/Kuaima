import { request } from "@/api/http";
export { wechatLogin, getCurrentUser } from "@/api/auth";

function query(params) {
  return Object.entries(params)
    .filter(
      ([, value]) => value !== undefined && value !== null && value !== "",
    )
    .map(
      ([key, value]) =>
        `${encodeURIComponent(key)}=${encodeURIComponent(value)}`,
    )
    .join("&");
}

export function getCertificationStatus() {
  return request({ url: "/worker/certification/status" });
}

export function submitCertification(data) {
  return request({ url: "/worker/certification", method: "POST", data });
}

export function listOrders(params = {}) {
  return request({
    url: `/boss/order?${query({ page: 0, size: 20, ...params })}`,
  });
}

export function getOrder(id) {
  return request({ url: `/boss/order/${id}` });
}

export function createOrder(data) {
  return request({ url: "/boss/order", method: "POST", data });
}

export function updateOrder(id, data) {
  return request({ url: `/boss/order/${id}`, method: "PUT", data });
}

export function deleteOrder(id) {
  return request({ url: `/boss/order/${id}`, method: "DELETE" });
}

export function changeOrderStatus(id, target) {
  return request({
    url: `/boss/order/${id}/status?target=${encodeURIComponent(target)}`,
    method: "PUT",
  });
}

export function remindOrderStart(id) {
  return request({ url: `/boss/order/${id}/remind-start`, method: "POST" });
}

export function applyOrder(orderId, data = {}) {
  return request({
    url: `/boss/order/${orderId}/apply?${query(data)}`,
    method: "POST",
  });
}

export function listWorkerItems(userId) {
  return request({
    url: `/boss/user/items?userId=${encodeURIComponent(userId)}`,
  });
}

export function listOrderItems(orderId) {
  return request({ url: `/boss/order/${orderId}/items` });
}

export function hireOrderItem(id) {
  return request({ url: `/boss/item/${id}/hire`, method: "PUT" });
}

export function confirmOrderItemWork(id) {
  return request({ url: `/boss/item/${id}/work`, method: "PUT" });
}

export function finishOrderItem(id) {
  return request({ url: `/boss/item/${id}/finish`, method: "PUT" });
}

export function cancelOrderItem(id, reason) {
  return request({
    url: `/boss/item/${id}/cancel?${query({ reason })}`,
    method: "PUT",
  });
}

export function createSettlement(itemId, workDays) {
  return request({
    url: `/settle?${query({ itemId, workDays })}`,
    method: "POST",
  });
}

export function paySettlement(id) {
  return request({ url: `/settle/${id}/pay`, method: "POST" });
}

export function listOrderSettlements(orderId) {
  return request({ url: `/settle/order/${orderId}` });
}

export function getWallet(userId) {
  return request({ url: `/wallet/${userId}` });
}

export function listWalletFlows(userId) {
  return request({ url: `/wallet/${userId}/flows` });
}

export function listWithdraws(userId) {
  return request({ url: `/wallet/${userId}/withdraws` });
}

export function applyWithdraw({ userId, amount, account, remark }) {
  return request({
    url: `/wallet/withdraw?${query({ userId, amount, account, remark })}`,
    method: "POST",
  });
}

export function listSettlements(userId) {
  return request({ url: `/settle/worker/${encodeURIComponent(userId)}` });
}

export function listMessages(userId, params = {}) {
  return request({
    url: `/message/list?${query({ userId, page: 0, size: 20, ...params })}`,
  });
}

export function unreadMessages(userId) {
  return request({ url: `/message/unread?userId=${userId}` });
}

export function readMessage(id, userId) {
  return request({
    url: `/message/${id}/read?userId=${encodeURIComponent(userId)}`,
    method: "PUT",
  });
}

export function readAllMessages(userId) {
  return request({
    url: `/message/readAll?userId=${encodeURIComponent(userId)}`,
    method: "PUT",
  });
}
