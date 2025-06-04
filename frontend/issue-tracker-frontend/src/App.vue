<template>
  <div id="app">
    <!-- 로그인 페이지가 아닐 때만 헤더 표시 -->
    <AppHeader v-if="!isLoginPage" />
    
    <!-- 라우터 뷰 -->
    <main class="main-content" :class="{ 'with-header': !isLoginPage }">
      <router-view />
    </main>
  </div>
</template>

<script>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'

export default {
  name: 'App',
  components: {
    AppHeader
  },
  setup() {
    const route = useRoute()
    
    const isLoginPage = computed(() => {
      return route.path === '/login'
    })
    
    return {
      isLoginPage
    }
  }
}
</script>

<style>
/* 전역 스타일 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-color: #f5f5f5;
  color: #333;
}

#app {
  min-height: 100vh;
}

.main-content {
  padding: 20px;
}

.main-content.with-header {
  padding-top: 80px; /* 헤더 높이만큼 여백 */
}

/* 공통 버튼 스타일 */
.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-primary:hover {
  background-color: #0056b3;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background-color: #545b62;
}

.btn-success {
  background-color: #28a745;
  color: white;
}

.btn-success:hover {
  background-color: #1e7e34;
}

/* 공통 카드 스타일 */
.card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 16px;
}

/* 로딩 상태 */
.loading {
  text-align: center;
  padding: 40px;
  color: #666;
}

/* 에러 상태 */
.error {
  color: #dc3545;
  background-color: #f8d7da;
  padding: 12px;
  border-radius: 4px;
  margin-bottom: 16px;
}

/* 레이블 스타일 */
.label {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  margin-right: 4px;
}

/* 반응형 */
@media (max-width: 768px) {
  .main-content {
    padding: 10px;
  }
}
</style>