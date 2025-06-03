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
}
