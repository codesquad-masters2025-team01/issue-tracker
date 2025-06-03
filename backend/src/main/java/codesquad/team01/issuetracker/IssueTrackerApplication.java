package codesquad.team01.issuetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import codesquad.team01.issuetracker.common.config.GithubOAuthProperties;

@EnableConfigurationProperties(GithubOAuthProperties.class)
@SpringBootApplication
public class IssueTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(IssueTrackerApplication.class, args);
	}
}
