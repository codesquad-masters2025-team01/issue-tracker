package codesquad.team01.issuetracker.common.exception;

public class DuplicateMilestoneTitleException extends RuntimeException {

	private final String milestoneTitle;

	public DuplicateMilestoneTitleException(String milestoneTitle) {
		super(milestoneTitle);
		this.milestoneTitle = milestoneTitle;
	}

	String getMilestoneTitle() {
		return milestoneTitle;
	}
}
