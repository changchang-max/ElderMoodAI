<template>
  <span class="emotion-tag" :style="{ color: info.color, background: info.color + '20' }">
    {{ info.icon }} {{ info.label }}
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ label: String })

const map = {
  happy:   { label: '开心', color: '#67C23A', icon: '😊' },
  calm:    { label: '平静', color: '#409EFF', icon: '😌' },
  sad:     { label: '低落', color: '#909399', icon: '😔' },
  anxious: { label: '焦虑', color: '#FF6B35', icon: '😰' },
  angry:   { label: '愤怒', color: '#F56C6C', icon: '😠' },
}

const info = computed(() => map[props.label] || { label: props.label, color: '#909399', icon: '😶' })
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
}
</style>
