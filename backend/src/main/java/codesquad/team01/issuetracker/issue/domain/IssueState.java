package codesquad.team01.issuetracker.issue.domain;

import codesquad.team01.issuetracker.common.exception.InvalidParameterException;
import codesquad.team01.issuetracker.issue.constants.IssueConstants;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IssueState {
	OPEN("open"),
	CLOSED("closed");

	private final String value;

	public static IssueState fromStateStr(String state) {

		if (state == null) {
			return OPEN;
		}

		for (IssueState issueState : IssueState.values()) {
			if (issueState.value.equalsIgnoreCase(state)) {
				return issueState;
			}
		}

		throw new InvalidParameterException("지원하지 않는 IssueState입니다.");
	}

	public static IssueState fromActionStr(String action) {

		if (action == null) {
			throw new InvalidParameterException("action은 필수 값입니다.");
		} else if (action.equalsIgnoreCase(OPEN.name())) {
			return OPEN;
		} else if (action.equalsIgnoreCase(IssueConstants.CLOSE)) {
			return CLOSED;
		}
		throw new InvalidParameterException("지원하지 않는 action입니다.");
	}
}
