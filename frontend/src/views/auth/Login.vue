<template>
  <div class="min-h-screen w-screen bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center px-4 sm:px-6 lg:px-8 overflow-y-auto">
    <div class="max-w-md w-full space-y-8 my-8">
      <!-- 头部 -->
      <div class="text-center">
        <div class="mx-auto h-16 w-16 bg-white rounded-xl flex items-center justify-center shadow-lg mb-4">
          <svg class="h-8 w-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
        </div>
        <h2 class="text-3xl font-bold text-white mb-2">Markdown Notes</h2>
        <p class="text-blue-100">优雅的在线笔记系统</p>
      </div>
      
      <!-- 登录表单 -->
      <div class="bg-white rounded-2xl shadow-2xl p-8 space-y-6">
        <div>
          <h3 class="text-2xl font-semibold text-gray-900 text-center mb-6">欢迎回来</h3>
        </div>

        <form @submit.prevent="handleLogin" class="space-y-6">
          <!-- 用户名/邮箱输入 -->
          <div>
            <label for="username" class="block text-sm font-medium text-gray-700 mb-2">
              用户名或邮箱
            </label>
            <div class="relative">
              <input
                id="username"
            v-model="loginForm.username"
                type="text"
                required
                class="w-full px-4 py-3 pl-12 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                placeholder="请输入用户名或邮箱"
              />
              <svg class="absolute left-4 top-3.5 h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
            </div>
            <div v-if="errors.username" class="mt-1 text-sm text-red-600">{{ errors.username }}</div>
          </div>

          <!-- 密码输入 -->
          <div>
            <label for="password" class="block text-sm font-medium text-gray-700 mb-2">
              密码
            </label>
            <div class="relative">
              <input
                id="password"
            v-model="loginForm.password"
                :type="showPassword ? 'text' : 'password'"
                required
                class="w-full px-4 py-3 pl-12 pr-12 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                placeholder="请输入密码"
              />
              <svg class="absolute left-4 top-3.5 h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
              </svg>
              <button
                type="button"
                @click="showPassword = !showPassword"
                class="absolute right-4 top-3.5 text-gray-400 hover:text-gray-600 transition-colors"
              >
                <svg v-if="showPassword" class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
                <svg v-else class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21" />
                </svg>
              </button>
            </div>
            <div v-if="errors.password" class="mt-1 text-sm text-red-600">{{ errors.password }}</div>
          </div>

          <!-- 验证码 -->
          <div>
            <label for="captcha" class="block text-sm font-medium text-gray-700 mb-2">
              验证码
            </label>
            <div class="flex space-x-3">
              <div class="flex-1">
                <input
                  id="captcha"
                  v-model="loginForm.captcha"
                  type="text"
                  required
                  class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                  placeholder="请输入验证码"
                />
                <div v-if="errors.captcha" class="mt-1 text-sm text-red-600">{{ errors.captcha }}</div>
              </div>
              <div class="flex-shrink-0">
                <Captcha ref="captchaRef" @change="handleCaptchaChange" />
              </div>
            </div>
          </div>

          <!-- 选项 -->
          <div class="flex items-center justify-between">
            <label class="flex items-center">
              <input
                v-model="loginForm.rememberMe"
                type="checkbox"
                class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <span class="ml-2 text-sm text-gray-700">记住我</span>
            </label>
            <a href="#" class="text-sm text-blue-600 hover:text-blue-500">忘记密码？</a>
          </div>


          <!-- 错误提示 -->
          <div v-if="errorMessage" class="bg-red-50 border border-red-200 rounded-lg p-3">
            <div class="flex">
              <svg class="h-5 w-5 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <div class="ml-3">
                <p class="text-sm text-red-800">{{ errorMessage }}</p>
              </div>
            </div>
          </div>

          <!-- 登录按钮 -->
          <button
            type="submit"
            :disabled="loading"
            class="w-full flex justify-center py-3 px-4 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200"
          >
            <svg v-if="loading" class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            {{ loading ? '登录中...' : '登录' }}
          </button>

          <!-- 分割线 -->
          <div class="relative">
            <div class="absolute inset-0 flex items-center">
              <div class="w-full border-t border-gray-300"></div>
            </div>
            <div class="relative flex justify-center text-sm">
              <span class="px-2 bg-white text-gray-500">或</span>
            </div>
        </div>
        
          <!-- 注册按钮 -->
          <button
            type="button"
            @click="$router.push('/register')"
            class="w-full flex justify-center py-3 px-4 border-2 border-gray-300 rounded-lg shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors"
          >
            创建新账号
          </button>
        </form>

        <!-- 底部 -->
        <div class="text-center">
          <p class="text-xs text-gray-500">© 2024 Markdown Notes. All rights reserved.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import Captcha from '@/components/Captcha.vue'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const showPassword = ref(false)
const errorMessage = ref('')
const captchaRef = ref()
const captchaCode = ref('')

const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
  rememberMe: false
})

const errors = reactive({
  username: '',
  password: '',
  captcha: ''
})

const handleCaptchaChange = (code: string) => {
  captchaCode.value = code
}

const validateForm = () => {
  errors.username = ''
  errors.password = ''
  errors.captcha = ''
  
  if (!loginForm.username.trim()) {
    errors.username = '请输入用户名或邮箱'
    return false
  }
  
  if (!loginForm.password.trim()) {
    errors.password = '请输入密码'
    return false
  }
  
  if (loginForm.password.length < 6) {
    errors.password = '密码长度不能少于6位'
    return false
  }
  
  if (!loginForm.captcha.trim()) {
    errors.captcha = '请输入验证码'
    return false
  }
  
  if (!captchaRef.value?.verify(loginForm.captcha)) {
    errors.captcha = '验证码错误'
    captchaRef.value?.refresh()
    loginForm.captcha = ''
    return false
  }
  
  return true
}

const handleLogin = async () => {
  if (!validateForm()) return
  
  loading.value = true
  errorMessage.value = ''
  
  try {
    const success = await authStore.login(
      loginForm.username, 
      loginForm.password, 
      loginForm.rememberMe
    )
    
    if (success) {
      const redirect = router.currentRoute.value.query.redirect as string
      router.push(redirect || '/notes')
    } else {
      errorMessage.value = '用户名或密码错误'
    }
  } catch (error: any) {
    errorMessage.value = error.message || '登录失败，请重试'
  } finally {
    loading.value = false
  }
}

</script>

<style scoped>
/* 自定义样式 */
.bg-gradient-to-br {
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
}

.bg-gradient-to-r {
  background: linear-gradient(90deg, #3b82f6 0%, #8b5cf6 100%);
}

.hover\:from-blue-700:hover {
  background: linear-gradient(90deg, #1d4ed8 0%, #7c3aed 100%);
}

input:focus {
  outline: none;
}

.animate-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>