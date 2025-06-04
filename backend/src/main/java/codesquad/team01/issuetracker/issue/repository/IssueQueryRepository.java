package codesquad.team01.issuetracker.issue.repository;

import java.time.LocalDateTime;
import java.util.List;

import codesquad.team01.issuetracker.common.dto.CursorDto;
import codesquad.team01.issuetracker.issue.domain.IssueState;
import codesquad.team01.issuetracker.issue.dto.IssueDto;
import codesquad.team01.issuetracker.milestone.dto.MilestoneDto;

public interface IssueQueryRepository {

	List<IssueDto.BaseRow> findIssuesWithFilters(IssueDto.ListQueryParams queryParams, CursorDto.CursorData cursor);

	IssueDto.CountResponse countIssuesWithFilters(IssueDto.CountQueryParams queryParams);

	int batchUpdateIssueStates(List<Integer> issueIds, IssueState action, LocalDateTime now);

	List<IssueDto.BatchIssueRow> findExistingIssuesByIds(List<Integer> issueIds);

	Integer createIssue(String title, String content, Integer writerId, Integer milestoneId, LocalDateTime now);

	IssueDto.DetailBaseRow findIssueById(Integer issueId);

	void addLabelsToIssue(Integer issueId, List<Integer> labelIds);

	void addAssigneesToIssue(Integer issueId, List<Integer> assigneeIds);

	void removeLabelsFromIssue(Integer issueId);

	void removeAssigneesFromIssue(Integer issueId);

	void updateIssue(Integer issueId, IssueDto.UpdateQueryParams queryParams, LocalDateTime now);

	IssueState findIssueStateByIssueId(Integer issueId);

	void deleteIssue(Integer issueId, LocalDateTime now);

	List<MilestoneDto.MilestoneIssueCountRow> countByMilestoneIds(List<Integer> milestoneIds);

}
