<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 顶部导航 -->
    <header class="bg-white shadow-sm">
      <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <div class="flex items-center">
            <h1 class="text-xl font-semibold text-gray-900">Markdown Notes</h1>
            <span class="ml-2 text-sm text-gray-500">- 分享笔记</span>
          </div>
          
          <div class="flex items-center space-x-4">
            <button
              v-if="!isFullscreen"
              @click="toggleFullscreen"
              class="text-gray-600 hover:text-gray-900 p-2 rounded"
              title="全屏阅读"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5l-5-5m5 5v-4m0 4h-4" />
              </svg>
            </button>
            
            <button
              @click="copyLink"
              class="text-gray-600 hover:text-gray-900 p-2 rounded"
              title="复制链接"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </header>

    <!-- 主内容区域 -->
    <main class="max-w-4xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
      <!-- 加载状态 -->
      <div v-if="loading" class="text-center py-12">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        <p class="mt-2 text-sm text-gray-500">加载中...</p>
      </div>

      <!-- 错误状态 -->
      <div v-else-if="error" class="text-center py-12">
        <svg class="mx-auto h-12 w-12 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
        </svg>
        <h3 class="mt-2 text-lg font-medium text-gray-900">笔记未找到</h3>
        <p class="mt-1 text-sm text-gray-500">{{ error }}</p>
      </div>

      <!-- 笔记内容 -->
      <article v-else-if="note" class="bg-white rounded-lg shadow-sm overflow-hidden">
        <!-- 笔记头部 -->
        <header class="border-b border-gray-200 px-6 py-4">
          <h1 class="text-2xl font-bold text-gray-900 mb-2">
            {{ note.title || '无标题' }}
          </h1>
          <div class="flex items-center text-sm text-gray-500 space-x-4">
            <span>作者: {{ note.author || '匿名' }}</span>
            <span>更新时间: {{ formatDate(note.updatedAt) }}</span>
            <span v-if="note.viewCount" class="flex items-center">
              <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
              </svg>
              {{ note.viewCount }} 次查看
            </span>
          </div>
        </header>

        <!-- 笔记内容 -->
        <div class="px-6 py-8">
          <div 
            class="prose prose-lg max-w-none"
            v-html="renderedContent"
          ></div>
        </div>

        <!-- 笔记底部 -->
        <footer class="border-t border-gray-200 px-6 py-4 bg-gray-50">
          <div class="flex items-center justify-between">
            <div class="text-sm text-gray-500">
              <p>本笔记通过 Markdown Notes 分享</p>
            </div>
            <div class="flex items-center space-x-2">
              <button
                @click="copyLink"
                class="inline-flex items-center px-3 py-1 border border-gray-300 text-sm font-medium rounded text-gray-700 bg-white hover:bg-gray-50"
              >
                <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                </svg>
                复制链接
              </button>
            </div>
          </div>
        </footer>
      </article>
    </main>

    <!-- 全屏遮罩 -->
    <div
      v-if="isFullscreen"
      class="fixed inset-0 bg-white z-50 overflow-auto"
      @click="exitFullscreen"
    >
      <div class="max-w-4xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
        <button
          @click="exitFullscreen"
          class="fixed top-4 right-4 text-gray-600 hover:text-gray-900 p-2 rounded-full bg-white shadow-lg"
          title="退出全屏"
        >
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
        
        <article class="prose prose-lg max-w-none">
          <h1>{{ note?.title || '无标题' }}</h1>
          <div v-html="renderedContent"></div>
        </article>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 响应式数据
const loading = ref(true)
const error = ref('')
const isFullscreen = ref(false)
const note = ref<any>(null)

// 计算属性
const renderedContent = computed(() => {
  if (!note.value?.content) return ''
  
  // 简单的Markdown渲染（实际项目中应该使用markdown-it等库）
  return note.value.content
    .replace(/^### (.*$)/gim, '<h3>$1</h3>')
    .replace(/^## (.*$)/gim, '<h2>$1</h2>')
    .replace(/^# (.*$)/gim, '<h1>$1</h1>')
    .replace(/^\* (.*$)/gim, '<li>$1</li>')
    .replace(/\*\*(.*)\*\*/gim, '<strong>$1</strong>')
    .replace(/\*(.*)\*/gim, '<em>$1</em>')
    .replace(/`([^`]+)`/gim, '<code>$1</code>')
    .replace(/\n\n/gim, '</p><p>')
    .replace(/\n/gim, '<br>')
    .replace(/^(.*)$/, '<p>$1</p>')
})

// 方法
const loadSharedNote = async () => {
  const shareCode = route.params.code as string
  
  if (!shareCode) {
    error.value = '分享链接无效'
    loading.value = false
    return
  }

  try {
    // TODO: 调用API获取分享的笔记
    // const response = await api.getSharedNote(shareCode)
    // note.value = response.data
    
    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    note.value = {
      id: 1,
      title: '分享的示例笔记',
      content: `# 欢迎使用 Markdown Notes

这是一个**分享的笔记**示例。

## 功能特点

* 支持 Markdown 语法
* 实时预览
* 在线分享
* 响应式设计

## 代码示例

\`\`\`javascript
function hello() {
  console.log('Hello, World!');
}
\`\`\`

> 这是一个引用块

感谢使用 Markdown Notes！`,
      author: '示例用户',
      updatedAt: new Date().toISOString(),
      viewCount: 42
    }
    
    // 增加浏览次数
    await incrementViewCount(shareCode)
  } catch (err) {
    error.value = '笔记加载失败或已被删除'
    console.error('加载分享笔记失败:', err)
  } finally {
    loading.value = false
  }
}

const incrementViewCount = async (shareCode: string) => {
  try {
    // TODO: 调用API增加浏览次数
    // await api.incrementShareView(shareCode)
  } catch (err) {
    console.error('增加浏览次数失败:', err)
  }
}

const copyLink = async () => {
  try {
    await navigator.clipboard.writeText(window.location.href)
    alert('链接已复制到剪贴板')
  } catch (err) {
    console.error('复制链接失败:', err)
    alert('复制链接失败，请手动复制地址栏中的链接')
  }
}

const toggleFullscreen = () => {
  isFullscreen.value = true
}

const exitFullscreen = () => {
  isFullscreen.value = false
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 键盘事件处理
const handleKeyDown = (event: KeyboardEvent) => {
  if (event.key === 'Escape' && isFullscreen.value) {
    exitFullscreen()
  }
}

// 生命周期
onMounted(() => {
  loadSharedNote()
  document.addEventListener('keydown', handleKeyDown)
})
</script>

<style scoped>
.prose {
  color: #374151;
  line-height: 1.75;
}

.prose h1 {
  font-size: 2.25rem;
  font-weight: 800;
  margin-top: 0;
  margin-bottom: 2rem;
  color: #111827;
}

.prose h2 {
  font-size: 1.875rem;
  font-weight: 700;
  margin-top: 2rem;
  margin-bottom: 1rem;
  color: #111827;
}

.prose h3 {
  font-size: 1.5rem;
  font-weight: 600;
  margin-top: 1.5rem;
  margin-bottom: 0.75rem;
  color: #111827;
}

.prose p {
  margin-bottom: 1.25rem;
}

.prose li {
  margin-left: 1.5rem;
  margin-bottom: 0.5rem;
  list-style-type: disc;
}

.prose strong {
  font-weight: 600;
  color: #111827;
}

.prose em {
  font-style: italic;
}

.prose code {
  background-color: #f3f4f6;
  padding: 0.125rem 0.375rem;
  border-radius: 0.25rem;
  font-family: ui-monospace, SFMono-Regular, "SF Mono", Consolas, "Liberation Mono", Menlo, monospace;
  font-size: 0.875em;
}

.prose blockquote {
  border-left: 4px solid #e5e7eb;
  padding-left: 1rem;
  margin: 1.5rem 0;
  font-style: italic;
  color: #6b7280;
}
</style>
