import { request } from "@/api/http";

function query(params) {
  return Object.entries(params || {})
    .filter(([, value]) => value !== undefined && value !== null && value !== "")
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join("&");
}

export const RESUME_STATUS_TEXT = { NEW: "新", PENDING: "待处理", VIEWED: "已查看", SENT: "已投递" };
export const RESUME_STATUS_CLS = { NEW: "blue", PENDING: "orange", VIEWED: "green", SENT: "gray" };

/** 简历库统计 */
export function getResumeStats() {
  return request({ url: "/boss/resumes/stats" });
}

/** 最新简历 */
export function getLatestResumes() {
  return request({ url: "/boss/resumes/latest" });
}

/** 简历列表 */
export function getResumes(filters = {}, page = 0, size = 20) {
  return request({ url: `/boss/resumes?${query({ ...filters, page, size })}` });
}

/** 简历详情 */
export function getResumeDetail(resumeId) {
  return request({ url: `/boss/resumes/${resumeId}` });
}

/** 收藏/取消收藏 */
export function toggleResumeFavorite(resumeId) {
  return request({ url: `/boss/resumes/${resumeId}/favorite`, method: "PUT" });
}

/** 更新简历状态 */
export function updateResumeStatus(resumeId, status) {
  return request({ url: `/boss/resumes/${resumeId}/status`, method: "PUT", data: { status } });
}

/** 删除简历 */
export function deleteResume(resumeId) {
  return request({ url: `/boss/resumes/${resumeId}`, method: "DELETE" });
}

/** 批量操作（action: read/fav/del/process） */
export function batchResumeAction(ids, action) {
  return request({ url: "/boss/resumes/batch", method: "POST", data: { ids, action } });
}

/** 新增导入记录 */
export function createResumeImport(data) {
  return request({ url: "/boss/resumes/imports", method: "POST", data });
}

/** 导入记录列表 */
export function getResumeImports() {
  return request({ url: "/boss/resumes/imports" });
}
