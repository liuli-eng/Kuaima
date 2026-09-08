<template>
  <div class="admin-layout">
    <Sidebar />
    <div class="admin-main" :class="{ collapsed: appStore.sidebarCollapsed }">
      <Topbar />
      <div class="admin-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
    <!-- 当天首次登录未读公告浮层 -->
    <UnreadNoticeModal />
  </div>
</template>

<script setup>
import Sidebar from './components/Sidebar.vue'
import Topbar from './components/Topbar.vue'
import UnreadNoticeModal from './components/UnreadNoticeModal.vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
