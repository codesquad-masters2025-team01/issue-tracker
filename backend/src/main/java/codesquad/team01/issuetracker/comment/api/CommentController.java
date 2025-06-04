package codesquad.team01.issuetracker.comment.api;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.team01.issuetracker.comment.dto.CommentDto;
import codesquad.team01.issuetracker.comment.service.CommentService;
import codesquad.team01.issuetracker.common.annotation.CurrentUserId;
import codesquad.team01.issuetracker.common.annotation.CursorParam;
import codesquad.team01.issuetracker.common.dto.ApiResponse;
import codesquad.team01.issuetracker.common.dto.CursorDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class CommentController {

	private final CommentService commentService;

	@GetMapping("/v1/issues/{issueId}/comments")
	public ResponseEntity<ApiResponse<CommentDto.CommentListResponse>> getComments(
		@PathVariable @Positive(message = "이슈 ID는 양수여야 합니다") Integer issueId,
		@CursorParam CursorDto.CursorData cursor) {

		log.info("이슈 댓글 목록 조회 요청: issueId={}, cursor={}", issueId, cursor);

		CommentDto.CommentListResponse response = commentService.getCommentsByIssueIdWithCursor(issueId, cursor);

		log.info("이슈 댓글 목록 조회 완료: issueId={}, commentCount={}, hasNext={}",
			issueId, response.totalCount(), response.cursor().hasNext());
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/v1/issues/{issueId}/comments")
	public ResponseEntity<ApiResponse<CommentDto.CommentResponse>> createComment(
		@PathVariable @Positive(message = "이슈 ID는 양수여야 합니다") Integer issueId,
		@Valid @RequestBody CommentDto.CreateRequest request,
		@CurrentUserId Integer currentUserId) {

		log.info("댓글 생성 요청: issueId={}, userId={}", issueId, currentUserId);

		CommentDto.CommentResponse response = commentService.createComment(issueId, request, currentUserId);
		URI location = URI.create("/api/v1/comments/" + response.id());

		log.info("댓글 생성 완료: commentId={}, issueId={}", response.id(), issueId);
		return ResponseEntity.created(location).body(ApiResponse.success(response));
	}

	@PutMapping("/v1/comments/{commentId}")
	public ResponseEntity<ApiResponse<CommentDto.CommentResponse>> updateComment(
		@PathVariable @Positive(message = "댓글 ID는 양수여야 합니다") Integer commentId,
		@Valid @RequestBody CommentDto.UpdateRequest request,
		@CurrentUserId Integer currentUserId) {

		log.info("댓글 수정 요청: commentId={}, userId={}", commentId, currentUserId);

		CommentDto.CommentResponse response = commentService.updateComment(commentId, request, currentUserId);

		log.info("댓글 수정 완료: commentId={}", commentId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@DeleteMapping("/v1/comments/{commentId}")
	public ResponseEntity<Void> deleteComment(
		@PathVariable @Positive(message = "댓글 ID는 양수여야 합니다") Integer commentId,
		@CurrentUserId Integer currentUserId) {

		log.info("댓글 삭제 요청: commentId={}, userId={}", commentId, currentUserId);

		commentService.deleteComment(commentId, currentUserId);

		log.info("댓글 삭제 완료: commentId={}", commentId);
		return ResponseEntity.noContent().build();
	}
}
