package codesquad.team01.issuetracker.auth.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import codesquad.team01.issuetracker.auth.domain.RefreshToken;
import codesquad.team01.issuetracker.auth.dto.AuthDto;
import codesquad.team01.issuetracker.auth.repository.RefreshTokenRepository;
import codesquad.team01.issuetracker.auth.util.UserAuthorizationJwtManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TokenService {

	private final UserAuthorizationJwtManager userAuthorizationJwtManager;
	private final RefreshTokenRepository refreshTokenRepository;

	public AuthDto.LoginResponse createTokens(int userId, String profileImageUrl, String username) {
		Map<String, Object> claims = new HashMap<>();

		claims.put("username", username);
		claims.put("profileImageUrl", profileImageUrl);

		// JWT 생성
		String accessToken = userAuthorizationJwtManager.createAccessToken(userId, claims);

		// DB에 저장된 리프레시 토큰 조회
		Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByUserId(userId);
		String refreshToken;

		if (optionalRefreshToken.isPresent()) {
			RefreshToken existing = optionalRefreshToken.get();
			// 만료 여부 체크
			if (userAuthorizationJwtManager.validateRefreshToken(existing.getToken())) {
				// 만료되지 않았으면 DB 토큰 그대로
				refreshToken = existing.getToken();
			} else {
				// 만료됐으면 새로 발급함 (DB 업데이트)
				refreshToken = userAuthorizationJwtManager.createRefreshToken(userId);
				RefreshToken updated = RefreshToken.builder()
					.id(existing.getId())
					.userId(userId)
					.token(refreshToken)
					.build();
				refreshTokenRepository.save(updated);
			}
		} else {
			// 없으면 새로 발급
			refreshToken = userAuthorizationJwtManager.createRefreshToken(userId);
			RefreshToken created = RefreshToken.builder().userId(userId).token(refreshToken).build();
			refreshTokenRepository.save(created);
		}
		return new AuthDto.LoginResponse(accessToken, refreshToken);
	}

}
