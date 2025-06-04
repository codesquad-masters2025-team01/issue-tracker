<template>
  <div class="label-manage-container">
    <!-- 헤더 -->
    <div class="page-header">
      <div class="header-left">
        <h1>레이블</h1>
        <p class="header-description">{{ labels.length }}개의 레이블</p>
      </div>
      <div class="header-actions">
        <button 
          type="button"
          class="btn-new-label"
          @click="openCreateModal"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm5 11h-4v4h-2v-4H7v-2h4V7h2v4h4v2z"/>
          </svg>
          새 레이블
        </button>
      </div>
    </div>

    <!-- 로딩 상태 -->
    <div v-if="loading" class="loading">
      레이블을 불러오는 중...
    </div>

    <!-- 레이블 목록 -->
    <div v-else class="labels-list">
      <div 
        v-for="label in labels" 
        :key="label.id"
        class="label-item"
      >
        <div class="label-preview">
          <span 
            class="label-badge"
            :style="{ 
              backgroundColor: label.color, 
              color: label.textColor === 'WHITE' ? '#fff' : '#000' 
            }"
          >
            {{ label.name }}
          </span>
        </div>
        
        <div class="label-details">
          <h3 class="label-name">{{ label.name }}</h3>
          <p class="label-description">{{ label.description || '설명 없음' }}</p>
        </div>

        <div class="label-actions">
          <button 
            type="button"
            class="btn-edit"
            @click="openEditModal(label)"
          >
            편집
          </button>
          <button 
            type="button"
            class="btn-delete"
            @click="deleteLabel(label)"
          >
            삭제
          </button>
        </div>
      </div>

      <!-- 레이블이 없는 경우 -->
      <div v-if="labels.length === 0" class="empty-state">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="currentColor" class="empty-icon">
          <path d="M17.63 5.84C17.27 5.33 16.67 5 16 5L5 5.01C3.9 5.01 3 5.9 3 7v10c0 1.1.9 1.99 2 1.99L16 19c.67 0 1.27-.33 1.63-.84L22 12l-4.37-6.16z"/>
        </svg>
        <h3>레이블이 없습니다</h3>
        <p>새 레이블을 만들어 이슈를 분류해보세요.</p>
        <button 
          type="button"
          class="btn-create-first"
          @click="openCreateModal"
        >
          첫 번째 레이블 만들기
        </button>
      </div>
    </div>

    <!-- 레이블 생성/수정 모달 -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h2>{{ isEditing ? '레이블 편집' : '새 레이블' }}</h2>
          <button type="button" class="btn-close" @click="closeModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
            </svg>
          </button>
        </div>

        <form @submit.prevent="submitForm" class="modal-content">
          <!-- 레이블 미리보기 -->
          <div class="preview-section">
            <h3>미리보기</h3>
            <span 
              class="label-preview-large"
              :style="{ 
                backgroundColor: form.color, 
                color: form.textColor === 'WHITE' ? '#fff' : '#000' 
              }"
            >
              {{ form.name || '레이블 이름' }}
            </span>
          </div>

          <!-- 레이블 이름 -->
          <div class="form-group">
            <label for="name">레이블 이름 *</label>
            <input
              id="name"
              type="text"
              v-model="form.name"
              placeholder="레이블 이름을 입력하세요"
              class="form-input"
              maxlength="100"
              required
            />
          </div>

          <!-- 설명 -->
          <div class="form-group">
            <label for="description">설명</label>
            <textarea
              id="description"
              v-model="form.description"
              placeholder="레이블에 대한 설명을 입력하세요 (선택사항)"
              class="form-textarea"
              rows="3"
            ></textarea>
          </div>

          <!-- 배경색 -->
          <div class="form-group">
            <label for="color">배경색 *</label>
            <div class="color-input-group">
              <input
                id="color"
                type="color"
                v-model="form.color"
                class="color-picker"
              />
              <input
                type="text"
                v-model="form.color"
                placeholder="#000000"
                class="color-text"
                pattern="^#([0-9A-Fa-f]{6})$"
                maxlength="7"
              />
              <button 
                type="button"
                class="btn-random-color"
                @click="generateRandomColor"
                title="랜덤 색상 생성"
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/>
                </svg>
              </button>
            </div>
          </div>

          <!-- 텍스트 색상 -->
          <div class="form-group">
            <label>텍스트 색상 *</label>
            <div class="radio-group">
              <label class="radio-option">
                <input
                  type="radio"
                  value="BLACK"
                  v-model="form.textColor"
                />
                <span class="radio-label">검은색</span>
              </label>
              <label class="radio-option">
                <input
                  type="radio"
                  value="WHITE"
                  v-model="form.textColor"
                />
                <span class="radio-label">흰색</span>
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
              :disabled="!form.name.trim() || isSubmitting"
            >
              {{ isSubmitting ? '처리 중...' : (isEditing ? '레이블 수정' : '레이블 생성') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { labelApi } from '@/services/api'

// 상태 관리
const loading = ref(true)
const labels = ref([])
const showModal = ref(false)
const isEditing = ref(false)
const editingLabelId = ref(null)
const isSubmitting = ref(false)
const formError = ref('')

// 폼 데이터
const form = reactive({
  name: '',
  description: '',
  color: '#0075ca',
  textColor: 'WHITE'
})

// 레이블 목록 로드
const loadLabels = async () => {
  loading.value = true
  try {
    const response = await labelApi.getLabels()
    labels.value = response.data.data.labels
    console.log('레이블 로드 완료:', labels.value.length)
  } catch (error) {
    console.error('레이블 로드 실패:', error)
  } finally {
    loading.value = false
  }
}

// 생성 모달 열기
const openCreateModal = () => {
  isEditing.value = false
  editingLabelId.value = null
  resetForm()
  showModal.value = true
}

// 편집 모달 열기
const openEditModal = (label) => {
  isEditing.value = true
  editingLabelId.value = label.id
  form.name = label.name
  form.description = label.description || ''
  form.color = label.color
  form.textColor = label.textColor
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
  form.name = ''
  form.description = ''
  form.color = '#0075ca'
  form.textColor = 'WHITE'
}

// 랜덤 색상 생성
const generateRandomColor = () => {
  const colors = [
    '#d73a49', '#0075ca', '#28a745', '#ffd33d', '#f66a0a',
    '#6f42c1', '#e1e4e8', '#586069', '#0366d6', '#2cbe4e',
    '#d1242f', '#f9c513', '#ff6347', '#20b2aa', '#9932cc',
    '#ff1493', '#00ced1', '#ffa500', '#adff2f', '#ff69b4'
  ]
  const randomColor = colors[Math.floor(Math.random() * colors.length)]
  form.color = randomColor
  
  // 색상 밝기에 따라 텍스트 색상 자동 조정
  const brightness = getBrightness(randomColor)
  form.textColor = brightness > 128 ? 'BLACK' : 'WHITE'
}

// 색상 밝기 계산 (0-255)
const getBrightness = (hexColor) => {
  const hex = hexColor.replace('#', '')
  const r = parseInt(hex.substr(0, 2), 16)
  const g = parseInt(hex.substr(2, 2), 16)
  const b = parseInt(hex.substr(4, 2), 16)
  return (r * 299 + g * 587 + b * 114) / 1000
}

// 폼 제출
const submitForm = async () => {
  if (!form.name.trim()) {
    formError.value = '레이블 이름을 입력해주세요.'
    return
  }

  if (!/^#([0-9A-Fa-f]{6})$/.test(form.color)) {
    formError.value = '올바른 색상 형식을 입력해주세요. (예: #ff0000)'
    return
  }

  isSubmitting.value = true
  formError.value = ''

  try {
    const payload = {
      name: form.name.trim(),
      description: form.description.trim() || null,
      color: form.color.toUpperCase(),
      textColor: form.textColor
    }

    if (isEditing.value) {
      // 레이블 수정
      await labelApi.updateLabel(editingLabelId.value, payload)
      console.log('레이블 수정 완료')
    } else {
      // 레이블 생성
      await labelApi.createLabel(payload)
      console.log('레이블 생성 완료')
    }

    // 목록 새로고침
    await loadLabels()
    closeModal()
    
  } catch (error) {
    console.error('레이블 저장 실패:', error)
    formError.value = error.response?.data?.message || '레이블 저장에 실패했습니다.'
  } finally {
    isSubmitting.value = false
  }
}

// 레이블 삭제
const deleteLabel = async (label) => {
  if (!confirm(`"${label.name}" 레이블을 삭제하시겠습니까?`)) {
    return
  }

  try {
    await labelApi.deleteLabel(label.id)
    console.log('레이블 삭제 완료')
    
    // 목록에서 제거
    labels.value = labels.value.filter(l => l.id !== label.id)
    
  } catch (error) {
    console.error('레이블 삭제 실패:', error)
    alert('레이블 삭제에 실패했습니다.')
  }
}

// 초기 데이터 로드
onMounted(() => {
  loadLabels()
})
</script>

<style scoped>
.label-manage-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e1e5e9;
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

.btn-new-label {
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

.btn-new-label:hover {
  background: #2c974b;
}

.loading {
  text-align: center;
  padding: 60px 20px;
  color: #656d76;
  font-size: 16px;
}

.labels-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.label-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #fff;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  transition: border-color 0.2s;
}

.label-item:hover {
  border-color: #b1b8c0;
}

.label-preview {
  min-width: 120px;
}

.label-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.label-details {
  flex: 1;
  min-width: 0;
}

.label-name {
  font-size: 16px;
  font-weight: 600;
  color: #24292f;
  margin: 0 0 4px 0;
}

.label-description {
  color: #656d76;
  font-size: 14px;
  margin: 0;
  word-break: break-word;
}

.label-actions {
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

.preview-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #f6f8fa;
  border-radius: 6px;
}

.preview-section h3 {
  font-size: 14px;
  font-weight: 600;
  color: #24292f;
  margin: 0 0 12px 0;
}

.label-preview-large {
  padding: 6px 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
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

.color-input-group {
  display: flex;
  gap: 8px;
  align-items: center;
}

.color-picker {
  width: 40px;
  height: 40px;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  cursor: pointer;
  background: none;
}

.color-text {
  flex: 1;
  min-width: 0;
}

.btn-random-color {
  padding: 8px;
  border: 1px solid #d0d7de;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
  color: #656d76;
  transition: all 0.2s;
}

.btn-random-color:hover {
  background: #f6f8fa;
  color: #24292f;
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
  
  .label-item {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .label-preview {
    min-width: auto;
  }
  
  .label-actions {
    justify-content: flex-start;
  }
  
  .modal {
    margin: 20px;
    width: calc(100% - 40px);
  }
  
  .color-input-group {
    flex-wrap: wrap;
  }
}
</style>