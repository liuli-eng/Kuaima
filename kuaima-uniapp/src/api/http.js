let BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api";
export const USE_MOCK = import.meta.env.VITE_USE_MOCK === "true";

// H5 开发环境通过 Vite 代理访问 /api；微信开发者工具不支持该相对代理地址。
// #ifdef MP-WEIXIN
// 真机预览时请通过 VITE_MP_API_BASE_URL 指向电脑局域网 IP 或 HTTPS 测试域名。
BASE_URL = import.meta.env.VITE_MP_API_BASE_URL || "http://192.168.2.88:8080";
// #endif

function formatError(error, fallback = "请求失败") {
  if (error instanceof Error) return error;
  if (typeof error === "string") return new Error(error);
  if (error && typeof error === "object") {
    const message = error.message || error.errMsg || error.msg || error.error;
    if (message) return new Error(String(message));
    try {
      return new Error(JSON.stringify(error));
    } catch (_) {
      return new Error(fallback);
    }
  }
  return new Error(fallback);
}

function mockResponse(url, method = "GET", data) {
  if (method !== "GET") return { success: true, ...data };
  if (url.includes("/worker/jobs")) {
    return null;
  }
  if (url.includes("/worker/profile"))
    return { name: "晴时见禾", nickname: "晴时见禾", availableAmount: "0.00" };
  if (url === "/worker/wallet" || url.startsWith("/worker/wallet?"))
    return {
      available: "0.00",
      availableBalance: "0.00",
      totalIncome: "0.00",
      pendingAmount: "0.00",
      records: [],
    };
  // 返回 null 让页面保留自身的演示数据，避免无后端时首屏变成空列表。
  if (url.includes("withdraw-records")) return null;
  if (url.includes("/worker/orders")) return null;
  if (url.includes("/worker/settlements")) return null;
  if (url.includes("/worker/appeals")) return null;
  if (url.includes("/worker/insurance")) return null;
  if (url.includes("/worker/notifications")) return null;
  return null;
}

export function request({ url, method = "GET", data, header = {} }) {
  if (USE_MOCK) return Promise.resolve(mockResponse(url, method, data));
  const token = uni.getStorageSync("token");
  const userId = uni.getStorageSync("userId") || "2001";
  const realUrl = resolveBackendUrl(url, userId, data);
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}${realUrl}`,
      method,
      data,
      timeout: 8000,
      header: {
        ...header,
        Authorization: token ? `Bearer ${token}` : "",
        "X-User-Id": userId,
      },
      success(response) {
        const payload = response.data;
        if (
          response.statusCode >= 200 &&
          response.statusCode < 300 &&
          (!payload?.code || payload.code === 200 || payload.code === "200" || payload.code === 0 || payload.code === "0")
        ) {
          resolve(payload?.data ?? payload);
          return;
        }
        reject(formatError(payload, `请求失败（${response.statusCode}）`));
      },
      fail(error) {
        reject(formatError(error, "网络请求失败"));
      },
    });
  });
}

function resolveBackendUrl(url, userId, data) {
  if (url.startsWith("/worker/jobs/")) return url.replace("/worker/jobs/", "/boss/order/");
  if (url === "/worker/jobs" || url.startsWith("/worker/jobs?")) {
    const query = url.includes("?") ? url.substring(url.indexOf("?")) : "?page=0&size=20";
    return `/boss/order${query.replace("pageNo", "page").replace("pageSize", "size")}`;
  }
  if (url.startsWith("/worker/orders/apply/")) {
    const orderId = url.split("/").pop();
    const params = {
      userId,
      ...(data || {}),
    };
    return `/boss/order/${orderId}/apply?${toQuery(params)}`;
  }
  if (url.startsWith("/worker/orders/")) return url.replace("/worker/orders/", "/boss/order/");
  if (url === "/worker/orders" || url.startsWith("/worker/orders?")) {
    return `/boss/user/items?userId=${encodeURIComponent(userId)}`;
  }
  if (url === "/worker/wallet" || url.startsWith("/worker/wallet?")) return `/wallet/${userId}`;
  if (url === "/worker/wallet/records") return `/wallet/${userId}/flows`;
  if (url === "/worker/wallet/withdraw-records") return `/wallet/${userId}/withdraws`;
  if (url === "/worker/wallet/withdraw") {
    const params = {
      userId,
      amount: Math.round(Number(data?.amount || 0) * 100),
      account: data?.account || data?.method || "",
      remark: data?.remark || "",
    };
    return `/wallet/withdraw?${toQuery(params)}`;
  }
  if (url === "/worker/notifications" || url.startsWith("/worker/notifications?")) return `/message/list?userId=${encodeURIComponent(userId)}&page=0&size=20`;
  if (url === "/worker/notification/unread") return `/message/unread?userId=${encodeURIComponent(userId)}`;

  // Phase 1: 用户资料与认证
  if (url.startsWith("/worker/profile/")) return url.replace("/worker/profile/", "/user/");
  if (url.startsWith("/worker/credit/")) return url.replace("/worker/credit/", "/user/").replace("/credit", "/credit");
  if (url === "/worker/credit" || url.startsWith("/worker/credit?")) {
    return `/user/${userId}/credit`;
  }
  if (url.startsWith("/worker/switch-identity")) return `/auth/switch-role`;
  if (url.startsWith("/worker/account-cancel")) return `/auth/cancel?userId=${userId}`;

  // Phase 3: 岗位收藏/浏览
  if (url.startsWith("/worker/favorites")) return url.replace("/worker/favorites", "/jobs/favorites");
  if (url.startsWith("/worker/browse")) return url.replace("/worker/browse", "/jobs/history");

  // Phase 4: 财务
  if (url.startsWith("/worker/coupon")) return url.replace("/worker/coupon", "/coupons");
  if (url.startsWith("/worker/deposit")) return url.replace("/worker/deposit", "/deposits");
  if (url.startsWith("/worker/invite")) return url.replace("/worker/invite", "/invite");
  if (url.startsWith("/worker/badge")) return `/badges/user/${userId}`;
  if (url.startsWith("/worker/star-level")) return `/star-level/${userId}`;
  if (url.startsWith("/worker/insurance")) return url.replace("/worker/insurance", "/insurance");

  // Phase 5: 消息/通知
  if (url.startsWith("/worker/notification-settings")) return url.replace("/worker/notification-settings", "/notification-settings");
  if (url.startsWith("/worker/service-chat")) return url.replace("/worker/service-chat", "/chat");

  // Phase 6: 学习/规则
  if (url.startsWith("/worker/classroom")) return url.replace("/worker/classroom", "/courses");
  if (url.startsWith("/worker/course-detail")) return url.replace("/worker/course-detail", "/courses");
  if (url.startsWith("/worker/course-video")) return url.replace("/worker/course-video", "/courses");
  if (url.startsWith("/worker/rule")) return url.replace("/worker/rule", "/rules");

  // Phase 7: 社群/客服
  if (url.startsWith("/worker/join-group")) return url.replace("/worker/join-group", "/social-groups");
  if (url.startsWith("/worker/faq")) return url.replace("/worker/faq", "/faq");

  // 月结/压薪订单
  if (url.startsWith("/worker/monthly-order")) return `/boss/order/monthly?userId=${userId}`;
  if (url.startsWith("/worker/press-order")) return `/boss/order/press-salary?userId=${userId}`;
  if (url.startsWith("/worker/press-salary")) return `/boss/order/press-salary?userId=${userId}`;

  return url;
}

function toQuery(params = {}) {
  return Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== null && value !== "")
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join("&");
}
