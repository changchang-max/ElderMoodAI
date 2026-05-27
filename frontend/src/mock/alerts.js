// Mock 预警记录数据（15条）
import { emotionLabelMap } from './emotions.js'

export let mockAlerts = [
  { id: 1,  elderId: 1, elderName: '张奶奶', emotionLabel: 'anxious', alertLevel: 'high',   status: 'pending', handledBy: null, handledAt: null, notifySent: true,  createdAt: new Date(Date.now() - 1 * 3600000).toISOString() },
  { id: 2,  elderId: 2, elderName: '李爷爷', emotionLabel: 'angry',   alertLevel: 'high',   status: 'pending', handledBy: null, handledAt: null, notifySent: true,  createdAt: new Date(Date.now() - 2 * 3600000).toISOString() },
  { id: 3,  elderId: 3, elderName: '王奶奶', emotionLabel: 'sad',     alertLevel: 'medium', status: 'pending', handledBy: null, handledAt: null, notifySent: true,  createdAt: new Date(Date.now() - 4 * 3600000).toISOString() },
  { id: 4,  elderId: 4, elderName: '陈爷爷', emotionLabel: 'anxious', alertLevel: 'medium', status: 'pending', handledBy: null, handledAt: null, notifySent: false, createdAt: new Date(Date.now() - 6 * 3600000).toISOString() },
  { id: 5,  elderId: 5, elderName: '刘奶奶', emotionLabel: 'sad',     alertLevel: 'low',    status: 'pending', handledBy: null, handledAt: null, notifySent: false, createdAt: new Date(Date.now() - 8 * 3600000).toISOString() },
  { id: 6,  elderId: 1, elderName: '张奶奶', emotionLabel: 'angry',   alertLevel: 'high',   status: 'pending', handledBy: null, handledAt: null, notifySent: true,  createdAt: new Date(Date.now() - 12 * 3600000).toISOString() },
  { id: 7,  elderId: 6, elderName: '赵爷爷', emotionLabel: 'anxious', alertLevel: 'medium', status: 'pending', handledBy: null, handledAt: null, notifySent: true,  createdAt: new Date(Date.now() - 18 * 3600000).toISOString() },
  { id: 8,  elderId: 2, elderName: '李爷爷', emotionLabel: 'sad',     alertLevel: 'low',    status: 'pending', handledBy: null, handledAt: null, notifySent: false, createdAt: new Date(Date.now() - 24 * 3600000).toISOString() },
  { id: 9,  elderId: 3, elderName: '王奶奶', emotionLabel: 'anxious', alertLevel: 'high',   status: 'pending', handledBy: null, handledAt: null, notifySent: true,  createdAt: new Date(Date.now() - 30 * 3600000).toISOString() },
  { id: 10, elderId: 4, elderName: '陈爷爷', emotionLabel: 'angry',   alertLevel: 'medium', status: 'pending', handledBy: null, handledAt: null, notifySent: false, createdAt: new Date(Date.now() - 36 * 3600000).toISOString() },
  { id: 11, elderId: 1, elderName: '张奶奶', emotionLabel: 'anxious', alertLevel: 'high',   status: 'handled', handledBy: '李护理员', handledAt: new Date(Date.now() - 40 * 3600000).toISOString(), notifySent: true, createdAt: new Date(Date.now() - 48 * 3600000).toISOString() },
  { id: 12, elderId: 5, elderName: '刘奶奶', emotionLabel: 'sad',     alertLevel: 'medium', status: 'handled', handledBy: '王家属',   handledAt: new Date(Date.now() - 50 * 3600000).toISOString(), notifySent: true, createdAt: new Date(Date.now() - 56 * 3600000).toISOString() },
  { id: 13, elderId: 2, elderName: '李爷爷', emotionLabel: 'angry',   alertLevel: 'high',   status: 'handled', handledBy: '李护理员', handledAt: new Date(Date.now() - 60 * 3600000).toISOString(), notifySent: true, createdAt: new Date(Date.now() - 72 * 3600000).toISOString() },
  { id: 14, elderId: 6, elderName: '赵爷爷', emotionLabel: 'anxious', alertLevel: 'low',    status: 'handled', handledBy: '系统管理员', handledAt: new Date(Date.now() - 80 * 3600000).toISOString(), notifySent: false, createdAt: new Date(Date.now() - 96 * 3600000).toISOString() },
  { id: 15, elderId: 3, elderName: '王奶奶', emotionLabel: 'sad',     alertLevel: 'medium', status: 'handled', handledBy: '李护理员', handledAt: new Date(Date.now() - 100 * 3600000).toISOString(), notifySent: true, createdAt: new Date(Date.now() - 120 * 3600000).toISOString() },
]

export const alertLevelMap = {
  high:   { label: '高危', color: '#F56C6C', type: 'danger' },
  medium: { label: '中危', color: '#E6A23C', type: 'warning' },
  low:    { label: '低危', color: '#F7C59F', type: '' },
}

export const alertStatusMap = {
  pending: { label: '未处理', type: 'danger' },
  handled: { label: '已处理', type: 'success' },
}

// 获取未处理预警数量
export const getPendingCount = () => mockAlerts.filter(a => a.status === 'pending').length
