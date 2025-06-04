package codesquad.team01.issuetracker.issue.exception;

import codesquad.team01.issuetracker.issue.domain.IssueState;

public class IssueStateException extends RuntimeException {
	private final IssueState currentState;

	public IssueStateException(String message, IssueState currentState) {
		super(message);
		this.currentState = currentState;
	}
}
