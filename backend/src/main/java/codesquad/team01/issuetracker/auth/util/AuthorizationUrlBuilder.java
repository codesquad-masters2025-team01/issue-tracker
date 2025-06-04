package codesquad.team01.issuetracker.auth.util;

import java.net.URI;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import codesquad.team01.issuetracker.common.config.GithubOAuthProperties;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthorizationUrlBuilder {

	private final GithubOAuthProperties properties;

	// 인증을 위한 uri 조립 후 반환
	public URI buildAuthorizeUri(String state) {
		return UriComponentsBuilder
			.fromUriString(properties.getAuthorizeUri())
			.queryParam("client_id", properties.getClientId())
			.queryParam("redirect_uri", properties.getRedirectUri())
			.queryParam("scope", properties.getScope())
			.queryParam("state", state)
			.build()
			.toUri();
	}

	// 깃헙로그인의 토큰을 프론트로 리다이렉트 하기 위한 uri 조립 후 반환
	public URI buildFrontendRedirectUri(String accessToken, String refreshToken) {
		// 프론트엔드 콜백 페이지 기본 URL (필요에 따라 변경하세요)
		String frontendBase = "http://localhost:3000/oauth/callback";

		return UriComponentsBuilder
			.fromUriString(frontendBase)
			.queryParam("accessToken", accessToken)
			.queryParam("refreshToken", refreshToken)
			.build()
			.toUri();
	}
}
