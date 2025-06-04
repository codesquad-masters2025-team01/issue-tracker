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
			.allowedOrigins("http://localhost:5173")
			.allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
			.allowCredentials(true);
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(cursorDataArgumentResolver);
		resolvers.add(currentUserIdArgumentResolver);
	}
}
