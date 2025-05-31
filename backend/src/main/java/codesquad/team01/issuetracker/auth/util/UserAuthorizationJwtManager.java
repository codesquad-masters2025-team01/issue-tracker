package codesquad.team01.issuetracker.auth.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserAuthorizationJwtManager {

	private final SecretKey key;

	@Value("${JWT_ACCESS_TOKEN_LIFETIME}")
	private final long accessTokenLifetime;

	@Value("${JWT_REFRESH_TOKEN_LIFETIME}")
	private final long refreshTokenLifetime;

	public UserAuthorizationJwtManager(@Value("${JWT_SECRET_KEY}") String secretKey,
		@Value("${JWT_ACCESS_TOKEN_LIFETIME}") long accessTokenLifetime,
		@Value("${JWT_REFRESH_TOKEN_LIFETIME}") long refreshTokenLifetime) {
		this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
		this.accessTokenLifetime = accessTokenLifetime;
		this.refreshTokenLifetime = refreshTokenLifetime;
	}

	public String createAccessToken(int subject, Map<String, Object> claims) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + accessTokenLifetime);
		return Jwts.builder()
			.claims(claims)
			.subject(String.valueOf(subject))
			.issuedAt(now)
			.expiration(expiry)
			.signWith(key, Jwts.SIG.HS256)
			.compact();
	}

	public String createRefreshToken(int subject) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + refreshTokenLifetime);
		return Jwts.builder()
			.subject(String.valueOf(subject))
			.issuedAt(now)
			.expiration(expiry)
			.signWith(key, Jwts.SIG.HS256)
			.compact();
	}

	public Claims parseClaims(String token) {
		try {
			Jwt<?, Claims> jwt = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
			return jwt.getPayload();
		} catch (JwtException e) {
			throw new IllegalArgumentException("허용되지 않거나 만료된 토큰입니다");
		}
	}

	public boolean validateRefreshToken(String token) {
		try {
			Jwt<?, Claims> jwt = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
			return true;
		} catch (ExpiredJwtException e) {
			log.info("Refresh token이 만료되었습니다. : {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			log.info("지원하지 않는 JWT 형식입니다. : {}", e.getMessage());
		} catch (MalformedJwtException e) {
			log.info("토큰이 올바르지 않거나 Base64 디코딩이 불가능합니다. : {}", e.getMessage());
		} catch (SignatureException e) {
			log.info("서명 검증이 실패하였습니다. : {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			log.info("메서드에 잘못된 인자가 있습니다. : {}", e.getMessage());
		}
		return false;
	}

}
