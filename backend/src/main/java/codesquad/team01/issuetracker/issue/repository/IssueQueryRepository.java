package codesquad.team01.issuetracker.issue.repository;

import java.util.List;

import codesquad.team01.issuetracker.common.dto.CursorDto;
import codesquad.team01.issuetracker.issue.domain.IssueState;
import codesquad.team01.issuetracker.issue.dto.IssueDto;

public interface IssueQueryRepository {

	List<IssueDto.BaseRow> findIssuesWithFilters(
		IssueState state, Integer writerId, Integer milestoneId,
		List<Integer> labelIds, List<Integer> assigneeIds, CursorDto.CursorData cursor);

	IssueDto.CountResponse countIssuesWithFilters(Integer writerId, Integer milestoneId,
		List<Integer> labelIds, List<Integer> assigneeIds);
}
