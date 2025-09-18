import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
// import { ElMessage } from 'element-plus'
import router from '@/router'

export interface User {
  id: number
  username: string
  email: string
  nickname: string
  avatar?: string
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('access_token'))
  const refreshToken = ref<string | null>(localStorage.getItem('refresh_token'))
  const user = ref<User | null>(null)

  const isAuthenticated = computed(() => !!token.value)

  const loadUserFromStorage = () => {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      try {
        user.value = JSON.parse(userStr)
      } catch (e) {
        console.error('Failed to parse user from storage', e)
      }
    }
  }

  const login = async (username: string, password: string, rememberMe: boolean = false) => {
    try {
      const response = await authApi.login({ username, password, rememberMe })
      const data = response.data.data
      
      token.value = data.accessToken
      refreshToken.value = data.refreshToken
      user.value = {
        id: data.userId,
        username: data.username,
        email: data.email,
        nickname: data.nickname,
        avatar: data.avatar
      }
      
      // Save to localStorage
      localStorage.setItem('access_token', data.accessToken)
      localStorage.setItem('refresh_token', data.refreshToken)
      localStorage.setItem('user', JSON.stringify(user.value))
      
      console.log('登录成功')
      router.push('/')
      
      return true
    } catch (error: any) {
      console.error('登录失败:', error.response?.data?.message || '登录失败')
      return false
    }
  }

  const register = async (data: any) => {
    try {
      const response = await authApi.register(data)
      const responseData = response.data.data
      
      token.value = responseData.accessToken
      refreshToken.value = responseData.refreshToken
      user.value = {
        id: responseData.userId,
        username: responseData.username,
        email: responseData.email,
        nickname: responseData.nickname,
        avatar: responseData.avatar
      }
      
      // Save to localStorage
      localStorage.setItem('access_token', responseData.accessToken)
      localStorage.setItem('refresh_token', responseData.refreshToken)
      localStorage.setItem('user', JSON.stringify(user.value))
      
      console.log('注册成功')
      router.push('/')
      
      return true
    } catch (error: any) {
      console.error('注册失败:', error.response?.data?.message || '注册失败')
      return false
    }
  }

  const logout = async () => {
    try {
      await authApi.logout()
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      token.value = null
      refreshToken.value = null
      user.value = null
      
      localStorage.removeItem('access_token')
      localStorage.removeItem('refresh_token')
      localStorage.removeItem('user')
      
      router.push('/login')
      console.log('已退出登录')
    }
  }

  const refreshAccessToken = async () => {
    if (!refreshToken.value) {
      logout()
      return null
    }
    
    try {
      const response = await authApi.refreshToken(refreshToken.value)
      const data = response.data.data
      
      token.value = data.accessToken
      localStorage.setItem('access_token', data.accessToken)
      
      return data.accessToken
    } catch (error) {
      logout()
      return null
    }
  }

  // Initialize
  loadUserFromStorage()

  return {
    token,
    refreshToken,
    user,
    isAuthenticated,
    login,
    register,
    logout,
    refreshAccessToken
  }
})