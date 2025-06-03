package codesquad.team01.issuetracker.label.repository;

import java.util.List;

import codesquad.team01.issuetracker.label.dto.LabelDto;

public interface LabelQueryRepository {
	List<LabelDto.IssueLabelRow> findLabelsByIssueIds(List<Integer> issueIds);

	List<LabelDto.LabelFilterResponse> findLabelsForFilter();
}
