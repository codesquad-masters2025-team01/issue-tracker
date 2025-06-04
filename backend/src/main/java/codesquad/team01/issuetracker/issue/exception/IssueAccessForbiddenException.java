package codesquad.team01.issuetracker.issue.exception;

public class IssueAccessForbiddenException extends RuntimeException {
	public IssueAccessForbiddenException(String message) {
		super(message);
	}

	public IssueAccessForbiddenException(Integer issueId, Integer userId) {
		super(String.format("이슈 %d에 대한 권한이 없습니다. 사용자 ID: %d", issueId, userId));
	}
}
