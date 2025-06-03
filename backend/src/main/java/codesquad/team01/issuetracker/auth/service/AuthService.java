package codesquad.team01.issuetracker.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import codesquad.team01.issuetracker.auth.client.GitHubClient;
import codesquad.team01.issuetracker.auth.dto.AuthDto;
import codesquad.team01.issuetracker.common.exception.InvalidPasswordException;
import codesquad.team01.issuetracker.common.exception.UserNotFoundException;
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
		User user = userRepository.findByLoginId(loginId).orElseThrow(UserNotFoundException::new);
		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new InvalidPasswordException();
		}
		return user;
	}

}
