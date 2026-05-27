// Mock 情感记录数据（90条，覆盖近30天）
const emotionLabels = ['happy', 'calm', 'sad', 'anxious', 'angry']
// 分布比例：开心20%、平静35%、低落20%、焦虑15%、愤怒10%
const emotionWeights = [20, 35, 20, 15, 10]

const healthScoreMap = {
  happy: () => Math.floor(Math.random() * 11) + 90,   // 90~100
  calm:  () => Math.floor(Math.random() * 20) + 70,   // 70~89
  sad:   () => Math.floor(Math.random() * 30) + 40,   // 40~69
  anxious: () => Math.floor(Math.random() * 20) + 20, // 20~39
  angry: () => Math.floor(Math.random() * 20),         // 0~19
}

function weightedRandom(labels, weights) {
  const total = weights.reduce((a, b) => a + b, 0)
  let r = Math.random() * total
  for (let i = 0; i < labels.length; i++) {
    r -= weights[i]
    if (r <= 0) return labels[i]
  }
  return labels[labels.length - 1]
}

function randomScore() {
  return parseFloat((Math.random() * 0.4 + 0.55).toFixed(4)) // 0.55~0.95
}

function generateRecord(id, elderId, daysAgo) {
  const label = weightedRandom(emotionLabels, emotionWeights)
  const confidence = parseFloat((Math.random() * 0.3 + 0.65).toFixed(4))
  const hasText  = Math.random() > 0.3
  const hasVoice = Math.random() > 0.5
  const hasImage = Math.random() > 0.6
  const date = new Date(Date.now() - daysAgo * 86400000 - Math.random() * 86400000)

  return {
    id,
    elderId,
    elderName: ['张奶奶','李爷爷','王奶奶','陈爷爷','刘奶奶','赵爷爷'][elderId - 1] || '张奶奶',
    submittedBy: Math.random() > 0.5 ? 2 : 3,
    emotionLabel: label,
    confidence,
    healthScore: healthScoreMap[label](),
    textScore:  hasText  ? randomScore() : null,
    voiceScore: hasVoice ? randomScore() : null,
    imageScore: hasImage ? randomScore() : null,
    textContent: hasText ? '今天状态还不错，吃饭也有胃口。' : null,
    voiceFile:  hasVoice ? `voice_${id}.mp3` : null,
    imageFile:  hasImage ? `image_${id}.jpg` : null,
    createdAt: date.toISOString(),
  }
}

// 生成90条记录，分布在6位已授权老人（id 1~6）中
export let mockEmotions = []
let id = 1
for (let day = 0; day < 30; day++) {
  const count = Math.floor(Math.random() * 4) + 1 // 每天1~4条
  for (let i = 0; i < count && id <= 90; i++) {
    const elderId = (id % 6) + 1
    mockEmotions.push(generateRecord(id, elderId, day))
    id++
  }
}

// 按时间倒序
mockEmotions.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))

export const emotionLabelMap = {
  happy:   { label: '开心', color: '#67C23A', icon: '😊' },
  calm:    { label: '平静', color: '#409EFF', icon: '😌' },
  sad:     { label: '低落', color: '#909399', icon: '😔' },
  anxious: { label: '焦虑', color: '#FF6B35', icon: '😰' },
  angry:   { label: '愤怒', color: '#F56C6C', icon: '😠' },
}

// 获取指定老人的情感记录
export const getEmotionsByElder = (elderId, range = 'week') => {
  const days = range === 'day' ? 1 : range === 'week' ? 7 : 30
  const cutoff = new Date(Date.now() - days * 86400000)
  return mockEmotions.filter(e =>
    e.elderId === elderId && new Date(e.createdAt) >= cutoff
  )
}

// 获取统计数据（用于可视化）
export const getEmotionStats = (elderId, range = 'week') => {
  const records = getEmotionsByElder(elderId, range)
  const days = range === 'day' ? 1 : range === 'week' ? 7 : 30

  // 趋势数据
  const trend = []
  for (let i = days - 1; i >= 0; i--) {
    const date = new Date(Date.now() - i * 86400000)
    const dateStr = date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
    const dayRecords = records.filter(r => {
      const d = new Date(r.createdAt)
      return d.toDateString() === date.toDateString()
    })
    trend.push({
      date: dateStr,
      avgHealthScore: dayRecords.length
        ? Math.round(dayRecords.reduce((s, r) => s + r.healthScore, 0) / dayRecords.length)
        : null,
      count: dayRecords.length,
      dominantEmotion: dayRecords.length
        ? dayRecords.sort((a, b) => b.confidence - a.confidence)[0].emotionLabel
        : null,
    })
  }

  // 占比数据
  const distribution = {}
  emotionLabels.forEach(l => { distribution[l] = 0 })
  records.forEach(r => { distribution[r.emotionLabel]++ })

  return { trend, distribution, total: records.length }
}
