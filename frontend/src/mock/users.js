// Mock 用户数据
export const mockUsers = [
  {
    id: 1,
    username: 'admin',
    password: '123456',
    role: 'admin',
    name: '系统管理员',
    phone: '13800000001',
    email: 'admin@eldermood.com',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin',
    isActive: true,
    emailNotify: true,
    smsNotify: false,
    siteNotify: true,
    lastLoginAt: new Date(Date.now() - 3600000).toISOString(),
    createdAt: '2024-01-01T00:00:00.000Z',
  },
  {
    id: 2,
    username: 'caregiver',
    password: '123456',
    role: 'caregiver',
    name: '李护理员',
    phone: '13800000002',
    email: 'caregiver@eldermood.com',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=caregiver',
    isActive: true,
    emailNotify: false,
    smsNotify: true,
    siteNotify: true,
    lastLoginAt: new Date(Date.now() - 7200000).toISOString(),
    createdAt: '2024-01-05T00:00:00.000Z',
  },
  {
    id: 3,
    username: 'family',
    password: '123456',
    role: 'family',
    name: '王家属',
    phone: '13800000003',
    email: 'family@eldermood.com',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=family',
    isActive: true,
    emailNotify: true,
    smsNotify: true,
    siteNotify: true,
    emailSmtpKey: '',
    lastLoginAt: new Date(Date.now() - 1800000).toISOString(),
    createdAt: '2024-01-10T00:00:00.000Z',
  },
]

export const roleLabels = {
  admin: '管理员',
  caregiver: '护理员',
  family: '家属',
}

export const roleColors = {
  admin: 'danger',
  caregiver: 'warning',
  family: 'success',
}
