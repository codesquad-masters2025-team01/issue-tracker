package codesquad.team01.issuetracker.issue.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import codesquad.team01.issuetracker.common.dto.CursorDto;
import codesquad.team01.issuetracker.issue.domain.IssueState;
import codesquad.team01.issuetracker.label.dto.LabelDto;
import codesquad.team01.issuetracker.milestone.dto.MilestoneDto;
import codesquad.team01.issuetracker.user.dto.UserDto;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IssueDto {

	private IssueDto() {
	}

	/**
	 * 요청 DTO
	 */

	// 열린/닫힌 이슈 개수 조회 필터 쿼리 요청 DTO
	@Builder
	public record CountQueryRequest(
		@Positive(message = "작성자 ID는 양수여야 합니다")
		Integer writerId,

		@Positive(message = "마일스톤 ID는 양수여야 합니다")
		Integer milestoneId,

		List<@Positive(message = "레이블 ID는 양수여야 합니다") Integer> labelIds,

		List<@Positive(message = "담당자 ID는 양수여야 합니다") Integer> assigneeIds
	) {
		@Override
		public String toString() {
			return "CountQueryRequest{" +
				"writerId=" + writerId +
				", milestoneId=" + milestoneId +
				", labelIds=" + labelIds +
				", assigneeIds=" + assigneeIds +
				'}';
		}
	}

	// 이슈 조회 필터 쿼리 요청 DTO
	@Builder
	public record ListQueryRequest(
		@Pattern(regexp = "^(open|closed)$", message = "state는 'open' 또는 'closed'만 가능합니다")
		String state,

		@Positive(message = "작성자 ID는 양수여야 합니다")
		Integer writerId,

		@Positive(message = "마일스톤 ID는 양수여야 합니다")
		Integer milestoneId,

		List<@Positive(message = "레이블 ID는 양수여야 합니다") Integer> labelIds,

		List<@Positive(message = "담당자 ID는 양수여야 합니다") Integer> assigneeIds

	) {
		@Override
		public String toString() {
			return "ListQueryRequest{" +
				"state='" + state + '\'' +
				", writerId=" + writerId +
				", milestoneId=" + milestoneId +
				", labelIds=" + labelIds +
				", assigneeIds=" + assigneeIds +
				'}';
		}

		public IssueState getIssueState() {
			return IssueState.fromStateStr(state);
		}
	}

	/**
	 * 응답 DTO
	 */
	// 이슈 목록 응답 DTO
	@Builder
	public record ListResponse(
		int totalCount,
		List<ListItemResponse> issues,
		CursorDto.CursorResponse cursor
	) {
	}

	// 이슈 목록 중 하나의 이슈 응답 DTO
	@Getter
	@Builder
	public static class ListItemResponse {
		private final int id;

		private final String title;
		private final String state;
		private final LocalDateTime createdAt;
		private final LocalDateTime updatedAt;
		private final UserDto.WriterResponse writer;
		private final MilestoneDto.ListItemResponse milestone;

		@Builder.Default
		private final List<UserDto.AssigneeResponse> assignees = new ArrayList<>();

		@Builder.Default
		private final List<LabelDto.FilterListItemResponse> labels = new ArrayList<>();
	}

	// 상태별 이슈 개수 응답 DTO
	@Builder
	public record CountResponse(
		int open,
		int closed
	) {
		@Override
		public String toString() {
			return "CountResponse{" +
				"open=" + open +
				", closed=" + closed +
				'}';
		}
	}

	/**
	 * DB 조회용 DTO
	 */
	// 이슈 기본 정보 행 DTO
	@Builder
	public record BaseRow(
		// issue
		int issueId,

		String issueTitle,
		IssueState issueState,
		LocalDateTime issueCreatedAt,
		LocalDateTime issueUpdatedAt,

		// writerId
		int writerId,

		String writerUsername,
		String writerProfileImageUrl,

		// milestoneId - nullable
		Integer milestoneId,

		String milestoneTitle
	) {
	}

	// 상태별 이슈 개수 행 DTO
	@Builder
	public record StateCountRow(
		String state,
		int count
	) {
	}

	/**
	 * 서비스 계층 DTO
	 */
	// 이슈 상세 정보 DTO
	@Builder
	public record Details(
		BaseRow baseInfo,
		List<UserDto.AssigneeResponse> assignees,
		List<LabelDto.FilterListItemResponse> labels
	) {
		public ListItemResponse toListItemResponse() { // Mapper 클래스로 따로 뺄지 고민
			return IssueDto.ListItemResponse.builder()
				.id(baseInfo.issueId())
				.title(baseInfo.issueTitle())
				.state(baseInfo.issueState().getValue())
				.createdAt(baseInfo.issueCreatedAt())
				.updatedAt(baseInfo.issueUpdatedAt())
				.writer(UserDto.WriterResponse.builder()
					.id(baseInfo.writerId())
					.username(baseInfo.writerUsername())
					.profileImageUrl(baseInfo.writerProfileImageUrl())
					.build()
				)
				.milestone(baseInfo.milestoneId() != null
					? MilestoneDto.ListItemResponse.builder()
					.id(baseInfo.milestoneId())
					.title(baseInfo.milestoneTitle())
					.build()
					: null)
				.assignees(assignees)
				.labels(labels)
				.build();
		}
	}

}
