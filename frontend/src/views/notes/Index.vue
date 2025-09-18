<template>
  <div class="px-4 sm:px-6 lg:px-8">
    <!-- 页面标题和操作 -->
    <div class="sm:flex sm:items-center">
      <div class="sm:flex-auto">
        <h1 class="text-2xl font-semibold text-gray-900">我的笔记</h1>
        <p class="mt-2 text-sm text-gray-700">
          管理和编辑您的Markdown笔记
        </p>
      </div>
      <div class="mt-4 sm:ml-16 sm:mt-0 sm:flex-none">
        <button
          @click="createNote"
          type="button"
          class="inline-flex items-center justify-center rounded-md bg-blue-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600"
        >
          新建笔记
        </button>
      </div>
    </div>

    <!-- 搜索框 -->
    <div class="mt-6">
      <div class="relative">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="搜索笔记..."
          class="block w-full rounded-md border-0 py-1.5 pl-10 pr-3 text-gray-900 ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm sm:leading-6"
        />
        <div class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
          <svg class="h-5 w-5 text-gray-400" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M9 3.5a5.5 5.5 0 100 11 5.5 5.5 0 000-11zM2 9a7 7 0 1112.452 4.391l3.328 3.329a.75.75 0 11-1.06 1.06l-3.329-3.328A7 7 0 012 9z" clip-rule="evenodd" />
          </svg>
        </div>
      </div>
    </div>

    <!-- 笔记列表 -->
    <div class="mt-8 flow-root">
      <div class="-mx-4 -my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
        <div class="inline-block min-w-full py-2 align-middle sm:px-6 lg:px-8">
          <div v-if="loading" class="text-center py-12">
            <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            <p class="mt-2 text-sm text-gray-500">加载中...</p>
          </div>
          
          <div v-else-if="filteredNotes.length === 0" class="text-center py-12">
            <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <h3 class="mt-2 text-sm font-semibold text-gray-900">暂无笔记</h3>
            <p class="mt-1 text-sm text-gray-500">开始创建您的第一个笔记吧！</p>
            <div class="mt-6">
              <button
                @click="createNote"
                type="button"
                class="inline-flex items-center rounded-md bg-blue-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-500"
              >
                新建笔记
              </button>
            </div>
          </div>

          <div v-else class="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            <div
              v-for="note in filteredNotes"
              :key="note.id"
              class="relative group bg-white p-6 rounded-lg shadow-sm ring-1 ring-gray-200 hover:shadow-md transition-shadow cursor-pointer"
              @click="editNote(note.id)"
            >
              <div class="flex items-start justify-between">
                <div class="flex-1 min-w-0">
                  <h3 class="text-lg font-medium text-gray-900 truncate">
                    {{ note.title || '无标题' }}
                  </h3>
                  <p class="mt-2 text-sm text-gray-500 line-clamp-3">
                    {{ note.content ? note.content.substring(0, 100) + '...' : '暂无内容' }}
                  </p>
                </div>
                <div class="ml-4 flex-shrink-0">
                  <button
                    @click.stop="deleteNote(note.id)"
                    class="opacity-0 group-hover:opacity-100 transition-opacity text-gray-400 hover:text-red-600"
                  >
                    <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  </button>
                </div>
              </div>
              <div class="mt-4 flex items-center justify-between text-xs text-gray-500">
                <span>{{ formatDate(note.updatedAt) }}</span>
                <span v-if="note.isPublic" class="inline-flex items-center px-2 py-1 rounded-full text-xs bg-green-100 text-green-800">
                  公开
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const searchQuery = ref('')
const notes = ref<any[]>([])

// 计算属性
const filteredNotes = computed(() => {
  if (!searchQuery.value) return notes.value
  return notes.value.filter(note =>
    note.title?.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    note.content?.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

// 方法
const loadNotes = async () => {
  loading.value = true
  try {
    // TODO: 调用API获取笔记列表
    // const response = await api.getNotes()
    // notes.value = response.data
    
    // 模拟数据
    notes.value = [
      {
        id: 1,
        title: '示例笔记',
        content: '这是一个示例笔记的内容...',
        isPublic: false,
        updatedAt: new Date().toISOString()
      }
    ]
  } catch (error) {
    console.error('加载笔记失败:', error)
  } finally {
    loading.value = false
  }
}

const createNote = () => {
  router.push('/notes/new')
}

const editNote = (id: number) => {
  router.push(`/notes/${id}`)
}

const deleteNote = async (id: number) => {
  if (!confirm('确定要删除这个笔记吗？')) return
  
  try {
    // TODO: 调用API删除笔记
    // await api.deleteNote(id)
    notes.value = notes.value.filter(note => note.id !== id)
  } catch (error) {
    console.error('删除笔记失败:', error)
  }
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 生命周期
onMounted(() => {
  loadNotes()
})
</script>

<style scoped>
.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
