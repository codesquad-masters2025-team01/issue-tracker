package codesquad.team01.issuetracker.issue.service;

import codesquad.team01.issuetracker.issue.dto.IssueDto;
import codesquad.team01.issuetracker.label.dto.LabelDto;
import codesquad.team01.issuetracker.user.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class IssueAssembler {

    public List<IssueDto.Details> assembleIssueDetails(
            List<IssueDto.BaseRow> issues,
            List<IssueDto.AssigneeRow> allAssignees,
            List<IssueDto.LabelRow> allLabels) {

        log.debug("이슈 {}개에 대한 상세 정보 조합", issues.size());

        // 담당자 정보를 이슈 id로 그룹화
        Map<Long, List<UserDto.SimpleResponse>> assigneesByIssueId = groupAssigneesByIssueId(allAssignees);
        log.debug("이슈별 담당자 정보 그룹화: {}개 이슈-담당자 매핑", assigneesByIssueId.size());

        // 레이블 정보를 이슈 id로 그룹화
        Map<Long, List<LabelDto.SimpleResponse>> labelsByIssueId = groupLabelsByIssueId(allLabels);
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

    // 담당자 정보 이슈 id로 그룹화
    private Map<Long, List<UserDto.SimpleResponse>> groupAssigneesByIssueId(List<IssueDto.AssigneeRow> assignees) {
        return assignees.stream()
                .collect(Collectors.groupingBy(
                        IssueDto.AssigneeRow::issueId,
                        Collectors.mapping(
                                row -> UserDto.SimpleResponse.builder()
                                        .id(row.assigneeId())
                                        .profileImageUrl(row.assigneeProfileImageUrl())
                                        .build(),
                                Collectors.toList()
                        )
                ));
    }

    // 레이블 정보 이슈 id로 그룹화
    private Map<Long, List<LabelDto.SimpleResponse>> groupLabelsByIssueId(List<IssueDto.LabelRow> labels) {
        return labels.stream()
                .collect(Collectors.groupingBy(
                        IssueDto.LabelRow::issueId,
                        Collectors.mapping(
                                row -> LabelDto.SimpleResponse.builder()
                                        .id(row.labelId())
                                        .name(row.labelName())
                                        .color(row.labelColor())
                                        .textColor(row.labelTextColor())
                                        .build(),
                                Collectors.toList()
                        )
                ));
    }
}