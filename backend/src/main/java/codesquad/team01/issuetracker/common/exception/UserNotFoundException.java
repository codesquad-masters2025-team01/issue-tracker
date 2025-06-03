package codesquad.team01.issuetracker.common.exception;

public class UserNotFoundException extends RuntimeException {
	public UserNotFoundException() {
		super("존재하지 않는 ID 입니다.");
	}
}
