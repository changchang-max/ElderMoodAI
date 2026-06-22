<template>
  <span class="alert-badge" :style="{ color: info.color, background: info.color + '1A', borderColor: info.color + '4D' }">
    <span class="alert-badge__dot" :style="{ background: info.color }"></span>
    <span class="alert-badge__label">{{ info.label }}</span>
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // Accepts Chinese values: '高' | '中' | '低'
  // Also accepts English aliases for backward compatibility: 'high' | 'medium' | 'low'
  level: {
    type: String,
    default: ''
  }
})

// Map by Chinese label (primary, per spec)
const chineseMap = {
  '高': { label: '高', color: '#F56C6C' },
  '中': { label: '中', color: '#E6A23C' },
  '低': { label: '低', color: '#F9B800' },
}

// English alias map for backward compatibility
const englishAlias = {
  high:   '高',
  medium: '中',
  low:    '低',
  // legacy verbose keys
  '高危': '高',
  '中危': '中',
  '低危': '低',
}

const info = computed(() => {
  const key = chineseMap[props.level]
    ? props.level
    : (englishAlias[props.level] ?? null)

  return key
    ? chineseMap[key]
    : { label: props.level || '未知', color: '#909399' }
})
</script>

<style lang="scss" scoped>
.alert-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 2px 10px;
  border-radius: 20px;
  border: 1px solid;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
  line-height: 1.6;
  transition: opacity 0.2s;

  &__dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    flex-shrink: 0;
  }

  &__label {
    line-height: 1;
  }
}
</style>
