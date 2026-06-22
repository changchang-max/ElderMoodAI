<template>
  <div class="emotion-pie-wrapper">
    <!-- 可选标题 -->
    <div v-if="title" class="pie-title">{{ title }}</div>

    <!-- ECharts 容器 -->
    <div ref="chartRef" class="chart-container"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

// ---------------------------------------------------------------------------
// Props
// ---------------------------------------------------------------------------
const props = defineProps({
  /**
   * 情感数据，支持两种格式：
   *   1. 对象格式（按英文 key）：{ happy: 10, calm: 5, sad: 3, anxious: 2, angry: 1 }
   *   2. 数组格式：[{ name: '开心', value: 10 }, ...]
   */
  data: {
    type: [Object, Array],
    default: () => ({})
  },
  /** 图表标题（可选） */
  title: {
    type: String,
    default: ''
  }
})

// ---------------------------------------------------------------------------
// 常量
// ---------------------------------------------------------------------------
/** 英文 key → 中文标签 */
const LABEL_MAP = {
  happy:   '开心',
  calm:    '平静',
  sad:     '低落',
  anxious: '焦虑',
  angry:   '愤怒'
}

/** 英文 key → 颜色；中文名 → 颜色（兼容数组格式） */
const COLOR_MAP = {
  happy:   '#67C23A',
  calm:    '#409EFF',
  sad:     '#909399',
  anxious: '#FF6B35',
  angry:   '#F56C6C',
  // 中文别名（数组格式传入中文 name 时使用）
  开心: '#67C23A',
  平静: '#409EFF',
  低落: '#909399',
  焦虑: '#FF6B35',
  愤怒: '#F56C6C'
}

// ---------------------------------------------------------------------------
// State
// ---------------------------------------------------------------------------
const chartRef = ref(null)
let chart = null

// ---------------------------------------------------------------------------
// Chart lifecycle
// ---------------------------------------------------------------------------
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

function onResize() {
  chart?.resize()
}

// ---------------------------------------------------------------------------
// Watchers
// ---------------------------------------------------------------------------
watch(() => props.data, renderChart, { deep: true })

// ---------------------------------------------------------------------------
// Data normalisation
// ---------------------------------------------------------------------------
/**
 * 将两种输入格式统一为 ECharts series data 数组。
 * 返回 Array<{ name: string, value: number, itemStyle: { color: string } }>
 */
function normaliseData() {
  if (Array.isArray(props.data)) {
    // 数组格式：[{ name, value }]
    return props.data
      .filter(item => item.value > 0)
      .map(item => ({
        name:      item.name,
        value:     item.value,
        itemStyle: { color: COLOR_MAP[item.name] || '#C0C4CC' }
      }))
  }

  // 对象格式：{ happy: 10, calm: 5, … }
  return Object.entries(props.data)
    .filter(([, v]) => v > 0)
    .map(([key, v]) => ({
      name:      LABEL_MAP[key] || key,
      value:     v,
      itemStyle: { color: COLOR_MAP[key] || '#C0C4CC' }
    }))
}

// ---------------------------------------------------------------------------
// Chart render
// ---------------------------------------------------------------------------
function renderChart() {
  if (!chart) return

  const seriesData = normaliseData()

  if (!seriesData.length) {
    chart.setOption(buildEmptyOption(), { notMerge: true })
    return
  }

  chart.setOption(buildPieOption(seriesData), { notMerge: true })
}

function buildPieOption(seriesData) {
  return {
    tooltip: {
      trigger: 'item',
      formatter(params) {
        return [
          `<div style="font-weight:600;margin-bottom:4px">${params.name}</div>`,
          `<div style="display:flex;justify-content:space-between;gap:16px">`,
          `  <span>次数</span><span style="font-weight:600">${params.value} 次</span>`,
          `</div>`,
          `<div style="display:flex;justify-content:space-between;gap:16px">`,
          `  <span>占比</span><span style="font-weight:600">${params.percent.toFixed(1)}%</span>`,
          `</div>`
        ].join('')
      }
    },
    legend: {
      orient: 'horizontal',
      bottom: 0,
      left: 'center',
      textStyle: {
        color: '#606266',
        fontSize: 12
      },
      itemWidth: 12,
      itemHeight: 12,
      itemGap: 16
    },
    series: [
      {
        name: '情感占比',
        type: 'pie',
        // 甜甜圈样式
        radius: ['38%', '68%'],
        center: ['50%', '44%'],
        data: seriesData,
        // 扇区标签关闭，靠 tooltip 展示细节
        label: {
          show: false
        },
        labelLine: {
          show: false
        },
        emphasis: {
          scale: true,
          scaleSize: 6,
          itemStyle: {
            shadowBlur: 12,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0,0,0,0.2)'
          }
        },
        // 扇区间距
        itemStyle: {
          borderRadius: 4,
          borderColor: '#ffffff',
          borderWidth: 2
        }
      }
    ]
  }
}

/** 无数据占位 option */
function buildEmptyOption() {
  return {
    graphic: [
      {
        type: 'text',
        left: 'center',
        top: 'middle',
        style: {
          text: '暂无数据',
          fill: '#C0C4CC',
          fontSize: 14
        }
      }
    ],
    series: []
  }
}
</script>

<style scoped>
.emotion-pie-wrapper {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.pie-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.chart-container {
  width: 100%;
  height: 280px;
}
</style>
