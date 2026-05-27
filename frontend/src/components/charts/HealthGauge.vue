<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({ score: { type: Number, default: 0 } })
const chartRef = ref(null)
let chart = null

onMounted(() => { chart = echarts.init(chartRef.value); renderChart(); window.addEventListener('resize', resize) })
onUnmounted(() => { chart?.dispose(); window.removeEventListener('resize', resize) })
watch(() => props.score, renderChart)
function resize() { chart?.resize() }

function getColor(score) {
  if (score >= 70) return '#67C23A'
  if (score >= 40) return '#E6A23C'
  return '#F56C6C'
}

function renderChart() {
  if (!chart) return
  const color = getColor(props.score)
  chart.setOption({
    series: [{
      type: 'gauge',
      startAngle: 200, endAngle: -20,
      min: 0, max: 100,
      radius: '90%',
      pointer: { show: true, length: '60%', width: 4 },
      progress: { show: true, width: 12, itemStyle: { color } },
      axisLine: { lineStyle: { width: 12, color: [[1, '#f0f0f0']] } },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: { show: false },
      detail: {
        valueAnimation: true,
        formatter: '{value}',
        fontSize: 32, fontWeight: 700,
        color,
        offsetCenter: [0, '20%']
      },
      title: { offsetCenter: [0, '50%'], fontSize: 13, color: '#909399' },
      data: [{ value: props.score, name: '健康评分' }]
    }]
  })
}
</script>

<style scoped>
.chart-container { width: 100%; height: 200px; }
</style>
