<template>
  <div class="markdown-editor">
    <!-- 에디터 탭 -->
    <div class="editor-tabs">
      <button 
        type="button"
        :class="['tab', { active: activeTab === 'write' }]"
        @click="activeTab = 'write'"
      >
        작성
      </button>
      <button 
        type="button"
        :class="['tab', { active: activeTab === 'preview' }]"
        @click="activeTab = 'preview'"
      >
        미리보기
      </button>
      
      <!-- 이미지 업로드 버튼 -->
      <div class="tab-actions">
        <input
          ref="fileInput"
          type="file"
          accept="image/*"
          multiple
          @change="handleFileSelect"
          style="display: none"
        />
        <button
          type="button"
          class="btn-upload"
          @click="$refs.fileInput.click()"
          :disabled="isUploading"
          title="이미지 업로드"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
            <path d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"/>
          </svg>
          {{ isUploading ? '업로드 중...' : '이미지' }}
        </button>
      </div>
    </div>

    <!-- 에디터 내용 -->
    <div class="editor-content">
      <!-- 작성 모드 -->
      <div v-if="activeTab === 'write'" class="write-mode">
        <textarea
          ref="textarea"
          :value="modelValue"
          @input="handleInput"
          @drop="handleDrop"
          @dragover.prevent
          @paste="handlePaste"
          :placeholder="placeholder"
          class="markdown-textarea"
          :rows="rows"
        ></textarea>
        
        <!-- 업로드 진행 상태 -->
        <div v-if="uploadQueue.length > 0" class="upload-status">
          <div v-for="upload in uploadQueue" :key="upload.id" class="upload-item">
            <span class="upload-filename">{{ upload.filename }}</span>
            <div class="upload-progress">
              <div 
                class="upload-progress-bar" 
                :style="{ width: upload.progress + '%' }"
              ></div>
            </div>
            <span class="upload-percentage">{{ upload.progress }}%</span>
          </div>
        </div>
      </div>

      <!-- 미리보기 모드 -->
      <div v-else class="preview-mode">
        <div 
          v-if="!modelValue.trim()" 
          class="empty-preview"
        >
          내용을 입력하면 미리보기가 표시됩니다.
        </div>
        <div 
          v-else
          class="markdown-preview"
          v-html="renderedMarkdown"
        ></div>
      </div>
    </div>

    <!-- 드래그 오버레이 -->
    <div v-if="isDragOver" class="drag-overlay">
      <div class="drag-message">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="currentColor">
          <path d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"/>
        </svg>
        <p>이미지를 여기에 드래그하세요</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { marked } from 'marked'
import { fileApi } from '@/services/api'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '마크다운을 입력하세요...'
  },
  rows: {
    type: Number,
    default: 10
  }
})

const emit = defineEmits(['update:modelValue'])

// 상태 관리
const activeTab = ref('write')
const isDragOver = ref(false)
const isUploading = ref(false)
const uploadQueue = ref([])
const textarea = ref(null)
const fileInput = ref(null)

// 마크다운 렌더링
const renderedMarkdown = computed(() => {
  if (!props.modelValue) return ''
  
  try {
    return marked(props.modelValue, {
      breaks: true,
      gfm: true,
      sanitize: false // 실제 운영에서는 sanitize 라이브러리 사용 권장
    })
  } catch (error) {
    console.error('Markdown 렌더링 오류:', error)
    return props.modelValue.replace(/\n/g, '<br>')
  }
})

// 입력 처리
const handleInput = (event) => {
  emit('update:modelValue', event.target.value)
}

// 파일 선택 처리
const handleFileSelect = (event) => {
  const files = Array.from(event.target.files)
  uploadFiles(files)
  // 파일 입력 초기화
  event.target.value = ''
}

// 드래그 앤 드롭 처리
const handleDrop = (event) => {
  event.preventDefault()
  isDragOver.value = false
  
  const files = Array.from(event.dataTransfer.files).filter(file => 
    file.type.startsWith('image/')
  )
  
  if (files.length > 0) {
    uploadFiles(files)
  }
}

// 붙여넣기 처리
const handlePaste = (event) => {
  const items = Array.from(event.clipboardData.items)
  const imageItems = items.filter(item => item.type.startsWith('image/'))
  
  if (imageItems.length > 0) {
    event.preventDefault()
    const files = imageItems.map(item => item.getAsFile()).filter(Boolean)
    uploadFiles(files)
  }
}

// 파일 업로드
const uploadFiles = async (files) => {
  if (!files.length) return
  
  isUploading.value = true
  
  for (const file of files) {
    const uploadId = Date.now() + Math.random()
    const uploadItem = {
      id: uploadId,
      filename: file.name,
      progress: 0
    }
    
    uploadQueue.value.push(uploadItem)
    
    try {
      // 플레이스홀더 텍스트 즉시 삽입
      const placeholder = `![업로드 중...](${file.name})`
      insertTextAtCursor(placeholder)
      
      // 실제 업로드
      const formData = new FormData()
      formData.append('image', file)
      
      const response = await fileApi.uploadImage(formData, {
        onUploadProgress: (progressEvent) => {
          const progress = Math.round(
            (progressEvent.loaded * 100) / progressEvent.total
          )
          const uploadIndex = uploadQueue.value.findIndex(u => u.id === uploadId)
          if (uploadIndex !== -1) {
            uploadQueue.value[uploadIndex].progress = progress
          }
        }
      })
      
      const imageUrl = response.data.data.imageUrl
      const markdownImage = `![${file.name}](${imageUrl})`
      
      // 플레이스홀더를 실제 이미지 URL로 교체
      const currentValue = props.modelValue
      const updatedValue = currentValue.replace(placeholder, markdownImage)
      emit('update:modelValue', updatedValue)
      
      console.log('이미지 업로드 완료:', imageUrl)
      
    } catch (error) {
      console.error('이미지 업로드 실패:', error)
      
      // 실패한 플레이스홀더 제거
      const currentValue = props.modelValue
      const placeholder = `![업로드 중...](${file.name})`
      const updatedValue = currentValue.replace(placeholder, `![업로드 실패: ${file.name}]()`)
      emit('update:modelValue', updatedValue)
      
      alert(`이미지 업로드 실패: ${file.name}`)
    } finally {
      // 업로드 큐에서 제거
      uploadQueue.value = uploadQueue.value.filter(u => u.id !== uploadId)
    }
  }
  
  isUploading.value = false
}

// 커서 위치에 텍스트 삽입
const insertTextAtCursor = (text) => {
  const textareaEl = textarea.value
  if (!textareaEl) return
  
  const start = textareaEl.selectionStart
  const end = textareaEl.selectionEnd
  const currentValue = props.modelValue
  
  const newValue = currentValue.substring(0, start) + text + currentValue.substring(end)
  emit('update:modelValue', newValue)
  
  // 커서 위치 조정
  nextTick(() => {
    const newCursorPos = start + text.length
    textareaEl.setSelectionRange(newCursorPos, newCursorPos)
    textareaEl.focus()
  })
}

// 드래그 이벤트 처리
const handleDragOver = (event) => {
  event.preventDefault()
  isDragOver.value = true
}

const handleDragLeave = (event) => {
  // 자식 요소로 드래그가 이동하는 경우 무시
  if (!event.currentTarget.contains(event.relatedTarget)) {
    isDragOver.value = false
  }
}
</script>

<style scoped>
.markdown-editor {
  position: relative;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  background: #fff;
}

.editor-tabs {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #d0d7de;
  background: #f6f8fa;
  border-radius: 6px 6px 0 0;
}

.tab {
  padding: 10px 16px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 14px;
  color: #656d76;
  transition: all 0.2s;
  border-bottom: 2px solid transparent;
}

.tab.active {
  color: #24292f;
  background: #fff;
  border-bottom-color: #fd8c73;
}

.tab:hover:not(.active) {
  color: #24292f;
  background: rgba(255, 255, 255, 0.5);
}

.tab-actions {
  margin-left: auto;
  padding: 8px 16px;
}

.btn-upload {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: 1px solid #d0d7de;
  background: #fff;
  color: #24292f;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.btn-upload:hover:not(:disabled) {
  background: #f6f8fa;
  border-color: #b1b8c0;
}

.btn-upload:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.editor-content {
  position: relative;
  min-height: 200px;
}

.write-mode {
  position: relative;
}

.markdown-textarea {
  width: 100%;
  border: none;
  outline: none;
  padding: 12px;
  font-size: 14px;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  resize: vertical;
  min-height: 200px;
  line-height: 1.5;
}

.preview-mode {
  padding: 12px;
  min-height: 200px;
}

.empty-preview {
  color: #656d76;
  font-style: italic;
  text-align: center;
  padding: 60px 20px;
}

.markdown-preview {
  color: #24292f;
  line-height: 1.6;
}

.markdown-preview :deep(h1),
.markdown-preview :deep(h2),
.markdown-preview :deep(h3),
.markdown-preview :deep(h4),
.markdown-preview :deep(h5),
.markdown-preview :deep(h6) {
  margin: 24px 0 16px 0;
  font-weight: 600;
  line-height: 1.25;
}

.markdown-preview :deep(h1) { font-size: 2em; border-bottom: 1px solid #eaecef; padding-bottom: 10px; }
.markdown-preview :deep(h2) { font-size: 1.5em; border-bottom: 1px solid #eaecef; padding-bottom: 8px; }
.markdown-preview :deep(h3) { font-size: 1.25em; }

.markdown-preview :deep(p) {
  margin: 16px 0;
}

.markdown-preview :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.markdown-preview :deep(code) {
  padding: 2px 4px;
  background: rgba(175, 184, 193, 0.2);
  border-radius: 3px;
  font-size: 85%;
}

.markdown-preview :deep(pre) {
  padding: 16px;
  background: #f6f8fa;
  border-radius: 6px;
  overflow-x: auto;
  margin: 16px 0;
}

.markdown-preview :deep(blockquote) {
  padding: 0 16px;
  color: #656d76;
  border-left: 4px solid #d0d7de;
  margin: 16px 0;
}

.markdown-preview :deep(ul),
.markdown-preview :deep(ol) {
  padding-left: 24px;
  margin: 16px 0;
}

.markdown-preview :deep(li) {
  margin: 4px 0;
}

.upload-status {
  position: absolute;
  bottom: 12px;
  right: 12px;
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid #d0d7de;
  border-radius: 6px;
  padding: 8px;
  min-width: 200px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.upload-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
  font-size: 12px;
}

.upload-item:last-child {
  margin-bottom: 0;
}

.upload-filename {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.upload-progress {
  width: 60px;
  height: 4px;
  background: #e1e5e9;
  border-radius: 2px;
  overflow: hidden;
}

.upload-progress-bar {
  height: 100%;
  background: #1f883d;
  transition: width 0.2s ease;
}

.upload-percentage {
  font-weight: 500;
  color: #1f883d;
  min-width: 35px;
  text-align: right;
}

.drag-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(33, 136, 255, 0.1);
  border: 2px dashed #2188ff;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

.drag-message {
  text-align: center;
  color: #2188ff;
}

.drag-message svg {
  opacity: 0.6;
  margin-bottom: 8px;
}

.drag-message p {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

@media (max-width: 768px) {
  .tab-actions {
    padding: 4px 8px;
  }
  
  .btn-upload {
    padding: 4px 8px;
    font-size: 11px;
  }
  
  .upload-status {
    position: static;
    margin-top: 8px;
  }
}
</style>