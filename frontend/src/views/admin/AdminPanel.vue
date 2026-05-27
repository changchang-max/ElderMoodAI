<template>
  <div>
    <div class="page-title">管理后台</div>
    <div class="page-card">
      <el-tabs v-model="activeTab">
        <!-- 用户管理 -->
        <el-tab-pane label="用户管理" name="users">
          <el-table :data="users" stripe>
            <el-table-column prop="name" label="姓名" width="100" />
            <el-table-column label="角色" width="90">
              <template #default="{ row }">
                <el-tag :type="{ admin:'danger', caregiver:'warning', family:'success' }[row.role]" size="small">
                  {{ { admin:'管理员', caregiver:'护理员', family:'家属' }[row.role] }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="phone" label="手机号" width="130" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-switch v-model="row.isActive" @change="(v) => toggleUser(row, v)" :disabled="row.id === currentUserId" />
              </template>
            </el-table-column>
            <el-table-column prop="lastLoginAt" label="最后登录" width="160" :formatter="(r) => r.lastLoginAt ? new Date(r.lastLoginAt).toLocaleString('zh-CN') : '从未'" />
          </el-table>
        </el-tab-pane>

        <!-- 系统配置 -->
        <el-tab-pane label="系统配置" name="config">
          <el-form label-width="140px" style="max-width:500px">
            <el-form-item label="焦虑预警阈值"><el-slider v-model="config.anxietyThreshold" :min="0.5" :max="1" :step="0.05" show-input /></el-form-item>
            <el-form-item label="低落预警阈值"><el-slider v-model="config.sadThreshold" :min="0.5" :max="1" :step="0.05" show-input /></el-form-item>
            <el-form-item label="愤怒预警阈值"><el-slider v-model="config.angryThreshold" :min="0.5" :max="1" :step="0.05" show-input /></el-form-item>
            <el-form-item label="数据保留天数"><el-input-number v-model="config.retentionDays" :min="30" :max="3650" /></el-form-item>
            <el-form-item><el-button type="primary" @click="saveConfig">保存配置</el-button></el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- AI状态监控 -->
        <el-tab-pane label="AI状态监控" name="ai">
          <el-row :gutter="16" style="margin-bottom:16px">
            <el-col :span="6">
              <el-card shadow="never">
                <div class="ai-stat">
                  <div class="ai-label">服务状态</div>
                  <el-tag :type="aiStatus.status === 'normal' ? 'success' : 'danger'" size="large">
                    {{ aiStatus.status === 'normal' ? '✅ 正常' : '❌ 异常' }}
                  </el-tag>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="never">
                <div class="ai-stat">
                  <div class="ai-label">识别准确率</div>
                  <div class="ai-value">{{ (aiStatus.accuracyRate * 100).toFixed(1) }}%</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="never">
                <div class="ai-stat">
                  <div class="ai-label">推理耗时</div>
                  <div class="ai-value">{{ aiStatus.latencyMs }}ms</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-button @click="refreshAiStatus" :loading="refreshing">刷新状态</el-button>
            </el-col>
          </el-row>
          <div v-if="aiStatus.recentErrors?.length">
            <p style="font-weight:600;margin-bottom:8px">最近错误日志</p>
            <el-table :data="aiStatus.recentErrors" size="small">
              <el-table-column prop="time" label="时间" width="180" :formatter="(r) => new Date(r.time).toLocaleString('zh-CN')" />
              <el-table-column prop="message" label="错误信息" />
            </el-table>
          </div>
          <el-empty v-else description="暂无错误日志" />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth.js'
import { mockUsers } from '@/mock/users.js'
import axios from 'axios'

const authStore = useAuthStore()
const currentUserId = computed(() => authStore.user?.id)
const activeTab = ref('users')
const users = ref([...mockUsers.map(({ password: _, ...u }) => u)])
const refreshing = ref(false)
const aiStatus = reactive({ status: 'normal', latencyMs: 620, accuracyRate: 0.85, recentErrors: [] })
const config = reactive({ anxietyThreshold: 0.7, sadThreshold: 0.6, angryThreshold: 0.75, retentionDays: 365 })

onMounted(refreshAiStatus)

async function toggleUser(row, val) {
  if (row.id === currentUserId.value) { row.isActive = true; ElMessage.warning('不能禁用当前登录账号'); return }
  await axios.patch(`/api/admin/users/${row.id}/status`, { isActive: val })
  ElMessage.success(val ? '账号已启用' : '账号已禁用')
}

function saveConfig() { ElMessage.success('配置已保存并生效') }

async function refreshAiStatus() {
  refreshing.value = true
  try {
    const res = await axios.get('/api/admin/ai-status')
    Object.assign(aiStatus, res.data.data)
  } finally { refreshing.value = false }
}
</script>

<style lang="scss" scoped>
.ai-stat { text-align: center; padding: 8px 0;
  .ai-label { font-size: 13px; color: $text-secondary; margin-bottom: 8px; }
  .ai-value { font-size: 24px; font-weight: 700; color: $primary-color; }
}
</style>
