<template>
  <header class="app-header">
    <div class="header-container">
      <!-- 로고/타이틀 -->
      <router-link to="/issues" class="logo">
        <h1>Issue Tracker</h1>
      </router-link>

      <!-- ✅ 여기 네비게이션 메뉴 삽입 -->
      <nav class="nav-menu">
        <router-link to="/issues" class="nav-link">이슈</router-link>
        <router-link to="/labels" class="nav-link">레이블</router-link>
        <router-link to="/milestones" class="nav-link">마일스톤</router-link>
      </nav>

      <!-- 사용자 정보 -->
      <div class="user-section">
        <div v-if="userInfo" class="user-info">
          <img 
            :src="userInfo.profileImage || 'https://issue-tracker-team01-bucket.s3.ap-northeast-2.amazonaws.com/images/2025/06/05/72a070f0-2a89-4f95-8fc1-2b9fc0b69b0f.jpeg'"
            :alt="userInfo.username"
            class="profile-image"
          />
          <span class="username">{{ userInfo.username }}</span>
        </div>

        <button @click="handleLogout" class="logout-btn">
          로그아웃
        </button>
      </div>
    </div>
  </header>
</template>


<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getUserFromToken } from '@/utils/auth'
import { authApi } from '@/services/api'

export default {
  name: 'AppHeader',
  setup() {
    const router = useRouter()
    const userInfo = ref(null)
    
    // JWT 토큰에서 사용자 정보 추출
    const loadUserInfo = () => {
      userInfo.value = getUserFromToken()
    }
    
    // 로그아웃
    const handleLogout = async () => {
      try {
        const refreshToken = localStorage.getItem('refreshToken')
        if (refreshToken) {
          await authApi.logout(refreshToken)
        }
      } catch (error) {
        console.error('로그아웃 요청 실패:', error)
      } finally {
        // 토큰 제거 및 로그인 페이지로 이동
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        router.push('/login')
      }
    }
    
    onMounted(() => {
      loadUserInfo()
    })
    
    return {
      userInfo,
      handleLogout
    }
  }
}
</script>

<style scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: white;
  border-bottom: 1px solid #e1e4e8;
  z-index: 1000;
  height: 60px;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  text-decoration: none;
  color: #333;
}

.logo h1 {
  font-size: 24px;
  font-weight: 600;
  font-style: italic;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.profile-image {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.username {
  font-weight: 500;
  color: #333;
}

.logout-btn {
  background: #6c757d;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.logout-btn:hover {
  background: #545b62;
}

.nav-menu {
  display: flex;
  gap: 16px;
  margin-left: 40px;
}

.nav-link {
  color: #333;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}

.nav-link:hover {
  color: #0366d6;
}

@media (max-width: 768px) {
  .header-container {
    padding: 0 10px;
  }
  
  .logo h1 {
    font-size: 20px;
  }
  
  .username {
    display: none;
  }
}
</style>