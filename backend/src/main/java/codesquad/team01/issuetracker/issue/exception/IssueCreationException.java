package codesquad.team01.issuetracker.issue.exception;

public class IssueCreationException extends RuntimeException {
	public IssueCreationException(String message) {
		super(message);
	}

	public IssueCreationException(String message, Throwable cause) {
		super(message, cause);
	}
}
