<template>
  <div class="milestone-manage-container">
    <!-- 헤더 -->
    <div class="page-header">
      <div class="header-left">
        <h1>마일스톤</h1>
        <p class="header-description">{{ milestones.length }}개의 마일스톤</p>
      </div>
      <div class="header-actions">
        <button 
          type="button"
          class="btn-new-milestone"
          @click="openCreateModal"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm5 11h-4v4h-2v-4H7v-2h4V7h2v4h4v2z"/>
          </svg>
          새 마일스톤
        </button>
      </div>
    </div>

    <!-- 상태 탭 -->
    <div class="state-tabs">
      <button 
        type="button"
        :class="['tab', { active: selectedState === 'OPEN' }]"
        @click="selectedState = 'OPEN'"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" fill="none"/>
        </svg>
        열린 마일스톤 ({{ openCount }})
      </button>
      <button 
        type="button"
        :class="['tab', { active: selectedState === 'CLOSED' }]"
        @click="selectedState = 'CLOSED'"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
        </svg>
        닫힌 마일스톤 ({{ closedCount }})
      </button>
    </div>

    <!-- 로딩 상태 -->
    <div v-if="loading" class="loading">
      마일스톤을 불러오는 중...
    </div>

    <!-- 마일스톤 목록 -->
    <div v-else class="milestones-list">
      <div 
        v-for="milestone in filteredMilestones" 
        :key="milestone.id"
        class="milestone-item"
      >
        <div class="milestone-main">
          <div class="milestone-info">
            <h3 class="milestone-title">
              <span 
                class="milestone-status"
                :class="{ 
                  open: milestone.state === 'OPEN', 
                  closed: milestone.state === 'CLOSED' 
                }"
              >
                <svg v-if="milestone.state === 'OPEN'" width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                  <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" fill="none"/>
                </svg>
                <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                </svg>
              </span>
              {{ milestone.title }}
            </h3>
            
            <p v-if="milestone.description" class="milestone-description">
              {{ milestone.description }}
            </p>
            
            <div class="milestone-meta">
              <span v-if="milestone.dueDate" class="due-date">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M19 3h-1V1h-2v2H8V1H6v2H5c-1.11 0-1.99.9-1.99 2L3 19c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11zM7 10h5v5H7z"/>
                </svg>
                {{ formatDate(milestone.dueDate) }}
                <span v-if="isOverdue(milestone.dueDate)" class="overdue-badge">지연됨</span>
              </span>
              <span class="issue-counts">
                {{ milestone.openCount }}개 열림, {{ milestone.closeCount }}개 완료
              </span>
            </div>
          </div>

          <div class="milestone-actions">
            <button 
              type="button"
              class="btn-edit"
              @click="openEditModal(milestone)"
            >
              편집
            </button>
            <button 
              type="button"
              class="btn-delete"
              @click="deleteMilestone(milestone)"
            >
              삭제
            </button>
          </div>
        </div>

        <!-- 진행률 바 -->
        <div class="progress-section">
          <div class="progress-bar">
            <div 
              class="progress-fill"
              :style="{ width: milestone.progress * 100 + '%' }"
            ></div>
          </div>
          <span class="progress-text">
            {{ Math.round(milestone.progress * 100) }}% 완료
          </span>
        </div>
      </div>

      <!-- 마일스톤이 없는 경우 -->
      <div v-if="filteredMilestones.length === 0" class="empty-state">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="currentColor" class="empty-icon">
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
        </svg>
        <h3>{{ selectedState === 'OPEN' ? '열린' : '닫힌' }} 마일스톤이 없습니다</h3>
        <p v-if="selectedState === 'OPEN'">새 마일스톤을 만들어 이슈를 관리해보세요.</p>
        <p v-else>완료된 마일스톤이 없습니다.</p>
        <button 
          v-if="selectedState === 'OPEN'"
          type="button"
          class="btn-create-first"
          @click="openCreateModal"
        >
          첫 번째 마일스톤 만들기
        </button>
      </div>
    </div>

    <!-- 마일스톤 생성/수정 모달 -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h2>{{ isEditing ? '마일스톤 편집' : '새 마일스톤' }}</h2>
          <button type="button" class="btn-close" @click="closeModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
            </svg>
          </button>
        </div>

        <form @submit.prevent="submitForm" class="modal-content">
          <!-- 마일스톤 제목 -->
          <div class="form-group">
            <label for="title">제목 *</label>
            <input
              id="title"
              type="text"
              v-model="form.title"
              placeholder="마일스톤 제목을 입력하세요"
              class="form-input"
              maxlength="500"
              required
            />
          </div>

          <!-- 설명 -->
          <div class="form-group">
            <label for="description">설명</label>
            <textarea
              id="description"
              v-model="form.description"
              placeholder="마일스톤에 대한 설명을 입력하세요 (선택사항)"
              class="form-textarea"
              rows="4"
            ></textarea>
          </div>

          <!-- 마감일 -->
          <div class="form-group">
            <label for="dueDate">마감일</label>
            <input
              id="dueDate"
              type="date"
              v-model="form.dueDate"
              class="form-input"
            />
            <small class="form-help">선택사항입니다. 비워두면 마감일이 없는 마일스톤이 됩니다.</small>
          </div>

          <!-- 상태 (편집 시에만) -->
          <div v-if="isEditing" class="form-group">
            <label>상태 *</label>
            <div class="radio-group">
              <label class="radio-option">
                <input
                  type="radio"
                  value="OPEN"
                  v-model="form.state"
                />
                <span class="radio-label">열림</span>
              </label>
              <label class="radio-option">
                <input
                  type="radio"
                  value="CLOSED"
                  v-model="form.state"
                />
                <span class="radio-label">닫힘</span>
              </label>
            </div>
          </div>

          <!-- 에러 메시지 -->
          <div v-if="formError" class="error-message">
            {{ formError }}
          </div>

          <!-- 모달 액션 -->
          <div class="modal-actions">
            <button 
              type="button"
              class="btn-cancel"
              @click="closeModal"
            >
              취소
            </button>
            <button 
              type="submit"
              class="btn-submit"
              :disabled="!form.title.trim() || isSubmitting"
            >
              {{ isSubmitting ? '처리 중...' : (isEditing ? '마일스톤 수정' : '마일스톤 생성') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { milestoneApi } from '@/services/api'

// 상태 관리
const loading = ref(true)
const milestones = ref([])
const selectedState = ref('OPEN')
const showModal = ref(false)
const isEditing = ref(false)
const editingMilestoneId = ref(null)
const isSubmitting = ref(false)
const formError = ref('')

// 폼 데이터
const form = reactive({
  title: '',
  description: '',
  dueDate: '',
  state: 'OPEN'
})

// 계산된 속성
const filteredMilestones = computed(() => {
  return milestones.value.filter(milestone => milestone.state === selectedState.value)
})

const openCount = computed(() => {
  return milestones.value.filter(m => m.state === 'OPEN').length
})

const closedCount = computed(() => {
  return milestones.value.filter(m => m.state === 'CLOSED').length
})

// 마일스톤 목록 로드
const loadMilestones = async () => {
  loading.value = true
  try {
    const response = await milestoneApi.getMilestones(selectedState.value)
    milestones.value = response.data.data.milestones
    console.log('마일스톤 로드 완료:', milestones.value.length)
  } catch (error) {
    console.error('마일스톤 로드 실패:', error)
  } finally {
    loading.value = false
  }
}

// 전체 마일스톤 로드 (개수 표시용)
const loadAllMilestones = async () => {
  try {
    const [openResponse, closedResponse] = await Promise.all([
      milestoneApi.getMilestones('OPEN'),
      milestoneApi.getMilestones('CLOSED')
    ])
    
    milestones.value = [
      ...openResponse.data.data.milestones,
      ...closedResponse.data.data.milestones
    ]
  } catch (error) {
    console.error('전체 마일스톤 로드 실패:', error)
  }
}

// 날짜 포맷팅
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// 마감일 지연 여부 확인
const isOverdue = (dateString) => {
  if (!dateString) return false
  const dueDate = new Date(dateString)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  dueDate.setHours(0, 0, 0, 0)
  return dueDate < today
}

// 생성 모달 열기
const openCreateModal = () => {
  isEditing.value = false
  editingMilestoneId.value = null
  resetForm()
  showModal.value = true
}

// 편집 모달 열기
const openEditModal = (milestone) => {
  isEditing.value = true
  editingMilestoneId.value = milestone.id
  form.title = milestone.title
  form.description = milestone.description || ''
  form.dueDate = milestone.dueDate || ''
  form.state = milestone.state
  showModal.value = true
}

// 모달 닫기
const closeModal = () => {
  showModal.value = false
  resetForm()
  formError.value = ''
}

// 폼 리셋
const resetForm = () => {
  form.title = ''
  form.description = ''
  form.dueDate = ''
  form.state = 'OPEN'
}

// 폼 제출
const submitForm = async () => {
  if (!form.title.trim()) {
    formError.value = '마일스톤 제목을 입력해주세요.'
    return
  }

  isSubmitting.value = true
  formError.value = ''

  try {
    const payload = {
      title: form.title.trim(),
      description: form.description.trim() || null,
      dueDate: form.dueDate || null
    }

    if (isEditing.value) {
      // 마일스톤 수정
      payload.state = form.state
      await milestoneApi.updateMilestone(editingMilestoneId.value, payload)
      console.log('마일스톤 수정 완료')
    } else {
      // 마일스톤 생성
      await milestoneApi.createMilestone(payload)
      console.log('마일스톤 생성 완료')
    }

    // 목록 새로고침
    await loadAllMilestones()
    closeModal()
    
  } catch (error) {
    console.error('마일스톤 저장 실패:', error)
    formError.value = error.response?.data?.message || '마일스톤 저장에 실패했습니다.'
  } finally {
    isSubmitting.value = false
  }
}

// 마일스톤 삭제
const deleteMilestone = async (milestone) => {
  const totalIssues = milestone.openCount + milestone.closeCount
  let confirmMessage = `"${milestone.title}" 마일스톤을 삭제하시겠습니까?`
  
  if (totalIssues > 0) {
    confirmMessage += `\n\n이 마일스톤에는 ${totalIssues}개의 이슈가 연결되어 있습니다. 삭제하면 이슈들의 마일스톤이 제거됩니다.`
  }

  if (!confirm(confirmMessage)) {
    return
  }

  try {
    await milestoneApi.deleteMilestone(milestone.id)
    console.log('마일스톤 삭제 완료')
    
    // 목록에서 제거
    milestones.value = milestones.value.filter(m => m.id !== milestone.id)
    
  } catch (error) {
    console.error('마일스톤 삭제 실패:', error)
    alert('마일스톤 삭제에 실패했습니다.')
  }
}

// 상태 변경 시 데이터 다시 로드
watch(selectedState, () => {
  loadMilestones()
})

// 초기 데이터 로드
onMounted(() => {
  loadAllMilestones()
})
</script>

<style scoped>
.milestone-manage-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 20px;
}

.header-left h1 {
  font-size: 28px;
  font-weight: 600;
  color: #24292f;
  margin: 0 0 5px 0;
}

.header-description {
  color: #656d76;
  margin: 0;
  font-size: 16px;
}

.btn-new-milestone {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #2da44e;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: background-color 0.2s;
}

.btn-new-milestone:hover {
  background: #2c974b;
}

.state-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 30px;
  border-bottom: 1px solid #e1e5e9;
}

.tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 16px;
  border: none;
  background: none;
  color: #656d76;
  cursor: pointer;
  font-size: 14px;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

.tab:hover {
  color: #24292f;
}

.tab.active {
  color: #24292f;
  border-bottom-color: #fd8c73;
}

.loading {
  text-align: center;
  padding: 60px 20px;
  color: #656d76;
  font-size: 16px;
}

.milestones-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.milestone-item {
  background: #fff;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  padding: 20px;
  transition: border-color 0.2s;
}

.milestone-item:hover {
  border-color: #b1b8c0;
}

.milestone-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.milestone-info {
  flex: 1;
  min-width: 0;
}

.milestone-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #24292f;
  margin: 0 0 8px 0;
}

.milestone-status.open {
  color: #2da44e;
}

.milestone-status.closed {
  color: #8250df;
}

.milestone-description {
  color: #656d76;
  font-size: 14px;
  margin: 0 0 12px 0;
  line-height: 1.5;
  word-break: break-word;
}

.milestone-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 14px;
  color: #656d76;
}

.due-date {
  display: flex;
  align-items: center;
  gap: 4px;
}

.overdue-badge {
  background: #d1242f;
  color: white;
  padding: 2px 6px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
  margin-left: 6px;
}

.issue-counts {
  display: flex;
  align-items: center;
}

.milestone-actions {
  display: flex;
  gap: 8px;
}

.btn-edit, .btn-delete {
  padding: 6px 12px;
  border: 1px solid #d0d7de;
  background: #fff;
  color: #24292f;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.btn-edit:hover {
  background: #f6f8fa;
  border-color: #b1b8c0;
}

.btn-delete:hover {
  background: #fff5f5;
  border-color: #d1242f;
  color: #d1242f;
}

.progress-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: #e1e5e9;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #2da44e;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 12px;
  color: #656d76;
  font-weight: 500;
  min-width: 60px;
  text-align: right;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #656d76;
}

.empty-icon {
  opacity: 0.3;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 20px;
  color: #24292f;
  margin: 0 0 8px 0;
}

.empty-state p {
  font-size: 16px;
  margin: 0 0 24px 0;
}

.btn-create-first {
  padding: 12px 24px;
  background: #2da44e;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: background-color 0.2s;
}

.btn-create-first:hover {
  background: #2c974b;
}

/* 모달 스타일 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e1e5e9;
}

.modal-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #24292f;
  margin: 0;
}

.btn-close {
  background: none;
  border: none;
  cursor: pointer;
  color: #656d76;
  padding: 4px;
  border-radius: 4px;
  transition: color 0.2s;
}

.btn-close:hover {
  color: #24292f;
}

.modal-content {
  padding: 20px 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-weight: 600;
  color: #24292f;
  margin-bottom: 6px;
  font-size: 14px;
}

.form-input, .form-textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-input:focus, .form-textarea:focus {
  outline: none;
  border-color: #0969da;
  box-shadow: 0 0 0 3px rgba(9, 105, 218, 0.1);
}

.form-textarea {
  resize: vertical;
  font-family: inherit;
}

.form-help {
  display: block;
  font-size: 12px;
  color: #656d76;
  margin-top: 4px;
}

.radio-group {
  display: flex;
  gap: 16px;
}

.radio-option {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.radio-label {
  font-size: 14px;
  color: #24292f;
}

.error-message {
  margin-bottom: 16px;
  padding: 12px;
  background: #fff8f0;
  border: 1px solid #d1242f;
  color: #d1242f;
  border-radius: 6px;
  font-size: 14px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e1e5e9;
}

.btn-cancel, .btn-submit {
  padding: 8px 16px;
  border: 1px solid;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-cancel {
  border-color: #d0d7de;
  background: #fff;
  color: #24292f;
}

.btn-cancel:hover {
  background: #f6f8fa;
}

.btn-submit {
  border-color: #2da44e;
  background: #2da44e;
  color: white;
}

.btn-submit:hover:not(:disabled) {
  background: #2c974b;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 반응형 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .milestone-main {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .milestone-actions {
    justify-content: flex-start;
  }
  
  .milestone-meta {
    flex-direction: column;
    gap: 8px;
  }
  
  .progress-section {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }
  
  .progress-text {
    text-align: left;
    min-width: auto;
  }
  
  .modal {
    margin: 20px;
    width: calc(100% - 40px);
  }
  
  .state-tabs {
    flex-wrap: wrap;
  }
}
</style>