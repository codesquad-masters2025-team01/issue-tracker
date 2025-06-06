package codesquad.team01.issuetracker.auth.filter;

import java.io.IOException;

import org.springframework.web.filter.GenericFilterBean;

import codesquad.team01.issuetracker.auth.util.JwtRequestTokenExtractor;
import codesquad.team01.issuetracker.auth.util.UserAuthorizationJwtManager;
import codesquad.team01.issuetracker.common.exception.UserNotFoundException;
import codesquad.team01.issuetracker.user.domain.User;
import codesquad.team01.issuetracker.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

	private final UserAuthorizationJwtManager jwtManager;
	private final UserRepository userRepository;

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
		throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)res;

		String path = request.getRequestURI();
		//필터를 거치지 않을 경로
		if (path.equals("/api/v1/auth/login") || path.startsWith("/api/v1/oauth")
			|| path.equals("/api/v1/auth/signup")) {
			chain.doFilter(request, response);
			return;
		}
		log.info("JWT 필터 동작: {}", path);

		// OPTIONS 요청 시(CORS에 관련) 다음 필터로 넘김
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			chain.doFilter(request, response);
			return;
		}

		String token = JwtRequestTokenExtractor.extractJwtRequestToken(request);
		log.info("Access Token:{}", token);

		Claims claims = jwtManager.parseClaims(token);

		Integer userId = Integer.valueOf(claims.getSubject());
		String username = claims.get("username", String.class);
		String profileImageUrl = claims.get("profileImageUrl", String.class);

		User user = userRepository.findById(userId)
			.orElseThrow(UserNotFoundException::new);

		request.setAttribute("authenticatedUser", user);
		request.setAttribute("username", username);
		request.setAttribute("profileImageUrl", profileImageUrl);

		chain.doFilter(request, response);
	}
}
