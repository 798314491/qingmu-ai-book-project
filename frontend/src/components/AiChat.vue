<template>
  <div class="h-full flex flex-col">
    <!-- 聊天历史 -->
    <div class="flex-1 overflow-y-auto mb-4 space-y-4">
      <div v-if="messages.length === 0" class="text-center py-8">
        <svg class="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
        </svg>
        <p class="text-sm text-gray-500 mb-4">AI助手已准备就绪</p>
        <div class="space-y-2">
          <button
            @click="sendQuickMessage('帮我润色这段文字')"
            class="block w-full text-left px-3 py-2 text-sm bg-gray-50 hover:bg-gray-100 rounded-md"
          >
            💡 润色文字
          </button>
          <button
            @click="sendQuickMessage('总结一下这段内容')"
            class="block w-full text-left px-3 py-2 text-sm bg-gray-50 hover:bg-gray-100 rounded-md"
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
            'max-w-xs lg:max-w-md px-4 py-2 rounded-lg text-sm',
            message.role === 'user'
              ? 'bg-blue-600 text-white'
              : 'bg-gray-100 text-gray-900'
          ]"
        >
          <div v-if="message.role === 'assistant' && message.loading" class="flex items-center space-x-2">
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

    <!-- 选中文本提示 -->
    <div v-if="selectedText" class="mb-3 p-3 bg-yellow-50 border border-yellow-200 rounded-md">
      <div class="flex items-start justify-between">
        <div class="flex-1">
          <p class="text-xs text-yellow-800 mb-1">选中的文本：</p>
          <p class="text-sm text-yellow-900 line-clamp-3">{{ selectedText }}</p>
        </div>
        <button
          @click="clearSelectedText"
          class="ml-2 text-yellow-600 hover:text-yellow-800"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </div>

    <!-- 输入框 -->
    <div class="flex space-x-2">
      <div class="flex-1">
        <textarea
          v-model="inputMessage"
          placeholder="输入消息..."
          class="w-full px-3 py-2 border border-gray-300 rounded-md resize-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          rows="3"
          @keydown.ctrl.enter="sendMessage"
          @keydown.meta.enter="sendMessage"
        ></textarea>
        <p class="text-xs text-gray-500 mt-1">Ctrl+Enter 发送</p>
      </div>
      <div class="flex flex-col space-y-2">
        <button
          @click="sendMessage"
          :disabled="!inputMessage.trim() || loading"
          class="px-4 py-2 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          发送
        </button>
        <button
          @click="clearChat"
          class="px-4 py-2 bg-gray-200 text-gray-700 text-sm rounded-md hover:bg-gray-300"
        >
          清空
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'

interface Props {
  selectedText?: string
}

interface Message {
  id: number
  role: 'user' | 'assistant'
  content: string
  loading?: boolean
  timestamp: Date
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'insert-text': [text: string]
}>()

const messages = ref<Message[]>([])
const inputMessage = ref('')
const loading = ref(false)
const messageIdCounter = ref(0)

// 计算属性
const selectedText = computed(() => props.selectedText)

// 监听选中文本变化
watch(selectedText, (newText) => {
  if (newText && inputMessage.value.trim() === '') {
    inputMessage.value = `针对以下内容：\n"${newText}"\n\n请帮我`
  }
})

// 方法
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
    // TODO: 调用AI API
    // const response = await aiApi.chat({
    //   message: currentMessage,
    //   context: selectedText.value
    // })
    
    // 模拟AI响应
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    const aiResponse = generateMockResponse(currentMessage)
    
    assistantMessage.loading = false
    assistantMessage.content = aiResponse
  } catch (error) {
    console.error('AI请求失败:', error)
    assistantMessage.loading = false
    assistantMessage.content = '抱歉，AI服务暂时不可用，请稍后重试。'
  } finally {
    loading.value = false
    await nextTick()
    scrollToBottom()
  }
}

const sendQuickMessage = (message: string) => {
  inputMessage.value = message
  sendMessage()
}

const generateMockResponse = (userMessage: string): string => {
  if (userMessage.includes('润色')) {
    return '我可以帮您润色文字，让表达更加优雅和准确。请提供需要润色的文本，我会为您提供改进建议。'
  } else if (userMessage.includes('总结')) {
    return '我可以帮您总结内容的要点。请提供需要总结的文本，我会提取关键信息并整理成简洁的摘要。'
  } else if (userMessage.includes('翻译')) {
    return '我可以帮您翻译文本。请提供需要翻译的内容，我会为您提供准确的翻译结果。'
  } else if (userMessage.includes('代码')) {
    return '我可以帮您解释代码的功能和逻辑。请提供需要解释的代码片段，我会详细说明其工作原理。'
  } else {
    return `您好！我是AI助手，我理解您的问题是："${userMessage}"。我会尽力为您提供帮助。由于这是演示版本，实际的AI功能需要连接到通义千问API才能正常工作。`
  }
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

const clearSelectedText = () => {
  // 清空选中文本的逻辑由父组件处理
}

const clearChat = () => {
  messages.value = []
  messageIdCounter.value = 0
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
</style>
