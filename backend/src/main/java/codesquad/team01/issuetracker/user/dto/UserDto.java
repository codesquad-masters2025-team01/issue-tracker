package codesquad.team01.issuetracker.user.dto;

import java.util.List;

import lombok.Builder;

public class UserDto {

	private UserDto() {
	}

	private static final String DEFAULT_PROFILE_IMAGE_URL = "/images/default-profile.png";

	/**
	 * 응답 DTO
	 */
	// 이슈 목록 - 작성자 응답 DTO
	@Builder
	public record WriterResponse(
		int id,
		String username,
		String profileImageUrl
	) {
		public String profileImageUrl() {
			return profileImageUrl != null ? profileImageUrl : DEFAULT_PROFILE_IMAGE_URL;
		}
	}

	// 담당자 응답 DTO
	@Builder
	public record AssigneeResponse(
		int id,
		String profileImageUrl
	) {
		public String profileImageUrl() {
			return profileImageUrl != null ? profileImageUrl : DEFAULT_PROFILE_IMAGE_URL;
		}
	}

	@Builder
	public record IssueDetailUserResponse(
		int id,
		String username,
		String profileImageUrl
	) {
		public String profileImageUrl() {
			return profileImageUrl != null ? profileImageUrl : DEFAULT_PROFILE_IMAGE_URL;
		}
	}

	// 필터용 사용자 목록 응답 Dto
	@Builder
	public record UserFilterListResponse(
		int totalCount,
		List<UserFilterResponse> users

	) {
	}

	// 필터용 사용자 응답 Dto
	@Builder
	public record UserFilterResponse(
		int id,

		String username,
		String profileImageUrl
	) {
		public String profileImageUrl() {
			return profileImageUrl != null ? profileImageUrl : DEFAULT_PROFILE_IMAGE_URL;
		}
	}

	// 댓글 작성자 응답 DTO
	@Builder
	public record CommentWriterResponse(
		int id,
		String username,
		String profileImageUrl
	) {
		public String profileImageUrl() {
			return profileImageUrl != null ? profileImageUrl : DEFAULT_PROFILE_IMAGE_URL;
		}
	}

	/**
	 * DB 조회용 DTO
	 */
	// 이슈 담당자 행 DTO
	@Builder
	public record IssueAssigneeRow(
		int issueId,
		int assigneeId,
		String assigneeProfileImageUrl
	) {
	}

	@Builder
	public record IssueDetailAssigneeRow(
		int assigneeId,
		String assigneeUsername,
		String assigneeProfileImageUrl
	) {
	}

	// 사용자 행 DTO
	@Builder
	public record UserFilterRow(
		int id,
		String username,
		String profileImageUrl
	) {
	}
}
