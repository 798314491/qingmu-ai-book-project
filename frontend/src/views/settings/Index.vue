<template>
  <div class="px-4 sm:px-6 lg:px-8">
    <div class="max-w-4xl mx-auto">
      <!-- 页面标题 -->
      <div class="mb-8">
        <h1 class="text-2xl font-semibold text-gray-900">设置</h1>
        <p class="mt-2 text-sm text-gray-700">
          管理您的账户设置和偏好
        </p>
      </div>

      <!-- 设置表单 -->
      <div class="space-y-6">
        <!-- 个人信息 -->
        <div class="bg-white shadow rounded-lg">
          <div class="px-4 py-5 sm:p-6">
            <h3 class="text-lg font-medium text-gray-900 mb-4">个人信息</h3>
            
            <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
              <div>
                <label for="username" class="block text-sm font-medium text-gray-700">用户名</label>
                <input
                  id="username"
                  v-model="settings.username"
                  type="text"
                  class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
                  @input="handleSettingChange"
                />
              </div>
              
              <div>
                <label for="email" class="block text-sm font-medium text-gray-700">邮箱</label>
                <input
                  id="email"
                  v-model="settings.email"
                  type="email"
                  class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
                  @input="handleSettingChange"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- 编辑器设置 -->
        <div class="bg-white shadow rounded-lg">
          <div class="px-4 py-5 sm:p-6">
            <h3 class="text-lg font-medium text-gray-900 mb-4">编辑器设置</h3>
            
            <div class="space-y-4">
              <div>
                <label for="theme" class="block text-sm font-medium text-gray-700">主题</label>
                <select
                  id="theme"
                  v-model="settings.theme"
                  class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
                  @change="handleThemeChange"
                >
                  <option value="light">浅色</option>
                  <option value="dark">深色</option>
                  <option value="auto">自动</option>
                </select>
              </div>
              
              <div>
                <label for="fontSize" class="block text-sm font-medium text-gray-700">字体大小</label>
                <select
                  id="fontSize"
                  v-model="settings.fontSize"
                  class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
                  @change="handleSettingChange"
                >
                  <option value="12">12px</option>
                  <option value="14">14px</option>
                  <option value="16">16px</option>
                  <option value="18">18px</option>
                  <option value="20">20px</option>
                </select>
              </div>
              
              <div>
                <label for="fontFamily" class="block text-sm font-medium text-gray-700">字体</label>
                <select
                  id="fontFamily"
                  v-model="settings.fontFamily"
                  class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
                  @change="handleSettingChange"
                >
                  <option value="system">系统默认</option>
                  <option value="mono">等宽字体</option>
                  <option value="serif">衬线字体</option>
                </select>
              </div>
            </div>
          </div>
        </div>

        <!-- 偏好设置 -->
        <div class="bg-white shadow rounded-lg">
          <div class="px-4 py-5 sm:p-6">
            <h3 class="text-lg font-medium text-gray-900 mb-4">偏好设置</h3>
            
            <div class="space-y-4">
              <div class="flex items-center justify-between">
                <div>
                  <label for="autoSave" class="text-sm font-medium text-gray-700">自动保存</label>
                  <p class="text-sm text-gray-500">编辑时自动保存笔记</p>
                </div>
                <input
                  id="autoSave"
                  v-model="settings.autoSave"
                  type="checkbox"
                  class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  @change="handleSettingChange"
                />
              </div>
              
              <div class="flex items-center justify-between">
                <div>
                  <label for="showLineNumbers" class="text-sm font-medium text-gray-700">显示行号</label>
                  <p class="text-sm text-gray-500">在编辑器中显示行号</p>
                </div>
                <input
                  id="showLineNumbers"
                  v-model="settings.showLineNumbers"
                  type="checkbox"
                  class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  @change="handleSettingChange"
                />
              </div>
              
              <div class="flex items-center justify-between">
                <div>
                  <label for="wordWrap" class="text-sm font-medium text-gray-700">自动换行</label>
                  <p class="text-sm text-gray-500">长行自动换行显示</p>
                </div>
                <input
                  id="wordWrap"
                  v-model="settings.wordWrap"
                  type="checkbox"
                  class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  @change="handleSettingChange"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- 密码修改 -->
        <div class="bg-white shadow rounded-lg">
          <div class="px-4 py-5 sm:p-6">
            <h3 class="text-lg font-medium text-gray-900 mb-4">修改密码</h3>
            
            <form @submit.prevent="changePassword" class="space-y-4">
              <div>
                <label for="currentPassword" class="block text-sm font-medium text-gray-700">当前密码</label>
                <input
                  id="currentPassword"
                  v-model="passwordForm.currentPassword"
                  type="password"
                  class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
                />
              </div>
              
              <div>
                <label for="newPassword" class="block text-sm font-medium text-gray-700">新密码</label>
                <input
                  id="newPassword"
                  v-model="passwordForm.newPassword"
                  type="password"
                  class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
                />
              </div>
              
              <div>
                <label for="confirmPassword" class="block text-sm font-medium text-gray-700">确认新密码</label>
                <input
                  id="confirmPassword"
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
                />
              </div>
              
              <div>
                <button
                  type="submit"
                  :disabled="changingPassword"
                  class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
                >
                  {{ changingPassword ? '修改中...' : '修改密码' }}
                </button>
              </div>
            </form>
          </div>
        </div>

        <!-- 保存按钮 -->
        <div class="flex justify-end">
          <button
            @click="saveSettings"
            :disabled="saving"
            class="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
          >
            {{ saving ? '保存中...' : '保存设置' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()

// 响应式数据
const settings = ref({
  username: '',
  email: '',
  theme: 'light',
  fontSize: '14',
  fontFamily: 'system',
  autoSave: true,
  showLineNumbers: false,
  wordWrap: true
})

const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const saving = ref(false)
const changingPassword = ref(false)

// 方法
const loadSettings = async () => {
  try {
    // TODO: 调用API获取用户设置
    // const response = await api.getUserSettings()
    // settings.value = response.data
    
    // 模拟数据
    settings.value = {
      username: 'user',
      email: 'user@example.com',
      theme: 'light',
      fontSize: '14',
      fontFamily: 'system',
      autoSave: true,
      showLineNumbers: false,
      wordWrap: true
    }
  } catch (error) {
    console.error('加载设置失败:', error)
  }
}

const saveSettings = async () => {
  if (saving.value) return
  
  saving.value = true
  try {
    // TODO: 调用API保存设置
    // await api.updateUserSettings(settings.value)
    console.log('保存设置:', settings.value)
    
    // 模拟延迟
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    alert('设置保存成功！')
  } catch (error) {
    console.error('保存设置失败:', error)
    alert('保存设置失败，请重试')
  } finally {
    saving.value = false
  }
}

const changePassword = async () => {
  if (changingPassword.value) return
  
  if (!passwordForm.value.currentPassword || !passwordForm.value.newPassword) {
    alert('请填写完整的密码信息')
    return
  }
  
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    alert('两次输入的新密码不一致')
    return
  }
  
  changingPassword.value = true
  try {
    // TODO: 调用API修改密码
    // await api.changePassword(passwordForm.value)
    
    // 模拟延迟
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    alert('密码修改成功！')
    passwordForm.value = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    alert('修改密码失败，请重试')
  } finally {
    changingPassword.value = false
  }
}

const handleThemeChange = () => {
  themeStore.setTheme(settings.value.theme)
  handleSettingChange()
}

const handleSettingChange = () => {
  // 可以在这里添加实时保存逻辑
  console.log('设置已更改:', settings.value)
}

// 生命周期
onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
/* 可以添加自定义样式 */
</style>
