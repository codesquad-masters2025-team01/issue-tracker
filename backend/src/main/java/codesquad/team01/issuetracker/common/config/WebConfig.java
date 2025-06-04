package codesquad.team01.issuetracker.common.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import codesquad.team01.issuetracker.common.resolver.CurrentUserIdArgumentResolver;
import codesquad.team01.issuetracker.common.resolver.CursorDataArgumentResolver;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {

	private final CursorDataArgumentResolver cursorDataArgumentResolver;
	private final CurrentUserIdArgumentResolver currentUserIdArgumentResolver;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
			.allowedOrigins(
				"http://localhost:3000",
				"http://localhost:5173",
				"http://43.200.183.38:5173",        // EC2 IP + 포트
				"http://issue-tracker.o-r.kr",      // 도메인
				"https://issue-tracker.o-r.kr"      // HTTPS 도메인
			)
			.allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
			.allowCredentials(true);
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(cursorDataArgumentResolver);
		resolvers.add(currentUserIdArgumentResolver);
	}
}
