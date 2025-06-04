package codesquad.team01.issuetracker.comment.exception;

public class CommentCreationException extends RuntimeException {
	public CommentCreationException(String message) {
		super(message);
	}

	public CommentCreationException(String message, Throwable cause) {
		super(message, cause);
	}
}
