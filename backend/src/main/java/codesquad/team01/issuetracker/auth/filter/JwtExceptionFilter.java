package codesquad.team01.issuetracker.auth.filter;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.team01.issuetracker.common.dto.ApiResponse;
import codesquad.team01.issuetracker.common.exception.TokenNotFoundException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {

	private final ObjectMapper objectMapper;

	@Override
	protected void doFilterInternal(HttpServletRequest request,
		HttpServletResponse response,
		FilterChain chain)
		throws ServletException, IOException {

		try {
			chain.doFilter(request, response);
		} catch (JwtException e) {
			log.warn("JWT 검증 실패: {}", e.getMessage());
			writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다.");
		} catch (TokenNotFoundException e) {
			log.debug("토큰 없음: {}", e.getMessage());
			writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		}
	}

	private void writeErrorResponse(HttpServletResponse res, int status, String message) throws IOException {
		res.setStatus(status);
		res.setContentType(MediaType.APPLICATION_JSON_VALUE);
		res.setCharacterEncoding("UTF-8");
		String body = objectMapper.writeValueAsString(ApiResponse.error(message));
		res.getWriter().write(body);
	}

	//필터를 거치지 않을 경로
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return path.startsWith("/api/v1/auth/login") || path.startsWith("/api/v1/oauth/");
	}
}
