package codesquad.team01.issuetracker.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

	private final boolean success;
	private final T data;
	private final String message;

	public static <T> ApiResponse<T> success(T data) {
		return ApiResponse.<T>builder()
			.success(true)
			.data(data)
			.build();
	}

	public static ApiResponse<?> error(String message) {
		return ApiResponse.builder()
			.success(false)
			.message(message)
			.build();
	}
}
