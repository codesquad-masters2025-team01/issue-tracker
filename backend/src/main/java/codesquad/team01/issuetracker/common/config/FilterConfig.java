package codesquad.team01.issuetracker.common.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.team01.issuetracker.auth.filter.JwtAccessTokenAuthenticationFilter;
import codesquad.team01.issuetracker.auth.filter.JwtExceptionFilter;
import codesquad.team01.issuetracker.auth.util.UserAuthorizationJwtManager;
import codesquad.team01.issuetracker.user.repository.UserRepository;

@Configuration
public class FilterConfig {

	@Bean
	public FilterRegistrationBean<JwtExceptionFilter> jwtExceptionFilter(ObjectMapper objectMapper) {
		var bean = new FilterRegistrationBean<JwtExceptionFilter>();
		bean.setFilter(new JwtExceptionFilter(objectMapper));
		bean.addUrlPatterns("/api/*");   // 이 경로로 들어오는
		bean.setOrder(1); //예외 필터 먼저 실행
		return bean;
	}

	@Bean
	public FilterRegistrationBean<JwtAccessTokenAuthenticationFilter> jwtAuthenticationFilter(
		UserAuthorizationJwtManager jwtManager,
		UserRepository userRepository) {
		var bean = new FilterRegistrationBean<JwtAccessTokenAuthenticationFilter>();
		bean.setFilter(new JwtAccessTokenAuthenticationFilter(jwtManager, userRepository));
		bean.addUrlPatterns("/api/*"); // -> /api/으로 시작하는 경로만 필터 적용
		bean.setOrder(2);                   // 예외 필터 다음으로 실행
		return bean;
	}
}
