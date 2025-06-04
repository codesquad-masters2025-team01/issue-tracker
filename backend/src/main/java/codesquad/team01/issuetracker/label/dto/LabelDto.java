package codesquad.team01.issuetracker.label.dto;

import java.util.List;

import codesquad.team01.issuetracker.label.domain.Label;
import codesquad.team01.issuetracker.label.domain.LabelTextColor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

public class LabelDto {
	private LabelDto() {
	}

	/*
	 * 응답 DTO
	 */
	// 이슈 목록 - 레이블 응답 DTO
	@Builder
	public record FilterListItemResponse(
		int id,
		String name,
		String color,
		String textColor
	) {
	}

	// 레이블 리스트를 구성하는 레이블
	@Builder
	public record IssueDetailLabelResponse(
		int id,
		String name,
		String color,
		String textColor
	) {
	}

	// 레이블 리스트를 구성하는 레이블
	@Builder
	public record ListItemResponse(
		int id,
		String name,
		String description,
		String color,
		String textColor
	) {
		public static ListItemResponse from(LabelRow row) {
			return new ListItemResponse(
				row.id(),
				row.name(),
				row.description(),
				row.color(),
				row.textColor()
			);
		}
	}

	// 레이블 목록 조회 시 반환하는 레이블 리스트
	public record ListResponse(
		int totalCount,
		List<ListItemResponse> labels
	) {
	}

	// 이슈 레이블 행 DTO
	@Builder
	public record IssueLabelRow(
		int issueId,
		int labelId,
		String labelName,
		String labelColor,
		LabelTextColor labelTextColor
	) {
	}

	@Builder
	public record IssueDetailLabelRow(
		int id,
		String name,
		String color,
		String textColor
	) {
	}

	// 필터용 레이블 응답 Dto
	@Builder
	public record LabelFilterResponse(
		int id,
		String name,
		String color
	) {
	}

	// 필터용 레이블 목록 응답 Dto
	@Builder
	public record LabelFilterListResponse(
		int totalCount,
		List<LabelFilterResponse> labels
	) {
	}

	// DB 로부터 조회해오는 레이블
	public record LabelRow(
		int id,
		String name,
		String description,
		String color,
		String textColor
	) {
	}

	// 레이블 생성 요청 dto
	public record LabelCreateRequest(
		@NotBlank(message = "Label 이름은 필수입니다.")
		@Size(max = 100, message = "Label 이름은 최대 100자 이내여야 합니다.")
		String name,

		String description,

		@NotBlank(message = "배경색은 필수입니다.")
		@Pattern(
			regexp = "^#([0-9A-Fa-f]{6})$",
			message = "색상은 #RRGGBB 형식이어야 합니다."
		)
		String color,

		@NotBlank(message = "글자색은 필수입니다.")
		@Pattern(
			regexp = "^(WHITE|BLACK)$",
			message = "글자 색은 WHITE 또는 BLACK 만 가능합니다."
		)
		String textColor
	) {
	}

	// 레이블 생성 후 응답하는 dto
	@Builder
	public record LabelCreateResponse(
		int id,
		String name,
		String description,
		String color,
		String textColor
	) {
		public static LabelCreateResponse from(Label label) {
			return new LabelCreateResponse(
				label.getId(),
				label.getName(),
				label.getDescription(),
				label.getColor(),
				label.getTextColor()
			);
		}
	}

	// 레이블 수정 요청 dto
	public record LabelUpdateRequest(
		@NotBlank(message = "Label 이름은 필수입니다.")
		@Size(max = 100, message = "Label 이름은 최대 100자 이내여야 합니다.")
		String name,
		String description,

		@NotBlank(message = "배경색은 필수입니다.")
		@Pattern(
			regexp = "^#([0-9A-Fa-f]{6})$",
			message = "색상은 #RRGGBB 형식이어야 합니다."
		)
		String color,

		@NotBlank(message = "글자색은 필수입니다.")
		@Pattern(
			regexp = "^(WHITE|BLACK)$",
			message = "글자 색은 WHITE 또는 BLACK 만 가능합니다."
		)
		String textColor
	) {
	}

	// 레이블 수정 응답 dto
	public record LabelUpdateResponse(
		int id,
		String name,
		String description,
		String color,
		String textColor
	) {
		public static LabelUpdateResponse from(Label label) {
			return new LabelUpdateResponse(
				label.getId(),
				label.getName(),
				label.getDescription(),
				label.getColor(),
				label.getTextColor()
			);
		}
	}

}
