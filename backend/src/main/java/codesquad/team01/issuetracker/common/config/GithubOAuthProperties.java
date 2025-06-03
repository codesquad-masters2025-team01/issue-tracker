package codesquad.team01.issuetracker.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

// application.yaml 의 github.oauth 아래에 정의된 모든 프로퍼티를 이 클래스의 필드와 자동으로 매핑
@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "github.oauth")
public class GithubOAuthProperties {
	private String clientId;
	private String clientSecret;
	private String authorizeUri;
	private String tokenUri;
	private String userInfoUri;
	private String userEmailUri;
	private String redirectUri;
	private String scope;
}
