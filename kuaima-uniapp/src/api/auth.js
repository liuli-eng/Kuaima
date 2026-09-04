import { request } from "@/api/http";

export function wechatLogin(data) {
  return request({ url: "/auth/wechat/login", method: "POST", data });
}

export function getCurrentUser() {
  return request({ url: "/auth/me" });
}
