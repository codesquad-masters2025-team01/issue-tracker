package codesquad.team01.issuetracker.common.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.team01.issuetracker.common.dto.CursorDto;
import codesquad.team01.issuetracker.common.exception.CursorException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class CursorEncoder {

	private final ObjectMapper objectMapper;

	public String encode(CursorDto.CursorData cursor) {
		if (cursor == null) {
			return null;
		}

		try {
			String json = objectMapper.writeValueAsString(cursor);
			return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
		} catch (JsonProcessingException e) {
			throw new CursorException("커서 인코딩 실패: " + e.getMessage());
		}
	}
}
