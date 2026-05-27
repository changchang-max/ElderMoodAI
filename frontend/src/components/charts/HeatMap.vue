<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({ elderId: { type: Number, default: 1 } })
const chartRef = ref(null)
let chart = null

onMounted(() => { chart = echarts.init(chartRef.value); renderChart(); window.addEventListener('resize', resize) })
onUnmounted(() => { chart?.dispose(); window.removeEventListener('resize', resize) })
watch(() => props.elderId, renderChart)
function resize() { chart?.resize() }

function renderChart() {
  if (!chart) return
  // 生成模拟热力图数据（7天 × 24小时）
  const days = ['周一','周二','周三','周四','周五','周六','周日']
  const hours = Array.from({ length: 24 }, (_, i) => `${i}时`)
  const data = []
  for (let d = 0; d < 7; d++) {
    for (let h = 0; h < 24; h++) {
      const active = (h >= 7 && h <= 22) ? Math.floor(Math.random() * 5) : 0
      data.push([h, d, active])
    }
  }

  chart.setOption({
    tooltip: { position: 'top', formatter: (p) => `${days[p.data[1]]} ${p.data[0]}时: ${p.data[2]}次` },
    grid: { left: 50, right: 20, top: 10, bottom: 40 },
    xAxis: { type: 'category', data: hours, axisLabel: { color: '#909399', fontSize: 10, interval: 2 } },
    yAxis: { type: 'category', data: days, axisLabel: { color: '#909399', fontSize: 12 } },
    visualMap: {
      min: 0, max: 5, calculable: true, orient: 'horizontal',
      left: 'center', bottom: 0, textStyle: { fontSize: 11 },
      inRange: { color: ['#f5f7fa', '#FF6B35'] }
    },
    series: [{ type: 'heatmap', data, label: { show: false } }]
  })
}
</script>

<style scoped>
.chart-container { width: 100%; height: 220px; }
</style>
