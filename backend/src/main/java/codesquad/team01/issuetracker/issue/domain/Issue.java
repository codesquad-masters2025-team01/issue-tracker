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

	private IssueState state = IssueState.OPEN;

	private LocalDateTime closedAt;

	private Integer writerId;

	private Integer milestoneId;

	@Builder
	private Issue(String title, String content, IssueState state,
		LocalDateTime closedAt, Integer writerId, Integer milestoneId) {

		this.title = title;
		this.content = content;
		this.state = state != null ? state : IssueState.OPEN; // 생성자에 들어가야하는지? 생성될 때 기본으로 open 아닌지? 이슈 생성 작업 때 고민해보자
		this.closedAt = closedAt;
		this.writerId = writerId;
		this.milestoneId = milestoneId;
	}
}
