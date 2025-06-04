package codesquad.team01.issuetracker.milestone.exception;

public class MilestoneNotFoundException extends RuntimeException {

	private final int id;

	public MilestoneNotFoundException(int id) {
		super(id + "를 찾을 수 없습니다.");
		this.id = id;
	}

	public int getId() {
		return id;
	}

}
