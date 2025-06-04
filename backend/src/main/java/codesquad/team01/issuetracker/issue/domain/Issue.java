package codesquad.team01.issuetracker.issue.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import codesquad.team01.issuetracker.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table("issue")
public class Issue extends BaseEntity {

	@Id
	private Integer id;

	private String title;
	private String content;

	private IssueState state;

	private LocalDateTime closedAt;

	private Integer writerId;

	private Integer milestoneId;

	@Builder
	private Issue(String title, String content, Integer writerId, Integer milestoneId) {

		this.title = title;
		this.content = content;
		this.state = IssueState.OPEN;
		this.writerId = writerId;
		this.milestoneId = milestoneId;
	}
}
