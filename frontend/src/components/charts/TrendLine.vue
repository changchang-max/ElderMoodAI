<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: { type: Array, default: () => [] },
  title: { type: String, default: '情感趋势' }
})

const chartRef = ref(null)
let chart = null

onMounted(() => {
  chart = echarts.init(chartRef.value)
  renderChart()
  window.addEventListener('resize', resize)
})

onUnmounted(() => {
  chart?.dispose()
  window.removeEventListener('resize', resize)
})

watch(() => props.data, renderChart, { deep: true })

function resize() { chart?.resize() }

function renderChart() {
  if (!chart || !props.data.length) return
  chart.setOption({
    tooltip: { trigger: 'axis', formatter: (p) => `${p[0].name}<br/>健康分: ${p[0].value ?? '无数据'}` },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: {
      type: 'category',
      data: props.data.map(d => d.date),
      axisLine: { lineStyle: { color: '#DCDFE6' } },
      axisLabel: { color: '#909399', fontSize: 12 }
    },
    yAxis: {
      type: 'value', min: 0, max: 100,
      axisLabel: { color: '#909399', fontSize: 12 },
      splitLine: { lineStyle: { color: '#F2F6FC' } }
    },
    series: [{
      type: 'line',
      data: props.data.map(d => d.avgHealthScore),
      smooth: true,
      symbol: 'circle', symbolSize: 6,
      lineStyle: { color: '#FF6B35', width: 2 },
      itemStyle: { color: '#FF6B35' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(255,107,53,0.3)' },
          { offset: 1, color: 'rgba(255,107,53,0.02)' }
        ])
      },
      connectNulls: true
    }]
  })
}
</script>

<style scoped>
.chart-container { width: 100%; height: 280px; }
</style>
