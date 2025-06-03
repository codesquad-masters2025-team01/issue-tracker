package codesquad.team01.issuetracker.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AuthDto {

	private AuthDto() {
	}

	public record GitHubUser(
		long id,
		String githubId,
		String avatarUrl,
		String email
	) {
	}

	public record GitHubEmail(
		String email,
		boolean primary,
		boolean verified,
		String visibility
	) {
	}

	public record LoginRequest(
		@NotNull String loginId,
		@NotBlank String password
	) {

	}

	public record LoginResponse(
		String accessToken,
		String refreshToken
	) {
	}

}
