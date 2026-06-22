<template>
  <span class="emotion-tag" :style="{ color: info.color, background: info.color + '20' }">
    <span class="emotion-icon">{{ info.icon }}</span>
    <span class="emotion-label">{{ info.label }}</span>
  </span>
</template>

<script setup>
import { computed } from 'vue'

// Accepts either:
//   emotion="开心"  (Chinese string, per spec)
//   label="happy"   (English key, legacy / internal use)
const props = defineProps({
  emotion: {
    type: String,
    default: ''
  },
  label: {
    type: String,
    default: ''
  }
})

// Map by Chinese label
const chineseMap = {
  '开心': { label: '开心', color: '#67C23A', icon: '😊' },
  '平静': { label: '平静', color: '#409EFF', icon: '😌' },
  '低落': { label: '低落', color: '#909399', icon: '😔' },
  '焦虑': { label: '焦虑', color: '#E6A23C', icon: '😰' },
  '愤怒': { label: '愤怒', color: '#F56C6C', icon: '😠' },
}

// Map by English key (backward compat)
const englishMap = {
  happy:   '开心',
  calm:    '平静',
  sad:     '低落',
  anxious: '焦虑',
  angry:   '愤怒',
}

const info = computed(() => {
  // Prefer the `emotion` prop (Chinese string), fall back to `label` (English key)
  const key = props.emotion || (englishMap[props.label] ?? props.label)
  return chineseMap[key] ?? { label: key || '未知', color: '#909399', icon: '😶' }
})
</script>

<style lang="scss" scoped>
.emotion-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  line-height: 1.6;
  transition: opacity 0.2s;

  .emotion-icon {
    font-size: 14px;
    line-height: 1;
  }

  .emotion-label {
    line-height: 1;
  }
}
</style>
