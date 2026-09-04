import { request } from "@/api/http";

function query(params) {
  return Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== null && value !== "")
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join("&");
}

export function wechatLogin(data) {
  return request({ url: "/auth/wechat/login", method: "POST", data });
}

export function getCurrentUser() {
  return request({ url: "/auth/me" });
}

export function listOrders(params = {}) {
  return request({ url: `/boss/order?${query({ page: 0, size: 20, ...params })}` });
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
  return request({ url: `/boss/order/${orderId}/apply?${query(data)}`, method: "POST" });
}

export function listWorkerItems(userId) {
  return request({ url: `/boss/user/items?userId=${encodeURIComponent(userId)}` });
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
  return request({ url: `/wallet/withdraw?${query({ userId, amount, account, remark })}`, method: "POST" });
}

export function listSettlements(userId) {
  return request({ url: `/settle/worker/${encodeURIComponent(userId)}` });
}

export function listMessages(userId, params = {}) {
  return request({ url: `/message/list?${query({ userId, page: 0, size: 20, ...params })}` });
}

export function unreadMessages(userId) {
  return request({ url: `/message/unread?userId=${userId}` });
}

export function readMessage(id, userId) {
  return request({ url: `/message/${id}/read?userId=${encodeURIComponent(userId)}`, method: "PUT" });
}

export function readAllMessages(userId) {
  return request({ url: `/message/readAll?userId=${encodeURIComponent(userId)}`, method: "PUT" });
}

// ============ Phase 1: 用户资料与认证 ============

export function getUserProfile(userId) {
  return request({ url: `/user/${userId}` });
}

export function updateUserProfile(userId, data) {
  return request({ url: `/user/${userId}`, method: "PUT", data });
}

export function submitRealname(userId, data) {
  return request({ url: `/user/${userId}/realname`, method: "POST", data });
}

export function submitEnterpriseCert(data) {
  return request({ url: `/boss/enterprise-cert`, method: "POST", data });
}

export function getCredit(userId) {
  return request({ url: `/user/${userId}/credit` });
}

export function getCreditFlows(userId, params = {}) {
  return request({ url: `/user/${userId}/credit/flows?${query({ page: 0, size: 20, ...params })}` });
}

export function listCertifications(userId) {
  return request({ url: `/user/${userId}/certifications` });
}

export function switchRole(role) {
  return request({ url: `/auth/switch-role?role=${encodeURIComponent(role)}`, method: "POST" });
}

export function cancelAccount(userId, reason) {
  return request({ url: `/auth/cancel?userId=${userId}&reason=${encodeURIComponent(reason)}`, method: "POST" });
}

export function getBossProfileStats(userId) {
  return request({ url: `/boss/profile/${userId}/stats` });
}

// ============ Phase 2: 老板端招工与人才 ============

export function saveDraft(data) {
  return request({ url: `/boss/order/draft`, method: "POST", data });
}

export function listDrafts(userId) {
  return request({ url: `/boss/orders/drafts?userId=${userId}` });
}

export function updateDraft(id, data) {
  return request({ url: `/boss/order/${id}/draft`, method: "PUT", data });
}

export function getBossStats(userId) {
  return request({ url: `/boss/stats?userId=${userId}` });
}

export function getJobCategories() {
  return request({ url: `/boss/job-categories` });
}

// 地址管理
export function listAddresses(userId) {
  return request({ url: `/boss/addresses?userId=${userId}` });
}

export function createAddress(data) {
  return request({ url: `/boss/addresses`, method: "POST", data });
}

export function deleteAddress(id) {
  return request({ url: `/boss/addresses/${id}`, method: "DELETE" });
}

export function setDefaultAddress(id) {
  return request({ url: `/boss/addresses/${id}/default`, method: "PUT" });
}

// 联系人管理
export function listContacts(userId) {
  return request({ url: `/boss/contacts?userId=${userId}` });
}

export function createContact(data) {
  return request({ url: `/boss/contacts`, method: "POST", data });
}

export function deleteContact(id) {
  return request({ url: `/boss/contacts/${id}`, method: "DELETE" });
}

export function setDefaultContact(id) {
  return request({ url: `/boss/contacts/${id}/default`, method: "PUT" });
}

// 人才管理
export function searchTalent(params = {}) {
  return request({ url: `/talent/search?${query({ page: 0, size: 20, ...params })}` });
}

export function listFavoriteTalent(bossId) {
  return request({ url: `/talent/favorites?bossId=${bossId}` });
}

export function favoriteTalent(data) {
  return request({ url: `/talent/favorites`, method: "POST", data });
}

export function unfavoriteTalent(id) {
  return request({ url: `/talent/favorites/${id}`, method: "DELETE" });
}

export function listHistoryTalent(bossId) {
  return request({ url: `/talent/history?bossId=${bossId}` });
}

export function inviteWorker(data) {
  return request({ url: `/talent/invite`, method: "POST", data });
}

export function getBossBlacklist(bossId) {
  return request({ url: `/talent/blacklist?bossId=${bossId}` });
}

// ============ Phase 3: 零工端岗位与订单 ============

export function filterJobs(params = {}) {
  return request({ url: `/boss/order/filter?${query({ page: 0, size: 20, ...params })}` });
}

export function listFavoriteJobs(userId) {
  return request({ url: `/jobs/favorites?userId=${userId}` });
}

export function favoriteJob(data) {
  return request({ url: `/jobs/favorites`, method: "POST", data });
}

export function unfavoriteJob(id) {
  return request({ url: `/jobs/favorites/${id}`, method: "DELETE" });
}

export function checkFavorite(userId, orderId) {
  return request({ url: `/jobs/favorites/check?userId=${userId}&orderId=${orderId}` });
}

export function listBrowseHistory(userId, params = {}) {
  return request({ url: `/jobs/history?userId=${userId}&${query({ page: 0, size: 20, ...params })}` });
}

export function recordBrowse(userId, orderId) {
  return request({ url: `/jobs/history`, method: "POST", data: { userId, orderId } });
}

export function clearBrowseHistory(userId) {
  return request({ url: `/jobs/history?userId=${userId}`, method: "DELETE" });
}

export function listMonthlyOrders(userId) {
  return request({ url: `/boss/order/monthly?userId=${userId}` });
}

export function listPressSalaryOrders(userId) {
  return request({ url: `/boss/order/press-salary?userId=${userId}` });
}

export function getSettlementDetail(id) {
  return request({ url: `/settle/${id}/detail` });
}

// ============ Phase 4: 财务系统 ============

// 积分
export function getPoints(userId) {
  return request({ url: `/points/${userId}` });
}

export function getPointsFlows(userId, params = {}) {
  return request({ url: `/points/${userId}/flows?${query({ page: 0, size: 20, ...params })}` });
}

// 奖励
export function listRewards() {
  return request({ url: `/rewards` });
}

export function getRewardDetail(id) {
  return request({ url: `/rewards/${id}` });
}

export function exchangeReward(id, userId) {
  return request({ url: `/rewards/${id}/exchange?userId=${userId}`, method: "POST" });
}

export function listRewardExchanges(userId) {
  return request({ url: `/rewards/exchanges?userId=${userId}` });
}

// 优惠券
export function listCoupons(userId, status) {
  return request({ url: `/coupons?userId=${userId}${status ? `&status=${status}` : ""}` });
}

export function claimCoupon(id, userId) {
  return request({ url: `/coupons/${id}/claim?userId=${userId}`, method: "POST" });
}

// 押金
export function listDeposits(userId) {
  return request({ url: `/deposits/${userId}` });
}

export function payDeposit(data) {
  return request({ url: `/deposits`, method: "POST", data });
}

export function refundDeposit(id) {
  return request({ url: `/deposits/${id}/refund`, method: "POST" });
}

// 邀请
export function getInviteCode(userId) {
  return request({ url: `/invite/code?userId=${userId}` });
}

export function getInvitePoster(userId) {
  return request({ url: `/invite/poster?userId=${userId}` });
}

export function getInviteRelations(userId) {
  return request({ url: `/invite/relations?userId=${userId}` });
}

// 子账号
export function listSubAccounts(parentId) {
  return request({ url: `/boss/sub-accounts?parentId=${parentId}` });
}

export function createSubAccount(data) {
  return request({ url: `/boss/sub-accounts`, method: "POST", data });
}

export function deleteSubAccount(id) {
  return request({ url: `/boss/sub-accounts/${id}`, method: "DELETE" });
}

export function updateSubAccountRole(id, role) {
  return request({ url: `/boss/sub-accounts/${id}/role?role=${encodeURIComponent(role)}`, method: "PUT" });
}

// 勋章
export function listBadges() {
  return request({ url: `/badges` });
}

export function listUserBadges(userId) {
  return request({ url: `/badges/user/${userId}` });
}

// 星级
export function getStarLevel(userId) {
  return request({ url: `/star-level/${userId}` });
}

// 费用/支付明细
export function listExpenses(userId, params = {}) {
  return request({ url: `/expenses?userId=${userId}&${query({ page: 0, size: 20, ...params })}` });
}

export function listPayments(userId, params = {}) {
  return request({ url: `/payments?userId=${userId}&${query({ page: 0, size: 20, ...params })}` });
}

// ============ Phase 5: 消息与通知 ============

export function getMessageDetail(id) {
  return request({ url: `/message/${id}` });
}

export function listSystemNotices(userId, params = {}) {
  return request({ url: `/message/system?userId=${userId}&${query({ page: 0, size: 20, ...params })}` });
}

export function getNotificationSettings(userId) {
  return request({ url: `/notification-settings/${userId}` });
}

export function updateNotificationSettings(userId, data) {
  return request({ url: `/notification-settings/${userId}`, method: "PUT", data });
}

export function listMissedCalls(userId, params = {}) {
  return request({ url: `/missed-calls?userId=${userId}&${query({ page: 0, size: 20, ...params })}` });
}

export function markMissedCallRead(id) {
  return request({ url: `/missed-calls/${id}/read`, method: "PUT" });
}

export function startChatSession(userId) {
  return request({ url: `/chat/sessions`, method: "POST", data: { userId } });
}

export function listChatMessages(sessionId, params = {}) {
  return request({ url: `/chat/sessions/${sessionId}/messages?${query({ page: 0, size: 50, ...params })}` });
}

export function sendChatMessage(sessionId, data) {
  return request({ url: `/chat/sessions/${sessionId}/messages`, method: "POST", data });
}

export function listNotices(params = {}) {
  return request({ url: `/notices?${query({ page: 0, size: 20, ...params })}` });
}

// ============ Phase 6: 学习中心与规则 ============

export function listCourses(params = {}) {
  return request({ url: `/courses?${query({ page: 0, size: 20, ...params })}` });
}

export function getCourseDetail(id) {
  return request({ url: `/courses/${id}` });
}

export function listCourseVideos(id) {
  return request({ url: `/courses/${id}/videos` });
}

export function getExam(courseId) {
  return request({ url: `/exams/${courseId}` });
}

export function submitExam(courseId, data) {
  return request({ url: `/exams/${courseId}/submit`, method: "POST", data });
}

export function getExamResult(userId, examId) {
  return request({ url: `/exams/result?userId=${userId}&examId=${examId}` });
}

export function listTrainingTasks(userId) {
  return request({ url: `/training-tasks?userId=${userId}` });
}

export function assignTrainingTask(data) {
  return request({ url: `/training-tasks`, method: "POST", data });
}

export function completeTrainingTask(id) {
  return request({ url: `/training-tasks/${id}/complete`, method: "PUT" });
}

export function listRules(params = {}) {
  return request({ url: `/rules?${query(params)}` });
}

export function getRuleDetail(id) {
  return request({ url: `/rules/${id}` });
}

// ============ Phase 7: 保险/合同/社群/客服 ============

export function listInsurance(userId) {
  return request({ url: `/insurance?userId=${userId}` });
}

export function buyInsurance(data) {
  return request({ url: `/insurance`, method: "POST", data });
}

export function getInsuranceDetail(id) {
  return request({ url: `/insurance/${id}` });
}

export function listContracts(bossId) {
  return request({ url: `/boss/contracts?bossId=${bossId}` });
}

export function createContract(data) {
  return request({ url: `/boss/contracts`, method: "POST", data });
}

export function listSocialGroups() {
  return request({ url: `/social-groups` });
}

export function listFaq(params = {}) {
  return request({ url: `/faq?${query(params)}` });
}
