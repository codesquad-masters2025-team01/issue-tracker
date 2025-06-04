package codesquad.team01.issuetracker.milestone.domain;

import codesquad.team01.issuetracker.common.exception.InvalidParameterException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MilestoneState {
	OPEN("open"),
	CLOSED("closed");

	private final String value;

	public static MilestoneState fromStateStr(String state) {
		if (state == null) {
			return OPEN;
		}

		for (MilestoneState milestoneState : MilestoneState.values()) {
			if (milestoneState.value.equalsIgnoreCase(state)) {
				return milestoneState;
			}
		}
		throw new InvalidParameterException("지원하지 않는 MilestoneState 입니다.");
	}
}
