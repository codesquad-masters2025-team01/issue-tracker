package codesquad.team01.issuetracker.label.domain;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public enum LabelTextColor {

	BLACK,
	WHITE;

	public static LabelTextColor fromTextColorStr(String textColor) {
		if (textColor == null) {
			return BLACK; // 기본값
		}

		try {
			return valueOf(textColor.toUpperCase());
		} catch (IllegalArgumentException e) {
			log.warn("지원하지 않는 색상 값 '{}'이(가 전달되어 기본 색상 BLACK(#000000)으로 설정되었습니다.", textColor);
			return BLACK; // 기본값
		}
	}

}
