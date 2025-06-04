<template>
  <div class="create-issue-container">
    <!-- 헤더 -->
    <div class="page-header">
      <h1>새로운 이슈 작성</h1>
      <div class="header-actions">
        <button 
          type="button" 
          class="btn-cancel"
          @click="$router.push('/issues')"
        >
          취소
        </button>
        <button 
          type="submit" 
          class="btn-submit"
          @click="handleSubmit"
          :disabled="!form.title.trim() || isSubmitting"
        >
          {{ isSubmitting ? '작성 중...' : '이슈 등록' }}
        </button>
      </div>
    </div>

    <div class="create-issue-content">
      <!-- 메인 폼 영역 -->
      <div class="main-form">
        <!-- 제목 -->
        <div class="form-group">
          <label for="title">제목</label>
          <input
            id="title"
            type="text"
            v-model="form.title"
            placeholder="이슈 제목을 입력하세요"
            class="form-input"
            maxlength="500"
          />
        </div>

        <!-- 내용 -->
        <div class="form-group">
          <label for="content">내용</label>
          <MarkdownEditor
            v-model="form.content"
            placeholder="이슈에 대한 자세한 설명을 마크다운으로 작성해주세요...

**기본 사용법:**
- **굵은 글씨**: **텍스트**
- *기울임 글씨*: *텍스트*
- 링크: [텍스트](URL)
- 이미지: 직접 드래그하거나 붙여넣기, 또는 ![alt](URL)
- 코드: `인라인 코드` 또는 ```언어명
- 목록: - 또는 1.

이미지는 드래그 앤 드롭, 붙여넣기, 또는 이미지 버튼으로 업로드할 수 있습니다."
            :rows="12"
          />
        </div>
      </div>

      <!-- 사이드바 -->
      <div class="sidebar">
        <!-- 담당자 -->
        <div class="sidebar-section">
          <h3>담당자</h3>
          <div class="dropdown" :class="{ open: dropdowns.assignees }">
            <button 
              type="button" 
              class="dropdown-trigger"
              @click="toggleDropdown('assignees')"
            >
              <span v-if="selectedAssignees.length === 0">담당자 없음</span>
              <span v-else>{{ selectedAssignees.length }}명 선택됨</span>
              <svg class="dropdown-icon" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" />
              </svg>
            </button>
            
            <div v-if="dropdowns.assignees" class="dropdown-content">
              <div class="search-input">
                <input 
                  type="text" 
                  v-model="searchUsers"
                  placeholder="사용자 검색..."
                  class="search-field"
                />
              </div>
              <div class="options-list">
                <label 
                  v-for="user in filteredUsers" 
                  :key="user.id"
                  class="option-item"
                >
                  <input
                    type="checkbox"
                    :value="user.id"
                    v-model="selectedAssignees"
                  />
                  <img 
                    :src="user.profileImageUrl" 
                    :alt="user.username"
                    class="user-avatar"
                  />
                  <span>{{ user.username }}</span>
                </label>
              </div>
            </div>
          </div>
        </div>

        <!-- 레이블 -->
        <div class="sidebar-section">
          <h3>레이블</h3>
          <div class="dropdown" :class="{ open: dropdowns.labels }">
            <button 
              type="button" 
              class="dropdown-trigger"
              @click="toggleDropdown('labels')"
            >
              <span v-if="selectedLabels.length === 0">레이블 없음</span>
              <span v-else>{{ selectedLabels.length }}개 선택됨</span>
              <svg class="dropdown-icon" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" />
              </svg>
            </button>
            
            <div v-if="dropdowns.labels" class="dropdown-content">
              <div class="search-input">
                <input 
                  type="text" 
                  v-model="searchLabels"
                  placeholder="레이블 검색..."
                  class="search-field"
                />
              </div>
              <div class="options-list">
                <label 
                  v-for="label in filteredLabels" 
                  :key="label.id"
                  class="option-item"
                >
                  <input
                    type="checkbox"
                    :value="label.id"
                    v-model="selectedLabels"
                  />
                  <span 
                    class="label-preview"
                    :style="{ 
                      backgroundColor: label.color, 
                      color: label.textColor === 'WHITE' ? '#fff' : '#000' 
                    }"
                  >
                    {{ label.name }}
                  </span>
                </label>
              </div>
            </div>
          </div>
        </div>

        <!-- 마일스톤 -->
        <div class="sidebar-section">
          <h3>마일스톤</h3>
          <div class="dropdown" :class="{ open: dropdowns.milestone }">
            <button 
              type="button" 
              class="dropdown-trigger"
              @click="toggleDropdown('milestone')"
            >
              <span v-if="!selectedMilestone">마일스톤 없음</span>
              <span v-else>{{ getMilestoneTitle(selectedMilestone) }}</span>
              <svg class="dropdown-icon" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" />
              </svg>
            </button>
            
            <div v-if="dropdowns.milestone" class="dropdown-content">
              <div class="options-list">
                <label class="option-item">
                  <input
                    type="radio"
                    :value="null"
                    v-model="selectedMilestone"
                  />
                  <span>마일스톤 없음</span>
                </label>
                <label 
                  v-for="milestone in milestones" 
                  :key="milestone.id"
                  class="option-item"
                >
                  <input
                    type="radio"
                    :value="milestone.id"
                    v-model="selectedMilestone"
                  />
                  <span>{{ milestone.title }}</span>
                </label>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 에러 메시지 -->
    <div v-if="error" class="error-message">
      {{ error }}
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { issueApi, labelApi, milestoneApi, userApi } from '@/services/api'
import MarkdownEditor from '@/components/MarkdownEditor.vue'

const router = useRouter()

// 상태 관리
const isSubmitting = ref(false)
const error = ref('')

// 폼 데이터
const form = reactive({
  title: '',
  content: ''
})

// 선택된 값들
const selectedAssignees = ref([])
const selectedLabels = ref([])
const selectedMilestone = ref(null)

// 드롭다운 상태
const dropdowns = reactive({
  assignees: false,
  labels: false,
  milestone: false
})

// 검색 필터
const searchUsers = ref('')
const searchLabels = ref('')

// 데이터
const users = ref([])
const labels = ref([])
const milestones = ref([])

// 계산된 속성은 제거 (MarkdownEditor에서 처리)

const filteredUsers = computed(() => {
  if (!searchUsers.value) return users.value
  return users.value.filter(user => 
    user.username.toLowerCase().includes(searchUsers.value.toLowerCase())
  )
})

const filteredLabels = computed(() => {
  if (!searchLabels.value) return labels.value
  return labels.value.filter(label => 
    label.name.toLowerCase().includes(searchLabels.value.toLowerCase())
  )
})

// 메서드
const toggleDropdown = (name) => {
  Object.keys(dropdowns).forEach(key => {
    dropdowns[key] = key === name ? !dropdowns[key] : false
  })
}

const getMilestoneTitle = (milestoneId) => {
  const milestone = milestones.value.find(m => m.id === milestoneId)
  return milestone ? milestone.title : ''
}

const handleSubmit = async () => {
  if (!form.title.trim()) {
    error.value = '제목을 입력해주세요.'
    return
  }

  isSubmitting.value = true
  error.value = ''

  try {
    const payload = {
      title: form.title.trim(),
      content: form.content.trim() || null,
      milestoneId: selectedMilestone.value,
      labelIds: selectedLabels.value,
      assigneeIds: selectedAssignees.value
    }

    console.log('이슈 생성 요청:', payload)

    const response = await issueApi.createIssue(payload)
    const newIssue = response.data.data

    console.log('이슈 생성 완료:', newIssue)

    // 생성된 이슈 상세 페이지로 이동
    router.push(`/issues/${newIssue.id}`)
  } catch (err) {
    console.error('이슈 생성 실패:', err)
    error.value = err.response?.data?.message || '이슈 생성에 실패했습니다.'
  } finally {
    isSubmitting.value = false
  }
}

// 데이터 로드
const loadData = async () => {
  try {
    const [usersRes, labelsRes, milestonesRes] = await Promise.all([
      userApi.getUsers(),
      labelApi.getLabels(),
      milestoneApi.getMilestones()
    ])

    users.value = usersRes.data.data.users
    labels.value = labelsRes.data.data.labels
    milestones.value = milestonesRes.data.data.milestones

    console.log('데이터 로드 완료:', { 
      users: users.value.length, 
      labels: labels.value.length, 
      milestones: milestones.value.length 
    })
  } catch (err) {
    console.error('데이터 로드 실패:', err)
    error.value = '필요한 데이터를 불러오는데 실패했습니다.'
  }
}

// 외부 클릭 시 드롭다운 닫기
const handleClickOutside = (event) => {
  if (!event.target.closest('.dropdown')) {
    Object.keys(dropdowns).forEach(key => {
      dropdowns[key] = false
    })
  }
}

onMounted(() => {
  loadData()
  document.addEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.create-issue-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e1e5e9;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #24292f;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 10px;
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

.btn-submit {
  padding: 8px 16px;
  border: 1px solid #1f883d;
  background: #1f883d;
  color: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.btn-submit:hover:not(:disabled) {
  background: #1a7f37;
}

.btn-submit:disabled {
  background: #94d3a2;
  border-color: #94d3a2;
  cursor: not-allowed;
}

.create-issue-content {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 30px;
}

.main-form {
  min-width: 0;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-weight: 600;
  color: #24292f;
  margin-bottom: 8px;
  font-size: 14px;
}

.form-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #0969da;
  box-shadow: 0 0 0 3px rgba(9, 105, 218, 0.1);
}

/* 에디터 관련 스타일은 MarkdownEditor 컴포넌트에서 처리 */

.sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-section h3 {
  font-size: 14px;
  font-weight: 600;
  color: #24292f;
  margin: 0 0 10px 0;
}

.dropdown {
  position: relative;
}

.dropdown-trigger {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d0d7de;
  background: #fff;
  text-align: left;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #24292f;
  transition: border-color 0.2s;
}

.dropdown-trigger:hover {
  border-color: #b1b8c0;
}

.dropdown.open .dropdown-trigger {
  border-color: #0969da;
}

.dropdown-icon {
  width: 16px;
  height: 16px;
  transition: transform 0.2s;
}

.dropdown.open .dropdown-icon {
  transform: rotate(180deg);
}

.dropdown-content {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  box-shadow: 0 8px 24px rgba(140, 149, 159, 0.2);
  z-index: 1000;
  max-height: 200px;
  overflow-y: auto;
}

.search-input {
  padding: 8px;
  border-bottom: 1px solid #e1e5e9;
}

.search-field {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #d0d7de;
  border-radius: 4px;
  font-size: 12px;
}

.options-list {
  padding: 4px 0;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;
  transition: background-color 0.2s;
  gap: 8px;
}

.option-item:hover {
  background: #f6f8fa;
}

.user-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  object-fit: cover;
}

.label-preview {
  padding: 2px 6px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.error-message {
  margin-top: 20px;
  padding: 12px;
  background: #fff8f0;
  border: 1px solid #d1242f;
  color: #d1242f;
  border-radius: 6px;
  font-size: 14px;
}

/* 반응형 */
@media (max-width: 768px) {
  .create-issue-content {
    grid-template-columns: 1fr;
  }
  
  .page-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: space-between;
  }
}
</style>