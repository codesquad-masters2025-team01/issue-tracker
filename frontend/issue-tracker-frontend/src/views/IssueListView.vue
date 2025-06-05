<template>
  <div class="issue-list-container">
    <!-- 헤더 -->
   <div class="list-header">
  <div class="header-left">
    <!-- 선택된 이슈가 있을 때만 표시되는 액션 버튼들 -->
    <div v-if="selectedIssues.length > 0" class="selection-actions">
      <button 
        v-if="currentState === 'open'"
        @click="batchUpdateIssues('close')"
        class="btn-action"
        :disabled="isUpdating"
      >
        {{ isUpdating ? '처리 중...' : '선택한 이슈 닫기' }}
      </button>
      <button 
        v-else
        @click="batchUpdateIssues('open')"
        class="btn-action"
        :disabled="isUpdating"
      >
        {{ isUpdating ? '처리 중...' : '선택한 이슈 열기' }}
      </button>
      <button 
        @click="clearSelection"
        class="btn-cancel"
      >
        선택 취소
      </button>
    </div>
  </div>
  <!-- 여기에 추가 -->
  <div class="header-actions">
    <router-link to="/issues/new" class="btn-new-issue">
      새 이슈
    </router-link>
  </div>
</div>
    
    <!-- 필터 -->
    <div class="filters">
      <div class="filter-tabs">
        <button 
          @click="changeState('open')" 
          :class="{ active: currentState === 'open' }"
          class="filter-tab"
        >
          열린 이슈 ({{ formatCount(counts.open) }})
        </button>
        <button 
          @click="changeState('closed')" 
          :class="{ active: currentState === 'closed' }"
          class="filter-tab"
        >
          닫힌 이슈 ({{ formatCount(counts.closed) }})
        </button>
      </div>
      <div class="filter-selects">
        <select v-model="filters.filter" @change="handleFilterChange">
          <option value="">이슈 필터</option>
          <option value="created">내가 작성한 이슈</option>
          <option value="assigned">내가 담당한 이슈</option>
          <option value="commented">내가 댓글 남긴 이슈</option>
        </select>
        <select v-model="filters.writer" @change="handleFilterChange">
          <option value="">작성자</option>
          <option v-for="user in users" :key="user.id" :value="user.id">{{ user.username }}</option>
        </select>
        <select v-model="filters.assignees" multiple @change="handleFilterChange">
          <option v-for="user in users" :key="user.id" :value="user.id">담당: {{ user.username }}</option>
        </select>
        <select v-model="filters.labels" multiple @change="handleFilterChange">
          <option v-for="label in labels" :key="label.id" :value="label.id">{{ label.name }}</option>
        </select>
        <select v-model="filters.milestone" @change="handleFilterChange">
          <option value="">마일스톤</option>
          <option v-for="ms in milestones" :key="ms.id" :value="ms.id">{{ ms.title }}</option>
        </select>
        <button @click="resetFilters" class="btn-reset">필터 초기화</button>
      </div>
      <div class="search-bar">
        <input 
          v-model="searchQuery"
          @keyup.enter="handleSearch"
          placeholder="이슈 검색..."
          class="search-input"
        />
        <button @click="handleSearch" class="search-btn">검색</button>
      </div>
    </div>
    
    <!-- 이슈 목록 -->
    <div class="issues-container" ref="containerRef">
      <div v-if="loading && issues.length === 0" class="loading">
        이슈를 불러오는 중...
      </div>
      
      <div v-else-if="issues.length === 0" class="no-issues">
        <p>{{ currentState === 'open' ? '열린' : '닫힌' }} 이슈가 없습니다.</p>
      </div>
      
      <div v-else class="issues">
        <div
          v-for="issue in issues"
          :key="issue.id"
          class="issue-item"
          :class="{ selected: selectedIssues.includes(issue.id) }"
          @click="toggleIssueSelection(issue.id)"
        >
          <div class="issue-checkbox">
            <input 
              type="checkbox"
              :checked="selectedIssues.includes(issue.id)"
              @click.stop
              @change="toggleIssueSelection(issue.id)"
            />
          </div>
          
          <div class="issue-status">
            <span class="status-icon" :class="issue.state">
              {{ issue.state === 'open' ? '🔴' : '✅' }}
            </span>
          </div>
          
          <div class="issue-content" @click.stop="$router.push(`/issues/${issue.id}`)">
            <div class="issue-header">
              <h3 class="issue-title">{{ issue.title }}</h3>
              <div class="issue-labels">
                <span
                  v-for="label in issue.labels"
                  :key="label.id"
                  class="label"
                  :style="{
                    backgroundColor: label.color,
                    color: label.textColor
                  }"
                >
                  {{ label.name }}
                </span>
              </div>
            </div>
            
            <div class="issue-meta">
              <span class="issue-number">#{{ issue.id }}</span>
              <span class="issue-info">
                {{ formatDate(issue.createdAt) }}에 {{ issue.writer?.username }}가 작성
              </span>
              <span v-if="issue.milestone" class="milestone">
                🏁 {{ issue.milestone.title }}
              </span>
            </div>
          </div>
          
          <div class="issue-assignees">
            <img
              v-for="assignee in issue.assignees"
              :key="assignee.id"
              :src="assignee.profileImageUrl || '/default-profile.png'"
              :alt="assignee.username"
              class="assignee-avatar"
              :title="assignee.username"
            />
          </div>
        </div>
      </div>
      
      <!-- 무한 스크롤 로딩 -->
      <div v-if="loading && issues.length > 0" class="loading-more">
        더 많은 이슈를 불러오는 중...
      </div>
      
      <!-- 스크롤 감지용 요소 -->
      <div ref="sentinelRef" class="scroll-sentinel"></div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { issueApi, userApi, labelFilterApi, milestoneFilterApi } from '@/services/api'
import qs from 'qs'

export default {
  name: 'IssueListView',
  setup() {
    const loading = ref(true)
    const issues = ref([])
    const currentState = ref('open')
    const searchQuery = ref('')
    const cursor = ref(null)
    const hasNext = ref(true)
    const containerRef = ref(null)
    const sentinelRef = ref(null)
    const selectedIssues = ref([])
    const isUpdating = ref(false)
    
    const counts = reactive({
      open: 0,
      closed: 0
    })
    
    const issueCount = ref({ open: 0, closed: 0 })
    
    let observer = null
    
    const filters = reactive({
      filter: '',
      writer: '',
      assignees: [],
      labels: [],
      milestone: ''
    })
    const users = ref([])
    const labels = ref([])
    const milestones = ref([])
    
    // 날짜 포맷팅
    const formatDate = (dateString) => {
      const utc = new Date(dateString);
      const date = new Date(utc.getTime() + 9 * 60 * 60 * 1000);
      const now = new Date();
      const diffTime = Math.abs(now - date);
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      
      if (diffDays === 1) return '1일 전';
      if (diffDays < 7) return `${diffDays}일 전`;
      if (diffDays < 30) return `${Math.ceil(diffDays / 7)}주 전`;
      return `${Math.ceil(diffDays / 30)}달 전`;
    }
    
    // 이슈 개수 포맷팅 (999+ 처리)
    const formatCount = (count) => {
      return count > 999 ? '999+' : count
    }
    
    // 이슈 개수 조회
    const fetchIssueCounts = async () => {
      try {
        const params = {
          writerId: filters.writer || undefined,
          milestoneId: filters.milestone || undefined,
          labelIds: filters.labels.length > 0 ? filters.labels : undefined,
          assigneeIds: filters.assignees.length > 0 ? filters.assignees : undefined,
          filter: filters.filter || undefined
        }
        const response = await issueApi.getIssueCount(params, {
          paramsSerializer: params => qs.stringify(params, { arrayFormat: 'repeat' })
        })
        counts.open = response.data.data.open
        counts.closed = response.data.data.closed
      } catch (error) {
        console.error('이슈 개수 조회 실패:', error)
      }
    }
    
    // 이슈 목록 조회
    const fetchIssues = async (reset = false) => {
      if (loading.value) return
      loading.value = true
      try {
        const params = {
          state: currentState.value,
          filter: filters.filter || undefined,
          writerId: filters.writer || undefined,
          milestoneId: filters.milestone || undefined,
          labelIds: filters.labels.length > 0 ? filters.labels : undefined,
          assigneeIds: filters.assignees.length > 0 ? filters.assignees : undefined,
          cursor: reset ? null : cursor.value
        }
        if (searchQuery.value.trim()) {
          params.search = searchQuery.value.trim()
        }
        const response = await issueApi.getIssues(params, {
          paramsSerializer: params => qs.stringify(params, { arrayFormat: 'repeat' })
        })
        const data = response.data.data
        if (reset) {
          issues.value = data.issues || []
        } else {
          issues.value.push(...(data.issues || []))
        }
        cursor.value = data.cursor?.next
        hasNext.value = data.cursor?.hasNext || false
      } catch (error) {
        console.error('이슈 목록 조회 실패:', error)
        if (reset) {
          issues.value = []
        }
      } finally {
        loading.value = false
      }
    }
    
    // 상태 변경
    const changeState = (state) => {
      if (currentState.value === state) return
      
      currentState.value = state
      cursor.value = null
      hasNext.value = true
      fetchIssues(true)
    }
    
    // 검색
    const handleSearch = () => {
      cursor.value = null
      hasNext.value = true
      fetchIssues(true)
    }
    
    // 무한 스크롤 설정
    const setupInfiniteScroll = () => {
      observer = new IntersectionObserver(
        (entries) => {
          const entry = entries[0]
          if (entry.isIntersecting && hasNext.value && !loading.value) {
            fetchIssues(false) // 더 많은 데이터 로드
          }
        },
        {
          threshold: 0.1,
          rootMargin: '100px'
        }
      )
      
      if (sentinelRef.value) {
        observer.observe(sentinelRef.value)
      }
    }
    
    // 무한 스크롤 해제
    const cleanupInfiniteScroll = () => {
      if (observer && sentinelRef.value) {
        observer.unobserve(sentinelRef.value)
        observer.disconnect()
      }
    }
    
    // 이슈 개수 로드
    const loadIssueCount = async () => {
      try {
        const response = await issueApi.getIssueCount()
        issueCount.value = response.data.data
      } catch (error) {
        console.error('이슈 개수 로드 실패:', error)
      }
    }
    
    // 이슈 선택 토글
    const toggleIssueSelection = (issueId) => {
      const index = selectedIssues.value.indexOf(issueId)
      if (index === -1) {
        selectedIssues.value.push(issueId)
      } else {
        selectedIssues.value.splice(index, 1)
      }
    }
    
    // 선택 초기화
    const clearSelection = () => {
      selectedIssues.value = []
    }
    
    // 이슈 배치 업데이트
    const batchUpdateIssues = async (action) => {
      if (selectedIssues.value.length === 0) return
      
      isUpdating.value = true
      try {
        const response = await issueApi.batchUpdateIssues(selectedIssues.value, action)
        const result = response.data.data
        
        // 성공/실패 메시지 표시
        if (result.successCount > 0) {
          alert(`${result.successCount}개의 이슈가 ${action === 'open' ? '열렸습니다' : '닫혔습니다'}.`)
        }
        if (result.failedCount > 0) {
          alert(`${result.failedCount}개의 이슈 처리에 실패했습니다.`)
        }
        
        // 목록 새로고침
        await Promise.all([fetchIssues(true), fetchIssueCounts()])
        clearSelection()
      } catch (error) {
        console.error('이슈 상태 변경 실패:', error)
        alert('이슈 상태 변경에 실패했습니다.')
      } finally {
        isUpdating.value = false
      }
    }
    
    // 필터 데이터 불러오기
    const loadFilterData = async () => {
      try {
        const [userRes, labelRes, msRes] = await Promise.all([
          userApi.getUsers(),
          labelFilterApi.getLabels(),
          milestoneFilterApi.getMilestones()
        ])
        users.value = userRes.data.data.users || []
        labels.value = labelRes.data.data.labels || []
        milestones.value = msRes.data.data.milestones || []
      } catch (e) {
        console.error('필터 데이터 로드 실패:', e)
      }
    }

    // 필터 변경 핸들러
    const handleFilterChange = () => {
      cursor.value = null
      hasNext.value = true
      fetchIssues(true)
      fetchIssueCounts()
    }
    const resetFilters = () => {
      filters.filter = ''
      filters.writer = ''
      filters.assignees = []
      filters.labels = []
      filters.milestone = ''
      handleFilterChange()
    }
    
    // 초기 데이터 로드
    onMounted(async () => {
      try {
        await Promise.all([fetchIssues(true), fetchIssueCounts(), loadFilterData()])
      } catch (error) {
        console.error('초기 데이터 로드 실패:', error)
      } finally {
        loading.value = false
      }
      
      // DOM 업데이트 후 무한 스크롤 설정
      nextTick(() => {
        setupInfiniteScroll()
      })
    })
    
    // 컴포넌트 언마운트
    onUnmounted(() => {
      cleanupInfiniteScroll()
    })
    
    return {
      loading,
      issues,
      currentState,
      searchQuery,
      counts,
      containerRef,
      sentinelRef,
      selectedIssues,
      isUpdating,
      formatDate,
      formatCount,
      changeState,
      handleSearch,
      toggleIssueSelection,
      clearSelection,
      batchUpdateIssues,
      filters,
      users,
      labels,
      milestones,
      handleFilterChange,
      resetFilters
    }
  }
}
</script>

<style scoped>
.issue-list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.list-header h1 {
  margin: 0;
  color: #333;
}

.new-issue-btn {
  background: #28a745;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s;
}

.new-issue-btn:hover {
  background: #218838;
}

.filters {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.filter-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.filter-tab {
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.filter-tab:hover {
  background: #e9ecef;
}

.filter-tab.active {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

.filter-selects {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.search-bar {
  display: flex;
  gap: 8px;
  max-width: 400px;
}

.search-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  font-size: 14px;
}

.search-btn {
  background: #6c757d;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.search-btn:hover {
  background: #5a6268;
}

.issues-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
}

.loading,
.no-issues,
.loading-more {
  text-align: center;
  padding: 40px 20px;
  color: #666;
}

.loading-more {
  padding: 20px;
  font-size: 14px;
}

.issues {
  border-top: 1px solid #e1e4e8;
}

.issue-item {
  display: flex;
  align-items: flex-start;
  padding: 16px;
  border-bottom: 1px solid #e1e4e8;
  cursor: pointer;
  transition: background-color 0.2s;
  gap: 12px;
}

.issue-item.selected {
  background-color: #f6f8fa;
}

.issue-item:hover {
  background: #f6f8fa;
}

.issue-item:last-child {
  border-bottom: none;
}

.issue-status {
  flex-shrink: 0;
  margin-top: 2px;
}

.status-icon {
  font-size: 16px;
}

.issue-content {
  flex: 1;
  min-width: 0;
}

.issue-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
  flex-wrap: wrap;
}

.issue-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #0969da;
}

.issue-labels {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.label {
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;
  white-space: nowrap;
}

.issue-meta {
  font-size: 12px;
  color: #656d76;
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.issue-number {
  font-weight: 500;
}

.milestone {
  color: #8250df;
}

.issue-assignees {
  display: flex;
  gap: 4px;
  align-items: flex-start;
  flex-shrink: 0;
}

.assignee-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  object-fit: cover;
}

.scroll-sentinel {
  height: 1px;
  margin: 10px 0;
}

.selection-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.btn-action {
  padding: 8px 16px;
  border: 1px solid #2da44e;
  background: #2da44e;
  color: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.btn-action:hover:not(:disabled) {
  background: #2c974b;
}

.btn-action:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-cancel {
  padding: 8px 16px;
  border: 1px solid #d0d7de;
  background: #fff;
  color: #24292f;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.btn-cancel:hover {
  background: #f6f8fa;
}

.issue-checkbox {
  display: flex;
  align-items: center;
  padding: 0 8px;
}

.issue-checkbox input[type="checkbox"] {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.btn-reset {
  padding: 4px 10px;
  border: 1px solid #d0d7de;
  background: #fff;
  color: #24292f;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.btn-reset:hover {
  background: #f6f8fa;
}

@media (max-width: 768px) {
  .issue-list-container {
    padding: 10px;
  }
  
  .list-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .filter-tabs {
    flex-direction: column;
  }
  
  .search-bar {
    max-width: none;
  }
  
  .issue-item {
    flex-direction: column;
    gap: 8px;
  }
  
  .issue-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .issue-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
    .btn-new-issue {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    background: #2da44e;
    color: white;
    text-decoration: none;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 500;
    transition: background-color 0.2s;
    }

    .btn-new-issue:hover {
    background: #2c974b;
    }
  
}
</style>