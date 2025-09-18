<template>
  <div class="h-screen flex flex-col bg-gray-50">
    <!-- 顶部工具栏 -->
    <header class="bg-white border-b border-gray-200 px-4 py-2 flex items-center justify-between">
      <div class="flex items-center space-x-4">
        <!-- Logo -->
        <div class="flex items-center space-x-2">
          <svg class="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
          <h1 class="text-xl font-semibold text-gray-900">Markdown Notes</h1>
        </div>
        
        <!-- 工具按钮 -->
        <div class="flex items-center space-x-2">
          <button
            @click="toggleSidebar"
            class="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-md"
            title="切换侧边栏"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
          
          <button
            @click="createNewNote"
            class="px-3 py-2 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700 transition-colors"
          >
            新建笔记
          </button>
        </div>
      </div>
      
      <!-- 用户信息和操作 -->
      <div class="flex items-center space-x-4">
        <!-- 搜索框 -->
        <div class="relative">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索笔记..."
            class="w-64 px-3 py-1.5 pl-9 text-sm border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            @keyup.enter="handleSearch"
          />
          <svg class="absolute left-3 top-2 w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </div>
        
        <!-- AI切换按钮 -->
        <button
          @click="toggleAiPanel"
          :class="[
            'p-2 rounded-md transition-colors',
            showAiPanel 
              ? 'bg-blue-100 text-blue-600' 
              : 'text-gray-500 hover:text-gray-700 hover:bg-gray-100'
          ]"
          title="切换AI助手"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
          </svg>
        </button>
        
        <!-- 用户菜单 -->
        <div class="relative">
          <button
            @click="showUserMenu = !showUserMenu"
            class="flex items-center space-x-2 p-2 text-gray-700 hover:bg-gray-100 rounded-md"
          >
            <div class="w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center text-white text-sm font-medium">
              {{ user?.username?.charAt(0).toUpperCase() }}
            </div>
            <span class="text-sm">{{ user?.nickname || user?.username }}</span>
          </button>
          
          <!-- 下拉菜单 -->
          <div v-if="showUserMenu" class="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg border border-gray-200 z-50">
            <div class="py-1">
              <router-link
                to="/settings"
                class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                @click="showUserMenu = false"
              >
                设置
              </router-link>
              <button
                @click="logout"
                class="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
              >
                退出登录
              </button>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- 主内容区域 -->
    <div class="flex-1 flex overflow-hidden">
      <!-- 左侧边栏 - 文件目录树 -->
      <aside
        :class="[
          'bg-white border-r border-gray-200 transition-all duration-300',
          showSidebar ? 'w-64' : 'w-0 overflow-hidden'
        ]"
      >
        <div class="h-full flex flex-col">
          <!-- 文件夹操作 -->
          <div class="p-4 border-b border-gray-200">
            <div class="flex items-center justify-between mb-3">
              <h3 class="text-sm font-medium text-gray-900">文件夹</h3>
              <button
                @click="createFolder"
                class="p-1 text-gray-400 hover:text-gray-600"
                title="新建文件夹"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                </svg>
              </button>
            </div>
            
            <!-- 快速导航 -->
            <div class="space-y-1">
              <button
                @click="filterNotes('all')"
                :class="[
                  'w-full flex items-center space-x-2 px-3 py-2 text-sm rounded-md transition-colors',
                  currentFilter === 'all' 
                    ? 'bg-blue-50 text-blue-700' 
                    : 'text-gray-700 hover:bg-gray-100'
                ]"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
                <span>所有笔记</span>
              </button>
              
              <button
                @click="filterNotes('starred')"
                :class="[
                  'w-full flex items-center space-x-2 px-3 py-2 text-sm rounded-md transition-colors',
                  currentFilter === 'starred' 
                    ? 'bg-blue-50 text-blue-700' 
                    : 'text-gray-700 hover:bg-gray-100'
                ]"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" />
                </svg>
                <span>收藏</span>
              </button>
              
              <button
                @click="filterNotes('recent')"
                :class="[
                  'w-full flex items-center space-x-2 px-3 py-2 text-sm rounded-md transition-colors',
                  currentFilter === 'recent' 
                    ? 'bg-blue-50 text-blue-700' 
                    : 'text-gray-700 hover:bg-gray-100'
                ]"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span>最近</span>
              </button>
            </div>
          </div>
          
          <!-- 笔记列表 -->
          <div class="flex-1 overflow-y-auto">
            <div class="p-2">
              <div
                v-for="note in filteredNotes"
                :key="note.id"
                @click="selectNote(note)"
                :class="[
                  'p-3 mb-2 rounded-md cursor-pointer transition-colors',
                  selectedNote?.id === note.id 
                    ? 'bg-blue-50 border border-blue-200' 
                    : 'hover:bg-gray-50'
                ]"
              >
                <div class="flex items-start justify-between">
                  <div class="flex-1 min-w-0">
                    <h4 class="text-sm font-medium text-gray-900 truncate">
                      {{ note.title || '无标题' }}
                    </h4>
                    <p class="text-xs text-gray-500 mt-1 line-clamp-2">
                      {{ note.summary || '暂无内容' }}
                    </p>
                    <div class="flex items-center justify-between mt-2">
                      <span class="text-xs text-gray-400">
                        {{ formatDate(note.updatedAt) }}
                      </span>
                      <button
                        v-if="note.isStarred"
                        class="text-yellow-500"
                      >
                        <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 24 24">
                          <path d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" />
                        </svg>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 空状态 -->
              <div v-if="filteredNotes.length === 0" class="text-center py-8">
                <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
                <p class="mt-2 text-sm text-gray-500">暂无笔记</p>
                <button
                  @click="createNewNote"
                  class="mt-2 text-sm text-blue-600 hover:text-blue-500"
                >
                  创建第一个笔记
                </button>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <!-- 中间编辑区域 -->
      <main class="flex-1 flex flex-col overflow-hidden">
        <router-view :selected-note="selectedNote" @note-updated="handleNoteUpdated" />
      </main>

      <!-- 右侧AI面板 -->
      <aside
        :class="[
          'bg-white border-l border-gray-200 transition-all duration-300',
          showAiPanel ? 'w-80' : 'w-0 overflow-hidden'
        ]"
      >
        <div class="h-full flex flex-col">
          <div class="p-4 border-b border-gray-200">
            <h3 class="text-sm font-medium text-gray-900">AI 助手</h3>
          </div>
          <div class="flex-1 p-4">
            <AiChat v-if="showAiPanel" :selected-text="selectedText" />
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import AiChat from '@/components/AiChat.vue'

const authStore = useAuthStore()
const router = useRouter()

// 响应式状态
const showSidebar = ref(true)
const showAiPanel = ref(false)
const showUserMenu = ref(false)
const searchQuery = ref('')
const selectedNote = ref(null)
const selectedText = ref('')
const currentFilter = ref('all')
const notes = ref([])

// 计算属性
const user = computed(() => authStore.user)

const filteredNotes = computed(() => {
  let filtered = notes.value

  switch (currentFilter.value) {
    case 'starred':
      filtered = notes.value.filter(note => note.isStarred)
      break
    case 'recent':
      filtered = notes.value.slice(0, 10)
      break
    default:
      filtered = notes.value
  }

  if (searchQuery.value) {
    filtered = filtered.filter(note =>
      note.title?.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      note.content?.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
  }

  return filtered
})

// 方法
const toggleSidebar = () => {
  showSidebar.value = !showSidebar.value
}

const toggleAiPanel = () => {
  showAiPanel.value = !showAiPanel.value
}

const createNewNote = () => {
  router.push('/notes/new')
}

const createFolder = () => {
  // TODO: 实现创建文件夹功能
  console.log('创建文件夹')
}

const filterNotes = (filter: string) => {
  currentFilter.value = filter
}

const selectNote = (note: any) => {
  selectedNote.value = note
  router.push(`/notes/${note.id}`)
}

const handleSearch = () => {
  // 搜索已通过计算属性处理
}

const handleNoteUpdated = (updatedNote: any) => {
  const index = notes.value.findIndex(note => note.id === updatedNote.id)
  if (index !== -1) {
    notes.value[index] = updatedNote
  }
}

const loadNotes = async () => {
  try {
    // TODO: 调用API获取笔记列表
    // const response = await notesApi.getNotes()
    // notes.value = response.data.data
    
    // 模拟数据
    notes.value = [
      {
        id: 1,
        title: '欢迎使用 Markdown Notes',
        content: '这是您的第一个笔记...',
        summary: '这是您的第一个笔记，您可以在这里记录想法、知识和灵感。',
        isStarred: false,
        updatedAt: new Date().toISOString()
      }
    ]
  } catch (error) {
    console.error('加载笔记失败:', error)
  }
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))
  
  if (diffDays === 0) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (diffDays === 1) {
    return '昨天'
  } else if (diffDays < 7) {
    return `${diffDays}天前`
  } else {
    return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
  }
}

const logout = async () => {
  showUserMenu.value = false
  await authStore.logout()
  router.push('/login')
}

// 生命周期
onMounted(() => {
  loadNotes()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
