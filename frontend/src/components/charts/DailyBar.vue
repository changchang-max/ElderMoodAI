<template>
  <div class="daily-bar-wrapper">
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
   * 每日统计数据数组，每项形如：
   * {
   *   date: '03/01',        // 日期标签
   *   count: 5,             // 当日监测次数
   *   avgHealthScore: 78    // 当日平均健康分（null 表示当天无数据）
   * }
   */
  data: {
    type: Array,
    default: () => []
  }
})

// ---------------------------------------------------------------------------
// Chart refs
// ---------------------------------------------------------------------------
const chartRef = ref(null)
let chart = null

// ---------------------------------------------------------------------------
// Lifecycle
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
// Watcher
// ---------------------------------------------------------------------------
watch(() => props.data, renderChart, { deep: true })

// ---------------------------------------------------------------------------
// Chart render
// ---------------------------------------------------------------------------
function renderChart() {
  if (!chart) return

  if (!props.data.length) {
    chart.setOption(buildEmptyOption(), true)
    return
  }

  chart.setOption(buildChartOption(), true)
}

function buildChartOption() {
  const dates = props.data.map(d => d.date)
  const counts = props.data.map(d => d.count ?? 0)
  const healthScores = props.data.map(d =>
    d.avgHealthScore !== null && d.avgHealthScore !== undefined ? d.avgHealthScore : null
  )

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter(params) {
        const header = `<div style="font-weight:600;margin-bottom:4px">${params[0]?.name}</div>`
        const rows = params.map(p => {
          const unit = p.seriesName === '监测次数' ? '次' : '分'
          const val = p.value !== null && p.value !== undefined ? `${p.value}${unit}` : '无数据'
          return `<div style="display:flex;justify-content:space-between;gap:16px">
            <span>${p.marker}${p.seriesName}</span>
            <span style="font-weight:600">${val}</span>
          </div>`
        }).join('')
        return header + rows
      }
    },
    legend: {
      data: ['监测次数', '平均健康分'],
      bottom: 0,
      textStyle: { color: '#606266', fontSize: 12 },
      itemWidth: 16,
      itemHeight: 10
    },
    grid: {
      left: 44,
      right: 44,
      top: 16,
      bottom: 48
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#DCDFE6' } },
      axisLabel: { color: '#909399', fontSize: 11 }
    },
    yAxis: [
      {
        type: 'value',
        name: '次数',
        nameTextStyle: { color: '#909399', fontSize: 11 },
        minInterval: 1,
        min: 0,
        axisLabel: { color: '#909399', fontSize: 11 },
        splitLine: { lineStyle: { color: '#F2F6FC' } }
      },
      {
        type: 'value',
        name: '健康分',
        nameTextStyle: { color: '#909399', fontSize: 11 },
        min: 0,
        max: 100,
        axisLabel: { color: '#909399', fontSize: 11 },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '监测次数',
        type: 'bar',
        yAxisIndex: 0,
        data: counts,
        barMaxWidth: 32,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#F7C59F' },
            { offset: 1, color: '#FFE0C8' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#FF8C5A' },
              { offset: 1, color: '#F7C59F' }
            ])
          }
        }
      },
      {
        name: '平均健康分',
        type: 'line',
        yAxisIndex: 1,
        data: healthScores,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#FF6B35', width: 2 },
        itemStyle: { color: '#FF6B35' },
        connectNulls: true
      }
    ]
  }
}

function buildEmptyOption() {
  return {
    graphic: [{
      type: 'text',
      left: 'center',
      top: 'middle',
      style: { text: '暂无数据', fill: '#C0C4CC', fontSize: 14 }
    }],
    // clear axes/series so nothing leftover renders
    xAxis: { type: 'category', data: [], axisLine: { show: false }, axisTick: { show: false } },
    yAxis: [{ show: false }, { show: false }],
    series: []
  }
}
</script>

<style scoped>
.daily-bar-wrapper {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.chart-container {
  width: 100%;
  height: 280px;
}
</style>
