<template>
  <div class="login-container">
    <div class="login-card">
      <h1>Issue Tracker</h1>
      
      <!-- OAuth 처리 중 -->
      <div v-if="isProcessing" class="processing">
        <p>GitHub 로그인 처리 중...</p>
        <div class="spinner"></div>
      </div>
      
      <!-- 일반 로그인 폼 -->
      <div v-else>
        <button @click="handleGithubLogin" class="github-btn">
          GitHub 계정으로 로그인
        </button>
        
        <div class="divider">or</div>
        
        <form @submit.prevent="handleLogin">
          <input 
            v-model="loginForm.loginId" 
            type="text" 
            placeholder="아이디"
            required 
          />
          <input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="비밀번호"
            required 
          />
          <button type="submit" :disabled="loading">
            {{ loading ? '로그인 중...' : '로그인' }}
          </button>
        </form>
        
        <div v-if="error" class="error">{{ error }}</div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/services/api'

export default {
  name: 'LoginView',
  setup() {
    const router = useRouter()
    const error = ref('')
    const loading = ref(false)
    const isProcessing = ref(false)
    
    const loginForm = ref({
      loginId: '',
      password: ''
    })
    
    // GitHub 로그인
    const handleGithubLogin = () => {
      console.log('GitHub 로그인 시작')
      authApi.githubLogin()
    }
    
    // 일반 로그인
    const handleLogin = async () => {
      loading.value = true
      error.value = ''
      
      try {
        const response = await authApi.login(loginForm.value)
        const { accessToken, refreshToken } = response.data.data
        
        localStorage.setItem('accessToken', accessToken)
        localStorage.setItem('refreshToken', refreshToken)
        
        router.push('/issues')
      } catch (err) {
        console.error('로그인 실패:', err)
        error.value = '로그인에 실패했습니다.'
      } finally {
        loading.value = false
      }
    }
    
    // OAuth 콜백 처리 (간단하게!)
    const handleOAuthCallback = () => {
      console.log('OAuth 콜백 처리')
      
      const params = new URLSearchParams(window.location.search)
      const accessToken = params.get('accessToken')
      const refreshToken = params.get('refreshToken')
      
      if (accessToken && refreshToken) {
        console.log('토큰 저장 및 이동')
        
        // 토큰 저장
        localStorage.setItem('accessToken', accessToken)
        localStorage.setItem('refreshToken', refreshToken)
        
        // URL 정리
        window.history.replaceState({}, document.title, '/login')
        
        // 이동
        router.push('/issues')
      }
    }
    
    onMounted(() => {
      console.log('=== LoginView onMounted ===')
      
      // URL에 OAuth 파라미터가 있는지 확인
      const params = new URLSearchParams(window.location.search)
      const hasOAuth = params.get('accessToken') || params.get('success')
      
      if (hasOAuth) {
        isProcessing.value = true
        // 즉시 처리
        handleOAuthCallback()
      }
    })
    
    return {
      loginForm,
      error,
      loading,
      isProcessing,
      handleGithubLogin,
      handleLogin
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.login-card {
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  width: 400px;
  text-align: center;
}

.login-card h1 {
  margin-bottom: 30px;
  color: #333;
}

.processing {
  padding: 40px 20px;
  color: #007bff;
}

.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 16px auto;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.github-btn {
  width: 100%;
  padding: 12px;
  background: #333;
  color: white;
  border: none;
  border-radius: 4px;
  margin-bottom: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.github-btn:hover {
  background: #555;
}

.divider {
  margin: 20px 0;
  color: #666;
}

.login-card form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.login-card input {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.login-card button[type="submit"] {
  padding: 12px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.login-card button[type="submit"]:hover:not(:disabled) {
  background: #0056b3;
}

.login-card button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error {
  margin-top: 16px;
  padding: 12px;
  background: #f8d7da;
  color: #721c24;
  border-radius: 4px;
  border: 1px solid #f5c6cb;
  font-size: 14px;
}
</style>