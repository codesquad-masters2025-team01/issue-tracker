package codesquad.team01.issuetracker.milestone.repository;

import java.util.List;

import codesquad.team01.issuetracker.milestone.dto.MilestoneDto;

public interface MilestoneQueryRepository {
	List<MilestoneDto.MilestoneFilterResponse> findMilestonesForFilter();

}
