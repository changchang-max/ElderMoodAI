<template>
  <el-tag :type="info.type" size="small" effect="light">{{ info.label }}</el-tag>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({ level: String })
const map = {
  high:   { label: '高危', type: 'danger' },
  medium: { label: '中危', type: 'warning' },
  low:    { label: '低危', type: '' },
}
const info = computed(() => map[props.level] || { label: props.level, type: 'info' })
</script>
