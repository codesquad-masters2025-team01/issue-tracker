package codesquad.team01.issuetracker.common.exception;

public class UserNotFoundException extends RuntimeException {

	private final Integer userId;

	public UserNotFoundException(Integer userId) {
		super(userId + "는 존재하지 않는 userId 입니다.");
		this.userId = userId;
	}

	public Integer getUserId() {
		return userId;
	}
}
