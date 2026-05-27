import { mockEmotions } from '@/mock/emotions.js'
import { mockAlerts } from '@/mock/alerts.js'

const emotionLabels = ['happy', 'calm', 'sad', 'anxious', 'angry']
// 分布权重：开心15%、平静30%、低落25%、焦虑20%、愤怒10%
const weights = [15, 30, 25, 20, 10]

const healthScoreMap = {
  happy:   () => Math.floor(Math.random() * 11) + 90,
  calm:    () => Math.floor(Math.random() * 20) + 70,
  sad:     () => Math.floor(Math.random() * 30) + 40,
  anxious: () => Math.floor(Math.random() * 20) + 20,
  angry:   () => Math.floor(Math.random() * 20),
}

function weightedRandom() {
  const total = weights.reduce((a, b) => a + b, 0)
  let r = Math.random() * total
  for (let i = 0; i < emotionLabels.length; i++) {
    r -= weights[i]
    if (r <= 0) return emotionLabels[i]
  }
  return 'calm'
}

/**
 * 模拟情感分析
 * @param {Object} params - { elderId, elderName, hasText, hasVoice, hasImage }
 * @returns {Promise<Object>} 分析结果
 */
export function mockAnalyze(params) {
  const delay = Math.floor(Math.random() * 3000) + 2000 // 2~5秒

  return new Promise((resolve) => {
    setTimeout(() => {
      const label = weightedRandom()
      const confidence = parseFloat((Math.random() * 0.25 + 0.70).toFixed(4))
      const healthScore = healthScoreMap[label]()

      const result = {
        id: mockEmotions.length + 1,
        elderId: params.elderId,
        elderName: params.elderName,
        emotionLabel: label,
        confidence,
        healthScore,
        textScore:  params.hasText  ? parseFloat((Math.random() * 0.3 + 0.65).toFixed(4)) : null,
        voiceScore: params.hasVoice ? parseFloat((Math.random() * 0.3 + 0.65).toFixed(4)) : null,
        imageScore: params.hasImage ? parseFloat((Math.random() * 0.3 + 0.65).toFixed(4)) : null,
        createdAt: new Date().toISOString(),
        // 是否触发预警
        shouldAlert: (label === 'anxious' || label === 'angry') && confidence >= 0.70,
        alertLevel: confidence >= 0.85 ? 'high' : confidence >= 0.70 ? 'medium' : 'low',
      }

      // 将结果加入 Mock 数据
      mockEmotions.unshift(result)

      // 如果触发预警，加入预警记录
      if (result.shouldAlert) {
        const newAlert = {
          id: mockAlerts.length + 1,
          elderId: params.elderId,
          elderName: params.elderName,
          emotionLabel: label,
          alertLevel: result.alertLevel,
          status: 'pending',
          handledBy: null,
          handledAt: null,
          notifySent: true,
          createdAt: new Date().toISOString(),
        }
        mockAlerts.unshift(newAlert)
      }

      resolve(result)
    }, delay)
  })
}

/**
 * 格式化进度条显示（0~1 → 0~100）
 */
export function scoreToPercent(score) {
  if (score === null || score === undefined) return 0
  return Math.round(score * 100)
}
