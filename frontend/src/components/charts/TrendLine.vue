<template>
  <div class="trend-line-wrapper">
    <!-- 标题栏 + 日/周/月切换 -->
    <div class="trend-header">
      <span class="trend-title">{{ title }}</span>
      <div class="period-tabs">
        <button
          v-for="tab in periodTabs"
          :key="tab.value"
          class="period-btn"
          :class="{ active: activePeriod === tab.value }"
          @click="handlePeriodChange(tab.value)"
        >
          {{ tab.label }}
        </button>
      </div>
    </div>

    <!-- ECharts 容器 -->
    <div ref="chartRef" class="chart-container"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

// ---------------------------------------------------------------------------
// Props
// ---------------------------------------------------------------------------
const props = defineProps({
  /**
   * 时间序列数据数组，每项形如：
   * {
   *   date: '03/01',
   *   happy: 2, calm: 5, sad: 1, anxious: 0, angry: 0,  // 各情感计数（可选）
   *   avgHealthScore: 78,                                  // 平均健康分（可选）
   *   count: 8                                             // 当日总记录数（可选）
   * }
   */
  data: {
    type: Array,
    default: () => []
  },
  /** 图表标题 */
  title: {
    type: String,
    default: '情感趋势'
  },
  /**
   * 当前时间段（外部控制）：'day' | 'week' | 'month'
   * 若父组件传入此 prop，则以父组件为准；否则由内部状态管理。
   */
  period: {
    type: String,
    default: null,
    validator: (v) => v === null || ['day', 'week', 'month'].includes(v)
  },
  /**
   * 是否展示多情感折线（true = 每种情感一条线）
   * false = 展示单条平均健康评分曲线（兼容旧用法）
   * 当设为 true 但数据中不含情感计数字段时，会自动降级为单线模式。
   */
  multiLine: {
    type: Boolean,
    default: false
  }
})

// ---------------------------------------------------------------------------
// Emits
// ---------------------------------------------------------------------------
const emit = defineEmits(['period-change'])

// ---------------------------------------------------------------------------
// 常量
// ---------------------------------------------------------------------------
const periodTabs = [
  { label: '日', value: 'day' },
  { label: '周', value: 'week' },
  { label: '月', value: 'month' }
]

const EMOTION_CONFIG = {
  happy:   { label: '开心', color: '#67C23A' },
  calm:    { label: '平静', color: '#409EFF' },
  sad:     { label: '低落', color: '#909399' },
  anxious: { label: '焦虑', color: '#FF6B35' },
  angry:   { label: '愤怒', color: '#F56C6C' }
}

// ---------------------------------------------------------------------------
// State
// ---------------------------------------------------------------------------
const chartRef = ref(null)
let chart = null

/** 内部周期状态（当 props.period 为 null 时使用） */
const internalPeriod = ref('week')

/** 实际生效的周期 */
const activePeriod = computed(() =>
  props.period !== null ? props.period : internalPeriod.value
)

// ---------------------------------------------------------------------------
// Period switch
// ---------------------------------------------------------------------------
function handlePeriodChange(value) {
  if (activePeriod.value === value) return
  internalPeriod.value = value
  emit('period-change', value)
}

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
watch(() => props.data,     renderChart, { deep: true })
watch(() => props.period,   renderChart)
watch(() => props.multiLine, renderChart)

// ---------------------------------------------------------------------------
// Chart render
// ---------------------------------------------------------------------------
function renderChart() {
  if (!chart) return

  const dates = props.data.map(d => d.date)

  if (!dates.length) {
    chart.setOption(buildEmptyOption())
    return
  }

  // 自动检测：数据中含有情感计数字段时才启用多线模式
  const hasEmotionCounts = props.data.some(d =>
    Object.keys(EMOTION_CONFIG).some(k => d[k] !== undefined)
  )
  const useMultiLine = props.multiLine && hasEmotionCounts

  if (useMultiLine) {
    chart.setOption(buildMultiLineOption(dates))
  } else {
    chart.setOption(buildSingleLineOption(dates))
  }
}

/** 多情感折线 option */
function buildMultiLineOption(dates) {
  const series = Object.entries(EMOTION_CONFIG).map(([key, cfg]) => ({
    name: cfg.label,
    type: 'line',
    data: props.data.map(d => d[key] ?? null),
    smooth: true,
    symbol: 'circle',
    symbolSize: 5,
    lineStyle: { color: cfg.color, width: 2 },
    itemStyle: { color: cfg.color },
    connectNulls: false
  }))

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross', crossStyle: { color: '#FF6B35' } },
      formatter(params) {
        const header = `<div style="font-weight:600;margin-bottom:4px">${params[0]?.name}</div>`
        const rows = params
          .filter(p => p.value !== null && p.value !== undefined)
          .map(p => `<div style="display:flex;justify-content:space-between;gap:16px">
            <span>${p.marker}${p.seriesName}</span>
            <span style="font-weight:600">${p.value}次</span>
          </div>`)
          .join('')
        return rows.length ? header + rows : `${header}<span style="color:#909399">暂无数据</span>`
      }
    },
    legend: {
      data: Object.values(EMOTION_CONFIG).map(c => c.label),
      bottom: 0,
      textStyle: { color: '#606266', fontSize: 12 },
      itemWidth: 16,
      itemHeight: 3
    },
    grid: { left: 40, right: 20, top: 20, bottom: 48 },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false,
      axisLine: { lineStyle: { color: '#DCDFE6' } },
      axisLabel: { color: '#909399', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: '#909399', fontSize: 11 },
      splitLine: { lineStyle: { color: '#F2F6FC' } }
    },
    series
  }
}

/** 单条健康评分折线 option（兼容旧用法） */
function buildSingleLineOption(dates) {
  return {
    tooltip: {
      trigger: 'axis',
      formatter(params) {
        const p = params[0]
        return `${p.name}<br/>健康分：<b>${p.value ?? '无数据'}</b>`
      }
    },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false,
      axisLine: { lineStyle: { color: '#DCDFE6' } },
      axisLabel: { color: '#909399', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      axisLabel: { color: '#909399', fontSize: 11 },
      splitLine: { lineStyle: { color: '#F2F6FC' } }
    },
    series: [{
      name: '平均健康分',
      type: 'line',
      data: props.data.map(d => d.avgHealthScore ?? null),
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: { color: '#FF6B35', width: 2 },
      itemStyle: { color: '#FF6B35' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(255,107,53,0.28)' },
          { offset: 1, color: 'rgba(255,107,53,0.02)' }
        ])
      },
      connectNulls: true
    }]
  }
}

/** 无数据占位 option */
function buildEmptyOption() {
  return {
    graphic: [{
      type: 'text',
      left: 'center',
      top: 'middle',
      style: { text: '暂无数据', fill: '#C0C4CC', fontSize: 14 }
    }]
  }
}
</script>

<style scoped>
.trend-line-wrapper {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.trend-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.trend-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.period-tabs {
  display: flex;
  gap: 2px;
  background: #f5f7fa;
  border-radius: 6px;
  padding: 2px;
}

.period-btn {
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 500;
  color: #606266;
  background: transparent;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  line-height: 1.5;
}

.period-btn:hover {
  color: #FF6B35;
  background: rgba(255, 107, 53, 0.08);
}

.period-btn.active {
  background: #FF6B35;
  color: #ffffff;
  box-shadow: 0 1px 4px rgba(255, 107, 53, 0.35);
}

.chart-container {
  width: 100%;
  height: 280px;
}
</style>
