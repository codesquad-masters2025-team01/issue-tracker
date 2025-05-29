package codesquad.team01.issuetracker.common.exception;

public class LabelNotFoundException extends RuntimeException {

	private final String labelName;

	public LabelNotFoundException(String labelName) {
		super(labelName);
		this.labelName = labelName;
	}

	public String getLabelName() {
		return labelName;
	}
}
