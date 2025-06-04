package codesquad.team01.issuetracker.issue.exception;

public class IssueNotFoundException extends RuntimeException {
	public IssueNotFoundException(String message) {
		super(message);
	}

	public IssueNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
