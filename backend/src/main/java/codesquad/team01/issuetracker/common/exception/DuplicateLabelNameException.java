package codesquad.team01.issuetracker.common.exception;

public class DuplicateLabelNameException extends RuntimeException {

	private final String labelName;

	public DuplicateLabelNameException(String labelName) {
		super(labelName);
		this.labelName = labelName;
	}

	public String getLabelName() {
		return labelName;
	}
}
