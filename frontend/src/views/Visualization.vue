<template>
  <div>
    <div class="page-title">历史数据可视化</div>
    <div class="page-card">
      <el-form inline>
        <el-form-item label="老人">
          <el-select v-model="elderId" style="width:140px" @change="loadStats">
            <el-option v-for="e in elders" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-radio-group v-model="range" @change="loadStats">
            <el-radio-button value="day">日</el-radio-button>
            <el-radio-button value="week">周</el-radio-button>
            <el-radio-button value="month">月</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </div>

    <el-row :gutter="16" v-if="stats">
      <el-col :span="16">
        <div class="page-card">
          <div class="card-title">情感趋势曲线</div>
          <TrendLine :data="stats.trend" />
        </div>
      </el-col>
      <el-col :span="8">
        <div class="page-card">
          <div class="card-title">情感类型占比</div>
          <EmotionPie :data="stats.distribution" />
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" v-if="stats">
      <el-col :span="14">
        <div class="page-card">
          <div class="card-title">每日统计</div>
          <DailyBar :data="stats.trend.map(d => ({ date: d.date, count: d.count, avgScore: d.avgHealthScore }))" />
        </div>
      </el-col>
      <el-col :span="10">
        <div class="page-card">
          <div class="card-title">时段分布热力图</div>
          <HeatMap :elder-id="elderId" />
        </div>
      </el-col>
    </el-row>

    <div class="page-card">
      <div class="card-title">历史记录</div>
      <el-empty v-if="records.length === 0" description="暂无数据，请先进行情感采集" />
      <el-table v-else :data="records" stripe>
        <el-table-column prop="elderName" label="老人" width="90" />
        <el-table-column label="情感状态" width="110">
          <template #default="{ row }"><EmotionTag :label="row.emotionLabel" /></template>
        </el-table-column>
        <el-table-column label="置信度" width="120">
          <template #default="{ row }">{{ Math.round(row.confidence * 100) }}%</template>
        </el-table-column>
        <el-table-column label="健康评分" width="120">
          <template #default="{ row }">
            <el-progress :percentage="row.healthScore" :color="scoreColor(row.healthScore)" :show-text="false" />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="分析时间" :formatter="(r) => new Date(r.createdAt).toLocaleString('zh-CN')" />
      </el-table>
      <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="total, prev, pager, next" class="pagination" @current-change="loadRecords" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import TrendLine from '@/components/charts/TrendLine.vue'
import EmotionPie from '@/components/charts/EmotionPie.vue'
import DailyBar from '@/components/charts/DailyBar.vue'
import HeatMap from '@/components/charts/HeatMap.vue'
import EmotionTag from '@/components/common/EmotionTag.vue'
import { mockElders } from '@/mock/elders.js'
import { getEmotionStats, mockEmotions } from '@/mock/emotions.js'

const elders = ref(mockElders.filter(e => e.privacyAuthorized))
const elderId = ref(elders.value[0]?.id || 1)
const range = ref('week')
const stats = ref(null)
const records = ref([]), page = ref(1), total = ref(0)

onMounted(() => { loadStats(); loadRecords() })

function loadStats() {
  stats.value = getEmotionStats(elderId.value, range.value)
}

function loadRecords() {
  const all = mockEmotions.filter(e => e.elderId === elderId.value)
  total.value = all.length
  records.value = all.slice((page.value - 1) * 10, page.value * 10)
}

function scoreColor(s) { return s >= 70 ? '#67C23A' : s >= 40 ? '#E6A23C' : '#F56C6C' }
</script>

<style lang="scss" scoped>
.card-title { font-size: 15px; font-weight: 600; margin-bottom: $spacing-md; }
.pagination { margin-top: $spacing-md; justify-content: flex-end; display: flex; }
</style>
