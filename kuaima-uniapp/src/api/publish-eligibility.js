import { request } from "@/api/http";

const APPROVED = "APPROVED";

export function getBossPublishEligibility() {
  return request({ url: "/boss/publish-eligibility" });
}

function normalizeStatus(value) {
  return String(value || "UNVERIFIED").toUpperCase();
}

function normalizeEligibility(data = {}) {
  const realnameStatus = normalizeStatus(data.realnameStatus);
  const enterpriseStatus = normalizeStatus(data.enterpriseStatus);
  const missing = Array.isArray(data.missing) ? data.missing.map((item) => String(item).toUpperCase()) : [];
  return {
    canPublish: data.canPublish === true && realnameStatus === APPROVED && enterpriseStatus === APPROVED,
    realnameStatus,
    enterpriseStatus,
    missing,
  };
}

function showStatusMessage(status, label) {
  if (status === "PENDING") {
    uni.showToast({ title: "认证审核中", icon: "none" });
    return true;
  }
  if (status === "REJECTED") {
    uni.showToast({ title: `${label}认证未通过，请重新提交认证`, icon: "none" });
    return false;
  }
  return false;
}

/**
 * 检查老板是否具备发布招工资格。
 * redirect=false 时只返回检查结果，页面可自行决定返回或展示内容。
 */
export async function checkBossPublishEligibility({ redirect = true } = {}) {
  const token = uni.getStorageSync("token");
  if (!token) {
    const result = { canPublish: false, realnameStatus: "UNVERIFIED", enterpriseStatus: "UNVERIFIED", missing: ["REALNAME", "ENTERPRISE"] };
    if (redirect) {
      uni.showToast({ title: "请先登录", icon: "none" });
      setTimeout(() => uni.reLaunch({ url: "/pages/login/login?role=boss" }), 200);
    }
    return result;
  }

  try {
    const result = normalizeEligibility(await getBossPublishEligibility());
    if (result.canPublish) return result;
    if (!redirect) return result;

    // 个人认证优先于企业认证处理。
    if (result.realnameStatus !== APPROVED) {
      if (!showStatusMessage(result.realnameStatus, "个人")) {
        uni.navigateTo({ url: "/pages/boss/realname" });
      }
      return result;
    }
    if (result.enterpriseStatus !== APPROVED) {
      if (!showStatusMessage(result.enterpriseStatus, "企业")) {
        uni.navigateTo({ url: "/pages/boss/enterprise-cert" });
      }
    }
    return result;
  } catch (error) {
    if (error?.statusCode === 401 || error?.code === 401) {
      uni.showToast({ title: "登录已失效，请重新登录", icon: "none" });
      if (redirect) setTimeout(() => uni.reLaunch({ url: "/pages/login/login?role=boss" }), 200);
      return { canPublish: false, unauthorized: true, realnameStatus: "UNVERIFIED", enterpriseStatus: "UNVERIFIED", missing: ["REALNAME", "ENTERPRISE"] };
    }
    uni.showToast({ title: error?.message || "认证状态查询失败，请稍后重试", icon: "none" });
    return { canPublish: false, requestFailed: true, realnameStatus: "UNVERIFIED", enterpriseStatus: "UNVERIFIED", missing: [] };
  }
}

export function redirectByPublishEligibility(result = {}) {
  if ([result.realnameStatus, result.enterpriseStatus].includes("PENDING")) {
    uni.showToast({ title: "认证审核中", icon: "none" });
    return;
  }
  const missing = Array.isArray(result.missing) ? result.missing : [];
  if (missing.includes("REALNAME")) {
    uni.navigateTo({ url: "/pages/boss/realname" });
    return;
  }
  if (missing.includes("ENTERPRISE")) {
    uni.navigateTo({ url: "/pages/boss/enterprise-cert" });
    return;
  }
  if (result.realnameStatus !== APPROVED) {
    if (result.realnameStatus === "REJECTED") {
      uni.showToast({ title: "个人认证未通过，请重新提交认证", icon: "none" });
    }
    uni.navigateTo({ url: "/pages/boss/realname" });
    return;
  }
  if (result.enterpriseStatus !== APPROVED) {
    if (result.enterpriseStatus === "REJECTED") {
      uni.showToast({ title: "企业认证未通过，请重新提交认证", icon: "none" });
    }
    uni.navigateTo({ url: "/pages/boss/enterprise-cert" });
  }
}
