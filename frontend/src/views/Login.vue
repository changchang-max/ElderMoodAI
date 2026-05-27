<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <div class="login-logo">🏠</div>
        <h1>居家老人情感分析及可视化系统</h1>
        <p>ElderMoodAI</p>
      </div>

      <el-tabs v-model="activeTab" class="login-tabs">
        <!-- 手机号登录 -->
        <el-tab-pane label="手机号登录" name="phone">
          <el-form :model="phoneForm" :rules="phoneRules" ref="phoneFormRef" @submit.prevent="loginByPhone">
            <el-form-item prop="phone">
              <el-input v-model="phoneForm.phone" placeholder="请输入手机号" size="large" :prefix-icon="Phone" />
            </el-form-item>
            <el-form-item prop="code">
              <div class="code-row">
                <el-input v-model="phoneForm.code" placeholder="请输入验证码（Mock: 123456）" size="large" />
                <el-button :disabled="countdown > 0" @click="sendCode" size="large" style="width:130px;flex-shrink:0">
                  {{ countdown > 0 ? `${countdown}s后重发` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="loginByPhone">
              登录
            </el-button>
          </el-form>
        </el-tab-pane>

        <!-- 邮箱登录 -->
        <el-tab-pane label="邮箱登录" name="email">
          <el-form :model="emailForm" :rules="emailRules" ref="emailFormRef" @submit.prevent="loginByEmail">
            <el-form-item prop="email">
              <el-input v-model="emailForm.email" placeholder="请输入邮箱" size="large" :prefix-icon="Message" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="emailForm.password" type="password" placeholder="请输入密码" size="large" :prefix-icon="Lock" show-password />
            </el-form-item>
            <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="loginByEmail">
              登录
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div class="demo-accounts">
        <p>演示账号（点击快速填入）：</p>
        <div class="demo-btns">
          <el-button size="small" @click="fillDemo('admin@eldermood.com','123456')">管理员</el-button>
          <el-button size="small" @click="fillDemo('caregiver@eldermood.com','123456')">护理员</el-button>
          <el-button size="small" @click="fillDemo('family@eldermood.com','123456')">家属</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Phone, Message, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth.js'
import axios from 'axios'

const router = useRouter()
const authStore = useAuthStore()
const activeTab = ref('email')
const loading = ref(false)
const countdown = ref(0)

const phoneForm = reactive({ phone: '', code: '' })
const emailForm = reactive({ email: '', password: '' })
const phoneFormRef = ref()
const emailFormRef = ref()

const phoneRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  code:  [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}
const emailRules = {
  email:    [{ required: true, type: 'email', message: '请输入有效邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function fillDemo(email, password) {
  activeTab.value = 'email'
  emailForm.email = email
  emailForm.password = password
}

async function sendCode() {
  if (!phoneForm.phone) { ElMessage.warning('请先输入手机号'); return }
  await axios.post('/api/auth/send-code', { phone: phoneForm.phone })
  ElMessage.success('验证码已发送（Mock：请使用 123456）')
  countdown.value = 60
  const timer = setInterval(() => { if (--countdown.value <= 0) clearInterval(timer) }, 1000)
}

async function loginByPhone() {
  await phoneFormRef.value?.validate()
  loading.value = true
  try {
    const result = await authStore.login(phoneForm, 'phone')
    if (result.success) { ElMessage.success('登录成功'); router.push('/dashboard') }
    else ElMessage.error(result.message)
  } finally { loading.value = false }
}

async function loginByEmail() {
  await emailFormRef.value?.validate()
  loading.value = true
  try {
    const result = await authStore.login(emailForm, 'email')
    if (result.success) { ElMessage.success('登录成功'); router.push('/dashboard') }
    else ElMessage.error(result.message)
  } finally { loading.value = false }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  display: flex; align-items: center; justify-content: center;
}
.login-card {
  background: $card-bg;
  border-radius: $border-radius-lg;
  padding: 40px;
  width: 420px;
  box-shadow: $shadow-lg;
}
.login-header {
  text-align: center; margin-bottom: $spacing-lg;
  .login-logo { font-size: 48px; margin-bottom: $spacing-sm; }
  h1 { font-size: 18px; font-weight: 700; color: $text-primary; line-height: 1.4; }
  p { color: $primary-color; font-size: 13px; margin-top: 4px; }
}
.code-row { display: flex; gap: $spacing-sm; }
.demo-accounts {
  margin-top: $spacing-lg; padding-top: $spacing-md;
  border-top: 1px solid $border-light;
  p { font-size: 12px; color: $text-secondary; margin-bottom: $spacing-sm; }
  .demo-btns { display: flex; gap: $spacing-sm; }
}
</style>
