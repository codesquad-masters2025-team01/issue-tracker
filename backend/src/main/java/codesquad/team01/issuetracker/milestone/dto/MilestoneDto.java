package codesquad.team01.issuetracker.milestone.dto;

import java.time.LocalDate;
import java.util.List;

import codesquad.team01.issuetracker.milestone.domain.Milestone;
import codesquad.team01.issuetracker.milestone.domain.MilestoneState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

public class MilestoneDto {
	private MilestoneDto() {
	}

	/**
	 * 응답 DTO
	 */
	// 이슈 목록 - 마일스톤 응답 DTO
	@Builder
	public record ListItemResponse(
		int id,
		String title
	) {
	}

	@Builder
	public record IssueDetailMilestoneResponse(
		int id,
		String title,
		LocalDate dueDate,
		int openCount,
		int closedCount
	) {
	}

	// 필터용 마일스톤 응답 DTO
	@Builder
	public record MilestoneFilterResponse(
		int id,
		String title
	) {
	}

	// 필터용 마일스톤 목록 응답 DTO
	@Builder
	public record MilestoneFilterListResponse(
		int totalCount,
		List<MilestoneFilterResponse> milestones
	) {
	}

	// 목록의 구성요소 DTO
	@Builder
	public record MilestoneListItem(
		int id,
		String title,
		String description,
		LocalDate dueDate,
		MilestoneState state,
		Long openCount,
		Long closeCount,
		double progress
	) {
		public static MilestoneListItem from(MilestoneRow row, MilestoneDto.MilestoneIssueCountRow count) {

			Long openCount = count.openCount();
			Long closedCount = count.closedCount();
			double progress = 0.0;
			Long total = openCount + closedCount;
			if (total > 0) {
				progress = (double)closedCount / total;
			}
			return new MilestoneListItem(
				row.id(),
				row.title(),
				row.description(),
				row.dueDate(),
				row.state(),
				openCount,
				closedCount,
				progress
			);
		}
	}

	// 마일스톤 목록 조회시 응답 DTO
	public record ListResponse(
		int totalCount,
		List<MilestoneListItem> milestones
	) {
	}

	// DB 조회용 DTO
	public record MilestoneRow(
		int id,
		String title,
		String description,
		LocalDate dueDate,
		MilestoneState state
	) {
	}

	// 마일스톤 생성 요청 dto
	public record MilestoneCreateRequest(
		@NotBlank
		@Size(max = 500)
		String title,

		String description,

		@Pattern(regexp = "^$|\\d{4}-\\d{2}-\\d{2}")    // YYYY-MM-DD
		String dueDate
	) {
	}

	// 마일 스톤 생성 후 응답 dto
	public record MilestoneCreateResponse(
		int id,
		String title,
		String description,
		LocalDate dueDate,
		MilestoneState state
	) {
		public static MilestoneCreateResponse from(Milestone milestone) {
			return new MilestoneCreateResponse(
				milestone.getId(),
				milestone.getTitle(),
				milestone.getDescription(),
				milestone.getDueDate(),
				milestone.getState()
			);
		}
	}

	// 마일스톤 수정 요청 dto
	public record MilestoneUpdateRequest(
		@NotBlank
		@Size(max = 500)
		String title,

		String description,

		@Pattern(regexp = "^$|\\d{4}-\\d{2}-\\d{2}")    // YYYY-MM-DD
		String dueDate,

		@NotBlank(message = "상태는 필수입니다.")
		@Pattern(
			regexp = "^(OPEN|CLOSED)$",
			message = "state 값은 'OPEN' 또는 'CLOSED' 만 가능합니다."
		)
		String state
	) {
	}

	// 마일스톤 수정 응답 dto
	public record MilestoneUpdateResponse(
		int id,
		String title,
		String description,
		LocalDate dueDate,
		MilestoneState state
	) {
		public static MilestoneUpdateResponse from(Milestone milestone) {
			return new MilestoneUpdateResponse(
				milestone.getId(),
				milestone.getTitle(),
				milestone.getDescription(),
				milestone.getDueDate(),
				milestone.getState()
			);
		}
	}

	@Builder
	public record MilestoneIssueCountRow(
		Integer milestoneId,
		Long openCount,
		Long closedCount
	) {
	}

	@Builder
	public record MilestoneIssueDetailCountRow(
		String state,
		int count
	) {
	}
}
