package codesquad.team01.issuetracker.milestone.dto;

import java.util.List;

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
}
