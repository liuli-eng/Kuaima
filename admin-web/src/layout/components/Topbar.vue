<template>
  <div class="admin-topbar">
    <div class="topbar-left">
      <!-- 折叠按钮 -->
      <div class="collapse-btn" @click="appStore.toggleSidebar()">
        <i class="fas" :class="appStore.sidebarCollapsed ? 'fa-indent' : 'fa-outdent'"></i>
      </div>
      <!-- 面包屑 -->
      <div class="breadcrumb">
        <span>首页</span>
        <template v-if="matchedBreadcrumbs.length">
          <span class="breadcrumb-separator">
            <i class="fas fa-chevron-right"></i>
          </span>
          <template v-for="(item, index) in matchedBreadcrumbs" :key="index">
            <span v-if="index < matchedBreadcrumbs.length - 1">
              <router-link :to="item.path" style="color: inherit; text-decoration: none;">
                {{ item.meta?.title }}
              </router-link>
            </span>
            <span v-else class="breadcrumb-current">{{ item.meta?.title }}</span>
          </template>
        </template>
      </div>
    </div>
    
    <div class="topbar-right">
      <!-- 搜索 -->
      <div class="search-box">
        <i class="fas fa-search"></i>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索功能..."
          size="default"
          class="search-input"
          clearable
        />
      </div>
      
      <!-- 通知 -->
      <el-popover
        placement="bottom"
        width="320"
        trigger="click"
        popper-class="notification-popover"
      >
        <template #reference>
          <div class="icon-btn" title="通知">
            <i class="fas fa-bell"></i>
            <span class="badge"></span>
          </div>
        </template>
        <div class="notification-panel">
          <div class="notification-header">
            <span>最新通知</span>
            <el-link type="primary" :underline="false">全部已读</el-link>
          </div>
          <div class="notification-list">
            <div class="notification-item">
              <div class="notification-icon" style="background: #EFF6FF; color: #2563EB;">
                <i class="fas fa-id-card"></i>
              </div>
              <div class="notification-content">
                <div class="notification-title">新的认证审核</div>
                <div class="notification-desc">有 8 条待审核的认证申请</div>
                <div class="notification-time">2分钟前</div>
              </div>
            </div>
            <div class="notification-item">
              <div class="notification-icon" style="background: #FEF2F2; color: #EF4444;">
                <i class="fas fa-ban"></i>
              </div>
              <div class="notification-content">
                <div class="notification-title">新的举报处理</div>
                <div class="notification-desc">有 3 条待处理的举报记录</div>
                <div class="notification-time">15分钟前</div>
              </div>
            </div>
            <div class="notification-item">
              <div class="notification-icon" style="background: #FFFBEB; color: #F59E0B;">
                <i class="fas fa-check-circle"></i>
              </div>
              <div class="notification-content">
                <div class="notification-title">招工审核</div>
                <div class="notification-desc">有 5 个招工信息待审核</div>
                <div class="notification-time">1小时前</div>
              </div>
            </div>
          </div>
          <div class="notification-footer">
            <router-link to="/admin/messages" style="color: var(--primary); text-decoration: none; font-size: 13px;">
              查看全部消息 <i class="fas fa-chevron-right" style="font-size: 10px;"></i>
            </router-link>
          </div>
        </div>
      </el-popover>
      
      <!-- 帮助 -->
      <div class="icon-btn" title="帮助">
        <i class="fas fa-question-circle"></i>
      </div>
      
      <!-- 用户信息 -->
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-info">
          <div class="user-avatar">{{ userStore.userInfo.avatar }}</div>
          <div>
            <div class="user-name">{{ userStore.userInfo.name }}</div>
            <div class="user-role">{{ userStore.userInfo.role }}</div>
          </div>
          <i class="fas fa-chevron-down" style="font-size:10px;color:var(--text-muted);"></i>
        </div>
        <template #dropdown>
          <el-dropdown-menu class="user-dropdown-menu">
            <div class="dropdown-header">
              <div class="dropdown-avatar">{{ userStore.userInfo.avatar }}</div>
              <div>
                <div class="dropdown-name">{{ userStore.userInfo.name }}</div>
                <div class="dropdown-role">{{ userStore.userInfo.role }}</div>
              </div>
            </div>
            <el-dropdown-item command="profile">
              <i class="fas fa-user"></i> 个人资料
            </el-dropdown-item>
            <el-dropdown-item command="password">
              <i class="fas fa-key"></i> 修改密码
            </el-dropdown-item>
            <el-dropdown-item command="settings">
              <i class="fas fa-cog"></i> 账户设置
            </el-dropdown-item>
            <el-dropdown-item divided command="logout" class="logout-item">
              <i class="fas fa-sign-out-alt"></i> 退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    
    <!-- 退出确认弹窗 -->
    <el-dialog
      v-model="logoutVisible"
      title="确认退出登录"
      width="360px"
      :show-close="false"
      center
      class="logout-dialog"
    >
      <div style="text-align: center; padding: 16px 0;">
        <div class="logout-icon">
          <i class="fas fa-exclamation-triangle"></i>
        </div>
        <div class="logout-title">确认退出登录？</div>
        <div class="logout-desc">退出后需要重新登录才能访问管理后台</div>
      </div>
      <template #footer>
        <el-button @click="logoutVisible = false">取消</el-button>
        <el-button type="danger" @click="doLogout">确认退出</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'

const appStore = useAppStore()
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const searchKeyword = ref('')
const logoutVisible = ref(false)

// 面包屑
const matchedBreadcrumbs = computed(() => {
  return route.matched.filter(item => item.meta && item.meta.title && !item.meta.public)
})

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/admin/profile')
      break
    case 'password':
      router.push('/admin/password')
      break
    case 'settings':
      router.push('/admin/account')
      break
    case 'logout':
      logoutVisible.value = true
      break
  }
}

const doLogout = () => {
  userStore.logout()
  logoutVisible.value = false
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.admin-topbar {
  background: #fff;
  padding: 14px 24px;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 50;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.2s;
  
  &:hover {
    background: var(--bg-page);
    color: var(--primary);
  }
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 14px;
  
  .breadcrumb-separator {
    font-size: 10px;
    color: var(--text-muted);
  }
  
  .breadcrumb-current {
    color: var(--text-primary);
    font-weight: 500;
  }
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-box {
  position: relative;
  
  i {
    position: absolute;
    left: 12px;
    top: 50%;
    transform: translateY(-50%);
    color: var(--text-muted);
    font-size: 13px;
    z-index: 1;
  }
  
  :deep(.search-input .el-input__wrapper) {
    padding-left: 34px;
    width: 240px;
  }
}

.icon-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.2s;
  position: relative;
  
  &:hover {
    background: var(--bg-page);
    color: var(--text-primary);
  }
  
  .badge {
    position: absolute;
    top: 6px;
    right: 6px;
    width: 8px;
    height: 8px;
    background: var(--danger);
    border-radius: 50%;
    border: 2px solid #fff;
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 8px 4px 16px;
  border-left: 1px solid var(--border);
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.2s;
  
  &:hover {
    background: var(--bg-page);
  }
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF8C42 0%, #FF6B35 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  font-size: 14px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
}

.user-role {
  font-size: 12px;
  color: var(--text-muted);
}

.notification-panel {
  .notification-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--border);
    margin-bottom: 8px;
    font-weight: 600;
    font-size: 14px;
  }
  
  .notification-list {
    max-height: 280px;
    overflow-y: auto;
  }
  
  .notification-item {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 12px 0;
    border-bottom: 1px solid var(--border);
    
    &:last-child {
      border-bottom: none;
    }
  }
  
  .notification-icon {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    flex-shrink: 0;
  }
  
  .notification-content {
    flex: 1;
    min-width: 0;
    
    .notification-title {
      font-size: 13px;
      font-weight: 500;
      margin-bottom: 2px;
    }
    
    .notification-desc {
      font-size: 12px;
      color: var(--text-secondary);
      margin-bottom: 4px;
    }
    
    .notification-time {
      font-size: 11px;
      color: var(--text-muted);
    }
  }
  
  .notification-footer {
    padding-top: 12px;
    text-align: center;
    border-top: 1px solid var(--border);
  }
}

:deep(.user-dropdown-menu) {
  min-width: 220px;
  padding: 8px;
  
  .dropdown-header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    border-bottom: 1px solid var(--el-border-color-lighter);
    margin-bottom: 4px;
  }
  
  .dropdown-avatar {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    background: linear-gradient(135deg, #FF8C42 0%, #FF6B35 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-weight: 600;
    font-size: 16px;
  }
  
  .dropdown-name {
    font-size: 14px;
    font-weight: 600;
  }
  
  .dropdown-role {
    font-size: 12px;
    color: var(--text-muted);
  }
  
  .el-dropdown-menu__item {
    padding: 10px 14px;
    border-radius: 6px;
    margin-bottom: 2px;
    
    i {
      width: 18px;
      margin-right: 8px;
      color: var(--text-muted);
      font-size: 14px;
    }
    
    &:hover {
      i {
        color: var(--primary);
      }
    }
    
    &.logout-item {
      color: var(--danger);
      margin-top: 4px;
      
      i {
        color: var(--danger);
      }
      
      &:hover {
        background: #FEF2F2;
      }
    }
  }
}

.logout-dialog {
  :deep(.el-dialog__body) {
    padding-top: 20px;
  }
}

.logout-icon {
  width: 56px;
  height: 56px;
  background: #FEF2F2;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  font-size: 24px;
  color: var(--danger);
}

.logout-title {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 8px;
}

.logout-desc {
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
