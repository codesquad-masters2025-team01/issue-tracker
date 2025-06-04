package codesquad.team01.issuetracker.comment.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.team01.issuetracker.comment.constants.CommentConstants;
import codesquad.team01.issuetracker.comment.dto.CommentDto;
import codesquad.team01.issuetracker.comment.exception.CommentAccessForbiddenException;
import codesquad.team01.issuetracker.comment.exception.CommentNotFoundException;
import codesquad.team01.issuetracker.comment.repository.CommentRepository;
import codesquad.team01.issuetracker.common.dto.CursorDto;
import codesquad.team01.issuetracker.common.util.CursorEncoder;
import codesquad.team01.issuetracker.issue.exception.IssueNotFoundException;
import codesquad.team01.issuetracker.issue.repository.IssueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommentService {

	private final CommentRepository commentRepository;
	private final IssueRepository issueRepository;

	private final CursorEncoder cursorEncoder;

	public CommentDto.CommentListResponse getCommentsByIssueIdWithCursor(Integer issueId, CursorDto.CursorData cursor) {
		log.info("이슈 댓글 목록 무한스크롤 조회: issueId={}, cursor={}", issueId, cursor);

		// 이슈 존재 확인
		validateIssueExists(issueId);

		// 댓글 조회 (pageSize + 1로 다음 페이지 존재 여부 확인)
		List<CommentDto.CommentRow> commentRows = commentRepository.findCommentsByIssueIdWithCursor(
			issueId, cursor, CommentConstants.PAGE_SIZE);

		boolean hasNext = commentRows.size() > CommentConstants.PAGE_SIZE;

		// 실제 반환할 댓글 목록 (PAGE_SIZE만큼만)
		List<CommentDto.CommentRow> pagedComments = hasNext ?
			commentRows.subList(0, CommentConstants.PAGE_SIZE) : commentRows;

		if (pagedComments.isEmpty()) {
			log.debug("조건에 맞는 댓글이 없습니다: issueId={}", issueId);
			return CommentDto.CommentListResponse.builder()
				.totalCount(0)
				.comments(List.of())
				.cursor(CursorDto.CursorResponse.builder()
					.next(null)
					.hasNext(false)
					.build())
				.build();
		}

		// 응답 DTO로 변환
		List<CommentDto.CommentResponse> comments = pagedComments.stream()
			.map(CommentDto.CommentRow::toResponse)
			.toList();

		// 다음 페이지 커서 생성
		String nextCursor = null;
		if (hasNext && !pagedComments.isEmpty()) {
			CommentDto.CommentRow lastComment = pagedComments.getLast();
			CursorDto.CursorData nextCursorData = CursorDto.CursorData.builder()
				.id(lastComment.commentId())
				.createdAt(lastComment.commentCreatedAt())
				.build();
			nextCursor = cursorEncoder.encode(nextCursorData);
		}

		log.info("이슈 댓글 무한스크롤 조회 완료: issueId={}, commentCount={}, hasNext={}",
			issueId, comments.size(), hasNext);

		return CommentDto.CommentListResponse.builder()
			.totalCount(comments.size())
			.comments(comments)
			.cursor(CursorDto.CursorResponse.builder()
				.next(nextCursor)
				.hasNext(hasNext)
				.build())
			.build();
	}

	@Transactional
	public CommentDto.CommentResponse createComment(Integer issueId, CommentDto.CreateRequest request,
		Integer currentUserId) {
		log.info("댓글 생성: issueId={}, userId={}", issueId, currentUserId);

		// 이슈 존재 확인
		validateIssueExists(issueId);

		LocalDateTime now = LocalDateTime.now();
		Integer commentId = commentRepository.createComment(
			request.content(),
			currentUserId,
			issueId,
			now
		);

		log.info("댓글 생성 완료: commentId={}, issueId={}", commentId, issueId);

		// 생성된 댓글 조회 후 반환
		return getComment(commentId);
	}

	@Transactional
	public CommentDto.CommentResponse updateComment(Integer commentId, CommentDto.UpdateRequest request,
		Integer currentUserId) {
		log.info("댓글 수정: commentId={}, userId={}", commentId, currentUserId);

		// 댓글 존재 확인 및 권한 검증
		CommentDto.CommentDetails comment = commentRepository.findCommentById(commentId)
			.orElseThrow(() -> new CommentNotFoundException(commentId));

		// 작성자 권한 확인
		// if (comment.writerId() != currentUserId) {
		// 	throw new CommentAccessForbiddenException(commentId, currentUserId);
		// }

		// 댓글이 속한 이슈 존재 확인
		validateIssueExists(comment.issueId());

		LocalDateTime now = LocalDateTime.now();
		commentRepository.updateComment(commentId, request.content(), now);

		log.info("댓글 수정 완료: commentId={}", commentId);

		// 수정된 댓글 조회 후 반환
		return getComment(commentId);
	}

	@Transactional
	public void deleteComment(Integer commentId, Integer currentUserId) {
		log.info("댓글 삭제: commentId={}, userId={}", commentId, currentUserId);

		// 댓글 존재 확인 및 권한 검증
		CommentDto.CommentDetails comment = commentRepository.findCommentById(commentId)
			.orElseThrow(() -> new CommentNotFoundException(commentId));

		// 작성자 권한 확인
		if (comment.writerId() != currentUserId) {
			throw new CommentAccessForbiddenException(commentId, currentUserId);
		}

		// 댓글이 속한 이슈 존재 확인
		validateIssueExists(comment.issueId());

		LocalDateTime now = LocalDateTime.now();
		commentRepository.deleteComment(commentId, now);

		log.info("댓글 삭제 완료: commentId={}", commentId);
	}

	private void validateIssueExists(Integer issueId) {
		try {
			issueRepository.findIssueById(issueId);
		} catch (Exception e) {
			throw new IssueNotFoundException("존재하지 않는 이슈입니다: " + issueId);
		}
	}

	private CommentDto.CommentResponse getComment(Integer commentId) {

		CommentDto.CommentDetails comment = commentRepository.findCommentById(commentId)
			.orElseThrow(() -> new CommentNotFoundException(commentId));

		// 댓글이 속한 이슈 존재 확인
		validateIssueExists(comment.issueId());

		// 작성자 정보 조회
		List<CommentDto.CommentRow> commentRows = commentRepository.findCommentsByIssueId(comment.issueId());

		return commentRows.stream()
			.filter(row -> row.commentId() == commentId)
			.map(CommentDto.CommentRow::toResponse)
			.findFirst()
			.orElseThrow(() -> new CommentNotFoundException(commentId));
	}
}
