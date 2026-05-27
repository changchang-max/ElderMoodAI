<template>
  <div class="stat-card" :style="{ borderTop: `3px solid ${color}` }">
    <div class="stat-icon" :style="{ background: color + '20', color }">
      <el-icon :size="24"><component :is="icon" /></el-icon>
    </div>
    <div class="stat-body">
      <div class="stat-value">{{ displayValue }}</div>
      <div class="stat-title">{{ title }}</div>
    </div>
    <div v-if="trend !== undefined" class="stat-trend" :class="trend >= 0 ? 'up' : 'down'">
      {{ trend >= 0 ? '↑' : '↓' }} {{ Math.abs(trend) }}%
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch, onMounted } from 'vue'

const props = defineProps({
  title: String,
  value: [Number, String],
  icon: String,
  color: { type: String, default: '#FF6B35' },
  trend: Number,
  suffix: { type: String, default: '' }
})

const displayValue = ref(0)

onMounted(() => animateTo(props.value))
watch(() => props.value, (v) => animateTo(v))

function animateTo(target) {
  if (typeof target !== 'number') { displayValue.value = target; return }
  const start = 0
  const duration = 800
  const startTime = Date.now()
  const tick = () => {
    const elapsed = Date.now() - startTime
    const progress = Math.min(elapsed / duration, 1)
    displayValue.value = Math.round(start + (target - start) * easeOut(progress)) + props.suffix
    if (progress < 1) requestAnimationFrame(tick)
  }
  requestAnimationFrame(tick)
}

function easeOut(t) { return 1 - Math.pow(1 - t, 3) }
</script>

<style lang="scss" scoped>
.stat-card {
  background: $card-bg;
  border-radius: $border-radius-md;
  padding: $spacing-lg;
  display: flex;
  align-items: center;
  gap: $spacing-md;
  box-shadow: $shadow-sm;
  transition: box-shadow 0.2s;
  &:hover { box-shadow: $shadow-md; }

  .stat-icon {
    width: 52px; height: 52px;
    border-radius: $border-radius-md;
    display: flex; align-items: center; justify-content: center;
    flex-shrink: 0;
  }
  .stat-body {
    flex: 1;
    .stat-value { font-size: 28px; font-weight: 700; color: $text-primary; line-height: 1.2; }
    .stat-title { font-size: 13px; color: $text-secondary; margin-top: 4px; }
  }
  .stat-trend {
    font-size: 12px; font-weight: 600;
    &.up { color: #67C23A; }
    &.down { color: #F56C6C; }
  }
}
</style>
