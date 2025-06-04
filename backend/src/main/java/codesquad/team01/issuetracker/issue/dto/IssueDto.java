package codesquad.team01.issuetracker.issue.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import codesquad.team01.issuetracker.common.dto.CursorDto;
import codesquad.team01.issuetracker.common.exception.InvalidParameterException;
import codesquad.team01.issuetracker.issue.domain.IssueState;
import codesquad.team01.issuetracker.label.dto.LabelDto;
import codesquad.team01.issuetracker.milestone.dto.MilestoneDto;
import codesquad.team01.issuetracker.user.dto.UserDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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

		@Pattern(regexp = "^(created|assigned|commented)$", message = "filter는 'created', 'assigned', 'commented'만 가능합니다")
		String filter,

		@Positive(message = "작성자 ID는 양수여야 합니다")
		Integer writerId,

		@Positive(message = "마일스톤 ID는 양수여야 합니다")
		Integer milestoneId,

		List<@Positive(message = "레이블 ID는 양수여야 합니다") Integer> labelIds,

		List<@Positive(message = "담당자 ID는 양수여야 합니다") Integer> assigneeIds,

		@Positive(message = "댓글 작성자 ID는 양수여야 합니다")
		Integer commentedUserId

	) {
		@Override
		public String toString() {
			return "ListQueryRequest{" +
				"state='" + state + '\'' +
				", filter='" + filter + '\'' +
				", writerId=" + writerId +
				", milestoneId=" + milestoneId +
				", labelIds=" + labelIds +
				", assigneeIds=" + assigneeIds +
				", commentedUserId=" + commentedUserId +
				'}';
		}

		public IssueState getIssueState() {
			return IssueState.fromStateStr(state);
		}
	}

	// 다중 선택 이슈 state 변화 요청 DTO
	@Builder
	public record BatchUpdateRequest(

		@NotEmpty(message = "이슈 ID 목록은 비어있을 수 없습니다")
		List<@Positive(message = "이슈 ID는 양수여야 합니다") Integer> issueIds,

		@NotBlank(message = "작업 타입은 필수입니다")
		@Pattern(regexp = "^(open|close)$", message = "action은 'open' 또는 'close'만 가능합니다")
		String action
	) {
		public IssueState getTargetState() {
			return IssueState.fromActionStr(action);
		}

		@Override
		public String toString() {
			return "BatchUpdateRequest{" +
				"issueIds=" + issueIds +
				", action='" + action + '\'' +
				'}';
		}
	}

	@Builder
	public record CreateRequest(
		@NotBlank(message = "제목은 필수입니다.")
		String title,
		String content,

		@Positive(message = "마일스톤 ID는 양수여야 합니다")
		Integer milestoneId,
		List<@Positive(message = "레이블 ID는 양수여야 합니다") Integer> labelIds,
		List<@Positive(message = "담당자 ID는 양수여야 합니다") Integer> assigneeIds
	) {

		public String content() {
			return content != null ? content : "";
		}

		public List<Integer> labelIds() {
			return labelIds != null ? labelIds : List.of();
		}

		public List<Integer> assigneeIds() {
			return assigneeIds != null ? assigneeIds : List.of();
		}

		@Override
		public String toString() {
			return "IssueCreationRequest{" +
				"title='" + title + '\'' +
				", content='" + content + '\'' +
				", milestoneId=" + milestoneId +
				", labelIds=" + labelIds +
				", assigneeIds=" + assigneeIds +
				'}';
		}
	}

	@Builder
	public record UpdateRequest(
		String title,
		String content,
		Boolean clearContent,

		@Positive(message = "마일스톤 ID는 양수여야 합니다")
		Integer milestoneId,
		Boolean clearMilestone,
		List<@Positive(message = "레이블 ID는 양수여야 합니다") Integer> labelIds,
		Boolean clearLabels,
		List<@Positive(message = "담당자 ID는 양수여야 합니다") Integer> assigneeIds,
		Boolean clearAssignees,
		@Pattern(regexp = "^(open|close)$", message = "action은 'open' 또는 'close'만 가능합니다")
		String action
	) {

		public boolean isUpdatingTitle() {
			return title != null;
		}

		public boolean isUpdatingContent() {
			return content != null || Boolean.TRUE.equals(clearContent);
		}

		public boolean isUpdatingMilestone() {
			return milestoneId != null || Boolean.TRUE.equals(clearMilestone);
		}

		public boolean isUpdatingLabels() {
			return labelIds != null || Boolean.TRUE.equals(clearLabels);
		}

		public boolean isUpdatingAssignees() {
			return assigneeIds != null || Boolean.TRUE.equals(clearAssignees);
		}

		public boolean isUpdatingState() {
			return action != null;
		}

		public boolean hasAnyFiled() {
			return isUpdatingTitle() || isUpdatingContent() || isUpdatingMilestone() ||
				isUpdatingLabels() || isUpdatingAssignees() || isUpdatingState();
		}

		public String getContent() {
			if (Boolean.TRUE.equals(clearContent)) {
				return null;
			}
			return content;
		}

		public Integer getMilestoneId() {
			if (Boolean.TRUE.equals(clearMilestone)) {
				return null;
			}
			return milestoneId;
		}

		public List<Integer> getLabelIds() {
			if (Boolean.TRUE.equals(clearLabels)) {
				return List.of();
			}
			return labelIds != null ? labelIds : List.of();
		}

		public List<Integer> getAssigneeIds() {
			if (Boolean.TRUE.equals(clearAssignees)) {
				return List.of();
			}
			return assigneeIds != null ? assigneeIds : List.of();
		}

		public IssueState getAction() {
			return IssueState.fromActionStr(action);
		}

		public void validateRequest() {

			if (title != null && title.trim().isEmpty()) {
				throw new InvalidParameterException("제목이 제공된 경우 공백일 수 없습니다.");
			}

			if (content != null && Boolean.TRUE.equals(clearContent)) {
				throw new InvalidParameterException("content와 clearContent를 동시 설정 불가합니다.");
			}

			if (milestoneId != null && Boolean.TRUE.equals(clearMilestone)) {
				throw new InvalidParameterException("milestoneId와 clearMilestone을 동시 설정 불가합니다.");
			}

			if (labelIds != null && Boolean.TRUE.equals(clearLabels)) {
				throw new InvalidParameterException("labelIds와 clearLabels를 동시 설정 불가합니다.");
			}

			if (assigneeIds != null && Boolean.TRUE.equals(clearAssignees)) {
				throw new InvalidParameterException("assigneeIds와 clearAssignees를 동시 설정 불가합니다.");
			}
		}

		@Override
		public String toString() {
			return "UpdateRequest{" +
				"title='" + title + '\'' +
				", content='" + content + '\'' +
				", clearContent=" + clearContent +
				", milestoneId=" + milestoneId +
				", clearMilestone=" + clearMilestone +
				", labelIds=" + labelIds +
				", clearLabels=" + clearLabels +
				", assigneeIds=" + assigneeIds +
				", clearAssignees=" + clearAssignees +
				", action='" + action + '\'' +
				'}';
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

	// 다중 선택 이슈 state 변화 응답 DTO
	@Builder
	public record BatchUpdateResponse(
		int totalCount,
		int successCount,
		int failedCount,
		List<Integer> failedIssueIds
	) {
		@Override
		public String toString() {
			return "BatchUpdateResponse{" +
				"totalCount=" + totalCount +
				", successCount=" + successCount +
				", failedCount=" + failedCount +
				", failedIssueIds=" + failedIssueIds +
				'}';
		}
	}

	@Builder
	public record IssueDetailsResponse(
		int id,
		String title,
		String content,
		String state,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		LocalDateTime closedAt,
		UserDto.IssueDetailUserResponse writer,
		MilestoneDto.IssueDetailMilestoneResponse milestone,
		List<LabelDto.IssueDetailLabelResponse> labels,
		List<UserDto.IssueDetailUserResponse> assignees,
		int commentCount
	) {
	}

	@Builder
	public record UpdateResponse(
		int id,
		String title,
		String content,
		String state,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		LocalDateTime closedAt,
		UserDto.IssueDetailUserResponse writer,
		MilestoneDto.IssueDetailMilestoneResponse milestone,
		List<LabelDto.IssueDetailLabelResponse> labels,
		List<UserDto.IssueDetailUserResponse> assignees,
		int commentCount
	) {
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

	@Builder
	public record DetailBaseRow(
		// issue
		int issueId,
		String issueTitle,
		String issueContent,
		IssueState issueState,
		LocalDateTime issueCreatedAt,
		LocalDateTime issueUpdatedAt,
		LocalDateTime issueClosedAt,

		// writerId
		int writerId,
		String writerUsername,
		String writerProfileImageUrl,

		// milestoneId - nullable
		Integer milestoneId,
		String milestoneTitle,
		LocalDate milestoneDueDate
	) {
	}

	// 상태별 이슈 개수 행 DTO
	@Builder
	public record StateCountRow(
		String state,
		int count
	) {
	}

	// 다중 선택 이슈 행 DTO
	@Builder
	public record BatchIssueRow(
		int issueId,
		IssueState currentState
	) {
	}

	public record IssueStateRow(
		IssueState state
	) {
	}

	@Builder
	public record IssueStateAndWriterIdRow(
		IssueState state,
		int writerId
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
		public ListItemResponse toListItemResponse() {
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

	@Builder
	public record SingleDetails(
		DetailBaseRow issue,
		List<LabelDto.IssueDetailLabelResponse> labels,
		List<UserDto.IssueDetailUserResponse> assignees,
		int commentCount
	) {
		public IssueDetailsResponse toCreateResponse(MilestoneDto.IssueDetailMilestoneResponse milestone) {
			return IssueDetailsResponse.builder()
				.id(issue.issueId())
				.title(issue.issueTitle())
				.content(issue.issueContent())
				.state(issue.issueState().getValue())
				.createdAt(issue.issueCreatedAt())
				.updatedAt(issue.issueUpdatedAt())
				.closedAt(issue.issueClosedAt())
				.writer(UserDto.IssueDetailUserResponse.builder()
					.id(issue.writerId())
					.username(issue.writerUsername())
					.profileImageUrl(issue.writerProfileImageUrl())
					.build()
				)
				.milestone(milestone)
				.assignees(assignees)
				.labels(labels)
				.commentCount(commentCount)
				.build();
		}
	}

	/**
	 * 쿼리 DTO
	 */
	// 이슈 목록 조회용 DTO
	public record ListQueryParams(
		IssueState state,
		String filter,
		Integer writerId,
		Integer milestoneId,
		List<Integer> labelIds,
		List<Integer> assigneeIds,
		Integer commentedUserId
	) {
		public static ListQueryParams from(ListQueryRequest request) {
			return new ListQueryParams(
				request.getIssueState(),
				request.filter(),
				request.writerId(),
				request.milestoneId(),
				request.labelIds() != null ? request.labelIds() : List.of(),
				request.assigneeIds() != null ? request.assigneeIds() : List.of(),
				request.commentedUserId()
			);
		}
	}

	// 이슈 개수 조회용 DTO
	public record CountQueryParams(
		Integer writerId,
		Integer milestoneId,
		List<Integer> labelIds,
		List<Integer> assigneeIds
	) {
		public static CountQueryParams from(CountQueryRequest request) {
			return new CountQueryParams(
				request.writerId(),
				request.milestoneId(),
				request.labelIds() != null ? request.labelIds() : List.of(),
				request.assigneeIds() != null ? request.assigneeIds() : List.of()
			);
		}
	}

	// 이슈 수정용 DTO
	public record UpdateQueryParams(
		String title,
		String content,
		Integer milestoneId,
		List<Integer> labelIds,
		List<Integer> assigneeIds,
		IssueState action,
		boolean isUpdatingTitle,
		boolean isUpdatingContent,
		boolean isUpdatingMilestone,
		boolean isUpdatingLabels,
		boolean isUpdatingAssignees,
		boolean isUpdatingState
	) {
		public static UpdateQueryParams from(UpdateRequest request) {
			return new UpdateQueryParams(
				request.title(),
				request.getContent(),
				request.getMilestoneId(),
				request.getLabelIds(),
				request.getAssigneeIds(),
				request.isUpdatingState() ? request.getAction() : null,
				request.isUpdatingTitle(),
				request.isUpdatingContent(),
				request.isUpdatingMilestone(),
				request.isUpdatingLabels(),
				request.isUpdatingAssignees(),
				request.isUpdatingState()
			);
		}
	}
}
