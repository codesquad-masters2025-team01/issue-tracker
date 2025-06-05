// src/services/api.js
import axios from 'axios'
import qs from 'qs'

// Nginx 리버스 프록시 환경에서는 모든 요청이 같은 도메인으로 감
// 로컬 개발환경에서만 프록시 사용
const isDevelopment = window.location.hostname === 'localhost'
const BASE_URL = '/api/v1'  // 모든 환경에서 동일 (Nginx가 프록시 처리)

console.log('API 환경:', isDevelopment ? '개발(로컬)' : '운영(Nginx 프록시)', 'BASE_URL:', BASE_URL)

// axios 인스턴스 생성
const api = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  paramsSerializer: params => qs.stringify(params, { arrayFormat: 'repeat' })
})

// 요청 인터셉터 - 토큰 자동 첨부
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 응답 인터셉터 - 에러 처리
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// 인증 API
export const authApi = {
  // GitHub 로그인 URL 리다이렉트
  githubLogin() {
    // Nginx가 /api/를 프록시하므로 상대 경로 사용
    window.location.href = `${BASE_URL}/oauth/github/login`
  },
  
  // 일반 로그인
  login(loginData) {
    return api.post('/auth/login', loginData)
  },
  
  // 로그아웃
  logout(refreshToken) {
    return api.post('/auth/logout', { refreshToken })
  },
  
  // 현재 사용자 정보 (더 이상 사용 안함)
  getMe() {
    return api.get('/auth/me')
  }
}

// 이슈 API
export const issueApi = {
  // 이슈 목록 조회
  getIssues(params = {}) {
    return api.get('/issues', { params })
  },
  
  // 이슈 개수 조회
  getIssueCount(params = {}, options = {}) {
    return api.get('/issues/count', { ...options, params })
  },
  
  // 이슈 상세 조회
  getIssue(id) {
    return api.get(`/issues/${id}`)
  },
  
  // 이슈 수정
  updateIssue(id, data) {
    return api.patch(`/issues/${id}`, data)
  },

  // 이슈 배치 업데이트
  batchUpdateIssues(issueIds, action) {
    return api.patch('/issues/batch', { issueIds, action })
  },

  createIssue: (data) => api.post('/issues', data)
}

// 댓글 API
export const commentApi = {
  // 댓글 목록 조회
  getComments(issueId, cursor = null) {
    const params = cursor ? { cursor } : {}
    return api.get(`/issues/${issueId}/comments`, { params })
  },
  
  // 댓글 생성
  createComment(issueId, content) {
    return api.post(`/issues/${issueId}/comments`, { content })
  },
  
  // 댓글 수정
  updateComment(commentId, content) {
    return api.put(`/comments/${commentId}`, { content })
  },
  
  // 댓글 삭제
  deleteComment(commentId) {
    return api.delete(`/comments/${commentId}`)
  }
}

// 레이블 API 추가
export const labelFilterApi = {
  getLabels: () => api.get('/labels/filters'),
}

// 마일스톤 API 추가  
export const milestoneFilterApi = {
  getMilestones: () => api.get('/milestones/filters'),
}

// 사용자 API 추가
export const userApi = {
  getUsers: () => api.get('/users/filters'),
}

export const milestoneApi = {
  getMilestones: (state = 'open') => api.get(`/milestones?state=${state}`),
  createMilestone: (data) => api.post('/milestones', data),
  updateMilestone: (id, data) => api.put(`/milestones/${id}`, data),
  deleteMilestone: (id) => api.delete(`/milestones/${id}`),
}

export const labelApi = {
  getLabels: () => api.get('/labels'),
  createLabel: (data) => api.post('/labels', data),
  updateLabel: (id, data) => api.put(`/labels/${id}`, data),
  deleteLabel: (id) => api.delete(`/labels/${id}`),
}

// 파일 업로드 API 추가
export const fileApi = {
  uploadImage: (formData, config = {}) => {
    return api.post('/images', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      ...config
    })
  }
}

export default api