package codesquad.team01.issuetracker.common.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import codesquad.team01.issuetracker.common.annotation.CurrentUserId;
import codesquad.team01.issuetracker.user.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(CurrentUserId.class) &&
			parameter.getParameterType().equals(Integer.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

		if (request == null) {
			log.error("HttpServletRequest를 가져올 수 없습니다.");
			return null;
		}

		User authenticatedUser = (User)request.getAttribute("authenticatedUser");

		if (authenticatedUser == null) {
			log.error("인증된 사용자 정보를 찾을 수 없습니다.");
			return null;
		}

		return authenticatedUser.getId();
	}
}
