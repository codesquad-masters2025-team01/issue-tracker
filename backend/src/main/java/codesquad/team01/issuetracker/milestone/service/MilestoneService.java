package codesquad.team01.issuetracker.milestone.service;

import java.util.List;

import org.springframework.stereotype.Service;

import codesquad.team01.issuetracker.milestone.dto.MilestoneDto;
import codesquad.team01.issuetracker.milestone.repository.MilestoneRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MilestoneService {

	private final MilestoneRepository milestoneRepository;

	public MilestoneDto.MilestoneFilterListResponse findMilestonesForFilter() {

		List<MilestoneDto.MilestoneFilterResponse> milestones =
			milestoneRepository.findMilestonesForFilter();

		return MilestoneDto.MilestoneFilterListResponse.builder()
			.totalCount(milestones.size())
			.milestones(milestones)
			.build();
	}
}
