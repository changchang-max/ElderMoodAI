<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({ data: { type: Array, default: () => [] } })
const chartRef = ref(null)
let chart = null

onMounted(() => { chart = echarts.init(chartRef.value); renderChart(); window.addEventListener('resize', resize) })
onUnmounted(() => { chart?.dispose(); window.removeEventListener('resize', resize) })
watch(() => props.data, renderChart, { deep: true })
function resize() { chart?.resize() }

function renderChart() {
  if (!chart || !props.data.length) return
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['监测次数', '平均健康分'], bottom: 0, textStyle: { fontSize: 12 } },
    grid: { left: 40, right: 40, top: 20, bottom: 50 },
    xAxis: { type: 'category', data: props.data.map(d => d.date), axisLabel: { color: '#909399', fontSize: 11 } },
    yAxis: [
      { type: 'value', name: '次数', min: 0, axisLabel: { color: '#909399', fontSize: 11 } },
      { type: 'value', name: '健康分', min: 0, max: 100, axisLabel: { color: '#909399', fontSize: 11 } }
    ],
    series: [
      { name: '监测次数', type: 'bar', data: props.data.map(d => d.count), itemStyle: { color: '#F7C59F', borderRadius: [4,4,0,0] } },
      { name: '平均健康分', type: 'line', yAxisIndex: 1, data: props.data.map(d => d.avgScore), smooth: true, lineStyle: { color: '#FF6B35' }, itemStyle: { color: '#FF6B35' }, connectNulls: true }
    ]
  })
}
</script>

<style scoped>
.chart-container { width: 100%; height: 280px; }
</style>
