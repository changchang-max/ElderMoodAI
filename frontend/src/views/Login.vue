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

      <!-- 注册按钮 -->
      <div class="register-section">
        <el-button text @click="showRegisterDialog = true">还没有账号？立即注册</el-button>
      </div>

      <div class="demo-accounts">
        <p>演示账号（点击快速填入）：</p>
        <div class="demo-btns">
          <el-button size="small" @click="fillDemo('admin@eldermood.com','123456')">管理员</el-button>
          <el-button size="small" @click="fillDemo('caregiver@eldermood.com','123456')">护理员</el-button>
          <el-button size="small" @click="fillDemo('family@eldermood.com','123456')">家属</el-button>
        </div>
      </div>
    </div>

    <!-- 注册对话框 -->
    <el-dialog v-model="showRegisterDialog" title="用户注册" width="450px" :close-on-click-modal="false">
      <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名（3-50字符）" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="验证码" prop="verificationCode">
          <div class="code-row">
            <el-input v-model="registerForm.verificationCode" placeholder="请输入验证码" />
            <el-button 
              :disabled="registerCountdown > 0" 
              @click="sendRegisterCode" 
              style="width:120px;flex-shrink:0"
              :loading="sendingCode">
              {{ registerCountdown > 0 ? `${registerCountdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码（6-50字符）" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegisterDialog = false">取消</el-button>
        <el-button type="primary" @click="handleRegister" :loading="registering">注册</el-button>
      </template>
    </el-dialog>
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

// 注册相关状态
const showRegisterDialog = ref(false)
const registerCountdown = ref(0)
const sendingCode = ref(false)
const registering = ref(false)

const phoneForm = reactive({ phone: '', code: '' })
const emailForm = reactive({ email: '', password: '' })
const registerForm = reactive({ 
  username: '', 
  email: '', 
  password: '', 
  confirmPassword: '',
  verificationCode: ''
})

const phoneFormRef = ref()
const emailFormRef = ref()
const registerFormRef = ref()

const phoneRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  code:  [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const emailRules = {
  email:    [{ required: true, type: 'email', message: '请输入有效邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度必须在3-50字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度必须在6-50字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
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

async function sendRegisterCode() {
  // 验证邮箱格式
  if (!registerForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  
  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailPattern.test(registerForm.email)) {
    ElMessage.warning('请输入有效的邮箱地址')
    return
  }

  sendingCode.value = true
  try {
    const response = await axios.post('/api/auth/send-verification-code', { 
      email: registerForm.email 
    })
    
    if (response.data.success) {
      ElMessage.success('验证码已发送，请查收邮箱')
      registerCountdown.value = 60
      const timer = setInterval(() => { 
        if (--registerCountdown.value <= 0) clearInterval(timer) 
      }, 1000)
    } else {
      ElMessage.error(response.data.message || '发送验证码失败')
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
    ElMessage.error(error.response?.data?.message || '发送验证码失败，请稍后重试')
  } finally {
    sendingCode.value = false
  }
}

async function handleRegister() {
  try {
    await registerFormRef.value?.validate()
  } catch (error) {
    return
  }

  registering.value = true
  try {
    const response = await axios.post('/api/auth/register', {
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password,
      verificationCode: registerForm.verificationCode
    })

    if (response.data.success) {
      ElMessage.success('注册成功！请登录')
      showRegisterDialog.value = false
      // 自动填充邮箱到登录表单
      activeTab.value = 'email'
      emailForm.email = registerForm.email
      // 清空注册表单
      Object.assign(registerForm, { 
        username: '', 
        email: '', 
        password: '', 
        confirmPassword: '',
        verificationCode: ''
      })
    } else {
      ElMessage.error(response.data.message || '注册失败')
    }
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error(error.response?.data?.message || '注册失败，请稍后重试')
  } finally {
    registering.value = false
  }
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
.register-section {
  text-align: center;
  margin-top: $spacing-md;
  padding-top: $spacing-md;
  border-top: 1px solid $border-light;
}
.demo-accounts {
  margin-top: $spacing-md; padding-top: $spacing-md;
  border-top: 1px solid $border-light;
  p { font-size: 12px; color: $text-secondary; margin-bottom: $spacing-sm; }
  .demo-btns { display: flex; gap: $spacing-sm; }
}
</style>
