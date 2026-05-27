<template>
  <div>
    <div class="page-title">预警中心</div>
    <div class="page-card">
      <div class="toolbar">
        <el-tabs v-model="statusFilter" @tab-change="loadAlerts">
          <el-tab-pane label="全部" name="all" />
          <el-tab-pane name="pending"><template #label><el-badge :value="pendingCount" :hidden="pendingCount===0">未处理</el-badge></template></el-tab-pane>
          <el-tab-pane label="已处理" name="handled" />
        </el-tabs>
        <el-button v-if="selectedIds.length > 0" type="warning" @click="batchHandle">批量标记已处理（{{ selectedIds.length }}）</el-button>
      </div>

      <el-table :data="tableData" @selection-change="handleSelectionChange" stripe>
        <el-table-column type="selection" width="50" :selectable="(row) => row.status === 'pending'" />
        <el-table-column prop="createdAt" label="预警时间" width="170" :formatter="(r) => new Date(r.createdAt).toLocaleString('zh-CN')" />
        <el-table-column prop="elderName" label="老人姓名" width="90" />
        <el-table-column label="情感状态" width="110">
          <template #default="{ row }"><EmotionTag :label="row.emotionLabel" /></template>
        </el-table-column>
        <el-table-column label="预警等级" width="90">
          <template #default="{ row }"><AlertBadge :level="row.alertLevel" /></template>
        </el-table-column>
        <el-table-column label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'handled' ? 'success' : 'danger'" size="small">
              {{ row.status === 'handled' ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handledBy" label="处理人" width="100" />
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button v-if="row.status === 'pending'" text type="primary" size="small" @click="handleOne(row)">标记已处理</el-button>
            <span v-else class="handled-time">{{ row.handledAt ? new Date(row.handledAt).toLocaleString('zh-CN') : '' }}</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="total, prev, pager, next" class="pagination" @current-change="loadAlerts" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import EmotionTag from '@/components/common/EmotionTag.vue'
import AlertBadge from '@/components/common/AlertBadge.vue'
import { mockAlerts, getPendingCount } from '@/mock/alerts.js'
import axios from 'axios'

const statusFilter = ref('all'), page = ref(1), total = ref(0)
const tableData = ref([]), selectedIds = ref([])
const pendingCount = computed(() => getPendingCount())

onMounted(loadAlerts)

function loadAlerts() {
  let list = statusFilter.value === 'all' ? [...mockAlerts] : mockAlerts.filter(a => a.status === statusFilter.value)
  total.value = list.length
  tableData.value = list.slice((page.value - 1) * 10, page.value * 10)
}

function handleSelectionChange(rows) { selectedIds.value = rows.map(r => r.id) }

async function handleOne(row) {
  await ElMessageBox.confirm(`确认将「${row.elderName}」的预警标记为已处理？`, '提示', { type: 'warning' })
  await axios.patch(`/api/alerts/${row.id}/handle`)
  ElMessage.success('已标记为处理')
  loadAlerts()
}

async function batchHandle() {
  await ElMessageBox.confirm(`确认批量处理 ${selectedIds.value.length} 条预警？`, '提示', { type: 'warning' })
  await axios.post('/api/alerts/batch-handle', { ids: selectedIds.value })
  ElMessage.success('批量处理成功')
  selectedIds.value = []
  loadAlerts()
}
</script>

<style lang="scss" scoped>
.toolbar { display: flex; justify-content: space-between; align-items: flex-start; }
.pagination { margin-top: $spacing-md; justify-content: flex-end; display: flex; }
.handled-time { font-size: 12px; color: $text-secondary; }
</style>
