package codesquad.team01.issuetracker.auth.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import codesquad.team01.issuetracker.auth.domain.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Integer> {
	Optional<RefreshToken> findByUserId(Integer userId);

	Optional<RefreshToken> findByToken(String token);

}
