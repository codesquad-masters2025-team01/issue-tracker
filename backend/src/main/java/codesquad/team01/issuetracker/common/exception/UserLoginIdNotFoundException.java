package codesquad.team01.issuetracker.common.exception;

public class UserLoginIdNotFoundException extends RuntimeException {

	private final String loginId;

	public UserLoginIdNotFoundException(String loginId) {
		super("'" + loginId + "'는 존재하지 않는 loginId 입니다.");
		this.loginId = loginId;
	}

	public String getLoginId() {
		return loginId;
	}
}
