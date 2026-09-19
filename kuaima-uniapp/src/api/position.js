import { request } from "@/api/http";

function query(params) {
  return Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== null && value !== "")
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join("&");
}

const STATUS_TEXT = { on: "在招中", off: "已停用" };
export const POSITION_STATUS_TEXT = STATUS_TEXT;

export function getStatusText(status) {
  return STATUS_TEXT[status] || "未知";
}

export function listPositions(params = {}) {
  return request({ url: `/boss/positions?${query({ page: 0, size: 50, ...params })}` });
}

export function getPosition(id) {
  return request({ url: `/boss/positions/${id}` });
}

export function getPositionByCode(code) {
  return request({ url: `/boss/positions/code/${code}` });
}

export function createPosition(data = {}) {
  return request({ url: "/boss/positions", method: "POST", data });
}

export function updatePosition(id, data = {}) {
  return request({ url: `/boss/positions/${id}`, method: "PUT", data });
}

export function deletePosition(id) {
  return request({ url: `/boss/positions/${id}`, method: "DELETE" });
}

export function togglePositionStatus(id) {
  return request({ url: `/boss/positions/${id}/toggle-status`, method: "POST" });
}

export function getPositionStats() {
  return request({ url: "/boss/positions/stats" });
}

export function getHotRankings(limit = 10) {
  return request({ url: `/boss/positions/hot-rankings?${query({ limit })}` });
}
