package codesquad.team01.issuetracker.issue.exception;

import codesquad.team01.issuetracker.issue.domain.IssueState;

public class ClosedIssueModificationException extends IssueStateException {
	public ClosedIssueModificationException(String fieldName) {
		super(String.format("닫힌 이슈의 %s은 수정할 수 없습니다.", fieldName), IssueState.CLOSED);
	}
}
