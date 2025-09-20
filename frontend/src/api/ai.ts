import request from './request'
import { useAuthStore } from '@/stores/auth'

export interface AiChatRequest {
  message: string
  context?: string
  conversationId?: string
  type?: string
}

export interface AiChatResponse {
  id: string
  conversationId?: string
  message: string
  response: string
  type: string
  timestamp: string
  userId: number
}

// 降级到普通API的辅助函数
const fallbackToNormalChat = async (
  data: AiChatRequest,
  onMessage: (content: string) => void,
  onComplete: () => void,
  onError: (error: string) => void
) => {
  try {
    const response = await aiApi.chat(data)
    console.log('普通API响应:', response)
    
    // 检查响应结构并提取内容
    let content = ''
    if (response?.data?.data?.response) {
      content = response.data.data.response
    } else if (response?.data?.response) {
      content = response.data.response
    } else if (response?.data?.data) {
      content = typeof response.data.data === 'string' ? response.data.data : JSON.stringify(response.data.data)
    } else {
      onError('收到了AI回复，但格式异常。请查看控制台日志。')
      return
    }
    
    // 模拟流式输出效果
    const words = content.split('')
    for (let i = 0; i < words.length; i++) {
      onMessage(words[i])
      // 添加小延迟模拟流式效果
      await new Promise(resolve => setTimeout(resolve, 20))
    }
    
    onComplete()
  } catch (error: any) {
    console.error('降级API请求失败:', error)
    onError(error.message || '请求失败')
  }
}

const fallbackToNormalEnhance = async (
  text: string,
  onMessage: (content: string) => void,
  onComplete: () => void,
  onError: (error: string) => void
) => {
  try {
    const response = await aiApi.enhanceText(text)
    console.log('润色普通API响应:', response)
    
    let content = ''
    if (response?.data?.data) {
      content = typeof response.data.data === 'string' ? response.data.data : JSON.stringify(response.data.data)
    } else if (response?.data) {
      content = typeof response.data === 'string' ? response.data : JSON.stringify(response.data)
    } else {
      onError('润色完成，但响应格式异常。请查看控制台了解详情。')
      return
    }
    
    // 模拟流式输出效果
    const words = content.split('')
    for (let i = 0; i < words.length; i++) {
      onMessage(words[i])
      await new Promise(resolve => setTimeout(resolve, 20))
    }
    
    onComplete()
  } catch (error: any) {
    console.error('润色降级API请求失败:', error)
    onError(error.message || '润色失败')
  }
}

const fallbackToNormalSummarize = async (
  content: string,
  onMessage: (content: string) => void,
  onComplete: () => void,
  onError: (error: string) => void
) => {
  try {
    const response = await aiApi.summarizeContent(content)
    console.log('总结普通API响应:', response)
    
    let summary = ''
    if (response?.data?.data) {
      summary = typeof response.data.data === 'string' ? response.data.data : JSON.stringify(response.data.data)
    } else if (response?.data) {
      summary = typeof response.data === 'string' ? response.data : JSON.stringify(response.data)
    } else {
      onError('总结完成，但响应格式异常。请查看控制台了解详情。')
      return
    }
    
    // 模拟流式输出效果
    const words = summary.split('')
    for (let i = 0; i < words.length; i++) {
      onMessage(words[i])
      await new Promise(resolve => setTimeout(resolve, 20))
    }
    
    onComplete()
  } catch (error: any) {
    console.error('总结降级API请求失败:', error)
    onError(error.message || '总结失败')
  }
}

export const aiApi = {
  // AI对话
  chat(data: AiChatRequest) {
    return request({
      url: '/ai/chat',
      method: 'post',
      data
    })
  },

  // 流式AI对话
  async streamChat(
    data: AiChatRequest,
    onMessage: (content: string) => void,
    onComplete: () => void,
    onError: (error: string) => void
  ) {
    try {
      // 获取认证token
      const authStore = useAuthStore()
      const token = authStore.token
      
      console.log('Auth store token:', token ? token.substring(0, 20) + '...' : 'null')
      console.log('Auth store isAuthenticated:', authStore.isAuthenticated)
      
      if (!token) {
        console.error('没有找到认证token')
        onError('请先登录')
        return
      }

      // 首先尝试流式API
      try {
        console.log('发送流式聊天请求，Token:', token ? token.substring(0, 20) + '...' : 'null')
        console.log('请求数据:', data)
        
        const response = await fetch('/api/ai/chat/stream', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify(data)
        })
        
        console.log('流式聊天响应状态:', response.status)
        console.log('响应头:', [...response.headers.entries()])

        if (!response.ok) {
          if (response.status === 404) {
            // 流式API不存在，降级到普通API
            console.log('流式API不可用，降级到普通API')
            await fallbackToNormalChat(data, onMessage, onComplete, onError)
            return
          } else if (response.status === 401) {
            onError('请先登录')
            return
          } else if (response.status === 403) {
            onError('没有权限使用AI功能')
            return
          } else {
            onError(`请求失败: ${response.status}`)
            return
          }
        }

        // 使用fetch响应流处理SSE数据
        const reader = response.body?.getReader()
        if (!reader) {
          onError('无法读取响应流')
          return
        }

        const decoder = new TextDecoder()
        let buffer = ''

        while (true) {
          const { done, value } = await reader.read()
          
          if (done) {
            onComplete()
            break
          }

          buffer += decoder.decode(value, { stream: true })
          const lines = buffer.split('\n')
          buffer = lines.pop() || ''

          for (const line of lines) {
            if (line.trim() === '') continue
            
            console.log('收到SSE行数据:', line)
            
            // 跳过event行，只处理data行
            if (line.startsWith('event:')) {
              console.log('跳过event行:', line)
              continue
            }
            
            // 只处理data行
            if (line.startsWith('data:')) {
              const jsonData = line.substring(5); // 移除 'data:' 前缀
              
              try {
                const parsed = JSON.parse(jsonData)
                console.log('解析的JSON数据:', parsed)
                if (parsed.type === 'content') {
                  console.log('发送内容到UI:', parsed.content)
                  onMessage(parsed.content)
                } else if (parsed.type === 'error') {
                  console.error('收到错误:', parsed.error)
                  onError(parsed.error)
                  return
                } else if (parsed.type === 'done') {
                  console.log('收到完成信号')
                  onComplete()
                  return
                }
              } catch (error) {
                console.error('解析流式数据失败:', error, '原始数据:', line)
              }
            }
          }
        }
      } catch (fetchError: any) {
        if (fetchError.message.includes('404') || fetchError.message.includes('Not Found')) {
          // 流式API不存在，降级到普通API
          console.log('流式API不可用，降级到普通API')
          await fallbackToNormalChat(data, onMessage, onComplete, onError)
        } else {
          throw fetchError
        }
      }
    } catch (error: any) {
      console.error('流式聊天请求失败:', error)
      onError(error.message || '网络错误')
    }
  },

  // 文字润色
  enhanceText(text: string) {
    return request({
      url: '/ai/enhance',
      method: 'post',
      data: text,
      headers: {
        'Content-Type': 'text/plain'
      }
    })
  },

  // 流式文字润色
  async streamEnhanceText(
    text: string,
    onMessage: (content: string) => void,
    onComplete: () => void,
    onError: (error: string) => void
  ) {
    try {
      // 获取认证token
      const authStore = useAuthStore()
      const token = authStore.token
      
      if (!token) {
        onError('请先登录')
        return
      }

      try {
        const response = await fetch('/api/ai/enhance/stream', {
          method: 'POST',
          headers: {
            'Content-Type': 'text/plain',
            'Authorization': `Bearer ${token}`
          },
          body: text
        })

        if (!response.ok) {
          if (response.status === 404) {
            // 流式API不存在，降级到普通API
            console.log('润色流式API不可用，降级到普通API')
            await fallbackToNormalEnhance(text, onMessage, onComplete, onError)
            return
          } else if (response.status === 401) {
            onError('请先登录')
          } else if (response.status === 403) {
            onError('没有权限使用润色功能')
          } else {
            onError(`请求失败: ${response.status}`)
          }
          return
        }

        const reader = response.body?.getReader()
        if (!reader) {
          onError('无法读取响应流')
          return
        }

        const decoder = new TextDecoder()
        let buffer = ''

        while (true) {
          const { done, value } = await reader.read()
          
          if (done) {
            onComplete()
            break
          }

          buffer += decoder.decode(value, { stream: true })
          const lines = buffer.split('\n')
          buffer = lines.pop() || ''

          for (const line of lines) {
            if (line.trim() === '') continue
            
            // 跳过event行，只处理data行
            if (line.startsWith('event:')) {
              continue
            }
            
            // 只处理data行
            if (line.startsWith('data:')) {
              const jsonData = line.substring(5); // 移除 'data:' 前缀
              
              try {
                const parsed = JSON.parse(jsonData)
                if (parsed.type === 'content') {
                  onMessage(parsed.content)
                } else if (parsed.type === 'error') {
                  onError(parsed.error)
                  return
                } else if (parsed.type === 'done') {
                  onComplete()
                  return
                }
              } catch (error) {
                console.error('解析润色流式数据失败:', error, '原始数据:', line)
              }
            }
          }
        }
      } catch (fetchError: any) {
        if (fetchError.message.includes('404') || fetchError.message.includes('Not Found')) {
          // 流式API不存在，降级到普通API
          console.log('润色流式API不可用，降级到普通API')
          await fallbackToNormalEnhance(text, onMessage, onComplete, onError)
        } else {
          throw fetchError
        }
      }
    } catch (error: any) {
      console.error('流式润色请求失败:', error)
      onError(error.message || '网络错误')
    }
  },

  // 内容总结
  summarizeContent(content: string) {
    return request({
      url: '/ai/summarize',
      method: 'post',
      data: content,
      headers: {
        'Content-Type': 'text/plain'
      }
    })
  },

  // 流式内容总结
  async streamSummarizeContent(
    content: string,
    onMessage: (content: string) => void,
    onComplete: () => void,
    onError: (error: string) => void
  ) {
    try {
      // 获取认证token
      const authStore = useAuthStore()
      const token = authStore.token
      
      if (!token) {
        onError('请先登录')
        return
      }

      try {
        const response = await fetch('/api/ai/summarize/stream', {
          method: 'POST',
          headers: {
            'Content-Type': 'text/plain',
            'Authorization': `Bearer ${token}`
          },
          body: content
        })

        if (!response.ok) {
          if (response.status === 404) {
            // 流式API不存在，降级到普通API
            console.log('总结流式API不可用，降级到普通API')
            await fallbackToNormalSummarize(content, onMessage, onComplete, onError)
            return
          } else if (response.status === 401) {
            onError('请先登录')
          } else if (response.status === 403) {
            onError('没有权限使用总结功能')
          } else {
            onError(`请求失败: ${response.status}`)
          }
          return
        }

        const reader = response.body?.getReader()
        if (!reader) {
          onError('无法读取响应流')
          return
        }

        const decoder = new TextDecoder()
        let buffer = ''

        while (true) {
          const { done, value } = await reader.read()
          
          if (done) {
            onComplete()
            break
          }

          buffer += decoder.decode(value, { stream: true })
          const lines = buffer.split('\n')
          buffer = lines.pop() || ''

          for (const line of lines) {
            if (line.trim() === '') continue
            
            // 跳过event行，只处理data行
            if (line.startsWith('event:')) {
              continue
            }
            
            // 只处理data行
            if (line.startsWith('data:')) {
              const jsonData = line.substring(5); // 移除 'data:' 前缀
              
              try {
                const parsed = JSON.parse(jsonData)
                if (parsed.type === 'content') {
                  onMessage(parsed.content)
                } else if (parsed.type === 'error') {
                  onError(parsed.error)
                  return
                } else if (parsed.type === 'done') {
                  onComplete()
                  return
                }
              } catch (error) {
                console.error('解析总结流式数据失败:', error, '原始数据:', line)
              }
            }
          }
        }
      } catch (fetchError: any) {
        if (fetchError.message.includes('404') || fetchError.message.includes('Not Found')) {
          // 流式API不存在，降级到普通API
          console.log('总结流式API不可用，降级到普通API')
          await fallbackToNormalSummarize(content, onMessage, onComplete, onError)
        } else {
          throw fetchError
        }
      }
    } catch (error: any) {
      console.error('流式总结请求失败:', error)
      onError(error.message || '网络错误')
    }
  },

  // 文本翻译
  translateText(text: string, targetLanguage: string) {
    return request({
      url: '/ai/translate',
      method: 'post',
      params: { text, targetLanguage }
    })
  },

  // 获取对话历史
  getConversations() {
    return request({
      url: '/ai/conversations',
      method: 'get'
    })
  },

  // 清空对话历史
  clearConversations() {
    return request({
      url: '/ai/conversations',
      method: 'delete'
    })
  }
}
