import { request } from "@/api/http";

export function wechatLogin(data) {
  return request({ url: "/auth/wechat/login", method: "POST", data });
}

export function getCurrentUser() {
  return request({ url: "/auth/me" });
}

export const LOGIN_STORAGE_KEYS = [
  "token",
  "userId",
  "userInfo",
  "role",
  "currentRole",
  "userPhone",
  "certStatus",
  "workerCertStatus",
  "bossCertStatus",
];

export function clearLoginStorage() {
  LOGIN_STORAGE_KEYS.forEach((key) => uni.removeStorageSync(key));
}

function redirectToLogin(roleHint) {
  const url = roleHint ? `/pages/login/login?role=${encodeURIComponent(roleHint)}` : "/pages/login/login";
  return new Promise((resolve) => {
    uni.reLaunch({ url, complete: resolve });
  });
}

export async function handleTokenInvalid(options = {}) {
  const { showToast = true, toastTitle = "登录已失效，请重新登录", role = "", clear = true } = options;
  if (clear) clearLoginStorage();
  if (showToast) uni.showToast({ title: toastTitle, icon: "none" });
  await new Promise((resolve) => setTimeout(resolve, 260));
  return redirectToLogin(role);
}

export async function logout() {
  let requestSucceeded = false;
  try {
    await request({
      url: "/auth/logout",
      method: "POST",
      skipUserIdHeader: true,
    });
    requestSucceeded = true;
  } catch (error) {
    console.warn("退出登录接口调用失败，继续清理本地登录态", error);
  } finally {
    clearLoginStorage();
    await redirectToLogin("");
    if (requestSucceeded) {
      try {
        uni.showToast({ title: "已退出登录", icon: "success" });
      } catch (_) {}
    }
  }
}
