package codesquad.team01.issuetracker.milestone.domain;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import codesquad.team01.issuetracker.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table("milestone")
public class Milestone extends BaseEntity {

	@Id
	private Integer id;

	private String title;

	private String description;

	private LocalDate dueDate;

	private MilestoneState state;

	@Builder
	private Milestone(String title, String description, LocalDate dueDate, MilestoneState state) {
		this.title = title;
		this.description = description;
		this.dueDate = dueDate;
		this.state = state;
	}

	public void update(String title, String description, LocalDate dueDate, MilestoneState state) {
		this.title = title;
		this.description = description;
		this.dueDate = dueDate;
		this.state = state;
	}
}
