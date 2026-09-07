import { useUserStore } from '@/stores/user'

/**
 * 解析权限对象，统一返回 { module: { key: boolean } } 结构
 * 支持 JSON 字符串或对象
 */
export function parsePermissions(perm) {
  if (!perm) return {}
  if (typeof perm === 'string') {
    try {
      return JSON.parse(perm)
    } catch {
      return {}
    }
  }
  return perm
}

/**
 * 判断当前登录用户是否拥有指定权限 key
 * @param {Object} permissions 权限树对象 { permUser: { u1: true }, ... }
 * @param {String} permKey 权限 key，如 'permUser.u1'
 * @returns {Boolean}
 */
export function hasPerm(permissions, permKey) {
  if (!permKey) return true // 未配置权限 key 视为公开
  // 超级管理员拥有全部权限
  const role = localStorage.getItem('admin_role')
  if (role === 'SUPER_ADMIN') return true
  if (!permissions) return false
  const perms = parsePermissions(permissions)
  const [mod, key] = String(permKey).split('.')
  if (!mod) return false
  const modPerm = perms[mod]
  if (!modPerm || typeof modPerm !== 'object') return false
  return !!modPerm[key]
}

/**
 * v-permission 指令：根据权限 key 控制元素显隐
 * 用法：v-permission="'permUser.u1'" 或 v-permission="['permUser.u1','permUser.u2']" (任一满足)
 */
export const vPermission = {
  mounted(el, binding) {
    const userStore = useUserStore()
    const perms = userStore.userInfo.permissions
    const value = binding.value
    let ok = false
    if (Array.isArray(value)) {
      ok = value.some((k) => hasPerm(perms, k))
    } else {
      ok = hasPerm(perms, value)
    }
    if (!ok) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}

export default { hasPerm, parsePermissions, vPermission }
