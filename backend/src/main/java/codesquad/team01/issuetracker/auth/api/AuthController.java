package codesquad.team01.issuetracker.auth.api;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import codesquad.team01.issuetracker.auth.dto.AuthDto;
import codesquad.team01.issuetracker.auth.service.AuthService;
import codesquad.team01.issuetracker.auth.service.TokenService;
import codesquad.team01.issuetracker.auth.util.AuthorizationUrlBuilder;
import codesquad.team01.issuetracker.common.dto.ApiResponse;
import codesquad.team01.issuetracker.user.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class AuthController {

	private final AuthService authService;
	private final TokenService tokenService;
	private final AuthorizationUrlBuilder authorizationUrlBuilder;

	// Authorization endpoint
	@GetMapping("/v1/oauth/github/login")
	public void redirectToGithub(HttpServletResponse response, HttpSession session) throws IOException {
		// CSRF 방지용 state 생성 및 세션 저장
		String state = UUID.randomUUID().toString();
		session.setAttribute("oauth_state", state);

		// URL 생성
		URI githubUri = authorizationUrlBuilder.buildAuthorizeUri(state);

		response.sendRedirect(githubUri.toString());
	}

	// Redirect(Callback) endpoint
	@GetMapping("/v1/oauth/callback")
	public void githubCallback(@RequestParam("code") String code, @RequestParam("state") String state,
		HttpSession session, HttpServletResponse response) throws IOException {
		String savedState = (String)session.getAttribute("oauth_state");
		if (savedState == null || !savedState.equals(state)) {
			throw new IllegalStateException("유효하지 않은 state");
		}
		session.removeAttribute("oauth_state");

		User oauthUser = authService.findGitHubUser(code);

		AuthDto.LoginResponse tokens = tokenService.createTokens(oauthUser.getId(), oauthUser.getProfileImageUrl(),
			oauthUser.getUsername());

		// 프론트엔드로 토큰과 함께 리다이렉트
		String frontendUrl = String.format(
			"http://issue-tracker.o-r.kr/login?accessToken=%s&refreshToken=%s&success=true",
			tokens.accessToken(),
			tokens.refreshToken()
		);

		response.sendRedirect(frontendUrl);
	}

	//자체 로그인
	@PostMapping("/v1/auth/login")
	public ApiResponse<AuthDto.LoginResponse> login(@RequestBody @Valid AuthDto.LoginRequest request) {

		User user = authService.authenticateUser(request.loginId(), request.password());

		AuthDto.LoginResponse tokens = tokenService.createTokens(user.getId(), user.getProfileImageUrl(),
			user.getUsername());

		return ApiResponse.success(tokens);
	}

	// 인증한 사용자 username, profileImageUrl
	@GetMapping("/v1/auth/me")
	public ApiResponse<?> getUsernameAndProfileImage(HttpServletRequest request) {
		String username = (String)request.getAttribute("username");
		String profileImage = (String)request.getAttribute("profileImageUrl");

		return ApiResponse.success(Map.of("username", username, "profileImage", profileImage));
	}

	//로그아웃
	@PostMapping("/v1/auth/logout")
	public String logout(@RequestBody AuthDto.LogoutRequest request) {
		authService.logout(request.refreshToken());
		//로그아웃 후 로그인페이지로 리다이렉트
		return "redirect:/login";
	}

	/*
	@PostMapping("/v1/auth/logout")
	public ApiResponse<?> logout(@RequestBody AuthDto.LogoutRequest request) {
		authService.logout(request.refreshToken());
		return ApiResponse.success("로그아웃이 성공적으로 처리되었습니다.");
	}
	*/
	@PostMapping("/v1/auth/signup")
	public ApiResponse<?> signup(@RequestBody AuthDto.SignUpRequest request) throws Exception {
		authService.signUp(request);
		return ApiResponse.success("회원가입이 완료되었습니다.");
	}
}
