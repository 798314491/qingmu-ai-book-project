<template>
  <div class="h-full w-full flex flex-col overflow-hidden chat-container p-4">
    <!-- 头部工具栏 -->
    <div class="flex items-center justify-between mb-4 flex-shrink-0">
      <h3 class="text-lg font-semibold text-gray-900">AI 助手</h3>
      <div class="flex items-center space-x-2">
        <button
          @click="toggleExpanded"
          class="px-3 py-1 text-sm bg-blue-100 text-blue-600 rounded-md hover:bg-blue-200 transition-colors"
          :title="isExpanded ? '收起' : '完全展开'"
        >
          {{ isExpanded ? '收起' : '展开' }}
        </button>
      </div>
    </div>

    <!-- 聊天历史 -->
    <div class="flex-1 overflow-y-auto mb-4 space-y-4 min-h-0 scrollbar-thin chat-messages">
      <div v-if="messages.length === 0" class="text-center py-8">
        <!-- AI文字标识 -->
        <div class="mx-auto h-16 w-16 bg-gradient-to-br from-blue-500 to-purple-600 rounded-full flex items-center justify-center mb-4">
          <span class="text-white font-bold text-xl">AI</span>
        </div>
        <p class="text-sm text-gray-500 mb-4">AI助手已准备就绪</p>
        <div class="space-y-2">
          <button
            @click="sendQuickMessage('润色文字')"
            class="block w-full text-left px-3 py-2 text-sm bg-gray-50 hover:bg-gray-100 rounded-md"
            :disabled="loading"
          >
            💡 润色文字
          </button>
          <button
            @click="sendQuickMessage('内容总结')"
            class="block w-full text-left px-3 py-2 text-sm bg-gray-50 hover:bg-gray-100 rounded-md"
            :disabled="loading"
          >
            📝 内容总结
          </button>
          <button
            @click="sendQuickMessage('翻译成英文')"
            class="block w-full text-left px-3 py-2 text-sm bg-gray-50 hover:bg-gray-100 rounded-md"
          >
            🌐 翻译文本
          </button>
          <button
            @click="sendQuickMessage('解释这段代码')"
            class="block w-full text-left px-3 py-2 text-sm bg-gray-50 hover:bg-gray-100 rounded-md"
          >
            💻 代码解释
          </button>
        </div>
      </div>
      
      <!-- 消息列表 -->
      <div
        v-for="message in messages"
        :key="message.id"
        :class="[
          'flex',
          message.role === 'user' ? 'justify-end' : 'justify-start'
        ]"
      >
        <div
          :class="[
            'max-w-xs lg:max-w-sm xl:max-w-md px-4 py-2 rounded-lg text-sm break-words',
            message.role === 'user'
              ? 'bg-blue-600 text-white'
              : 'bg-gray-100 text-gray-900'
          ]"
        >
          <div v-if="message.role === 'assistant' && message.loading && !message.content" class="flex items-center space-x-2">
            <div class="flex space-x-1">
              <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
              <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0.1s"></div>
              <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0.2s"></div>
            </div>
            <span>AI正在思考...</span>
          </div>
          <div v-else class="whitespace-pre-wrap">{{ message.content }}</div>
          
          <!-- 消息操作 -->
          <div v-if="message.role === 'assistant' && !message.loading" class="mt-2 flex space-x-2">
            <button
              @click="copyToClipboard(message.content)"
              class="text-xs text-gray-500 hover:text-gray-700"
              title="复制"
            >
              📋
            </button>
            <button
              @click="insertToNote(message.content)"
              class="text-xs text-gray-500 hover:text-gray-700"
              title="插入到笔记"
            >
              📝
            </button>
          </div>
        </div>
      </div>
    </div>


    <!-- 输入框 -->
    <div class="flex-shrink-0 mt-auto pt-4">
      <div class="mb-2">
        <textarea
          v-model="inputMessage"
          placeholder="输入消息... (按回车发送)"
          class="w-full px-3 py-2 border border-gray-300 rounded-md resize-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm"
          rows="3"
          @keydown.enter="handleEnterKey"
        ></textarea>
        <p class="text-xs text-gray-500 mt-1">按回车发送消息</p>
      </div>
      <div class="flex justify-end">
        <button
          @click="sendMessage"
          :disabled="!inputMessage.trim() || loading"
          class="px-4 py-2 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {{ loading ? '发送中...' : '发送' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { aiApi, type AiChatRequest } from '@/api/ai'

interface Props {}

interface Message {
  id: number
  role: 'user' | 'assistant'
  content: string
  loading?: boolean
  timestamp: Date
}

defineProps<Props>()
const emit = defineEmits<{
  'insert-text': [text: string]
  'toggle-expanded': []
}>()

// 展开状态
const isExpanded = ref(false)

const messages = ref<Message[]>([])
const inputMessage = ref('')
const loading = ref(false)
const messageIdCounter = ref(0)

// 计算属性

// 方法
const toggleExpanded = () => {
  isExpanded.value = !isExpanded.value
  emit('toggle-expanded')
}

const handleEnterKey = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || loading.value) return

  const userMessage: Message = {
    id: ++messageIdCounter.value,
    role: 'user',
    content: inputMessage.value,
    timestamp: new Date()
  }

  const assistantMessage: Message = {
    id: ++messageIdCounter.value,
    role: 'assistant',
    content: '',
    loading: true,
    timestamp: new Date()
  }

  messages.value.push(userMessage, assistantMessage)
  const currentMessage = inputMessage.value
  inputMessage.value = ''
  loading.value = true

  try {
    const chatRequest: AiChatRequest = {
      message: currentMessage,
      context: '',
      type: 'chat'
    }
    
    // 使用流式API
    await aiApi.streamChat(
      chatRequest,
      // onMessage: 接收流式数据
      (content: string) => {
        console.log(`[${new Date().toLocaleTimeString()}] 收到流式数据:`, content)
        
        // 强制触发Vue响应式更新
        const newContent = assistantMessage.content + content
        assistantMessage.content = newContent
        
        console.log(`[${new Date().toLocaleTimeString()}] 更新后内容长度:`, assistantMessage.content.length)
        
        // 强制触发响应式更新
        messages.value = [...messages.value]
        
        nextTick(() => {
          scrollToBottom()
        })
      },
      // onComplete: 完成回调
      () => {
        assistantMessage.loading = false
        loading.value = false
      },
      // onError: 错误回调
      (error: string) => {
        assistantMessage.loading = false
        assistantMessage.content = `❌ 请求失败：${error}`
        loading.value = false
      }
    )
    
  } catch (error: any) {
    console.error('AI请求失败:', error)
    console.error('错误详情:', {
      message: error.message,
      response: error.response,
      request: error.request
    })
    
    // 确保loading状态被清除
    assistantMessage.loading = false
    
    // 详细的错误处理
    if (error.response) {
      // 服务器返回了错误状态码
      const status = error.response.status
      const errorMsg = error.response.data?.message || error.response.data || '服务器错误'
      
      if (status === 401) {
        assistantMessage.content = '请先登录后再使用AI功能。'
      } else if (status === 403) {
        assistantMessage.content = '没有权限使用AI功能，请检查账号设置。'
      } else if (status === 500) {
        assistantMessage.content = `服务器内部错误：${errorMsg}`
      } else {
        assistantMessage.content = `请求失败 (${status})：${errorMsg}`
      }
    } else if (error.request) {
      // 请求发出但没有收到响应（网络问题、CORS等）
      assistantMessage.content = '网络连接失败，请检查：\n1. 网络连接是否正常\n2. 服务器是否启动\n3. 跨域配置是否正确'
    } else {
      // 其他错误
      assistantMessage.content = `请求配置错误：${error.message}`
    }
  } finally {
    // 确保所有状态都被重置
    loading.value = false
    assistantMessage.loading = false
    
    await nextTick()
    scrollToBottom()
  }
}

const sendQuickMessage = (message: string) => {
  if (loading.value) {
    console.warn('AI正在处理请求，请等待...')
    return
  }
  inputMessage.value = message
  sendMessage()
}


const copyToClipboard = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    // TODO: 显示复制成功提示
  } catch (error) {
    console.error('复制失败:', error)
  }
}

const insertToNote = (text: string) => {
  emit('insert-text', text)
}



const scrollToBottom = () => {
  // 滚动到底部的逻辑
}



</script>

<style scoped>
.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

.animate-bounce {
  animation: bounce 1.4s infinite;
}

/* 自定义滚动条样式 */
.scrollbar-thin {
  scrollbar-width: thin;
}

.scrollbar-thin::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.scrollbar-thin::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 3px;
}

.scrollbar-thin::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
  transition: background-color 0.2s ease;
}

.scrollbar-thin::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

.scrollbar-thin::-webkit-scrollbar-thumb:active {
  background: #64748b;
}

/* 确保聊天区域有足够的高度 */
.chat-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}
</style>
