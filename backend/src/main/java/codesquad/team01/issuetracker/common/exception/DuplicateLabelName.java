package codesquad.team01.issuetracker.common.exception;

public class DuplicateLabelName extends RuntimeException {

	private final String labelName;

	public DuplicateLabelName(String labelName) {
		super(labelName);
		this.labelName = labelName;
	}

	public String getLabelName() {
		return labelName;
	}
}
