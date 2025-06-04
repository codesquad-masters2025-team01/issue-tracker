package codesquad.team01.issuetracker.issue.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import codesquad.team01.issuetracker.issue.domain.IssueState;
import codesquad.team01.issuetracker.issue.dto.IssueDto;
import codesquad.team01.issuetracker.label.dto.LabelDto;
import codesquad.team01.issuetracker.milestone.dto.MilestoneDto;
import codesquad.team01.issuetracker.user.dto.UserDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IssueAssembler {

	public List<IssueDto.Details> assembleIssueDetails(
		List<IssueDto.BaseRow> issues,
		List<UserDto.IssueAssigneeRow> assignees,
		List<LabelDto.IssueLabelRow> labels) {

		log.debug("이슈 {}개에 대한 상세 정보 조합", issues.size());

		// 담당자 정보를 이슈 id로 그룹화
		Map<Integer, List<UserDto.AssigneeResponse>> assigneesByIssueId = groupAssigneesByIssueId(assignees);
		log.debug("이슈별 담당자 정보 그룹화: {}개 이슈-담당자 매핑", assigneesByIssueId.size());

		// 레이블 정보를 이슈 id로 그룹화
		Map<Integer, List<LabelDto.FilterListItemResponse>> labelsByIssueId = groupLabelsByIssueId(labels);

		log.debug("이슈별 레이블 정보 그룹화: {}개 이슈-레이블 매핑", labelsByIssueId.size());

		// 각 이슈 정보에 담당자, 레이블 결합
		return issues.stream()
			.map(issue -> IssueDto.Details.builder()
				.baseInfo(issue)
				.assignees(assigneesByIssueId.getOrDefault(issue.issueId(), List.of()))
				.labels(labelsByIssueId.getOrDefault(issue.issueId(), List.of()))
				.build())
			.toList();
	}

	public IssueDto.IssueDetailsResponse assembleSingleIssueDetails(
		IssueDto.DetailBaseRow issue,
		List<LabelDto.IssueDetailLabelRow> labelRows,
		List<UserDto.IssueDetailAssigneeRow> assigneeRows,
		int commentCount,
		List<MilestoneDto.MilestoneIssueDetailCountRow> milestoneIssueCountRows
	) {
		log.debug("단일 이슈 상세 정보 조합: issueId={}", issue.issueId());

		// 아래의 toCreateResponse처럼 메서드로 처리할지? - 추후 고민
		List<LabelDto.IssueDetailLabelResponse> labels = labelRows.stream()
			.map(row -> LabelDto.IssueDetailLabelResponse.builder()
				.id(row.id())
				.name(row.name())
				.color(row.color())
				.textColor(row.textColor())
				.build())
			.toList();

		List<UserDto.IssueDetailUserResponse> assignees = assigneeRows.stream()
			.map(row -> UserDto.IssueDetailUserResponse.builder()
				.id(row.assigneeId())
				.username(row.assigneeUsername())
				.profileImageUrl(row.assigneeProfileImageUrl())
				.build()
			).toList();

		// 마일스톤 정보 생성 (이슈 개수 포함)
		MilestoneDto.IssueDetailMilestoneResponse milestone = null;
		if (issue.milestoneId() != null) {
			Map<IssueState, Integer> countByState = milestoneIssueCountRows.stream()
				.collect(Collectors.toMap(
					row -> IssueState.fromStateStr(row.state()),
					MilestoneDto.MilestoneIssueDetailCountRow::count
				));

			int openCount = countByState.getOrDefault(IssueState.OPEN, 0);
			int closedCount = countByState.getOrDefault(IssueState.CLOSED, 0);

			milestone = MilestoneDto.IssueDetailMilestoneResponse.builder()
				.id(issue.milestoneId())
				.title(issue.milestoneTitle())
				.dueDate(issue.milestoneDueDate())
				.openCount(openCount)
				.closedCount(closedCount)
				.build();
		}

		return IssueDto.SingleDetails.builder()
			.issue(issue)
			.labels(labels)
			.assignees(assignees)
			.commentCount(commentCount)
			.build().toCreateResponse(milestone);
	}

	// 담당자 정보 이슈 id로 그룹화
	private Map<Integer, List<UserDto.AssigneeResponse>> groupAssigneesByIssueId(

		List<UserDto.IssueAssigneeRow> assignees) {
		return assignees.stream()
			.collect(Collectors.groupingBy(
				UserDto.IssueAssigneeRow::issueId,
				Collectors.mapping(
					row -> UserDto.AssigneeResponse.builder()
						.id(row.assigneeId())
						.profileImageUrl(row.assigneeProfileImageUrl())
						.build(),
					Collectors.toList()
				)
			));
	}

	// 레이블 정보 이슈 id로 그룹화
	private Map<Integer, List<LabelDto.FilterListItemResponse>> groupLabelsByIssueId(
		List<LabelDto.IssueLabelRow> labels) {

		return labels.stream()
			.collect(Collectors.groupingBy(
				LabelDto.IssueLabelRow::issueId,
				Collectors.mapping(
					row -> LabelDto.FilterListItemResponse.builder()
						.id(row.labelId())
						.name(row.labelName())
						.color(row.labelColor())
						.textColor(row.labelTextColor().name())
						.build(),
					Collectors.toList()
				)
			));
	}
}
