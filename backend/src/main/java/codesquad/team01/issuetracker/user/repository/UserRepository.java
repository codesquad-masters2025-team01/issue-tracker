package codesquad.team01.issuetracker.user.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import codesquad.team01.issuetracker.user.domain.User;

public interface UserRepository extends CrudRepository<User, Integer>, UserQueryRepository {
	Optional<User> findByLoginId(String id);

	Optional<User> findByUsername(String username);

	Optional<User> findByEmail(String email);

	Optional<User> findByProviderIdAndAuthProvider(Long providerId, String authProvider);

}
