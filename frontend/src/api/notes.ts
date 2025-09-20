import request from './request'

export interface Note {
  id: number
  title: string
  content: string
  summary?: string
  folderId?: number
  tags?: string
  isStarred: boolean
  isPublic: boolean
  isDeleted: boolean
  createdAt: string
  updatedAt: string
}

export interface NoteCreateRequest {
  title: string
  content?: string
  folderId?: number
  tags?: string
  isPublic?: boolean
}

export interface NoteUpdateRequest {
  title?: string
  content?: string
  folderId?: number
  tags?: string
  isPublic?: boolean
  isStarred?: boolean
}

export interface NotesPageResponse {
  records: Note[]
  total: number
  size: number
  current: number
  pages: number
}

export const notesApi = {
  // 获取笔记列表（分页）
  getNotes(page: number = 1, size: number = 20, keyword?: string, folderId?: number) {
    return request({
      url: '/notes',
      method: 'get',
      params: { page, size, keyword, folderId }
    })
  },

  // 获取单个笔记
  getNoteById(id: number) {
    return request({
      url: `/notes/${id}`,
      method: 'get'
    })
  },

  // 创建笔记
  createNote(data: NoteCreateRequest) {
    return request({
      url: '/notes',
      method: 'post',
      data
    })
  },

  // 更新笔记
  updateNote(id: number, data: NoteUpdateRequest) {
    return request({
      url: `/notes/${id}`,
      method: 'put',
      data
    })
  },

  // 删除笔记
  deleteNote(id: number) {
    return request({
      url: `/notes/${id}`,
      method: 'delete'
    })
  },

  // 搜索笔记
  searchNotes(keyword: string) {
    return request({
      url: '/notes/search',
      method: 'get',
      params: { keyword }
    })
  },

  // 切换收藏状态
  toggleStar(id: number) {
    return request({
      url: `/notes/${id}/star`,
      method: 'post'
    })
  },

  // 获取收藏笔记
  getStarredNotes() {
    return request({
      url: '/notes/starred',
      method: 'get'
    })
  },

  // 获取最近笔记
  getRecentNotes(limit: number = 10) {
    return request({
      url: '/notes/recent',
      method: 'get',
      params: { limit }
    })
  }
}
