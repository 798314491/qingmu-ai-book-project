import request from './request'

export interface LoginRequest {
  username: string
  password: string
  rememberMe?: boolean
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
  nickname?: string
}

export interface JwtResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  userId: number
  username: string
  email: string
  nickname: string
  avatar?: string
}

export const authApi = {
  login(data: LoginRequest) {
    return request({
      url: '/auth/login',
      method: 'post',
      data
    })
  },

  register(data: RegisterRequest) {
    return request({
      url: '/auth/register',
      method: 'post',
      data
    })
  },

  logout() {
    return request({
      url: '/auth/logout',
      method: 'post'
    })
  },

  refreshToken(refreshToken: string) {
    return request({
      url: '/auth/refresh',
      method: 'post',
      params: { refreshToken }
    })
  }
}