package codesquad.team01.issuetracker.comment.dto;

import java.time.LocalDateTime;
import java.util.List;

import codesquad.team01.issuetracker.common.dto.CursorDto;
import codesquad.team01.issuetracker.user.dto.UserDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

public class CommentDto {

	/**
	 * 요청 DTO
	 */
	public record CreateRequest(

		@NotBlank
		String content
	) {
	}

	public record UpdateRequest(
		@NotBlank
		String content
	) {
	}

	/**
	 * 응답 DTO
	 */
	@Builder
	public record CommentResponse(
		int id,
		String content,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		UserDto.CommentWriterResponse writer
	) {
	}

	@Builder
	public record CommentListResponse(
		int totalCount,
		List<CommentResponse> comments,
		CursorDto.CursorResponse cursor
	) {
	}

	/**
	 * 내부 처리용 DTO
	 */
	@Builder
	public record CommentDetails(
		int id,
		String content,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		int writerId,
		int issueId
	) {
	}

	/**
	 * DB 조회용 DTO
	 */
	@Builder
	public record CommentRow(
		int commentId,
		String commentContent,
		LocalDateTime commentCreatedAt,
		LocalDateTime commentUpdatedAt,
		int writerId,
		String writerUsername,
		String writerProfileImageUrl
	) {
		public CommentResponse toResponse() {
			return CommentResponse.builder()
				.id(commentId)
				.content(commentContent)
				.createdAt(commentCreatedAt)
				.updatedAt(commentUpdatedAt)
				.writer(UserDto.CommentWriterResponse.builder()
					.id(writerId)
					.username(writerUsername)
					.profileImageUrl(writerProfileImageUrl)
					.build())
				.build();
		}
	}

	/**
	 * 쿼리 파라미터용 DTO
	 */
	@Builder
	public record CommentQueryParams(
		Integer issueId,
		CursorDto.CursorData cursor,
		int pageSize
	) {
	}
}
