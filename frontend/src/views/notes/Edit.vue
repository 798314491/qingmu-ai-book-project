<template>
  <div class="h-full flex flex-col">
    <!-- 工具栏 -->
    <div class="bg-white border-b px-4 py-3 flex items-center justify-between">
      <div class="flex items-center space-x-4">
        <button
          @click="goBack"
          class="inline-flex items-center text-gray-600 hover:text-gray-900"
        >
          <svg class="w-5 h-5 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
          返回
        </button>
        
        <input
          v-model="note.title"
          type="text"
          placeholder="笔记标题"
          class="text-lg font-medium border-none outline-none bg-transparent"
          @input="handleTitleChange"
        />
      </div>
      
      <div class="flex items-center space-x-2">
        <button
          @click="togglePreview"
          :class="[
            'px-3 py-1 rounded text-sm',
            showPreview 
              ? 'bg-blue-100 text-blue-700' 
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          ]"
        >
          {{ showPreview ? '编辑' : '预览' }}
        </button>
        
        <button
          @click="saveNote"
          :disabled="saving"
          class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
        >
          <svg v-if="saving" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          {{ saving ? '保存中...' : '保存' }}
        </button>
      </div>
    </div>

    <!-- 编辑区域 -->
    <div class="flex-1 flex overflow-hidden">
      <!-- 编辑器 -->
      <div v-show="!showPreview" class="flex-1 flex flex-col">
        <textarea
          v-model="note.content"
          ref="editorRef"
          placeholder="开始写作..."
          class="flex-1 w-full p-4 border-none outline-none resize-none font-mono text-sm leading-relaxed"
          @input="handleContentChange"
        ></textarea>
      </div>
      
      <!-- 预览区域 -->
      <div v-show="showPreview" class="flex-1 overflow-auto">
        <div class="p-4 prose prose-sm max-w-none" v-html="renderedContent"></div>
      </div>
      
      <!-- 分屏模式 -->
      <div v-if="splitView" class="flex-1 border-l overflow-auto">
        <div class="p-4 prose prose-sm max-w-none" v-html="renderedContent"></div>
      </div>
    </div>

    <!-- 状态栏 -->
    <div class="bg-gray-50 border-t px-4 py-2 flex items-center justify-between text-xs text-gray-500">
      <div class="flex items-center space-x-4">
        <span>字数: {{ wordCount }}</span>
        <span>行数: {{ lineCount }}</span>
        <span v-if="lastSaved">最后保存: {{ formatDate(lastSaved) }}</span>
      </div>
      
      <div class="flex items-center space-x-2">
        <label class="inline-flex items-center">
          <input
            v-model="note.isPublic"
            type="checkbox"
            class="form-checkbox h-4 w-4 text-blue-600"
            @change="handlePublicChange"
          />
          <span class="ml-2">公开分享</span>
        </label>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 响应式数据
const note = ref({
  id: null as number | null,
  title: '',
  content: '',
  isPublic: false
})

const saving = ref(false)
const showPreview = ref(false)
const splitView = ref(false)
const lastSaved = ref<string | null>(null)
const editorRef = ref<HTMLTextAreaElement>()

// 计算属性
const wordCount = computed(() => {
  return note.value.content.length
})

const lineCount = computed(() => {
  return note.value.content.split('\n').length
})

const renderedContent = computed(() => {
  // 简单的Markdown渲染（实际项目中应该使用markdown-it等库）
  return note.value.content
    .replace(/^### (.*$)/gim, '<h3>$1</h3>')
    .replace(/^## (.*$)/gim, '<h2>$1</h2>')
    .replace(/^# (.*$)/gim, '<h1>$1</h1>')
    .replace(/^\* (.*$)/gim, '<li>$1</li>')
    .replace(/\*\*(.*)\*\*/gim, '<strong>$1</strong>')
    .replace(/\*(.*)\*/gim, '<em>$1</em>')
    .replace(/\n/gim, '<br>')
})

// 方法
const loadNote = async () => {
  const noteId = route.params.id
  if (noteId === 'new') {
    // 新建笔记
    note.value = {
      id: null,
      title: '',
      content: '',
      isPublic: false
    }
    await nextTick()
    editorRef.value?.focus()
    return
  }

  try {
    // TODO: 调用API获取笔记详情
    // const response = await api.getNote(noteId)
    // note.value = response.data
    
    // 模拟数据
    note.value = {
      id: Number(noteId),
      title: '示例笔记',
      content: '# 示例笔记\n\n这是一个**示例**笔记的内容...\n\n## 子标题\n\n* 列表项1\n* 列表项2',
      isPublic: false
    }
  } catch (error) {
    console.error('加载笔记失败:', error)
    router.push('/notes')
  }
}

const saveNote = async () => {
  if (saving.value) return
  
  saving.value = true
  try {
    if (note.value.id) {
      // TODO: 更新笔记
      // await api.updateNote(note.value.id, note.value)
    } else {
      // TODO: 创建笔记
      // const response = await api.createNote(note.value)
      // note.value.id = response.data.id
      // router.replace(`/notes/${note.value.id}`)
    }
    
    lastSaved.value = new Date().toISOString()
  } catch (error) {
    console.error('保存笔记失败:', error)
  } finally {
    saving.value = false
  }
}

const goBack = () => {
  router.push('/notes')
}

const togglePreview = () => {
  showPreview.value = !showPreview.value
}

const handleTitleChange = () => {
  // 自动保存标题
  debounceAutoSave()
}

const handleContentChange = () => {
  // 自动保存内容
  debounceAutoSave()
}

const handlePublicChange = () => {
  // 立即保存公开状态
  saveNote()
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 防抖自动保存
let autoSaveTimer: NodeJS.Timeout | null = null
const debounceAutoSave = () => {
  if (autoSaveTimer) {
    clearTimeout(autoSaveTimer)
  }
  autoSaveTimer = setTimeout(() => {
    if (note.value.title || note.value.content) {
      saveNote()
    }
  }, 2000)
}

// 键盘快捷键
const handleKeyDown = (event: KeyboardEvent) => {
  if (event.ctrlKey || event.metaKey) {
    switch (event.key) {
      case 's':
        event.preventDefault()
        saveNote()
        break
      case 'p':
        event.preventDefault()
        togglePreview()
        break
    }
  }
}

// 生命周期
onMounted(() => {
  loadNote()
  document.addEventListener('keydown', handleKeyDown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeyDown)
  if (autoSaveTimer) {
    clearTimeout(autoSaveTimer)
  }
})
</script>

<style scoped>
.prose {
  color: #374151;
  max-width: none;
}

.prose h1 {
  font-size: 1.875rem;
  font-weight: 700;
  margin-top: 2rem;
  margin-bottom: 1rem;
}

.prose h2 {
  font-size: 1.5rem;
  font-weight: 600;
  margin-top: 1.5rem;
  margin-bottom: 0.75rem;
}

.prose h3 {
  font-size: 1.25rem;
  font-weight: 600;
  margin-top: 1rem;
  margin-bottom: 0.5rem;
}

.prose li {
  margin-left: 1rem;
  list-style-type: disc;
}

.prose strong {
  font-weight: 600;
}

.prose em {
  font-style: italic;
}
</style>
