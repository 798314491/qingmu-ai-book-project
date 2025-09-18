import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
// import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

// Create axios instance
const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor
service.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// Response interceptor
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    
    // Success
    if (res.code === 200 || res.code === 0) {
      return response
    }
    
    // Business error
    console.error('Business error:', res.message || 'Error')
    return Promise.reject(new Error(res.message || 'Error'))
  },
  async (error) => {
    const authStore = useAuthStore()
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          // Try to refresh token
          if (authStore.refreshToken) {
            const newToken = await authStore.refreshAccessToken()
            if (newToken) {
              // Retry original request
              error.config.headers.Authorization = `Bearer ${newToken}`
              return service(error.config)
            }
          }
          
          // Failed to refresh or no refresh token
          console.error('登录已过期，请重新登录')
          authStore.logout()
          router.push('/login')
          break
          
        case 403:
          console.error('没有权限访问')
          break
          
        case 404:
          console.error('请求的资源不存在')
          break
          
        case 500:
          console.error('服务器错误')
          break
          
        default:
          console.error(data?.message || `请求失败: ${status}`)
      }
    } else if (error.request) {
      console.error('网络错误，请检查网络连接')
    } else {
      console.error('请求配置错误')
    }
    
    return Promise.reject(error)
  }
)

export default service