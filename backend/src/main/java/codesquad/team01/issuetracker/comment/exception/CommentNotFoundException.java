package codesquad.team01.issuetracker.comment.exception;

public class CommentNotFoundException extends RuntimeException {
	public CommentNotFoundException(String message) {
		super(message);
	}

	public CommentNotFoundException(Integer commentId) {
		super("댓글을 찾을 수 없습니다: " + commentId);
	}
}
