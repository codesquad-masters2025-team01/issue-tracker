// src/utils/auth.js
// JWT 토큰 디코딩 함수
export function decodeJWT(token) {
  try {
    if (!token) return null
    
    // JWT는 header.payload.signature 형태
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    
    return JSON.parse(jsonPayload)
  } catch (error) {
    console.error('JWT 디코딩 실패:', error)
    return null
  }
}

// 토큰에서 사용자 정보 추출
export function getUserFromToken() {
  const token = localStorage.getItem('accessToken')
  if (!token) return null
  
  const payload = decodeJWT(token)
  if (!payload) return null
  
  return {
    id: payload.sub, // subject (사용자 ID)
    username: payload.username,
    profileImageUrl: payload.profileImageUrl
  }
}

// 토큰 만료 확인
export function isTokenExpired(token) {
  const payload = decodeJWT(token)
  if (!payload) return true
  
  const currentTime = Math.floor(Date.now() / 1000)
  return payload.exp < currentTime
}