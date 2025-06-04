package codesquad.team01.issuetracker.comment.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import codesquad.team01.issuetracker.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table("comment")
public class Comment extends BaseEntity {

	@Id
	private Integer id;
	private String content;
	private Integer writerId;
	private Integer issueId;

	@Builder
	private Comment(String content, Integer writerId, Integer issueId) {
		this.content = content;
		this.writerId = writerId;
		this.issueId = issueId;
	}
}
