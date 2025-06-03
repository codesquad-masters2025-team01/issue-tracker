package codesquad.team01.issuetracker.common.resolver;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.team01.issuetracker.common.annotation.CursorParam;
import codesquad.team01.issuetracker.common.dto.CursorDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class CursorDataArgumentResolver implements HandlerMethodArgumentResolver {

	private final ObjectMapper objectMapper;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(CursorParam.class) &&
			parameter.getParameterType().equals(CursorDto.CursorData.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter,
		ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest,
		WebDataBinderFactory binderFactory) throws Exception {

		CursorParam annotation = parameter.getParameterAnnotation(CursorParam.class);
		String paramName = annotation.value();
		String cursor = webRequest.getParameter(paramName);

		return decodeCursor(cursor);
	}

	private CursorDto.CursorData decodeCursor(String cursor) {
		if (cursor == null || cursor.isBlank()) {
			return null;
		}

		try {
			String decoded = new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8);
			return objectMapper.readValue(decoded, CursorDto.CursorData.class);
		} catch (Exception e) {
			log.warn("커서 디코딩 실패: {}", e.getMessage());
			return null; // 디코딩 실패 시 첫 페이지
		}
	}
}
