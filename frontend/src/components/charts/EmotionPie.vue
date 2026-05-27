<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({ data: { type: Object, default: () => ({}) } })
const chartRef = ref(null)
let chart = null

const colorMap = { happy: '#67C23A', calm: '#409EFF', sad: '#909399', anxious: '#FF6B35', angry: '#F56C6C' }
const labelMap = { happy: '开心', calm: '平静', sad: '低落', anxious: '焦虑', angry: '愤怒' }

onMounted(() => { chart = echarts.init(chartRef.value); renderChart(); window.addEventListener('resize', resize) })
onUnmounted(() => { chart?.dispose(); window.removeEventListener('resize', resize) })
watch(() => props.data, renderChart, { deep: true })
function resize() { chart?.resize() }

function renderChart() {
  if (!chart) return
  const seriesData = Object.entries(props.data)
    .filter(([, v]) => v > 0)
    .map(([k, v]) => ({ name: labelMap[k] || k, value: v, itemStyle: { color: colorMap[k] || '#ccc' } }))

  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}次 ({d}%)' },
    legend: { bottom: 0, textStyle: { color: '#606266', fontSize: 12 } },
    series: [{
      type: 'pie', radius: ['40%', '70%'],
      center: ['50%', '45%'],
      data: seriesData,
      label: { show: false },
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.2)' } }
    }]
  })
}
</script>

<style scoped>
.chart-container { width: 100%; height: 280px; }
</style>
