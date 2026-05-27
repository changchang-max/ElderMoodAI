<template>
  <div>
    <div class="page-title">系统设置</div>
    <div class="page-card">
      <el-tabs v-model="activeTab">
        <!-- 账号信息 -->
        <el-tab-pane label="账号信息" name="account">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="姓名">{{ user?.name }}</el-descriptions-item>
            <el-descriptions-item label="角色"><el-tag :type="roleTagType">{{ roleLabel }}</el-tag></el-descriptions-item>
            <el-descriptions-item label="手机号">{{ user?.phone || '未绑定' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ user?.email || '未绑定' }}</el-descriptions-item>
          </el-descriptions>
          <el-divider>修改密码</el-divider>
          <el-form :model="pwdForm" label-width="100px" style="max-width:400px">
            <el-form-item label="旧密码"><el-input v-model="pwdForm.old" type="password" show-password /></el-form-item>
            <el-form-item label="新密码"><el-input v-model="pwdForm.new" type="password" show-password /></el-form-item>
            <el-form-item label="确认密码"><el-input v-model="pwdForm.confirm" type="password" show-password /></el-form-item>
            <el-form-item><el-button type="primary" @click="changePwd">保存修改</el-button></el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 预警阈值 -->
        <el-tab-pane label="预警阈值" name="threshold">
          <el-form label-width="120px" style="max-width:500px">
            <el-form-item label="焦虑触发阈值">
              <el-slider v-model="thresholds.anxiety" :min="0.5" :max="1" :step="0.05" show-input />
            </el-form-item>
            <el-form-item label="低落触发阈值">
              <el-slider v-model="thresholds.sad" :min="0.5" :max="1" :step="0.05" show-input />
            </el-form-item>
            <el-form-item label="愤怒触发阈值">
              <el-slider v-model="thresholds.angry" :min="0.5" :max="1" :step="0.05" show-input />
            </el-form-item>
            <el-form-item><el-button type="primary" @click="saveThresholds">保存配置</el-button></el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 数据隐私 -->
        <el-tab-pane label="数据隐私" name="privacy">
          <el-alert title="数据加密状态：AES-256 已启用 ✅" type="success" :closable="false" style="margin-bottom:16px" />
          <el-form label-width="120px" style="max-width:400px">
            <el-form-item label="数据保留期限">
              <el-input-number v-model="retentionDays" :min="30" :max="3650" />
              <span style="margin-left:8px;color:#909399">天</span>
            </el-form-item>
            <el-form-item>
              <el-button @click="exportData">导出我的数据</el-button>
              <el-button type="danger" @click="deleteData" style="margin-left:8px">删除数据</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 推送方式 -->
        <el-tab-pane label="推送方式" name="notify">
          <el-form label-width="100px" style="max-width:400px">
            <el-form-item label="站内通知"><el-switch v-model="notify.site" /></el-form-item>
            <el-form-item label="短信通知"><el-switch v-model="notify.sms" /></el-form-item>
            <el-form-item label="邮件通知"><el-switch v-model="notify.email" /></el-form-item>
            <el-form-item v-if="notify.email" label="邮件授权码">
              <el-input v-model="notify.smtpKey" type="password" show-password placeholder="请输入邮件授权码" />
            </el-form-item>
            <el-form-item><el-button type="primary" @click="saveNotify">保存设置</el-button></el-form-item>
          </el-form>
          <el-divider />
          <el-descriptions title="版本信息" :column="2">
            <el-descriptions-item label="系统版本">v1.0.0</el-descriptions-item>
            <el-descriptions-item label="发布日期">2026-05-06</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth.js'

const authStore = useAuthStore()
const user = computed(() => authStore.user)
const activeTab = ref('account')
const roleLabel = computed(() => ({ admin:'管理员', caregiver:'护理员', family:'家属' }[user.value?.role] || ''))
const roleTagType = computed(() => ({ admin:'danger', caregiver:'warning', family:'success' }[user.value?.role] || ''))

const pwdForm = reactive({ old: '', new: '', confirm: '' })
const thresholds = reactive({ anxiety: 0.7, sad: 0.6, angry: 0.75 })
const retentionDays = ref(365)
const notify = reactive({ site: true, sms: false, email: false, smtpKey: '' })

function changePwd() {
  if (!pwdForm.old) { ElMessage.warning('请输入旧密码'); return }
  if (pwdForm.new !== pwdForm.confirm) { ElMessage.error('两次密码不一致'); return }
  ElMessage.success('密码修改成功（Mock）')
  Object.assign(pwdForm, { old: '', new: '', confirm: '' })
}

function saveThresholds() { ElMessage.success('预警阈值已保存') }

function exportData() { ElMessage.info('数据导出请求已提交，稍后将生成下载链接（Mock）') }

async function deleteData() {
  await ElMessageBox.confirm('此操作将永久删除所有情感数据，且不可恢复！', '危险操作', { type: 'error', confirmButtonText: '确认删除' })
  ElMessage.success('数据已删除，操作已记录至审计日志（Mock）')
}

function saveNotify() { ElMessage.success('推送设置已保存') }
</script>
