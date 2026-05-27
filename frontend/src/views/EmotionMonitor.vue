<template>
  <div>
    <div class="page-title">实时情感监测</div>
    <el-row :gutter="16">
      <!-- 采集区 -->
      <el-col :span="12">
        <div class="page-card">
          <div class="card-title">数据采集</div>
          <el-form label-width="80px">
            <el-form-item label="选择老人">
              <el-select v-model="selectedElderId" placeholder="请选择已授权老人" style="width:100%">
                <el-option v-for="e in authorizedElders" :key="e.id" :label="e.name" :value="e.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="文本输入">
              <el-input v-model="textContent" type="textarea" :rows="3" placeholder="输入老人的对话内容或描述..." />
            </el-form-item>
            <el-form-item label="语音上传">
              <el-upload :auto-upload="false" :on-change="onVoiceChange" accept=".mp3,.wav" :limit="1" :file-list="voiceList">
                <el-button :icon="Microphone">选择语音文件（mp3/wav）</el-button>
              </el-upload>
            </el-form-item>
            <el-form-item label="图像上传">
              <el-upload :auto-upload="false" :on-change="onImageChange" accept=".jpg,.jpeg,.png" :limit="1" list-type="picture-card" :file-list="imageList">
                <el-icon><Plus /></el-icon>
              </el-upload>
            </el-form-item>
          </el-form>

          <div v-if="analyzing" class="analyzing-area">
            <el-progress :percentage="analyzeProgress" :color="'#FF6B35'" />
            <p class="analyzing-text">🔍 情感分析中，请稍候...</p>
          </div>

          <el-button
            type="primary" size="large" style="width:100%;margin-top:16px"
            :disabled="!canAnalyze" :loading="analyzing"
            @click="startAnalyze"
          >
            {{ analyzing ? '分析中...' : '开始分析' }}
          </el-button>
          <p v-if="!selectedElderId" class="hint-text">请先选择已授权的老人</p>
          <p v-else-if="!hasInput" class="hint-text">请至少输入一种数据（文本/语音/图像）</p>
        </div>
      </el-col>

      <!-- 结果区 -->
      <el-col :span="12">
        <div class="page-card result-card">
          <div class="card-title">分析结果</div>
          <div v-if="!result" class="empty-result">
            <el-empty description="暂无分析结果，请提交数据后开始分析" />
          </div>
          <div v-else class="result-content">
            <div class="result-main">
              <EmotionTag :label="result.emotionLabel" class="result-emotion" />
              <div class="result-confidence">
                <span>综合置信度</span>
                <el-progress :percentage="Math.round(result.confidence * 100)" :color="'#FF6B35'" />
              </div>
            </div>

            <div class="modal-scores">
              <div class="modal-item">
                <span>文本分析</span>
                <el-progress v-if="result.textScore" :percentage="Math.round(result.textScore * 100)" :color="'#409EFF'" />
                <span v-else class="not-collected">未采集</span>
              </div>
              <div class="modal-item">
                <span>语音分析</span>
                <el-progress v-if="result.voiceScore" :percentage="Math.round(result.voiceScore * 100)" :color="'#67C23A'" />
                <span v-else class="not-collected">未采集</span>
              </div>
              <div class="modal-item">
                <span>图像分析</span>
                <el-progress v-if="result.imageScore" :percentage="Math.round(result.imageScore * 100)" :color="'#E6A23C'" />
                <span v-else class="not-collected">未采集</span>
              </div>
            </div>

            <div class="gauge-area">
              <p class="gauge-label">情感健康评分</p>
              <HealthGauge :score="result.healthScore" />
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Microphone, Plus } from '@element-plus/icons-vue'
import { ElNotification } from 'element-plus'
import EmotionTag from '@/components/common/EmotionTag.vue'
import HealthGauge from '@/components/charts/HealthGauge.vue'
import { getAuthorizedElders } from '@/mock/elders.js'
import { mockAnalyze } from '@/utils/mockAnalyze.js'
import { useNotificationStore } from '@/stores/notification.js'

const notifyStore = useNotificationStore()
const authorizedElders = ref([])
const selectedElderId = ref(null)
const textContent = ref('')
const voiceList = ref([]), imageList = ref([])
const analyzing = ref(false), analyzeProgress = ref(0)
const result = ref(null)

onMounted(() => { authorizedElders.value = getAuthorizedElders() })

const hasInput = computed(() => textContent.value.trim() || voiceList.value.length || imageList.value.length)
const canAnalyze = computed(() => selectedElderId.value && hasInput.value && !analyzing.value)

function onVoiceChange(file, list) { voiceList.value = list.slice(-1) }
function onImageChange(file, list) { imageList.value = list.slice(-1) }

async function startAnalyze() {
  analyzing.value = true
  analyzeProgress.value = 0
  result.value = null

  const elder = authorizedElders.value.find(e => e.id === selectedElderId.value)
  const timer = setInterval(() => {
    if (analyzeProgress.value < 90) analyzeProgress.value += Math.random() * 15
  }, 400)

  try {
    const res = await mockAnalyze({
      elderId: selectedElderId.value,
      elderName: elder?.name || '',
      hasText: !!textContent.value.trim(),
      hasVoice: voiceList.value.length > 0,
      hasImage: imageList.value.length > 0,
    })
    analyzeProgress.value = 100
    result.value = res

    if (res.shouldAlert) {
      notifyStore.addAlert(res)
      ElNotification({ title: '⚠️ 情感预警', message: `${elder?.name} 检测到${emotionText(res.emotionLabel)}情绪（置信度 ${Math.round(res.confidence * 100)}%）`, type: 'error', duration: 6000 })
    }
  } finally {
    clearInterval(timer)
    analyzing.value = false
  }
}

function emotionText(l) { return { happy:'开心', calm:'平静', sad:'低落', anxious:'焦虑', angry:'愤怒' }[l] || l }
</script>

<style lang="scss" scoped>
.card-title { font-size: 15px; font-weight: 600; margin-bottom: $spacing-md; }
.analyzing-area { margin-top: $spacing-md; .analyzing-text { text-align: center; color: $primary-color; margin-top: $spacing-sm; } }
.hint-text { text-align: center; color: $text-secondary; font-size: 12px; margin-top: $spacing-sm; }
.result-card { min-height: 400px; }
.empty-result { display: flex; align-items: center; justify-content: center; min-height: 300px; }
.result-content {
  .result-main { display: flex; align-items: center; gap: $spacing-lg; margin-bottom: $spacing-lg;
    .result-emotion { font-size: 18px; padding: 6px 16px; }
    .result-confidence { flex: 1; span { font-size: 13px; color: $text-secondary; display: block; margin-bottom: 6px; } }
  }
  .modal-scores { margin-bottom: $spacing-md;
    .modal-item { display: flex; align-items: center; gap: $spacing-md; margin-bottom: $spacing-sm;
      span:first-child { width: 70px; font-size: 13px; color: $text-secondary; flex-shrink: 0; }
      .el-progress { flex: 1; }
      .not-collected { color: $text-placeholder; font-size: 12px; }
    }
  }
  .gauge-area { .gauge-label { text-align: center; font-size: 13px; color: $text-secondary; margin-bottom: 4px; } }
}
</style>
