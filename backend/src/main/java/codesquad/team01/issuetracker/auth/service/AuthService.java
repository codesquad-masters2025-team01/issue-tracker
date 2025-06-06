package codesquad.team01.issuetracker.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import codesquad.team01.issuetracker.auth.client.GitHubClient;
import codesquad.team01.issuetracker.auth.domain.RefreshToken;
import codesquad.team01.issuetracker.auth.dto.AuthDto;
import codesquad.team01.issuetracker.auth.repository.RefreshTokenRepository;
import codesquad.team01.issuetracker.common.exception.InvalidPasswordException;
import codesquad.team01.issuetracker.common.exception.TokenNotFoundException;
import codesquad.team01.issuetracker.common.exception.UserLoginIdNotFoundException;
import codesquad.team01.issuetracker.user.domain.User;
import codesquad.team01.issuetracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final GitHubClient gitHubClient;
	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;

	private final String GITHUB = "github";
	private final PasswordEncoder passwordEncoder;

	public User findGitHubUser(String code) {
		// 깃헙에 access token 요청
		log.info("GitHub 에서 받은 인증 코드로 로그인 시작 ={}", code);
		String accessToken = gitHubClient.fetchAccessToken(code);

		// 사용자 정보 요청
		log.debug("Received access token");
		AuthDto.GitHubUser gitHubUser = gitHubClient.fetchUserInfo(accessToken);
		log.info("사용자 정보 요청 : id={}, githubId={}", gitHubUser.id(), gitHubUser.githubId());

		User oauthUser = findOrCreateUser(gitHubUser);
		log.info("사용자 id로 로그인 응답 생성 ={}", oauthUser.getId());

		return oauthUser;
	}

	private User findOrCreateUser(AuthDto.GitHubUser gitHubUser) {
		return userRepository.findByProviderIdAndAuthProvider(gitHubUser.id(), GITHUB)
			.orElseGet(() -> userRepository.save(User.builder()
				.loginId(null)
				.username(gitHubUser.githubId())
				.email(gitHubUser.email())
				.providerId(gitHubUser.id())
				.authProvider(GITHUB)
				.build()));
	}

	public User authenticateUser(String loginId, String password) {
		User user = userRepository.findByLoginId(loginId)
			.orElseThrow(() -> new UserLoginIdNotFoundException(loginId));
		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new InvalidPasswordException(password);
		}
		return user;
	}

	//로그아웃
	public void logout(String refreshToken) {
		RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
			.orElseThrow(TokenNotFoundException::new);
		refreshTokenRepository.delete(tokenEntity);
	}

	public User signUp(AuthDto.SignUpRequest request) throws Exception {
		//loginId 중복 검증
		if (userRepository.findByLoginId(request.loginId()).isPresent()) {
			throw new Exception("이미 존재하는 로그인 아이디 입니다");
		}

		//username 중복 검증
		if (userRepository.findByUsername(request.username()).isPresent()) {
			throw new Exception("이미 존재하는 사용자 이름 입니다");
		}
		//email 중복 검증
		if (userRepository.findByEmail(request.email()).isPresent()) {
			throw new Exception("이미 존재하는 이메일 입니다");
		}

		//프로필 이미지 url
		String profileImageUrl = "";
		if (request.profileImageUrl() != null) {
			profileImageUrl = request.profileImageUrl();
		}

		//password 인코딩
		String encodedPassword = passwordEncoder.encode(request.password());
		//db에 저장
		User user = new User(request.loginId(), request.username(), request.email(), encodedPassword, null, "local",
			profileImageUrl);

		userRepository.save(user);
		return user;
	}
}
