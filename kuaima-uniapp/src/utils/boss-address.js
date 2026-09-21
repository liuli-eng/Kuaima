export function readAddressCoordinates(item = {}) {
  const longitudeValue = item.longitude ?? item.lng;
  const latitudeValue = item.latitude ?? item.lat;
  const longitude =
    longitudeValue === null || longitudeValue === undefined || longitudeValue === ""
      ? null
      : Number(longitudeValue);
  const latitude =
    latitudeValue === null || latitudeValue === undefined || latitudeValue === ""
      ? null
      : Number(latitudeValue);
  return {
    longitude: Number.isFinite(longitude) ? longitude : null,
    latitude: Number.isFinite(latitude) ? latitude : null,
  };
}

export function formatBossAddress(item = {}) {
  const structured = [item.city, item.district, item.detail || item.detailAddress]
    .filter(Boolean)
    .join("");
  return String(structured || item.address || "").trim();
}

export function normalizeBossAddressSelection(item = {}) {
  const { longitude, latitude } = readAddressCoordinates(item);
  return {
    addressId: item.addressId ?? item.id ?? null,
    address: formatBossAddress(item),
    longitude,
    latitude,
    name: item.name || item.locationName || "工作地点",
    city: item.city || "",
    district: item.district || "",
    detail: item.detail || item.detailAddress || item.address || "",
    display: String(item.display || [item.name, formatBossAddress(item)].filter(Boolean).join(" ")).trim(),
  };
}

export function validateBossWorkLocation(location = {}, { allowEmpty = false } = {}) {
  const address = String(location.address || location.detail || location.display || "").trim();
  const { longitude, latitude } = readAddressCoordinates(location);
  if (allowEmpty && !address) return { valid: true, address: "", longitude: null, latitude: null };
  if (!address) return { valid: false, message: "请选择工作地址，系统需要地址定位才能发布岗位" };
  if (longitude === null || latitude === null) {
    return {
      valid: false,
      message: allowEmpty
        ? "工作地址必须提供经度和纬度，请重新选择地址"
        : "请选择工作地址，系统需要地址定位才能发布岗位",
    };
  }
  if (longitude < -180 || longitude > 180) {
    return { valid: false, message: "经度必须在 -180 到 180 之间" };
  }
  if (latitude < -90 || latitude > 90) {
    return { valid: false, message: "纬度必须在 -90 到 90 之间" };
  }
  return { valid: true, address, longitude, latitude };
}
