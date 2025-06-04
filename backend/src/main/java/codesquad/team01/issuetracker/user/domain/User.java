package codesquad.team01.issuetracker.user.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import codesquad.team01.issuetracker.common.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table("users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"id", "loginId", "username"})
public class User extends BaseEntity {

	@Id
	@EqualsAndHashCode.Include
	private Integer id;

	private String loginId;

	private String username;

	private String email;

	private String password;

	private Long providerId;

	private String authProvider;

	private String profileImageUrl;

	@Builder
	public User(String loginId, String username, String email, String password, Long providerId, String authProvider,
		String profileImageUrl) {
		this.loginId = loginId;
		this.username = username;
		this.email = email;
		this.password = password;
		this.providerId = providerId;
		this.authProvider = authProvider;
		this.profileImageUrl = profileImageUrl;
	}
}
