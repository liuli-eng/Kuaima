export function hasEnterprisePermission(code) {
  const role = String(uni.getStorageSync("role") || "").toUpperCase();
  if (role !== "BOSS") return true;
  const enterpriseId = uni.getStorageSync("enterpriseId");
  const memberRole = String(uni.getStorageSync("enterpriseMemberRole") || "").toUpperCase();
  if (memberRole === "OWNER") return true;
  const permissions = uni.getStorageSync("enterprisePermissions");
  if (!enterpriseId || !Array.isArray(permissions)) return true;
  return permissions.includes(code);
}

export function currentEnterprisePermissions() {
  const permissions = uni.getStorageSync("enterprisePermissions");
  return Array.isArray(permissions) ? permissions : [];
}
