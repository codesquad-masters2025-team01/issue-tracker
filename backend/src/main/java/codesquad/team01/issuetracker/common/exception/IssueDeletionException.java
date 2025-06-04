package codesquad.team01.issuetracker.common.exception;

public class IssueDeletionException extends RuntimeException {

	public IssueDeletionException(String message) {
		super(message);
	}

	public IssueDeletionException(String message, Throwable cause) {
		super(message, cause);
	}
}
