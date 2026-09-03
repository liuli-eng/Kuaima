import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { title: '管理员登录', public: true }
  },
  {
    path: '/',
    component: () => import('@/layout/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      // 数据统计
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { title: '数据概览', icon: 'fa-chart-line' }
      },
      // 用户管理
      {
        path: 'workers',
        name: 'Workers',
        component: () => import('@/views/user/Workers.vue'),
        meta: { title: '零工管理', icon: 'fa-user' }
      },
      {
        path: 'bosses',
        name: 'Bosses',
        component: () => import('@/views/user/Bosses.vue'),
        meta: { title: '老板管理', icon: 'fa-building' }
      },
      // 招工管理
      {
        path: 'jobs',
        name: 'Jobs',
        component: () => import('@/views/job/Jobs.vue'),
        meta: { title: '招工管理', icon: 'fa-briefcase' }
      },
      {
        path: 'jobs/edit/:id?',
        name: 'JobEdit',
        component: () => import('@/views/job/JobEdit.vue'),
        meta: { title: '编辑招工', icon: 'fa-edit', hidden: true }
      },
      {
        path: 'jobs/applicants/:id',
        name: 'JobApplicants',
        component: () => import('@/views/job/JobApplicants.vue'),
        meta: { title: '报名人员', icon: 'fa-users', hidden: true }
      },
      {
        path: 'job-audit',
        name: 'JobAudit',
        component: () => import('@/views/job/JobAudit.vue'),
        meta: { title: '招工审核', icon: 'fa-check-circle' }
      },
      // 订单结算
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/order/Orders.vue'),
        meta: { title: '用工订单', icon: 'fa-clipboard-list' }
      },
      {
        path: 'settlement',
        name: 'Settlement',
        component: () => import('@/views/order/Settlement.vue'),
        meta: { title: '结算管理', icon: 'fa-coins' }
      },
      // 财务运营
      {
        path: 'finance',
        name: 'Finance',
        component: () => import('@/views/finance/Finance.vue'),
        meta: { title: '财务报表', icon: 'fa-chart-pie' }
      },
      {
        path: 'points',
        name: 'Points',
        component: () => import('@/views/finance/Points.vue'),
        meta: { title: '积分管理', icon: 'fa-star' }
      },
      // 内容管理
      {
        path: 'certification',
        name: 'Certification',
        component: () => import('@/views/content/Certification.vue'),
        meta: { title: '认证审核', icon: 'fa-id-card' }
      },
      {
        path: 'certification/detail/:id',
        name: 'CertificationDetail',
        component: () => import('@/views/content/CertificationDetail.vue'),
        meta: { title: '认证详情', icon: 'fa-eye', hidden: true }
      },
      {
        path: 'banners',
        name: 'Banners',
        component: () => import('@/views/content/Banners.vue'),
        meta: { title: 'Banner管理', icon: 'fa-image' }
      },
      {
        path: 'notices',
        name: 'Notices',
        component: () => import('@/views/content/Notices.vue'),
        meta: { title: '公告管理', icon: 'fa-bullhorn' }
      },
      {
        path: 'rules',
        name: 'Rules',
        component: () => import('@/views/content/Rules.vue'),
        meta: { title: '规则管理', icon: 'fa-book' }
      },
      {
        path: 'rules/edit/:id?',
        name: 'RulesEdit',
        component: () => import('@/views/content/RulesEdit.vue'),
        meta: { title: '新增规则', icon: 'fa-edit', hidden: true }
      },
      // 消息客服
      {
        path: 'messages',
        name: 'Messages',
        component: () => import('@/views/message/Messages.vue'),
        meta: { title: '消息管理', icon: 'fa-envelope' }
      },
      {
        path: 'service',
        name: 'Service',
        component: () => import('@/views/message/Service.vue'),
        meta: { title: '客服管理', icon: 'fa-headset' }
      },
      {
        path: 'service/chat/:id',
        name: 'ServiceChat',
        component: () => import('@/views/message/ServiceChat.vue'),
        meta: { title: '会话处理', icon: 'fa-comments', hidden: true }
      },
      // 风控管理
      {
        path: 'reports',
        name: 'Reports',
        component: () => import('@/views/risk/Reports.vue'),
        meta: { title: '举报处理', icon: 'fa-ban' }
      },
      {
        path: 'blacklist',
        name: 'Blacklist',
        component: () => import('@/views/risk/Blacklist.vue'),
        meta: { title: '黑名单管理', icon: 'fa-user-times' }
      },
      // 系统管理
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/system/Settings.vue'),
        meta: { title: '系统设置', icon: 'fa-cog' }
      },
      {
        path: 'admin-user',
        name: 'AdminUser',
        component: () => import('@/views/system/AdminUser.vue'),
        meta: { title: '管理员管理', icon: 'fa-user-cog' }
      },
      {
        path: 'logs',
        name: 'Logs',
        component: () => import('@/views/system/Logs.vue'),
        meta: { title: '操作日志', icon: 'fa-file-alt' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/system/Profile.vue'),
        meta: { title: '个人资料', icon: 'fa-user-circle', hidden: true }
      },
      {
        path: 'password',
        name: 'Password',
        component: () => import('@/views/system/Password.vue'),
        meta: { title: '修改密码', icon: 'fa-key', hidden: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token') || sessionStorage.getItem('admin_token')
  
  if (to.meta.public) {
    next()
  } else if (!token && to.path !== '/login') {
    next('/login')
  } else {
    next()
  }
})

export default router
