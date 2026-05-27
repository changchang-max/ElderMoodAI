import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(JSON.parse(localStorage.getItem('elder_user') || 'null'))
  const token = ref(localStorage.getItem('elder_token') || '')

  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const role = computed(() => user.value?.role || '')
  const isAdmin = computed(() => role.value === 'admin')
  const isCaregiver = computed(() => role.value === 'caregiver')
  const isFamily = computed(() => role.value === 'family')

  // 角色权限映射
  const rolePermissions = {
    admin:     ['dashboard', 'monitor', 'visualization', 'alerts', 'elders', 'settings', 'admin', 'help'],
    caregiver: ['dashboard', 'monitor', 'visualization', 'alerts', 'elders', 'settings', 'help'],
    family:    ['dashboard', 'monitor', 'visualization', 'alerts', 'settings', 'help'],
  }

  function hasPermission(page) {
    if (!role.value) return false
    return rolePermissions[role.value]?.includes(page) ?? false
  }

  async function login(credentials, type = 'email') {
    const url = type === 'email' ? '/api/auth/login/email' : '/api/auth/login/phone'
    const res = await axios.post(url, credentials)
    if (res.data.code === 200) {
      token.value = res.data.data.accessToken
      user.value = res.data.data.user
      localStorage.setItem('elder_token', token.value)
      localStorage.setItem('elder_user', JSON.stringify(user.value))
      return { success: true }
    }
    return { success: false, message: res.data.message }
  }

  async function logout() {
    try { await axios.post('/api/auth/logout') } catch {}
    token.value = ''
    user.value = null
    localStorage.removeItem('elder_token')
    localStorage.removeItem('elder_user')
  }

  function updateUser(updates) {
    user.value = { ...user.value, ...updates }
    localStorage.setItem('elder_user', JSON.stringify(user.value))
  }

  return { user, token, isLoggedIn, role, isAdmin, isCaregiver, isFamily, hasPermission, login, logout, updateUser }
})
