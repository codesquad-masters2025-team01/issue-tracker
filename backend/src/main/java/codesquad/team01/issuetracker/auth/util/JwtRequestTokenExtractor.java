package codesquad.team01.issuetracker.auth.util;

import codesquad.team01.issuetracker.common.exception.TokenNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

public class JwtRequestTokenExtractor {

	public static final String JWT_HEADER = "Authorization";
	public static final String BEARER = "Bearer ";

	public static String extractJwtRequestToken(HttpServletRequest request) {
		String authHead = request.getHeader(JWT_HEADER);
		if (authHead != null && authHead.startsWith(BEARER)) {
			return authHead.replace(BEARER, "");
		}
		throw new TokenNotFoundException();
	}
}
