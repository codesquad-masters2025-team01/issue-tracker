package codesquad.team01.issuetracker.comment.exception;

public class CommentAccessForbiddenException extends RuntimeException {
	public CommentAccessForbiddenException(String message) {
		super(message);
	}

	public CommentAccessForbiddenException(Integer commentId, Integer userId) {
		super(String.format("댓글 %d에 대한 권한이 없습니다. 사용자 ID: %d", commentId, userId));
	}
}
