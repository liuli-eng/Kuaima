<template>
  <div class="admin-sidebar" :class="{ collapsed: appStore.sidebarCollapsed }">
    <!-- Logo -->
    <div class="sidebar-header">
      <div class="sidebar-logo">
        <i class="fas fa-bolt"></i>
      </div>
      <div v-show="!appStore.sidebarCollapsed">
        <div class="sidebar-title">快马日结</div>
        <div class="sidebar-version">管理后台 v2.0</div>
      </div>
    </div>
    
    <!-- 菜单 -->
    <nav class="sidebar-menu">
      <template v-for="group in menuGroups" :key="group.id">
        <div class="menu-group-title" v-show="!appStore.sidebarCollapsed">{{ group.name }}</div>
        <router-link
          v-for="item in group.items"
          :key="item.path"
          :to="item.path"
          class="menu-item"
          active-class="active"
        >
          <i :class="['fas', item.icon]"></i>
          <span v-show="!appStore.sidebarCollapsed">{{ item.name }}</span>
          <el-badge
            v-if="item.badge && !appStore.sidebarCollapsed"
            :value="item.badge"
            :max="99"
            class="menu-badge"
          />
        </router-link>
      </template>
    </nav>
  </div>
</template>

<script setup>
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

const menuGroups = [
  {
    id: 'dashboard',
    name: '数据统计',
    items: [
      { path: '/dashboard', name: '数据概览', icon: 'fa-chart-line' }
    ]
  },
  {
    id: 'user',
    name: '用户管理',
    items: [
      { path: '/workers', name: '零工管理', icon: 'fa-user' },
      { path: '/bosses', name: '老板管理', icon: 'fa-building' }
    ]
  },
  {
    id: 'job',
    name: '招工管理',
    items: [
      { path: '/jobs', name: '招工管理', icon: 'fa-briefcase' },
      { path: '/job-audit', name: '招工审核', icon: 'fa-check-circle', badge: 5 }
    ]
  },
  {
    id: 'order',
    name: '订单结算',
    items: [
      { path: '/orders', name: '用工订单', icon: 'fa-clipboard-list' },
      { path: '/settlement', name: '结算管理', icon: 'fa-coins' }
    ]
  },
  {
    id: 'finance',
    name: '财务运营',
    items: [
      { path: '/finance', name: '财务报表', icon: 'fa-chart-pie' },
      { path: '/points', name: '积分管理', icon: 'fa-star' }
    ]
  },
  {
    id: 'content',
    name: '内容管理',
    items: [
      { path: '/certification', name: '认证审核', icon: 'fa-id-card', badge: 8 },
      { path: '/notices', name: '公告管理', icon: 'fa-bullhorn' },
      { path: '/rules', name: '规则管理', icon: 'fa-book' },
      { path: '/banners', name: 'Banner管理', icon: 'fa-image' }
    ]
  },
  {
    id: 'message',
    name: '消息客服',
    items: [
      { path: '/messages', name: '消息管理', icon: 'fa-envelope' },
      { path: '/service', name: '客服管理', icon: 'fa-headset' }
    ]
  },
  {
    id: 'risk',
    name: '风控管理',
    items: [
      { path: '/reports', name: '举报处理', icon: 'fa-ban' },
      { path: '/blacklist', name: '黑名单管理', icon: 'fa-user-times' }
    ]
  },
  {
    id: 'system',
    name: '系统管理',
    items: [
      { path: '/settings', name: '系统设置', icon: 'fa-cog' },
      { path: '/logs', name: '操作日志', icon: 'fa-file-alt' }
    ]
  }
]
</script>

<style scoped>
.admin-sidebar {
  width: 220px;
  background: var(--sidebar-bg);
  color: #fff;
  flex-shrink: 0;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  overflow-y: auto;
  z-index: 100;
  transition: width 0.3s;
  
  &.collapsed {
    width: 64px;
    
    .menu-item span,
    .menu-group-title {
      display: none;
    }
  }
  
  &::-webkit-scrollbar {
    width: 4px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(255,255,255,0.1);
    border-radius: 2px;
  }
}

.sidebar-header {
  padding: 20px 20px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  display: flex;
  align-items: center;
  gap: 10px;
}

.sidebar-logo {
  width: 36px;
  height: 36px;
  background: var(--primary);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  flex-shrink: 0;
}

.sidebar-title {
  font-size: 16px;
  font-weight: 600;
}

.sidebar-version {
  font-size: 11px;
  color: rgba(255,255,255,0.5);
}

.sidebar-menu {
  padding: 12px 0;
}

.menu-group-title {
  padding: 12px 20px 8px;
  font-size: 12px;
  color: rgba(255,255,255,0.4);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  color: rgba(255,255,255,0.7);
  cursor: pointer;
  transition: all 0.2s;
  border-left: 3px solid transparent;
  text-decoration: none;
  font-size: 14px;
  
  &:hover {
    background: var(--sidebar-hover);
    color: #fff;
  }
  
  &.active {
    background: rgba(255,107,53,0.1);
    color: var(--primary);
    border-left-color: var(--primary);
  }
  
  i {
    width: 20px;
    text-align: center;
    font-size: 14px;
  }
}

.menu-badge {
  margin-left: auto;
}
</style>
