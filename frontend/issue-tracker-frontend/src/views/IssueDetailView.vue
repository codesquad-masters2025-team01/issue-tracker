<template>
  <div class="issue-detail-container">
    <div v-if="loading" class="loading">
      이슈를 불러오는 중...
    </div>

    <div v-else-if="issue" class="issue-detail">
      <!-- 이슈 헤더 -->
      <div class="issue-header">
        <div class="issue-title-section">
          <h1 class="issue-title" v-if="!isEditingTitle" @click="startEditTitle">
            {{ issue.title }}
          </h1>
          <div v-else class="edit-title">
            <input
              type="text"
              v-model="editTitle"
              class="title-input"
              @keyup.enter="saveTitle"
              @keyup.esc="cancelEditTitle"
              ref="titleInput"
            />
            <div class="edit-actions">
              <button @click="saveTitle" class="btn-save">저장</button>
              <button @click="cancelEditTitle" class="btn-cancel">취소</button>
            </div>
          </div>
          <div class="issue-meta">
            <span class="issue-number">#{{ issue.id }}</span>
            <span 
              class="issue-state"
              :class="{ 
                open: issue.state === 'open', 
                closed: issue.state === 'closed' 
              }"
            >
              {{ issue.state === 'open' ? '열린 이슈' : '닫힌 이슈' }}
            </span>
            <span class="issue-author">
              {{ issue.writer.username }}님이 {{ formatDate(issue.createdAt) }}에 작성
            </span>
          </div>
        </div>

        <div class="issue-actions">
          <button 
            type="button"
            class="btn-state-toggle"
            :class="{ 
              close: issue.state === 'open', 
              reopen: issue.state === 'closed' 
            }"
            @click="toggleIssueState"
            :disabled="isToggling"
          >
            {{ isToggling ? '처리 중...' : (issue.state === 'open' ? '이슈 닫기' : '이슈 다시 열기') }}
          </button>
          <button 
            type="button"
            class="btn-delete-issue"
            @click="deleteIssue"
            style="margin-left: 8px; background: #d1242f; color: white; border: 1px solid #d1242f; border-radius: 6px; padding: 8px 16px; font-size: 14px; font-weight: 500; cursor: pointer;"
          >
            이슈 삭제
          </button>
        </div>
      </div>

      <!-- 이슈 내용 -->
      <div class="issue-content-section">
        <div class="content-header">
          <div class="author-info">
            <img 
              :src="issue.writer.profileImageUrl" 
              :alt="issue.writer.username"
              class="author-avatar"
            />
            <div class="author-details">
              <strong>{{ issue.writer.username }}</strong>
              <span>{{ formatDate(issue.createdAt) }}</span>
            </div>
          </div>
        </div>

        <!-- 이슈 내용 (마크다운 렌더링) -->
        <div class="issue-content">
          <div v-if="!isEditingContent" class="markdown-content" v-html="renderMarkdown(issue.content)" @click="startEditContent"></div>
          <div v-else class="edit-content">
            <MarkdownEditor
              v-model="editContent"
              :rows="12"
              ref="contentEditor"
            />
            <div class="edit-actions">
              <button @click="saveContent" class="btn-save">저장</button>
              <button @click="cancelEditContent" class="btn-cancel">취소</button>
            </div>
          </div>
        </div>

        <!-- 이슈 메타데이터 -->
        <div class="issue-metadata">
          <div v-if="issue.labels && issue.labels.length > 0" class="metadata-section">
            <h4>레이블</h4>
            <div class="labels">
              <span 
                v-for="label in issue.labels" 
                :key="label.id"
                class="label"
                :style="{ 
                  backgroundColor: label.color, 
                  color: label.textColor === 'WHITE' ? '#fff' : '#000' 
                }"
              >
                {{ label.name }}
              </span>
            </div>
          </div>

          <div v-if="issue.milestone" class="metadata-section">
            <h4>마일스톤</h4>
            <div class="milestone">
              <span class="milestone-title">{{ issue.milestone.title }}</span>
              <div v-if="issue.milestone.dueDate" class="milestone-due">
                마감일: {{ formatDate(issue.milestone.dueDate) }}
              </div>
            </div>
          </div>

          <div v-if="issue.assignees && issue.assignees.length > 0" class="metadata-section">
            <h4>담당자</h4>
            <div class="assignees">
              <div 
                v-for="assignee in issue.assignees" 
                :key="assignee.id"
                class="assignee"
              >
                <img 
                  :src="assignee.profileImageUrl" 
                  :alt="assignee.username"
                  class="assignee-avatar"
                />
                <span>{{ assignee.username }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 댓글 섹션 -->
      <div class="comments-section">
        <div class="new-comment">
          <div class="comment-form">
            <div class="comment-author">
              <img 
                :src="currentUser?.profileImageUrl || 'https://issue-tracker-team01-bucket.s3.ap-northeast-2.amazonaws.com/images/2025/06/05/72a070f0-2a89-4f95-8fc1-2b9fc0b69b0f.jpeg'" 
                :alt="currentUser?.username"
                class="author-avatar"
              />
            </div>
            <div class="comment-input">
              <MarkdownEditor
                v-model="newComment"
                placeholder="댓글을 마크다운으로 작성해주세요...\n\n**기본 사용법:**\n- **굵은 글씨**: **텍스트**\n- *기울임 글씨*: *텍스트*  \n- 링크: [텍스트](URL)\n- 이미지: 드래그하거나 붙여넣기, 또는 ![이미지](URL)\n- 코드: \`인라인 코드\` 또는 \`\`\`언어명\n- 목록: - 또는 1.\n\n이미지는 드래그 앤 드롭, 붙여넣기로 업로드할 수 있습니다."
                :rows="6"
              />
              <div class="comment-actions">
                <button 
                  type="button"
                  class="btn-submit"
                  @click="submitComment"
                  :disabled="!newComment.trim() || isSubmittingComment"
                >
                  {{ isSubmittingComment ? '작성 중...' : '댓글 작성' }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <h3 class="comments-title">
          댓글 {{ comments.length }}개
        </h3>

        <!-- 댓글 목록 -->
        <div class="comments-list">
          <div 
            v-for="comment in comments" 
            :key="comment.id"
            class="comment"
          >
            <div class="comment-header">
              <div class="comment-author">
                <img 
                  :src="comment.writer.profileImageUrl" 
                  :alt="comment.writer.username"
                  class="author-avatar"
                />
                <strong>{{ comment.writer.username }}</strong>
                <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
              </div>
              
              <div class="comment-actions">
                <button 
                  type="button"
                  class="btn-edit"
                  @click="startEdit(comment)"
                  :disabled="isEditingComment"
                >
                  수정
                </button>
                <button 
                  type="button"
                  class="btn-delete"
                  @click="deleteComment(comment.id)"
                  :disabled="isDeletingComment"
                >
                  삭제
                </button>
              </div>
            </div>

            <!-- 댓글 수정 모드 -->
            <div v-if="editingComment === comment.id" class="comment-edit">
              <MarkdownEditor
                v-model="editCommentText"
                placeholder="댓글을 수정하세요..."
                :rows="4"
              />
              <div class="edit-actions">
                <button 
                  type="button"
                  class="btn-cancel"
                  @click="cancelEdit"
                >
                  취소
                </button>
                <button 
                  type="button"
                  class="btn-save"
                  @click="saveEdit(comment.id)"
                  :disabled="!editCommentText.trim() || isEditingComment"
                >
                  {{ isEditingComment ? '저장 중...' : '저장' }}
                </button>
              </div>
            </div>

            <!-- 댓글 내용 (읽기 모드) -->
            <div 
              v-else
              class="comment-content markdown-content"
              v-html="renderMarkdown(comment.content)"
            ></div>
          </div>
        </div>

        <!-- 무한 스크롤 로더 -->
        <div ref="loadMoreTrigger" class="load-more-trigger">
          <div v-if="isLoadingMore" class="loading-more">
            댓글을 더 불러오는 중...
          </div>
        </div>
      </div>
    </div>

    <div v-else class="error">
      이슈를 찾을 수 없습니다.
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { issueApi, commentApi } from '@/services/api'
import { getUserFromToken } from '@/utils/auth'
import MarkdownEditor from '@/components/MarkdownEditor.vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import hljs from 'highlight.js'
import api from '@/services/api'

const route = useRoute()
const router = useRouter()


// 상태 관리
const loading = ref(true)
const issue = ref(null)
const comments = ref([])
const newComment = ref('')
const isSubmittingComment = ref(false)
const isLoadingMore = ref(false)
const hasMore = ref(true)
const cursor = ref(null)
const loadMoreTrigger = ref(null)
let observer = null
const isToggling = ref(false)

// 댓글 수정 관련
const editingComment = ref(null)
const editCommentText = ref('')
const isEditingComment = ref(false)
const isDeletingComment = ref(false)

// 이슈 수정 관련 상태
const isEditingTitle = ref(false)
const isEditingContent = ref(false)
const editTitle = ref('')
const editContent = ref('')
const titleInput = ref(null)
const contentEditor = ref(null)

// 현재 사용자
const currentUser = computed(() => {
  const token = localStorage.getItem('accessToken')
  if (!token) return null
  
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return {
      id: payload.id,
      username: payload.username,
      profileImageUrl: payload.profileImageUrl
    }
  } catch (error) {
    console.error('토큰 파싱 실패:', error)
    return null
  }
})

// 마크다운 렌더링 함수
const renderMarkdown = (content) => {
  if (!content) return '<p class="empty-content">내용이 없습니다.</p>'
  
  try {
    // 마크다운을 HTML로 변환
    const rawHtml = marked(content, {
      breaks: true,
      gfm: true,
      highlight: function(code, lang) {
        // 코드 하이라이팅 (highlight.js 사용)
        if (lang && hljs.getLanguage(lang)) {
          try {
            return hljs.highlight(code, { language: lang }).value
          } catch (__) {}
        }
        return code
      }
    })
    
    // XSS 방지를 위한 HTML 정제
    return DOMPurify.sanitize(rawHtml, {
      ALLOWED_TAGS: ['p', 'br', 'strong', 'em', 'u', 's', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 
                     'ul', 'ol', 'li', 'blockquote', 'code', 'pre', 'a', 'img', 'table', 'thead', 
                     'tbody', 'tr', 'th', 'td', 'hr', 'div', 'span'],
      ALLOWED_ATTR: ['href', 'title', 'src', 'alt', 'class', 'data-language', 'target', 'rel']
    })
  } catch (error) {
    console.error('Markdown 렌더링 오류:', error)
    return content.replace(/\n/g, '<br>')
  }
}

// 날짜 포맷팅
const formatDate = (dateString) => {
  const utc = new Date(dateString);
  const kst = new Date(utc.getTime() + 9 * 60 * 60 * 1000);
  return kst.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

// 이슈 상태 토글
const toggleIssueState = async () => {
  isToggling.value = true
  try {
    const newState = issue.value.state === 'open' ? 'close' : 'open'
    await issueApi.updateIssue(issue.value.id, { action: newState })
    
    // 이슈 상태 업데이트
    issue.value.state = newState === 'close' ? 'closed' : 'open'
    if (newState === 'close') {
      issue.value.closedAt = new Date().toISOString()
    } else {
      issue.value.closedAt = null
    }
  } catch (error) {
    console.error('이슈 상태 변경 실패:', error)
    alert('이슈 상태 변경에 실패했습니다.')
  } finally {
    isToggling.value = false
  }
}

// 댓글 작성
const submitComment = async () => {
  if (!newComment.value.trim()) return

  isSubmittingComment.value = true
  try {
    const response = await commentApi.createComment(issue.value.id, newComment.value.trim())

    comments.value.push(response.data.data)
    newComment.value = ''
    issue.value.commentCount = (issue.value.commentCount || 0) + 1

    console.log('댓글 작성 완료')
  } catch (error) {
    console.error('댓글 작성 실패:', error)
    alert('댓글 작성에 실패했습니다.')
  } finally {
    isSubmittingComment.value = false
  }
}

// 댓글 수정 시작
const startEdit = (comment) => {
  editingComment.value = comment.id
  editCommentText.value = comment.content
}

// 댓글 수정 취소
const cancelEdit = () => {
  editingComment.value = null
  editCommentText.value = ''
}

// 댓글 수정 저장
const saveEdit = async (commentId) => {
  if (!editCommentText.value.trim()) return

  isEditingComment.value = true
  try {
    const response = await commentApi.updateComment(commentId, editCommentText.value.trim())

    // 댓글 목록에서 해당 댓글 업데이트
    const index = comments.value.findIndex(c => c.id === commentId)
    if (index !== -1) {
      comments.value[index] = response.data.data
    }

    editingComment.value = null
    editCommentText.value = ''

    console.log('댓글 수정 완료')
  } catch (error) {
    console.error('댓글 수정 실패:', error)
    alert('댓글 수정에 실패했습니다.')
  } finally {
    isEditingComment.value = false
  }
}

// 댓글 삭제
const deleteComment = async (commentId) => {
  if (!confirm('댓글을 삭제하시겠습니까?')) return

  isDeletingComment.value = true
  try {
    await commentApi.deleteComment(commentId)

    // 댓글 목록에서 제거
    comments.value = comments.value.filter(c => c.id !== commentId)
    issue.value.commentCount = Math.max(0, (issue.value.commentCount || 0) - 1)

    console.log('댓글 삭제 완료')
  } catch (error) {
    console.error('댓글 삭제 실패:', error)
    alert('댓글 삭제에 실패했습니다.')
  } finally {
    isDeletingComment.value = false
  }
}

// 이슈 로드
const loadIssue = async () => {
  try {
    const response = await issueApi.getIssue(route.params.id)
    issue.value = response.data.data
  } catch (error) {
    console.error('이슈 로드 실패:', error)
  }
}

// 댓글 로드
const loadComments = async (isLoadMore = false) => {
  if (isLoadMore) {
    isLoadingMore.value = true
  }

  try {
    const response = await commentApi.getComments(route.params.id, cursor.value)
    const newComments = response.data.data.comments

    if (isLoadMore) {
      comments.value.push(...newComments)
    } else {
      comments.value = newComments
    }

    cursor.value = response.data.data.cursor.next
    hasMore.value = response.data.data.cursor.hasNext
  } catch (error) {
    console.error('댓글 로드 실패:', error)
  } finally {
    if (isLoadMore) {
      isLoadingMore.value = false
    }
  }
}

// 무한 스크롤 설정
const setupInfiniteScroll = () => {
  // 기존 observer가 있으면 정리
  if (observer) {
    observer.disconnect()
  }

  // loadMoreTrigger.value가 유효할 때만 observer 설정
  if (loadMoreTrigger.value) {
    observer = new IntersectionObserver(
      (entries) => {
        const entry = entries[0]
        if (entry.isIntersecting && hasMore.value && !isLoadingMore.value) {
          console.log('무한스크롤 트리거됨') // 디버깅용
          loadComments(true)
        }
      },
      {
        threshold: 0.1,
        rootMargin: '100px' // 이 부분도 추가
      }
    )

    observer.observe(loadMoreTrigger.value)
    console.log('무한스크롤 observer 등록됨') // 디버깅용
  } else {
    console.log('loadMoreTrigger 요소가 아직 준비되지 않음');
  }
}

// cleanupInfiniteScroll 함수
const cleanupInfiniteScroll = () => {
  if (observer) {
    observer.disconnect()
    observer = null
  }
}

// 제목 수정 시작
const startEditTitle = () => {
  if (issue.value.state === 'closed') {
    alert('닫힌 이슈는 수정할 수 없습니다.')
    return
  }
  editTitle.value = issue.value.title
  isEditingTitle.value = true
  nextTick(() => {
    titleInput.value?.focus()
  })
}

// 제목 수정 취소
const cancelEditTitle = () => {
  isEditingTitle.value = false
  editTitle.value = ''
}

// 제목 수정 저장
const saveTitle = async () => {
  if (!editTitle.value.trim()) {
    alert('제목을 입력해주세요.')
    return
  }

  try {
    const response = await issueApi.updateIssue(issue.value.id, {
      title: editTitle.value.trim()
    })
    issue.value = response.data.data
    isEditingTitle.value = false
  } catch (error) {
    console.error('제목 수정 실패:', error)
    alert('제목 수정에 실패했습니다.')
  }
}

// 내용 수정 시작
const startEditContent = () => {
  if (issue.value.state === 'closed') {
    alert('닫힌 이슈는 수정할 수 없습니다.')
    return
  }
  editContent.value = issue.value.content || ''
  isEditingContent.value = true
  nextTick(() => {
    contentEditor.value?.focus()
  })
}

// 내용 수정 취소
const cancelEditContent = () => {
  isEditingContent.value = false
  editContent.value = ''
}

// 내용 수정 저장
const saveContent = async () => {
  try {
    const response = await issueApi.updateIssue(issue.value.id, {
      content: editContent.value.trim() || null
    })
    issue.value = response.data.data
    isEditingContent.value = false
  } catch (error) {
    console.error('내용 수정 실패:', error)
    alert('내용 수정에 실패했습니다.')
  }
}

// 이슈 삭제
const deleteIssue = async () => {
  if (!confirm('정말로 이 이슈를 삭제하시겠습니까?')) return;
  try {
    await api.delete(`/issues/${issue.value.id}`);
    alert('이슈가 삭제되었습니다.');
    router.push('/issues');
  } catch (error) {
    alert('이슈 삭제에 실패했습니다.');
    console.error('이슈 삭제 실패:', error);
  }
}

// onMounted 수정 (setupInfiniteScroll 직접 호출 대신 watch에 의존)
onMounted(async () => {
  try {
    await Promise.all([loadIssue(), loadComments()])

    // DOM 업데이트 후 무한 스크롤 설정 (watch가 처리)
    // nextTick(() => {
    //   setupInfiniteScroll()
    // })

  } catch (error) {
    console.error('초기 데이터 로드 실패:', error)
  } finally {
    loading.value = false
  }
})

// watch loadMoreTrigger ref
watch(loadMoreTrigger, (newValue) => {
  if (newValue) {
    console.log('loadMoreTrigger ref 감지됨, 무한스크롤 설정 시도');
    setupInfiniteScroll();
  }
});

// 맨 아래 onUnmounted 추가
onUnmounted(() => {
  cleanupInfiniteScroll()
})
</script>

<style scoped>
.issue-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.loading, .error {
  text-align: center;
  padding: 60px 20px;
  color: #656d76;
  font-size: 16px;
}

.issue-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e1e5e9;
}

.issue-title {
  font-size: 28px;
  font-weight: 600;
  color: #24292f;
  margin: 0 0 10px 0;
  line-height: 1.2;
}

.issue-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #656d76;
}

.issue-number {
  color: #656d76;
  font-weight: 500;
}

.issue-state {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
}

.issue-state.open {
  background: #2da44e;
  color: white;
}

.issue-state.closed {
  background: #8250df;
  color: white;
}

.btn-state-toggle {
  padding: 8px 16px;
  border: 1px solid;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-state-toggle.close {
  border-color: #d1242f;
  background: #d1242f;
  color: white;
}

.btn-state-toggle.close:hover:not(:disabled) {
  background: #b91c27;
}

.btn-state-toggle.reopen {
  border-color: #2da44e;
  background: #2da44e;
  color: white;
}

.btn-state-toggle.reopen:hover:not(:disabled) {
  background: #2c974b;
}

.btn-state-toggle:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.issue-content-section {
  background: #fff;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  margin-bottom: 30px;
}

.content-header {
  padding: 16px;
  background: #f6f8fa;
  border-bottom: 1px solid #d0d7de;
  border-radius: 6px 6px 0 0;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.author-details {
  display: flex;
  flex-direction: column;
  font-size: 14px;
}

.author-details strong {
  color: #24292f;
}

.author-details span {
  color: #656d76;
  font-size: 12px;
}

.issue-content {
  padding: 20px;
}

.issue-metadata {
  padding: 16px;
  border-top: 1px solid #d0d7de;
  background: #f6f8fa;
  display: flex;
  gap: 30px;
  flex-wrap: wrap;
}

.metadata-section h4 {
  font-size: 12px;
  font-weight: 600;
  color: #656d76;
  text-transform: uppercase;
  margin: 0 0 8px 0;
}

.labels {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.label {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.milestone-title {
  font-weight: 500;
  color: #24292f;
}

.milestone-due {
  font-size: 12px;
  color: #656d76;
  margin-top: 4px;
}

.assignees {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.assignee {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.assignee-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  object-fit: cover;
}

.comments-section {
  margin-top: 30px;
}

.comments-title {
  font-size: 20px;
  font-weight: 600;
  color: #24292f;
  margin: 0 0 20px 0;
}

.comment {
  background: #fff;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  margin-bottom: 20px;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f6f8fa;
  border-bottom: 1px solid #d0d7de;
  border-radius: 6px 6px 0 0;
}

.comment-author {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.comment-date {
  color: #656d76;
  font-weight: normal;
}

.comment-actions {
  display: flex;
  gap: 8px;
}

.btn-edit, .btn-delete {
  padding: 4px 8px;
  border: 1px solid #d0d7de;
  background: #fff;
  color: #24292f;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.btn-edit:hover:not(:disabled) {
  background: #f6f8fa;
}

.btn-delete:hover:not(:disabled) {
  background: #fff5f5;
  border-color: #d1242f;
  color: #d1242f;
}

.btn-edit:disabled, .btn-delete:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.comment-content {
  padding: 16px;
}

.comment-edit {
  padding: 16px;
}

.edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 10px;
}

.btn-cancel, .btn-save {
  padding: 6px 12px;
  border: 1px solid;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
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

.btn-save {
  border-color: #2da44e;
  background: #2da44e;
  color: white;
}

.btn-save:hover:not(:disabled) {
  background: #2c974b;
}

.btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.new-comment {
  margin-top: 30px;
}

.comment-form {
  display: flex;
  gap: 15px;
  background: #fff;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  padding: 16px;
}

.comment-input {
  flex: 1;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.btn-submit {
  padding: 8px 16px;
  border: 1px solid #2da44e;
  background: #2da44e;
  color: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.btn-submit:hover:not(:disabled) {
  background: #2c974b;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.load-more-trigger {
  height: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.loading-more {
  color: #656d76;
  font-size: 14px;
}

/* 마크다운 콘텐츠 스타일링 */
.markdown-content {
  line-height: 1.6;
  color: #24292f;
  word-wrap: break-word;
}

.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3),
.markdown-content :deep(h4),
.markdown-content :deep(h5),
.markdown-content :deep(h6) {
  margin: 24px 0 16px 0;
  font-weight: 600;
  line-height: 1.25;
}

.markdown-content :deep(h1) { 
  font-size: 2em; 
  border-bottom: 1px solid #eaecef; 
  padding-bottom: 10px; 
}

.markdown-content :deep(h2) { 
  font-size: 1.5em; 
  border-bottom: 1px solid #eaecef; 
  padding-bottom: 8px; 
}

.markdown-content :deep(h3) { 
  font-size: 1.25em; 
}

.markdown-content :deep(p) {
  margin: 16px 0;
}

.markdown-content :deep(p:first-child) {
  margin-top: 0;
}

.markdown-content :deep(p:last-child) {
  margin-bottom: 0;
}

.markdown-content :deep(img) {
  max-width: 100%;
  max-height: 160px;
  height: auto;
  display: block;
  margin: 8px 0;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.markdown-content :deep(code) {
  padding: 2px 4px;
  background: rgba(175, 184, 193, 0.2);
  border-radius: 3px;
  font-size: 85%;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}

.markdown-content :deep(pre) {
  padding: 16px;
  background: #f6f8fa;
  border-radius: 6px;
  overflow-x: auto;
  margin: 16px 0;
  border: 1px solid #e1e5e9;
}

.markdown-content :deep(pre code) {
  background: none;
  padding: 0;
  font-size: 12px;
  line-height: 1.45;
}

.markdown-content :deep(blockquote) {
  padding: 0 16px;
  color: #656d76;
  border-left: 4px solid #d0d7de;
  margin: 16px 0;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  padding-left: 24px;
  margin: 16px 0;
}

.markdown-content :deep(li) {
  margin: 4px 0;
}

.markdown-content :deep(table) {
  border-collapse: collapse;
  margin: 16px 0;
  width: 100%;
  border: 1px solid #d0d7de;
}

.markdown-content :deep(th),
.markdown-content :deep(td) {
  border: 1px solid #d0d7de;
  padding: 8px 12px;
  text-align: left;
}

.markdown-content :deep(th) {
  background: #f6f8fa;
  font-weight: 600;
}

.markdown-content :deep(a) {
  color: #0969da;
  text-decoration: none;
}

.markdown-content :deep(a:hover) {
  text-decoration: underline;
}

.markdown-content :deep(hr) {
  border: none;
  border-top: 1px solid #d0d7de;
  margin: 24px 0;
}

.empty-content {
  color: #656d76;
  font-style: italic;
  margin: 0;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .issue-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .issue-metadata {
    flex-direction: column;
    gap: 15px;
  }
  
  .comment-form {
    flex-direction: column;
  }
  
  .comment-header {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  
  .comment-actions {
    justify-content: flex-start;
  }
}

.edit-title {
  margin-bottom: 16px;
}

.title-input {
  width: 100%;
  padding: 8px 12px;
  font-size: 24px;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  margin-bottom: 8px;
}

.edit-content {
  margin-top: 16px;
}

.edit-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.btn-save {
  padding: 6px 12px;
  background: #2da44e;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.btn-save:hover {
  background: #2c974b;
}

.btn-cancel {
  padding: 6px 12px;
  background: #f6f8fa;
  color: #24292f;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  cursor: pointer;
}

.btn-cancel:hover {
  background: #f3f4f6;
}

.issue-title {
  cursor: pointer;
}

.issue-title:hover {
  color: #0969da;
}

.markdown-content {
  cursor: pointer;
}

.markdown-content:hover {
  background: #f6f8fa;
}

.markdown-content img {
  max-width: 100%;
  max-height: 160px;
  height: auto;
  display: block;
  margin: 8px 0;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
</style>