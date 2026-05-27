// Mock 首页概览数据
import { mockEmotions } from './emotions.js'
import { mockAlerts } from './alerts.js'

export const getDashboardData = () => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  const todayEmotions = mockEmotions.filter(e => new Date(e.createdAt) >= today)
  const todayAlerts   = mockAlerts.filter(a => new Date(a.createdAt) >= today)
  const avgScore = todayEmotions.length
    ? Math.round(todayEmotions.reduce((s, e) => s + e.healthScore, 0) / todayEmotions.length)
    : 72

  // 近7天趋势
  const trend7Days = []
  for (let i = 6; i >= 0; i--) {
    const date = new Date(Date.now() - i * 86400000)
    const dateStr = date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
    const dayRecords = mockEmotions.filter(e => {
      const d = new Date(e.createdAt)
      return d.toDateString() === date.toDateString()
    })
    trend7Days.push({
      date: dateStr,
      avgHealthScore: dayRecords.length
        ? Math.round(dayRecords.reduce((s, r) => s + r.healthScore, 0) / dayRecords.length)
        : Math.floor(Math.random() * 30) + 60,
      count: dayRecords.length || Math.floor(Math.random() * 5) + 1,
    })
  }

  // 情感占比（近7天）
  const cutoff7 = new Date(Date.now() - 7 * 86400000)
  const week7 = mockEmotions.filter(e => new Date(e.createdAt) >= cutoff7)
  const distribution = { happy: 0, calm: 0, sad: 0, anxious: 0, angry: 0 }
  week7.forEach(e => { distribution[e.emotionLabel]++ })

  return {
    todayMonitorCount: new Set(todayEmotions.map(e => e.elderId)).size || 4,
    todayAlertCount: todayAlerts.length || 2,
    avgHealthScore: avgScore,
    systemStatus: 'normal',
    trend7Days,
    distribution,
    recentAlerts: mockAlerts.slice(0, 5),
    recentRecords: mockEmotions.slice(0, 5),
  }
}
