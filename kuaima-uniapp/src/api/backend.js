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

export function getUser(id) {
  return request({ url: `/user/${id}` });
}

export function updateUser(id, data) {
  return request({ url: `/user/${id}`, method: "PUT", data });
}

export function submitRealname(id, data) {
  return request({ url: `/user/${id}/realname`, method: "POST", data });
}

export function listCertifications(id) {
  return request({ url: `/user/${id}/certifications` });
}

export function getCredit(id) {
  return request({ url: `/user/${id}/credit` });
}

export function listCreditFlows(id, params = {}) {
  return request({
    url: `/user/${id}/credit/flows?${query({ page: 0, size: 20, ...params })}`,
  });
}

export function switchRole(role) {
  return request({
    url: `/auth/switch-role?${query({ role })}`,
    method: "POST",
  });
}

export function cancelAccount(userId, reason) {
  return request({
    url: `/auth/cancel?${query({ userId, reason })}`,
    method: "POST",
  });
}

export function submitEnterpriseCertification(data) {
  return request({ url: "/boss/enterprise-cert", method: "POST", data });
}

export function getBossProfile(userId) {
  return request({ url: `/boss/profile/${userId}` });
}

export function getBossStats(userId) {
  return request({ url: `/boss/stats?${query({ userId })}` });
}

export function saveOrderDraft(data) {
  return request({ url: "/boss/order/draft", method: "POST", data });
}

export function listOrderDrafts(userId) {
  return request({ url: `/boss/orders/drafts?${query({ userId })}` });
}

export function updateOrderDraft(id, data) {
  return request({ url: `/boss/order/${id}/draft`, method: "PUT", data });
}

export function listJobCategories() {
  return request({ url: "/boss/job-categories" });
}

export function listBossAddresses(userId) {
  return request({ url: `/boss/addresses?${query({ userId })}` });
}

export function createBossAddress(data) {
  return request({ url: "/boss/addresses", method: "POST", data });
}

export function deleteBossAddress(id) {
  return request({ url: `/boss/addresses/${id}`, method: "DELETE" });
}

export function setDefaultBossAddress(id) {
  return request({ url: `/boss/addresses/${id}/default`, method: "PUT" });
}

export function listBossContacts(userId) {
  return request({ url: `/boss/contacts?${query({ userId })}` });
}

export function createBossContact(data) {
  return request({ url: "/boss/contacts", method: "POST", data });
}

export function deleteBossContact(id) {
  return request({ url: `/boss/contacts/${id}`, method: "DELETE" });
}

export function setDefaultBossContact(id) {
  return request({ url: `/boss/contacts/${id}/default`, method: "PUT" });
}

export function searchTalents(params = {}) {
  return request({
    url: `/talent/search?${query({ page: 0, size: 20, ...params })}`,
  });
}

export function listFavoriteTalents(bossId) {
  return request({ url: `/talent/favorites?${query({ bossId })}` });
}

export function favoriteTalent(data) {
  return request({ url: "/talent/favorites", method: "POST", data });
}

export function unfavoriteTalent(id) {
  return request({ url: `/talent/favorites/${id}`, method: "DELETE" });
}

export function listTalentHistory(bossId) {
  return request({ url: `/talent/history?${query({ bossId })}` });
}

export function inviteTalent(data) {
  return request({ url: "/talent/invite", method: "POST", data });
}

export function listTalentBlacklist(bossId) {
  return request({ url: `/talent/blacklist?${query({ bossId })}` });
}

export function filterOrders(params = {}) {
  return request({
    url: `/boss/order/filter?${query({ page: 0, size: 20, ...params })}`,
  });
}

export function listMonthlyOrders(userId) {
  return request({ url: `/boss/order/monthly?${query({ userId })}` });
}

export function listPressSalaryOrders(userId) {
  return request({ url: `/boss/order/press-salary?${query({ userId })}` });
}

export function listFavoriteJobs(userId) {
  return request({ url: `/jobs/favorites?${query({ userId })}` });
}

export function favoriteJob(data) {
  return request({ url: "/jobs/favorites", method: "POST", data });
}

export function unfavoriteJob(id) {
  return request({ url: `/jobs/favorites/${id}`, method: "DELETE" });
}

export function checkFavoriteJob(userId, orderId) {
  return request({
    url: `/jobs/favorites/check?${query({ userId, orderId })}`,
  });
}

export function listBrowseHistory(userId, params = {}) {
  return request({
    url: `/jobs/history?${query({ userId, page: 0, size: 20, ...params })}`,
  });
}

export function recordJobBrowse(data) {
  return request({ url: "/jobs/history", method: "POST", data });
}

export function clearJobBrowseHistory(userId) {
  return request({
    url: `/jobs/history?${query({ userId })}`,
    method: "DELETE",
  });
}

export function getSettlementDetail(id) {
  return request({ url: `/settle/${id}/detail` });
}

export function getMessage(id) {
  return request({ url: `/message/${id}` });
}

export function listSystemMessages(userId, params = {}) {
  return request({
    url: `/message/system?${query({ userId, page: 0, size: 20, ...params })}`,
  });
}

export function getNotificationSettings(userId) {
  return request({ url: `/notification-settings/${userId}` });
}

export function updateNotificationSettings(userId, data) {
  return request({
    url: `/notification-settings/${userId}`,
    method: "PUT",
    data,
  });
}

export function getPoints(userId) {
  return request({ url: `/points/${userId}` });
}
export function listPointFlows(userId, params = {}) {
  return request({
    url: `/points/${userId}/flows?${query({ page: 0, size: 20, ...params })}`,
  });
}
export function listRewards() {
  return request({ url: "/rewards" });
}
export function listRewardExchanges(userId) {
  return request({ url: `/rewards/exchanges?${query({ userId })}` });
}
export function exchangeReward(id, userId) {
  return request({
    url: `/rewards/${id}/exchange?${query({ userId })}`,
    method: "POST",
  });
}
export function listCoupons(userId, status) {
  return request({ url: `/coupons?${query({ userId, status })}` });
}
export function claimCoupon(id, userId) {
  return request({
    url: `/coupons/${id}/claim?${query({ userId })}`,
    method: "POST",
  });
}
export function getDeposit(userId) {
  return request({ url: `/deposits/${userId}` });
}
export function createDeposit(data) {
  return request({ url: "/deposits", method: "POST", data });
}
export function refundDeposit(id) {
  return request({ url: `/deposits/${id}/refund`, method: "POST" });
}
export function getInviteCode(userId) {
  return request({ url: `/invite/code?${query({ userId })}` });
}
export function getInvitePoster(userId) {
  return request({ url: `/invite/poster?${query({ userId })}` });
}
export function listInviteRelations(userId) {
  return request({ url: `/invite/relations?${query({ userId })}` });
}
export function listBadges() {
  return request({ url: "/badges" });
}
export function listUserBadges(userId) {
  return request({ url: `/badges/user/${userId}` });
}
export function getStarLevel(userId) {
  return request({ url: `/star-level/${userId}` });
}
export function listInsurance(userId) {
  return request({ url: `/insurance?${query({ userId })}` });
}
export function getInsurance(id) {
  return request({ url: `/insurance/${id}` });
}
export function buyInsurance(data) {
  return request({ url: "/insurance", method: "POST", data });
}

export function listCourses(params = {}) {
  return request({
    url: `/courses?${query({ page: 0, size: 20, ...params })}`,
  });
}

export function getCourse(id) {
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
  return request({ url: `/exams/result?${query({ userId, examId })}` });
}

export function listTrainingTasks(userId) {
  return request({ url: `/training-tasks?${query({ userId })}` });
}

export function completeTrainingTask(id) {
  return request({ url: `/training-tasks/${id}/complete`, method: "PUT" });
}

export function listRules(category) {
  return request({ url: `/rules?${query({ category })}` });
}

export function getRule(id) {
  return request({ url: `/rules/${id}` });
}

export function listFaq(category) {
  return request({ url: `/faq?${query({ category })}` });
}

export function createChatSession(data) {
  return request({ url: "/chat/sessions", method: "POST", data });
}

export function listChatMessages(id, params = {}) {
  return request({
    url: `/chat/sessions/${id}/messages?${query({ page: 0, size: 50, ...params })}`,
  });
}

export function sendChatMessage(id, data) {
  return request({
    url: `/chat/sessions/${id}/messages`,
    method: "POST",
    data,
  });
}

export function listSocialGroups() {
  return request({ url: "/social-groups" });
}

export function listBossContracts(bossId) {
  return request({ url: `/boss/contracts?${query({ bossId })}` });
}

export function getBossContract(id) {
  return request({ url: `/boss/contracts/${id}` });
}

export function createBossContract(data) {
  return request({ url: "/boss/contracts", method: "POST", data });
}

export function listExpenses(userId, params = {}) {
  return request({
    url: `/expenses?${query({ userId, page: 0, size: 20, ...params })}`,
  });
}

export function listPayments(userId, params = {}) {
  return request({
    url: `/payments?${query({ userId, page: 0, size: 20, ...params })}`,
  });
}

export function listNotices(params = {}) {
  return request({
    url: `/notices?${query({ page: 0, size: 20, ...params })}`,
  });
}

export function listMissedCalls(userId, params = {}) {
  return request({
    url: `/missed-calls?${query({ userId, page: 0, size: 20, ...params })}`,
  });
}

export function readMissedCall(id) {
  return request({ url: `/missed-calls/${id}/read`, method: "PUT" });
}
