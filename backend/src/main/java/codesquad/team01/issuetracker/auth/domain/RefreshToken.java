package codesquad.team01.issuetracker.auth.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("refresh_token")
public class RefreshToken {
	@Id
	private Integer id;
	private Integer userId;
	private String token;
}
