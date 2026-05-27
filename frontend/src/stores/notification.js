import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref([])
  const unreadCount = computed(() => notifications.value.filter(n => !n.read).length)

  function addAlert(alert) {
    notifications.value.unshift({
      id: Date.now(),
      type: 'alert',
      elderName: alert.elderName,
      emotionLabel: alert.emotionLabel,
      alertLevel: alert.alertLevel,
      message: `${alert.elderName} 检测到${emotionLabelText(alert.emotionLabel)}情绪，请关注`,
      read: false,
      createdAt: new Date().toISOString(),
    })
    // 最多保留50条
    if (notifications.value.length > 50) {
      notifications.value = notifications.value.slice(0, 50)
    }
  }

  function markRead(id) {
    const n = notifications.value.find(n => n.id === id)
    if (n) n.read = true
  }

  function markAllRead() {
    notifications.value.forEach(n => { n.read = true })
  }

  function emotionLabelText(label) {
    const map = { happy: '开心', calm: '平静', sad: '低落', anxious: '焦虑', angry: '愤怒' }
    return map[label] || label
  }

  return { notifications, unreadCount, addAlert, markRead, markAllRead }
})
