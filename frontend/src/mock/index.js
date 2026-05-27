import Mock from 'mockjs'
import { mockUsers } from './users.js'
import { mockElders, getAuthorizedElders } from './elders.js'
import { mockEmotions, getEmotionStats } from './emotions.js'
import { mockAlerts, getPendingCount } from './alerts.js'
import { getDashboardData } from './dashboard.js'

// 全局延迟 50~200ms 模拟网络请求
Mock.setup({ timeout: '50-200' })

// ===== 认证接口 =====
Mock.mock('/api/auth/login/email', 'post', (options) => {
  const { email, password } = JSON.parse(options.body)
  const user = mockUsers.find(u => u.email === email && u.password === password)
  if (!user) return { code: 401, message: '邮箱或密码错误' }
  if (!user.isActive) return { code: 403, message: '账号已被禁用' }
  const { password: _, ...safeUser } = user
  return { code: 200, data: { accessToken: `mock_token_${user.id}`, user: safeUser } }
})

Mock.mock('/api/auth/login/phone', 'post', (options) => {
  const { phone, code } = JSON.parse(options.body)
  if (code !== '123456') return { code: 401, message: '验证码错误' }
  const user = mockUsers.find(u => u.phone === phone)
  if (!user) return { code: 401, message: '手机号未注册' }
  if (!user.isActive) return { code: 403, message: '账号已被禁用' }
  const { password: _, ...safeUser } = user
  return { code: 200, data: { accessToken: `mock_token_${user.id}`, user: safeUser } }
})

Mock.mock('/api/auth/send-code', 'post', () => ({
  code: 200, message: '验证码已发送（Mock：请使用 123456）'
}))

Mock.mock('/api/auth/logout', 'post', () => ({ code: 200, message: '退出成功' }))

// ===== 首页概览 =====
Mock.mock('/api/emotion/dashboard', 'get', () => ({
  code: 200, data: getDashboardData()
}))

// ===== 老人管理 =====
Mock.mock(/\/api\/elders(\?.*)?$/, 'get', (options) => {
  const url = options.url
  const page = parseInt(url.match(/page=(\d+)/)?.[1] || '1')
  const pageSize = parseInt(url.match(/pageSize=(\d+)/)?.[1] || '10')
  const keyword = decodeURIComponent(url.match(/keyword=([^&]*)/)?.[1] || '')
  let list = keyword
    ? mockElders.filter(e => e.name.includes(keyword))
    : [...mockElders]
  const total = list.length
  list = list.slice((page - 1) * pageSize, page * pageSize)
  return { code: 200, data: { total, list } }
})

Mock.mock('/api/elders', 'post', (options) => {
  const body = JSON.parse(options.body)
  const newElder = {
    ...body,
    id: mockElders.length + 1,
    privacyAuthorized: false,
    authorizedAt: null,
    authorizedBy: null,
    guardians: [],
    alertThreshold: null,
    createdBy: 1,
    createdAt: new Date().toISOString(),
  }
  mockElders.push(newElder)
  return { code: 201, data: newElder }
})

Mock.mock(/\/api\/elders\/\d+$/, 'put', (options) => {
  const id = parseInt(options.url.match(/\/api\/elders\/(\d+)/)[1])
  const body = JSON.parse(options.body)
  const idx = mockElders.findIndex(e => e.id === id)
  if (idx === -1) return { code: 404, message: '老人不存在' }
  mockElders[idx] = { ...mockElders[idx], ...body }
  return { code: 200, data: mockElders[idx] }
})

Mock.mock(/\/api\/elders\/\d+\/authorization$/, 'patch', (options) => {
  const id = parseInt(options.url.match(/\/api\/elders\/(\d+)/)[1])
  const { authorized } = JSON.parse(options.body)
  const elder = mockElders.find(e => e.id === id)
  if (!elder) return { code: 404, message: '老人不存在' }
  elder.privacyAuthorized = authorized
  elder.authorizedAt = authorized ? new Date().toISOString() : null
  return { code: 200, message: authorized ? '授权成功' : '已关闭授权' }
})

// ===== 情感分析 =====
Mock.mock('/api/emotion/records', 'get', (options) => {
  const url = options.url
  const elderId = parseInt(url.match(/elderId=(\d+)/)?.[1] || '0')
  const page = parseInt(url.match(/page=(\d+)/)?.[1] || '1')
  const pageSize = parseInt(url.match(/pageSize=(\d+)/)?.[1] || '20')
  let list = elderId ? mockEmotions.filter(e => e.elderId === elderId) : [...mockEmotions]
  const total = list.length
  list = list.slice((page - 1) * pageSize, page * pageSize)
  return { code: 200, data: { total, list } }
})

Mock.mock('/api/emotion/statistics', 'get', (options) => {
  const url = options.url
  const elderId = parseInt(url.match(/elderId=(\d+)/)?.[1] || '1')
  const range = url.match(/range=(\w+)/)?.[1] || 'week'
  return { code: 200, data: getEmotionStats(elderId, range) }
})

// ===== 预警 =====
Mock.mock(/\/api\/alerts(\?.*)?$/, 'get', (options) => {
  const url = options.url
  const status = url.match(/status=(\w+)/)?.[1] || 'all'
  const page = parseInt(url.match(/page=(\d+)/)?.[1] || '1')
  const pageSize = parseInt(url.match(/pageSize=(\d+)/)?.[1] || '20')
  let list = status === 'all' ? [...mockAlerts] : mockAlerts.filter(a => a.status === status)
  const total = list.length
  list = list.slice((page - 1) * pageSize, page * pageSize)
  return { code: 200, data: { total, list, pendingCount: getPendingCount() } }
})

Mock.mock(/\/api\/alerts\/\d+\/handle$/, 'patch', (options) => {
  const id = parseInt(options.url.match(/\/api\/alerts\/(\d+)/)[1])
  const alert = mockAlerts.find(a => a.id === id)
  if (!alert) return { code: 404, message: '预警记录不存在' }
  if (alert.status === 'handled') return { code: 409, message: '已处理，无需重复操作' }
  alert.status = 'handled'
  alert.handledAt = new Date().toISOString()
  alert.handledBy = '当前用户'
  return { code: 200, message: '标记成功' }
})

Mock.mock('/api/alerts/batch-handle', 'post', (options) => {
  const { ids } = JSON.parse(options.body)
  let handled = 0
  ids.forEach(id => {
    const alert = mockAlerts.find(a => a.id === id)
    if (alert && alert.status === 'pending') {
      alert.status = 'handled'
      alert.handledAt = new Date().toISOString()
      alert.handledBy = '当前用户'
      handled++
    }
  })
  return { code: 200, data: { handled } }
})

// ===== 管理后台 =====
Mock.mock('/api/admin/users', 'get', () => ({
  code: 200, data: { total: mockUsers.length, list: mockUsers.map(({ password: _, ...u }) => u) }
}))

Mock.mock(/\/api\/admin\/users\/\d+\/status$/, 'patch', (options) => {
  const id = parseInt(options.url.match(/\/api\/admin\/users\/(\d+)/)[1])
  const { isActive } = JSON.parse(options.body)
  const user = mockUsers.find(u => u.id === id)
  if (!user) return { code: 404, message: '用户不存在' }
  user.isActive = isActive
  return { code: 200, message: isActive ? '账号已启用' : '账号已禁用' }
})

Mock.mock('/api/admin/ai-status', 'get', () => ({
  code: 200, data: {
    status: Math.random() > 0.2 ? 'normal' : 'degraded',
    latencyMs: Math.floor(Math.random() * 800) + 400,
    accuracyRate: 0.85,
    lastCheckedAt: new Date().toISOString(),
    recentErrors: Math.random() > 0.7 ? [
      { time: new Date(Date.now() - 3600000).toISOString(), message: 'API 响应超时（5000ms）' },
    ] : [],
  }
}))

console.log('[Mock] 数据层已启动，所有 API 请求将被拦截')
