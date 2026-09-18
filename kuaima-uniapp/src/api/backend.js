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

function normalizeId(value, fieldName) {
  const id = Number(value);
  if (!Number.isSafeInteger(id) || id <= 0) {
    throw new Error(`${fieldName} 不合法`);
  }
  return id;
}

export async function getCertificationStatus(userId) {
  const id = userId || uni.getStorageSync("userId") || "2001";
  const profile = await request({ url: `/user/${encodeURIComponent(id)}` });
  let certStatus = profile?.certStatus || "未认证";

  // 当前后端审核接口只更新认证记录，可能未同步 User.certStatus；
  // 因此用户资料未显示通过时，再以最新的个人实名认证记录为准。
  if (!["已通过", "通过", "已认证"].includes(certStatus)) {
    try {
      const records = await request({
        url: `/user/${encodeURIComponent(id)}/certifications`,
      });
      const rows = Array.isArray(records)
        ? records
        : records?.records || records?.content || [];
      const realname = rows.find((item) =>
        ["REALNAME", "零工实名", "个人实名", "实名认证"].includes(
          item?.type || item?.certType,
        ),
      );
      if (realname?.status) certStatus = realname.status;
    } catch (_) {}
  }

  return { ...(profile || {}), certStatus };
}

export function submitCertification(data) {
  const userId = uni.getStorageSync("userId") || "2001";
  return request({
    url: `/user/${encodeURIComponent(userId)}/realname`,
    method: "POST",
    data,
  });
}

export function listOrders(params = {}) {
  return request({
    url: `/boss/order?${query({ page: 0, size: 20, ...params })}`,
    skipUserIdHeader: true,
  });
}

/** 老板端招工订单分页列表，保留后端的 page/total 等分页元数据。 */
export function listBossOrders(params = {}) {
  return request({
    url: `/boss/order?${query({ page: 0, size: 20, ...params })}`,
    rawResponse: true,
    skipUserIdHeader: true,
  });
}

export function getBossOrderStats(dateRange = "ALL") {
  return request({
    url: `/boss/order/stats?${query({ dateRange })}`,
    skipUserIdHeader: true,
  });
}

export function getOrder(id) {
  return request({ url: `/boss/order/${id}`, skipUserIdHeader: true });
}

/** 老板端全局招工默认设置，身份由当前 JWT 判断。 */
export function getBossRecruitSettings() {
  return request({ url: "/boss/recruit-settings", skipUserIdHeader: true });
}

export function updateBossRecruitSettings(data = {}) {
  return request({
    url: "/boss/recruit-settings",
    method: "PUT",
    data,
    skipUserIdHeader: true,
  });
}

export function createOrder(data) {
  return request({
    url: "/boss/order",
    method: "POST",
    data,
    skipUserIdHeader: true,
  });
}

export function updateOrder(id, data) {
  return request({
    url: `/boss/order/${id}`,
    method: "PUT",
    data,
    skipUserIdHeader: true,
  });
}

export function deleteOrder(id) {
  return request({
    url: `/boss/order/${id}`,
    method: "DELETE",
    skipUserIdHeader: true,
  });
}

export function changeOrderStatus(id, target) {
  return request({
    url: `/boss/order/${id}/status?target=${encodeURIComponent(target)}`,
    method: "PUT",
    skipUserIdHeader: true,
  });
}

export function createBossOrderTemplate(orderId, data) {
  return request({
    url: `/boss/order/${normalizeId(orderId, "orderId")}/template`,
    method: "POST",
    data,
    skipUserIdHeader: true,
  });
}

export function listBossOrderTemplates(params = {}) {
  return request({
    url: `/boss/order/templates?${query({ page: 0, size: 20, ...params })}`,
    skipUserIdHeader: true,
  });
}

export function getBossOrderTemplate(id) {
  return request({
    url: `/boss/order/templates/${normalizeId(id, "templateId")}`,
    skipUserIdHeader: true,
  });
}

export function updateBossOrderTemplate(id, data) {
  return request({
    url: `/boss/order/templates/${normalizeId(id, "templateId")}`,
    method: "PUT",
    data,
    skipUserIdHeader: true,
  });
}

export function deleteBossOrderTemplate(id) {
  return request({
    url: `/boss/order/templates/${normalizeId(id, "templateId")}`,
    method: "DELETE",
    skipUserIdHeader: true,
  });
}

export function getBossRecruitAccounts() {
  return request({ url: "/boss/recruit-accounts", skipUserIdHeader: true });
}

export function addBossRecruitAccount(data = {}) {
  return request({
    url: "/boss/recruit-accounts",
    method: "POST",
    data,
    skipUserIdHeader: true,
  });
}

/** 已关联老板账号快速登录，后端需校验绑定关系并签发目标账号 JWT。 */
export function quickLoginBossRecruitAccount(accountId) {
  return request({
    url: `/boss/recruit-accounts/${normalizeId(accountId, "accountId")}/quick-login`,
    method: "POST",
    skipUserIdHeader: true,
  });
}

export function switchBossRecruitAccount(accountId) {
  return request({
    url: "/boss/recruit-accounts/current",
    method: "PUT",
    data: { accountId: normalizeId(accountId, "accountId") },
    skipUserIdHeader: true,
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

/** 零工端公开岗位，只返回后端允许展示的“招工中”岗位。 */
export function listPublicJobs(params = {}) {
  return request({
    url: `/jobs?${query({ page: 0, size: 20, ...params })}`,
  });
}

export function getPublicJob(orderId) {
  return request({ url: `/jobs/${normalizeId(orderId, "orderId")}` });
}

/** 零工身份由 JWT 获取，禁止再提交 userId。 */
export function applyPublicJob(orderId, data = {}) {
  return request({
    url: `/jobs/${normalizeId(orderId, "orderId")}/apply`,
    method: "POST",
    data: {
      remark: String(data.remark || "").trim(),
      trial: data.trial === true,
    },
  });
}

export function listWorkerItems(userId) {
  return request({
    url: `/boss/user/items?userId=${encodeURIComponent(userId)}`,
  });
}

/** 零工端查询当前登录零工本人的报名条目（岗位维度）列表。 */
export function listWorkerApplications(userId) {
  const uid = userId || uni.getStorageSync("userId");
  return request({
    url: `/worker/items${uid ? `?userId=${encodeURIComponent(uid)}` : ""}`,
  });
}

/** 零工端取消自己的报名条目；reason 可为空字符串。 */
export function cancelWorkerItem(id, reason) {
  const r = String(reason || "").trim();
  return request({
    url: `/worker/items/${normalizeId(id, "itemId")}/cancel${r ? `?reason=${encodeURIComponent(r)}` : ""}`,
    method: "PUT",
  });
}

/** 当前登录零工的个人资料，身份由 JWT 获取。 */
export function getWorkerProfile() {
  return request({ url: "/worker/profile" });
}

/** 零工端“我的”页面统计概览，身份由当前 JWT 获取。 */
export function getWorkerProfileOverview() {
  return request({
    url: "/worker/profile/overview",
    skipUserIdHeader: true,
  });
}

export function updateWorkerProfile(data = {}) {
  return request({ url: "/worker/profile", method: "PUT", data });
}

/** 零工订单聚合列表，岗位信息由后端一次返回。 */
export function listWorkerOrders(params = {}) {
  return request({
    url: `/worker/orders?${query({ page: 0, size: 20, ...params })}`,
  });
}

/** 零工评价订单所属老板；同一报名条目重复提交时更新原评价。 */
export function saveWorkerBossReview(itemId, data) {
  return request({
    url: `/worker/items/${normalizeId(itemId, "itemId")}/boss-review`,
    method: "PUT",
    data,
  });
}

export function listOrderItems(orderId) {
  return request({
    url: `/boss/order/${orderId}/items`,
    skipUserIdHeader: true,
  });
}

export function hireOrderItem(id) {
  return request({ url: `/boss/item/${id}/hire`, method: "PUT" });
}

export function rejectOrderItem(id, reason) {
  return request({
    url: `/boss/item/${id}/reject?${query({ reason: String(reason || "").trim() })}`,
    method: "PUT",
  });
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

export function listBossPendingSettlements() {
  return request({ url: "/boss/settlements/pending" });
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

export function getBossMessageSummary() {
  return request({ url: "/boss/message/summary" });
}

export function listBossMessageHistory(params = {}) {
  return request({
    url: `/boss/message/history?${query({ page: 0, size: 20, ...params })}`,
    rawResponse: true,
  });
}

/** 获取已发布公告列表（管理员人工创建） */
export function listNotices(params = {}) {
  return request({
    url: `/notices?${query({ page: 0, size: 20, ...params })}`,
  });
}

export function unreadMessages(userId, role) {
  return request({ url: `/message/unread?${query({ userId, role })}` });
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

export function getBossProfileStats(userId) {
  return request({ url: `/boss/profile/${userId}/stats` });
}

export function getBossProfileAssets(userId) {
  return request({ url: `/boss/profile/${userId}/assets` });
}

export function getBossStats(userId) {
  return request({ url: `/boss/stats?${query({ userId })}` });
}

export function getBossHomeOverview(params = {}) {
  return request({
    url: `/boss/home/overview?${query(params)}`,
    skipUserIdHeader: true,
  });
}

export function getBossHomeSchedule(date, accountId) {
  return request({
    url: `/boss/home/schedule?${query({ date, accountId })}`,
    skipUserIdHeader: true,
  });
}

/** 当前老板今日开工码/早退码状态，身份由 JWT 获取。 */
export function getBossAttendanceCodes() {
  return request({ url: "/boss/attendance-codes", skipUserIdHeader: true, skipMock: true });
}

export function refreshBossWorkCode() {
  return request({
    url: "/boss/attendance-codes/work/refresh",
    method: "POST",
    skipUserIdHeader: true,
    skipMock: true,
  });
}

export function refreshBossLeaveCode() {
  return request({
    url: "/boss/attendance-codes/leave/refresh",
    method: "POST",
    skipUserIdHeader: true,
    skipMock: true,
  });
}

export function workerCheckIn(orderId, code) {
  return request({
    url: `/worker/orders/${normalizeId(orderId, "orderId")}/check-in`,
    method: "POST",
    data: { code: String(code || "").trim() },
    skipUserIdHeader: true,
    skipMock: true,
  });
}

export function workerEarlyLeave(orderId, code) {
  return request({
    url: `/worker/orders/${normalizeId(orderId, "orderId")}/early-leave`,
    method: "POST",
    data: { code: String(code || "").trim() },
    skipUserIdHeader: true,
    skipMock: true,
  });
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

export function listJobCategoryTree() {
  return request({ url: "/job-categories/tree" });
}

export function listHotJobCategories() {
  return request({ url: "/job-categories/hot" });
}

export function searchJobCategories(keyword, size = 20) {
  return request({
    url: `/job-categories/search?${query({ keyword, size })}`,
  });
}

export function createJobEnterpriseType(data = {}) {
  return request({
    url: "/boss/job-categories/enterprise-types",
    method: "POST",
    data,
  });
}

export function createJobCategory(data = {}) {
  return request({
    url: "/boss/job-categories/jobs",
    method: "POST",
    data,
  });
}

export function listBossAddresses(userId) {
  return request({ url: `/boss/addresses?${query({ userId })}` });
}
export function searchBossAddresses(userId, keyword) {
  return request({
    url: `/boss/addresses/search?${query({ userId, keyword })}`,
  });
}

export function createBossAddress(data) {
  return request({ url: "/boss/addresses", method: "POST", data });
}

export function deleteBossAddress(id) {
  return request({ url: `/boss/addresses/${id}`, method: "DELETE" });
}
export function updateBossAddress(id, data) {
  return request({
    url: `/boss/addresses/${encodeURIComponent(id)}`,
    method: "PUT",
    data,
  });
}
export function useBossAddress(id) {
  return request({
    url: `/boss/addresses/${encodeURIComponent(id)}/use`,
    method: "PUT",
  });
}

export function setDefaultBossAddress(id) {
  return request({ url: `/boss/addresses/${id}/default`, method: "PUT" });
}

/** 老板端招工地址，归属由当前 JWT 判断。 */
export function listBossRecruitAddresses() {
  return request({ url: "/boss/recruit-addresses", skipUserIdHeader: true });
}

export function createBossRecruitAddress(data = {}) {
  return request({
    url: "/boss/recruit-addresses",
    method: "POST",
    data,
    skipUserIdHeader: true,
  });
}

export function updateBossRecruitAddress(id, data = {}) {
  return request({
    url: `/boss/recruit-addresses/${normalizeId(id, "addressId")}`,
    method: "PUT",
    data,
    skipUserIdHeader: true,
  });
}

export function deleteBossRecruitAddress(id) {
  return request({
    url: `/boss/recruit-addresses/${normalizeId(id, "addressId")}`,
    method: "DELETE",
    skipUserIdHeader: true,
  });
}

export function setDefaultBossRecruitAddress(id) {
  return request({
    url: `/boss/recruit-addresses/${normalizeId(id, "addressId")}/default`,
    method: "PUT",
    skipUserIdHeader: true,
  });
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

/** 老板端人才库，人才与老板身份均由后端 JWT/真实数据确定。 */
export function listBossTalents(params = {}) {
  return request({
    url: `/boss/talents?${query({ page: 0, size: 20, ...params })}`,
    rawResponse: true,
    skipUserIdHeader: true,
  });
}

export function toggleBossTalentFavorite(workerId, favorite) {
  return request({
    url: `/boss/talents/${normalizeId(workerId, "workerId")}/favorite`,
    method: "PUT",
    data: favorite === undefined ? {} : { favorite: Boolean(favorite) },
    skipUserIdHeader: true,
  });
}

export function inviteBossTalent(workerId, data = {}) {
  return request({
    url: `/boss/talents/${normalizeId(workerId, "workerId")}/invite`,
    method: "POST",
    data,
    skipUserIdHeader: true,
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

export function listTalentHistory() {
  // 后端从当前老板 JWT 识别归属，不再传 bossId，也不使用 X-User-Id 作为查询依据。
  return request({ url: "/talent/history", skipUserIdHeader: true });
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

export function favoriteJob(data = {}) {
  return request({
    url: "/jobs/favorites",
    method: "POST",
    data: {
      userId: normalizeId(data.userId, "userId"),
      orderId: normalizeId(data.orderId, "orderId"),
    },
  });
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

export function recordJobBrowse(data = {}) {
  return request({
    url: "/jobs/history",
    method: "POST",
    data: {
      userId: normalizeId(data.userId, "userId"),
      orderId: normalizeId(data.orderId, "orderId"),
    },
  });
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

/** 老板端积分余额、积分套餐和规则，身份由当前 JWT 判断。 */
export function getBossPointsOverview() {
  return request({ url: "/boss/points", skipUserIdHeader: true });
}

/** 老板端赠送积分给零工，身份由当前 JWT 判断。 */
export function giftBossPoints(data = {}, idempotencyKey = "") {
  return request({
    url: "/boss/points/gift",
    method: "POST",
    data,
    header: idempotencyKey ? { "Idempotency-Key": idempotencyKey } : {},
    skipUserIdHeader: true,
  });
}

/** 老板端积分明细，身份由当前 JWT 判断，支持兑换/购买分类和分页。 */
export function listBossPointRecords(params = {}) {
  return request({
    url: `/boss/points/records?${query({ page: 0, size: 20, category: "EXCHANGE", ...params })}`,
    skipUserIdHeader: true,
    rawResponse: true,
  });
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

export function listBossSubAccounts() {
  return request({ url: "/boss/sub-accounts", skipUserIdHeader: true });
}

export function createBossSubAccount(data = {}) {
  return request({
    url: "/boss/sub-accounts",
    method: "POST",
    data,
    skipUserIdHeader: true,
  });
}

/** 零工课堂首页聚合内容（课程、视频、考试题目和平台规则）。 */
export function getWorkerClassroomOverview() {
  return request({ url: "/worker/classroom/overview", skipUserIdHeader: true });
}

export function getWorkerClassroomQuiz() {
  return request({ url: "/worker/classroom/quiz" });
}

export function submitWorkerClassroomQuiz(data) {
  return request({ url: "/worker/classroom/quiz/submit", method: "POST", data });
}

export function completeTrainingTask(id) {
  return request({ url: `/training-tasks/${id}/complete`, method: "PUT" });
}

export function listRules(category) {
  return request({
    url: `/rules?${query({ category })}`,
    method: "GET",
  });
}

export function getRule(id) {
  return request({
    url: `/rules/${encodeURIComponent(id)}`,
    method: "GET",
  });
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

export function listMissedCalls(userId, params = {}) {
  return request({
    url: `/missed-calls?${query({ userId, page: 0, size: 20, ...params })}`,
  });
}

export function readMissedCall(id) {
  return request({ url: `/missed-calls/${id}/read`, method: "PUT" });
}
