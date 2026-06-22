<template>
  <div class="health-gauge-wrapper">
    <div ref="chartRef" class="chart-container"></div>
    <div class="gauge-label">
      <span class="label-text" :style="{ color: currentColor }">{{ healthLabel }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

// ---------------------------------------------------------------------------
// Props
// ---------------------------------------------------------------------------
const props = defineProps({
  /** 健康评分，0~100 */
  score: {
    type: Number,
    default: 0,
    validator: (v) => v >= 0 && v <= 100
  }
})

// ---------------------------------------------------------------------------
// Color thresholds（规格要求）
//   0  ~ 60  → 红色  #F56C6C（健康堪忧）
//   60 ~ 80  → 橙黄  #E6A23C（一般）
//   80 ~ 100 → 绿色  #67C23A（良好）
// ---------------------------------------------------------------------------
function getColor(score) {
  if (score >= 80) return '#67C23A'
  if (score >= 60) return '#E6A23C'
  return '#F56C6C'
}

const currentColor = computed(() => getColor(props.score))

const healthLabel = computed(() => {
  if (props.score >= 80) return '情绪良好'
  if (props.score >= 60) return '情绪一般'
  return '情绪堪忧'
})

// ---------------------------------------------------------------------------
// Chart lifecycle
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

function onResize() {
  chart?.resize()
}

// ---------------------------------------------------------------------------
// Watcher – re-render when score changes
// ---------------------------------------------------------------------------
watch(() => props.score, renderChart)

// ---------------------------------------------------------------------------
// Chart render
// ---------------------------------------------------------------------------
function renderChart() {
  if (!chart) return

  const color = getColor(props.score)

  chart.setOption({
    series: [
      {
        type: 'gauge',
        // 从左下角开始，扫过上方到右下角（200° → -20°，共 220° 弧度）
        startAngle: 200,
        endAngle: -20,
        min: 0,
        max: 100,
        radius: '88%',
        center: ['50%', '60%'],

        // 进度弧（已填充部分）
        progress: {
          show: true,
          width: 14,
          itemStyle: { color }
        },

        // 轨道弧（背景部分）
        axisLine: {
          lineStyle: {
            width: 14,
            color: [[1, '#EBEEF5']]
          }
        },

        // 刻度 / 分割线 / 标签 —— 全部隐藏，保持干净
        axisTick:  { show: false },
        splitLine: { show: false },
        axisLabel: { show: false },

        // 指针
        pointer: {
          show: true,
          length: '58%',
          width: 5,
          itemStyle: { color }
        },

        // 中心数值
        detail: {
          valueAnimation: true,
          formatter: '{value}',
          fontSize: 34,
          fontWeight: 700,
          color,
          offsetCenter: [0, '15%']
        },

        // 仪表盘标题（数值下方文字，由外层 .gauge-label 替代，此处置空）
        title: { show: false },

        data: [{ value: props.score, name: '健康评分' }]
      }
    ]
  })
}
</script>

<style scoped>
.health-gauge-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.chart-container {
  width: 100%;
  height: 200px;
}

.gauge-label {
  margin-top: -8px;
  text-align: center;
}

.label-text {
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.5px;
  transition: color 0.3s;
}
</style>
