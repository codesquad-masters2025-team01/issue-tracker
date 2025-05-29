package codesquad.team01.issuetracker.common.exception;

public class LabelNotFoundException extends RuntimeException {

	private final String name;

	public LabelNotFoundException(String name) {
		super("'" + name + "' 레이블을 찾을 수 없습니다.");
		this.name = name;
	}
}
