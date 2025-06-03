package codesquad.team01.issuetracker.common.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CursorDto {

	private CursorDto() {
	}

	@Getter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class CursorData {
		private Integer id;
		private LocalDateTime createdAt;
	}

	@Builder
	public record CursorResponse(
		String next,
		boolean hasNext
	) {
	}
}
