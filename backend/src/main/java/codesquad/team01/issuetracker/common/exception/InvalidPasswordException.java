package codesquad.team01.issuetracker.common.exception;

public class InvalidPasswordException extends RuntimeException {

	private final String wrongPassword;

	public InvalidPasswordException(String wrongPassword) {
		super("입력한" + wrongPassword + "와 비밀번호가 일치하지 않습니다.");
		this.wrongPassword = wrongPassword;
	}

	public String getWrongPassword() {
		return wrongPassword;
	}
}
