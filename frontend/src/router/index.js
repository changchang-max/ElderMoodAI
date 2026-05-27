import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/components/layout/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard',      name: 'Dashboard',      component: () => import('@/views/Dashboard.vue') },
      { path: 'monitor',        name: 'Monitor',        component: () => import('@/views/EmotionMonitor.vue') },
      { path: 'visualization',  name: 'Visualization',  component: () => import('@/views/Visualization.vue') },
      { path: 'alerts',         name: 'Alerts',         component: () => import('@/views/AlertCenter.vue') },
      { path: 'elders',         name: 'Elders',         component: () => import('@/views/ElderManage.vue') },
      { path: 'settings',       name: 'Settings',       component: () => import('@/views/Settings.vue') },
      { path: 'help',           name: 'Help',           component: () => import('@/views/Help.vue') },
      { path: 'admin',          name: 'Admin',          component: () => import('@/views/admin/AdminPanel.vue'), meta: { role: 'admin' } },
    ]
  },
  {
    path: '/403',
    name: '403',
    component: () => import('@/views/403.vue')
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

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('elder_token')
  const user = JSON.parse(localStorage.getItem('elder_user') || 'null')

  if (to.meta.requiresAuth === false) {
    if (token && to.path === '/login') return next('/dashboard')
    return next()
  }

  if (!token) return next('/login')

  if (to.meta.role && user?.role !== to.meta.role) {
    return next('/403')
  }

  next()
})

export default router
