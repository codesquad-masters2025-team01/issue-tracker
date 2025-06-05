package codesquad.team01.issuetracker.issue.domain;

import java.util.List;

import codesquad.team01.issuetracker.common.exception.InvalidParameterException;
import codesquad.team01.issuetracker.issue.dto.IssueDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IssueFilter {
	CREATED("created") {
		@Override
		public IssueDto.ListQueryRequest applyFilter(IssueDto.ListQueryRequest request, Integer currentUserId) {
			return IssueDto.ListQueryRequest.builder()
				.state(request.state())
				.filter(request.filter())
				.writerId(currentUserId) // 로그인된 사용자로 강제 설정 ex) "filter=created&writerId=로그인된 사용자가 아닌 사용자의 id"
				.milestoneId(request.milestoneId())
				.labelIds(request.labelIds())
				.assigneeIds(request.assigneeIds())
				.build();
		}
	},

	ASSIGNED("assigned") {
		@Override
		public IssueDto.ListQueryRequest applyFilter(IssueDto.ListQueryRequest request, Integer currentUserId) {
			return IssueDto.ListQueryRequest.builder()
				.state(request.state())
				.filter(request.filter())
				.writerId(request.writerId())
				.milestoneId(request.milestoneId())
				.labelIds(request.labelIds())
				.assigneeIds(List.of(currentUserId))
				.build();
		}
	},

	COMMENTED("commented") {
		@Override
		public IssueDto.ListQueryRequest applyFilter(IssueDto.ListQueryRequest request, Integer currentUserId) {
			return IssueDto.ListQueryRequest.builder()
				.state(request.state())
				.filter(request.filter())
				.writerId(request.writerId())
				.milestoneId(request.milestoneId())
				.labelIds(request.labelIds())
				.assigneeIds(request.assigneeIds())
				.commentedUserId(currentUserId)
				.build();
		}
	};

	private final String value;

	public static IssueFilter fromFilterStr(String filter) {
		if (filter == null) {
			return null;
		}

		for (IssueFilter issueFilter : IssueFilter.values()) {
			if (issueFilter.value.equalsIgnoreCase(filter)) {
				return issueFilter;
			}
		}

		throw new InvalidParameterException("지원하지 않는 filter입니다: " + filter);
	}

	public abstract IssueDto.ListQueryRequest applyFilter(IssueDto.ListQueryRequest request, Integer currentUserId);
}
