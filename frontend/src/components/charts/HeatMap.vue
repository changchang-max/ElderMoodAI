<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import { mockEmotions } from '@/mock/emotions.js'

// ---------------------------------------------------------------------------
// Props
// ---------------------------------------------------------------------------
const props = defineProps({
  /** 老人 ID，切换时重新计算热力图数据 */
  elderId: {
    type: Number,
    default: 1
  }
})

// ---------------------------------------------------------------------------
// 常量
// ---------------------------------------------------------------------------
/** Y 轴情感类型（从下到上排列，ECharts category 轴从底部开始） */
const EMOTION_LABELS = ['angry', 'anxious', 'sad', 'calm', 'happy']
const EMOTION_DISPLAY = {
  happy:   '开心',
  calm:    '平静',
  sad:     '低落',
  anxious: '焦虑',
  angry:   '愤怒',
}
const HOURS = Array.from({ length: 24 }, (_, i) => `${i}时`)

// ---------------------------------------------------------------------------
// ECharts
// ---------------------------------------------------------------------------
const chartRef = ref(null)
let chart = null

onMounted(() => {
  chart = echarts.init(chartRef.value)
  renderChart()
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  chart?.dispose()
  chart = null
  window.removeEventListener('resize', onResize)
})

watch(() => props.elderId, renderChart)

function onResize() {
  chart?.resize()
}

// ---------------------------------------------------------------------------
// 数据计算
// ---------------------------------------------------------------------------
/**
 * 从 mockEmotions 中统计指定老人每小时各情感出现次数
 * 返回格式：[[hour, emotionIndex, count], ...]  emotionIndex 对应 EMOTION_LABELS 的下标
 */
function buildHeatData(elderId) {
  // 初始化计数表：emotion × hour
  const countMap = {}
  EMOTION_LABELS.forEach((label, idx) => {
    countMap[idx] = new Array(24).fill(0)
  })

  const records = mockEmotions.filter(r => r.elderId === elderId)

  records.forEach(r => {
    const hour = new Date(r.createdAt).getHours()
    const emotionIdx = EMOTION_LABELS.indexOf(r.emotionLabel)
    if (emotionIdx !== -1 && hour >= 0 && hour < 24) {
      countMap[emotionIdx][hour]++
    }
  })

  const data = []
  EMOTION_LABELS.forEach((_, emotionIdx) => {
    for (let h = 0; h < 24; h++) {
      data.push([h, emotionIdx, countMap[emotionIdx][h]])
    }
  })

  return { data, maxCount: Math.max(...records.map((_, i) => 1), ...data.map(d => d[2])) }
}

// ---------------------------------------------------------------------------
// 渲染
// ---------------------------------------------------------------------------
function renderChart() {
  if (!chart) return

  const { data, maxCount } = buildHeatData(props.elderId)
  const hasData = data.some(d => d[2] > 0)

  if (!hasData) {
    chart.setOption({
      graphic: [{
        type: 'text',
        left: 'center',
        top: 'middle',
        style: { text: '暂无数据', fill: '#C0C4CC', fontSize: 14 }
      }]
    })
    return
  }

  const yAxisLabels = EMOTION_LABELS.map(k => EMOTION_DISPLAY[k])

  chart.setOption({
    tooltip: {
      position: 'top',
      formatter(params) {
        const hour = params.data[0]
        const emotionName = yAxisLabels[params.data[1]]
        const count = params.data[2]
        return `${emotionName} · ${hour}:00 – ${hour + 1}:00<br/>记录次数：<b>${count}</b>`
      }
    },
    grid: {
      left: 52,
      right: 20,
      top: 10,
      bottom: 52
    },
    xAxis: {
      type: 'category',
      data: HOURS,
      splitArea: { show: true },
      axisLabel: {
        color: '#909399',
        fontSize: 10,
        interval: 2  // 每3小时显示一个标签，避免拥挤
      },
      axisLine: { lineStyle: { color: '#DCDFE6' } }
    },
    yAxis: {
      type: 'category',
      data: yAxisLabels,
      splitArea: { show: true },
      axisLabel: {
        color: '#606266',
        fontSize: 12
      },
      axisLine: { lineStyle: { color: '#DCDFE6' } }
    },
    visualMap: {
      min: 0,
      max: Math.max(maxCount, 1),
      calculable: false,
      show: true,
      orient: 'horizontal',
      left: 'center',
      bottom: 2,
      itemWidth: 14,
      itemHeight: 80,
      textStyle: { fontSize: 10, color: '#909399' },
      text: ['多', '少'],
      inRange: {
        color: ['#f5f7fa', '#FFDBC6', '#FF8C5A', '#FF6B35', '#E55A25']
      }
    },
    series: [{
      name: '时段情感分布',
      type: 'heatmap',
      data,
      label: {
        show: false
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 8,
          shadowColor: 'rgba(255, 107, 53, 0.5)'
        }
      }
    }]
  })
}
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 240px;
}
</style>
