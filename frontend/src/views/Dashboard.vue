<template>
  <div class="dashboard">
    <div class="page-title">首页概览</div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6"><StatCard title="今日监测人数" :value="data.todayMonitorCount" icon="User" color="#409EFF" /></el-col>
      <el-col :span="6"><StatCard title="今日预警次数" :value="data.todayAlertCount" icon="Warning" color="#F56C6C" /></el-col>
      <el-col :span="6"><StatCard title="平均情感健康分" :value="data.avgHealthScore" suffix="分" icon="TrendCharts" color="#FF6B35" /></el-col>
      <el-col :span="6"><StatCard title="系统运行状态" :value="data.systemStatus === 'normal' ? '正常' : '异常'" icon="Monitor" :color="data.systemStatus === 'normal' ? '#67C23A' : '#F56C6C'" /></el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="15">
        <div class="page-card">
          <div class="card-title">近7天情感趋势</div>
          <TrendLine :data="data.trend7Days" />
        </div>
      </el-col>
      <el-col :span="9">
        <div class="page-card">
          <div class="card-title">情感类型占比</div>
          <EmotionPie :data="data.distribution" />
        </div>
      </el-col>
    </el-row>

    <!-- 底部列表 -->
    <el-row :gutter="16">
      <el-col :span="12">
        <div class="page-card">
          <div class="card-title-row">
            <span class="card-title">最新预警</span>
            <el-button text size="small" @click="$router.push('/alerts')">查看全部</el-button>
          </div>
          <el-table :data="data.recentAlerts" size="small" :show-header="false">
            <el-table-column width="80">
              <template #default="{ row }"><AlertBadge :level="row.alertLevel" /></template>
            </el-table-column>
            <el-table-column prop="elderName" width="80" />
            <el-table-column>
              <template #default="{ row }"><EmotionTag :label="row.emotionLabel" /></template>
            </el-table-column>
            <el-table-column prop="createdAt" :formatter="(r) => formatTime(r.createdAt)" />
          </el-table>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="page-card">
          <div class="card-title-row">
            <span class="card-title">最近监测记录</span>
            <el-button text size="small" @click="$router.push('/visualization')">查看全部</el-button>
          </div>
          <el-table :data="data.recentRecords" size="small" :show-header="false">
            <el-table-column prop="elderName" width="80" />
            <el-table-column>
              <template #default="{ row }"><EmotionTag :label="row.emotionLabel" /></template>
            </el-table-column>
            <el-table-column>
              <template #default="{ row }">
                <el-progress :percentage="row.healthScore" :color="scoreColor(row.healthScore)" :show-text="false" />
              </template>
            </el-table-column>
            <el-table-column :formatter="(r) => formatTime(r.createdAt)" width="90" />
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import StatCard from '@/components/common/StatCard.vue'
import EmotionTag from '@/components/common/EmotionTag.vue'
import AlertBadge from '@/components/common/AlertBadge.vue'
import TrendLine from '@/components/charts/TrendLine.vue'
import EmotionPie from '@/components/charts/EmotionPie.vue'
import { getDashboardData } from '@/mock/dashboard.js'

const data = reactive({ todayMonitorCount: 0, todayAlertCount: 0, avgHealthScore: 0, systemStatus: 'normal', trend7Days: [], distribution: {}, recentAlerts: [], recentRecords: [] })

onMounted(() => Object.assign(data, getDashboardData()))

function formatTime(iso) {
  const d = new Date(iso), now = new Date()
  const diff = Math.floor((now - d) / 60000)
  if (diff < 60) return `${diff}分钟前`
  if (diff < 1440) return `${Math.floor(diff/60)}小时前`
  return d.toLocaleDateString('zh-CN')
}

function scoreColor(s) { return s >= 70 ? '#67C23A' : s >= 40 ? '#E6A23C' : '#F56C6C' }
</script>

<style lang="scss" scoped>
.stat-row { margin-bottom: $spacing-md; }
.chart-row { margin-bottom: $spacing-md; }
.card-title { font-size: 15px; font-weight: 600; color: $text-primary; margin-bottom: $spacing-md; display: block; }
.card-title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: $spacing-sm; }
</style>
